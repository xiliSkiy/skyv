package com.skyeye.common.constant;

/**
 * 设备状态枚举
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum DeviceStatus {
    
    /** 离线 */
    OFFLINE(0, "离线", "设备处于离线状态"),
    
    /** 在线 */
    ONLINE(1, "在线", "设备正常在线运行"),
    
    /** 故障 */
    FAULT(2, "故障", "设备出现故障"),
    
    /** 维护中 */
    MAINTENANCE(3, "维护中", "设备正在进行维护");

    private final Integer code;
    private final String name;
    private final String description;

    DeviceStatus(Integer code, String name, String description) {
        this.code = code;
        this.name = name;
        this.description = description;
    }

    public Integer getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 根据代码获取设备状态
     * 
     * @param code 状态代码
     * @return 设备状态枚举
     */
    public static DeviceStatus fromCode(Integer code) {
        if (code == null) {
            return null;
        }
        
        for (DeviceStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown device status code: " + code);
    }

    /**
     * 根据名称获取设备状态
     * 
     * @param name 状态名称
     * @return 设备状态枚举
     */
    public static DeviceStatus fromName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return null;
        }
        
        for (DeviceStatus status : values()) {
            if (status.name.equals(name)) {
                return status;
            }
        }
        
        throw new IllegalArgumentException("Unknown device status name: " + name);
    }

    @Override
    public String toString() {
        return String.format("DeviceStatus{code=%d, name='%s', description='%s'}", 
            code, name, description);
    }
} 