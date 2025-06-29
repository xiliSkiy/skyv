package com.skyeye.scheduler.service.impl;

import com.skyeye.device.entity.Device;
import com.skyeye.device.service.DeviceService;
import com.skyeye.scheduler.dto.*;
import com.skyeye.scheduler.entity.*;
import com.skyeye.scheduler.repository.*;
import com.skyeye.scheduler.service.TaskScheduleTriggerService;
import com.skyeye.scheduler.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.criteria.Predicate;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 任务服务实现类
 */
@Service
@RequiredArgsConstructor
public class TaskServiceImpl implements TaskService {

    private final TaskRepository taskRepository;
    private final TaskDeviceRepository taskDeviceRepository;
    private final TaskMetricRepository taskMetricRepository;
    private final TaskScheduleRepository taskScheduleRepository;
    private final TaskExecutionRepository taskExecutionRepository;
    private final MetricTemplateRepository metricTemplateRepository;
    private final TaskDraftRepository taskDraftRepository;
    private final DeviceService deviceService;
    private final TaskScheduleTriggerService taskScheduleTriggerService;

    @Override
    public Page<TaskDTO> findTasksByPage(TaskQueryDTO queryDTO) {
        // 构建分页参数
        Pageable pageable;
        if (StringUtils.hasText(queryDTO.getSortBy())) {
            Sort.Direction direction = "asc".equalsIgnoreCase(queryDTO.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize(), Sort.by(direction, queryDTO.getSortBy()));
        } else {
            pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize(), Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        // 构建查询条件
        Specification<Task> specification = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            
            // 任务类型
            if (StringUtils.hasText(queryDTO.getTaskType())) {
                predicates.add(criteriaBuilder.equal(root.get("taskType"), queryDTO.getTaskType()));
            }
            
            // 任务状态
            if (StringUtils.hasText(queryDTO.getStatus())) {
                Integer statusCode = convertStatusToCode(queryDTO.getStatus());
                if (statusCode != null) {
                    predicates.add(criteriaBuilder.equal(root.get("status"), statusCode));
                }
            }
            
            // 优先级
            if (StringUtils.hasText(queryDTO.getPriority())) {
                Integer priorityCode = convertPriorityToCode(queryDTO.getPriority());
                if (priorityCode != null) {
                    predicates.add(criteriaBuilder.equal(root.get("priority"), priorityCode));
                }
            }
            
            // 标签
            if (StringUtils.hasText(queryDTO.getTag())) {
                predicates.add(criteriaBuilder.like(root.get("tags"), "%" + queryDTO.getTag() + "%"));
            }
            
            // 关键字（任务名称/ID/负责人）
            if (StringUtils.hasText(queryDTO.getKeyword())) {
                String keyword = "%" + queryDTO.getKeyword() + "%";
                predicates.add(
                    criteriaBuilder.or(
                        criteriaBuilder.like(root.get("taskName"), keyword),
                        criteriaBuilder.like(root.get("id").as(String.class), keyword)
                    )
                );
            }
            
            // 创建时间范围
            if (StringUtils.hasText(queryDTO.getStartDate())) {
                LocalDateTime startDateTime = LocalDate.parse(queryDTO.getStartDate()).atStartOfDay();
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("createdAt"), startDateTime));
            }
            
            if (StringUtils.hasText(queryDTO.getEndDate())) {
                LocalDateTime endDateTime = LocalDate.parse(queryDTO.getEndDate()).atTime(LocalTime.MAX);
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("createdAt"), endDateTime));
            }
            
            // 只查询未删除的记录
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 执行查询
        Page<Task> taskPage = taskRepository.findAll(specification, pageable);
        
        // 转换为DTO
        return taskPage.map(this::convertToTaskDTO);
    }

    @Override
    public TaskDTO findTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        return convertToTaskDTO(task);
    }

    @Override
    @Transactional
    public TaskDTO createTask(TaskCreateDTO createDTO) {
        // 创建任务实体
        Task task = new Task();
        task.setTaskName(createDTO.getTaskName());
        task.setTaskType(createDTO.getTaskType());
        task.setDescription(createDTO.getDescription());
        task.setPriority(convertPriorityToCode(createDTO.getPriority()));
        
        // 处理标签
        if (createDTO.getTags() != null && !createDTO.getTags().isEmpty()) {
            task.setTags(String.join(",", createDTO.getTags()));
        }
        
        // 设置默认状态为已调度
        task.setStatus(2); // 2-已调度
        
        // 保存任务
        final Task savedTask = taskRepository.save(task);
        
        // 保存任务设备关联
        if (createDTO.getDevices() != null && !createDTO.getDevices().isEmpty()) {
            List<TaskDevice> taskDevices = createDTO.getDevices().stream()
                    .map(deviceDTO -> {
                        TaskDevice taskDevice = new TaskDevice();
                        taskDevice.setTaskId(savedTask.getId());
                        taskDevice.setDeviceId(deviceDTO.getDeviceId());
                        taskDevice.setDeviceName(deviceDTO.getDeviceName());
                        taskDevice.setDeviceType(deviceDTO.getDeviceType());
                        return taskDevice;
                    })
                    .collect(Collectors.toList());
            
            taskDeviceRepository.saveAll(taskDevices);
        }
        
        // 保存任务指标关联
        if (createDTO.getMetrics() != null && !createDTO.getMetrics().isEmpty()) {
            List<TaskMetric> taskMetrics = createDTO.getMetrics().stream()
                    .map(metricDTO -> {
                        TaskMetric taskMetric = new TaskMetric();
                        taskMetric.setTaskId(savedTask.getId());
                        taskMetric.setMetricName(metricDTO.getMetricName());
                        taskMetric.setMetricType(metricDTO.getMetricType());
                        taskMetric.setMetricParams(metricDTO.getMetricParams());
                        taskMetric.setSortOrder(metricDTO.getSortOrder());
                        return taskMetric;
                    })
                    .collect(Collectors.toList());
            
            taskMetricRepository.saveAll(taskMetrics);
        }
        
        // 保存任务调度信息
        if (createDTO.getSchedule() != null) {
            TaskSchedule taskSchedule = new TaskSchedule();
            taskSchedule.setTaskId(savedTask.getId());
            taskSchedule.setScheduleType(createDTO.getSchedule().getScheduleType());
            taskSchedule.setCronExpression(createDTO.getSchedule().getCronExpression());
            taskSchedule.setStartTime(createDTO.getSchedule().getStartTime());
            taskSchedule.setEndTime(createDTO.getSchedule().getEndTime());
            taskSchedule.setIntervalValue(createDTO.getSchedule().getIntervalValue());
            taskSchedule.setIntervalUnit(createDTO.getSchedule().getIntervalUnit());
            taskSchedule.setTriggerType(createDTO.getSchedule().getTriggerType());
            taskSchedule.setTriggerEvent(createDTO.getSchedule().getTriggerEvent());
            taskSchedule.setMaxExecutions(createDTO.getSchedule().getMaxExecutions());
            taskSchedule.setTimeoutMinutes(createDTO.getSchedule().getTimeoutMinutes());
            taskSchedule.setRetryStrategy(createDTO.getSchedule().getRetryStrategy());
            taskSchedule.setMaxRetries(createDTO.getSchedule().getMaxRetries());
            
            taskScheduleRepository.save(taskSchedule);
        }
        
        return findTaskById(savedTask.getId());
    }

    @Override
    @Transactional
    public TaskDTO updateTask(Long id, TaskCreateDTO createDTO) {
        // 查询任务
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 更新任务基本信息
        task.setTaskName(createDTO.getTaskName());
        task.setTaskType(createDTO.getTaskType());
        task.setDescription(createDTO.getDescription());
        task.setPriority(convertPriorityToCode(createDTO.getPriority()));
        
        // 处理标签
        if (createDTO.getTags() != null && !createDTO.getTags().isEmpty()) {
            task.setTags(String.join(",", createDTO.getTags()));
        } else {
            task.setTags(null);
        }
        
        // 保存任务
        final Task savedTask = taskRepository.save(task);
        
        // 更新任务设备关联
        if (createDTO.getDevices() != null) {
            // 删除原有关联
            taskDeviceRepository.deleteByTaskId(id);
            
            // 保存新关联
            if (!createDTO.getDevices().isEmpty()) {
                List<TaskDevice> taskDevices = createDTO.getDevices().stream()
                        .map(deviceDTO -> {
                            TaskDevice taskDevice = new TaskDevice();
                            taskDevice.setTaskId(savedTask.getId());
                            taskDevice.setDeviceId(deviceDTO.getDeviceId());
                            taskDevice.setDeviceName(deviceDTO.getDeviceName());
                            taskDevice.setDeviceType(deviceDTO.getDeviceType());
                            return taskDevice;
                        })
                        .collect(Collectors.toList());
                
                taskDeviceRepository.saveAll(taskDevices);
            }
        }
        
        // 更新任务指标关联
        if (createDTO.getMetrics() != null) {
            // 删除原有关联
            taskMetricRepository.deleteByTaskId(id);
            
            // 保存新关联
            if (!createDTO.getMetrics().isEmpty()) {
                List<TaskMetric> taskMetrics = createDTO.getMetrics().stream()
                        .map(metricDTO -> {
                            TaskMetric taskMetric = new TaskMetric();
                            taskMetric.setTaskId(savedTask.getId());
                            taskMetric.setMetricName(metricDTO.getMetricName());
                            taskMetric.setMetricType(metricDTO.getMetricType());
                            taskMetric.setMetricParams(metricDTO.getMetricParams());
                            taskMetric.setSortOrder(metricDTO.getSortOrder());
                            return taskMetric;
                        })
                        .collect(Collectors.toList());
                
                taskMetricRepository.saveAll(taskMetrics);
            }
        }
        
        // 更新任务调度信息
        if (createDTO.getSchedule() != null) {
            // 查询原有调度信息
            TaskSchedule taskSchedule = taskScheduleRepository.findByTaskId(id)
                    .orElseGet(() -> {
                        TaskSchedule newSchedule = new TaskSchedule();
                        newSchedule.setTaskId(id);
                        return newSchedule;
                    });
            
            // 更新调度信息
            taskSchedule.setScheduleType(createDTO.getSchedule().getScheduleType());
            taskSchedule.setCronExpression(createDTO.getSchedule().getCronExpression());
            taskSchedule.setStartTime(createDTO.getSchedule().getStartTime());
            taskSchedule.setEndTime(createDTO.getSchedule().getEndTime());
            taskSchedule.setIntervalValue(createDTO.getSchedule().getIntervalValue());
            taskSchedule.setIntervalUnit(createDTO.getSchedule().getIntervalUnit());
            taskSchedule.setTriggerType(createDTO.getSchedule().getTriggerType());
            taskSchedule.setTriggerEvent(createDTO.getSchedule().getTriggerEvent());
            taskSchedule.setMaxExecutions(createDTO.getSchedule().getMaxExecutions());
            taskSchedule.setTimeoutMinutes(createDTO.getSchedule().getTimeoutMinutes());
            taskSchedule.setRetryStrategy(createDTO.getSchedule().getRetryStrategy());
            taskSchedule.setMaxRetries(createDTO.getSchedule().getMaxRetries());
            
            taskScheduleRepository.save(taskSchedule);
        }
        
        return findTaskById(id);
    }

    @Override
    @Transactional
    public void deleteTask(Long id) {
        // 查询任务
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 逻辑删除
        task.setDeletedAt(LocalDateTime.now());
        taskRepository.save(task);
    }

    @Override
    @Transactional
    public TaskDTO startTask(Long id) {
        // 查询任务
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 更新状态为运行中
        task.setStatus(1); // 1-运行中
        taskRepository.save(task);
        
        // 创建任务执行记录
        TaskExecution execution = new TaskExecution();
        execution.setTaskId(id);
        execution.setExecutionId(UUID.randomUUID().toString()); // 生成唯一的执行ID
        execution.setStartTime(LocalDateTime.now());
        execution.setStatus(1); // 1-运行中
        taskExecutionRepository.save(execution);
        
        return findTaskById(id);
    }

    @Override
    @Transactional
    public TaskDTO pauseTask(Long id) {
        // 查询任务
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 更新状态为已暂停
        task.setStatus(3); // 3-已暂停
        taskRepository.save(task);
        
        // 更新任务执行记录
        List<TaskExecution> executions = taskExecutionRepository.findByTaskIdAndStatus(id, 1); // 1-运行中
        if (!executions.isEmpty()) {
            TaskExecution execution = executions.get(0);
            execution.setStatus(3); // 3-已暂停
            taskExecutionRepository.save(execution);
        }
        
        return findTaskById(id);
    }

    @Override
    @Transactional
    public TaskDTO stopTask(Long id) {
        // 查询任务
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 更新状态为已调度
        task.setStatus(2); // 2-已调度
        taskRepository.save(task);
        
        // 更新任务执行记录
        List<TaskExecution> executions = taskExecutionRepository.findByTaskIdAndStatus(id, 1); // 1-运行中
        if (!executions.isEmpty()) {
            TaskExecution execution = executions.get(0);
            execution.setStatus(4); // 4-已完成
            execution.setEndTime(LocalDateTime.now());
            // 计算执行时长（毫秒）
            if (execution.getStartTime() != null && execution.getEndTime() != null) {
                long startMillis = execution.getStartTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                long endMillis = execution.getEndTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                execution.setExecutionTime(endMillis - startMillis);
            }
            taskExecutionRepository.save(execution);
        }
        
        return findTaskById(id);
    }

    @Override
    @Transactional
    public void batchTaskAction(String action, TaskBatchActionDTO batchActionDTO) {
        if (batchActionDTO.getIds() == null || batchActionDTO.getIds().isEmpty()) {
            throw new RuntimeException("任务ID列表不能为空");
        }
        
        switch (action) {
            case "start":
                batchActionDTO.getIds().forEach(this::startTask);
                break;
            case "pause":
                batchActionDTO.getIds().forEach(this::pauseTask);
                break;
            case "stop":
                batchActionDTO.getIds().forEach(this::stopTask);
                break;
            case "delete":
                batchActionDTO.getIds().forEach(this::deleteTask);
                break;
            default:
                throw new RuntimeException("不支持的操作类型");
        }
    }

    @Override
    public TaskStatsDTO getTaskStats() {
        TaskStatsDTO statsDTO = new TaskStatsDTO();
        
        // 查询各状态任务数量
        statsDTO.setTotal(taskRepository.countTotalTasks());
        statsDTO.setRunning(taskRepository.countRunningTasks());
        statsDTO.setScheduled(taskRepository.countScheduledTasks());
        statsDTO.setPaused(taskRepository.countPausedTasks());
        statsDTO.setCompleted(taskRepository.countCompletedTasks());
        statsDTO.setFailed(taskRepository.countFailedTasks());
        
        // 查询今日创建任务数量
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);
        statsDTO.setTodayCreated(taskRepository.countTodayCreatedTasks(startOfDay, endOfDay));
        
        // 查询今日执行任务数量
        // 使用查询今日执行记录数量来代替
        long todayExecutions = taskExecutionRepository.findByStartTimeBetween(startOfDay, endOfDay).size();
        statsDTO.setTodayExecuted(todayExecutions);
        
        return statsDTO;
    }

    @Override
    public Page<TaskDeviceDTO> getAvailableDevices(TaskQueryDTO queryDTO) {
        // 构建分页参数
        Pageable pageable;
        if (StringUtils.hasText(queryDTO.getSortBy())) {
            Sort.Direction direction = "asc".equalsIgnoreCase(queryDTO.getOrder()) ? Sort.Direction.ASC : Sort.Direction.DESC;
            pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize(), Sort.by(direction, queryDTO.getSortBy()));
        } else {
            pageable = PageRequest.of(queryDTO.getPageNum() - 1, queryDTO.getPageSize(), Sort.by(Sort.Direction.ASC, "id"));
        }
        
        // 从设备服务获取设备列表
        String deviceName = null;
        String deviceType = null;
        Integer status = null;
        Long groupId = null;
        
        // 解析查询参数
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            deviceName = queryDTO.getKeyword();
        }
        
        if (StringUtils.hasText(queryDTO.getTaskType())) {
            deviceType = queryDTO.getTaskType();
        }
        
        if (StringUtils.hasText(queryDTO.getStatus())) {
            if ("online".equalsIgnoreCase(queryDTO.getStatus())) {
                status = 1; // 在线
            } else if ("offline".equalsIgnoreCase(queryDTO.getStatus())) {
                status = 0; // 离线
            } else if ("maintenance".equalsIgnoreCase(queryDTO.getStatus())) {
                status = 2; // 维护中
            }
        }
        
        // 调用设备服务获取设备列表
        Page<Device> devicePage = deviceService.findByConditions(
                deviceName, deviceType, status, groupId, pageable);
        
        // 转换为TaskDeviceDTO
        return devicePage.map(device -> {
            TaskDeviceDTO dto = new TaskDeviceDTO();
            dto.setId(device.getId());
            dto.setDeviceId(device.getId());
            dto.setDeviceCode(device.getCode());
            dto.setDeviceName(device.getName());
            dto.setDeviceType(device.getType());
            dto.setLocation(device.getLocation());
            dto.setStatus(convertDeviceStatus(device.getStatus()));
            dto.setIpAddress(device.getIpAddress());
            return dto;
        });
    }
    
    /**
     * 转换设备状态
     */
    private String convertDeviceStatus(Integer status) {
        if (status == null) return "unknown";
        
        switch (status) {
            case 0: return "offline";
            case 1: return "online";
            case 2: return "maintenance";
            default: return "unknown";
        }
    }

    @Override
    public List<MetricTemplateDTO> getMetricTemplates() {
        List<MetricTemplate> templates = metricTemplateRepository.findAll();
        
        return templates.stream()
                .map(this::convertToMetricTemplateDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public TaskDraftDTO saveTaskDraft(TaskDraftDTO draftDTO) {
        // 查询草稿
        TaskDraft draft;
        if (StringUtils.hasText(draftDTO.getDraftId())) {
            draft = taskDraftRepository.findByDraftId(draftDTO.getDraftId())
                    .orElseGet(() -> {
                        TaskDraft newDraft = new TaskDraft();
                        newDraft.setDraftId(UUID.randomUUID().toString());
                        return newDraft;
                    });
        } else {
            draft = new TaskDraft();
            draft.setDraftId(UUID.randomUUID().toString());
        }
        
        // 更新草稿
        draft.setDraftData(draftDTO.getDraftData());
        draft.setStep(draftDTO.getStep());
        draft.setCreatedBy(draftDTO.getCreatedBy());
        
        // 保存草稿
        draft = taskDraftRepository.save(draft);
        
        return convertToTaskDraftDTO(draft);
    }

    @Override
    public TaskDraftDTO getTaskDraft(String draftId) {
        TaskDraft draft = taskDraftRepository.findByDraftId(draftId)
                .orElseThrow(() -> new RuntimeException("草稿不存在"));
        
        return convertToTaskDraftDTO(draft);
    }

    @Override
    @Transactional
    public void deleteTaskDraft(String draftId) {
        taskDraftRepository.deleteByDraftId(draftId);
    }

    /**
     * 将任务实体转换为DTO
     */
    private TaskDTO convertToTaskDTO(Task task) {
        TaskDTO dto = new TaskDTO();
        dto.setId(task.getId());
        dto.setTaskName(task.getTaskName());
        dto.setTaskType(task.getTaskType());
        dto.setDescription(task.getDescription());
        dto.setStatus(convertStatusToString(task.getStatus()));
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        dto.setPriority(convertPriorityToString(task.getPriority()));
        
        // 处理标签
        if (StringUtils.hasText(task.getTags())) {
            dto.setTags(Arrays.asList(task.getTags().split(",")));
        }
        
        // 查询设备数量
        Long deviceCount = taskDeviceRepository.countByTaskId(task.getId());
        dto.setDeviceCount(deviceCount.intValue());
        
        // 查询执行次数
        Long executionCount = taskExecutionRepository.countByTaskId(task.getId());
        dto.setExecutionCount(executionCount.intValue());
        
        // 查询设备列表
        List<TaskDevice> taskDevices = taskDeviceRepository.findByTaskId(task.getId());
        if (!taskDevices.isEmpty()) {
            dto.setDevices(taskDevices.stream()
                    .map(this::convertToTaskDeviceDTO)
                    .collect(Collectors.toList()));
        }
        
        // 查询指标列表
        List<TaskMetric> taskMetrics = taskMetricRepository.findByTaskId(task.getId());
        if (!taskMetrics.isEmpty()) {
            dto.setMetrics(taskMetrics.stream()
                    .map(this::convertToTaskMetricDTO)
                    .collect(Collectors.toList()));
        }
        
        // 查询调度信息
        taskScheduleRepository.findByTaskId(task.getId())
                .ifPresent(schedule -> dto.setSchedule(convertToTaskScheduleDTO(schedule)));
        
        // 查询最近一次执行记录
        taskExecutionRepository.findTopByTaskIdOrderByStartTimeDesc(task.getId())
                .ifPresent(execution -> {
                    dto.setStartTime(execution.getStartTime());
                    dto.setEndTime(execution.getEndTime());
                });
        
        // 查询下次执行时间
        taskScheduleRepository.findByTaskId(task.getId())
                .ifPresent(schedule -> {
                    // 这里应该根据调度信息计算下次执行时间
                    // 由于涉及复杂的cron表达式解析，这里简化处理
                    if (schedule.getStartTime() != null && schedule.getStartTime().isAfter(LocalDateTime.now())) {
                        dto.setNextExecutionTime(schedule.getStartTime());
                    }
                });
        
        return dto;
    }

    /**
     * 将任务设备实体转换为DTO
     */
    private TaskDeviceDTO convertToTaskDeviceDTO(TaskDevice taskDevice) {
        TaskDeviceDTO dto = new TaskDeviceDTO();
        dto.setId(taskDevice.getId());
        dto.setDeviceId(taskDevice.getDeviceId());
        dto.setDeviceName(taskDevice.getDeviceName());
        dto.setDeviceType(taskDevice.getDeviceType());
        return dto;
    }

    /**
     * 将任务指标实体转换为DTO
     */
    private TaskMetricDTO convertToTaskMetricDTO(TaskMetric taskMetric) {
        TaskMetricDTO dto = new TaskMetricDTO();
        dto.setId(taskMetric.getId());
        dto.setMetricName(taskMetric.getMetricName());
        dto.setMetricType(taskMetric.getMetricType());
        dto.setMetricParams(taskMetric.getMetricParams());
        dto.setSortOrder(taskMetric.getSortOrder());
        return dto;
    }

    /**
     * 将任务调度实体转换为DTO
     */
    private TaskScheduleDTO convertToTaskScheduleDTO(TaskSchedule taskSchedule) {
        TaskScheduleDTO dto = new TaskScheduleDTO();
        dto.setId(taskSchedule.getId());
        dto.setScheduleType(taskSchedule.getScheduleType());
        dto.setCronExpression(taskSchedule.getCronExpression());
        dto.setStartTime(taskSchedule.getStartTime());
        dto.setEndTime(taskSchedule.getEndTime());
        dto.setIntervalValue(taskSchedule.getIntervalValue());
        dto.setIntervalUnit(taskSchedule.getIntervalUnit());
        dto.setTriggerType(taskSchedule.getTriggerType());
        dto.setTriggerEvent(taskSchedule.getTriggerEvent());
        dto.setMaxExecutions(taskSchedule.getMaxExecutions());
        dto.setTimeoutMinutes(taskSchedule.getTimeoutMinutes());
        dto.setRetryStrategy(taskSchedule.getRetryStrategy());
        dto.setMaxRetries(taskSchedule.getMaxRetries());
        return dto;
    }

    /**
     * 将指标模板实体转换为DTO
     */
    private MetricTemplateDTO convertToMetricTemplateDTO(MetricTemplate template) {
        MetricTemplateDTO dto = new MetricTemplateDTO();
        dto.setId(template.getId());
        dto.setTemplateName(template.getTemplateName());
        dto.setCategory(template.getCategory());
        dto.setMetricType(template.getMetricType());
        dto.setDeviceType(template.getDeviceType());
        dto.setDefaultParams(template.getDefaultParams());
        dto.setDescription(template.getDescription());
        dto.setIsSystem(template.getIsSystem());
        return dto;
    }

    /**
     * 将任务草稿实体转换为DTO
     */
    private TaskDraftDTO convertToTaskDraftDTO(TaskDraft draft) {
        TaskDraftDTO dto = new TaskDraftDTO();
        dto.setDraftId(draft.getDraftId());
        dto.setDraftData(draft.getDraftData());
        dto.setStep(draft.getStep());
        dto.setCreatedAt(draft.getCreatedAt());
        dto.setUpdatedAt(draft.getUpdatedAt());
        dto.setCreatedBy(draft.getCreatedBy());
        return dto;
    }

    /**
     * 将状态码转换为状态字符串
     */
    private String convertStatusToString(Integer status) {
        if (status == null) {
            return "unknown";
        }
        
        switch (status) {
            case 1: return "running";
            case 2: return "scheduled";
            case 3: return "paused";
            case 4: return "completed";
            case 5: return "failed";
            default: return "unknown";
        }
    }

    /**
     * 将状态字符串转换为状态码
     */
    private Integer convertStatusToCode(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        
        switch (status.toLowerCase()) {
            case "running": return 1;
            case "scheduled": return 2;
            case "paused": return 3;
            case "completed": return 4;
            case "failed": return 5;
            default: return null;
        }
    }

    /**
     * 将优先级字符串转换为优先级码
     */
    private Integer convertPriorityToCode(String priority) {
        if (!StringUtils.hasText(priority)) {
            return 2; // 默认中优先级
        }
        
        switch (priority.toLowerCase()) {
            case "high": return 3;
            case "medium": return 2;
            case "low": return 1;
            default: return 2;
        }
    }

    /**
     * 将优先级码转换为优先级字符串
     */
    private String convertPriorityToString(Integer priority) {
        if (priority == null) {
            return "medium";
        }
        
        switch (priority) {
            case 3: return "high";
            case 2: return "medium";
            case 1: return "low";
            default: return "medium";
        }
    }

    @Override
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }
    
    @Override
    public Page<Task> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable);
    }
    
    @Override
    public List<TaskDevice> getTaskDevices(Long taskId) {
        return taskDeviceRepository.findByTaskId(taskId);
    }
    
    @Override
    public Task updateTask(Task task) {
        return taskRepository.save(task);
    }
    
    @Override
    public Task createTask(Task task) {
        return taskRepository.save(task);
    }
    
    @Override
    public TaskBatch createCollectionTask(Long taskId, Long collectorId, List<Long> deviceIds, List<Long> metricIds) {
        // 创建任务批次（示例实现）
        TaskBatch batch = new TaskBatch();
        batch.setTaskId(taskId);
        batch.setCollectorId(collectorId);
        batch.setBatchName("Batch-" + System.currentTimeMillis());
        batch.setStatus("CREATED");
        batch.setCreatedAt(LocalDateTime.now());
        
        // 实际项目中应该保存并返回
        return batch;
    }
    
    @Override
    public boolean submitCollectionTask(Long batchId) {
        // 实际项目中的提交逻辑
        return true;
    }
    
    @Override
    public boolean cancelCollectionTask(Long batchId) {
        // 实际项目中的取消逻辑
        return true;
    }
    
    @Override
    public Page<TaskBatch> findBatches(Long taskId, Pageable pageable) {
        // 实际项目中的查询逻辑
        return Page.empty(pageable);
    }
    
    @Override
    public Page<Task> findTasksByDevice(Long deviceId, Pageable pageable) {
        // 获取设备关联的任务ID
        List<Long> taskIds = taskDeviceRepository.findByDeviceId(deviceId).stream()
                .map(TaskDevice::getTaskId)
                .collect(Collectors.toList());
        
        if (taskIds.isEmpty()) {
            return Page.empty(pageable);
        }
        
        // 构建查询条件
        Specification<Task> spec = (root, query, cb) -> root.get("id").in(taskIds);
        return taskRepository.findAll(spec, pageable);
    }

    @Override
    public TaskScheduleTriggerDTO createTaskTrigger(Long taskId, TaskScheduleTriggerDTO triggerDTO) {
        triggerDTO.setTaskId(taskId);
        TaskScheduleTrigger trigger = taskScheduleTriggerService.createTrigger(triggerDTO);
        
        TaskScheduleTriggerDTO resultDTO = new TaskScheduleTriggerDTO();
        BeanUtils.copyProperties(trigger, resultDTO);
        return resultDTO;
    }
    
    @Override
    public TaskScheduleTriggerDTO updateTaskTrigger(Long triggerId, TaskScheduleTriggerDTO triggerDTO) {
        TaskScheduleTrigger trigger = taskScheduleTriggerService.updateTrigger(triggerId, triggerDTO);
        
        TaskScheduleTriggerDTO resultDTO = new TaskScheduleTriggerDTO();
        BeanUtils.copyProperties(trigger, resultDTO);
        return resultDTO;
    }
    
    @Override
    public boolean updateTriggerStatus(Long triggerId, boolean enabled) {
        return taskScheduleTriggerService.updateTriggerStatus(triggerId, enabled);
    }
    
    @Override
    public boolean deleteTaskTrigger(Long triggerId) {
        taskScheduleTriggerService.deleteTrigger(triggerId);
        return true;
    }
    
    @Override
    public List<TaskScheduleTriggerDTO> getTaskTriggers(Long taskId) {
        List<TaskScheduleTrigger> triggers = taskScheduleTriggerService.findByTaskId(taskId);
        
        return triggers.stream()
                .map(trigger -> {
                    TaskScheduleTriggerDTO dto = new TaskScheduleTriggerDTO();
                    BeanUtils.copyProperties(trigger, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<Long> getTaskDeviceIds(Long taskId) {
        List<TaskDevice> taskDevices = taskDeviceRepository.findByTaskId(taskId);
        return taskDevices.stream()
                .map(TaskDevice::getDeviceId)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> getTaskMetricIds(Long taskId) {
        List<TaskMetric> taskMetrics = taskMetricRepository.findByTaskId(taskId);
        return taskMetrics.stream()
                .map(TaskMetric::getMetricId)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<TaskDevice> addDevices(Long taskId, List<Long> deviceIds) {
        // 查询任务是否存在
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 查询设备信息
        List<Device> devices = deviceService.findAllById(deviceIds);
        
        // 构建任务设备关联
        List<TaskDevice> taskDevices = devices.stream()
                .map(device -> {
                    TaskDevice taskDevice = new TaskDevice();
                    taskDevice.setTaskId(taskId);
                    taskDevice.setDeviceId(device.getId());
                    taskDevice.setDeviceName(device.getName());
                    taskDevice.setDeviceType(device.getType());
                    return taskDevice;
                })
                .collect(Collectors.toList());
        
        // 保存关联
        return taskDeviceRepository.saveAll(taskDevices);
    }
    
    @Override
    public boolean removeDevice(Long taskId, Long deviceId) {
        // 查询任务是否存在
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 删除关联
        TaskDevice taskDevice = taskDeviceRepository.findByTaskIdAndDeviceId(taskId, deviceId);
        if (taskDevice != null) {
            taskDeviceRepository.delete(taskDevice);
        }
        return true;
    }
    
    @Override
    public boolean resumeTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 只有已暂停的任务可以恢复
        if (task.getStatus() != 3) {
            throw new RuntimeException("当前任务状态不允许恢复");
        }
        
        task.setStatus(1); // 运行中
        taskRepository.save(task);
        
        // 更新当前执行记录
        taskExecutionRepository.findTopByTaskIdOrderByStartTimeDesc(id)
                .ifPresent(execution -> {
                    if (execution.getStatus() == 3) {
                        execution.setStatus(1); // 运行中
                        taskExecutionRepository.save(execution);
                    }
                });
        
        return true;
    }
    
    @Override
    public boolean triggerTask(Long taskId, Long triggerId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 检查任务状态是否允许触发
        if (task.getStatus() != 2 && task.getStatus() != 3) {
            throw new RuntimeException("当前任务状态不允许触发");
        }
        
        // 如果提供了触发器ID，则使用特定触发器
        if (triggerId != null) {
            return taskScheduleTriggerService.fireTrigger(triggerId);
        } else {
            // 直接触发任务
            task.setStatus(1); // 运行中
            taskRepository.save(task);
            
            // 记录任务执行记录
            TaskExecution execution = new TaskExecution();
            execution.setTaskId(taskId);
            execution.setExecutionId(UUID.randomUUID().toString());
            execution.setStartTime(LocalDateTime.now());
            execution.setStatus(1); // 运行中
            taskExecutionRepository.save(execution);
            
            return true;
        }
    }

    @Override
    @Transactional
    public Long saveBasicInfo(TaskBasicInfoDTO taskBasicInfo) {
        Task task;
        
        // 如果有ID，则更新，否则创建新任务
        if (taskBasicInfo.getId() != null) {
            task = taskRepository.findById(taskBasicInfo.getId())
                    .orElseThrow(() -> new RuntimeException("任务不存在"));
        } else {
            task = new Task();
            task.setStatus(1); // 1-草稿状态
            task.setCreatedAt(LocalDateTime.now());
        }
        
        // 设置任务基本信息
        task.setTaskName(taskBasicInfo.getName());
        task.setTaskType(taskBasicInfo.getType());
        task.setDescription(taskBasicInfo.getDescription());
        task.setPriority(taskBasicInfo.getPriority());
        
        // 处理标签
        if (taskBasicInfo.getTags() != null && !taskBasicInfo.getTags().isEmpty()) {
            task.setTags(String.join(",", taskBasicInfo.getTags()));
        }
        
        // 设置报警相关信息
        task.setEnableAlert(taskBasicInfo.getEnableAlert());
        task.setAlertLevel(taskBasicInfo.getAlertLevel());
        
        if (taskBasicInfo.getAlertNotifyTypes() != null && !taskBasicInfo.getAlertNotifyTypes().isEmpty()) {
            task.setAlertNotifyTypes(String.join(",", taskBasicInfo.getAlertNotifyTypes()));
        }
        
        if (taskBasicInfo.getAlertReceivers() != null && !taskBasicInfo.getAlertReceivers().isEmpty()) {
            task.setAlertReceivers(taskBasicInfo.getAlertReceivers().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(",")));
        }
        
        // 更新时间
        task.setUpdatedAt(LocalDateTime.now());
        
        // 保存任务
        Task savedTask = taskRepository.save(task);
        return savedTask.getId();
    }

    @Override
    public List<DeviceTypeTreeDTO> getDeviceTypeTree() {
        // 从设备服务获取设备类型树
        List<Map<String, Object>> deviceTypes = deviceService.getDeviceTypeTree();
        
        // 将结果转换为DTO
        return deviceTypes.stream()
                .map(this::convertToDeviceTypeTreeDTO)
                .collect(Collectors.toList());
    }
    
    private DeviceTypeTreeDTO convertToDeviceTypeTreeDTO(Map<String, Object> typeMap) {
        DeviceTypeTreeDTO dto = new DeviceTypeTreeDTO();
        dto.setId(String.valueOf(typeMap.get("id")));
        dto.setName(String.valueOf(typeMap.get("name")));
        dto.setIcon(String.valueOf(typeMap.get("icon")));
        dto.setDeviceCount((Integer) typeMap.getOrDefault("deviceCount", 0));
        
        // 处理子类型
        if (typeMap.containsKey("children")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) typeMap.get("children");
            if (children != null && !children.isEmpty()) {
                dto.setChildren(children.stream()
                        .map(this::convertToDeviceTypeTreeDTO)
                        .collect(Collectors.toList()));
            }
        }
        
        return dto;
    }

    @Override
    public Page<TaskDeviceDTO> getDevicesByType(String typeId, TaskDeviceQueryDTO queryDTO) {
        // 构建分页参数
        Pageable pageable = createPageable(queryDTO);
        
        // 调用设备服务获取指定类型的设备
        Page<Device> devicePage = deviceService.findByType(typeId, queryDTO.getKeyword(), 
                queryDTO.getStatus(), pageable);
        
        // 转换为DTO
        return devicePage.map(this::convertToTaskDeviceDTO);
    }
    
    private TaskDeviceDTO convertToTaskDeviceDTO(Device device) {
        TaskDeviceDTO dto = new TaskDeviceDTO();
        dto.setDeviceId(device.getId());
        dto.setDeviceName(device.getDeviceName());
        dto.setDeviceType(device.getDeviceType());
        dto.setDeviceModel(device.getDeviceModel());
        dto.setIpAddress(device.getIpAddress());
        dto.setStatus(convertDeviceStatus(device.getStatus()));
        dto.setLocation(device.getLocation());
        dto.setLastOnlineTime(device.getLastOnlineTime());
        
        // 设置标签
        if (StringUtils.hasText(device.getTags())) {
            dto.setTags(Arrays.asList(device.getTags().split(",")));
        }
        
        return dto;
    }

    @Override
    public List<AreaDTO> getAreaList() {
        // 从设备服务获取区域列表
        List<Map<String, Object>> areas = deviceService.getAreaList();
        
        // 将结果转换为DTO
        return areas.stream()
                .map(this::convertToAreaDTO)
                .collect(Collectors.toList());
    }
    
    private AreaDTO convertToAreaDTO(Map<String, Object> areaMap) {
        AreaDTO dto = new AreaDTO();
        dto.setId(String.valueOf(areaMap.get("id")));
        dto.setName(String.valueOf(areaMap.get("name")));
        dto.setLevel((Integer) areaMap.getOrDefault("level", 0));
        dto.setParentId(String.valueOf(areaMap.getOrDefault("parentId", "")));
        dto.setLongitude((Double) areaMap.getOrDefault("longitude", 0.0));
        dto.setLatitude((Double) areaMap.getOrDefault("latitude", 0.0));
        dto.setDeviceCount((Integer) areaMap.getOrDefault("deviceCount", 0));
        
        // 处理子区域
        if (areaMap.containsKey("children")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) areaMap.get("children");
            if (children != null && !children.isEmpty()) {
                dto.setChildren(children.stream()
                        .map(this::convertToAreaDTO)
                        .collect(Collectors.toList()));
            }
        }
        
        // 处理边界点
        if (areaMap.containsKey("boundary")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> boundary = (List<Map<String, Object>>) areaMap.get("boundary");
            if (boundary != null && !boundary.isEmpty()) {
                dto.setBoundary(boundary.stream()
                        .map(point -> {
                            AreaPointDTO pointDTO = new AreaPointDTO();
                            pointDTO.setLongitude((Double) point.getOrDefault("longitude", 0.0));
                            pointDTO.setLatitude((Double) point.getOrDefault("latitude", 0.0));
                            return pointDTO;
                        })
                        .collect(Collectors.toList()));
            }
        }
        
        return dto;
    }

    @Override
    public Page<TaskDeviceDTO> getDevicesByArea(String areaId, TaskDeviceQueryDTO queryDTO) {
        // 构建分页参数
        Pageable pageable = createPageable(queryDTO);
        
        // 调用设备服务获取指定区域的设备
        Page<Device> devicePage = deviceService.findByArea(areaId, queryDTO.getKeyword(), 
                queryDTO.getStatus(), pageable);
        
        // 转换为DTO
        return devicePage.map(this::convertToTaskDeviceDTO);
    }

    @Override
    public List<String> getAllTags() {
        // 从设备服务获取所有标签
        return deviceService.getAllTags();
    }

    @Override
    public Page<TaskDeviceDTO> getDevicesByTags(List<String> tags, String tagOperator, TaskDeviceQueryDTO queryDTO) {
        // 构建分页参数
        Pageable pageable = createPageable(queryDTO);
        
        // 调用设备服务获取指定标签的设备
        Page<Device> devicePage = deviceService.findByTags(tags, tagOperator, 
                queryDTO.getKeyword(), queryDTO.getStatus(), pageable);
        
        // 转换为DTO
        return devicePage.map(this::convertToTaskDeviceDTO);
    }
    
    private Pageable createPageable(TaskDeviceQueryDTO queryDTO) {
        // 构建分页和排序参数
        Sort sort;
        if (StringUtils.hasText(queryDTO.getSortBy())) {
            Sort.Direction direction = "asc".equalsIgnoreCase(queryDTO.getSortDirection()) ? 
                    Sort.Direction.ASC : Sort.Direction.DESC;
            sort = Sort.by(direction, queryDTO.getSortBy());
        } else {
            sort = Sort.by(Sort.Direction.DESC, "createdAt");
        }
        
        return PageRequest.of(queryDTO.getPage() - 1, queryDTO.getSize(), sort);
    }

    @Override
    @Transactional
    public boolean saveTaskDeviceSelection(Long taskId, List<Long> deviceIds) {
        // 验证任务是否存在
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 先删除原有的设备关联
        taskDeviceRepository.deleteByTaskId(taskId);
        
        if (deviceIds != null && !deviceIds.isEmpty()) {
            // 获取设备信息
            List<Device> devices = deviceService.findByIds(deviceIds);
            
            // 创建设备关联
            List<TaskDevice> taskDevices = devices.stream()
                    .map(device -> {
                        TaskDevice taskDevice = new TaskDevice();
                        taskDevice.setTaskId(taskId);
                        taskDevice.setDeviceId(device.getId());
                        taskDevice.setDeviceName(device.getDeviceName());
                        taskDevice.setDeviceType(device.getDeviceType());
                        taskDevice.setStatus(1); // 1-正常
                        taskDevice.setCreatedAt(LocalDateTime.now());
                        return taskDevice;
                    })
                    .collect(Collectors.toList());
            
            // 批量保存
            taskDeviceRepository.saveAll(taskDevices);
        }
        
        // 更新任务状态为进行中（设备已选择）
        task.setStatus(2); // 2-进行中
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        return true;
    }

    @Override
    public List<TaskDeviceDTO> getSelectedDevices(Long taskId) {
        // 获取任务已选设备
        List<TaskDevice> taskDevices = taskDeviceRepository.findByTaskId(taskId);
        
        // 转换为DTO
        return taskDevices.stream()
                .map(this::convertToTaskDeviceDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Map<String, Integer> getDeviceTypeStats(List<Long> deviceIds) {
        // 调用设备服务获取设备类型统计
        return deviceService.getDeviceTypeStats(deviceIds);
    }

    @Override
    public Map<String, Integer> getDeviceStatusStats(List<Long> deviceIds) {
        // 调用设备服务获取设备状态统计
        return deviceService.getDeviceStatusStats(deviceIds);
    }

    @Override
    public List<MetricCategoryDTO> getMetricCategories() {
        // 从指标服务获取指标分类
        List<Map<String, Object>> categories = deviceService.getMetricCategories();
        
        // 转换为DTO
        return categories.stream()
                .map(this::convertToMetricCategoryDTO)
                .collect(Collectors.toList());
    }
    
    private MetricCategoryDTO convertToMetricCategoryDTO(Map<String, Object> categoryMap) {
        MetricCategoryDTO dto = new MetricCategoryDTO();
        dto.setId(String.valueOf(categoryMap.get("id")));
        dto.setName(String.valueOf(categoryMap.get("name")));
        dto.setDescription(String.valueOf(categoryMap.getOrDefault("description", "")));
        dto.setIcon(String.valueOf(categoryMap.getOrDefault("icon", "")));
        dto.setMetricCount((Integer) categoryMap.getOrDefault("metricCount", 0));
        
        // 处理子分类
        if (categoryMap.containsKey("children")) {
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> children = (List<Map<String, Object>>) categoryMap.get("children");
            if (children != null && !children.isEmpty()) {
                dto.setChildren(children.stream()
                        .map(this::convertToMetricCategoryDTO)
                        .collect(Collectors.toList()));
            }
        }
        
        return dto;
    }

    @Override
    public List<MetricDTO> getMetricsByCategory(String categoryId) {
        // 从指标服务获取指定分类的指标
        List<Map<String, Object>> metrics = deviceService.getMetricsByCategory(categoryId);
        
        // 转换为DTO
        return metrics.stream()
                .map(this::convertToMetricDTO)
                .collect(Collectors.toList());
    }
    
    private MetricDTO convertToMetricDTO(Map<String, Object> metricMap) {
        MetricDTO dto = new MetricDTO();
        dto.setId(Long.valueOf(String.valueOf(metricMap.get("id"))));
        dto.setName(String.valueOf(metricMap.get("name")));
        dto.setCode(String.valueOf(metricMap.get("code")));
        dto.setDescription(String.valueOf(metricMap.getOrDefault("description", "")));
        dto.setUnit(String.valueOf(metricMap.getOrDefault("unit", "")));
        dto.setDataType(String.valueOf(metricMap.getOrDefault("dataType", "")));
        dto.setCollectionMethod(String.valueOf(metricMap.getOrDefault("collectionMethod", "")));
        dto.setCollectionInterval((Integer) metricMap.getOrDefault("collectionInterval", 60));
        dto.setCategoryId(String.valueOf(metricMap.getOrDefault("categoryId", "")));
        dto.setCategoryName(String.valueOf(metricMap.getOrDefault("categoryName", "")));
        dto.setIsSystem((Boolean) metricMap.getOrDefault("isSystem", false));
        
        // 处理标签
        if (metricMap.containsKey("tags")) {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) metricMap.get("tags");
            dto.setTags(tags);
        }
        
        // 处理支持的设备类型
        if (metricMap.containsKey("supportedDeviceTypes")) {
            @SuppressWarnings("unchecked")
            List<String> supportedDeviceTypes = (List<String>) metricMap.get("supportedDeviceTypes");
            dto.setSupportedDeviceTypes(supportedDeviceTypes);
        }
        
        return dto;
    }

    @Override
    public List<MetricDTO> searchMetrics(String keyword) {
        // 从指标服务搜索指标
        List<Map<String, Object>> metrics = deviceService.searchMetrics(keyword);
        
        // 转换为DTO
        return metrics.stream()
                .map(this::convertToMetricDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean saveTaskMetrics(Long taskId, List<Long> metricIds) {
        // 验证任务是否存在
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 先删除原有的指标关联
        taskMetricRepository.deleteByTaskId(taskId);
        
        if (metricIds != null && !metricIds.isEmpty()) {
            // 获取指标信息
            List<Map<String, Object>> metrics = deviceService.getMetricsByIds(metricIds);
            
            // 创建指标关联
            List<TaskMetric> taskMetrics = metrics.stream()
                    .map(metric -> {
                        TaskMetric taskMetric = new TaskMetric();
                        taskMetric.setTaskId(taskId);
                        taskMetric.setMetricId(Long.valueOf(String.valueOf(metric.get("id"))));
                        taskMetric.setMetricName(String.valueOf(metric.get("name")));
                        taskMetric.setMetricType(String.valueOf(metric.getOrDefault("dataType", "")));
                        taskMetric.setMetricParams("{}"); // 默认空参数
                        taskMetric.setCreatedAt(LocalDateTime.now());
                        return taskMetric;
                    })
                    .collect(Collectors.toList());
            
            // 批量保存
            taskMetricRepository.saveAll(taskMetrics);
        }
        
        // 更新任务状态为进行中（指标已配置）
        task.setStatus(3); // 3-指标已配置
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        return true;
    }

    @Override
    public List<MetricDTO> getSelectedMetrics(Long taskId) {
        // 获取任务已选指标
        List<TaskMetric> taskMetrics = taskMetricRepository.findByTaskId(taskId);
        
        if (taskMetrics.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 获取指标详细信息
        List<Long> metricIds = taskMetrics.stream()
                .map(TaskMetric::getMetricId)
                .collect(Collectors.toList());
        
        List<Map<String, Object>> metrics = deviceService.getMetricsByIds(metricIds);
        
        // 转换为DTO
        return metrics.stream()
                .map(this::convertToMetricDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean saveTaskSchedule(Long taskId, TaskScheduleDTO scheduleDTO) {
        // 验证任务是否存在
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 查找现有调度设置
        TaskSchedule taskSchedule = taskScheduleRepository.findByTaskId(taskId)
                .orElse(new TaskSchedule());
        
        // 设置调度信息
        taskSchedule.setTaskId(taskId);
        taskSchedule.setScheduleType(scheduleDTO.getScheduleType());
        taskSchedule.setCronExpression(scheduleDTO.getCronExpression());
        taskSchedule.setStartTime(scheduleDTO.getStartTime());
        taskSchedule.setEndTime(scheduleDTO.getEndTime());
        taskSchedule.setFrequency(scheduleDTO.getFrequency());
        // 处理调度时间，确保类型匹配
        if (scheduleDTO.getScheduleTime() != null) {
            taskSchedule.setScheduleTime(LocalDateTime.parse(scheduleDTO.getScheduleTime()));
        }
        taskSchedule.setWeekdays(scheduleDTO.getWeekdays() != null ? 
                scheduleDTO.getWeekdays().stream().map(String::valueOf).collect(Collectors.joining(",")) : null);
        taskSchedule.setIntervalValue(scheduleDTO.getIntervalValue());
        taskSchedule.setIntervalUnit(scheduleDTO.getIntervalUnit());
        taskSchedule.setTriggerType(scheduleDTO.getTriggerType());
        taskSchedule.setTriggerEvent(scheduleDTO.getTriggerEvent());
        taskSchedule.setMaxExecutions(scheduleDTO.getMaxExecutions());
        taskSchedule.setTimeoutMinutes(scheduleDTO.getTimeoutMinutes());
        taskSchedule.setRetryStrategy(scheduleDTO.getRetryStrategy());
        taskSchedule.setMaxRetries(scheduleDTO.getMaxRetries());
        
        if (taskSchedule.getId() == null) {
            taskSchedule.setCreatedAt(LocalDateTime.now());
        }
        taskSchedule.setUpdatedAt(LocalDateTime.now());
        
        // 保存调度设置
        taskScheduleRepository.save(taskSchedule);
        
        // 更新任务状态为待执行
        task.setStatus(4); // 4-待执行
        task.setUpdatedAt(LocalDateTime.now());
        taskRepository.save(task);
        
        return true;
    }

    @Override
    public TaskScheduleDTO getTaskSchedule(Long taskId) {
        // 查找任务调度设置
        return taskScheduleRepository.findByTaskId(taskId)
                .map(this::convertToTaskScheduleDTO)
                .orElse(new TaskScheduleDTO());
    }

    @Override
    @Transactional
    public TaskDTO completeTaskCreation(Long taskId) {
        // 验证任务是否存在
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 验证任务是否已完成所有配置
        List<TaskDevice> devices = taskDeviceRepository.findByTaskId(taskId);
        if (devices.isEmpty()) {
            throw new RuntimeException("任务未配置设备");
        }
        
        List<TaskMetric> metrics = taskMetricRepository.findByTaskId(taskId);
        if (metrics.isEmpty()) {
            throw new RuntimeException("任务未配置指标");
        }
        
        Optional<TaskSchedule> schedule = taskScheduleRepository.findByTaskId(taskId);
        if (!schedule.isPresent()) {
            throw new RuntimeException("任务未配置调度计划");
        }
        
        // 更新任务状态为已创建
        task.setStatus(5); // 5-已创建
        task.setUpdatedAt(LocalDateTime.now());
        Task updatedTask = taskRepository.save(task);
        
        // 根据调度设置创建任务触发器
        TaskSchedule taskSchedule = schedule.get();
        if ("realtime".equals(taskSchedule.getScheduleType())) {
            // 实时执行，立即触发
            triggerTask(taskId, null);
        } else {
            // 创建触发器
            TaskScheduleTriggerDTO triggerDTO = new TaskScheduleTriggerDTO();
            triggerDTO.setTaskId(taskId);
            // 根据调度类型设置触发器类型
            Integer triggerTypeCode = convertScheduleTypeToTriggerType(taskSchedule.getScheduleType());
            triggerDTO.setTriggerType(triggerTypeCode);
            triggerDTO.setCronExpression(taskSchedule.getCronExpression());
            triggerDTO.setStartTime(taskSchedule.getStartTime());
            triggerDTO.setEndTime(taskSchedule.getEndTime());
            triggerDTO.setEnabled(true);
            
            taskScheduleTriggerService.createTrigger(triggerDTO);
        }
        
        return convertToTaskDTO(updatedTask);
    }
    
    /**
     * 将调度类型转换为触发器类型代码
     * @param scheduleType 调度类型
     * @return 触发器类型代码
     */
    private Integer convertScheduleTypeToTriggerType(String scheduleType) {
        switch (scheduleType) {
            case "scheduled":
                return 1; // cron类型
            case "periodic":
                return 2; // simple类型
            case "daily":
                return 3; // daily类型
            case "triggered":
                return 4; // event类型
            default:
                return 1; // 默认为cron类型
        }
    }
} 