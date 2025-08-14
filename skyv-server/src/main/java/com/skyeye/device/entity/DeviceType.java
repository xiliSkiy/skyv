package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;

/**
 * 设备类型实体
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_device_types")
public class DeviceType extends BaseEntity {

    /**
     * 类型名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 类型编码
     */
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    /**
     * 父类型ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 图标
     */
    @Column(name = "icon", length = 100)
    private String icon;

    /**
     * 支持的协议列表
     */
    @Column(name = "protocol_types", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private List<String> protocols;

    /**
     * 排序
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 是否启用
     */
    @Column(name = "is_enabled")
    private Boolean isEnabled = true;

    /**
     * 设备数量
     */
    @Column(name = "device_count")
    private Integer deviceCount = 0;

    /**
     * 子类型列表（不持久化到数据库，用于前端树形结构展示）
     */
    @Transient
    private List<DeviceType> children;

    /**
     * 父类型（不持久化到数据库）
     */
    @Transient
    private DeviceType parent;
} 