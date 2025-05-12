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
@Table(name = "roles")
public class Role extends BaseEntity {

    /**
     * 角色名称
     */
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * 角色编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 角色描述
     */
    @Column(length = 200)
    private String description;
} 