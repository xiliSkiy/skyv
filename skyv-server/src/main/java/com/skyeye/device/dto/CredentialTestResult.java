package com.skyeye.device.dto;

import lombok.Builder;
import lombok.Data;

import java.sql.Timestamp;

/**
 * 凭据测试结果DTO
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class CredentialTestResult {

    /**
     * 测试是否成功
     */
    private Boolean success;

    /**
     * 测试消息
     */
    private String message;

    /**
     * 错误代码（如果失败）
     */
    private String errorCode;

    /**
     * 响应时间（毫秒）
     */
    private Long responseTime;

    /**
     * 测试时间
     */
    private Timestamp testTime;

    /**
     * 测试的协议类型
     */
    private String protocolType;

    /**
     * 测试的目标地址
     */
    private String targetAddress;

    /**
     * 测试详情
     */
    private String details;
}
