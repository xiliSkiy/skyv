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
 * 设备协议实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_device_protocols")
@Comment("设备协议表")
@SQLDelete(sql = "UPDATE tb_device_protocols SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@Where(clause = "deleted_at IS NULL")
public class DeviceProtocol extends BaseEntity {

    @Comment("协议名称")
    @Column(name = "name", nullable = false, length = 50, unique = true)
    private String name;

    @Comment("协议编码")
    @Column(name = "code", nullable = false, length = 50, unique = true)
    private String code;

    @Comment("协议版本")
    @Column(name = "version", length = 20)
    private String version;

    @Comment("协议描述")
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Comment("默认端口")
    @Column(name = "port")
    private Integer port;

    @Comment("配置参数模式JSON")
    @Column(name = "config_schema", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String configSchema;

    @Comment("是否启用")
    @Column(name = "is_enabled", columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isEnabled = true;

    @Comment("使用次数")
    @Column(name = "usage_count", columnDefinition = "INTEGER DEFAULT 0")
    private Integer usageCount = 0;

    @Comment("删除时间")
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
} 