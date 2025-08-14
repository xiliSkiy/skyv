package com.skyeye.user.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * 用户实体类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Entity
@Table(name = "tb_users", indexes = {
    @Index(name = "idx_users_username", columnList = "username"),
    @Index(name = "idx_users_email", columnList = "email"),
    @Index(name = "idx_users_status", columnList = "status")
})
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 用户名，唯一
     */
    @Column(name = "username", unique = true, nullable = false, length = 50)
    private String username;

    /**
     * 密码，加密存储
     */
    @JsonIgnore
    @Column(name = "password", nullable = false, length = 255)
    private String password;

    /**
     * 真实姓名
     */
    @Column(name = "real_name", length = 100)
    private String realName;

    /**
     * 邮箱
     */
    @Column(name = "email", length = 100)
    private String email;

    /**
     * 手机号
     */
    @Column(name = "phone", length = 20)
    private String phone;

    /**
     * 头像URL
     */
    @Column(name = "avatar", length = 500)
    private String avatar;

    /**
     * 用户状态：1-正常，0-禁用，2-锁定
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 是否为系统管理员
     */
    @Column(name = "is_admin", nullable = false)
    private Boolean isAdmin = false;

    /**
     * 最后登录时间
     */
    @Column(name = "last_login_time")
    private LocalDateTime lastLoginTime;

    /**
     * 最后登录IP
     */
    @Column(name = "last_login_ip", length = 50)
    private String lastLoginIp;

    /**
     * 登录失败次数
     */
    @Column(name = "login_fail_count", nullable = false)
    private Integer loginFailCount = 0;

    /**
     * 账户锁定时间
     */
    @Column(name = "locked_time")
    private LocalDateTime lockedTime;

    /**
     * 密码最后修改时间
     */
    @Column(name = "password_changed_time")
    private LocalDateTime passwordChangedTime;

    /**
     * 备注
     */
    @Column(name = "remark", length = 500)
    private String remark;

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
     * 用户角色关联
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
        name = "tb_user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    /**
     * 检查用户是否被锁定
     * 
     * @return true如果用户被锁定
     */
    public boolean isLocked() {
        return status == 2 || (lockedTime != null && lockedTime.isAfter(LocalDateTime.now()));
    }

    /**
     * 检查用户是否被禁用
     * 
     * @return true如果用户被禁用
     */
    public boolean isDisabled() {
        return status == 0;
    }

    /**
     * 检查用户是否正常
     * 
     * @return true如果用户状态正常
     */
    public boolean isActive() {
        return status == 1 && !isLocked() && deletedAt == null;
    }

    /**
     * 重置登录失败次数
     */
    public void resetLoginFailCount() {
        this.loginFailCount = 0;
        this.lockedTime = null;
    }

    /**
     * 增加登录失败次数
     */
    public void incrementLoginFailCount() {
        this.loginFailCount++;
        
        // 连续失败5次锁定账户30分钟
        if (this.loginFailCount >= 5) {
            this.status = 2;
            this.lockedTime = LocalDateTime.now().plusMinutes(30);
        }
    }

    /**
     * 更新最后登录信息
     * 
     * @param loginIp 登录IP
     */
    public void updateLastLogin(String loginIp) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = loginIp;
        resetLoginFailCount();
    }
} 