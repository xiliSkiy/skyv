-- ================================================
-- 设备相关支持表初始数据
-- 版本: V1.0.9
-- 创建时间: 2024-01-01
-- 说明: 设备类型、区域、分组、标签、协议、模板初始数据
-- ================================================

-- 插入设备协议初始数据
INSERT INTO tb_device_protocols (name, code, version, description, port, config_schema, usage_count) VALUES
('RTSP协议', 'RTSP', '1.0', '实时流传输协议，用于视频流传输', 554, '{"username": "string", "password": "string", "stream_url": "string"}', 45),
('ONVIF协议', 'ONVIF', '2.0', '开放网络视频接口论坛标准', 80, '{"username": "string", "password": "string", "service_url": "string"}', 35),
('HTTP协议', 'HTTP', '1.1', '超文本传输协议', 80, '{"username": "string", "password": "string", "api_url": "string"}', 30),
('HTTPS协议', 'HTTPS', '1.1', '安全超文本传输协议', 443, '{"username": "string", "password": "string", "api_url": "string", "cert_verify": "boolean"}', 20),
('MODBUS协议', 'MODBUS', 'TCP', '工业通信协议', 502, '{"slave_id": "integer", "register_address": "integer", "data_type": "string"}', 25),
('MQTT协议', 'MQTT', '3.1.1', '消息队列遥测传输协议', 1883, '{"broker_host": "string", "broker_port": "integer", "username": "string", "password": "string", "topic": "string"}', 15),
('TCP协议', 'TCP', '1.0', '传输控制协议', 0, '{"host": "string", "port": "integer", "timeout": "integer"}', 10),
('UDP协议', 'UDP', '1.0', '用户数据报协议', 0, '{"host": "string", "port": "integer", "timeout": "integer"}', 5)
ON CONFLICT (code) DO NOTHING;

-- 插入设备模板初始数据
INSERT INTO tb_device_templates (name, code, device_type_id, protocol_id, manufacturer, model, config_template, default_settings, description, usage_count) VALUES
('海康威视网络摄像头模板', 'HIKVISION_NETWORK_CAMERA', 1, 1, '海康威视', 'DS-2CD2XXX', 
 '{"ip": "", "port": 554, "username": "admin", "password": "", "channel": 1}',
 '{"resolution": "1920x1080", "framerate": 25, "bitrate": 4096}',
 '海康威视网络摄像头通用配置模板', 8),

('大华网络摄像头模板', 'DAHUA_NETWORK_CAMERA', 1, 2, '大华技术', 'DH-IPC-XXX',
 '{"ip": "", "port": 80, "username": "admin", "password": "", "onvif_port": 80}',
 '{"resolution": "1920x1080", "framerate": 25, "bitrate": 4096}',
 '大华网络摄像头ONVIF配置模板', 5),

('宇视网络摄像头模板', 'UNIVIEW_NETWORK_CAMERA', 1, 1, '宇视科技', 'IPC-XXX',
 '{"ip": "", "port": 554, "username": "admin", "password": "", "channel": 1}',
 '{"resolution": "1920x1080", "framerate": 25, "bitrate": 4096}',
 '宇视网络摄像头配置模板', 3),

('温湿度传感器模板', 'TEMP_HUMIDITY_SENSOR', 2, 5, '研华科技', 'ADAM-6017',
 '{"ip": "", "port": 502, "slave_id": 1, "temp_register": 30001, "humidity_register": 30002}',
 '{"sampling_interval": 60, "alarm_temp_high": 35, "alarm_temp_low": 5, "alarm_humidity_high": 80}',
 'MODBUS温湿度传感器配置模板', 12),

('门禁控制器模板', 'ACCESS_CONTROLLER', 3, 3, '立林科技', 'TCP/IP控制器',
 '{"ip": "", "port": 80, "username": "admin", "password": "", "device_id": ""}',
 '{"door_delay": 5, "alarm_enable": true, "card_format": "IC"}',
 'HTTP门禁控制器配置模板', 6)
ON CONFLICT (code) DO NOTHING;

-- 更新现有设备类型数据，添加新字段的值
UPDATE tb_device_types SET 
    is_enabled = TRUE,
    device_count = 0
WHERE is_enabled IS NULL;

-- 如果存在具体的设备类型，更新协议信息
UPDATE tb_device_types SET 
    protocol_types = '["RTSP", "ONVIF", "HTTP"]'
WHERE code LIKE 'CAMERA%' AND protocol_types IS NULL;

UPDATE tb_device_types SET 
    protocol_types = '["MODBUS", "MQTT", "HTTP"]'
WHERE code LIKE 'SENSOR%' AND protocol_types IS NULL;

UPDATE tb_device_types SET 
    protocol_types = '["HTTP", "HTTPS"]'
WHERE code LIKE 'ACCESS%' AND protocol_types IS NULL;

-- 更新现有设备区域数据
UPDATE tb_device_areas SET 
    area_type = 'other',
    device_count = 0,
    online_count = 0,
    alert_count = 0,
    status = 'normal'
WHERE area_type IS NULL;

-- 更新现有设备分组数据
UPDATE tb_device_groups SET 
    group_type = 'normal',
    device_count = 0,
    online_count = 0,
    offline_count = 0,
    error_count = 0,
    unknown_count = 0
WHERE group_type IS NULL;

-- 如果没有code，生成一个
UPDATE tb_device_groups SET 
    code = 'GROUP_' || id 
WHERE code IS NULL;

-- 重新设置序列值
SELECT setval('tb_device_protocols_id_seq', 100);
SELECT setval('tb_device_templates_id_seq', 100); 