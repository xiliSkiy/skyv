package com.skyeye.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * SNMP采集插件配置
 * 
 * @author SkyEye Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "skyeye.collector.snmp")
public class SnmpConfig {

    /**
     * 是否启用SNMP插件
     */
    private boolean enabled = true;

    /**
     * 默认超时时间（毫秒）
     */
    private int defaultTimeout = 15000;

    /**
     * 默认重试次数
     */
    private int defaultRetries = 2;

    /**
     * 最大并发会话数
     */
    private int maxConcurrentSessions = 50;

    /**
     * 会话缓存过期时间（秒）
     */
    private int sessionCacheExpire = 300;

    /**
     * 默认SNMP版本
     */
    private String defaultVersion = "v2c";

    /**
     * 默认社区字符串
     */
    private String defaultCommunity = "public";

    /**
     * 默认端口
     */
    private int defaultPort = 161;

    /**
     * 最大PDU大小
     */
    private int maxSizeRequestPDU = 65535;

    /**
     * 是否启用批量操作
     */
    private boolean enableBulk = true;

    /**
     * 批量操作最大重复次数
     */
    private int maxRepetitions = 50;

    /**
     * 是否启用会话复用
     */
    private boolean enableSessionReuse = true;

    /**
     * 连接池配置
     */
    private PoolConfig pool = new PoolConfig();

    /**
     * 性能配置
     */
    private PerformanceConfig performance = new PerformanceConfig();

    /**
     * 安全配置
     */
    private SecurityConfig security = new SecurityConfig();

    @Data
    public static class PoolConfig {
        /**
         * 核心池大小
         */
        private int coreSize = 5;

        /**
         * 最大池大小
         */
        private int maxSize = 20;

        /**
         * 队列大小
         */
        private int queueSize = 100;

        /**
         * 空闲超时时间（秒）
         */
        private int idleTimeout = 60;
    }

    @Data
    public static class PerformanceConfig {
        /**
         * 是否启用性能监控
         */
        private boolean enableMetrics = true;

        /**
         * 统计窗口大小
         */
        private int metricsWindowSize = 1000;

        /**
         * 是否启用缓存
         */
        private boolean enableCache = true;

        /**
         * 缓存大小
         */
        private int cacheSize = 500;

        /**
         * 缓存TTL（秒）
         */
        private int cacheTtl = 60;
    }

    @Data
    public static class SecurityConfig {
        /**
         * 是否验证响应源地址
         */
        private boolean validateResponseSource = true;

        /**
         * 是否记录安全事件
         */
        private boolean logSecurityEvents = true;

        /**
         * 最大认证失败次数
         */
        private int maxAuthFailures = 3;

        /**
         * 认证失败锁定时间（秒）
         */
        private int authFailureLockTime = 300;
    }
}
