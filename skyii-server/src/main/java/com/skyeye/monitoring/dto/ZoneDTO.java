package com.skyeye.monitoring.dto;

import java.time.LocalDateTime;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 监控区域数据传输对象
 */
@Data
public class ZoneDTO {
    
    private Long id;
    
    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称长度不能超过100个字符")
    private String zoneName;
    
    @NotBlank(message = "区域编码不能为空")
    @Size(max = 50, message = "区域编码长度不能超过50个字符")
    private String zoneCode;
    
    private String description;
    
    private Long parentId;
    
    private Integer level;
    
    private Integer sortOrder;
    
    private Integer status;
    
    private Long createdBy;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 