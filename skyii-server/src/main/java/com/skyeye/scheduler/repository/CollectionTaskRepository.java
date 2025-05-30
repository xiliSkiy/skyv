package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.CollectionTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 采集任务数据访问层
 */
@Repository
public interface CollectionTaskRepository extends JpaRepository<CollectionTask, Long> {

    /**
     * 根据批次ID查询采集任务
     * @param batchId 批次ID
     * @return 采集任务列表
     */
    List<CollectionTask> findByBatchId(Long batchId);
    
    /**
     * 根据批次ID和状态查询采集任务
     * @param batchId 批次ID
     * @param status 状态
     * @return 采集任务列表
     */
    List<CollectionTask> findByBatchIdAndStatus(Long batchId, String status);
    
    /**
     * 统计批次中的任务状态数量
     * @param batchId 批次ID
     * @param status 状态
     * @return 数量
     */
    Long countByBatchIdAndStatus(Long batchId, String status);
    
    /**
     * 查询设备最近的采集任务
     * @param deviceId 设备ID
     * @param metricId 指标ID
     * @param limit 限制数量
     * @return 采集任务列表
     */
    List<CollectionTask> findByDeviceIdAndMetricIdOrderByCreatedAtDesc(Long deviceId, Long metricId);
    
    /**
     * 更新任务状态
     * @param id 任务ID
     * @param status 状态
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE CollectionTask ct SET ct.status = :status WHERE ct.id = :id")
    int updateStatus(@Param("id") Long id, @Param("status") String status);
    
    /**
     * 更新任务执行结果
     * @param id 任务ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param executionTime 执行时间
     * @param result 结果
     * @param errorMessage 错误信息
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE CollectionTask ct SET ct.status = :status, ct.startTime = :startTime, " +
           "ct.endTime = :endTime, ct.executionTime = :executionTime, " +
           "ct.result = :result, ct.errorMessage = :errorMessage " +
           "WHERE ct.id = :id")
    int updateTaskResult(@Param("id") Long id, 
                       @Param("status") String status,
                       @Param("startTime") LocalDateTime startTime,
                       @Param("endTime") LocalDateTime endTime,
                       @Param("executionTime") Long executionTime,
                       @Param("result") String result,
                       @Param("errorMessage") String errorMessage);
} 