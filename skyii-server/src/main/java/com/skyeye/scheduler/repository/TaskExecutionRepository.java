package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.TaskExecution;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务执行数据访问接口
 */
@Repository
public interface TaskExecutionRepository extends JpaRepository<TaskExecution, Long> {

    /**
     * 根据任务ID查询任务执行记录
     * @param taskId 任务ID
     * @return 任务执行记录列表
     */
    List<TaskExecution> findByTaskId(Long taskId);

    /**
     * 根据任务ID查询任务执行记录，分页
     * @param taskId 任务ID
     * @param pageable 分页参数
     * @return 分页任务执行记录列表
     */
    Page<TaskExecution> findByTaskId(Long taskId, Pageable pageable);

    /**
     * 根据任务ID和状态查询任务执行记录
     * @param taskId 任务ID
     * @param status 状态
     * @return 任务执行记录列表
     */
    List<TaskExecution> findByTaskIdAndStatus(Long taskId, Integer status);

    /**
     * 根据执行ID查询任务执行记录
     * @param executionId 执行ID
     * @return 任务执行记录
     */
    Optional<TaskExecution> findByExecutionId(String executionId);

    /**
     * 根据任务ID查询最近一次执行记录
     * @param taskId 任务ID
     * @return 任务执行记录
     */
    Optional<TaskExecution> findTopByTaskIdOrderByStartTimeDesc(Long taskId);

    /**
     * 查询指定时间范围内的任务执行记录
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务执行记录列表
     */
    List<TaskExecution> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 统计任务执行记录数量
     * @param taskId 任务ID
     * @return 执行记录数量
     */
    Long countByTaskId(Long taskId);

    /**
     * 根据状态统计任务执行记录数量
     * @param status 状态
     * @return 执行记录数量
     */
    Long countByStatus(Integer status);
} 