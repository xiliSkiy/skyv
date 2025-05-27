package com.skyeye.task.controller;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.task.dto.TaskScheduleDTO;
import com.skyeye.task.service.TaskScheduleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 任务调度控制器
 */
@RestController
@RequestMapping("/v1/task/schedules")
public class TaskScheduleController {

    @Autowired
    private TaskScheduleService taskScheduleService;
    
    /**
     * 创建任务调度
     */
    @PostMapping
    public ApiResponse<TaskScheduleDTO> createTaskSchedule(@RequestBody TaskScheduleDTO taskScheduleDTO) {
        TaskScheduleDTO createdTask = taskScheduleService.createTaskSchedule(taskScheduleDTO);
        return ApiResponse.success(createdTask);
    }
    
    /**
     * 获取任务调度列表
     */
    @GetMapping
    public ApiResponse<List<TaskScheduleDTO>> getTaskSchedules() {
        List<TaskScheduleDTO> tasks = taskScheduleService.getAllTaskSchedules();
        return ApiResponse.success(tasks);
    }
    
    /**
     * 获取任务调度详情
     */
    @GetMapping("/{id}")
    public ApiResponse<TaskScheduleDTO> getTaskSchedule(@PathVariable Long id) {
        TaskScheduleDTO task = taskScheduleService.getTaskScheduleById(id);
        return ApiResponse.success(task);
    }
    
    /**
     * 更新任务调度
     */
    @PutMapping("/{id}")
    public ApiResponse<TaskScheduleDTO> updateTaskSchedule(
            @PathVariable Long id, @RequestBody TaskScheduleDTO taskScheduleDTO) {
        TaskScheduleDTO updatedTask = taskScheduleService.updateTaskSchedule(id, taskScheduleDTO);
        return ApiResponse.success(updatedTask);
    }
    
    /**
     * 删除任务调度
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteTaskSchedule(@PathVariable Long id) {
        taskScheduleService.deleteTaskSchedule(id);
        return ApiResponse.success();
    }
    
    /**
     * 手动触发任务执行
     */
    @PostMapping("/{id}/trigger")
    public ApiResponse<Void> triggerTask(@PathVariable Long id) {
        taskScheduleService.triggerTask(id);
        return ApiResponse.success();
    }
    
    /**
     * 暂停任务
     */
    @PutMapping("/{id}/pause")
    public ApiResponse<Void> pauseTask(@PathVariable Long id) {
        taskScheduleService.pauseTask(id);
        return ApiResponse.success();
    }
    
    /**
     * 恢复任务
     */
    @PutMapping("/{id}/resume")
    public ApiResponse<Void> resumeTask(@PathVariable Long id) {
        taskScheduleService.resumeTask(id);
        return ApiResponse.success();
    }
}