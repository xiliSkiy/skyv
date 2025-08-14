-- ================================================
-- 设备相关表字段修正和增强
-- 版本: V1.0.8
-- 创建时间: 2024-01-01
-- 说明: 为现有设备管理表添加缺失字段和功能
-- ================================================

-- 1. 为 tb_device_types 添加缺失字段
ALTER TABLE tb_device_types 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS is_enabled BOOLEAN DEFAULT TRUE,
ADD COLUMN IF NOT EXISTS device_count INTEGER DEFAULT 0;

-- 更新字段注释
COMMENT ON COLUMN tb_device_types.deleted_at IS '删除时间(软删除)';
COMMENT ON COLUMN tb_device_types.is_enabled IS '是否启用';
COMMENT ON COLUMN tb_device_types.device_count IS '设备数量统计';

-- 2. 为 tb_device_areas 添加缺失字段
ALTER TABLE tb_device_areas 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS area_type VARCHAR(20) DEFAULT 'other',
ADD COLUMN IF NOT EXISTS device_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS online_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS alert_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS status VARCHAR(20) DEFAULT 'normal';

-- 更新字段注释
COMMENT ON COLUMN tb_device_areas.deleted_at IS '删除时间(软删除)';
COMMENT ON COLUMN tb_device_areas.area_type IS '区域类型: building/floor/room/other';
COMMENT ON COLUMN tb_device_areas.device_count IS '设备数量统计';
COMMENT ON COLUMN tb_device_areas.online_count IS '在线设备数';
COMMENT ON COLUMN tb_device_areas.alert_count IS '报警数量';
COMMENT ON COLUMN tb_device_areas.status IS '区域状态: normal/abnormal';

-- 3. 为 tb_device_groups 添加缺失字段
ALTER TABLE tb_device_groups 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS group_type VARCHAR(20) DEFAULT 'normal',
ADD COLUMN IF NOT EXISTS code VARCHAR(50),
ADD COLUMN IF NOT EXISTS icon VARCHAR(100),
ADD COLUMN IF NOT EXISTS conditions JSONB,
ADD COLUMN IF NOT EXISTS device_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS online_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS offline_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS error_count INTEGER DEFAULT 0,
ADD COLUMN IF NOT EXISTS unknown_count INTEGER DEFAULT 0;

-- 为 code 字段添加唯一约束（如果不存在）
DO $$
BEGIN
    -- 先给已有记录生成code
    UPDATE tb_device_groups SET code = 'GROUP_' || id WHERE code IS NULL;
    
    -- 添加唯一约束
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint 
        WHERE conname = 'tb_device_groups_code_key'
    ) THEN
        ALTER TABLE tb_device_groups ADD CONSTRAINT tb_device_groups_code_key UNIQUE (code);
    END IF;
END $$;

-- 更新字段注释
COMMENT ON COLUMN tb_device_groups.deleted_at IS '删除时间(软删除)';
COMMENT ON COLUMN tb_device_groups.group_type IS '分组类型: normal/smart/dynamic';
COMMENT ON COLUMN tb_device_groups.code IS '分组编码';
COMMENT ON COLUMN tb_device_groups.icon IS '分组图标';
COMMENT ON COLUMN tb_device_groups.conditions IS '动态分组条件JSON';
COMMENT ON COLUMN tb_device_groups.device_count IS '设备数量统计';
COMMENT ON COLUMN tb_device_groups.online_count IS '在线设备数';
COMMENT ON COLUMN tb_device_groups.offline_count IS '离线设备数';
COMMENT ON COLUMN tb_device_groups.error_count IS '故障设备数';
COMMENT ON COLUMN tb_device_groups.unknown_count IS '未知状态设备数';

-- 4. 为 tb_device_tags 添加缺失字段
ALTER TABLE tb_device_tags 
ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP NULL,
ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP;

-- 更新字段注释
COMMENT ON COLUMN tb_device_tags.deleted_at IS '删除时间(软删除)';
COMMENT ON COLUMN tb_device_tags.updated_at IS '更新时间';

-- 5. 创建设备协议表
CREATE TABLE IF NOT EXISTS tb_device_protocols (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    code VARCHAR(50) NOT NULL UNIQUE,
    version VARCHAR(20),
    description TEXT,
    port INTEGER,
    config_schema JSONB,
    is_enabled BOOLEAN DEFAULT TRUE,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_device_protocols_code ON tb_device_protocols(code);
CREATE INDEX IF NOT EXISTS idx_device_protocols_enabled ON tb_device_protocols(is_enabled);

-- 添加注释
COMMENT ON TABLE tb_device_protocols IS '设备协议表';
COMMENT ON COLUMN tb_device_protocols.name IS '协议名称';
COMMENT ON COLUMN tb_device_protocols.code IS '协议编码';
COMMENT ON COLUMN tb_device_protocols.version IS '协议版本';
COMMENT ON COLUMN tb_device_protocols.description IS '协议描述';
COMMENT ON COLUMN tb_device_protocols.port IS '默认端口';
COMMENT ON COLUMN tb_device_protocols.config_schema IS '配置参数模式JSON';

-- 6. 创建设备模板表
CREATE TABLE IF NOT EXISTS tb_device_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) NOT NULL UNIQUE,
    device_type_id BIGINT REFERENCES tb_device_types(id) ON DELETE SET NULL,
    protocol_id BIGINT REFERENCES tb_device_protocols(id) ON DELETE SET NULL,
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    config_template JSONB,
    default_settings JSONB,
    metrics JSONB,
    description TEXT,
    is_enabled BOOLEAN DEFAULT TRUE,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMP NULL
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_device_templates_type_id ON tb_device_templates(device_type_id);
CREATE INDEX IF NOT EXISTS idx_device_templates_protocol_id ON tb_device_templates(protocol_id);
CREATE INDEX IF NOT EXISTS idx_device_templates_enabled ON tb_device_templates(is_enabled);

-- 添加注释
COMMENT ON TABLE tb_device_templates IS '设备模板表';
COMMENT ON COLUMN tb_device_templates.name IS '模板名称';
COMMENT ON COLUMN tb_device_templates.code IS '模板编码';
COMMENT ON COLUMN tb_device_templates.device_type_id IS '设备类型ID';
COMMENT ON COLUMN tb_device_templates.protocol_id IS '协议ID';

-- 7. 创建设备分组关联表
CREATE TABLE IF NOT EXISTS tb_device_group_relations (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL REFERENCES tb_devices(id) ON DELETE CASCADE,
    group_id BIGINT NOT NULL REFERENCES tb_device_groups(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(device_id, group_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_device_group_relations_device_id ON tb_device_group_relations(device_id);
CREATE INDEX IF NOT EXISTS idx_device_group_relations_group_id ON tb_device_group_relations(group_id);

-- 添加注释
COMMENT ON TABLE tb_device_group_relations IS '设备分组关联表';

-- 8. 为设备表添加关联字段
ALTER TABLE tb_devices ADD COLUMN IF NOT EXISTS protocol_id BIGINT REFERENCES tb_device_protocols(id) ON DELETE SET NULL;
ALTER TABLE tb_devices ADD COLUMN IF NOT EXISTS template_id BIGINT REFERENCES tb_device_templates(id) ON DELETE SET NULL;

-- 创建关联字段索引
CREATE INDEX IF NOT EXISTS idx_devices_protocol_id ON tb_devices(protocol_id);
CREATE INDEX IF NOT EXISTS idx_devices_template_id ON tb_devices(template_id);

-- 9. 创建更新时间触发器（如果不存在）
DO $$
BEGIN
    -- 为设备协议表创建更新时间触发器
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger 
        WHERE tgname = 'update_device_protocols_updated_at'
    ) THEN
        CREATE TRIGGER update_device_protocols_updated_at
            BEFORE UPDATE ON tb_device_protocols
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at_column();
    END IF;
    
    -- 为设备模板表创建更新时间触发器
    IF NOT EXISTS (
        SELECT 1 FROM pg_trigger 
        WHERE tgname = 'update_device_templates_updated_at'
    ) THEN
        CREATE TRIGGER update_device_templates_updated_at
            BEFORE UPDATE ON tb_device_templates
            FOR EACH ROW
            EXECUTE FUNCTION update_updated_at_column();
    END IF;
END $$; 