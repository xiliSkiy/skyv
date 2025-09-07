package com.skyeye.common.config;

import com.skyeye.user.entity.Permission;
import com.skyeye.user.entity.Role;
import com.skyeye.user.entity.User;
import com.skyeye.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * 数据初始化器
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    @PersistenceContext
    private EntityManager entityManager;
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Starting data initialization...");
        
        try {
            // 初始化权限
            initializePermissions();
            
            // 初始化角色
            initializeRoles();
            
            // 初始化默认用户
            initializeDefaultUsers();
            
            log.info("Data initialization completed successfully");
        } catch (Exception e) {
            log.error("Data initialization failed", e);
        }
    }

    /**
     * 初始化系统权限
     */
    private void initializePermissions() {
        log.info("Initializing permissions...");
        
        List<PermissionData> permissions = Arrays.asList(
            // 用户管理权限
            new PermissionData("user:view", "查看用户", "用户查看权限", "user", "view", "API"),
            new PermissionData("user:create", "创建用户", "用户创建权限", "user", "create", "API"),
            new PermissionData("user:update", "更新用户", "用户更新权限", "user", "update", "API"),
            new PermissionData("user:delete", "删除用户", "用户删除权限", "user", "delete", "API"),
            
            // 设备管理权限
            new PermissionData("device:view", "查看设备", "设备查看权限", "device", "view", "API"),
            new PermissionData("device:create", "创建设备", "设备创建权限", "device", "create", "API"),
            new PermissionData("device:update", "更新设备", "设备更新权限", "device", "update", "API"),
            new PermissionData("device:delete", "删除设备", "设备删除权限", "device", "delete", "API"),
            new PermissionData("device:control", "控制设备", "设备控制权限", "device", "control", "API"),
            
            // 监控管理权限
            new PermissionData("monitoring:view", "查看监控", "监控查看权限", "monitoring", "view", "API"),
            new PermissionData("monitoring:control", "控制监控", "监控控制权限", "monitoring", "control", "API"),
            
            // 报警管理权限
            new PermissionData("alert:view", "查看报警", "报警查看权限", "alert", "view", "API"),
            new PermissionData("alert:handle", "处理报警", "报警处理权限", "alert", "handle", "API"),
            new PermissionData("alert:config", "配置报警", "报警配置权限", "alert", "config", "API"),
            
            // 任务管理权限
            new PermissionData("task:view", "查看任务", "任务查看权限", "task", "view", "API"),
            new PermissionData("task:create", "创建任务", "任务创建权限", "task", "create", "API"),
            new PermissionData("task:update", "更新任务", "任务更新权限", "task", "update", "API"),
            new PermissionData("task:delete", "删除任务", "任务删除权限", "task", "delete", "API"),
            new PermissionData("task:execute", "执行任务", "任务执行权限", "task", "execute", "API"),
            new PermissionData("task:manage", "管理任务", "任务管理权限", "task", "manage", "API"),
            
            // 系统管理权限
            new PermissionData("system:view", "查看系统", "系统查看权限", "system", "view", "API"),
            new PermissionData("system:config", "系统配置", "系统配置权限", "system", "config", "API"),
            new PermissionData("system:log", "系统日志", "系统日志权限", "system", "log", "API")
        );

        for (PermissionData permissionData : permissions) {
            Permission existingPermission = entityManager
                .createQuery("SELECT p FROM Permission p WHERE p.code = :code", Permission.class)
                .setParameter("code", permissionData.code)
                .getResultStream()
                .findFirst()
                .orElse(null);

            if (existingPermission == null) {
                Permission permission = new Permission();
                permission.setName(permissionData.name);
                permission.setCode(permissionData.code);
                permission.setDescription(permissionData.description);
                permission.setResource(permissionData.resource);
                permission.setAction(permissionData.action);
                permission.setType(permissionData.type);
                permission.setStatus(1);
                permission.setIsSystem(true);
                permission.setSortOrder(0);
                
                entityManager.persist(permission);
            }
        }
        
        entityManager.flush();
        log.info("Permissions initialization completed");
    }

    /**
     * 初始化系统角色
     */
    private void initializeRoles() {
        log.info("Initializing roles...");
        
        // 管理员角色
        Role adminRole = findOrCreateRole("ADMIN", "系统管理员", "系统管理员角色，拥有所有权限");
        List<Permission> allPermissions = entityManager
            .createQuery("SELECT p FROM Permission p WHERE p.isSystem = true", Permission.class)
            .getResultList();
        adminRole.setPermissions(Set.copyOf(allPermissions));
        
        // 普通用户角色
        Role userRole = findOrCreateRole("USER", "普通用户", "普通用户角色，拥有基本查看权限");
        List<Permission> userPermissions = entityManager
            .createQuery("SELECT p FROM Permission p WHERE p.code IN (:codes)", Permission.class)
            .setParameter("codes", Arrays.asList(
                "device:view", "monitoring:view", "alert:view", "task:view"
            ))
            .getResultList();
        userRole.setPermissions(Set.copyOf(userPermissions));
        
        // 设备管理员角色
        Role deviceManagerRole = findOrCreateRole("DEVICE_MANAGER", "设备管理员", "设备管理员角色，负责设备管理");
        List<Permission> devicePermissions = entityManager
            .createQuery("SELECT p FROM Permission p WHERE p.resource IN (:resources)", Permission.class)
            .setParameter("resources", Arrays.asList("device", "monitoring", "alert", "task"))
            .getResultList();
        deviceManagerRole.setPermissions(Set.copyOf(devicePermissions));
        
        entityManager.flush();
        log.info("Roles initialization completed");
    }

    /**
     * 初始化默认用户
     */
    private void initializeDefaultUsers() {
        log.info("Initializing default users...");
        
        // 检查是否已存在管理员用户
        if (!userRepository.existsByUsername("admin")) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123456"));
            adminUser.setRealName("系统管理员");
            adminUser.setEmail("admin@skyeye.com");
            adminUser.setStatus(1);
            adminUser.setIsAdmin(true);
            adminUser.setPasswordChangedTime(LocalDateTime.now());
            
            // 分配管理员角色
            Role adminRole = entityManager
                .createQuery("SELECT r FROM Role r WHERE r.code = 'ADMIN'", Role.class)
                .getSingleResult();
            adminUser.setRoles(Set.of(adminRole));
            
            userRepository.save(adminUser);
            log.info("Default admin user created: admin/admin123456");
        }
        
        // 检查是否已存在测试用户
        if (!userRepository.existsByUsername("user")) {
            User testUser = new User();
            testUser.setUsername("user");
            testUser.setPassword(passwordEncoder.encode("user123456"));
            testUser.setRealName("测试用户");
            testUser.setEmail("user@skyeye.com");
            testUser.setStatus(1);
            testUser.setIsAdmin(false);
            testUser.setPasswordChangedTime(LocalDateTime.now());
            
            // 分配普通用户角色
            Role userRole = entityManager
                .createQuery("SELECT r FROM Role r WHERE r.code = 'USER'", Role.class)
                .getSingleResult();
            testUser.setRoles(Set.of(userRole));
            
            userRepository.save(testUser);
            log.info("Default test user created: user/user123456");
        }
        
        log.info("Default users initialization completed");
    }

    /**
     * 查找或创建角色
     */
    private Role findOrCreateRole(String code, String name, String description) {
        Role role = entityManager
            .createQuery("SELECT r FROM Role r WHERE r.code = :code", Role.class)
            .setParameter("code", code)
            .getResultStream()
            .findFirst()
            .orElse(null);

        if (role == null) {
            role = new Role();
            role.setCode(code);
            role.setName(name);
            role.setDescription(description);
            role.setStatus(1);
            role.setIsSystem(true);
            role.setSortOrder(0);
            
            entityManager.persist(role);
        }
        
        return role;
    }

    /**
     * 权限数据内部类
     */
    private static class PermissionData {
        String code;
        String name;
        String description;
        String resource;
        String action;
        String type;

        PermissionData(String code, String name, String description, String resource, String action, String type) {
            this.code = code;
            this.name = name;
            this.description = description;
            this.resource = resource;
            this.action = action;
            this.type = type;
        }
    }
} 