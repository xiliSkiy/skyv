package com.skyeye.scheduler.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

import java.io.Serializable;import java.util.List;import java.util.Map;

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
    
    private Long createdBy;
    
    private String createdByName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")    private LocalDateTime updatedAt;        /**     * 模板包含的指标列表     */    private List<MetricDTO> metrics;        /**     * 指标数量     */    private Integer metricCount;        /**     * 使用次数     */    private Integer usageCount;        /**     * 指标ID列表，用于创建和更新模板     */    private List<Long> metricIds;
    
    // 分类枚举值
    public static final String CATEGORY_VIDEO = "video";
    public static final String CATEGORY_SENSOR = "sensor";
    public static final String CATEGORY_SYSTEM = "system";
    public static final String CATEGORY_CUSTOM = "custom";
    
    // 获取分类名称
    public String getCategoryName() {
        if (category == null) {
            return "未分类";
        }
        
        switch (category) {
            case CATEGORY_VIDEO:
                return "视频分析";
            case CATEGORY_SENSOR:
                return "传感器数据";
            case CATEGORY_SYSTEM:
                return "系统指标";
            case CATEGORY_CUSTOM:
                return "自定义指标";
            default:
                return "未分类";
        }
    }
} 