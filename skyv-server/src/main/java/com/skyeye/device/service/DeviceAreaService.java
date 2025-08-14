package com.skyeye.device.service;

import com.skyeye.device.dto.DeviceAreaDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 设备区域服务接口
 */
public interface DeviceAreaService {

    /**
     * 分页查询设备区域
     */
    Page<DeviceAreaDto> getDeviceAreas(String name, String code, Long parentId, Integer level, Pageable pageable);

    /**
     * 获取设备区域树形结构
     */
    List<DeviceAreaDto> getDeviceAreaTree();

    /**
     * 根据ID获取设备区域详情
     */
    DeviceAreaDto getDeviceAreaById(Long id);

    /**
     * 创建设备区域
     */
    DeviceAreaDto createDeviceArea(DeviceAreaDto deviceAreaDto);

    /**
     * 更新设备区域
     */
    DeviceAreaDto updateDeviceArea(Long id, DeviceAreaDto deviceAreaDto);

    /**
     * 删除设备区域
     */
    void deleteDeviceArea(Long id);

    /**
     * 检查区域名称是否唯一
     */
    boolean isNameUnique(String name, Long id);

    /**
     * 检查区域编码是否唯一
     */
    boolean isCodeUnique(String code, Long id);

    /**
     * 获取区域统计信息
     */
    DeviceAreaDto getAreaStats(Long id);
} 