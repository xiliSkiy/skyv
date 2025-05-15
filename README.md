# SkyEye 智能监控系统

SkyEye是一套综合性的智能监控解决方案，集成了设备管理、实时监控、智能分析、报警管理等功能，通过AI技术提升监控效率和安全性。

## 项目结构

- `skyii-server`: 后端服务（Spring Boot）
- `skyii-web`: 前端应用（Vue 3）
- `docker`: Docker相关配置文件

## 技术栈

### 后端
- Java 11
- Spring Boot 2.7.x
- Spring Security + JWT
- Spring Data JPA
- MySQL 8.0
- Redis

### 前端
- Vue 3
- Element Plus
- Vite
- Pinia
- Vue Router

## 快速开始

### 环境要求
- JDK 11+
- Node.js 14+
- Docker & Docker Compose
- Maven 3.6+

### 启动步骤

1. 启动Docker环境（MySQL和Redis）
```bash
./start-docker-env.sh
```

2. 启动后端服务
```bash
./start-server.sh
```

3. 启动前端服务
```bash
cd skyii-web
npm install
npm run dev
```

4. 访问系统
- 前端: http://localhost:3000
- API文档: http://localhost:8080/api/swagger-ui/index.html

## 默认账号

- 用户名: admin
- 密码: admin123

## 功能模块

- 用户认证与权限管理
- 设备管理
- 实时监控
- 报警中心
- 数据分析
- 任务调度
- 系统设置

## 开发指南

详细开发文档请参考 [开发指南](docs/development-guide.md)。 

## 项目编译
mvn clean package -Dmaven.test.skip=true