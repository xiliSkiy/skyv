package com.skyeye.device.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.EncryptionKeyDto;
import com.skyeye.device.entity.EncryptionKey;
import com.skyeye.device.service.CredentialEncryptionService;
import com.skyeye.device.service.DeviceCredentialService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;

/**
 * 加密密钥管理控制器
 * 
 * @author SkyEye Team
 */
@RestController
@RequestMapping("/api/encryption-keys")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class EncryptionKeyController {

    private final CredentialEncryptionService encryptionService;
    private final DeviceCredentialService credentialService;

    /**
     * 获取当前活跃的加密密钥
     */
    @GetMapping("/current")
    @PreAuthorize("hasPermission('encryption_key', 'view')")
    public ApiResponse<EncryptionKeyDto> getCurrentActiveKey() {
        EncryptionKey activeKey = encryptionService.getCurrentActiveKey();
        EncryptionKeyDto dto = convertToDto(activeKey);
        return ApiResponse.success(dto);
    }

    /**
     * 创建新的加密密钥
     */
    @PostMapping
    @PreAuthorize("hasPermission('encryption_key', 'create')")
    public ApiResponse<EncryptionKeyDto> createNewKey(@RequestParam String keyName) {
        EncryptionKey newKey = encryptionService.createNewKey(keyName);
        EncryptionKeyDto dto = convertToDto(newKey);
        return ApiResponse.success(dto);
    }

    /**
     * 轮换加密密钥
     */
    @PostMapping("/{keyId}/rotate")
    @PreAuthorize("hasPermission('encryption_key', 'rotate')")
    public ApiResponse<RotateKeyResult> rotateKey(@PathVariable Long keyId) {
        // 创建新密钥
        EncryptionKey newKey = encryptionService.rotateKey(keyId);
        
        // 更新使用旧密钥的凭据
        int rotatedCount = credentialService.rotateEncryptionKey(keyId, newKey.getId());
        
        RotateKeyResult result = RotateKeyResult.builder()
                .newKey(convertToDto(newKey))
                .rotatedCredentialCount(rotatedCount)
                .build();
        
        return ApiResponse.success(result);
    }

    /**
     * 验证密钥是否有效
     */
    @GetMapping("/{keyId}/validate")
    @PreAuthorize("hasPermission('encryption_key', 'view')")
    public ApiResponse<Boolean> validateKey(@PathVariable Long keyId) {
        boolean isValid = encryptionService.isKeyValid(keyId);
        return ApiResponse.success(isValid);
    }

    /**
     * 转换为DTO对象
     */
    private EncryptionKeyDto convertToDto(EncryptionKey key) {
        EncryptionKeyDto dto = new EncryptionKeyDto();
        dto.setId(key.getId());
        dto.setKeyName(key.getKeyName());
        dto.setKeyVersion(key.getKeyVersion());
        dto.setAlgorithm(key.getAlgorithm());
        dto.setKeyHash(key.getKeyHash());
        dto.setStatus(key.getStatus());
        dto.setIsActive(key.getIsActive());
        dto.setEffectiveTime(key.getEffectiveTime());
        dto.setExpireTime(key.getExpireTime());
        dto.setRemark(key.getRemark());
        dto.setCreatedAt(key.getCreatedAt() != null ? Timestamp.valueOf(key.getCreatedAt()) : null);
        dto.setUpdatedAt(key.getUpdatedAt() != null ? Timestamp.valueOf(key.getUpdatedAt()) : null);
        dto.setCreatedBy(key.getCreatedBy() != null ? key.getCreatedBy().toString() : null);
        dto.setUpdatedBy(key.getUpdatedBy() != null ? key.getUpdatedBy().toString() : null);
        return dto;
    }

    /**
     * 密钥轮换结果DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class RotateKeyResult {
        private EncryptionKeyDto newKey;
        private Integer rotatedCredentialCount;
    }
}
