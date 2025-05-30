package com.skyeye.scheduler.entity;

import com.skyeye.collector.entity.Collector;
import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务批次实体类
 */
@Data
@Entity
@Table(name = "tb_task_batches")
@EqualsAndHashCode(callSuper = true)
public class TaskBatch extends BaseEntity {
    
    /**
     * 关联任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    /**
     * 采集器ID
     */
    @Column(name = "collector_id", nullable = false)
    private Long collectorId;
    
    /**
     * 批次名称
     */
    @Column(name = "batch_name", nullable = false)
    private String batchName;
    
    /**
     * 批次描述
     */
    @Column(name = "description")
    private String description;
    
    /**
     * 状态：PENDING, PROCESSING, COMPLETED, FAILED, CANCELLED
     */
    @Column(name = "status", nullable = false)
    private String status;
    
    /**
     * 计划执行时间
     */
    @Column(name = "scheduled_time")
    private LocalDateTime scheduledTime;
    
    /**
     * 最后执行时间
     */
    @Column(name = "last_execution_time")
    private LocalDateTime lastExecutionTime;
    
    /**
     * 总任务数
     */
    @Column(name = "total_tasks")
    private Integer totalTasks = 0;
    
    /**
     * 已完成任务数
     */
    @Column(name = "completed_tasks")
    private Integer completedTasks = 0;
    
    /**
     * 优先级
     */
    @Column(name = "priority")
    private Integer priority = 5;
    
    /**
     * 任务关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;
    
    /**
     * 采集器关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collector_id", insertable = false, updatable = false)
    private Collector collector;
} 