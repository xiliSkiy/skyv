-- ========================================
-- SkyEye 智能监控系统 - 设备管理表
-- 版本: V1.0.2
-- 创建时间: 2024-12-19
-- 描述: 设备类型、区域、分组、标签、设备信息相关表结构
-- ========================================

-- ========================================
-- 1. 设备类型表
-- ========================================
CREATE TABLE tb_device_types (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50) UNIQUE NOT NULL,
    parent_id BIGINT,
    level INTEGER DEFAULT 0,
    icon VARCHAR(100),
    description TEXT,
    protocol_types JSONB,
    default_config JSONB,
    is_system BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (parent_id) REFERENCES tb_device_types(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_device_types_parent ON tb_device_types (parent_id);
CREATE INDEX idx_device_types_code ON tb_device_types (code);
CREATE INDEX idx_device_types_level ON tb_device_types (level);
CREATE INDEX idx_device_types_system ON tb_device_types (is_system);
CREATE INDEX idx_device_types_sort_order ON tb_device_types (sort_order);

-- 创建更新时间触发器
CREATE TRIGGER update_device_types_updated_at
    BEFORE UPDATE ON tb_device_types
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_device_types IS '设备类型表';
COMMENT ON COLUMN tb_device_types.id IS '设备类型ID';
COMMENT ON COLUMN tb_device_types.name IS '类型名称';
COMMENT ON COLUMN tb_device_types.code IS '类型编码';
COMMENT ON COLUMN tb_device_types.parent_id IS '父类型ID';
COMMENT ON COLUMN tb_device_types.level IS '层级';
COMMENT ON COLUMN tb_device_types.icon IS '图标';
COMMENT ON COLUMN tb_device_types.description IS '描述';
COMMENT ON COLUMN tb_device_types.protocol_types IS '支持的协议类型JSON';
COMMENT ON COLUMN tb_device_types.default_config IS '默认配置模板JSON';
COMMENT ON COLUMN tb_device_types.is_system IS '是否系统类型';
COMMENT ON COLUMN tb_device_types.sort_order IS '排序';

-- ========================================
-- 2. 设备区域表
-- ========================================
CREATE TABLE tb_device_areas (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    code VARCHAR(50),
    parent_id BIGINT,
    level INTEGER DEFAULT 0,
    full_path VARCHAR(500),
    description TEXT,
    location_info JSONB,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    FOREIGN KEY (parent_id) REFERENCES tb_device_areas(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_device_areas_parent ON tb_device_areas (parent_id);
CREATE INDEX idx_device_areas_code ON tb_device_areas (code);
CREATE INDEX idx_device_areas_level ON tb_device_areas (level);
CREATE INDEX idx_device_areas_sort_order ON tb_device_areas (sort_order);

-- 创建更新时间触发器
CREATE TRIGGER update_device_areas_updated_at
    BEFORE UPDATE ON tb_device_areas
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_device_areas IS '设备区域表';
COMMENT ON COLUMN tb_device_areas.id IS '区域ID';
COMMENT ON COLUMN tb_device_areas.name IS '区域名称';
COMMENT ON COLUMN tb_device_areas.code IS '区域编码';
COMMENT ON COLUMN tb_device_areas.parent_id IS '父区域ID';
COMMENT ON COLUMN tb_device_areas.level IS '层级';
COMMENT ON COLUMN tb_device_areas.full_path IS '完整路径';
COMMENT ON COLUMN tb_device_areas.description IS '区域描述';
COMMENT ON COLUMN tb_device_areas.location_info IS '位置信息JSON(经纬度等)';
COMMENT ON COLUMN tb_device_areas.sort_order IS '排序';

-- ========================================
-- 3. 设备分组表
-- ========================================
CREATE TABLE tb_device_groups (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    created_by BIGINT,
    is_system BOOLEAN DEFAULT FALSE,
    sort_order INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_device_groups_created_by ON tb_device_groups (created_by);
CREATE INDEX idx_device_groups_system ON tb_device_groups (is_system);
CREATE INDEX idx_device_groups_sort_order ON tb_device_groups (sort_order);

-- 创建更新时间触发器
CREATE TRIGGER update_device_groups_updated_at
    BEFORE UPDATE ON tb_device_groups
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_device_groups IS '设备分组表';
COMMENT ON COLUMN tb_device_groups.id IS '分组ID';
COMMENT ON COLUMN tb_device_groups.name IS '分组名称';
COMMENT ON COLUMN tb_device_groups.description IS '分组描述';
COMMENT ON COLUMN tb_device_groups.color IS '分组颜色';
COMMENT ON COLUMN tb_device_groups.created_by IS '创建人';
COMMENT ON COLUMN tb_device_groups.is_system IS '是否系统分组';
COMMENT ON COLUMN tb_device_groups.sort_order IS '排序';

-- ========================================
-- 4. 设备标签表
-- ========================================
CREATE TABLE tb_device_tags (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) UNIQUE NOT NULL,
    color VARCHAR(7) DEFAULT '#409EFF',
    description VARCHAR(200),
    created_by BIGINT,
    usage_count INTEGER DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_device_tags_name ON tb_device_tags (name);
CREATE INDEX idx_device_tags_created_by ON tb_device_tags (created_by);
CREATE INDEX idx_device_tags_usage_count ON tb_device_tags (usage_count DESC);

-- 添加注释
COMMENT ON TABLE tb_device_tags IS '设备标签表';
COMMENT ON COLUMN tb_device_tags.id IS '标签ID';
COMMENT ON COLUMN tb_device_tags.name IS '标签名称';
COMMENT ON COLUMN tb_device_tags.color IS '标签颜色';
COMMENT ON COLUMN tb_device_tags.description IS '标签描述';
COMMENT ON COLUMN tb_device_tags.created_by IS '创建人';
COMMENT ON COLUMN tb_device_tags.usage_count IS '使用次数';

-- ========================================
-- 5. 设备表
-- ========================================
CREATE TABLE tb_devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    device_type_id BIGINT NOT NULL,
    area_id BIGINT,
    group_id BIGINT,
    
    -- 网络信息
    ip_address INET,
    port INTEGER,
    mac_address MACADDR,
    protocol VARCHAR(20),
    
    -- 设备状态
    status INTEGER DEFAULT 1,
    health_score INTEGER DEFAULT 100,
    
    -- 设备配置
    config JSONB,
    credentials JSONB,
    
    -- 位置信息
    location VARCHAR(200),
    coordinates POINT,
    
    -- 设备信息
    manufacturer VARCHAR(100),
    model VARCHAR(100),
    serial_number VARCHAR(100),
    firmware_version VARCHAR(50),
    purchase_date DATE,
    warranty_date DATE,
    
    -- 其他信息
    description TEXT,
    remark TEXT,
    
    -- 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_online_at TIMESTAMP,
    last_collect_at TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    FOREIGN KEY (device_type_id) REFERENCES tb_device_types(id) ON DELETE RESTRICT,
    FOREIGN KEY (area_id) REFERENCES tb_device_areas(id) ON DELETE SET NULL,
    FOREIGN KEY (group_id) REFERENCES tb_device_groups(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_devices_type ON tb_devices (device_type_id);
CREATE INDEX idx_devices_area ON tb_devices (area_id);
CREATE INDEX idx_devices_group ON tb_devices (group_id);
CREATE INDEX idx_devices_status ON tb_devices (status);
CREATE INDEX idx_devices_ip ON tb_devices (ip_address);
CREATE INDEX idx_devices_name ON tb_devices USING gin(to_tsvector('english', name));
CREATE INDEX idx_devices_updated ON tb_devices (updated_at DESC);
CREATE INDEX idx_devices_health_score ON tb_devices (health_score);
CREATE INDEX idx_devices_last_online ON tb_devices (last_online_at DESC);
CREATE INDEX idx_devices_manufacturer ON tb_devices (manufacturer);
CREATE INDEX idx_devices_model ON tb_devices (model);
CREATE INDEX idx_devices_serial_number ON tb_devices (serial_number);

-- 创建复合索引优化常用查询
CREATE INDEX idx_devices_type_status_area ON tb_devices (device_type_id, status, area_id);
CREATE INDEX idx_devices_status_health ON tb_devices (status, health_score);

-- 创建更新时间触发器
CREATE TRIGGER update_devices_updated_at
    BEFORE UPDATE ON tb_devices
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_devices IS '设备信息表';
COMMENT ON COLUMN tb_devices.id IS '设备ID';
COMMENT ON COLUMN tb_devices.name IS '设备名称';
COMMENT ON COLUMN tb_devices.device_type_id IS '设备类型ID';
COMMENT ON COLUMN tb_devices.area_id IS '设备区域ID';
COMMENT ON COLUMN tb_devices.group_id IS '设备分组ID';
COMMENT ON COLUMN tb_devices.ip_address IS '设备IP地址';
COMMENT ON COLUMN tb_devices.port IS '设备端口';
COMMENT ON COLUMN tb_devices.mac_address IS 'MAC地址';
COMMENT ON COLUMN tb_devices.protocol IS '通信协议';
COMMENT ON COLUMN tb_devices.status IS '设备状态：1在线 0离线 2故障 3维护';
COMMENT ON COLUMN tb_devices.health_score IS '健康评分0-100';
COMMENT ON COLUMN tb_devices.config IS '设备配置JSON';
COMMENT ON COLUMN tb_devices.credentials IS '认证信息JSON(加密存储)';
COMMENT ON COLUMN tb_devices.location IS '物理位置描述';
COMMENT ON COLUMN tb_devices.coordinates IS '地理坐标';
COMMENT ON COLUMN tb_devices.manufacturer IS '制造商';
COMMENT ON COLUMN tb_devices.model IS '设备型号';
COMMENT ON COLUMN tb_devices.serial_number IS '序列号';
COMMENT ON COLUMN tb_devices.firmware_version IS '固件版本';
COMMENT ON COLUMN tb_devices.purchase_date IS '采购日期';
COMMENT ON COLUMN tb_devices.warranty_date IS '保修到期日期';
COMMENT ON COLUMN tb_devices.description IS '设备描述';
COMMENT ON COLUMN tb_devices.remark IS '备注信息';
COMMENT ON COLUMN tb_devices.last_online_at IS '最后在线时间';
COMMENT ON COLUMN tb_devices.last_collect_at IS '最后采集时间';

-- ========================================
-- 6. 设备标签关联表
-- ========================================
CREATE TABLE tb_device_tag_relations (
    device_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    PRIMARY KEY (device_id, tag_id),
    FOREIGN KEY (device_id) REFERENCES tb_devices(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tb_device_tags(id) ON DELETE CASCADE,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_device_tags_device ON tb_device_tag_relations (device_id);
CREATE INDEX idx_device_tags_tag ON tb_device_tag_relations (tag_id);
CREATE INDEX idx_device_tags_created_at ON tb_device_tag_relations (created_at DESC);

-- 添加注释
COMMENT ON TABLE tb_device_tag_relations IS '设备标签关联表';
COMMENT ON COLUMN tb_device_tag_relations.device_id IS '设备ID';
COMMENT ON COLUMN tb_device_tag_relations.tag_id IS '标签ID';
COMMENT ON COLUMN tb_device_tag_relations.created_at IS '关联时间';
COMMENT ON COLUMN tb_device_tag_relations.created_by IS '创建人'; 