-- ========================================
-- 修复设备表网络字段类型
-- 将INET和MACADDR类型改为VARCHAR，避免类型转换问题
-- ========================================

-- 修改IP地址字段类型
ALTER TABLE tb_devices 
ALTER COLUMN ip_address TYPE VARCHAR(45);

-- 修改MAC地址字段类型  
ALTER TABLE tb_devices 
ALTER COLUMN mac_address TYPE VARCHAR(17);

-- 添加注释说明
COMMENT ON COLUMN tb_devices.ip_address IS 'IP地址 (IPv4或IPv6格式)';
COMMENT ON COLUMN tb_devices.mac_address IS 'MAC地址 (格式: XX:XX:XX:XX:XX:XX)';

-- 验证修改
SELECT column_name, data_type, character_maximum_length 
FROM information_schema.columns 
WHERE table_name = 'tb_devices' 
AND column_name IN ('ip_address', 'mac_address');
