package com.skyeye.collector.service;

import com.skyeye.collector.dto.TaskCreateRequest;
import com.skyeye.collector.dto.TaskQueryRequest;
import com.skyeye.collector.dto.TaskUpdateRequest;
import com.skyeye.collector.dto.TaskExecutionResult;
import com.skyeye.collector.entity.CollectionTask;
import com.skyeye.collector.entity.TaskStatistics;
import org.springframework.data.domain.Page;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

/**
 * 采集任务服务接口
 * 
 * @author SkyEye Team
 */
public interface CollectionTaskService {

    /**
     * 创建采集任务
     * 
     * @param request 任务创建请求
     * @return 创建的任务
     */
    CollectionTask createTask(TaskCreateRequest request);

    /**
     * 更新采集任务
     * 
     * @param taskId 任务ID
     * @param request 任务更新请求
     * @return 更新后的任务
     */
    CollectionTask updateTask(Long taskId, TaskUpdateRequest request);

    /**
     * 删除采集任务
     * 
     * @param taskId 任务ID
     */
    void deleteTask(Long taskId);

    /**
     * 根据ID获取任务
     * 
     * @param taskId 任务ID
     * @return 任务
     */
    CollectionTask getTaskById(Long taskId);

    /**
     * 根据名称获取任务
     * 
     * @param name 任务名称
     * @return 任务
     */
    CollectionTask getTaskByName(String name);

    /**
     * 分页查询任务
     * 
     * @param request 查询请求
     * @return 任务分页结果
     */
    Page<CollectionTask> getTasks(TaskQueryRequest request);

    /**
     * 获取所有启用的任务
     * 
     * @return 任务列表
     */
    List<CollectionTask> getAllEnabledTasks();

    /**
     * 获取指定设备相关的任务
     * 
     * @param deviceId 设备ID
     * @return 任务列表
     */
    List<CollectionTask> getTasksByDeviceId(Long deviceId);

    /**
     * 检查任务名称是否存在
     * 
     * @param name 任务名称
     * @return 是否存在
     */
    boolean existsByName(String name);

    /**
     * 检查任务名称是否存在（排除指定ID）
     * 
     * @param name 任务名称
     * @param excludeId 排除的任务ID
     * @return 是否存在
     */
    boolean existsByNameExcludeId(String name, Long excludeId);

    /**
     * 执行任务
     * 
     * @param taskId 任务ID
     * @return 执行结果
     */
    TaskExecutionResult executeTask(Long taskId);

    /**
     * 手动执行任务
     * 
     * @param taskId 任务ID
     * @return 执行结果
     */
    Map<String, Object> manualExecuteTask(Long taskId);

    /**
     * 暂停任务
     * 
     * @param taskId 任务ID
     */
    void pauseTask(Long taskId);

    /**
     * 恢复任务
     * 
     * @param taskId 任务ID
     */
    void resumeTask(Long taskId);

    /**
     * 启用任务
     * 
     * @param taskId 任务ID
     */
    void enableTask(Long taskId);

    /**
     * 禁用任务
     * 
     * @param taskId 任务ID
     */
    void disableTask(Long taskId);

    /**
     * 更新任务状态
     * 
     * @param taskId 任务ID
     * @param status 新状态
     */
    void updateTaskStatus(Long taskId, Integer status);

    /**
     * 获取任务统计信息
     * 
     * @param taskId 任务ID
     * @return 统计信息
     */
    Map<String, Object> getTaskStatistics(Long taskId);

    /**
     * 获取任务执行历史
     * 
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 执行历史
     */
    List<Map<String, Object>> getTaskExecutionHistory(Long taskId, int limit);

    /**
     * 获取任务执行日志
     * 
     * @param taskId 任务ID
     * @param limit 限制数量
     * @return 执行日志
     */
    List<Map<String, Object>> getTaskExecutionLogs(Long taskId, int limit);

    /**
     * 验证任务配置
     * 
     * @param request 任务创建请求
     * @return 验证结果
     */
    Map<String, Object> validateTaskConfig(TaskCreateRequest request);

    /**
     * 测试任务执行
     * 
     * @param taskId 任务ID
     * @return 测试结果
     */
    Map<String, Object> testTaskExecution(Long taskId);

    /**
     * 获取任务概览统计
     * 
     * @return 概览统计
     */
    Map<String, Object> getTaskOverviewStatistics();

    /**
     * 获取任务性能排名
     * 
     * @param date 统计日期
     * @param limit 限制数量
     * @return 性能排名
     */
    List<Map<String, Object>> getTaskPerformanceRanking(String date, int limit);

    /**
     * 清理过期任务数据
     * 
     * @param beforeDate 清理日期
     * @return 清理的记录数
     */
    int cleanupExpiredTaskData(String beforeDate);

    /**
     * 批量更新任务状态
     * 
     * @param taskIds 任务ID列表
     * @param status 新状态
     * @return 更新的记录数
     */
    int batchUpdateTaskStatus(List<Long> taskIds, Integer status);

    /**
     * 批量启用任务
     * 
     * @param taskIds 任务ID列表
     * @return 更新的记录数
     */
    int batchEnableTasks(List<Long> taskIds);

    /**
     * 批量禁用任务
     * 
     * @param taskIds 任务ID列表
     * @return 更新的记录数
     */
    int batchDisableTasks(List<Long> taskIds);

    /**
     * 获取任务调度信息
     * 
     * @param taskId 任务ID
     * @return 调度信息
     */
    Map<String, Object> getTaskScheduleInfo(Long taskId);

    /**
     * 更新任务下次执行时间
     * 
     * @param taskId 任务ID
     * @param nextExecutionTime 下次执行时间
     */
    void updateNextExecutionTime(Long taskId, Timestamp nextExecutionTime);

    /**
     * 获取需要执行的任务
     * 
     * @return 任务列表
     */
    List<CollectionTask> getTasksReadyForExecution();

    /**
     * 更新任务执行统计
     * 
     * @param taskId 任务ID
     * @param success 是否成功
     * @param executionTime 执行时间
     */
    void updateTaskExecutionStatistics(Long taskId, boolean success, long executionTime);
}
