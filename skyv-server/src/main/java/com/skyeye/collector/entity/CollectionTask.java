package com.skyeye.collector.entity;

import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 采集任务实体
 * 
 * @author SkyEye Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_collection_tasks")
@org.hibernate.annotations.DynamicUpdate
public class CollectionTask extends BaseEntity {

    /**
     * 任务名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 任务描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 采集器ID
     */
    @Column(name = "collector_id")
    private Long collectorId;

    /**
     * 调度类型
     * SIMPLE: 简单调度（固定间隔）
     * CRON: Cron表达式调度
     * EVENT: 事件触发调度
     */
    @Column(name = "schedule_type", nullable = false, length = 20)
    private String scheduleType;

    /**
     * 调度配置（JSON格式）
     */
    @Column(name = "schedule_config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> scheduleConfig;

    /**
     * 目标设备ID列表
     */
    @Column(name = "target_devices", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<Long> targetDevices;

    /**
     * 指标配置列表
     */
    @Column(name = "metrics_config", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<MetricConfig> metricsConfig;

    /**
     * 任务状态
     * 1: 启用
     * 0: 禁用
     * 2: 暂停
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 优先级（数值越小优先级越高）
     */
    @Column(name = "priority")
    private Integer priority = 5;

    /**
     * 是否启用重试
     */
    @Column(name = "enable_retry")
    private Boolean enableRetry = true;

    /**
     * 重试次数
     */
    @Column(name = "retry_times")
    private Integer retryTimes = 3;

    /**
     * 重试间隔（毫秒）
     */
    @Column(name = "retry_interval")
    private Long retryInterval = 1000L;

    /**
     * 超时时间（秒）
     */
    @Column(name = "timeout")
    private Integer timeout = 300;

    /**
     * 并发执行数
     */
    @Column(name = "max_concurrency")
    private Integer maxConcurrency = 5;

    /**
     * 上次执行时间
     */
    @Column(name = "last_execution_time")
    private Timestamp lastExecutionTime;

    /**
     * 下次执行时间
     */
    @Column(name = "next_execution_time")
    private Timestamp nextExecutionTime;

    /**
     * 执行次数
     */
    @Column(name = "execution_count")
    private Long executionCount = 0L;

    /**
     * 成功次数
     */
    @Column(name = "success_count")
    private Long successCount = 0L;

    /**
     * 失败次数
     */
    @Column(name = "failure_count")
    private Long failureCount = 0L;

    /**
     * 平均执行时间（毫秒）
     */
    @Column(name = "average_execution_time")
    private Long averageExecutionTime;

    /**
     * 最后执行状态
     */
    @Column(name = "last_execution_status", length = 20)
    private String lastExecutionStatus;

    /**
     * 最后执行错误信息
     */
    @Column(name = "last_execution_error", columnDefinition = "TEXT")
    private String lastExecutionError;

    /**
     * 任务标签（JSON格式）
     */
    @Column(name = "tags", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> tags;

    /**
     * 任务参数（JSON格式）
     */
    @Column(name = "parameters", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> parameters;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    /**
     * 生效时间
     */
    @Column(name = "effective_time")
    private Timestamp effectiveTime;

    /**
     * 失效时间
     */
    @Column(name = "expire_time")
    private Timestamp expireTime;

    /**
     * 备注
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
