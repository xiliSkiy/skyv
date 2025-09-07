package com.skyeye.collector.dto;

import lombok.Data;

import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 任务更新请求
 * 
 * @author SkyEye Team
 */
@Data
public class TaskUpdateRequest {

    /**
     * 任务名称
     */
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    private String name;

    /**
     * 任务描述
     */
    @Size(max = 1000, message = "任务描述长度不能超过1000个字符")
    private String description;

    /**
     * 采集器ID
     */
    private Long collectorId;

    /**
     * 调度类型
     */
    private String scheduleType;

    /**
     * 调度配置
     */
    private Map<String, Object> scheduleConfig;

    /**
     * 目标设备ID列表
     */
    private List<Long> targetDevices;

    /**
     * 指标配置列表
     */
    private List<MetricConfig> metricsConfig;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 优先级
     */
    private Integer priority;

    /**
     * 是否启用重试
     */
    private Boolean enableRetry;

    /**
     * 重试次数
     */
    private Integer retryTimes;

    /**
     * 重试间隔（毫秒）
     */
    private Long retryInterval;

    /**
     * 超时时间（秒）
     */
    private Integer timeout;

    /**
     * 并发执行数
     */
    private Integer maxConcurrency;

    /**
     * 任务标签
     */
    private List<String> tags;

    /**
     * 任务参数
     */
    private Map<String, Object> parameters;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 生效时间
     */
    private Timestamp effectiveTime;

    /**
     * 失效时间
     */
    private Timestamp expireTime;

    /**
     * 备注
     */
    private String remarks;
}
