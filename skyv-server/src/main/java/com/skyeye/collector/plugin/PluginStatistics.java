package com.skyeye.collector.plugin;

import lombok.Data;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 插件统计信息
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class PluginStatistics {

    /**
     * 总采集次数
     */
    @Builder.Default
    private AtomicLong totalCollections = new AtomicLong(0);

    /**
     * 成功采集次数
     */
    @Builder.Default
    private AtomicLong successfulCollections = new AtomicLong(0);

    /**
     * 失败采集次数
     */
    @Builder.Default
    private AtomicLong failedCollections = new AtomicLong(0);

    /**
     * 总采集时间（毫秒）
     */
    @Builder.Default
    private AtomicLong totalCollectionTime = new AtomicLong(0);

    /**
     * 最小响应时间（毫秒）
     */
    private Long minResponseTime;

    /**
     * 最大响应时间（毫秒）
     */
    private Long maxResponseTime;

    /**
     * 平均响应时间（毫秒）
     */
    private Double averageResponseTime;

    /**
     * 最后采集时间
     */
    private LocalDateTime lastCollectionTime;

    /**
     * 最后成功时间
     */
    private LocalDateTime lastSuccessTime;

    /**
     * 最后失败时间
     */
    private LocalDateTime lastFailureTime;

    /**
     * 统计开始时间
     */
    private LocalDateTime statisticsStartTime;

    /**
     * 当前活跃连接数
     */
    @Builder.Default
    private AtomicLong activeConnections = new AtomicLong(0);

    /**
     * 队列中等待的任务数
     */
    @Builder.Default
    private AtomicLong queuedTasks = new AtomicLong(0);

    /**
     * 内存使用量（字节）
     */
    private Long memoryUsage;

    /**
     * CPU使用率（百分比）
     */
    private Double cpuUsage;

    /**
     * 网络流量统计
     */
    private NetworkStatistics networkStatistics;

    /**
     * 错误统计
     */
    private Map<String, Long> errorStatistics;

    /**
     * 自定义指标
     */
    private Map<String, Object> customMetrics;

    /**
     * 计算成功率
     */
    public double getSuccessRate() {
        long total = totalCollections.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) successfulCollections.get() / total * 100;
    }

    /**
     * 计算失败率
     */
    public double getFailureRate() {
        long total = totalCollections.get();
        if (total == 0) {
            return 0.0;
        }
        return (double) failedCollections.get() / total * 100;
    }

    /**
     * 更新平均响应时间
     */
    public void updateAverageResponseTime(long responseTime) {
        long total = totalCollections.get();
        if (total == 0) {
            averageResponseTime = (double) responseTime;
        } else {
            averageResponseTime = (averageResponseTime * (total - 1) + responseTime) / total;
        }
    }

    /**
     * 更新最小/最大响应时间
     */
    public void updateResponseTimeRange(long responseTime) {
        if (minResponseTime == null || responseTime < minResponseTime) {
            minResponseTime = responseTime;
        }
        if (maxResponseTime == null || responseTime > maxResponseTime) {
            maxResponseTime = responseTime;
        }
    }

    /**
     * 记录成功采集
     */
    public void recordSuccess(long responseTime) {
        totalCollections.incrementAndGet();
        successfulCollections.incrementAndGet();
        totalCollectionTime.addAndGet(responseTime);
        lastCollectionTime = LocalDateTime.now();
        lastSuccessTime = LocalDateTime.now();
        updateAverageResponseTime(responseTime);
        updateResponseTimeRange(responseTime);
    }

    /**
     * 记录失败采集
     */
    public void recordFailure(long responseTime) {
        totalCollections.incrementAndGet();
        failedCollections.incrementAndGet();
        totalCollectionTime.addAndGet(responseTime);
        lastCollectionTime = LocalDateTime.now();
        lastFailureTime = LocalDateTime.now();
        updateAverageResponseTime(responseTime);
        updateResponseTimeRange(responseTime);
    }

    /**
     * 重置统计信息
     */
    public void reset() {
        totalCollections.set(0);
        successfulCollections.set(0);
        failedCollections.set(0);
        totalCollectionTime.set(0);
        activeConnections.set(0);
        queuedTasks.set(0);
        minResponseTime = null;
        maxResponseTime = null;
        averageResponseTime = null;
        lastCollectionTime = null;
        lastSuccessTime = null;
        lastFailureTime = null;
        statisticsStartTime = LocalDateTime.now();
        if (errorStatistics != null) {
            errorStatistics.clear();
        }
        if (customMetrics != null) {
            customMetrics.clear();
        }
    }

    /**
     * 网络统计信息
     */
    @Data
    @Builder
    public static class NetworkStatistics {
        /**
         * 发送字节数
         */
        @Builder.Default
        private AtomicLong bytesSent = new AtomicLong(0);

        /**
         * 接收字节数
         */
        @Builder.Default
        private AtomicLong bytesReceived = new AtomicLong(0);

        /**
         * 发送包数
         */
        @Builder.Default
        private AtomicLong packetsSent = new AtomicLong(0);

        /**
         * 接收包数
         */
        @Builder.Default
        private AtomicLong packetsReceived = new AtomicLong(0);

        /**
         * 连接错误数
         */
        @Builder.Default
        private AtomicLong connectionErrors = new AtomicLong(0);

        /**
         * 超时错误数
         */
        @Builder.Default
        private AtomicLong timeoutErrors = new AtomicLong(0);
    }
}
