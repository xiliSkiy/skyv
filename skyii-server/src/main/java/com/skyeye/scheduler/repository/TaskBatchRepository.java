package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.TaskBatch;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务批次数据访问层
 */
@Repository
public interface TaskBatchRepository extends JpaRepository<TaskBatch, Long> {

    /**
     * 根据任务ID查询批次列表
     * @param taskId 任务ID
     * @return 批次列表
     */
    List<TaskBatch> findByTaskId(Long taskId);
    
    /**
     * 根据采集器ID查询待处理的批次
     * @param collectorId 采集器ID
     * @param status 状态
     * @return 批次列表
     */
    List<TaskBatch> findByCollectorIdAndStatus(Long collectorId, String status);
    
    /**
     * 查询可调度的任务批次（按优先级降序）
     * @param status 状态
     * @param time 当前时间
     * @return 批次列表
     */
    @Query("SELECT tb FROM TaskBatch tb WHERE tb.status = :status AND tb.scheduledTime <= :time ORDER BY tb.priority DESC")
    List<TaskBatch> findSchedulableBatches(@Param("status") String status, @Param("time") LocalDateTime time);
    
    /**
     * 更新批次状态
     * @param id 批次ID
     * @param status 状态
     * @param lastExecutionTime 最后执行时间
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE TaskBatch tb SET tb.status = :status, tb.lastExecutionTime = :lastExecutionTime WHERE tb.id = :id")
    int updateBatchStatus(@Param("id") Long id, @Param("status") String status, @Param("lastExecutionTime") LocalDateTime lastExecutionTime);
    
    /**
     * 更新批次完成任务数
     * @param id 批次ID
     * @param completedTasks 已完成任务数
     * @return 更新行数
     */
    @Modifying
    @Query("UPDATE TaskBatch tb SET tb.completedTasks = :completedTasks WHERE tb.id = :id")
    int updateCompletedTasks(@Param("id") Long id, @Param("completedTasks") Integer completedTasks);
} 