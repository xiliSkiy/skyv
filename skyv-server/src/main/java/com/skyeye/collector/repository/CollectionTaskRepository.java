package com.skyeye.collector.repository;

import com.skyeye.collector.entity.CollectionTask;
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
 * 采集任务Repository
 * 
 * @author SkyEye Team
 */
@Repository
public interface CollectionTaskRepository extends JpaRepository<CollectionTask, Long>, JpaSpecificationExecutor<CollectionTask> {

    /**
     * 根据名称查找任务
     */
    Optional<CollectionTask> findByName(String name);

    /**
     * 根据状态查找任务
     */
    List<CollectionTask> findByStatus(Integer status);

    /**
     * 根据状态查找任务（分页）
     */
    Page<CollectionTask> findByStatus(Integer status, Pageable pageable);

    /**
     * 根据调度类型查找任务
     */
    List<CollectionTask> findByScheduleType(String scheduleType);

    /**
     * 根据采集器ID查找任务
     */
    List<CollectionTask> findByCollectorId(Long collectorId);

    /**
     * 根据是否启用查找任务
     */
    List<CollectionTask> findByIsEnabled(Boolean isEnabled);

    /**
     * 根据优先级查找任务
     */
    List<CollectionTask> findByPriorityLessThanEqualOrderByPriorityAsc(Integer maxPriority);

    /**
     * 查找启用的任务
     */
    List<CollectionTask> findByIsEnabledTrueAndStatusOrderByPriorityAsc(Integer status);

    /**
     * 查找需要执行的任务（下次执行时间已到）
     */
    @Query("SELECT t FROM CollectionTask t WHERE t.isEnabled = true AND t.status = 1 AND t.nextExecutionTime <= :now ORDER BY t.priority ASC")
    List<CollectionTask> findTasksReadyForExecution(@Param("now") Timestamp now);

    /**
     * 查找指定设备相关的任务
     */
    @Query(value = "SELECT * FROM tb_collection_tasks t WHERE t.is_enabled = true AND t.status = 1 AND JSON_CONTAINS(t.target_devices, JSON_ARRAY(:deviceId)) AND t.deleted_at IS NULL", nativeQuery = true)
    List<CollectionTask> findTasksByDeviceId(@Param("deviceId") Long deviceId);

    /**
     * 统计各状态的任务数量
     */
    @Query("SELECT t.status, COUNT(t) FROM CollectionTask t GROUP BY t.status")
    List<Object[]> countByStatus();

    /**
     * 统计各调度类型的任务数量
     */
    @Query("SELECT t.scheduleType, COUNT(t) FROM CollectionTask t GROUP BY t.scheduleType")
    List<Object[]> countByScheduleType();

    /**
     * 统计各优先级的任务数量
     */
    @Query("SELECT t.priority, COUNT(t) FROM CollectionTask t GROUP BY t.priority")
    List<Object[]> countByPriority();

    /**
     * 查找最近执行的任务
     */
    @Query("SELECT t FROM CollectionTask t WHERE t.lastExecutionTime IS NOT NULL ORDER BY t.lastExecutionTime DESC")
    List<CollectionTask> findRecentlyExecutedTasks(Pageable pageable);

    /**
     * 查找长时间未执行的任务
     */
    @Query("SELECT t FROM CollectionTask t WHERE t.lastExecutionTime IS NULL OR t.lastExecutionTime < :threshold ORDER BY t.createdAt ASC")
    List<CollectionTask> findLongInactiveTasks(@Param("threshold") Timestamp threshold);

    /**
     * 查找执行失败的任务
     */
    @Query("SELECT t FROM CollectionTask t WHERE t.lastExecutionStatus = 'FAILED' ORDER BY t.lastExecutionTime DESC")
    List<CollectionTask> findFailedTasks(Pageable pageable);

    /**
     * 查找高优先级任务
     */
    @Query("SELECT t FROM CollectionTask t WHERE t.priority <= :maxPriority ORDER BY t.priority ASC, t.createdAt ASC")
    List<CollectionTask> findHighPriorityTasks(@Param("maxPriority") Integer maxPriority);

    /**
     * 更新任务状态
     */
    @Modifying
    @Query("UPDATE CollectionTask t SET t.status = :status, t.updatedAt = :updatedAt WHERE t.id = :taskId")
    int updateTaskStatus(@Param("taskId") Long taskId, @Param("status") Integer status, @Param("updatedAt") Timestamp updatedAt);

    /**
     * 更新任务执行统计
     */
    @Modifying
    @Query("UPDATE CollectionTask t SET t.executionCount = t.executionCount + 1, t.lastExecutionTime = :lastExecutionTime, t.updatedAt = :updatedAt WHERE t.id = :taskId")
    int incrementExecutionCount(@Param("taskId") Long taskId, @Param("lastExecutionTime") Timestamp lastExecutionTime, @Param("updatedAt") Timestamp updatedAt);

    /**
     * 更新任务成功统计
     */
    @Modifying
    @Query("UPDATE CollectionTask t SET t.successCount = t.successCount + 1, t.lastExecutionStatus = 'SUCCESS', t.updatedAt = :updatedAt WHERE t.id = :taskId")
    int incrementSuccessCount(@Param("taskId") Long taskId, @Param("updatedAt") Timestamp updatedAt);

    /**
     * 更新任务失败统计
     */
    @Modifying
    @Query("UPDATE CollectionTask t SET t.failureCount = t.failureCount + 1, t.lastExecutionStatus = 'FAILED', t.lastExecutionError = :errorMessage, t.updatedAt = :updatedAt WHERE t.id = :taskId")
    int incrementFailureCount(@Param("taskId") Long taskId, @Param("errorMessage") String errorMessage, @Param("updatedAt") Timestamp updatedAt);

    /**
     * 更新任务平均执行时间
     */
    @Modifying
    @Query("UPDATE CollectionTask t SET t.averageExecutionTime = :averageTime, t.updatedAt = :updatedAt WHERE t.id = :taskId")
    int updateAverageExecutionTime(@Param("taskId") Long taskId, @Param("averageTime") Long averageTime, @Param("updatedAt") Timestamp updatedAt);

    /**
     * 设置下次执行时间
     */
    @Modifying
    @Query("UPDATE CollectionTask t SET t.nextExecutionTime = :nextExecutionTime, t.updatedAt = :updatedAt WHERE t.id = :taskId")
    int setNextExecutionTime(@Param("taskId") Long taskId, @Param("nextExecutionTime") Timestamp nextExecutionTime, @Param("updatedAt") Timestamp updatedAt);

    /**
     * 检查任务名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查任务名称是否存在（排除指定ID）
     */
    @Query("SELECT COUNT(t) > 0 FROM CollectionTask t WHERE t.name = :name AND t.id != :excludeId")
    boolean existsByNameExcludeId(@Param("name") String name, @Param("excludeId") Long excludeId);

    /**
     * 根据标签查找任务
     */
    @Query(value = "SELECT * FROM tb_collection_tasks t WHERE t.tags IS NOT NULL AND JSON_CONTAINS(t.tags, JSON_QUOTE(:tag)) AND t.deleted_at IS NULL", nativeQuery = true)
    List<CollectionTask> findTasksByTag(@Param("tag") String tag);

    /**
     * 查找所有启用的任务
     */
    List<CollectionTask> findByIsEnabledTrue();

    /**
     * 查找所有启用的任务（分页）
     */
    Page<CollectionTask> findByIsEnabledTrue(Pageable pageable);
}
