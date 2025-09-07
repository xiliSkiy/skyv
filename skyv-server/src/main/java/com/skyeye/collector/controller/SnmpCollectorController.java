package com.skyeye.collector.controller;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.plugin.AvailableMetric;
import com.skyeye.collector.plugin.ConnectionTestResult;
import com.skyeye.collector.plugin.impl.SnmpCollectorPlugin;
import com.skyeye.collector.registry.PluginRegistry;
import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.entity.Device;
import com.skyeye.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * SNMP采集插件控制器
 * 
 * @author SkyEye Team
 */
@Slf4j
@RestController
@RequestMapping("/api/collector/snmp")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class SnmpCollectorController {

    private final PluginRegistry pluginRegistry;
    private final DeviceService deviceService;

    /**
     * 测试SNMP连接
     */
    @PostMapping("/test-connection")
    @PreAuthorize("hasPermission('device', 'test')")
    public ApiResponse<ConnectionTestResult> testConnection(
            @RequestParam Long deviceId,
            @RequestBody(required = false) Map<String, Object> credentials) {
        
        try {
            Device device = deviceService.getDeviceEntityById(deviceId);
            if (device == null) {
                return ApiResponse.error(404, "设备不存在");
            }

            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            CollectionContext context = CollectionContext.createForTest();
            if (credentials != null && !credentials.isEmpty()) {
                context.setCredentials(credentials);
            }

            ConnectionTestResult result = pluginOpt.get().testConnection(device, context);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("SNMP连接测试失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "连接测试异常: " + e.getMessage());
        }
    }

    /**
     * 发现设备可用指标
     */
    @PostMapping("/discover-metrics")
    @PreAuthorize("hasPermission('device', 'view')")
    public ApiResponse<List<AvailableMetric>> discoverMetrics(
            @RequestParam Long deviceId,
            @RequestBody(required = false) Map<String, Object> credentials) {
        
        try {
            Device device = deviceService.getDeviceEntityById(deviceId);
            if (device == null) {
                return ApiResponse.error(404, "设备不存在");
            }

            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            CollectionContext context = CollectionContext.createDefault();
            if (credentials != null && !credentials.isEmpty()) {
                context.setCredentials(credentials);
            }

            List<AvailableMetric> metrics = pluginOpt.get().discoverMetrics(device, context);
            return ApiResponse.success(metrics);

        } catch (Exception e) {
            log.error("SNMP指标发现失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "指标发现异常: " + e.getMessage());
        }
    }

    /**
     * 执行SNMP数据采集
     */
    @PostMapping("/collect")
    @PreAuthorize("hasPermission('device', 'collect')")
    public ApiResponse<CollectionResult> collectData(
            @RequestParam Long deviceId,
            @RequestBody SnmpCollectRequest request) {
        
        try {
            Device device = deviceService.getDeviceEntityById(deviceId);
            if (device == null) {
                return ApiResponse.error(404, "设备不存在");
            }

            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            CollectionResult result = pluginOpt.get().collect(device, request.getMetricConfig(), context);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("SNMP数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 批量执行SNMP数据采集
     */
    @PostMapping("/collect-batch")
    @PreAuthorize("hasPermission('device', 'collect')")
    public ApiResponse<List<CollectionResult>> collectBatchData(
            @RequestParam Long deviceId,
            @RequestBody SnmpBatchCollectRequest request) {
        
        try {
            Device device = deviceService.getDeviceEntityById(deviceId);
            if (device == null) {
                return ApiResponse.error(404, "设备不存在");
            }

            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            List<CollectionResult> results = pluginOpt.get().collectBatch(
                    device, request.getMetricConfigs(), context);
            return ApiResponse.success(results);

        } catch (Exception e) {
            log.error("SNMP批量数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "批量数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 获取SNMP配置模板
     */
    @GetMapping("/config-template")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<Map<String, Object>> getConfigTemplate() {
        try {
            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            Map<String, Object> template = pluginOpt.get().getConfigTemplate();
            return ApiResponse.success(template);

        } catch (Exception e) {
            log.error("获取SNMP配置模板失败", e);
            return ApiResponse.error(500, "获取配置模板异常: " + e.getMessage());
        }
    }

    /**
     * 验证SNMP指标配置
     */
    @PostMapping("/validate-config")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<com.skyeye.collector.plugin.ConfigValidationResult> validateConfig(
            @RequestBody MetricConfig metricConfig) {
        
        try {
            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            com.skyeye.collector.plugin.ConfigValidationResult result = 
                    pluginOpt.get().validateConfig(metricConfig);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("SNMP配置验证失败", e);
            return ApiResponse.error(500, "配置验证异常: " + e.getMessage());
        }
    }

    /**
     * 获取SNMP插件统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<com.skyeye.collector.plugin.PluginStatistics> getStatistics() {
        try {
            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            com.skyeye.collector.plugin.PluginStatistics statistics = pluginOpt.get().getStatistics();
            return ApiResponse.success(statistics);

        } catch (Exception e) {
            log.error("获取SNMP插件统计失败", e);
            return ApiResponse.error(500, "获取统计信息异常: " + e.getMessage());
        }
    }

    /**
     * 获取SNMP插件健康状态
     */
    @GetMapping("/health")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<com.skyeye.collector.plugin.PluginHealthStatus> getHealthStatus() {
        try {
            Optional<SnmpCollectorPlugin> pluginOpt = getSnmpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "SNMP插件未注册");
            }

            com.skyeye.collector.plugin.PluginHealthStatus healthStatus = pluginOpt.get().getHealthStatus();
            return ApiResponse.success(healthStatus);

        } catch (Exception e) {
            log.error("获取SNMP插件健康状态失败", e);
            return ApiResponse.error(500, "获取健康状态异常: " + e.getMessage());
        }
    }

    /**
     * 获取SNMP插件实例
     */
    @SuppressWarnings("unchecked")
    private Optional<SnmpCollectorPlugin> getSnmpPlugin() {
        return pluginRegistry.getPlugin("SNMP")
                .map(plugin -> (SnmpCollectorPlugin) plugin);
    }

    /**
     * SNMP采集请求
     */
    @lombok.Data
    public static class SnmpCollectRequest {
        private MetricConfig metricConfig;
        private Map<String, Object> credentials;
    }

    /**
     * SNMP批量采集请求
     */
    @lombok.Data
    public static class SnmpBatchCollectRequest {
        private List<MetricConfig> metricConfigs;
        private Map<String, Object> credentials;
    }
}
