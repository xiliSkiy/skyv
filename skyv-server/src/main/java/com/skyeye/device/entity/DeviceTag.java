package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;

/**
 * 设备标签实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_device_tags")
@Comment("设备标签表")
public class DeviceTag extends BaseEntity {

    @Comment("标签名称")
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Comment("标签颜色")
    @Column(name = "color", length = 7, columnDefinition = "VARCHAR(7) DEFAULT '#409EFF'")
    private String color = "#409EFF";

    @Comment("标签描述")
    @Column(name = "description", length = 200)
    private String description;

    @Comment("创建人")
    @Column(name = "created_by")
    private Long createdBy;

    @Comment("使用次数")
    @Column(name = "usage_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer usageCount = 0;
} 