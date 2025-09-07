package com.skyeye.device.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.Map;

/**
 * 凭据创建/更新请求DTO
 * 
 * @author SkyEye Team
 */
@Data
public class CredentialRequest {

    /**
     * 凭据名称
     */
    @NotBlank(message = "凭据名称不能为空")
    private String credentialName;

    /**
     * 凭据类型
     */
    @NotBlank(message = "凭据类型不能为空")
    private String credentialType;

    /**
     * 协议类型
     */
    private String protocolType;

    /**
     * 凭据数据（明文，将被加密存储）
     */
    @NotNull(message = "凭据数据不能为空")
    private Map<String, Object> credentialData;

    /**
     * 是否为默认凭据
     */
    private Boolean isDefault = false;

    /**
     * 备注信息
     */
    private String remark;
}
