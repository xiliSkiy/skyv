package com.skyeye.collector.repository;

import com.skyeye.collector.entity.CollectionData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

/**
 * 采集数据Repository
 * 
 * @author SkyEye Team
 */
@Repository
public interface CollectionDataRepository extends JpaRepository<CollectionData, Long>, JpaSpecificationExecutor<CollectionData> {

    /**
     * 根据设备ID查询采集数据
     */
    List<CollectionData> findByDeviceIdOrderByCollectedAtDesc(Long deviceId);

    /**
     * 根据设备ID分页查询采集数据
     */
    Page<CollectionData> findByDeviceIdOrderByCollectedAtDesc(Long deviceId, Pageable pageable);

    /**
     * 根据设备ID和指标名称查询采集数据
     */
    List<CollectionData> findByDeviceIdAndMetricNameOrderByCollectedAtDesc(Long deviceId, String metricName);

    /**
     * 根据时间范围查询采集数据
     */
    @Query("SELECT cd FROM CollectionData cd WHERE cd.collectedAt BETWEEN :startTime AND :endTime ORDER BY cd.collectedAt DESC")
    List<CollectionData> findByTimeRange(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime);

    /**
     * 根据设备ID和时间范围查询采集数据
     */
    @Query("SELECT cd FROM CollectionData cd WHERE cd.deviceId = :deviceId AND cd.collectedAt BETWEEN :startTime AND :endTime ORDER BY cd.collectedAt DESC")
    List<CollectionData> findByDeviceAndTimeRange(@Param("deviceId") Long deviceId, 
                                                 @Param("startTime") Timestamp startTime, 
                                                 @Param("endTime") Timestamp endTime);

    /**
     * 根据插件类型查询采集数据
     */
    List<CollectionData> findByPluginTypeOrderByCollectedAtDesc(String pluginType);

    /**
     * 查询最新的采集数据
     */
    @Query("SELECT cd FROM CollectionData cd WHERE cd.deviceId = :deviceId AND cd.metricName = :metricName ORDER BY cd.collectedAt DESC")
    List<CollectionData> findLatestByDeviceAndMetric(@Param("deviceId") Long deviceId, 
                                                    @Param("metricName") String metricName, 
                                                    Pageable pageable);

    /**
     * 统计设备的采集数据数量
     */
    @Query("SELECT COUNT(cd) FROM CollectionData cd WHERE cd.deviceId = :deviceId")
    Long countByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 统计指定时间范围内的采集数据数量
     */
    @Query("SELECT COUNT(cd) FROM CollectionData cd WHERE cd.collectedAt BETWEEN :startTime AND :endTime")
    Long countByTimeRange(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime);

    /**
     * 删除过期数据
     */
    @Modifying
    @Query("DELETE FROM CollectionData cd WHERE cd.expiresAt < :now")
    int deleteExpiredData(@Param("now") Timestamp now);

    /**
     * 删除指定时间之前的数据
     */
    @Modifying
    @Query("DELETE FROM CollectionData cd WHERE cd.collectedAt < :beforeTime")
    int deleteDataBefore(@Param("beforeTime") Timestamp beforeTime);

    /**
     * 根据任务ID查询采集数据
     */
    List<CollectionData> findByTaskIdOrderByCollectedAtDesc(Long taskId);

    /**
     * 查询数据质量评分低于指定值的数据
     */
    @Query("SELECT cd FROM CollectionData cd WHERE cd.qualityScore < :minScore ORDER BY cd.collectedAt DESC")
    List<CollectionData> findLowQualityData(@Param("minScore") Integer minScore);

    /**
     * 统计各插件类型的数据量
     */
    @Query("SELECT cd.pluginType, COUNT(cd) FROM CollectionData cd GROUP BY cd.pluginType")
    List<Object[]> countByPluginType();

    /**
     * 查询指定会话的采集数据
     */
    List<CollectionData> findBySessionIdOrderByCollectedAtDesc(String sessionId);
}
