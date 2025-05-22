package com.skyeye.collector.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.skyeye.collector.entity.Collector;

/**
 * 采集器数据访问层
 */
@Repository
public interface DeviceCollectorRepository extends JpaRepository<Collector, Long>, JpaSpecificationExecutor<Collector> {
    
    /**
     * 根据采集器名称查询
     * @param collectorName 采集器名称
     * @return 采集器
     */
    Optional<Collector> findByCollectorName(String collectorName);
    
    /**
     * 根据采集器类型查询
     * @param collectorType 采集器类型
     * @return 采集器列表
     */
    List<Collector> findByCollectorType(String collectorType);
    
    /**
     * 根据状态查询
     * @param status 状态
     * @return 采集器列表
     */
    List<Collector> findByStatus(Integer status);
    
    /**
     * 查询主采集器
     * @return 主采集器
     */
    Optional<Collector> findByIsMainTrue();
    
    /**
     * 根据状态统计
     * @return 状态统计
     */
    @Query("SELECT c.status, COUNT(c) FROM Collector c GROUP BY c.status")
    List<Object[]> countByStatus();
    
    /**
     * 根据状态统计数量
     * @param status 状态
     * @return 数量
     */
    long countByStatus(Integer status);
} 