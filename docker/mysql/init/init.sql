-- 创建数据库
CREATE DATABASE IF NOT EXISTS skyeye DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;

USE skyeye;

-- 用户表
CREATE TABLE IF NOT EXISTS tb_users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码',
    name VARCHAR(50) COMMENT '姓名',
    email VARCHAR(100) COMMENT '邮箱',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    UNIQUE KEY uk_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

-- 角色表
CREATE TABLE IF NOT EXISTS tb_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '角色ID',
    role_code VARCHAR(50) NOT NULL COMMENT '角色编码',
    role_name VARCHAR(50) NOT NULL COMMENT '角色名称',
    description VARCHAR(255) COMMENT '角色描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色表';

-- 用户角色关联表
CREATE TABLE IF NOT EXISTS tb_user_roles (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_role_id (role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户角色关联表';

-- 权限表
CREATE TABLE IF NOT EXISTS tb_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '权限ID',
    permission_code VARCHAR(100) NOT NULL COMMENT '权限编码',
    permission_name VARCHAR(100) NOT NULL COMMENT '权限名称',
    description VARCHAR(255) COMMENT '权限描述',
    type TINYINT DEFAULT 1 COMMENT '类型：1-菜单，2-按钮，3-API',
    parent_id BIGINT DEFAULT 0 COMMENT '父权限ID',
    path VARCHAR(255) COMMENT '路径',
    component VARCHAR(255) COMMENT '组件',
    icon VARCHAR(100) COMMENT '图标',
    sort INT DEFAULT 0 COMMENT '排序',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-正常',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    UNIQUE KEY uk_permission_code (permission_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='权限表';

-- 角色权限关联表
CREATE TABLE IF NOT EXISTS tb_role_permissions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    role_id BIGINT NOT NULL COMMENT '角色ID',
    permission_id BIGINT NOT NULL COMMENT '权限ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_role_id (role_id),
    KEY idx_permission_id (permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='角色权限关联表';

-- 设备表
CREATE TABLE IF NOT EXISTS tb_devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设备ID',
    device_code VARCHAR(50) NOT NULL COMMENT '设备编码',
    device_name VARCHAR(100) NOT NULL COMMENT '设备名称',
    device_type VARCHAR(50) NOT NULL COMMENT '设备类型',
    ip_address VARCHAR(50) COMMENT 'IP地址',
    port INT COMMENT '端口',
    username VARCHAR(50) COMMENT '用户名',
    password VARCHAR(100) COMMENT '密码',
    rtsp_url VARCHAR(255) COMMENT 'RTSP地址',
    location VARCHAR(255) COMMENT '安装位置',
    description VARCHAR(255) COMMENT '设备描述',
    status TINYINT DEFAULT 1 COMMENT '状态：0-离线，1-在线，2-故障',
    group_id BIGINT COMMENT '分组ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    UNIQUE KEY uk_device_code (device_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备表';

-- 设备分组表
CREATE TABLE IF NOT EXISTS tb_device_groups (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分组ID',
    group_name VARCHAR(50) NOT NULL COMMENT '分组名称',
    description VARCHAR(255) COMMENT '分组描述',
    parent_id BIGINT DEFAULT 0 COMMENT '父分组ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备分组表';

-- 设备指标表
CREATE TABLE IF NOT EXISTS tb_device_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '指标ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    cpu_usage DECIMAL(5,2) COMMENT 'CPU使用率',
    memory_usage DECIMAL(5,2) COMMENT '内存使用率',
    disk_usage DECIMAL(5,2) COMMENT '磁盘使用率',
    network_speed DECIMAL(10,2) COMMENT '网络速度',
    temperature DECIMAL(5,2) COMMENT '温度',
    collect_time DATETIME NOT NULL COMMENT '采集时间',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_device_id (device_id),
    KEY idx_collect_time (collect_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备指标表';

-- 报警信息表
CREATE TABLE IF NOT EXISTS tb_alerts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '报警ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    alert_type VARCHAR(50) NOT NULL COMMENT '报警类型',
    alert_level TINYINT NOT NULL COMMENT '报警级别：1-低，2-中，3-高',
    alert_content TEXT NOT NULL COMMENT '报警内容',
    alert_time DATETIME NOT NULL COMMENT '报警时间',
    status TINYINT DEFAULT 0 COMMENT '状态：0-未处理，1-已处理，2-已忽略',
    process_user_id BIGINT COMMENT '处理人ID',
    process_time DATETIME COMMENT '处理时间',
    process_result VARCHAR(255) COMMENT '处理结果',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_device_id (device_id),
    KEY idx_alert_time (alert_time),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警信息表';

-- 报警规则表
CREATE TABLE IF NOT EXISTS tb_alert_rules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '规则ID',
    rule_name VARCHAR(100) NOT NULL COMMENT '规则名称',
    rule_type VARCHAR(50) NOT NULL COMMENT '规则类型',
    device_id BIGINT COMMENT '设备ID，为空表示适用所有设备',
    device_group_id BIGINT COMMENT '设备分组ID，为空表示不按分组',
    metric_name VARCHAR(50) NOT NULL COMMENT '指标名称',
    operator VARCHAR(10) NOT NULL COMMENT '操作符：>、<、>=、<=、==、!=',
    threshold VARCHAR(50) NOT NULL COMMENT '阈值',
    alert_level TINYINT NOT NULL COMMENT '报警级别：1-低，2-中，3-高',
    continuous_times INT DEFAULT 1 COMMENT '连续次数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='报警规则表';

-- 任务表
CREATE TABLE IF NOT EXISTS tb_tasks (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '任务ID',
    task_name VARCHAR(100) NOT NULL COMMENT '任务名称',
    task_type VARCHAR(50) NOT NULL COMMENT '任务类型',
    description VARCHAR(255) COMMENT '任务描述',
    cron_expression VARCHAR(50) COMMENT 'Cron表达式',
    task_params TEXT COMMENT '任务参数',
    status TINYINT DEFAULT 1 COMMENT '状态：0-禁用，1-启用',
    created_by BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务表';

-- 任务执行记录表
CREATE TABLE IF NOT EXISTS tb_task_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '记录ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    start_time DATETIME NOT NULL COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    execution_time BIGINT COMMENT '执行时长(ms)',
    status TINYINT NOT NULL COMMENT '状态：0-失败，1-成功',
    result TEXT COMMENT '执行结果',
    error_msg TEXT COMMENT '错误信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_task_id (task_id),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行记录表';

-- 系统设置表
CREATE TABLE IF NOT EXISTS tb_settings (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '设置ID',
    setting_key VARCHAR(50) NOT NULL COMMENT '设置键',
    setting_value TEXT COMMENT '设置值',
    setting_type VARCHAR(20) DEFAULT 'string' COMMENT '值类型：string,number,boolean,json',
    description VARCHAR(255) COMMENT '描述',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_setting_key (setting_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统设置表';

-- 操作日志表
CREATE TABLE IF NOT EXISTS tb_operation_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    user_id BIGINT COMMENT '用户ID',
    username VARCHAR(50) COMMENT '用户名',
    operation VARCHAR(50) NOT NULL COMMENT '操作类型',
    method VARCHAR(100) COMMENT '请求方法',
    path VARCHAR(255) COMMENT '请求路径',
    ip VARCHAR(50) COMMENT 'IP地址',
    user_agent VARCHAR(255) COMMENT '用户代理',
    request_params TEXT COMMENT '请求参数',
    response_result TEXT COMMENT '响应结果',
    execution_time BIGINT COMMENT '执行时长(ms)',
    status TINYINT COMMENT '状态：0-失败，1-成功',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_user_id (user_id),
    KEY idx_operation (operation),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';

-- 系统日志表
CREATE TABLE IF NOT EXISTS tb_system_logs (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '日志ID',
    log_level VARCHAR(10) NOT NULL COMMENT '日志级别',
    log_tag VARCHAR(50) COMMENT '日志标签',
    log_content TEXT NOT NULL COMMENT '日志内容',
    stack_trace TEXT COMMENT '堆栈信息',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    KEY idx_log_level (log_level),
    KEY idx_log_tag (log_tag),
    KEY idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统日志表';

-- 任务草稿表
CREATE TABLE IF NOT EXISTS tb_task_drafts (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    draft_id VARCHAR(50) NOT NULL COMMENT '草稿ID',
    draft_data TEXT COMMENT '草稿数据（JSON格式）',
    step INT DEFAULT 0 COMMENT '当前步骤：0-基本信息，1-设备选择，2-指标配置，3-调度设置',
    created_by BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    UNIQUE KEY uk_draft_id (draft_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务草稿表';

-- 任务设备关联表
CREATE TABLE IF NOT EXISTS tb_task_devices (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    device_id BIGINT NOT NULL COMMENT '设备ID',
    device_name VARCHAR(100) COMMENT '设备名称',
    device_type VARCHAR(50) COMMENT '设备类型',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_task_id (task_id),
    KEY idx_device_id (device_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务设备关联表';

-- 任务指标表
CREATE TABLE IF NOT EXISTS tb_task_metrics (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    metric_name VARCHAR(100) NOT NULL COMMENT '指标名称',
    metric_type VARCHAR(50) NOT NULL COMMENT '指标类型',
    metric_params TEXT COMMENT '指标参数（JSON格式）',
    sort_order INT DEFAULT 0 COMMENT '排序',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务指标表';

-- 指标模板表
CREATE TABLE IF NOT EXISTS tb_metric_templates (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    template_name VARCHAR(100) NOT NULL COMMENT '模板名称',
    category VARCHAR(50) COMMENT '分类',
    metric_type VARCHAR(50) NOT NULL COMMENT '指标类型',
    device_type VARCHAR(50) COMMENT '适用设备类型',
    default_params TEXT COMMENT '默认参数（JSON格式）',
    description VARCHAR(255) COMMENT '描述',
    is_system TINYINT DEFAULT 0 COMMENT '是否系统内置：0-否，1-是',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='指标模板表';

-- 任务调度表
CREATE TABLE IF NOT EXISTS tb_task_schedules (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    schedule_type VARCHAR(50) NOT NULL COMMENT '调度类型：realtime-实时执行，scheduled-定时执行，periodic-周期执行，triggered-触发执行',
    cron_expression VARCHAR(100) COMMENT 'Cron表达式',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    interval_value INT COMMENT '间隔值',
    interval_unit VARCHAR(20) COMMENT '间隔单位：minutes-分钟，hours-小时，days-天',
    trigger_type VARCHAR(50) COMMENT '触发类型：event-事件触发，threshold-阈值触发，api-API调用触发',
    trigger_event VARCHAR(100) COMMENT '触发事件',
    max_executions INT DEFAULT 0 COMMENT '最大执行次数，0表示不限制',
    timeout_minutes INT DEFAULT 0 COMMENT '超时时间（分钟），0表示不限制',
    retry_strategy VARCHAR(50) DEFAULT 'none' COMMENT '重试策略：none-不重试，immediate-立即重试，interval-间隔重试',
    max_retries INT DEFAULT 0 COMMENT '最大重试次数',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    KEY idx_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务调度表';

-- 任务执行表
CREATE TABLE IF NOT EXISTS tb_task_executions (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT 'ID',
    execution_id VARCHAR(50) COMMENT '执行ID',
    task_id BIGINT NOT NULL COMMENT '任务ID',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    execution_time BIGINT COMMENT '执行时长(ms)',
    status INT DEFAULT 1 COMMENT '执行状态：1-运行中，2-已调度，3-已暂停，4-已完成，5-执行失败',
    result TEXT COMMENT '结果信息',
    error_message TEXT COMMENT '错误信息',
    execution_count INT DEFAULT 1 COMMENT '执行次数（重试次数+1）',
    device_count INT DEFAULT 0 COMMENT '执行设备数量',
    success_count INT DEFAULT 0 COMMENT '成功设备数量',
    failed_count INT DEFAULT 0 COMMENT '失败设备数量',
    created_by BIGINT COMMENT '创建人ID',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    deleted_at DATETIME DEFAULT NULL COMMENT '删除时间',
    UNIQUE KEY uk_execution_id (execution_id),
    KEY idx_task_id (task_id),
    KEY idx_status (status),
    KEY idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='任务执行表';

-- 初始化数据

-- 初始化角色
INSERT INTO tb_roles (role_code, role_name, description) VALUES 
('ADMIN', '系统管理员', '系统管理员，拥有所有权限'),
('OPERATOR', '操作员', '系统操作员，拥有基本操作权限'),
('VIEWER', '访客', '系统访客，只有查看权限');

-- 初始化管理员用户（密码：admin123，实际应用中应该使用加密密码）
INSERT INTO tb_users (username, password, name, email, status) VALUES 
('admin', '$2a$10$X/uX0JZ9iBEJFM.TJKDgpuXCZWqDcVN7vXbfzLmNe0Y4MSgJZoaDW', '系统管理员', 'admin@skyeye.com', 1);

-- 关联管理员用户和管理员角色
INSERT INTO tb_user_roles (user_id, role_id) VALUES (1, 1);

-- 初始化权限
INSERT INTO tb_permissions (permission_code, permission_name, type, parent_id, path, component, icon, sort) VALUES 
-- 菜单权限
('dashboard', '控制台', 1, 0, '/dashboard', 'dashboard/Dashboard', 'Odometer', 1),
('device', '设备管理', 1, 0, '/device', 'device/DeviceList', 'VideoCamera', 2),
('device:add', '添加设备', 1, 2, '/device/add', 'device/DeviceAdd', '', 1),
('device:detail', '设备详情', 1, 2, '/device/:id', 'device/DeviceDetail', '', 2),
('monitoring', '实时监控', 1, 0, '/monitoring', 'monitoring/Monitoring', 'Monitor', 3),
('alert', '报警中心', 1, 0, '/alerts', 'alert/AlertList', 'Bell', 4),
('analytics', '数据分析', 1, 0, '/analytics', 'analytics/Analytics', 'DataAnalysis', 5),
('task', '任务调度', 1, 0, '/task', 'task/TaskList', 'Calendar', 6),
('history', '历史记录', 1, 0, '/history', 'history/History', 'Clock', 7),
('settings', '系统设置', 1, 0, '/settings', 'settings/Settings', 'Setting', 8),
('user', '用户管理', 1, 0, '/user', 'user/UserList', 'User', 9),

-- 按钮权限
('device:create', '创建设备', 2, 2, '', '', '', 1),
('device:update', '编辑设备', 2, 2, '', '', '', 2),
('device:delete', '删除设备', 2, 2, '', '', '', 3),
('device:export', '导出设备', 2, 2, '', '', '', 4),
('device:import', '导入设备', 2, 2, '', '', '', 5),
('alert:process', '处理报警', 2, 6, '', '', '', 1),
('alert:ignore', '忽略报警', 2, 6, '', '', '', 2),
('task:create', '创建任务', 2, 8, '', '', '', 1),
('task:update', '编辑任务', 2, 8, '', '', '', 2),
('task:delete', '删除任务', 2, 8, '', '', '', 3),
('task:execute', '执行任务', 2, 8, '', '', '', 4),
('user:create', '创建用户', 2, 11, '', '', '', 1),
('user:update', '编辑用户', 2, 11, '', '', '', 2),
('user:delete', '删除用户', 2, 11, '', '', '', 3),
('user:reset', '重置密码', 2, 11, '', '', '', 4);

-- 关联角色和权限（管理员角色拥有所有权限）
INSERT INTO tb_role_permissions (role_id, permission_id)
SELECT 1, id FROM tb_permissions;

-- 关联操作员角色和部分权限
INSERT INTO tb_role_permissions (role_id, permission_id)
SELECT 2, id FROM tb_permissions WHERE permission_code IN 
('dashboard', 'device', 'device:detail', 'monitoring', 'alert', 'alert:process', 'alert:ignore', 'history', 'analytics');

-- 关联访客角色和查看权限
INSERT INTO tb_role_permissions (role_id, permission_id)
SELECT 3, id FROM tb_permissions WHERE permission_code IN 
('dashboard', 'device', 'device:detail', 'monitoring', 'history'); 