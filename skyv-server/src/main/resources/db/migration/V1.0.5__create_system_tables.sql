-- ========================================
-- SkyEye 智能监控系统 - 系统配置表
-- 版本: V1.0.5
-- 创建时间: 2024-12-19
-- 描述: 系统配置、操作日志相关表结构
-- ========================================

-- ========================================
-- 1. 系统配置表
-- ========================================
CREATE TABLE tb_system_settings (
    id BIGSERIAL PRIMARY KEY,
    group_name VARCHAR(50) NOT NULL,
    key_name VARCHAR(100) NOT NULL,
    value TEXT,
    value_type VARCHAR(20) DEFAULT 'STRING',
    default_value TEXT,
    description VARCHAR(200),
    is_encrypted BOOLEAN DEFAULT FALSE,
    is_readonly BOOLEAN DEFAULT FALSE,
    validation_rule VARCHAR(500),
    sort_order INTEGER DEFAULT 0,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(group_name, key_name),
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_system_settings_group ON tb_system_settings (group_name);
CREATE UNIQUE INDEX idx_system_settings_unique ON tb_system_settings (group_name, key_name);
CREATE INDEX idx_system_settings_type ON tb_system_settings (value_type);
CREATE INDEX idx_system_settings_encrypted ON tb_system_settings (is_encrypted);
CREATE INDEX idx_system_settings_readonly ON tb_system_settings (is_readonly);
CREATE INDEX idx_system_settings_sort ON tb_system_settings (group_name, sort_order);

-- 创建更新时间触发器
CREATE TRIGGER update_system_settings_updated_at
    BEFORE UPDATE ON tb_system_settings
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_system_settings IS '系统配置表';
COMMENT ON COLUMN tb_system_settings.id IS '配置ID';
COMMENT ON COLUMN tb_system_settings.group_name IS '配置分组';
COMMENT ON COLUMN tb_system_settings.key_name IS '配置键名';
COMMENT ON COLUMN tb_system_settings.value IS '配置值';
COMMENT ON COLUMN tb_system_settings.value_type IS '值类型：STRING,INTEGER,BOOLEAN,JSON';
COMMENT ON COLUMN tb_system_settings.default_value IS '默认值';
COMMENT ON COLUMN tb_system_settings.description IS '配置描述';
COMMENT ON COLUMN tb_system_settings.is_encrypted IS '是否加密存储';
COMMENT ON COLUMN tb_system_settings.is_readonly IS '是否只读';
COMMENT ON COLUMN tb_system_settings.validation_rule IS '验证规则';
COMMENT ON COLUMN tb_system_settings.sort_order IS '排序';
COMMENT ON COLUMN tb_system_settings.updated_by IS '更新人';

-- ========================================
-- 2. 操作日志表（分区表）
-- ========================================
CREATE TABLE tb_operation_logs (
    id BIGSERIAL,
    
    -- 用户信息
    user_id BIGINT,
    username VARCHAR(50),
    
    -- 操作信息
    module VARCHAR(50) NOT NULL,
    operation VARCHAR(100) NOT NULL,
    operation_desc VARCHAR(200),
    
    -- 目标信息
    target_type VARCHAR(50),
    target_id BIGINT,
    target_name VARCHAR(200),
    
    -- 请求信息
    request_method VARCHAR(10),
    request_url VARCHAR(500),
    request_params JSONB,
    
    -- 响应信息
    response_status INTEGER,
    response_time INTEGER,
    
    -- 详细信息
    details JSONB,
    before_data JSONB,
    after_data JSONB,
    
    -- 客户端信息
    ip_address INET,
    user_agent TEXT,
    
    -- 结果信息
    success BOOLEAN DEFAULT TRUE,
    error_message TEXT,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id, created_at),
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE SET NULL
) PARTITION BY RANGE (created_at);

-- 创建当前年度的月度分区表
SELECT create_monthly_partition('tb_operation_logs', DATE_TRUNC('month', CURRENT_DATE));
SELECT create_monthly_partition('tb_operation_logs', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month');
SELECT create_monthly_partition('tb_operation_logs', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '2 months');

-- 创建索引
CREATE INDEX idx_operation_logs_user ON tb_operation_logs (user_id, created_at DESC);
CREATE INDEX idx_operation_logs_module ON tb_operation_logs (module, created_at DESC);
CREATE INDEX idx_operation_logs_operation ON tb_operation_logs (operation, created_at DESC);
CREATE INDEX idx_operation_logs_target ON tb_operation_logs (target_type, target_id, created_at DESC);
CREATE INDEX idx_operation_logs_ip ON tb_operation_logs (ip_address, created_at DESC);
CREATE INDEX idx_operation_logs_success ON tb_operation_logs (success, created_at DESC);
CREATE INDEX idx_operation_logs_username ON tb_operation_logs (username, created_at DESC);

-- 添加注释
COMMENT ON TABLE tb_operation_logs IS '操作日志表（按月分区）';
COMMENT ON COLUMN tb_operation_logs.id IS '日志ID';
COMMENT ON COLUMN tb_operation_logs.user_id IS '操作用户ID';
COMMENT ON COLUMN tb_operation_logs.username IS '用户名';
COMMENT ON COLUMN tb_operation_logs.module IS '操作模块';
COMMENT ON COLUMN tb_operation_logs.operation IS '操作类型';
COMMENT ON COLUMN tb_operation_logs.operation_desc IS '操作描述';
COMMENT ON COLUMN tb_operation_logs.target_type IS '操作对象类型';
COMMENT ON COLUMN tb_operation_logs.target_id IS '操作对象ID';
COMMENT ON COLUMN tb_operation_logs.target_name IS '操作对象名称';
COMMENT ON COLUMN tb_operation_logs.request_method IS '请求方法';
COMMENT ON COLUMN tb_operation_logs.request_url IS '请求URL';
COMMENT ON COLUMN tb_operation_logs.request_params IS '请求参数JSON';
COMMENT ON COLUMN tb_operation_logs.response_status IS '响应状态码';
COMMENT ON COLUMN tb_operation_logs.response_time IS '响应时间(毫秒)';
COMMENT ON COLUMN tb_operation_logs.details IS '操作详情JSON';
COMMENT ON COLUMN tb_operation_logs.before_data IS '操作前数据JSON';
COMMENT ON COLUMN tb_operation_logs.after_data IS '操作后数据JSON';
COMMENT ON COLUMN tb_operation_logs.ip_address IS 'IP地址';
COMMENT ON COLUMN tb_operation_logs.user_agent IS '用户代理';
COMMENT ON COLUMN tb_operation_logs.success IS '是否成功';
COMMENT ON COLUMN tb_operation_logs.error_message IS '错误信息';

-- ========================================
-- 3. 系统通知表
-- ========================================
CREATE TABLE tb_system_notifications (
    id BIGSERIAL PRIMARY KEY,
    
    -- 通知信息
    title VARCHAR(200) NOT NULL,
    content TEXT,
    type VARCHAR(20) DEFAULT 'INFO',
    level VARCHAR(20) DEFAULT 'NORMAL',
    
    -- 目标信息
    target_type VARCHAR(20) DEFAULT 'ALL',
    target_users JSONB,
    target_roles JSONB,
    
    -- 状态信息
    status VARCHAR(20) DEFAULT 'ACTIVE',
    is_read BOOLEAN DEFAULT FALSE,
    
    -- 显示配置
    show_popup BOOLEAN DEFAULT FALSE,
    auto_close BOOLEAN DEFAULT TRUE,
    close_timeout INTEGER DEFAULT 5000,
    
    -- 时间信息
    publish_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expire_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 创建信息
    created_by BIGINT,
    updated_by BIGINT,
    
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_system_notifications_type ON tb_system_notifications (type);
CREATE INDEX idx_system_notifications_level ON tb_system_notifications (level);
CREATE INDEX idx_system_notifications_status ON tb_system_notifications (status);
CREATE INDEX idx_system_notifications_target_type ON tb_system_notifications (target_type);
CREATE INDEX idx_system_notifications_publish ON tb_system_notifications (publish_at DESC);
CREATE INDEX idx_system_notifications_expire ON tb_system_notifications (expire_at);
CREATE INDEX idx_system_notifications_created_by ON tb_system_notifications (created_by);

-- 创建更新时间触发器
CREATE TRIGGER update_system_notifications_updated_at
    BEFORE UPDATE ON tb_system_notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_system_notifications IS '系统通知表';
COMMENT ON COLUMN tb_system_notifications.id IS '通知ID';
COMMENT ON COLUMN tb_system_notifications.title IS '通知标题';
COMMENT ON COLUMN tb_system_notifications.content IS '通知内容';
COMMENT ON COLUMN tb_system_notifications.type IS '通知类型：INFO,WARNING,ERROR,SUCCESS';
COMMENT ON COLUMN tb_system_notifications.level IS '通知级别：NORMAL,IMPORTANT,URGENT';
COMMENT ON COLUMN tb_system_notifications.target_type IS '目标类型：ALL,USER,ROLE';
COMMENT ON COLUMN tb_system_notifications.target_users IS '目标用户JSON数组';
COMMENT ON COLUMN tb_system_notifications.target_roles IS '目标角色JSON数组';
COMMENT ON COLUMN tb_system_notifications.status IS '状态：ACTIVE,INACTIVE,EXPIRED';
COMMENT ON COLUMN tb_system_notifications.is_read IS '是否已读';
COMMENT ON COLUMN tb_system_notifications.show_popup IS '是否弹窗显示';
COMMENT ON COLUMN tb_system_notifications.auto_close IS '是否自动关闭';
COMMENT ON COLUMN tb_system_notifications.close_timeout IS '自动关闭时间(毫秒)';
COMMENT ON COLUMN tb_system_notifications.publish_at IS '发布时间';
COMMENT ON COLUMN tb_system_notifications.expire_at IS '过期时间';

-- ========================================
-- 4. 用户通知读取记录表
-- ========================================
CREATE TABLE tb_user_notification_reads (
    user_id BIGINT NOT NULL,
    notification_id BIGINT NOT NULL,
    read_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, notification_id),
    FOREIGN KEY (user_id) REFERENCES tb_users(id) ON DELETE CASCADE,
    FOREIGN KEY (notification_id) REFERENCES tb_system_notifications(id) ON DELETE CASCADE
);

-- 创建索引
CREATE INDEX idx_user_notification_reads_user ON tb_user_notification_reads (user_id, read_at DESC);
CREATE INDEX idx_user_notification_reads_notification ON tb_user_notification_reads (notification_id);

-- 添加注释
COMMENT ON TABLE tb_user_notification_reads IS '用户通知读取记录表';
COMMENT ON COLUMN tb_user_notification_reads.user_id IS '用户ID';
COMMENT ON COLUMN tb_user_notification_reads.notification_id IS '通知ID';
COMMENT ON COLUMN tb_user_notification_reads.read_at IS '读取时间';

-- ========================================
-- 5. 文件管理表
-- ========================================
CREATE TABLE tb_file_storage (
    id BIGSERIAL PRIMARY KEY,
    
    -- 文件信息
    original_name VARCHAR(255) NOT NULL,
    stored_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT NOT NULL,
    file_type VARCHAR(100),
    mime_type VARCHAR(100),
    
    -- 文件分类
    category VARCHAR(50),
    module VARCHAR(50),
    business_type VARCHAR(50),
    business_id BIGINT,
    
    -- 存储信息
    storage_type VARCHAR(20) DEFAULT 'LOCAL',
    storage_config JSONB,
    
    -- 访问控制
    is_public BOOLEAN DEFAULT FALSE,
    access_token VARCHAR(100),
    
    -- 状态信息
    status INTEGER DEFAULT 1,
    download_count INTEGER DEFAULT 0,
    
    -- 时间信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- 创建信息
    created_by BIGINT,
    updated_by BIGINT,
    
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_file_storage_category ON tb_file_storage (category);
CREATE INDEX idx_file_storage_module ON tb_file_storage (module);
CREATE INDEX idx_file_storage_business ON tb_file_storage (business_type, business_id);
CREATE INDEX idx_file_storage_type ON tb_file_storage (file_type);
CREATE INDEX idx_file_storage_storage_type ON tb_file_storage (storage_type);
CREATE INDEX idx_file_storage_public ON tb_file_storage (is_public);
CREATE INDEX idx_file_storage_status ON tb_file_storage (status);
CREATE INDEX idx_file_storage_created_by ON tb_file_storage (created_by);
CREATE INDEX idx_file_storage_created_at ON tb_file_storage (created_at DESC);

-- 创建更新时间触发器
CREATE TRIGGER update_file_storage_updated_at
    BEFORE UPDATE ON tb_file_storage
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_file_storage IS '文件存储表';
COMMENT ON COLUMN tb_file_storage.id IS '文件ID';
COMMENT ON COLUMN tb_file_storage.original_name IS '原始文件名';
COMMENT ON COLUMN tb_file_storage.stored_name IS '存储文件名';
COMMENT ON COLUMN tb_file_storage.file_path IS '文件路径';
COMMENT ON COLUMN tb_file_storage.file_size IS '文件大小(字节)';
COMMENT ON COLUMN tb_file_storage.file_type IS '文件类型';
COMMENT ON COLUMN tb_file_storage.mime_type IS 'MIME类型';
COMMENT ON COLUMN tb_file_storage.category IS '文件分类';
COMMENT ON COLUMN tb_file_storage.module IS '所属模块';
COMMENT ON COLUMN tb_file_storage.business_type IS '业务类型';
COMMENT ON COLUMN tb_file_storage.business_id IS '业务ID';
COMMENT ON COLUMN tb_file_storage.storage_type IS '存储类型：LOCAL,OSS,COS';
COMMENT ON COLUMN tb_file_storage.storage_config IS '存储配置JSON';
COMMENT ON COLUMN tb_file_storage.is_public IS '是否公开访问';
COMMENT ON COLUMN tb_file_storage.access_token IS '访问令牌';
COMMENT ON COLUMN tb_file_storage.status IS '状态：1正常 0删除';
COMMENT ON COLUMN tb_file_storage.download_count IS '下载次数'; 