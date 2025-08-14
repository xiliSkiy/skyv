package com.skyeye.device.service;

import com.skyeye.device.dto.DeviceTemplateDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 设备模板服务接口
 */
public interface DeviceTemplateService {

    /**
     * 分页查询设备模板
     */
    Page<DeviceTemplateDto> getDeviceTemplates(String name, String code, Long deviceTypeId, 
                                               Long protocolId, String manufacturer, Boolean isEnabled, 
                                               Pageable pageable);

    /**
     * 获取所有启用的设备模板
     */
    List<DeviceTemplateDto> getAllEnabledTemplates();

    /**
     * 根据设备类型获取模板
     */
    List<DeviceTemplateDto> getTemplatesByDeviceType(Long deviceTypeId);

    /**
     * 根据协议获取模板
     */
    List<DeviceTemplateDto> getTemplatesByProtocol(Long protocolId);

    /**
     * 根据ID获取设备模板详情
     */
    DeviceTemplateDto getDeviceTemplateById(Long id);

    /**
     * 创建设备模板
     */
    DeviceTemplateDto createDeviceTemplate(DeviceTemplateDto deviceTemplateDto);

    /**
     * 更新设备模板
     */
    DeviceTemplateDto updateDeviceTemplate(Long id, DeviceTemplateDto deviceTemplateDto);

    /**
     * 删除设备模板
     */
    void deleteDeviceTemplate(Long id);

    /**
     * 检查模板编码是否唯一
     */
    boolean isCodeUnique(String code, Long id);

    /**
     * 检查模板名称是否唯一
     */
    boolean isNameUnique(String name, Long id);

    /**
     * 获取模板统计信息
     */
    DeviceTemplateDto getTemplateStats(Long id);
} 