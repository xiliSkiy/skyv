package com.skyeye.device.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 设备凭据数据传输对象
 * 
 * @author SkyEye Team
 */
@Data
public class DeviceCredentialDto {

    /**
     * 凭据ID
     */
    private Long id;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 设备名称
     */
    private String deviceName;

    /**
     * 凭据名称
     */
    private String credentialName;

    /**
     * 凭据类型
     */
    private String credentialType;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 是否为默认凭据
     */
    private Boolean isDefault;

    /**
     * 凭据状态
     */
    private Integer status;

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
     * 凭据数据（仅在需要时解密返回，不包含在列表查询中）
     */
    private Map<String, Object> credentialData;
}
