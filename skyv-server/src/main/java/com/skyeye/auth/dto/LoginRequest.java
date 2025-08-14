package com.skyeye.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求DTO
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class LoginRequest {

    /**
     * 用户名
     */
    @NotBlank(message = "用户名不能为空")
    @Size(min = 3, max = 50, message = "用户名长度必须在3-50个字符之间")
    private String username;

    /**
     * 密码
     */
    @NotBlank(message = "密码不能为空")
    @Size(min = 6, max = 50, message = "密码长度必须在6-50个字符之间")
    private String password;

    /**
     * 验证码（可选）
     */
    private String captcha;

    /**
     * 验证码Key（可选）
     */
    private String captchaKey;

    /**
     * 记住我
     */
    private Boolean rememberMe = false;
} 