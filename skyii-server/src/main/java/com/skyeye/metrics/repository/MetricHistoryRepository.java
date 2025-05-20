package com.skyeye.metrics.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.skyeye.metrics.entity.MetricHistory;

/**
 * 指标采集历史数据访问层
 */
public interface MetricHistoryRepository extends JpaRepository<MetricHistory, Long> {
    
    /**
     * 根据指标ID查询采集历史
     * @param metricId 指标ID
     * @param pageable 分页参数
     * @return 采集历史分页数据
     */
    Page<MetricHistory> findByMetricId(Long metricId, Pageable pageable);
    
    /**
     * 根据设备ID查询采集历史
     * @param deviceId 设备ID
     * @param pageable 分页参数
     * @return 采集历史分页数据
     */
    Page<MetricHistory> findByDeviceId(Long deviceId, Pageable pageable);
    
    /**
     * 根据采集器ID查询采集历史
     * @param collectorId 采集器ID
     * @param pageable 分页参数
     * @return 采集历史分页数据
     */
    Page<MetricHistory> findByCollectorId(Long collectorId, Pageable pageable);
    
    /**
     * 根据状态查询采集历史
     * @param status 状态
     * @param pageable 分页参数
     * @return 采集历史分页数据
     */
    Page<MetricHistory> findByStatus(Integer status, Pageable pageable);
    
    /**
     * 根据采集时间范围查询采集历史
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 采集历史分页数据
     */
    Page<MetricHistory> findByCollectionTimeBetween(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * 根据条件组合查询采集历史
     * @param metricId 指标ID
     * @param deviceId 设备ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 采集历史分页数据
     */
    @Query("SELECT h FROM MetricHistory h WHERE " +
           "(:metricId IS NULL OR h.metricId = :metricId) AND " +
           "(:deviceId IS NULL OR h.deviceId = :deviceId) AND " +
           "(:status IS NULL OR h.status = :status) AND " +
           "(:startTime IS NULL OR h.collectionTime >= :startTime) AND " +
           "(:endTime IS NULL OR h.collectionTime <= :endTime)")
    Page<MetricHistory> findByConditions(
            @Param("metricId") Long metricId,
            @Param("deviceId") Long deviceId,
            @Param("status") Integer status,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime,
            Pageable pageable);
    
    /**
     * 获取最近的采集历史
     * @param metricId 指标ID
     * @param limit 限制条数
     * @return 采集历史列表
     */
    List<MetricHistory> findTop10ByMetricIdOrderByCollectionTimeDesc(Long metricId);
    
    /**
     * 统计指定时间范围内的采集次数
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 采集次数
     */
    long countByCollectionTimeBetween(LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 统计指定指标的采集次数
     * @param metricId 指标ID
     * @return 采集次数
     */
    long countByMetricId(Long metricId);
    
    /**
     * 统计指定状态的采集次数
     * @param status 状态
     * @return 采集次数
     */
    long countByStatus(Integer status);
} 