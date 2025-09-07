package com.skyeye.collector.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 数据采集结果
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class CollectionResult {

    /**
     * 采集是否成功
     */
    private boolean success;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 采集到的指标数据
     * key: 指标名称, value: 指标值
     */
    private Map<String, Object> metrics;

    /**
     * 错误消息（采集失败时）
     */
    private String errorMessage;

    /**
     * 错误代码
     */
    private String errorCode;

    /**
     * 采集时间戳
     */
    private long timestamp;

    /**
     * 采集开始时间
     */
    private LocalDateTime startTime;

    /**
     * 采集结束时间
     */
    private LocalDateTime endTime;

    /**
     * 采集耗时（毫秒）
     */
    private long duration;

    /**
     * 数据质量评分（0-100）
     */
    private int qualityScore;

    /**
     * 插件类型
     */
    private String pluginType;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 采集会话ID
     */
    private String sessionId;

    /**
     * 重试次数
     */
    private int retryCount;

    /**
     * 数据来源
     */
    private String dataSource;

    /**
     * 原始数据
     */
    private String rawData;

    /**
     * 附加信息
     */
    private Map<String, Object> metadata;

    /**
     * 采集状态
     */
    private CollectionStatus status;

    /**
     * 创建成功的采集结果
     */
    public static CollectionResult success(Map<String, Object> metrics) {
        return CollectionResult.builder()
                .success(true)
                .metrics(metrics)
                .timestamp(System.currentTimeMillis())
                .endTime(LocalDateTime.now())
                .qualityScore(100)
                .status(CollectionStatus.SUCCESS)
                .build();
    }

    /**
     * 创建失败的采集结果
     */
    public static CollectionResult failure(String errorMessage) {
        return CollectionResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .endTime(LocalDateTime.now())
                .qualityScore(0)
                .status(CollectionStatus.FAILED)
                .build();
    }

    /**
     * 创建失败的采集结果（带错误代码）
     */
    public static CollectionResult failure(String errorCode, String errorMessage) {
        return CollectionResult.builder()
                .success(false)
                .errorCode(errorCode)
                .errorMessage(errorMessage)
                .timestamp(System.currentTimeMillis())
                .endTime(LocalDateTime.now())
                .qualityScore(0)
                .status(CollectionStatus.FAILED)
                .build();
    }

    /**
     * 创建超时的采集结果
     */
    public static CollectionResult timeout() {
        return CollectionResult.builder()
                .success(false)
                .errorCode("TIMEOUT")
                .errorMessage("采集超时")
                .timestamp(System.currentTimeMillis())
                .endTime(LocalDateTime.now())
                .qualityScore(0)
                .status(CollectionStatus.TIMEOUT)
                .build();
    }

    /**
     * 采集状态枚举
     */
    public enum CollectionStatus {
        SUCCESS,    // 成功
        FAILED,     // 失败
        TIMEOUT,    // 超时
        CANCELLED,  // 取消
        PARTIAL     // 部分成功
    }
}
