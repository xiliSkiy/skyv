package com.skyeye.scheduler.service;

import com.skyeye.common.response.PageResult;
import com.skyeye.scheduler.dto.CollectionResultDTO;
import com.skyeye.scheduler.dto.CollectionTaskDTO;
import com.skyeye.scheduler.entity.CollectionResult;
import com.skyeye.scheduler.entity.CollectionTask;
import com.skyeye.scheduler.entity.TaskBatch;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/**
 * 采集任务服务接口
 */
public interface CollectionTaskService {

    /**
     * 创建任务批次
     * @param taskId 任务ID
     * @param collectorId 采集器ID
     * @param batchName 批次名称
     * @param description 批次描述
     * @return 任务批次
     */
    TaskBatch createTaskBatch(Long taskId, Long collectorId, String batchName, String description);
    
    /**
     * 为批次创建采集任务
     * @param batchId 批次ID
     * @param tasks 采集任务列表
     * @return 创建的任务列表
     */
    List<CollectionTask> createCollectionTasks(Long batchId, List<CollectionTaskDTO> tasks);
    
    /**
     * 提交批次（开始处理）
     * @param batchId 批次ID
     * @return 是否成功
     */
    boolean submitBatch(Long batchId);
    
    /**
     * 取消批次
     * @param batchId 批次ID
     * @return 是否成功
     */
    boolean cancelBatch(Long batchId);
    
    /**
     * 根据采集器ID获取待处理批次
     * @param collectorId 采集器ID
     * @return 批次列表
     */
    List<TaskBatch> getPendingBatches(Long collectorId);
    
    /**
     * 根据批次ID获取待处理任务
     * @param batchId 批次ID
     * @return 任务列表
     */
    List<CollectionTask> getPendingTasks(Long batchId);
    
    /**
     * 更新任务状态
     * @param taskId 任务ID
     * @param status 状态
     * @return 是否成功
     */
    boolean updateTaskStatus(Long taskId, String status);
    
    /**
     * 提交采集结果
     * @param resultDTO 结果DTO
     * @return 结果实体
     */
    CollectionResult submitTaskResult(CollectionResultDTO resultDTO);
    
    /**
     * 更新批次状态
     * @param batchId 批次ID
     * @return 是否成功
     */
    boolean updateBatchStatus(Long batchId);
    
    /**
     * 根据ID查询批次
     * @param batchId 批次ID
     * @return 批次信息
     */
    Optional<TaskBatch> findBatchById(Long batchId);
    
    /**
     * 根据ID查询任务
     * @param taskId 任务ID
     * @return 任务信息
     */
    Optional<CollectionTask> findTaskById(Long taskId);
    
    /**
     * 分页查询批次
     * @param taskId 任务ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResult<TaskBatch> findBatchByTaskId(Long taskId, Pageable pageable);
    
    /**
     * 分页查询任务
     * @param batchId 批次ID
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResult<CollectionTask> findTasksByBatchId(Long batchId, Pageable pageable);
    
    /**
     * 查询设备最近的采集结果
     * @param deviceId 设备ID
     * @param metricId 指标ID
     * @param limit 限制数量
     * @return 结果列表
     */
    List<CollectionResult> findRecentResults(Long deviceId, Long metricId, int limit);
} 