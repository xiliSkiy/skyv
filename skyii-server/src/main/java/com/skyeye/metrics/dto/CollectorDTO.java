package com.skyeye.metrics.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 采集器配置数据传输对象
 */
@Data
public class CollectorDTO {
    
    private Long id;
    
    @NotBlank(message = "采集器名称不能为空")
    @Size(max = 100, message = "采集器名称长度不能超过100个字符")
    private String collectorName;
    
    @NotBlank(message = "采集器类型不能为空")
    @Size(max = 50, message = "采集器类型长度不能超过50个字符")
    private String collectorType;
    
    private String host;
    
    private Integer port;
    
    private Boolean isMain = false;
    
    private String description;
    
    private String configParams;
    
    private Integer status = 1;
    
    private String statusInfo;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;
} 