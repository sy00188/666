#!/bin/bash

# =====================================================
# 档案管理系统 - Docker 启动脚本
# 版本: v1.0
# 说明: 使用Docker Compose启动所有服务
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

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        log_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null && ! docker compose version &> /dev/null; then
        log_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    log_success "Docker环境检查通过"
}

# 检查Docker服务状态
check_docker_service() {
    if ! docker info &> /dev/null; then
        log_error "Docker服务未运行，请启动Docker"
        exit 1
    fi
    
    log_success "Docker服务运行正常"
}

# 创建必要的目录
create_directories() {
    log_info "创建必要的目录..."
    
    mkdir -p data/mysql
    mkdir -p data/redis
    mkdir -p logs/backend
    mkdir -p logs/nginx
    mkdir -p upload
    mkdir -p mysql-config
    
    log_success "目录创建完成"
}

# 创建MySQL配置文件
create_mysql_config() {
    log_info "创建MySQL配置文件..."
    
    cat > mysql-config/my.cnf << EOF
[mysqld]
# 基本设置
default-storage-engine=INNODB
character-set-server=utf8mb4
collation-server=utf8mb4_unicode_ci
init_connect='SET NAMES utf8mb4'

# 性能优化
max_connections=200
innodb_buffer_pool_size=256M
innodb_log_file_size=64M
innodb_flush_log_at_trx_commit=2
innodb_flush_method=O_DIRECT

# 安全设置
skip-name-resolve
sql_mode=STRICT_TRANS_TABLES,NO_ZERO_DATE,NO_ZERO_IN_DATE,ERROR_FOR_DIVISION_BY_ZERO

# 时区设置
default-time-zone='+08:00'

[mysql]
default-character-set=utf8mb4

[client]
default-character-set=utf8mb4
EOF
    
    log_success "MySQL配置文件创建完成"
}

# 启动服务
start_services() {
    log_info "启动Docker服务..."
    
    # 使用环境变量文件
    if [ -f ".env.docker" ]; then
        export $(cat .env.docker | grep -v '^#' | xargs)
    fi
    
    # 检查是否使用docker compose或docker-compose
    if docker compose version &> /dev/null; then
        COMPOSE_CMD="docker compose"
    else
        COMPOSE_CMD="docker-compose"
    fi
    
    # 拉取最新镜像
    log_info "拉取Docker镜像..."
    $COMPOSE_CMD pull
    
    # 构建自定义镜像
    log_info "构建应用镜像..."
    $COMPOSE_CMD build
    
    # 启动服务
    log_info "启动所有服务..."
    $COMPOSE_CMD up -d
    
    log_success "Docker服务启动完成"
}

# 等待服务就绪
wait_for_services() {
    log_info "等待服务启动..."
    
    # 等待MySQL
    log_info "等待MySQL启动..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if docker exec archive_mysql mysqladmin ping -h localhost -u root -proot123456 &> /dev/null; then
            log_success "MySQL已就绪"
            break
        fi
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -le 0 ]; then
        log_error "MySQL启动超时"
        return 1
    fi
    
    # 等待后端
    log_info "等待后端服务启动..."
    timeout=120
    while [ $timeout -gt 0 ]; do
        if curl -f http://localhost:8080/auth/captcha &> /dev/null; then
            log_success "后端服务已就绪"
            break
        fi
        sleep 3
        timeout=$((timeout - 3))
    done
    
    if [ $timeout -le 0 ]; then
        log_error "后端服务启动超时"
        return 1
    fi
    
    # 等待前端
    log_info "等待前端服务启动..."
    timeout=60
    while [ $timeout -gt 0 ]; do
        if curl -f http://localhost:80 &> /dev/null; then
            log_success "前端服务已就绪"
            break
        fi
        sleep 2
        timeout=$((timeout - 2))
    done
    
    if [ $timeout -le 0 ]; then
        log_error "前端服务启动超时"
        return 1
    fi
}

# 显示服务状态
show_status() {
    echo ""
    log_info "=== Docker 服务状态 ==="
    
    # 使用docker compose或docker-compose
    if docker compose version &> /dev/null; then
        docker compose ps
    else
        docker-compose ps
    fi
    
    echo ""
    log_success "=== 服务访问地址 ==="
    echo "  前端应用: http://localhost:80"
    echo "  后端API: http://localhost:8080"
    echo "  MySQL: localhost:3306"
    echo "  Redis: localhost:6379"
    echo ""
    
    log_info "=== 管理命令 ==="
    echo "  查看日志: docker-compose logs -f [service_name]"
    echo "  停止服务: docker-compose down"
    echo "  重启服务: docker-compose restart [service_name]"
    echo "  进入容器: docker exec -it [container_name] /bin/bash"
    echo ""
}

# 主函数
main() {
    echo ""
    log_info "=== 档案管理系统 Docker 启动脚本 ==="
    echo ""
    
    # 检查环境
    check_docker
    check_docker_service
    
    # 准备环境
    create_directories
    create_mysql_config
    
    # 启动服务
    start_services
    
    # 等待服务就绪
    if wait_for_services; then
        show_status
        log_success "所有服务启动完成！"
    else
        log_error "部分服务启动失败，请检查日志"
        exit 1
    fi
}

# 执行主函数
main "$@"