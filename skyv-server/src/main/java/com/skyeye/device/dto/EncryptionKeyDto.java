package com.skyeye.device.dto;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 加密密钥数据传输对象
 * 
 * @author SkyEye Team
 */
@Data
public class EncryptionKeyDto {

    /**
     * 密钥ID
     */
    private Long id;

    /**
     * 密钥名称
     */
    private String keyName;

    /**
     * 密钥版本
     */
    private Integer keyVersion;

    /**
     * 加密算法
     */
    private String algorithm;

    /**
     * 密钥哈希值
     */
    private String keyHash;

    /**
     * 密钥状态（1:活跃 0:已停用 2:已轮换）
     */
    private Integer status;

    /**
     * 是否为当前活跃密钥
     */
    private Boolean isActive;

    /**
     * 密钥生效时间
     */
    private Timestamp effectiveTime;

    /**
     * 密钥过期时间
     */
    private Timestamp expireTime;

    /**
     * 备注信息
     */
    private String remark;

    /**
     * 创建时间
     */
    private Timestamp createdAt;

    /**
     * 更新时间
     */
    private Timestamp updatedAt;

    /**
     * 创建者
     */
    private String createdBy;

    /**
     * 更新者
     */
    private String updatedBy;

    /**
     * 使用该密钥的凭据数量
     */
    private Long usageCount;
}
