package com.skyeye.collector.scheduler;

/**
 * 调度器状态枚举
 * 
 * @author SkyEye Team
 */
public enum SchedulerStatus {

    /**
     * 已停止
     */
    STOPPED("已停止"),

    /**
     * 正在启动
     */
    STARTING("正在启动"),

    /**
     * 运行中
     */
    RUNNING("运行中"),

    /**
     * 正在停止
     */
    STOPPING("正在停止"),

    /**
     * 暂停
     */
    PAUSED("暂停"),

    /**
     * 错误
     */
    ERROR("错误");

    private final String description;

    SchedulerStatus(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    /**
     * 检查调度器是否正在运行
     * 
     * @return 是否正在运行
     */
    public boolean isRunning() {
        return this == RUNNING;
    }

    /**
     * 检查调度器是否可以接受新任务
     * 
     * @return 是否可以接受新任务
     */
    public boolean canAcceptTasks() {
        return this == RUNNING || this == PAUSED;
    }

    /**
     * 检查调度器是否已停止
     * 
     * @return 是否已停止
     */
    public boolean isStopped() {
        return this == STOPPED || this == ERROR;
    }
}
