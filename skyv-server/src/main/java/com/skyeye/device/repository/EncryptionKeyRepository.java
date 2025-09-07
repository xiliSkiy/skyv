package com.skyeye.device.repository;

import com.skyeye.device.entity.EncryptionKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;

/**
 * 加密密钥数据访问层
 * 
 * @author SkyEye Team
 */
@Repository
public interface EncryptionKeyRepository extends JpaRepository<EncryptionKey, Long> {

    /**
     * 查找当前活跃的加密密钥
     *
     * @return 活跃密钥
     */
    Optional<EncryptionKey> findByIsActiveTrue();

    /**
     * 根据状态查找加密密钥列表
     *
     * @param status 状态
     * @return 密钥列表
     */
    List<EncryptionKey> findByStatus(Integer status);

    /**
     * 根据密钥名称查找密钥
     *
     * @param keyName 密钥名称
     * @return 密钥列表
     */
    List<EncryptionKey> findByKeyName(String keyName);

    /**
     * 根据密钥名称和版本查找密钥
     *
     * @param keyName 密钥名称
     * @param keyVersion 密钥版本
     * @return 密钥
     */
    Optional<EncryptionKey> findByKeyNameAndKeyVersion(String keyName, Integer keyVersion);

    /**
     * 查找最大密钥版本号
     *
     * @return 最大版本号
     */
    @Query("SELECT MAX(ek.keyVersion) FROM EncryptionKey ek")
    Optional<Integer> findMaxKeyVersion();

    /**
     * 查找最大密钥版本号（按密钥名称）
     *
     * @param keyName 密钥名称
     * @return 最大版本号
     */
    @Query("SELECT MAX(ek.keyVersion) FROM EncryptionKey ek WHERE ek.keyName = :keyName")
    Optional<Integer> findMaxKeyVersionByKeyName(String keyName);

    /**
     * 查找可用的密钥（状态为1且未过期）
     *
     * @return 可用密钥列表
     */
    @Query("SELECT ek FROM EncryptionKey ek WHERE ek.status = 1 " +
           "AND (ek.expireTime IS NULL OR ek.expireTime > CURRENT_TIMESTAMP) " +
           "ORDER BY ek.effectiveTime DESC")
    List<EncryptionKey> findAvailableKeys();

    /**
     * 查找即将过期的密钥（未来N天内过期）
     *
     * @param endTime 截止时间
     * @return 即将过期的密钥列表
     */
    @Query("SELECT ek FROM EncryptionKey ek WHERE ek.status = 1 " +
           "AND ek.expireTime IS NOT NULL " +
           "AND ek.expireTime <= :endTime")
    List<EncryptionKey> findKeysExpiringBefore(@Param("endTime") Timestamp endTime);

    /**
     * 检查密钥名称是否存在
     *
     * @param keyName 密钥名称
     * @return 是否存在
     */
    boolean existsByKeyName(String keyName);

    /**
     * 统计活跃密钥数量
     *
     * @return 活跃密钥数量
     */
    long countByIsActiveTrue();

    /**
     * 统计指定状态的密钥数量
     *
     * @param status 状态
     * @return 密钥数量
     */
    long countByStatus(Integer status);
}
