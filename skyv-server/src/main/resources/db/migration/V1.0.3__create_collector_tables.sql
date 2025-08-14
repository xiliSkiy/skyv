-- ========================================
-- SkyEye 智能监控系统 - 数据采集表
-- 版本: V1.0.3
-- 创建时间: 2024-12-19
-- 描述: 采集器、指标模板、采集任务、采集数据相关表结构
-- ========================================

-- ========================================
-- 1. 采集器表
-- ========================================
CREATE TABLE tb_collectors (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    version VARCHAR(20),
    
    -- 状态信息
    status INTEGER DEFAULT 1,
    health_status VARCHAR(20) DEFAULT 'HEALTHY',
    load_percentage INTEGER DEFAULT 0,
    
    -- 配置信息
    config JSONB,
    capabilities JSONB,
    
    -- 网络信息
    host_ip INET,
    network_zone VARCHAR(100),
    
    -- 主从信息
    is_main BOOLEAN DEFAULT FALSE,
    master_id BIGINT,
    
    -- 统计信息
    total_devices INTEGER DEFAULT 0,
    active_tasks INTEGER DEFAULT 0,
    success_rate DECIMAL(5,2) DEFAULT 100.00,
    
    -- 时间信息
    last_heartbeat TIMESTAMP,
    started_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT,
    
    FOREIGN KEY (master_id) REFERENCES tb_collectors(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_collectors_type ON tb_collectors (type);
CREATE INDEX idx_collectors_status ON tb_collectors (status);
CREATE INDEX idx_collectors_zone ON tb_collectors (network_zone);
CREATE INDEX idx_collectors_heartbeat ON tb_collectors (last_heartbeat DESC);
CREATE INDEX idx_collectors_health ON tb_collectors (health_status);
CREATE INDEX idx_collectors_is_main ON tb_collectors (is_main);
CREATE INDEX idx_collectors_master ON tb_collectors (master_id);

-- 创建更新时间触发器
CREATE TRIGGER update_collectors_updated_at
    BEFORE UPDATE ON tb_collectors
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_collectors IS '采集器信息表';
COMMENT ON COLUMN tb_collectors.id IS '采集器ID';
COMMENT ON COLUMN tb_collectors.name IS '采集器名称';
COMMENT ON COLUMN tb_collectors.type IS '采集器类型';
COMMENT ON COLUMN tb_collectors.version IS '版本号';
COMMENT ON COLUMN tb_collectors.status IS '状态：1在线 0离线 2告警';
COMMENT ON COLUMN tb_collectors.health_status IS '健康状态';
COMMENT ON COLUMN tb_collectors.load_percentage IS '负载百分比';
COMMENT ON COLUMN tb_collectors.config IS '采集器配置JSON';
COMMENT ON COLUMN tb_collectors.capabilities IS '能力描述JSON';
COMMENT ON COLUMN tb_collectors.host_ip IS '主机IP';
COMMENT ON COLUMN tb_collectors.network_zone IS '网络区域';
COMMENT ON COLUMN tb_collectors.is_main IS '是否主采集器';
COMMENT ON COLUMN tb_collectors.master_id IS '主采集器ID';
COMMENT ON COLUMN tb_collectors.total_devices IS '分配设备总数';
COMMENT ON COLUMN tb_collectors.active_tasks IS '活跃任务数';
COMMENT ON COLUMN tb_collectors.success_rate IS '成功率';
COMMENT ON COLUMN tb_collectors.last_heartbeat IS '最后心跳时间';
COMMENT ON COLUMN tb_collectors.started_at IS '启动时间';

-- ========================================
-- 2. 指标模板表
-- ========================================
CREATE TABLE tb_metric_templates (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    category VARCHAR(50),
    device_type_id BIGINT,
    
    -- 模板定义
    metrics JSONB NOT NULL,
    collection_config JSONB,
    
    -- 模板属性
    is_system BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    version VARCHAR(20) DEFAULT '1.0',
    
    -- 统计信息
    usage_count INTEGER DEFAULT 0,
    
    -- 创建信息
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    FOREIGN KEY (device_type_id) REFERENCES tb_device_types(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_metric_templates_type ON tb_metric_templates (device_type_id);
CREATE INDEX idx_metric_templates_category ON tb_metric_templates (category);
CREATE INDEX idx_metric_templates_system ON tb_metric_templates (is_system);
CREATE INDEX idx_metric_templates_active ON tb_metric_templates (is_active);
CREATE INDEX idx_metric_templates_usage ON tb_metric_templates (usage_count DESC);

-- 创建更新时间触发器
CREATE TRIGGER update_metric_templates_updated_at
    BEFORE UPDATE ON tb_metric_templates
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_metric_templates IS '指标模板表';
COMMENT ON COLUMN tb_metric_templates.id IS '模板ID';
COMMENT ON COLUMN tb_metric_templates.name IS '模板名称';
COMMENT ON COLUMN tb_metric_templates.category IS '指标分类';
COMMENT ON COLUMN tb_metric_templates.device_type_id IS '适用设备类型';
COMMENT ON COLUMN tb_metric_templates.metrics IS '指标定义JSON';
COMMENT ON COLUMN tb_metric_templates.collection_config IS '采集配置JSON';
COMMENT ON COLUMN tb_metric_templates.is_system IS '是否系统模板';
COMMENT ON COLUMN tb_metric_templates.is_active IS '是否启用';
COMMENT ON COLUMN tb_metric_templates.version IS '模板版本';
COMMENT ON COLUMN tb_metric_templates.usage_count IS '使用次数';

-- ========================================
-- 3. 采集任务表
-- ========================================
CREATE TABLE tb_collection_tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    
    -- 关联信息
    collector_id BIGINT,
    template_id BIGINT,
    
    -- 调度配置
    schedule_type VARCHAR(20) NOT NULL,
    schedule_config JSONB NOT NULL,
    
    -- 目标配置
    target_devices JSONB,
    target_criteria JSONB,
    
    -- 指标配置
    metrics_config JSONB,
    collection_timeout INTEGER DEFAULT 30,
    retry_times INTEGER DEFAULT 3,
    
    -- 任务状态
    status INTEGER DEFAULT 1,
    priority INTEGER DEFAULT 5,
    
    -- 执行统计
    total_executions BIGINT DEFAULT 0,
    success_executions BIGINT DEFAULT 0,
    last_execution_at TIMESTAMP,
    next_execution_at TIMESTAMP,
    
    -- 创建信息
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    
    FOREIGN KEY (collector_id) REFERENCES tb_collectors(id) ON DELETE SET NULL,
    FOREIGN KEY (template_id) REFERENCES tb_metric_templates(id) ON DELETE SET NULL,
    FOREIGN KEY (created_by) REFERENCES tb_users(id) ON DELETE SET NULL,
    FOREIGN KEY (updated_by) REFERENCES tb_users(id) ON DELETE SET NULL
);

-- 创建索引
CREATE INDEX idx_collection_tasks_collector ON tb_collection_tasks (collector_id);
CREATE INDEX idx_collection_tasks_template ON tb_collection_tasks (template_id);
CREATE INDEX idx_collection_tasks_status ON tb_collection_tasks (status);
CREATE INDEX idx_collection_tasks_next_exec ON tb_collection_tasks (next_execution_at);
CREATE INDEX idx_collection_tasks_priority ON tb_collection_tasks (priority DESC);
CREATE INDEX idx_collection_tasks_schedule_type ON tb_collection_tasks (schedule_type);

-- 创建更新时间触发器
CREATE TRIGGER update_collection_tasks_updated_at
    BEFORE UPDATE ON tb_collection_tasks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- 添加注释
COMMENT ON TABLE tb_collection_tasks IS '采集任务表';
COMMENT ON COLUMN tb_collection_tasks.id IS '任务ID';
COMMENT ON COLUMN tb_collection_tasks.name IS '任务名称';
COMMENT ON COLUMN tb_collection_tasks.description IS '任务描述';
COMMENT ON COLUMN tb_collection_tasks.collector_id IS '采集器ID';
COMMENT ON COLUMN tb_collection_tasks.template_id IS '指标模板ID';
COMMENT ON COLUMN tb_collection_tasks.schedule_type IS '调度类型：SIMPLE,CRON,EVENT';
COMMENT ON COLUMN tb_collection_tasks.schedule_config IS '调度配置JSON';
COMMENT ON COLUMN tb_collection_tasks.target_devices IS '目标设备JSON数组';
COMMENT ON COLUMN tb_collection_tasks.target_criteria IS '目标筛选条件JSON';
COMMENT ON COLUMN tb_collection_tasks.metrics_config IS '指标配置JSON';
COMMENT ON COLUMN tb_collection_tasks.collection_timeout IS '采集超时时间(秒)';
COMMENT ON COLUMN tb_collection_tasks.retry_times IS '重试次数';
COMMENT ON COLUMN tb_collection_tasks.status IS '状态：1启用 0禁用 2暂停 3错误';
COMMENT ON COLUMN tb_collection_tasks.priority IS '优先级1-10';
COMMENT ON COLUMN tb_collection_tasks.total_executions IS '总执行次数';
COMMENT ON COLUMN tb_collection_tasks.success_executions IS '成功执行次数';
COMMENT ON COLUMN tb_collection_tasks.last_execution_at IS '最后执行时间';
COMMENT ON COLUMN tb_collection_tasks.next_execution_at IS '下次执行时间';

-- ========================================
-- 4. 采集数据表（分区表）
-- ========================================
CREATE TABLE tb_collection_data (
    id BIGSERIAL,
    
    -- 关联信息
    device_id BIGINT NOT NULL,
    collector_id BIGINT NOT NULL,
    task_id BIGINT,
    
    -- 指标信息
    metric_name VARCHAR(100) NOT NULL,
    metric_category VARCHAR(50),
    metric_value DECIMAL(20,6),
    metric_unit VARCHAR(20),
    metric_data JSONB,
    
    -- 质量信息
    quality_score INTEGER DEFAULT 100,
    is_valid BOOLEAN DEFAULT TRUE,
    
    -- 时间信息
    collected_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id, collected_at),
    FOREIGN KEY (device_id) REFERENCES tb_devices(id) ON DELETE CASCADE,
    FOREIGN KEY (collector_id) REFERENCES tb_collectors(id) ON DELETE CASCADE,
    FOREIGN KEY (task_id) REFERENCES tb_collection_tasks(id) ON DELETE SET NULL
) PARTITION BY RANGE (collected_at);

-- 创建当前年度的月度分区表
SELECT create_monthly_partition('tb_collection_data', DATE_TRUNC('month', CURRENT_DATE));
SELECT create_monthly_partition('tb_collection_data', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month');
SELECT create_monthly_partition('tb_collection_data', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '2 months');

-- 创建索引（在分区表上）
CREATE INDEX idx_collection_data_device_time ON tb_collection_data (device_id, collected_at DESC);
CREATE INDEX idx_collection_data_metric_time ON tb_collection_data (metric_name, collected_at DESC);
CREATE INDEX idx_collection_data_collector ON tb_collection_data (collector_id, collected_at DESC);
CREATE INDEX idx_collection_data_task ON tb_collection_data (task_id, collected_at DESC);
CREATE INDEX idx_collection_data_quality ON tb_collection_data (quality_score, collected_at DESC);
CREATE INDEX idx_collection_data_valid ON tb_collection_data (is_valid, collected_at DESC);

-- 添加注释
COMMENT ON TABLE tb_collection_data IS '采集数据表（按月分区）';
COMMENT ON COLUMN tb_collection_data.id IS '数据ID';
COMMENT ON COLUMN tb_collection_data.device_id IS '设备ID';
COMMENT ON COLUMN tb_collection_data.collector_id IS '采集器ID';
COMMENT ON COLUMN tb_collection_data.task_id IS '任务ID';
COMMENT ON COLUMN tb_collection_data.metric_name IS '指标名称';
COMMENT ON COLUMN tb_collection_data.metric_category IS '指标分类';
COMMENT ON COLUMN tb_collection_data.metric_value IS '指标数值';
COMMENT ON COLUMN tb_collection_data.metric_unit IS '指标单位';
COMMENT ON COLUMN tb_collection_data.metric_data IS '复杂数据JSON存储';
COMMENT ON COLUMN tb_collection_data.quality_score IS '数据质量评分';
COMMENT ON COLUMN tb_collection_data.is_valid IS '数据是否有效';
COMMENT ON COLUMN tb_collection_data.collected_at IS '采集时间';
COMMENT ON COLUMN tb_collection_data.created_at IS '入库时间';

-- ========================================
-- 5. 采集日志表（分区表）
-- ========================================
CREATE TABLE tb_collection_logs (
    id BIGSERIAL,
    task_id BIGINT,
    collector_id BIGINT,
    device_id BIGINT,
    
    -- 执行信息
    execution_id UUID DEFAULT gen_random_uuid(),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    duration_ms INTEGER,
    
    -- 结果信息
    status VARCHAR(20) NOT NULL,
    success_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    error_message TEXT,
    
    -- 详细信息
    execution_details JSONB,
    
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    PRIMARY KEY (id, created_at),
    FOREIGN KEY (task_id) REFERENCES tb_collection_tasks(id) ON DELETE SET NULL,
    FOREIGN KEY (collector_id) REFERENCES tb_collectors(id) ON DELETE SET NULL,
    FOREIGN KEY (device_id) REFERENCES tb_devices(id) ON DELETE SET NULL
) PARTITION BY RANGE (created_at);

-- 创建当前年度的月度分区表
SELECT create_monthly_partition('tb_collection_logs', DATE_TRUNC('month', CURRENT_DATE));
SELECT create_monthly_partition('tb_collection_logs', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '1 month');
SELECT create_monthly_partition('tb_collection_logs', DATE_TRUNC('month', CURRENT_DATE) + INTERVAL '2 months');

-- 创建索引
CREATE INDEX idx_collection_logs_task ON tb_collection_logs (task_id, created_at DESC);
CREATE INDEX idx_collection_logs_collector ON tb_collection_logs (collector_id, created_at DESC);
CREATE INDEX idx_collection_logs_device ON tb_collection_logs (device_id, created_at DESC);
CREATE INDEX idx_collection_logs_status ON tb_collection_logs (status, created_at DESC);
CREATE INDEX idx_collection_logs_execution ON tb_collection_logs (execution_id);

-- 添加注释
COMMENT ON TABLE tb_collection_logs IS '采集执行日志表（按月分区）';
COMMENT ON COLUMN tb_collection_logs.id IS '日志ID';
COMMENT ON COLUMN tb_collection_logs.task_id IS '任务ID';
COMMENT ON COLUMN tb_collection_logs.collector_id IS '采集器ID';
COMMENT ON COLUMN tb_collection_logs.device_id IS '设备ID';
COMMENT ON COLUMN tb_collection_logs.execution_id IS '执行ID';
COMMENT ON COLUMN tb_collection_logs.start_time IS '开始时间';
COMMENT ON COLUMN tb_collection_logs.end_time IS '结束时间';
COMMENT ON COLUMN tb_collection_logs.duration_ms IS '执行时长(毫秒)';
COMMENT ON COLUMN tb_collection_logs.status IS '执行状态';
COMMENT ON COLUMN tb_collection_logs.success_count IS '成功指标数';
COMMENT ON COLUMN tb_collection_logs.failed_count IS '失败指标数';
COMMENT ON COLUMN tb_collection_logs.error_message IS '错误信息';
COMMENT ON COLUMN tb_collection_logs.execution_details IS '执行详情JSON'; 