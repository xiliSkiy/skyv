package com.skyeye.scheduler.controller;

import com.skyeye.common.response.Result;
import com.skyeye.scheduler.dto.*;
import com.skyeye.scheduler.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务管理控制器
 */
@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    /**
     * 分页查询任务列表
     */
    @GetMapping
    public Result<Page<TaskDTO>> getTaskList(TaskQueryDTO queryDTO) {
        return Result.success(taskService.findTasksByPage(queryDTO));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public Result<TaskDTO> getTaskDetail(@PathVariable Long id) {
        return Result.success(taskService.findTaskById(id));
    }

    /**
     * 创建任务
     */
    @PostMapping
    public Result<TaskDTO> createTask(@RequestBody TaskCreateDTO createDTO) {
        return Result.success(taskService.createTask(createDTO));
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public Result<TaskDTO> updateTask(@PathVariable Long id, @RequestBody TaskCreateDTO createDTO) {
        return Result.success(taskService.updateTask(id, createDTO));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return Result.success();
    }

    /**
     * 启动任务
     */
    @PutMapping("/{id}/start")
    public Result<TaskDTO> startTask(@PathVariable Long id) {
        return Result.success(taskService.startTask(id));
    }

    /**
     * 暂停任务
     */
    @PutMapping("/{id}/pause")
    public Result<TaskDTO> pauseTask(@PathVariable Long id) {
        return Result.success(taskService.pauseTask(id));
    }

    /**
     * 停止任务
     */
    @PutMapping("/{id}/stop")
    public Result<TaskDTO> stopTask(@PathVariable Long id) {
        return Result.success(taskService.stopTask(id));
    }

    /**
     * 批量操作任务
     */
    @PutMapping("/batch/{action}")
    public Result<Void> batchTaskAction(@PathVariable String action, @RequestBody TaskBatchActionDTO batchActionDTO) {
        taskService.batchTaskAction(action, batchActionDTO);
        return Result.success();
    }

    /**
     * 获取任务统计数据
     */
    @GetMapping("/stats")
    public Result<TaskStatsDTO> getTaskStats() {
        return Result.success(taskService.getTaskStats());
    }

    /**
     * 获取可用设备列表
     */
    @GetMapping("/devices")
    public Result<Page<TaskDeviceDTO>> getAvailableDevices(TaskQueryDTO queryDTO) {
        return Result.success(taskService.getAvailableDevices(queryDTO));
    }

    /**
     * 获取指标模板列表
     */
    @GetMapping("/metric-templates")
    public Result<List<MetricTemplateDTO>> getMetricTemplates() {
        return Result.success(taskService.getMetricTemplates());
    }

    /**
     * 保存任务草稿
     */
    @PostMapping("/draft")
    public Result<TaskDraftDTO> saveTaskDraft(@RequestBody TaskDraftDTO draftDTO) {
        return Result.success(taskService.saveTaskDraft(draftDTO));
    }

    /**
     * 获取任务草稿
     */
    @GetMapping("/draft/{draftId}")
    public Result<TaskDraftDTO> getTaskDraft(@PathVariable String draftId) {
        return Result.success(taskService.getTaskDraft(draftId));
    }

    /**
     * 删除任务草稿
     */
    @DeleteMapping("/draft/{draftId}")
    public Result<Void> deleteTaskDraft(@PathVariable String draftId) {
        taskService.deleteTaskDraft(draftId);
        return Result.success();
    }
} 