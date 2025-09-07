package com.skyeye.collector.manager;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.engine.CollectorEngine;
import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.registry.PluginRegistry;
import com.skyeye.collector.service.CollectionLogService;
import com.skyeye.device.entity.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 采集引擎管理器
 * 负责管理采集引擎的生命周期、监控和统计
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CollectorEngineManager {

    private final CollectorEngine collectorEngine;
    private final PluginRegistry pluginRegistry;
    private final CollectionLogService collectionLogService;

    // 统计信息
    private final AtomicLong totalCollections = new AtomicLong(0);
    private final AtomicLong successfulCollections = new AtomicLong(0);
    private final AtomicLong failedCollections = new AtomicLong(0);
    private final AtomicInteger activeCollections = new AtomicInteger(0);

    // 性能统计
    private final Map<String, AtomicLong> pluginExecutionCounts = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> pluginExecutionTimes = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> pluginErrorCounts = new ConcurrentHashMap<>();

    private volatile LocalDateTime lastCleanupTime = LocalDateTime.now();

    /**
     * 执行数据采集并更新统计信息
     * 
     * @param device 设备
     * @param metricConfig 指标配置
     * @param context 采集上下文
     * @return 采集结果
     */
    public CollectionResult executeCollection(Device device, MetricConfig metricConfig, CollectionContext context) {
        totalCollections.incrementAndGet();
        activeCollections.incrementAndGet();
        
        long startTime = System.currentTimeMillis();
        String pluginType = null;
        
        try {
            CollectionResult result = collectorEngine.executeCollection(device, metricConfig, context);
            
            pluginType = result.getPluginType();
            long executionTime = System.currentTimeMillis() - startTime;
            
            // 更新统计信息
            updateExecutionStatistics(pluginType, executionTime, result.isSuccess());
            
            if (result.isSuccess()) {
                successfulCollections.incrementAndGet();
            } else {
                failedCollections.incrementAndGet();
            }
            
            return result;
            
        } catch (Exception e) {
            log.error("采集执行异常: device={}, metric={}", device.getId(), metricConfig.getMetricName(), e);
            failedCollections.incrementAndGet();
            
            if (pluginType != null) {
                pluginErrorCounts.computeIfAbsent(pluginType, k -> new AtomicLong(0)).incrementAndGet();
            }
            
            throw e;
        } finally {
            activeCollections.decrementAndGet();
        }
    }

    /**
     * 异步执行数据采集
     * 
     * @param device 设备
     * @param metricConfig 指标配置
     * @param context 采集上下文
     * @return 采集结果的Future
     */
    @Async("collectionExecutor")
    public CompletableFuture<CollectionResult> executeCollectionAsync(Device device, 
                                                                     MetricConfig metricConfig, 
                                                                     CollectionContext context) {
        return CompletableFuture.supplyAsync(() -> executeCollection(device, metricConfig, context));
    }

    /**
     * 批量执行数据采集
     * 
     * @param device 设备
     * @param metricConfigs 指标配置列表
     * @param context 采集上下文
     * @return 采集结果列表
     */
    public List<CollectionResult> executeBatchCollection(Device device, 
                                                        List<MetricConfig> metricConfigs, 
                                                        CollectionContext context) {
        List<CollectionResult> results = new ArrayList<>();
        
        for (MetricConfig metricConfig : metricConfigs) {
            CollectionResult result = executeCollection(device, metricConfig, context);
            results.add(result);
        }
        
        return results;
    }

    /**
     * 异步批量执行数据采集
     * 
     * @param device 设备
     * @param metricConfigs 指标配置列表
     * @param context 采集上下文
     * @return 采集结果列表的Future
     */
    @Async("collectionExecutor")
    public CompletableFuture<List<CollectionResult>> executeBatchCollectionAsync(Device device, 
                                                                                List<MetricConfig> metricConfigs, 
                                                                                CollectionContext context) {
        return CompletableFuture.supplyAsync(() -> executeBatchCollection(device, metricConfigs, context));
    }

    /**
     * 获取引擎状态信息
     * 
     * @return 状态信息
     */
    public Map<String, Object> getEngineStatus() {
        Map<String, Object> status = new HashMap<>();
        
        // 基本统计
        status.put("totalCollections", totalCollections.get());
        status.put("successfulCollections", successfulCollections.get());
        status.put("failedCollections", failedCollections.get());
        status.put("activeCollections", activeCollections.get());
        status.put("successRate", calculateSuccessRate());
        
        // 插件统计
        Map<String, Object> pluginStats = new HashMap<>();
        for (String pluginType : pluginExecutionCounts.keySet()) {
            Map<String, Object> pluginInfo = new HashMap<>();
            pluginInfo.put("executionCount", pluginExecutionCounts.get(pluginType).get());
            pluginInfo.put("averageExecutionTime", calculateAverageExecutionTime(pluginType));
            pluginInfo.put("errorCount", pluginErrorCounts.getOrDefault(pluginType, new AtomicLong(0)).get());
            pluginStats.put(pluginType, pluginInfo);
        }
        status.put("pluginStatistics", pluginStats);
        
        // 系统信息
        status.put("registeredPlugins", pluginRegistry.getAllPlugins().size());
        status.put("lastCleanupTime", lastCleanupTime.toString());
        status.put("timestamp", System.currentTimeMillis());
        
        return status;
    }

    /**
     * 获取详细的引擎统计信息
     * 
     * @return 详细统计信息
     */
    public Map<String, Object> getDetailedStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 基本统计
        statistics.put("basicStats", getEngineStatus());
        
        // 引擎统计
        statistics.put("engineStats", collectorEngine.getEngineStatistics());
        
        // 插件详细信息
        Map<String, Object> pluginDetails = new HashMap<>();
        for (CollectorPlugin plugin : pluginRegistry.getAllPlugins()) {
            Map<String, Object> pluginInfo = new HashMap<>();
            pluginInfo.put("type", plugin.getPluginType());
            pluginInfo.put("name", plugin.getPluginName());
            pluginInfo.put("version", plugin.getPluginVersion());
            pluginInfo.put("description", plugin.getPluginDescription());
            pluginInfo.put("supportedProtocols", plugin.getSupportedProtocols());
            pluginInfo.put("supportedMetricTypes", plugin.getSupportedMetricTypes());
            pluginInfo.put("healthStatus", plugin.getHealthStatus());
            pluginInfo.put("statistics", plugin.getStatistics());
            pluginInfo.put("configTemplate", plugin.getConfigTemplate());
            
            pluginDetails.put(plugin.getPluginType(), pluginInfo);
        }
        statistics.put("pluginDetails", pluginDetails);
        
        // 最近的日志统计
        Timestamp oneHourAgo = Timestamp.valueOf(LocalDateTime.now().minusHours(1));
        Timestamp now = Timestamp.valueOf(LocalDateTime.now());
        Map<String, Object> recentStats = collectionLogService.getCollectionStatistics(oneHourAgo, now);
        statistics.put("recentStatistics", recentStats);
        
        return statistics;
    }

    /**
     * 重置统计信息
     */
    public void resetStatistics() {
        totalCollections.set(0);
        successfulCollections.set(0);
        failedCollections.set(0);
        activeCollections.set(0);
        
        pluginExecutionCounts.clear();
        pluginExecutionTimes.clear();
        pluginErrorCounts.clear();
        
        log.info("引擎统计信息已重置");
    }

    /**
     * 定期清理任务
     */
    @Scheduled(fixedRate = 3600000) // 每小时执行一次
    public void performScheduledCleanup() {
        try {
            log.debug("开始执行定期清理任务");
            
            // 清理过期数据
            int expiredDataCount = collectorEngine.cleanupExpiredData();
            
            // 清理过期日志（保留30天）
            Timestamp thirtyDaysAgo = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
            int expiredLogsCount = collectionLogService.cleanupLogs(thirtyDaysAgo);
            
            // 处理超时任务（超过30分钟未完成的任务）
            int timeoutTasksCount = collectionLogService.handleTimeoutTasks(30);
            
            lastCleanupTime = LocalDateTime.now();
            
            log.info("定期清理完成: 清理过期数据{}条, 清理过期日志{}条, 处理超时任务{}个", 
                    expiredDataCount, expiredLogsCount, timeoutTasksCount);
                    
        } catch (Exception e) {
            log.error("定期清理任务执行失败", e);
        }
    }

    /**
     * 健康检查
     * 
     * @return 健康状态
     */
    public Map<String, Object> performHealthCheck() {
        Map<String, Object> healthStatus = new HashMap<>();
        boolean isHealthy = true;
        List<String> issues = new ArrayList<>();
        
        try {
            // 检查插件状态
            for (CollectorPlugin plugin : pluginRegistry.getAllPlugins()) {
                var pluginHealth = plugin.getHealthStatus();
                if (!pluginHealth.isHealthy()) {
                    isHealthy = false;
                    issues.add("插件 " + plugin.getPluginType() + " 状态异常: " + pluginHealth.getMessage());
                }
            }
            
            // 检查活跃采集任务数量
            int activeCount = activeCollections.get();
            if (activeCount > 100) { // 假设100是合理的上限
                issues.add("活跃采集任务过多: " + activeCount);
            }
            
            // 检查成功率
            double successRate = calculateSuccessRate();
            if (successRate < 80.0) { // 成功率低于80%认为有问题
                isHealthy = false;
                issues.add("采集成功率过低: " + String.format("%.2f%%", successRate));
            }
            
            healthStatus.put("healthy", isHealthy);
            healthStatus.put("issues", issues);
            healthStatus.put("activeCollections", activeCount);
            healthStatus.put("successRate", successRate);
            healthStatus.put("checkTime", System.currentTimeMillis());
            
        } catch (Exception e) {
            log.error("健康检查执行失败", e);
            healthStatus.put("healthy", false);
            healthStatus.put("error", "健康检查执行异常: " + e.getMessage());
        }
        
        return healthStatus;
    }

    /**
     * 更新执行统计信息
     * 
     * @param pluginType 插件类型
     * @param executionTime 执行时间
     * @param success 是否成功
     */
    private void updateExecutionStatistics(String pluginType, long executionTime, boolean success) {
        if (pluginType != null) {
            pluginExecutionCounts.computeIfAbsent(pluginType, k -> new AtomicLong(0)).incrementAndGet();
            pluginExecutionTimes.computeIfAbsent(pluginType, k -> new AtomicLong(0)).addAndGet(executionTime);
            
            if (!success) {
                pluginErrorCounts.computeIfAbsent(pluginType, k -> new AtomicLong(0)).incrementAndGet();
            }
        }
    }

    /**
     * 计算成功率
     * 
     * @return 成功率百分比
     */
    private double calculateSuccessRate() {
        long total = totalCollections.get();
        if (total == 0) {
            return 100.0;
        }
        return (double) successfulCollections.get() / total * 100.0;
    }

    /**
     * 计算平均执行时间
     * 
     * @param pluginType 插件类型
     * @return 平均执行时间（毫秒）
     */
    private double calculateAverageExecutionTime(String pluginType) {
        AtomicLong totalTime = pluginExecutionTimes.get(pluginType);
        AtomicLong count = pluginExecutionCounts.get(pluginType);
        
        if (totalTime == null || count == null || count.get() == 0) {
            return 0.0;
        }
        
        return (double) totalTime.get() / count.get();
    }

    /**
     * 获取插件性能排名
     * 
     * @return 插件性能排名
     */
    public List<Map<String, Object>> getPluginPerformanceRanking() {
        List<Map<String, Object>> ranking = new ArrayList<>();
        
        for (String pluginType : pluginExecutionCounts.keySet()) {
            Map<String, Object> performance = new HashMap<>();
            performance.put("pluginType", pluginType);
            performance.put("executionCount", pluginExecutionCounts.get(pluginType).get());
            performance.put("averageExecutionTime", calculateAverageExecutionTime(pluginType));
            performance.put("errorCount", pluginErrorCounts.getOrDefault(pluginType, new AtomicLong(0)).get());
            
            long execCount = pluginExecutionCounts.get(pluginType).get();
            long errorCount = pluginErrorCounts.getOrDefault(pluginType, new AtomicLong(0)).get();
            double errorRate = execCount > 0 ? (double) errorCount / execCount * 100.0 : 0.0;
            performance.put("errorRate", errorRate);
            
            ranking.add(performance);
        }
        
        // 按平均执行时间排序
        ranking.sort((a, b) -> Double.compare(
                (Double) a.get("averageExecutionTime"),
                (Double) b.get("averageExecutionTime")
        ));
        
        return ranking;
    }
}
