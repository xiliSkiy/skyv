package com.skyeye.metrics.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skyeye.common.response.ResponseResult;
import com.skyeye.metrics.dto.MetricDTO;
import com.skyeye.metrics.service.MetricService;

/**
 * 指标配置控制器
 */
@RestController
@RequestMapping("/v1/metrics")
public class MetricController {

    @Autowired
    private MetricService metricService;
    
    /**
     * 获取指标列表
     * @param page 页码
     * @param size 每页大小
     * @param metricName 指标名称（模糊查询）
     * @param metricType 指标类型
     * @param status 状态
     * @param sortBy 排序字段
     * @param order 排序方式
     * @return 指标列表
     */
    @GetMapping
    public ResponseEntity<ResponseResult<Page<MetricDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String metricName,
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) Integer status,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        
        Sort sort = "desc".equalsIgnoreCase(order) 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<MetricDTO> metrics = metricService.findByConditions(metricName, metricType, status, pageable);
        
        return ResponseEntity.ok(ResponseResult.success(metrics));
    }
    
    /**
     * 获取所有指标
     * @return 指标列表
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseResult<List<MetricDTO>>> listAll() {
        List<MetricDTO> metrics = metricService.listAll();
        return ResponseEntity.ok(ResponseResult.success(metrics));
    }
    
    /**
     * 根据ID获取指标
     * @param id 指标ID
     * @return 指标信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult<MetricDTO>> getById(@PathVariable Long id) {
        MetricDTO metric = metricService.getById(id);
        return ResponseEntity.ok(ResponseResult.success(metric));
    }
    
    /**
     * 根据指标类型获取指标列表
     * @param metricType 指标类型
     * @return 指标列表
     */
    @GetMapping("/type/{metricType}")
    public ResponseEntity<ResponseResult<List<MetricDTO>>> getByType(@PathVariable String metricType) {
        List<MetricDTO> metrics = metricService.findByMetricType(metricType);
        return ResponseEntity.ok(ResponseResult.success(metrics));
    }
    
    /**
     * 根据适用设备类型获取指标列表
     * @param deviceType 设备类型
     * @return 指标列表
     */
    @GetMapping("/device-type/{deviceType}")
    public ResponseEntity<ResponseResult<List<MetricDTO>>> getByDeviceType(@PathVariable String deviceType) {
        List<MetricDTO> metrics = metricService.findByApplicableDeviceType(deviceType);
        return ResponseEntity.ok(ResponseResult.success(metrics));
    }
    
    /**
     * 创建指标
     * @param metricDTO 指标信息
     * @return 创建的指标信息
     */
    @PostMapping
    public ResponseEntity<ResponseResult<MetricDTO>> create(@Valid @RequestBody MetricDTO metricDTO) {
        MetricDTO createdMetric = metricService.save(metricDTO);
        return ResponseEntity.ok(ResponseResult.success(createdMetric));
    }
    
    /**
     * 批量创建指标
     * @param metricDTOs 指标信息列表
     * @return 创建的指标信息列表
     */
    @PostMapping("/batch")
    public ResponseEntity<ResponseResult<List<MetricDTO>>> batchCreate(@Valid @RequestBody List<MetricDTO> metricDTOs) {
        List<MetricDTO> createdMetrics = metricService.batchSave(metricDTOs);
        return ResponseEntity.ok(ResponseResult.success(createdMetrics));
    }
    
    /**
     * 更新指标
     * @param id 指标ID
     * @param metricDTO 指标信息
     * @return 更新后的指标信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseResult<MetricDTO>> update(
            @PathVariable Long id, 
            @Valid @RequestBody MetricDTO metricDTO) {
        
        MetricDTO updatedMetric = metricService.update(id, metricDTO);
        return ResponseEntity.ok(ResponseResult.success(updatedMetric));
    }
    
    /**
     * 删除指标
     * @param id 指标ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseResult<Void>> delete(@PathVariable Long id) {
        metricService.delete(id);
        return ResponseEntity.ok(ResponseResult.success());
    }
    
    /**
     * 批量删除指标
     * @param ids 指标ID列表
     * @return 操作结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ResponseResult<Void>> batchDelete(@RequestBody List<Long> ids) {
        metricService.batchDelete(ids);
        return ResponseEntity.ok(ResponseResult.success());
    }
    
    /**
     * 更新指标状态
     * @param id 指标ID
     * @param status 状态
     * @return 更新后的指标信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseResult<MetricDTO>> updateStatus(
            @PathVariable Long id, 
            @RequestParam Integer status) {
        
        MetricDTO updatedMetric = metricService.updateStatus(id, status);
        return ResponseEntity.ok(ResponseResult.success(updatedMetric));
    }
    
    /**
     * 批量更新指标状态
     * @param ids 指标ID列表
     * @param status 状态
     * @return 更新后的指标信息列表
     */
    @PutMapping("/batch/status")
    public ResponseEntity<ResponseResult<List<MetricDTO>>> batchUpdateStatus(
            @RequestBody List<Long> ids, 
            @RequestParam Integer status) {
        
        List<MetricDTO> updatedMetrics = metricService.batchUpdateStatus(ids, status);
        return ResponseEntity.ok(ResponseResult.success(updatedMetrics));
    }
    
    /**
     * 导入指标
     * @param metrics 指标列表
     * @return 导入结果
     */
    @PostMapping("/import")
    public ResponseEntity<ResponseResult<String>> importMetrics(@RequestBody List<MetricDTO> metrics) {
        String result = metricService.importMetrics(metrics);
        return ResponseEntity.ok(ResponseResult.success(result));
    }
    
    /**
     * 导出指标
     * @param ids 指标ID列表，为空则导出所有
     * @return 导出的指标列表
     */
    @PostMapping("/export")
    public ResponseEntity<ResponseResult<List<MetricDTO>>> exportMetrics(@RequestBody(required = false) List<Long> ids) {
        List<MetricDTO> metrics = metricService.exportMetrics(ids);
        return ResponseEntity.ok(ResponseResult.success(metrics));
    }
    
    /**
     * 获取指标统计信息
     * @return 统计信息
     */
    @GetMapping("/statistics")
    public ResponseEntity<ResponseResult<Object>> getStatistics() {
        Object statistics = metricService.getMetricStatistics();
        return ResponseEntity.ok(ResponseResult.success(statistics));
    }
    
    /**
     * 检查指标Key是否存在
     * @param metricKey 指标Key
     * @param id 排除的指标ID（用于更新时检查）
     * @return 是否存在
     */
    @GetMapping("/check-key")
    public ResponseEntity<ResponseResult<Boolean>> checkMetricKey(
            @RequestParam String metricKey, 
            @RequestParam(required = false) Long id) {
        
        boolean exists = metricService.isMetricKeyExist(metricKey, id);
        return ResponseEntity.ok(ResponseResult.success(exists));
    }
    
    /**
     * 获取指标概览数据
     * @return 概览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 获取指标总数
        long total = metricService.count();
        overview.put("total", total);
        
        // 获取已启用的指标数量
        long enabled = metricService.countByStatus(1);
        overview.put("enabled", enabled);
        
        // 获取配置了报警规则的指标数量
        long withAlert = metricService.countByHasAlertRule();
        overview.put("withAlert", withAlert);
        
        return ResponseEntity.ok(ResponseResult.success(overview));
    }
} 