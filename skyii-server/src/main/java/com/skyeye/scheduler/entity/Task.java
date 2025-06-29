package com.skyeye.scheduler.entity;

import com.skyeye.collector.entity.Collector;
import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务实体类
 */
@Data
@Entity
@Table(name = "tb_tasks")
@EqualsAndHashCode(callSuper = true)
public class Task extends BaseEntity {

    /**
     * 任务名称
     */
    @Column(name = "task_name", nullable = false, length = 100)
    private String taskName;

    /**
     * 任务类型
     */
    @Column(name = "task_type", nullable = false, length = 50)
    private String taskType;

    /**
     * 任务描述
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Cron表达式
     */
    @Column(name = "cron_expression", length = 50)
    private String cronExpression;

    /**
     * 任务参数（JSON格式）
     */
    @Column(name = "task_params", columnDefinition = "TEXT")
    private String taskParams;

    /**
     * 任务状态：1-待执行，2-已调度，3-执行中，4-已完成，5-已失败，6-已暂停
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
    /**
     * 优先级：1-低，2-中，3-高
     */
    @Column(name = "priority")
    private Integer priority = 2;
    
    /**
     * 标签（逗号分隔）
     */
    @Column(name = "tags", length = 255)
    private String tags;
    
    /**
     * 计划开始时间
     */
    @Column(name = "planned_start_time")
    private LocalDateTime plannedStartTime;
    
    /**
     * 计划结束时间
     */
    @Column(name = "planned_end_time")
    private LocalDateTime plannedEndTime;
    
    /**
     * 实际开始时间
     */
    @Column(name = "actual_start_time")
    private LocalDateTime actualStartTime;
    
    /**
     * 实际结束时间
     */
    @Column(name = "actual_end_time")
    private LocalDateTime actualEndTime;
    
    /**
     * 执行耗时(毫秒)
     */
    @Column(name = "execution_time")
    private Long executionTime;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 执行结果
     */
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;
    
    /**
     * 是否是采集任务
     */
    @Column(name = "is_collection_task")
    private Boolean isCollectionTask = false;
    
    /**
     * 采集器ID
     */
    @Column(name = "collector_id")
    private Long collectorId;
    
    /**
     * 采集类型：SSH, SNMP, HTTP等
     */
    @Column(name = "collect_type")
    private String collectType;
    
    /**
     * 采集参数(JSON格式)
     */
    @Column(name = "collect_params", columnDefinition = "TEXT")
    private String collectParams;
    
    /**
     * 是否启用报警
     */
    @Column(name = "enable_alert")
    private Boolean enableAlert = false;
    
    /**
     * 报警级别：info, warning, error, critical
     */
    @Column(name = "alert_level", length = 20)
    private String alertLevel;
    
    /**
     * 报警通知方式（逗号分隔）：email, sms, webhook, etc.
     */
    @Column(name = "alert_notify_types", length = 100)
    private String alertNotifyTypes;
    
    /**
     * 报警通知接收人ID（逗号分隔）
     */
    @Column(name = "alert_receivers", length = 255)
    private String alertReceivers;
    
    /**
     * 采集器关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id", insertable = false, updatable = false)
    private Collector collector;

    /**
     * 任务设备关联（一对多）
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskDevice> taskDevices;
    
    /**
     * 任务指标关联（一对多）
     */
    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<TaskMetric> taskMetrics;
    
    /**
     * 任务调度关联（一对一）
     */
    @OneToOne(mappedBy = "task", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TaskSchedule taskSchedule;
} 