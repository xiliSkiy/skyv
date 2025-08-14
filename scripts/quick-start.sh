#!/bin/bash

# SkyEye 快速启动脚本
# ===================

set -e

# 颜色定义
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
NC='\033[0m'

echo -e "${BLUE}"
cat << 'EOF'
 ____  _          _____           
/ ___|| | ___   _| ____|_   _  ___ 
\___ \| |/ / | | |  _| | | | |/ _ \
 ___) |   <| |_| | |___| |_| |  __/
|____/|_|\_\\__, |_____|\__, |\___|
            |___/       |___/      

智能监控系统 - 快速启动
EOF
echo -e "${NC}"

log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# 设置权限
chmod +x scripts/*.sh

# 检查配置文件
if [ ! -f "docker-compose.middleware.yml" ]; then
    log_warning "中间件配置文件不存在，请先确保文件完整"
    exit 1
fi

# 选择启动模式
echo "请选择启动模式:"
echo "1) 仅核心服务 (PostgreSQL + Redis)"
echo "2) 核心 + 管理工具"
echo "3) 核心 + 日志服务"
echo "4) 完整服务"
echo "5) 自定义"

read -p "请输入选择 (1-5): " choice

case $choice in
    1)
        log_info "启动核心服务..."
        ./scripts/init-middleware.sh start core
        ;;
    2)
        log_info "启动核心服务和管理工具..."
        ./scripts/init-middleware.sh start core
        sleep 10
        ./scripts/init-middleware.sh start tools
        ;;
    3)
        log_info "启动核心服务和日志服务..."
        ./scripts/init-middleware.sh start core
        sleep 10
        ./scripts/init-middleware.sh start logging
        ;;
    4)
        log_info "启动完整服务..."
        ./scripts/init-middleware.sh start all
        ;;
    5)
        echo "可用的服务配置:"
        echo "- core: 核心服务"
        echo "- logging: 日志服务" 
        echo "- storage: 存储服务"
        echo "- messaging: 消息服务"
        echo "- timeseries: 时序服务"
        echo "- tools: 管理工具"
        echo "- all: 所有服务"
        read -p "请输入配置名称: " profile
        ./scripts/init-middleware.sh start "$profile"
        ;;
    *)
        log_warning "无效选择，启动核心服务"
        ./scripts/init-middleware.sh start core
        ;;
esac

echo ""
log_success "启动完成！"
echo ""
echo "常用命令："
echo "  查看状态: ./scripts/init-middleware.sh status"
echo "  查看日志: ./scripts/init-middleware.sh logs"
echo "  停止服务: ./scripts/init-middleware.sh stop"
echo "  验证数据库: ./scripts/verify-database.sh" 