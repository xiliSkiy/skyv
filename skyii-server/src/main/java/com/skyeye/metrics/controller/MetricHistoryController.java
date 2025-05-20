package com.skyeye.metrics.controller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.metrics.dto.MetricHistoryDTO;
import com.skyeye.metrics.service.MetricHistoryService;

/**
 * 指标采集历史控制器
 */
@RestController
@RequestMapping("/v1/metric-history")
public class MetricHistoryController {
    
    @Autowired
    private MetricHistoryService metricHistoryService;
    
    /**
     * 分页查询采集历史
     * @param metricId 指标ID
     * @param deviceId 设备ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 分页采集历史数据
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MetricHistoryDTO>>> getMetricHistory(
            @RequestParam(required = false) Long metricId,
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startTime,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endTime,
            @PageableDefault(size = 10) Pageable pageable) {
        Page<MetricHistoryDTO> page = metricHistoryService.findByConditions(metricId, deviceId, status, startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    
    /**
     * 获取指定指标的最近采集历史
     * @param metricId 指标ID
     * @return 采集历史列表
     */
    @GetMapping("/latest/{metricId}")
    public ResponseEntity<ApiResponse<List<MetricHistoryDTO>>> getLatestHistory(@PathVariable Long metricId) {
        List<MetricHistoryDTO> history = metricHistoryService.findLatestByMetricId(metricId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    /**
     * 获取采集历史详情
     * @param id 采集历史ID
     * @return 采集历史详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MetricHistoryDTO>> getMetricHistoryById(@PathVariable Long id) {
        MetricHistoryDTO history = metricHistoryService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
    
    /**
     * 批量删除采集历史
     * @param ids ID列表
     * @return 响应结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteHistory(@RequestBody List<Long> ids) {
        metricHistoryService.deleteByIds(ids);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 获取采集历史统计信息
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<Object>> getStatistics() {
        Object statistics = metricHistoryService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(statistics));
    }
    
    /**
     * 手动触发指标采集
     * @param metricId 指标ID
     * @param deviceId 设备ID
     * @return 采集结果
     */
    @PostMapping("/trigger")
    public ResponseEntity<ApiResponse<MetricHistoryDTO>> triggerCollection(
            @RequestParam Long metricId,
            @RequestParam(required = false) Long deviceId) {
        MetricHistoryDTO result = metricHistoryService.triggerCollection(metricId, deviceId);
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 清理历史数据
     * @param days 保留天数
     * @return 清理结果
     */
    @DeleteMapping("/clean")
    public ResponseEntity<ApiResponse<Integer>> cleanHistory(@RequestParam Integer days) {
        int count = metricHistoryService.cleanHistory(days);
        return ResponseEntity.ok(ApiResponse.success(count));
    }
} 