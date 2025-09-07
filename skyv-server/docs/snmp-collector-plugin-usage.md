# SNMP采集插件使用指南

## 概述

SNMP采集插件是SkyEye智能监控系统中用于收集网络设备数据的核心组件。它支持SNMPv1、SNMPv2c和SNMPv3协议，能够从路由器、交换机、服务器等网络设备中采集系统信息、性能指标和状态数据。

## 主要特性

- **多版本支持**：支持SNMPv1、SNMPv2c、SNMPv3协议
- **安全认证**：SNMPv3支持用户认证和数据加密
- **智能发现**：自动发现设备支持的指标类型
- **批量采集**：支持一次请求采集多个指标
- **会话复用**：智能缓存SNMP会话，提高性能
- **错误处理**：完善的错误处理和重试机制
- **配置验证**：实时验证配置参数的有效性

## 支持的指标类型

### 1. 系统信息 (system_info)
采集设备基本系统信息：
- 系统描述 (sysDescr)
- 系统名称 (sysName)
- 系统位置 (sysLocation)
- 系统联系人 (sysContact)
- 系统运行时间 (sysUpTime)

### 2. 接口统计 (interface_stats)
采集网络接口流量统计：
- 接口数量 (ifNumber)
- 入站字节数 (ifInOctets)
- 出站字节数 (ifOutOctets)
- 入站包数 (ifInUcastPkts)
- 出站包数 (ifOutUcastPkts)

### 3. CPU使用率 (cpu_usage)
采集设备CPU使用情况：
- CPU使用百分比
- 支持多种厂商的CPU指标OID

### 4. 内存使用率 (memory_usage)
采集设备内存使用情况：
- 总内存容量
- 已使用内存
- 可用内存
- 内存使用百分比

### 5. 存储使用率 (storage_usage)
采集设备存储使用情况：
- 存储总容量
- 已使用存储
- 存储使用百分比

### 6. 网络流量 (network_traffic)
采集接口流量信息：
- 接口速度
- 接口状态
- 接口描述
- 实时流量统计

### 7. 自定义OID (custom_oid)
采集自定义OID数据：
- 灵活指定任意OID
- 支持多种数据类型解析
- 自定义数据转换

## 配置说明

### 基础配置

```json
{
  "pluginType": "SNMP",
  "timeout": 30,
  "retryTimes": 3,
  "retryInterval": 1000,
  "snmpConfig": {
    "version": "v2c",
    "community": "public",
    "port": 161,
    "maxSizeRequestPDU": 65535
  }
}
```

### SNMPv1/v2c配置

```json
{
  "version": "v2c",
  "community": "public",
  "port": 161
}
```

### SNMPv3配置

```json
{
  "version": "v3",
  "port": 161,
  "v3Config": {
    "username": "snmpuser",
    "authProtocol": "MD5",
    "authPassword": "authpass123",
    "privProtocol": "DES",
    "privPassword": "privpass123"
  }
}
```

### 指标配置示例

#### 系统信息采集
```json
{
  "metricName": "system_info",
  "metricType": "system_info",
  "displayName": "系统信息",
  "description": "采集设备系统基本信息",
  "timeout": 30,
  "interval": 300,
  "enabled": true
}
```

#### 接口流量采集
```json
{
  "metricName": "interface_traffic",
  "metricType": "interface_stats",
  "displayName": "接口流量统计",
  "description": "采集网络接口流量数据",
  "parameters": {
    "interfaceIndex": "1"
  },
  "timeout": 30,
  "interval": 60,
  "enabled": true
}
```

#### 自定义OID采集
```json
{
  "metricName": "custom_metric",
  "metricType": "custom_oid",
  "displayName": "自定义指标",
  "description": "采集自定义OID数据",
  "parameters": {
    "oid": "1.3.6.1.2.1.1.1.0",
    "dataType": "STRING"
  },
  "timeout": 30,
  "interval": 60,
  "enabled": true
}
```

## API接口使用

### 1. 测试SNMP连接

```http
POST /api/collector/snmp/test-connection?deviceId=1
Content-Type: application/json

{
  "version": "v2c",
  "community": "public",
  "port": 161
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": {
    "success": true,
    "message": "SNMP连接测试成功",
    "responseTime": 120,
    "testTime": "2024-01-01T10:00:00"
  }
}
```

### 2. 发现设备指标

```http
POST /api/collector/snmp/discover-metrics?deviceId=1
Content-Type: application/json

{
  "version": "v2c",
  "community": "public"
}
```

**响应示例：**
```json
{
  "code": 200,
  "message": "操作成功",
  "data": [
    {
      "name": "system_info",
      "displayName": "系统信息",
      "description": "获取设备系统基本信息",
      "type": "system",
      "dataType": "OBJECT",
      "core": true,
      "complexity": 1,
      "recommendedInterval": 300
    }
  ]
}
```

### 3. 执行数据采集

```http
POST /api/collector/snmp/collect?deviceId=1
Content-Type: application/json

{
  "metricConfig": {
    "metricName": "system_info",
    "metricType": "system_info",
    "timeout": 30
  },
  "credentials": {
    "version": "v2c",
    "community": "public"
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
      "sysDescr": "Linux version 4.15.0",
      "sysName": "server01",
      "sysLocation": "Data Center A",
      "sysUpTime": 86400,
      "sysUpTimeFormatted": "1天 0小时 0分钟 0秒"
    },
    "timestamp": 1704096000000,
    "duration": 150,
    "qualityScore": 100
  }
}
```

### 4. 批量数据采集

```http
POST /api/collector/snmp/collect-batch?deviceId=1
Content-Type: application/json

{
  "metricConfigs": [
    {
      "metricName": "system_info",
      "metricType": "system_info"
    },
    {
      "metricName": "cpu_usage",
      "metricType": "cpu_usage"
    }
  ],
  "credentials": {
    "version": "v2c",
    "community": "public"
  }
}
```

### 5. 验证配置

```http
POST /api/collector/snmp/validate-config
Content-Type: application/json

{
  "metricName": "custom_metric",
  "metricType": "custom_oid",
  "parameters": {
    "oid": "1.3.6.1.2.1.1.1.0",
    "dataType": "STRING"
  }
}
```

## 常见OID参考

### 系统信息OID
| 描述 | OID |
|------|-----|
| 系统描述 | 1.3.6.1.2.1.1.1.0 |
| 系统运行时间 | 1.3.6.1.2.1.1.3.0 |
| 系统名称 | 1.3.6.1.2.1.1.5.0 |
| 系统位置 | 1.3.6.1.2.1.1.6.0 |

### 接口统计OID
| 描述 | OID |
|------|-----|
| 接口数量 | 1.3.6.1.2.1.2.1.0 |
| 接口入站字节 | 1.3.6.1.2.1.2.2.1.10.{index} |
| 接口出站字节 | 1.3.6.1.2.1.2.2.1.16.{index} |
| 接口状态 | 1.3.6.1.2.1.2.2.1.8.{index} |

### CPU和内存OID
| 描述 | OID |
|------|-----|
| CPU使用率 | 1.3.6.1.2.1.25.3.3.1.2 |
| 物理内存总量 | 1.3.6.1.4.1.2021.4.5.0 |
| 可用内存 | 1.3.6.1.4.1.2021.4.6.0 |

## 故障排除

### 常见错误及解决方案

#### 1. 连接超时
**错误信息**：`SNMP连接超时`
**解决方案**：
- 检查设备IP地址和端口
- 确认网络连通性
- 检查防火墙设置
- 增加超时时间

#### 2. 认证失败
**错误信息**：`SNMP认证失败`
**解决方案**：
- 检查社区字符串是否正确
- 验证SNMPv3用户名和密码
- 确认设备SNMP服务已启用

#### 3. OID不存在
**错误信息**：`指定的OID不存在`
**解决方案**：
- 使用指标发现功能探测可用OID
- 查阅设备MIB文档
- 验证OID格式是否正确

#### 4. 权限不足
**错误信息**：`SNMP权限不足`
**解决方案**：
- 检查社区字符串的读权限
- 确认SNMPv3用户权限配置
- 联系设备管理员调整SNMP配置

### 性能优化建议

1. **合理设置采集间隔**：
   - 系统信息：5-10分钟
   - 性能指标：1-5分钟
   - 流量统计：30秒-1分钟

2. **使用批量采集**：
   - 相同设备的多个指标可以批量采集
   - 减少网络开销和设备负载

3. **启用会话复用**：
   - 默认启用，避免频繁建立连接
   - 合理设置会话缓存时间

4. **优化超时和重试**：
   - 根据网络状况调整超时时间
   - 设置合适的重试次数和间隔

## 安全注意事项

1. **凭据管理**：
   - 使用强密码和复杂社区字符串
   - 定期更换SNMP凭据
   - 避免在日志中记录敏感信息

2. **网络安全**：
   - 限制SNMP访问的源IP地址
   - 使用SNMPv3加密传输
   - 监控SNMP访问日志

3. **权限控制**：
   - 使用只读社区字符串
   - 避免给予写权限
   - 定期审查SNMP用户权限

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 支持SNMPv1/v2c/v3协议
  - 实现基础指标采集功能
  - 提供完整的API接口
