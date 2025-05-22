package com.skyeye.collector.controller;

import com.skyeye.collector.dto.BatchAssociateDevicesRequest;
import com.skyeye.collector.dto.CollectorDeviceRelationDTO;
import com.skyeye.collector.service.CollectorDeviceService;
import com.skyeye.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The controller for the association between collectors and devices.
 */
@Slf4j
@RestController
@RequestMapping("/collector-devices")
public class CollectorDeviceController {

    @Autowired
    private CollectorDeviceService collectorDeviceService;
    
    /**
     * 关联设备到采集器
     * @param relationDTO 关联信息
     * @return 创建的关联关系
     */
    @PostMapping
    public ApiResponse<CollectorDeviceRelationDTO> associateDevice(@Valid @RequestBody CollectorDeviceRelationDTO relationDTO) {
        return ApiResponse.success(collectorDeviceService.associateDevice(relationDTO));
    }
    
    /**
     * 批量关联设备到采集器
     * @param request 批量关联请求
     * @return 成功关联的设备数量
     */
    @PostMapping("/batch")
    public ApiResponse<Integer> batchAssociateDevices(@Valid @RequestBody BatchAssociateDevicesRequest request) {
        int count = collectorDeviceService.batchAssociateDevices(request);
        return ApiResponse.success(count);
    }
    
    /**
     * 取消设备与采集器的关联
     * @param id 关联关系ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> disassociateDevice(@PathVariable Long id) {
        collectorDeviceService.disassociateDevice(id);
        return ApiResponse.success();
    }
    
    /**
     * 取消设备与采集器的关联
     * @param collectorId 采集器ID
     * @param deviceId 设备ID
     * @return 操作结果
     */
    @DeleteMapping("/collector/{collectorId}/device/{deviceId}")
    public ApiResponse<Void> disassociateDevice(@PathVariable Long collectorId, @PathVariable Long deviceId) {
        collectorDeviceService.disassociateDevice(collectorId, deviceId);
        return ApiResponse.success();
    }
    
    /**
     * 获取采集器关联的设备列表
     * @param collectorId 采集器ID
     * @param page 页码
     * @param size 每页大小
     * @param deviceType 设备类型
     * @return 设备列表
     */
    @GetMapping("/collector/{collectorId}/devices")
    public ApiResponse<Page<CollectorDeviceRelationDTO>> getDevicesByCollectorId(
            @PathVariable Long collectorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String deviceType) {
        
        Map<String, Object> params = new HashMap<>();
        if (deviceType != null) params.put("deviceType", deviceType);
        
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CollectorDeviceRelationDTO> devices = collectorDeviceService.getDevicesByCollectorId(collectorId, params, pageable);
        
        return ApiResponse.success(devices);
    }
    
    /**
     * 获取设备关联的采集器列表
     * @param deviceId 设备ID
     * @return 采集器列表
     */
    @GetMapping("/device/{deviceId}/collectors")
    public ApiResponse<List<CollectorDeviceRelationDTO>> getCollectorsByDeviceId(@PathVariable Long deviceId) {
        return ApiResponse.success(collectorDeviceService.getCollectorsByDeviceId(deviceId));
    }
    
    /**
     * 获取采集器关联的设备数量
     * @param collectorId 采集器ID
     * @return 设备数量
     */
    @GetMapping("/collector/{collectorId}/device-count")
    public ApiResponse<Long> countDevicesByCollectorId(@PathVariable Long collectorId) {
        return ApiResponse.success(collectorDeviceService.countDevicesByCollectorId(collectorId));
    }
    
    /**
     * 获取未关联采集器的设备列表
     * @param page 页码
     * @param size 每页大小
     * @param deviceName 设备名称
     * @param deviceType 设备类型
     * @param status 状态
     * @return 未关联采集器的设备列表
     */
    @GetMapping("/unassociated-devices")
    public ApiResponse<Page<Map<String, Object>>> getUnassociatedDevices(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String deviceName,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Integer status) {
        
        Map<String, Object> params = new HashMap<>();
        if (deviceName != null) params.put("deviceName", deviceName);
        if (deviceType != null) params.put("deviceType", deviceType);
        if (status != null) params.put("status", status);
        
        Pageable pageable = PageRequest.of(page, size);
        Page<Map<String, Object>> devices = collectorDeviceService.getUnassociatedDevices(params, pageable);
        
        return ApiResponse.success(devices);
    }
} 