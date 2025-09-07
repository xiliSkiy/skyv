package com.skyeye.collector.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务执行结果DTO
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class TaskExecutionResult {

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 执行ID
     */
    private String executionId;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 消息
     */
    private String message;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行时长（毫秒）
     */
    private Long executionTime;

    /**
     * 执行进度（百分比）
     */
    private Integer progress;

    /**
     * 采集的数据量
     */
    private Integer dataCount;

    /**
     * 成功采集的数据量
     */
    private Integer successDataCount;

    /**
     * 失败采集的数据量
     */
    private Integer failedDataCount;

    /**
     * 错误信息
     */
    private String errorMessage;

    /**
     * 错误详情
     */
    private String errorDetails;

    /**
     * 重试次数
     */
    private Integer retryCount;

    /**
     * 最大重试次数
     */
    private Integer maxRetryTimes;

    /**
     * 下次重试时间
     */
    private LocalDateTime nextRetryTime;

    /**
     * 执行设备列表
     */
    private List<DeviceExecutionResult> deviceResults;

    /**
     * 执行指标列表
     */
    private List<MetricExecutionResult> metricResults;

    /**
     * 执行统计信息
     */
    private Map<String, Object> statistics;

    /**
     * 执行日志
     */
    private List<String> executionLogs;

    /**
     * 备注
     */
    private String remarks;

    /**
     * 设备执行结果
     */
    @Data
    @Builder
    public static class DeviceExecutionResult {
        private Long deviceId;
        private String deviceName;
        private String status;
        private String result;
        private Long executionTime;
        private Integer dataCount;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }

    /**
     * 指标执行结果
     */
    @Data
    @Builder
    public static class MetricExecutionResult {
        private String metricName;
        private String metricType;
        private String status;
        private String result;
        private Long executionTime;
        private Integer dataCount;
        private String errorMessage;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
    }
}
