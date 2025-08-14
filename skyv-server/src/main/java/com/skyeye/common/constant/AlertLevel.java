package com.skyeye.common.constant;

/**
 * 报警级别枚举
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public enum AlertLevel {
    
    /** 低级 */
    LOW("LOW", "低级", 1, "#52c41a"),
    
    /** 中级 */
    MEDIUM("MEDIUM", "中级", 2, "#faad14"),
    
    /** 高级 */
    HIGH("HIGH", "高级", 3, "#fa8c16"),
    
    /** 严重 */
    CRITICAL("CRITICAL", "严重", 4, "#f5222d");

    private final String code;
    private final String name;
    private final Integer priority;
    private final String color;

    AlertLevel(String code, String name, Integer priority, String color) {
        this.code = code;
        this.name = name;
        this.priority = priority;
        this.color = color;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public Integer getPriority() {
        return priority;
    }

    public String getColor() {
        return color;
    }

    /**
     * 根据代码获取报警级别
     * 
     * @param code 级别代码
     * @return 报警级别枚举
     */
    public static AlertLevel fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        for (AlertLevel level : values()) {
            if (level.code.equals(code)) {
                return level;
            }
        }
        
        throw new IllegalArgumentException("Unknown alert level code: " + code);
    }

    /**
     * 根据优先级获取报警级别
     * 
     * @param priority 优先级
     * @return 报警级别枚举
     */
    public static AlertLevel fromPriority(Integer priority) {
        if (priority == null) {
            return null;
        }
        
        for (AlertLevel level : values()) {
            if (level.priority.equals(priority)) {
                return level;
            }
        }
        
        throw new IllegalArgumentException("Unknown alert level priority: " + priority);
    }

    /**
     * 判断是否为高级别报警（HIGH或CRITICAL）
     * 
     * @return true如果是高级别报警
     */
    public boolean isHighLevel() {
        return this == HIGH || this == CRITICAL;
    }

    /**
     * 判断是否为严重报警
     * 
     * @return true如果是严重报警
     */
    public boolean isCritical() {
        return this == CRITICAL;
    }

    @Override
    public String toString() {
        return String.format("AlertLevel{code='%s', name='%s', priority=%d, color='%s'}", 
            code, name, priority, color);
    }
} 