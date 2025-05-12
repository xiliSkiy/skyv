package com.skyeye.auth.repository;

import com.skyeye.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 角色仓库接口
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    
    /**
     * 根据角色编码查找角色
     */
    Optional<Role> findByCode(String code);
    
    /**
     * 根据角色名称查找角色
     */
    Optional<Role> findByName(String name);
} 