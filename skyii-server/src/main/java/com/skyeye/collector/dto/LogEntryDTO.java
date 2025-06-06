package com.skyeye.collector.dto;

import javax.validation.constraints.NotBlank;
import lombok.Data;
import java.util.Map;

/**
 * 日志条目DTO
 */
@Data
public class LogEntryDTO {
    
    /**
     * 日志级别
     */
    @NotBlank(message = "日志级别不能为空")
    private String level;
    
    /**
     * 日志消息
     */
    @NotBlank(message = "日志消息不能为空")
    private String message;
    
    /**
     * 时间戳
     */
    @NotBlank(message = "时间戳不能为空")
    private String timestamp;
    
    /**
     * 关联任务ID
     */
    private Long taskId;
    
    /**
     * 关联批次ID
     */
    private Long batchId;
    
    /**
     * 上下文信息
     */
    private Map<String, Object> context;
} 