# SkyEye系统 - 服务端与采集端交互设计方案

## 1. 设计概述

### 1.1 架构图

```
┌────────────────────────┐                  ┌────────────────────────┐
│    skyii-server        │                  │    skyii-collector     │
│    (Java/Spring Boot)  │◄─────HTTP/S─────►│    (Go)                │
└────────────────────────┘                  └────────────────────────┘
           │                                            │
           │                                            │
           ▼                                            ▼
     ┌──────────┐                                ┌──────────────┐
     │  数据库   │                                │  目标设备     │
     └──────────┘                                └──────────────┘
```

### 1.2 设计原则

- **无状态通信**：采用RESTful设计理念，服务端不保存采集端状态
- **轻量级数据交换**：使用JSON作为数据交换格式
- **安全可靠**：采用HTTPS+Token认证保障通信安全
- **故障容错**：设计重试机制和异常处理流程
- **高效传输**：优化数据传输格式，减少网络开销

## 2. 核心交互流程

### 2.1 采集端注册与心跳

1. **注册流程**: 采集端启动后向服务端注册
2. **心跳机制**: 定期向服务端汇报状态和健康信息

### 2.2 任务分发与执行

1. **任务获取**: 采集端定期从服务端拉取待执行任务
2. **状态更新**: 采集端定期向服务端更新任务执行状态
3. **结果回传**: 采集端将采集结果回传至服务端

## 3. API设计

### 3.1 采集端注册与管理API

#### 3.1.1 采集端注册

```
POST /api/v1/collectors/register
```

**请求体**:
```json
{
  "collectorId": "string",       // 采集端唯一标识(首次为空)
  "hostname": "string",          // 主机名
  "ip": "string",                // IP地址
  "version": "string",           // 采集器版本
  "capabilities": ["string"],    // 支持的采集类型
  "tags": ["string"]             // 标签
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "collectorId": "string",     // 服务端分配的采集端ID
    "token": "string",           // 认证Token
    "serverTime": "string"       // 服务端时间
  }
}
```

#### 3.1.2 采集端心跳

```
POST /api/v1/collectors/{collectorId}/heartbeat
```

**请求体**:
```json
{
  "timestamp": "string",         // 时间戳
  "status": "string",            // 状态(ONLINE/BUSY/ERROR)
  "metrics": {                   // 性能指标
    "cpu": 0.0,                  // CPU使用率
    "memory": 0.0,               // 内存使用率
    "disk": 0.0,                 // 磁盘使用率
    "runningTasks": 0            // 运行中任务数
  },
  "error": "string"              // 错误信息(可选)
}
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": {
    "serverTime": "string",      // 服务端时间
    "action": "string"           // 服务端指令(CONTINUE/PAUSE/UPGRADE)
  }
}
```

### 3.2 任务获取与状态更新API

#### 3.2.1 获取待执行任务批次

```
GET /api/v1/collectors/{collectorId}/batches
```

**请求参数**:
```
status: SCHEDULED         // 筛选状态(可选)
limit: 10                 // 最大返回批次数(可选)
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "batchId": 0,             // 批次ID
      "taskId": 0,              // 任务ID
      "batchName": "string",    // 批次名称
      "status": "string",       // 状态
      "priority": 0,            // 优先级
      "scheduledTime": "string" // 计划执行时间
    }
  ]
}
```

#### 3.2.2 获取批次中的采集任务

```
GET /api/v1/collectors/{collectorId}/batches/{batchId}/tasks
```

**响应体**:
```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "taskId": 0,               // 任务ID
      "batchId": 0,              // 批次ID
      "deviceId": 0,             // 设备ID
      "metricId": 0,             // 指标ID
      "deviceName": "string",    // 设备名称
      "metricName": "string",    // 指标名称
      "targetAddress": "string", // 目标地址
      "targetPort": 0,           // 目标端口
      "collectType": "string",   // 采集类型
      "collectParams": "string"  // 采集参数(JSON)
    }
  ]
}
```

#### 3.2.3 更新批次状态

```
PUT /api/v1/collectors/{collectorId}/batches/{batchId}/status
```

**请求体**:
```json
{
  "status": "string",           // 状态(RUNNING/COMPLETED/FAILED)
  "startTime": "string",        // 开始时间(可选)
  "endTime": "string",          // 结束时间(可选)
  "message": "string"           // 状态说明(可选)
}
```

#### 3.2.4 更新任务状态

```
PUT /api/v1/collectors/{collectorId}/tasks/{taskId}/status
```

**请求体**:
```json
{
  "status": "string",           // 状态(PENDING/RUNNING/COMPLETED/FAILED)
  "startTime": "string",        // 开始时间(可选)
  "endTime": "string",          // 结束时间(可选)
  "executionTime": 0,           // 执行时间(毫秒,可选)
  "message": "string"           // 状态说明(可选)
}
```

### 3.3 结果回传API

#### 3.3.1 上传采集结果

```
POST /api/v1/collectors/{collectorId}/results
```

**请求体**:
```json
[
  {
    "taskId": 0,                // 任务ID
    "batchId": 0,               // 批次ID
    "deviceId": 0,              // 设备ID
    "metricId": 0,              // 指标ID
    "resultValue": "string",    // 结果值(可为JSON)
    "resultType": "string",     // 结果类型
    "status": "string",         // 状态(SUCCESS/WARNING/ERROR)
    "executionTime": 0,         // 执行时间(毫秒)
    "timestamp": "string"       // 采集时间戳
  }
]
```

#### 3.3.2 上传日志

```
POST /api/v1/collectors/{collectorId}/logs
```

**请求体**:
```json
[
  {
    "level": "string",          // 日志级别(INFO/WARN/ERROR)
    "message": "string",        // 日志内容
    "timestamp": "string",      // 时间戳
    "taskId": 0,                // 关联任务ID(可选)
    "batchId": 0,               // 关联批次ID(可选)
    "context": {}               // 上下文信息(可选)
  }
]
```

## 4. 安全机制

### 4.1 传输层安全

- 采用HTTPS协议加密传输
- 配置TLS 1.2+，禁用弱加密算法
- 证书验证与主机名验证

### 4.2 认证机制

- 基于Token的认证方式
- 在HTTP请求头中加入认证信息
  ```
  Authorization: Bearer {token}
  ```
- Token过期时间设置为24小时，支持自动续期

### 4.3 授权控制

- 采集端仅能访问与自身ID相关的资源
- 服务端进行权限验证，防止越权访问
- 敏感操作记录审计日志

## 5. 错误处理与故障恢复

### 5.1 错误响应格式

```json
{
  "code": 400,                    // HTTP状态码
  "message": "参数错误",           // 错误描述
  "details": "无效的采集器ID",     // 详细信息
  "timestamp": "2023-05-01T12:00:00Z"  // 时间戳
}
```

### 5.2 错误码规范

| 错误码 | 说明 |
|-------|------|
| 400 | 请求参数错误 |
| 401 | 未授权(token无效) |
| 403 | 权限不足 |
| 404 | 资源不存在 |
| 409 | 资源冲突 |
| 429 | 请求过于频繁 |
| 500 | 服务器错误 |
| 503 | 服务不可用 |

### 5.3 重试策略

- 采集端实现指数退避算法进行重试
- 任务获取失败后延迟重试，增加重试间隔
- 结果上传失败后本地缓存，定期尝试重传
- 设置最大重试次数和超时时间

## 6. Go语言采集端实现架构

### 6.1 核心模块

```
skyii-collector/
├── cmd/                 # 程序入口
│   └── collector/
│       └── main.go      # 主程序入口
├── config/              # 配置管理
│   └── config.go        # 配置定义和加载
├── internal/            # 内部实现
│   ├── api/             # API客户端
│   │   ├── client.go    # HTTP客户端
│   │   └── models.go    # API数据模型
│   ├── collector/       # 采集器核心逻辑
│   │   ├── manager.go   # 采集器管理
│   │   └── registry.go  # 注册管理
│   ├── executor/        # 任务执行器
│   │   ├── executor.go  # 执行器实现
│   │   └── scheduler.go # 任务调度器
│   ├── plugins/         # 采集插件
│   │   ├── plugin.go    # 插件接口
│   │   ├── http.go      # HTTP采集插件
│   │   └── snmp.go      # SNMP采集插件
│   └── storage/         # 本地存储
│       └── storage.go   # 结果缓存
└── pkg/                 # 公共包
    ├── logger/          # 日志组件
    ├── models/          # 数据模型
    └── utils/           # 工具函数
```

### 6.2 关键接口定义

```go
// 采集插件接口
type CollectPlugin interface {
    // 插件类型
    Type() string
    // 执行采集
    Collect(ctx context.Context, task *models.CollectionTask) (*models.CollectionResult, error)
    // 验证参数
    Validate(params map[string]interface{}) error
}

// API客户端接口
type ApiClient interface {
    // 注册采集器
    Register(ctx context.Context, info *models.CollectorInfo) (*models.RegisterResponse, error)
    // 发送心跳
    Heartbeat(ctx context.Context, status *models.HeartbeatRequest) (*models.HeartbeatResponse, error)
    // 获取批次
    GetBatches(ctx context.Context, limit int) ([]*models.TaskBatch, error)
    // 获取任务
    GetTasks(ctx context.Context, batchId int64) ([]*models.CollectionTask, error)
    // 更新批次状态
    UpdateBatchStatus(ctx context.Context, batchId int64, status *models.StatusUpdate) error
    // 更新任务状态
    UpdateTaskStatus(ctx context.Context, taskId int64, status *models.StatusUpdate) error
    // 上传结果
    UploadResults(ctx context.Context, results []*models.CollectionResult) error
    // 上传日志
    UploadLogs(ctx context.Context, logs []*models.LogEntry) error
}
```

## 7. 高可用与扩展性设计

### 7.1 采集端高可用

- 多采集器部署，相同标签和能力采集器实现互备
- 服务端任务分发时考虑负载均衡
- 采集器在线状态实时监控，故障快速转移

### 7.2 服务端横向扩展

- API服务无状态设计，支持水平扩展
- 结果处理服务异步化，避免瓶颈
- 采用分布式队列缓冲大量采集结果

### 7.3 采集端横向扩展

- 单采集器支持多协程并行执行任务
- 动态调整并发度，根据系统负载自动优化
- 设计分级缓存，减轻服务端压力

## 8. 数据优化

### 8.1 批量处理

- 采集结果批量上传，减少API调用次数
- 批次内任务批量获取，减少网络往返
- 批量状态更新，提高处理效率

### 8.2 数据压缩

- HTTP传输启用gzip压缩
- 大体积结果数据考虑压缩后传输
- 优化JSON格式，减少冗余字段

### 8.3 增量传输

- 结果数据支持增量传输
- 大型集合数据分批传输
- 支持断点续传机制

## 9. 示例交互流程

### 9.1 完整交互序列图

```
┌────────┐                             ┌────────┐                           ┌────────┐
│采集端   │                             │服务端   │                           │数据库   │
└────┬───┘                             └────┬───┘                           └────┬───┘
     │                                      │                                    │
     │  1. 采集端注册                        │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  2. 保存采集端信息                  │
     │                                      │ ────────────────────────────────>  │
     │  3. 返回Token                        │                                    │
     │ <─────────────────────────────────   │                                    │
     │                                      │                                    │
     │  4. 定期心跳                         │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  5. 更新采集端状态                  │
     │                                      │ ────────────────────────────────>  │
     │  6. 获取待执行批次                    │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  7. 查询任务批次                    │
     │                                      │ ────────────────────────────────>  │
     │  8. 返回批次信息                      │                                    │
     │ <─────────────────────────────────   │                                    │
     │                                      │                                    │
     │  9. 获取批次内任务                    │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  10. 查询任务详情                   │
     │                                      │ ────────────────────────────────>  │
     │  11. 返回任务列表                     │                                    │
     │ <─────────────────────────────────   │                                    │
     │                                      │                                    │
     │  12. 更新批次为运行中                 │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  13. 更新批次状态                   │
     │                                      │ ────────────────────────────────>  │
     │                                      │                                    │
     │  14. 更新任务为运行中                 │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  15. 更新任务状态                   │
     │                                      │ ────────────────────────────────>  │
     │                                      │                                    │
     │  16. 执行任务并收集结果              │                                    │
     │ ─────┐                               │                                    │
     │      │                               │                                    │
     │ <────┘                               │                                    │
     │                                      │                                    │
     │  17. 上传采集结果                    │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  18. 保存采集结果                   │
     │                                      │ ────────────────────────────────>  │
     │                                      │                                    │
     │  19. 更新任务为已完成                │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  20. 更新任务状态                   │
     │                                      │ ────────────────────────────────>  │
     │                                      │                                    │
     │  21. 更新批次为已完成                │                                    │
     │ ─────────────────────────────────>   │                                    │
     │                                      │  22. 更新批次状态                   │
     │                                      │ ────────────────────────────────>  │
     │                                      │                                    │
```

## 10. 总结与最佳实践

### 10.1 关键设计要点

1. **基于拉取的任务获取模式**：采集端主动拉取任务，避免网络限制
2. **异步结果处理**：结果异步上传，不阻塞采集流程
3. **健壮的错误处理**：完善的重试机制和错误恢复能力
4. **分层安全机制**：传输加密+令牌认证+授权控制
5. **优化的数据传输**：批量处理+数据压缩+增量传输

### 10.2 最佳实践建议

1. **配置合理的心跳间隔**：建议30-60秒，平衡实时性和网络开销
2. **实现本地缓存机制**：暂存采集结果，防止网络故障导致数据丢失
3. **断网恢复策略**：网络恢复后优先上传历史结果
4. **设置合理的超时参数**：API调用超时、采集操作超时、重试超时
5. **完善的日志记录**：关键操作和错误详细记录，便于问题排查

人机交互如一：设计清晰、简洁的API接口，使开发体验一致和可预测