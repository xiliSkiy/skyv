package com.skyeye.scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;

/**
 * 心跳DTO
 */
@Data
public class HeartbeatDTO {
    
    /**
     * 采集器唯一编码
     */
    @NotBlank(message = "采集器编码不能为空")
    private String collectorCode;
    
    /**
     * 状态信息
     */
    private String status;
    
    /**
     * 资源使用情况(JSON格式)
     */
    private String resources;
    
    /**
     * 版本
     */
    private String version;
    
    /**
     * 运行时间(秒)
     */
    private Long uptime;
    
    /**
     * 额外信息(JSON格式)
     */
    private String additionalInfo;
} 