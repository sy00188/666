#!/bin/bash

# =====================================================
# 档案管理系统 - 一键启动脚本
# 版本: v1.0
# 说明: 自动启动MySQL、后端、前端服务
# =====================================================

set -e  # 遇到错误立即退出

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 日志函数
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查命令是否存在
check_command() {
    if ! command -v $1 &> /dev/null; then
        log_error "$1 命令未找到，请先安装"
        return 1
    fi
    return 0
}

# 检查端口是否被占用
check_port() {
    local port=$1
    if lsof -Pi :$port -sTCP:LISTEN -t >/dev/null 2>&1; then
        return 0  # 端口被占用
    else
        return 1  # 端口空闲
    fi
}

# 等待端口启动
wait_for_port() {
    local port=$1
    local service_name=$2
    local max_attempts=30
    local attempt=1
    
    log_info "等待 $service_name 在端口 $port 启动..."
    
    while [ $attempt -le $max_attempts ]; do
        if check_port $port; then
            log_success "$service_name 已在端口 $port 启动"
            return 0
        fi
        
        echo -n "."
        sleep 2
        attempt=$((attempt + 1))
    done
    
    log_error "$service_name 启动超时"
    return 1
}

# 启动MySQL数据库
start_mysql() {
    log_info "检查MySQL服务状态..."
    
    # 检查MySQL是否已经运行
    if pgrep -f mysqld > /dev/null; then
        log_success "MySQL服务已在运行"
        return 0
    fi
    
    log_info "启动MySQL服务..."
    
    # 尝试不同的MySQL启动方式
    if [ -f "/usr/local/mysql/support-files/mysql.server" ]; then
        sudo /usr/local/mysql/support-files/mysql.server start
    elif command -v mysqld_safe &> /dev/null; then
        sudo mysqld_safe --user=mysql --daemonize
    elif command -v brew &> /dev/null && brew services list | grep mysql; then
        brew services start mysql
    else
        log_error "无法找到MySQL启动方式"
        return 1
    fi
    
    # 等待MySQL启动
    sleep 5
    
    if pgrep -f mysqld > /dev/null; then
        log_success "MySQL服务启动成功"
        return 0
    else
        log_error "MySQL服务启动失败"
        return 1
    fi
}

# 初始化数据库
init_database() {
    log_info "检查数据库是否需要初始化..."
    
    # 检查数据库是否存在
    if /usr/local/mysql/bin/mysql -u root -p -e "USE archive_management_system;" 2>/dev/null; then
        log_success "数据库已存在，跳过初始化"
        return 0
    fi
    
    log_info "初始化数据库..."
    
    # 执行数据库初始化脚本
    if [ -f "01_database_init.sql" ]; then
        /usr/local/mysql/bin/mysql -u root -p < 01_database_init.sql
        log_success "数据库初始化完成"
    else
        log_warning "数据库初始化脚本不存在"
    fi
    
    # 执行触发器脚本
    if [ -f "03_triggers.sql" ]; then
        /usr/local/mysql/bin/mysql -u root -p archive_management_system < 03_triggers.sql
        log_success "数据库触发器创建完成"
    fi
}

# 启动后端服务
start_backend() {
    log_info "启动后端服务..."
    
    # 检查后端端口是否已被占用
    if check_port 8080; then
        log_warning "端口8080已被占用，可能后端服务已在运行"
        return 0
    fi
    
    # 检查启动脚本是否存在
    if [ ! -f "start-backend.sh" ]; then
        log_error "后端启动脚本不存在"
        return 1
    fi
    
    # 后台启动后端服务
    nohup ./start-backend.sh > backend.log 2>&1 &
    echo $! > backend.pid
    
    # 等待后端服务启动
    if wait_for_port 8080 "后端服务"; then
        log_success "后端服务启动成功 (PID: $(cat backend.pid))"
        return 0
    else
        log_error "后端服务启动失败"
        return 1
    fi
}

# 启动前端服务
start_frontend() {
    log_info "启动前端服务..."
    
    # 检查前端端口是否已被占用
    if check_port 5173; then
        log_warning "端口5173已被占用，可能前端服务已在运行"
        return 0
    fi
    
    # 检查前端目录是否存在
    if [ ! -d "frontend" ]; then
        log_error "前端目录不存在"
        return 1
    fi
    
    # 进入前端目录并启动服务
    cd frontend
    
    # 检查依赖是否已安装
    if [ ! -d "node_modules" ]; then
        log_info "安装前端依赖..."
        npm install
    fi
    
    # 后台启动前端服务
    nohup npm run dev > ../frontend.log 2>&1 &
    echo $! > ../frontend.pid
    
    cd ..
    
    # 等待前端服务启动
    if wait_for_port 5173 "前端服务"; then
        log_success "前端服务启动成功 (PID: $(cat frontend.pid))"
        return 0
    else
        log_error "前端服务启动失败"
        return 1
    fi
}

# 显示服务状态
show_status() {
    echo ""
    log_info "=== 服务启动完成 ==="
    echo ""
    
    # MySQL状态
    if pgrep -f mysqld > /dev/null; then
        log_success "✓ MySQL数据库: 运行中"
    else
        log_error "✗ MySQL数据库: 未运行"
    fi
    
    # 后端状态
    if check_port 8080; then
        log_success "✓ 后端服务: http://localhost:8080"
    else
        log_error "✗ 后端服务: 未运行"
    fi
    
    # 前端状态
    if check_port 5173; then
        log_success "✓ 前端服务: http://localhost:5173"
    else
        log_error "✗ 前端服务: 未运行"
    fi
    
    echo ""
    log_info "使用 './stop-all.sh' 停止所有服务"
    log_info "使用 './check-status.sh' 检查服务状态"
    echo ""
}

# 主函数
main() {
    echo ""
    log_info "=== 档案管理系统启动脚本 ==="
    echo ""
    
    # 检查必要的命令
    log_info "检查系统环境..."
    
    # 启动服务
    start_mysql || exit 1
    init_database || log_warning "数据库初始化可能失败，请检查"
    start_backend || exit 1
    start_frontend || exit 1
    
    # 显示最终状态
    show_status
    
    log_success "所有服务启动完成！"
}

# 执行主函数
main "$@"