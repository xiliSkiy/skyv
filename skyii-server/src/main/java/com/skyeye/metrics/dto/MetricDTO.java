package com.skyeye.metrics.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 指标配置数据传输对象
 */
@Data
public class MetricDTO {
    
    private Long id;
    
    @NotBlank(message = "指标名称不能为空")
    @Size(max = 100, message = "指标名称长度不能超过100个字符")
    private String metricName;
    
    @NotBlank(message = "指标标识符不能为空")
    @Size(max = 100, message = "指标标识符长度不能超过100个字符")
    private String metricKey;
    
    @NotBlank(message = "指标类型不能为空")
    @Size(max = 50, message = "指标类型长度不能超过50个字符")
    private String metricType;
    
    private String description;
    
    private String applicableDeviceType;
    
    @NotBlank(message = "采集方式不能为空")
    @Size(max = 50, message = "采集方式长度不能超过50个字符")
    private String collectionMethod;
    
    private String protocolConfig;
    
    @NotNull(message = "采集频率不能为空")
    private Integer collectionInterval;
    
    private String collectionIntervalUnit = "s";
    
    private String dataType = "gauge";
    
    private String unit;
    
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