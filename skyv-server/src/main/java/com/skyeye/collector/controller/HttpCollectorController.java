package com.skyeye.collector.controller;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.plugin.AvailableMetric;
import com.skyeye.collector.plugin.ConnectionTestResult;
import com.skyeye.collector.plugin.impl.HttpCollectorPlugin;
import com.skyeye.collector.registry.PluginRegistry;
import com.skyeye.common.response.ApiResponse;
import com.skyeye.device.dto.DeviceDto;
import com.skyeye.device.entity.Device;
import com.skyeye.device.entity.DeviceType;
import com.skyeye.device.service.DeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * HTTP采集插件控制器
 * 
 * @author SkyEye Team
 */
@Slf4j
@RestController
@RequestMapping("/api/collector/http")
@RequiredArgsConstructor
@PreAuthorize("hasRole('USER')")
public class HttpCollectorController {

    private final PluginRegistry pluginRegistry;
    private final DeviceService deviceService;

    /**
     * 测试HTTP连接
     */
    @PostMapping("/test-connection")
    @PreAuthorize("hasPermission('device', 'test')")
    public ApiResponse<ConnectionTestResult> testConnection(
            @RequestParam Long deviceId,
            @RequestBody(required = false) Map<String, Object> credentials) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            Device device = convertToDevice(deviceDto);

            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            CollectionContext context = CollectionContext.createForTest();
            if (credentials != null && !credentials.isEmpty()) {
                context.setCredentials(credentials);
            }

            ConnectionTestResult result = pluginOpt.get().testConnection(device, context);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("HTTP连接测试失败: deviceId={}", deviceId, e);
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
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            Device device = convertToDevice(deviceDto);

            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            CollectionContext context = CollectionContext.createDefault();
            if (credentials != null && !credentials.isEmpty()) {
                context.setCredentials(credentials);
            }

            List<AvailableMetric> metrics = pluginOpt.get().discoverMetrics(device, context);
            return ApiResponse.success(metrics);

        } catch (Exception e) {
            log.error("HTTP指标发现失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "指标发现异常: " + e.getMessage());
        }
    }

    /**
     * 执行HTTP数据采集
     */
    @PostMapping("/collect")
    @PreAuthorize("hasPermission('device', 'collect')")
    public ApiResponse<CollectionResult> collectData(
            @RequestParam Long deviceId,
            @RequestBody HttpCollectRequest request) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            Device device = convertToDevice(deviceDto);

            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            CollectionResult result = pluginOpt.get().collect(device, request.getMetricConfig(), context);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("HTTP数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 批量执行HTTP数据采集
     */
    @PostMapping("/collect-batch")
    @PreAuthorize("hasPermission('device', 'collect')")
    public ApiResponse<List<CollectionResult>> collectBatchData(
            @RequestParam Long deviceId,
            @RequestBody HttpBatchCollectRequest request) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            Device device = convertToDevice(deviceDto);

            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            CollectionContext context = CollectionContext.createDefault();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            List<CollectionResult> results = pluginOpt.get().collectBatch(
                    device, request.getMetricConfigs(), context);
            return ApiResponse.success(results);

        } catch (Exception e) {
            log.error("HTTP批量数据采集失败: deviceId={}", deviceId, e);
            return ApiResponse.error(500, "批量数据采集异常: " + e.getMessage());
        }
    }

    /**
     * 测试特定HTTP端点
     */
    @PostMapping("/test-endpoint")
    @PreAuthorize("hasPermission('device', 'test')")
    public ApiResponse<EndpointTestResult> testEndpoint(
            @RequestParam Long deviceId,
            @RequestBody EndpointTestRequest request) {
        
        try {
            DeviceDto deviceDto = deviceService.getDeviceById(deviceId);
            if (deviceDto == null) {
                return ApiResponse.error(404, "设备不存在");
            }
            Device device = convertToDevice(deviceDto);

            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            // 构建测试指标配置
            MetricConfig testConfig = MetricConfig.builder()
                    .metricName("endpoint_test")
                    .metricType("custom_endpoint")
                    .parameters(Map.of(
                            "path", request.getPath(),
                            "method", request.getMethod(),
                            "headers", request.getHeaders() != null ? request.getHeaders() : Map.of()
                    ))
                    .timeout(30)
                    .build();

            CollectionContext context = CollectionContext.createForTest();
            if (request.getCredentials() != null && !request.getCredentials().isEmpty()) {
                context.setCredentials(request.getCredentials());
            }

            CollectionResult result = pluginOpt.get().collect(device, testConfig, context);
            
            EndpointTestResult testResult = EndpointTestResult.builder()
                    .success(result.isSuccess())
                    .statusCode((Integer) result.getMetrics().get("httpStatusCode"))
                    .responseTime((Long) result.getMetrics().get("responseTime"))
                    .hasContent((Boolean) result.getMetrics().get("hasContent"))
                    .contentType((String) result.getMetrics().get("contentType"))
                    .errorMessage(result.getErrorMessage())
                    .build();

            return ApiResponse.success(testResult);

        } catch (Exception e) {
            log.error("HTTP端点测试失败: deviceId={}, path={}", deviceId, request.getPath(), e);
            return ApiResponse.error(500, "端点测试异常: " + e.getMessage());
        }
    }

    /**
     * 获取HTTP配置模板
     */
    @GetMapping("/config-template")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<Map<String, Object>> getConfigTemplate() {
        try {
            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            Map<String, Object> template = pluginOpt.get().getConfigTemplate();
            return ApiResponse.success(template);

        } catch (Exception e) {
            log.error("获取HTTP配置模板失败", e);
            return ApiResponse.error(500, "获取配置模板异常: " + e.getMessage());
        }
    }

    /**
     * 验证HTTP指标配置
     */
    @PostMapping("/validate-config")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<com.skyeye.collector.plugin.ConfigValidationResult> validateConfig(
            @RequestBody MetricConfig metricConfig) {
        
        try {
            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            com.skyeye.collector.plugin.ConfigValidationResult result = 
                    pluginOpt.get().validateConfig(metricConfig);
            return ApiResponse.success(result);

        } catch (Exception e) {
            log.error("HTTP配置验证失败", e);
            return ApiResponse.error(500, "配置验证异常: " + e.getMessage());
        }
    }

    /**
     * 获取HTTP插件统计信息
     */
    @GetMapping("/statistics")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<com.skyeye.collector.plugin.PluginStatistics> getStatistics() {
        try {
            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            com.skyeye.collector.plugin.PluginStatistics statistics = pluginOpt.get().getStatistics();
            return ApiResponse.success(statistics);

        } catch (Exception e) {
            log.error("获取HTTP插件统计失败", e);
            return ApiResponse.error(500, "获取统计信息异常: " + e.getMessage());
        }
    }

    /**
     * 获取HTTP插件健康状态
     */
    @GetMapping("/health")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<com.skyeye.collector.plugin.PluginHealthStatus> getHealthStatus() {
        try {
            Optional<HttpCollectorPlugin> pluginOpt = getHttpPlugin();
            if (!pluginOpt.isPresent()) {
                return ApiResponse.error(500, "HTTP插件未注册");
            }

            com.skyeye.collector.plugin.PluginHealthStatus healthStatus = pluginOpt.get().getHealthStatus();
            return ApiResponse.success(healthStatus);

        } catch (Exception e) {
            log.error("获取HTTP插件健康状态失败", e);
            return ApiResponse.error(500, "获取健康状态异常: " + e.getMessage());
        }
    }

    /**
     * 获取支持的认证类型
     */
    @GetMapping("/auth-types")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<List<String>> getSupportedAuthTypes() {
        try {
            List<String> authTypes = List.of("none", "basic", "bearer", "apikey");
            return ApiResponse.success(authTypes);

        } catch (Exception e) {
            log.error("获取支持的认证类型失败", e);
            return ApiResponse.error(500, "获取认证类型异常: " + e.getMessage());
        }
    }

    /**
     * 获取支持的HTTP方法
     */
    @GetMapping("/http-methods")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<List<String>> getSupportedHttpMethods() {
        try {
            List<String> methods = List.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS");
            return ApiResponse.success(methods);

        } catch (Exception e) {
            log.error("获取支持的HTTP方法失败", e);
            return ApiResponse.error(500, "获取HTTP方法异常: " + e.getMessage());
        }
    }

    /**
     * 获取HTTP插件实例
     */
    private Optional<HttpCollectorPlugin> getHttpPlugin() {
        return pluginRegistry.getPlugin("HTTP")
                .map(plugin -> (HttpCollectorPlugin) plugin);
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
        // 注意：这里需要从数据库查询获取protocols，暂时设置为HTTP
        deviceType.setProtocols(List.of("HTTP"));
        
        // 临时设置deviceType字段（仅用于插件判断）
        device.setDeviceType(deviceType);
        
        return device;
    }

    /**
     * HTTP采集请求
     */
    @lombok.Data
    public static class HttpCollectRequest {
        private MetricConfig metricConfig;
        private Map<String, Object> credentials;
    }

    /**
     * HTTP批量采集请求
     */
    @lombok.Data
    public static class HttpBatchCollectRequest {
        private List<MetricConfig> metricConfigs;
        private Map<String, Object> credentials;
    }

    /**
     * 端点测试请求
     */
    @lombok.Data
    public static class EndpointTestRequest {
        private String path;
        private String method = "GET";
        private Map<String, String> headers;
        private Map<String, Object> credentials;
    }

    /**
     * 端点测试结果
     */
    @lombok.Data
    @lombok.Builder
    public static class EndpointTestResult {
        private boolean success;
        private Integer statusCode;
        private Long responseTime;
        private Boolean hasContent;
        private String contentType;
        private String errorMessage;
    }
}
