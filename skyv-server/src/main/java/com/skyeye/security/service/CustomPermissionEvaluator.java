package com.skyeye.security.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 自定义权限评估器
 * 用于处理 @PreAuthorize("hasPermission('resource', 'action')") 表达式
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class CustomPermissionEvaluator implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication authentication, Object targetDomainObject, Object permission) {
        log.debug("Evaluating permission: targetDomainObject={}, permission={}", targetDomainObject, permission);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        // 处理 hasPermission('resource', 'action') 形式的调用
        if (targetDomainObject instanceof String && permission instanceof String) {
            String resource = (String) targetDomainObject;
            String action = (String) permission;
            return hasPermission(authentication, resource, action);
        }

        log.debug("Unsupported permission check format");
        return false;
    }

    @Override
    public boolean hasPermission(Authentication authentication, Serializable targetId, String targetType, Object permission) {
        log.debug("Evaluating permission: targetId={}, targetType={}, permission={}", targetId, targetType, permission);
        
        if (authentication == null || !authentication.isAuthenticated()) {
            log.debug("Authentication is null or not authenticated");
            return false;
        }

        // 这个方法可以用于基于对象ID的权限检查
        // 目前暂不实现，直接返回false
        return false;
    }

    /**
     * 检查用户是否具有指定资源的指定操作权限
     * 
     * @param authentication 认证信息
     * @param resource 资源名称
     * @param action 操作类型
     * @return 是否有权限
     */
    private boolean hasPermission(Authentication authentication, String resource, String action) {
        String requiredPermission = resource + ":" + action;
        log.debug("Checking permission: {} for user: {}", requiredPermission, authentication.getName());

        // 管理员拥有所有权限
        boolean isAdmin = authentication.getAuthorities().stream()
            .anyMatch(authority -> "ROLE_ADMIN".equals(authority.getAuthority()));
        
        if (isAdmin) {
            log.debug("User {} is admin, granting permission", authentication.getName());
            return true;
        }

        // 检查用户是否具有特定权限
        boolean hasPermission = authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .peek(authority -> log.debug("User authority: {}", authority))
            .anyMatch(authority -> requiredPermission.equals(authority));

        log.debug("Permission check result for {}: {}", requiredPermission, hasPermission);
        return hasPermission;
    }
}
