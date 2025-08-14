package com.skyeye.device.repository;

import com.skyeye.device.entity.DeviceTemplate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 设备模板数据访问层
 */
@Repository
public interface DeviceTemplateRepository extends JpaRepository<DeviceTemplate, Long> {

    /**
     * 根据编码查找模板
     */
    Optional<DeviceTemplate> findByCode(String code);

    /**
     * 根据设备类型查找模板
     */
    List<DeviceTemplate> findByDeviceTypeIdAndIsEnabledTrue(Long deviceTypeId);

    /**
     * 根据协议查找模板
     */
    List<DeviceTemplate> findByProtocolIdAndIsEnabledTrue(Long protocolId);

    /**
     * 获取启用的模板
     */
    List<DeviceTemplate> findByIsEnabledTrueOrderByUsageCountDesc();

    /**
     * 分页查询模板
     */
    @Query("SELECT dt FROM DeviceTemplate dt WHERE " +
           "(:name IS NULL OR dt.name LIKE %:name%) AND " +
           "(:code IS NULL OR dt.code LIKE %:code%) AND " +
           "(:deviceTypeId IS NULL OR dt.deviceTypeId = :deviceTypeId) AND " +
           "(:protocolId IS NULL OR dt.protocolId = :protocolId) AND " +
           "(:manufacturer IS NULL OR dt.manufacturer LIKE %:manufacturer%) AND " +
           "(:isEnabled IS NULL OR dt.isEnabled = :isEnabled)")
    Page<DeviceTemplate> findByConditions(@Param("name") String name,
                                          @Param("code") String code,
                                          @Param("deviceTypeId") Long deviceTypeId,
                                          @Param("protocolId") Long protocolId,
                                          @Param("manufacturer") String manufacturer,
                                          @Param("isEnabled") Boolean isEnabled,
                                          Pageable pageable);

    /**
     * 检查编码是否唯一（排除自身）
     */
    @Query("SELECT COUNT(dt) FROM DeviceTemplate dt WHERE dt.code = :code AND (:id IS NULL OR dt.id != :id)")
    long countByCodeAndIdNot(@Param("code") String code, @Param("id") Long id);

    /**
     * 检查名称是否唯一（排除自身）
     */
    @Query("SELECT COUNT(dt) FROM DeviceTemplate dt WHERE dt.name = :name AND (:id IS NULL OR dt.id != :id)")
    long countByNameAndIdNot(@Param("name") String name, @Param("id") Long id);

    /**
     * 统计使用该模板的设备数量
     */
    @Query("SELECT COUNT(d) FROM Device d WHERE d.templateId = :templateId")
    Long countDevicesByTemplateId(@Param("templateId") Long templateId);
} 