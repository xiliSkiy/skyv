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
 * 采集器配置实体类
 */
@Entity
@Table(name = "tb_collectors")
@SQLDelete(sql = "UPDATE tb_collectors SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Data
public class Collector {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "collector_name", nullable = false)
    private String collectorName;
    
    @Column(name = "collector_type", nullable = false)
    private String collectorType;
    
    @Column(name = "host")
    private String host;
    
    @Column(name = "port")
    private Integer port;
    
    @Column(name = "is_main")
    private Boolean isMain;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "config_params", columnDefinition = "TEXT")
    private String configParams;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "status_info")
    private String statusInfo;
    
    @Column(name = "last_heartbeat")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime lastHeartbeat;
    
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