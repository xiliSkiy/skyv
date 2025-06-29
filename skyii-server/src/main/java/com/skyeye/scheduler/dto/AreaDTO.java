package com.skyeye.scheduler.dto;

import lombok.Data;

import java.util.List;

/**
 * 区域DTO
 */
@Data
public class AreaDTO {
    
    /**
     * 区域ID
     */
    private String id;
    
    /**
     * 区域名称
     */
    private String name;
    
    /**
     * 区域级别
     */
    private Integer level;
    
    /**
     * 父区域ID
     */
    private String parentId;
    
    /**
     * 经度
     */
    private Double longitude;
    
    /**
     * 纬度
     */
    private Double latitude;
    
    /**
     * 设备数量
     */
    private Integer deviceCount;
    
    /**
     * 子区域
     */
    private List<AreaDTO> children;
    
    /**
     * 区域边界坐标点
     */
    private List<AreaPointDTO> boundary;
} 