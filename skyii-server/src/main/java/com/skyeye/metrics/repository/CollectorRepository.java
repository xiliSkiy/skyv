package com.skyeye.metrics.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.skyeye.metrics.entity.Collector;

/**
 * 采集器配置数据访问层
 */
public interface CollectorRepository extends JpaRepository<Collector, Long> {
    
    /**
     * 根据采集器类型查找采集器
     * @param collectorType 采集器类型
     * @return 采集器列表
     */
    List<Collector> findByCollectorType(String collectorType);
    
    /**
     * 根据采集器状态查找采集器
     * @param status 状态
     * @return 采集器列表
     */
    List<Collector> findByStatus(Integer status);
    
    /**
     * 查找主采集器
     * @return 采集器对象
     */
    Optional<Collector> findByIsMainTrue();
    
    /**
     * 统计各状态的采集器数量
     * @return 状态及对应数量的列表
     */
    @Query("SELECT c.status, COUNT(c) FROM Collector c GROUP BY c.status")
    List<Object[]> countByStatus();
    
    /**
     * 统计指定状态的采集器数量
     * @param status 状态值
     * @return 指定状态的采集器数量
     */
    long countByStatus(Integer status);
} 