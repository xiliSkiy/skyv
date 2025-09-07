package com.skyeye.collector.plugin.impl;

import com.jayway.jsonpath.JsonPath;
import com.skyeye.collector.dto.CollectorConfig;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.plugin.AvailableMetric;
import com.skyeye.device.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * HTTP采集插件辅助类
 * 包含HTTP操作的具体实现方法
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
public class HttpCollectorPluginHelper {

    private static final Pattern URL_PATH_PATTERN = Pattern.compile("^/[a-zA-Z0-9/_\\-\\.]*$");
    private static final Set<String> VALID_HTTP_METHODS = Set.of("GET", "POST", "PUT", "DELETE", "HEAD", "OPTIONS");
    private static final Set<String> VALID_RESPONSE_FORMATS = Set.of("json", "xml", "text", "html");

    /**
     * 初始化HTTP客户端
     */
    public void initializeHttpClients(CollectorConfig config) {
        log.debug("初始化HTTP客户端配置");
        // 这里可以根据配置初始化不同的HTTP客户端
    }

    /**
     * 构建完整的URL
     */
    public String buildUrl(Device device, MetricConfig metricConfig, Map<String, Object> credentials) {
        Map<String, Object> parameters = metricConfig.getParameters();
        if (parameters == null) {
            throw new IllegalArgumentException("缺少HTTP请求参数");
        }

        String protocol = (String) credentials.getOrDefault("protocol", "http");
        Integer port = (Integer) credentials.getOrDefault("port", "https".equals(protocol) ? 443 : 80);
        String path = (String) parameters.get("path");
        
        if (path == null || path.isEmpty()) {
            throw new IllegalArgumentException("缺少HTTP路径参数");
        }

        // 构建基础URL
        StringBuilder url = new StringBuilder();
        url.append(protocol).append("://").append(device.getIpAddress());
        
        // 添加端口（如果不是默认端口）
        if (!isDefaultPort(protocol, port)) {
            url.append(":").append(port);
        }
        
        // 添加路径
        if (!path.startsWith("/")) {
            url.append("/");
        }
        url.append(path);
        
        // 添加查询参数
        @SuppressWarnings("unchecked")
        Map<String, Object> queryParams = (Map<String, Object>) parameters.get("queryParams");
        if (queryParams != null && !queryParams.isEmpty()) {
            url.append("?");
            StringJoiner joiner = new StringJoiner("&");
            for (Map.Entry<String, Object> entry : queryParams.entrySet()) {
                joiner.add(entry.getKey() + "=" + entry.getValue());
            }
            url.append(joiner.toString());
        }
        
        return url.toString();
    }

    /**
     * 构建测试URL
     */
    public String buildTestUrl(Device device, Map<String, Object> credentials) {
        String protocol = (String) credentials.getOrDefault("protocol", "http");
        Integer port = (Integer) credentials.getOrDefault("port", "https".equals(protocol) ? 443 : 80);
        
        StringBuilder url = new StringBuilder();
        url.append(protocol).append("://").append(device.getIpAddress());
        
        if (!isDefaultPort(protocol, port)) {
            url.append(":").append(port);
        }
        
        // 使用根路径进行测试
        url.append("/");
        
        return url.toString();
    }

    /**
     * 构建HTTP请求头
     */
    public HttpHeaders buildHeaders(Map<String, Object> credentials, MetricConfig metricConfig) {
        HttpHeaders headers = new HttpHeaders();
        
        // 设置默认请求头
        headers.set(HttpHeaders.USER_AGENT, "SkyEye-Monitor/1.0");
        headers.set(HttpHeaders.ACCEPT, "application/json, application/xml, text/plain, */*");
        
        // 添加自定义请求头
        if (metricConfig != null && metricConfig.getParameters() != null) {
            @SuppressWarnings("unchecked")
            Map<String, String> customHeaders = (Map<String, String>) metricConfig.getParameters().get("headers");
            if (customHeaders != null) {
                customHeaders.forEach(headers::set);
            }
        }
        
        // 处理认证
        if (credentials != null) {
            @SuppressWarnings("unchecked")
            Map<String, Object> authConfig = (Map<String, Object>) credentials.get("auth");
            if (authConfig != null) {
                addAuthentication(headers, authConfig);
            }
        }
        
        return headers;
    }

    /**
     * 获取HTTP方法
     */
    public HttpMethod getHttpMethod(MetricConfig metricConfig) {
        String method = "GET";
        if (metricConfig.getParameters() != null) {
            method = (String) metricConfig.getParameters().getOrDefault("method", "GET");
        }
        return HttpMethod.valueOf(method.toUpperCase());
    }

    /**
     * 构建请求体
     */
    public String buildRequestBody(MetricConfig metricConfig) {
        if (metricConfig.getParameters() == null) {
            return null;
        }
        
        return (String) metricConfig.getParameters().get("requestBody");
    }

    /**
     * 解析HTTP响应
     */
    public Map<String, Object> parseResponse(ResponseEntity<String> response, 
                                           MetricConfig metricConfig, 
                                           long responseTime) {
        Map<String, Object> metrics = new HashMap<>();
        
        // 基础指标
        metrics.put("httpStatusCode", response.getStatusCode().value());
        metrics.put("responseTime", responseTime);
        metrics.put("contentLength", response.getHeaders().getContentLength());
        metrics.put("contentType", response.getHeaders().getContentType());
        
        String responseBody = response.getBody();
        if (responseBody != null && !responseBody.isEmpty()) {
            metrics.put("hasContent", true);
            
            // 根据指标类型解析响应
            String metricType = metricConfig.getMetricType();
            Map<String, Object> parameters = metricConfig.getParameters();
            
            switch (metricType) {
                case "health_check":
                    parseHealthCheck(metrics, responseBody, parameters);
                    break;
                case "api_response":
                    parseApiResponse(metrics, responseBody, parameters);
                    break;
                case "json_path":
                    parseJsonPath(metrics, responseBody, parameters);
                    break;
                case "xml_path":
                    parseXmlPath(metrics, responseBody, parameters);
                    break;
                case "regex_extract":
                    parseRegexExtract(metrics, responseBody, parameters);
                    break;
                case "response_time":
                    // 响应时间已经记录
                    break;
                case "status_code":
                    // 状态码已经记录
                    break;
                case "custom_endpoint":
                    parseCustomEndpoint(metrics, responseBody, parameters);
                    break;
            }
        } else {
            metrics.put("hasContent", false);
        }
        
        return metrics;
    }

    /**
     * 解析健康检查响应
     */
    private void parseHealthCheck(Map<String, Object> metrics, String responseBody, Map<String, Object> parameters) {
        try {
            // 尝试解析JSON格式的健康检查响应
            if (responseBody.trim().startsWith("{")) {
                Object statusValue = JsonPath.read(responseBody, "$.status");
                metrics.put("healthStatus", statusValue);
                
                // 检查是否健康
                boolean isHealthy = "UP".equalsIgnoreCase(String.valueOf(statusValue)) ||
                                  "OK".equalsIgnoreCase(String.valueOf(statusValue)) ||
                                  "HEALTHY".equalsIgnoreCase(String.valueOf(statusValue));
                metrics.put("isHealthy", isHealthy);
                
                // 尝试获取更多详细信息
                try {
                    Object details = JsonPath.read(responseBody, "$.details");
                    metrics.put("healthDetails", details);
                } catch (Exception e) {
                    // 忽略details字段不存在的错误
                }
            } else {
                // 简单文本响应
                boolean isHealthy = responseBody.toLowerCase().contains("ok") ||
                                  responseBody.toLowerCase().contains("up") ||
                                  responseBody.toLowerCase().contains("healthy");
                metrics.put("isHealthy", isHealthy);
                metrics.put("healthStatus", responseBody.trim());
            }
        } catch (Exception e) {
            log.warn("解析健康检查响应失败", e);
            metrics.put("parseError", e.getMessage());
        }
    }

    /**
     * 解析API响应
     */
    private void parseApiResponse(Map<String, Object> metrics, String responseBody, Map<String, Object> parameters) {
        try {
            if (responseBody.trim().startsWith("{") || responseBody.trim().startsWith("[")) {
                // JSON响应
                metrics.put("responseFormat", "json");
                
                // 尝试解析为JSON对象
                Object jsonData = JsonPath.parse(responseBody).json();
                metrics.put("jsonData", jsonData);
                
                // 如果指定了特定字段，则提取
                String extractField = (String) parameters.get("extractField");
                if (extractField != null && !extractField.isEmpty()) {
                    Object fieldValue = JsonPath.read(responseBody, extractField);
                    metrics.put("extractedValue", fieldValue);
                }
            } else if (responseBody.trim().startsWith("<")) {
                // XML响应
                metrics.put("responseFormat", "xml");
                // XML解析逻辑可以在这里实现
            } else {
                // 文本响应
                metrics.put("responseFormat", "text");
                metrics.put("textContent", responseBody);
            }
        } catch (Exception e) {
            log.warn("解析API响应失败", e);
            metrics.put("parseError", e.getMessage());
        }
    }

    /**
     * 解析JSON路径
     */
    private void parseJsonPath(Map<String, Object> metrics, String responseBody, Map<String, Object> parameters) {
        try {
            String jsonPath = (String) parameters.get("jsonPath");
            if (jsonPath == null || jsonPath.isEmpty()) {
                jsonPath = (String) parameters.get("extractPath"); // 兼容性
            }
            
            if (jsonPath != null && !jsonPath.isEmpty()) {
                Object value = JsonPath.read(responseBody, jsonPath);
                metrics.put("extractedValue", value);
                metrics.put("jsonPath", jsonPath);
            }
        } catch (Exception e) {
            log.warn("JSON路径解析失败: {}", e.getMessage());
            metrics.put("parseError", e.getMessage());
        }
    }

    /**
     * 解析XML路径
     */
    private void parseXmlPath(Map<String, Object> metrics, String responseBody, Map<String, Object> parameters) {
        try {
            String xpathExpr = (String) parameters.get("xpath");
            if (xpathExpr != null && !xpathExpr.isEmpty()) {
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(responseBody.getBytes()));
                
                XPath xpath = XPathFactory.newInstance().newXPath();
                NodeList nodes = (NodeList) xpath.compile(xpathExpr).evaluate(doc, XPathConstants.NODESET);
                
                List<String> values = new ArrayList<>();
                for (int i = 0; i < nodes.getLength(); i++) {
                    values.add(nodes.item(i).getTextContent());
                }
                
                metrics.put("extractedValues", values);
                metrics.put("xpath", xpathExpr);
            }
        } catch (Exception e) {
            log.warn("XML路径解析失败: {}", e.getMessage());
            metrics.put("parseError", e.getMessage());
        }
    }

    /**
     * 解析正则表达式提取
     */
    private void parseRegexExtract(Map<String, Object> metrics, String responseBody, Map<String, Object> parameters) {
        try {
            String regex = (String) parameters.get("regex");
            if (regex != null && !regex.isEmpty()) {
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(responseBody);
                
                List<String> matches = new ArrayList<>();
                while (matcher.find()) {
                    if (matcher.groupCount() > 0) {
                        matches.add(matcher.group(1)); // 第一个捕获组
                    } else {
                        matches.add(matcher.group(0)); // 整个匹配
                    }
                }
                
                metrics.put("extractedValues", matches);
                metrics.put("matchCount", matches.size());
                metrics.put("regex", regex);
            }
        } catch (Exception e) {
            log.warn("正则表达式解析失败: {}", e.getMessage());
            metrics.put("parseError", e.getMessage());
        }
    }

    /**
     * 解析自定义端点
     */
    private void parseCustomEndpoint(Map<String, Object> metrics, String responseBody, Map<String, Object> parameters) {
        // 自定义端点可以结合多种解析方式
        parseApiResponse(metrics, responseBody, parameters);
        
        // 检查期望值
        Object expectedValue = parameters.get("expectedValue");
        if (expectedValue != null) {
            Object extractedValue = metrics.get("extractedValue");
            boolean matches = Objects.equals(String.valueOf(expectedValue), String.valueOf(extractedValue));
            metrics.put("valueMatches", matches);
            metrics.put("expectedValue", expectedValue);
        }
    }

    /**
     * 添加认证信息到请求头
     */
    private void addAuthentication(HttpHeaders headers, Map<String, Object> authConfig) {
        String authType = (String) authConfig.get("type");
        if (authType == null || "none".equals(authType)) {
            return;
        }
        
        switch (authType.toLowerCase()) {
            case "basic":
                String username = (String) authConfig.get("username");
                String password = (String) authConfig.get("password");
                if (username != null && password != null) {
                    String auth = username + ":" + password;
                    String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());
                    headers.set(HttpHeaders.AUTHORIZATION, "Basic " + encodedAuth);
                }
                break;
                
            case "bearer":
                String token = (String) authConfig.get("token");
                if (token != null) {
                    headers.set(HttpHeaders.AUTHORIZATION, "Bearer " + token);
                }
                break;
                
            case "apikey":
                String apiKey = (String) authConfig.get("apiKey");
                String apiKeyHeader = (String) authConfig.getOrDefault("apiKeyHeader", "X-API-KEY");
                if (apiKey != null) {
                    headers.set(apiKeyHeader, apiKey);
                }
                break;
                
            default:
                log.warn("不支持的认证类型: {}", authType);
        }
    }

    /**
     * 测试端点是否可访问
     */
    public boolean testEndpoint(Device device, Map<String, Object> credentials, String path) {
        try {
            String protocol = (String) credentials.getOrDefault("protocol", "http");
            Integer port = (Integer) credentials.getOrDefault("port", "https".equals(protocol) ? 443 : 80);
            
            StringBuilder url = new StringBuilder();
            url.append(protocol).append("://").append(device.getIpAddress());
            
            if (!isDefaultPort(protocol, port)) {
                url.append(":").append(port);
            }
            
            url.append(path);
            
            RestTemplate testTemplate = new RestTemplate();
            HttpHeaders headers = buildHeaders(credentials, null);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            ResponseEntity<String> response = testTemplate.exchange(
                    url.toString(), HttpMethod.HEAD, entity, String.class);
            
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算数据质量评分
     */
    public int calculateQualityScore(ResponseEntity<String> response, Map<String, Object> metrics) {
        int score = 100;
        
        // 根据HTTP状态码评分
        HttpStatusCode statusCode = response.getStatusCode();
        HttpStatus status = HttpStatus.valueOf(statusCode.value());
        if (status.is2xxSuccessful()) {
            // 2xx 状态码，保持满分
        } else if (status.is3xxRedirection()) {
            score -= 10; // 重定向扣10分
        } else if (status.is4xxClientError()) {
            score -= 30; // 客户端错误扣30分
        } else if (status.is5xxServerError()) {
            score -= 50; // 服务器错误扣50分
        }
        
        // 根据响应时间评分
        Long responseTime = (Long) metrics.get("responseTime");
        if (responseTime != null) {
            if (responseTime > 10000) { // 超过10秒
                score -= 30;
            } else if (responseTime > 5000) { // 超过5秒
                score -= 20;
            } else if (responseTime > 2000) { // 超过2秒
                score -= 10;
            }
        }
        
        // 根据内容可用性评分
        Boolean hasContent = (Boolean) metrics.get("hasContent");
        if (hasContent != null && !hasContent) {
            score -= 20; // 无内容扣20分
        }
        
        // 根据解析错误评分
        if (metrics.containsKey("parseError")) {
            score -= 25; // 解析错误扣25分
        }
        
        return Math.max(score, 0);
    }

    /**
     * 确定错误代码
     */
    public String determineErrorCode(Exception e) {
        String message = e.getMessage();
        if (message == null) {
            return "UNKNOWN_ERROR";
        }
        
        if (message.contains("timeout")) {
            return "TIMEOUT_ERROR";
        } else if (message.contains("connection")) {
            return "CONNECTION_ERROR";
        } else if (message.contains("authentication") || message.contains("401")) {
            return "AUTH_ERROR";
        } else if (message.contains("404")) {
            return "NOT_FOUND_ERROR";
        } else if (message.contains("500")) {
            return "SERVER_ERROR";
        } else {
            return "HTTP_ERROR";
        }
    }

    /**
     * 验证HTTP路径格式
     */
    public boolean isValidPath(String path) {
        return path != null && (path.equals("/") || URL_PATH_PATTERN.matcher(path).matches());
    }

    /**
     * 验证HTTP方法
     */
    public boolean isValidHttpMethod(String method) {
        return method != null && VALID_HTTP_METHODS.contains(method.toUpperCase());
    }

    /**
     * 验证响应格式
     */
    public boolean isValidResponseFormat(String format) {
        return format != null && VALID_RESPONSE_FORMATS.contains(format.toLowerCase());
    }

    /**
     * 检查是否为默认端口
     */
    private boolean isDefaultPort(String protocol, Integer port) {
        if ("http".equals(protocol) && port == 80) {
            return true;
        }
        if ("https".equals(protocol) && port == 443) {
            return true;
        }
        return false;
    }

    /**
     * 创建健康检查指标
     */
    public AvailableMetric createHealthCheckMetric() {
        return AvailableMetric.builder()
                .name("health_check")
                .displayName("健康检查")
                .description("检查服务健康状态")
                .type("health")
                .dataType(AvailableMetric.DataType.OBJECT)
                .category("health")
                .core(true)
                .complexity(1)
                .recommendedInterval(60)
                .build();
    }

    /**
     * 创建指标端点指标
     */
    public AvailableMetric createMetricsEndpointMetric() {
        return AvailableMetric.builder()
                .name("metrics_endpoint")
                .displayName("指标端点")
                .description("获取应用程序指标")
                .type("metrics")
                .dataType(AvailableMetric.DataType.OBJECT)
                .category("performance")
                .core(false)
                .complexity(2)
                .recommendedInterval(60)
                .build();
    }

    /**
     * 创建状态端点指标
     */
    public AvailableMetric createStatusEndpointMetric() {
        return AvailableMetric.builder()
                .name("status_endpoint")
                .displayName("状态端点")
                .description("获取服务状态信息")
                .type("status")
                .dataType(AvailableMetric.DataType.OBJECT)
                .category("status")
                .core(true)
                .complexity(1)
                .recommendedInterval(120)
                .build();
    }

    /**
     * 创建API信息指标
     */
    public AvailableMetric createApiInfoMetric() {
        return AvailableMetric.builder()
                .name("api_info")
                .displayName("API信息")
                .description("获取API版本和信息")
                .type("info")
                .dataType(AvailableMetric.DataType.OBJECT)
                .category("info")
                .core(false)
                .complexity(1)
                .recommendedInterval(300)
                .build();
    }

    /**
     * 创建响应时间指标
     */
    public AvailableMetric createResponseTimeMetric() {
        return AvailableMetric.builder()
                .name("response_time")
                .displayName("响应时间")
                .description("HTTP请求响应时间")
                .type("performance")
                .dataType(AvailableMetric.DataType.INTEGER)
                .unit("ms")
                .category("performance")
                .core(true)
                .complexity(1)
                .recommendedInterval(60)
                .build();
    }

    /**
     * 创建状态码指标
     */
    public AvailableMetric createStatusCodeMetric() {
        return AvailableMetric.builder()
                .name("status_code")
                .displayName("HTTP状态码")
                .description("HTTP响应状态码")
                .type("status")
                .dataType(AvailableMetric.DataType.INTEGER)
                .category("status")
                .core(true)
                .complexity(1)
                .recommendedInterval(60)
                .build();
    }
}
