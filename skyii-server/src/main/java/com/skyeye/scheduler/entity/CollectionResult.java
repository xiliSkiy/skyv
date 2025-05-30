package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集结果实体类
 */
@Data
@Entity
@Table(name = "tb_collection_results")
@EqualsAndHashCode(callSuper = true)
public class CollectionResult extends BaseEntity {
    
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
     * 指标ID
     */
    @Column(name = "metric_id", nullable = false)
    private Long metricId;
    
    /**
     * 采集器ID
     */
    @Column(name = "collector_id", nullable = false)
    private Long collectorId;
    
    /**
     * 指标值
     */
    @Column(name = "metric_value")
    private String metricValue;
    
    /**
     * 指标单位
     */
    @Column(name = "metric_unit")
    private String metricUnit;
    
    /**
     * 采集时间
     */
    @Column(name = "collection_time", nullable = false)
    private LocalDateTime collectionTime;
    
    /**
     * 状态：SUCCESS, ERROR
     */
    @Column(name = "status", nullable = false)
    private String status;
    
    /**
     * 执行时间(毫秒)
     */
    @Column(name = "execution_time")
    private Long executionTime;
    
    /**
     * 额外数据(JSON格式)
     */
    @Column(name = "additional_data", columnDefinition = "TEXT")
    private String additionalData;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
} 