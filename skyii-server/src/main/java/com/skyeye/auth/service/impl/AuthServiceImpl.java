 package com.skyeye.auth.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.skyeye.auth.entity.User;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.service.AuthService;

/**
 * 认证服务实现类
 */
@Service
public class AuthServiceImpl implements AuthService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || 
                "anonymousUser".equals(authentication.getPrincipal())) {
            return null;
        }
        
        String username = authentication.getName();
        return userRepository.findByUsername(username).orElse(null);
    }
}