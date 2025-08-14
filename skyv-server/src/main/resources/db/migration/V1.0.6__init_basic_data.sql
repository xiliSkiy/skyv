-- ========================================
-- SkyEye 智能监控系统 - 基础数据初始化
-- 版本: V1.0.6
-- 创建时间: 2024-12-19
-- 描述: 初始化系统基础数据
-- ========================================

-- ========================================
-- 1. 初始化权限数据
-- ========================================
INSERT INTO tb_permissions (name, code, resource, action, type, description, is_system, sort_order) VALUES
-- 系统管理权限
('用户查看', 'user:view', 'user', 'view', 'BUTTON', '查看用户信息', TRUE, 1),
('用户创建', 'user:create', 'user', 'create', 'BUTTON', '创建用户', TRUE, 2),
('用户编辑', 'user:edit', 'user', 'edit', 'BUTTON', '编辑用户', TRUE, 3),
('用户删除', 'user:delete', 'user', 'delete', 'BUTTON', '删除用户', TRUE, 4),
('角色管理', 'role:manage', 'role', 'manage', 'BUTTON', '角色管理', TRUE, 5),
('权限管理', 'permission:manage', 'permission', 'manage', 'BUTTON', '权限管理', TRUE, 6),

-- 设备管理权限
('设备查看', 'device:view', 'device', 'view', 'BUTTON', '查看设备信息', TRUE, 10),
('设备创建', 'device:create', 'device', 'create', 'BUTTON', '创建设备', TRUE, 11),
('设备编辑', 'device:edit', 'device', 'edit', 'BUTTON', '编辑设备', TRUE, 12),
('设备删除', 'device:delete', 'device', 'delete', 'BUTTON', '删除设备', TRUE, 13),
('设备测试', 'device:test', 'device', 'test', 'BUTTON', '测试设备连接', TRUE, 14),
('设备类型管理', 'device_type:manage', 'device_type', 'manage', 'BUTTON', '设备类型管理', TRUE, 15),
('设备区域管理', 'device_area:manage', 'device_area', 'manage', 'BUTTON', '设备区域管理', TRUE, 16),
('设备分组管理', 'device_group:manage', 'device_group', 'manage', 'BUTTON', '设备分组管理', TRUE, 17),

-- 监控管理权限
('监控查看', 'monitoring:view', 'monitoring', 'view', 'BUTTON', '查看监控画面', TRUE, 20),
('监控控制', 'monitoring:control', 'monitoring', 'control', 'BUTTON', '监控设备控制', TRUE, 21),

-- 任务管理权限
('任务查看', 'task:view', 'task', 'view', 'BUTTON', '查看任务', TRUE, 30),
('任务创建', 'task:create', 'task', 'create', 'BUTTON', '创建任务', TRUE, 31),
('任务编辑', 'task:edit', 'task', 'edit', 'BUTTON', '编辑任务', TRUE, 32),
('任务删除', 'task:delete', 'task', 'delete', 'BUTTON', '删除任务', TRUE, 33),
('任务执行', 'task:execute', 'task', 'execute', 'BUTTON', '执行任务', TRUE, 34),

-- 采集器管理权限
('采集器查看', 'collector:view', 'collector', 'view', 'BUTTON', '查看采集器', TRUE, 40),
('采集器管理', 'collector:manage', 'collector', 'manage', 'BUTTON', '管理采集器', TRUE, 41),

-- 报警管理权限
('报警查看', 'alert:view', 'alert', 'view', 'BUTTON', '查看报警信息', TRUE, 50),
('报警处理', 'alert:handle', 'alert', 'handle', 'BUTTON', '处理报警事件', TRUE, 51),
('报警规则管理', 'alert_rule:manage', 'alert_rule', 'manage', 'BUTTON', '报警规则管理', TRUE, 52),

-- 数据分析权限
('数据分析', 'analytics:view', 'analytics', 'view', 'BUTTON', '数据分析查看', TRUE, 60),

-- 历史记录权限
('历史查看', 'history:view', 'history', 'view', 'BUTTON', '查看历史记录', TRUE, 70),

-- 系统设置权限
('系统设置', 'system:setting', 'system', 'setting', 'BUTTON', '系统设置', TRUE, 80),
('日志查看', 'log:view', 'log', 'view', 'BUTTON', '查看系统日志', TRUE, 81);

-- ========================================
-- 2. 初始化角色数据
-- ========================================
INSERT INTO tb_roles (name, code, description, is_system, sort_order) VALUES
('超级管理员', 'ADMIN', '系统超级管理员，拥有所有权限', TRUE, 1),
('用户管理员', 'USER_MANAGER', '用户管理员，管理用户和权限', TRUE, 2),
('设备管理员', 'DEVICE_MANAGER', '设备管理员，管理设备和采集任务', TRUE, 3),
('普通用户', 'USER', '普通用户，查看监控数据', TRUE, 4);

-- ========================================
-- 3. 分配角色权限
-- ========================================
-- 超级管理员拥有所有权限
INSERT INTO tb_role_permissions (role_id, permission_id)
SELECT 1, id FROM tb_permissions WHERE is_system = TRUE;

-- 用户管理员权限
INSERT INTO tb_role_permissions (role_id, permission_id)
SELECT 2, id FROM tb_permissions 
WHERE code IN ('user:view', 'user:create', 'user:edit', 'user:delete', 'role:manage', 'permission:manage', 'log:view');

-- 设备管理员权限
INSERT INTO tb_role_permissions (role_id, permission_id)
SELECT 3, id FROM tb_permissions 
WHERE code IN ('device:view', 'device:create', 'device:edit', 'device:delete', 'device:test', 
               'device_type:manage', 'device_area:manage', 'device_group:manage',
               'task:view', 'task:create', 'task:edit', 'task:delete', 'task:execute',
               'collector:view', 'collector:manage', 'monitoring:view', 'history:view');

-- 普通用户权限
INSERT INTO tb_role_permissions (role_id, permission_id)
SELECT 4, id FROM tb_permissions 
WHERE code IN ('device:view', 'monitoring:view', 'alert:view', 'analytics:view', 'history:view');

-- ========================================
-- 4. 初始化设备类型数据
-- ========================================
INSERT INTO tb_device_types (name, code, level, icon, description, protocol_types, is_system, sort_order) VALUES
-- 一级分类
('网络设备', 'NETWORK', 0, 'network', '网络设备根分类', '["SNMP", "SSH", "HTTP"]', TRUE, 1),
('监控设备', 'MONITOR', 0, 'camera', '监控设备根分类', '["RTSP", "HTTP", "ONVIF"]', TRUE, 2),
('服务器设备', 'SERVER', 0, 'server', '服务器设备根分类', '["SNMP", "SSH", "WMI", "HTTP"]', TRUE, 3),
('传感器设备', 'SENSOR', 0, 'sensor', '传感器设备根分类', '["MODBUS", "HTTP", "MQTT"]', TRUE, 4);

-- 二级分类
INSERT INTO tb_device_types (name, code, parent_id, level, icon, description, protocol_types, is_system, sort_order) VALUES
-- 网络设备子类
('路由器', 'ROUTER', 1, 1, 'router', '路由器设备', '["SNMP", "SSH"]', TRUE, 11),
('交换机', 'SWITCH', 1, 1, 'switch', '交换机设备', '["SNMP", "SSH"]', TRUE, 12),
('防火墙', 'FIREWALL', 1, 1, 'firewall', '防火墙设备', '["SNMP", "SSH", "HTTP"]', TRUE, 13),

-- 监控设备子类
('摄像头', 'CAMERA', 2, 1, 'video-camera', '监控摄像头', '["RTSP", "ONVIF"]', TRUE, 21),
('NVR', 'NVR', 2, 1, 'nvr', '网络视频录像机', '["HTTP", "RTSP"]', TRUE, 22),
('DVR', 'DVR', 2, 1, 'dvr', '数字视频录像机', '["HTTP", "RTSP"]', TRUE, 23),

-- 服务器设备子类
('Linux服务器', 'LINUX_SERVER', 3, 1, 'linux', 'Linux服务器', '["SSH", "SNMP"]', TRUE, 31),
('Windows服务器', 'WINDOWS_SERVER', 3, 1, 'windows', 'Windows服务器', '["WMI", "SNMP"]', TRUE, 32),
('数据库服务器', 'DATABASE_SERVER', 3, 1, 'database', '数据库服务器', '["SSH", "SNMP", "HTTP"]', TRUE, 33),

-- 传感器设备子类
('温湿度传感器', 'TEMP_HUMIDITY', 4, 1, 'temperature', '温湿度传感器', '["MODBUS", "HTTP"]', TRUE, 41),
('烟雾传感器', 'SMOKE_SENSOR', 4, 1, 'smoke', '烟雾传感器', '["MODBUS", "HTTP"]', TRUE, 42),
('门禁设备', 'ACCESS_CONTROL', 4, 1, 'door', '门禁控制设备', '["HTTP", "TCP"]', TRUE, 43);

-- ========================================
-- 5. 初始化设备区域数据
-- ========================================
INSERT INTO tb_device_areas (name, code, level, full_path, description, sort_order) VALUES
-- 一级区域
('总部大楼', 'HQ_BUILDING', 0, '总部大楼', '公司总部大楼', 1),
('数据中心', 'DATA_CENTER', 0, '数据中心', '数据中心机房', 2),
('分公司', 'BRANCH_OFFICE', 0, '分公司', '各地分公司', 3);

-- 二级区域
INSERT INTO tb_device_areas (name, code, parent_id, level, full_path, description, sort_order) VALUES
-- 总部大楼子区域
('1楼大厅', 'HQ_FLOOR1_LOBBY', 1, 1, '总部大楼/1楼大厅', '总部大楼一楼大厅', 11),
('2楼办公区', 'HQ_FLOOR2_OFFICE', 1, 1, '总部大楼/2楼办公区', '总部大楼二楼办公区', 12),
('3楼会议室', 'HQ_FLOOR3_MEETING', 1, 1, '总部大楼/3楼会议室', '总部大楼三楼会议室', 13),
('地下停车场', 'HQ_PARKING', 1, 1, '总部大楼/地下停车场', '总部大楼地下停车场', 14),

-- 数据中心子区域
('A机房', 'DC_ROOM_A', 2, 1, '数据中心/A机房', '数据中心A机房', 21),
('B机房', 'DC_ROOM_B', 2, 1, '数据中心/B机房', '数据中心B机房', 22),
('网络机房', 'DC_NETWORK_ROOM', 2, 1, '数据中心/网络机房', '数据中心网络机房', 23);

-- ========================================
-- 6. 初始化设备分组数据
-- ========================================
INSERT INTO tb_device_groups (name, description, color, is_system, sort_order) VALUES
('核心设备', '核心业务设备分组', '#FF6B6B', TRUE, 1),
('监控设备', '视频监控设备分组', '#4ECDC4', TRUE, 2),
('网络设备', '网络基础设备分组', '#45B7D1', TRUE, 3),
('服务器设备', '服务器设备分组', '#96CEB4', TRUE, 4),
('测试设备', '测试环境设备分组', '#FFEAA7', TRUE, 5);

-- ========================================
-- 7. 初始化设备标签数据
-- ========================================
INSERT INTO tb_device_tags (name, color, description, usage_count) VALUES
('生产环境', '#67C23A', '生产环境设备', 0),
('测试环境', '#E6A23C', '测试环境设备', 0),
('核心业务', '#F56C6C', '核心业务相关设备', 0),
('备用设备', '#909399', '备用设备', 0),
('新采购', '#409EFF', '新采购设备', 0),
('待维护', '#E6A23C', '需要维护的设备', 0),
('高可用', '#67C23A', '高可用设备', 0),
('24小时监控', '#F56C6C', '需要24小时监控的设备', 0);

-- ========================================
-- 8. 初始化系统配置数据
-- ========================================
INSERT INTO tb_system_settings (group_name, key_name, value, value_type, description, sort_order) VALUES
-- 系统基本配置
('system', 'name', 'SkyEye智能监控系统', 'STRING', '系统名称', 1),
('system', 'version', '1.0.0', 'STRING', '系统版本', 2),
('system', 'description', '智能视频监控与安防管理系统', 'STRING', '系统描述', 3),
('system', 'timezone', 'Asia/Shanghai', 'STRING', '系统时区', 4),
('system', 'language', 'zh_CN', 'STRING', '系统语言', 5),

-- 主题配置
('theme', 'primary_color', '#1e3c72', 'STRING', '主色调', 10),
('theme', 'secondary_color', '#2a5298', 'STRING', '辅助色', 11),
('theme', 'dark_mode', 'false', 'BOOLEAN', '深色模式', 12),

-- 安全配置
('security', 'jwt_expiration', '86400000', 'INTEGER', 'JWT过期时间(毫秒)', 20),
('security', 'jwt_refresh_expiration', '604800000', 'INTEGER', 'JWT刷新Token过期时间(毫秒)', 21),
('security', 'password_min_length', '8', 'INTEGER', '密码最小长度', 22),
('security', 'login_max_attempts', '5', 'INTEGER', '最大登录尝试次数', 23),
('security', 'account_lock_time', '1800', 'INTEGER', '账户锁定时间(秒)', 24),

-- 采集配置
('collector', 'default_timeout', '30', 'INTEGER', '默认采集超时时间(秒)', 30),
('collector', 'max_concurrent_tasks', '100', 'INTEGER', '最大并发任务数', 31),
('collector', 'data_retention_days', '90', 'INTEGER', '数据保留天数', 32),
('collector', 'heartbeat_interval', '60', 'INTEGER', '心跳间隔(秒)', 33),

-- 报警配置
('alert', 'default_notification', 'true', 'BOOLEAN', '默认启用通知', 40),
('alert', 'max_pending_alerts', '1000', 'INTEGER', '最大待处理报警数', 41),
('alert', 'auto_resolve_timeout', '3600', 'INTEGER', '自动解决超时时间(秒)', 42),

-- 文件配置
('file', 'upload_max_size', '10485760', 'INTEGER', '文件上传最大大小(字节)', 50),
('file', 'allowed_types', 'jpg,jpeg,png,gif,pdf,doc,docx,xls,xlsx', 'STRING', '允许上传的文件类型', 51),
('file', 'storage_path', '/data/skyeye/files', 'STRING', '文件存储路径', 52);

-- ========================================
-- 9. 创建默认系统通知
-- ========================================
INSERT INTO tb_system_notifications (title, content, type, level, target_type, show_popup, auto_close) VALUES
('欢迎使用SkyEye智能监控系统', 
 '欢迎使用SkyEye智能监控系统！系统已成功初始化，您可以开始配置设备和监控任务。', 
 'INFO', 'NORMAL', 'ALL', TRUE, FALSE),
 
('系统初始化完成', 
 '系统数据库初始化完成，默认管理员账号：admin，密码：admin123456。请及时修改默认密码。', 
 'WARNING', 'IMPORTANT', 'ALL', TRUE, FALSE); 