package com.skyeye.collector.scheduler;

import com.skyeye.collector.entity.CollectionTask;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.concurrent.ScheduledFuture;

/**
 * 调度任务包装器
 * 
 * @author SkyEye Team
 */
@Data
public class ScheduledTask {

    /**
     * 任务ID
     */
    private final Long taskId;

    /**
     * 任务名称
     */
    private final String taskName;

    /**
     * 调度类型
     */
    private final String scheduleType;

    /**
     * 调度配置
     */
    private final Object scheduleConfig;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecutionTime;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecutionTime;

    /**
     * 执行次数
     */
    private long executionCount;

    /**
     * 成功次数
     */
    private long successCount;

    /**
     * 失败次数
     */
    private long failureCount;

    /**
     * 平均执行时间（毫秒）
     */
    private long averageExecutionTime;

    /**
     * 最后执行状态
     */
    private String lastExecutionStatus;

    /**
     * 最后执行错误信息
     */
    private String lastExecutionError;

    /**
     * 任务状态
     */
    private TaskExecutionStatus status;

    /**
     * 优先级
     */
    private int priority;

    /**
     * 是否启用
     */
    private boolean enabled;

    /**
     * 创建时间
     */
    private final LocalDateTime createdAt;

    /**
     * 最后更新时间
     */
    private LocalDateTime lastUpdated;

    /**
     * 调度Future
     */
    private ScheduledFuture<?> scheduledFuture;

    /**
     * 任务对象引用
     */
    private CollectionTask task;

    /**
     * 构造函数
     */
    public ScheduledTask(CollectionTask task) {
        this.taskId = task.getId();
        this.taskName = task.getName();
        this.scheduleType = task.getScheduleType();
        this.scheduleConfig = task.getScheduleConfig();
        this.priority = task.getPriority();
        this.enabled = task.getIsEnabled();
        this.status = TaskExecutionStatus.SCHEDULED;
        this.createdAt = LocalDateTime.now();
        this.lastUpdated = LocalDateTime.now();
        this.task = task;
    }

    /**
     * 更新执行统计
     */
    public void updateExecutionStatistics(boolean success, long executionTime) {
        this.lastExecutionTime = LocalDateTime.now();
        this.executionCount++;
        
        if (success) {
            this.successCount++;
            this.lastExecutionStatus = "SUCCESS";
        } else {
            this.failureCount++;
            this.lastExecutionStatus = "FAILED";
        }

        // 更新平均执行时间
        if (this.averageExecutionTime == 0) {
            this.averageExecutionTime = executionTime;
        } else {
            this.averageExecutionTime = (this.averageExecutionTime + executionTime) / 2;
        }

        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 设置执行错误
     */
    public void setExecutionError(String errorMessage) {
        this.lastExecutionStatus = "FAILED";
        this.lastExecutionError = errorMessage;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 更新下次执行时间
     */
    public void updateNextExecutionTime(LocalDateTime nextExecutionTime) {
        this.nextExecutionTime = nextExecutionTime;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 设置任务状态
     */
    public void setStatus(TaskExecutionStatus status) {
        this.status = status;
        this.lastUpdated = LocalDateTime.now();
    }

    /**
     * 检查任务是否可以执行
     */
    public boolean canExecute() {
        return this.enabled && 
               this.status == TaskExecutionStatus.SCHEDULED && 
               this.nextExecutionTime != null &&
               LocalDateTime.now().isAfter(this.nextExecutionTime);
    }

    /**
     * 获取成功率
     */
    public double getSuccessRate() {
        if (this.executionCount == 0) {
            return 0.0;
        }
        return (double) this.successCount / this.executionCount * 100;
    }

    /**
     * 获取失败率
     */
    public double getFailureRate() {
        if (this.executionCount == 0) {
            return 0.0;
        }
        return (double) this.failureCount / this.executionCount * 100;
    }

    /**
     * 检查任务是否过期
     */
    public boolean isExpired() {
        return this.nextExecutionTime != null && 
               LocalDateTime.now().isAfter(this.nextExecutionTime.plusDays(1));
    }

    /**
     * 获取任务摘要信息
     */
    public String getSummary() {
        return String.format("任务[%d] %s - 状态: %s, 执行次数: %d, 成功率: %.2f%%", 
                this.taskId, this.taskName, this.status, this.executionCount, getSuccessRate());
    }
}
