package com.skyeye;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * SkyEye智能监控系统主应用程序类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@SpringBootApplication
@EnableCaching
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
public class SkyEyeApplication {

    public static void main(String[] args) {
        SpringApplication.run(SkyEyeApplication.class, args);
        System.out.println("========================================");
        System.out.println("  SkyEye智能监控系统启动成功！");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("  API文档: http://localhost:8080/swagger-ui.html");
        System.out.println("  健康检查: http://localhost:8080/actuator/health");
        System.out.println("========================================");
    }
} 