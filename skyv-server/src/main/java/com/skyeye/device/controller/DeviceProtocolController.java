package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceProtocolDto;
import com.skyeye.device.service.DeviceProtocolService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备协议管理控制器
 */
@RestController
@RequestMapping("/api/device-protocols")
@RequiredArgsConstructor
@Slf4j
public class DeviceProtocolController {

    private final DeviceProtocolService deviceProtocolService;

    @GetMapping
    public ApiResponse<Page<DeviceProtocolDto>> getDeviceProtocols(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String version,
            @RequestParam(required = false) Boolean isEnabled,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "usageCount") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        Sort sort = "desc".equalsIgnoreCase(order) ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<DeviceProtocolDto> result = deviceProtocolService.getDeviceProtocols(name, code, version, isEnabled, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/enabled")
    public ApiResponse<List<DeviceProtocolDto>> getAllEnabledProtocols() {
        List<DeviceProtocolDto> result = deviceProtocolService.getAllEnabledProtocols();
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<DeviceProtocolDto> getDeviceProtocolById(@PathVariable Long id) {
        DeviceProtocolDto result = deviceProtocolService.getDeviceProtocolById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<DeviceProtocolDto> createDeviceProtocol(@Valid @RequestBody DeviceProtocolDto deviceProtocolDto) {
        DeviceProtocolDto result = deviceProtocolService.createDeviceProtocol(deviceProtocolDto);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceProtocolDto> updateDeviceProtocol(
            @PathVariable Long id,
            @Valid @RequestBody DeviceProtocolDto deviceProtocolDto) {
        DeviceProtocolDto result = deviceProtocolService.updateDeviceProtocol(id, deviceProtocolDto);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeviceProtocol(@PathVariable Long id) {
        deviceProtocolService.deleteDeviceProtocol(id);
        return ApiResponse.success();
    }

    @GetMapping("/validate/code")
    public ApiResponse<Map<String, Boolean>> validateCode(
            @RequestParam String code,
            @RequestParam(required = false) Long id) {
        boolean isUnique = deviceProtocolService.isCodeUnique(code, id);
        return ApiResponse.success(Map.of("isUnique", isUnique));
    }

    @GetMapping("/validate/name")
    public ApiResponse<Map<String, Boolean>> validateName(
            @RequestParam String name,
            @RequestParam(required = false) Long id) {
        boolean isUnique = deviceProtocolService.isNameUnique(name, id);
        return ApiResponse.success(Map.of("isUnique", isUnique));
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<DeviceProtocolDto> getProtocolStats(@PathVariable Long id) {
        DeviceProtocolDto result = deviceProtocolService.getProtocolStats(id);
        return ApiResponse.success(result);
    }
} 