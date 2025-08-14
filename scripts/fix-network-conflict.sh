#!/bin/bash

# SkyEye 网络冲突修复脚本
# =========================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

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

log_info "检查和修复Docker网络冲突..."

# 检查当前网络
log_info "当前Docker网络列表："
docker network ls | grep -E "(skyeye|skyii|skyiii)"

echo ""

# 检查网络子网配置
log_info "检查现有网络的子网配置："
for network in $(docker network ls --format "{{.Name}}" | grep -E "(skyeye|skyii|skyiii)"); do
    subnet=$(docker network inspect "$network" 2>/dev/null | grep -o '"Subnet": "[^"]*"' | cut -d'"' -f4 || echo "无法获取")
    echo "  $network: $subnet"
done

echo ""

# 停止可能冲突的容器
log_info "停止可能冲突的服务..."
docker-compose -f docker-compose.middleware.yml down 2>/dev/null || log_warning "中间件服务未运行"
docker-compose down 2>/dev/null || log_warning "主服务未运行"

# 检查是否有网络仍在使用
log_info "检查网络使用情况..."
for network in $(docker network ls --format "{{.Name}}" | grep -E "skyeye"); do
    if docker network inspect "$network" --format '{{len .Containers}}' 2>/dev/null | grep -q "0"; then
        log_info "网络 $network 未被使用，可以安全删除"
    else
        log_warning "网络 $network 仍在使用中"
        docker network inspect "$network" --format '{{range $k, $v := .Containers}}{{$v.Name}} {{end}}'
    fi
done

echo ""

# 提供修复选项
log_info "修复选项："
echo "1) 清理所有skyeye相关网络（推荐）"
echo "2) 仅修复冲突的网络"
echo "3) 手动处理"
echo "4) 查看详细诊断信息"

read -p "请选择修复方式 (1-4): " choice

case $choice in
    1)
        log_info "清理所有skyeye相关网络..."
        
        # 停止所有相关容器
        docker ps -a --format "{{.Names}}" | grep -E "(skyeye|skyii|skyiii)" | xargs -r docker stop 2>/dev/null || true
        docker ps -a --format "{{.Names}}" | grep -E "(skyeye|skyii|skyiii)" | xargs -r docker rm 2>/dev/null || true
        
        # 删除网络
        docker network ls --format "{{.Name}}" | grep -E "(skyeye|skyii|skyiii)" | xargs -r docker network rm 2>/dev/null || true
        
        log_success "网络清理完成"
        ;;
    2)
        log_info "修复冲突网络..."
        
        # 检查具体冲突
        if docker network ls | grep -q "skyv_skyeye-middleware"; then
            log_info "删除冲突的中间件网络..."
            docker network rm skyv_skyeye-middleware 2>/dev/null || log_warning "无法删除网络"
        fi
        
        log_success "冲突网络修复完成"
        ;;
    3)
        log_info "手动处理指导："
        echo "1. 停止所有容器: docker-compose down"
        echo "2. 删除网络: docker network rm <network_name>"
        echo "3. 重新启动: ./scripts/init-middleware.sh start core"
        ;;
    4)
        log_info "详细诊断信息："
        echo ""
        echo "=== Docker网络列表 ==="
        docker network ls
        echo ""
        echo "=== 网络详细信息 ==="
        for network in $(docker network ls --format "{{.Name}}" | grep -E "(skyeye|skyii|skyiii)"); do
            echo "--- $network ---"
            docker network inspect "$network" | grep -A 5 -B 5 "Subnet"
            echo ""
        done
        echo ""
        echo "=== 容器状态 ==="
        docker ps -a | grep -E "(skyeye|skyii|skyiii)" || echo "无相关容器"
        ;;
    *)
        log_error "无效选择"
        exit 1
        ;;
esac

echo ""
log_info "现在可以尝试重新启动服务："
echo "  ./scripts/init-middleware.sh start core"
echo ""
log_success "网络冲突修复完成！" 