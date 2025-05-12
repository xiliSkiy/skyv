package com.skyeye.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * JWT响应DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    
    /**
     * 访问令牌
     */
    private String token;
    
    /**
     * 令牌类型
     */
    private String type = "Bearer";
    
    /**
     * 用户ID
     */
    private Long id;
    
    /**
     * 用户名
     */
    private String username;
    
    /**
     * 用户邮箱
     */
    private String email;
    
    /**
     * 用户角色列表
     */
    private List<String> roles;
    
    public JwtResponse(String token, Long id, String username, String email, List<String> roles) {
        this.token = token;
        this.id = id;
        this.username = username;
        this.email = email;
        this.roles = roles;
    }
} 