package com.skyeye.collector.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.sql.Timestamp;

/**
 * 采集日志实体
 * 
 * @author SkyEye Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_collection_logs")
@org.hibernate.annotations.DynamicUpdate
public class CollectionLog extends BaseEntity {

    /**
     * 任务ID
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 执行ID（用于关联一次批量执行）
     */
    @Column(name = "execution_id", length = 100)
    private String executionId;

    /**
     * 指标名称
     */
    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    /**
     * 采集插件类型
     */
    @Column(name = "plugin_type", length = 50)
    private String pluginType;

    /**
     * 开始时间
     */
    @Column(name = "start_time", nullable = false)
    private Timestamp startTime;

    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private Timestamp endTime;

    /**
     * 执行状态
     */
    @Column(name = "status", nullable = false, length = 20)
    private String status; // RUNNING, COMPLETED, FAILED, CANCELLED

    /**
     * 是否成功
     */
    @Column(name = "success")
    private Boolean success;

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 错误代码
     */
    @Column(name = "error_code", length = 50)
    private String errorCode;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time")
    private Long responseTime;

    /**
     * 数据质量评分
     */
    @Column(name = "quality_score")
    private Integer qualityScore;

    /**
     * 采集数据量
     */
    @Column(name = "data_count")
    private Integer dataCount;

    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 0;

    /**
     * 采集配置（JSON格式）
     */
    @Column(name = "config_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configData;

    /**
     * 额外信息（JSON格式）
     */
    @Column(name = "extra_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String extraData;

    /**
     * 创建警告日志
     */
    public static CollectionLog warn(String message, Long taskId) {
        CollectionLog log = new CollectionLog();
        log.setTaskId(taskId);
        log.setStatus("WARNING");
        log.setErrorMessage(message);
        log.setStartTime(new Timestamp(System.currentTimeMillis()));
        return log;
    }

    /**
     * 创建警告日志（带详细信息）
     */
    public static CollectionLog warn(String message, Long taskId, String pluginType, String metricName) {
        CollectionLog log = new CollectionLog();
        log.setTaskId(taskId);
        log.setPluginType(pluginType);
        log.setMetricName(metricName);
        log.setStatus("WARNING");
        log.setErrorMessage(message);
        log.setStartTime(new Timestamp(System.currentTimeMillis()));
        return log;
    }

    /**
     * 创建错误日志
     */
    public static CollectionLog error(String message, Long taskId, String pluginType, Exception exception) {
        CollectionLog log = new CollectionLog();
        log.setTaskId(taskId);
        log.setPluginType(pluginType);
        log.setStatus("FAILED");
        log.setSuccess(false);
        log.setErrorMessage(message + ": " + exception.getMessage());
        log.setStartTime(new Timestamp(System.currentTimeMillis()));
        return log;
    }

    /**
     * 创建信息日志
     */
    public static CollectionLog info(String message, Long taskId, int successCount, int totalCount, long responseTime) {
        CollectionLog log = new CollectionLog();
        log.setTaskId(taskId);
        log.setStatus("COMPLETED");
        log.setSuccess(true);
        log.setErrorMessage(message);
        log.setDataCount(successCount);
        log.setResponseTime(responseTime);
        log.setStartTime(new Timestamp(System.currentTimeMillis()));
        log.setEndTime(new Timestamp(System.currentTimeMillis()));
        return log;
    }
}
