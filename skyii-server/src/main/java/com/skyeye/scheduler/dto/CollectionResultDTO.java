package com.skyeye.scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 采集结果DTO
 */
@Data
public class CollectionResultDTO {
    
    /**
     * 任务ID
     */
    @NotNull(message = "任务ID不能为空")
    private Long taskId;
    
    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;
    
    /**
     * 指标ID
     */
    @NotNull(message = "指标ID不能为空")
    private Long metricId;
    
    /**
     * 采集器ID
     */
    @NotNull(message = "采集器ID不能为空")
    private Long collectorId;
    
    /**
     * 指标值
     */
    private String metricValue;
    
    /**
     * 指标单位
     */
    private String metricUnit;
    
    /**
     * 采集时间
     */
    private LocalDateTime collectionTime;
    
    /**
     * 状态：SUCCESS, ERROR
     */
    @NotNull(message = "状态不能为空")
    private String status;
    
    /**
     * 执行时间(毫秒)
     */
    private Long executionTime;
    
    /**
     * 额外数据(JSON格式)
     */
    private String additionalData;
    
    /**
     * 错误信息
     */
    private String errorMessage;
} 