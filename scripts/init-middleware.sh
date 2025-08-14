#!/bin/bash

# SkyEye 中间件初始化脚本
# =========================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 配置变量
COMPOSE_FILE="docker-compose.middleware.yml"
WAIT_TIMEOUT=300

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

# 显示横幅
show_banner() {
    echo "=================================================="
    echo "      SkyEye 智能监控系统 - 中间件初始化"
    echo "=================================================="
    echo ""
}

# 检查依赖
check_dependencies() {
    log_info "检查系统依赖..."
    
    if ! command -v docker &> /dev/null; then
        log_error "Docker 未安装"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        log_error "Docker Compose 未安装"
        exit 1
    fi
    
    log_success "依赖检查完成"
}

# 创建必要目录
create_directories() {
    log_info "创建必要目录..."
    
    mkdir -p docker/logs/{postgres,redis,elasticsearch,minio,rabbitmq,influxdb}
    mkdir -p docker/pgadmin
    
    log_success "目录创建完成"
}

# 创建pgAdmin服务器配置
create_pgadmin_config() {
    log_info "创建pgAdmin配置..."
    
    cat > docker/pgadmin/servers.json << 'EOF'
{
    "Servers": {
        "1": {
            "Name": "SkyEye PostgreSQL",
            "Group": "Servers",
            "Host": "skyeye-postgres",
            "Port": 5432,
            "MaintenanceDB": "skyeye",
            "Username": "skyeye_app",
            "PassFile": "/pgpass",
            "SSLMode": "prefer"
        }
    }
}
EOF
    
    log_success "pgAdmin配置创建完成"
}

# 启动核心服务
start_core_services() {
    log_info "启动核心中间件服务..."
    
    docker-compose -f $COMPOSE_FILE up -d postgres redis
    
    log_info "等待核心服务启动..."
    wait_for_service "postgres" "PostgreSQL"
    wait_for_service "redis" "Redis"
    
    log_success "核心服务启动完成"
}

# 启动扩展服务
start_extended_services() {
    local profile=$1
    log_info "启动扩展服务: $profile"
    
    case $profile in
        "logging")
            docker-compose -f $COMPOSE_FILE --profile logging up -d elasticsearch
            wait_for_service "elasticsearch" "Elasticsearch"
            ;;
        "storage")
            docker-compose -f $COMPOSE_FILE --profile storage up -d minio
            wait_for_service "minio" "MinIO"
            ;;
        "messaging")
            docker-compose -f $COMPOSE_FILE --profile messaging up -d rabbitmq
            wait_for_service "rabbitmq" "RabbitMQ"
            ;;
        "timeseries")
            docker-compose -f $COMPOSE_FILE --profile timeseries up -d influxdb
            wait_for_service "influxdb" "InfluxDB"
            ;;
        "tools")
            docker-compose -f $COMPOSE_FILE --profile tools up -d pgadmin redis-commander
            ;;
        "all")
            docker-compose -f $COMPOSE_FILE --profile logging --profile storage --profile messaging --profile timeseries --profile tools up -d
            ;;
    esac
}

# 等待服务启动
wait_for_service() {
    local service=$1
    local name=$2
    local max_wait=${WAIT_TIMEOUT:-300}
    local wait_time=0
    
    log_info "等待 $name 服务启动..."
    
    while [ $wait_time -lt $max_wait ]; do
        if docker-compose -f $COMPOSE_FILE exec -T $service echo "Service is ready" &>/dev/null; then
            log_success "$name 服务已启动"
            return 0
        fi
        
        sleep 5
        wait_time=$((wait_time + 5))
        printf "."
    done
    
    echo ""
    log_warning "$name 服务启动超时，请检查日志"
    return 1
}

# 验证数据库初始化
verify_database() {
    log_info "验证数据库初始化..."
    
    # 等待数据库完全启动
    sleep 10
    
    # 检查数据库连接
    if docker-compose -f $COMPOSE_FILE exec -T postgres pg_isready -U skyeye_app -d skyeye &>/dev/null; then
        log_success "数据库连接正常"
    else
        log_error "数据库连接失败"
        return 1
    fi
    
    # 检查表是否创建
    local table_count=$(docker-compose -f $COMPOSE_FILE exec -T postgres psql -U skyeye_app -d skyeye -t -c "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema = 'public';" 2>/dev/null | tr -d ' \n')
    
    if [ "$table_count" -gt 20 ]; then
        log_success "数据库表初始化完成 ($table_count 个表)"
    else
        log_warning "数据库表数量异常: $table_count"
    fi
    
    # 检查基础数据
    local user_count=$(docker-compose -f $COMPOSE_FILE exec -T postgres psql -U skyeye_app -d skyeye -t -c "SELECT COUNT(*) FROM tb_users;" 2>/dev/null | tr -d ' \n')
    
    if [ "$user_count" -gt 0 ]; then
        log_success "基础数据初始化完成 ($user_count 个用户)"
    else
        log_warning "基础数据初始化异常"
    fi
}

# 显示服务状态
show_services_status() {
    log_info "服务状态概览:"
    echo "======================================"
    
    docker-compose -f $COMPOSE_FILE ps --format "table {{.Name}}\t{{.State}}\t{{.Ports}}"
    
    echo "======================================"
}

# 显示访问信息
show_access_info() {
    log_info "服务访问信息:"
    echo "======================================"
    echo "PostgreSQL:"
    echo "  地址: localhost:5432"
    echo "  数据库: skyeye"
    echo "  用户: skyeye_app"
    echo "  密码: skyeye_app_2024"
    echo ""
    echo "Redis:"
    echo "  地址: localhost:6379"
    echo "  密码: skyeye_redis_2024"
    echo ""
    
    # 检查扩展服务
    if docker-compose -f $COMPOSE_FILE ps elasticsearch &>/dev/null; then
        echo "Elasticsearch: http://localhost:9200"
    fi
    
    if docker-compose -f $COMPOSE_FILE ps minio &>/dev/null; then
        echo "MinIO 控制台: http://localhost:9001"
        echo "  用户: skyeye_minio / skyeye_minio_2024"
    fi
    
    if docker-compose -f $COMPOSE_FILE ps rabbitmq &>/dev/null; then
        echo "RabbitMQ 管理界面: http://localhost:15672"
        echo "  用户: skyeye_rabbitmq / skyeye_rabbitmq_2024"
    fi
    
    if docker-compose -f $COMPOSE_FILE ps influxdb &>/dev/null; then
        echo "InfluxDB: http://localhost:8086"
        echo "  用户: skyeye_admin / skyeye_influxdb_2024"
    fi
    
    if docker-compose -f $COMPOSE_FILE ps pgadmin &>/dev/null; then
        echo "pgAdmin: http://localhost:5050"
        echo "  用户: admin@skyeye.com / skyeye_pgadmin_2024"
    fi
    
    if docker-compose -f $COMPOSE_FILE ps redis-commander &>/dev/null; then
        echo "Redis Commander: http://localhost:8081"
        echo "  用户: admin / skyeye_redis_commander_2024"
    fi
    
    echo "======================================"
}

# 停止所有服务
stop_services() {
    log_info "停止所有中间件服务..."
    
    docker-compose -f $COMPOSE_FILE down
    
    log_success "服务已停止"
}

# 清理数据
cleanup_data() {
    log_warning "这将删除所有数据卷，数据将无法恢复！"
    read -p "确认删除所有数据？(y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        log_info "清理数据卷..."
        docker-compose -f $COMPOSE_FILE down -v
        log_success "数据清理完成"
    else
        log_info "取消清理操作"
    fi
}

# 显示日志
show_logs() {
    local service=$1
    
    if [ -z "$service" ]; then
        docker-compose -f $COMPOSE_FILE logs --tail=50 -f
    else
        docker-compose -f $COMPOSE_FILE logs --tail=50 -f $service
    fi
}

# 显示帮助
show_help() {
    echo "SkyEye 中间件初始化脚本"
    echo ""
    echo "用法: $0 [命令] [选项]"
    echo ""
    echo "命令:"
    echo "  start [profile]     启动服务 (默认: core)"
    echo "  stop               停止所有服务"
    echo "  status             显示服务状态"
    echo "  logs [service]     显示日志"
    echo "  verify             验证数据库"
    echo "  cleanup            清理所有数据"
    echo "  restart            重启服务"
    echo ""
    echo "配置文件 (profiles):"
    echo "  core               核心服务 (PostgreSQL + Redis)"
    echo "  logging            日志服务 (Elasticsearch)"
    echo "  storage            存储服务 (MinIO)"
    echo "  messaging          消息服务 (RabbitMQ)"
    echo "  timeseries         时序服务 (InfluxDB)"
    echo "  tools              管理工具 (pgAdmin + Redis Commander)"
    echo "  all                所有服务"
    echo ""
    echo "示例:"
    echo "  $0 start                    # 启动核心服务"
    echo "  $0 start logging           # 启动核心+日志服务"
    echo "  $0 start all               # 启动所有服务"
    echo "  $0 logs postgres           # 查看PostgreSQL日志"
}

# 主函数
main() {
    local command=${1:-start}
    local profile=${2:-core}
    
    case $command in
        "start")
            show_banner
            check_dependencies
            create_directories
            create_pgadmin_config
            
            start_core_services
            
            if [ "$profile" != "core" ]; then
                start_extended_services "$profile"
            fi
            
            verify_database
            show_services_status
            show_access_info
            
            log_success "中间件初始化完成！"
            ;;
        "stop")
            stop_services
            ;;
        "status")
            show_services_status
            ;;
        "logs")
            show_logs "$profile"
            ;;
        "verify")
            verify_database
            ;;
        "cleanup")
            cleanup_data
            ;;
        "restart")
            stop_services
            sleep 3
            main start "$profile"
            ;;
        "help"|"--help"|"-h")
            show_help
            ;;
        *)
            log_error "未知命令: $command"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@" 