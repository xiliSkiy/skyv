package com.skyeye.collector.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.sql.Timestamp;

/**
 * 任务统计实体
 * 
 * @author SkyEye Team
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_task_statistics")
@org.hibernate.annotations.DynamicUpdate
public class TaskStatistics extends BaseEntity {

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;

    /**
     * 统计日期（YYYY-MM-DD格式）
     */
    @Column(name = "stat_date", nullable = false, length = 10)
    private String statDate;

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
     * 总执行时间（毫秒）
     */
    @Column(name = "total_execution_time")
    private Long totalExecutionTime = 0L;

    /**
     * 平均执行时间（毫秒）
     */
    @Column(name = "average_execution_time")
    private Long averageExecutionTime = 0L;

    /**
     * 最短执行时间（毫秒）
     */
    @Column(name = "min_execution_time")
    private Long minExecutionTime;

    /**
     * 最长执行时间（毫秒）
     */
    @Column(name = "max_execution_time")
    private Long maxExecutionTime;

    /**
     * 成功率
     */
    @Column(name = "success_rate")
    private Double successRate = 0.0;

    /**
     * 平均数据采集量
     */
    @Column(name = "average_data_count")
    private Double averageDataCount = 0.0;

    /**
     * 总数据采集量
     */
    @Column(name = "total_data_count")
    private Long totalDataCount = 0L;

    /**
     * 平均质量评分
     */
    @Column(name = "average_quality_score")
    private Double averageQualityScore = 0.0;

    /**
     * 错误类型统计（JSON格式）
     */
    @Column(name = "error_type_stats", columnDefinition = "jsonb")
    private String errorTypeStats;

    /**
     * 设备执行统计（JSON格式）
     */
    @Column(name = "device_execution_stats", columnDefinition = "jsonb")
    private String deviceExecutionStats;

    /**
     * 指标执行统计（JSON格式）
     */
    @Column(name = "metric_execution_stats", columnDefinition = "jsonb")
    private String metricExecutionStats;

    /**
     * 备注
     */
    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
