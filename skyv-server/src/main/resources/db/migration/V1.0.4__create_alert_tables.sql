-- ========================================
-- SkyEye 智能监控系统 - 报警管理表
-- 版本: V1.0.4
-- 创建时间: 2024-12-19
-- 描述: 报警规则、报警记录相关表结构
-- ========================================

-- ========================================
-- 1. 报警规则表
-- ========================================
CREATE TABLE tb_alert_rules (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- 适用范围
    device_type_id BIGINT,
    device_group_id BIGINT,
    target_devices JSONB,
    
    -- 触发条件
    metric_name VARCHAR(100),
    condition_type VARCHAR(20) NOT NULL,
    threshold_value DECIMAL(20,6),
    threshold_config JSONB,
    
    -- 报警级别
    level VARCHAR(20) NOT NULL,
    severity INTEGER DEFAULT 1,
    
    -- 触发配置
    continuous_count INTEGER DEFAULT 1,
    time_window INTEGER DEFAULT 300,
    recovery_condition JSONB,
    
    -- 通知配置
    notification_config JSONB,
    escalation_config JSONB,
    
    -- 规则状态
    enabled BOOLEAN DEFAULT TRUE,
    
    -- 统计信息
    trigger_count BIGINT DEFAULT 0,
    last_trigger_at TIMESTAMP,
    
    -- 创建信息
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    FOREIGN KEY (device_type_id) REFERENCES tb_device_types(id) ON DELETE SET NULL,
    FOREIGN KEY (device_group_id) REFERENCES tb_device_groups(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_alert_rules_device_type ON tb_alert_rules (device_type_id);
CREATE INDEX idx_alert_rules_device_group ON tb_alert_rules (device_group_id);
CREATE INDEX idx_alert_rules_metric ON tb_alert_rules (metric_name);
CREATE INDEX idx_alert_rules_level ON tb_alert_rules (level);
CREATE INDEX idx_alert_rules_enabled ON tb_alert_rules (enabled);
CREATE INDEX idx_alert_rules_condition ON tb_alert_rules (condition_type);
CREATE INDEX idx_alert_rules_severity ON tb_alert_rules (severity DESC);
CREATE INDEX idx_alert_rules_trigger_count ON tb_alert_rules (trigger_count DESC);

-- 创建更新时间触发器
CREATE TRIGGER update_alert_rules_updated_at
    BEFORE UPDATE ON tb_alert_rules
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_alert_rules IS '报警规则表';
COMMENT ON COLUMN tb_alert_rules.id IS '规则ID';
COMMENT ON COLUMN tb_alert_rules.name IS '规则名称';
COMMENT ON COLUMN tb_alert_rules.description IS '规则描述';
COMMENT ON COLUMN tb_alert_rules.device_type_id IS '设备类型ID';
COMMENT ON COLUMN tb_alert_rules.device_group_id IS '设备分组ID';
COMMENT ON COLUMN tb_alert_rules.target_devices IS '目标设备JSON数组';
COMMENT ON COLUMN tb_alert_rules.metric_name IS '指标名称';
COMMENT ON COLUMN tb_alert_rules.condition_type IS '条件类型：GT,LT,EQ,NE,IN,CONTAINS';
COMMENT ON COLUMN tb_alert_rules.threshold_value IS '阈值';
COMMENT ON COLUMN tb_alert_rules.threshold_config IS '复杂阈值配置JSON';
COMMENT ON COLUMN tb_alert_rules.level IS '报警级别：CRITICAL,HIGH,MEDIUM,LOW';
COMMENT ON COLUMN tb_alert_rules.severity IS '严重程度1-10';
COMMENT ON COLUMN tb_alert_rules.continuous_count IS '连续触发次数';
COMMENT ON COLUMN tb_alert_rules.time_window IS '时间窗口(秒)';
COMMENT ON COLUMN tb_alert_rules.recovery_condition IS '恢复条件JSON';
COMMENT ON COLUMN tb_alert_rules.notification_config IS '通知配置JSON';
COMMENT ON COLUMN tb_alert_rules.escalation_config IS '升级配置JSON';
COMMENT ON COLUMN tb_alert_rules.enabled IS '是否启用';
COMMENT ON COLUMN tb_alert_rules.trigger_count IS '触发次数';
COMMENT ON COLUMN tb_alert_rules.last_trigger_at IS '最后触发时间';

-- ========================================
-- 2. 报警记录表（分区表）
-- ========================================
CREATE TABLE tb_alerts (
    id BIGSERIAL,
    
    -- 关联信息
    rule_id BIGINT,
    device_id BIGINT NOT NULL,
    
    -- 报警信息
    title VARCHAR(200) NOT NULL,
    description TEXT,
    level VARCHAR(20) NOT NULL,
    category VARCHAR(50),
    
    -- 触发信息
    trigger_value DECIMAL(20,6),
    threshold_value DECIMAL(20,6),
    trigger_condition VARCHAR(500),
    trigger_data JSONB,
    
    -- 状态信息
    status VARCHAR(20) DEFAULT 'PENDING',
    
    -- 处理信息
    processed_by BIGINT,
    processed_at TIMESTAMP,
    process_note TEXT,
    resolution TEXT,
    
    -- 通知信息
    notification_sent BOOLEAN DEFAULT FALSE,
    notification_log JSONB,
    
    -- 时间信息
    trigger_time TIMESTAMP NOT NULL,
    resolved_time TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id, trigger_time),
    FOREIGN KEY (rule_id) REFERENCES tb_alert_rules(id) ON DELETE SET NULL,
    FOREIGN KEY (device_id) REFERENCES tb_devices(id) ON DELETE CASCADE,
    FOREIGN KEY (processed_by) REFERENCES tb_users(id) ON DELETE SET NULL
) PARTITION BY RANGE (trigger_time);

-- 创建当前年度的月度分区表
SELECT create_monthly_partition('tb_alerts', DATE_TRUNC('month', CURRENT_DATE));
SELECT create_monthly_partition('tb_alerts', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month');
SELECT create_monthly_partition('tb_alerts', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '2 months');

-- 创建索引
CREATE INDEX idx_alerts_device_time ON tb_alerts (device_id, trigger_time DESC);
CREATE INDEX idx_alerts_status ON tb_alerts (status, trigger_time DESC);
CREATE INDEX idx_alerts_level ON tb_alerts (level, trigger_time DESC);
CREATE INDEX idx_alerts_rule ON tb_alerts (rule_id, trigger_time DESC);
CREATE INDEX idx_alerts_category ON tb_alerts (category, trigger_time DESC);
CREATE INDEX idx_alerts_processed_by ON tb_alerts (processed_by, processed_at DESC);
CREATE INDEX idx_alerts_notification ON tb_alerts (notification_sent, trigger_time DESC);

-- 添加注释
COMMENT ON TABLE tb_alerts IS '报警记录表（按月分区）';
COMMENT ON COLUMN tb_alerts.id IS '报警ID';
COMMENT ON COLUMN tb_alerts.rule_id IS '报警规则ID';
COMMENT ON COLUMN tb_alerts.device_id IS '设备ID';
COMMENT ON COLUMN tb_alerts.title IS '报警标题';
COMMENT ON COLUMN tb_alerts.description IS '报警描述';
COMMENT ON COLUMN tb_alerts.level IS '报警级别';
COMMENT ON COLUMN tb_alerts.category IS '报警分类';
COMMENT ON COLUMN tb_alerts.trigger_value IS '触发值';
COMMENT ON COLUMN tb_alerts.threshold_value IS '阈值';
COMMENT ON COLUMN tb_alerts.trigger_condition IS '触发条件描述';
COMMENT ON COLUMN tb_alerts.trigger_data IS '触发数据JSON';
COMMENT ON COLUMN tb_alerts.status IS '状态：PENDING,PROCESSING,RESOLVED,IGNORED,CLOSED';
COMMENT ON COLUMN tb_alerts.processed_by IS '处理人';
COMMENT ON COLUMN tb_alerts.processed_at IS '处理时间';
COMMENT ON COLUMN tb_alerts.process_note IS '处理备注';
COMMENT ON COLUMN tb_alerts.resolution IS '解决方案';
COMMENT ON COLUMN tb_alerts.notification_sent IS '是否已发送通知';
COMMENT ON COLUMN tb_alerts.notification_log IS '通知发送日志JSON';
COMMENT ON COLUMN tb_alerts.trigger_time IS '触发时间';
COMMENT ON COLUMN tb_alerts.resolved_time IS '解决时间';

-- ========================================
-- 3. 报警通知配置表
-- ========================================
CREATE TABLE tb_alert_notifications (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL,
    config JSONB NOT NULL,
    enabled BOOLEAN DEFAULT TRUE,
    
    -- 过滤条件
    level_filter JSONB,
    device_filter JSONB,
    time_filter JSONB,
    
    -- 统计信息
    total_sent INTEGER DEFAULT 0,
    success_sent INTEGER DEFAULT 0,
    last_sent_at TIMESTAMP,
    
    -- 创建信息
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_alert_notifications_type ON tb_alert_notifications (type);
CREATE INDEX idx_alert_notifications_enabled ON tb_alert_notifications (enabled);
CREATE INDEX idx_alert_notifications_created_by ON tb_alert_notifications (created_by);

-- 创建更新时间触发器
CREATE TRIGGER update_alert_notifications_updated_at
    BEFORE UPDATE ON tb_alert_notifications
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_alert_notifications IS '报警通知配置表';
COMMENT ON COLUMN tb_alert_notifications.id IS '通知配置ID';
COMMENT ON COLUMN tb_alert_notifications.name IS '配置名称';
COMMENT ON COLUMN tb_alert_notifications.type IS '通知类型：EMAIL,SMS,WEBHOOK,WECHAT';
COMMENT ON COLUMN tb_alert_notifications.config IS '通知配置JSON';
COMMENT ON COLUMN tb_alert_notifications.enabled IS '是否启用';
COMMENT ON COLUMN tb_alert_notifications.level_filter IS '级别过滤条件JSON';
COMMENT ON COLUMN tb_alert_notifications.device_filter IS '设备过滤条件JSON';
COMMENT ON COLUMN tb_alert_notifications.time_filter IS '时间过滤条件JSON';
COMMENT ON COLUMN tb_alert_notifications.total_sent IS '总发送次数';
COMMENT ON COLUMN tb_alert_notifications.success_sent IS '成功发送次数';
COMMENT ON COLUMN tb_alert_notifications.last_sent_at IS '最后发送时间';

-- ========================================
-- 4. 报警处理记录表
-- ========================================
CREATE TABLE tb_alert_processes (
    id BIGSERIAL PRIMARY KEY,
    alert_id BIGINT NOT NULL,
    
    -- 处理信息
    action VARCHAR(50) NOT NULL,
    operator_id BIGINT,
    operator_name VARCHAR(100),
    
    -- 处理内容
    content TEXT,
    attachments JSONB,
    
    -- 处理结果
    result VARCHAR(20),
    
    -- 时间信息
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (operator_id) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_alert_processes_alert ON tb_alert_processes (alert_id, created_at DESC);
CREATE INDEX idx_alert_processes_operator ON tb_alert_processes (operator_id, created_at DESC);
CREATE INDEX idx_alert_processes_action ON tb_alert_processes (action, created_at DESC);
CREATE INDEX idx_alert_processes_result ON tb_alert_processes (result);

-- 添加注释
COMMENT ON TABLE tb_alert_processes IS '报警处理记录表';
COMMENT ON COLUMN tb_alert_processes.id IS '处理记录ID';
COMMENT ON COLUMN tb_alert_processes.alert_id IS '报警ID';
COMMENT ON COLUMN tb_alert_processes.action IS '处理动作：ASSIGN,PROCESS,RESOLVE,IGNORE,CLOSE';
COMMENT ON COLUMN tb_alert_processes.operator_id IS '操作人ID';
COMMENT ON COLUMN tb_alert_processes.operator_name IS '操作人姓名';
COMMENT ON COLUMN tb_alert_processes.content IS '处理内容';
COMMENT ON COLUMN tb_alert_processes.attachments IS '附件JSON';
COMMENT ON COLUMN tb_alert_processes.result IS '处理结果：SUCCESS,FAILED';
COMMENT ON COLUMN tb_alert_processes.created_at IS '处理时间'; 