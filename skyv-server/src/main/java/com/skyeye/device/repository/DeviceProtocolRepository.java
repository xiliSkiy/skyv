package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceProtocol;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备协议数据访问层
 */
@Repository
public interface DeviceProtocolRepository extends JpaRepository<DeviceProtocol, Long> {

    /**
     * 根据编码查找协议
     */
    Optional<DeviceProtocol> findByCode(String code);

    /**
     * 根据名称查找协议
     */
    Optional<DeviceProtocol> findByName(String name);

    /**
     * 获取启用的协议
     */
    List<DeviceProtocol> findByIsEnabledTrueOrderByUsageCountDesc();

    /**
     * 分页查询协议
     */
    @Query("SELECT dp FROM DeviceProtocol dp WHERE " +
           "(:name IS NULL OR dp.name LIKE %:name%) AND " +
           "(:code IS NULL OR dp.code LIKE %:code%) AND " +
           "(:version IS NULL OR dp.version LIKE %:version%) AND " +
           "(:isEnabled IS NULL OR dp.isEnabled = :isEnabled)")
    Page<DeviceProtocol> findByConditions(@Param("name") String name,
                                          @Param("code") String code,
                                          @Param("version") String version,
                                          @Param("isEnabled") Boolean isEnabled,
                                          Pageable pageable);

    /**
     * 检查编码是否唯一（排除自身）
     */
    @Query("SELECT COUNT(dp) FROM DeviceProtocol dp WHERE dp.code = :code AND (:id IS NULL OR dp.id != :id)")
    long countByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查名称是否唯一（排除自身）
     */
    @Query("SELECT COUNT(dp) FROM DeviceProtocol dp WHERE dp.name = :name AND (:id IS NULL OR dp.id != :id)")
    long countByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * 统计使用该协议的设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.protocolId = :protocolId")
    Long countDevicesByProtocolId(@Param("protocolId") Long protocolId);

    /**
     * 统计使用该协议的模板数量
     */
    @Query("SELECT COUNT(dt) FROM DeviceTemplate dt WHERE dt.protocolId = :protocolId")
    Long countTemplatesByProtocolId(@Param("protocolId") Long protocolId);
} 