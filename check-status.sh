#!/bin/bash

# =====================================================
# 档案管理系统 - 服务状态检查脚本
# 版本: v1.0
# 说明: 检查MySQL、后端、前端服务状态
# =====================================================

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
CYAN='\033[0;36m'
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

log_header() {
    echo -e "${PURPLE}$1${NC}"
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

# 获取端口进程信息
get_port_info() {
    local port=$1
    local pid=$(lsof -ti:$port 2>/dev/null)
    if [ -n "$pid" ]; then
        local cmd=$(ps -p $pid -o comm= 2>/dev/null)
        local user=$(ps -p $pid -o user= 2>/dev/null)
        echo "PID: $pid, 用户: $user, 命令: $cmd"
    else
        echo "无进程信息"
    fi
}

# 测试HTTP连接
test_http() {
    local url=$1
    local timeout=${2:-5}
    
    if curl -s --connect-timeout $timeout "$url" >/dev/null 2>&1; then
        return 0
    else
        return 1
    fi
}

# 检查MySQL状态
check_mysql() {
    log_header "=== MySQL 数据库状态 ==="
    
    # 检查MySQL进程
    if pgrep -f mysqld > /dev/null; then
        local mysql_pid=$(pgrep -f mysqld)
        log_success "✓ MySQL进程运行中 (PID: $mysql_pid)"
        
        # 检查MySQL端口
        if check_port 3306; then
            log_success "✓ MySQL端口3306监听中"
            echo "  $(get_port_info 3306)"
        else
            log_warning "✗ MySQL端口3306未监听"
        fi
        
        # 尝试连接MySQL
        if /usr/local/mysql/bin/mysql -u root -p -e "SELECT 1;" >/dev/null 2>&1; then
            log_success "✓ MySQL数据库连接正常"
        else
            log_warning "✗ MySQL数据库连接失败（可能需要密码）"
        fi
        
        # 检查目标数据库
        if /usr/local/mysql/bin/mysql -u root -p -e "USE archive_management_system;" >/dev/null 2>&1; then
            log_success "✓ 档案管理数据库存在"
        else
            log_warning "✗ 档案管理数据库不存在"
        fi
        
    else
        log_error "✗ MySQL进程未运行"
    fi
    
    echo ""
}

# 检查后端服务状态
check_backend() {
    log_header "=== 后端服务状态 ==="
    
    # 检查后端端口
    if check_port 8080; then
        log_success "✓ 后端服务端口8080运行中"
        echo "  $(get_port_info 8080)"
        
        # 测试后端API
        if test_http "http://localhost:8080/auth/captcha" 3; then
            log_success "✓ 后端API响应正常"
        else
            log_warning "✗ 后端API无响应"
        fi
        
        # 检查PID文件
        if [ -f "backend.pid" ]; then
            local pid=$(cat backend.pid)
            if kill -0 "$pid" 2>/dev/null; then
                log_success "✓ 后端PID文件有效 (PID: $pid)"
            else
                log_warning "✗ 后端PID文件无效"
            fi
        else
            log_info "- 后端PID文件不存在"
        fi
        
    else
        log_error "✗ 后端服务端口8080未运行"
        
        # 检查是否有残留的PID文件
        if [ -f "backend.pid" ]; then
            log_warning "- 发现残留的后端PID文件"
        fi
    fi
    
    # 检查后端日志
    if [ -f "backend.log" ]; then
        local log_size=$(wc -l < backend.log)
        log_info "- 后端日志文件存在 ($log_size 行)"
        
        # 显示最后几行日志
        echo "  最近日志:"
        tail -3 backend.log 2>/dev/null | sed 's/^/    /'
    else
        log_info "- 后端日志文件不存在"
    fi
    
    echo ""
}

# 检查前端服务状态
check_frontend() {
    log_header "=== 前端服务状态 ==="
    
    # 检查前端端口
    if check_port 5173; then
        log_success "✓ 前端服务端口5173运行中"
        echo "  $(get_port_info 5173)"
        
        # 测试前端页面
        if test_http "http://localhost:5173" 3; then
            log_success "✓ 前端页面响应正常"
        else
            log_warning "✗ 前端页面无响应"
        fi
        
        # 检查PID文件
        if [ -f "frontend.pid" ]; then
            local pid=$(cat frontend.pid)
            if kill -0 "$pid" 2>/dev/null; then
                log_success "✓ 前端PID文件有效 (PID: $pid)"
            else
                log_warning "✗ 前端PID文件无效"
            fi
        else
            log_info "- 前端PID文件不存在"
        fi
        
    else
        log_error "✗ 前端服务端口5173未运行"
        
        # 检查是否有残留的PID文件
        if [ -f "frontend.pid" ]; then
            log_warning "- 发现残留的前端PID文件"
        fi
    fi
    
    # 检查前端目录和依赖
    if [ -d "frontend" ]; then
        log_success "✓ 前端目录存在"
        
        if [ -d "frontend/node_modules" ]; then
            log_success "✓ 前端依赖已安装"
        else
            log_warning "✗ 前端依赖未安装"
        fi
    else
        log_error "✗ 前端目录不存在"
    fi
    
    # 检查前端日志
    if [ -f "frontend.log" ]; then
        local log_size=$(wc -l < frontend.log)
        log_info "- 前端日志文件存在 ($log_size 行)"
        
        # 显示最后几行日志
        echo "  最近日志:"
        tail -3 frontend.log 2>/dev/null | sed 's/^/    /'
    else
        log_info "- 前端日志文件不存在"
    fi
    
    echo ""
}

# 检查系统资源
check_system() {
    log_header "=== 系统资源状态 ==="
    
    # 检查磁盘空间
    local disk_usage=$(df -h . | awk 'NR==2 {print $5}' | sed 's/%//')
    if [ "$disk_usage" -lt 90 ]; then
        log_success "✓ 磁盘空间充足 (已使用: ${disk_usage}%)"
    else
        log_warning "✗ 磁盘空间不足 (已使用: ${disk_usage}%)"
    fi
    
    # 检查内存使用
    if command -v free >/dev/null 2>&1; then
        local mem_usage=$(free | awk 'NR==2{printf "%.0f", $3*100/$2}')
        if [ "$mem_usage" -lt 90 ]; then
            log_success "✓ 内存使用正常 (已使用: ${mem_usage}%)"
        else
            log_warning "✗ 内存使用过高 (已使用: ${mem_usage}%)"
        fi
    else
        log_info "- 无法检查内存使用情况"
    fi
    
    # 检查负载
    if command -v uptime >/dev/null 2>&1; then
        local load=$(uptime | awk -F'load average:' '{print $2}' | awk '{print $1}' | sed 's/,//')
        log_info "- 系统负载: $load"
    fi
    
    echo ""
}

# 显示服务摘要
show_summary() {
    log_header "=== 服务状态摘要 ==="
    
    local mysql_status="❌"
    local backend_status="❌"
    local frontend_status="❌"
    
    # MySQL状态
    if pgrep -f mysqld > /dev/null; then
        mysql_status="✅"
    fi
    
    # 后端状态
    if check_port 8080; then
        backend_status="✅"
    fi
    
    # 前端状态
    if check_port 5173; then
        frontend_status="✅"
    fi
    
    echo -e "${CYAN}┌─────────────────────────────────────┐${NC}"
    echo -e "${CYAN}│${NC}  服务名称        状态      地址        ${CYAN}│${NC}"
    echo -e "${CYAN}├─────────────────────────────────────┤${NC}"
    echo -e "${CYAN}│${NC}  MySQL数据库     $mysql_status      localhost:3306  ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  后端服务        $backend_status      localhost:8080  ${CYAN}│${NC}"
    echo -e "${CYAN}│${NC}  前端服务        $frontend_status      localhost:5173  ${CYAN}│${NC}"
    echo -e "${CYAN}└─────────────────────────────────────┘${NC}"
    
    echo ""
    
    # 提供操作建议
    if [[ "$mysql_status" == "❌" || "$backend_status" == "❌" || "$frontend_status" == "❌" ]]; then
        log_warning "部分服务未运行，建议执行:"
        echo "  ./start-all.sh    # 启动所有服务"
    else
        log_success "所有服务运行正常！"
        echo "  前端访问: http://localhost:5173"
        echo "  后端API: http://localhost:8080"
    fi
    
    echo ""
    log_info "其他可用命令:"
    echo "  ./stop-all.sh     # 停止所有服务"
    echo "  ./check-status.sh # 重新检查状态"
}

# 主函数
main() {
    echo ""
    log_header "=== 档案管理系统状态检查 ==="
    echo ""
    
    # 检查各个服务
    check_mysql
    check_backend
    check_frontend
    check_system
    
    # 显示摘要
    show_summary
}

# 执行主函数
main "$@"