-- ========================================
-- SkyEye 智能监控系统数据库初始化脚本
-- 版本: V1.0.0
-- 创建时间: 2024-12-19
-- 描述: 数据库基础配置和扩展安装
-- ========================================

-- 创建扩展
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pgcrypto";

-- 设置时区
SET timezone = 'Asia/Shanghai';

-- 创建数据库函数

-- 自动创建月度分区表的函数
CREATE OR REPLACE FUNCTION create_monthly_partition(table_name TEXT, start_date TIMESTAMP)
RETURNS void AS $$
DECLARE
    partition_name TEXT;
    end_date TIMESTAMP;
BEGIN
    partition_name := table_name || '_y' || to_char(start_date, 'YYYY') || 'm' || to_char(start_date, 'MM');
    end_date := start_date + INTERVAL '1 month';
    
    EXECUTE format('CREATE TABLE IF NOT EXISTS %I PARTITION OF %I FOR VALUES FROM (%L) TO (%L)',
                   partition_name, table_name, start_date, end_date);
END;
$$ LANGUAGE plpgsql;

-- 定期清理旧分区的函数
CREATE OR REPLACE FUNCTION drop_old_partitions(table_name TEXT, retention_months INTEGER)
RETURNS void AS $$
DECLARE
    partition_name TEXT;
    cutoff_date DATE;
BEGIN
    cutoff_date := date_trunc('month', CURRENT_DATE) - (retention_months || ' months')::INTERVAL;
    
    FOR partition_name IN 
        SELECT schemaname||'.'||tablename 
        FROM pg_tables 
        WHERE tablename LIKE table_name || '_y%m%'
        AND tablename < table_name || '_y' || to_char(cutoff_date, 'YYYY') || 'm' || to_char(cutoff_date, 'MM')
    LOOP
        EXECUTE 'DROP TABLE IF EXISTS ' || partition_name;
    END LOOP;
END;
$$ LANGUAGE plpgsql;

-- 加密敏感字段的函数
CREATE OR REPLACE FUNCTION encrypt_sensitive_data(data TEXT, encryption_key TEXT DEFAULT 'skyeye_default_key')
RETURNS TEXT AS $$
BEGIN
    IF data IS NULL OR data = '' THEN
        RETURN data;
    END IF;
    RETURN encode(encrypt(data::bytea, encryption_key, 'aes'), 'base64');
END;
$$ LANGUAGE plpgsql;

-- 解密敏感字段的函数
CREATE OR REPLACE FUNCTION decrypt_sensitive_data(encrypted_data TEXT, encryption_key TEXT DEFAULT 'skyeye_default_key')
RETURNS TEXT AS $$
BEGIN
    IF encrypted_data IS NULL OR encrypted_data = '' THEN
        RETURN encrypted_data;
    END IF;
    RETURN convert_from(decrypt(decode(encrypted_data, 'base64'), encryption_key, 'aes'), 'UTF8');
EXCEPTION
    WHEN OTHERS THEN
        RETURN NULL;
END;
$$ LANGUAGE plpgsql;

-- 更新时间戳触发器函数
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 函数注释
COMMENT ON FUNCTION create_monthly_partition(TEXT, TIMESTAMP) IS '自动创建月度分区表';
COMMENT ON FUNCTION drop_old_partitions(TEXT, INTEGER) IS '清理旧的分区表';
COMMENT ON FUNCTION encrypt_sensitive_data(TEXT, TEXT) IS '加密敏感数据';
COMMENT ON FUNCTION decrypt_sensitive_data(TEXT, TEXT) IS '解密敏感数据';
COMMENT ON FUNCTION update_updated_at_column() IS '自动更新updated_at字段'; 