package com.skyeye.security.jwt;

import com.skyeye.common.constant.Constants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT认证过滤器
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        try {
            // 从请求中解析JWT Token
            String jwt = resolveToken(request);
            
            if (StringUtils.hasText(jwt) && jwtTokenProvider.validateToken(jwt)) {
                // Token有效，设置认证信息
                Authentication authentication = jwtTokenProvider.getAuthentication(jwt);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                
                log.debug("Set Authentication to security context for '{}', uri: {}", 
                    authentication.getName(), request.getRequestURI());
            } else if (StringUtils.hasText(jwt)) {
                log.debug("Invalid JWT token, uri: {}", request.getRequestURI());
            }
        } catch (Exception ex) {
            log.error("Could not set user authentication in security context", ex);
        }

        filterChain.doFilter(request, response);
    }

    /**
     * 从HTTP请求中解析JWT Token
     * 
     * @param request HTTP请求
     * @return JWT Token
     */
    private String resolveToken(HttpServletRequest request) {
        // 从Authorization头获取Token
        String bearerToken = request.getHeader(Constants.Jwt.TOKEN_HEADER);
        
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(Constants.Jwt.TOKEN_PREFIX)) {
            return bearerToken.substring(Constants.Jwt.TOKEN_PREFIX.length());
        }
        
        // 从请求参数获取Token（用于WebSocket等场景）
        String token = request.getParameter("token");
        if (StringUtils.hasText(token)) {
            return token;
        }
        
        return null;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();
        
        // 跳过不需要认证的路径
        return path.startsWith("/api/auth/") ||
               path.startsWith("/api/health") ||
               path.startsWith("/actuator/") ||
               path.startsWith("/swagger-ui/") ||
               path.startsWith("/v3/api-docs") ||
               path.equals("/favicon.ico") ||
               path.equals("/error");
    }
} 