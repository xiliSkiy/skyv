package com.skyeye.scheduler.enums;

/**
 * 任务状态枚举
 */
public enum TaskStatus {
    /**
     * 待执行
     */
    PENDING("待执行"),
    
    /**
     * 已调度
     */
    SCHEDULED("已调度"),
    
    /**
     * 执行中
     */
    RUNNING("执行中"),
    
    /**
     * 已完成
     */
    COMPLETED("已完成"),
    
    /**
     * 已失败
     */
    FAILED("已失败"),
    
    /**
     * 已取消
     */
    CANCELLED("已取消"),
    
    /**
     * 已暂停
     */
    PAUSED("已暂停");
    
    private final String desc;
    
    TaskStatus(String desc) {
        this.desc = desc;
    }
    
    public String getDesc() {
        return desc;
    }
} 