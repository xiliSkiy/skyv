package com.skyeye.scheduler.dto;

import java.util.List;

import lombok.Data;

/**
 * 应用模板请求DTO
 */
@Data
public class ApplyTemplateRequest {
    
    /**
     * 设备ID列表
     */
    private List<Long> deviceIds;
    
    /**
     * 更新策略：merge-合并，overwrite-覆盖
     */
    private String updateStrategy = "merge";
    
    /**
     * 应用方式：selected-选择设备，all-全部适用设备
     */
    private String applyMode = "selected";
} 