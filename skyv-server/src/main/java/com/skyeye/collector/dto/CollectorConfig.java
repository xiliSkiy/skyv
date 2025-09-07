package com.skyeye.collector.dto;

import lombok.Data;
import lombok.Builder;

import java.util.Map;
import java.util.List;

/**
 * 采集器配置
 * 定义了采集插件的配置参数
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class CollectorConfig {

    /**
     * 配置名称
     */
    private String name;

    /**
     * 配置描述
     */
    private String description;

    /**
     * 插件类型
     */
    private String pluginType;

    /**
     * 插件版本
     */
    private String pluginVersion;

    /**
     * 全局配置参数
     */
    private Map<String, Object> globalParameters;

    /**
     * 连接配置
     */
    private ConnectionConfig connectionConfig;

    /**
     * 性能配置
     */
    private PerformanceConfig performanceConfig;

    /**
     * 安全配置
     */
    private SecurityConfig securityConfig;

    /**
     * 重试配置
     */
    private RetryConfig retryConfig;

    /**
     * 缓存配置
     */
    private CacheConfig cacheConfig;

    /**
     * 监控配置
     */
    private MonitoringConfig monitoringConfig;

    /**
     * 日志配置
     */
    private LoggingConfig loggingConfig;

    /**
     * 扩展配置
     */
    private Map<String, Object> extensionConfig;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 连接配置
     */
    @Data
    @Builder
    public static class ConnectionConfig {
        /**
         * 连接超时（毫秒）
         */
        private int connectTimeout = 5000;

        /**
         * 读取超时（毫秒）
         */
        private int readTimeout = 30000;

        /**
         * 最大连接数
         */
        private int maxConnections = 10;

        /**
         * 连接池大小
         */
        private int poolSize = 5;

        /**
         * 连接保活时间（毫秒）
         */
        private long keepAliveTime = 60000;

        /**
         * 是否启用连接复用
         */
        private boolean reuseConnection = true;

        /**
         * 连接验证间隔（毫秒）
         */
        private long validationInterval = 30000;
    }

    /**
     * 性能配置
     */
    @Data
    @Builder
    public static class PerformanceConfig {
        /**
         * 最大并发采集数
         */
        private int maxConcurrency = 5;

        /**
         * 队列大小
         */
        private int queueSize = 100;

        /**
         * 批处理大小
         */
        private int batchSize = 10;

        /**
         * 批处理超时（毫秒）
         */
        private int batchTimeout = 5000;

        /**
         * 是否启用批处理
         */
        private boolean batchEnabled = false;

        /**
         * 内存使用限制（MB）
         */
        private int memoryLimit = 256;

        /**
         * CPU使用限制（百分比）
         */
        private int cpuLimit = 80;
    }

    /**
     * 安全配置
     */
    @Data
    @Builder
    public static class SecurityConfig {
        /**
         * 是否启用SSL
         */
        private boolean sslEnabled = false;

        /**
         * SSL版本
         */
        private String sslVersion = "TLSv1.2";

        /**
         * 证书验证模式
         */
        private CertificateValidation certificateValidation = CertificateValidation.REQUIRED;

        /**
         * 信任库路径
         */
        private String trustStorePath;

        /**
         * 信任库密码
         */
        private String trustStorePassword;

        /**
         * 客户端证书路径
         */
        private String clientCertPath;

        /**
         * 客户端私钥路径
         */
        private String clientKeyPath;

        /**
         * 是否启用主机名验证
         */
        private boolean hostnameVerification = true;

        public enum CertificateValidation {
            REQUIRED,   // 必需验证
            OPTIONAL,   // 可选验证
            DISABLED    // 禁用验证
        }
    }

    /**
     * 重试配置
     */
    @Data
    @Builder
    public static class RetryConfig {
        /**
         * 默认重试次数
         */
        private int defaultRetryTimes = 3;

        /**
         * 重试间隔（毫秒）
         */
        private long retryInterval = 1000;

        /**
         * 重试策略
         */
        private RetryStrategy retryStrategy = RetryStrategy.EXPONENTIAL_BACKOFF;

        /**
         * 最大重试间隔（毫秒）
         */
        private long maxRetryInterval = 60000;

        /**
         * 重试乘数
         */
        private double backoffMultiplier = 2.0;

        /**
         * 需要重试的错误类型
         */
        private List<String> retryableErrors;

        public enum RetryStrategy {
            FIXED_INTERVAL,     // 固定间隔
            LINEAR_BACKOFF,     // 线性退避
            EXPONENTIAL_BACKOFF // 指数退避
        }
    }

    /**
     * 缓存配置
     */
    @Data
    @Builder
    public static class CacheConfig {
        /**
         * 是否启用缓存
         */
        private boolean enabled = false;

        /**
         * 缓存类型
         */
        private CacheType cacheType = CacheType.MEMORY;

        /**
         * 默认TTL（秒）
         */
        private int defaultTtl = 300;

        /**
         * 最大缓存大小
         */
        private int maxCacheSize = 1000;

        /**
         * 缓存清理间隔（秒）
         */
        private int cleanupInterval = 300;

        /**
         * Redis配置（当缓存类型为REDIS时）
         */
        private Map<String, Object> redisConfig;

        public enum CacheType {
            MEMORY,     // 内存缓存
            REDIS,      // Redis缓存
            HYBRID      // 混合缓存
        }
    }

    /**
     * 监控配置
     */
    @Data
    @Builder
    public static class MonitoringConfig {
        /**
         * 是否启用指标收集
         */
        private boolean metricsEnabled = true;

        /**
         * 是否启用健康检查
         */
        private boolean healthCheckEnabled = true;

        /**
         * 健康检查间隔（秒）
         */
        private int healthCheckInterval = 60;

        /**
         * 性能统计收集间隔（秒）
         */
        private int statisticsInterval = 300;

        /**
         * 告警配置
         */
        private Map<String, Object> alertConfig;
    }

    /**
     * 日志配置
     */
    @Data
    @Builder
    public static class LoggingConfig {
        /**
         * 日志级别
         */
        private LogLevel logLevel = LogLevel.INFO;

        /**
         * 是否启用详细日志
         */
        private boolean verboseLogging = false;

        /**
         * 是否记录原始数据
         */
        private boolean logRawData = false;

        /**
         * 日志文件路径
         */
        private String logFilePath;

        /**
         * 日志文件最大大小（MB）
         */
        private int maxLogFileSize = 100;

        /**
         * 日志文件保留天数
         */
        private int logRetentionDays = 30;

        public enum LogLevel {
            TRACE, DEBUG, INFO, WARN, ERROR
        }
    }
}
