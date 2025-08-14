package com.skyeye.auth.controller;

import com.skyeye.auth.dto.LoginRequest;
import com.skyeye.auth.dto.LoginResponse;
import com.skyeye.auth.dto.RefreshTokenRequest;
import com.skyeye.auth.service.AuthService;
import com.skyeye.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @param httpRequest HTTP请求
     * @return 登录响应
     */
    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request,
                                          HttpServletRequest httpRequest) {
        log.info("User login attempt: {}", request.getUsername());
        
        LoginResponse response = authService.login(request, httpRequest);
        
        log.info("User login successful: {}", request.getUsername());
        return ApiResponse.success("登录成功", response);
    }

    /**
     * 用户登出
     * 
     * @param httpRequest HTTP请求
     * @return 响应结果
     */
    @PostMapping("/logout")
    public ApiResponse<Void> logout(HttpServletRequest httpRequest) {
        log.info("User logout request");
        
        authService.logout(httpRequest);
        
        log.info("User logout successful");
        return ApiResponse.<Void>success("登出成功", null);
    }

    /**
     * 刷新Token
     * 
     * @param request 刷新Token请求
     * @return 新的Token信息
     */
    @PostMapping("/refresh")
    public ApiResponse<LoginResponse> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        log.info("Token refresh request");
        
        LoginResponse response = authService.refreshToken(request);
        
        log.info("Token refresh successful");
        return ApiResponse.success("Token刷新成功", response);
    }

    /**
     * 检查Token有效性
     * 
     * @param httpRequest HTTP请求
     * @return 检查结果
     */
    @GetMapping("/check")
    public ApiResponse<Boolean> checkToken(HttpServletRequest httpRequest) {
        // 这个接口主要用于前端检查Token是否有效
        // 如果请求能够到达这里，说明Token是有效的（通过了JWT过滤器）
        return ApiResponse.success("Token有效", true);
    }

    /**
     * 获取当前用户信息
     * 
     * @param httpRequest HTTP请求
     * @return 用户信息
     */
    @GetMapping("/me")
    public ApiResponse<LoginResponse.UserInfo> getCurrentUser(HttpServletRequest httpRequest) {
        // TODO: 实现获取当前用户信息的逻辑
        // 这里可以从SecurityContext中获取当前用户信息
        return ApiResponse.success("获取用户信息成功", null);
    }
} 