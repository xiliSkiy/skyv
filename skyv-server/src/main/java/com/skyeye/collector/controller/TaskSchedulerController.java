package com.skyeye.collector.controller;

import com.skyeye.collector.scheduler.SchedulerStatistics;
import com.skyeye.collector.scheduler.SchedulerStatus;
import com.skyeye.collector.scheduler.TaskScheduler;
import com.skyeye.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 任务调度器控制器
 * 
 * @author SkyEye Team
 */
@Slf4j
@RestController
@RequestMapping("/api/collector/scheduler")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class TaskSchedulerController {

    private final TaskScheduler taskScheduler;

    /**
     * 获取调度器状态
     */
    @GetMapping("/status")
    @PreAuthorize("hasPermission('scheduler', 'view')")
    public ApiResponse<Map<String, Object>> getSchedulerStatus() {
        try {
            SchedulerStatus status = taskScheduler.getStatus();
            return ApiResponse.success(Map.of(
                    "status", status.name(),
                    "description", status.getDescription(),
                    "canAcceptTasks", status.canAcceptTasks(),
                    "isRunning", status.isRunning(),
                    "isStopped", status.isStopped()
            ));
        } catch (Exception e) {
            log.error("获取调度器状态失败", e);
            return ApiResponse.error(500, "获取调度器状态失败: " + e.getMessage());
        }
    }

    /**
     * 启动调度器
     */
    @PostMapping("/start")
    @PreAuthorize("hasPermission('scheduler', 'manage')")
    public ApiResponse<Map<String, Object>> startScheduler() {
        try {
            taskScheduler.start();
            return ApiResponse.success(Map.of(
                    "message", "调度器启动成功",
                    "status", taskScheduler.getStatus().name(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("启动调度器失败", e);
            return ApiResponse.error(500, "启动调度器失败: " + e.getMessage());
        }
    }

    /**
     * 停止调度器
     */
    @PostMapping("/stop")
    @PreAuthorize("hasPermission('scheduler', 'manage')")
    public ApiResponse<Map<String, Object>> stopScheduler() {
        try {
            taskScheduler.stop();
            return ApiResponse.success(Map.of(
                    "message", "调度器停止成功",
                    "status", taskScheduler.getStatus().name(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("停止调度器失败", e);
            return ApiResponse.error(500, "停止调度器失败: " + e.getMessage());
        }
    }

    /**
     * 获取调度器统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('scheduler', 'view')")
    public ApiResponse<SchedulerStatistics> getSchedulerStatistics() {
        try {
            SchedulerStatistics statistics = taskScheduler.getStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取调度器统计信息失败", e);
            return ApiResponse.error(500, "获取调度器统计信息失败: " + e.getMessage());
        }
    }

    /**
     * 获取已调度的任务ID列表
     */
    @GetMapping("/scheduled-tasks")
    @PreAuthorize("hasPermission('scheduler', 'view')")
    public ApiResponse<List<Long>> getScheduledTaskIds() {
        try {
            List<Long> taskIds = taskScheduler.getScheduledTaskIds();
            return ApiResponse.success(taskIds);
        } catch (Exception e) {
            log.error("获取已调度任务列表失败", e);
            return ApiResponse.error(500, "获取已调度任务列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查任务是否已调度
     */
    @GetMapping("/tasks/{taskId}/scheduled")
    @PreAuthorize("hasPermission('scheduler', 'view')")
    public ApiResponse<Map<String, Object>> isTaskScheduled(@PathVariable Long taskId) {
        try {
            boolean isScheduled = taskScheduler.isTaskScheduled(taskId);
            LocalDateTime nextExecutionTime = taskScheduler.getNextExecutionTime(taskId);
            
            return ApiResponse.success(Map.of(
                    "taskId", taskId,
                    "isScheduled", isScheduled,
                    "nextExecutionTime", nextExecutionTime
            ));
        } catch (Exception e) {
            log.error("检查任务调度状态失败: taskId={}", taskId, e);
            return ApiResponse.error(500, "检查任务调度状态失败: " + e.getMessage());
        }
    }

    /**
     * 重新加载所有任务
     */
    @PostMapping("/reload")
    @PreAuthorize("hasPermission('scheduler', 'manage')")
    public ApiResponse<Map<String, Object>> reloadAllTasks() {
        try {
            taskScheduler.reloadAllTasks();
            return ApiResponse.success(Map.of(
                    "message", "重新加载任务成功",
                    "scheduledTaskCount", taskScheduler.getScheduledTaskIds().size(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("重新加载任务失败", e);
            return ApiResponse.error(500, "重新加载任务失败: " + e.getMessage());
        }
    }

    /**
     * 清理过期任务
     */
    @PostMapping("/cleanup")
    @PreAuthorize("hasPermission('scheduler', 'manage')")
    public ApiResponse<Map<String, Object>> cleanupExpiredTasks() {
        try {
            taskScheduler.cleanupExpiredTasks();
            return ApiResponse.success(Map.of(
                    "message", "清理过期任务成功",
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("清理过期任务失败", e);
            return ApiResponse.error(500, "清理过期任务失败: " + e.getMessage());
        }
    }

    /**
     * 获取调度器健康状态
     */
    @GetMapping("/health")
    @PreAuthorize("hasPermission('scheduler', 'view')")
    public ApiResponse<Map<String, Object>> getSchedulerHealth() {
        try {
            SchedulerStatus status = taskScheduler.getStatus();
            SchedulerStatistics statistics = taskScheduler.getStatistics();
            
            boolean isHealthy = status == SchedulerStatus.RUNNING;
            String healthMessage = isHealthy ? "调度器运行正常" : "调度器状态异常: " + status.getDescription();
            
            return ApiResponse.success(Map.of(
                    "healthy", isHealthy,
                    "message", healthMessage,
                    "status", status.name(),
                    "uptime", statistics.getFormattedUptime(),
                    "totalTasks", statistics.getTotalTasks(),
                    "scheduledTasks", statistics.getScheduledTasks(),
                    "runningTasks", statistics.getRunningTasks(),
                    "lastActiveTime", statistics.getLastActiveTime(),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("获取调度器健康状态失败", e);
            return ApiResponse.error(500, "获取调度器健康状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取调度器性能指标
     */
    @GetMapping("/metrics")
    @PreAuthorize("hasPermission('scheduler', 'view')")
    public ApiResponse<Map<String, Object>> getSchedulerMetrics() {
        try {
            SchedulerStatistics statistics = taskScheduler.getStatistics();
            
            return ApiResponse.success(Map.of(
                    "performance", Map.of(
                            "successRate", statistics.getSuccessRate(),
                            "failureRate", statistics.getFailureRate(),
                            "averageExecutionTime", statistics.getAverageExecutionTime(),
                            "minExecutionTime", statistics.getMinExecutionTime(),
                            "maxExecutionTime", statistics.getMaxExecutionTime()
                    ),
                    "resources", Map.of(
                            "activeThreads", statistics.getActiveThreads(),
                            "totalThreads", statistics.getTotalThreads(),
                            "queuedTasks", statistics.getQueuedTasks(),
                            "memoryUsage", statistics.getMemoryUsage(),
                            "cpuUsage", statistics.getCpuUsage()
                    ),
                    "execution", Map.of(
                            "todayExecutions", statistics.getTodayExecutions(),
                            "todaySuccesses", statistics.getTodaySuccesses(),
                            "todayFailures", statistics.getTodayFailures(),
                            "totalExecutions", statistics.getTodayExecutions()
                    ),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("获取调度器性能指标失败", e);
            return ApiResponse.error(500, "获取调度器性能指标失败: " + e.getMessage());
        }
    }

    /**
     * 获取调度器配置信息
     */
    @GetMapping("/config")
    @PreAuthorize("hasPermission('scheduler', 'view')")
    public ApiResponse<Map<String, Object>> getSchedulerConfig() {
        try {
            return ApiResponse.success(Map.of(
                    "threadPoolSize", 10, // 从配置中获取
                    "maxQueueSize", 100,  // 从配置中获取
                    "keepAliveSeconds", 60, // 从配置中获取
                    "cleanupIntervalMinutes", 30, // 从配置中获取
                    "features", List.of(
                            "SIMPLE_SCHEDULING",
                            "CRON_SCHEDULING", 
                            "EVENT_SCHEDULING",
                            "TASK_PAUSE_RESUME",
                            "AUTOMATIC_CLEANUP",
                            "STATISTICS_COLLECTION"
                    ),
                    "supportedScheduleTypes", List.of("SIMPLE", "CRON", "EVENT"),
                    "timestamp", LocalDateTime.now()
            ));
        } catch (Exception e) {
            log.error("获取调度器配置信息失败", e);
            return ApiResponse.error(500, "获取调度器配置信息失败: " + e.getMessage());
        }
    }
}
