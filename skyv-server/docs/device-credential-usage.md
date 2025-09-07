# 设备凭据加密存储功能使用说明

## 功能概述

本功能实现了设备登录凭据的安全存储和管理，使用AES-256-GCM算法对敏感凭据数据进行加密，支持密钥轮换和多种凭据类型。

## 主要特性

- **安全加密存储**：使用AES-256-GCM算法加密凭据数据
- **密钥管理**：支持密钥版本控制和安全轮换
- **多协议支持**：支持SNMP、HTTP、SSH等多种协议凭据
- **默认凭据管理**：支持设置设备默认凭据
- **凭据复制**：支持将凭据复制到多个设备
- **安全审计**：完整的操作日志记录

## API接口说明

### 1. 设备凭据管理

#### 创建设备凭据
```http
POST /api/devices/{deviceId}/credentials
Content-Type: application/json

{
    "credentialName": "SNMP默认凭据",
    "credentialType": "USERNAME_PASSWORD",
    "protocolType": "SNMP",
    "credentialData": {
        "username": "admin",
        "password": "password123",
        "community": "public",
        "version": "v2c"
    },
    "isDefault": true,
    "remark": "设备SNMP访问凭据"
}
```

#### 获取设备凭据列表
```http
GET /api/devices/{deviceId}/credentials
```

#### 获取设备默认凭据数据
```http
GET /api/devices/{deviceId}/credentials/default
GET /api/devices/{deviceId}/credentials/default/SNMP
```

### 2. 凭据详细操作

#### 获取凭据详细信息（包含解密数据）
```http
GET /api/credentials/{credentialId}/data
```

#### 更新凭据
```http
PUT /api/credentials/{credentialId}
Content-Type: application/json

{
    "credentialName": "更新后的凭据名称",
    "credentialData": {
        "username": "newuser",
        "password": "newpassword"
    }
}
```

#### 设置为默认凭据
```http
PUT /api/credentials/{credentialId}/default
```

#### 测试凭据连接
```http
POST /api/credentials/{credentialId}/test
```

#### 复制凭据到其他设备
```http
POST /api/credentials/{credentialId}/copy
Content-Type: application/json

[2, 3, 4]  // 目标设备ID列表
```

### 3. 加密密钥管理

#### 获取当前活跃密钥
```http
GET /api/encryption-keys/current
```

#### 创建新密钥
```http
POST /api/encryption-keys?keyName=new-key-2024
```

#### 轮换密钥
```http
POST /api/encryption-keys/{keyId}/rotate
```

## 数据结构说明

### 凭据类型 (credentialType)
- `USERNAME_PASSWORD`: 用户名密码类型
- `API_KEY`: API密钥类型
- `CERTIFICATE`: 证书类型
- `TOKEN`: 令牌类型

### 协议类型 (protocolType)
- `SNMP`: 简单网络管理协议
- `HTTP`: 超文本传输协议
- `SSH`: 安全外壳协议
- `TELNET`: 远程登录协议
- `FTP`: 文件传输协议

### 凭据数据格式

#### SNMP凭据
```json
{
    "version": "v2c|v3",
    "community": "public",
    "username": "snmpuser",
    "password": "snmppass",
    "authProtocol": "MD5|SHA",
    "privProtocol": "DES|AES",
    "privPassword": "privpass"
}
```

#### HTTP凭据
```json
{
    "authType": "basic|bearer|api_key",
    "username": "httpuser",
    "password": "httppass",
    "token": "bearer_token",
    "apiKey": "api_key_value",
    "headers": {
        "X-API-Key": "key_value"
    }
}
```

#### SSH凭据
```json
{
    "username": "sshuser",
    "password": "sshpass",
    "privateKey": "-----BEGIN RSA PRIVATE KEY-----...",
    "passphrase": "key_passphrase",
    "port": 22
}
```

## 安全注意事项

1. **密钥安全**：
   - 主密钥应存储在安全的密钥管理服务中
   - 定期轮换加密密钥
   - 监控密钥使用情况

2. **访问控制**：
   - 严格控制凭据解密权限
   - 记录所有敏感操作日志
   - 定期审计权限分配

3. **数据传输**：
   - 凭据数据传输必须使用HTTPS
   - 避免在日志中记录敏感信息
   - 客户端不应缓存解密后的凭据

## 配置说明

### 应用配置
```yaml
skyeye:
  security:
    # 主密钥（生产环境应使用环境变量或密钥管理服务）
    master-key: ${SKYEYE_MASTER_KEY:SkyEye-Master-Key-2024-Default}
    # 密钥轮换周期（天）
    key-rotation-days: 90
```

### 权限配置
确保用户具有以下权限：
- `credential:create` - 创建凭据
- `credential:view` - 查看凭据
- `credential:update` - 更新凭据
- `credential:delete` - 删除凭据
- `credential:decrypt` - 解密凭据数据
- `credential:test` - 测试凭据
- `encryption_key:*` - 密钥管理权限（仅管理员）

## 使用示例

### Java客户端示例
```java
// 创建SNMP凭据
CredentialRequest request = new CredentialRequest();
request.setCredentialName("SNMP默认凭据");
request.setCredentialType("USERNAME_PASSWORD");
request.setProtocolType("SNMP");

Map<String, Object> credentialData = new HashMap<>();
credentialData.put("version", "v2c");
credentialData.put("community", "public");
request.setCredentialData(credentialData);
request.setIsDefault(true);

// 调用API创建凭据
ApiResponse<DeviceCredential> response = credentialService.saveCredential(deviceId, request);
```

### 获取并使用凭据
```java
// 获取设备默认SNMP凭据
Map<String, Object> credentials = credentialService.getDefaultCredential(deviceId, "SNMP");
String community = (String) credentials.get("community");
String version = (String) credentials.get("version");

// 使用凭据进行SNMP操作
// ...
```

## 故障排除

### 常见问题

1. **凭据解密失败**
   - 检查加密密钥是否有效
   - 确认密钥版本匹配
   - 查看错误日志获取详细信息

2. **密钥轮换失败**
   - 确保有足够的权限
   - 检查是否有凭据正在使用旧密钥
   - 查看轮换操作日志

3. **凭据测试失败**
   - 验证凭据数据格式是否正确
   - 检查网络连接
   - 确认目标设备配置

### 日志示例
```
2024-01-01 10:00:00 INFO  - 保存设备凭据成功: deviceId=1, credentialName=SNMP默认凭据
2024-01-01 10:01:00 INFO  - 设置默认凭据成功: credentialId=1, deviceId=1
2024-01-01 10:02:00 ERROR - 解密凭据数据失败: credentialId=1
```

## 版本更新记录

- **v1.0.0** (2024-01-01)
  - 初始版本发布
  - 支持基本凭据管理功能
  - 实现AES-256-GCM加密算法
  - 支持密钥轮换机制
