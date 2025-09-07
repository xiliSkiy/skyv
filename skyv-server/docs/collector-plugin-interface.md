# 数据采集插件接口设计文档

## 概述

本文档描述了SkyEye智能监控系统中数据采集插件的接口设计。采集插件系统采用可扩展的架构，支持多种协议和设备类型的数据采集，提供统一的插件管理和生命周期控制。

## 核心架构

### 插件接口体系

```
CollectorPlugin (核心接口)
├── AbstractCollectorPlugin (抽象基类)
├── SnmpCollectorPlugin (SNMP实现)
├── HttpCollectorPlugin (HTTP实现)
└── SshCollectorPlugin (SSH实现)
```

### 管理组件

- **PluginRegistry**: 插件注册管理器
- **PluginLifecycleManager**: 插件生命周期管理器
- **CollectorEngine**: 采集引擎（后续实现）

## 核心接口

### 1. CollectorPlugin 接口

```java
public interface CollectorPlugin {
    // 基本信息
    String getPluginType();
    String getPluginName();
    String getPluginVersion();
    String getPluginDescription();
    
    // 支持能力
    boolean supports(DeviceType deviceType);
    boolean supportsProtocol(String protocol);
    List<String> getSupportedProtocols();
    List<String> getSupportedMetricTypes();
    
    // 数据采集
    CollectionResult collect(Device device, MetricConfig metricConfig, CollectionContext context);
    List<CollectionResult> collectBatch(Device device, List<MetricConfig> metricConfigs, CollectionContext context);
    
    // 生命周期
    void initialize(CollectorConfig config) throws PluginException;
    void destroy();
    
    // 健康状态
    PluginHealthStatus getHealthStatus();
    PluginStatistics getStatistics();
    
    // 连接测试
    ConnectionTestResult testConnection(Device device, CollectionContext context);
    
    // 指标发现
    List<AvailableMetric> discoverMetrics(Device device, CollectionContext context);
    
    // 配置验证
    ConfigValidationResult validateConfig(MetricConfig metricConfig);
    Map<String, Object> getConfigTemplate();
    
    // 性能特性
    boolean supportsConcurrentCollection();
    int getRecommendedConcurrency();
    int getPriority();
}
```

### 2. 数据传输对象

#### CollectionResult - 采集结果
```java
@Data
@Builder
public class CollectionResult {
    private boolean success;                    // 是否成功
    private Map<String, Object> metrics;        // 采集到的指标数据
    private String errorMessage;               // 错误消息
    private String errorCode;                  // 错误代码
    private long timestamp;                    // 时间戳
    private LocalDateTime startTime;           // 开始时间
    private LocalDateTime endTime;             // 结束时间
    private long duration;                     // 耗时（毫秒）
    private int qualityScore;                  // 数据质量评分
    private String pluginType;                 // 插件类型
    private Long deviceId;                     // 设备ID
    private String metricName;                 // 指标名称
    private CollectionStatus status;           // 采集状态
}
```

#### MetricConfig - 指标配置
```java
@Data
@Builder
public class MetricConfig {
    private String metricName;                 // 指标名称
    private String metricType;                 // 指标类型
    private String displayName;                // 显示名称
    private String description;                // 描述
    private String unit;                       // 单位
    private MetricDataType dataType;           // 数据类型
    private Map<String, Object> parameters;    // 指标参数
    private int timeout;                       // 超时时间
    private int retryTimes;                    // 重试次数
    private long retryInterval;                // 重试间隔
    private boolean cacheEnabled;              // 是否启用缓存
    private int cacheTtl;                      // 缓存TTL
    private int interval;                      // 采集间隔
    private int priority;                      // 优先级
    private boolean enabled;                   // 是否启用
    private Map<String, String> tags;          // 标签
    private DataTransform transform;           // 数据转换规则
    private ThresholdConfig thresholds;        // 阈值配置
}
```

#### CollectionContext - 采集上下文
```java
@Data
@Builder
public class CollectionContext {
    private String sessionId;                  // 会话ID
    private Long taskId;                       // 任务ID
    private Long userId;                       // 用户ID
    private LocalDateTime startTime;           // 开始时间
    private LocalDateTime deadline;            // 超时时间
    private Map<String, Object> credentials;   // 设备凭据
    private Map<String, Object> parameters;    // 采集参数
    private Map<String, Object> variables;     // 共享变量
    private CollectorConfig collectorConfig;   // 采集器配置
    private boolean debugMode;                 // 调试模式
    private boolean testMode;                  // 测试模式
    private int maxRetries;                    // 最大重试次数
    private int currentRetry;                  // 当前重试次数
    private int priority;                      // 优先级
}
```

### 3. 插件管理

#### PluginRegistry - 插件注册表
```java
public interface PluginRegistry {
    void registerPlugin(CollectorPlugin plugin) throws PluginException;
    boolean unregisterPlugin(String pluginType);
    Optional<CollectorPlugin> getPlugin(String pluginType);
    List<CollectorPlugin> getAllPlugins();
    List<CollectorPlugin> getPluginsForDeviceType(DeviceType deviceType);
    List<CollectorPlugin> getPluginsForProtocol(String protocol);
    boolean enablePlugin(String pluginType);
    boolean disablePlugin(String pluginType);
    PluginValidationResult validatePlugin(CollectorPlugin plugin);
}
```

#### PluginLifecycleManager - 生命周期管理器
```java
public interface PluginLifecycleManager {
    void start() throws PluginException;
    void stop() throws PluginException;
    void initializePlugin(CollectorPlugin plugin, CollectorConfig config) throws PluginException;
    void startPlugin(String pluginType) throws PluginException;
    void stopPlugin(String pluginType) throws PluginException;
    void restartPlugin(String pluginType) throws PluginException;
    PluginLifecycleState getPluginState(String pluginType);
    boolean isPluginReady(String pluginType);
    PluginHealthCheckResult performHealthCheck(String pluginType);
}
```

## 插件开发指南

### 1. 基础插件开发

继承 `AbstractCollectorPlugin` 抽象类：

```java
@Component
public class MyCollectorPlugin extends AbstractCollectorPlugin {
    
    @Override
    public String getPluginType() {
        return "MY_PROTOCOL";
    }
    
    @Override
    public String getPluginName() {
        return "我的协议采集插件";
    }
    
    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }
    
    @Override
    public String getPluginDescription() {
        return "支持MY_PROTOCOL协议的数据采集";
    }
    
    @Override
    public boolean supports(DeviceType deviceType) {
        return deviceType.getProtocolTypes().contains("MY_PROTOCOL");
    }
    
    @Override
    public boolean supportsProtocol(String protocol) {
        return "MY_PROTOCOL".equals(protocol);
    }
    
    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("MY_PROTOCOL");
    }
    
    @Override
    public List<String> getSupportedMetricTypes() {
        return Arrays.asList("cpu_usage", "memory_usage", "disk_usage");
    }
    
    @Override
    protected void doInitialize(CollectorConfig config) throws PluginException {
        // 初始化插件资源
        log.info("初始化 {} 插件", getPluginType());
    }
    
    @Override
    protected void doDestroy() {
        // 清理插件资源
        log.info("销毁 {} 插件", getPluginType());
    }
    
    @Override
    protected CollectionResult doCollect(Device device, MetricConfig metricConfig, CollectionContext context) {
        try {
            // 实现具体的数据采集逻辑
            Map<String, Object> metrics = new HashMap<>();
            
            // 根据指标类型采集数据
            switch (metricConfig.getMetricType()) {
                case "cpu_usage":
                    metrics.put("cpu_usage", collectCpuUsage(device, context));
                    break;
                case "memory_usage":
                    metrics.put("memory_usage", collectMemoryUsage(device, context));
                    break;
                default:
                    return CollectionResult.failure("UNSUPPORTED_METRIC", 
                            "不支持的指标类型: " + metricConfig.getMetricType());
            }
            
            return CollectionResult.success(metrics);
            
        } catch (Exception e) {
            return CollectionResult.failure("COLLECTION_ERROR", 
                    "采集失败: " + e.getMessage());
        }
    }
    
    @Override
    protected ConnectionTestResult doTestConnection(Device device, CollectionContext context) {
        // 实现连接测试逻辑
        return ConnectionTestResult.success("连接测试通过", 100);
    }
    
    @Override
    protected List<AvailableMetric> doDiscoverMetrics(Device device, CollectionContext context) {
        // 发现设备可用的指标
        List<AvailableMetric> metrics = new ArrayList<>();
        
        metrics.add(AvailableMetric.builder()
                .name("cpu_usage")
                .displayName("CPU使用率")
                .description("设备CPU使用百分比")
                .type("system")
                .dataType(AvailableMetric.DataType.FLOAT)
                .unit("%")
                .core(true)
                .complexity(1)
                .recommendedInterval(60)
                .build());
        
        return metrics;
    }
    
    // 具体的采集方法
    private double collectCpuUsage(Device device, CollectionContext context) {
        // 实现CPU使用率采集
        return 0.0;
    }
    
    private double collectMemoryUsage(Device device, CollectionContext context) {
        // 实现内存使用率采集
        return 0.0;
    }
}
```

### 2. 插件注册

插件会通过Spring的自动装配机制自动注册：

```java
@Configuration
public class PluginAutoConfiguration {
    
    @Autowired
    private PluginRegistry pluginRegistry;
    
    @Autowired
    private List<CollectorPlugin> plugins;
    
    @PostConstruct
    public void registerPlugins() {
        for (CollectorPlugin plugin : plugins) {
            try {
                pluginRegistry.registerPlugin(plugin);
            } catch (PluginException e) {
                log.error("注册插件失败: {}", plugin.getPluginType(), e);
            }
        }
    }
}
```

### 3. 配置示例

#### 指标配置示例
```json
{
    "metricName": "cpu_usage",
    "metricType": "system",
    "displayName": "CPU使用率",
    "description": "设备CPU使用百分比",
    "unit": "%",
    "dataType": "FLOAT",
    "parameters": {
        "oid": "1.3.6.1.4.1.2021.11.9.0",
        "instance": "0"
    },
    "timeout": 30,
    "retryTimes": 3,
    "retryInterval": 1000,
    "cacheEnabled": true,
    "cacheTtl": 60,
    "interval": 300,
    "priority": 5,
    "enabled": true,
    "tags": {
        "category": "system",
        "level": "basic"
    }
}
```

#### 采集器配置示例
```json
{
    "name": "SNMP采集器配置",
    "pluginType": "SNMP",
    "globalParameters": {
        "version": "v2c",
        "community": "public",
        "timeout": 5000
    },
    "connectionConfig": {
        "connectTimeout": 5000,
        "readTimeout": 30000,
        "maxConnections": 10,
        "poolSize": 5
    },
    "performanceConfig": {
        "maxConcurrency": 5,
        "queueSize": 100,
        "batchSize": 10,
        "batchEnabled": true
    },
    "retryConfig": {
        "defaultRetryTimes": 3,
        "retryInterval": 1000,
        "retryStrategy": "EXPONENTIAL_BACKOFF"
    },
    "cacheConfig": {
        "enabled": true,
        "cacheType": "MEMORY",
        "defaultTtl": 300,
        "maxCacheSize": 1000
    }
}
```

## API接口

### 插件管理API

#### 获取所有插件
```http
GET /api/plugins
```

#### 获取插件详情
```http
GET /api/plugins/{pluginType}
```

#### 启用/禁用插件
```http
POST /api/plugins/{pluginType}/enable
POST /api/plugins/{pluginType}/disable
```

#### 重启插件
```http
POST /api/plugins/{pluginType}/restart
```

#### 健康检查
```http
POST /api/plugins/{pluginType}/health-check
POST /api/plugins/health-check
```

#### 获取插件统计
```http
GET /api/plugins/{pluginType}/statistics
```

#### 获取配置模板
```http
GET /api/plugins/{pluginType}/config-template
```

## 最佳实践

### 1. 错误处理
- 使用标准的错误代码和消息
- 提供详细的错误上下文信息
- 实现适当的重试机制

### 2. 性能优化
- 合理使用缓存机制
- 支持批量采集
- 实现连接池管理
- 避免阻塞操作

### 3. 安全考虑
- 安全存储和使用设备凭据
- 验证输入参数
- 记录安全相关操作日志

### 4. 监控与调试
- 提供详细的统计信息
- 实现健康检查机制
- 支持调试模式
- 记录关键操作日志

### 5. 配置管理
- 提供配置验证
- 支持动态配置更新
- 提供配置模板和文档

## 扩展点

### 1. 自定义数据转换
通过实现 `DataTransform` 接口自定义数据转换逻辑。

### 2. 自定义缓存策略
通过配置 `CacheConfig` 实现不同的缓存策略。

### 3. 自定义监听器
实现 `PluginRegistryListener` 和 `PluginLifecycleListener` 监听插件事件。

### 4. 自定义指标发现
重写 `doDiscoverMetrics` 方法实现设备指标的自动发现。

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 实现核心插件接口
  - 提供插件注册和生命周期管理
  - 支持基础的数据采集功能
