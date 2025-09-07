package com.skyeye.collector.plugin;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 插件健康状态
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class PluginHealthStatus {

    /**
     * 健康状态
     */
    private HealthStatus status;

    /**
     * 状态消息
     */
    private String message;

    /**
     * 详细信息
     */
    private String details;

    /**
     * 检查时间
     */
    private LocalDateTime checkTime;

    /**
     * 上次成功时间
     */
    private LocalDateTime lastSuccessTime;

    /**
     * 上次失败时间
     */
    private LocalDateTime lastFailureTime;

    /**
     * 连续失败次数
     */
    private int consecutiveFailures;

    /**
     * 响应时间（毫秒）
     */
    private long responseTime;

    /**
     * 额外指标
     */
    private Map<String, Object> metrics;

    /**
     * 建议操作
     */
    private String recommendation;

    /**
     * 健康状态枚举
     */
    public enum HealthStatus {
        HEALTHY,        // 健康
        DEGRADED,       // 降级
        UNHEALTHY,      // 不健康
        UNKNOWN         // 未知
    }

    /**
     * 创建健康状态
     */
    public static PluginHealthStatus healthy(String message) {
        return PluginHealthStatus.builder()
                .status(HealthStatus.HEALTHY)
                .message(message)
                .checkTime(LocalDateTime.now())
                .lastSuccessTime(LocalDateTime.now())
                .consecutiveFailures(0)
                .build();
    }

    /**
     * 创建不健康状态
     */
    public static PluginHealthStatus unhealthy(String message) {
        return PluginHealthStatus.builder()
                .status(HealthStatus.UNHEALTHY)
                .message(message)
                .checkTime(LocalDateTime.now())
                .lastFailureTime(LocalDateTime.now())
                .build();
    }

    /**
     * 创建降级状态
     */
    public static PluginHealthStatus degraded(String message) {
        return PluginHealthStatus.builder()
                .status(HealthStatus.DEGRADED)
                .message(message)
                .checkTime(LocalDateTime.now())
                .build();
    }

    /**
     * 检查是否健康
     * 
     * @return 是否健康
     */
    public boolean isHealthy() {
        return status == HealthStatus.HEALTHY;
    }
}
