package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceTagDto;
import com.skyeye.device.service.DeviceTagService;
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
 * 设备标签管理控制器
 */
@RestController
@RequestMapping("/api/device-tags")
@RequiredArgsConstructor
@Slf4j
public class DeviceTagController {

    private final DeviceTagService deviceTagService;

    @GetMapping
    public ApiResponse<Page<DeviceTagDto>> getDeviceTags(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "usageCount") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        Sort sort = "desc".equalsIgnoreCase(order) ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<DeviceTagDto> result = deviceTagService.getDeviceTags(name, description, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/all")
    public ApiResponse<List<DeviceTagDto>> getAllDeviceTags() {
        List<DeviceTagDto> result = deviceTagService.getAllDeviceTags();
        return ApiResponse.success(result);
    }

    @GetMapping("/popular")
    public ApiResponse<List<DeviceTagDto>> getPopularTags() {
        List<DeviceTagDto> result = deviceTagService.getPopularTags();
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<DeviceTagDto> getDeviceTagById(@PathVariable Long id) {
        DeviceTagDto result = deviceTagService.getDeviceTagById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<DeviceTagDto> createDeviceTag(@Valid @RequestBody DeviceTagDto deviceTagDto) {
        DeviceTagDto result = deviceTagService.createDeviceTag(deviceTagDto);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceTagDto> updateDeviceTag(
            @PathVariable Long id,
            @Valid @RequestBody DeviceTagDto deviceTagDto) {
        DeviceTagDto result = deviceTagService.updateDeviceTag(id, deviceTagDto);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeviceTag(@PathVariable Long id) {
        deviceTagService.deleteDeviceTag(id);
        return ApiResponse.success();
    }

    @GetMapping("/validate/name")
    public ApiResponse<Map<String, Boolean>> validateName(
            @RequestParam String name,
            @RequestParam(required = false) Long id) {
        boolean isUnique = deviceTagService.isNameUnique(name, id);
        return ApiResponse.success(Map.of("isUnique", isUnique));
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<DeviceTagDto> getTagStats(@PathVariable Long id) {
        DeviceTagDto result = deviceTagService.getTagStats(id);
        return ApiResponse.success(result);
    }
} 