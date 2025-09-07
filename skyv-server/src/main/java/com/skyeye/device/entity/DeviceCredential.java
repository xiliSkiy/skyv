package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备凭据实体类
 * 用于存储设备登录凭据的加密信息
 * 
 * @author SkyEye Team
 */
@Entity
@Table(name = "tb_device_credentials")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceCredential extends BaseEntity {

    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 凭据名称（如：默认凭据、管理员凭据等）
     */
    @Column(name = "credential_name", nullable = false, length = 100)
    private String credentialName;

    /**
     * 凭据类型（如：USERNAME_PASSWORD、API_KEY、CERTIFICATE等）
     */
    @Column(name = "credential_type", nullable = false, length = 50)
    private String credentialType;

    /**
     * 协议类型（如：SNMP、HTTP、SSH等）
     */
    @Column(name = "protocol_type", length = 50)
    private String protocolType;

    /**
     * 加密后的凭据数据
     */
    @Column(name = "encrypted_data", nullable = false, columnDefinition = "TEXT")
    private String encryptedData;

    /**
     * 加密向量（IV）
     */
    @Column(name = "encryption_iv", nullable = false, length = 32)
    private String encryptionIv;

    /**
     * 加密密钥ID
     */
    @Column(name = "encryption_key_id", nullable = false)
    private Long encryptionKeyId;

    /**
     * 是否为默认凭据
     */
    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    /**
     * 凭据状态（1:启用 0:禁用）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 备注信息
     */
    @Column(name = "remark", length = 500)
    private String remark;

    /**
     * 设备关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_id", insertable = false, updatable = false)
    private Device device;

    /**
     * 加密密钥关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encryption_key_id", insertable = false, updatable = false)
    private EncryptionKey encryptionKey;
}
