package com.skyeye.scheduler.repository;

import com.skyeye.collector.entity.Collector;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 采集器数据访问层
 */
@Repository
public interface CollectorRepository extends JpaRepository<Collector, Long> {

    /**
     * 根据采集器名称查询
     * @param collectorName 采集器名称
     * @return 采集器信息
     */
    Optional<Collector> findByCollectorName(String collectorName);
    
    /**
     * 根据状态查询采集器列表
     * @param status 状态
     * @return 采集器列表
     */
    List<Collector> findByStatus(Integer status);
    
    /**
     * 更新采集器心跳时间
     * @param id 采集器ID
     * @param time 心跳时间
     * @param status 状态
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE Collector c SET c.lastHeartbeat = :time, c.status = :status WHERE c.id = :id")
    int updateHeartbeat(@Param("id") Long id, @Param("time") LocalDateTime time, @Param("status") Integer status);
    
    /**
     * 查询超过指定时间未心跳的采集器
     * @param time 比较时间
     * @param status 当前状态
     * @return 采集器列表
     */
    @Query("SELECT c FROM Collector c WHERE c.lastHeartbeat < :time AND c.status = :status")
    List<Collector> findTimeoutCollectors(@Param("time") LocalDateTime time, @Param("status") Integer status);
} 