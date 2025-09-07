package com.skyeye.security.config;

import com.skyeye.security.jwt.JwtAuthenticationFilter;
import com.skyeye.security.service.CustomPermissionEvaluator;
import com.skyeye.security.service.UserDetailsServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionHandler;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security安全配置
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final CustomPermissionEvaluator permissionEvaluator;

    /**
     * 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 认证提供者
     */
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * 认证管理器
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * 方法安全表达式处理器
     * 配置权限评估器
     */
    @Bean
    public MethodSecurityExpressionHandler methodSecurityExpressionHandler() {
        DefaultMethodSecurityExpressionHandler expressionHandler = new DefaultMethodSecurityExpressionHandler();
        expressionHandler.setPermissionEvaluator(permissionEvaluator);
        return expressionHandler;
    }

    /**
     * CORS配置
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOriginPatterns(List.of("*"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    /**
     * 安全过滤器链
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // 禁用CSRF保护
            .csrf(AbstractHttpConfigurer::disable)
            
            // 启用CORS
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            
            // 配置会话管理为无状态
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            
            // 配置请求授权规则
            .authorizeHttpRequests(authz -> authz
                // 公开访问的路径
                .requestMatchers(
                    "/api/auth/**",           // 认证相关接口
                    "/api/health/**",         // 健康检查接口
                    "/actuator/**",           // Spring Boot Actuator
                    "/swagger-ui/**",         // Swagger UI
                    "/v3/api-docs/**",        // OpenAPI文档
                    "/swagger-resources/**",   // Swagger资源
                    "/webjars/**",            // WebJars
                    "/favicon.ico",           // 网站图标
                    "/error"                  // 错误页面
                ).permitAll()
                
                // 管理员接口
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                
                // 用户管理接口
                .requestMatchers("/api/user/**").hasAnyRole("ADMIN", "USER_MANAGER")
                .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "USER_MANAGER")
                
                // 设备管理接口
                .requestMatchers("/api/device/**").hasAnyRole("ADMIN", "DEVICE_MANAGER", "USER")
                .requestMatchers("/api/devices/**").hasAnyRole("ADMIN", "DEVICE_MANAGER", "USER")
                
                // 采集器和任务管理接口
                .requestMatchers("/api/collector/**").hasAnyRole("ADMIN", "DEVICE_MANAGER", "USER")
                
                // 其他所有请求都需要认证
                .anyRequest().authenticated()
            )
            
            // 设置认证提供者
            .authenticationProvider(authenticationProvider())
            
            // 添加JWT过滤器
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            
            // 异常处理
            .exceptionHandling(exceptions -> exceptions
                .authenticationEntryPoint((request, response, authException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(401);
                    response.getWriter().write("""
                        {
                            "code": 401,
                            "message": "未授权访问",
                            "timestamp": %d
                        }
                        """.formatted(System.currentTimeMillis()));
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    response.setContentType("application/json;charset=UTF-8");
                    response.setStatus(403);
                    response.getWriter().write("""
                        {
                            "code": 403,
                            "message": "权限不足",
                            "timestamp": %d
                        }
                        """.formatted(System.currentTimeMillis()));
                })
            );

        return http.build();
    }
} 