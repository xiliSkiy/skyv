package com.skyeye.common.controller;

import com.skyeye.common.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 测试控制器
 */
@RestController
@RequestMapping("/test")
public class TestController {

    /**
     * 测试接口
     */
    @GetMapping("/hello")
    public ApiResponse<String> hello() {
        return ApiResponse.success("Hello, SkyEye!", "测试成功");
    }
} 