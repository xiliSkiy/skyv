package com.skyeye.collector.dto;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 采集上下文
 * 包含采集过程中的上下文信息和共享数据
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class CollectionContext {

    /**
     * 会话ID
     */
    private String sessionId;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 采集开始时间
     */
    private LocalDateTime startTime;

    /**
     * 采集超时时间
     */
    private LocalDateTime deadline;

    /**
     * 设备凭据信息
     */
    private Map<String, Object> credentials;

    /**
     * 采集参数
     */
    private Map<String, Object> parameters;

    /**
     * 共享变量（线程安全）
     */
    @Builder.Default
    private Map<String, Object> variables = new ConcurrentHashMap<>();

    /**
     * 共享数据（线程安全）
     */
    @Builder.Default
    private Map<String, Object> sharedData = new ConcurrentHashMap<>();

    /**
     * 采集配置
     */
    private CollectorConfig collectorConfig;

    /**
     * 是否为调试模式
     */
    private boolean debugMode = false;

    /**
     * 是否为测试模式
     */
    private boolean testMode = false;

    /**
     * 超时时间（毫秒）
     */
    private long timeout = 30000;

    /**
     * 最大重试次数
     */
    private int maxRetries = 3;

    /**
     * 重试次数
     */
    private int retryTimes = 3;

    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval = 1000;

    /**
     * 当前重试次数
     */
    private int currentRetry = 0;

    /**
     * 是否启用缓存
     */
    private boolean enableCache = true;

    /**
     * 是否启用重试
     */
    private boolean enableRetry = true;

    /**
     * 是否启用并行采集
     */
    private boolean enableParallelCollection = false;

    /**
     * 最大并发数
     */
    private int maxConcurrency = 10;

    /**
     * 采集优先级
     */
    private int priority = 5;

    /**
     * 执行模式
     */
    private String executionMode = "NORMAL";

    /**
     * 标签信息
     */
    private Map<String, String> tags;

    /**
     * 元数据
     */
    private Map<String, Object> metadata;

    /**
     * 回调函数配置
     */
    private CallbackConfig callbackConfig;

    /**
     * 进度监听器
     */
    private ProgressListener progressListener;

    /**
     * 缓存策略
     */
    private CacheStrategy cacheStrategy = CacheStrategy.AUTO;

    /**
     * 数据格式
     */
    private DataFormat dataFormat = DataFormat.JSON;

    /**
     * 编码格式
     */
    private String encoding = "UTF-8";

    /**
     * 时区
     */
    private String timezone = "UTC";

    /**
     * 获取变量值
     */
    public Object getVariable(String key) {
        return variables.get(key);
    }

    /**
     * 设置变量值
     */
    public void setVariable(String key, Object value) {
        variables.put(key, value);
    }

    /**
     * 移除变量
     */
    public Object removeVariable(String key) {
        return variables.remove(key);
    }

    /**
     * 检查变量是否存在
     */
    public boolean hasVariable(String key) {
        return variables.containsKey(key);
    }

    /**
     * 清空所有变量
     */
    public void clearVariables() {
        variables.clear();
    }

    /**
     * 增加重试次数
     */
    public void incrementRetry() {
        this.currentRetry++;
    }

    /**
     * 重置重试次数
     */
    public void resetRetry() {
        this.currentRetry = 0;
    }

    /**
     * 检查是否可以重试
     */
    public boolean canRetry() {
        return currentRetry < maxRetries;
    }

    /**
     * 检查是否超时
     */
    public boolean isTimeout() {
        return deadline != null && LocalDateTime.now().isAfter(deadline);
    }

    /**
     * 获取剩余时间（毫秒）
     */
    public long getRemainingTime() {
        if (deadline == null) {
            return Long.MAX_VALUE;
        }
        return java.time.Duration.between(LocalDateTime.now(), deadline).toMillis();
    }

    /**
     * 回调配置
     */
    @Data
    @Builder
    public static class CallbackConfig {
        /**
         * 成功回调URL
         */
        private String successCallbackUrl;

        /**
         * 失败回调URL
         */
        private String failureCallbackUrl;

        /**
         * 回调方法
         */
        private String callbackMethod = "POST";

        /**
         * 回调头信息
         */
        private Map<String, String> callbackHeaders;

        /**
         * 回调超时时间（毫秒）
         */
        private int callbackTimeout = 5000;

        /**
         * 是否启用回调
         */
        private boolean enabled = false;
    }

    /**
     * 进度监听器接口
     */
    public interface ProgressListener {
        /**
         * 进度更新
         */
        void onProgress(String phase, int progress, String message);

        /**
         * 阶段开始
         */
        void onPhaseStart(String phase);

        /**
         * 阶段结束
         */
        void onPhaseEnd(String phase, boolean success);
    }

    /**
     * 缓存策略枚举
     */
    public enum CacheStrategy {
        ALWAYS,     // 总是使用缓存
        NEVER,      // 从不使用缓存
        AUTO        // 自动决定
    }

    /**
     * 数据格式枚举
     */
    public enum DataFormat {
        JSON,       // JSON格式
        XML,        // XML格式
        YAML,       // YAML格式
        CSV,        // CSV格式
        BINARY,     // 二进制格式
        TEXT        // 纯文本格式
    }

    /**
     * 创建默认上下文
     */
    public static CollectionContext createDefault() {
        return CollectionContext.builder()
                .sessionId(java.util.UUID.randomUUID().toString())
                .startTime(LocalDateTime.now())
                .debugMode(false)
                .testMode(false)
                .timeout(30000)
                .maxRetries(3)
                .retryTimes(3)
                .retryInterval(1000)
                .currentRetry(0)
                .enableCache(true)
                .enableRetry(true)
                .enableParallelCollection(false)
                .maxConcurrency(10)
                .priority(5)
                .executionMode("NORMAL")
                .cacheStrategy(CacheStrategy.AUTO)
                .dataFormat(DataFormat.JSON)
                .encoding("UTF-8")
                .timezone("UTC")
                .variables(new ConcurrentHashMap<>())
                .sharedData(new ConcurrentHashMap<>())
                .build();
    }

    /**
     * 创建测试上下文
     */
    public static CollectionContext createForTest() {
        return CollectionContext.builder()
                .sessionId("test-" + System.currentTimeMillis())
                .startTime(LocalDateTime.now())
                .debugMode(true)
                .testMode(true)
                .timeout(10000)
                .maxRetries(1)
                .retryTimes(1)
                .retryInterval(500)
                .currentRetry(0)
                .enableCache(false)
                .enableRetry(false)
                .enableParallelCollection(false)
                .maxConcurrency(1)
                .priority(1)
                .executionMode("TEST")
                .cacheStrategy(CacheStrategy.NEVER)
                .dataFormat(DataFormat.JSON)
                .encoding("UTF-8")
                .timezone("UTC")
                .variables(new ConcurrentHashMap<>())
                .sharedData(new ConcurrentHashMap<>())
                .build();
    }

    /**
     * 创建当前上下文的副本
     * 
     * @return 上下文副本
     */
    public CollectionContext copy() {
        return CollectionContext.builder()
                .sessionId(this.sessionId)
                .taskId(this.taskId)
                .userId(this.userId)
                .startTime(this.startTime)
                .deadline(this.deadline)
                .credentials(this.credentials != null ? new ConcurrentHashMap<>(this.credentials) : null)
                .parameters(this.parameters != null ? new ConcurrentHashMap<>(this.parameters) : null)
                .variables(this.variables != null ? new ConcurrentHashMap<>(this.variables) : null)
                .sharedData(this.sharedData != null ? new ConcurrentHashMap<>(this.sharedData) : null)
                .collectorConfig(this.collectorConfig)
                .debugMode(this.debugMode)
                .testMode(this.testMode)
                .timeout(this.timeout)
                .maxRetries(this.maxRetries)
                .retryTimes(this.retryTimes)
                .retryInterval(this.retryInterval)
                .currentRetry(this.currentRetry)
                .enableCache(this.enableCache)
                .enableRetry(this.enableRetry)
                .enableParallelCollection(this.enableParallelCollection)
                .maxConcurrency(this.maxConcurrency)
                .priority(this.priority)
                .executionMode(this.executionMode)
                .tags(this.tags != null ? new ConcurrentHashMap<>(this.tags) : null)
                .metadata(this.metadata != null ? new ConcurrentHashMap<>(this.metadata) : null)
                .callbackConfig(this.callbackConfig)
                .progressListener(this.progressListener)
                .cacheStrategy(this.cacheStrategy)
                .dataFormat(this.dataFormat)
                .encoding(this.encoding)
                .timezone(this.timezone)
                .build();
    }
}
