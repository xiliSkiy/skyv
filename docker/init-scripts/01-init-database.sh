#!/bin/bash
set -e

# SkyEye Database Initialization Script
echo "Starting SkyEye database initialization..."

# 创建数据库（如果不存在）
psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" <<-EOSQL
    -- 创建扩展
    CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
    CREATE EXTENSION IF NOT EXISTS "pgcrypto";
    CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
    
    -- 设置时区
    SET timezone = 'Asia/Shanghai';
    
    -- 显示版本信息
    SELECT version();
    
    -- 显示已安装的扩展
    SELECT extname, extversion FROM pg_extension;
EOSQL

echo "Database initialization completed successfully!"

# 如果存在SQL脚本文件，执行它们
if [ -d "/docker-entrypoint-initdb.d/sql" ]; then
    echo "Executing SQL migration scripts..."
    for f in /docker-entrypoint-initdb.d/sql/V*.sql; do
        if [ -f "$f" ]; then
            echo "Processing $f file..."
            psql -v ON_ERROR_STOP=1 --username "$POSTGRES_USER" --dbname "$POSTGRES_DB" -f "$f"
        fi
    done
    echo "SQL migration scripts execution completed!"
fi

echo "SkyEye database setup completed!" 