package com.skyeye.scheduler.service.impl;

import com.cronutils.model.Cron;
import com.cronutils.model.CronType;
import com.cronutils.model.definition.CronDefinitionBuilder;
import com.cronutils.model.time.ExecutionTime;
import com.cronutils.parser.CronParser;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.response.PageResult;
import com.skyeye.scheduler.dto.CollectionTaskDTO;
import com.skyeye.scheduler.dto.TaskScheduleTriggerDTO;
import com.skyeye.scheduler.entity.Task;
import com.skyeye.scheduler.entity.TaskBatch;
import com.skyeye.scheduler.entity.TaskDevice;
import com.skyeye.scheduler.entity.TaskMetric;
import com.skyeye.scheduler.entity.TaskScheduleTrigger;
import com.skyeye.scheduler.repository.TaskRepository;
import com.skyeye.scheduler.repository.TaskScheduleTriggerRepository;
import com.skyeye.scheduler.service.CollectionTaskService;
import com.skyeye.scheduler.service.TaskScheduleTriggerService;
import com.skyeye.scheduler.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.ArrayList;

/**
 * 任务调度触发器服务实现类
 */
@Slf4j
@Service
public class TaskScheduleTriggerServiceImpl implements TaskScheduleTriggerService {

    private final TaskScheduleTriggerRepository triggerRepository;
    private final TaskRepository taskRepository;
    private final TaskService taskService;
    private final CollectionTaskService collectionTaskService;

    @Autowired
    public TaskScheduleTriggerServiceImpl(
            TaskScheduleTriggerRepository triggerRepository,
            TaskRepository taskRepository,
            @Lazy TaskService taskService,
            CollectionTaskService collectionTaskService) {
        this.triggerRepository = triggerRepository;
        this.taskRepository = taskRepository;
        this.taskService = taskService;
        this.collectionTaskService = collectionTaskService;
    }

    @Override
    @Transactional
    public TaskScheduleTrigger createTrigger(TaskScheduleTriggerDTO triggerDTO) {
        // 检查任务是否存在
        Task task = taskRepository.findById(triggerDTO.getTaskId())
                .orElseThrow(() -> new RuntimeException("任务不存在"));

        // 创建触发器
        TaskScheduleTrigger trigger = new TaskScheduleTrigger();
        BeanUtils.copyProperties(triggerDTO, trigger);
        
        // 设置创建时间和默认值
        trigger.setCreatedAt(LocalDateTime.now());
        trigger.setUpdatedAt(LocalDateTime.now());
        
        if (trigger.getEnabled() == null) {
            trigger.setEnabled(true);
        }
        
        // 计算下次执行时间
        if (trigger.getTriggerType() == 1 && trigger.getCronExpression() != null) {
            trigger.setNextFireTime(calculateNextFireTime(trigger));
        }
        
        return triggerRepository.save(trigger);
    }

    @Override
    @Transactional
    public TaskScheduleTrigger updateTrigger(Long triggerId, TaskScheduleTriggerDTO triggerDTO) {
        // 查询触发器
        TaskScheduleTrigger trigger = triggerRepository.findById(triggerId)
                .orElseThrow(() -> new RuntimeException("触发器不存在"));
        
        // 更新属性
        if (triggerDTO.getTriggerName() != null) {
            trigger.setTriggerName(triggerDTO.getTriggerName());
        }
        
        if (triggerDTO.getTriggerType() != null) {
            trigger.setTriggerType(triggerDTO.getTriggerType());
        }
        
        if (triggerDTO.getCronExpression() != null) {
            trigger.setCronExpression(triggerDTO.getCronExpression());
        }
        
        if (triggerDTO.getStartTime() != null) {
            trigger.setStartTime(triggerDTO.getStartTime());
        }
        
        if (triggerDTO.getEndTime() != null) {
            trigger.setEndTime(triggerDTO.getEndTime());
        }
        
        if (triggerDTO.getRepeatCount() != null) {
            trigger.setRepeatCount(triggerDTO.getRepeatCount());
        }
        
        if (triggerDTO.getRepeatInterval() != null) {
            trigger.setRepeatInterval(triggerDTO.getRepeatInterval());
        }
        
        if (triggerDTO.getEventCondition() != null) {
            trigger.setEventCondition(triggerDTO.getEventCondition());
        }
        
        if (triggerDTO.getPriority() != null) {
            trigger.setPriority(triggerDTO.getPriority());
        }
        
        trigger.setUpdatedAt(LocalDateTime.now());
        
        // 重新计算下次执行时间
        if (trigger.getTriggerType() == 1 && trigger.getCronExpression() != null) {
            trigger.setNextFireTime(calculateNextFireTime(trigger));
        }
        
        return triggerRepository.save(trigger);
    }

    @Override
    @Transactional
    public void deleteTrigger(Long triggerId) {
        triggerRepository.deleteById(triggerId);
    }

    @Override
    @Transactional
    public boolean updateTriggerStatus(Long triggerId, boolean enabled) {
        TaskScheduleTrigger trigger = triggerRepository.findById(triggerId)
                .orElseThrow(() -> new RuntimeException("触发器不存在"));
        
        trigger.setEnabled(enabled);
        trigger.setUpdatedAt(LocalDateTime.now());
        
        // 如果启用触发器，需要计算下次执行时间
        if (enabled && trigger.getTriggerType() == 1 && trigger.getCronExpression() != null) {
            trigger.setNextFireTime(calculateNextFireTime(trigger));
        }
        
        triggerRepository.save(trigger);
        return true;
    }

    @Override
    public TaskScheduleTrigger findById(Long triggerId) {
        return triggerRepository.findById(triggerId)
                .orElseThrow(() -> new RuntimeException("触发器不存在"));
    }

    @Override
    public List<TaskScheduleTrigger> findByTaskId(Long taskId) {
        return triggerRepository.findByTaskId(taskId);
    }

    @Override
    public List<TaskScheduleTrigger> findByEventType(String eventType) {
        return triggerRepository.findByEventConditionContaining(eventType);
    }

    @Override
    public LocalDateTime calculateNextFireTime(TaskScheduleTrigger trigger) {
        if (trigger.getTriggerType() != 1 || trigger.getCronExpression() == null) {
            return null;
        }
        
        try {
            // 解析cron表达式
            CronParser parser = new CronParser(CronDefinitionBuilder.instanceDefinitionFor(CronType.SPRING));
            Cron cron = parser.parse(trigger.getCronExpression());
            ExecutionTime executionTime = ExecutionTime.forCron(cron);
            
            // 计算下次执行时间
            ZonedDateTime now = LocalDateTime.now().atZone(ZoneId.systemDefault());
            Optional<ZonedDateTime> nextExecution = executionTime.nextExecution(now);
            
            return nextExecution.map(ZonedDateTime::toLocalDateTime).orElse(null);
        } catch (Exception e) {
            log.error("计算下次执行时间失败: {}", e.getMessage());
            return null;
        }
    }

    @Override
    @Transactional
    public boolean fireTrigger(Long triggerId) {
        // 查找触发器
        TaskScheduleTrigger trigger = triggerRepository.findById(triggerId)
                .orElseThrow(() -> new RuntimeException("触发器不存在"));
        
        // 更新触发器状态
        trigger.setLastFireTime(LocalDateTime.now());
        trigger.setFiredCount(trigger.getFiredCount() + 1);
        trigger.setNextFireTime(calculateNextFireTime(trigger));
        triggerRepository.save(trigger);
        
        // 获取任务信息
        Long taskId = trigger.getTaskId();
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("任务不存在"));
        
        // 移除对taskService.triggerTask的调用，直接处理任务执行逻辑
        if (Boolean.TRUE.equals(task.getIsCollectionTask())) {
            try {
                log.info("开始为任务[{}]创建采集批次", taskId);
                
                // 1. 创建任务批次
                String batchName = "批次-" + task.getTaskName() + "-" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
                TaskBatch batch = collectionTaskService.createTaskBatch(
                    taskId, 
                    task.getCollectorId(), 
                    batchName,
                    "由触发器[" + trigger.getTriggerName() + "]自动创建的批次"
                );
                
                // 2. 获取任务关联的设备和指标
                List<TaskDevice> taskDevices = task.getTaskDevices();
                List<TaskMetric> taskMetrics = task.getTaskMetrics();
                
                if (taskDevices != null && !taskDevices.isEmpty() && taskMetrics != null && !taskMetrics.isEmpty()) {
                    // 3. 根据设备和指标组合生成采集任务
                    List<CollectionTaskDTO> collectionTasks = new ArrayList<>();
                    
                    for (TaskDevice device : taskDevices) {
                        for (TaskMetric metric : taskMetrics) {
                            CollectionTaskDTO collectionTask = new CollectionTaskDTO();
                            collectionTask.setDeviceId(device.getDeviceId());
                            collectionTask.setMetricId(metric.getMetricId());
                            collectionTask.setDeviceName(device.getDeviceName());
                            collectionTask.setMetricName(metric.getMetricName());
                            // 由于TaskDevice没有直接的IP地址和端口字段，我们使用默认值或从关联设备获取
                            collectionTask.setTargetAddress(""); // 默认空地址，实际采集时会通过设备ID查询
                            collectionTask.setTargetPort(0);     // 默认端口，实际采集时会通过设备ID查询
                            collectionTask.setCollectType(task.getCollectType());
                            collectionTask.setCollectParams(task.getCollectParams());
                            collectionTask.setTimeout(30000); // 默认30秒超时
                            collectionTask.setRetryCount(3);  // 默认重试3次
                            
                            collectionTasks.add(collectionTask);
                        }
                    }
                    
                    // 4. 创建采集任务
                    if (!collectionTasks.isEmpty()) {
                        collectionTaskService.createCollectionTasks(batch.getId(), collectionTasks);
                        
                        // 5. 提交批次，开始处理
                        boolean submitted = collectionTaskService.submitBatch(batch.getId());
                        if (submitted) {
                            log.info("任务[{}]的采集批次[{}]已创建并提交，包含{}个采集任务", 
                                    taskId, batch.getId(), collectionTasks.size());
                            return true;
                        } else {
                            log.warn("任务[{}]的采集批次[{}]提交失败", taskId, batch.getId());
                            return false;
                        }
                    } else {
                        log.warn("任务[{}]没有有效的设备和指标组合，无法创建采集任务", taskId);
                        return false;
                    }
                } else {
                    log.warn("任务[{}]没有关联设备或指标，无法创建采集任务", taskId);
                    return false;
                }
                
            } catch (Exception e) {
                log.error("为任务[{}]创建采集批次时发生错误", taskId, e);
                return false;
            }
        } else {
            // 非采集任务的处理逻辑
            log.info("触发非采集任务[{}]：{}", taskId, task.getTaskName());
            // 这里可以添加其他类型任务的处理逻辑
            return true;
        }
    }

    @Override
    public List<TaskScheduleTrigger> findTriggersToExecute(LocalDateTime now) {
        return triggerRepository.findTriggersToExecute(now);
    }

    @Override
    @Transactional
    public TaskScheduleTrigger updateTriggerFireTime(Long triggerId, LocalDateTime lastFireTime, LocalDateTime nextFireTime) {
        TaskScheduleTrigger trigger = triggerRepository.findById(triggerId)
                .orElseThrow(() -> new RuntimeException("触发器不存在"));
        
        if (lastFireTime != null) {
            trigger.setLastFireTime(lastFireTime);
        }
        
        if (nextFireTime != null) {
            trigger.setNextFireTime(nextFireTime);
        } else if (trigger.getTriggerType() == 1 && trigger.getCronExpression() != null) {
            // 如果未提供下次执行时间，则自动计算
            trigger.setNextFireTime(calculateNextFireTime(trigger));
        }
        
        return triggerRepository.save(trigger);
    }

    @Override
    public List<TaskScheduleTrigger> findTriggerable() {
        LocalDateTime now = LocalDateTime.now();
        return triggerRepository.findTriggerable(now, true);
    }

    @Override
    @Transactional
    public boolean updateNextFireTime(Long id, LocalDateTime nextFireTime) {
        int updated = triggerRepository.updateNextFireTime(id, nextFireTime);
        return updated > 0;
    }

    @Override
    @Transactional
    public boolean updateFireInfo(Long id, LocalDateTime lastFireTime, boolean increaseFiredCount) {
        Optional<TaskScheduleTrigger> optionalTrigger = triggerRepository.findById(id);
        if (!optionalTrigger.isPresent()) {
            return false;
        }
        
        TaskScheduleTrigger trigger = optionalTrigger.get();
        int firedCount = trigger.getFiredCount();
        if (increaseFiredCount) {
            firedCount++;
        }
        
        int updated = triggerRepository.updateFireInfo(id, lastFireTime, firedCount);
        return updated > 0;
    }

    @Override
    public List<TaskScheduleTrigger> findByCollectorId(Long collectorId) {
        return triggerRepository.findByCollectorIdAndEnabled(collectorId, true);
    }

    @Override
    public PageResult<TaskScheduleTrigger> findAll(Pageable pageable) {
        Page<TaskScheduleTrigger> page = triggerRepository.findAll(pageable);
        return PageResult.of(page);
    }
} 