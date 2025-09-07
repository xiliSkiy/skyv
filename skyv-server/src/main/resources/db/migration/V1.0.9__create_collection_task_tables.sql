-- 创建采集任务相关数据表

-- 采集任务表
CREATE TABLE IF NOT EXISTS tb_collection_tasks (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    collector_id BIGINT,
    schedule_type VARCHAR(20) NOT NULL,
    schedule_config JSONB,
    target_devices JSONB,
    metrics_config JSONB,
    status INTEGER NOT NULL DEFAULT 1,
    priority INTEGER DEFAULT 5,
    enable_retry BOOLEAN DEFAULT true,
    retry_times INTEGER DEFAULT 3,
    retry_interval BIGINT DEFAULT 1000,
    timeout INTEGER DEFAULT 300,
    max_concurrency INTEGER DEFAULT 5,
    last_execution_time TIMESTAMP,
    next_execution_time TIMESTAMP,
    execution_count BIGINT DEFAULT 0,
    success_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    average_execution_time BIGINT,
    last_execution_status VARCHAR(20),
    last_execution_error TEXT,
    tags JSONB,
    parameters JSONB,
    is_enabled BOOLEAN DEFAULT true,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- 任务统计表
CREATE TABLE IF NOT EXISTS tb_task_statistics (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL,
    stat_date VARCHAR(10) NOT NULL,
    execution_count BIGINT DEFAULT 0,
    success_count BIGINT DEFAULT 0,
    failure_count BIGINT DEFAULT 0,
    total_execution_time BIGINT DEFAULT 0,
    average_execution_time BIGINT DEFAULT 0,
    min_execution_time BIGINT,
    max_execution_time BIGINT,
    success_rate DOUBLE PRECISION DEFAULT 0.0,
    average_data_count DOUBLE PRECISION DEFAULT 0.0,
    total_data_count BIGINT DEFAULT 0,
    average_quality_score DOUBLE PRECISION DEFAULT 0.0,
    error_type_stats JSONB,
    device_execution_stats JSONB,
    metric_execution_stats JSONB,
    remarks TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- 创建索引
-- 采集任务表索引
CREATE INDEX IF NOT EXISTS idx_collection_tasks_name ON tb_collection_tasks(name);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_status ON tb_collection_tasks(status);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_schedule_type ON tb_collection_tasks(schedule_type);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_collector_id ON tb_collection_tasks(collector_id);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_is_enabled ON tb_collection_tasks(is_enabled);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_priority ON tb_collection_tasks(priority);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_next_execution_time ON tb_collection_tasks(next_execution_time);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_last_execution_time ON tb_collection_tasks(last_execution_time);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_created_at ON tb_collection_tasks(created_at DESC);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_enabled_status ON tb_collection_tasks(is_enabled, status);
CREATE INDEX IF NOT EXISTS idx_collection_tasks_priority_enabled ON tb_collection_tasks(priority, is_enabled);

-- 任务统计表索引
CREATE INDEX IF NOT EXISTS idx_task_statistics_task_id ON tb_task_statistics(task_id);
CREATE INDEX IF NOT EXISTS idx_task_statistics_stat_date ON tb_task_statistics(stat_date);
CREATE INDEX IF NOT EXISTS idx_task_statistics_task_date ON tb_task_statistics(task_id, stat_date);
CREATE INDEX IF NOT EXISTS idx_task_statistics_success_rate ON tb_task_statistics(success_rate DESC);
CREATE INDEX IF NOT EXISTS idx_task_statistics_execution_count ON tb_task_statistics(execution_count DESC);
CREATE INDEX IF NOT EXISTS idx_task_statistics_execution_time ON tb_task_statistics(average_execution_time ASC);

-- 创建唯一约束
CREATE UNIQUE INDEX IF NOT EXISTS uk_collection_tasks_name ON tb_collection_tasks(name);
CREATE UNIQUE INDEX IF NOT EXISTS uk_task_statistics_task_date ON tb_task_statistics(task_id, stat_date);

-- 添加表注释
COMMENT ON TABLE tb_collection_tasks IS '采集任务表';
COMMENT ON COLUMN tb_collection_tasks.name IS '任务名称';
COMMENT ON COLUMN tb_collection_tasks.description IS '任务描述';
COMMENT ON COLUMN tb_collection_tasks.collector_id IS '采集器ID';
COMMENT ON COLUMN tb_collection_tasks.schedule_type IS '调度类型(SIMPLE,CRON,EVENT)';
COMMENT ON COLUMN tb_collection_tasks.schedule_config IS '调度配置(JSON)';
COMMENT ON COLUMN tb_collection_tasks.target_devices IS '目标设备ID列表(JSON)';
COMMENT ON COLUMN tb_collection_tasks.metrics_config IS '指标配置列表(JSON)';
COMMENT ON COLUMN tb_collection_tasks.status IS '任务状态(1:启用 0:禁用 2:暂停)';
COMMENT ON COLUMN tb_collection_tasks.priority IS '优先级(数值越小优先级越高)';
COMMENT ON COLUMN tb_collection_tasks.enable_retry IS '是否启用重试';
COMMENT ON COLUMN tb_collection_tasks.retry_times IS '重试次数';
COMMENT ON COLUMN tb_collection_tasks.retry_interval IS '重试间隔(毫秒)';
COMMENT ON COLUMN tb_collection_tasks.timeout IS '超时时间(秒)';
COMMENT ON COLUMN tb_collection_tasks.max_concurrency IS '并发执行数';
COMMENT ON COLUMN tb_collection_tasks.last_execution_time IS '上次执行时间';
COMMENT ON COLUMN tb_collection_tasks.next_execution_time IS '下次执行时间';
COMMENT ON COLUMN tb_collection_tasks.execution_count IS '执行次数';
COMMENT ON COLUMN tb_collection_tasks.success_count IS '成功次数';
COMMENT ON COLUMN tb_collection_tasks.failure_count IS '失败次数';
COMMENT ON COLUMN tb_collection_tasks.average_execution_time IS '平均执行时间(毫秒)';
COMMENT ON COLUMN tb_collection_tasks.last_execution_status IS '最后执行状态';
COMMENT ON COLUMN tb_collection_tasks.last_execution_error IS '最后执行错误信息';
COMMENT ON COLUMN tb_collection_tasks.tags IS '任务标签(JSON)';
COMMENT ON COLUMN tb_collection_tasks.parameters IS '任务参数(JSON)';
COMMENT ON COLUMN tb_collection_tasks.is_enabled IS '是否启用';
COMMENT ON COLUMN tb_collection_tasks.remarks IS '备注';

COMMENT ON TABLE tb_task_statistics IS '任务统计表';
COMMENT ON COLUMN tb_task_statistics.task_id IS '任务ID';
COMMENT ON COLUMN tb_task_statistics.stat_date IS '统计日期(YYYY-MM-DD格式)';
COMMENT ON COLUMN tb_task_statistics.execution_count IS '执行次数';
COMMENT ON COLUMN tb_task_statistics.success_count IS '成功次数';
COMMENT ON COLUMN tb_task_statistics.failure_count IS '失败次数';
COMMENT ON COLUMN tb_task_statistics.total_execution_time IS '总执行时间(毫秒)';
COMMENT ON COLUMN tb_task_statistics.average_execution_time IS '平均执行时间(毫秒)';
COMMENT ON COLUMN tb_task_statistics.min_execution_time IS '最短执行时间(毫秒)';
COMMENT ON COLUMN tb_task_statistics.max_execution_time IS '最长执行时间(毫秒)';
COMMENT ON COLUMN tb_task_statistics.success_rate IS '成功率';
COMMENT ON COLUMN tb_task_statistics.average_data_count IS '平均数据采集量';
COMMENT ON COLUMN tb_task_statistics.total_data_count IS '总数据采集量';
COMMENT ON COLUMN tb_task_statistics.average_quality_score IS '平均质量评分';
COMMENT ON COLUMN tb_task_statistics.error_type_stats IS '错误类型统计(JSON)';
COMMENT ON COLUMN tb_task_statistics.device_execution_stats IS '设备执行统计(JSON)';
COMMENT ON COLUMN tb_task_statistics.metric_execution_stats IS '指标执行统计(JSON)';
COMMENT ON COLUMN tb_task_statistics.remarks IS '备注';

-- 插入示例数据
INSERT INTO tb_collection_tasks (
    name, description, schedule_type, schedule_config, target_devices, 
    metrics_config, status, priority, is_enabled
) VALUES (
    '系统健康检查任务',
    '定期检查系统关键指标的健康状态',
    'SIMPLE',
    '{"frequency": "minutes", "interval": 5}',
    '[1, 2, 3]',
    '[{"metricName": "cpu_usage", "metricType": "system_metrics", "pluginType": "SNMP", "parameters": {"oid": "1.3.6.1.4.1.2021.11.9.0"}}, {"metricName": "memory_usage", "metricType": "system_metrics", "pluginType": "SNMP", "parameters": {"oid": "1.3.6.1.4.1.2021.4.6.0"}}]',
    1,
    1,
    true
) ON CONFLICT (name) DO NOTHING;

INSERT INTO tb_collection_tasks (
    name, description, schedule_type, schedule_config, target_devices, 
    metrics_config, status, priority, is_enabled
) VALUES (
    '网络接口监控任务',
    '监控网络接口的流量和状态',
    'SIMPLE',
    '{"frequency": "minutes", "interval": 10}',
    '[1, 2]',
    '[{"metricName": "interface_in_octets", "metricType": "network_metrics", "pluginType": "SNMP", "parameters": {"oid": "1.3.6.1.2.1.2.2.1.10"}}, {"metricName": "interface_out_octets", "metricType": "network_metrics", "pluginType": "SNMP", "parameters": {"oid": "1.3.6.1.2.1.2.2.1.16"}}]',
    1,
    2,
    true
) ON CONFLICT (name) DO NOTHING;

INSERT INTO tb_collection_tasks (
    name, description, schedule_type, schedule_config, target_devices, 
    metrics_config, status, priority, is_enabled
) VALUES (
    'Web服务健康检查',
    '检查Web服务的响应时间和状态码',
    'SIMPLE',
    '{"frequency": "minutes", "interval": 2}',
    '[3]',
    '[{"metricName": "response_time", "metricType": "web_metrics", "pluginType": "HTTP", "parameters": {"url": "/health", "timeout": 5000}}, {"metricName": "status_code", "metricType": "web_metrics", "pluginType": "HTTP", "parameters": {"url": "/", "timeout": 5000}}]',
    1,
    3,
    true
) ON CONFLICT (name) DO NOTHING;
