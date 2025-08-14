package com.skyeye.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;

/**
 * 权限实体类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_permissions", indexes = {
    @Index(name = "idx_permissions_code", columnList = "code"),
    @Index(name = "idx_permissions_resource", columnList = "resource")
})
@EntityListeners(AuditingEntityListener.class)
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 权限名称
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    /**
     * 权限代码，唯一
     */
    @Column(name = "code", unique = true, nullable = false, length = 100)
    private String code;

    /**
     * 权限描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 资源标识
     */
    @Column(name = "resource", length = 100)
    private String resource;

    /**
     * 操作类型
     */
    @Column(name = "action", length = 50)
    private String action;

    /**
     * 权限类型：MENU-菜单，BUTTON-按钮，API-接口
     */
    @Column(name = "type", length = 20)
    private String type;

    /**
     * 权限状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 是否为系统内置权限
     */
    @Column(name = "is_system", nullable = false)
    private Boolean isSystem = false;

    /**
     * 排序号
     */
    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    /**
     * 创建时间
     */
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Timestamp createdAt;

    /**
     * 更新时间
     */
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;

    /**
     * 软删除标记
     */
    @Column(name = "deleted_at")
    private Timestamp deletedAt;

    /**
     * 检查权限是否启用
     * 
     * @return true如果权限启用
     */
    public boolean isEnabled() {
        return status == 1 && deletedAt == null;
    }
} 