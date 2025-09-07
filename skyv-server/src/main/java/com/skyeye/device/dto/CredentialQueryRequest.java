package com.skyeye.device.dto;

import lombok.Data;

/**
 * 凭据查询请求DTO
 * 
 * @author SkyEye Team
 */
@Data
public class CredentialQueryRequest {

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 凭据名称（模糊查询）
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
     * 凭据状态（1:启用 0:禁用）
     */
    private Integer status;

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 10;

    /**
     * 排序字段
     */
    private String sortBy = "updatedAt";

    /**
     * 排序方向（asc/desc）
     */
    private String sortDir = "desc";
}
