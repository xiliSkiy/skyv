package com.skyeye.scheduler.service;

import com.skyeye.common.response.PageResult;
import com.skyeye.scheduler.dto.*;
import com.skyeye.scheduler.entity.Task;
import com.skyeye.scheduler.entity.TaskBatch;
import com.skyeye.scheduler.entity.TaskDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 分页查询任务
     * @param queryDTO 查询条件
     * @return 任务分页列表
     */
    Page<TaskDTO> findTasksByPage(TaskQueryDTO queryDTO);

    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDTO findTaskById(Long id);

    /**
     * 创建任务
     * @param createDTO 创建参数
     * @return 创建后的任务
     */
    TaskDTO createTask(TaskCreateDTO createDTO);

    /**
     * 更新任务
     * @param id 任务ID
     * @param createDTO 更新参数
     * @return 更新后的任务
     */
    TaskDTO updateTask(Long id, TaskCreateDTO createDTO);

    /**
     * 删除任务
     * @param id 任务ID
     */
    void deleteTask(Long id);

    /**
     * 启动任务
     * @param id 任务ID
     * @return 更新后的任务
     */
    TaskDTO startTask(Long id);

    /**
     * 暂停任务
     * @param id 任务ID
     * @return 更新后的任务
     */
    TaskDTO pauseTask(Long id);

    /**
     * 停止任务
     * @param id 任务ID
     * @return 更新后的任务
     */
    TaskDTO stopTask(Long id);

    /**
     * 批量操作任务
     * @param action 操作类型
     * @param batchActionDTO 批量操作参数
     */
    void batchTaskAction(String action, TaskBatchActionDTO batchActionDTO);

    /**
     * 获取任务统计信息
     * @return 统计信息
     */
    TaskStatsDTO getTaskStats();

    /**
     * 分页查询可用设备
     * @param queryDTO 查询条件
     * @return 设备分页列表
     */
    Page<TaskDeviceDTO> getAvailableDevices(TaskQueryDTO queryDTO);

    /**
     * 获取指标模板列表
     * @return 指标模板列表
     */
    List<MetricTemplateDTO> getMetricTemplates();

    /**
     * 保存任务草稿
     * @param draftDTO 草稿信息
     * @return 保存后的草稿
     */
    TaskDraftDTO saveTaskDraft(TaskDraftDTO draftDTO);

    /**
     * 获取任务草稿
     * @param draftId 草稿ID
     * @return 草稿信息
     */
    TaskDraftDTO getTaskDraft(String draftId);

    /**
     * 删除任务草稿
     * @param draftId 草稿ID
     */
    void deleteTaskDraft(String draftId);

    /**
     * 根据ID查询任务
     * @param id 任务ID
     * @return 任务
     */
    Optional<Task> findById(Long id);

    /**
     * 查询所有任务
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<Task> findAll(Pageable pageable);

    /**
     * 查询任务设备列表
     * @param taskId 任务ID
     * @return 设备列表
     */
    List<TaskDevice> getTaskDevices(Long taskId);

    /**
     * 更新任务
     * @param task 任务信息
     * @return 更新后的任务
     */
    Task updateTask(Task task);

    /**
     * 创建任务
     * @param task 任务信息
     * @return 创建后的任务
     */
    Task createTask(Task task);

    /**
     * 创建采集任务
     * @param taskId 任务ID
     * @param collectorId 采集器ID
     * @param deviceIds 设备ID列表
     * @param metricIds 指标ID列表
     * @return 任务批次
     */
    TaskBatch createCollectionTask(Long taskId, Long collectorId, List<Long> deviceIds, List<Long> metricIds);

    /**
     * 提交采集任务
     * @param batchId 批次ID
     * @return 是否成功
     */
    boolean submitCollectionTask(Long batchId);

    /**
     * 取消采集任务
     * @param batchId 批次ID
     * @return 是否成功
     */
    boolean cancelCollectionTask(Long batchId);

    /**
     * 查询任务批次
     * @param taskId 任务ID
     * @param pageable 分页参数
     * @return 批次分页列表
     */
    Page<TaskBatch> findBatches(Long taskId, Pageable pageable);

    /**
     * 查询设备关联的任务
     * @param deviceId 设备ID
     * @param pageable 分页参数
     * @return 任务分页列表
     */
    Page<Task> findTasksByDevice(Long deviceId, Pageable pageable);

    /**
     * 创建任务触发器
     * @param taskId 任务ID
     * @param triggerDTO 触发器信息
     * @return 创建后的触发器
     */
    TaskScheduleTriggerDTO createTaskTrigger(Long taskId, TaskScheduleTriggerDTO triggerDTO);

    /**
     * 更新任务触发器
     * @param triggerId 触发器ID
     * @param triggerDTO 触发器信息
     * @return 更新后的触发器
     */
    TaskScheduleTriggerDTO updateTaskTrigger(Long triggerId, TaskScheduleTriggerDTO triggerDTO);

    /**
     * 更新触发器状态
     * @param triggerId 触发器ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean updateTriggerStatus(Long triggerId, boolean enabled);

    /**
     * 删除任务触发器
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    boolean deleteTaskTrigger(Long triggerId);

    /**
     * 获取任务触发器列表
     * @param taskId 任务ID
     * @return 触发器列表
     */
    List<TaskScheduleTriggerDTO> getTaskTriggers(Long taskId);

    /**
     * 获取任务设备ID列表
     * @param taskId 任务ID
     * @return 设备ID列表
     */
    List<Long> getTaskDeviceIds(Long taskId);

    /**
     * 获取任务指标ID列表
     * @param taskId 任务ID
     * @return 指标ID列表
     */
    List<Long> getTaskMetricIds(Long taskId);

    /**
     * 添加设备到任务
     * @param taskId 任务ID
     * @param deviceIds 设备ID列表
     * @return 创建的任务设备关联
     */
    List<TaskDevice> addDevices(Long taskId, List<Long> deviceIds);

    /**
     * 从任务中移除设备
     * @param taskId 任务ID
     * @param deviceId 设备ID
     * @return 是否成功
     */
    boolean removeDevice(Long taskId, Long deviceId);

    /**
     * 恢复暂停的任务
     * @param id 任务ID
     * @return 是否成功
     */
    boolean resumeTask(Long id);

    /**
     * 触发任务执行
     * @param taskId 任务ID
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    boolean triggerTask(Long taskId, Long triggerId);
} 