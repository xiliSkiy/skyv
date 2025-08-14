package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceTypeDto;
import com.skyeye.device.service.DeviceTypeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备类型管理控制器
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/device-types")
@RequiredArgsConstructor
public class DeviceTypeController {

    private final DeviceTypeService deviceTypeService;

    /**
     * 获取设备类型树形列表
     */
    @GetMapping("/tree")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<DeviceTypeDto>> getDeviceTypeTree(@RequestParam(required = false) String name) {
        log.info("Getting device type tree with name: {}", name);
        
        List<DeviceTypeDto> result = deviceTypeService.getDeviceTypeTree(name);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 获取所有设备类型（平铺列表）
     */
    @GetMapping
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<DeviceTypeDto>> getAllDeviceTypes() {
        log.info("Getting all device types");
        
        List<DeviceTypeDto> result = deviceTypeService.getAllDeviceTypes();
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 根据ID获取设备类型详情
     */
    @GetMapping("/{id}")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<DeviceTypeDto> getDeviceTypeById(@PathVariable Long id) {
        log.info("Getting device type by id: {}", id);
        
        DeviceTypeDto result = deviceTypeService.getDeviceTypeById(id);
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 创建设备类型
     */
    @PostMapping
    // @PreAuthorize("hasAuthority('device:create')")
    public ApiResponse<DeviceTypeDto> createDeviceType(@Valid @RequestBody DeviceTypeDto deviceTypeDto) {
        log.info("Creating device type: {}", deviceTypeDto.getName());
        
        DeviceTypeDto result = deviceTypeService.createDeviceType(deviceTypeDto);
        return ApiResponse.success("创建成功", result);
    }

    /**
     * 更新设备类型
     */
    @PutMapping("/{id}")
    // @PreAuthorize("hasAuthority('device:update')")
    public ApiResponse<DeviceTypeDto> updateDeviceType(@PathVariable Long id, 
                                                     @Valid @RequestBody DeviceTypeDto deviceTypeDto) {
        log.info("Updating device type with id: {}", id);
        
        DeviceTypeDto result = deviceTypeService.updateDeviceType(id, deviceTypeDto);
        return ApiResponse.success("更新成功", result);
    }

    /**
     * 删除设备类型
     */
    @DeleteMapping("/{id}")
    // @PreAuthorize("hasAuthority('device:delete')")
    public ApiResponse<Void> deleteDeviceType(@PathVariable Long id) {
        log.info("Deleting device type with id: {}", id);
        
        deviceTypeService.deleteDeviceType(id);
        return ApiResponse.success("删除成功", (Void) null);
    }

    /**
     * 批量删除设备类型
     */
    @DeleteMapping("/batch")
    // @PreAuthorize("hasAuthority('device:delete')")
    public ApiResponse<Void> batchDeleteDeviceTypes(@RequestBody Map<String, List<Long>> request) {
        List<Long> ids = request.get("ids");
        log.info("Batch deleting device types with ids: {}", ids);
        
        deviceTypeService.batchDeleteDeviceTypes(ids);
        return ApiResponse.success("批量删除成功", (Void) null);
    }

    /**
     * 检查设备类型名称是否存在
     */
    @GetMapping("/check/name")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Boolean> checkNameExists(@RequestParam String name, 
                                              @RequestParam(required = false) Long excludeId) {
        log.debug("Checking if device type name exists: {}", name);
        
        boolean exists = deviceTypeService.existsByName(name, excludeId);
        return ApiResponse.success("检查完成", exists);
    }

    /**
     * 检查编码是否存在
     */
    @GetMapping("/check/code")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Boolean> checkCodeExists(@RequestParam String code, 
                                               @RequestParam(required = false) Long excludeId) {
        log.debug("Checking if device type code exists: {}", code);
        
        boolean exists = deviceTypeService.existsByCode(code, excludeId);
        return ApiResponse.success("检查完成", exists);
    }

    /**
     * 获取设备类型统计信息
     */
    @GetMapping("/stats")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<Object> getDeviceTypeStats() {
        log.info("Getting device type statistics");
        
        Object result = deviceTypeService.getDeviceTypeStats();
        return ApiResponse.success("查询成功", result);
    }

    /**
     * 根据父类型ID获取子类型
     */
    @GetMapping("/parent/{parentId}/children")
    // @PreAuthorize("hasAuthority('device:view')")
    public ApiResponse<List<DeviceTypeDto>> getChildTypes(@PathVariable Long parentId) {
        log.info("Getting child types for parent id: {}", parentId);
        
        List<DeviceTypeDto> result = deviceTypeService.getChildTypes(parentId);
        return ApiResponse.success("查询成功", result);
    }
} 