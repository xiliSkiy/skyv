package com.skyeye.scheduler.dto;

import lombok.Data;

/**
 * 采集任务DTO
 */
@Data
public class CollectionTaskDTO {
    
    /**
     * 任务ID
     */
    private Long id;
    
    /**
     * 批次ID
     */
    private Long batchId;
    
    /**
     * 设备ID
     */
    private Long deviceId;
    
    /**
     * 指标ID
     */
    private Long metricId;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 指标名称
     */
    private String metricName;
    
    /**
     * 目标地址
     */
    private String targetAddress;
    
    /**
     * 目标端口
     */
    private Integer targetPort;
    
    /**
     * 采集类型
     */
    private String collectType;
    
    /**
     * 采集参数(JSON格式)
     */
    private String collectParams;
    
    /**
     * 认证信息(加密)
     */
    private String authInfo;
    
    /**
     * 超时时间(毫秒)
     */
    private Integer timeout = 30000;
    
    /**
     * 重试次数
     */
    private Integer retryCount = 3;
} 