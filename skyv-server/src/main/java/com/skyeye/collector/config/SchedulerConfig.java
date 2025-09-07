package com.skyeye.collector.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 调度器配置
 * 
 * @author SkyEye Team
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "skyeye.scheduler")
public class SchedulerConfig {

    /**
     * 线程池大小
     */
    private int threadPoolSize = 10;

    /**
     * 最大队列大小
     */
    private int maxQueueSize = 100;

    /**
     * 线程保活时间（秒）
     */
    private long keepAliveSeconds = 60;

    /**
     * 清理间隔（分钟）
     */
    private long cleanupIntervalMinutes = 30;

    /**
     * 统计更新间隔（分钟）
     */
    private long statisticsUpdateIntervalMinutes = 1;

    /**
     * 任务执行超时时间（秒）
     */
    private long taskExecutionTimeoutSeconds = 300;

    /**
     * 是否启用自动清理
     */
    private boolean autoCleanupEnabled = true;

    /**
     * 是否启用统计收集
     */
    private boolean statisticsEnabled = true;

    /**
     * 是否启用任务重试
     */
    private boolean retryEnabled = true;

    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 3;

    /**
     * 重试间隔（毫秒）
     */
    private long retryIntervalMs = 1000;

    /**
     * 是否启用任务优先级
     */
    private boolean priorityEnabled = true;

    /**
     * 是否启用任务并发控制
     */
    private boolean concurrencyControlEnabled = true;

    /**
     * 最大并发任务数
     */
    private int maxConcurrentTasks = 50;

    /**
     * 是否启用任务监控
     */
    private boolean monitoringEnabled = true;

    /**
     * 监控数据保留天数
     */
    private int monitoringDataRetentionDays = 30;

    /**
     * 是否启用任务日志
     */
    private boolean taskLoggingEnabled = true;

    /**
     * 任务日志级别
     */
    private String taskLogLevel = "INFO";

    /**
     * 是否启用性能监控
     */
    private boolean performanceMonitoringEnabled = true;

    /**
     * 性能监控采样间隔（秒）
     */
    private long performanceSamplingIntervalSeconds = 60;

    /**
     * 是否启用告警通知
     */
    private boolean alertNotificationEnabled = true;

    /**
     * 告警阈值（失败率百分比）
     */
    private double alertThresholdPercentage = 10.0;

    /**
     * 告警检查间隔（分钟）
     */
    private long alertCheckIntervalMinutes = 5;

    /**
     * 是否启用任务预热
     */
    private boolean taskWarmupEnabled = false;

    /**
     * 任务预热时间（秒）
     */
    private long taskWarmupTimeSeconds = 30;

    /**
     * 是否启用任务负载均衡
     */
    private boolean loadBalancingEnabled = false;

    /**
     * 负载均衡策略
     */
    private String loadBalancingStrategy = "ROUND_ROBIN";

    /**
     * 是否启用任务故障转移
     */
    private boolean failoverEnabled = false;

    /**
     * 故障转移重试次数
     */
    private int failoverRetryTimes = 2;

    /**
     * 是否启用任务限流
     */
    private boolean rateLimitingEnabled = false;

    /**
     * 限流速率（任务/秒）
     */
    private int rateLimitPerSecond = 100;

    /**
     * 是否启用任务熔断
     */
    private boolean circuitBreakerEnabled = false;

    /**
     * 熔断器失败阈值
     */
    private int circuitBreakerFailureThreshold = 5;

    /**
     * 熔断器恢复时间（秒）
     */
    private long circuitBreakerRecoveryTimeSeconds = 60;
}
