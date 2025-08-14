package com.skyeye.auth.service;

import com.skyeye.auth.dto.LoginRequest;
import com.skyeye.auth.dto.LoginResponse;
import com.skyeye.auth.dto.RefreshTokenRequest;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.response.ResponseCode;
import com.skyeye.security.jwt.JwtTokenProvider;
import com.skyeye.user.entity.User;
import com.skyeye.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 认证服务
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String TOKEN_BLACKLIST_PREFIX = "auth:blacklist:";
    private static final String LOGIN_ATTEMPT_PREFIX = "auth:attempt:";

    /**
     * 用户登录
     * 
     * @param request 登录请求
     * @param httpRequest HTTP请求
     * @return 登录响应
     */
    @Transactional
    public LoginResponse login(LoginRequest request, HttpServletRequest httpRequest) {
        String username = request.getUsername();
        String clientIp = getClientIp(httpRequest);
        
        try {
            // 检查登录尝试次数
            checkLoginAttempts(username, clientIp);
            
            // 执行认证
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, request.getPassword())
            );

            // 获取用户信息
            User user = userRepository.findActiveByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

            // 检查用户状态
            validateUserStatus(user);

            // 生成Token
            List<String> roles = user.getRoles().stream()
                .filter(role -> role.isEnabled())
                .map(role -> role.getCode())
                .collect(Collectors.toList());

            String accessToken = jwtTokenProvider.createToken(username, user.getId(), roles);
            String refreshToken = jwtTokenProvider.createRefreshToken(username, user.getId());

            // 更新用户登录信息
            user.updateLastLogin(clientIp);
            userRepository.save(user);

            // 清除登录失败记录
            clearLoginAttempts(username, clientIp);

            // 构建响应
            return buildLoginResponse(user, accessToken, refreshToken);

        } catch (AuthenticationException e) {
            // 记录登录失败
            recordLoginFailure(username, clientIp);
            handleAuthenticationException(e, username);
            throw new BusinessException(ResponseCode.LOGIN_FAILED, "登录失败");
        }
    }

    /**
     * 用户登出
     * 
     * @param httpRequest HTTP请求
     */
    public void logout(HttpServletRequest httpRequest) {
        String token = resolveToken(httpRequest);
        if (StringUtils.hasText(token)) {
            // 将Token加入黑名单
            addTokenToBlacklist(token);
            log.info("User logged out, token added to blacklist");
        }
    }

    /**
     * 刷新Token
     * 
     * @param request 刷新Token请求
     * @return 登录响应
     */
    public LoginResponse refreshToken(RefreshTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (!jwtTokenProvider.validateToken(refreshToken)) {
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "刷新Token无效");
        }

        if (!jwtTokenProvider.isRefreshToken(refreshToken)) {
            throw new BusinessException(ResponseCode.TOKEN_INVALID, "Token类型错误");
        }

        String username = jwtTokenProvider.getUsername(refreshToken);
        Long userId = jwtTokenProvider.getUserId(refreshToken);

        User user = userRepository.findActiveByUsername(username)
            .orElseThrow(() -> new BusinessException("用户不存在"));

        validateUserStatus(user);

        // 生成新的Token
        List<String> roles = user.getRoles().stream()
            .filter(role -> role.isEnabled())
            .map(role -> role.getCode())
            .collect(Collectors.toList());

        String newAccessToken = jwtTokenProvider.createToken(username, userId, roles);
        String newRefreshToken = jwtTokenProvider.createRefreshToken(username, userId);

        // 将旧的刷新Token加入黑名单
        addTokenToBlacklist(refreshToken);

        return buildLoginResponse(user, newAccessToken, newRefreshToken);
    }

    /**
     * 检查Token是否在黑名单中
     * 
     * @param token JWT Token
     * @return true如果在黑名单中
     */
    public boolean isTokenBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(TOKEN_BLACKLIST_PREFIX + token));
    }

    /**
     * 检查登录尝试次数
     * 
     * @param username 用户名
     * @param clientIp 客户端IP
     */
    private void checkLoginAttempts(String username, String clientIp) {
        String userKey = LOGIN_ATTEMPT_PREFIX + "user:" + username;
        String ipKey = LOGIN_ATTEMPT_PREFIX + "ip:" + clientIp;

        Integer userAttempts = (Integer) redisTemplate.opsForValue().get(userKey);
        Integer ipAttempts = (Integer) redisTemplate.opsForValue().get(ipKey);

        if (userAttempts != null && userAttempts >= 5) {
            throw new BusinessException("用户登录失败次数过多，请30分钟后再试");
        }

        if (ipAttempts != null && ipAttempts >= 10) {
            throw new BusinessException("IP登录失败次数过多，请1小时后再试");
        }
    }

    /**
     * 记录登录失败
     * 
     * @param username 用户名
     * @param clientIp 客户端IP
     */
    private void recordLoginFailure(String username, String clientIp) {
        String userKey = LOGIN_ATTEMPT_PREFIX + "user:" + username;
        String ipKey = LOGIN_ATTEMPT_PREFIX + "ip:" + clientIp;

        // 用户失败次数，30分钟过期
        redisTemplate.opsForValue().increment(userKey);
        redisTemplate.expire(userKey, 30, TimeUnit.MINUTES);

        // IP失败次数，1小时过期
        redisTemplate.opsForValue().increment(ipKey);
        redisTemplate.expire(ipKey, 1, TimeUnit.HOURS);

        // 更新数据库中的失败次数
        User user = userRepository.findActiveByUsername(username).orElse(null);
        if (user != null) {
            user.incrementLoginFailCount();
            userRepository.save(user);
        }
    }

    /**
     * 清除登录失败记录
     * 
     * @param username 用户名
     * @param clientIp 客户端IP
     */
    private void clearLoginAttempts(String username, String clientIp) {
        String userKey = LOGIN_ATTEMPT_PREFIX + "user:" + username;
        String ipKey = LOGIN_ATTEMPT_PREFIX + "ip:" + clientIp;

        redisTemplate.delete(userKey);
        redisTemplate.delete(ipKey);
    }

    /**
     * 验证用户状态
     * 
     * @param user 用户实体
     */
    private void validateUserStatus(User user) {
        if (user.isDisabled()) {
            throw new BusinessException("用户账户已被禁用");
        }

        if (user.isLocked()) {
            throw new BusinessException("用户账户已被锁定");
        }

        if (!user.isActive()) {
            throw new BusinessException("用户账户不可用");
        }
    }

    /**
     * 将Token加入黑名单
     * 
     * @param token JWT Token
     */
    private void addTokenToBlacklist(String token) {
        try {
            // 获取Token过期时间
            long expiration = jwtTokenProvider.getExpiration(token).getTime();
            long currentTime = System.currentTimeMillis();
            long ttl = expiration - currentTime;

            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                    TOKEN_BLACKLIST_PREFIX + token, 
                    "blacklisted", 
                    ttl, 
                    TimeUnit.MILLISECONDS
                );
            }
        } catch (Exception e) {
            log.error("Failed to add token to blacklist", e);
        }
    }

    /**
     * 构建登录响应
     * 
     * @param user 用户实体
     * @param accessToken 访问Token
     * @param refreshToken 刷新Token
     * @return 登录响应
     */
    private LoginResponse buildLoginResponse(User user, String accessToken, String refreshToken) {
        List<String> roles = user.getRoles().stream()
            .filter(role -> role.isEnabled())
            .map(role -> role.getCode())
            .collect(Collectors.toList());

        List<String> permissions = user.getRoles().stream()
            .filter(role -> role.isEnabled())
            .flatMap(role -> role.getPermissions().stream())
            .filter(permission -> permission.isEnabled())
            .map(permission -> permission.getCode())
            .distinct()
            .collect(Collectors.toList());

        LoginResponse.UserInfo userInfo = LoginResponse.UserInfo.builder()
            .id(user.getId())
            .username(user.getUsername())
            .realName(user.getRealName())
            .email(user.getEmail())
            .phone(user.getPhone())
            .avatar(user.getAvatar())
            .isAdmin(user.getIsAdmin())
            .roles(roles)
            .permissions(permissions)
            .lastLoginTime(user.getLastLoginTime())
            .lastLoginIp(user.getLastLoginIp())
            .build();

        return LoginResponse.builder()
            .accessToken(accessToken)
            .refreshToken(refreshToken)
            .tokenType("Bearer")
            .expiresIn(24 * 60 * 60 * 1000L) // 24小时
            .userInfo(userInfo)
            .build();
    }

    /**
     * 处理认证异常
     * 
     * @param e 认证异常
     * @param username 用户名
     */
    private void handleAuthenticationException(AuthenticationException e, String username) {
        if (e instanceof BadCredentialsException) {
            log.warn("Invalid credentials for user: {}", username);
        } else if (e instanceof DisabledException) {
            log.warn("User account is disabled: {}", username);
        } else if (e instanceof LockedException) {
            log.warn("User account is locked: {}", username);
        } else {
            log.warn("Authentication failed for user: {}, reason: {}", username, e.getMessage());
        }
    }

    /**
     * 从HTTP请求中解析Token
     * 
     * @param request HTTP请求
     * @return JWT Token
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * 获取客户端IP地址
     * 
     * @param request HTTP请求
     * @return 客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xForwardedFor)) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (StringUtils.hasText(xRealIp)) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }
} 