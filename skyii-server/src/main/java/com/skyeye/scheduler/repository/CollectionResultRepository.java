package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.CollectionResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集结果数据访问层
 */
@Repository
public interface CollectionResultRepository extends JpaRepository<CollectionResult, Long> {

    /**
     * 根据任务ID查询采集结果
     * @param taskId 任务ID
     * @return 采集结果列表
     */
    List<CollectionResult> findByTaskId(Long taskId);
    
    /**
     * 根据设备ID和指标ID查询最近的采集结果
     * @param deviceId 设备ID
     * @param metricId 指标ID
     * @param limit 限制数量
     * @return 采集结果列表
     */
    @Query(nativeQuery = true, value = "SELECT * FROM tb_collection_result WHERE device_id = :deviceId AND metric_id = :metricId ORDER BY collection_time DESC LIMIT :limit")
    List<CollectionResult> findRecentResults(@Param("deviceId") Long deviceId, @Param("metricId") Long metricId, @Param("limit") int limit);
    
    /**
     * 查询时间范围内的采集结果
     * @param deviceId 设备ID
     * @param metricId 指标ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 采集结果列表
     */
    List<CollectionResult> findByDeviceIdAndMetricIdAndCollectionTimeBetween(Long deviceId, Long metricId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 查询采集器的采集结果
     * @param collectorId 采集器ID
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 采集结果列表
     */
    List<CollectionResult> findByCollectorIdAndCollectionTimeBetween(Long collectorId, LocalDateTime startTime, LocalDateTime endTime);
} 