# 任务调度器使用指南

## 概述

任务调度器是SkyEye智能监控系统的核心组件，负责管理和执行各种类型的采集任务。它支持多种调度策略，提供完整的任务生命周期管理，并具备强大的监控和统计功能。

## 主要特性

### 1. 多种调度策略
- **简单调度（SIMPLE）**：固定间隔执行，支持分钟、小时、天级别
- **Cron调度（CRON）**：基于Cron表达式的复杂调度
- **事件调度（EVENT）**：基于系统事件的触发调度

### 2. 任务管理功能
- **任务调度**：自动调度和管理任务执行
- **任务控制**：支持暂停、恢复、取消任务
- **优先级管理**：支持任务优先级设置
- **并发控制**：可配置的并发执行数量

### 3. 监控和统计
- **实时监控**：任务执行状态和性能监控
- **统计分析**：详细的执行统计和性能分析
- **健康检查**：调度器健康状态监控
- **性能指标**：CPU、内存、线程等资源监控

### 4. 高可用性
- **自动恢复**：任务执行失败后的自动重试
- **故障转移**：支持任务故障转移机制
- **负载均衡**：任务负载均衡策略
- **熔断保护**：防止系统过载的熔断机制

## 核心组件

### 1. TaskScheduler（调度器接口）
- 定义调度器的基本操作方法
- 支持任务的调度、取消、暂停、恢复等操作
- 提供调度器状态和统计信息查询

### 2. TaskSchedulerImpl（调度器实现）
- 实现具体的调度逻辑
- 管理线程池和任务执行
- 处理不同类型的调度策略
- 收集和更新统计信息

### 3. ScheduledTask（调度任务包装器）
- 封装调度任务的详细信息
- 跟踪任务执行状态和统计
- 管理任务的下次执行时间

### 4. SchedulerStatus（调度器状态）
- 定义调度器的各种状态
- 提供状态检查和操作方法

### 5. SchedulerStatistics（调度器统计）
- 记录调度器的运行统计信息
- 提供性能指标和运行时长计算

## 调度策略详解

### 1. 简单调度（SIMPLE）

#### 按分钟调度
```json
{
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 5
  }
}
```
- 每5分钟执行一次
- 支持1-59分钟的间隔设置

#### 按小时调度
```json
{
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "hours",
    "interval": 2
  }
}
```
- 每2小时执行一次
- 支持1-23小时的间隔设置

#### 按天调度
```json
{
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "days",
    "interval": 1
  }
}
```
- 每天执行一次
- 支持1-30天的间隔设置

### 2. Cron调度（CRON）

#### 每天凌晨2点执行
```json
{
  "scheduleType": "CRON",
  "scheduleConfig": {
    "cronExpression": "0 0 2 * * ?"
  }
}
```

#### 每周一上午9点执行
```json
{
  "scheduleType": "CRON",
  "scheduleConfig": {
    "cronExpression": "0 0 9 ? * MON"
  }
}
```

#### 每月1号上午10点执行
```json
{
  "scheduleType": "CRON",
  "scheduleConfig": {
    "cronExpression": "0 0 10 1 * ?"
  }
}
```

#### 每5分钟执行一次
```json
{
  "scheduleType": "CRON",
  "scheduleConfig": {
    "cronExpression": "0 */5 * * * ?"
  }
}
```

### 3. 事件调度（EVENT）

#### 设备上线时执行
```json
{
  "scheduleType": "EVENT",
  "scheduleConfig": {
    "eventType": "DEVICE_ONLINE",
    "eventSource": "device_id",
    "eventValue": 1
  }
}
```

#### 系统告警时执行
```json
{
  "scheduleType": "EVENT",
  "scheduleConfig": {
    "eventType": "SYSTEM_ALERT",
    "alertLevel": "CRITICAL"
  }
}
```

#### 数据采集完成时执行
```json
{
  "scheduleType": "EVENT",
  "scheduleConfig": {
    "eventType": "DATA_COLLECTION_COMPLETED",
    "metricName": "cpu_usage",
    "threshold": 80
  }
}
```

## API接口

### 调度器管理接口

#### 获取调度器状态
```http
GET /api/collector/scheduler/status
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "status": "RUNNING",
    "description": "运行中",
    "canAcceptTasks": true,
    "isRunning": true,
    "isStopped": false
  }
}
```

#### 启动调度器
```http
POST /api/collector/scheduler/start
```

#### 停止调度器
```http
POST /api/collector/scheduler/stop
```

#### 获取调度器统计信息
```http
GET /api/collector/scheduler/statistics
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalTasks": 15,
    "scheduledTasks": 12,
    "runningTasks": 2,
    "pausedTasks": 1,
    "errorTasks": 0,
    "todayExecutions": 288,
    "todaySuccesses": 275,
    "todayFailures": 13,
    "successRate": 95.49,
    "averageExecutionTime": 1250,
    "startTime": "2024-01-01T10:00:00",
    "lastActiveTime": "2024-01-01T15:30:00",
    "uptime": "5小时30分钟",
    "activeThreads": 8,
    "totalThreads": 10,
    "queuedTasks": 0,
    "memoryUsage": 512000000,
    "cpuUsage": 15.5
  }
}
```

#### 获取已调度的任务列表
```http
GET /api/collector/scheduler/scheduled-tasks
```

#### 检查任务是否已调度
```http
GET /api/collector/scheduler/tasks/{taskId}/scheduled
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 1,
    "isScheduled": true,
    "nextExecutionTime": "2024-01-01T16:00:00"
  }
}
```

#### 重新加载所有任务
```http
POST /api/collector/scheduler/reload
```

#### 清理过期任务
```http
POST /api/collector/scheduler/cleanup
```

### 监控接口

#### 获取调度器健康状态
```http
GET /api/collector/scheduler/health
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "healthy": true,
    "message": "调度器运行正常",
    "status": "RUNNING",
    "uptime": "5小时30分钟",
    "totalTasks": 15,
    "scheduledTasks": 12,
    "runningTasks": 2,
    "lastActiveTime": "2024-01-01T15:30:00"
  }
}
```

#### 获取调度器性能指标
```http
GET /api/collector/scheduler/metrics
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "performance": {
      "successRate": 95.49,
      "failureRate": 4.51,
      "averageExecutionTime": 1250,
      "minExecutionTime": 800,
      "maxExecutionTime": 3000
    },
    "resources": {
      "activeThreads": 8,
      "totalThreads": 10,
      "queuedTasks": 0,
      "memoryUsage": 512000000,
      "cpuUsage": 15.5
    },
    "execution": {
      "todayExecutions": 288,
      "todaySuccesses": 275,
      "todayFailures": 13
    }
  }
}
```

#### 获取调度器配置信息
```http
GET /api/collector/scheduler/config
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "threadPoolSize": 10,
    "maxQueueSize": 100,
    "keepAliveSeconds": 60,
    "cleanupIntervalMinutes": 30,
    "features": [
      "SIMPLE_SCHEDULING",
      "CRON_SCHEDULING",
      "EVENT_SCHEDULING",
      "TASK_PAUSE_RESUME",
      "AUTOMATIC_CLEANUP",
      "STATISTICS_COLLECTION"
    ],
    "supportedScheduleTypes": ["SIMPLE", "CRON", "EVENT"]
  }
}
```

## 配置说明

### 基础配置

```yaml
skyeye:
  scheduler:
    # 线程池配置
    thread-pool-size: 10
    max-queue-size: 100
    keep-alive-seconds: 60
    
    # 清理配置
    cleanup-interval-minutes: 30
    auto-cleanup-enabled: true
    
    # 统计配置
    statistics-update-interval-minutes: 1
    statistics-enabled: true
    
    # 任务配置
    task-execution-timeout-seconds: 300
    retry-enabled: true
    max-retry-times: 3
    retry-interval-ms: 1000
    
    # 优先级和并发控制
    priority-enabled: true
    concurrency-control-enabled: true
    max-concurrent-tasks: 50
    
    # 监控配置
    monitoring-enabled: true
    monitoring-data-retention-days: 30
    task-logging-enabled: true
    task-log-level: INFO
    
    # 性能监控
    performance-monitoring-enabled: true
    performance-sampling-interval-seconds: 60
    
    # 告警配置
    alert-notification-enabled: true
    alert-threshold-percentage: 10.0
    alert-check-interval-minutes: 5
    
    # 高级功能
    task-warmup-enabled: false
    task-warmup-time-seconds: 30
    load-balancing-enabled: false
    load-balancing-strategy: ROUND_ROBIN
    failover-enabled: false
    failover-retry-times: 2
    rate-limiting-enabled: false
    rate-limit-per-second: 100
    circuit-breaker-enabled: false
    circuit-breaker-failure-threshold: 5
    circuit-breaker-recovery-time-seconds: 60
```

### 高级配置

#### 负载均衡策略
```yaml
skyeye:
  scheduler:
    load-balancing-enabled: true
    load-balancing-strategy: ROUND_ROBIN  # ROUND_ROBIN, LEAST_CONNECTIONS, WEIGHTED
```

#### 熔断器配置
```yaml
skyeye:
  scheduler:
    circuit-breaker-enabled: true
    circuit-breaker-failure-threshold: 5
    circuit-breaker-recovery-time-seconds: 60
```

#### 限流配置
```yaml
skyeye:
  scheduler:
    rate-limiting-enabled: true
    rate-limit-per-second: 100
```

## 使用示例

### 1. 创建定时采集任务

```java
// 创建每5分钟执行一次的系统监控任务
CollectionTask systemMonitorTask = new CollectionTask();
systemMonitorTask.setName("系统监控任务");
systemMonitorTask.setScheduleType("SIMPLE");
systemMonitorTask.setScheduleConfig(Map.of(
    "frequency", "minutes",
    "interval", 5
));
systemMonitorTask.setTargetDevices(Arrays.asList(1L, 2L, 3L));
systemMonitorTask.setMetricsConfig(Arrays.asList(
    new MetricConfig("cpu_usage", "system_metrics", "SNMP", 
        Map.of("oid", "1.3.6.1.4.1.2021.11.9.0")),
    new MetricConfig("memory_usage", "system_metrics", "SNMP", 
        Map.of("oid", "1.3.6.1.4.1.2021.4.6.0"))
));
systemMonitorTask.setPriority(1);
systemMonitorTask.setIsEnabled(true);

// 保存任务
CollectionTask savedTask = collectionTaskService.createTask(systemMonitorTask);

// 任务会自动被调度器调度执行
```

### 2. 创建Cron调度任务

```java
// 创建每天凌晨2点执行的备份任务
CollectionTask backupTask = new CollectionTask();
backupTask.setName("数据备份任务");
backupTask.setScheduleType("CRON");
backupTask.setScheduleConfig(Map.of(
    "cronExpression", "0 0 2 * * ?"
));
backupTask.setTargetDevices(Arrays.asList(1L));
backupTask.setMetricsConfig(Arrays.asList(
    new MetricConfig("backup_status", "backup_metrics", "HTTP", 
        Map.of("url", "/backup", "method", "POST"))
));
backupTask.setPriority(2);
backupTask.setIsEnabled(true);

CollectionTask savedBackupTask = collectionTaskService.createTask(backupTask);
```

### 3. 创建事件触发任务

```java
// 创建设备上线时执行的初始化任务
CollectionTask initTask = new CollectionTask();
initTask.setName("设备初始化任务");
initTask.setScheduleType("EVENT");
initTask.setScheduleConfig(Map.of(
    "eventType", "DEVICE_ONLINE",
    "eventSource", "device_id"
));
initTask.setTargetDevices(Arrays.asList(1L));
initTask.setMetricsConfig(Arrays.asList(
    new MetricConfig("device_status", "device_metrics", "SNMP", 
        Map.of("oid", "1.3.6.1.2.1.1.3.0"))
));
initTask.setPriority(1);
initTask.setIsEnabled(true);

CollectionTask savedInitTask = collectionTaskService.createTask(initTask);
```

### 4. 管理调度器

```java
// 获取调度器状态
SchedulerStatus status = taskScheduler.getStatus();
if (status == SchedulerStatus.STOPPED) {
    // 启动调度器
    taskScheduler.start();
}

// 获取调度器统计信息
SchedulerStatistics statistics = taskScheduler.getStatistics();
log.info("调度器运行时长: {}", statistics.getFormattedUptime());
log.info("当前任务数: {}", statistics.getTotalTasks());
log.info("成功率: {:.2f}%", statistics.getSuccessRate());

// 重新加载所有任务
taskScheduler.reloadAllTasks();

// 清理过期任务
taskScheduler.cleanupExpiredTasks();
```

## 最佳实践

### 1. 任务设计原则
- **单一职责**：每个任务专注于特定的监控目标
- **合理频率**：根据指标特性设置合适的采集频率
- **优先级管理**：关键任务设置高优先级
- **资源控制**：合理设置超时时间和并发数

### 2. 调度策略选择
- **简单调度**：适用于固定间隔的常规监控任务
- **Cron调度**：适用于复杂的定时任务，如备份、报表生成等
- **事件调度**：适用于响应式任务，如设备上线、告警触发等

### 3. 性能优化
- **线程池配置**：根据系统资源合理配置线程池大小
- **任务分组**：将相关任务分组，避免同时执行
- **错峰执行**：设置不同的执行时间，避免资源竞争
- **监控告警**：设置合理的告警阈值，及时发现问题

### 4. 高可用性
- **故障恢复**：启用自动重试和故障转移机制
- **负载均衡**：使用负载均衡策略分散任务负载
- **熔断保护**：启用熔断器防止系统过载
- **健康检查**：定期检查调度器健康状态

### 5. 运维管理
- **日志记录**：启用详细的任务执行日志
- **统计收集**：收集和分析任务执行统计
- **定期清理**：定期清理过期的任务和统计数据
- **配置管理**：使用配置文件管理调度器参数

## 故障排除

### 常见问题

#### 1. 调度器启动失败
**症状**：调度器状态为ERROR
**排查步骤**：
1. 检查线程池配置是否正确
2. 验证数据库连接是否正常
3. 查看启动日志中的错误信息
4. 检查系统资源是否充足

#### 2. 任务执行失败
**症状**：任务执行状态为FAILED
**排查步骤**：
1. 检查任务配置是否正确
2. 验证目标设备是否在线
3. 查看任务执行日志
4. 检查网络连通性

#### 3. 任务调度异常
**症状**：任务未按预期时间执行
**排查步骤**：
1. 检查调度配置是否正确
2. 验证系统时间是否同步
3. 查看调度器状态
4. 检查任务是否被暂停

#### 4. 性能问题
**症状**：任务执行缓慢或系统响应慢
**排查步骤**：
1. 检查线程池使用情况
2. 监控系统资源使用率
3. 分析任务执行统计
4. 调整并发和超时配置

### 监控指标

#### 关键指标
- **任务执行成功率**：应保持在95%以上
- **平均执行时间**：根据任务复杂度设定合理阈值
- **线程池使用率**：应保持在80%以下
- **内存使用率**：应保持在70%以下
- **CPU使用率**：应保持在60%以下

#### 告警设置
```yaml
skyeye:
  scheduler:
    alert-notification-enabled: true
    alert-threshold-percentage: 10.0  # 失败率超过10%时告警
    alert-check-interval-minutes: 5   # 每5分钟检查一次
```

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 实现基础调度功能
  - 支持简单调度和Cron调度
  - 提供完整的REST API接口
  - 实现任务监控和统计功能
  - 支持任务暂停、恢复、取消等操作
  - 实现自动清理和健康检查
  - 支持事件调度和高级功能配置
