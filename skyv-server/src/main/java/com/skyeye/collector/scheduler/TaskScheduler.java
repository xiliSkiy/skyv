package com.skyeye.collector.scheduler;

import com.skyeye.collector.entity.CollectionTask;

import java.util.List;

/**
 * 任务调度器接口
 * 
 * @author SkyEye Team
 */
public interface TaskScheduler {

    /**
     * 调度任务
     * 
     * @param task 要调度的任务
     */
    void scheduleTask(CollectionTask task);

    /**
     * 取消任务调度
     * 
     * @param taskId 任务ID
     */
    void cancelTask(Long taskId);

    /**
     * 重新调度任务
     * 
     * @param task 要重新调度的任务
     */
    void rescheduleTask(CollectionTask task);

    /**
     * 暂停任务调度
     * 
     * @param taskId 任务ID
     */
    void pauseTask(Long taskId);

    /**
     * 恢复任务调度
     * 
     * @param taskId 任务ID
     */
    void resumeTask(Long taskId);

    /**
     * 获取所有已调度的任务ID
     * 
     * @return 任务ID列表
     */
    List<Long> getScheduledTaskIds();

    /**
     * 检查任务是否已调度
     * 
     * @param taskId 任务ID
     * @return 是否已调度
     */
    boolean isTaskScheduled(Long taskId);

    /**
     * 获取任务的下次执行时间
     * 
     * @param taskId 任务ID
     * @return 下次执行时间，如果未调度则返回null
     */
    java.time.LocalDateTime getNextExecutionTime(Long taskId);

    /**
     * 启动调度器
     */
    void start();

    /**
     * 停止调度器
     */
    void stop();

    /**
     * 获取调度器状态
     * 
     * @return 调度器状态
     */
    SchedulerStatus getStatus();

    /**
     * 获取调度统计信息
     * 
     * @return 统计信息
     */
    SchedulerStatistics getStatistics();

    /**
     * 清理过期的调度任务
     */
    void cleanupExpiredTasks();

    /**
     * 重新加载所有启用的任务
     */
    void reloadAllTasks();
}
