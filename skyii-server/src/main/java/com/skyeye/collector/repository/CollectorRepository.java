package com.skyeye.collector.repository;

import com.skyeye.collector.entity.Collector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 采集端仓库接口
 */
@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {
    
    /**
     * 根据采集端ID查询采集端
     * 
     * @param collectorId 采集端ID
     * @return 采集端
     */
    Optional<Collector> findByCollectorId(String collectorId);
    
    /**
     * 根据采集器名称查询
     * @param collectorName 采集器名称
     * @return 采集器
     */
    Optional<Collector> findByCollectorName(String collectorName);
    
    /**
     * 根据状态查询
     * @param status 状态
     * @return 采集器列表
     */
    List<Collector> findByStatus(String status);
    
    /**
     * 根据状态查询（兼容Integer参数）
     * @param status 状态
     * @return 采集器列表
     */
    default List<Collector> findByStatus(Integer status) {
        return findByStatus(status != null ? status.toString() : null);
    }
    
    /**
     * 查询超时的采集器
     * @param timeoutThreshold 超时阈值
     * @param status 当前状态
     * @return 超时的采集器列表
     */
    @Query("SELECT c FROM Collector c WHERE c.lastHeartbeatTime < :timeoutThreshold AND c.status = :status")
    List<Collector> findTimeoutCollectors(@Param("timeoutThreshold") LocalDateTime timeoutThreshold, @Param("status") String status);
    
    /**
     * 查询超时的采集器（兼容Integer参数）
     * @param timeoutThreshold 超时阈值
     * @param status 当前状态
     * @return 超时的采集器列表
     */
    default List<Collector> findTimeoutCollectors(LocalDateTime timeoutThreshold, int status) {
        return findTimeoutCollectors(timeoutThreshold, String.valueOf(status));
    }
} 