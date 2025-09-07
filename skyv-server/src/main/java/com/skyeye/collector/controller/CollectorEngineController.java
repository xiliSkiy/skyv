package com.skyeye.collector.controller;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.entity.CollectionData;
import com.skyeye.collector.entity.CollectionLog;
import com.skyeye.collector.manager.CollectorEngineManager;
import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.registry.PluginRegistry;
import com.skyeye.collector.repository.CollectionDataRepository;
import com.skyeye.collector.service.CollectionLogService;
import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceDto;
import com.skyeye.device.entity.Device;
import com.skyeye.device.entity.DeviceType;
import com.skyeye.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


/**
 * 采集引擎控制器
 * 
 * @author SkyEye Team
 */
@Slf4j
@RestController
@RequestMapping("/api/collector/engine")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class CollectorEngineController {

    private final CollectorEngineManager engineManager;
    private final PluginRegistry pluginRegistry;
    private final DeviceService deviceService;
    private final CollectionLogService collectionLogService;
    private final CollectionDataRepository collectionDataRepository;

    /**
     * 执行单个数据采集
     */
    @PostMapping("/collect")
    @PreAuthorize("hasPermission('collector', 'execute')")
    public ApiResponse<CollectionResult> executeCollection(
            @RequestParam Long deviceId,
            @RequestBody CollectionRequest request) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            
            Device device = convertToDevice(deviceDto);
            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            CollectionResult result = engineManager.executeCollection(device, request.getMetricConfig(), context);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("执行数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 异步执行数据采集
     */
    @PostMapping("/collect-async")
    @PreAuthorize("hasPermission('collector', 'execute')")
    public ApiResponse<String> executeCollectionAsync(
            @RequestParam Long deviceId,
            @RequestBody CollectionRequest request) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            
            Device device = convertToDevice(deviceDto);
            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            engineManager.executeCollectionAsync(device, request.getMetricConfig(), context);
            
            return ApiResponse.success("异步采集任务已提交，会话ID: " + context.getSessionId());

        } catch (Exception e) {
            log.error("提交异步数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "异步数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 批量执行数据采集
     */
    @PostMapping("/collect-batch")
    @PreAuthorize("hasPermission('collector', 'execute')")
    public ApiResponse<List<CollectionResult>> executeBatchCollection(
            @RequestParam Long deviceId,
            @RequestBody BatchCollectionRequest request) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            
            Device device = convertToDevice(deviceDto);
            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            List<CollectionResult> results = engineManager.executeBatchCollection(
                    device, request.getMetricConfigs(), context);
            return ApiResponse.success(results);

        } catch (Exception e) {
            log.error("执行批量数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "批量数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 异步批量执行数据采集
     */
    @PostMapping("/collect-batch-async")
    @PreAuthorize("hasPermission('collector', 'execute')")
    public ApiResponse<String> executeBatchCollectionAsync(
            @RequestParam Long deviceId,
            @RequestBody BatchCollectionRequest request) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            
            Device device = convertToDevice(deviceDto);
            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            engineManager.executeBatchCollectionAsync(device, request.getMetricConfigs(), context);
            
            return ApiResponse.success("异步批量采集任务已提交，会话ID: " + context.getSessionId());

        } catch (Exception e) {
            log.error("提交异步批量数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "异步批量数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 获取引擎状态
     */
    @GetMapping("/status")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<Map<String, Object>> getEngineStatus() {
        try {
            Map<String, Object> status = engineManager.getEngineStatus();
            return ApiResponse.success(status);
        } catch (Exception e) {
            log.error("获取引擎状态失败", e);
            return ApiResponse.error(500, "获取引擎状态异常: " + e.getMessage());
        }
    }

    /**
     * 获取详细统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<Map<String, Object>> getDetailedStatistics() {
        try {
            Map<String, Object> statistics = engineManager.getDetailedStatistics();
            return ApiResponse.success(statistics);
        } catch (Exception e) {
            log.error("获取详细统计信息失败", e);
            return ApiResponse.error(500, "获取统计信息异常: " + e.getMessage());
        }
    }

    /**
     * 获取插件性能排名
     */
    @GetMapping("/plugin-performance")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<List<Map<String, Object>>> getPluginPerformanceRanking() {
        try {
            List<Map<String, Object>> ranking = engineManager.getPluginPerformanceRanking();
            return ApiResponse.success(ranking);
        } catch (Exception e) {
            log.error("获取插件性能排名失败", e);
            return ApiResponse.error(500, "获取性能排名异常: " + e.getMessage());
        }
    }

    /**
     * 重置统计信息
     */
    @PostMapping("/reset-statistics")
    @PreAuthorize("hasPermission('collector', 'manage')")
    public ApiResponse<Void> resetStatistics() {
        try {
            engineManager.resetStatistics();
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("重置统计信息失败", e);
            return ApiResponse.error(500, "重置统计信息异常: " + e.getMessage());
        }
    }

    /**
     * 执行健康检查
     */
    @GetMapping("/health")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<Map<String, Object>> performHealthCheck() {
        try {
            Map<String, Object> healthStatus = engineManager.performHealthCheck();
            return ApiResponse.success(healthStatus);
        } catch (Exception e) {
            log.error("执行健康检查失败", e);
            return ApiResponse.error(500, "健康检查异常: " + e.getMessage());
        }
    }

    /**
     * 获取已注册的插件列表
     */
    @GetMapping("/plugins")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<List<Map<String, Object>>> getRegisteredPlugins() {
        try {
            List<Map<String, Object>> plugins = new ArrayList<>();
            
            for (CollectorPlugin plugin : pluginRegistry.getAllPlugins()) {
                Map<String, Object> pluginInfo = Map.of(
                        "type", plugin.getPluginType(),
                        "name", plugin.getPluginName(),
                        "version", plugin.getPluginVersion(),
                        "description", plugin.getPluginDescription(),
                        "supportedProtocols", plugin.getSupportedProtocols(),
                        "supportedMetricTypes", plugin.getSupportedMetricTypes(),
                        "healthStatus", plugin.getHealthStatus()
                );
                plugins.add(pluginInfo);
            }
            
            return ApiResponse.success(plugins);
        } catch (Exception e) {
            log.error("获取插件列表失败", e);
            return ApiResponse.error(500, "获取插件列表异常: " + e.getMessage());
        }
    }

    /**
     * 获取采集数据
     */
    @GetMapping("/data")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<Page<CollectionData>> getCollectionData(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) String metricName,
            @RequestParam(required = false) String pluginType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size) {
        
        try {
            Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "collectedAt"));
            
            Specification<CollectionData> spec = (root, query, criteriaBuilder) -> {
                var predicates = new ArrayList<jakarta.persistence.criteria.Predicate>();
                
                if (deviceId != null) {
                    predicates.add(criteriaBuilder.equal(root.get("deviceId"), deviceId));
                }
                if (metricName != null && !metricName.trim().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("metricName"), metricName));
                }
                if (pluginType != null && !pluginType.trim().isEmpty()) {
                    predicates.add(criteriaBuilder.equal(root.get("pluginType"), pluginType));
                }
                
                return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
            };
            
            Page<CollectionData> data = collectionDataRepository.findAll(spec, pageable);
            return ApiResponse.success(data);
            
        } catch (Exception e) {
            log.error("获取采集数据失败", e);
            return ApiResponse.error(500, "获取采集数据异常: " + e.getMessage());
        }
    }

    /**
     * 获取采集日志
     */
    @GetMapping("/logs")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<List<CollectionLog>> getCollectionLogs(
            @RequestParam(required = false) Long deviceId,
            @RequestParam(required = false) Long taskId,
            @RequestParam(required = false) String status,
            @RequestParam(defaultValue = "50") int limit) {
        
        try {
            List<CollectionLog> logs;
            
            if (deviceId != null) {
                logs = collectionLogService.getDeviceLogs(deviceId, limit);
            } else if (taskId != null) {
                logs = collectionLogService.getTaskLogs(taskId, limit);
            } else if ("FAILED".equals(status)) {
                logs = collectionLogService.getFailedLogs(limit);
            } else if ("RUNNING".equals(status)) {
                logs = collectionLogService.getRunningLogs();
            } else {
                // 获取最近的日志
                logs = collectionLogService.getFailedLogs(limit); // 默认显示失败的日志
            }
            
            return ApiResponse.success(logs);
            
        } catch (Exception e) {
            log.error("获取采集日志失败", e);
            return ApiResponse.error(500, "获取采集日志异常: " + e.getMessage());
        }
    }

    /**
     * 获取采集统计信息
     */
    @GetMapping("/collection-statistics")
    @PreAuthorize("hasPermission('collector', 'view')")
    public ApiResponse<Map<String, Object>> getCollectionStatistics(
            @RequestParam(required = false) String timeRange) {
        
        try {
            Timestamp startTime, endTime = Timestamp.valueOf(LocalDateTime.now());
            
            // 根据时间范围参数设置开始时间
            switch (timeRange != null ? timeRange : "1h") {
                case "1h":
                    startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(1));
                    break;
                case "24h":
                    startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(24));
                    break;
                case "7d":
                    startTime = Timestamp.valueOf(LocalDateTime.now().minusDays(7));
                    break;
                case "30d":
                    startTime = Timestamp.valueOf(LocalDateTime.now().minusDays(30));
                    break;
                default:
                    startTime = Timestamp.valueOf(LocalDateTime.now().minusHours(1));
            }
            
            Map<String, Object> statistics = collectionLogService.getCollectionStatistics(startTime, endTime);
            return ApiResponse.success(statistics);
            
        } catch (Exception e) {
            log.error("获取采集统计信息失败", e);
            return ApiResponse.error(500, "获取统计信息异常: " + e.getMessage());
        }
    }

    /**
     * 将DeviceDto转换为Device实体
     */
    private Device convertToDevice(DeviceDto deviceDto) {
        Device device = new Device();
        device.setId(deviceDto.getId());
        device.setName(deviceDto.getName());
        device.setIpAddress(deviceDto.getIpAddress());
        device.setPort(deviceDto.getPort());
        device.setLocation(deviceDto.getLocation());
        device.setDescription(deviceDto.getDescription());
        device.setDeviceTypeId(deviceDto.getDeviceTypeId());
        
        // 创建DeviceType对象用于插件判断
        DeviceType deviceType = new DeviceType();
        deviceType.setId(deviceDto.getDeviceTypeId());
        deviceType.setName(deviceDto.getDeviceTypeName());
        // 注意：这里需要从数据库查询获取protocols，暂时设置为支持所有协议
        deviceType.setProtocols(List.of("HTTP", "HTTPS", "SNMP", "SSH"));
        
        // 临时设置deviceType字段（仅用于插件判断）
        device.setDeviceType(deviceType);
        
        return device;
    }

    /**
     * 单个采集请求
     */
    @lombok.Data
    public static class CollectionRequest {
        private MetricConfig metricConfig;
        private Map<String, Object> credentials;
    }

    /**
     * 批量采集请求
     */
    @lombok.Data
    public static class BatchCollectionRequest {
        private List<MetricConfig> metricConfigs;
        private Map<String, Object> credentials;
    }
}
