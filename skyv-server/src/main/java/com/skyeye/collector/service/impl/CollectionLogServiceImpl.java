package com.skyeye.collector.service.impl;

import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.entity.CollectionLog;
import com.skyeye.collector.repository.CollectionLogRepository;
import com.skyeye.collector.service.CollectionLogService;
import com.skyeye.common.util.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采集日志服务实现
 * 
 * @author SkyEye Team
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionLogServiceImpl implements CollectionLogService {

    private final CollectionLogRepository collectionLogRepository;

    @Override
    @Transactional
    public CollectionLog logCollection(Long deviceId, String metricName, CollectionResult result) {
        CollectionLog log = new CollectionLog();
        log.setDeviceId(deviceId);
        log.setMetricName(metricName);
        log.setPluginType(result.getPluginType());
        log.setExecutionId(result.getSessionId());
        log.setStartTime(result.getStartTime() != null ? 
                Timestamp.valueOf(result.getStartTime()) : new Timestamp(System.currentTimeMillis()));
        log.setEndTime(result.getEndTime() != null ? 
                Timestamp.valueOf(result.getEndTime()) : new Timestamp(System.currentTimeMillis()));
        log.setStatus(result.isSuccess() ? "COMPLETED" : "FAILED");
        log.setSuccess(result.isSuccess());
        log.setErrorMessage(result.getErrorMessage());
        log.setErrorCode(result.getErrorCode());
        log.setResponseTime(calculateResponseTime(result));
        log.setQualityScore(result.getQualityScore());
        log.setDataCount(result.getMetrics() != null ? result.getMetrics().size() : 0);
        
        // 序列化额外数据
        if (result.getMetrics() != null && !result.getMetrics().isEmpty()) {
            log.setExtraData(JsonUtils.toJson(result.getMetrics()));
        }

        return collectionLogRepository.save(log);
    }

    @Override
    @Transactional
    public CollectionLog logCollectionStart(Long taskId, Long deviceId, String executionId, 
                                          String metricName, String pluginType) {
        CollectionLog log = new CollectionLog();
        log.setTaskId(taskId);
        log.setDeviceId(deviceId);
        log.setExecutionId(executionId);
        log.setMetricName(metricName);
        log.setPluginType(pluginType);
        log.setStartTime(new Timestamp(System.currentTimeMillis()));
        log.setStatus("RUNNING");

        return collectionLogRepository.save(log);
    }

    @Override
    @Transactional
    public void logCollectionEnd(Long logId, CollectionResult result) {
        collectionLogRepository.findById(logId).ifPresent(log -> {
            log.setEndTime(result.getEndTime() != null ? 
                    Timestamp.valueOf(result.getEndTime()) : new Timestamp(System.currentTimeMillis()));
            log.setStatus(result.isSuccess() ? "COMPLETED" : "FAILED");
            log.setSuccess(result.isSuccess());
            log.setErrorMessage(result.getErrorMessage());
            log.setErrorCode(result.getErrorCode());
            log.setResponseTime(calculateResponseTime(result));
            log.setQualityScore(result.getQualityScore());
            log.setDataCount(result.getMetrics() != null ? result.getMetrics().size() : 0);
            
            if (result.getMetrics() != null && !result.getMetrics().isEmpty()) {
                log.setExtraData(JsonUtils.toJson(result.getMetrics()));
            }

            collectionLogRepository.save(log);
        });
    }

    @Override
    @Transactional
    public void logCollectionFailure(Long logId, String errorMessage, String errorCode) {
        collectionLogRepository.findById(logId).ifPresent(log -> {
            log.setEndTime(new Timestamp(System.currentTimeMillis()));
            log.setStatus("FAILED");
            log.setSuccess(false);
            log.setErrorMessage(errorMessage);
            log.setErrorCode(errorCode);

            collectionLogRepository.save(log);
        });
    }

    @Override
    public List<CollectionLog> getDeviceLogs(Long deviceId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return collectionLogRepository.findByDeviceIdOrderByStartTimeDesc(deviceId, pageRequest).getContent();
    }

    @Override
    public List<CollectionLog> getTaskLogs(Long taskId, int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return collectionLogRepository.findByTaskIdOrderByStartTimeDesc(taskId, pageRequest).getContent();
    }

    @Override
    public List<CollectionLog> getFailedLogs(int limit) {
        PageRequest pageRequest = PageRequest.of(0, limit);
        return collectionLogRepository.findFailedLogs(pageRequest).getContent();
    }

    @Override
    public List<CollectionLog> getRunningLogs() {
        return collectionLogRepository.findRunningLogs();
    }

    @Override
    public Map<String, Object> getCollectionStatistics(Timestamp startTime, Timestamp endTime) {
        Map<String, Object> statistics = new HashMap<>();

        // 获取成功率统计
        Object[] successRateData = collectionLogRepository.getSuccessRateInTimeRange(startTime, endTime);
        if (successRateData != null && successRateData.length >= 2) {
            Long totalCount = (Long) successRateData[0];
            Long successCount = (Long) successRateData[1];
            
            statistics.put("totalCount", totalCount);
            statistics.put("successCount", successCount);
            statistics.put("failureCount", totalCount - successCount);
            statistics.put("successRate", totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0);
        }

        // 获取状态统计
        List<Object[]> statusStats = collectionLogRepository.countByStatus();
        Map<String, Long> statusMap = statusStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
        statistics.put("statusStatistics", statusMap);

        // 获取插件统计
        List<Object[]> pluginStats = collectionLogRepository.countByPluginType();
        Map<String, Long> pluginMap = pluginStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
        statistics.put("pluginStatistics", pluginMap);

        // 获取平均响应时间
        Double avgResponseTime = collectionLogRepository.getAverageResponseTime();
        statistics.put("averageResponseTime", avgResponseTime != null ? avgResponseTime : 0.0);

        return statistics;
    }

    @Override
    public Map<String, Long> getPluginStatistics() {
        List<Object[]> pluginStats = collectionLogRepository.countByPluginType();
        return pluginStats.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
    }

    @Override
    public Double getSuccessRate(Timestamp startTime, Timestamp endTime) {
        Object[] successRateData = collectionLogRepository.getSuccessRateInTimeRange(startTime, endTime);
        if (successRateData != null && successRateData.length >= 2) {
            Long totalCount = (Long) successRateData[0];
            Long successCount = (Long) successRateData[1];
            return totalCount > 0 ? (double) successCount / totalCount * 100 : 0.0;
        }
        return 0.0;
    }

    @Override
    @Transactional
    public int cleanupLogs(Timestamp beforeTime) {
        try {
            int deletedCount = collectionLogRepository.deleteLogsBefore(beforeTime);
            log.info("清理采集日志完成，删除了 {} 条记录", deletedCount);
            return deletedCount;
        } catch (Exception e) {
            log.error("清理采集日志失败", e);
            return 0;
        }
    }

    @Override
    @Transactional
    public int handleTimeoutTasks(int timeoutMinutes) {
        Timestamp timeoutThreshold = Timestamp.valueOf(LocalDateTime.now().minusMinutes(timeoutMinutes));
        List<CollectionLog> timeoutLogs = collectionLogRepository.findTimeoutLogs(timeoutThreshold);
        
        int handledCount = 0;
        for (CollectionLog log : timeoutLogs) {
            log.setEndTime(new Timestamp(System.currentTimeMillis()));
            log.setStatus("FAILED");
            log.setSuccess(false);
            log.setErrorMessage("任务执行超时");
            log.setErrorCode("TIMEOUT");
            collectionLogRepository.save(log);
            handledCount++;
        }
        
        if (handledCount > 0) {
            log.warn("处理了 {} 个超时任务", handledCount);
        }
        
        return handledCount;
    }

    @Override
    public CollectionLog getLogByExecutionId(String executionId) {
        return collectionLogRepository.findByExecutionId(executionId).orElse(null);
    }

    @Override
    @Transactional
    public void updateRetryCount(Long logId, int retryCount) {
        collectionLogRepository.findById(logId).ifPresent(log -> {
            log.setRetryCount(retryCount);
            collectionLogRepository.save(log);
        });
    }

    /**
     * 计算响应时间
     */
    private Long calculateResponseTime(CollectionResult result) {
        if (result.getStartTime() != null && result.getEndTime() != null) {
            return Timestamp.valueOf(result.getEndTime()).getTime() - 
                   Timestamp.valueOf(result.getStartTime()).getTime();
        }
        return null;
    }
}
