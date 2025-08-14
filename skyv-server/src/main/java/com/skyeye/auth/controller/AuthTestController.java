package com.skyeye.auth.controller;

import com.skyeye.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 认证测试控制器
 * 用于测试前后端认证功能对接
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/auth/test")
public class AuthTestController {

    /**
     * 测试公开接口（无需认证）
     * 
     * @return 测试响应
     */
    @GetMapping("/public")
    public ApiResponse<Map<String, Object>> testPublicEndpoint() {
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个公开接口，无需认证");
        data.put("timestamp", LocalDateTime.now());
        data.put("status", "success");
        
        return ApiResponse.success("公开接口测试成功", data);
    }

    /**
     * 测试需要认证的接口
     * 
     * @return 测试响应
     */
    @GetMapping("/protected")
    public ApiResponse<Map<String, Object>> testProtectedEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个受保护的接口，需要认证");
        data.put("timestamp", LocalDateTime.now());
        data.put("username", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        data.put("authenticated", authentication.isAuthenticated());
        
        return ApiResponse.success("受保护接口测试成功", data);
    }

    /**
     * 测试管理员接口
     * 
     * @return 测试响应
     */
    @GetMapping("/admin")
    public ApiResponse<Map<String, Object>> testAdminEndpoint() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> data = new HashMap<>();
        data.put("message", "这是一个管理员接口，需要管理员权限");
        data.put("timestamp", LocalDateTime.now());
        data.put("username", authentication.getName());
        data.put("authorities", authentication.getAuthorities());
        
        return ApiResponse.success("管理员接口测试成功", data);
    }

    /**
     * 获取用户信息测试接口
     * 
     * @return 用户信息
     */
    @GetMapping("/userinfo")
    public ApiResponse<Map<String, Object>> getUserInfo() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("username", authentication.getName());
        userInfo.put("authenticated", authentication.isAuthenticated());
        userInfo.put("authorities", authentication.getAuthorities());
        userInfo.put("principal", authentication.getPrincipal());
        userInfo.put("details", authentication.getDetails());
        
        return ApiResponse.success("获取用户信息成功", userInfo);
    }
} 