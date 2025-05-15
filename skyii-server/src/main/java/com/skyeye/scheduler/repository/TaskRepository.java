package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务数据访问接口
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {

    /**
     * 根据状态查询任务
     * @param status 状态
     * @return 任务列表
     */
    List<Task> findByStatus(Integer status);

    /**
     * 根据任务类型查询任务
     * @param taskType 任务类型
     * @return 任务列表
     */
    List<Task> findByTaskType(String taskType);

    /**
     * 根据任务名称模糊查询
     * @param taskName 任务名称
     * @param pageable 分页参数
     * @return 分页任务列表
     */
    Page<Task> findByTaskNameContaining(String taskName, Pageable pageable);

    /**
     * 根据多个条件查询任务
     * @param taskType 任务类型
     * @param status 状态
     * @param priority 优先级
     * @param pageable 分页参数
     * @return 分页任务列表
     */
    Page<Task> findByTaskTypeAndStatusAndPriority(String taskType, Integer status, Integer priority, Pageable pageable);

    /**
     * 统计各状态任务数量
     * @return 状态和数量的列表
     */
    @Query("SELECT t.status AS status, COUNT(t) AS count FROM Task t GROUP BY t.status")
    List<Object[]> countByStatus();

    /**
     * 根据创建人ID查询任务
     * @param createdBy 创建人ID
     * @param pageable 分页参数
     * @return 分页任务列表
     */
    Page<Task> findByCreatedBy(Long createdBy, Pageable pageable);

    /**
     * 根据标签查询任务
     * @param tag 标签
     * @param pageable 分页参数
     * @return 分页任务列表
     */
    @Query("SELECT t FROM Task t WHERE t.tags LIKE %:tag%")
    Page<Task> findByTag(@Param("tag") String tag, Pageable pageable);

    /**
     * 统计各状态任务数量
     * @return 统计结果
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.deletedAt IS NULL")
    Long countTotalTasks();

    /**
     * 统计运行中任务数量
     * @return 统计结果
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 1 AND t.deletedAt IS NULL")
    Long countRunningTasks();

    /**
     * 统计已调度任务数量
     * @return 统计结果
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 2 AND t.deletedAt IS NULL")
    Long countScheduledTasks();

    /**
     * 统计已暂停任务数量
     * @return 统计结果
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 3 AND t.deletedAt IS NULL")
    Long countPausedTasks();

    /**
     * 统计已完成任务数量
     * @return 统计结果
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 4 AND t.deletedAt IS NULL")
    Long countCompletedTasks();

    /**
     * 统计执行失败任务数量
     * @return 统计结果
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.status = 5 AND t.deletedAt IS NULL")
    Long countFailedTasks();

    /**
     * 统计今日创建任务数量
     * @param startOfDay 今日开始时间
     * @param endOfDay 今日结束时间
     * @return 统计结果
     */
    @Query("SELECT COUNT(t) FROM Task t WHERE t.createdAt BETWEEN ?1 AND ?2 AND t.deletedAt IS NULL")
    Long countTodayCreatedTasks(LocalDateTime startOfDay, LocalDateTime endOfDay);
} 