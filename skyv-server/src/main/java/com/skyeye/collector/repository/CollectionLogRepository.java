package com.skyeye.collector.repository;

import com.skyeye.collector.entity.CollectionLog;
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
import java.util.Optional;

/**
 * 采集日志Repository
 * 
 * @author SkyEye Team
 */
@Repository
public interface CollectionLogRepository extends JpaRepository<CollectionLog, Long>, JpaSpecificationExecutor<CollectionLog> {

    /**
     * 根据执行ID查询日志
     */
    Optional<CollectionLog> findByExecutionId(String executionId);

    /**
     * 根据任务ID查询日志
     */
    List<CollectionLog> findByTaskIdOrderByStartTimeDesc(Long taskId);

    /**
     * 根据任务ID分页查询日志
     */
    Page<CollectionLog> findByTaskIdOrderByStartTimeDesc(Long taskId, Pageable pageable);

    /**
     * 根据设备ID查询日志
     */
    List<CollectionLog> findByDeviceIdOrderByStartTimeDesc(Long deviceId);

    /**
     * 根据设备ID分页查询日志
     */
    Page<CollectionLog> findByDeviceIdOrderByStartTimeDesc(Long deviceId, Pageable pageable);

    /**
     * 根据状态查询日志
     */
    List<CollectionLog> findByStatusOrderByStartTimeDesc(String status);

    /**
     * 查询失败的采集日志
     */
    @Query("SELECT cl FROM CollectionLog cl WHERE cl.success = false ORDER BY cl.startTime DESC")
    List<CollectionLog> findFailedLogs();

    /**
     * 查询失败的采集日志（分页）
     */
    @Query("SELECT cl FROM CollectionLog cl WHERE cl.success = false ORDER BY cl.startTime DESC")
    Page<CollectionLog> findFailedLogs(Pageable pageable);

    /**
     * 根据插件类型查询日志
     */
    List<CollectionLog> findByPluginTypeOrderByStartTimeDesc(String pluginType);

    /**
     * 根据时间范围查询日志
     */
    @Query("SELECT cl FROM CollectionLog cl WHERE cl.startTime BETWEEN :startTime AND :endTime ORDER BY cl.startTime DESC")
    List<CollectionLog> findByTimeRange(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime);

    /**
     * 统计成功率
     */
    @Query("SELECT COUNT(cl), SUM(CASE WHEN cl.success = true THEN 1 ELSE 0 END) FROM CollectionLog cl WHERE cl.startTime BETWEEN :startTime AND :endTime")
    Object[] getSuccessRateInTimeRange(@Param("startTime") Timestamp startTime, @Param("endTime") Timestamp endTime);

    /**
     * 统计各状态的数量
     */
    @Query("SELECT cl.status, COUNT(cl) FROM CollectionLog cl GROUP BY cl.status")
    List<Object[]> countByStatus();

    /**
     * 统计各插件类型的执行次数
     */
    @Query("SELECT cl.pluginType, COUNT(cl) FROM CollectionLog cl GROUP BY cl.pluginType")
    List<Object[]> countByPluginType();

    /**
     * 查询平均响应时间
     */
    @Query("SELECT AVG(cl.responseTime) FROM CollectionLog cl WHERE cl.success = true AND cl.responseTime IS NOT NULL")
    Double getAverageResponseTime();

    /**
     * 根据插件类型查询平均响应时间
     */
    @Query("SELECT AVG(cl.responseTime) FROM CollectionLog cl WHERE cl.pluginType = :pluginType AND cl.success = true AND cl.responseTime IS NOT NULL")
    Double getAverageResponseTimeByPlugin(@Param("pluginType") String pluginType);

    /**
     * 查询最近的执行日志
     */
    @Query("SELECT cl FROM CollectionLog cl WHERE cl.deviceId = :deviceId AND cl.metricName = :metricName ORDER BY cl.startTime DESC")
    List<CollectionLog> findRecentLogs(@Param("deviceId") Long deviceId, 
                                      @Param("metricName") String metricName, 
                                      Pageable pageable);

    /**
     * 删除指定时间之前的日志
     */
    @Modifying
    @Query("DELETE FROM CollectionLog cl WHERE cl.startTime < :beforeTime")
    int deleteLogsBefore(@Param("beforeTime") Timestamp beforeTime);

    /**
     * 查询正在运行的任务
     */
    @Query("SELECT cl FROM CollectionLog cl WHERE cl.status = 'RUNNING' ORDER BY cl.startTime DESC")
    List<CollectionLog> findRunningLogs();

    /**
     * 统计重试次数大于指定值的日志
     */
    @Query("SELECT COUNT(cl) FROM CollectionLog cl WHERE cl.retryCount > :minRetryCount")
    Long countHighRetryLogs(@Param("minRetryCount") Integer minRetryCount);

    /**
     * 查询超时的执行日志
     */
    @Query("SELECT cl FROM CollectionLog cl WHERE cl.status = 'RUNNING' AND cl.startTime < :timeoutThreshold")
    List<CollectionLog> findTimeoutLogs(@Param("timeoutThreshold") Timestamp timeoutThreshold);
}
