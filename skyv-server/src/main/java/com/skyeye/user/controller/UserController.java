package com.skyeye.user.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.user.dto.UserCreateRequest;
import com.skyeye.user.dto.UserDto;
import com.skyeye.user.entity.Role;
import com.skyeye.user.entity.User;
import com.skyeye.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @Transactional(readOnly = true)
    // @PreAuthorize("hasAuthority('user:view')")
    public ApiResponse<Map<String, Object>> getUserList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "") String name,
            @RequestParam(defaultValue = "") String email,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortOrder) {
        
        try {
            // 创建排序对象
            Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
            Pageable pageable = PageRequest.of(page, limit, sort);
            
            // 查询活跃用户
            Page<User> userPage = userRepository.findActiveUsers(pageable);
            
            // 转换为DTO避免懒加载问题
            List<UserDto> userDtos = userPage.getContent().stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
            
            // 构建响应数据
            Map<String, Object> result = new HashMap<>();
            result.put("list", userDtos);
            result.put("total", userPage.getTotalElements());
            result.put("page", page);
            result.put("limit", limit);
            result.put("totalPages", userPage.getTotalPages());
            
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("获取用户列表失败", e);
            return ApiResponse.businessError("获取用户列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('user:view')")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        try {
            Optional<User> user = userRepository.findById(id);
            if (user.isPresent() && user.get().getDeletedAt() == null) {
                return ApiResponse.success(user.get());
            } else {
                return ApiResponse.businessError("用户不存在");
            }
        } catch (Exception e) {
            log.error("获取用户详情失败", e);
            return ApiResponse.businessError("获取用户详情失败：" + e.getMessage());
        }
    }

    /**
     * 创建用户
     */
    @PostMapping
    @PreAuthorize("hasAuthority('user:create')")
    public ApiResponse<User> createUser(@Valid @RequestBody UserCreateRequest request) {
        try {
            // 检查用户名是否已存在
            if (userRepository.existsByUsername(request.getUsername())) {
                return ApiResponse.businessError("用户名已存在");
            }
            
            // 检查邮箱是否已存在
            if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
                return ApiResponse.businessError("邮箱已存在");
            }
            
            // 检查手机号是否已存在
            if (request.getPhone() != null && userRepository.existsByPhone(request.getPhone())) {
                return ApiResponse.businessError("手机号已存在");
            }
            
            // 创建用户对象
            User user = new User();
            user.setUsername(request.getUsername());
            // 这里应该使用密码加密，暂时使用明文
            user.setPassword(request.getPassword()); // TODO: 需要加密
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setAvatar(request.getAvatar());
            user.setStatus(request.getStatus());
            user.setIsAdmin(request.getIsAdmin());
            user.setRemark(request.getRemark());
            
            // 保存用户
            User savedUser = userRepository.save(user);
            
            return ApiResponse.success(savedUser);
        } catch (Exception e) {
            log.error("创建用户失败", e);
            return ApiResponse.businessError("创建用户失败：" + e.getMessage());
        }
    }

    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<User> updateUser(@PathVariable Long id, @Valid @RequestBody UserCreateRequest request) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (!optionalUser.isPresent() || optionalUser.get().getDeletedAt() != null) {
                return ApiResponse.businessError("用户不存在");
            }
            
            User user = optionalUser.get();
            
            // 检查用户名是否已被其他用户使用
            if (!user.getUsername().equals(request.getUsername()) && 
                userRepository.existsByUsername(request.getUsername())) {
                return ApiResponse.businessError("用户名已存在");
            }
            
            // 更新用户信息
            user.setUsername(request.getUsername());
            if (request.getPassword() != null && !request.getPassword().isEmpty()) {
                user.setPassword(request.getPassword()); // TODO: 需要加密
            }
            user.setRealName(request.getRealName());
            user.setEmail(request.getEmail());
            user.setPhone(request.getPhone());
            user.setAvatar(request.getAvatar());
            user.setStatus(request.getStatus());
            user.setIsAdmin(request.getIsAdmin());
            user.setRemark(request.getRemark());
            
            User savedUser = userRepository.save(user);
            
            return ApiResponse.success(savedUser);
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return ApiResponse.businessError("更新用户失败：" + e.getMessage());
        }
    }

    /**
     * 删除用户（软删除）
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('user:delete')")
    public ApiResponse<Void> deleteUser(@PathVariable Long id) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (!optionalUser.isPresent() || optionalUser.get().getDeletedAt() != null) {
                return ApiResponse.businessError("用户不存在");
            }
            
            User user = optionalUser.get();
            user.setDeletedAt(new Timestamp(System.currentTimeMillis()));
            userRepository.save(user);
            
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("删除用户失败", e);
            return ApiResponse.businessError("删除用户失败：" + e.getMessage());
        }
    }

    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    // @PreAuthorize("hasAuthority('user:view')")
    public ApiResponse<List<Role>> getAllRoles() {
        try {
            // 暂时返回空列表，需要实现角色查询
            List<Role> roles = new ArrayList<>();
            return ApiResponse.success(roles);
        } catch (Exception e) {
            log.error("获取角色列表失败", e);
            return ApiResponse.businessError("获取角色列表失败：" + e.getMessage());
        }
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    // @PreAuthorize("hasAuthority('user:view')")
    public ApiResponse<Map<String, Object>> getUserStatistics() {
        try {
            // 统计用户数量
            long totalUsers = userRepository.count();
            long activeUsers = userRepository.findByStatus(1).size();
            long disabledUsers = userRepository.findByStatus(0).size();
            
            Map<String, Object> statistics = new HashMap<>();
            statistics.put("totalUsers", totalUsers);
            statistics.put("activeUsers", activeUsers);
            statistics.put("disabledUsers", disabledUsers);
            statistics.put("lockedUsers", 0L); // 暂时设为0
            
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取用户统计信息失败", e);
            return ApiResponse.businessError("获取用户统计信息失败：" + e.getMessage());
        }
    }

    /**
     * 重置用户密码
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (!optionalUser.isPresent() || optionalUser.get().getDeletedAt() != null) {
                return ApiResponse.businessError("用户不存在");
            }
            
            User user = optionalUser.get();
            user.setPassword(newPassword); // TODO: 需要加密
            user.setPasswordChangedTime(LocalDateTime.now());
            userRepository.save(user);
            
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("重置密码失败", e);
            return ApiResponse.businessError("重置密码失败：" + e.getMessage());
        }
    }

    /**
     * 启用/禁用用户
     */
    @PostMapping("/{id}/toggle-status")
    @PreAuthorize("hasAuthority('user:update')")
    public ApiResponse<Void> toggleUserStatus(@PathVariable Long id) {
        try {
            Optional<User> optionalUser = userRepository.findById(id);
            if (!optionalUser.isPresent() || optionalUser.get().getDeletedAt() != null) {
                return ApiResponse.businessError("用户不存在");
            }
            
            User user = optionalUser.get();
            user.setStatus(user.getStatus() == 1 ? 0 : 1);
            userRepository.save(user);
            
            return ApiResponse.success();
        } catch (Exception e) {
            log.error("切换用户状态失败", e);
            return ApiResponse.businessError("切换用户状态失败：" + e.getMessage());
        }
    }

    /**
     * 将User实体转换为UserDto
     */
    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRealName(user.getRealName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        dto.setIsAdmin(user.getIsAdmin());
        dto.setCreatedAt(user.getCreatedAt());
        dto.setUpdatedAt(user.getUpdatedAt());
        dto.setRemark(user.getRemark());
        return dto;
    }
} 