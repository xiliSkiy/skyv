package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 设备分组实体
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Entity
@Table(name = "tb_device_groups")
@Data
@EqualsAndHashCode(callSuper = true)
public class DeviceGroup extends BaseEntity {

    /**
     * 分组名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 分组类型：normal-普通分组, smart-智能分组, dynamic-动态分组
     */
    @Column(name = "type", nullable = false, length = 20)
    private String type;

    /**
     * 分组描述
     */
    @Column(name = "description", length = 500)
    private String description;

    /**
     * 分组规则（智能分组和动态分组使用）
     */
    @Column(name = "rule", columnDefinition = "TEXT")
    private String rule;

    /**
     * 是否重要分组
     */
    @Column(name = "is_important", nullable = false)
    private Boolean isImportant = false;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = true;

    /**
     * 设备数量（冗余字段，用于快速查询）
     */
    @Column(name = "device_count")
    private Integer deviceCount = 0;
} 