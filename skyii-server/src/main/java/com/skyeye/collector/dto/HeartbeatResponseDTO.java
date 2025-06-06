package com.skyeye.collector.dto;

import lombok.Data;

/**
 * 采集端心跳响应DTO
 */
@Data
public class HeartbeatResponseDTO {
    
    /**
     * 服务端时间
     */
    private String serverTime;
    
    /**
     * 服务端指令(CONTINUE/PAUSE/UPGRADE)
     */
    private String action;
} 