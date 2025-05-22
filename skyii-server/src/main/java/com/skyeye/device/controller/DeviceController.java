package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.common.response.PageMeta;
import com.skyeye.device.dto.DeviceDTO;
import com.skyeye.device.entity.Device;
import com.skyeye.device.service.DeviceService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 设备控制器
 */
@Slf4j
@RestController
@RequestMapping("/devices")
@Api(tags = "设备管理")
public class DeviceController {

    @Autowired
    private DeviceService deviceService;

    /**
     * 创建设备
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("创建设备")
    public ApiResponse<Device> createDevice(@Valid @RequestBody DeviceDTO deviceDTO) {
        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);
        
        Device createdDevice = deviceService.createDevice(device);
        return ApiResponse.success(createdDevice, "设备创建成功");
    }

    /**
     * 更新设备
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("更新设备")
    public ApiResponse<Device> updateDevice(
            @ApiParam("设备ID") @PathVariable Long id,
            @Valid @RequestBody DeviceDTO deviceDTO) {
        Device device = new Device();
        BeanUtils.copyProperties(deviceDTO, device);
        
        Device updatedDevice = deviceService.updateDevice(id, device);
        return ApiResponse.success(updatedDevice, "设备更新成功");
    }

    /**
     * 删除设备
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ApiOperation("删除设备")
    public ApiResponse<Void> deleteDevice(@ApiParam("设备ID") @PathVariable Long id) {
        deviceService.deleteDevice(id);
        return ApiResponse.success(null, "设备删除成功");
    }

    /**
     * 获取设备详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取设备详情")
    public ApiResponse<Device> getDevice(@ApiParam("设备ID") @PathVariable Long id) {
        return deviceService.findById(id)
                .map(device -> ApiResponse.success(device, "获取设备成功"))
                .orElse(ApiResponse.error("设备不存在"));
    }

    /**
     * 分页查询设备
     */
    @GetMapping
    @ApiOperation("分页查询设备")
    public ApiResponse<List<Device>> getDevices(
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页记录数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("排序字段") @RequestParam(defaultValue = "id") String sort,
            @ApiParam("排序方向") @RequestParam(defaultValue = "desc") String direction,
            @ApiParam("采集器ID") @RequestParam(required = false) Long collectorId,
            @ApiParam("包含已分配设备") @RequestParam(required = false, defaultValue = "false") boolean includeAssigned) {
        
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        
        Page<Device> devicePage;
        if (collectorId != null) {
            // 如果指定了采集器ID，根据采集器ID查询设备
            devicePage = deviceService.findByCollectorId(collectorId, includeAssigned, pageable);
        } else {
            devicePage = deviceService.findAll(pageable);
        }
        
        PageMeta pageMeta = new PageMeta(
                devicePage.getTotalElements(),
                devicePage.getNumber(),
                devicePage.getSize()
        );
        
        // 确保返回空列表而不是null
        List<Device> devices = devicePage.getContent();
        return ApiResponse.success(devices != null ? devices : List.of(), "获取设备列表成功", pageMeta);
    }

    /**
     * 条件查询设备
     */
    @GetMapping("/search")
    @ApiOperation("条件查询设备")
    public ApiResponse<List<Device>> searchDevices(
            @ApiParam("设备名称") @RequestParam(required = false) String name,
            @ApiParam("设备类型") @RequestParam(required = false) String type,
            @ApiParam("设备状态") @RequestParam(required = false) Integer status,
            @ApiParam("分组ID") @RequestParam(required = false) Long groupId,
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页记录数") @RequestParam(defaultValue = "10") int size,
            @ApiParam("排序字段") @RequestParam(defaultValue = "id") String sort,
            @ApiParam("排序方向") @RequestParam(defaultValue = "desc") String direction) {
        
        Sort.Direction dir = "asc".equalsIgnoreCase(direction) ? Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(dir, sort));
        
        Page<Device> devicePage = deviceService.findByConditions(name, type, status, groupId, pageable);
        
        PageMeta pageMeta = new PageMeta(
                devicePage.getTotalElements(),
                devicePage.getNumber(),
                devicePage.getSize()
        );
        
        return ApiResponse.success(devicePage.getContent(), "获取设备列表成功", pageMeta);
    }

    /**
     * 根据分组ID查询设备
     */
    @GetMapping("/group/{groupId}")
    @ApiOperation("根据分组ID查询设备")
    public ApiResponse<List<Device>> getDevicesByGroupId(
            @ApiParam("分组ID") @PathVariable Long groupId,
            @ApiParam("页码") @RequestParam(defaultValue = "0") int page,
            @ApiParam("每页记录数") @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Device> devicePage = deviceService.findByGroupId(groupId, pageable);
        
        PageMeta pageMeta = new PageMeta(
                devicePage.getTotalElements(),
                devicePage.getNumber(),
                devicePage.getSize()
        );
        
        return ApiResponse.success(devicePage.getContent(), "获取设备列表成功", pageMeta);
    }

    /**
     * 统计设备状态
     */
    @GetMapping("/stats/status")
    @ApiOperation("统计设备状态")
    public ApiResponse<Map<Integer, Long>> countByStatus() {
        Map<Integer, Long> statusCounts = deviceService.countByStatus();
        return ApiResponse.success(statusCounts, "获取设备状态统计成功");
    }

    /**
     * 更新设备状态
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("更新设备状态")
    public ApiResponse<Device> updateStatus(
            @ApiParam("设备ID") @PathVariable Long id,
            @ApiParam("设备状态") @RequestParam Integer status) {
        
        Device device = deviceService.updateStatus(id, status);
        return ApiResponse.success(device, "设备状态更新成功");
    }

    /**
     * 批量更新设备状态
     */
    @PutMapping("/batch/status")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("批量更新设备状态")
    public ApiResponse<Void> batchUpdateStatus(
            @ApiParam("设备ID列表") @RequestBody List<Long> ids,
            @ApiParam("设备状态") @RequestParam Integer status) {
        
        deviceService.batchUpdateStatus(ids, status);
        return ApiResponse.success(null, "设备状态批量更新成功");
    }

    /**
     * 检查设备连接
     */
    @GetMapping("/{id}/check-connection")
    @PreAuthorize("hasRole('ADMIN') or hasRole('OPERATOR')")
    @ApiOperation("检查设备连接")
    public ApiResponse<Boolean> checkConnection(@ApiParam("设备ID") @PathVariable Long id) {
        boolean connected = deviceService.checkConnection(id);
        return ApiResponse.success(connected, connected ? "设备连接成功" : "设备连接失败");
    }
} 