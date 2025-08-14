#!/bin/bash

# SkyEye 数据库验证脚本
# ========================

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# 数据库连接参数
DB_HOST=${DB_HOST:-localhost}
DB_PORT=${DB_PORT:-5432}
DB_NAME=${DB_NAME:-skyeye}
DB_USER=${DB_USER:-skyeye_app}
DB_PASSWORD=${DB_PASSWORD:-skyeye_app_2024}

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

# 执行SQL查询
execute_sql() {
    local sql="$1"
    local description="$2"
    
    log_info "检查: $description"
    
    if result=$(PGPASSWORD=$DB_PASSWORD psql -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME -t -c "$sql" 2>/dev/null); then
        echo "$result" | sed 's/^[ \t]*//'
        return 0
    else
        log_error "查询失败: $description"
        return 1
    fi
}

# 检查数据库连接
check_connection() {
    log_info "检查数据库连接..."
    
    if PGPASSWORD=$DB_PASSWORD pg_isready -h $DB_HOST -p $DB_PORT -U $DB_USER -d $DB_NAME &>/dev/null; then
        log_success "数据库连接正常"
        return 0
    else
        log_error "数据库连接失败"
        return 1
    fi
}

# 检查扩展
check_extensions() {
    log_info "检查数据库扩展..."
    
    local extensions=$(execute_sql "SELECT extname, extversion FROM pg_extension WHERE extname IN ('uuid-ossp', 'pgcrypto');" "数据库扩展")
    
    if echo "$extensions" | grep -q "uuid-ossp"; then
        log_success "uuid-ossp 扩展已安装"
    else
        log_error "uuid-ossp 扩展未安装"
    fi
    
    if echo "$extensions" | grep -q "pgcrypto"; then
        log_success "pgcrypto 扩展已安装"
    else
        log_error "pgcrypto 扩展未安装"
    fi
}

# 检查函数
check_functions() {
    log_info "检查数据库函数..."
    
    local functions=$(execute_sql "SELECT proname FROM pg_proc WHERE proname IN ('create_monthly_partition', 'drop_old_partitions', 'encrypt_sensitive_data', 'decrypt_sensitive_data', 'update_updated_at_column');" "数据库函数")
    
    local expected_functions=("create_monthly_partition" "drop_old_partitions" "encrypt_sensitive_data" "decrypt_sensitive_data" "update_updated_at_column")
    
    for func in "${expected_functions[@]}"; do
        if echo "$functions" | grep -q "$func"; then
            log_success "函数 $func 已创建"
        else
            log_error "函数 $func 未找到"
        fi
    done
}

# 检查表结构
check_tables() {
    log_info "检查数据表..."
    
    local tables=$(execute_sql "SELECT tablename FROM pg_tables WHERE schemaname = 'public' ORDER BY tablename;" "数据表列表")
    
    local expected_tables=(
        "tb_users" "tb_roles" "tb_permissions" "tb_user_roles" "tb_role_permissions"
        "tb_devices" "tb_device_types" "tb_device_areas" "tb_device_groups" "tb_device_tags" "tb_device_tag_relations"
        "tb_collectors" "tb_metric_templates" "tb_collection_tasks" "tb_collection_data" "tb_collection_logs"
        "tb_alert_rules" "tb_alerts" "tb_alert_notifications" "tb_alert_processes"
        "tb_system_settings" "tb_operation_logs" "tb_system_notifications" "tb_user_notification_reads" "tb_file_storage"
    )
    
    local missing_tables=()
    
    for table in "${expected_tables[@]}"; do
        if echo "$tables" | grep -q "$table"; then
            log_success "表 $table 已创建"
        else
            log_error "表 $table 未找到"
            missing_tables+=("$table")
        fi
    done
    
    if [ ${#missing_tables[@]} -eq 0 ]; then
        log_success "所有必需的表都已创建"
    else
        log_error "缺失 ${#missing_tables[@]} 个表"
    fi
}

# 检查分区表
check_partitions() {
    log_info "检查分区表..."
    
    local partitions=$(execute_sql "SELECT schemaname, tablename FROM pg_tables WHERE tablename LIKE '%_y%m%' ORDER BY tablename;" "分区表")
    
    if [ -n "$partitions" ]; then
        log_success "分区表已创建:"
        echo "$partitions"
    else
        log_warning "未找到分区表，可能需要手动创建"
    fi
}

# 检查索引
check_indexes() {
    log_info "检查索引..."
    
    local index_count=$(execute_sql "SELECT COUNT(*) FROM pg_indexes WHERE schemaname = 'public';" "索引数量")
    
    if [ "$index_count" -gt 50 ]; then
        log_success "索引已创建，总数: $index_count"
    else
        log_warning "索引数量较少: $index_count，请检查是否正常"
    fi
}

# 检查基础数据
check_basic_data() {
    log_info "检查基础数据..."
    
    # 检查权限数据
    local permission_count=$(execute_sql "SELECT COUNT(*) FROM tb_permissions;" "权限数量")
    log_info "权限数量: $permission_count"
    
    # 检查角色数据
    local role_count=$(execute_sql "SELECT COUNT(*) FROM tb_roles;" "角色数量")
    log_info "角色数量: $role_count"
    
    # 检查设备类型数据
    local device_type_count=$(execute_sql "SELECT COUNT(*) FROM tb_device_types;" "设备类型数量")
    log_info "设备类型数量: $device_type_count"
    
    # 检查系统配置数据
    local setting_count=$(execute_sql "SELECT COUNT(*) FROM tb_system_settings;" "系统配置数量")
    log_info "系统配置数量: $setting_count"
    
    if [ "$permission_count" -gt 0 ] && [ "$role_count" -gt 0 ] && [ "$device_type_count" -gt 0 ] && [ "$setting_count" -gt 0 ]; then
        log_success "基础数据初始化完成"
    else
        log_error "基础数据初始化不完整"
    fi
}

# 检查数据库性能
check_performance() {
    log_info "检查数据库性能配置..."
    
    local shared_buffers=$(execute_sql "SHOW shared_buffers;" "shared_buffers")
    local effective_cache_size=$(execute_sql "SHOW effective_cache_size;" "effective_cache_size")
    local max_connections=$(execute_sql "SHOW max_connections;" "max_connections")
    
    log_info "shared_buffers: $shared_buffers"
    log_info "effective_cache_size: $effective_cache_size"
    log_info "max_connections: $max_connections"
}

# 生成验证报告
generate_report() {
    log_info "生成验证报告..."
    
    local report_file="database_verification_$(date +%Y%m%d_%H%M%S).txt"
    
    cat > "$report_file" << EOF
SkyEye 数据库验证报告
=====================
验证时间: $(date)
数据库: $DB_HOST:$DB_PORT/$DB_NAME
用户: $DB_USER

表数量统计:
$(execute_sql "SELECT 'tb_users' as table_name, COUNT(*) as count FROM tb_users
UNION ALL SELECT 'tb_roles', COUNT(*) FROM tb_roles
UNION ALL SELECT 'tb_permissions', COUNT(*) FROM tb_permissions
UNION ALL SELECT 'tb_devices', COUNT(*) FROM tb_devices
UNION ALL SELECT 'tb_device_types', COUNT(*) FROM tb_device_types
UNION ALL SELECT 'tb_system_settings', COUNT(*) FROM tb_system_settings;" "表统计")

数据库大小:
$(execute_sql "SELECT pg_size_pretty(pg_database_size('$DB_NAME'));" "数据库大小")

连接状态:
$(execute_sql "SELECT COUNT(*) as active_connections FROM pg_stat_activity WHERE datname = '$DB_NAME';" "活跃连接数")
EOF

    log_success "验证报告已生成: $report_file"
}

# 主函数
main() {
    log_info "开始验证 SkyEye 数据库..."
    echo "================================="
    
    # 检查连接
    if ! check_connection; then
        log_error "数据库连接失败，退出验证"
        exit 1
    fi
    
    # 执行各项检查
    check_extensions
    echo "--------------------------------"
    
    check_functions
    echo "--------------------------------"
    
    check_tables
    echo "--------------------------------"
    
    check_partitions
    echo "--------------------------------"
    
    check_indexes
    echo "--------------------------------"
    
    check_basic_data
    echo "--------------------------------"
    
    check_performance
    echo "--------------------------------"
    
    generate_report
    
    echo "================================="
    log_success "数据库验证完成！"
}

# 显示帮助信息
show_help() {
    echo "SkyEye 数据库验证脚本"
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -h, --host HOST       数据库主机 (默认: localhost)"
    echo "  -p, --port PORT       数据库端口 (默认: 5432)"
    echo "  -d, --database DB     数据库名称 (默认: skyeye)"
    echo "  -u, --user USER       数据库用户 (默认: skyeye_app)"
    echo "  -w, --password PASS   数据库密码 (默认: skyeye_app_2024)"
    echo "  --help                显示此帮助信息"
    echo ""
    echo "环境变量:"
    echo "  DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD"
}

# 解析命令行参数
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--host)
            DB_HOST="$2"
            shift 2
            ;;
        -p|--port)
            DB_PORT="$2"
            shift 2
            ;;
        -d|--database)
            DB_NAME="$2"
            shift 2
            ;;
        -u|--user)
            DB_USER="$2"
            shift 2
            ;;
        -w|--password)
            DB_PASSWORD="$2"
            shift 2
            ;;
        --help)
            show_help
            exit 0
            ;;
        *)
            log_error "未知参数: $1"
            show_help
            exit 1
            ;;
    esac
done

# 执行主函数
main 