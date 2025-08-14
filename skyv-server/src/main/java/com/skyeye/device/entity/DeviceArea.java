package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.List;
import java.util.Map;

/**
 * 设备区域实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_device_areas")
@Comment("设备区域表")
public class DeviceArea extends BaseEntity {

    @Comment("区域名称")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Comment("区域编码")
    @Column(name = "code", length = 50)
    private String code;

    @Comment("父区域ID")
    @Column(name = "parent_id")
    private Long parentId;

    @Comment("层级")
    @Column(name = "level", columnDefinition = "INTEGER DEFAULT 0")
    private Integer level = 0;

    @Comment("完整路径")
    @Column(name = "full_path", length = 500)
    private String fullPath;

    @Comment("区域描述")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Comment("位置信息JSON(经纬度等)")
    @Column(name = "location_info", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> locationInfo;

    @Comment("排序")
    @Column(name = "sort_order", columnDefinition = "INTEGER DEFAULT 0")
    private Integer sortOrder = 0;

    @Comment("创建人")
    @Column(name = "created_by")
    private Long createdBy;

    @Comment("更新人")
    @Column(name = "updated_by")
    private Long updatedBy;

    // 关联关系 - 子区域
    @OneToMany(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private List<DeviceArea> children;

    // 关联关系 - 父区域
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", insertable = false, updatable = false)
    private DeviceArea parent;
} 