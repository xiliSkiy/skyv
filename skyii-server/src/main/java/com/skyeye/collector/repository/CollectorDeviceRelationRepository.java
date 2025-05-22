package com.skyeye.collector.repository;

import com.skyeye.collector.entity.CollectorDeviceRelation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 采集器与设备关联仓库接口
 */
public interface CollectorDeviceRelationRepository extends JpaRepository<CollectorDeviceRelation, Long> {

    /**
     * 根据采集器ID查询关联关系
     * @param collectorId 采集器ID
     * @return 关联关系列表
     */
    List<CollectorDeviceRelation> findByCollectorId(Long collectorId);
    
    /**
     * 根据设备ID查询关联关系
     * @param deviceId 设备ID
     * @return 关联关系列表
     */
    List<CollectorDeviceRelation> findByDeviceId(Long deviceId);
    
    /**
     * 根据采集器ID和设备ID查询关联关系
     * @param collectorId 采集器ID
     * @param deviceId 设备ID
     * @return 关联关系
     */
    Optional<CollectorDeviceRelation> findByCollectorIdAndDeviceId(Long collectorId, Long deviceId);
    
    /**
     * 根据采集器ID删除关联关系
     * @param collectorId 采集器ID
     */
    void deleteByCollectorId(Long collectorId);
    
    /**
     * 根据设备ID删除关联关系
     * @param deviceId 设备ID
     */
    void deleteByDeviceId(Long deviceId);
    
    /**
     * 分页查询采集器关联的设备
     * @param collectorId 采集器ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<CollectorDeviceRelation> findByCollectorId(Long collectorId, Pageable pageable);
    
    /**
     * 统计采集器关联的设备数量
     * @param collectorId 采集器ID
     * @return 设备数量
     */
    long countByCollectorId(Long collectorId);
    
    /**
     * 根据设备类型查询采集器关联的设备
     * @param collectorId 采集器ID
     * @param deviceType 设备类型
     * @param pageable 分页参数
     * @return 分页结果
     */
    @Query("SELECT cdr FROM CollectorDeviceRelation cdr JOIN com.skyeye.device.entity.Device d ON cdr.deviceId = d.id " +
           "WHERE cdr.collectorId = :collectorId AND d.type = :deviceType")
    Page<CollectorDeviceRelation> findByCollectorIdAndDeviceType(
            @Param("collectorId") Long collectorId,
            @Param("deviceType") String deviceType,
            Pageable pageable);
} 