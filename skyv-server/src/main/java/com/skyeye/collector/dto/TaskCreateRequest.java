package com.skyeye.collector.dto;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 任务创建请求
 * 
 * @author SkyEye Team
 */
@Data
public class TaskCreateRequest {

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
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
    @NotBlank(message = "调度类型不能为空")
    private String scheduleType;

    /**
     * 调度配置
     */
    @NotNull(message = "调度配置不能为空")
    private Map<String, Object> scheduleConfig;

    /**
     * 目标设备ID列表
     */
    @NotEmpty(message = "目标设备不能为空")
    private List<Long> targetDevices;

    /**
     * 指标配置列表 (可选，如果为空则自动生成默认指标配置)
     */
    private List<MetricConfig> metricsConfig;

    /**
     * 优先级
     */
    private Integer priority = 5;

    /**
     * 是否启用重试
     */
    private Boolean enableRetry = true;

    /**
     * 重试次数
     */
    private Integer retryTimes = 3;

    /**
     * 重试间隔（毫秒）
     */
    private Long retryInterval = 1000L;

    /**
     * 超时时间（秒）
     */
    private Integer timeout = 300;

    /**
     * 并发执行数
     */
    private Integer maxConcurrency = 5;

    /**
     * 任务标签
     */
    private List<String> tags;

    /**
     * 任务参数
     */
    private Map<String, Object> parameters;

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
