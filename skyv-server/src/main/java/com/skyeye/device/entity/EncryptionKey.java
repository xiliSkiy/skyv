package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

/**
 * 加密密钥实体类
 * 用于管理AES加密密钥的版本和轮换
 * 
 * @author SkyEye Team
 */
@Entity
@Table(name = "tb_encryption_keys")
@Data
@EqualsAndHashCode(callSuper = true)
public class EncryptionKey extends BaseEntity {

    /**
     * 密钥名称
     */
    @Column(name = "key_name", nullable = false, length = 100)
    private String keyName;

    /**
     * 密钥版本
     */
    @Column(name = "key_version", nullable = false)
    private Integer keyVersion;

    /**
     * 加密算法（如：AES-256-GCM）
     */
    @Column(name = "algorithm", nullable = false, length = 50)
    private String algorithm;

    /**
     * 加密后的密钥数据（使用主密钥加密）
     */
    @Column(name = "encrypted_key_data", nullable = false, columnDefinition = "TEXT")
    private String encryptedKeyData;

    /**
     * 密钥哈希值（用于验证密钥完整性）
     */
    @Column(name = "key_hash", nullable = false, length = 64)
    private String keyHash;

    /**
     * 密钥状态（1:活跃 0:已停用 2:已轮换）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 是否为当前活跃密钥
     */
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = false;

    /**
     * 密钥生效时间
     */
    @Column(name = "effective_time", nullable = false)
    private Timestamp effectiveTime;

    /**
     * 密钥过期时间
     */
    @Column(name = "expire_time")
    private Timestamp expireTime;

    /**
     * 备注信息
     */
    @Column(name = "remark", length = 500)
    private String remark;
}
