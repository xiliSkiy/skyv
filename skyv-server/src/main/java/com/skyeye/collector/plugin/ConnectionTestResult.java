package com.skyeye.collector.plugin;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 连接测试结果
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class ConnectionTestResult {

    /**
     * 测试是否成功
     */
    private boolean success;

    /**
     * 测试消息
     */
    private String message;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 错误详情
     */
    private String errorDetails;

    /**
     * 测试时间
     */
    private LocalDateTime testTime;

    /**
     * 响应时间（毫秒）
     */
    private long responseTime;

    /**
     * 连接信息
     */
    private ConnectionInfo connectionInfo;

    /**
     * 测试步骤结果
     */
    private Map<String, TestStepResult> stepResults;

    /**
     * 建议操作
     */
    private String recommendation;

    /**
     * 额外信息
     */
    private Map<String, Object> additionalInfo;

    /**
     * 连接信息
     */
    @Data
    @Builder
    public static class ConnectionInfo {
        /**
         * 目标主机
         */
        private String host;

        /**
         * 目标端口
         */
        private Integer port;

        /**
         * 协议类型
         */
        private String protocol;

        /**
         * 连接状态
         */
        private String status;

        /**
         * 本地地址
         */
        private String localAddress;

        /**
         * 远程地址
         */
        private String remoteAddress;

        /**
         * SSL信息
         */
        private SslInfo sslInfo;
    }

    /**
     * SSL信息
     */
    @Data
    @Builder
    public static class SslInfo {
        /**
         * 是否启用SSL
         */
        private boolean enabled;

        /**
         * SSL版本
         */
        private String version;

        /**
         * 密码套件
         */
        private String cipherSuite;

        /**
         * 证书信息
         */
        private String certificateInfo;

        /**
         * 证书过期时间
         */
        private LocalDateTime certificateExpiry;
    }

    /**
     * 测试步骤结果
     */
    @Data
    @Builder
    public static class TestStepResult {
        /**
         * 步骤名称
         */
        private String stepName;

        /**
         * 是否成功
         */
        private boolean success;

        /**
         * 步骤消息
         */
        private String message;

        /**
         * 步骤耗时（毫秒）
         */
        private long duration;

        /**
         * 步骤详情
         */
        private String details;
    }

    /**
     * 创建成功的测试结果
     */
    public static ConnectionTestResult success(String message, long responseTime) {
        return ConnectionTestResult.builder()
                .success(true)
                .message(message)
                .testTime(LocalDateTime.now())
                .responseTime(responseTime)
                .build();
    }

    /**
     * 创建失败的测试结果
     */
    public static ConnectionTestResult failure(String errorCode, String message) {
        return ConnectionTestResult.builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .testTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建超时的测试结果
     */
    public static ConnectionTestResult timeout(long timeoutMs) {
        return ConnectionTestResult.builder()
                .success(false)
                .errorCode("TIMEOUT")
                .message("连接超时")
                .testTime(LocalDateTime.now())
                .responseTime(timeoutMs)
                .recommendation("检查网络连接和防火墙设置")
                .build();
    }
}
