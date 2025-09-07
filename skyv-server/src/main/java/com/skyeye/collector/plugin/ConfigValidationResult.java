package com.skyeye.collector.plugin;

import lombok.Data;
import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * 配置验证结果
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class ConfigValidationResult {

    /**
     * 验证是否通过
     */
    private boolean valid;

    /**
     * 验证消息
     */
    private String message;

    /**
     * 验证错误列表
     */
    private List<ValidationError> errors;

    /**
     * 验证警告列表
     */
    private List<ValidationWarning> warnings;

    /**
     * 建议修改
     */
    private List<String> suggestions;

    /**
     * 修正后的配置
     */
    private Map<String, Object> correctedConfig;

    /**
     * 验证详情
     */
    private String details;

    /**
     * 验证错误
     */
    @Data
    @Builder
    public static class ValidationError {
        /**
         * 错误字段
         */
        private String field;

        /**
         * 错误代码
         */
        private String errorCode;

        /**
         * 错误消息
         */
        private String message;

        /**
         * 当前值
         */
        private Object currentValue;

        /**
         * 期望值
         */
        private Object expectedValue;

        /**
         * 错误严重级别
         */
        private Severity severity;

        public enum Severity {
            LOW,      // 低
            MEDIUM,   // 中
            HIGH,     // 高
            CRITICAL  // 严重
        }
    }

    /**
     * 验证警告
     */
    @Data
    @Builder
    public static class ValidationWarning {
        /**
         * 警告字段
         */
        private String field;

        /**
         * 警告代码
         */
        private String warningCode;

        /**
         * 警告消息
         */
        private String message;

        /**
         * 当前值
         */
        private Object currentValue;

        /**
         * 建议值
         */
        private Object suggestedValue;
    }

    /**
     * 创建成功的验证结果
     */
    public static ConfigValidationResult success() {
        return ConfigValidationResult.builder()
                .valid(true)
                .message("配置验证通过")
                .build();
    }

    /**
     * 创建成功的验证结果（带消息）
     */
    public static ConfigValidationResult success(String message) {
        return ConfigValidationResult.builder()
                .valid(true)
                .message(message)
                .build();
    }

    /**
     * 创建失败的验证结果
     */
    public static ConfigValidationResult failure(String message) {
        return ConfigValidationResult.builder()
                .valid(false)
                .message(message)
                .build();
    }

    /**
     * 创建失败的验证结果（带错误列表）
     */
    public static ConfigValidationResult failure(String message, List<ValidationError> errors) {
        return ConfigValidationResult.builder()
                .valid(false)
                .message(message)
                .errors(errors)
                .build();
    }
}
