package com.skyeye.device.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 加密凭据数据传输对象
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class EncryptedCredential {
    
    /**
     * 加密后的数据
     */
    private String encryptedData;
    
    /**
     * 加密向量（IV）
     */
    private String iv;
    
    /**
     * 加密密钥ID
     */
    private Long keyId;
    
    /**
     * 加密算法
     */
    private String algorithm;
}
