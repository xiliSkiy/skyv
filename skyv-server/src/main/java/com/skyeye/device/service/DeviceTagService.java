package com.skyeye.device.service;

import com.skyeye.device.dto.DeviceTagDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 设备标签服务接口
 */
public interface DeviceTagService {

    /**
     * 分页查询设备标签
     */
    Page<DeviceTagDto> getDeviceTags(String name, String description, Pageable pageable);

    /**
     * 获取所有设备标签
     */
    List<DeviceTagDto> getAllDeviceTags();

    /**
     * 根据ID获取设备标签详情
     */
    DeviceTagDto getDeviceTagById(Long id);

    /**
     * 创建设备标签
     */
    DeviceTagDto createDeviceTag(DeviceTagDto deviceTagDto);

    /**
     * 更新设备标签
     */
    DeviceTagDto updateDeviceTag(Long id, DeviceTagDto deviceTagDto);

    /**
     * 删除设备标签
     */
    void deleteDeviceTag(Long id);

    /**
     * 检查标签名称是否唯一
     */
    boolean isNameUnique(String name, Long id);

    /**
     * 获取热门标签
     */
    List<DeviceTagDto> getPopularTags();

    /**
     * 获取标签统计信息
     */
    DeviceTagDto getTagStats(Long id);
} 