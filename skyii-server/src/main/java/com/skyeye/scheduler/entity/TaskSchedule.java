package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务调度信息实体类
 */
@Data
@Entity
@Table(name = "tb_task_schedules")
@EqualsAndHashCode(callSuper = true)
public class TaskSchedule extends BaseEntity {

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 任务关联
     */
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;
    
    /**
     * Cron表达式
     */
    @Column(name = "cron_expression", length = 100)
    private String cronExpression;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;

    /**
     * 调度类型：realtime-实时执行，scheduled-定时执行，periodic-周期执行，triggered-触发执行
     */
    @Column(name = "schedule_type", nullable = false, length = 20)
    private String scheduleType;
    
    /**
     * 执行频率：once-单次，daily-每天，weekly-每周，monthly-每月，custom-自定义
     */
    @Column(name = "frequency", length = 20)
    private String frequency;
    
    /**
     * 调度时间
     */
    @Column(name = "schedule_time")
    private LocalDateTime scheduleTime;
    
    /**
     * 周几执行（JSON数组格式）
     */
    @Column(name = "weekdays", length = 20)
    private String weekdays;
    
    /**
     * 间隔值
     */
    @Column(name = "interval_value")
    private Integer intervalValue;
    
    /**
     * 间隔单位：minutes-分钟，hours-小时，days-天
     */
    @Column(name = "interval_unit", length = 10)
    private String intervalUnit;
    
    /**
     * 触发类型：event-事件触发，threshold-阈值触发，api-API调用触发
     */
    @Column(name = "trigger_type", length = 20)
    private String triggerType;
    
    /**
     * 触发事件
     */
    @Column(name = "trigger_event", length = 50)
    private String triggerEvent;
    
    /**
     * 开始日期
     */
    @Column(name = "start_date")
    private LocalDateTime startDate;
    
    /**
     * 结束日期
     */
    @Column(name = "end_date")
    private LocalDateTime endDate;
    
    /**
     * 每日开始时间（格式：HH:mm）
     */
    @Column(name = "daily_start_time", length = 5)
    private String dailyStartTime;
    
    /**
     * 每日结束时间（格式：HH:mm）
     */
    @Column(name = "daily_end_time", length = 5)
    private String dailyEndTime;
    
    /**
     * 最大执行次数
     */
    @Column(name = "max_executions")
    private Integer maxExecutions;
    
    /**
     * 超时时间（分钟）
     */
    @Column(name = "timeout_minutes")
    private Integer timeoutMinutes;
    
    /**
     * 重试策略：none-不重试，immediate-立即重试，incremental-递增间隔重试
     */
    @Column(name = "retry_strategy", length = 20)
    private String retryStrategy;
    
    /**
     * 最大重试次数
     */
    @Column(name = "max_retries")
    private Integer maxRetries;
    
    /**
     * 调度优先级：high-高，normal-中，low-低
     */
    @Column(name = "priority_level", length = 10)
    private String priorityLevel;
    
    /**
     * 是否启用通知：0-禁用，1-启用
     */
    @Column(name = "enable_notifications")
    private Boolean enableNotifications = true;
    
    /**
     * 是否自动重启：0-禁用，1-启用
     */
    @Column(name = "enable_auto_restart")
    private Boolean enableAutoRestart = false;
    
    /**
     * 上次执行时间
     */
    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;
    
    /**
     * 下次执行时间
     */
    @Column(name = "next_execution_time")
    private LocalDateTime nextExecutionTime;
    
    /**
     * 已执行次数
     */
    @Column(name = "execution_count")
    private Integer executionCount = 0;
} 