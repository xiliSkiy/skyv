-- 创建采集相关数据表

-- 采集数据表
CREATE TABLE IF NOT EXISTS tb_collection_data (
    id BIGSERIAL PRIMARY KEY,
    device_id BIGINT NOT NULL,
    task_id BIGINT,
    metric_name VARCHAR(100) NOT NULL,
    metric_type VARCHAR(50),
    metric_value DECIMAL(20,6),
    metric_data JSONB,
    collected_at TIMESTAMP NOT NULL,
    quality_score INTEGER,
    plugin_type VARCHAR(50),
    session_id VARCHAR(100),
    response_time BIGINT,
    status INTEGER DEFAULT 1,
    error_message TEXT,
    tags JSONB,
    data_version INTEGER DEFAULT 1,
    expires_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_collection_data_device_id ON tb_collection_data(device_id);
CREATE INDEX IF NOT EXISTS idx_collection_data_task_id ON tb_collection_data(task_id);
CREATE INDEX IF NOT EXISTS idx_collection_data_metric_name ON tb_collection_data(metric_name);
CREATE INDEX IF NOT EXISTS idx_collection_data_collected_at ON tb_collection_data(collected_at DESC);
CREATE INDEX IF NOT EXISTS idx_collection_data_plugin_type ON tb_collection_data(plugin_type);
CREATE INDEX IF NOT EXISTS idx_collection_data_session_id ON tb_collection_data(session_id);
CREATE INDEX IF NOT EXISTS idx_collection_data_expires_at ON tb_collection_data(expires_at);
CREATE INDEX IF NOT EXISTS idx_collection_data_device_metric ON tb_collection_data(device_id, metric_name, collected_at DESC);

-- 采集日志表
CREATE TABLE IF NOT EXISTS tb_collection_logs (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT,
    device_id BIGINT NOT NULL,
    execution_id VARCHAR(100),
    metric_name VARCHAR(100) NOT NULL,
    plugin_type VARCHAR(50),
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    success BOOLEAN,
    error_message TEXT,
    error_code VARCHAR(50),
    response_time BIGINT,
    quality_score INTEGER,
    data_count INTEGER,
    retry_count INTEGER DEFAULT 0,
    config_data JSONB,
    extra_data JSONB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_by BIGINT,
    updated_by BIGINT
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_collection_logs_task_id ON tb_collection_logs(task_id);
CREATE INDEX IF NOT EXISTS idx_collection_logs_device_id ON tb_collection_logs(device_id);
CREATE INDEX IF NOT EXISTS idx_collection_logs_execution_id ON tb_collection_logs(execution_id);
CREATE INDEX IF NOT EXISTS idx_collection_logs_start_time ON tb_collection_logs(start_time DESC);
CREATE INDEX IF NOT EXISTS idx_collection_logs_status ON tb_collection_logs(status);
CREATE INDEX IF NOT EXISTS idx_collection_logs_success ON tb_collection_logs(success);
CREATE INDEX IF NOT EXISTS idx_collection_logs_plugin_type ON tb_collection_logs(plugin_type);
CREATE INDEX IF NOT EXISTS idx_collection_logs_device_metric ON tb_collection_logs(device_id, metric_name, start_time DESC);

-- 添加表注释
COMMENT ON TABLE tb_collection_data IS '采集数据表';
COMMENT ON COLUMN tb_collection_data.device_id IS '设备ID';
COMMENT ON COLUMN tb_collection_data.task_id IS '任务ID';
COMMENT ON COLUMN tb_collection_data.metric_name IS '指标名称';
COMMENT ON COLUMN tb_collection_data.metric_type IS '指标类型';
COMMENT ON COLUMN tb_collection_data.metric_value IS '指标数值';
COMMENT ON COLUMN tb_collection_data.metric_data IS '原始数据(JSON)';
COMMENT ON COLUMN tb_collection_data.collected_at IS '采集时间';
COMMENT ON COLUMN tb_collection_data.quality_score IS '数据质量评分';
COMMENT ON COLUMN tb_collection_data.plugin_type IS '采集插件类型';
COMMENT ON COLUMN tb_collection_data.session_id IS '会话ID';
COMMENT ON COLUMN tb_collection_data.response_time IS '响应时间(ms)';
COMMENT ON COLUMN tb_collection_data.status IS '数据状态(1:正常 0:异常)';
COMMENT ON COLUMN tb_collection_data.error_message IS '错误信息';
COMMENT ON COLUMN tb_collection_data.tags IS '标签(JSON)';
COMMENT ON COLUMN tb_collection_data.data_version IS '数据版本';
COMMENT ON COLUMN tb_collection_data.expires_at IS '过期时间';

COMMENT ON TABLE tb_collection_logs IS '采集日志表';
COMMENT ON COLUMN tb_collection_logs.task_id IS '任务ID';
COMMENT ON COLUMN tb_collection_logs.device_id IS '设备ID';
COMMENT ON COLUMN tb_collection_logs.execution_id IS '执行ID';
COMMENT ON COLUMN tb_collection_logs.metric_name IS '指标名称';
COMMENT ON COLUMN tb_collection_logs.plugin_type IS '采集插件类型';
COMMENT ON COLUMN tb_collection_logs.start_time IS '开始时间';
COMMENT ON COLUMN tb_collection_logs.end_time IS '结束时间';
COMMENT ON COLUMN tb_collection_logs.status IS '执行状态(RUNNING,COMPLETED,FAILED,CANCELLED)';
COMMENT ON COLUMN tb_collection_logs.success IS '是否成功';
COMMENT ON COLUMN tb_collection_logs.error_message IS '错误信息';
COMMENT ON COLUMN tb_collection_logs.error_code IS '错误代码';
COMMENT ON COLUMN tb_collection_logs.response_time IS '响应时间(ms)';
COMMENT ON COLUMN tb_collection_logs.quality_score IS '数据质量评分';
COMMENT ON COLUMN tb_collection_logs.data_count IS '采集数据量';
COMMENT ON COLUMN tb_collection_logs.retry_count IS '重试次数';
COMMENT ON COLUMN tb_collection_logs.config_data IS '采集配置(JSON)';
COMMENT ON COLUMN tb_collection_logs.extra_data IS '额外信息(JSON)';
