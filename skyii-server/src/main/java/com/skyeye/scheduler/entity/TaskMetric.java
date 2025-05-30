package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 任务指标关联实体类
 */
@Data
@Entity
@Table(name = "tb_task_metric")
@EqualsAndHashCode(callSuper = true)
public class TaskMetric extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    /**
     * 任务关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    /**
     * 指标ID
     */
    @Column(name = "metric_id")
    private Long metricId;

    /**
     * 指标名称
     */
    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;
    
    /**
     * 指标类型
     */
    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;
    
    /**
     * 指标参数（JSON格式）
     */
    @Column(name = "metric_params", columnDefinition = "TEXT")
    private String metricParams;
    
    /**
     * 设备类型
     */
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    /**
     * 指标描述
     */
    @Column(name = "description", length = 255)
    private String description;
    
    /**
     * 指标配置（JSON格式）
     */
    @Column(name = "config", columnDefinition = "TEXT")
    private String config;
    
    /**
     * 排序序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
} 