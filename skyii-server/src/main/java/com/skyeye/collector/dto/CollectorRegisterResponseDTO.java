package com.skyeye.collector.dto;

import lombok.Data;

/**
 * 采集端注册响应DTO
 */
@Data
public class CollectorRegisterResponseDTO {
    
    /**
     * 服务端分配的采集端ID
     */
    private String collectorId;
    
    /**
     * 认证Token
     */
    private String token;
    
    /**
     * 服务端时间
     */
    private String serverTime;
} 