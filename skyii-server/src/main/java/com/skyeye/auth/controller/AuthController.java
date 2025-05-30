package com.skyeye.auth.controller;

import com.skyeye.auth.dto.JwtResponse;
import com.skyeye.auth.dto.LoginRequest;
import com.skyeye.auth.dto.RegisterRequest;
import com.skyeye.auth.dto.UserInfoResponse;
import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.util.JwtUtils;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.response.ApiResponse;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@Api(tags = "认证管理")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    @ApiOperation("用户登录")
    public ApiResponse<JwtResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        // 认证用户
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));

        // 设置认证信息到上下文
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // 生成JWT
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(userDetails);

        // 获取用户信息
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        // 获取用户角色
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        return ApiResponse.success(new JwtResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                roles
        ),"登录成功");
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/info")
    @ApiOperation("获取当前用户信息")
    public ApiResponse<UserInfoResponse> getUserInfo() {
        // 获取当前用户
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();

        // 查询用户信息
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException("用户不存在"));

        // 获取用户角色
        List<String> roles = user.getRoles().stream()
                .map(Role::getCode)
                .collect(Collectors.toList());

        // 构建用户信息响应
        UserInfoResponse userInfo = new UserInfoResponse();
        userInfo.setId(user.getId());
        userInfo.setUsername(user.getUsername());
        userInfo.setName(user.getName());
        userInfo.setEmail(user.getEmail());
        userInfo.setPhone(user.getPhone());
        userInfo.setAvatar(user.getAvatar());
        userInfo.setRoles(roles);
        userInfo.setStatus(user.getStatus());
        userInfo.setLastLoginTime(user.getLastLoginTime());

        return ApiResponse.success(userInfo,"获取用户信息成功");
    }

    /**
     * 用户注册
     */
    @PostMapping("/register")
    @ApiOperation("用户注册")
    public ApiResponse<Void> register(@Valid @RequestBody RegisterRequest registerRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BusinessException("用户名已存在");
        }

        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }

        // 创建新用户
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword()));
        user.setName(registerRequest.getName());
        user.setEmail(registerRequest.getEmail());
        user.setPhone(registerRequest.getPhone());
        user.setStatus(1);

        // 设置默认角色
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByCode("USER")
                .orElseThrow(() -> new BusinessException("角色不存在"));
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);

        return ApiResponse.success(null,"注册成功");
    }
} 