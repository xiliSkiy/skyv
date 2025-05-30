package com.skyeye.scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 任务调度触发器数据传输对象
 */
@Data
public class TaskScheduleTriggerDTO {
    
    /**
     * 主键ID
     */
    private Long id;
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    
    /**
     * 触发器名称
     */
    @NotBlank(message = "触发器名称不能为空")
    private String triggerName;
    
    /**
     * 触发器类型：1-cron, 2-simple, 3-daily, 4-event
     */
    @NotNull(message = "触发器类型不能为空")
    private Integer triggerType;
    
    /**
     * cron表达式
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
     * 重复次数
     */
    private Integer repeatCount;
    
    /**
     * 重复间隔(秒)
     */
    private Integer repeatInterval;
    
    /**
     * 事件触发条件
     */
    private String eventCondition;
    
    /**
     * 优先级
     */
    private Integer priority;
    
    /**
     * 是否启用
     */
    private Boolean enabled;
    
    /**
     * 上次触发时间
     */
    private LocalDateTime lastFireTime;
    
    /**
     * 下次触发时间
     */
    private LocalDateTime nextFireTime;
    
    /**
     * 采集器ID
     */
    private Long collectorId;
    
    /**
     * 已触发次数
     */
    private Integer firedCount;
} 