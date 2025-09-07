package com.skyeye.collector.service;

import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.entity.CollectionLog;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 采集日志服务接口
 * 
 * @author SkyEye Team
 */
public interface CollectionLogService {

    /**
     * 记录采集日志
     * 
     * @param deviceId 设备ID
     * @param metricName 指标名称
     * @param result 采集结果
     * @return 采集日志
     */
    CollectionLog logCollection(Long deviceId, String metricName, CollectionResult result);

    /**
     * 记录采集开始
     * 
     * @param taskId 任务ID
     * @param deviceId 设备ID
     * @param executionId 执行ID
     * @param metricName 指标名称
     * @param pluginType 插件类型
     * @return 采集日志
     */
    CollectionLog logCollectionStart(Long taskId, Long deviceId, String executionId, 
                                   String metricName, String pluginType);

    /**
     * 更新采集结束
     * 
     * @param logId 日志ID
     * @param result 采集结果
     */
    void logCollectionEnd(Long logId, CollectionResult result);

    /**
     * 记录采集失败
     * 
     * @param logId 日志ID
     * @param errorMessage 错误信息
     * @param errorCode 错误代码
     */
    void logCollectionFailure(Long logId, String errorMessage, String errorCode);

    /**
     * 获取设备的采集日志
     * 
     * @param deviceId 设备ID
     * @param limit 限制数量
     * @return 采集日志列表
     */
    List<CollectionLog> getDeviceLogs(Long deviceId, int limit);

    /**
     * 获取任务的采集日志
     * 
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 采集日志列表
     */
    List<CollectionLog> getTaskLogs(Long taskId, int limit);

    /**
     * 获取失败的采集日志
     * 
     * @param limit 限制数量
     * @return 失败的采集日志列表
     */
    List<CollectionLog> getFailedLogs(int limit);

    /**
     * 获取正在运行的采集日志
     * 
     * @return 正在运行的采集日志列表
     */
    List<CollectionLog> getRunningLogs();

    /**
     * 获取采集统计信息
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 统计信息
     */
    Map<String, Object> getCollectionStatistics(Timestamp startTime, Timestamp endTime);

    /**
     * 获取插件执行统计
     * 
     * @return 插件统计信息
     */
    Map<String, Long> getPluginStatistics();

    /**
     * 获取成功率统计
     * 
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 成功率
     */
    Double getSuccessRate(Timestamp startTime, Timestamp endTime);

    /**
     * 清理过期日志
     * 
     * @param beforeTime 清理时间点
     * @return 清理的数量
     */
    int cleanupLogs(Timestamp beforeTime);

    /**
     * 检查并处理超时任务
     * 
     * @param timeoutMinutes 超时分钟数
     * @return 处理的超时任务数量
     */
    int handleTimeoutTasks(int timeoutMinutes);

    /**
     * 根据执行ID查询日志
     * 
     * @param executionId 执行ID
     * @return 采集日志
     */
    CollectionLog getLogByExecutionId(String executionId);

    /**
     * 更新日志重试次数
     * 
     * @param logId 日志ID
     * @param retryCount 重试次数
     */
    void updateRetryCount(Long logId, int retryCount);
}
