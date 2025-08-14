# SkyEye 智能监控系统 - 后端服务

## 项目概述

SkyEye智能监控系统是一套综合性的智能监控解决方案，集成了设备管理、实时监控、智能分析、报警管理等功能。本项目为后端服务部分，基于Spring Boot 3.x构建。

## 技术栈

- **Java**: 17
- **Spring Boot**: 3.2.0
- **Spring Data JPA**: 数据访问层
- **Spring Security**: 安全认证
- **Spring Cache**: 缓存支持
- **PostgreSQL**: 主数据库
- **Redis**: 缓存数据库
- **JWT**: 身份认证
- **Lombok**: 代码简化
- **Jackson**: JSON处理
- **Maven**: 依赖管理

## 项目结构

```
src/main/java/com/skyeye/
├── SkyEyeApplication.java          # 主应用程序入口
├── common/                         # 公共模块
│   ├── config/                     # 配置类
│   │   ├── DatabaseConfig.java     # 数据库配置
│   │   ├── RedisConfig.java        # Redis配置
│   │   └── JpaConfig.java          # JPA配置
│   ├── constant/                   # 常量定义
│   │   ├── Constants.java          # 系统常量
│   │   ├── DeviceStatus.java       # 设备状态枚举
│   │   └── AlertLevel.java         # 报警级别枚举
│   ├── exception/                  # 异常处理
│   │   ├── BusinessException.java  # 业务异常
│   │   └── GlobalExceptionHandler.java # 全局异常处理器
│   ├── response/                   # 响应封装
│   │   ├── ApiResponse.java        # 统一API响应格式
│   │   └── ResponseCode.java       # 响应码枚举
│   ├── util/                       # 工具类
│   │   ├── JsonUtils.java          # JSON工具类
│   │   └── DateUtils.java          # 日期工具类
│   └── controller/                 # 公共控制器
│       └── HealthController.java   # 健康检查控制器
├── security/                       # 安全模块
├── auth/                          # 认证模块
├── user/                          # 用户管理模块
├── device/                        # 设备管理模块
├── collector/                     # 数据采集模块
├── monitoring/                    # 监控管理模块
├── alert/                         # 报警管理模块
├── analytics/                     # 数据分析模块
├── task/                          # 任务调度模块
├── history/                       # 历史记录模块
└── system/                        # 系统配置模块
```

## 配置说明

### 数据库配置

系统支持多环境配置：

- **开发环境**: PostgreSQL (skyeye_dev)
- **测试环境**: H2内存数据库
- **生产环境**: PostgreSQL (skyeye)

### Redis配置

- **开发环境**: database = 1
- **测试环境**: 不使用Redis
- **生产环境**: database = 0

### 应用配置

主要配置项：

```yaml
spring:
  application:
    name: skyeye-monitoring
  datasource:
    url: jdbc:postgresql://localhost:5432/skyeye
    username: skyeye_app
    password: skyeye_app_2024
  data:
    redis:
      host: localhost
      port: 6379
```

## 快速开始

### 环境要求

- JDK 17+
- Maven 3.6+
- PostgreSQL 12+
- Redis 6+

### 启动步骤

1. **克隆项目**
   ```bash
   git clone <repository-url>
   cd skyv-server
   ```

2. **配置数据库**
   ```sql
   CREATE DATABASE skyeye;
   CREATE USER skyeye_app WITH PASSWORD 'skyeye_app_2024';
   GRANT ALL PRIVILEGES ON DATABASE skyeye TO skyeye_app;
   ```

3. **启动Redis**
   ```bash
   redis-server
   ```

4. **编译项目**
   ```bash
   mvn clean compile
   ```

5. **运行应用**
   ```bash
   mvn spring-boot:run
   ```

### 验证启动

访问以下端点验证系统启动：

- 健康检查: http://localhost:8080/api/health
- 系统信息: http://localhost:8080/api/health/info
- Actuator健康检查: http://localhost:8080/actuator/health

## API文档

系统提供RESTful API接口，所有接口返回统一格式：

```json
{
  "code": 200,
  "message": "操作成功",
  "data": {},
  "meta": {},
  "timestamp": 1640995200000
}
```

### 响应码说明

- **2xx**: 成功响应
- **4xx**: 客户端错误
- **5xx**: 服务器错误
- **6xx**: 业务错误
- **7xx**: 设备相关错误
- **8xx**: 任务相关错误
- **9xx**: 文件相关错误

## 开发规范

### 代码规范

- 使用Java 17语法特性
- 遵循阿里巴巴Java开发手册
- 所有公共方法必须有Javadoc注释
- 异常处理必须记录日志
- 数据库操作必须使用事务

### 包命名规范

- `controller`: REST控制器
- `service`: 业务服务层
- `repository`: 数据访问层
- `entity`: 实体类
- `dto`: 数据传输对象
- `config`: 配置类
- `util`: 工具类

### 数据库规范

- 表名使用`tb_`前缀
- 字段名使用下划线分隔
- 主键统一使用`id`
- 必须包含`created_at`、`updated_at`字段
- 使用软删除（`deleted_at`字段）

## 部署说明

### Docker部署

```dockerfile
FROM openjdk:17-jdk-slim
COPY target/skyeye-monitoring-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### 环境变量

生产环境建议使用环境变量配置：

```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/skyeye
export SPRING_DATASOURCE_USERNAME=skyeye_app
export SPRING_DATASOURCE_PASSWORD=your_password
export SPRING_DATA_REDIS_HOST=redis
```

## 监控与运维

### 健康检查

- `/actuator/health`: 应用健康状态
- `/actuator/metrics`: 应用指标
- `/api/health`: 自定义健康检查

### 日志配置

- 开发环境: DEBUG级别，控制台输出
- 生产环境: INFO级别，文件输出
- 日志文件: `/var/log/skyeye/skyeye.log`

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 联系方式

- 项目维护者: SkyEye Team
- 邮箱: support@skyeye.com
- 文档: https://docs.skyeye.com 