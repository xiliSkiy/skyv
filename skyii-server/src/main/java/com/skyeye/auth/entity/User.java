package com.skyeye.auth.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    /**
     * 用户名
     */
    @Column(nullable = false, unique = true, length = 50)
    private String username;

    /**
     * 密码
     */
    @Column(nullable = false)
    private String password;

    /**
     * 姓名
     */
    @Column(length = 50)
    private String name;

    /**
     * 邮箱
     */
    @Column(unique = true, length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(length = 20)
    private String phone;

    /**
     * 头像
     */
    @Column(length = 200)
    private String avatar;

    /**
     * 账号状态（0-禁用，1-启用）
     */
    @Column(nullable = false)
    private Integer status = 1;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private java.time.LocalDateTime lastLoginTime;

    /**
     * 用户角色
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();
} 