package com.skyeye.scheduler.service;

import com.skyeye.scheduler.dto.*;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 任务服务接口
 */
public interface TaskService {

    /**
     * 分页查询任务列表
     * @param queryDTO 查询参数
     * @return 分页任务列表
     */
    Page<TaskDTO> findTasksByPage(TaskQueryDTO queryDTO);

    /**
     * 根据ID查询任务详情
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDTO findTaskById(Long id);

    /**
     * 创建任务
     * @param createDTO 创建参数
     * @return 任务详情
     */
    TaskDTO createTask(TaskCreateDTO createDTO);

    /**
     * 更新任务
     * @param id 任务ID
     * @param createDTO 更新参数
     * @return 任务详情
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
     * @return 任务详情
     */
    TaskDTO startTask(Long id);

    /**
     * 暂停任务
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDTO pauseTask(Long id);

    /**
     * 停止任务
     * @param id 任务ID
     * @return 任务详情
     */
    TaskDTO stopTask(Long id);

    /**
     * 批量操作任务
     * @param action 操作类型：start, pause, stop, delete
     * @param batchActionDTO 批量操作参数
     */
    void batchTaskAction(String action, TaskBatchActionDTO batchActionDTO);

    /**
     * 获取任务统计数据
     * @return 任务统计数据
     */
    TaskStatsDTO getTaskStats();

    /**
     * 获取可用设备列表
     * @param queryDTO 查询参数
     * @return 分页设备列表
     */
    Page<TaskDeviceDTO> getAvailableDevices(TaskQueryDTO queryDTO);

    /**
     * 获取指标模板列表
     * @return 指标模板列表
     */
    List<MetricTemplateDTO> getMetricTemplates();

    /**
     * 保存任务草稿
     * @param draftDTO 草稿数据
     * @return 草稿ID
     */
    TaskDraftDTO saveTaskDraft(TaskDraftDTO draftDTO);

    /**
     * 获取任务草稿
     * @param draftId 草稿ID
     * @return 草稿数据
     */
    TaskDraftDTO getTaskDraft(String draftId);

    /**
     * 删除任务草稿
     * @param draftId 草稿ID
     */
    void deleteTaskDraft(String draftId);
} 