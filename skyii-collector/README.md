# SkyEye 采集器 (Collector)

SkyEye采集器是SkyEye智能监控系统的核心组件，负责从各种设备和系统中采集监控数据，并将数据传输到SkyEye服务器。

## 功能特点

- **多协议支持**：内置支持SNMP、HTTP、SSH等多种采集协议
- **插件化架构**：支持动态加载和管理多种采集插件
- **高效调度**：基于优先级的任务调度系统，支持并发采集
- **灵活配置**：支持本地配置和远程下发配置
- **数据缓存**：本地缓存机制，确保数据不丢失
- **远程管理**：支持远程控制和配置更新
- **高可靠性**：自动重连、故障恢复机制
- **资源控制**：CPU和内存使用限制，避免影响被监控系统

## 系统架构

```
+------------------+     +-------------------+     +----------------+
| 设备管理/任务下发 | <--> | 采集调度和执行系统 | <--> | 数据处理和传输 |
+------------------+     +-------------------+     +----------------+
        ^                         ^                        ^
        |                         |                        |
+------------------+     +-------------------+     +----------------+
|    插件管理系统   |     |   资源和配置管理   |     |   心跳和注册   |
+------------------+     +-------------------+     +----------------+
```



## 快速开始

### 构建采集器

```bash
# 克隆代码
git clone https://github.com/skyeye/skyii-collector.git
cd skyii-collector

 go mod tidy
# 构建
go build -o skyii-collector cmd/collector/main.go
```

### 注册采集器

```bash
# 使用令牌注册
./skyii-collector --register --token <YOUR_TOKEN> --server http://skyeye-server:8080/api
```

### 启动采集器

```bash
# 启动采集器
./skyii-collector

# 使用自定义配置文件
./skyii-collector --config /path/to/config.yaml
```

## 配置说明

采集器使用YAML格式的配置文件，默认为`config.yaml`。主要配置项包括：

```yaml
# 采集器基本配置
collector_id: "collector-hostname-12345"
name: "skyii-collector"
log_level: "info"
log_path: "logs"

# 服务器连接配置
server:
  url: "http://localhost:8080/api"
  api_key: "YOUR_API_KEY"
  heartbeat_interval: 60
  timeout: 10
  ssl: false
  verify_ssl: true

# 采集配置
collection:
  max_concurrency: 10
  retry_count: 3
  retry_interval: 5
  buffer_size: 1000
  batch_size: 100
  data_ttl: 3600
  protocols:
    snmp: true
    http: true
    ssh: false

# 系统配置
system:
  cpu_limit: 80
  memory_limit: 512
  disk_path: "data"
  disk_limit: 1024

# 插件配置
plugins:
  snmp:
    community: "public"
    timeout: 5
  http:
    timeout: 10
    user_agent: "SkyEye-Collector/1.0"
```

## 插件开发

采集器支持开发自定义插件来扩展采集能力。插件需要实现`Plugin`接口：

```go
type Plugin interface {
    Initialize(config map[string]interface{}) error
    Collect(deviceID string, params map[string]interface{}) (*CollectResult, error)
    Validate(params map[string]interface{}) (bool, string, error)
    GetMetadata() PluginMetadata
    Shutdown() error
}
```

## 通信协议

采集器与SkyEye服务器之间使用HTTP/HTTPS协议通信，数据格式为JSON。主要接口包括：

- `/collectors/register` - 采集器注册
- `/collectors/heartbeat` - 采集器心跳
- `/collectors/metrics` - 指标数据上报
- `/collectors/tasks` - 获取任务列表
- `/collectors/plugins` - 获取插件列表
- `/collectors/config` - 获取配置

## 部署建议

- 在生产环境中，建议以系统服务方式运行采集器
- 对于需要采集敏感数据的场景，建议启用SSL加密传输
- 根据被监控设备的数量和性能要求，调整`max_concurrency`参数
- 对于网络不稳定的环境，可以增大`buffer_size`和`data_ttl`参数

## 故障排除

常见问题处理：

1. 无法连接到服务器
   - 检查网络连接
   - 确认服务器地址和端口是否正确
   - 检查防火墙设置

2. 采集数据失败
   - 确认目标设备是否可达
   - 检查采集参数是否正确
   - 查看日志获取详细错误信息

3. 性能问题
   - 减少并发采集任务数
   - 增加采集间隔
   - 检查系统资源使用情况

## 许可证

本项目采用 [MIT 许可证](LICENSE)。 