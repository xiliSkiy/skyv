#!/bin/bash

# SkyEye 智能监控系统部署脚本
# ================================

set -e

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

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
    # 检查Docker
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装，请先安装 Docker"
        exit 1
    fi
    
    # 检查Docker Compose
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装，请先安装 Docker Compose"
        exit 1
    fi
    
    # 检查Git
    if ! command -v git &> /dev/null; then
        log_error "Git 未安装，请先安装 Git"
        exit 1
    fi
    
    log_success "系统依赖检查完成"
}

# 创建必要的目录
create_directories() {
    log_info "创建必要的目录..."
    
    mkdir -p docker/logs/{postgres,server,nginx}
    mkdir -p docker/files
    mkdir -p docker/nginx/ssl
    mkdir -p docker/prometheus
    mkdir -p docker/grafana/provisioning
    
    log_success "目录创建完成"
}

# 设置权限
set_permissions() {
    log_info "设置文件权限..."
    
    # 设置脚本执行权限
    chmod +x scripts/*.sh
    chmod +x docker/init-scripts/*.sh
    
    # 设置日志目录权限
    chmod -R 755 docker/logs
    chmod -R 755 docker/files
    
    log_success "权限设置完成"
}

# 构建镜像
build_images() {
    log_info "构建Docker镜像..."
    
    # 构建PostgreSQL镜像
    log_info "构建PostgreSQL镜像..."
    docker build -t skyeye-postgres:latest -f docker/Dockerfile.postgres docker/
    
    # 构建后端镜像
    if [ -f "skyv-server/Dockerfile" ]; then
        log_info "构建后端应用镜像..."
        docker build -t skyeye-server:latest skyv-server/
    else
        log_warning "后端Dockerfile不存在，跳过构建"
    fi
    
    # 构建前端镜像
    if [ -f "skyv-web/Dockerfile" ]; then
        log_info "构建前端应用镜像..."
        docker build -t skyeye-web:latest skyv-web/
    else
        log_warning "前端Dockerfile不存在，跳过构建"
    fi
    
    log_success "镜像构建完成"
}

# 启动服务
start_services() {
    log_info "启动服务..."
    
    # 停止现有服务
    docker-compose down
    
    # 启动基础服务
    log_info "启动基础服务（数据库和缓存）..."
    docker-compose up -d postgres redis
    
    # 等待数据库启动
    log_info "等待数据库启动..."
    sleep 30
    
    # 启动应用服务
    log_info "启动应用服务..."
    docker-compose up -d skyeye-server
    
    # 等待后端启动
    log_info "等待后端服务启动..."
    sleep 60
    
    # 启动前端和代理服务
    log_info "启动前端和代理服务..."
    docker-compose up -d skyeye-web nginx
    
    log_success "服务启动完成"
}

# 启动监控服务
start_monitoring() {
    log_info "启动监控服务..."
    
    docker-compose --profile monitoring up -d prometheus grafana
    
    log_success "监控服务启动完成"
}

# 检查服务状态
check_services() {
    log_info "检查服务状态..."
    
    # 等待服务完全启动
    sleep 10
    
    # 检查PostgreSQL
    if docker-compose exec -T postgres pg_isready -U skyeye_app -d skyeye &> /dev/null; then
        log_success "PostgreSQL 服务正常"
    else
        log_error "PostgreSQL 服务异常"
    fi
    
    # 检查Redis
    if docker-compose exec -T redis redis-cli ping &> /dev/null; then
        log_success "Redis 服务正常"
    else
        log_error "Redis 服务异常"
    fi
    
    # 检查后端服务
    if curl -f http://localhost:8080/actuator/health &> /dev/null; then
        log_success "后端服务正常"
    else
        log_warning "后端服务可能还在启动中"
    fi
    
    # 显示服务状态
    docker-compose ps
}

# 显示访问信息
show_access_info() {
    log_info "服务访问信息："
    echo "================================="
    echo "前端应用: http://localhost"
    echo "后端API: http://localhost:8080"
    echo "代理服务: http://localhost:8081"
    echo "数据库: localhost:5432"
    echo "Redis: localhost:6379"
    echo "Prometheus: http://localhost:9090 (需要启动监控)"
    echo "Grafana: http://localhost:3000 (需要启动监控)"
    echo "================================="
    echo "默认账号:"
    echo "管理员: admin / admin123456"
    echo "普通用户: user / user123456"
    echo "Grafana: admin / skyeye_grafana_2024"
    echo "================================="
}

# 清理函数
cleanup() {
    log_info "清理临时文件..."
    # 这里可以添加清理逻辑
    log_success "清理完成"
}

# 主函数
main() {
    log_info "开始部署 SkyEye 智能监控系统..."
    
    # 检查参数
    MONITORING=false
    BUILD_IMAGES=true
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --monitoring)
                MONITORING=true
                shift
                ;;
            --no-build)
                BUILD_IMAGES=false
                shift
                ;;
            --help)
                echo "用法: $0 [选项]"
                echo "选项:"
                echo "  --monitoring    启动监控服务"
                echo "  --no-build      跳过镜像构建"
                echo "  --help          显示帮助信息"
                exit 0
                ;;
            *)
                log_error "未知参数: $1"
                exit 1
                ;;
        esac
    done
    
    # 执行部署步骤
    check_dependencies
    create_directories
    set_permissions
    
    if [ "$BUILD_IMAGES" = true ]; then
        build_images
    fi
    
    start_services
    
    if [ "$MONITORING" = true ]; then
        start_monitoring
    fi
    
    check_services
    show_access_info
    
    log_success "SkyEye 智能监控系统部署完成！"
}

# 捕获退出信号，执行清理
trap cleanup EXIT

# 执行主函数
main "$@" 