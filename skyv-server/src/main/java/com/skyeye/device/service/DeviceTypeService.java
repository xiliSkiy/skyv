package com.skyeye.device.service;

import com.skyeye.device.dto.DeviceTypeDto;

import java.util.List;

/**
 * 设备类型服务接口
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public interface DeviceTypeService {

    /**
     * 获取设备类型树形列表
     *
     * @param name 类型名称（可选，用于搜索）
     * @return 设备类型树形列表
     */
    List<DeviceTypeDto> getDeviceTypeTree(String name);

    /**
     * 获取所有设备类型（平铺列表）
     *
     * @return 设备类型列表
     */
    List<DeviceTypeDto> getAllDeviceTypes();

    /**
     * 根据ID获取设备类型详情
     *
     * @param id 设备类型ID
     * @return 设备类型详情
     */
    DeviceTypeDto getDeviceTypeById(Long id);

    /**
     * 创建设备类型
     *
     * @param deviceTypeDto 设备类型信息
     * @return 创建的设备类型
     */
    DeviceTypeDto createDeviceType(DeviceTypeDto deviceTypeDto);

    /**
     * 更新设备类型
     *
     * @param id 设备类型ID
     * @param deviceTypeDto 设备类型信息
     * @return 更新的设备类型
     */
    DeviceTypeDto updateDeviceType(Long id, DeviceTypeDto deviceTypeDto);

    /**
     * 删除设备类型
     *
     * @param id 设备类型ID
     */
    void deleteDeviceType(Long id);

    /**
     * 批量删除设备类型
     *
     * @param ids 设备类型ID列表
     */
    void batchDeleteDeviceTypes(List<Long> ids);

    /**
     * 检查编码是否存在
     *
     * @param code 类型编码
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByCode(String code, Long excludeId);

    /**
     * 检查名称是否存在
     *
     * @param name 类型名称
     * @param excludeId 排除的ID（用于更新时检查）
     * @return 是否存在
     */
    boolean existsByName(String name, Long excludeId);

    /**
     * 获取设备类型统计信息
     *
     * @return 统计信息
     */
    Object getDeviceTypeStats();

    /**
     * 根据父类型ID获取子类型
     *
     * @param parentId 父类型ID
     * @return 子类型列表
     */
    List<DeviceTypeDto> getChildTypes(Long parentId);
} 