package com.skyeye.settings.service;

import com.skyeye.common.dto.SystemSettingDTO;

import java.util.List;
import java.util.Map;

/**
 * 系统设置Service接口
 */
public interface SystemSettingService {
    
    /**
     * 获取所有系统设置
     * @return 设置列表
     */
    List<SystemSettingDTO> getAllSettings();
    
    /**
     * 根据设置键获取设置
     * @param key 设置键
     * @return 设置对象
     */
    SystemSettingDTO getSetting(String key);
    
    /**
     * 根据设置键前缀获取设置组
     * @param group 设置组前缀
     * @return 设置组对象
     */
    Map<String, Object> getSettingsByGroup(String group);
    
    /**
     * 保存或更新设置
     * @param dto 设置对象
     * @return 更新后的设置对象
     */
    SystemSettingDTO saveSetting(SystemSettingDTO dto);
    
    /**
     * 批量保存或更新设置
     * @param settings 设置组
     * @return 是否成功
     */
    boolean batchSaveSettings(Map<String, Map<String, Object>> settings);
    
    /**
     * 删除设置
     * @param key 设置键
     */
    void deleteSetting(String key);
    
    /**
     * 获取系统信息
     * @return 系统信息
     */
    Map<String, Object> getSystemInfo();
}