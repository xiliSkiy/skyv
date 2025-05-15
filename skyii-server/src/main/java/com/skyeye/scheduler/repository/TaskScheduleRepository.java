package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.Task;
import com.skyeye.scheduler.entity.TaskSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务调度数据访问接口
 */
@Repository
public interface TaskScheduleRepository extends JpaRepository<TaskSchedule, Long> {

    /**
     * 根据任务查询任务调度信息
     * @param task 任务
     * @return 任务调度信息
     */
    Optional<TaskSchedule> findByTask(Task task);

    /**
     * 根据任务ID查询任务调度
     * @param taskId 任务ID
     * @return 任务调度
     */
    Optional<TaskSchedule> findByTaskId(Long taskId);

    /**
     * 根据任务ID删除任务调度
     * @param taskId 任务ID
     */
    void deleteByTaskId(Long taskId);

    /**
     * 根据调度类型查询任务调度
     * @param scheduleType 调度类型
     * @return 任务调度列表
     */
    List<TaskSchedule> findByScheduleType(String scheduleType);

    /**
     * 查询开始时间在指定时间范围内的任务调度
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @return 任务调度列表
     */
    List<TaskSchedule> findByStartTimeBetween(LocalDateTime startTime, LocalDateTime endTime);

    /**
     * 查询未来要执行的任务调度
     * @param now 当前时间
     * @return 任务调度列表
     */
    List<TaskSchedule> findByStartTimeAfter(LocalDateTime now);

    /**
     * 查询已过期的任务调度
     * @param now 当前时间
     * @return 任务调度列表
     */
    List<TaskSchedule> findByEndTimeBefore(LocalDateTime now);
} 