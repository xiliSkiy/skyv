package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集任务实体类
 */
@Data
@Entity
@Table(name = "tb_collection_tasks")
@EqualsAndHashCode(callSuper = true)
public class CollectionTask extends BaseEntity {
    
    /**
     * 批次ID
     */
    @Column(name = "batch_id", nullable = false)
    private Long batchId;
    
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
     * 设备名称
     */
    @Column(name = "device_name")
    private String deviceName;
    
    /**
     * 设备类型
     */
    @Column(name = "device_type")
    private String deviceType;
    
    /**
     * 指标名称
     */
    @Column(name = "metric_name")
    private String metricName;
    
    /**
     * 状态：PENDING, RUNNING, COMPLETED, FAILED, CANCELLED
     */
    @Column(name = "status", nullable = false)
    private String status = "PENDING";
    
    /**
     * 目标地址
     */
    @Column(name = "target_address")
    private String targetAddress;
    
    /**
     * 目标端口
     */
    @Column(name = "target_port")
    private Integer targetPort;
    
    /**
     * 采集类型
     */
    @Column(name = "collect_type")
    private String collectType;
    
    /**
     * 采集参数(JSON格式)
     */
    @Column(name = "collect_params", columnDefinition = "TEXT")
    private String collectParams;
    
    /**
     * 认证信息(加密)
     */
    @Column(name = "auth_info", columnDefinition = "TEXT")
    private String authInfo;
    
    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 执行时间(毫秒)
     */
    @Column(name = "execution_time")
    private Long executionTime;
    
    /**
     * 结果值
     */
    @Column(name = "result")
    private String result;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 超时时间(毫秒)
     */
    @Column(name = "timeout")
    private Integer timeout = 30000;
    
    /**
     * 重试次数
     */
    @Column(name = "retry_count")
    private Integer retryCount = 3;
    
    /**
     * 已重试次数
     */
    @Column(name = "retried_count")
    private Integer retriedCount = 0;
    
    /**
     * 批次关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "batch_id", insertable = false, updatable = false)
    private TaskBatch taskBatch;
} 