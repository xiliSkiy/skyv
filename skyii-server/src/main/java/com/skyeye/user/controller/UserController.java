package com.skyeye.user.controller;

import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.response.Result;
import com.skyeye.common.response.PageMeta;
import com.skyeye.common.response.Result;
import com.skyeye.user.dto.UserCreateRequest;
import com.skyeye.user.dto.UserDTO;
import com.skyeye.user.dto.UserQueryRequest;
import com.skyeye.user.dto.UserUpdateRequest;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理控制器
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Api(tags = "用户管理")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    @ApiOperation("获取用户列表")
    public Result<List<UserDTO>> getUserList(UserQueryRequest queryRequest) {
        // 构建查询条件
        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 用户名模糊查询
            if (queryRequest.getUsername() != null && !queryRequest.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + queryRequest.getUsername() + "%"));
            }
            
            // 姓名模糊查询
            if (queryRequest.getName() != null && !queryRequest.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + queryRequest.getName() + "%"));
            }
            
            // 邮箱模糊查询
            if (queryRequest.getEmail() != null && !queryRequest.getEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("email"), "%" + queryRequest.getEmail() + "%"));
            }
            
            // 状态精确查询
            if (queryRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryRequest.getStatus()));
            }
            
            // 创建日期范围查询
            if (queryRequest.getStartDate() != null && queryRequest.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"), queryRequest.getStartDate(), queryRequest.getEndDate()));
            } else if (queryRequest.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), queryRequest.getStartDate()));
            } else if (queryRequest.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), queryRequest.getEndDate()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 构建分页和排序
        int page = queryRequest.getPage() != null ? queryRequest.getPage() : 0;
        int size = queryRequest.getSize() != null ? queryRequest.getSize() : 10;
        String sortBy = queryRequest.getSortBy() != null ? queryRequest.getSortBy() : "id";
        String sortOrder = queryRequest.getSortOrder() != null ? queryRequest.getSortOrder() : "desc";
        
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        // 执行查询
        Page<User> userPage = userRepository.findAll(specification, pageable);
        
        // 转换为DTO
        List<UserDTO> userDTOList = userPage.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        // 构建分页元数据
        PageMeta pageMeta = new PageMeta();
        pageMeta.setPage(page);
        pageMeta.setLimit(size);
        pageMeta.setTotal(userPage.getTotalElements());
        pageMeta.setTotalPages(userPage.getTotalPages());
        
        return Result.success(userDTOList, "获取用户列表成功", pageMeta);
    }
    
    /**
     * 获取用户详情
     */
    @GetMapping("/{id}")
    @ApiOperation("获取用户详情")
    public Result<UserDTO> getUserDetail(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        return Result.success(convertToDTO(user), "获取用户详情成功");
    }
    
    /**
     * 创建用户
     */
    @PostMapping
    @ApiOperation("创建用户")
    public Result<UserDTO> createUser(@Valid @RequestBody UserCreateRequest createRequest) {
        // 检查用户名是否已存在
        if (userRepository.existsByUsername(createRequest.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (userRepository.existsByEmail(createRequest.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 创建用户
        User user = new User();
        user.setUsername(createRequest.getUsername());
        user.setPassword(passwordEncoder.encode(createRequest.getPassword()));
        user.setName(createRequest.getName());
        user.setEmail(createRequest.getEmail());
        user.setPhone(createRequest.getPhone());
        user.setStatus(createRequest.getStatus());
        
        // 设置角色
        Set<Role> roles = new HashSet<>();
        if (createRequest.getRoleIds() != null && !createRequest.getRoleIds().isEmpty()) {
            roles = createRequest.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new BusinessException("角色不存在: " + roleId)))
                    .collect(Collectors.toSet());
        } else {
            // 设置默认角色
            Role userRole = roleRepository.findByCode("USER")
                    .orElseThrow(() -> new BusinessException("默认角色不存在"));
            roles.add(userRole);
        }
        user.setRoles(roles);
        
        // 保存用户
        user = userRepository.save(user);
        
        return Result.success(convertToDTO(user), "创建用户成功");
    }
    
    /**
     * 更新用户
     */
    @PutMapping("/{id}")
    @ApiOperation("更新用户")
    public Result<UserDTO> updateUser(@PathVariable Long id, @Valid @RequestBody UserUpdateRequest updateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 检查用户名是否已存在
        if (!user.getUsername().equals(updateRequest.getUsername()) && 
                userRepository.existsByUsername(updateRequest.getUsername())) {
            throw new BusinessException("用户名已存在");
        }
        
        // 检查邮箱是否已存在
        if (!user.getEmail().equals(updateRequest.getEmail()) && 
                userRepository.existsByEmail(updateRequest.getEmail())) {
            throw new BusinessException("邮箱已被使用");
        }
        
        // 更新用户信息
        user.setUsername(updateRequest.getUsername());
        user.setName(updateRequest.getName());
        user.setEmail(updateRequest.getEmail());
        user.setPhone(updateRequest.getPhone());
        user.setStatus(updateRequest.getStatus());
        user.setAvatar(updateRequest.getAvatar());
        
        // 更新角色
        if (updateRequest.getRoleIds() != null && !updateRequest.getRoleIds().isEmpty()) {
            Set<Role> roles = updateRequest.getRoleIds().stream()
                    .map(roleId -> roleRepository.findById(roleId)
                            .orElseThrow(() -> new BusinessException("角色不存在: " + roleId)))
                    .collect(Collectors.toSet());
            user.setRoles(roles);
        }
        
        // 保存用户
        user = userRepository.save(user);
        
        return Result.success(convertToDTO(user), "更新用户成功");
    }
    
    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @ApiOperation("删除用户")
    public Result<Void> deleteUser(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 不允许删除管理员账号
        if (user.getRoles().stream().anyMatch(role -> role.getCode().equals("ADMIN"))) {
            throw new BusinessException("不能删除管理员账号");
        }
        
        userRepository.delete(user);
        
        return Result.success(null, "删除用户成功");
    }
    
    /**
     * 批量删除用户
     */
    @DeleteMapping("/batch")
    @ApiOperation("批量删除用户")
    public Result<Void> batchDeleteUsers(@RequestBody List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException("请选择要删除的用户");
        }
        
        // 查询用户
        List<User> users = userRepository.findAllById(ids);
        
        // 检查是否包含管理员账号
        boolean hasAdmin = users.stream()
                .anyMatch(user -> user.getRoles().stream()
                        .anyMatch(role -> role.getCode().equals("ADMIN")));
        
        if (hasAdmin) {
            throw new BusinessException("不能删除管理员账号");
        }
        
        userRepository.deleteAll(users);
        
        return Result.success(null, "批量删除用户成功");
    }
    
    /**
     * 重置用户密码
     */
    @PutMapping("/{id}/reset-password")
    @ApiOperation("重置用户密码")
    public Result<Void> resetPassword(@PathVariable Long id, @RequestParam String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException("用户不存在"));
        
        // 更新密码
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return Result.success(null, "重置密码成功");
    }
    
    /**
     * 获取所有角色
     */
    @GetMapping("/roles")
    @ApiOperation("获取所有角色")
    public Result<List<Role>> getAllRoles() {
        List<Role> roles = roleRepository.findAll();
        return Result.success(roles, "获取角色列表成功");
    }
    
    /**
     * 获取用户统计信息
     */
    @GetMapping("/statistics")
    @ApiOperation("获取用户统计信息")
    public Result<Map<String, Object>> getUserStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 总用户数
        long totalUsers = userRepository.count();
        statistics.put("totalUsers", totalUsers);
        
        // 活跃用户数
        long activeUsers = userRepository.countByStatus(1);
        statistics.put("activeUsers", activeUsers);
        
        // 锁定用户数
        long lockedUsers = userRepository.countByStatus(2);
        statistics.put("lockedUsers", lockedUsers);
        
        // 非活跃用户数
        long inactiveUsers = userRepository.countByStatus(0);
        statistics.put("inactiveUsers", inactiveUsers);
        
        // 在线用户数（假设通过最后登录时间判断，24小时内登录过的算在线）
        LocalDateTime onlineThreshold = LocalDateTime.now().minusHours(24);
        long onlineUsers = userRepository.countByLastLoginTimeAfter(onlineThreshold);
        statistics.put("onlineUsers", onlineUsers);
        
        // 最近7天新增用户数
        LocalDateTime weekAgo = LocalDateTime.now().minusDays(7);
        long newUsersThisWeek = userRepository.countByCreatedAtAfter(weekAgo);
        statistics.put("newUsersThisWeek", newUsersThisWeek);
        
        // 最近30天新增用户数
        LocalDateTime monthAgo = LocalDateTime.now().minusDays(30);
        long newUsersThisMonth = userRepository.countByCreatedAtAfter(monthAgo);
        statistics.put("newUsersThisMonth", newUsersThisMonth);
        
        // 按角色统计
        List<Role> allRoles = roleRepository.findAll();
        Map<String, Long> roleStats = new HashMap<>();
        for (Role role : allRoles) {
            long count = userRepository.findByRoleId(role.getId()).size();
            roleStats.put(role.getCode(), count);
        }
        statistics.put("roleStats", roleStats);
        
        return Result.success(statistics, "获取用户统计信息成功");
    }
    
    /**
     * 获取在线用户列表
     */
    @GetMapping("/online")
    @ApiOperation("获取在线用户列表")
    public Result<List<UserDTO>> getOnlineUsers() {
        // 24小时内登录过的算在线
        LocalDateTime onlineThreshold = LocalDateTime.now().minusHours(24);
        List<User> onlineUsers = userRepository.findByLastLoginTimeAfter(onlineThreshold);
        
        List<UserDTO> userDTOList = onlineUsers.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
                
        return Result.success(userDTOList, "获取在线用户列表成功");
    }
    
    /**
     * 导出用户数据
     */
    @GetMapping("/export")
    @ApiOperation("导出用户数据")
    public ResponseEntity<byte[]> exportUserData(UserQueryRequest queryRequest) {
        // 构建查询条件
        Specification<User> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 用户名模糊查询
            if (queryRequest.getUsername() != null && !queryRequest.getUsername().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("username"), "%" + queryRequest.getUsername() + "%"));
            }
            
            // 姓名模糊查询
            if (queryRequest.getName() != null && !queryRequest.getName().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + queryRequest.getName() + "%"));
            }
            
            // 邮箱模糊查询
            if (queryRequest.getEmail() != null && !queryRequest.getEmail().isEmpty()) {
                predicates.add(criteriaBuilder.like(root.get("email"), "%" + queryRequest.getEmail() + "%"));
            }
            
            // 状态精确查询
            if (queryRequest.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), queryRequest.getStatus()));
            }
            
            // 创建日期范围查询
            if (queryRequest.getStartDate() != null && queryRequest.getEndDate() != null) {
                predicates.add(criteriaBuilder.between(root.get("createdAt"), queryRequest.getStartDate(), queryRequest.getEndDate()));
            } else if (queryRequest.getStartDate() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), queryRequest.getStartDate()));
            } else if (queryRequest.getEndDate() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), queryRequest.getEndDate()));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
        
        // 构建排序
        String sortBy = queryRequest.getSortBy() != null ? queryRequest.getSortBy() : "id";
        String sortOrder = queryRequest.getSortOrder() != null ? queryRequest.getSortOrder() : "desc";
        Sort sort = Sort.by(Sort.Direction.fromString(sortOrder), sortBy);
        
        // 执行查询，不分页获取所有符合条件的数据
        List<User> users = userRepository.findAll(specification, sort);
        
        // 转换为CSV格式
        StringBuilder csvContent = new StringBuilder();
        csvContent.append("ID,用户名,姓名,邮箱,手机号,状态,角色,最后登录时间,创建时间,更新时间\n");
        
        for (User user : users) {
            csvContent.append(user.getId()).append(",");
            csvContent.append(user.getUsername()).append(",");
            csvContent.append(user.getName() != null ? user.getName() : "").append(",");
            csvContent.append(user.getEmail() != null ? user.getEmail() : "").append(",");
            csvContent.append(user.getPhone() != null ? user.getPhone() : "").append(",");
            
            // 状态
            String status;
            switch (user.getStatus()) {
                case 1:
                    status = "活跃";
                    break;
                case 2:
                    status = "锁定";
                    break;
                case 0:
                    status = "非活跃";
                    break;
                default:
                    status = "未知";
            }
            csvContent.append(status).append(",");
            
            // 角色
            String roles = user.getRoles().stream()
                    .map(Role::getName)
                    .collect(Collectors.joining(";"));
            csvContent.append(roles).append(",");
            
            // 时间
            csvContent.append(user.getLastLoginTime() != null ? user.getLastLoginTime() : "").append(",");
            csvContent.append(user.getCreatedAt() != null ? user.getCreatedAt() : "").append(",");
            csvContent.append(user.getUpdatedAt() != null ? user.getUpdatedAt() : "").append("\n");
        }
        
        // 设置响应头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("text/csv"));
        String filename = "用户数据_" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".csv";
        headers.setContentDispositionFormData("attachment", filename);
        
        return ResponseEntity
                .ok()
                .headers(headers)
                .body(csvContent.toString().getBytes());
    }
    
    /**
     * 转换为DTO
     */
    private UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        BeanUtils.copyProperties(user, dto);
        
        // 设置角色信息
        List<Map<String, Object>> roles = user.getRoles().stream()
                .map(role -> {
                    Map<String, Object> roleMap = new HashMap<>();
                    roleMap.put("id", role.getId());
                    roleMap.put("name", role.getName());
                    roleMap.put("code", role.getCode());
                    return roleMap;
                })
                .collect(Collectors.toList());
        dto.setRoles(roles);
        
        return dto;
    }
} 