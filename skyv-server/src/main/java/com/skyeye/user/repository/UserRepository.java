package com.skyeye.user.repository;

import com.skyeye.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long>, JpaSpecificationExecutor<User> {

    /**
     * 根据用户名查找用户（包含软删除的用户）
     * 
     * @param username 用户名
     * @return 用户信息
     */
    Optional<User> findByUsername(String username);

    /**
     * 根据用户名查找活跃用户（排除软删除的用户）
     * 
     * @param username 用户名
     * @return 用户信息
     */
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles r LEFT JOIN FETCH r.permissions WHERE u.username = :username AND u.deletedAt IS NULL")
    Optional<User> findActiveByUsername(@Param("username") String username);

    /**
     * 根据邮箱查找用户
     * 
     * @param email 邮箱
     * @return 用户信息
     */
    Optional<User> findByEmail(String email);

    /**
     * 根据手机号查找用户
     * 
     * @param phone 手机号
     * @return 用户信息
     */
    Optional<User> findByPhone(String phone);

    /**
     * 检查用户名是否存在
     * 
     * @param username 用户名
     * @return true如果存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     * 
     * @param email 邮箱
     * @return true如果存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     * 
     * @param phone 手机号
     * @return true如果存在
     */
    boolean existsByPhone(String phone);

    /**
     * 查找活跃用户列表（排除软删除的用户）
     * 
     * @param pageable 分页参数
     * @return 用户分页列表
     */
    @Query("SELECT u FROM User u WHERE u.deletedAt IS NULL")
    Page<User> findActiveUsers(Pageable pageable);

    /**
     * 根据状态查找用户
     * 
     * @param status 用户状态
     * @return 用户列表
     */
    List<User> findByStatus(Integer status);

    /**
     * 根据角色查找用户
     * 
     * @param roleId 角色ID
     * @return 用户列表
     */
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.id = :roleId AND u.deletedAt IS NULL")
    List<User> findByRoleId(@Param("roleId") Long roleId);

    /**
     * 统计用户总数（排除软删除）
     * 
     * @return 用户总数
     */
    @Query("SELECT COUNT(u) FROM User u WHERE u.deletedAt IS NULL")
    Long countActiveUsers();

    /**
     * 根据状态统计用户数量
     * 
     * @param status 用户状态
     * @return 用户数量
     */
    Long countByStatus(Integer status);

    /**
     * 查找需要解锁的用户（锁定时间已过期）
     * 
     * @param currentTime 当前时间
     * @return 需要解锁的用户列表
     */
    @Query("SELECT u FROM User u WHERE u.status = 2 AND u.lockedTime IS NOT NULL AND u.lockedTime < :currentTime")
    List<User> findUsersToUnlock(@Param("currentTime") LocalDateTime currentTime);

    /**
     * 批量解锁用户
     * 
     * @param userIds 用户ID列表
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 1, u.lockedTime = NULL, u.loginFailCount = 0 WHERE u.id IN :userIds")
    void unlockUsers(@Param("userIds") List<Long> userIds);

    /**
     * 软删除用户
     * 
     * @param id 用户ID
     * @param deletedAt 删除时间
     */
    @Modifying
    @Query("UPDATE User u SET u.deletedAt = :deletedAt WHERE u.id = :id")
    void softDelete(@Param("id") Long id, @Param("deletedAt") LocalDateTime deletedAt);

    /**
     * 更新用户最后登录信息
     * 
     * @param id 用户ID
     * @param lastLoginTime 最后登录时间
     * @param lastLoginIp 最后登录IP
     */
    @Modifying
    @Query("UPDATE User u SET u.lastLoginTime = :lastLoginTime, u.lastLoginIp = :lastLoginIp, " +
           "u.loginFailCount = 0, u.lockedTime = NULL WHERE u.id = :id")
    void updateLastLogin(@Param("id") Long id, 
                        @Param("lastLoginTime") LocalDateTime lastLoginTime,
                        @Param("lastLoginIp") String lastLoginIp);

    /**
     * 增加登录失败次数
     * 
     * @param id 用户ID
     */
    @Modifying
    @Query("UPDATE User u SET u.loginFailCount = u.loginFailCount + 1 WHERE u.id = :id")
    void incrementLoginFailCount(@Param("id") Long id);

    /**
     * 锁定用户账户
     * 
     * @param id 用户ID
     * @param lockedTime 锁定时间
     */
    @Modifying
    @Query("UPDATE User u SET u.status = 2, u.lockedTime = :lockedTime WHERE u.id = :id")
    void lockUser(@Param("id") Long id, @Param("lockedTime") LocalDateTime lockedTime);
} 