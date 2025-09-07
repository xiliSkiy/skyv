package com.skyeye.collector.scheduler;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 调度器统计信息
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class SchedulerStatistics {

    /**
     * 总任务数
     */
    private int totalTasks;

    /**
     * 已调度任务数
     */
    private int scheduledTasks;

    /**
     * 运行中任务数
     */
    private int runningTasks;

    /**
     * 暂停任务数
     */
    private int pausedTasks;

    /**
     * 错误任务数
     */
    private int errorTasks;

    /**
     * 今日执行次数
     */
    private long todayExecutions;

    /**
     * 今日成功次数
     */
    private long todaySuccesses;

    /**
     * 今日失败次数
     */
    private long todayFailures;

    /**
     * 平均执行时间（毫秒）
     */
    private long averageExecutionTime;

    /**
     * 最短执行时间（毫秒）
     */
    private long minExecutionTime;

    /**
     * 最长执行时间（毫秒）
     */
    private long maxExecutionTime;

    /**
     * 调度器启动时间
     */
    private LocalDateTime startTime;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 线程池活跃线程数
     */
    private int activeThreads;

    /**
     * 线程池总线程数
     */
    private int totalThreads;

    /**
     * 队列中等待的任务数
     */
    private int queuedTasks;

    /**
     * 内存使用情况（MB）
     */
    private long memoryUsage;

    /**
     * CPU使用率（百分比）
     */
    private double cpuUsage;

    /**
     * 成功率
     */
    public double getSuccessRate() {
        if (todayExecutions == 0) {
            return 0.0;
        }
        return (double) todaySuccesses / todayExecutions * 100;
    }

    /**
     * 失败率
     */
    public double getFailureRate() {
        if (todayExecutions == 0) {
            return 0.0;
        }
        return (double) todayFailures / todayExecutions * 100;
    }

    /**
     * 调度器运行时长（秒）
     */
    public long getUptimeSeconds() {
        if (startTime == null) {
            return 0;
        }
        return java.time.Duration.between(startTime, LocalDateTime.now()).getSeconds();
    }

    /**
     * 调度器运行时长（分钟）
     */
    public long getUptimeMinutes() {
        return getUptimeSeconds() / 60;
    }

    /**
     * 调度器运行时长（小时）
     */
    public long getUptimeHours() {
        return getUptimeMinutes() / 60;
    }

    /**
     * 调度器运行时长（天）
     */
    public long getUptimeDays() {
        return getUptimeHours() / 24;
    }

    /**
     * 格式化运行时长
     */
    public String getFormattedUptime() {
        long days = getUptimeDays();
        long hours = getUptimeHours() % 24;
        long minutes = getUptimeMinutes() % 60;
        long seconds = getUptimeSeconds() % 60;

        if (days > 0) {
            return String.format("%d天%d小时%d分钟%d秒", days, hours, minutes, seconds);
        } else if (hours > 0) {
            return String.format("%d小时%d分钟%d秒", hours, minutes, seconds);
        } else if (minutes > 0) {
            return String.format("%d分钟%d秒", minutes, seconds);
        } else {
            return String.format("%d秒", seconds);
        }
    }
}
