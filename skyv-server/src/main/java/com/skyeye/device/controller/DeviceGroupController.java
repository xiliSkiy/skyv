package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceGroupDto;
import com.skyeye.device.service.DeviceGroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备分组管理控制器
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/device-groups")
@RequiredArgsConstructor
public class DeviceGroupController {

    private final DeviceGroupService deviceGroupService;

    /**
     * 获取所有设备分组
     */
    @GetMapping
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<DeviceGroupDto>> getAllDeviceGroups() {
        log.info("Getting all device groups");
        
        List<DeviceGroupDto> result = deviceGroupService.getAllDeviceGroups();
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 根据ID获取设备分组详情
     */
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<DeviceGroupDto> getDeviceGroupById(@PathVariable Long id) {
        log.info("Getting device group by id: {}", id);
        
        DeviceGroupDto result = deviceGroupService.getDeviceGroupById(id);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 创建设备分组
     */
    @PostMapping
    // @PreAuthorize("hasAuthority('device:create')")
    public ApiResponse<DeviceGroupDto> createDeviceGroup(@Valid @RequestBody DeviceGroupDto deviceGroupDto) {
        log.info("Creating device group: {}", deviceGroupDto.getName());
        
        DeviceGroupDto result = deviceGroupService.createDeviceGroup(deviceGroupDto);
        return ApiResponse.success("创建成功", result);
    }

    /**
     * 更新设备分组
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('device:update')")
    public ApiResponse<DeviceGroupDto> updateDeviceGroup(@PathVariable Long id, 
                                                       @Valid @RequestBody DeviceGroupDto deviceGroupDto) {
        log.info("Updating device group with id: {}", id);
        
        DeviceGroupDto result = deviceGroupService.updateDeviceGroup(id, deviceGroupDto);
        return ApiResponse.success("更新成功", result);
    }

    /**
     * 删除设备分组
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('device:delete')")
    public ApiResponse<Void> deleteDeviceGroup(@PathVariable Long id) {
        log.info("Deleting device group with id: {}", id);
        
        deviceGroupService.deleteDeviceGroup(id);
        return ApiResponse.success("删除成功", (Void) null);
    }

    /**
     * 批量删除设备分组
     */
    @DeleteMapping("/batch")
    // @PreAuthorize("hasAuthority('device:delete')")
    public ApiResponse<Void> batchDeleteDeviceGroups(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        log.info("Batch deleting device groups with ids: {}", ids);
        
        deviceGroupService.batchDeleteDeviceGroups(ids);
        return ApiResponse.success("批量删除成功", (Void) null);
    }

    /**
     * 检查设备分组名称是否存在
     */
    @GetMapping("/check/name")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Boolean> checkNameExists(@RequestParam String name, 
                                              @RequestParam(required = false) Long excludeId) {
        log.debug("Checking if device group name exists: {}", name);
        
        boolean exists = deviceGroupService.existsByName(name, excludeId);
        return ApiResponse.success("检查完成", exists);
    }

    /**
     * 根据类型获取设备分组
     */
    @GetMapping("/by-type/{type}")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<DeviceGroupDto>> getDeviceGroupsByType(@PathVariable String type) {
        log.info("Getting device groups by type: {}", type);
        
        List<DeviceGroupDto> result = deviceGroupService.getDeviceGroupsByType(type);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 获取重要设备分组
     */
    @GetMapping("/important")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<DeviceGroupDto>> getImportantDeviceGroups() {
        log.info("Getting important device groups");
        
        List<DeviceGroupDto> result = deviceGroupService.getImportantDeviceGroups();
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 获取设备分组统计信息
     */
    @GetMapping("/stats")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Object> getDeviceGroupStats() {
        log.info("Getting device group statistics");
        
        Object result = deviceGroupService.getDeviceGroupStats();
        return ApiResponse.success("查询成功", result);
    }
} 