package com.skyeye.collector.controller;

import com.skyeye.collector.dto.TaskCreateRequest;
import com.skyeye.collector.dto.TaskQueryRequest;
import com.skyeye.collector.dto.TaskUpdateRequest;
import com.skyeye.collector.entity.CollectionTask;
import com.skyeye.collector.service.CollectionTaskService;
import com.skyeye.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 采集任务控制器
 * 
 * @author SkyEye Team
 */
@Slf4j
@RestController
@RequestMapping("/api/collector/tasks")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CollectionTaskController {

    private final CollectionTaskService collectionTaskService;

    /**
     * 创建采集任务
     */
    @PostMapping
    @PreAuthorize("hasPermission('task', 'create')")
    public ApiResponse<CollectionTask> createTask(@RequestBody TaskCreateRequest request) {
        try {
            CollectionTask task = collectionTaskService.createTask(request);
            return ApiResponse.success(task);
        } catch (Exception e) {
            log.error("创建采集任务失败", e);
            return ApiResponse.error(500, "创建任务失败: " + e.getMessage());
        }
    }

    /**
     * 更新采集任务
     */
    @PutMapping("/{taskId}")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<CollectionTask> updateTask(@PathVariable Long taskId, @RequestBody TaskUpdateRequest request) {
        try {
            CollectionTask task = collectionTaskService.updateTask(taskId, request);
            return ApiResponse.success(task);
        } catch (Exception e) {
            log.error("更新采集任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "更新任务失败: " + e.getMessage());
        }
    }

    /**
     * 删除采集任务
     */
    @DeleteMapping("/{taskId}")
    @PreAuthorize("hasPermission('task', 'delete')")
    public ApiResponse<Void> deleteTask(@PathVariable Long taskId) {
        try {
            collectionTaskService.deleteTask(taskId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("删除采集任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "删除任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取采集任务详情
     */
    @GetMapping("/{taskId}")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<CollectionTask> getTask(@PathVariable Long taskId) {
        try {
            CollectionTask task = collectionTaskService.getTaskById(taskId);
            return ApiResponse.success(task);
        } catch (Exception e) {
            log.error("获取采集任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "获取任务失败: " + e.getMessage());
        }
    }

    /**
     * 分页查询采集任务
     */
    @GetMapping
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<Page<CollectionTask>> getTasks(@ModelAttribute TaskQueryRequest request) {
        try {
            Page<CollectionTask> tasks = collectionTaskService.getTasks(request);
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            log.error("查询采集任务失败", e);
            return ApiResponse.error(500, "查询任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取所有启用的任务
     */
    @GetMapping("/enabled")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<List<CollectionTask>> getEnabledTasks() {
        try {
            List<CollectionTask> tasks = collectionTaskService.getAllEnabledTasks();
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            log.error("获取启用任务失败", e);
            return ApiResponse.error(500, "获取启用任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取指定设备相关的任务
     */
    @GetMapping("/device/{deviceId}")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<List<CollectionTask>> getTasksByDevice(@PathVariable Long deviceId) {
        try {
            List<CollectionTask> tasks = collectionTaskService.getTasksByDeviceId(deviceId);
            return ApiResponse.success(tasks);
        } catch (Exception e) {
            log.error("获取设备相关任务失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "获取设备相关任务失败: " + e.getMessage());
        }
    }

    /**
     * 检查任务名称是否存在
     */
    @GetMapping("/check-name")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<Map<String, Object>> checkTaskNameExists(
            @RequestParam String name,
            @RequestParam(required = false) Long excludeId) {
        try {
            boolean exists;
            if (excludeId != null) {
                exists = collectionTaskService.existsByNameExcludeId(name, excludeId);
            } else {
                exists = collectionTaskService.existsByName(name);
            }
            
            return ApiResponse.success(Map.of(
                    "exists", exists,
                    "name", name,
                    "message", exists ? "任务名称已存在" : "任务名称可用"
            ));
        } catch (Exception e) {
            log.error("检查任务名称失败: name={}, excludeId={}", name, excludeId, e);
            return ApiResponse.error(500, "检查任务名称失败: " + e.getMessage());
        }
    }

    /**
     * 执行采集任务
     */
    @PostMapping("/{taskId}/execute")
    @PreAuthorize("hasPermission('task', 'execute')")
    public ApiResponse<Void> executeTask(@PathVariable Long taskId) {
        try {
            collectionTaskService.executeTask(taskId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("执行采集任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "执行任务失败: " + e.getMessage());
        }
    }

    /**
     * 手动执行采集任务
     */
    @PostMapping("/{taskId}/manual-execute")
    @PreAuthorize("hasPermission('task', 'execute')")
    public ApiResponse<Map<String, Object>> manualExecuteTask(@PathVariable Long taskId) {
        try {
            Map<String, Object> result = collectionTaskService.manualExecuteTask(taskId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("手动执行采集任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "手动执行任务失败: " + e.getMessage());
        }
    }

    /**
     * 暂停任务
     */
    @PostMapping("/{taskId}/pause")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<Void> pauseTask(@PathVariable Long taskId) {
        try {
            collectionTaskService.pauseTask(taskId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("暂停任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "暂停任务失败: " + e.getMessage());
        }
    }

    /**
     * 恢复任务
     */
    @PostMapping("/{taskId}/resume")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<Void> resumeTask(@PathVariable Long taskId) {
        try {
            collectionTaskService.resumeTask(taskId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("恢复任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "恢复任务失败: " + e.getMessage());
        }
    }

    /**
     * 启用任务
     */
    @PostMapping("/{taskId}/enable")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<Void> enableTask(@PathVariable Long taskId) {
        try {
            collectionTaskService.enableTask(taskId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("启用任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "启用任务失败: " + e.getMessage());
        }
    }

    /**
     * 禁用任务
     */
    @PostMapping("/{taskId}/disable")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<Void> disableTask(@PathVariable Long taskId) {
        try {
            collectionTaskService.disableTask(taskId);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("禁用任务失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "禁用任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务统计信息
     */
    @GetMapping("/{taskId}/statistics")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<Map<String, Object>> getTaskStatistics(@PathVariable Long taskId) {
        try {
            Map<String, Object> statistics = collectionTaskService.getTaskStatistics(taskId);
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取任务统计失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "获取任务统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务执行历史
     */
    @GetMapping("/{taskId}/execution-history")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<List<Map<String, Object>>> getTaskExecutionHistory(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<Map<String, Object>> history = collectionTaskService.getTaskExecutionHistory(taskId, limit);
            return ApiResponse.success(history);
        } catch (Exception e) {
            log.error("获取任务执行历史失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "获取任务执行历史失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务执行日志
     */
    @GetMapping("/{taskId}/execution-logs")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<List<Map<String, Object>>> getTaskExecutionLogs(
            @PathVariable Long taskId,
            @RequestParam(defaultValue = "50") int limit) {
        try {
            List<Map<String, Object>> logs = collectionTaskService.getTaskExecutionLogs(taskId, limit);
            return ApiResponse.success(logs);
        } catch (Exception e) {
            log.error("获取任务执行日志失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "获取任务执行日志失败: " + e.getMessage());
        }
    }

    /**
     * 验证任务配置
     */
    @PostMapping("/validate")
    @PreAuthorize("hasPermission('task', 'create')")
    public ApiResponse<Map<String, Object>> validateTaskConfig(@RequestBody TaskCreateRequest request) {
        try {
            Map<String, Object> result = collectionTaskService.validateTaskConfig(request);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("验证任务配置失败", e);
            return ApiResponse.error(500, "验证任务配置失败: " + e.getMessage());
        }
    }

    /**
     * 测试任务执行
     */
    @PostMapping("/{taskId}/test")
    @PreAuthorize("hasPermission('task', 'execute')")
    public ApiResponse<Map<String, Object>> testTaskExecution(@PathVariable Long taskId) {
        try {
            Map<String, Object> result = collectionTaskService.testTaskExecution(taskId);
            return ApiResponse.success(result);
        } catch (Exception e) {
            log.error("测试任务执行失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "测试任务执行失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务概览统计
     */
    @GetMapping("/overview-statistics")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<Map<String, Object>> getTaskOverviewStatistics() {
        try {
            Map<String, Object> statistics = collectionTaskService.getTaskOverviewStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取任务概览统计失败", e);
            return ApiResponse.error(500, "获取任务概览统计失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务性能排名
     */
    @GetMapping("/performance-ranking")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<List<Map<String, Object>>> getTaskPerformanceRanking(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String date) {
        try {
            if (date == null) {
                date = java.time.LocalDate.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            }
            List<Map<String, Object>> ranking = collectionTaskService.getTaskPerformanceRanking(date, limit);
            return ApiResponse.success(ranking);
        } catch (Exception e) {
            log.error("获取任务性能排名失败", e);
            return ApiResponse.error(500, "获取任务性能排名失败: " + e.getMessage());
        }
    }

    /**
     * 批量更新任务状态
     */
    @PostMapping("/batch-update-status")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<Map<String, Object>> batchUpdateTaskStatus(
            @RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<?> taskIdsRaw = (List<?>) request.get("taskIds");
            Integer status = (Integer) request.get("status");

            if (taskIdsRaw == null || taskIdsRaw.isEmpty()) {
                return ApiResponse.error(400, "任务ID列表不能为空");
            }
            if (status == null) {
                return ApiResponse.error(400, "状态不能为空");
            }

            // 将Integer转换为Long
            List<Long> taskIds = taskIdsRaw.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            throw new IllegalArgumentException("无效的任务ID类型: " + id.getClass());
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());

            int updatedCount = collectionTaskService.batchUpdateTaskStatus(taskIds, status);
            return ApiResponse.success(Map.of(
                    "updatedCount", updatedCount,
                    "totalCount", taskIds.size()
            ));
        } catch (Exception e) {
            log.error("批量更新任务状态失败", e);
            return ApiResponse.error(500, "批量更新任务状态失败: " + e.getMessage());
        }
    }

    /**
     * 批量启用任务
     */
    @PostMapping("/batch-enable")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<Map<String, Object>> batchEnableTasks(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<?> taskIdsRaw = (List<?>) request.get("taskIds");

            if (taskIdsRaw == null || taskIdsRaw.isEmpty()) {
                return ApiResponse.error(400, "任务ID列表不能为空");
            }

            // 将Integer转换为Long
            List<Long> taskIds = taskIdsRaw.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            throw new IllegalArgumentException("无效的任务ID类型: " + id.getClass());
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());

            int updatedCount = collectionTaskService.batchEnableTasks(taskIds);
            return ApiResponse.success(Map.of(
                    "updatedCount", updatedCount,
                    "totalCount", taskIds.size()
            ));
        } catch (Exception e) {
            log.error("批量启用任务失败", e);
            return ApiResponse.error(500, "批量启用任务失败: " + e.getMessage());
        }
    }

    /**
     * 批量禁用任务
     */
    @PostMapping("/batch-disable")
    @PreAuthorize("hasPermission('task', 'update')")
    public ApiResponse<Map<String, Object>> batchDisableTasks(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<?> taskIdsRaw = (List<?>) request.get("taskIds");

            if (taskIdsRaw == null || taskIdsRaw.isEmpty()) {
                return ApiResponse.error(400, "任务ID列表不能为空");
            }

            // 将Integer转换为Long
            List<Long> taskIds = taskIdsRaw.stream()
                    .map(id -> {
                        if (id instanceof Integer) {
                            return ((Integer) id).longValue();
                        } else if (id instanceof Long) {
                            return (Long) id;
                        } else {
                            throw new IllegalArgumentException("无效的任务ID类型: " + id.getClass());
                        }
                    })
                    .collect(java.util.stream.Collectors.toList());

            int updatedCount = collectionTaskService.batchDisableTasks(taskIds);
            return ApiResponse.success(Map.of(
                    "updatedCount", updatedCount,
                    "totalCount", taskIds.size()
            ));
        } catch (Exception e) {
            log.error("批量禁用任务失败", e);
            return ApiResponse.error(500, "批量禁用任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取任务调度信息
     */
    @GetMapping("/{taskId}/schedule-info")
    @PreAuthorize("hasPermission('task', 'view')")
    public ApiResponse<Map<String, Object>> getTaskScheduleInfo(@PathVariable Long taskId) {
        try {
            Map<String, Object> scheduleInfo = collectionTaskService.getTaskScheduleInfo(taskId);
            return ApiResponse.success(scheduleInfo);
        } catch (Exception e) {
            log.error("获取任务调度信息失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "获取任务调度信息失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期任务数据
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasPermission('task', 'manage')")
    public ApiResponse<Map<String, Object>> cleanupExpiredTaskData(
            @RequestParam(defaultValue = "30") int days) {
        try {
            String beforeDate = java.time.LocalDate.now()
                    .minusDays(days)
                    .format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            
            int deletedCount = collectionTaskService.cleanupExpiredTaskData(beforeDate);
            return ApiResponse.success(Map.of(
                    "deletedCount", deletedCount,
                    "beforeDate", beforeDate
            ));
        } catch (Exception e) {
            log.error("清理过期任务数据失败", e);
            return ApiResponse.error(500, "清理过期任务数据失败: " + e.getMessage());
        }
    }
}
