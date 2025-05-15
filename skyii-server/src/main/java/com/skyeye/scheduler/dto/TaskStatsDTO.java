package com.skyeye.scheduler.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务统计数据传输对象
 */
@Data
public class TaskStatsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总任务数
     */
    private Long total = 0L;

    /**
     * 运行中任务数
     */
    private Long running = 0L;

    /**
     * 已调度任务数
     */
    private Long scheduled = 0L;

    /**
     * 已暂停任务数
     */
    private Long paused = 0L;

    /**
     * 已完成任务数
     */
    private Long completed = 0L;

    /**
     * 执行失败任务数
     */
    private Long failed = 0L;

    /**
     * 今日创建任务数
     */
    private Long todayCreated = 0L;

    /**
     * 今日执行任务数
     */
    private Long todayExecuted = 0L;
} 