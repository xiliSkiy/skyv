package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.util.JsonUtils;
import com.skyeye.device.dto.EncryptedCredential;
import com.skyeye.device.entity.EncryptionKey;
import com.skyeye.device.repository.EncryptionKeyRepository;
import com.skyeye.device.service.CredentialEncryptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Map;

/**
 * 凭据加密服务实现类
 * 使用AES-256-GCM算法进行加密
 * 
 * @author SkyEye Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CredentialEncryptionServiceImpl implements CredentialEncryptionService {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int KEY_SIZE = 256;
    private static final int IV_SIZE = 12; // GCM标准IV长度
    private static final int TAG_SIZE = 128; // GCM标签长度

    private final EncryptionKeyRepository encryptionKeyRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${skyeye.security.master-key:SkyEye-Master-Key-2024-Default}")
    private String masterKey;

    @Override
    public EncryptedCredential encrypt(Map<String, Object> plainData, Long keyId) {
        try {
            // 获取加密密钥
            EncryptionKey encryptionKey = getEncryptionKey(keyId);
            if (!isKeyValid(keyId)) {
                throw new BusinessException("加密密钥无效或已过期");
            }

            // 解密密钥数据
            SecretKey secretKey = decryptKeyData(encryptionKey.getEncryptedKeyData());

            // 生成随机IV
            byte[] iv = new byte[IV_SIZE];
            secureRandom.nextBytes(iv);

            // 执行加密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec);

            String plainJson = JsonUtils.toJson(plainData);
            byte[] encryptedBytes = cipher.doFinal(plainJson.getBytes(StandardCharsets.UTF_8));

            return EncryptedCredential.builder()
                    .encryptedData(Base64.getEncoder().encodeToString(encryptedBytes))
                    .iv(Base64.getEncoder().encodeToString(iv))
                    .keyId(keyId)
                    .algorithm(encryptionKey.getAlgorithm())
                    .build();

        } catch (Exception e) {
            log.error("加密凭据数据失败", e);
            throw new BusinessException("加密凭据数据失败: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> decrypt(EncryptedCredential encryptedCredential) {
        try {
            // 获取加密密钥
            EncryptionKey encryptionKey = getEncryptionKey(encryptedCredential.getKeyId());
            
            // 解密密钥数据
            SecretKey secretKey = decryptKeyData(encryptionKey.getEncryptedKeyData());

            // 执行解密
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] iv = Base64.getDecoder().decode(encryptedCredential.getIv());
            GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec);

            byte[] encryptedBytes = Base64.getDecoder().decode(encryptedCredential.getEncryptedData());
            byte[] decryptedBytes = cipher.doFinal(encryptedBytes);

            String decryptedJson = new String(decryptedBytes, StandardCharsets.UTF_8);
            @SuppressWarnings("unchecked")
            Map<String, Object> result = JsonUtils.fromJson(decryptedJson, Map.class);
            return result;

        } catch (Exception e) {
            log.error("解密凭据数据失败", e);
            throw new BusinessException("解密凭据数据失败: " + e.getMessage());
        }
    }

    @Override
    public EncryptionKey getCurrentActiveKey() {
        return encryptionKeyRepository.findByIsActiveTrue()
                .orElseThrow(() -> new BusinessException("未找到活跃的加密密钥"));
    }

    @Override
    @Transactional
    public EncryptionKey createNewKey(String keyName) {
        try {
            // 生成新的AES密钥
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(KEY_SIZE);
            SecretKey secretKey = keyGenerator.generateKey();

            // 使用主密钥加密新生成的密钥
            String encryptedKeyData = encryptKeyData(secretKey);
            String keyHash = generateKeyHash(secretKey.getEncoded());

            // 获取当前最大版本号
            Integer maxVersion = encryptionKeyRepository.findMaxKeyVersion().orElse(0);

            // 创建密钥实体
            EncryptionKey encryptionKey = new EncryptionKey();
            encryptionKey.setKeyName(keyName);
            encryptionKey.setKeyVersion(maxVersion + 1);
            encryptionKey.setAlgorithm("AES-256-GCM");
            encryptionKey.setEncryptedKeyData(encryptedKeyData);
            encryptionKey.setKeyHash(keyHash);
            encryptionKey.setStatus(1);
            encryptionKey.setIsActive(false); // 新创建的密钥默认不激活
            encryptionKey.setEffectiveTime(Timestamp.valueOf(LocalDateTime.now()));

            return encryptionKeyRepository.save(encryptionKey);

        } catch (Exception e) {
            log.error("创建加密密钥失败", e);
            throw new BusinessException("创建加密密钥失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public EncryptionKey rotateKey(Long oldKeyId) {
        // 停用旧密钥
        EncryptionKey oldKey = getEncryptionKey(oldKeyId);
        oldKey.setIsActive(false);
        oldKey.setStatus(2); // 已轮换
        encryptionKeyRepository.save(oldKey);

        // 创建新密钥
        String newKeyName = "Auto-Rotated-" + System.currentTimeMillis();
        EncryptionKey newKey = createNewKey(newKeyName);
        
        // 激活新密钥
        newKey.setIsActive(true);
        return encryptionKeyRepository.save(newKey);
    }

    @Override
    public boolean isKeyValid(Long keyId) {
        return encryptionKeyRepository.findById(keyId)
                .map(key -> {
                    // 检查密钥状态
                    if (key.getStatus() != 1) {
                        return false;
                    }
                    
                    // 检查是否过期
                    if (key.getExpireTime() != null) {
                        return key.getExpireTime().after(new Timestamp(System.currentTimeMillis()));
                    }
                    
                    return true;
                })
                .orElse(false);
    }

    /**
     * 获取加密密钥
     */
    private EncryptionKey getEncryptionKey(Long keyId) {
        return encryptionKeyRepository.findById(keyId)
                .orElseThrow(() -> new BusinessException("加密密钥不存在: " + keyId));
    }

    /**
     * 使用主密钥加密密钥数据
     */
    private String encryptKeyData(SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        
        // 生成IV
        byte[] iv = new byte[IV_SIZE];
        secureRandom.nextBytes(iv);
        
        // 使用主密钥
        SecretKeySpec masterKeySpec = new SecretKeySpec(
                generateMasterKeyBytes(), ALGORITHM);
        
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.ENCRYPT_MODE, masterKeySpec, gcmSpec);
        
        byte[] encryptedKey = cipher.doFinal(secretKey.getEncoded());
        
        // 将IV和加密数据合并
        byte[] combined = new byte[IV_SIZE + encryptedKey.length];
        System.arraycopy(iv, 0, combined, 0, IV_SIZE);
        System.arraycopy(encryptedKey, 0, combined, IV_SIZE, encryptedKey.length);
        
        return Base64.getEncoder().encodeToString(combined);
    }

    /**
     * 使用主密钥解密密钥数据
     */
    private SecretKey decryptKeyData(String encryptedKeyData) throws Exception {
        byte[] combined = Base64.getDecoder().decode(encryptedKeyData);
        
        // 分离IV和加密数据
        byte[] iv = new byte[IV_SIZE];
        byte[] encryptedKey = new byte[combined.length - IV_SIZE];
        System.arraycopy(combined, 0, iv, 0, IV_SIZE);
        System.arraycopy(combined, IV_SIZE, encryptedKey, 0, encryptedKey.length);
        
        Cipher cipher = Cipher.getInstance(TRANSFORMATION);
        SecretKeySpec masterKeySpec = new SecretKeySpec(
                generateMasterKeyBytes(), ALGORITHM);
        
        GCMParameterSpec gcmSpec = new GCMParameterSpec(TAG_SIZE, iv);
        cipher.init(Cipher.DECRYPT_MODE, masterKeySpec, gcmSpec);
        
        byte[] keyBytes = cipher.doFinal(encryptedKey);
        return new SecretKeySpec(keyBytes, ALGORITHM);
    }

    /**
     * 生成主密钥字节数组
     */
    private byte[] generateMasterKeyBytes() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(masterKey.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成密钥哈希值
     */
    private String generateKeyHash(byte[] keyBytes) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(keyBytes);
        return Base64.getEncoder().encodeToString(hash);
    }
}
