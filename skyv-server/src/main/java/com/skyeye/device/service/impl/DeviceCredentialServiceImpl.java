package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.util.JsonUtils;
import com.skyeye.device.dto.CredentialRequest;
import com.skyeye.device.dto.DeviceCredentialDto;
import com.skyeye.device.dto.EncryptedCredential;
import com.skyeye.device.entity.Device;
import com.skyeye.device.entity.DeviceCredential;
import com.skyeye.device.entity.EncryptionKey;
import com.skyeye.device.repository.DeviceCredentialRepository;
import com.skyeye.device.repository.DeviceRepository;
import com.skyeye.device.service.CredentialEncryptionService;
import com.skyeye.device.service.DeviceCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备凭据管理服务实现类
 * 
 * @author SkyEye Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceCredentialServiceImpl implements DeviceCredentialService {

    private final DeviceCredentialRepository credentialRepository;
    private final DeviceRepository deviceRepository;
    private final CredentialEncryptionService encryptionService;

    @Override
    @Transactional
    public DeviceCredential saveCredential(Long deviceId, CredentialRequest request) {
        // 验证设备是否存在
        deviceRepository.findById(deviceId)
                .orElseThrow(() -> new BusinessException("设备不存在"));

        // 检查凭据名称是否重复
        if (credentialRepository.existsByDeviceIdAndCredentialName(deviceId, request.getCredentialName())) {
            throw new BusinessException("凭据名称已存在");
        }

        // 如果设置为默认凭据，需要取消其他默认凭据
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultCredential(deviceId, request.getProtocolType());
        }

        // 获取当前活跃的加密密钥
        EncryptionKey activeKey = encryptionService.getCurrentActiveKey();

        // 加密凭据数据
        EncryptedCredential encryptedCredential = encryptionService.encrypt(
                request.getCredentialData(), activeKey.getId());

        // 创建凭据实体
        DeviceCredential credential = new DeviceCredential();
        credential.setDeviceId(deviceId);
        credential.setCredentialName(request.getCredentialName());
        credential.setCredentialType(request.getCredentialType());
        credential.setProtocolType(request.getProtocolType());
        credential.setEncryptedData(encryptedCredential.getEncryptedData());
        credential.setEncryptionIv(encryptedCredential.getIv());
        credential.setEncryptionKeyId(encryptedCredential.getKeyId());
        credential.setIsDefault(request.getIsDefault());
        credential.setStatus(1);
        credential.setRemark(request.getRemark());

        DeviceCredential savedCredential = credentialRepository.save(credential);

        log.info("保存设备凭据成功: deviceId={}, credentialName={}", deviceId, request.getCredentialName());
        return savedCredential;
    }

    @Override
    @Transactional
    public DeviceCredential updateCredential(Long credentialId, CredentialRequest request) {
        DeviceCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("凭据不存在"));

        // 检查凭据名称是否重复（排除当前凭据）
        if (credentialRepository.existsByDeviceIdAndCredentialNameAndIdNot(
                credential.getDeviceId(), request.getCredentialName(), credentialId)) {
            throw new BusinessException("凭据名称已存在");
        }

        // 如果设置为默认凭据，需要取消其他默认凭据
        if (Boolean.TRUE.equals(request.getIsDefault())) {
            clearDefaultCredential(credential.getDeviceId(), request.getProtocolType());
        }

        // 如果凭据数据有变化，重新加密
        if (request.getCredentialData() != null && !request.getCredentialData().isEmpty()) {
            EncryptionKey activeKey = encryptionService.getCurrentActiveKey();
            EncryptedCredential encryptedCredential = encryptionService.encrypt(
                    request.getCredentialData(), activeKey.getId());

            credential.setEncryptedData(encryptedCredential.getEncryptedData());
            credential.setEncryptionIv(encryptedCredential.getIv());
            credential.setEncryptionKeyId(encryptedCredential.getKeyId());
        }

        // 更新其他字段
        credential.setCredentialName(request.getCredentialName());
        credential.setCredentialType(request.getCredentialType());
        credential.setProtocolType(request.getProtocolType());
        credential.setIsDefault(request.getIsDefault());
        credential.setRemark(request.getRemark());

        DeviceCredential updatedCredential = credentialRepository.save(credential);

        log.info("更新设备凭据成功: credentialId={}, credentialName={}", 
                credentialId, request.getCredentialName());
        return updatedCredential;
    }

    @Override
    @Transactional
    public void deleteCredential(Long credentialId) {
        DeviceCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("凭据不存在"));

        credentialRepository.delete(credential);

        log.info("删除设备凭据成功: credentialId={}, deviceId={}", 
                credentialId, credential.getDeviceId());
    }

    @Override
    public DeviceCredentialDto getCredentialWithData(Long credentialId) {
        DeviceCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("凭据不存在"));

        DeviceCredentialDto dto = convertToDto(credential);
        
        // 解密凭据数据
        try {
            EncryptedCredential encryptedCredential = EncryptedCredential.builder()
                    .encryptedData(credential.getEncryptedData())
                    .iv(credential.getEncryptionIv())
                    .keyId(credential.getEncryptionKeyId())
                    .build();

            Map<String, Object> credentialData = encryptionService.decrypt(encryptedCredential);
            dto.setCredentialData(credentialData);
        } catch (Exception e) {
            log.error("解密凭据数据失败: credentialId={}", credentialId, e);
            throw new BusinessException("解密凭据数据失败");
        }

        return dto;
    }

    @Override
    public DeviceCredentialDto getCredentialInfo(Long credentialId) {
        DeviceCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("凭据不存在"));

        return convertToDto(credential);
    }

    @Override
    public List<DeviceCredentialDto> getDeviceCredentials(Long deviceId) {
        List<DeviceCredential> credentials = credentialRepository.findByDeviceIdAndStatus(deviceId, 1);
        return credentials.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<DeviceCredentialDto> getDeviceCredentials(Long deviceId, Pageable pageable) {
        Page<DeviceCredential> credentialPage = credentialRepository.findByDeviceId(deviceId, pageable);
        return credentialPage.map(this::convertToDto);
    }

    @Override
    public Map<String, Object> getDefaultCredential(Long deviceId) {
        DeviceCredential credential = credentialRepository.findByDeviceIdAndIsDefault(deviceId, true)
                .orElseThrow(() -> new BusinessException("设备未配置默认凭据"));

        return getCredentialData(credential.getId());
    }

    @Override
    public Map<String, Object> getDefaultCredential(Long deviceId, String protocol) {
        DeviceCredential credential = credentialRepository
                .findByDeviceIdAndProtocolTypeAndIsDefault(deviceId, protocol, true)
                .orElseThrow(() -> new BusinessException("设备未配置该协议的默认凭据"));

        return getCredentialData(credential.getId());
    }

    @Override
    public Map<String, Object> getCredentialData(Long credentialId) {
        DeviceCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("凭据不存在"));

        EncryptedCredential encryptedCredential = EncryptedCredential.builder()
                .encryptedData(credential.getEncryptedData())
                .iv(credential.getEncryptionIv())
                .keyId(credential.getEncryptionKeyId())
                .build();

        return encryptionService.decrypt(encryptedCredential);
    }

    @Override
    @Transactional
    public void setDefaultCredential(Long credentialId) {
        DeviceCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("凭据不存在"));

        // 取消同设备同协议的其他默认凭据
        clearDefaultCredential(credential.getDeviceId(), credential.getProtocolType());

        // 设置为默认凭据
        credential.setIsDefault(true);
        credentialRepository.save(credential);

        log.info("设置默认凭据成功: credentialId={}, deviceId={}", 
                credentialId, credential.getDeviceId());
    }

    @Override
    @Transactional
    public void setCredentialEnabled(Long credentialId, boolean enabled) {
        DeviceCredential credential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("凭据不存在"));

        credential.setStatus(enabled ? 1 : 0);
        credentialRepository.save(credential);

        log.info("设置凭据状态成功: credentialId={}, enabled={}", credentialId, enabled);
    }

    @Override
    public boolean testCredential(Long credentialId) {
        try {
            // 获取凭据数据
            Map<String, Object> credentialData = getCredentialData(credentialId);
            
            // 这里应该根据凭据类型和协议实现具体的连接测试逻辑
            // 当前只是简单验证凭据数据是否完整
            return credentialData != null && !credentialData.isEmpty();
            
        } catch (Exception e) {
            log.error("测试凭据连接失败: credentialId={}", credentialId, e);
            return false;
        }
    }

    @Override
    @Transactional
    public int rotateEncryptionKey(Long oldKeyId, Long newKeyId) {
        List<DeviceCredential> credentials = credentialRepository.findCredentialsNeedKeyRotation(oldKeyId);
        int rotatedCount = 0;

        for (DeviceCredential credential : credentials) {
            try {
                // 解密原数据
                EncryptedCredential oldEncrypted = EncryptedCredential.builder()
                        .encryptedData(credential.getEncryptedData())
                        .iv(credential.getEncryptionIv())
                        .keyId(oldKeyId)
                        .build();

                Map<String, Object> plainData = encryptionService.decrypt(oldEncrypted);

                // 使用新密钥重新加密
                EncryptedCredential newEncrypted = encryptionService.encrypt(plainData, newKeyId);

                // 更新凭据
                credential.setEncryptedData(newEncrypted.getEncryptedData());
                credential.setEncryptionIv(newEncrypted.getIv());
                credential.setEncryptionKeyId(newKeyId);

                credentialRepository.save(credential);
                rotatedCount++;

            } catch (Exception e) {
                log.error("轮换凭据密钥失败: credentialId={}", credential.getId(), e);
            }
        }

        log.info("密钥轮换完成: oldKeyId={}, newKeyId={}, rotatedCount={}", 
                oldKeyId, newKeyId, rotatedCount);
        return rotatedCount;
    }

    @Override
    @Transactional
    public int copyCredentialToDevices(Long credentialId, List<Long> targetDeviceIds) {
        DeviceCredential sourceCredential = credentialRepository.findById(credentialId)
                .orElseThrow(() -> new BusinessException("源凭据不存在"));

        // 解密源凭据数据
        Map<String, Object> credentialData = getCredentialData(credentialId);

        int copiedCount = 0;
        for (Long targetDeviceId : targetDeviceIds) {
            try {
                // 检查目标设备是否存在
                if (!deviceRepository.existsById(targetDeviceId)) {
                    log.warn("目标设备不存在: deviceId={}", targetDeviceId);
                    continue;
                }

                // 检查是否已存在同名凭据
                if (credentialRepository.existsByDeviceIdAndCredentialName(
                        targetDeviceId, sourceCredential.getCredentialName())) {
                    log.warn("目标设备已存在同名凭据: deviceId={}, credentialName={}", 
                            targetDeviceId, sourceCredential.getCredentialName());
                    continue;
                }

                // 创建复制请求
                CredentialRequest request = new CredentialRequest();
                request.setCredentialName(sourceCredential.getCredentialName());
                request.setCredentialType(sourceCredential.getCredentialType());
                request.setProtocolType(sourceCredential.getProtocolType());
                request.setCredentialData(credentialData);
                request.setIsDefault(false); // 复制的凭据默认不设为默认
                request.setRemark("从设备ID " + sourceCredential.getDeviceId() + " 复制");

                // 保存到目标设备
                saveCredential(targetDeviceId, request);
                copiedCount++;

            } catch (Exception e) {
                log.error("复制凭据到设备失败: targetDeviceId={}", targetDeviceId, e);
            }
        }

        log.info("复制凭据完成: sourceCredentialId={}, copiedCount={}", credentialId, copiedCount);
        return copiedCount;
    }

    /**
     * 取消默认凭据设置
     */
    private void clearDefaultCredential(Long deviceId, String protocolType) {
        List<DeviceCredential> defaultCredentials;
        
        if (protocolType != null) {
            defaultCredentials = credentialRepository.findByDeviceIdAndProtocolType(deviceId, protocolType)
                    .stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsDefault()))
                    .collect(Collectors.toList());
        } else {
            defaultCredentials = credentialRepository.findByDeviceId(deviceId)
                    .stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsDefault()))
                    .collect(Collectors.toList());
        }

        for (DeviceCredential credential : defaultCredentials) {
            credential.setIsDefault(false);
            credentialRepository.save(credential);
        }
    }

    /**
     * 转换为DTO对象
     */
    private DeviceCredentialDto convertToDto(DeviceCredential credential) {
        DeviceCredentialDto dto = new DeviceCredentialDto();
        dto.setId(credential.getId());
        dto.setDeviceId(credential.getDeviceId());
        dto.setCredentialName(credential.getCredentialName());
        dto.setCredentialType(credential.getCredentialType());
        dto.setProtocolType(credential.getProtocolType());
        dto.setIsDefault(credential.getIsDefault());
        dto.setStatus(credential.getStatus());
        dto.setRemark(credential.getRemark());
        dto.setCreatedAt(credential.getCreatedAt() != null ? 
                java.sql.Timestamp.valueOf(credential.getCreatedAt()) : null);
        dto.setUpdatedAt(credential.getUpdatedAt() != null ? 
                java.sql.Timestamp.valueOf(credential.getUpdatedAt()) : null);
        dto.setCreatedBy(credential.getCreatedBy() != null ? credential.getCreatedBy().toString() : null);
        dto.setUpdatedBy(credential.getUpdatedBy() != null ? credential.getUpdatedBy().toString() : null);

        // 如果有设备信息，设置设备名称
        if (credential.getDevice() != null) {
            dto.setDeviceName(credential.getDevice().getName());
        }

        return dto;
    }
}
