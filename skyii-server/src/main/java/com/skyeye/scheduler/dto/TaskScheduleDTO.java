package com.skyeye.scheduler.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务调度数据传输对象
 */
@Data
public class TaskScheduleDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * ID
     */
    private Long id;

    /**
     * 调度类型：realtime-实时执行，scheduled-定时执行，periodic-周期执行，triggered-触发执行
     */
    private String scheduleType;

    /**
     * Cron表达式
     */
    private String cronExpression;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行频率：once-单次，daily-每天，weekly-每周，monthly-每月，custom-自定义
     */
    private String frequency;

    /**
     * 调度时间
     */
    private String scheduleTime;

    /**
     * 周几执行
     */
    private List<Integer> weekdays;

    /**
     * 间隔值
     */
    private Integer intervalValue;

    /**
     * 间隔单位：minutes-分钟，hours-小时，days-天
     */
    private String intervalUnit;

    /**
     * 触发类型：event-事件触发，threshold-阈值触发，api-API调用触发
     */
    private String triggerType;

    /**
     * 触发事件
     */
    private String triggerEvent;

    /**
     * 最大执行次数
     */
    private Integer maxExecutions;

    /**
     * 超时时间（分钟）
     */
    private Integer timeoutMinutes;

    /**
     * 重试策略
     */
    private String retryStrategy;

    /**
     * 最大重试次数
     */
    private Integer maxRetries;

    /**
     * 执行限制
     */
    private Map<String, Object> limits;

    /**
     * 高级设置
     */
    private Map<String, Object> advanced;

    /**
     * 上次执行时间
     */
    private LocalDateTime lastExecutionTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecutionTime;

    /**
     * 已执行次数
     */
    private Integer executionCount;
} 