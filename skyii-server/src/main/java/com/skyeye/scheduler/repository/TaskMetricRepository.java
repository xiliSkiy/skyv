package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.Task;
import com.skyeye.scheduler.entity.TaskMetric;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 任务指标数据访问接口
 */
@Repository
public interface TaskMetricRepository extends JpaRepository<TaskMetric, Long> {

    /**
     * 根据任务查询任务指标列表
     * @param task 任务
     * @return 任务指标列表
     */
    List<TaskMetric> findByTask(Task task);

    /**
     * 根据任务ID查询任务指标
     * @param taskId 任务ID
     * @return 任务指标列表
     */
    List<TaskMetric> findByTaskId(Long taskId);

    /**
     * 根据任务ID删除任务指标
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);

    /**
     * 根据任务ID和指标类型查询任务指标
     * @param taskId 任务ID
     * @param metricType 指标类型
     * @return 任务指标列表
     */
    List<TaskMetric> findByTaskIdAndMetricType(Long taskId, String metricType);

    /**
     * 根据任务ID和指标名称查询任务指标
     * @param taskId 任务ID
     * @param metricName 指标名称
     * @return 任务指标
     */
    TaskMetric findByTaskIdAndMetricName(Long taskId, String metricName);

    /**
     * 根据任务ID查询任务指标，按排序字段排序
     * @param taskId 任务ID
     * @return 任务指标列表
     */
    List<TaskMetric> findByTaskIdOrderBySortOrderAsc(Long taskId);

    /**
     * 根据任务ID统计指标数量
     * @param taskId 任务ID
     * @return 指标数量
     */
    @Query("SELECT COUNT(tm) FROM TaskMetric tm WHERE tm.task.id = :taskId")
    Long countByTaskId(@Param("taskId") Long taskId);
} 