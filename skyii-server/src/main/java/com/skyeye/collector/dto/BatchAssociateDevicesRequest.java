package com.skyeye.collector.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 批量关联设备请求
 */
@Data
public class BatchAssociateDevicesRequest {
    
    /**
     * 采集器ID
     */
    @NotNull(message = "采集器ID不能为空")
    private Long collectorId;
    
    /**
     * 设备ID列表
     */
    @NotNull(message = "设备ID列表不能为空")
    private List<Long> deviceIds;
    
    /**
     * 创建方式：0-手动分配，1-自动分配
     */
    private Integer createMode = 0;
    
    /**
     * 是否应用指标模板
     */
    private Boolean applyMetricTemplate = false;
    
    /**
     * 指标模板ID，当applyMetricTemplate为true时必填
     */
    private Long metricTemplateId;
} 