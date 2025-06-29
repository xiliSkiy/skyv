package com.skyeye.scheduler.dto;

import java.time.LocalDateTime;
import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 指标DTO
 */
@Data
public class MetricDTO {
    
    /**
     * 指标ID
     */
    private Long id;
    
    /**
     * 指标名称
     */
    @NotBlank(message = "指标名称不能为空")
    @Size(max = 100, message = "指标名称长度不能超过100个字符")
    private String name;
    
    /**
     * 指标编码
     */
    @NotBlank(message = "指标标识符不能为空")
    @Size(max = 100, message = "指标标识符长度不能超过100个字符")
    private String code;
    
    /**
     * 指标描述
     */
    private String description;
    
    /**
     * 指标单位
     */
    private String unit;
    
    /**
     * 数据类型
     */
    private String dataType;
    
    /**
     * 采集方式
     */
    @NotBlank(message = "采集方式不能为空")
    @Size(max = 50, message = "采集方式长度不能超过50个字符")
    private String collectionMethod;
    
    /**
     * 采集周期（秒）
     */
    @NotNull(message = "采集频率不能为空")
    private Integer collectionInterval;
    
    /**
     * 分类ID
     */
    private String categoryId;
    
    /**
     * 分类名称
     */
    private String categoryName;
    
    /**
     * 是否为系统指标
     */
    private Boolean isSystem;
    
    /**
     * 标签列表
     */
    private List<String> tags;
    
    /**
     * 支持的设备类型
     */
    private List<String> supportedDeviceTypes;
    
    @NotBlank(message = "指标类型不能为空")
    @Size(max = 50, message = "指标类型长度不能超过50个字符")
    private String metricType;
    
    private String applicableDeviceType;
    
    private String collectionIntervalUnit = "s";
    
    private String dataFormat = "default";
    
    private String dataTransformType = "none";
    
    private String transformFormula;
    
    private String minValue;
    
    private String maxValue;
    
    private String processingScript;
    
    private Boolean enableCache = true;
    
    private String retentionPeriod = "30d";
    
    private Boolean thresholdEnabled = true;
    
    private String warningThreshold;
    
    private Integer warningDuration;
    
    private String criticalThreshold;
    
    private Integer criticalDuration;
    
    private String notificationGroups;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCollectionTime;
    
    private Integer status = 1;
}