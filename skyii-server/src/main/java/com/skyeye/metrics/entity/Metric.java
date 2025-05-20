package com.skyeye.metrics.entity;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

/**
 * 指标配置实体类
 */
@Entity
@Table(name = "tb_metrics")
@SQLDelete(sql = "UPDATE tb_metrics SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Data
public class Metric {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "metric_name", nullable = false)
    private String metricName;
    
    @Column(name = "metric_key", nullable = false, unique = true)
    private String metricKey;
    
    @Column(name = "metric_type", nullable = false)
    private String metricType;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "applicable_device_type")
    private String applicableDeviceType;
    
    @Column(name = "collection_method", nullable = false)
    private String collectionMethod;
    
    @Column(name = "protocol_config", columnDefinition = "TEXT")
    private String protocolConfig;
    
    @Column(name = "collection_interval", nullable = false)
    private Integer collectionInterval;
    
    @Column(name = "collection_interval_unit")
    private String collectionIntervalUnit;
    
    @Column(name = "data_type")
    private String dataType;
    
    @Column(name = "unit")
    private String unit;
    
    @Column(name = "data_format")
    private String dataFormat;
    
    @Column(name = "data_transform_type")
    private String dataTransformType;
    
    @Column(name = "transform_formula")
    private String transformFormula;
    
    @Column(name = "min_value")
    private String minValue;
    
    @Column(name = "max_value")
    private String maxValue;
    
    @Column(name = "processing_script", columnDefinition = "TEXT")
    private String processingScript;
    
    @Column(name = "enable_cache")
    private Boolean enableCache;
    
    @Column(name = "retention_period")
    private String retentionPeriod;
    
    @Column(name = "threshold_enabled")
    private Boolean thresholdEnabled;
    
    @Column(name = "warning_threshold")
    private String warningThreshold;
    
    @Column(name = "warning_duration")
    private Integer warningDuration;
    
    @Column(name = "critical_threshold")
    private String criticalThreshold;
    
    @Column(name = "critical_duration")
    private Integer criticalDuration;
    
    @Column(name = "notification_groups")
    private String notificationGroups;
    
    @Column(name = "last_collection_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastCollectionTime;
    
    @Column(name = "alert_rule_config", columnDefinition = "TEXT")
    private String alertRuleConfig;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "created_by")
    private Long createdBy;
    
    @Column(name = "created_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    @JsonIgnore
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 