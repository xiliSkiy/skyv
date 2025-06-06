package com.skyeye.collector.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 状态更新DTO
 */
@Data
public class StatusUpdateDTO {
    
    /**
     * 状态
     */
    @NotBlank(message = "状态不能为空")
    private String status;
    
    /**
     * 开始时间
     */
    private String startTime;
    
    /**
     * 结束时间
     */
    private String endTime;
    
    /**
     * 执行时间(毫秒)
     */
    private Integer executionTime;
    
    /**
     * 状态消息
     */
    private String message;
} 