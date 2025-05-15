package com.skyeye.scheduler.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 任务指标数据传输对象
 */
@Data
public class TaskMetricDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 指标ID
     */
    private Long id;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 指标参数（JSON格式）
     */
    private String metricParams;

    /**
     * 设备类型
     */
    private String deviceType;

    /**
     * 指标描述
     */
    private String description;

    /**
     * 适用设备数量
     */
    private Integer deviceCount;

    /**
     * 指标配置
     */
    private Map<String, Object> config;

    /**
     * 排序序号
     */
    private Integer sortOrder;
} 