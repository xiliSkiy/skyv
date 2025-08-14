# SkyEye 数据库脚本使用说明

## 📁 文件结构

```
skyv/
├── docker/                                    # Docker配置文件
│   ├── Dockerfile.postgres                    # PostgreSQL镜像构建文件
│   ├── postgresql.conf                        # PostgreSQL配置
│   ├── pg_hba.conf                           # 访问控制配置
│   ├── redis.conf                            # Redis配置
│   ├── nginx/default.conf                    # Nginx反向代理配置
│   └── init-scripts/01-init-database.sh      # 数据库初始化脚本
├── skyv-server/src/main/resources/db/migration/  # 数据库迁移脚本
│   ├── V1.0.0__init_database.sql             # 基础配置和函数
│   ├── V1.0.1__create_user_tables.sql        # 用户权限管理表
│   ├── V1.0.2__create_device_tables.sql      # 设备管理表
│   ├── V1.0.3__create_collector_tables.sql   # 数据采集表
│   ├── V1.0.4__create_alert_tables.sql       # 报警管理表
│   ├── V1.0.5__create_system_tables.sql      # 系统配置表
│   └── V1.0.6__init_basic_data.sql           # 基础数据初始化
├── docker-compose.yml                        # Docker Compose配置
├── scripts/
│   ├── deploy.sh                             # 自动部署脚本
│   └── verify-database.sh                    # 数据库验证脚本
├── 数据库部署说明.md                           # 详细部署文档
└── README-Database.md                        # 本文档
```

## 🚀 快速开始

### 1. 一键部署（推荐）

```bash
# 执行部署脚本
chmod +x scripts/deploy.sh
./scripts/deploy.sh

# 或者使用Docker Compose
docker-compose up -d
```

### 2. 验证部署结果

```bash
# 验证数据库
chmod +x scripts/verify-database.sh
./scripts/verify-database.sh
```

### 3. 访问系统

- **前端**: http://localhost
- **后端API**: http://localhost:8080  
- **数据库**: localhost:5432
- **Redis**: localhost:6379

## 📊 数据库设计概览

### 核心表结构

| 模块 | 主要表 | 功能 |
|------|--------|------|
| 用户管理 | tb_users, tb_roles, tb_permissions | 用户认证和权限控制 |
| 设备管理 | tb_devices, tb_device_types, tb_device_areas | 设备信息和分类管理 |
| 数据采集 | tb_collectors, tb_collection_tasks, tb_collection_data | 数据采集和存储 |
| 报警系统 | tb_alert_rules, tb_alerts | 报警规则和事件管理 |
| 系统配置 | tb_system_settings, tb_operation_logs | 系统配置和日志 |

### 分区表设计

为了处理大数据量，以下表采用按月分区：
- `tb_collection_data` - 采集数据（主要数据表）
- `tb_alerts` - 报警记录
- `tb_operation_logs` - 操作日志
- `tb_collection_logs` - 采集日志

## 🛠 手动执行脚本

### 1. 按顺序执行SQL脚本

```bash
# 进入数据库目录
cd skyv-server/src/main/resources/db/migration

# 依次执行脚本
psql -h localhost -U skyeye_app -d skyeye -f V1.0.0__init_database.sql
psql -h localhost -U skyeye_app -d skyeye -f V1.0.1__create_user_tables.sql
psql -h localhost -U skyeye_app -d skyeye -f V1.0.2__create_device_tables.sql
psql -h localhost -U skyeye_app -d skyeye -f V1.0.3__create_collector_tables.sql
psql -h localhost -U skyeye_app -d skyeye -f V1.0.4__create_alert_tables.sql
psql -h localhost -U skyeye_app -d skyeye -f V1.0.5__create_system_tables.sql
psql -h localhost -U skyeye_app -d skyeye -f V1.0.6__init_basic_data.sql
```

### 2. 验证执行结果

```bash
# 检查表是否创建成功
psql -h localhost -U skyeye_app -d skyeye -c "\dt"

# 检查基础数据
psql -h localhost -U skyeye_app -d skyeye -c "SELECT COUNT(*) FROM tb_users;"
psql -h localhost -U skyeye_app -d skyeye -c "SELECT COUNT(*) FROM tb_permissions;"
```

## 🔧 脚本功能说明

### deploy.sh 部署脚本

**功能**：
- 检查系统依赖（Docker, Docker Compose）
- 创建必要目录和设置权限
- 构建Docker镜像
- 启动所有服务
- 检查服务状态

**用法**：
```bash
./scripts/deploy.sh [选项]

选项:
  --monitoring    启动监控服务（Prometheus + Grafana）
  --no-build      跳过镜像构建
  --help          显示帮助信息
```

### verify-database.sh 验证脚本

**功能**：
- 检查数据库连接
- 验证扩展和函数
- 检查表结构和索引
- 验证分区表和基础数据
- 生成验证报告

**用法**：
```bash
./scripts/verify-database.sh [选项]

选项:
  -h, --host HOST       数据库主机
  -p, --port PORT       数据库端口  
  -d, --database DB     数据库名称
  -u, --user USER       数据库用户
  -w, --password PASS   数据库密码
  --help                显示帮助信息
```

## 📋 默认配置

### 数据库连接信息
- **主机**: localhost
- **端口**: 5432
- **数据库**: skyeye
- **用户**: skyeye_app
- **密码**: skyeye_app_2024

### Redis连接信息
- **主机**: localhost
- **端口**: 6379
- **密码**: skyeye_redis_2024

### 默认用户账号
- **管理员**: admin / admin123456
- **普通用户**: user / user123456

## 🔍 故障排查

### 1. 数据库连接失败

```bash
# 检查容器状态
docker-compose ps

# 查看数据库日志
docker-compose logs postgres

# 测试连接
docker exec skyeye-postgres pg_isready -U skyeye_app
```

### 2. 脚本执行失败

```bash
# 检查脚本权限
ls -la scripts/

# 手动设置权限
chmod +x scripts/*.sh
chmod +x docker/init-scripts/*.sh
```

### 3. 内存不足

```bash
# 检查系统资源
docker stats

# 调整PostgreSQL配置
# 编辑 docker/postgresql.conf
shared_buffers = 128MB  # 减少内存使用
```

## 📈 性能优化

### 1. 数据库配置优化

```ini
# postgresql.conf 关键参数
shared_buffers = 256MB          # 共享缓冲区
effective_cache_size = 1GB      # 有效缓存大小
work_mem = 4MB                  # 工作内存
maintenance_work_mem = 64MB     # 维护工作内存
```

### 2. 分区维护

```sql
-- 创建新分区
SELECT create_monthly_partition('tb_collection_data', '2024-01-01'::date);

-- 删除旧分区
SELECT drop_old_partitions('tb_collection_data', 3);
```

## 🔒 安全建议

1. **修改默认密码**：生产环境必须修改所有默认密码
2. **网络隔离**：使用防火墙限制数据库访问
3. **SSL连接**：启用SSL加密数据传输
4. **定期备份**：制定数据备份计划
5. **权限控制**：遵循最小权限原则

## 📞 技术支持

如需帮助，请：
1. 查看详细部署文档：`数据库部署说明.md`
2. 运行验证脚本检查问题
3. 查看相关日志文件
4. 检查系统资源使用情况

---

**注意**：生产环境部署前请务必进行充分测试！ 