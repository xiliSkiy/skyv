package com.skyeye.scheduler.dto;

import lombok.Data;

import java.util.List;

/**
 * 设备类型树DTO
 */
@Data
public class DeviceTypeTreeDTO {
    
    /**
     * 类型ID
     */
    private String id;
    
    /**
     * 类型名称
     */
    private String name;
    
    /**
     * 图标
     */
    private String icon;
    
    /**
     * 设备数量
     */
    private Integer deviceCount;
    
    /**
     * 子类型
     */
    private List<DeviceTypeTreeDTO> children;
} 