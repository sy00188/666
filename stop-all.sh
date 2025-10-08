#!/bin/bash

# =====================================================
# 档案管理系统 - 一键停止脚本
# 版本: v1.0
# 说明: 停止MySQL、后端、前端服务
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

# 停止进程
stop_process() {
    local pid_file=$1
    local service_name=$2
    
    if [ -f "$pid_file" ]; then
        local pid=$(cat "$pid_file")
        if kill -0 "$pid" 2>/dev/null; then
            log_info "停止 $service_name (PID: $pid)..."
            kill "$pid"
            
            # 等待进程结束
            local count=0
            while kill -0 "$pid" 2>/dev/null && [ $count -lt 10 ]; do
                sleep 1
                count=$((count + 1))
            done
            
            # 如果进程仍在运行，强制杀死
            if kill -0 "$pid" 2>/dev/null; then
                log_warning "强制停止 $service_name..."
                kill -9 "$pid" 2>/dev/null || true
            fi
            
            log_success "$service_name 已停止"
        else
            log_warning "$service_name 进程不存在 (PID: $pid)"
        fi
        
        # 删除PID文件
        rm -f "$pid_file"
    else
        log_info "$service_name PID文件不存在，可能未运行"
    fi
}

# 停止端口上的进程
stop_port_process() {
    local port=$1
    local service_name=$2
    
    local pid=$(lsof -ti:$port 2>/dev/null || true)
    if [ -n "$pid" ]; then
        log_info "停止端口 $port 上的 $service_name (PID: $pid)..."
        kill "$pid" 2>/dev/null || true
        
        # 等待进程结束
        sleep 2
        
        # 检查是否还在运行
        if lsof -ti:$port >/dev/null 2>&1; then
            log_warning "强制停止端口 $port 上的进程..."
            kill -9 $(lsof -ti:$port) 2>/dev/null || true
        fi
        
        log_success "$service_name 已停止"
    else
        log_info "端口 $port 上没有运行 $service_name"
    fi
}

# 停止前端服务
stop_frontend() {
    log_info "停止前端服务..."
    
    # 通过PID文件停止
    stop_process "frontend.pid" "前端服务"
    
    # 通过端口停止（备用方案）
    stop_port_process 5173 "前端服务"
}

# 停止后端服务
stop_backend() {
    log_info "停止后端服务..."
    
    # 通过PID文件停止
    stop_process "backend.pid" "后端服务"
    
    # 通过端口停止（备用方案）
    stop_port_process 8080 "后端服务"
}

# 停止MySQL服务
stop_mysql() {
    log_info "停止MySQL服务..."
    
    # 检查MySQL是否在运行
    if ! pgrep -f mysqld > /dev/null; then
        log_info "MySQL服务未运行"
        return 0
    fi
    
    # 尝试不同的MySQL停止方式
    if [ -f "/usr/local/mysql/support-files/mysql.server" ]; then
        sudo /usr/local/mysql/support-files/mysql.server stop
    elif command -v mysqladmin &> /dev/null; then
        mysqladmin -u root -p shutdown
    elif command -v brew &> /dev/null && brew services list | grep mysql; then
        brew services stop mysql
    else
        # 直接杀死MySQL进程
        log_warning "使用强制方式停止MySQL..."
        sudo pkill -f mysqld || true
    fi
    
    # 等待MySQL停止
    local count=0
    while pgrep -f mysqld > /dev/null && [ $count -lt 10 ]; do
        sleep 1
        count=$((count + 1))
    done
    
    if pgrep -f mysqld > /dev/null; then
        log_warning "MySQL可能仍在运行"
    else
        log_success "MySQL服务已停止"
    fi
}

# 清理日志文件
cleanup_logs() {
    log_info "清理日志文件..."
    
    # 清理应用日志
    [ -f "backend.log" ] && rm -f backend.log && log_info "删除后端日志文件"
    [ -f "frontend.log" ] && rm -f frontend.log && log_info "删除前端日志文件"
    
    # 清理PID文件
    [ -f "backend.pid" ] && rm -f backend.pid
    [ -f "frontend.pid" ] && rm -f frontend.pid
    
    log_success "日志文件清理完成"
}

# 显示停止状态
show_status() {
    echo ""
    log_info "=== 服务停止状态 ==="
    echo ""
    
    # MySQL状态
    if pgrep -f mysqld > /dev/null; then
        log_warning "✗ MySQL数据库: 仍在运行"
    else
        log_success "✓ MySQL数据库: 已停止"
    fi
    
    # 后端状态
    if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warning "✗ 后端服务: 仍在运行"
    else
        log_success "✓ 后端服务: 已停止"
    fi
    
    # 前端状态
    if lsof -Pi :5173 -sTCP:LISTEN -t >/dev/null 2>&1; then
        log_warning "✗ 前端服务: 仍在运行"
    else
        log_success "✓ 前端服务: 已停止"
    fi
    
    echo ""
}

# 主函数
main() {
    echo ""
    log_info "=== 档案管理系统停止脚本 ==="
    echo ""
    
    # 按顺序停止服务（前端 -> 后端 -> 数据库）
    stop_frontend
    stop_backend
    
    # 询问是否停止MySQL
    echo ""
    read -p "是否停止MySQL数据库服务? (y/N): " -n 1 -r
    echo ""
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        stop_mysql
    else
        log_info "跳过MySQL停止"
    fi
    
    # 清理文件
    cleanup_logs
    
    # 显示最终状态
    show_status
    
    log_success "服务停止完成！"
}

# 执行主函数
main "$@"