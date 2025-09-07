# HTTP采集插件使用指南

## 概述

HTTP采集插件是SkyEye智能监控系统中用于收集HTTP/HTTPS服务数据的核心组件。它支持REST API、Web服务、健康检查端点等多种HTTP服务的数据采集，能够监控Web应用程序、微服务、API网关等基于HTTP协议的服务。

## 主要特性

- **多协议支持**：支持HTTP和HTTPS协议
- **灵活认证**：支持Basic Auth、Bearer Token、API Key等认证方式
- **多种响应格式**：支持JSON、XML、纯文本等响应格式解析
- **智能提取**：支持JSONPath、XPath、正则表达式等数据提取方式
- **性能监控**：自动收集响应时间、状态码等性能指标
- **健康检查**：专门的健康检查端点监控
- **批量采集**：支持一次请求采集多个指标
- **错误处理**：完善的错误处理和重试机制

## 支持的指标类型

### 1. 健康检查 (health_check)
监控应用程序健康状态：
- 健康状态判断
- 详细健康信息
- 组件状态检查

### 2. API响应 (api_response)
采集API响应数据：
- JSON数据解析
- XML数据解析
- 响应格式识别
- 字段值提取

### 3. JSON路径提取 (json_path)
使用JSONPath提取特定数据：
- 支持复杂JSONPath表达式
- 多值提取
- 类型自动转换

### 4. XML路径提取 (xml_path)
使用XPath提取XML数据：
- 支持标准XPath语法
- 节点值提取
- 属性值提取

### 5. 正则表达式提取 (regex_extract)
使用正则表达式提取数据：
- 模式匹配
- 捕获组提取
- 多次匹配

### 6. 响应时间 (response_time)
监控HTTP请求性能：
- 请求响应时间
- 网络延迟
- 服务性能评估

### 7. 状态码 (status_code)
监控HTTP状态码：
- 成功率统计
- 错误类型分析
- 可用性监控

### 8. 自定义端点 (custom_endpoint)
自定义端点监控：
- 灵活配置
- 多种解析方式组合
- 期望值验证

## 配置说明

### 基础配置

```json
{
  "pluginType": "HTTP",
  "timeout": 30,
  "retryTimes": 3,
  "retryInterval": 1000,
  "httpConfig": {
    "protocol": "http",
    "port": 80,
    "connectTimeout": 5000,
    "readTimeout": 30000,
    "followRedirects": true,
    "maxRedirects": 5
  }
}
```

### 认证配置

#### 无认证
```json
{
  "auth": {
    "type": "none"
  }
}
```

#### Basic认证
```json
{
  "auth": {
    "type": "basic",
    "username": "admin",
    "password": "password123"
  }
}
```

#### Bearer Token认证
```json
{
  "auth": {
    "type": "bearer",
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
  }
}
```

#### API Key认证
```json
{
  "auth": {
    "type": "apikey",
    "apiKey": "your-api-key-here",
    "apiKeyHeader": "X-API-KEY"
  }
}
```

### SSL配置

```json
{
  "ssl": {
    "verifySSL": true,
    "trustAllCerts": false,
    "keyStore": "/path/to/keystore.jks",
    "keyStorePassword": "password"
  }
}
```

### 指标配置示例

#### 健康检查监控
```json
{
  "metricName": "app_health",
  "metricType": "health_check",
  "displayName": "应用健康检查",
  "description": "监控应用程序健康状态",
  "parameters": {
    "path": "/actuator/health",
    "method": "GET",
    "expectedStatus": "UP"
  },
  "timeout": 30,
  "interval": 60,
  "enabled": true
}
```

#### API响应监控
```json
{
  "metricName": "user_count",
  "metricType": "json_path",
  "displayName": "用户数量",
  "description": "获取系统用户总数",
  "parameters": {
    "path": "/api/v1/stats",
    "method": "GET",
    "jsonPath": "$.data.userCount",
    "headers": {
      "Accept": "application/json"
    }
  },
  "timeout": 30,
  "interval": 300,
  "enabled": true
}
```

#### 自定义API监控
```json
{
  "metricName": "service_status",
  "metricType": "custom_endpoint",
  "displayName": "服务状态",
  "description": "监控服务运行状态",
  "parameters": {
    "path": "/api/status",
    "method": "GET",
    "responseFormat": "json",
    "extractPath": "$.status",
    "expectedValue": "running",
    "queryParams": {
      "format": "json",
      "detail": "true"
    }
  },
  "timeout": 30,
  "interval": 120,
  "enabled": true
}
```

#### POST请求监控
```json
{
  "metricName": "api_test",
  "metricType": "api_response",
  "displayName": "API测试",
  "description": "测试API端点响应",
  "parameters": {
    "path": "/api/v1/test",
    "method": "POST",
    "requestBody": "{\"action\":\"test\"}",
    "headers": {
      "Content-Type": "application/json",
      "Accept": "application/json"
    }
  },
  "timeout": 30,
  "interval": 300,
  "enabled": true
}
```

## API接口使用

### 1. 测试HTTP连接

```http
POST /api/collector/http/test-connection?deviceId=1
Content-Type: application/json

{
  "protocol": "http",
  "port": 8080,
  "auth": {
    "type": "basic",
    "username": "admin",
    "password": "admin123"
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "message": "HTTP连接测试成功",
    "responseTime": 85,
    "testTime": "2024-01-01T10:00:00"
  }
}
```

### 2. 发现设备指标

```http
POST /api/collector/http/discover-metrics?deviceId=1
Content-Type: application/json

{
  "protocol": "http",
  "port": 8080
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "name": "health_check",
      "displayName": "健康检查",
      "description": "检查服务健康状态",
      "type": "health",
      "dataType": "OBJECT",
      "core": true,
      "recommendedInterval": 60
    },
    {
      "name": "response_time",
      "displayName": "响应时间",
      "description": "HTTP请求响应时间",
      "type": "performance",
      "dataType": "LONG",
      "unit": "ms",
      "core": true,
      "recommendedInterval": 60
    }
  ]
}
```

### 3. 执行数据采集

```http
POST /api/collector/http/collect?deviceId=1
Content-Type: application/json

{
  "metricConfig": {
    "metricName": "health_check",
    "metricType": "health_check",
    "parameters": {
      "path": "/health",
      "method": "GET"
    },
    "timeout": 30
  },
  "credentials": {
    "protocol": "http",
    "port": 8080,
    "auth": {
      "type": "none"
    }
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "metrics": {
      "httpStatusCode": 200,
      "responseTime": 120,
      "hasContent": true,
      "isHealthy": true,
      "healthStatus": "UP",
      "healthDetails": {
        "diskSpace": "UP",
        "database": "UP"
      }
    },
    "timestamp": 1704096000000,
    "qualityScore": 100
  }
}
```

### 4. 测试特定端点

```http
POST /api/collector/http/test-endpoint?deviceId=1
Content-Type: application/json

{
  "path": "/api/v1/status",
  "method": "GET",
  "headers": {
    "Accept": "application/json"
  },
  "credentials": {
    "protocol": "http",
    "port": 8080
  }
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "statusCode": 200,
    "responseTime": 95,
    "hasContent": true,
    "contentType": "application/json"
  }
}
```

### 5. 批量数据采集

```http
POST /api/collector/http/collect-batch?deviceId=1
Content-Type: application/json

{
  "metricConfigs": [
    {
      "metricName": "health_check",
      "metricType": "health_check",
      "parameters": {
        "path": "/health"
      }
    },
    {
      "metricName": "user_count",
      "metricType": "json_path",
      "parameters": {
        "path": "/api/stats",
        "jsonPath": "$.userCount"
      }
    }
  ],
  "credentials": {
    "protocol": "http",
    "port": 8080
  }
}
```

### 6. 验证配置

```http
POST /api/collector/http/validate-config
Content-Type: application/json

{
  "metricName": "test_api",
  "metricType": "json_path",
  "parameters": {
    "path": "/api/v1/data",
    "method": "GET",
    "jsonPath": "$.result.value"
  }
}
```

## JSONPath表达式参考

### 基本语法
| 表达式 | 描述 | 示例 |
|--------|------|------|
| `$` | 根对象 | `$.store` |
| `.` | 子对象 | `$.store.book` |
| `[]` | 数组访问 | `$.store.book[0]` |
| `*` | 通配符 | `$.store.*` |
| `..` | 递归下降 | `$..price` |
| `[start:end]` | 数组切片 | `$.store.book[0:2]` |
| `[?()]` | 过滤表达式 | `$.store.book[?(@.price < 10)]` |

### 常用示例
```json
{
  "store": {
    "book": [
      {
        "title": "Java编程",
        "price": 25.99,
        "inStock": true
      },
      {
        "title": "Python入门",
        "price": 19.99,
        "inStock": false
      }
    ],
    "bicycle": {
      "color": "red",
      "price": 299.99
    }
  },
  "stats": {
    "totalBooks": 2,
    "averagePrice": 22.99
  }
}
```

| JSONPath | 结果 |
|----------|------|
| `$.stats.totalBooks` | `2` |
| `$.store.book[0].title` | `"Java编程"` |
| `$.store.book[*].price` | `[25.99, 19.99]` |
| `$..price` | `[25.99, 19.99, 299.99]` |
| `$.store.book[?(@.inStock)].title` | `["Java编程"]` |

## 故障排除

### 常见错误及解决方案

#### 1. 连接超时
**错误信息**：`HTTP连接超时`
**解决方案**：
- 检查设备IP地址和端口
- 确认网络连通性
- 检查防火墙设置
- 增加connectTimeout和readTimeout

#### 2. 认证失败
**错误信息**：`HTTP 401 Unauthorized`
**解决方案**：
- 检查认证类型是否正确
- 验证用户名密码或Token
- 确认API Key配置
- 检查认证头格式

#### 3. 路径不存在
**错误信息**：`HTTP 404 Not Found`
**解决方案**：
- 确认API路径是否正确
- 检查API版本
- 使用指标发现功能探测可用端点
- 查阅API文档

#### 4. JSON解析失败
**错误信息**：`JSON路径解析失败`
**解决方案**：
- 验证JSONPath表达式语法
- 检查响应数据格式
- 确认目标字段存在
- 使用在线JSONPath测试工具

#### 5. SSL证书错误
**错误信息**：`SSL证书验证失败`
**解决方案**：
- 配置正确的证书
- 设置trustAllCerts为true（仅测试环境）
- 检查证书有效期
- 配置自定义TrustStore

### 性能优化建议

1. **合理设置超时时间**：
   - connectTimeout: 5-10秒
   - readTimeout: 30-60秒
   - 根据服务响应特性调整

2. **使用批量采集**：
   - 相同设备的多个指标可以批量采集
   - 减少HTTP连接开销

3. **优化采集间隔**：
   - 健康检查：1-2分钟
   - 性能指标：30秒-1分钟
   - 配置信息：5-10分钟

4. **启用HTTP Keep-Alive**：
   - 复用HTTP连接
   - 减少连接建立开销

## 安全注意事项

1. **凭据管理**：
   - 使用强密码和安全Token
   - 定期轮换API Key
   - 避免在日志中记录敏感信息

2. **网络安全**：
   - 优先使用HTTPS协议
   - 验证SSL证书
   - 限制访问源IP

3. **权限控制**：
   - 使用最小权限原则
   - 避免使用管理员账号
   - 定期审查API权限

4. **数据保护**：
   - 避免记录敏感响应数据
   - 使用安全的存储方式
   - 遵循数据保护法规

## 最佳实践

1. **监控策略**：
   - 分层监控：基础可用性 -> 性能指标 -> 业务指标
   - 关键路径优先
   - 设置合理的报警阈值

2. **配置管理**：
   - 使用配置模板
   - 版本控制配置文件
   - 环境隔离

3. **运维管理**：
   - 监控插件本身的健康状态
   - 定期检查插件统计信息
   - 及时处理采集错误

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 支持HTTP/HTTPS协议
  - 实现多种认证方式
  - 提供完整的API接口
  - 支持多种数据提取方式
