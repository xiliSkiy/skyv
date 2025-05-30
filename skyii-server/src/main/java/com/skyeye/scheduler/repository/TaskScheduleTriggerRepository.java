package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.TaskScheduleTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务调度触发器数据访问接口
 */
@Repository
public interface TaskScheduleTriggerRepository extends JpaRepository<TaskScheduleTrigger, Long> {

    /**
     * 根据任务ID查询触发器列表
     * @param taskId 任务ID
     * @return 触发器列表
     */
    List<TaskScheduleTrigger> findByTaskId(Long taskId);

    /**
     * 根据事件条件查询触发器
     * @param eventCondition 事件条件
     * @return 触发器列表
     */
    List<TaskScheduleTrigger> findByEventConditionContaining(String eventCondition);

    /**
     * 根据采集器ID和启用状态查询触发器
     * @param collectorId 采集器ID
     * @param enabled 是否启用
     * @return 触发器列表
     */
    List<TaskScheduleTrigger> findByCollectorIdAndEnabled(Long collectorId, Boolean enabled);

    /**
     * 查询需要执行的触发器
     * @param now 当前时间
     * @param enabled 是否启用
     * @return 触发器列表
     */
    @Query("SELECT t FROM TaskScheduleTrigger t WHERE t.enabled = :enabled AND t.nextFireTime <= :now")
    List<TaskScheduleTrigger> findTriggerable(@Param("now") LocalDateTime now, @Param("enabled") Boolean enabled);

    /**
     * 查询需要执行的触发器
     * @param now 当前时间
     * @return 触发器列表
     */
    @Query("SELECT t FROM TaskScheduleTrigger t WHERE t.enabled = true AND t.nextFireTime <= :now")
    List<TaskScheduleTrigger> findTriggersToExecute(@Param("now") LocalDateTime now);

    /**
     * 更新触发器状态
     * @param id 触发器ID
     * @param enabled 是否启用
     * @return 更新记录数
     */
    @Modifying
    @Query("UPDATE TaskScheduleTrigger t SET t.enabled = :enabled, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateStatus(@Param("id") Long id, @Param("enabled") Boolean enabled);

    /**
     * 更新下次执行时间
     * @param id 触发器ID
     * @param nextFireTime 下次执行时间
     * @return 更新记录数
     */
    @Modifying
    @Query("UPDATE TaskScheduleTrigger t SET t.nextFireTime = :nextFireTime, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateNextFireTime(@Param("id") Long id, @Param("nextFireTime") LocalDateTime nextFireTime);

    /**
     * 更新触发信息
     * @param id 触发器ID
     * @param lastFireTime 上次执行时间
     * @param firedCount 执行次数
     * @return 更新记录数
     */
    @Modifying
    @Query("UPDATE TaskScheduleTrigger t SET t.lastFireTime = :lastFireTime, t.firedCount = :firedCount, t.updatedAt = CURRENT_TIMESTAMP WHERE t.id = :id")
    int updateFireInfo(@Param("id") Long id, @Param("lastFireTime") LocalDateTime lastFireTime, @Param("firedCount") Integer firedCount);
} 