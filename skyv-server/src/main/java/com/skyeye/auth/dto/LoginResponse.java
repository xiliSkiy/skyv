package com.skyeye.auth.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 登录响应DTO
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@Builder
public class LoginResponse {

    /**
     * 访问Token
     */
    private String accessToken;

    /**
     * 刷新Token
     */
    private String refreshToken;

    /**
     * Token类型
     */
    private String tokenType = "Bearer";

    /**
     * Token过期时间（毫秒）
     */
    private Long expiresIn;

    /**
     * 用户信息
     */
    private UserInfo userInfo;

    /**
     * 用户信息内部类
     */
    @Data
    @Builder
    public static class UserInfo {
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
         * 是否为管理员
         */
        private Boolean isAdmin;

        /**
         * 用户角色列表
         */
        private List<String> roles;

        /**
         * 用户权限列表
         */
        private List<String> permissions;

        /**
         * 最后登录时间
         */
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        private LocalDateTime lastLoginTime;

        /**
         * 最后登录IP
         */
        private String lastLoginIp;
    }
} 