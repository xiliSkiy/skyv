package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.CredentialRequest;
import com.skyeye.device.dto.DeviceCredentialDto;
import com.skyeye.device.entity.DeviceCredential;
import com.skyeye.device.service.DeviceCredentialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 设备凭据管理控制器
 * 
 * @author SkyEye Team
 */
@RestController
@RequestMapping("/api/devices/{deviceId}/credentials")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class DeviceCredentialController {

    private final DeviceCredentialService credentialService;

    /**
     * 创建设备凭据
     */
    @PostMapping
    @PreAuthorize("hasPermission('device', 'create') or hasPermission('credential', 'create')")
    public ApiResponse<DeviceCredential> createCredential(
            @PathVariable Long deviceId,
            @Valid @RequestBody CredentialRequest request) {
        
        DeviceCredential credential = credentialService.saveCredential(deviceId, request);
        return ApiResponse.success(credential);
    }

    /**
     * 获取设备凭据列表
     */
    @GetMapping
    @PreAuthorize("hasPermission('device', 'view') or hasPermission('credential', 'view')")
    public ApiResponse<List<DeviceCredentialDto>> getDeviceCredentials(@PathVariable Long deviceId) {
        List<DeviceCredentialDto> credentials = credentialService.getDeviceCredentials(deviceId);
        return ApiResponse.success(credentials);
    }

    /**
     * 分页查询设备凭据
     */
    @GetMapping("/page")
    @PreAuthorize("hasPermission('device', 'view') or hasPermission('credential', 'view')")
    public ApiResponse<Page<DeviceCredentialDto>> getDeviceCredentialsPage(
            @PathVariable Long deviceId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "updatedAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort.Direction direction = "asc".equalsIgnoreCase(sortDir) ? 
                Sort.Direction.ASC : Sort.Direction.DESC;
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(direction, sortBy));
        
        Page<DeviceCredentialDto> credentialPage = credentialService.getDeviceCredentials(deviceId, pageable);
        return ApiResponse.success(credentialPage);
    }

    /**
     * 获取设备默认凭据数据
     */
    @GetMapping("/default")
    @PreAuthorize("hasPermission('device', 'view') or hasPermission('credential', 'view')")
    public ApiResponse<Map<String, Object>> getDefaultCredential(@PathVariable Long deviceId) {
        Map<String, Object> credentialData = credentialService.getDefaultCredential(deviceId);
        return ApiResponse.success(credentialData);
    }

    /**
     * 获取设备指定协议的默认凭据数据
     */
    @GetMapping("/default/{protocol}")
    @PreAuthorize("hasPermission('device', 'view') or hasPermission('credential', 'view')")
    public ApiResponse<Map<String, Object>> getDefaultCredentialByProtocol(
            @PathVariable Long deviceId,
            @PathVariable String protocol) {
        
        Map<String, Object> credentialData = credentialService.getDefaultCredential(deviceId, protocol);
        return ApiResponse.success(credentialData);
    }
}

/**
 * 单个凭据管理控制器
 */
@RestController
@RequestMapping("/api/credentials")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
class CredentialController {

    private final DeviceCredentialService credentialService;

    /**
     * 获取凭据基本信息
     */
    @GetMapping("/{credentialId}")
    @PreAuthorize("hasPermission('credential', 'view')")
    public ApiResponse<DeviceCredentialDto> getCredentialInfo(@PathVariable Long credentialId) {
        DeviceCredentialDto credential = credentialService.getCredentialInfo(credentialId);
        return ApiResponse.success(credential);
    }

    /**
     * 获取凭据详细信息（包含解密数据）
     */
    @GetMapping("/{credentialId}/data")
    @PreAuthorize("hasPermission('credential', 'view')")
    public ApiResponse<DeviceCredentialDto> getCredentialWithData(@PathVariable Long credentialId) {
        DeviceCredentialDto credential = credentialService.getCredentialWithData(credentialId);
        return ApiResponse.success(credential);
    }

    /**
     * 获取凭据数据（仅返回解密后的凭据数据）
     */
    @GetMapping("/{credentialId}/decrypt")
    @PreAuthorize("hasPermission('credential', 'decrypt')")
    public ApiResponse<Map<String, Object>> getCredentialData(@PathVariable Long credentialId) {
        Map<String, Object> credentialData = credentialService.getCredentialData(credentialId);
        return ApiResponse.success(credentialData);
    }

    /**
     * 更新凭据
     */
    @PutMapping("/{credentialId}")
    @PreAuthorize("hasPermission('credential', 'update')")
    public ApiResponse<DeviceCredential> updateCredential(
            @PathVariable Long credentialId,
            @Valid @RequestBody CredentialRequest request) {
        
        DeviceCredential credential = credentialService.updateCredential(credentialId, request);
        return ApiResponse.success(credential);
    }

    /**
     * 删除凭据
     */
    @DeleteMapping("/{credentialId}")
    @PreAuthorize("hasPermission('credential', 'delete')")
    public ApiResponse<Void> deleteCredential(@PathVariable Long credentialId) {
        credentialService.deleteCredential(credentialId);
        return ApiResponse.success(null);
    }

    /**
     * 设置为默认凭据
     */
    @PutMapping("/{credentialId}/default")
    @PreAuthorize("hasPermission('credential', 'update')")
    public ApiResponse<Void> setDefaultCredential(@PathVariable Long credentialId) {
        credentialService.setDefaultCredential(credentialId);
        return ApiResponse.success(null);
    }

    /**
     * 启用/禁用凭据
     */
    @PutMapping("/{credentialId}/status")
    @PreAuthorize("hasPermission('credential', 'update')")
    public ApiResponse<Void> setCredentialEnabled(
            @PathVariable Long credentialId,
            @RequestParam boolean enabled) {
        
        credentialService.setCredentialEnabled(credentialId, enabled);
        return ApiResponse.success(null);
    }

    /**
     * 测试凭据连接
     */
    @PostMapping("/{credentialId}/test")
    @PreAuthorize("hasPermission('credential', 'test')")
    public ApiResponse<Boolean> testCredential(@PathVariable Long credentialId) {
        boolean result = credentialService.testCredential(credentialId);
        return ApiResponse.success(result);
    }

    /**
     * 复制凭据到其他设备
     */
    @PostMapping("/{credentialId}/copy")
    @PreAuthorize("hasPermission('credential', 'create')")
    public ApiResponse<Integer> copyCredentialToDevices(
            @PathVariable Long credentialId,
            @RequestBody List<Long> targetDeviceIds) {
        
        int copiedCount = credentialService.copyCredentialToDevices(credentialId, targetDeviceIds);
        return ApiResponse.success(copiedCount);
    }
}
