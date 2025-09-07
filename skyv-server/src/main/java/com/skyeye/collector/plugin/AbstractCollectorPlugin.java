package com.skyeye.collector.plugin;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.CollectorConfig;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.device.entity.Device;

import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 抽象采集插件基类
 * 提供通用的插件实现逻辑
 * 
 * @author SkyEye Team
 */
@Slf4j
public abstract class AbstractCollectorPlugin implements CollectorPlugin {

    /**
     * 插件配置
     */
    protected CollectorConfig config;

    /**
     * 插件统计信息
     */
    protected final PluginStatistics statistics;

    /**
     * 插件状态
     */
    protected final AtomicBoolean initialized = new AtomicBoolean(false);
    protected final AtomicBoolean destroyed = new AtomicBoolean(false);

    /**
     * 缓存
     */
    protected final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 最后健康检查时间
     */
    protected volatile LocalDateTime lastHealthCheck;

    /**
     * 最后健康检查结果
     */
    protected volatile PluginHealthStatus lastHealthStatus;

    public AbstractCollectorPlugin() {
        this.statistics = PluginStatistics.builder().build();
        this.lastHealthCheck = LocalDateTime.now();
        this.lastHealthStatus = PluginHealthStatus.healthy("插件已创建");
    }

    @Override
    public final void initialize(CollectorConfig config) throws PluginException {
        if (initialized.get()) {
            throw new PluginException("ALREADY_INITIALIZED", "插件已经初始化");
        }

        if (destroyed.get()) {
            throw new PluginException("PLUGIN_DESTROYED", "插件已销毁，无法初始化");
        }

        try {
            this.config = config;
            doInitialize(config);
            initialized.set(true);
            log.info("插件 {} 初始化成功", getPluginType());
        } catch (Exception e) {
            log.error("插件 {} 初始化失败", getPluginType(), e);
            throw new PluginException("INITIALIZATION_FAILED", 
                    "插件初始化失败: " + e.getMessage(), e);
        }
    }

    @Override
    public final void destroy() {
        if (destroyed.get()) {
            log.warn("插件 {} 已经销毁", getPluginType());
            return;
        }

        try {
            doDestroy();
            destroyed.set(true);
            initialized.set(false);
            
            // 清理缓存
            cache.clear();
            
            log.info("插件 {} 销毁成功", getPluginType());
        } catch (Exception e) {
            log.error("插件 {} 销毁失败", getPluginType(), e);
        }
    }

    @Override
    public final CollectionResult collect(Device device, MetricConfig metricConfig, CollectionContext context) {
        if (!initialized.get()) {
            return CollectionResult.failure("PLUGIN_NOT_INITIALIZED", "插件未初始化");
        }

        if (destroyed.get()) {
            return CollectionResult.failure("PLUGIN_DESTROYED", "插件已销毁");
        }

        long startTime = System.currentTimeMillis();
        LocalDateTime collectionStartTime = LocalDateTime.now();

        try {
            // 检查缓存
            if (isCacheEnabled(metricConfig)) {
                CollectionResult cachedResult = getCachedResult(device, metricConfig);
                if (cachedResult != null) {
                    log.debug("使用缓存结果: device={}, metric={}", device.getId(), metricConfig.getMetricName());
                    return cachedResult;
                }
            }

            // 执行实际采集
            CollectionResult result = doCollect(device, metricConfig, context);
            
            // 设置采集时间信息
            result.setStartTime(collectionStartTime);
            result.setEndTime(LocalDateTime.now());
            result.setDuration(System.currentTimeMillis() - startTime);
            result.setPluginType(getPluginType());
            result.setDeviceId(device.getId());
            result.setMetricName(metricConfig.getMetricName());
            result.setSessionId(context.getSessionId());

            // 更新统计信息
            if (result.isSuccess()) {
                statistics.recordSuccess(result.getDuration());
            } else {
                statistics.recordFailure(result.getDuration());
            }

            // 缓存结果
            if (result.isSuccess() && isCacheEnabled(metricConfig)) {
                cacheResult(device, metricConfig, result);
            }

            return result;

        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            statistics.recordFailure(duration);
            
            log.error("采集数据失败: device={}, metric={}", 
                    device.getId(), metricConfig.getMetricName(), e);
            
            return CollectionResult.failure("COLLECTION_ERROR", 
                    "采集失败: " + e.getMessage());
        }
    }

    @Override
    public List<CollectionResult> collectBatch(Device device, List<MetricConfig> metricConfigs, CollectionContext context) {
        List<CollectionResult> results = new ArrayList<>();
        
        for (MetricConfig metricConfig : metricConfigs) {
            CollectionResult result = collect(device, metricConfig, context);
            results.add(result);
            
            // 如果是测试模式或调试模式，可以提前退出
            if (context.isTestMode() && !result.isSuccess()) {
                break;
            }
        }
        
        return results;
    }

    @Override
    public PluginHealthStatus getHealthStatus() {
        try {
            PluginHealthStatus currentStatus = doHealthCheck();
            lastHealthCheck = LocalDateTime.now();
            lastHealthStatus = currentStatus;
            return currentStatus;
        } catch (Exception e) {
            log.error("健康检查失败: {}", getPluginType(), e);
            PluginHealthStatus errorStatus = PluginHealthStatus.unhealthy("健康检查异常: " + e.getMessage());
            lastHealthStatus = errorStatus;
            return errorStatus;
        }
    }

    @Override
    public PluginStatistics getStatistics() {
        return statistics;
    }

    @Override
    public ConnectionTestResult testConnection(Device device, CollectionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            return doTestConnection(device, context);
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("连接测试失败: device={}", device.getId(), e);
            return ConnectionTestResult.failure("CONNECTION_TEST_ERROR", 
                    "连接测试异常: " + e.getMessage());
        }
    }

    @Override
    public List<AvailableMetric> discoverMetrics(Device device, CollectionContext context) {
        try {
            return doDiscoverMetrics(device, context);
        } catch (Exception e) {
            log.error("指标发现失败: device={}", device.getId(), e);
            return Collections.emptyList();
        }
    }

    @Override
    public ConfigValidationResult validateConfig(MetricConfig metricConfig) {
        try {
            return doValidateConfig(metricConfig);
        } catch (Exception e) {
            log.error("配置验证失败: metric={}", metricConfig.getMetricName(), e);
            return ConfigValidationResult.failure("配置验证异常: " + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getConfigTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("pluginType", getPluginType());
        template.put("timeout", 30);
        template.put("retryTimes", 3);
        template.put("retryInterval", 1000);
        template.put("cacheEnabled", false);
        template.put("cacheTtl", 60);
        
        // 子类可以覆盖此方法添加特定配置
        return template;
    }

    @Override
    public boolean supportsConcurrentCollection() {
        return true; // 默认支持并发
    }

    @Override
    public int getRecommendedConcurrency() {
        return 5; // 默认推荐并发度
    }

    @Override
    public int getPriority() {
        return 5; // 默认优先级
    }

    /**
     * 子类需要实现的初始化方法
     */
    protected abstract void doInitialize(CollectorConfig config) throws PluginException;

    /**
     * 子类需要实现的销毁方法
     */
    protected abstract void doDestroy();

    /**
     * 子类需要实现的采集方法
     */
    protected abstract CollectionResult doCollect(Device device, MetricConfig metricConfig, CollectionContext context);

    /**
     * 子类可以覆盖的健康检查方法
     */
    protected PluginHealthStatus doHealthCheck() {
        if (!initialized.get()) {
            return PluginHealthStatus.unhealthy("插件未初始化");
        }
        if (destroyed.get()) {
            return PluginHealthStatus.unhealthy("插件已销毁");
        }
        return PluginHealthStatus.healthy("插件运行正常");
    }

    /**
     * 子类可以覆盖的连接测试方法
     */
    protected ConnectionTestResult doTestConnection(Device device, CollectionContext context) {
        long responseTime = 0; // 可以在子类中实现实际的连接测试逻辑
        return ConnectionTestResult.success("连接测试通过", responseTime);
    }

    /**
     * 子类可以覆盖的指标发现方法
     */
    protected List<AvailableMetric> doDiscoverMetrics(Device device, CollectionContext context) {
        return Collections.emptyList();
    }

    /**
     * 子类可以覆盖的配置验证方法
     */
    protected ConfigValidationResult doValidateConfig(MetricConfig metricConfig) {
        return ConfigValidationResult.success("配置验证通过");
    }

    /**
     * 检查是否启用缓存
     */
    protected boolean isCacheEnabled(MetricConfig metricConfig) {
        return metricConfig.isCacheEnabled() && config != null && 
               config.getCacheConfig() != null && config.getCacheConfig().isEnabled();
    }

    /**
     * 获取缓存结果
     */
    protected CollectionResult getCachedResult(Device device, MetricConfig metricConfig) {
        String cacheKey = buildCacheKey(device, metricConfig);
        CacheEntry entry = cache.get(cacheKey);
        
        if (entry != null && !entry.isExpired()) {
            return entry.getResult();
        }
        
        return null;
    }

    /**
     * 缓存结果
     */
    protected void cacheResult(Device device, MetricConfig metricConfig, CollectionResult result) {
        String cacheKey = buildCacheKey(device, metricConfig);
        long ttl = metricConfig.getCacheTtl() * 1000L; // 转换为毫秒
        CacheEntry entry = new CacheEntry(result, System.currentTimeMillis() + ttl);
        cache.put(cacheKey, entry);
    }

    /**
     * 构建缓存键
     */
    protected String buildCacheKey(Device device, MetricConfig metricConfig) {
        return String.format("%s:%s:%s", getPluginType(), device.getId(), metricConfig.getMetricName());
    }

    /**
     * 缓存条目
     */
    protected static class CacheEntry {
        private final CollectionResult result;
        private final long expireTime;

        public CacheEntry(CollectionResult result, long expireTime) {
            this.result = result;
            this.expireTime = expireTime;
        }

        public CollectionResult getResult() {
            return result;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }
}
