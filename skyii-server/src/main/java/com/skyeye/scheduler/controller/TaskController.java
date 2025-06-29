package com.skyeye.scheduler.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.scheduler.dto.*;
import com.skyeye.scheduler.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

/**
 * 任务调度控制器
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
    public ApiResponse getTaskList(TaskQueryDTO queryDTO) {
        return ApiResponse.success(taskService.findTasksByPage(queryDTO));
    }

    /**
     * 获取任务详情
     */
    @GetMapping("/{id}")
    public ApiResponse getTaskDetail(@PathVariable Long id) {
        return ApiResponse.success(taskService.findTaskById(id));
    }

    /**
     * 创建任务
     */
    @PostMapping
    public ApiResponse createTask(@RequestBody TaskCreateDTO createDTO) {
        return ApiResponse.success(taskService.createTask(createDTO));
    }

    /**
     * 更新任务
     */
    @PutMapping("/{id}")
    public ApiResponse updateTask(@PathVariable Long id, @RequestBody TaskCreateDTO createDTO) {
        return ApiResponse.success(taskService.updateTask(id, createDTO));
    }

    /**
     * 删除任务
     */
    @DeleteMapping("/{id}")
    public ApiResponse deleteTask(@PathVariable Long id) {
        taskService.deleteTask(id);
        return ApiResponse.success();
    }

    /**
     * 启动任务
     */
    @PutMapping("/{id}/start")
    public ApiResponse startTask(@PathVariable Long id) {
        return ApiResponse.success(taskService.startTask(id));
    }

    /**
     * 暂停任务
     */
    @PutMapping("/{id}/pause")
    public ApiResponse pauseTask(@PathVariable Long id) {
        return ApiResponse.success(taskService.pauseTask(id));
    }

    /**
     * 停止任务
     */
    @PutMapping("/{id}/stop")
    public ApiResponse stopTask(@PathVariable Long id) {
        return ApiResponse.success(taskService.stopTask(id));
    }

    /**
     * 批量操作任务
     */
    @PutMapping("/batch/{action}")
    public ApiResponse batchTaskAction(@PathVariable String action, @RequestBody TaskBatchActionDTO batchActionDTO) {
        taskService.batchTaskAction(action, batchActionDTO);
        return ApiResponse.success();
    }

    /**
     * 获取任务统计数据
     */
    @GetMapping("/stats")
    public ApiResponse getTaskStats() {
        return ApiResponse.success(taskService.getTaskStats());
    }

    /**
     * 获取可用设备列表
     */
    @GetMapping("/devices")
    public ApiResponse getAvailableDevices(TaskQueryDTO queryDTO) {
        return ApiResponse.success(taskService.getAvailableDevices(queryDTO));
    }

    /**
     * 获取指标模板列表
     */
    @GetMapping("/metric-templates")
    public ApiResponse getMetricTemplates() {
        return ApiResponse.success(taskService.getMetricTemplates());
    }

    /**
     * 保存任务基本信息
     * @param taskBasicInfo 任务基本信息
     * @return 保存结果
     */
    @PostMapping("/basic-info")
    public ApiResponse saveBasicInfo(@RequestBody @Valid TaskBasicInfoDTO taskBasicInfo) {
        Long taskId = taskService.saveBasicInfo(taskBasicInfo);
        Map<String, Object> data = Map.of("taskId", taskId);
        return ApiResponse.success(data);
    }

    /**
     * 保存任务草稿
     * @param taskDraft 任务草稿数据
     * @return 保存结果
     */
    @PostMapping("/draft")
    public ApiResponse saveTaskDraft(@RequestBody TaskDraftDTO taskDraft) {
        TaskDraftDTO savedDraft = taskService.saveTaskDraft(taskDraft);
        Map<String, Object> data = Map.of("draftId", savedDraft.getDraftId());
        return ApiResponse.success(data);
    }

    /**
     * 获取任务草稿
     * @param draftId 草稿ID
     * @return 草稿数据
     */
    @GetMapping("/draft/{draftId}")
    public ApiResponse getTaskDraft(@PathVariable String draftId) {
        TaskDraftDTO taskDraft = taskService.getTaskDraft(draftId);
        return ApiResponse.success(taskDraft);
    }

    /**
     * 删除任务草稿
     */
    @DeleteMapping("/draft/{draftId}")
    public ApiResponse deleteTaskDraft(@PathVariable String draftId) {
        taskService.deleteTaskDraft(draftId);
        return ApiResponse.success();
    }

    /**
     * 获取设备类型树
     */
    @GetMapping("/device-types")
    public ApiResponse getDeviceTypeTree() {
        return ApiResponse.success(taskService.getDeviceTypeTree());
    }

    /**
     * 根据设备类型获取设备列表
     */
    @GetMapping("/devices/by-type/{typeId}")
    public ApiResponse getDevicesByType(
            @PathVariable String typeId,
            TaskDeviceQueryDTO queryDTO) {
        return ApiResponse.success(taskService.getDevicesByType(typeId, queryDTO));
    }

    /**
     * 获取区域列表
     */
    @GetMapping("/areas")
    public ApiResponse getAreaList() {
        return ApiResponse.success(taskService.getAreaList());
    }

    /**
     * 根据区域获取设备列表
     */
    @GetMapping("/devices/by-area/{areaId}")
    public ApiResponse getDevicesByArea(
            @PathVariable String areaId,
            TaskDeviceQueryDTO queryDTO) {
        return ApiResponse.success(taskService.getDevicesByArea(areaId, queryDTO));
    }

    /**
     * 获取所有标签
     */
    @GetMapping("/tags")
    public ApiResponse getAllTags() {
        return ApiResponse.success(taskService.getAllTags());
    }

    /**
     * 根据标签获取设备列表
     */
    @GetMapping("/devices/by-tags")
    public ApiResponse getDevicesByTags(
            @RequestParam List<String> tags,
            @RequestParam(defaultValue = "AND") String tagOperator,
            TaskDeviceQueryDTO queryDTO) {
        return ApiResponse.success(taskService.getDevicesByTags(tags, tagOperator, queryDTO));
    }

    /**
     * 保存任务设备选择
     */
    @PostMapping("/{taskId}/devices")
    public ApiResponse saveTaskDeviceSelection(
            @PathVariable Long taskId,
            @RequestBody List<Long> deviceIds) {
        boolean success = taskService.saveTaskDeviceSelection(taskId, deviceIds);
        return ApiResponse.success(success);
    }

    /**
     * 获取任务已选设备
     */
    @GetMapping("/{taskId}/selected-devices")
    public ApiResponse getSelectedDevices(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.getSelectedDevices(taskId));
    }

    /**
     * 获取设备类型统计
     */
    @PostMapping("/device-type-stats")
    public ApiResponse getDeviceTypeStats(@RequestBody List<Long> deviceIds) {
        return ApiResponse.success(taskService.getDeviceTypeStats(deviceIds));
    }

    /**
     * 获取设备状态统计
     */
    @PostMapping("/device-status-stats")
    public ApiResponse getDeviceStatusStats(@RequestBody List<Long> deviceIds) {
        return ApiResponse.success(taskService.getDeviceStatusStats(deviceIds));
    }

    /**
     * 获取指标分类列表
     */
    @GetMapping("/metric-categories")
    public ApiResponse getMetricCategories() {
        return ApiResponse.success(taskService.getMetricCategories());
    }

    /**
     * 根据分类获取指标列表
     */
    @GetMapping("/metrics/by-category/{categoryId}")
    public ApiResponse getMetricsByCategory(@PathVariable String categoryId) {
        return ApiResponse.success(taskService.getMetricsByCategory(categoryId));
    }

    /**
     * 搜索指标
     */
    @GetMapping("/metrics/search")
    public ApiResponse searchMetrics(@RequestParam String keyword) {
        return ApiResponse.success(taskService.searchMetrics(keyword));
    }

    /**
     * 保存任务指标配置
     */
    @PostMapping("/{taskId}/metrics")
    public ApiResponse saveTaskMetrics(
            @PathVariable Long taskId,
            @RequestBody List<Long> metricIds) {
        boolean success = taskService.saveTaskMetrics(taskId, metricIds);
        return ApiResponse.success(success);
    }

    /**
     * 获取任务已选指标
     */
    @GetMapping("/{taskId}/selected-metrics")
    public ApiResponse getSelectedMetrics(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.getSelectedMetrics(taskId));
    }

    /**
     * 保存任务调度设置
     */
    @PostMapping("/{taskId}/schedule")
    public ApiResponse saveTaskSchedule(
            @PathVariable Long taskId,
            @RequestBody @Valid TaskScheduleDTO scheduleDTO) {
        boolean success = taskService.saveTaskSchedule(taskId, scheduleDTO);
        return ApiResponse.success(success);
    }

    /**
     * 获取任务调度设置
     */
    @GetMapping("/{taskId}/schedule")
    public ApiResponse getTaskSchedule(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.getTaskSchedule(taskId));
    }

    /**
     * 提交并完成任务创建
     */
    @PostMapping("/{taskId}/complete")
    public ApiResponse completeTaskCreation(@PathVariable Long taskId) {
        return ApiResponse.success(taskService.completeTaskCreation(taskId));
    }
} 