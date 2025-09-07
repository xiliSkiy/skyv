package com.skyeye.collector.service.impl;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.dto.TaskCreateRequest;
import com.skyeye.collector.dto.TaskQueryRequest;
import com.skyeye.collector.dto.TaskUpdateRequest;
import com.skyeye.collector.dto.TaskExecutionResult;
import com.skyeye.collector.entity.CollectionLog;
import com.skyeye.collector.entity.CollectionTask;
import com.skyeye.collector.entity.TaskStatistics;
import com.skyeye.collector.engine.CollectorEngine;
import com.skyeye.collector.repository.CollectionLogRepository;
import com.skyeye.collector.repository.CollectionTaskRepository;
import com.skyeye.collector.repository.TaskStatisticsRepository;
import com.skyeye.collector.scheduler.TaskScheduler;
import com.skyeye.collector.service.CollectionTaskService;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.util.JsonUtils;
import com.skyeye.device.entity.Device;
import com.skyeye.device.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**
 * 采集任务服务实现
 * 
 * @author SkyEye Team
 */
@Slf4j
@Service
public class CollectionTaskServiceImpl implements CollectionTaskService {

    private final CollectionTaskRepository collectionTaskRepository;
    private final TaskStatisticsRepository taskStatisticsRepository;
    private final CollectionLogRepository collectionLogRepository;
    private final DeviceRepository deviceRepository;
    private final CollectorEngine collectorEngine;
    private final TaskScheduler taskScheduler;
    private final Executor taskExecutor;

    public CollectionTaskServiceImpl(CollectionTaskRepository collectionTaskRepository,
                                   TaskStatisticsRepository taskStatisticsRepository,
                                   CollectionLogRepository collectionLogRepository,
                                   DeviceRepository deviceRepository,
                                   CollectorEngine collectorEngine,
                                   @Lazy TaskScheduler taskScheduler,
                                   @Qualifier("applicationTaskExecutor") Executor taskExecutor) {
        this.collectionTaskRepository = collectionTaskRepository;
        this.taskStatisticsRepository = taskStatisticsRepository;
        this.collectionLogRepository = collectionLogRepository;
        this.deviceRepository = deviceRepository;
        this.collectorEngine = collectorEngine;
        this.taskScheduler = taskScheduler;
        this.taskExecutor = taskExecutor;
    }

    @Override
    @Transactional
    public CollectionTask createTask(TaskCreateRequest request) {
        log.info("开始创建采集任务: {}", request.getName());

        // 1. 验证任务名称唯一性
        if (collectionTaskRepository.existsByName(request.getName())) {
            throw new BusinessException("任务名称已存在: " + request.getName());
        }

        // 2. 验证目标设备
        List<Device> devices = deviceRepository.findAllById(request.getTargetDevices());
        if (devices.size() != request.getTargetDevices().size()) {
            throw new BusinessException("部分目标设备不存在");
        }

        // 3. 处理指标配置（如果为空则自动生成）
        List<MetricConfig> metricsConfig = request.getMetricsConfig();
        if (metricsConfig == null || metricsConfig.isEmpty()) {
            log.info("指标配置为空，开始自动生成默认指标配置...");
            metricsConfig = generateDefaultMetricsConfig(devices);
            log.info("自动生成了 {} 个默认指标配置", metricsConfig.size());
        } else {
            // 如果提供了指标配置，仍然需要验证
            validateMetricsConfig(metricsConfig);
        }

        // 4. 创建任务
        CollectionTask task = new CollectionTask();
        task.setName(request.getName());
        task.setDescription(request.getDescription());
        task.setCollectorId(request.getCollectorId());
        task.setScheduleType(request.getScheduleType());
        task.setScheduleConfig(request.getScheduleConfig());
        task.setTargetDevices(request.getTargetDevices());
        task.setMetricsConfig(metricsConfig);
        task.setStatus(1); // 默认启用
        task.setPriority(request.getPriority());
        task.setEnableRetry(request.getEnableRetry());
        task.setRetryTimes(request.getRetryTimes());
        task.setRetryInterval(request.getRetryInterval());
        task.setTimeout(request.getTimeout());
        task.setMaxConcurrency(request.getMaxConcurrency());
        task.setTags(request.getTags());
        task.setParameters(request.getParameters());
        task.setIsEnabled(true);
        task.setEffectiveTime(request.getEffectiveTime());
        task.setExpireTime(request.getExpireTime());
        task.setRemarks(request.getRemarks());

        // 5. 计算下次执行时间
        task.setNextExecutionTime(calculateNextExecutionTime(request.getScheduleType(), request.getScheduleConfig()));

        // 6. 保存任务
        task = collectionTaskRepository.save(task);

        // 7. 自动调度任务（如果任务已启用）
        if (task.getIsEnabled() && task.getStatus() == 1) {
            try {
                taskScheduler.scheduleTask(task);
                log.info("任务已自动添加到调度器: taskId={}, name={}", task.getId(), task.getName());
            } catch (Exception e) {
                log.warn("自动调度任务失败，任务已创建但未调度: taskId={}, error={}", task.getId(), e.getMessage());
            }
        }

        log.info("采集任务创建成功: taskId={}, name={}", task.getId(), task.getName());

        return task;
    }

    @Override
    @Transactional
    public CollectionTask updateTask(Long taskId, TaskUpdateRequest request) {
        log.info("开始更新采集任务: taskId={}", taskId);

        CollectionTask task = collectionTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在: " + taskId));

        // 1. 验证任务名称唯一性（如果修改了名称）
        if (request.getName() != null && !request.getName().equals(task.getName())) {
            if (collectionTaskRepository.existsByNameExcludeId(request.getName(), taskId)) {
                throw new BusinessException("任务名称已存在: " + request.getName());
            }
            task.setName(request.getName());
        }

        // 2. 更新其他字段
        if (request.getDescription() != null) {
            task.setDescription(request.getDescription());
        }
        if (request.getCollectorId() != null) {
            task.setCollectorId(request.getCollectorId());
        }
        if (request.getScheduleType() != null) {
            task.setScheduleType(request.getScheduleType());
        }
        if (request.getScheduleConfig() != null) {
            task.setScheduleConfig(request.getScheduleConfig());
            // 重新计算下次执行时间
            task.setNextExecutionTime(calculateNextExecutionTime(task.getScheduleType(), task.getScheduleConfig()));
        }
        if (request.getTargetDevices() != null) {
            // 验证目标设备
            List<Device> devices = deviceRepository.findAllById(request.getTargetDevices());
            if (devices.size() != request.getTargetDevices().size()) {
                throw new BusinessException("部分目标设备不存在");
            }
            task.setTargetDevices(request.getTargetDevices());
        }
        if (request.getMetricsConfig() != null) {
            validateMetricsConfig(request.getMetricsConfig());
            task.setMetricsConfig(request.getMetricsConfig());
        }
        if (request.getStatus() != null) {
            task.setStatus(request.getStatus());
        }
        if (request.getPriority() != null) {
            task.setPriority(request.getPriority());
        }
        if (request.getEnableRetry() != null) {
            task.setEnableRetry(request.getEnableRetry());
        }
        if (request.getRetryTimes() != null) {
            task.setRetryTimes(request.getRetryTimes());
        }
        if (request.getRetryInterval() != null) {
            task.setRetryInterval(request.getRetryInterval());
        }
        if (request.getTimeout() != null) {
            task.setTimeout(request.getTimeout());
        }
        if (request.getMaxConcurrency() != null) {
            task.setMaxConcurrency(request.getMaxConcurrency());
        }
        if (request.getTags() != null) {
            task.setTags(request.getTags());
        }
        if (request.getParameters() != null) {
            task.setParameters(request.getParameters());
        }
        if (request.getIsEnabled() != null) {
            task.setIsEnabled(request.getIsEnabled());
        }
        if (request.getRemarks() != null) {
            task.setRemarks(request.getRemarks());
        }
        if (request.getEffectiveTime() != null) {
            task.setEffectiveTime(request.getEffectiveTime());
        }
        if (request.getExpireTime() != null) {
            task.setExpireTime(request.getExpireTime());
        }

        task = collectionTaskRepository.save(task);

        // 重新调度任务（如果任务已启用）
        if (task.getIsEnabled() && task.getStatus() == 1) {
            try {
                taskScheduler.rescheduleTask(task);
                log.info("任务已重新调度: taskId={}, name={}", task.getId(), task.getName());
            } catch (Exception e) {
                log.warn("重新调度任务失败: taskId={}, error={}", task.getId(), e.getMessage());
            }
        } else {
            // 如果任务被禁用，取消调度
            try {
                taskScheduler.cancelTask(task.getId());
                log.info("任务已取消调度: taskId={}, name={}", task.getId(), task.getName());
            } catch (Exception e) {
                log.warn("取消任务调度失败: taskId={}, error={}", task.getId(), e.getMessage());
            }
        }

        log.info("采集任务更新成功: taskId={}, name={}", task.getId(), task.getName());

        return task;
    }

    @Override
    @Transactional
    public void deleteTask(Long taskId) {
        log.info("开始删除采集任务: taskId={}", taskId);

        CollectionTask task = collectionTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在: " + taskId));

        // 检查任务状态
        if (task.getStatus() == 1) {
            throw new BusinessException("无法删除正在运行的任务，请先停止任务");
        }

        // 取消任务调度
        try {
            taskScheduler.cancelTask(taskId);
            log.info("任务调度已取消: taskId={}", taskId);
        } catch (Exception e) {
            log.warn("取消任务调度失败: taskId={}, error={}", taskId, e.getMessage());
        }

        // 删除相关统计数据
        taskStatisticsRepository.deleteByTaskId(taskId);

        // 删除任务
        collectionTaskRepository.deleteById(taskId);

        log.info("采集任务删除成功: taskId={}, name={}", taskId, task.getName());
    }

    @Override
    public CollectionTask getTaskById(Long taskId) {
        return collectionTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在: " + taskId));
    }

    @Override
    public CollectionTask getTaskByName(String name) {
        return collectionTaskRepository.findByName(name)
                .orElseThrow(() -> new BusinessException("任务不存在: " + name));
    }

    @Override
    public Page<CollectionTask> getTasks(TaskQueryRequest request) {
        // 构建查询条件
        Specification<CollectionTask> spec = (root, query, criteriaBuilder) -> {
            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (StringUtils.hasText(request.getName())) {
                predicates.add(criteriaBuilder.like(root.get("name"), "%" + request.getName() + "%"));
            }

            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            if (StringUtils.hasText(request.getScheduleType())) {
                predicates.add(criteriaBuilder.equal(root.get("scheduleType"), request.getScheduleType()));
            }

            if (request.getCollectorId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("collectorId"), request.getCollectorId()));
            }

            if (request.getIsEnabled() != null) {
                predicates.add(criteriaBuilder.equal(root.get("isEnabled"), request.getIsEnabled()));
            }

            if (request.getMinPriority() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("priority"), request.getMinPriority()));
            }

            if (request.getMaxPriority() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("priority"), request.getMaxPriority()));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        // 构建分页和排序
        Sort sort = Sort.by(Sort.Direction.fromString(request.getSortOrder()), request.getSortBy());
        Pageable pageable = PageRequest.of(request.getPage() - 1, request.getSize(), sort);

        return collectionTaskRepository.findAll(spec, pageable);
    }

    @Override
    public List<CollectionTask> getAllEnabledTasks() {
        return collectionTaskRepository.findByIsEnabledTrue();
    }

    @Override
    public List<CollectionTask> getTasksByDeviceId(Long deviceId) {
        return collectionTaskRepository.findTasksByDeviceId(deviceId);
    }

    @Override
    public boolean existsByName(String name) {
        log.debug("检查任务名称是否存在: name={}", name);
        return collectionTaskRepository.existsByName(name);
    }

    @Override
    public boolean existsByNameExcludeId(String name, Long excludeId) {
        log.debug("检查任务名称是否存在（排除ID）: name={}, excludeId={}", name, excludeId);
        return collectionTaskRepository.existsByNameExcludeId(name, excludeId);
    }

    @Override
    public TaskExecutionResult executeTask(Long taskId) {
        log.info("开始执行采集任务: taskId={}", taskId);

        CollectionTask task = collectionTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在: " + taskId));

        if (!task.getIsEnabled() || task.getStatus() != 1) {
            log.warn("任务未启用或状态不正确: taskId={}, enabled={}, status={}", 
                    taskId, task.getIsEnabled(), task.getStatus());
            return TaskExecutionResult.builder()
                    .taskId(taskId)
                    .taskName(task.getName())
                    .success(false)
                    .message("任务未启用或状态不正确")
                    .build();
        }

        // 异步执行任务
        String executionId = java.util.UUID.randomUUID().toString();
        CompletableFuture.runAsync(() -> doExecuteTask(task), taskExecutor);

        return TaskExecutionResult.builder()
                .taskId(taskId)
                .taskName(task.getName())
                .executionId(executionId)
                .success(true)
                .message("任务已提交执行")
                .build();
    }

    @Override
    public Map<String, Object> manualExecuteTask(Long taskId) {
        log.info("开始手动执行采集任务: taskId={}", taskId);

        CollectionTask task = collectionTaskRepository.findById(taskId)
                .orElseThrow(() -> new BusinessException("任务不存在: " + taskId));

        if (!task.getIsEnabled()) {
            throw new BusinessException("任务未启用，无法执行");
        }

        // 异步执行任务
        CompletableFuture.runAsync(() -> doExecuteTask(task), taskExecutor);

        return Map.of(
                "taskId", taskId,
                "taskName", task.getName(),
                "status", "EXECUTING",
                "message", "任务已提交执行"
        );
    }

    @Override
    @Transactional
    public void pauseTask(Long taskId) {
        updateTaskStatus(taskId, 2); // 2: 暂停
        log.info("任务已暂停: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void resumeTask(Long taskId) {
        updateTaskStatus(taskId, 1); // 1: 启用
        log.info("任务已恢复: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void enableTask(Long taskId) {
        CollectionTask task = getTaskById(taskId);
        task.setIsEnabled(true);
        collectionTaskRepository.save(task);
        log.info("任务已启用: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void disableTask(Long taskId) {
        CollectionTask task = getTaskById(taskId);
        task.setIsEnabled(false);
        collectionTaskRepository.save(task);
        log.info("任务已禁用: taskId={}", taskId);
    }

    @Override
    @Transactional
    public void updateTaskStatus(Long taskId, Integer status) {
        collectionTaskRepository.updateTaskStatus(taskId, status, new Timestamp(System.currentTimeMillis()));
        log.info("任务状态已更新: taskId={}, status={}", taskId, status);
    }

    @Override
    public Map<String, Object> getTaskStatistics(Long taskId) {
        CollectionTask task = getTaskById(taskId);
        
        Map<String, Object> statistics = new HashMap<>();
        statistics.put("taskId", taskId);
        statistics.put("taskName", task.getName());
        statistics.put("executionCount", task.getExecutionCount());
        statistics.put("successCount", task.getSuccessCount());
        statistics.put("failureCount", task.getFailureCount());
        statistics.put("successRate", task.getExecutionCount() > 0 ? 
                (double) task.getSuccessCount() / task.getExecutionCount() * 100 : 0.0);
        statistics.put("averageExecutionTime", task.getAverageExecutionTime());
        statistics.put("lastExecutionTime", task.getLastExecutionTime());
        statistics.put("lastExecutionStatus", task.getLastExecutionStatus());
        statistics.put("lastExecutionError", task.getLastExecutionError());

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getTaskExecutionHistory(Long taskId, int limit) {
        // 这里可以扩展为查询更详细的执行历史
        return new ArrayList<>();
    }

    @Override
    public List<Map<String, Object>> getTaskExecutionLogs(Long taskId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by(Sort.Direction.DESC, "startTime"));
        Page<CollectionLog> logs = collectionLogRepository.findByTaskIdOrderByStartTimeDesc(taskId, pageable);
        
        return logs.getContent().stream()
                .map(log -> {
                    Map<String, Object> result = new HashMap<>();
                    result.put("id", log.getId());
                    result.put("executionId", log.getExecutionId());
                    result.put("startTime", log.getStartTime());
                    result.put("status", log.getStatus());
                    result.put("success", log.getSuccess());
                    result.put("errorMessage", log.getErrorMessage());
                    result.put("responseTime", log.getResponseTime());
                    return result;
                })
                .toList();
    }

    @Override
    public Map<String, Object> validateTaskConfig(TaskCreateRequest request) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 验证基本信息
        if (request.getName() == null || request.getName().trim().isEmpty()) {
            errors.add("任务名称不能为空");
        }

        // 验证调度配置（如果有的话）
        if (request.getScheduleType() != null) {
            if ("SIMPLE".equals(request.getScheduleType())) {
                if (request.getScheduleConfig() != null && 
                    (!request.getScheduleConfig().containsKey("frequency") || 
                     !request.getScheduleConfig().containsKey("interval"))) {
                    errors.add("简单调度必须包含frequency和interval配置");
                }
            } else if ("CRON".equals(request.getScheduleType())) {
                if (request.getScheduleConfig() != null && 
                    !request.getScheduleConfig().containsKey("cronExpression")) {
                    errors.add("Cron调度必须包含cronExpression配置");
                }
            }
        }

        // 验证目标设备（只在设备选择步骤后验证）
        if (request.getTargetDevices() != null && request.getTargetDevices().isEmpty()) {
            warnings.add("目标设备列表为空");
        }

        // 验证指标配置（只在指标配置步骤后验证）
        if (request.getMetricsConfig() != null && request.getMetricsConfig().isEmpty()) {
            warnings.add("指标配置列表为空");
        }

        // 如果是完整验证（包含设备和指标），则要求这些字段必填
        boolean isCompleteValidation = request.getTargetDevices() != null && request.getMetricsConfig() != null;
        if (isCompleteValidation) {
            if (request.getTargetDevices().isEmpty()) {
                errors.add("目标设备不能为空");
            }
            if (request.getMetricsConfig().isEmpty()) {
                errors.add("指标配置不能为空");
            }
        }

        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("warnings", warnings);

        return result;
    }

    @Override
    public Map<String, Object> testTaskExecution(Long taskId) {
        // 这里可以实现任务执行测试逻辑
        return Map.of(
                "taskId", taskId,
                "status", "TESTING",
                "message", "任务测试功能待实现"
        );
    }

    @Override
    public Map<String, Object> getTaskOverviewStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 统计各状态的任务数量
        List<Object[]> statusStats = collectionTaskRepository.countByStatus();
        Map<String, Long> statusMap = new HashMap<>();
        for (Object[] stat : statusStats) {
            statusMap.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("statusStatistics", statusMap);

        // 统计各调度类型的任务数量
        List<Object[]> scheduleStats = collectionTaskRepository.countByScheduleType();
        Map<String, Long> scheduleMap = new HashMap<>();
        for (Object[] stat : scheduleStats) {
            scheduleMap.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("scheduleStatistics", scheduleMap);

        // 统计各优先级的任务数量
        List<Object[]> priorityStats = collectionTaskRepository.countByPriority();
        Map<String, Long> priorityMap = new HashMap<>();
        for (Object[] stat : priorityStats) {
            priorityMap.put(stat[0].toString(), (Long) stat[1]);
        }
        statistics.put("priorityStatistics", priorityMap);

        return statistics;
    }

    @Override
    public List<Map<String, Object>> getTaskPerformanceRanking(String date, int limit) {
        // 这里可以实现任务性能排名逻辑
        return new ArrayList<>();
    }

    @Override
    @Transactional
    public int cleanupExpiredTaskData(String beforeDate) {
        return taskStatisticsRepository.deleteStatisticsBefore(beforeDate);
    }

    @Override
    @Transactional
    public int batchUpdateTaskStatus(List<Long> taskIds, Integer status) {
        int updatedCount = 0;
        for (Long taskId : taskIds) {
            try {
                collectionTaskRepository.updateTaskStatus(taskId, status, new Timestamp(System.currentTimeMillis()));
                updatedCount++;
            } catch (Exception e) {
                log.error("批量更新任务状态失败: taskId={}, status={}", taskId, status, e);
            }
        }
        return updatedCount;
    }

    @Override
    @Transactional
    public int batchEnableTasks(List<Long> taskIds) {
        int updatedCount = 0;
        for (Long taskId : taskIds) {
            try {
                enableTask(taskId);
                updatedCount++;
            } catch (Exception e) {
                log.error("批量启用任务失败: taskId={}", taskId, e);
            }
        }
        return updatedCount;
    }

    @Override
    @Transactional
    public int batchDisableTasks(List<Long> taskIds) {
        int updatedCount = 0;
        for (Long taskId : taskIds) {
            try {
                disableTask(taskId);
                updatedCount++;
            } catch (Exception e) {
                log.error("批量禁用任务失败: taskId={}", taskId, e);
            }
        }
        return updatedCount;
    }

    @Override
    public Map<String, Object> getTaskScheduleInfo(Long taskId) {
        CollectionTask task = getTaskById(taskId);
        
        Map<String, Object> scheduleInfo = new HashMap<>();
        scheduleInfo.put("taskId", taskId);
        scheduleInfo.put("scheduleType", task.getScheduleType());
        scheduleInfo.put("scheduleConfig", task.getScheduleConfig());
        scheduleInfo.put("lastExecutionTime", task.getLastExecutionTime());
        scheduleInfo.put("nextExecutionTime", task.getNextExecutionTime());
        scheduleInfo.put("status", task.getStatus());
        scheduleInfo.put("isEnabled", task.getIsEnabled());

        return scheduleInfo;
    }

    @Override
    @Transactional
    public void updateNextExecutionTime(Long taskId, Timestamp nextExecutionTime) {
        collectionTaskRepository.setNextExecutionTime(taskId, nextExecutionTime, new Timestamp(System.currentTimeMillis()));
    }

    @Override
    public List<CollectionTask> getTasksReadyForExecution() {
        return collectionTaskRepository.findTasksReadyForExecution(new Timestamp(System.currentTimeMillis()));
    }

    @Override
    @Transactional
    public void updateTaskExecutionStatistics(Long taskId, boolean success, long executionTime) {
        // 更新任务执行统计
        if (success) {
            collectionTaskRepository.incrementSuccessCount(taskId, new Timestamp(System.currentTimeMillis()));
        } else {
            collectionTaskRepository.incrementFailureCount(taskId, "执行失败", new Timestamp(System.currentTimeMillis()));
        }

        // 更新平均执行时间
        CollectionTask task = getTaskById(taskId);
        long currentAvg = task.getAverageExecutionTime() != null ? task.getAverageExecutionTime() : 0;
        long newAvg = (currentAvg + executionTime) / 2;
        collectionTaskRepository.updateAverageExecutionTime(taskId, newAvg, new Timestamp(System.currentTimeMillis()));

        // 更新任务统计记录
        updateTaskStatisticsRecord(taskId, success, executionTime);
    }

    /**
     * 执行采集任务的核心逻辑
     */
    private void doExecuteTask(CollectionTask task) {
        String executionId = UUID.randomUUID().toString();
        Timestamp startTime = new Timestamp(System.currentTimeMillis());
        long startTimeMillis = System.currentTimeMillis();

        try {
            // 1. 记录执行开始
            CollectionLog log = new CollectionLog();
            log.setTaskId(task.getId());
            log.setExecutionId(executionId);
            log.setStartTime(startTime);
            log.setStatus("RUNNING");
            log.setPluginType("TASK_EXECUTOR");
            collectionLogRepository.save(log);

            // 2. 更新任务执行次数
            collectionTaskRepository.incrementExecutionCount(task.getId(), startTime, new Timestamp(System.currentTimeMillis()));

            // 3. 执行采集任务
            int successCount = 0;
            int failedCount = 0;
            int totalMetrics = 0;

            for (Long deviceId : task.getTargetDevices()) {
                Device device = deviceRepository.findById(deviceId).orElse(null);
                if (device == null) {
                    log.warn("设备不存在，跳过: deviceId={}", deviceId);
                    continue;
                }

                for (var metricConfig : task.getMetricsConfig()) {
                    totalMetrics++;
                    try {
                        CollectionContext context = CollectionContext.createDefault();
                        context.setTaskId(task.getId());
                        context.setSessionId(executionId);

                        CollectionResult result = collectorEngine.executeCollection(device, metricConfig, context);
                        if (result.isSuccess()) {
                            successCount++;
                        } else {
                            failedCount++;
                            log.warn("指标采集失败: deviceId={}, metric={}, error={}", 
                                    deviceId, metricConfig.getMetricName(), result.getErrorMessage());
                        }
                    } catch (Exception e) {
                        failedCount++;
                        log.error("指标采集异常: deviceId={}, metric={}", 
                                deviceId, metricConfig.getMetricName(), e);
                    }
                }
            }

            // 4. 更新执行结果
            long executionTime = System.currentTimeMillis() - startTimeMillis;
            log.setEndTime(new Timestamp(System.currentTimeMillis()));
            log.setStatus("COMPLETED");
            log.setSuccess(successCount > 0);
            log.setResponseTime(executionTime);
            log.setDataCount(totalMetrics);
            log.setExtraData(JsonUtils.toJson(Map.of(
                    "successCount", successCount,
                    "failedCount", failedCount,
                    "totalMetrics", totalMetrics
            )));
            collectionLogRepository.save(log);

            // 5. 更新任务统计
            updateTaskExecutionStatistics(task.getId(), successCount > 0, executionTime);

            // 6. 计算下次执行时间
            Timestamp nextExecutionTime = calculateNextExecutionTime(task.getScheduleType(), task.getScheduleConfig());
            if (nextExecutionTime != null) {
                updateNextExecutionTime(task.getId(), nextExecutionTime);
            }

            log.info("任务执行完成: taskId={}, successCount={}, failedCount={}, executionTime={}ms", 
                    task.getId(), successCount, failedCount, executionTime);

        } catch (Exception e) {
            log.error("任务执行异常: taskId={}", task.getId(), e);

            // 记录执行失败
            try {
                Optional<CollectionLog> logOpt = collectionLogRepository.findByExecutionId(executionId);
                if (logOpt.isPresent()) {
                    CollectionLog log = logOpt.get();
                    log.setEndTime(new Timestamp(System.currentTimeMillis()));
                    log.setStatus("FAILED");
                    log.setSuccess(false);
                    log.setErrorMessage(e.getMessage());
                    log.setErrorCode("EXECUTION_ERROR");
                    collectionLogRepository.save(log);
                }
            } catch (Exception logException) {
                log.error("记录执行失败日志异常: taskId={}", task.getId(), logException);
            }

            // 更新任务失败统计
            updateTaskExecutionStatistics(task.getId(), false, System.currentTimeMillis() - startTimeMillis);
        }
    }

    /**
     * 验证指标配置
     */
    private void validateMetricsConfig(List<com.skyeye.collector.dto.MetricConfig> metricsConfig) {
        if (metricsConfig == null || metricsConfig.isEmpty()) {
            throw new BusinessException("指标配置不能为空");
        }

        for (var config : metricsConfig) {
            if (config.getMetricName() == null || config.getMetricName().trim().isEmpty()) {
                throw new BusinessException("指标名称不能为空");
            }
            if (config.getMetricType() == null || config.getMetricType().trim().isEmpty()) {
                throw new BusinessException("指标类型不能为空");
            }
        }
    }

    /**
     * 计算下次执行时间
     */
    private Timestamp calculateNextExecutionTime(String scheduleType, Map<String, Object> scheduleConfig) {
        if (scheduleConfig == null) {
            return null;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextTime = null;

        if ("SIMPLE".equals(scheduleType)) {
            String frequency = (String) scheduleConfig.get("frequency");
            Integer interval = (Integer) scheduleConfig.get("interval");
            
            if (frequency != null && interval != null) {
                switch (frequency.toLowerCase()) {
                    case "minutes":
                        nextTime = now.plusMinutes(interval);
                        break;
                    case "hours":
                        nextTime = now.plusHours(interval);
                        break;
                    case "days":
                        nextTime = now.plusDays(interval);
                        break;
                    default:
                        nextTime = now.plusMinutes(interval);
                }
            }
        } else if ("CRON".equals(scheduleType)) {
            // 这里可以实现Cron表达式解析逻辑
            // 暂时返回1小时后
            nextTime = now.plusHours(1);
        }

        return nextTime != null ? Timestamp.valueOf(nextTime) : null;
    }

    /**
     * 更新任务统计记录
     */
    private void updateTaskStatisticsRecord(Long taskId, boolean success, long executionTime) {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        
        TaskStatistics statistics = taskStatisticsRepository.findByTaskIdAndStatDate(taskId, today)
                .orElse(new TaskStatistics());

        if (statistics.getId() == null) {
            statistics.setTaskId(taskId);
            statistics.setStatDate(today);
        }

        // 更新统计信息
        statistics.setExecutionCount(statistics.getExecutionCount() + 1);
        if (success) {
            statistics.setSuccessCount(statistics.getSuccessCount() + 1);
        } else {
            statistics.setFailureCount(statistics.getFailureCount() + 1);
        }

        // 更新执行时间统计
        long totalTime = statistics.getTotalExecutionTime() + executionTime;
        statistics.setTotalExecutionTime(totalTime);
        statistics.setAverageExecutionTime(totalTime / statistics.getExecutionCount());

        // 更新成功率
        if (statistics.getExecutionCount() > 0) {
            statistics.setSuccessRate((double) statistics.getSuccessCount() / statistics.getExecutionCount() * 100);
        }

        taskStatisticsRepository.save(statistics);
    }

    /**
     * 根据设备类型自动生成默认指标配置
     * 
     * @param devices 目标设备列表
     * @return 默认指标配置列表
     */
    private List<MetricConfig> generateDefaultMetricsConfig(List<Device> devices) {
        List<MetricConfig> metricsConfigs = new ArrayList<>();
        
        for (Device device : devices) {
            List<MetricConfig> deviceMetrics = generateMetricsForDevice(device);
            metricsConfigs.addAll(deviceMetrics);
        }
        
        return metricsConfigs;
    }

    /**
     * 为单个设备生成默认指标配置
     * 
     * @param device 设备信息
     * @return 该设备的默认指标配置列表
     */
    private List<MetricConfig> generateMetricsForDevice(Device device) {
        List<MetricConfig> metrics = new ArrayList<>();
        
        if (device.getDeviceType() == null) {
            log.warn("设备 {} 没有设备类型信息，跳过指标生成", device.getName());
            return metrics;
        }
        
        String deviceTypeName = device.getDeviceType().getName();
        String deviceTypeCode = device.getDeviceType().getCode();
        
        log.info("为设备 {} (类型: {}) 生成默认指标配置", device.getName(), deviceTypeName);
        
        // 根据设备类型生成对应的指标配置
        switch (deviceTypeCode.toLowerCase()) {
            case "camera":
            case "ipcamera":
                metrics.addAll(generateCameraMetrics(device));
                break;
            case "sensor":
            case "temperature_sensor":
            case "humidity_sensor":
                metrics.addAll(generateSensorMetrics(device));
                break;
            case "controller":
            case "access_controller":
                metrics.addAll(generateControllerMetrics(device));
                break;
            default:
                // 为未知类型的设备生成通用指标
                metrics.addAll(generateCommonMetrics(device));
                break;
        }
        
        log.info("为设备 {} 生成了 {} 个指标配置", device.getName(), metrics.size());
        return metrics;
    }

    /**
     * 生成摄像头设备的默认指标配置
     */
    private List<MetricConfig> generateCameraMetrics(Device device) {
        List<MetricConfig> metrics = new ArrayList<>();
        
        // 设备状态指标
        metrics.add(MetricConfig.builder()
                .metricName("device_status")
                .metricType("device_health")
                .displayName("设备状态")
                .description("设备在线状态和健康状态")
                .unit("boolean")
                .dataType(MetricConfig.MetricDataType.BOOLEAN)
                .interval(60)
                .priority(1)
                .enabled(true)
                .build());
        
        // CPU使用率
        metrics.add(MetricConfig.builder()
                .metricName("cpu_usage")
                .metricType("system_metrics")
                .displayName("CPU使用率")
                .description("设备CPU使用率")
                .unit("%")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(300) // 5分钟
                .priority(2)
                .enabled(true)
                .build());
        
        // 内存使用率
        metrics.add(MetricConfig.builder()
                .metricName("memory_usage")
                .metricType("system_metrics")
                .displayName("内存使用率")
                .description("设备内存使用率")
                .unit("%")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(300)
                .priority(2)
                .enabled(true)
                .build());
        
        // 存储使用率
        metrics.add(MetricConfig.builder()
                .metricName("storage_usage")
                .metricType("system_metrics")
                .displayName("存储使用率")
                .description("设备存储空间使用率")
                .unit("%")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(600) // 10分钟
                .priority(3)
                .enabled(true)
                .build());
        
        // 视频流状态
        metrics.add(MetricConfig.builder()
                .metricName("video_stream_status")
                .metricType("camera_metrics")
                .displayName("视频流状态")
                .description("视频流是否正常")
                .unit("boolean")
                .dataType(MetricConfig.MetricDataType.BOOLEAN)
                .interval(120) // 2分钟
                .priority(1)
                .enabled(true)
                .build());
        
        return metrics;
    }

    /**
     * 生成传感器设备的默认指标配置
     */
    private List<MetricConfig> generateSensorMetrics(Device device) {
        List<MetricConfig> metrics = new ArrayList<>();
        
        // 设备状态指标
        metrics.add(MetricConfig.builder()
                .metricName("device_status")
                .metricType("device_health")
                .displayName("设备状态")
                .description("设备在线状态和健康状态")
                .unit("boolean")
                .dataType(MetricConfig.MetricDataType.BOOLEAN)
                .interval(60)
                .priority(1)
                .enabled(true)
                .build());
        
        // 电池电量（如果是无线传感器）
        metrics.add(MetricConfig.builder()
                .metricName("battery_level")
                .metricType("device_metrics")
                .displayName("电池电量")
                .description("传感器电池电量")
                .unit("%")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(3600) // 1小时
                .priority(2)
                .enabled(true)
                .build());
        
        // 信号强度
        metrics.add(MetricConfig.builder()
                .metricName("signal_strength")
                .metricType("device_metrics")
                .displayName("信号强度")
                .description("设备信号强度")
                .unit("dBm")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(300)
                .priority(3)
                .enabled(true)
                .build());
        
        // 温度值（如果是温度传感器）
        metrics.add(MetricConfig.builder()
                .metricName("temperature")
                .metricType("sensor_data")
                .displayName("温度")
                .description("环境温度值")
                .unit("°C")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(60)
                .priority(1)
                .enabled(true)
                .build());
        
        // 湿度值（如果是湿度传感器）
        metrics.add(MetricConfig.builder()
                .metricName("humidity")
                .metricType("sensor_data")
                .displayName("湿度")
                .description("环境湿度值")
                .unit("%RH")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(60)
                .priority(1)
                .enabled(true)
                .build());
        
        return metrics;
    }

    /**
     * 生成控制器设备的默认指标配置
     */
    private List<MetricConfig> generateControllerMetrics(Device device) {
        List<MetricConfig> metrics = new ArrayList<>();
        
        // 设备状态指标
        metrics.add(MetricConfig.builder()
                .metricName("device_status")
                .metricType("device_health")
                .displayName("设备状态")
                .description("设备在线状态和健康状态")
                .unit("boolean")
                .dataType(MetricConfig.MetricDataType.BOOLEAN)
                .interval(60)
                .priority(1)
                .enabled(true)
                .build());
        
        // CPU使用率
        metrics.add(MetricConfig.builder()
                .metricName("cpu_usage")
                .metricType("system_metrics")
                .displayName("CPU使用率")
                .description("设备CPU使用率")
                .unit("%")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(300)
                .priority(2)
                .enabled(true)
                .build());
        
        // 内存使用率
        metrics.add(MetricConfig.builder()
                .metricName("memory_usage")
                .metricType("system_metrics")
                .displayName("内存使用率")
                .description("设备内存使用率")
                .unit("%")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(300)
                .priority(2)
                .enabled(true)
                .build());
        
        // 门禁事件计数（如果是门禁控制器）
        metrics.add(MetricConfig.builder()
                .metricName("access_events")
                .metricType("controller_metrics")
                .displayName("门禁事件数")
                .description("门禁访问事件计数")
                .unit("count")
                .dataType(MetricConfig.MetricDataType.COUNTER)
                .interval(60)
                .priority(1)
                .enabled(true)
                .build());
        
        return metrics;
    }

    /**
     * 生成通用设备的默认指标配置
     */
    private List<MetricConfig> generateCommonMetrics(Device device) {
        List<MetricConfig> metrics = new ArrayList<>();
        
        // 设备状态指标
        metrics.add(MetricConfig.builder()
                .metricName("device_status")
                .metricType("device_health")
                .displayName("设备状态")
                .description("设备在线状态和健康状态")
                .unit("boolean")
                .dataType(MetricConfig.MetricDataType.BOOLEAN)
                .interval(60)
                .priority(1)
                .enabled(true)
                .build());
        
        // 网络连接状态
        metrics.add(MetricConfig.builder()
                .metricName("network_status")
                .metricType("network_metrics")
                .displayName("网络状态")
                .description("设备网络连接状态")
                .unit("boolean")
                .dataType(MetricConfig.MetricDataType.BOOLEAN)
                .interval(120)
                .priority(2)
                .enabled(true)
                .build());
        
        // 响应时间
        metrics.add(MetricConfig.builder()
                .metricName("response_time")
                .metricType("network_metrics")
                .displayName("响应时间")
                .description("设备网络响应时间")
                .unit("ms")
                .dataType(MetricConfig.MetricDataType.GAUGE)
                .interval(300)
                .priority(3)
                .enabled(true)
                .build());
        
        return metrics;
    }
}
