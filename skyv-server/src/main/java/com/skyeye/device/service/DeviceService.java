package com.skyeye.device.service;

import com.skyeye.device.dto.DeviceDto;
import com.skyeye.device.dto.DeviceQueryRequest;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Map;

/**
 * 设备服务接口
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface DeviceService {

    /**
     * 分页查询设备列表
     */
    Page<DeviceDto> getDeviceList(DeviceQueryRequest request);

    /**
     * 获取所有设备列表（不分页）
     */
    List<DeviceDto> getAllDevices();

    /**
     * 根据ID获取设备详情
     */
    DeviceDto getDeviceById(Long id);

    /**
     * 创建设备
     */
    DeviceDto createDevice(DeviceDto deviceDto);

    /**
     * 更新设备
     */
    DeviceDto updateDevice(Long id, DeviceDto deviceDto);

    /**
     * 删除设备
     */
    void deleteDevice(Long id);

    /**
     * 批量删除设备
     */
    void batchDeleteDevices(List<Long> ids);

    /**
     * 测试设备连接
     */
    Map<String, Object> testDeviceConnection(Long id);

    /**
     * 获取设备状态统计
     */
    Map<String, Object> getDeviceStats();

    /**
     * 根据名称检查设备是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据IP地址检查设备是否存在
     */
    boolean existsByIpAddress(String ipAddress);

    /**
     * 根据MAC地址检查设备是否存在
     */
    boolean existsByMacAddress(String macAddress);

    /**
     * 根据序列号检查设备是否存在
     */
    boolean existsBySerialNumber(String serialNumber);
} 