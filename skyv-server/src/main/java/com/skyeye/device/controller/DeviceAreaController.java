package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceAreaDto;
import com.skyeye.device.service.DeviceAreaService;

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
 * 设备区域管理控制器
 */
@RestController
@RequestMapping("/api/device-areas")
@RequiredArgsConstructor
@Slf4j
public class DeviceAreaController {

    private final DeviceAreaService deviceAreaService;

    @GetMapping
    public ApiResponse<Page<DeviceAreaDto>> getDeviceAreas(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long parentId,
            @RequestParam(required = false) Integer level,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "sortOrder") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {

        Sort sort = "desc".equalsIgnoreCase(order) ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<DeviceAreaDto> result = deviceAreaService.getDeviceAreas(name, code, parentId, level, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/tree")
    public ApiResponse<List<DeviceAreaDto>> getDeviceAreaTree() {
        List<DeviceAreaDto> result = deviceAreaService.getDeviceAreaTree();
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<DeviceAreaDto> getDeviceAreaById(@PathVariable Long id) {
        DeviceAreaDto result = deviceAreaService.getDeviceAreaById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<DeviceAreaDto> createDeviceArea(@Valid @RequestBody DeviceAreaDto deviceAreaDto) {
        DeviceAreaDto result = deviceAreaService.createDeviceArea(deviceAreaDto);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceAreaDto> updateDeviceArea(
            @PathVariable Long id,
            @Valid @RequestBody DeviceAreaDto deviceAreaDto) {
        DeviceAreaDto result = deviceAreaService.updateDeviceArea(id, deviceAreaDto);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeviceArea(@PathVariable Long id) {
        deviceAreaService.deleteDeviceArea(id);
        return ApiResponse.success();
    }

    @GetMapping("/validate/name")
    public ApiResponse<Map<String, Boolean>> validateName(
            @RequestParam String name,
            @RequestParam(required = false) Long id) {
        boolean isUnique = deviceAreaService.isNameUnique(name, id);
        return ApiResponse.success(Map.of("isUnique", isUnique));
    }

    @GetMapping("/validate/code")
    public ApiResponse<Map<String, Boolean>> validateCode(
            @RequestParam String code,
            @RequestParam(required = false) Long id) {
        boolean isUnique = deviceAreaService.isCodeUnique(code, id);
        return ApiResponse.success(Map.of("isUnique", isUnique));
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<DeviceAreaDto> getAreaStats(@PathVariable Long id) {
        DeviceAreaDto result = deviceAreaService.getAreaStats(id);
        return ApiResponse.success(result);
    }
} 