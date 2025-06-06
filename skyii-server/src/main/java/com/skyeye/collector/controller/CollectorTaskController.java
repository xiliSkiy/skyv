package com.skyeye.collector.controller;

import com.skyeye.collector.dto.CollectionResultDTO;
import com.skyeye.collector.dto.LogEntryDTO;
import com.skyeye.collector.dto.StatusUpdateDTO;
import com.skyeye.collector.service.CollectorTaskService;
import com.skyeye.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * 采集端任务控制器
 * 处理采集端任务和结果的API
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/collectors")
public class CollectorTaskController {
    
    @Autowired
    private CollectorTaskService taskService;
    
    /**
     * 获取待执行任务批次
     *
     * @param collectorId 采集端ID
     * @param status      筛选状态
     * @param limit       最大返回批次数
     * @return 任务批次列表
     */
    @GetMapping("/{collectorId}/batches")
    public ApiResponse<?> getBatches(
            @PathVariable String collectorId,
            @RequestParam(required = false, defaultValue = "SCHEDULED") String status,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        log.info("采集端[{}]请求获取状态为[{}]的批次，最大数量: {}", collectorId, status, limit);
        return ApiResponse.success(taskService.getBatchesForCollector(collectorId, status, limit));
    }
    
    /**
     * 获取批次内的任务
     *
     * @param collectorId 采集端ID
     * @param batchId     批次ID
     * @return 任务列表
     */
    @GetMapping("/{collectorId}/batches/{batchId}/tasks")
    public ApiResponse<?> getTasks(
            @PathVariable String collectorId,
            @PathVariable Long batchId) {
        log.info("采集端[{}]请求获取批次[{}]的任务", collectorId, batchId);
        return ApiResponse.success(taskService.getTasksForBatch(collectorId, batchId));
    }
    
    /**
     * 更新批次状态
     *
     * @param collectorId 采集端ID
     * @param batchId     批次ID
     * @param statusDTO   状态更新DTO
     * @return 结果
     */
    @PutMapping("/{collectorId}/batches/{batchId}/status")
    public ApiResponse<?> updateBatchStatus(
            @PathVariable String collectorId,
            @PathVariable Long batchId,
            @RequestBody @Valid StatusUpdateDTO statusDTO) {
        log.info("采集端[{}]请求更新批次[{}]状态为: {}", collectorId, batchId, statusDTO.getStatus());
        taskService.updateBatchStatus(collectorId, batchId, statusDTO);
        return ApiResponse.success();
    }
    
    /**
     * 更新任务状态
     *
     * @param collectorId 采集端ID
     * @param taskId      任务ID
     * @param statusDTO   状态更新DTO
     * @return 结果
     */
    @PutMapping("/{collectorId}/tasks/{taskId}/status")
    public ApiResponse<?> updateTaskStatus(
            @PathVariable String collectorId,
            @PathVariable Long taskId,
            @RequestBody @Valid StatusUpdateDTO statusDTO) {
        log.info("采集端[{}]请求更新任务[{}]状态为: {}", collectorId, taskId, statusDTO.getStatus());
        taskService.updateTaskStatus(collectorId, taskId, statusDTO);
        return ApiResponse.success();
    }
    
    /**
     * 上传采集结果
     *
     * @param collectorId 采集端ID
     * @param results     采集结果
     * @return 结果
     */
    @PostMapping("/{collectorId}/results")
    public ApiResponse<?> uploadResults(
            @PathVariable String collectorId,
            @RequestBody @Valid List<CollectionResultDTO> results) {
        log.info("采集端[{}]上传{}条采集结果", collectorId, results.size());
        taskService.saveCollectionResults(collectorId, results);
        return ApiResponse.success();
    }
    
    /**
     * 上传日志
     *
     * @param collectorId 采集端ID
     * @param logs        日志条目
     * @return 结果
     */
    @PostMapping("/{collectorId}/logs")
    public ApiResponse<?> uploadLogs(
            @PathVariable String collectorId,
            @RequestBody @Valid List<LogEntryDTO> logs) {
        log.info("采集端[{}]上传{}条日志", collectorId, logs.size());
        taskService.saveCollectorLogs(collectorId, logs);
        return ApiResponse.success();
    }
} 