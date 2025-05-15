package com.skyeye.settings.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeye.common.dto.SystemSettingDTO;
import com.skyeye.common.entity.SystemSetting;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.settings.repository.SystemSettingRepository;
import com.skyeye.settings.service.SystemSettingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 系统设置Service实现类
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SystemSettingServiceImpl implements SystemSettingService {

    private final SystemSettingRepository systemSettingRepository;
    private final ObjectMapper objectMapper;

    @Override
    public List<SystemSettingDTO> getAllSettings() {
        List<SystemSetting> settings = systemSettingRepository.findAll();
        return settings.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public SystemSettingDTO getSetting(String key) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                .orElseThrow(() -> new BusinessException("设置不存在: " + key));
        return convertToDTO(setting);
    }

    @Override
    public Map<String, Object> getSettingsByGroup(String group) {
        List<SystemSetting> settings = systemSettingRepository.findBySettingKeyStartingWith(group + ".");
        Map<String, Object> result = new HashMap<>();
        
        for (SystemSetting setting : settings) {
            String key = setting.getSettingKey().substring(group.length() + 1); // 去掉前缀和点
            Object value = parseValue(setting.getSettingValue(), setting.getSettingType());
            result.put(key, value);
        }
        
        return result;
    }

    @Override
    @Transactional
    public SystemSettingDTO saveSetting(SystemSettingDTO dto) {
        SystemSetting setting = systemSettingRepository.findBySettingKey(dto.getSettingKey())
                .orElse(new SystemSetting());
        
        setting.setSettingKey(dto.getSettingKey());
        setting.setSettingValue(dto.getSettingValue());
        setting.setSettingType(dto.getSettingType());
        setting.setDescription(dto.getDescription());
        
        SystemSetting savedSetting = systemSettingRepository.save(setting);
        return convertToDTO(savedSetting);
    }

    @Override
    @Transactional
    public boolean batchSaveSettings(Map<String, Map<String, Object>> settings) {
        try {
            for (Map.Entry<String, Map<String, Object>> groupEntry : settings.entrySet()) {
                String group = groupEntry.getKey();
                Map<String, Object> groupSettings = groupEntry.getValue();
                
                for (Map.Entry<String, Object> settingEntry : groupSettings.entrySet()) {
                    String key = group + "." + settingEntry.getKey();
                    Object value = settingEntry.getValue();
                    String valueType = getValueType(value);
                    String valueStr = convertValueToString(value);
                    
                    SystemSetting setting = systemSettingRepository.findBySettingKey(key)
                            .orElse(new SystemSetting());
                    
                    setting.setSettingKey(key);
                    setting.setSettingValue(valueStr);
                    setting.setSettingType(valueType);
                    setting.setDescription(key);
                    
                    systemSettingRepository.save(setting);
                }
            }
            return true;
        } catch (Exception e) {
            log.error("批量保存设置失败", e);
            throw new BusinessException("批量保存设置失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteSetting(String key) {
        systemSettingRepository.deleteBySettingKey(key);
    }

    @Override
    public Map<String, Object> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // 系统信息
        OperatingSystemMXBean osBean = ManagementFactory.getOperatingSystemMXBean();
        info.put("osName", System.getProperty("os.name"));
        info.put("osVersion", System.getProperty("os.version"));
        info.put("osArch", System.getProperty("os.arch"));
        info.put("availableProcessors", osBean.getAvailableProcessors());
        info.put("systemLoad", osBean.getSystemLoadAverage());
        
        // 内存信息
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        info.put("heapMemoryUsage", memoryBean.getHeapMemoryUsage());
        info.put("nonHeapMemoryUsage", memoryBean.getNonHeapMemoryUsage());
        
        // JVM信息
        info.put("javaVersion", System.getProperty("java.version"));
        info.put("javaVendor", System.getProperty("java.vendor"));
        info.put("javaHome", System.getProperty("java.home"));
        
        // 应用信息
        info.put("appVersion", "v2.5.3"); // 从配置文件或环境变量中获取
        info.put("startTime", ManagementFactory.getRuntimeMXBean().getStartTime());
        info.put("uptime", ManagementFactory.getRuntimeMXBean().getUptime());
        info.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
        
        return info;
    }
    
    /**
     * 将实体转换为DTO
     */
    private SystemSettingDTO convertToDTO(SystemSetting entity) {
        SystemSettingDTO dto = new SystemSettingDTO();
        BeanUtils.copyProperties(entity, dto);
        return dto;
    }
    
    /**
     * 根据类型解析值
     */
    private Object parseValue(String value, String type) {
        if (value == null) {
            return null;
        }
        
        try {
            switch (type) {
                case "number":
                    if (value.contains(".")) {
                        return Double.parseDouble(value);
                    } else {
                        return Integer.parseInt(value);
                    }
                case "boolean":
                    return Boolean.parseBoolean(value);
                case "json":
                    return objectMapper.readValue(value, Map.class);
                case "string":
                default:
                    return value;
            }
        } catch (JsonProcessingException e) {
            log.error("解析JSON失败", e);
            return value;
        }
    }
    
    /**
     * 获取值的类型
     */
    private String getValueType(Object value) {
        if (value == null) {
            return "string";
        }
        
        if (value instanceof Number) {
            return "number";
        } else if (value instanceof Boolean) {
            return "boolean";
        } else if (value instanceof Map || value instanceof List) {
            return "json";
        } else {
            return "string";
        }
    }
    
    /**
     * 将值转换为字符串
     */
    private String convertValueToString(Object value) {
        if (value == null) {
            return null;
        }
        
        if (value instanceof Map || value instanceof List) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (JsonProcessingException e) {
                log.error("转换JSON失败", e);
                return value.toString();
            }
        } else {
            return value.toString();
        }
    }
}