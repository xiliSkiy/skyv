package com.skyeye.metrics.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 指标采集历史实体类
 */
@Entity
@Table(name = "tb_metric_history")
@Data
public class MetricHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "metric_id", nullable = false)
    private Long metricId;
    
    @Column(name = "device_id")
    private Long deviceId;
    
    @Column(name = "collector_id")
    private Long collectorId;
    
    @Column(name = "collection_time", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectionTime;
    
    @Column(name = "raw_value", columnDefinition = "TEXT")
    private String rawValue;
    
    @Column(name = "processed_value")
    private String processedValue;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "status_info")
    private String statusInfo;
    
    @Column(name = "collection_duration")
    private Long collectionDuration;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "metric_id", insertable = false, updatable = false)
    private Metric metric;
} 