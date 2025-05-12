package com.skyeye.config.init;

import com.skyeye.auth.entity.Role;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.repository.RoleRepository;
import com.skyeye.auth.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * 数据初始化类
 * 系统启动时初始化基础数据
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) throws Exception {
        log.info("开始初始化基础数据...");
        
        initRoles();
        initAdminUser();
        
        log.info("基础数据初始化完成");
    }

    /**
     * 初始化角色
     */
    private void initRoles() {
        if (roleRepository.count() == 0) {
            log.info("初始化角色数据");
            
            // 创建管理员角色
            Role adminRole = new Role();
            adminRole.setName("管理员");
            adminRole.setCode("ADMIN");
            adminRole.setDescription("系统管理员，拥有所有权限");
            roleRepository.save(adminRole);
            
            // 创建普通用户角色
            Role userRole = new Role();
            userRole.setName("普通用户");
            userRole.setCode("USER");
            userRole.setDescription("普通用户，拥有基本权限");
            roleRepository.save(userRole);
            
            // 创建操作员角色
            Role operatorRole = new Role();
            operatorRole.setName("操作员");
            operatorRole.setCode("OPERATOR");
            operatorRole.setDescription("系统操作员，拥有操作权限");
            roleRepository.save(operatorRole);
            
            log.info("角色数据初始化完成");
        }
    }

    /**
     * 初始化管理员用户
     */
    private void initAdminUser() {
        if (!userRepository.existsByUsername("admin")) {
            log.info("初始化管理员用户");
            
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setName("系统管理员");
            adminUser.setEmail("admin@skyeye.com");
            adminUser.setStatus(1);
            
            // 设置管理员角色
            Set<Role> roles = new HashSet<>();
            roleRepository.findByCode("ADMIN").ifPresent(roles::add);
            adminUser.setRoles(roles);
            
            userRepository.save(adminUser);
            
            log.info("管理员用户初始化完成");
        }
    }
} 