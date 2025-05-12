# SkyEye智能监控系统

SkyEye智能监控系统是一套综合性的智能监控解决方案，集成了设备管理、实时监控、智能分析、报警管理等功能，通过AI技术提升监控效率和安全性。系统采用模块化设计，支持多种设备接入和灵活配置，适用于各类安防监控场景。

## 技术栈

- **后端**：Spring Boot 2.7.5、Spring Data JPA、Spring Security
- **数据库**：MySQL 8.0
- **认证**：JWT（JSON Web Token）
- **文档**：Swagger 3.0
- **构建工具**：Maven

## 项目结构

```
skyii-server/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/
│   │   │       └── skyeye/
│   │   │           ├── App.java                    # 应用程序入口
│   │   │           ├── auth/                       # 认证模块
│   │   │           │   ├── config/                 # 安全配置
│   │   │           │   ├── controller/             # 认证控制器
│   │   │           │   ├── dto/                    # 数据传输对象
│   │   │           │   ├── entity/                 # 实体类
│   │   │           │   ├── filter/                 # 过滤器
│   │   │           │   ├── repository/             # 数据访问层
│   │   │           │   ├── service/                # 服务层
│   │   │           │   └── util/                   # 工具类
│   │   │           ├── common/                     # 公共模块
│   │   │           │   ├── entity/                 # 基础实体
│   │   │           │   ├── exception/              # 异常处理
│   │   │           │   └── response/               # 响应封装
│   │   │           ├── config/                     # 配置模块
│   │   │           │   └── init/                   # 数据初始化
│   │   │           ├── device/                     # 设备管理模块
│   │   │           │   ├── controller/             # 控制器
│   │   │           │   ├── dto/                    # 数据传输对象
│   │   │           │   ├── entity/                 # 实体类
│   │   │           │   ├── repository/             # 数据访问层
│   │   │           │   └── service/                # 服务层
│   │   │           ├── stream/                     # 视频流处理模块
│   │   │           ├── alert/                      # 报警服务模块
│   │   │           ├── analytics/                  # 数据分析模块
│   │   │           ├── scheduler/                  # 任务调度模块
│   │   │           ├── config/                     # 系统配置模块
│   │   │           ├── storage/                    # 存储服务模块
│   │   │           ├── logging/                    # 日志服务模块
│   │   │           └── ai/                         # AI分析模块
│   │   │
│   │   └── resources/
│   │       └── application.yml                     # 应用配置文件
│   └── test/                                       # 测试代码
└── pom.xml                                         # Maven配置文件
```

## 功能模块

- **用户认证与权限管理**：用户登录、注册、权限控制
- **设备管理**：设备添加、编辑、删除、查询、分组管理
- **监控管理**：实时监控、云台控制
- **报警中心**：报警管理、报警配置
- **数据分析**：数据统计、分析报告
- **任务调度**：任务管理、计划设置
- **系统设置**：基本设置、网络设置、存储设置等

## 快速开始

### 环境要求

- JDK 11+
- Maven 3.6+
- MySQL 8.0+

### 运行步骤

1. 克隆项目
   ```bash
   git clone https://github.com/yourusername/skyii.git
   cd skyii
   ```

2. 配置数据库
   - 创建MySQL数据库：`skyeye`
   - 修改`application.yml`中的数据库连接信息

3. 构建项目
   ```bash
   mvn clean package
   ```

4. 运行项目
   ```bash
   java -jar target/skyii-server-1.0-SNAPSHOT.jar
   ```

5. 访问API文档
   ```
   http://localhost:8080/api/swagger-ui/
   ```

## API文档

系统集成了Swagger文档，启动后可通过以下地址访问：
```
http://localhost:8080/api/swagger-ui/
```

## 默认账号

系统初始化后会创建以下默认账号：
- 管理员账号：admin / admin123 

export JAVA_HOME=$(/usr/libexec/java_home -v 11) && mvn clean package -Dmaven.test.skip=true