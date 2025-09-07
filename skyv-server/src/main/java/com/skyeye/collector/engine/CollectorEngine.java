package com.skyeye.collector.engine;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;

import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.entity.CollectionData;
import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.registry.PluginRegistry;
import com.skyeye.collector.repository.CollectionDataRepository;
import com.skyeye.collector.service.CollectionLogService;
import com.skyeye.common.util.JsonUtils;
import com.skyeye.device.entity.Device;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * 采集引擎
 * 负责管理和调度采集插件的执行
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
public class CollectorEngine {

    private final PluginRegistry pluginRegistry;
    private final CollectionLogService collectionLogService;
    private final CollectionDataRepository collectionDataRepository;
    private final Executor collectionExecutor;

    public CollectorEngine(PluginRegistry pluginRegistry,
                          CollectionLogService collectionLogService,
                          CollectionDataRepository collectionDataRepository,
                          @Qualifier("applicationTaskExecutor") Executor collectionExecutor) {
        this.pluginRegistry = pluginRegistry;
        this.collectionLogService = collectionLogService;
        this.collectionDataRepository = collectionDataRepository;
        this.collectionExecutor = collectionExecutor;
    }

    /**
     * 执行单个设备的数据采集
     * 
     * @param device 设备
     * @param metricConfig 指标配置
     * @param context 采集上下文
     * @return 采集结果
     */
    public CollectionResult executeCollection(Device device, MetricConfig metricConfig, CollectionContext context) {
        log.debug("开始执行采集: device={}, metric={}", device.getId(), metricConfig.getMetricName());
        
        try {
            // 1. 选择合适的采集插件
            CollectorPlugin plugin = selectPlugin(device, metricConfig);
            if (plugin == null) {
                return CollectionResult.builder()
                        .success(false)
                        .errorMessage("未找到适合的采集插件")
                        .errorCode("NO_PLUGIN_FOUND")
                        .timestamp(System.currentTimeMillis())
                        .startTime(LocalDateTime.now())
                        .endTime(LocalDateTime.now())
                        .deviceId(device.getId())
                        .metricName(metricConfig.getMetricName())
                        .sessionId(context.getSessionId())
                        .build();
            }

            // 2. 执行数据采集
            CollectionResult result = plugin.collect(device, metricConfig, context);

            // 3. 记录采集日志
            collectionLogService.logCollection(device.getId(), metricConfig.getMetricName(), result);

            // 4. 保存采集数据
            if (result.isSuccess() && result.getMetrics() != null && !result.getMetrics().isEmpty()) {
                saveCollectionData(device.getId(), metricConfig, result);
            }

            log.debug("采集完成: device={}, metric={}, success={}", 
                    device.getId(), metricConfig.getMetricName(), result.isSuccess());

            return result;

        } catch (Exception e) {
            log.error("采集执行异常: device={}, metric={}", 
                    device.getId(), metricConfig.getMetricName(), e);

            CollectionResult errorResult = CollectionResult.builder()
                    .success(false)
                    .errorMessage("采集执行异常: " + e.getMessage())
                    .errorCode("EXECUTION_ERROR")
                    .timestamp(System.currentTimeMillis())
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now())
                    .deviceId(device.getId())
                    .metricName(metricConfig.getMetricName())
                    .sessionId(context.getSessionId())
                    .build();

            // 记录错误日志
            collectionLogService.logCollection(device.getId(), metricConfig.getMetricName(), errorResult);

            return errorResult;
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
    public CompletableFuture<CollectionResult> executeCollectionAsync(Device device, 
                                                                     MetricConfig metricConfig, 
                                                                     CollectionContext context) {
        return CompletableFuture.supplyAsync(() -> 
                executeCollection(device, metricConfig, context), collectionExecutor);
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
        log.debug("开始批量采集: device={}, metricCount={}", device.getId(), metricConfigs.size());

        List<CollectionResult> results = new ArrayList<>();
        String batchSessionId = context.getSessionId() != null ? 
                context.getSessionId() : UUID.randomUUID().toString();

        for (MetricConfig metricConfig : metricConfigs) {
            // 为每个指标创建独立的上下文
            CollectionContext metricContext = context.copy();
            metricContext.setSessionId(batchSessionId);

            CollectionResult result = executeCollection(device, metricConfig, metricContext);
            results.add(result);
        }

        log.debug("批量采集完成: device={}, totalCount={}, successCount={}", 
                device.getId(), results.size(), 
                results.stream().mapToInt(r -> r.isSuccess() ? 1 : 0).sum());

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
    public CompletableFuture<List<CollectionResult>> executeBatchCollectionAsync(Device device, 
                                                                                List<MetricConfig> metricConfigs, 
                                                                                CollectionContext context) {
        log.debug("开始异步批量采集: device={}, metricCount={}", device.getId(), metricConfigs.size());

        String batchSessionId = context.getSessionId() != null ? 
                context.getSessionId() : UUID.randomUUID().toString();

        List<CompletableFuture<CollectionResult>> futures = new ArrayList<>();

        for (MetricConfig metricConfig : metricConfigs) {
            CollectionContext metricContext = context.copy();
            metricContext.setSessionId(batchSessionId);

            CompletableFuture<CollectionResult> future = executeCollectionAsync(device, metricConfig, metricContext);
            futures.add(future);
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(v -> futures.stream()
                        .map(CompletableFuture::join)
                        .toList());
    }

    /**
     * 选择合适的采集插件
     * 
     * @param device 设备
     * @param metricConfig 指标配置
     * @return 采集插件
     */
    private CollectorPlugin selectPlugin(Device device, MetricConfig metricConfig) {
        // 1. 优先根据指标配置中指定的插件类型选择
        String preferredPluginType = metricConfig.getPluginType();
        if (preferredPluginType != null) {
            Optional<CollectorPlugin> plugin = pluginRegistry.getPlugin(preferredPluginType);
            if (plugin.isPresent() && plugin.get().supports(device.getDeviceType())) {
                return plugin.get();
            }
        }

        // 2. 根据设备类型自动选择支持的插件
        return pluginRegistry.getAllPlugins().stream()
                .filter(plugin -> plugin.supports(device.getDeviceType()))
                .findFirst()
                .orElse(null);
    }

    /**
     * 保存采集数据
     * 
     * @param deviceId 设备ID
     * @param metricConfig 指标配置
     * @param result 采集结果
     */
    @Transactional
    protected void saveCollectionData(Long deviceId, MetricConfig metricConfig, CollectionResult result) {
        try {
            for (Map.Entry<String, Object> entry : result.getMetrics().entrySet()) {
                CollectionData data = new CollectionData();
                data.setDeviceId(deviceId);
                data.setTaskId(result.getTaskId());
                data.setMetricName(entry.getKey());
                data.setMetricType(metricConfig.getMetricType());
                data.setMetricValue(parseNumericValue(entry.getValue()));
                data.setMetricData(JsonUtils.toJson(entry.getValue()));
                data.setCollectedAt(new Timestamp(result.getTimestamp()));
                data.setQualityScore(result.getQualityScore());
                data.setPluginType(result.getPluginType());
                data.setSessionId(result.getSessionId());
                data.setResponseTime(calculateResponseTime(result));
                data.setStatus(result.isSuccess() ? 1 : 0);
                
                if (!result.isSuccess()) {
                    data.setErrorMessage(result.getErrorMessage());
                }

                // 设置数据过期时间（根据指标类型设置不同的保留时间）
                data.setExpiresAt(calculateExpirationTime(metricConfig));

                collectionDataRepository.save(data);
            }

            log.debug("保存采集数据完成: deviceId={}, metricCount={}", 
                    deviceId, result.getMetrics().size());

        } catch (Exception e) {
            log.error("保存采集数据失败: deviceId={}, metric={}", 
                    deviceId, metricConfig.getMetricName(), e);
        }
    }

    /**
     * 解析数值型数据
     * 
     * @param value 原始值
     * @return 数值
     */
    private BigDecimal parseNumericValue(Object value) {
        if (value == null) {
            return null;
        }

        try {
            if (value instanceof Number) {
                return BigDecimal.valueOf(((Number) value).doubleValue());
            } else if (value instanceof String) {
                String strValue = (String) value;
                // 尝试解析字符串中的数字
                if (strValue.matches("-?\\d+(\\.\\d+)?")) {
                    return new BigDecimal(strValue);
                }
            } else if (value instanceof Boolean) {
                return ((Boolean) value) ? BigDecimal.ONE : BigDecimal.ZERO;
            }
        } catch (NumberFormatException e) {
            log.debug("无法解析数值: {}", value);
        }

        return null;
    }

    /**
     * 计算响应时间
     * 
     * @param result 采集结果
     * @return 响应时间（毫秒）
     */
    private Long calculateResponseTime(CollectionResult result) {
        if (result.getStartTime() != null && result.getEndTime() != null) {
            return Timestamp.valueOf(result.getEndTime()).getTime() - 
                   Timestamp.valueOf(result.getStartTime()).getTime();
        }
        return null;
    }

    /**
     * 计算数据过期时间
     * 
     * @param metricConfig 指标配置
     * @return 过期时间
     */
    private Timestamp calculateExpirationTime(MetricConfig metricConfig) {
        // 根据指标类型设置不同的保留时间
        int retentionDays;
        String metricType = metricConfig.getMetricType();
        
        switch (metricType) {
            case "health_check":
            case "status_code":
            case "response_time":
                retentionDays = 7; // 基础监控数据保留7天
                break;
            case "system_info":
            case "interface_stats":
                retentionDays = 30; // 系统和接口数据保留30天
                break;
            case "custom_oid":
            case "custom_endpoint":
                retentionDays = 90; // 自定义数据保留90天
                break;
            default:
                retentionDays = 30; // 默认保留30天
        }

        return Timestamp.valueOf(LocalDateTime.now().plusDays(retentionDays));
    }

    /**
     * 获取引擎统计信息
     * 
     * @return 统计信息
     */
    public Map<String, Object> getEngineStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 插件统计
        Map<String, Object> pluginStats = new HashMap<>();
        pluginRegistry.getAllPlugins().forEach(plugin -> {
            Map<String, Object> pluginInfo = new HashMap<>();
            pluginInfo.put("type", plugin.getPluginType());
            pluginInfo.put("name", plugin.getPluginName());
            pluginInfo.put("version", plugin.getPluginVersion());
            pluginInfo.put("supportedProtocols", plugin.getSupportedProtocols());
            pluginInfo.put("supportedMetricTypes", plugin.getSupportedMetricTypes());
            pluginInfo.put("healthStatus", plugin.getHealthStatus());
            pluginInfo.put("statistics", plugin.getStatistics());
            
            pluginStats.put(plugin.getPluginType(), pluginInfo);
        });
        statistics.put("plugins", pluginStats);

        // 系统统计
        statistics.put("totalPlugins", pluginRegistry.getAllPlugins().size());
        statistics.put("timestamp", System.currentTimeMillis());

        return statistics;
    }

    /**
     * 清理过期数据
     * 
     * @return 清理的数据量
     */
    @Transactional
    public int cleanupExpiredData() {
        try {
            Timestamp now = new Timestamp(System.currentTimeMillis());
            int deletedCount = collectionDataRepository.deleteExpiredData(now);
            log.info("清理过期采集数据完成，删除了 {} 条记录", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理过期数据失败", e);
            return 0;
        }
    }

    /**
     * 获取采集数据统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    public Map<String, Object> getCollectionDataStatistics(Timestamp startTime, Timestamp endTime) {
        Map<String, Object> statistics = new HashMap<>();
        
        try {
            // 总数据量
            Long totalCount = collectionDataRepository.countByTimeRange(startTime, endTime);
            statistics.put("totalCount", totalCount);

            // 插件类型统计
            List<Object[]> pluginStats = collectionDataRepository.countByPluginType();
            Map<String, Long> pluginMap = pluginStats.stream()
                    .collect(Collectors.toMap(
                            row -> (String) row[0],
                            row -> (Long) row[1]
                    ));
            statistics.put("pluginStatistics", pluginMap);

            statistics.put("timeRange", Map.of(
                    "startTime", startTime.toString(),
                    "endTime", endTime.toString()
            ));

        } catch (Exception e) {
            log.error("获取采集数据统计失败", e);
            statistics.put("error", e.getMessage());
        }

        return statistics;
    }
}
