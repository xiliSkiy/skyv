package com.skyeye.collector.plugin.impl;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.CollectorConfig;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.plugin.*;
import com.skyeye.device.entity.Device;
import com.skyeye.device.entity.DeviceType;
import com.skyeye.device.service.DeviceCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * HTTP协议数据采集插件
 * 支持REST API、Web服务、HTTP端点的数据采集
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class HttpCollectorPlugin extends AbstractCollectorPlugin {

    private final DeviceCredentialService credentialService;
    private final HttpCollectorPluginHelper helper;
    private final RestTemplate restTemplate;

    /**
     * HTTP客户端缓存
     */
    private final Map<String, RestTemplate> clientCache = new ConcurrentHashMap<>();

    @Override
    public String getPluginType() {
        return "HTTP";
    }

    @Override
    public String getPluginName() {
        return "HTTP数据采集插件";
    }

    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }

    @Override
    public String getPluginDescription() {
        return "支持HTTP/HTTPS协议的数据采集插件，可采集REST API、Web服务、监控端点等数据";
    }

    @Override
    public boolean supports(DeviceType deviceType) {
        if (deviceType.getProtocols() == null) {
            return false;
        }
        return deviceType.getProtocols().contains("HTTP") || 
               deviceType.getProtocols().contains("HTTPS");
    }

    @Override
    public boolean supportsProtocol(String protocol) {
        return "HTTP".equalsIgnoreCase(protocol) || "HTTPS".equalsIgnoreCase(protocol);
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("HTTP", "HTTPS");
    }

    @Override
    public List<String> getSupportedMetricTypes() {
        return Arrays.asList(
                "health_check",     // 健康检查
                "api_response",     // API响应数据
                "json_path",        // JSON路径提取
                "xml_path",         // XML路径提取
                "regex_extract",    // 正则表达式提取
                "response_time",    // 响应时间
                "status_code",      // HTTP状态码
                "custom_endpoint"   // 自定义端点
        );
    }

    @Override
    protected void doInitialize(CollectorConfig config) throws PluginException {
        try {
            log.info("初始化HTTP采集插件");
            
            // 初始化HTTP客户端配置
            helper.initializeHttpClients(config);
            
            log.info("HTTP采集插件初始化完成");
        } catch (Exception e) {
            throw new PluginException("HTTP_INIT_FAILED", "HTTP插件初始化失败", e);
        }
    }

    @Override
    protected void doDestroy() {
        log.info("销毁HTTP采集插件");
        
        // 清理HTTP客户端缓存
        clientCache.clear();
        
        log.info("HTTP采集插件销毁完成");
    }

    @Override
    protected CollectionResult doCollect(Device device, MetricConfig metricConfig, CollectionContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        long requestStartTime = System.currentTimeMillis();
        
        try {
            // 1. 获取设备HTTP凭据
            Map<String, Object> credentials = getDeviceCredentials(device, context);
            
            // 2. 构建HTTP请求
            String url = helper.buildUrl(device, metricConfig, credentials);
            HttpHeaders headers = helper.buildHeaders(credentials, metricConfig);
            HttpMethod method = helper.getHttpMethod(metricConfig);
            String requestBody = helper.buildRequestBody(metricConfig);
            
            HttpEntity<String> entity = new HttpEntity<>(requestBody, headers);
            
            // 3. 执行HTTP请求
            ResponseEntity<String> response = restTemplate.exchange(url, method, entity, String.class);
            
            long responseTime = System.currentTimeMillis() - requestStartTime;
            
            // 4. 解析响应数据
            Map<String, Object> metrics = helper.parseResponse(response, metricConfig, responseTime);
            
            return CollectionResult.builder()
                    .success(true)
                    .metrics(metrics)
                    .timestamp(System.currentTimeMillis())
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .qualityScore(helper.calculateQualityScore(response, metrics))
                    .pluginType(getPluginType())
                    .deviceId(device.getId())
                    .metricName(metricConfig.getMetricName())
                    .sessionId(context.getSessionId())
                    .build();
                    
        } catch (Exception e) {
            log.error("HTTP采集失败: device={}, metric={}", 
                    device.getId(), metricConfig.getMetricName(), e);
            
            return CollectionResult.builder()
                    .success(false)
                    .errorMessage("HTTP采集失败: " + e.getMessage())
                    .errorCode(helper.determineErrorCode(e))
                    .timestamp(System.currentTimeMillis())
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .pluginType(getPluginType())
                    .deviceId(device.getId())
                    .metricName(metricConfig.getMetricName())
                    .sessionId(context.getSessionId())
                    .build();
        }
    }

    @Override
    protected ConnectionTestResult doTestConnection(Device device, CollectionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取设备凭据
            Map<String, Object> credentials = getDeviceCredentials(device, context);
            
            // 构建测试URL
            String testUrl = helper.buildTestUrl(device, credentials);
            HttpHeaders headers = helper.buildHeaders(credentials, null);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // 发送HEAD请求测试连接
            ResponseEntity<String> response = restTemplate.exchange(
                    testUrl, HttpMethod.HEAD, entity, String.class);
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.getStatusCode().is2xxSuccessful()) {
                return ConnectionTestResult.success("HTTP连接测试成功", responseTime);
            } else {
                return ConnectionTestResult.failure("HTTP_ERROR", 
                        "HTTP响应错误: " + response.getStatusCode());
            }
            
        } catch (Exception e) {
            log.error("HTTP连接测试失败: device={}", device.getId(), e);
            return ConnectionTestResult.failure("CONNECTION_FAILED", 
                    "HTTP连接测试失败: " + e.getMessage());
        }
    }

    @Override
    protected List<AvailableMetric> doDiscoverMetrics(Device device, CollectionContext context) {
        List<AvailableMetric> metrics = new ArrayList<>();
        
        try {
            Map<String, Object> credentials = getDeviceCredentials(device, context);
            
            // 尝试发现常见的监控端点
            if (helper.testEndpoint(device, credentials, "/health")) {
                metrics.add(helper.createHealthCheckMetric());
            }
            
            if (helper.testEndpoint(device, credentials, "/metrics")) {
                metrics.add(helper.createMetricsEndpointMetric());
            }
            
            if (helper.testEndpoint(device, credentials, "/status")) {
                metrics.add(helper.createStatusEndpointMetric());
            }
            
            if (helper.testEndpoint(device, credentials, "/api/v1/info")) {
                metrics.add(helper.createApiInfoMetric());
            }
            
            // 总是提供基础的响应时间和状态码指标
            metrics.add(helper.createResponseTimeMetric());
            metrics.add(helper.createStatusCodeMetric());
            
        } catch (Exception e) {
            log.error("HTTP指标发现失败: device={}", device.getId(), e);
        }
        
        return metrics;
    }

    @Override
    protected ConfigValidationResult doValidateConfig(MetricConfig metricConfig) {
        List<ConfigValidationResult.ValidationError> errors = new ArrayList<>();
        List<ConfigValidationResult.ValidationWarning> warnings = new ArrayList<>();
        
        Map<String, Object> parameters = metricConfig.getParameters();
        if (parameters == null) {
            errors.add(ConfigValidationResult.ValidationError.builder()
                    .field("parameters")
                    .errorCode("MISSING_PARAMETERS")
                    .message("HTTP采集参数不能为空")
                    .severity(ConfigValidationResult.ValidationError.Severity.HIGH)
                    .build());
        } else {
            // 验证URL路径
            String path = (String) parameters.get("path");
            if (path == null || path.trim().isEmpty()) {
                errors.add(ConfigValidationResult.ValidationError.builder()
                        .field("path")
                        .errorCode("MISSING_PATH")
                        .message("HTTP路径不能为空")
                        .severity(ConfigValidationResult.ValidationError.Severity.HIGH)
                        .build());
            } else if (!helper.isValidPath(path)) {
                errors.add(ConfigValidationResult.ValidationError.builder()
                        .field("path")
                        .errorCode("INVALID_PATH")
                        .message("HTTP路径格式无效")
                        .currentValue(path)
                        .severity(ConfigValidationResult.ValidationError.Severity.HIGH)
                        .build());
            }
            
            // 验证HTTP方法
            String method = (String) parameters.getOrDefault("method", "GET");
            if (!helper.isValidHttpMethod(method)) {
                errors.add(ConfigValidationResult.ValidationError.builder()
                        .field("method")
                        .errorCode("INVALID_METHOD")
                        .message("不支持的HTTP方法")
                        .currentValue(method)
                        .severity(ConfigValidationResult.ValidationError.Severity.MEDIUM)
                        .build());
            }
            
            // 验证响应格式
            String responseFormat = (String) parameters.getOrDefault("responseFormat", "json");
            if (!helper.isValidResponseFormat(responseFormat)) {
                warnings.add(ConfigValidationResult.ValidationWarning.builder()
                        .field("responseFormat")
                        .warningCode("UNSUPPORTED_FORMAT")
                        .message("响应格式可能不被完全支持")
                        .currentValue(responseFormat)
                        .suggestedValue("json")
                        .build());
            }
            
            // 验证超时时间
            if (metricConfig.getTimeout() <= 0) {
                warnings.add(ConfigValidationResult.ValidationWarning.builder()
                        .field("timeout")
                        .warningCode("INVALID_TIMEOUT")
                        .message("超时时间应大于0")
                        .currentValue(metricConfig.getTimeout())
                        .suggestedValue(30)
                        .build());
            }
        }
        
        boolean valid = errors.isEmpty();
        String message = valid ? "HTTP配置验证通过" : "HTTP配置验证失败";
        
        return ConfigValidationResult.builder()
                .valid(valid)
                .message(message)
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    @Override
    public Map<String, Object> getConfigTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("pluginType", "HTTP");
        template.put("timeout", 30);
        template.put("retryTimes", 3);
        template.put("retryInterval", 1000);
        
        // HTTP特定配置
        Map<String, Object> httpConfig = new HashMap<>();
        httpConfig.put("protocol", "http");  // http, https
        httpConfig.put("port", 80);
        httpConfig.put("connectTimeout", 5000);
        httpConfig.put("readTimeout", 30000);
        httpConfig.put("followRedirects", true);
        httpConfig.put("maxRedirects", 5);
        
        // 认证配置
        Map<String, Object> authConfig = new HashMap<>();
        authConfig.put("type", "none");  // none, basic, bearer, apikey
        authConfig.put("username", "");
        authConfig.put("password", "");
        authConfig.put("token", "");
        authConfig.put("apiKey", "");
        authConfig.put("apiKeyHeader", "X-API-KEY");
        httpConfig.put("auth", authConfig);
        
        // SSL配置
        Map<String, Object> sslConfig = new HashMap<>();
        sslConfig.put("verifySSL", true);
        sslConfig.put("trustAllCerts", false);
        sslConfig.put("keyStore", "");
        sslConfig.put("keyStorePassword", "");
        httpConfig.put("ssl", sslConfig);
        
        template.put("httpConfig", httpConfig);
        
        // 指标配置示例
        Map<String, Object> metricExample = new HashMap<>();
        metricExample.put("path", "/api/v1/status");
        metricExample.put("method", "GET");
        metricExample.put("responseFormat", "json");
        metricExample.put("extractPath", "$.status");
        metricExample.put("expectedValue", "ok");
        template.put("metricExample", metricExample);
        
        return template;
    }

    /**
     * 获取设备HTTP凭据
     */
    private Map<String, Object> getDeviceCredentials(Device device, CollectionContext context) {
        try {
            Map<String, Object> credentials = context.getCredentials();
            if (credentials != null && !credentials.isEmpty()) {
                return credentials;
            }
            
            // 从凭据服务获取
            return credentialService.getDefaultCredential(device.getId(), "HTTP");
        } catch (Exception e) {
            log.warn("获取HTTP凭据失败，使用默认配置: device={}", device.getId());
            
            // 返回默认凭据
            Map<String, Object> defaultCredentials = new HashMap<>();
            defaultCredentials.put("protocol", "http");
            defaultCredentials.put("port", 80);
            defaultCredentials.put("auth", Map.of("type", "none"));
            return defaultCredentials;
        }
    }
}
