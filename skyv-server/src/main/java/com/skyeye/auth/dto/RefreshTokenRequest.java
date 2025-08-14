package com.skyeye.auth.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * 刷新Token请求DTO
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class RefreshTokenRequest {

    /**
     * 刷新Token
     */
    @NotBlank(message = "刷新Token不能为空")
    private String refreshToken;
} 