package com.skyeye.scheduler.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.Map;

/**
 * 指标模板数据传输对象
 */
@Data
public class MetricTemplateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 模板ID
     */
    private Long id;

    /**
     * 模板名称
     */
    private String templateName;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 指标类型
     */
    private String metricType;

    /**
     * 模板分类：video-视频分析，sensor-传感器数据
     */
    private String category;

    /**
     * 默认参数（JSON格式）
     */
    private String defaultParams;

    /**
     * 适用设备类型
     */
    private String deviceType;
    
    /**
     * 是否系统内置
     */
    private Boolean isSystem;
} 