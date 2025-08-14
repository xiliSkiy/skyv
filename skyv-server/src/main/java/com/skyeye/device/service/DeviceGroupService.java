package com.skyeye.device.service;

import com.skyeye.device.dto.DeviceGroupDto;

import java.util.List;

/**
 * 设备分组服务接口
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface DeviceGroupService {

    /**
     * 获取所有设备分组列表
     *
     * @return 设备分组列表
     */
    List<DeviceGroupDto> getAllDeviceGroups();

    /**
     * 根据ID获取设备分组详情
     *
     * @param id 设备分组ID
     * @return 设备分组详情
     */
    DeviceGroupDto getDeviceGroupById(Long id);

    /**
     * 创建设备分组
     *
     * @param deviceGroupDto 设备分组信息
     * @return 创建的设备分组
     */
    DeviceGroupDto createDeviceGroup(DeviceGroupDto deviceGroupDto);

    /**
     * 更新设备分组
     *
     * @param id 设备分组ID
     * @param deviceGroupDto 设备分组信息
     * @return 更新的设备分组
     */
    DeviceGroupDto updateDeviceGroup(Long id, DeviceGroupDto deviceGroupDto);

    /**
     * 删除设备分组
     *
     * @param id 设备分组ID
     */
    void deleteDeviceGroup(Long id);

    /**
     * 批量删除设备分组
     *
     * @param ids 设备分组ID列表
     */
    void batchDeleteDeviceGroups(List<Long> ids);

    /**
     * 检查分组名称是否存在
     *
     * @param name 分组名称
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByName(String name, Long excludeId);

    /**
     * 根据类型获取设备分组
     *
     * @param type 分组类型
     * @return 设备分组列表
     */
    List<DeviceGroupDto> getDeviceGroupsByType(String type);

    /**
     * 获取重要设备分组
     *
     * @return 重要设备分组列表
     */
    List<DeviceGroupDto> getImportantDeviceGroups();

    /**
     * 获取设备分组统计信息
     *
     * @return 统计信息
     */
    Object getDeviceGroupStats();
} 