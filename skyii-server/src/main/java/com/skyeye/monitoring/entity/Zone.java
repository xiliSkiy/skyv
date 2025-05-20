package com.skyeye.monitoring.entity;

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
 * 监控区域实体类
 */
@Entity
@Table(name = "tb_monitoring_zones")
@SQLDelete(sql = "UPDATE tb_monitoring_zones SET deleted_at = NOW() WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
@Data
public class Zone {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "zone_name", nullable = false)
    private String zoneName;
    
    @Column(name = "zone_code", nullable = false, unique = true)
    private String zoneCode;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "parent_id")
    private Long parentId;
    
    @Column(name = "level")
    private Integer level;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
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