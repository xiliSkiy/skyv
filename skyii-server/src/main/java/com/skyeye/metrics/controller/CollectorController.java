package com.skyeye.metrics.controller;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

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
import com.skyeye.metrics.dto.CollectorDTO;
import com.skyeye.metrics.service.CollectorService;

/**
 * 采集器配置控制器
 */
@RestController
@RequestMapping("/v1/collectors")
public class CollectorController {

    @Autowired
    private CollectorService collectorService;
    
    /**
     * 获取采集器列表
     * @param page 页码
     * @param size 每页大小
     * @param sortBy 排序字段
     * @param order 排序方式
     * @return 采集器列表
     */
    @GetMapping
    public ResponseEntity<ResponseResult<Page<CollectorDTO>>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String order) {
        
        Sort sort = "desc".equalsIgnoreCase(order) 
                ? Sort.by(sortBy).descending() 
                : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<CollectorDTO> collectors = collectorService.page(pageable);
        
        return ResponseEntity.ok(ResponseResult.success(collectors));
    }
    
    /**
     * 获取所有采集器
     * @return 采集器列表
     */
    @GetMapping("/all")
    public ResponseEntity<ResponseResult<List<CollectorDTO>>> listAll() {
        List<CollectorDTO> collectors = collectorService.listAll();
        return ResponseEntity.ok(ResponseResult.success(collectors));
    }
    
    /**
     * 根据ID获取采集器
     * @param id 采集器ID
     * @return 采集器信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ResponseResult<CollectorDTO>> getById(@PathVariable Long id) {
        CollectorDTO collector = collectorService.getById(id);
        return ResponseEntity.ok(ResponseResult.success(collector));
    }
    
    /**
     * 获取主采集器
     * @return 主采集器信息
     */
    @GetMapping("/main")
    public ResponseEntity<ResponseResult<CollectorDTO>> getMainCollector() {
        CollectorDTO mainCollector = collectorService.getMainCollector();
        return ResponseEntity.ok(ResponseResult.success(mainCollector));
    }
    
    /**
     * 根据采集器类型获取列表
     * @param collectorType 采集器类型
     * @return 采集器列表
     */
    @GetMapping("/type/{collectorType}")
    public ResponseEntity<ResponseResult<List<CollectorDTO>>> getByType(@PathVariable String collectorType) {
        List<CollectorDTO> collectors = collectorService.findByCollectorType(collectorType);
        return ResponseEntity.ok(ResponseResult.success(collectors));
    }
    
    /**
     * 根据状态获取采集器列表
     * @param status 状态
     * @return 采集器列表
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<ResponseResult<List<CollectorDTO>>> getByStatus(@PathVariable Integer status) {
        List<CollectorDTO> collectors = collectorService.findByStatus(status);
        return ResponseEntity.ok(ResponseResult.success(collectors));
    }
    
    /**
     * 创建采集器
     * @param collectorDTO 采集器信息
     * @return 创建的采集器信息
     */
    @PostMapping
    public ResponseEntity<ResponseResult<CollectorDTO>> create(@Valid @RequestBody CollectorDTO collectorDTO) {
        CollectorDTO createdCollector = collectorService.save(collectorDTO);
        return ResponseEntity.ok(ResponseResult.success(createdCollector));
    }
    
    /**
     * 更新采集器
     * @param id 采集器ID
     * @param collectorDTO 采集器信息
     * @return 更新后的采集器信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ResponseResult<CollectorDTO>> update(
            @PathVariable Long id, 
            @Valid @RequestBody CollectorDTO collectorDTO) {
        
        CollectorDTO updatedCollector = collectorService.update(id, collectorDTO);
        return ResponseEntity.ok(ResponseResult.success(updatedCollector));
    }
    
    /**
     * 删除采集器
     * @param id 采集器ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ResponseResult<Void>> delete(@PathVariable Long id) {
        collectorService.delete(id);
        return ResponseEntity.ok(ResponseResult.success());
    }
    
    /**
     * 测试采集器连接
     * @param id 采集器ID
     * @return 测试结果
     */
    @PostMapping("/{id}/test-connection")
    public ResponseEntity<ResponseResult<Map<String, Object>>> testConnection(@PathVariable Long id) {
        Map<String, Object> result = collectorService.testConnection(id);
        return ResponseEntity.ok(ResponseResult.success(result));
    }
    
    /**
     * 设置为主采集器
     * @param id 采集器ID
     * @return 设置后的采集器信息
     */
    @PutMapping("/{id}/set-as-main")
    public ResponseEntity<ResponseResult<CollectorDTO>> setAsMain(@PathVariable Long id) {
        CollectorDTO collector = collectorService.setAsMain(id);
        return ResponseEntity.ok(ResponseResult.success(collector));
    }
    
    /**
     * 更新采集器状态
     * @param id 采集器ID
     * @param status 状态
     * @param statusInfo 状态信息
     * @return 更新后的采集器信息
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<ResponseResult<CollectorDTO>> updateStatus(
            @PathVariable Long id, 
            @RequestParam Integer status,
            @RequestParam(required = false) String statusInfo) {
        
        CollectorDTO updatedCollector = collectorService.updateStatus(id, status, statusInfo);
        return ResponseEntity.ok(ResponseResult.success(updatedCollector));
    }
    
    /**
     * 获取采集器状态统计
     * @return 状态统计信息
     */
    @GetMapping("/status-statistics")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getStatusStatistics() {
        Map<String, Object> statistics = collectorService.getStatusStatistics();
        return ResponseEntity.ok(ResponseResult.success(statistics));
    }
    
    /**
     * 获取采集器概览数据
     * @return 概览数据
     */
    @GetMapping("/overview")
    public ResponseEntity<ResponseResult<Map<String, Object>>> getOverview() {
        Map<String, Object> overview = new HashMap<>();
        
        // 获取采集器总数
        overview.put("total", collectorService.count());
        
        // 获取在线采集器数量
        overview.put("online", collectorService.countByStatus(1)); // 假设状态1表示在线
        
        // 获取离线采集器数量
        overview.put("offline", collectorService.countByStatus(0)); // 假设状态0表示离线
        
        // 获取警告状态的采集器数量
        overview.put("warning", collectorService.countByStatus(2)); // 假设状态2表示警告
        
        return ResponseEntity.ok(ResponseResult.success(overview));
    }

    /**
     * 获取采集区域列表
     * @return 区域列表
     */
    @GetMapping("/zones")
    public ResponseEntity<ResponseResult<List<Map<String, Object>>>> getCollectionZones() {
        // 创建一个模拟的区域列表数据
        List<Map<String, Object>> zones = new ArrayList<>();
        
        // 添加一些模拟数据
        Map<String, Object> zone1 = new HashMap<>();
        zone1.put("id", 1);
        zone1.put("zoneName", "默认区域");
        zone1.put("zoneCode", "DEFAULT");
        
        Map<String, Object> zone2 = new HashMap<>();
        zone2.put("id", 2);
        zone2.put("zoneName", "北区");
        zone2.put("zoneCode", "NORTH");
        
        zones.add(zone1);
        zones.add(zone2);
        
        return ResponseEntity.ok(ResponseResult.success(zones));
    }
} 