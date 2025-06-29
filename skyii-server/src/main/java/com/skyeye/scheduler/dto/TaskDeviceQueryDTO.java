package com.skyeye.scheduler.dto;

import lombok.Data;

/**
 * 任务设备查询DTO
 */
@Data
public class TaskDeviceQueryDTO {
    
    /**
     * 关键词搜索
     */
    private String keyword;
    
    /**
     * 设备状态
     */
    private String status;
    
    /**
     * 排序字段
     */
    private String sortBy;
    
    /**
     * 排序方向
     */
    private String sortDirection;
    
    /**
     * 页码
     */
    private Integer page = 1;
    
    /**
     * 每页数量
     */
    private Integer size = 10;
    
    /**
     * 视图类型（卡片/列表）
     */
    private String viewType = "card";
} 