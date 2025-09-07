# 采集引擎管理器使用指南

## 概述

采集引擎管理器是SkyEye智能监控系统中的核心组件，负责协调和管理所有数据采集插件的执行。它提供了统一的采集调度、结果处理、性能统计和监控功能。

## 主要组件

### 1. CollectorEngine（采集引擎）
- **功能**：执行具体的数据采集任务
- **特性**：插件选择、结果处理、数据保存、错误处理
- **支持**：同步和异步执行、批量采集

### 2. CollectorEngineManager（引擎管理器）
- **功能**：管理引擎生命周期和性能统计
- **特性**：性能监控、健康检查、统计分析、定期清理
- **监控**：实时状态、插件性能、成功率统计

### 3. CollectionLogService（采集日志服务）
- **功能**：记录和管理采集过程的详细日志
- **特性**：执行追踪、错误记录、统计分析、日志清理

### 4. CollectionData/CollectionLog（数据和日志实体）
- **功能**：持久化采集结果和执行记录
- **特性**：结构化存储、索引优化、数据保留策略

## 核心功能

### 数据采集执行

#### 单个指标采集
```http
POST /api/collector/engine/collect?deviceId=1
Content-Type: application/json

{
  "metricConfig": {
    "metricName": "cpu_usage",
    "metricType": "system_metrics",
    "pluginType": "SNMP",
    "parameters": {
      "oid": "1.3.6.1.4.1.2021.11.9.0"
    },
    "timeout": 30
  },
  "credentials": {
    "version": "V2C",
    "community": "public"
  }
}
```

#### 批量指标采集
```http
POST /api/collector/engine/collect-batch?deviceId=1
Content-Type: application/json

{
  "metricConfigs": [
    {
      "metricName": "cpu_usage",
      "metricType": "system_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.4.1.2021.11.9.0"
      }
    },
    {
      "metricName": "memory_usage",
      "metricType": "system_metrics", 
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.4.1.2021.4.6.0"
      }
    }
  ],
  "credentials": {
    "version": "V2C",
    "community": "public"
  }
}
```

#### 异步采集
```http
POST /api/collector/engine/collect-async?deviceId=1
Content-Type: application/json

{
  "metricConfig": {
    "metricName": "interface_stats",
    "metricType": "network_metrics",
    "pluginType": "SNMP",
    "parameters": {
      "oid": "1.3.6.1.2.1.2.2.1.10"
    }
  }
}
```

### 引擎状态监控

#### 获取引擎状态
```http
GET /api/collector/engine/status
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCollections": 15420,
    "successfulCollections": 14890,
    "failedCollections": 530,
    "activeCollections": 12,
    "successRate": 96.56,
    "pluginStatistics": {
      "SNMP": {
        "executionCount": 8900,
        "averageExecutionTime": 1250.5,
        "errorCount": 145
      },
      "HTTP": {
        "executionCount": 6520,
        "averageExecutionTime": 850.2,
        "errorCount": 385
      }
    },
    "registeredPlugins": 2,
    "lastCleanupTime": "2024-01-01T10:30:00",
    "timestamp": 1704096000000
  }
}
```

#### 获取详细统计信息
```http
GET /api/collector/engine/statistics
```

#### 获取插件性能排名
```http
GET /api/collector/engine/plugin-performance
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "pluginType": "HTTP",
      "executionCount": 6520,
      "averageExecutionTime": 850.2,
      "errorCount": 385,
      "errorRate": 5.9
    },
    {
      "pluginType": "SNMP",
      "executionCount": 8900,
      "averageExecutionTime": 1250.5,
      "errorCount": 145,
      "errorRate": 1.6
    }
  ]
}
```

### 健康检查

#### 执行健康检查
```http
GET /api/collector/engine/health
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "healthy": true,
    "issues": [],
    "activeCollections": 12,
    "successRate": 96.56,
    "checkTime": 1704096000000
  }
}
```

### 插件管理

#### 获取已注册插件
```http
GET /api/collector/engine/plugins
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "type": "SNMP",
      "name": "SNMP数据采集插件",
      "version": "1.0.0",
      "description": "支持SNMPv1/v2c/v3协议的数据采集插件",
      "supportedProtocols": ["SNMP"],
      "supportedMetricTypes": ["system_info", "interface_stats", "cpu_usage"],
      "healthStatus": {
        "status": "HEALTHY",
        "message": "运行正常"
      }
    },
    {
      "type": "HTTP",
      "name": "HTTP数据采集插件", 
      "version": "1.0.0",
      "description": "支持HTTP/HTTPS协议的数据采集插件",
      "supportedProtocols": ["HTTP", "HTTPS"],
      "supportedMetricTypes": ["health_check", "api_response", "json_path"],
      "healthStatus": {
        "status": "HEALTHY",
        "message": "运行正常"
      }
    }
  ]
}
```

### 数据查询

#### 获取采集数据
```http
GET /api/collector/engine/data?deviceId=1&metricName=cpu_usage&page=1&size=20
```

#### 获取采集日志
```http
GET /api/collector/engine/logs?deviceId=1&limit=50
```

#### 获取采集统计
```http
GET /api/collector/engine/collection-statistics?timeRange=24h
```

## 配置说明

### 引擎配置
采集引擎支持以下配置参数：

#### 线程池配置
```yaml
collector:
  engine:
    corePoolSize: 10
    maxPoolSize: 50
    queueCapacity: 100
    keepAliveSeconds: 300
```

#### 数据保留策略
```yaml
collector:
  data:
    retention:
      health_check: 7d      # 健康检查数据保留7天
      system_metrics: 30d   # 系统指标保留30天
      custom_metrics: 90d   # 自定义指标保留90天
```

#### 清理策略
```yaml
collector:
  cleanup:
    interval: 3600s         # 清理间隔1小时
    logRetention: 30d       # 日志保留30天
    timeoutMinutes: 30      # 超时任务处理阈值
```

### 性能调优

#### 并发控制
- 根据设备数量和网络状况调整线程池大小
- 合理设置采集超时时间
- 避免同时采集过多指标

#### 内存优化
- 定期清理过期数据和日志
- 控制采集结果的数据量
- 使用分页查询大量数据

#### 网络优化
- 合理设置重试次数和间隔
- 使用连接池复用网络连接
- 根据网络状况调整超时时间

## 监控和告警

### 关键指标监控
1. **采集成功率**：监控整体采集成功率，低于阈值时告警
2. **响应时间**：监控平均响应时间，超过阈值时告警
3. **活跃任务数**：监控并发采集任务数，防止过载
4. **错误率**：监控插件错误率，及时发现问题

### 告警配置示例
```yaml
monitoring:
  alerts:
    - name: "采集成功率低"
      condition: "successRate < 80"
      severity: "high"
    - name: "响应时间过长"
      condition: "averageResponseTime > 5000"
      severity: "medium"
    - name: "活跃任务过多"
      condition: "activeCollections > 100"
      severity: "medium"
```

## 故障排除

### 常见问题

#### 1. 采集失败率高
**症状**：成功率低于预期
**排查步骤**：
1. 检查设备网络连通性
2. 验证设备凭据配置
3. 查看详细错误日志
4. 检查插件健康状态

#### 2. 响应时间过长
**症状**：采集响应时间超过预期
**排查步骤**：
1. 检查网络延迟
2. 优化采集参数
3. 减少并发采集数量
4. 调整超时配置

#### 3. 内存使用过高
**症状**：系统内存占用持续增长
**排查步骤**：
1. 检查数据清理策略
2. 查看是否有内存泄漏
3. 优化采集数据结构
4. 调整数据保留时间

#### 4. 插件异常
**症状**：特定插件执行失败
**排查步骤**：
1. 查看插件健康状态
2. 检查插件配置
3. 验证设备协议支持
4. 重启插件服务

### 日志分析

#### 采集执行日志
```sql
-- 查询最近的失败日志
SELECT * FROM tb_collection_logs 
WHERE success = false 
ORDER BY start_time DESC 
LIMIT 50;

-- 查询插件错误统计
SELECT plugin_type, COUNT(*) as error_count
FROM tb_collection_logs 
WHERE success = false 
  AND start_time > NOW() - INTERVAL '1 hour'
GROUP BY plugin_type;
```

#### 性能分析日志
```sql
-- 查询平均响应时间
SELECT plugin_type, AVG(response_time) as avg_response_time
FROM tb_collection_logs 
WHERE success = true 
  AND response_time IS NOT NULL
GROUP BY plugin_type;

-- 查询超时任务
SELECT * FROM tb_collection_logs 
WHERE status = 'RUNNING' 
  AND start_time < NOW() - INTERVAL '30 minutes';
```

## 最佳实践

### 采集策略
1. **分层采集**：关键指标高频采集，一般指标低频采集
2. **错峰采集**：避免所有设备同时采集
3. **渐进重试**：采集失败时使用指数退避策略
4. **优雅降级**：系统负载高时减少非关键指标采集

### 数据管理
1. **分区存储**：按时间分区存储采集数据
2. **压缩存储**：对历史数据进行压缩
3. **冷热分离**：热数据存储在高速存储，冷数据存储在成本较低的存储
4. **定期清理**：按照数据保留策略定期清理过期数据

### 运维管理
1. **监控仪表盘**：建立实时监控仪表盘
2. **告警机制**：设置多级告警机制
3. **性能基线**：建立性能基线用于异常检测
4. **容量规划**：根据增长趋势进行容量规划

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 实现基础采集引擎功能
  - 支持SNMP和HTTP插件
  - 提供完整的API接口
  - 实现性能监控和统计功能
