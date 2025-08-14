package com.skyeye.user.dto;

import lombok.Data;

import java.sql.Timestamp;

/**
 * 用户数据传输对象（简化版）
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class UserDto {

    /**
     * 用户ID
     */
    private Long id;

    /**
     * 用户名
     */
    private String username;

    /**
     * 真实姓名
     */
    private String realName;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String phone;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 用户状态
     */
    private Integer status;

    /**
     * 是否为管理员
     */
    private Boolean isAdmin;

    /**
     * 创建时间
     */
    private Timestamp createdAt;

    /**
     * 更新时间
     */
    private Timestamp updatedAt;

    /**
     * 备注
     */
    private String remark;
} 