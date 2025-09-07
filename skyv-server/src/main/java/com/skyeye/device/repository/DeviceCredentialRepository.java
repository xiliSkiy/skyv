package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceCredential;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备凭据数据访问层
 * 
 * @author SkyEye Team
 */
@Repository
public interface DeviceCredentialRepository extends JpaRepository<DeviceCredential, Long>, 
        JpaSpecificationExecutor<DeviceCredential> {

    /**
     * 根据设备ID查找凭据列表
     *
     * @param deviceId 设备ID
     * @return 凭据列表
     */
    List<DeviceCredential> findByDeviceId(Long deviceId);

    /**
     * 根据设备ID和状态查找凭据列表
     *
     * @param deviceId 设备ID
     * @param status 状态
     * @return 凭据列表
     */
    List<DeviceCredential> findByDeviceIdAndStatus(Long deviceId, Integer status);

    /**
     * 根据设备ID查找默认凭据
     *
     * @param deviceId 设备ID
     * @param isDefault 是否默认
     * @return 默认凭据
     */
    Optional<DeviceCredential> findByDeviceIdAndIsDefault(Long deviceId, Boolean isDefault);

    /**
     * 根据设备ID、协议类型查找凭据
     *
     * @param deviceId 设备ID
     * @param protocolType 协议类型
     * @return 凭据列表
     */
    List<DeviceCredential> findByDeviceIdAndProtocolType(Long deviceId, String protocolType);

    /**
     * 根据设备ID、协议类型查找默认凭据
     *
     * @param deviceId 设备ID
     * @param protocolType 协议类型
     * @param isDefault 是否默认
     * @return 默认凭据
     */
    Optional<DeviceCredential> findByDeviceIdAndProtocolTypeAndIsDefault(
            Long deviceId, String protocolType, Boolean isDefault);

    /**
     * 检查设备是否存在指定名称的凭据
     *
     * @param deviceId 设备ID
     * @param credentialName 凭据名称
     * @return 是否存在
     */
    boolean existsByDeviceIdAndCredentialName(Long deviceId, String credentialName);

    /**
     * 检查设备是否存在指定名称的凭据（排除指定ID）
     *
     * @param deviceId 设备ID
     * @param credentialName 凭据名称
     * @param excludeId 排除的凭据ID
     * @return 是否存在
     */
    boolean existsByDeviceIdAndCredentialNameAndIdNot(Long deviceId, String credentialName, Long excludeId);

    /**
     * 根据加密密钥ID查找使用该密钥的凭据
     *
     * @param encryptionKeyId 加密密钥ID
     * @return 凭据列表
     */
    List<DeviceCredential> findByEncryptionKeyId(Long encryptionKeyId);

    /**
     * 统计设备的凭据数量
     *
     * @param deviceId 设备ID
     * @return 凭据数量
     */
    long countByDeviceId(Long deviceId);

    /**
     * 统计使用指定加密密钥的凭据数量
     *
     * @param encryptionKeyId 加密密钥ID
     * @return 凭据数量
     */
    long countByEncryptionKeyId(Long encryptionKeyId);

    /**
     * 分页查询设备凭据
     *
     * @param deviceId 设备ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<DeviceCredential> findByDeviceId(Long deviceId, Pageable pageable);

    /**
     * 查询需要密钥轮换的凭据
     *
     * @param oldKeyId 旧密钥ID
     * @return 凭据列表
     */
    @Query("SELECT dc FROM DeviceCredential dc WHERE dc.encryptionKeyId = :oldKeyId AND dc.status = 1")
    List<DeviceCredential> findCredentialsNeedKeyRotation(@Param("oldKeyId") Long oldKeyId);

    /**
     * 批量更新凭据的加密密钥ID
     *
     * @param oldKeyId 旧密钥ID
     * @param newKeyId 新密钥ID
     * @return 更新数量
     */
    @Query("UPDATE DeviceCredential dc SET dc.encryptionKeyId = :newKeyId " +
           "WHERE dc.encryptionKeyId = :oldKeyId AND dc.status = 1")
    int updateEncryptionKeyId(@Param("oldKeyId") Long oldKeyId, @Param("newKeyId") Long newKeyId);
}
