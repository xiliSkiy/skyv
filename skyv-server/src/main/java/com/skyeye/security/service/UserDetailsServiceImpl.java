package com.skyeye.security.service;

import com.skyeye.user.entity.User;
import com.skyeye.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Spring Security用户详情服务实现
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Authenticating user: {}", username);

        User user = userRepository.findActiveByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));

        return createUserPrincipal(user);
    }

    /**
     * 创建用户主体对象
     * 
     * @param user 用户实体
     * @return UserDetails
     */
    private UserDetails createUserPrincipal(User user) {
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
            .filter(role -> role.isEnabled())
            .flatMap(role -> role.getPermissions().stream())
            .filter(permission -> permission.isEnabled())
            .map(permission -> new SimpleGrantedAuthority(permission.getCode()))
            .collect(Collectors.toSet());

        // 添加角色权限
        user.getRoles().stream()
            .filter(role -> role.isEnabled())
            .forEach(role -> authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getCode())));

        return org.springframework.security.core.userdetails.User.builder()
            .username(user.getUsername())
            .password(user.getPassword())
            .disabled(!user.isActive())
            .accountLocked(user.isLocked())
            .credentialsExpired(false)
            .accountExpired(false)
            .authorities(authorities)
            .build();
    }
} 