package com.skyeye.collector.controller;

import com.skyeye.collector.dto.CollectorDTO;
import com.skyeye.collector.dto.CollectorMetricsDTO;
import com.skyeye.collector.service.CollectorService;
import com.skyeye.common.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 采集器控制器
 */
@Slf4j
@RestController
@RequestMapping("/collectors")
public class CollectorController {

    @Autowired
    private CollectorService collectorService;
    
    /**
     * 分页查询采集器列表
     * @param page 页码
     * @param size 每页大小
     * @param collectorName 采集器名称
     * @param collectorType 采集器类型
     * @param status 状态
     * @param networkZone 网络区域
     * @return 分页结果
     */
    @GetMapping
    public ApiResponse<List<CollectorDTO>> getCollectors(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String collectorName,
            @RequestParam(required = false) String collectorType,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String networkZone) {
        
        Map<String, Object> params = new HashMap<>();
        if (collectorName != null) params.put("collectorName", collectorName);
        if (collectorType != null) params.put("collectorType", collectorType);
        if (status != null) params.put("status", status);
        if (networkZone != null) params.put("networkZone", networkZone);
        
        // 前端从1开始计数，后端从0开始，需要减1
        int pageIndex = page > 0 ? page - 1 : 0;
        log.info("查询采集器列表: 前端页码={}, 后端页码={}, 每页大小={}", page, pageIndex, size);
        
        Pageable pageable = PageRequest.of(pageIndex, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<CollectorDTO> collectors = collectorService.findCollectors(params, pageable);
        
        // 确保返回一个列表而不是Page对象
        List<CollectorDTO> collectorList = collectors.getContent();
        
        // 创建分页元数据
        Map<String, Object> meta = new HashMap<>();
        meta.put("total", collectors.getTotalElements());
        meta.put("page", page); // 返回前端传入的页码
        meta.put("size", collectors.getSize());
        
        return ApiResponse.success(collectorList, "获取采集器列表成功", meta);
    }
    
    /**
     * 获取所有采集器
     * @return 采集器列表
     */
    @GetMapping("/all")
    public ApiResponse<List<CollectorDTO>> getAllCollectors() {
        return ApiResponse.success(collectorService.getAllCollectors());
    }
    
    /**
     * 获取采集器详情
     * @param id 采集器ID
     * @return 采集器详情
     */
    @GetMapping("/{id}")
    public ApiResponse<CollectorDTO> getCollectorById(@PathVariable Long id) {
        return ApiResponse.success(collectorService.getCollectorById(id));
    }
    
    /**
     * 创建采集器
     * @param collectorDTO 采集器信息
     * @return 创建的采集器
     */
    @PostMapping
    public ApiResponse<CollectorDTO> createCollector(@Valid @RequestBody CollectorDTO collectorDTO) {
        return ApiResponse.success(collectorService.createCollector(collectorDTO));
    }
    
    /**
     * 更新采集器
     * @param id 采集器ID
     * @param collectorDTO 采集器信息
     * @return 更新后的采集器
     */
    @PutMapping("/{id}")
    public ApiResponse<CollectorDTO> updateCollector(@PathVariable Long id, @Valid @RequestBody CollectorDTO collectorDTO) {
        return ApiResponse.success(collectorService.updateCollector(id, collectorDTO));
    }
    
    /**
     * 删除采集器
     * @param id 采集器ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCollector(@PathVariable Long id) {
        collectorService.deleteCollector(id);
        return ApiResponse.success();
    }
    
    /**
     * 获取采集器监控指标
     * @param id 采集器ID
     * @return 监控指标
     */
    @GetMapping("/{id}/metrics")
    public ApiResponse<CollectorMetricsDTO> getCollectorMetrics(@PathVariable Long id) {
        return ApiResponse.success(collectorService.getCollectorMetrics(id));
    }
    
    /**
     * 测试采集器连接
     * @param params 包含采集器参数的请求体
     * @return 测试结果
     */
    @PostMapping("/test")
    public ApiResponse<Map<String, Object>> testConnection(@RequestBody Map<String, Object> params) {
        // 获取ID或其他必要参数
        Object idObj = params.get("id");
        Long id = null;
        if (idObj != null) {
            if (idObj instanceof Number) {
                id = ((Number) idObj).longValue();
            } else if (idObj instanceof String) {
                try {
                    id = Long.parseLong((String) idObj);
                } catch (NumberFormatException e) {
                    log.error("无效的采集器ID格式: {}", idObj);
                }
            }
        }
        
        Map<String, Object> result = new HashMap<>();
        // 如果有ID，使用ID进行测试
        if (id != null) {
            result = collectorService.testConnection(id);
        } else {
            // 否则使用提供的参数进行测试
            result = collectorService.testConnectionWithParams(params);
        }
        
        return ApiResponse.success(result);
    }
    
    /**
     * 测试采集器连接（通过ID）
     * @param id 采集器ID
     * @return 测试结果
     */
    @PostMapping("/{id}/test-connection")
    public ApiResponse<Map<String, Object>> testConnectionById(@PathVariable Long id) {
        Map<String, Object> result = collectorService.testConnection(id);
        return ApiResponse.success(result);
    }
    
    /**
     * 生成采集器注册令牌
     * @param id 采集器ID
     * @param expireHours 过期小时数
     * @return 注册令牌
     */
    @PostMapping("/{id}/token")
    public ApiResponse<Map<String, String>> generateRegistrationToken(
            @PathVariable Long id,
            @RequestParam(defaultValue = "24") Integer expireHours) {
        
        String token = collectorService.generateRegistrationToken(id, expireHours);
        
        Map<String, String> result = new HashMap<>();
        result.put("token", token);
        result.put("expireHours", expireHours.toString());
        
        return ApiResponse.success(result);
    }
    
    /**
     * 根据注册令牌激活采集器
     * @param token 注册令牌
     * @param collectorInfo 采集器连接信息
     * @return 更新后的采集器
     */
    @PostMapping("/activate")
    public ApiResponse<CollectorDTO> activateCollector(
            @RequestParam String token,
            @RequestBody Map<String, Object> collectorInfo) {
        
        return ApiResponse.success(collectorService.activateCollector(token, collectorInfo));
    }
    
    /**
     * 重启采集器
     * @param id 采集器ID
     * @return 操作结果
     */
    @PostMapping("/{id}/restart")
    public ApiResponse<Boolean> restartCollector(@PathVariable Long id) {
        return ApiResponse.success(collectorService.restartCollector(id));
    }
    
    /**
     * 获取采集器状态统计
     * @return 状态统计
     */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Long>> getCollectorStatusCount() {
        return ApiResponse.success(collectorService.getCollectorStatusCount());
    }
    
    /**
     * 获取采集器网络区域列表
     * @return 网络区域列表
     */
    @GetMapping("/network-zones")
    public ApiResponse<List<String>> getNetworkZones() {
        return ApiResponse.success(collectorService.getNetworkZones());
    }
} 