package com.skyeye.auth.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 角色实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_roles")
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    @Column(name = "role_name", nullable = false, length = 50)
    private String name;

    /**
     * 角色编码
     */
    @Column(name = "role_code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 角色描述
     */
    @Column(name = "description", length = 255)
    private String description;
} 