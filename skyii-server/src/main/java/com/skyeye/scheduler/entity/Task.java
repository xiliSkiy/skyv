package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
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
     * 状态：0-禁用，1-启用
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;
    
    /**
     * 任务优先级：1-低，2-中，3-高
     */
    @Column(name = "priority")
    private Integer priority = 2;
    
    /**
     * 任务标签（逗号分隔）
     */
    @Column(name = "tags", length = 255)
    private String tags;
    
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