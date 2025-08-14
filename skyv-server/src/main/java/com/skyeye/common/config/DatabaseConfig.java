package com.skyeye.common.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 数据库配置类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableJpaRepositories(basePackages = "com.skyeye.**.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // 移除自定义数据源配置，使用Spring Boot的自动配置
    // Spring Boot会自动根据application.yml中的配置创建HikariDataSource
} 