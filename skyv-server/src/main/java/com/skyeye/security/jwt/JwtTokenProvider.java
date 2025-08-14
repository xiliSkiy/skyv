package com.skyeye.security.jwt;

import com.skyeye.common.constant.Constants;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * JWT Token提供者
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class JwtTokenProvider {

    @Value("${skyeye.jwt.secret}")
    private String secretKey;

    @Value("${skyeye.jwt.expiration}")
    private long validityInMilliseconds;

    @Value("${skyeye.jwt.refresh-expiration}")
    private long refreshValidityInMilliseconds;

    /**
     * 获取签名密钥
     * 
     * @return SecretKey
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 创建访问Token
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @param roles 角色列表
     * @return JWT Token
     */
    public String createToken(String username, Long userId, List<String> roles) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(Constants.Jwt.USER_ID_CLAIM, userId);
        claims.put(Constants.Jwt.USERNAME_CLAIM, username);
        claims.put(Constants.Jwt.ROLES_CLAIM, roles);

        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 创建刷新Token
     * 
     * @param username 用户名
     * @param userId 用户ID
     * @return 刷新Token
     */
    public String createRefreshToken(String username, Long userId) {
        Claims claims = Jwts.claims().setSubject(username);
        claims.put(Constants.Jwt.USER_ID_CLAIM, userId);
        claims.put(Constants.Jwt.USERNAME_CLAIM, username);
        claims.put("type", "refresh");

        Date now = new Date();
        Date validity = new Date(now.getTime() + refreshValidityInMilliseconds);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(now)
                .setExpiration(validity)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * 验证Token有效性
     * 
     * @param token JWT Token
     * @return true如果Token有效
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (SecurityException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    /**
     * 从Token获取用户名
     * 
     * @param token JWT Token
     * @return 用户名
     */
    public String getUsername(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            return claims.getSubject();
        } catch (Exception e) {
            log.error("Error extracting username from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token获取用户ID
     * 
     * @param token JWT Token
     * @return 用户ID
     */
    public Long getUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            return claims.get(Constants.Jwt.USER_ID_CLAIM, Long.class);
        } catch (Exception e) {
            log.error("Error extracting user ID from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 从Token获取角色列表
     * 
     * @param token JWT Token
     * @return 角色列表
     */
    @SuppressWarnings("unchecked")
    public List<String> getRoles(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            return (List<String>) claims.get(Constants.Jwt.ROLES_CLAIM);
        } catch (Exception e) {
            log.error("Error extracting roles from token: {}", e.getMessage());
            return Arrays.asList();
        }
    }

    /**
     * 从Token获取认证信息
     * 
     * @param token JWT Token
     * @return Authentication
     */
    public Authentication getAuthentication(String token) {
        String username = getUsername(token);
        List<String> roles = getRoles(token);
        
        Collection<? extends GrantedAuthority> authorities = roles.stream()
            .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
            .collect(Collectors.toList());
        
        User principal = new User(username, "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }

    /**
     * 获取Token过期时间
     * 
     * @param token JWT Token
     * @return 过期时间
     */
    public Date getExpiration(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            return claims.getExpiration();
        } catch (Exception e) {
            log.error("Error extracting expiration from token: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 检查Token是否即将过期（30分钟内）
     * 
     * @param token JWT Token
     * @return true如果即将过期
     */
    public boolean isTokenExpiringSoon(String token) {
        Date expiration = getExpiration(token);
        if (expiration == null) {
            return true;
        }
        
        long timeToExpiry = expiration.getTime() - System.currentTimeMillis();
        return timeToExpiry < 30 * 60 * 1000; // 30分钟
    }

    /**
     * 检查是否为刷新Token
     * 
     * @param token JWT Token
     * @return true如果是刷新Token
     */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
            return "refresh".equals(claims.get("type"));
        } catch (Exception e) {
            return false;
        }
    }
} 