package com.skyeye.collector.scheduler;

/**
 * 任务执行状态枚举
 * 
 * @author SkyEye Team
 */
public enum TaskExecutionStatus {

    /**
     * 已调度
     */
    SCHEDULED("已调度"),

    /**
     * 运行中
     */
    RUNNING("运行中"),

    /**
     * 已完成
     */
    COMPLETED("已完成"),

    /**
     * 执行失败
     */
    FAILED("执行失败"),

    /**
     * 已暂停
     */
    PAUSED("已暂停"),

    /**
     * 已取消
     */
    CANCELLED("已取消"),

    /**
     * 等待中
     */
    WAITING("等待中"),

    /**
     * 超时
     */
    TIMEOUT("超时");

    private final String description;

    TaskExecutionStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查任务是否可以执行
     * 
     * @return 是否可以执行
     */
    public boolean canExecute() {
        return this == SCHEDULED || this == WAITING;
    }

    /**
     * 检查任务是否正在运行
     * 
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return this == RUNNING;
    }

    /**
     * 检查任务是否已完成
     * 
     * @return 是否已完成
     */
    public boolean isCompleted() {
        return this == COMPLETED || this == FAILED || this == CANCELLED || this == TIMEOUT;
    }

    /**
     * 检查任务是否处于活跃状态
     * 
     * @return 是否处于活跃状态
     */
    public boolean isActive() {
        return this == SCHEDULED || this == RUNNING || this == WAITING;
    }
}
