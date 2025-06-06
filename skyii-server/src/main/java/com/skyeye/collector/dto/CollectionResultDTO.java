package com.skyeye.collector.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

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
     * 批次ID
     */
    @NotNull(message = "批次ID不能为空")
    private Long batchId;
    
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
     * 结果值
     */
    @NotBlank(message = "结果值不能为空")
    private String resultValue;
    
    /**
     * 结果类型
     */
    @NotBlank(message = "结果类型不能为空")
    private String resultType;
    
    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
    
    /**
     * 执行时间(毫秒)
     */
    @NotNull(message = "执行时间不能为空")
    private Integer executionTime;
    
    /**
     * 时间戳
     */
    @NotBlank(message = "时间戳不能为空")
    private String timestamp;
} 