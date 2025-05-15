package com.skyeye.common.entity;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 系统设置实体类
 */
@Data
@Entity
@Table(name = "tb_settings")
public class SystemSetting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 设置键
     */
    @Column(name = "setting_key", nullable = false, unique = true)
    private String settingKey;
    
    /**
     * 设置值
     */
    @Column(name = "setting_value", columnDefinition = "TEXT")
    private String settingValue;
    
    /**
     * 值类型：string,number,boolean,json
     */
    @Column(name = "setting_type", length = 20)
    private String settingType;
    
    /**
     * 描述
     */
    @Column(name = "description", length = 255)
    private String description;
    
    /**
     * 创建时间
     */
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}