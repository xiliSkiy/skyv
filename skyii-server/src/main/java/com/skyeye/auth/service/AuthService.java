 package com.skyeye.auth.service;

import com.skyeye.auth.entity.User;

/**
 * 认证服务接口
 * 提供用户认证和获取当前用户信息等功能
 */
public interface AuthService {
    
    /**
     * 获取当前登录用户
     * 
     * @return 当前登录的用户对象，如果未登录则返回null
     */
    User getCurrentUser();
}