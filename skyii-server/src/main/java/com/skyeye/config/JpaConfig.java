package com.skyeye.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA配置类，启用审计功能
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
} 