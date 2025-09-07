package com.skyeye.device.service;

import com.skyeye.device.dto.CredentialRequest;
import com.skyeye.device.dto.DeviceCredentialDto;
import com.skyeye.device.entity.DeviceCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 设备凭据管理服务接口
 * 
 * @author SkyEye Team
 */
public interface DeviceCredentialService {

    /**
     * 保存设备凭据
     *
     * @param deviceId 设备ID
     * @param request 凭据请求
     * @return 凭据实体
     */
    DeviceCredential saveCredential(Long deviceId, CredentialRequest request);

    /**
     * 更新设备凭据
     *
     * @param credentialId 凭据ID
     * @param request 凭据请求
     * @return 凭据实体
     */
    DeviceCredential updateCredential(Long credentialId, CredentialRequest request);

    /**
     * 删除设备凭据
     *
     * @param credentialId 凭据ID
     */
    void deleteCredential(Long credentialId);

    /**
     * 获取凭据详情（包含解密后的数据）
     *
     * @param credentialId 凭据ID
     * @return 凭据详情
     */
    DeviceCredentialDto getCredentialWithData(Long credentialId);

    /**
     * 获取凭据基本信息（不包含敏感数据）
     *
     * @param credentialId 凭据ID
     * @return 凭据基本信息
     */
    DeviceCredentialDto getCredentialInfo(Long credentialId);

    /**
     * 获取设备的所有凭据列表
     *
     * @param deviceId 设备ID
     * @return 凭据列表
     */
    List<DeviceCredentialDto> getDeviceCredentials(Long deviceId);

    /**
     * 分页查询设备凭据
     *
     * @param deviceId 设备ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<DeviceCredentialDto> getDeviceCredentials(Long deviceId, Pageable pageable);

    /**
     * 获取设备的默认凭据数据
     *
     * @param deviceId 设备ID
     * @return 凭据数据
     */
    Map<String, Object> getDefaultCredential(Long deviceId);

    /**
     * 获取设备指定协议的默认凭据数据
     *
     * @param deviceId 设备ID
     * @param protocol 协议类型
     * @return 凭据数据
     */
    Map<String, Object> getDefaultCredential(Long deviceId, String protocol);

    /**
     * 获取解密后的凭据数据
     *
     * @param credentialId 凭据ID
     * @return 凭据数据
     */
    Map<String, Object> getCredentialData(Long credentialId);

    /**
     * 设置默认凭据
     *
     * @param credentialId 凭据ID
     */
    void setDefaultCredential(Long credentialId);

    /**
     * 启用/禁用凭据
     *
     * @param credentialId 凭据ID
     * @param enabled 是否启用
     */
    void setCredentialEnabled(Long credentialId, boolean enabled);

    /**
     * 测试凭据连接
     *
     * @param credentialId 凭据ID
     * @return 测试结果
     */
    boolean testCredential(Long credentialId);

    /**
     * 批量轮换密钥
     *
     * @param oldKeyId 旧密钥ID
     * @param newKeyId 新密钥ID
     * @return 轮换数量
     */
    int rotateEncryptionKey(Long oldKeyId, Long newKeyId);

    /**
     * 复制凭据到其他设备
     *
     * @param credentialId 源凭据ID
     * @param targetDeviceIds 目标设备ID列表
     * @return 复制成功的数量
     */
    int copyCredentialToDevices(Long credentialId, List<Long> targetDeviceIds);
}
