package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceTemplateDto;
import com.skyeye.device.service.DeviceTemplateService;
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
 * 设备模板管理控制器
 */
@RestController
@RequestMapping("/api/device-templates")
@RequiredArgsConstructor
@Slf4j
public class DeviceTemplateController {

    private final DeviceTemplateService deviceTemplateService;

    @GetMapping
    public ApiResponse<Page<DeviceTemplateDto>> getDeviceTemplates(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            @RequestParam(required = false) Long deviceTypeId,
            @RequestParam(required = false) Long protocolId,
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) Boolean isEnabled,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "usageCount") String sortBy,
            @RequestParam(defaultValue = "desc") String order) {

        Sort sort = "desc".equalsIgnoreCase(order) ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page - 1, limit, sort);

        Page<DeviceTemplateDto> result = deviceTemplateService.getDeviceTemplates(
                name, code, deviceTypeId, protocolId, manufacturer, isEnabled, pageable);
        return ApiResponse.success(result);
    }

    @GetMapping("/enabled")
    public ApiResponse<List<DeviceTemplateDto>> getAllEnabledTemplates() {
        List<DeviceTemplateDto> result = deviceTemplateService.getAllEnabledTemplates();
        return ApiResponse.success(result);
    }

    @GetMapping("/by-device-type/{deviceTypeId}")
    public ApiResponse<List<DeviceTemplateDto>> getTemplatesByDeviceType(@PathVariable Long deviceTypeId) {
        List<DeviceTemplateDto> result = deviceTemplateService.getTemplatesByDeviceType(deviceTypeId);
        return ApiResponse.success(result);
    }

    @GetMapping("/by-protocol/{protocolId}")
    public ApiResponse<List<DeviceTemplateDto>> getTemplatesByProtocol(@PathVariable Long protocolId) {
        List<DeviceTemplateDto> result = deviceTemplateService.getTemplatesByProtocol(protocolId);
        return ApiResponse.success(result);
    }

    @GetMapping("/{id}")
    public ApiResponse<DeviceTemplateDto> getDeviceTemplateById(@PathVariable Long id) {
        DeviceTemplateDto result = deviceTemplateService.getDeviceTemplateById(id);
        return ApiResponse.success(result);
    }

    @PostMapping
    public ApiResponse<DeviceTemplateDto> createDeviceTemplate(@Valid @RequestBody DeviceTemplateDto deviceTemplateDto) {
        DeviceTemplateDto result = deviceTemplateService.createDeviceTemplate(deviceTemplateDto);
        return ApiResponse.success(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceTemplateDto> updateDeviceTemplate(
            @PathVariable Long id,
            @Valid @RequestBody DeviceTemplateDto deviceTemplateDto) {
        DeviceTemplateDto result = deviceTemplateService.updateDeviceTemplate(id, deviceTemplateDto);
        return ApiResponse.success(result);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteDeviceTemplate(@PathVariable Long id) {
        deviceTemplateService.deleteDeviceTemplate(id);
        return ApiResponse.success();
    }

    @GetMapping("/validate/code")
    public ApiResponse<Map<String, Boolean>> validateCode(
            @RequestParam String code,
            @RequestParam(required = false) Long id) {
        boolean isUnique = deviceTemplateService.isCodeUnique(code, id);
        return ApiResponse.success(Map.of("isUnique", isUnique));
    }

    @GetMapping("/validate/name")
    public ApiResponse<Map<String, Boolean>> validateName(
            @RequestParam String name,
            @RequestParam(required = false) Long id) {
        boolean isUnique = deviceTemplateService.isNameUnique(name, id);
        return ApiResponse.success(Map.of("isUnique", isUnique));
    }

    @GetMapping("/{id}/stats")
    public ApiResponse<DeviceTemplateDto> getTemplateStats(@PathVariable Long id) {
        DeviceTemplateDto result = deviceTemplateService.getTemplateStats(id);
        return ApiResponse.success(result);
    }
} 