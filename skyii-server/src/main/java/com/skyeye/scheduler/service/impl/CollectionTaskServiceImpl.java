package com.skyeye.scheduler.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.common.response.PageResult;
import com.skyeye.scheduler.dto.CollectionResultDTO;
import com.skyeye.scheduler.dto.CollectionTaskDTO;
import com.skyeye.scheduler.entity.CollectionResult;
import com.skyeye.scheduler.entity.CollectionTask;
import com.skyeye.scheduler.entity.TaskBatch;
import com.skyeye.scheduler.enums.TaskStatus;
import com.skyeye.scheduler.repository.CollectionResultRepository;
import com.skyeye.scheduler.repository.CollectionTaskRepository;
import com.skyeye.scheduler.repository.TaskBatchRepository;
import com.skyeye.scheduler.service.CollectionTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 采集任务服务实现
 */
@Slf4j
@Service
public class CollectionTaskServiceImpl implements CollectionTaskService {

    @Autowired
    private TaskBatchRepository taskBatchRepository;
    
    @Autowired
    private CollectionTaskRepository collectionTaskRepository;
    
    @Autowired
    private CollectionResultRepository collectionResultRepository;

    @Override
    @Transactional
    public TaskBatch createTaskBatch(Long taskId, Long collectorId, String batchName, String description) {
        TaskBatch batch = new TaskBatch();
        batch.setTaskId(taskId);
        batch.setCollectorId(collectorId);
        batch.setBatchName(batchName);
        batch.setDescription(description);
        batch.setStatus(TaskStatus.PENDING.name());
        batch.setScheduledTime(LocalDateTime.now());
        return taskBatchRepository.save(batch);
    }

    @Override
    @Transactional
    public List<CollectionTask> createCollectionTasks(Long batchId, List<CollectionTaskDTO> tasks) {
        Optional<TaskBatch> optionalBatch = taskBatchRepository.findById(batchId);
        if (!optionalBatch.isPresent()) {
            throw new BusinessException("批次不存在");
        }
        
        TaskBatch batch = optionalBatch.get();
        List<CollectionTask> createdTasks = new ArrayList<>();
        
        for (CollectionTaskDTO taskDTO : tasks) {
            CollectionTask task = new CollectionTask();
            BeanUtils.copyProperties(taskDTO, task);
            task.setBatchId(batchId);
            task.setStatus(TaskStatus.PENDING.name());
            createdTasks.add(collectionTaskRepository.save(task));
        }
        
        // 更新批次总任务数
        batch.setTotalTasks(batch.getTotalTasks() + createdTasks.size());
        taskBatchRepository.save(batch);
        
        return createdTasks;
    }

    @Override
    @Transactional
    public boolean submitBatch(Long batchId) {
        Optional<TaskBatch> optionalBatch = taskBatchRepository.findById(batchId);
        if (!optionalBatch.isPresent()) {
            return false;
        }
        
        TaskBatch batch = optionalBatch.get();
        if (!TaskStatus.PENDING.name().equals(batch.getStatus())) {
            return false;
        }
        
        batch.setStatus(TaskStatus.SCHEDULED.name());
        taskBatchRepository.save(batch);
        return true;
    }

    @Override
    @Transactional
    public boolean cancelBatch(Long batchId) {
        Optional<TaskBatch> optionalBatch = taskBatchRepository.findById(batchId);
        if (!optionalBatch.isPresent()) {
            return false;
        }
        
        TaskBatch batch = optionalBatch.get();
        if (TaskStatus.RUNNING.name().equals(batch.getStatus())) {
            return false;
        }
        
        batch.setStatus(TaskStatus.CANCELLED.name());
        taskBatchRepository.save(batch);
        
        // 取消所有未开始的任务
        List<CollectionTask> pendingTasks = collectionTaskRepository.findByBatchIdAndStatus(batchId, TaskStatus.PENDING.name());
        for (CollectionTask task : pendingTasks) {
            task.setStatus(TaskStatus.CANCELLED.name());
            collectionTaskRepository.save(task);
        }
        
        return true;
    }

    @Override
    public List<TaskBatch> getPendingBatches(Long collectorId) {
        return taskBatchRepository.findByCollectorIdAndStatus(collectorId, TaskStatus.SCHEDULED.name());
    }

    @Override
    public List<CollectionTask> getPendingTasks(Long batchId) {
        return collectionTaskRepository.findByBatchIdAndStatus(batchId, TaskStatus.PENDING.name());
    }

    @Override
    @Transactional
    public boolean updateTaskStatus(Long taskId, String status) {
        Optional<CollectionTask> optionalTask = collectionTaskRepository.findById(taskId);
        if (!optionalTask.isPresent()) {
            return false;
        }
        
        CollectionTask task = optionalTask.get();
        task.setStatus(status);
        
        if (TaskStatus.RUNNING.name().equals(status)) {
            task.setStartTime(LocalDateTime.now());
        }
        
        collectionTaskRepository.save(task);
        return true;
    }

    @Override
    @Transactional
    public CollectionResult submitTaskResult(CollectionResultDTO resultDTO) {
        // 更新任务状态
        Optional<CollectionTask> optionalTask = collectionTaskRepository.findById(resultDTO.getTaskId());
        if (!optionalTask.isPresent()) {
            throw new BusinessException("任务不存在");
        }
        
        CollectionTask task = optionalTask.get();
        task.setStatus(resultDTO.getStatus());
        task.setEndTime(LocalDateTime.now());
        
        if (task.getStartTime() != null) {
            task.setExecutionTime(resultDTO.getExecutionTime());
        }
        
        task.setResult(resultDTO.getMetricValue());
        task.setErrorMessage(resultDTO.getErrorMessage());
        collectionTaskRepository.save(task);
        
        // 保存采集结果
        CollectionResult result = new CollectionResult();
        BeanUtils.copyProperties(resultDTO, result);
        
        if (resultDTO.getCollectionTime() == null) {
            result.setCollectionTime(LocalDateTime.now());
        }
        
        return collectionResultRepository.save(result);
    }

    @Override
    @Transactional
    public boolean updateBatchStatus(Long batchId) {
        Optional<CollectionTask> optionalTask = collectionTaskRepository.findById(batchId);
        if (!optionalTask.isPresent()) {
            return false;
        }
        
        CollectionTask task = optionalTask.get();
        Long batchIdFromTask = task.getBatchId();
        
        Optional<TaskBatch> optionalBatch = taskBatchRepository.findById(batchIdFromTask);
        if (!optionalBatch.isPresent()) {
            return false;
        }
        
        TaskBatch batch = optionalBatch.get();
        
        // 统计已完成任务数
        Long completedCount = collectionTaskRepository.countByBatchIdAndStatus(batchIdFromTask, TaskStatus.COMPLETED.name());
        batch.setCompletedTasks(completedCount.intValue());
        
        // 检查是否所有任务都已完成
        if (batch.getCompletedTasks() >= batch.getTotalTasks()) {
            batch.setStatus(TaskStatus.COMPLETED.name());
        } else {
            // 检查是否有运行中的任务
            Long runningCount = collectionTaskRepository.countByBatchIdAndStatus(batchIdFromTask, TaskStatus.RUNNING.name());
            if (runningCount > 0) {
                batch.setStatus(TaskStatus.RUNNING.name());
            }
        }
        
        batch.setLastExecutionTime(LocalDateTime.now());
        taskBatchRepository.save(batch);
        return true;
    }

    @Override
    public Optional<TaskBatch> findBatchById(Long batchId) {
        return taskBatchRepository.findById(batchId);
    }

    @Override
    public Optional<CollectionTask> findTaskById(Long taskId) {
        return collectionTaskRepository.findById(taskId);
    }

    @Override
    public PageResult<TaskBatch> findBatchByTaskId(Long taskId, Pageable pageable) {
        // 使用分页查询，目前未实现分页功能，返回全部数据
        List<TaskBatch> batches = taskBatchRepository.findByTaskId(taskId);
        return PageResult.of(batches, 1, batches.size(), batches.size());
    }

    @Override
    public PageResult<CollectionTask> findTasksByBatchId(Long batchId, Pageable pageable) {
        // 使用分页查询，目前未实现分页功能，返回全部数据
        List<CollectionTask> tasks = collectionTaskRepository.findByBatchId(batchId);
        return PageResult.of(tasks, 1, tasks.size(), tasks.size());
    }

    @Override
    public List<CollectionResult> findRecentResults(Long deviceId, Long metricId, int limit) {
        return collectionResultRepository.findRecentResults(deviceId, metricId, limit);
    }
} 