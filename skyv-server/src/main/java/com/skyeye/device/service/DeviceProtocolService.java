package com.skyeye.device.service;

import com.skyeye.device.dto.DeviceProtocolDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 设备协议服务接口
 */
public interface DeviceProtocolService {

    /**
     * 分页查询设备协议
     */
    Page<DeviceProtocolDto> getDeviceProtocols(String name, String code, String version, 
                                               Boolean isEnabled, Pageable pageable);

    /**
     * 获取所有启用的设备协议
     */
    List<DeviceProtocolDto> getAllEnabledProtocols();

    /**
     * 根据ID获取设备协议详情
     */
    DeviceProtocolDto getDeviceProtocolById(Long id);

    /**
     * 创建设备协议
     */
    DeviceProtocolDto createDeviceProtocol(DeviceProtocolDto deviceProtocolDto);

    /**
     * 更新设备协议
     */
    DeviceProtocolDto updateDeviceProtocol(Long id, DeviceProtocolDto deviceProtocolDto);

    /**
     * 删除设备协议
     */
    void deleteDeviceProtocol(Long id);

    /**
     * 检查协议编码是否唯一
     */
    boolean isCodeUnique(String code, Long id);

    /**
     * 检查协议名称是否唯一
     */
    boolean isNameUnique(String name, Long id);

    /**
     * 获取协议统计信息
     */
    DeviceProtocolDto getProtocolStats(Long id);
} 