package com.skyeye.user.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.util.HashSet;
import java.util.Set;

/**
 * 角色实体类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_roles", indexes = {
    @Index(name = "idx_roles_name", columnList = "name"),
    @Index(name = "idx_roles_code", columnList = "code")
})
@EntityListeners(AuditingEntityListener.class)
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 角色名称，唯一
     */
    @Column(name = "name", unique = true, nullable = false, length = 50)
    private String name;

    /**
     * 角色代码，唯一
     */
    @Column(name = "code", unique = true, nullable = false, length = 50)
    private String code;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 200)
    private String description;

    /**
     * 角色状态：1-启用，0-禁用
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 是否为系统内置角色
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
     * 角色权限关联
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tb_role_permissions",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions = new HashSet<>();

    /**
     * 检查角色是否启用
     * 
     * @return true如果角色启用
     */
    public boolean isEnabled() {
        return status == 1 && deletedAt == null;
    }
} 