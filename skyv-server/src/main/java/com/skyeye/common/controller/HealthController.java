package com.skyeye.common.controller;

import com.skyeye.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 健康检查控制器
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@RestController
@RequestMapping("/api/health")
public class HealthController {

    /**
     * 健康检查接口
     * 
     * @return 健康状态
     */
    @GetMapping
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> healthInfo = new HashMap<>();
        healthInfo.put("status", "UP");
        healthInfo.put("timestamp", LocalDateTime.now());
        healthInfo.put("application", "SkyEye Monitoring System");
        healthInfo.put("version", "1.0.0");
        
        return ApiResponse.success("系统运行正常", healthInfo);
    }

    /**
     * 系统信息接口
     * 
     * @return 系统信息
     */
    @GetMapping("/info")
    public ApiResponse<Map<String, Object>> info() {
        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("application", "SkyEye Monitoring System");
        systemInfo.put("version", "1.0.0");
        systemInfo.put("java.version", System.getProperty("java.version"));
        systemInfo.put("java.vendor", System.getProperty("java.vendor"));
        systemInfo.put("os.name", System.getProperty("os.name"));
        systemInfo.put("os.version", System.getProperty("os.version"));
        systemInfo.put("user.timezone", System.getProperty("user.timezone"));
        
        return ApiResponse.success("系统信息获取成功", systemInfo);
    }
} 