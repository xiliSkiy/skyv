package com.skyeye.scheduler.dto;

import lombok.Data;

import java.util.List;

/**
 * 指标分类DTO
 */
@Data
public class MetricCategoryDTO {
    
    /**
     * 分类ID
     */
    private String id;
    
    /**
     * 分类名称
     */
    private String name;
    
    /**
     * 分类描述
     */
    private String description;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 指标数量
     */
    private Integer metricCount;
    
    /**
     * 子分类
     */
    private List<MetricCategoryDTO> children;
} 