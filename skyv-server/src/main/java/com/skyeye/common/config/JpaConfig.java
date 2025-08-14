package com.skyeye.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.fasterxml.jackson.databind.DeserializationFeature;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;

import java.util.Optional;

/**
 * JPA配置类
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
public class JpaConfig {

    /**
     * 配置审计功能的用户提供者
     * 用于自动填充创建者和修改者字段
     */
    @Bean
    public AuditorAware<String> auditorProvider() {
        return new AuditorAwareImpl();
    }

    /**
     * 审计用户提供者实现类
     */
    public static class AuditorAwareImpl implements AuditorAware<String> {
        
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated() 
                || "anonymousUser".equals(authentication.getPrincipal())) {
                return Optional.of("system");
            }
            
            return Optional.of(authentication.getName());
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.featuresToEnable(
            DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT
        );
    }
} 