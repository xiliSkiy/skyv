# 采集任务管理使用指南

## 概述

采集任务管理系统是SkyEye智能监控系统的核心组件，负责创建、配置、调度和管理各种数据采集任务。它支持多种调度策略，提供完整的任务生命周期管理，并具备强大的监控和统计功能。

## 主要功能

### 1. 任务管理
- **任务创建**：支持创建各种类型的采集任务
- **任务配置**：灵活的配置选项，支持多种调度策略
- **任务监控**：实时监控任务执行状态和性能
- **任务统计**：详细的执行统计和性能分析

### 2. 调度策略
- **简单调度**：固定间隔执行（分钟、小时、天）
- **Cron调度**：基于Cron表达式的复杂调度
- **事件调度**：基于系统事件的触发调度

### 3. 执行控制
- **手动执行**：支持手动触发任务执行
- **批量操作**：批量启用、禁用、更新任务状态
- **优先级管理**：支持任务优先级设置
- **并发控制**：可配置的并发执行数量

## 核心组件

### 1. CollectionTask（采集任务实体）
- 任务基本信息（名称、描述、状态等）
- 调度配置（类型、参数、下次执行时间等）
- 目标设备和指标配置
- 执行统计（次数、成功率、平均时间等）

### 2. TaskStatistics（任务统计实体）
- 按日期统计任务执行情况
- 性能指标（执行时间、成功率、数据量等）
- 错误统计和性能排名

### 3. CollectionTaskService（任务服务）
- 任务CRUD操作
- 任务执行和调度
- 统计信息管理
- 配置验证和测试

### 4. CollectionTaskController（任务控制器）
- REST API接口
- 权限控制
- 异常处理
- 批量操作支持

## API接口

### 任务管理接口

#### 创建采集任务
```http
POST /api/collector/tasks
Content-Type: application/json

{
  "name": "系统健康检查",
  "description": "定期检查系统关键指标",
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 5
  },
  "targetDevices": [1, 2, 3],
  "metricsConfig": [
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
  "priority": 1,
  "timeout": 300,
  "maxConcurrency": 5
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "系统健康检查",
    "description": "定期检查系统关键指标",
    "scheduleType": "SIMPLE",
    "status": 1,
    "priority": 1,
    "isEnabled": true,
    "nextExecutionTime": "2024-01-01T10:05:00",
    "createdAt": "2024-01-01T10:00:00"
  }
}
```

#### 更新采集任务
```http
PUT /api/collector/tasks/1
Content-Type: application/json

{
  "description": "更新后的任务描述",
  "priority": 2,
  "timeout": 600
}
```

#### 删除采集任务
```http
DELETE /api/collector/tasks/1
```

#### 获取任务详情
```http
GET /api/collector/tasks/1
```

#### 分页查询任务
```http
GET /api/collector/tasks?page=1&size=20&status=1&scheduleType=SIMPLE
```

### 任务执行接口

#### 执行任务
```http
POST /api/collector/tasks/1/execute
```

#### 手动执行任务
```http
POST /api/collector/tasks/1/manual-execute
```

#### 暂停任务
```http
POST /api/collector/tasks/1/pause
```

#### 恢复任务
```http
POST /api/collector/tasks/1/resume
```

#### 启用任务
```http
POST /api/collector/tasks/1/enable
```

#### 禁用任务
```http
POST /api/collector/tasks/1/disable
```

### 任务监控接口

#### 获取任务统计
```http
GET /api/collector/tasks/1/statistics
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 1,
    "taskName": "系统健康检查",
    "executionCount": 288,
    "successCount": 275,
    "failureCount": 13,
    "successRate": 95.49,
    "averageExecutionTime": 1250,
    "lastExecutionTime": "2024-01-01T10:00:00",
    "lastExecutionStatus": "SUCCESS"
  }
}
```

#### 获取任务执行历史
```http
GET /api/collector/tasks/1/execution-history?limit=50
```

#### 获取任务执行日志
```http
GET /api/collector/tasks/1/execution-logs?limit=50
```

#### 获取任务概览统计
```http
GET /api/collector/tasks/overview-statistics
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "statusStatistics": {
      "1": 15,
      "0": 3,
      "2": 2
    },
    "scheduleStatistics": {
      "SIMPLE": 12,
      "CRON": 6,
      "EVENT": 2
    },
    "priorityStatistics": {
      "1": 5,
      "2": 8,
      "3": 4,
      "4": 2,
      "5": 1
    }
  }
}
```

#### 获取任务性能排名
```http
GET /api/collector/tasks/performance-ranking?limit=10&date=2024-01-01
```

### 批量操作接口

#### 批量更新任务状态
```http
POST /api/collector/tasks/batch-update-status
Content-Type: application/json

{
  "taskIds": [1, 2, 3],
  "status": 0
}
```

#### 批量启用任务
```http
POST /api/collector/tasks/batch-enable
Content-Type: application/json

{
  "taskIds": [1, 2, 3]
}
```

#### 批量禁用任务
```http
POST /api/collector/tasks/batch-disable
Content-Type: application/json

{
  "taskIds": [1, 2, 3]
}
```

### 配置管理接口

#### 验证任务配置
```http
POST /api/collector/tasks/validate
Content-Type: application/json

{
  "name": "新任务",
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 5
  },
  "targetDevices": [1, 2],
  "metricsConfig": [...]
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "valid": true,
    "errors": [],
    "warnings": []
  }
}
```

#### 测试任务执行
```http
POST /api/collector/tasks/1/test
```

#### 获取任务调度信息
```http
GET /api/collector/tasks/1/schedule-info
```

### 维护接口

#### 清理过期任务数据
```http
POST /api/collector/tasks/cleanup?days=30
```

## 调度配置说明

### 简单调度（SIMPLE）

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

### Cron调度（CRON）

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

### 事件调度（EVENT）

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

## 任务配置示例

### 系统监控任务
```json
{
  "name": "系统性能监控",
  "description": "监控系统CPU、内存、磁盘等关键指标",
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 1
  },
  "targetDevices": [1, 2, 3],
  "metricsConfig": [
    {
      "metricName": "cpu_usage",
      "metricType": "system_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.4.1.2021.11.9.0"
      },
      "timeout": 30
    },
    {
      "metricName": "memory_usage",
      "metricType": "system_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.4.1.2021.4.6.0"
      },
      "timeout": 30
    },
    {
      "metricName": "disk_usage",
      "metricType": "system_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.4.1.2021.9.1.9.1"
      },
      "timeout": 30
    }
  ],
  "priority": 1,
  "timeout": 300,
  "maxConcurrency": 3,
  "enableRetry": true,
  "retryTimes": 3,
  "retryInterval": 1000
}
```

### 网络监控任务
```json
{
  "name": "网络接口监控",
  "description": "监控网络接口流量和状态",
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 5
  },
  "targetDevices": [1, 2],
  "metricsConfig": [
    {
      "metricName": "interface_in_octets",
      "metricType": "network_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.2.1.2.2.1.10"
      }
    },
    {
      "metricName": "interface_out_octets",
      "metricType": "network_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.2.1.2.2.1.16"
      }
    },
    {
      "metricName": "interface_status",
      "metricType": "network_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.2.1.2.2.1.8"
      }
    }
  ],
  "priority": 2,
  "timeout": 120
}
```

### Web服务监控任务
```json
{
  "name": "Web服务健康检查",
  "description": "检查Web服务的响应时间和状态",
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 2
  },
  "targetDevices": [3],
  "metricsConfig": [
    {
      "metricName": "response_time",
      "metricType": "web_metrics",
      "pluginType": "HTTP",
      "parameters": {
        "url": "/health",
        "method": "GET",
        "timeout": 5000
      }
    },
    {
      "metricName": "status_code",
      "metricType": "web_metrics",
      "pluginType": "HTTP",
      "parameters": {
        "url": "/",
        "method": "GET",
        "timeout": 5000
      }
    }
  ],
  "priority": 3,
  "timeout": 60
}
```

## 最佳实践

### 1. 任务设计原则
- **单一职责**：每个任务专注于特定的监控目标
- **合理频率**：根据指标特性设置合适的采集频率
- **优先级管理**：关键任务设置高优先级
- **资源控制**：合理设置超时时间和并发数

### 2. 性能优化
- **批量采集**：将相关指标配置在同一个任务中
- **错峰执行**：避免所有任务同时执行
- **资源监控**：监控任务执行对系统资源的影响
- **定期清理**：定期清理过期的统计数据

### 3. 监控和告警
- **任务状态监控**：监控任务的启用状态和执行状态
- **执行成功率监控**：设置成功率告警阈值
- **执行时间监控**：监控任务执行时间的变化
- **错误日志分析**：定期分析任务执行错误日志

### 4. 维护和运维
- **配置备份**：定期备份任务配置
- **版本管理**：记录任务配置的变更历史
- **测试验证**：重要任务变更前进行测试
- **文档维护**：保持任务配置文档的更新

## 故障排除

### 常见问题

#### 1. 任务执行失败
**症状**：任务执行状态为FAILED
**排查步骤**：
1. 检查目标设备是否在线
2. 验证设备凭据配置
3. 查看详细错误日志
4. 检查网络连通性

#### 2. 任务执行超时
**症状**：任务执行时间超过配置的超时时间
**排查步骤**：
1. 检查网络延迟
2. 优化采集参数
3. 调整超时配置
4. 检查设备性能

#### 3. 任务调度异常
**症状**：任务未按预期时间执行
**排查步骤**：
1. 检查调度配置
2. 验证系统时间
3. 查看调度器状态
4. 检查任务状态

#### 4. 统计数据异常
**症状**：统计数据显示异常值
**排查步骤**：
1. 检查数据采集质量
2. 验证统计计算逻辑
3. 查看数据完整性
4. 检查时间同步

### 日志分析

#### 任务执行日志
```sql
-- 查询最近的失败日志
SELECT * FROM tb_collection_logs 
WHERE task_id = 1 AND success = false 
ORDER BY start_time DESC 
LIMIT 50;

-- 查询任务执行统计
SELECT 
    DATE(stat_date) as date,
    execution_count,
    success_count,
    failure_count,
    success_rate,
    average_execution_time
FROM tb_task_statistics 
WHERE task_id = 1 
ORDER BY stat_date DESC;
```

#### 任务状态查询
```sql
-- 查询所有启用的任务
SELECT id, name, schedule_type, next_execution_time, status
FROM tb_collection_tasks 
WHERE is_enabled = true AND status = 1
ORDER BY priority ASC;

-- 查询需要执行的任务
SELECT id, name, priority, next_execution_time
FROM tb_collection_tasks 
WHERE is_enabled = true AND status = 1 
  AND next_execution_time <= NOW()
ORDER BY priority ASC;
```

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 实现基础任务管理功能
  - 支持简单调度和Cron调度
  - 提供完整的REST API接口
  - 实现任务统计和监控功能
  - 支持批量操作和配置验证
