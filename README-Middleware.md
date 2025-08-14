# SkyEye 中间件初始化脚本使用说明

## 🚀 快速开始

### 一键启动
```bash
# 最简单的启动方式
./scripts/quick-start.sh
```

### 手动启动
```bash
# 仅启动核心服务 (PostgreSQL + Redis)
./scripts/init-middleware.sh start core

# 启动完整服务
./scripts/init-middleware.sh start all

# 使用管理工具
./scripts/middleware-manager.sh
```

## 📁 文件说明

| 文件 | 功能 |
|------|------|
| `docker-compose.middleware.yml` | 中间件服务配置 |
| `scripts/init-middleware.sh` | 中间件初始化脚本 |
| `scripts/quick-start.sh` | 快速启动脚本 |
| `scripts/middleware-manager.sh` | 交互式管理工具 |
| `scripts/verify-database.sh` | 数据库验证脚本 |

## 🔧 服务配置

### 核心服务 (core)
- **PostgreSQL**: 数据库服务
- **Redis**: 缓存服务

### 扩展服务
- **logging**: Elasticsearch 日志搜索
- **storage**: MinIO 对象存储
- **messaging**: RabbitMQ 消息队列
- **timeseries**: InfluxDB 时序数据库
- **tools**: pgAdmin + Redis Commander 管理工具

## 📋 常用命令

### 启动服务
```bash
# 启动核心服务
./scripts/init-middleware.sh start core

# 启动核心服务 + 管理工具
./scripts/init-middleware.sh start core
./scripts/init-middleware.sh start tools

# 启动所有服务
./scripts/init-middleware.sh start all
```

### 管理服务
```bash
# 查看状态
./scripts/init-middleware.sh status

# 查看日志
./scripts/init-middleware.sh logs postgres

# 停止服务
./scripts/init-middleware.sh stop

# 重启服务
./scripts/init-middleware.sh restart
```

### 数据库操作
```bash
# 验证数据库
./scripts/verify-database.sh

# 连接数据库
docker-compose -f docker-compose.middleware.yml exec postgres psql -U skyeye_app -d skyeye
```

## 🌐 服务访问地址

### 数据库
- **PostgreSQL**: `localhost:5432`
  - 数据库: `skyeye`
  - 用户: `skyeye_app`
  - 密码: `skyeye_app_2024`

- **Redis**: `localhost:6379`
  - 密码: `skyeye_redis_2024`

### 管理工具
- **pgAdmin**: http://localhost:5050
  - 邮箱: `admin@skyeye.com`
  - 密码: `skyeye_pgadmin_2024`

- **Redis Commander**: http://localhost:8081
  - 用户: `admin`
  - 密码: `skyeye_redis_commander_2024`

### 扩展服务
- **Elasticsearch**: http://localhost:9200
- **MinIO**: http://localhost:9001
  - 用户: `skyeye_minio`
  - 密码: `skyeye_minio_2024`
- **RabbitMQ**: http://localhost:15672
  - 用户: `skyeye_rabbitmq`
  - 密码: `skyeye_rabbitmq_2024`
- **InfluxDB**: http://localhost:8086
  - 用户: `skyeye_admin`
  - 密码: `skyeye_influxdb_2024`

## 💾 数据管理

### 备份数据
```bash
# 自动备份
./scripts/middleware-manager.sh
# 选择 "7) 备份数据"

# 手动备份 PostgreSQL
docker-compose -f docker-compose.middleware.yml exec postgres pg_dump -U skyeye_app skyeye > backup.sql

# 手动备份 Redis
docker-compose -f docker-compose.middleware.yml exec redis redis-cli --rdb backup.rdb
```

### 恢复数据
```bash
# 恢复 PostgreSQL
docker-compose -f docker-compose.middleware.yml exec -T postgres psql -U skyeye_app skyeye < backup.sql

# 恢复 Redis
docker-compose -f docker-compose.middleware.yml stop redis
docker cp backup.rdb container_name:/data/dump.rdb
docker-compose -f docker-compose.middleware.yml start redis
```

## 🔍 故障排查

### 检查服务状态
```bash
# 查看所有容器状态
docker-compose -f docker-compose.middleware.yml ps

# 查看资源使用
docker stats

# 查看服务日志
./scripts/init-middleware.sh logs [service_name]
```

### 常见问题

**1. 数据库连接失败**
```bash
# 检查容器状态
docker-compose -f docker-compose.middleware.yml ps postgres

# 查看日志
docker-compose -f docker-compose.middleware.yml logs postgres

# 测试连接
./scripts/verify-database.sh
```

**2. 内存不足**
```bash
# 检查内存使用
free -h
docker stats

# 减少服务或调整内存限制
# 编辑 docker-compose.middleware.yml 中的 deploy.resources.limits
```

**3. 端口冲突**
```bash
# 检查端口占用
netstat -tulpn | grep :5432

# 修改端口映射
# 编辑 docker-compose.middleware.yml 中的 ports 配置
```

## ⚡ 性能优化

### PostgreSQL 优化
- 调整 `shared_buffers` 和 `effective_cache_size`
- 根据数据量调整 `work_mem`
- 定期执行 `VACUUM` 和 `ANALYZE`

### Redis 优化
- 设置合适的 `maxmemory` 和 `maxmemory-policy`
- 启用 AOF 持久化
- 监控内存使用情况

### 系统资源
- 确保至少 4GB 可用内存
- 监控磁盘空间使用
- 定期清理旧的日志文件

## 🛡️ 安全建议

1. **修改默认密码**: 生产环境必须修改所有默认密码
2. **网络隔离**: 使用防火墙限制访问
3. **SSL/TLS**: 启用数据传输加密
4. **定期备份**: 制定自动备份策略
5. **访问控制**: 限制管理工具的访问权限

## 📞 获取帮助

```bash
# 查看脚本帮助
./scripts/init-middleware.sh help

# 使用交互式管理工具
./scripts/middleware-manager.sh

# 验证数据库状态
./scripts/verify-database.sh --help
```

---

**注意**: 首次启动可能需要几分钟时间来下载镜像和初始化数据库，请耐心等待。 