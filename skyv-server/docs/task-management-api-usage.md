# 任务管理API接口使用指南

## 概述

任务管理API接口是SkyEye智能监控系统的重要组成部分，提供了完整的任务生命周期管理功能。通过这些接口，用户可以创建、配置、监控和管理各种数据采集任务，实现自动化数据采集和监控。

## API基础信息

### 基础路径
```
/api/collector/tasks
```

### 认证要求
- 所有接口都需要用户认证
- 使用JWT Token进行身份验证
- 需要相应的权限才能访问特定接口

### 响应格式
所有API接口都使用统一的响应格式：
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    // 响应数据
  }
}
```

## 核心功能接口

### 1. 任务CRUD操作

#### 1.1 创建任务
**接口地址：** `POST /api/collector/tasks`

**权限要求：** `task:create`

**请求示例：**
```json
{
  "name": "系统性能监控任务",
  "description": "监控系统CPU、内存、磁盘等关键指标",
  "scheduleType": "SIMPLE",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 5
  },
  "targetDevices": [1, 2, 3],
  "metricsConfig": [
    {
      "name": "cpu_usage",
      "type": "system_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.4.1.2021.11.9.0"
      }
    },
    {
      "name": "memory_usage",
      "type": "system_metrics",
      "pluginType": "SNMP",
      "parameters": {
        "oid": "1.3.6.1.4.1.2021.4.6.0"
      }
    }
  ],
  "priority": 1,
  "enableRetry": true,
  "retryTimes": 3,
  "retryInterval": 1000,
  "timeout": 300,
  "maxConcurrency": 5,
  "tags": ["system", "performance"],
  "parameters": {
    "collectionTimeout": 60,
    "dataRetention": 30
  },
  "remarks": "系统关键指标监控任务"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "系统性能监控任务",
    "description": "监控系统CPU、内存、磁盘等关键指标",
    "scheduleType": "SIMPLE",
    "scheduleConfig": {
      "frequency": "minutes",
      "interval": 5
    },
    "targetDevices": [1, 2, 3],
    "metricsConfig": [...],
    "priority": 1,
    "status": 1,
    "isEnabled": true,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

#### 1.2 更新任务
**接口地址：** `PUT /api/collector/tasks/{id}`

**权限要求：** `task:update`

**请求示例：**
```json
{
  "description": "更新后的任务描述",
  "scheduleConfig": {
    "frequency": "minutes",
    "interval": 10
  },
  "priority": 2,
  "tags": ["system", "performance", "updated"]
}
```

#### 1.3 删除任务
**接口地址：** `DELETE /api/collector/tasks/{id}`

**权限要求：** `task:delete`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": null
}
```

#### 1.4 获取任务列表
**接口地址：** `GET /api/collector/tasks`

**权限要求：** `task:view`

**查询参数：**
- `page`: 页码（默认1）
- `size`: 每页大小（默认20）
- `name`: 任务名称（模糊查询）
- `status`: 任务状态
- `scheduleType`: 调度类型
- `collectorId`: 采集器ID
- `targetDeviceId`: 目标设备ID
- `isEnabled`: 是否启用
- `minPriority`: 最小优先级
- `maxPriority`: 最大优先级
- `startTime`: 开始时间
- `endTime`: 结束时间
- `tags`: 标签（JSON数组）
- `sortBy`: 排序字段
- `sortOrder`: 排序方向

**请求示例：**
```http
GET /api/collector/tasks?page=1&size=10&status=1&scheduleType=SIMPLE&sortBy=createdAt&sortOrder=DESC
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "name": "系统性能监控任务",
        "description": "监控系统CPU、内存、磁盘等关键指标",
        "scheduleType": "SIMPLE",
        "status": 1,
        "isEnabled": true,
        "priority": 1,
        "createdAt": "2024-01-01T10:00:00"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 10,
    "number": 0
  }
}
```

#### 1.5 获取任务详情
**接口地址：** `GET /api/collector/tasks/{id}`

**权限要求：** `task:view`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "id": 1,
    "name": "系统性能监控任务",
    "description": "监控系统CPU、内存、磁盘等关键指标",
    "collectorId": 1,
    "scheduleType": "SIMPLE",
    "scheduleConfig": {
      "frequency": "minutes",
      "interval": 5
    },
    "targetDevices": [1, 2, 3],
    "metricsConfig": [...],
    "status": 1,
    "priority": 1,
    "enableRetry": true,
    "retryTimes": 3,
    "retryInterval": 1000,
    "timeout": 300,
    "maxConcurrency": 5,
    "lastExecutionTime": "2024-01-01T10:00:00",
    "nextExecutionTime": "2024-01-01T10:05:00",
    "executionCount": 12,
    "successCount": 11,
    "failureCount": 1,
    "averageExecutionTime": 1250,
    "lastExecutionStatus": "SUCCESS",
    "lastExecutionError": null,
    "tags": ["system", "performance"],
    "parameters": {...},
    "isEnabled": true,
    "remarks": "系统关键指标监控任务",
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T10:00:00"
  }
}
```

### 2. 任务执行控制

#### 2.1 执行任务
**接口地址：** `POST /api/collector/tasks/{id}/execute`

**权限要求：** `task:execute`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 1,
    "taskName": "系统性能监控任务",
    "executionId": "exec_20240101_100000_001",
    "status": "RUNNING",
    "result": "SUCCESS",
    "startTime": "2024-01-01T10:00:00",
    "progress": 0,
    "dataCount": 0,
    "successDataCount": 0,
    "failedDataCount": 0
  }
}
```

#### 2.2 手动执行任务
**接口地址：** `POST /api/collector/tasks/{id}/manual-execute`

**权限要求：** `task:execute`

**说明：** 手动执行任务会立即执行，不受调度时间限制

#### 2.3 测试任务执行
**接口地址：** `POST /api/collector/tasks/{id}/test`

**权限要求：** `task:execute`

**说明：** 测试任务执行不会影响正式数据，用于验证任务配置

### 3. 任务状态管理

#### 3.1 暂停任务
**接口地址：** `POST /api/collector/tasks/{id}/pause`

**权限要求：** `task:update`

**说明：** 暂停任务后，任务将不会按计划执行，但可以手动执行

#### 3.2 恢复任务
**接口地址：** `POST /api/collector/tasks/{id}/resume`

**权限要求：** `task:update`

**说明：** 恢复暂停的任务，任务将重新按计划执行

#### 3.3 启用任务
**接口地址：** `POST /api/collector/tasks/{id}/enable`

**权限要求：** `task:update`

**说明：** 启用禁用的任务

#### 3.4 禁用任务
**接口地址：** `POST /api/collector/tasks/{id}/disable`

**权限要求：** `task:update`

**说明：** 禁用任务，任务将不会执行

#### 3.5 更新任务状态
**接口地址：** `PUT /api/collector/tasks/{id}/status`

**权限要求：** `task:update`

**请求示例：**
```json
{
  "status": 0,
  "reason": "系统维护",
  "operatorId": 1,
  "operatorName": "admin",
  "remarks": "临时禁用任务进行系统维护"
}
```

### 4. 批量操作

#### 4.1 批量更新任务状态
**接口地址：** `POST /api/collector/tasks/batch-update-status`

**权限要求：** `task:update`

**请求示例：**
```json
{
  "taskIds": [1, 2, 3],
  "status": 0,
  "reason": "批量禁用任务"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalCount": 3,
    "successCount": 3,
    "failedCount": 0,
    "failedTaskIds": [],
    "message": "批量更新任务状态成功"
  }
}
```

#### 4.2 批量启用任务
**接口地址：** `POST /api/collector/tasks/batch-enable`

**权限要求：** `task:update`

**请求示例：**
```json
[1, 2, 3]
```

#### 4.3 批量禁用任务
**接口地址：** `POST /api/collector/tasks/batch-disable`

**权限要求：** `task:update`

**请求示例：**
```json
[1, 2, 3]
```

### 5. 任务监控和统计

#### 5.1 获取任务统计信息
**接口地址：** `GET /api/collector/tasks/{id}/statistics`

**权限要求：** `task:view`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 1,
    "taskName": "系统性能监控任务",
    "totalExecutions": 288,
    "successExecutions": 275,
    "failedExecutions": 13,
    "successRate": 95.49,
    "averageExecutionTime": 1250,
    "minExecutionTime": 800,
    "maxExecutionTime": 3000,
    "totalDataCount": 8640,
    "averageDataCount": 30,
    "lastExecutionTime": "2024-01-01T15:30:00",
    "nextExecutionTime": "2024-01-01T15:35:00",
    "uptime": "5小时30分钟",
    "status": "RUNNING"
  }
}
```

#### 5.2 获取任务执行历史
**接口地址：** `GET /api/collector/tasks/{id}/execution-history`

**权限要求：** `task:view`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "executionId": "exec_20240101_150000_001",
      "startTime": "2024-01-01T15:00:00",
      "endTime": "2024-01-01T15:00:02",
      "status": "SUCCESS",
      "executionTime": 2000,
      "dataCount": 30,
      "errorMessage": null
    },
    {
      "executionId": "exec_20240101_145500_001",
      "startTime": "2024-01-01T14:55:00",
      "endTime": "2024-01-01T14:55:01",
      "status": "SUCCESS",
      "executionTime": 1500,
      "dataCount": 30,
      "errorMessage": null
    }
  ]
}
```

#### 5.3 获取任务执行日志
**接口地址：** `GET /api/collector/tasks/{id}/execution-logs`

**权限要求：** `task:view`

**查询参数：**
- `page`: 页码
- `size`: 每页大小
- `status`: 执行状态
- `startTime`: 开始时间
- `endTime`: 结束时间
- `result`: 执行结果
- `errorMessage`: 错误信息
- `sortBy`: 排序字段
- `sortOrder`: 排序方向

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "content": [
      {
        "id": 1,
        "taskId": 1,
        "executionId": "exec_20240101_150000_001",
        "status": "SUCCESS",
        "result": "SUCCESS",
        "startTime": "2024-01-01T15:00:00",
        "endTime": "2024-01-01T15:00:02",
        "executionTime": 2000,
        "dataCount": 30,
        "errorMessage": null,
        "createdAt": "2024-01-01T15:00:02"
      }
    ],
    "totalElements": 1,
    "totalPages": 1,
    "size": 20,
    "number": 0
  }
}
```

#### 5.4 获取任务概览统计
**接口地址：** `GET /api/collector/tasks/overview-statistics`

**权限要求：** `task:view`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "totalTasks": 15,
    "enabledTasks": 12,
    "disabledTasks": 3,
    "runningTasks": 2,
    "pausedTasks": 1,
    "todayExecutions": 288,
    "todaySuccesses": 275,
    "todayFailures": 13,
    "successRate": 95.49,
    "averageExecutionTime": 1250,
    "totalDataCollected": 8640,
    "activeCollectors": 5,
    "activeDevices": 25
  }
}
```

#### 5.5 获取任务性能排名
**接口地址：** `GET /api/collector/tasks/performance-ranking`

**权限要求：** `task:view`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "rank": 1,
      "taskId": 1,
      "taskName": "系统性能监控任务",
      "successRate": 98.5,
      "averageExecutionTime": 1200,
      "totalExecutions": 288,
      "totalDataCount": 8640
    },
    {
      "rank": 2,
      "taskId": 2,
      "taskName": "网络监控任务",
      "successRate": 96.2,
      "averageExecutionTime": 1500,
      "totalExecutions": 144,
      "totalDataCount": 4320
    }
  ]
}
```

### 6. 任务配置和验证

#### 6.1 验证任务配置
**接口地址：** `POST /api/collector/tasks/validate`

**权限要求：** `task:create`

**说明：** 验证任务配置是否正确，不会创建实际任务

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "valid": true,
    "message": "任务配置验证通过",
    "warnings": [],
    "errors": [],
    "suggestions": [
      "建议设置任务超时时间为300秒",
      "建议启用任务重试机制"
    ]
  }
}
```

#### 6.2 获取任务调度信息
**接口地址：** `GET /api/collector/tasks/{id}/schedule-info`

**权限要求：** `task:view`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 1,
    "taskName": "系统性能监控任务",
    "scheduleType": "SIMPLE",
    "scheduleConfig": {
      "frequency": "minutes",
      "interval": 5
    },
    "status": 1,
    "isEnabled": true,
    "priority": 1,
    "lastExecutionTime": "2024-01-01T15:00:00",
    "nextExecutionTime": "2024-01-01T15:05:00",
    "executionCount": 288,
    "successCount": 275,
    "failureCount": 13,
    "averageExecutionTime": 1250,
    "lastExecutionStatus": "SUCCESS",
    "lastExecutionError": null,
    "createdAt": "2024-01-01T10:00:00",
    "updatedAt": "2024-01-01T15:00:00"
  }
}
```

#### 6.3 获取任务执行状态
**接口地址：** `GET /api/collector/tasks/{id}/execution-status`

**权限要求：** `task:view`

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "taskId": 1,
    "taskName": "系统性能监控任务",
    "status": 1,
    "isEnabled": true,
    "lastExecutionTime": "2024-01-01T15:00:00",
    "nextExecutionTime": "2024-01-01T15:05:00",
    "executionCount": 288,
    "successCount": 275,
    "failureCount": 13,
    "lastExecutionStatus": "SUCCESS",
    "lastExecutionError": null,
    "currentTime": "2024-01-01T15:02:30"
  }
}
```

### 7. 系统维护

#### 7.1 清理过期任务数据
**接口地址：** `POST /api/collector/tasks/cleanup`

**权限要求：** `task:manage`

**说明：** 清理过期的任务执行日志和统计数据

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "cleanedLogs": 1500,
    "cleanedStatistics": 45,
    "freedStorage": "2.5GB",
    "message": "清理过期任务数据成功"
  }
}
```

## 错误处理

### 常见错误码

| 错误码 | 说明 | 解决方案 |
|--------|------|----------|
| 400 | 请求参数错误 | 检查请求参数格式和必填字段 |
| 401 | 未授权访问 | 检查用户认证和Token有效性 |
| 403 | 权限不足 | 检查用户权限配置 |
| 404 | 资源不存在 | 检查任务ID是否正确 |
| 409 | 资源冲突 | 检查任务名称是否重复 |
| 500 | 服务器内部错误 | 查看服务器日志，联系管理员 |

### 错误响应示例

```json
{
  "code": 400,
  "message": "请求参数错误",
  "errors": [
    {
      "field": "name",
      "message": "任务名称不能为空"
    },
    {
      "field": "scheduleType",
      "message": "不支持的调度类型: INVALID"
    }
  ]
}
```

## 最佳实践

### 1. 任务命名规范
- 使用描述性的名称，如"系统性能监控任务"
- 包含任务类型和监控目标
- 避免使用特殊字符和空格

### 2. 调度配置优化
- 根据指标特性设置合适的采集频率
- 避免同时执行大量任务
- 使用错峰执行策略

### 3. 权限管理
- 为不同角色分配适当的权限
- 定期审查权限配置
- 使用最小权限原则

### 4. 监控和告警
- 设置任务执行成功率告警
- 监控任务执行时间
- 定期检查任务状态

### 5. 数据管理
- 定期清理过期数据
- 设置合理的数据保留策略
- 监控存储空间使用情况

## 集成示例

### 1. 前端集成

```javascript
// 创建任务
async function createTask(taskData) {
  try {
    const response = await fetch('/api/collector/tasks', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Authorization': `Bearer ${token}`
      },
      body: JSON.stringify(taskData)
    });
    
    const result = await response.json();
    if (result.code === 200) {
      console.log('任务创建成功:', result.data);
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('创建任务失败:', error);
    throw error;
  }
}

// 获取任务列表
async function getTasks(params = {}) {
  try {
    const queryString = new URLSearchParams(params).toString();
    const response = await fetch(`/api/collector/tasks?${queryString}`, {
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    if (result.code === 200) {
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('获取任务列表失败:', error);
    throw error;
  }
}

// 执行任务
async function executeTask(taskId) {
  try {
    const response = await fetch(`/api/collector/tasks/${taskId}/execute`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      }
    });
    
    const result = await response.json();
    if (result.code === 200) {
      console.log('任务执行成功:', result.data);
      return result.data;
    } else {
      throw new Error(result.message);
    }
  } catch (error) {
    console.error('执行任务失败:', error);
    throw error;
  }
}
```

### 2. 后端集成

```java
@RestController
@RequestMapping("/api/integration")
public class IntegrationController {
    
    @Autowired
    private CollectionTaskService taskService;
    
    @PostMapping("/create-monitoring-task")
    public ApiResponse<CollectionTask> createMonitoringTask(@RequestBody Map<String, Object> request) {
        try {
            // 构建任务创建请求
            TaskCreateRequest taskRequest = new TaskCreateRequest();
            taskRequest.setName((String) request.get("name"));
            taskRequest.setScheduleType("SIMPLE");
            taskRequest.setScheduleConfig(Map.of(
                "frequency", "minutes",
                "interval", 5
            ));
            
            // 创建任务
            CollectionTask task = taskService.createTask(taskRequest);
            return ApiResponse.success(task);
            
        } catch (Exception e) {
            return ApiResponse.error(500, "创建监控任务失败: " + e.getMessage());
        }
    }
    
    @GetMapping("/task-status/{taskId}")
    public ApiResponse<Map<String, Object>> getTaskStatus(@PathVariable Long taskId) {
        try {
            CollectionTask task = taskService.getTaskById(taskId);
            Map<String, Object> status = Map.of(
                "taskId", task.getId(),
                "name", task.getName(),
                "status", task.getStatus(),
                "isEnabled", task.getIsEnabled(),
                "lastExecutionTime", task.getLastExecutionTime(),
                "nextExecutionTime", task.getNextExecutionTime()
            );
            return ApiResponse.success(status);
            
        } catch (Exception e) {
            return ApiResponse.error(500, "获取任务状态失败: " + e.getMessage());
        }
    }
}
```

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 实现完整的任务CRUD操作
  - 支持任务执行控制
  - 提供任务监控和统计功能
  - 实现批量操作接口
  - 支持任务配置验证
  - 提供系统维护接口
  - 完整的权限控制和错误处理
