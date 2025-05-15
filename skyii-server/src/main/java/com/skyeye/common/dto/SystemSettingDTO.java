package com.skyeye.common.dto;

import lombok.Data;

/**
 * 系统设置DTO
 */
@Data
public class SystemSettingDTO {
    
    /**
     * 设置ID
     */
    private Long id;
    
    /**
     * 设置键
     */
    private String settingKey;
    
    /**
     * 设置值
     */
    private String settingValue;
    
    /**
     * 值类型：string,number,boolean,json
     */
    private String settingType;
    
    /**
     * 描述
     */
    private String description;
}