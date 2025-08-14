#!/bin/bash

# SkyEye 中间件管理工具
# =====================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
PURPLE='\033[0;35m'
NC='\033[0m'

COMPOSE_FILE="docker-compose.middleware.yml"

# 日志函数
log_info() { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error() { echo -e "${RED}[ERROR]${NC} $1"; }

# 显示菜单
show_menu() {
    clear
    echo -e "${PURPLE}"
    echo "════════════════════════════════════════"
    echo "       SkyEye 中间件管理工具"
    echo "════════════════════════════════════════"
    echo -e "${NC}"
    echo "1) 启动服务"
    echo "2) 停止服务" 
    echo "3) 重启服务"
    echo "4) 查看状态"
    echo "5) 查看日志"
    echo "6) 数据库管理"
    echo "7) 备份数据"
    echo "8) 恢复数据"
    echo "9) 清理数据"
    echo "10) 性能监控"
    echo "11) 健康检查"
    echo "0) 退出"
    echo "════════════════════════════════════════"
}

# 启动服务菜单
start_services_menu() {
    echo "选择要启动的服务配置："
    echo "1) 核心服务 (PostgreSQL + Redis)"
    echo "2) 核心 + 管理工具"
    echo "3) 核心 + 日志服务"
    echo "4) 核心 + 存储服务"
    echo "5) 核心 + 消息服务"
    echo "6) 核心 + 时序数据库"
    echo "7) 完整服务"
    echo "8) 自定义"
    
    read -p "请选择 (1-8): " choice
    
    case $choice in
        1) ./scripts/init-middleware.sh start core ;;
        2) ./scripts/init-middleware.sh start core && sleep 5 && ./scripts/init-middleware.sh start tools ;;
        3) ./scripts/init-middleware.sh start core && sleep 5 && ./scripts/init-middleware.sh start logging ;;
        4) ./scripts/init-middleware.sh start core && sleep 5 && ./scripts/init-middleware.sh start storage ;;
        5) ./scripts/init-middleware.sh start core && sleep 5 && ./scripts/init-middleware.sh start messaging ;;
        6) ./scripts/init-middleware.sh start core && sleep 5 && ./scripts/init-middleware.sh start timeseries ;;
        7) ./scripts/init-middleware.sh start all ;;
        8) 
            read -p "输入配置名称 (core/logging/storage/messaging/timeseries/tools/all): " profile
            ./scripts/init-middleware.sh start "$profile"
            ;;
        *) log_error "无效选择" ;;
    esac
    
    read -p "按回车键继续..."
}

# 查看状态
show_status() {
    echo "════════════════════════════════════════"
    echo "           服务运行状态"
    echo "════════════════════════════════════════"
    
    docker-compose -f $COMPOSE_FILE ps
    
    echo ""
    echo "════════════════════════════════════════"
    echo "           资源使用情况"
    echo "════════════════════════════════════════"
    
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}\t{{.MemPerc}}" $(docker-compose -f $COMPOSE_FILE ps -q) 2>/dev/null || echo "暂无运行的容器"
    
    read -p "按回车键继续..."
}

# 查看日志菜单
logs_menu() {
    echo "选择要查看日志的服务："
    echo "1) PostgreSQL"
    echo "2) Redis"
    echo "3) Elasticsearch"
    echo "4) MinIO"
    echo "5) RabbitMQ"
    echo "6) InfluxDB"
    echo "7) pgAdmin"
    echo "8) Redis Commander"
    echo "9) 所有服务"
    
    read -p "请选择 (1-9): " choice
    
    case $choice in
        1) docker-compose -f $COMPOSE_FILE logs --tail=100 -f postgres ;;
        2) docker-compose -f $COMPOSE_FILE logs --tail=100 -f redis ;;
        3) docker-compose -f $COMPOSE_FILE logs --tail=100 -f elasticsearch ;;
        4) docker-compose -f $COMPOSE_FILE logs --tail=100 -f minio ;;
        5) docker-compose -f $COMPOSE_FILE logs --tail=100 -f rabbitmq ;;
        6) docker-compose -f $COMPOSE_FILE logs --tail=100 -f influxdb ;;
        7) docker-compose -f $COMPOSE_FILE logs --tail=100 -f pgadmin ;;
        8) docker-compose -f $COMPOSE_FILE logs --tail=100 -f redis-commander ;;
        9) docker-compose -f $COMPOSE_FILE logs --tail=50 -f ;;
        *) log_error "无效选择" ;;
    esac
}

# 数据库管理菜单
database_menu() {
    echo "数据库管理选项："
    echo "1) 连接数据库"
    echo "2) 执行SQL脚本"
    echo "3) 查看数据库大小"
    echo "4) 查看表信息"
    echo "5) 创建分区"
    echo "6) 清理旧分区"
    echo "7) 重建索引"
    echo "8) 更新统计信息"
    
    read -p "请选择 (1-8): " choice
    
    case $choice in
        1) 
            docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye
            ;;
        2)
            read -p "输入SQL文件路径: " sql_file
            if [ -f "$sql_file" ]; then
                docker-compose -f $COMPOSE_FILE exec -T postgres psql -U skyeye_app -d skyeye < "$sql_file"
            else
                log_error "文件不存在: $sql_file"
            fi
            ;;
        3)
            docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye -c "SELECT pg_size_pretty(pg_database_size('skyeye')) as database_size;"
            ;;
        4)
            docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye -c "SELECT schemaname, tablename, pg_size_pretty(pg_total_relation_size(schemaname||'.'||tablename)) as size FROM pg_tables WHERE schemaname = 'public' ORDER BY pg_total_relation_size(schemaname||'.'||tablename) DESC LIMIT 20;"
            ;;
        5)
            read -p "输入表名: " table_name
            read -p "输入分区日期 (YYYY-MM-DD): " partition_date
            docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye -c "SELECT create_monthly_partition('$table_name', '$partition_date'::date);"
            ;;
        6)
            read -p "输入表名: " table_name
            read -p "输入保留月数: " retention_months
            docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye -c "SELECT drop_old_partitions('$table_name', $retention_months);"
            ;;
        7)
            log_info "重建索引中..."
            docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye -c "REINDEX DATABASE skyeye;"
            ;;
        8)
            log_info "更新统计信息中..."
            docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye -c "ANALYZE;"
            ;;
        *) log_error "无效选择" ;;
    esac
    
    read -p "按回车键继续..."
}

# 备份数据
backup_data() {
    local backup_dir="backups/$(date +%Y%m%d_%H%M%S)"
    mkdir -p "$backup_dir"
    
    log_info "开始备份数据..."
    
    # 备份PostgreSQL
    if docker-compose -f $COMPOSE_FILE ps postgres | grep -q "Up"; then
        log_info "备份PostgreSQL数据..."
        docker-compose -f $COMPOSE_FILE exec -T postgres pg_dump -U skyeye_app skyeye > "$backup_dir/postgres_backup.sql"
        log_success "PostgreSQL备份完成"
    fi
    
    # 备份Redis
    if docker-compose -f $COMPOSE_FILE ps redis | grep -q "Up"; then
        log_info "备份Redis数据..."
        docker-compose -f $COMPOSE_FILE exec redis redis-cli --rdb - > "$backup_dir/redis_backup.rdb"
        log_success "Redis备份完成"
    fi
    
    log_success "数据备份完成，保存在: $backup_dir"
    read -p "按回车键继续..."
}

# 恢复数据
restore_data() {
    echo "可用的备份文件："
    ls -la backups/ 2>/dev/null || log_warning "没有找到备份文件"
    
    read -p "输入备份目录名称: " backup_dir
    
    if [ ! -d "backups/$backup_dir" ]; then
        log_error "备份目录不存在"
        read -p "按回车键继续..."
        return
    fi
    
    log_warning "恢复数据将覆盖现有数据！"
    read -p "确认继续？(y/N): " -n 1 -r
    echo
    
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        # 恢复PostgreSQL
        if [ -f "backups/$backup_dir/postgres_backup.sql" ]; then
            log_info "恢复PostgreSQL数据..."
            docker-compose -f $COMPOSE_FILE exec -T postgres psql -U skyeye_app -d skyeye < "backups/$backup_dir/postgres_backup.sql"
            log_success "PostgreSQL恢复完成"
        fi
        
        # 恢复Redis
        if [ -f "backups/$backup_dir/redis_backup.rdb" ]; then
            log_info "恢复Redis数据..."
            docker-compose -f $COMPOSE_FILE stop redis
            docker cp "backups/$backup_dir/redis_backup.rdb" $(docker-compose -f $COMPOSE_FILE ps -q redis):/data/dump.rdb
            docker-compose -f $COMPOSE_FILE start redis
            log_success "Redis恢复完成"
        fi
        
        log_success "数据恢复完成"
    fi
    
    read -p "按回车键继续..."
}

# 性能监控
performance_monitor() {
    echo "════════════════════════════════════════"
    echo "           性能监控信息"
    echo "════════════════════════════════════════"
    
    # 系统资源
    echo "系统资源使用："
    echo "CPU: $(top -bn1 | grep "Cpu(s)" | awk '{print $2}' | cut -d'%' -f1)%"
    echo "内存: $(free | grep Mem | awk '{printf "%.1f%%", $3/$2 * 100.0}')"
    echo "磁盘: $(df -h / | awk 'NR==2{printf "%s", $5}')"
    
    echo ""
    echo "Docker资源使用："
    docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}"
    
    echo ""
    echo "数据库连接数："
    if docker-compose -f $COMPOSE_FILE ps postgres | grep -q "Up"; then
        docker-compose -f $COMPOSE_FILE exec postgres psql -U skyeye_app -d skyeye -c "SELECT count(*) as active_connections FROM pg_stat_activity;"
    fi
    
    read -p "按回车键继续..."
}

# 健康检查
health_check() {
    echo "════════════════════════════════════════"
    echo "           服务健康检查"
    echo "════════════════════════════════════════"
    
    # 检查PostgreSQL
    if docker-compose -f $COMPOSE_FILE exec postgres pg_isready -U skyeye_app &>/dev/null; then
        echo -e "PostgreSQL: ${GREEN}✓ 健康${NC}"
    else
        echo -e "PostgreSQL: ${RED}✗ 异常${NC}"
    fi
    
    # 检查Redis
    if docker-compose -f $COMPOSE_FILE exec redis redis-cli ping | grep -q PONG; then
        echo -e "Redis: ${GREEN}✓ 健康${NC}"
    else
        echo -e "Redis: ${RED}✗ 异常${NC}"
    fi
    
    # 检查其他服务
    services=("elasticsearch" "minio" "rabbitmq" "influxdb")
    for service in "${services[@]}"; do
        if docker-compose -f $COMPOSE_FILE ps "$service" | grep -q "Up"; then
            echo -e "$service: ${GREEN}✓ 运行中${NC}"
        else
            echo -e "$service: ${YELLOW}- 未启动${NC}"
        fi
    done
    
    read -p "按回车键继续..."
}

# 主循环
main() {
    while true; do
        show_menu
        read -p "请选择操作 (0-11): " choice
        
        case $choice in
            1) start_services_menu ;;
            2) 
                ./scripts/init-middleware.sh stop
                read -p "按回车键继续..."
                ;;
            3) 
                ./scripts/init-middleware.sh restart
                read -p "按回车键继续..."
                ;;
            4) show_status ;;
            5) logs_menu ;;
            6) database_menu ;;
            7) backup_data ;;
            8) restore_data ;;
            9) 
                ./scripts/init-middleware.sh cleanup
                read -p "按回车键继续..."
                ;;
            10) performance_monitor ;;
            11) health_check ;;
            0) 
                log_info "感谢使用 SkyEye 中间件管理工具"
                exit 0
                ;;
            *) 
                log_error "无效选择，请重新输入"
                sleep 2
                ;;
        esac
    done
}

# 执行主函数
main "$@" 