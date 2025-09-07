package com.skyeye.device.service;

import com.skyeye.device.dto.EncryptedCredential;
import com.skyeye.device.entity.EncryptionKey;

import java.util.Map;

/**
 * 凭据加密服务接口
 * 
 * @author SkyEye Team
 */
public interface CredentialEncryptionService {

    /**
     * 加密凭据数据
     *
     * @param plainData 明文数据
     * @param keyId 加密密钥ID
     * @return 加密结果
     */
    EncryptedCredential encrypt(Map<String, Object> plainData, Long keyId);

    /**
     * 解密凭据数据
     *
     * @param encryptedCredential 加密凭据对象
     * @return 明文数据
     */
    Map<String, Object> decrypt(EncryptedCredential encryptedCredential);

    /**
     * 获取当前活跃的加密密钥
     *
     * @return 加密密钥
     */
    EncryptionKey getCurrentActiveKey();

    /**
     * 创建新的加密密钥
     *
     * @param keyName 密钥名称
     * @return 新创建的密钥
     */
    EncryptionKey createNewKey(String keyName);

    /**
     * 轮换加密密钥
     *
     * @param oldKeyId 旧密钥ID
     * @return 新密钥
     */
    EncryptionKey rotateKey(Long oldKeyId);

    /**
     * 验证密钥是否有效
     *
     * @param keyId 密钥ID
     * @return 是否有效
     */
    boolean isKeyValid(Long keyId);
}
