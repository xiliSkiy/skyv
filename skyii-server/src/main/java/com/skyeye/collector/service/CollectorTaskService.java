package com.skyeye.collector.service;

import com.skyeye.collector.dto.CollectionResultDTO;
import com.skyeye.collector.dto.LogEntryDTO;
import com.skyeye.collector.dto.StatusUpdateDTO;

import java.util.List;

/**
 * 采集任务服务接口
 */
public interface CollectorTaskService {
    
    /**
     * 获取采集端的任务批次
     *
     * @param collectorId 采集端ID
     * @param status      筛选状态
     * @param limit       最大返回数量
     * @return 任务批次列表
     */
    List<?> getBatchesForCollector(String collectorId, String status, Integer limit);
    
    /**
     * 获取批次中的任务
     *
     * @param collectorId 采集端ID
     * @param batchId     批次ID
     * @return 任务列表
     */
    List<?> getTasksForBatch(String collectorId, Long batchId);
    
    /**
     * 更新批次状态
     *
     * @param collectorId 采集端ID
     * @param batchId     批次ID
     * @param statusDTO   状态更新DTO
     */
    void updateBatchStatus(String collectorId, Long batchId, StatusUpdateDTO statusDTO);
    
    /**
     * 更新任务状态
     *
     * @param collectorId 采集端ID
     * @param taskId      任务ID
     * @param statusDTO   状态更新DTO
     */
    void updateTaskStatus(String collectorId, Long taskId, StatusUpdateDTO statusDTO);
    
    /**
     * 保存采集结果
     *
     * @param collectorId 采集端ID
     * @param results     采集结果列表
     */
    void saveCollectionResults(String collectorId, List<CollectionResultDTO> results);
    
    /**
     * 保存采集端日志
     *
     * @param collectorId 采集端ID
     * @param logs        日志列表
     */
    void saveCollectorLogs(String collectorId, List<LogEntryDTO> logs);
} 