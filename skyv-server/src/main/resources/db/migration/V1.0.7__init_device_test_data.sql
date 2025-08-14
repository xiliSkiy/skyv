-- ========================================
-- 设备管理测试数据初始化
-- ========================================

-- 插入设备类型数据
INSERT INTO tb_device_types (name, code, icon, description, config_schema, status, created_at, updated_at) VALUES
('摄像头', 'CAMERA', 'video-camera', '视频监控摄像头设备', '{}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('传感器', 'SENSOR', 'sensor', '环境监测传感器设备', '{}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('门禁', 'ACCESS', 'lock', '门禁控制器设备', '{}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('控制器', 'CONTROLLER', 'cpu', '智能控制器设备', '{}', 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入设备区域数据
INSERT INTO tb_device_areas (name, code, description, parent_id, level, sort_order, created_at, updated_at) VALUES
('北区', 'NORTH', '北区监控区域', NULL, 1, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('南区', 'SOUTH', '南区监控区域', NULL, 1, 2, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('东区', 'EAST', '东区监控区域', NULL, 1, 3, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('西区', 'WEST', '西区监控区域', NULL, 1, 4, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入设备分组数据
INSERT INTO tb_device_groups (name, description, created_at, updated_at) VALUES
('重要设备', '关键监控设备组', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('一般设备', '普通监控设备组', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('测试设备', '测试用途设备组', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入设备标签数据
INSERT INTO tb_device_tags (name, color, description, created_at, updated_at) VALUES
('重要', '#FF4444', '重要设备标签', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('室内', '#4444FF', '室内设备标签', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('室外', '#44FF44', '室外设备标签', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('测试', '#FFAA44', '测试设备标签', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 插入测试设备数据
INSERT INTO tb_devices (
    name, device_type_id, area_id, group_id, 
    ip_address, port, mac_address, protocol, 
    status, health_score, 
    location, manufacturer, model, serial_number, firmware_version,
    description, created_at, updated_at, last_online_at
) VALUES
-- 设备1: HD摄像头
(
    'HD摄像头-会议室', 
    (SELECT id FROM tb_device_types WHERE code = 'CAMERA' LIMIT 1),
    (SELECT id FROM tb_device_areas WHERE code = 'NORTH' LIMIT 1),
    (SELECT id FROM tb_device_groups WHERE name = '重要设备' LIMIT 1),
    '192.168.1.101', 8080, '00:11:22:33:44:01', 'RTSP',
    1, 95,
    '会议室A区', '海康威视', 'DS-2CD2T47G1', 'HK202301001', 'V5.6.3',
    '会议室高清监控摄像头', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
-- 设备2: 温湿度传感器
(
    '温湿度传感器-办公区',
    (SELECT id FROM tb_device_types WHERE code = 'SENSOR' LIMIT 1),
    (SELECT id FROM tb_device_areas WHERE code = 'EAST' LIMIT 1),
    (SELECT id FROM tb_device_groups WHERE name = '一般设备' LIMIT 1),
    '192.168.1.102', 502, '00:11:22:33:44:02', 'MODBUS',
    1, 88,
    '办公区域B栋', '施耐德', 'SHT30-DIS', 'SN202301002', 'V2.1.0',
    '办公区环境监测传感器', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
-- 设备3: 门禁控制器
(
    '门禁控制器-正门',
    (SELECT id FROM tb_device_types WHERE code = 'ACCESS' LIMIT 1),
    (SELECT id FROM tb_device_areas WHERE code = 'SOUTH' LIMIT 1),
    (SELECT id FROM tb_device_groups WHERE name = '重要设备' LIMIT 1),
    '192.168.1.103', 4370, '00:11:22:33:44:03', 'TCP',
    2, 0,
    '大楼正门入口', '大华技术', 'ASC1202B', 'DH202301003', 'V3.2.1',
    '正门门禁控制系统', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '2 hours'
),
-- 设备4: 360全景摄像头
(
    '360全景摄像头-大厅',
    (SELECT id FROM tb_device_types WHERE code = 'CAMERA' LIMIT 1),
    (SELECT id FROM tb_device_areas WHERE code = 'WEST' LIMIT 1),
    (SELECT id FROM tb_device_groups WHERE name = '重要设备' LIMIT 1),
    '192.168.1.104', 8080, '00:11:22:33:44:04', 'RTSP',
    3, 25,
    '大厅中央区域', '宇视科技', 'IPC6852SR-X', 'UV202301004', 'V1.8.7',
    '大厅360度全景监控', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '1 day'
),
-- 设备5: 烟雾传感器
(
    '烟雾传感器-档案室',
    (SELECT id FROM tb_device_types WHERE code = 'SENSOR' LIMIT 1),
    (SELECT id FROM tb_device_areas WHERE code = 'NORTH' LIMIT 1),
    (SELECT id FROM tb_device_groups WHERE name = '测试设备' LIMIT 1),
    '192.168.1.105', 502, '00:11:22:33:44:05', 'MODBUS',
    1, 92,
    '档案室防火区', '霍尼韦尔', 'FSS-SD-355', 'HW202301005', 'V4.0.2',
    '档案室烟雾检测传感器', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
-- 设备6: 红外摄像头
(
    '红外摄像头-停车场',
    (SELECT id FROM tb_device_types WHERE code = 'CAMERA' LIMIT 1),
    (SELECT id FROM tb_device_areas WHERE code = 'SOUTH' LIMIT 1),
    (SELECT id FROM tb_device_groups WHERE name = '一般设备' LIMIT 1),
    '192.168.1.106', 8080, '00:11:22:33:44:06', 'RTSP',
    1, 78,
    '地下停车场', '海康威视', 'DS-2CD2T86G2', 'HK202301006', 'V5.6.3',
    '停车场夜视监控摄像头', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
),
-- 设备7: 空气质量传感器
(
    '空气质量传感器-实验室',
    (SELECT id FROM tb_device_types WHERE code = 'SENSOR' LIMIT 1),
    (SELECT id FROM tb_device_areas WHERE code = 'EAST' LIMIT 1),
    (SELECT id FROM tb_device_groups WHERE name = '重要设备' LIMIT 1),
    '192.168.1.107', 502, '00:11:22:33:44:07', 'MODBUS',
    4, 60,
    '化学实验室', '西门子', 'QAD2012', 'SI202301007', 'V1.5.3',
    '实验室空气质量监测（维护中）', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP - INTERVAL '3 hours'
);

-- 创建设备-标签关联关系表（如果需要多对多关系）
-- 这里简化处理，不创建关联表，在应用层处理标签逻辑 