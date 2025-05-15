package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 指标模板实体类
 */
@Data
@Entity
@Table(name = "tb_metric_templates")
@EqualsAndHashCode(callSuper = true)
public class MetricTemplate extends BaseEntity {

    /**
     * 模板名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String templateName;

    /**
     * 模板描述
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 指标类型
     */
    @Column(name = "metric_type", nullable = false, length = 50)
    private String metricType;

    /**
     * 模板分类：video-视频分析，sensor-传感器数据
     */
    @Column(name = "category", nullable = false, length = 20)
    private String category;

    /**
     * 默认参数（JSON格式）
     */
    @Column(name = "default_params", columnDefinition = "TEXT")
    private String defaultParams;

    /**
     * 适用设备类型
     */
    @Column(name = "device_type", length = 50)
    private String deviceType;

    /**
     * 是否系统内置：0-否，1-是
     */
    @Column(name = "is_system")
    private Boolean isSystem = false;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;
} 