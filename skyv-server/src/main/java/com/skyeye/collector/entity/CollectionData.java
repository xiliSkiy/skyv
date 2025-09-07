package com.skyeye.collector.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * 采集数据实体
 * 
 * @author SkyEye Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_collection_data")
@org.hibernate.annotations.DynamicUpdate
public class CollectionData extends BaseEntity {

    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 任务ID
     */
    @Column(name = "task_id")
    private Long taskId;

    /**
     * 指标名称
     */
    @Column(name = "metric_name", nullable = false, length = 100)
    private String metricName;

    /**
     * 指标类型
     */
    @Column(name = "metric_type", length = 50)
    private String metricType;

    /**
     * 指标数值（用于数值型指标）
     */
    @Column(name = "metric_value", precision = 20, scale = 6)
    private BigDecimal metricValue;

    /**
     * 原始数据（JSON格式）
     */
    @Column(name = "metric_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metricData;

    /**
     * 采集时间
     */
    @Column(name = "collected_at", nullable = false)
    private Timestamp collectedAt;

    /**
     * 数据质量评分
     */
    @Column(name = "quality_score")
    private Integer qualityScore;

    /**
     * 采集插件类型
     */
    @Column(name = "plugin_type", length = 50)
    private String pluginType;

    /**
     * 会话ID
     */
    @Column(name = "session_id", length = 100)
    private String sessionId;

    /**
     * 响应时间（毫秒）
     */
    @Column(name = "response_time")
    private Long responseTime;

    /**
     * 数据状态
     */
    @Column(name = "status")
    private Integer status = 1; // 1:正常 0:异常

    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    /**
     * 标签（JSON格式）
     */
    @Column(name = "tags", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String tags;

    /**
     * 数据版本
     */
    @Column(name = "data_version")
    private Integer dataVersion = 1;

    /**
     * 过期时间
     */
    @Column(name = "expires_at")
    private Timestamp expiresAt;
}
