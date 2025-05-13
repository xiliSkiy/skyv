package com.skyeye.auth.repository;

import com.skyeye.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户仓库接口
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {
    
    /**
     * 根据用户名查找用户
     */
    Optional<User> findByUsername(String username);
    
    /**
     * 根据邮箱查找用户
     */
    Optional<User> findByEmail(String email);
    
    /**
     * 检查用户名是否存在
     */
    boolean existsByUsername(String username);
    
    /**
     * 检查邮箱是否存在
     */
    boolean existsByEmail(String email);
    
    /**
     * 统计指定状态的用户数量
     */
    long countByStatus(Integer status);
    
    /**
     * 统计指定时间之后登录的用户数量
     */
    long countByLastLoginTimeAfter(LocalDateTime time);
    
    /**
     * 查找在线用户列表
     */
    List<User> findByLastLoginTimeAfter(LocalDateTime time);
    
    /**
     * 按角色ID查询用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId")
    List<User> findByRoleId(Long roleId);
    
    /**
     * 统计最近注册的用户数量
     */
    long countByCreatedAtAfter(LocalDateTime time);
    
    /**
     * 统计最近更新的用户数量
     */
    long countByUpdatedAtAfter(LocalDateTime time);
} 