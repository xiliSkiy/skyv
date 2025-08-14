package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 设备模板实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_device_templates")
@Comment("设备模板表")
@SQLDelete(sql = "UPDATE tb_device_templates SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class DeviceTemplate extends BaseEntity {

    @Comment("模板名称")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Comment("模板编码")
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Comment("设备类型ID")
    @Column(name = "device_type_id")
    private Long deviceTypeId;

    @Comment("协议ID")
    @Column(name = "protocol_id")
    private Long protocolId;

    @Comment("制造商")
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    @Comment("设备型号")
    @Column(name = "model", length = 100)
    private String model;

    @Comment("配置模板JSON")
    @Column(name = "config_template", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configTemplate;

    @Comment("默认设置JSON")
    @Column(name = "default_settings", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String defaultSettings;

    @Comment("指标配置JSON")
    @Column(name = "metrics", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String metrics;

    @Comment("描述")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Comment("是否启用")
    @Column(name = "is_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isEnabled = true;

    @Comment("使用次数")
    @Column(name = "usage_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer usageCount = 0;

    @Comment("删除时间")
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    // 关联关系 - 设备类型
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "device_type_id", insertable = false, updatable = false)
    private DeviceType deviceType;

    // 关联关系 - 协议
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "protocol_id", insertable = false, updatable = false)
    private DeviceProtocol protocol;
} 