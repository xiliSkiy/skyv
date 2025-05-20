package com.skyeye.scheduler.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

/**
 * 指标模板应用历史DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricTemplateApplicationDTO {
    
    /**
     * ID
     */
    private Long id;
    
    /**
     * 模板ID
     */
    private Long templateId;
    
    /**
     * 模板名称
     */
    private String templateName;
    
    /**
     * 应用时间
     */
    private LocalDateTime applyTime;
    
    /**
     * 应用用户ID
     */
    private Long applyUserId;
    
    /**
     * 应用用户名
     */
    private String applyUser;
    
    /**
     * 影响指标数
     */
    private Integer affectedCount;
    
    /**
     * 状态：SUCCESS-成功，PARTIAL-部分成功，FAILED-失败
     */
    private String status;
    
    /**
     * 应用结果信息
     */
    private String resultInfo;
    
    /**
     * 应用设备ID列表
     */
    private List<Long> deviceIds;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;
} 