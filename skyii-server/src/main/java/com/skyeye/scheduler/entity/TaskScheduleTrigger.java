package com.skyeye.scheduler.entity;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务调度触发器实体类
 */
@Data
@Entity
@Table(name = "tb_task_schedule_trigger")
public class TaskScheduleTrigger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 触发器名称
     */
    @Column(name = "trigger_name", length = 100)
    private String triggerName;

    /**
     * 触发器类型：1-cron, 2-simple, 3-daily, 4-event
     */
    @Column(name = "trigger_type")
    private Integer triggerType;

    /**
     * cron表达式
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
     * 重复次数
     */
    @Column(name = "repeat_count")
    private Integer repeatCount;

    /**
     * 重复间隔(秒)
     */
    @Column(name = "repeat_interval")
    private Integer repeatInterval;

    /**
     * 事件触发条件
     */
    @Column(name = "event_condition", length = 255)
    private String eventCondition;

    /**
     * 优先级
     */
    @Column(name = "priority")
    private Integer priority;

    /**
     * 是否启用
     */
    @Column(name = "enabled")
    private Boolean enabled = true;

    /**
     * 已触发次数
     */
    @Column(name = "fired_count")
    private Integer firedCount = 0;

    /**
     * 上次触发时间
     */
    @Column(name = "last_fire_time")
    private LocalDateTime lastFireTime;

    /**
     * 下次触发时间
     */
    @Column(name = "next_fire_time")
    private LocalDateTime nextFireTime;

    /**
     * 创建时间
     */
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    @Column(name = "created_by", length = 50)
    private String createdBy;

    /**
     * 更新人
     */
    @Column(name = "updated_by", length = 50)
    private String updatedBy;

    /**
     * 采集器ID
     */
    @Column(name = "collector_id")
    private Long collectorId;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.firedCount == null) {
            this.firedCount = 0;
        }
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
} 