#!/bin/bash

# SkyEye智能监控系统 - Docker环境启动脚本

echo "=== SkyEye智能监控系统 - Docker环境启动脚本 ==="
echo "准备启动环境..."

# 创建必要的目录结构
echo "创建目录结构..."
mkdir -p docker/mysql/data
mkdir -p docker/mysql/conf
mkdir -p docker/mysql/init
mkdir -p docker/redis/data
mkdir -p docker/redis/conf

# 复制配置文件
echo "复制配置文件..."
cp docker/mysql/conf/my.cnf docker/mysql/conf/my.cnf 2>/dev/null || echo "MySQL配置文件已存在"
cp docker/redis/conf/redis.conf docker/redis/conf/redis.conf 2>/dev/null || echo "Redis配置文件已存在"

# 复制初始化SQL脚本
echo "复制初始化SQL脚本..."
cp docker/mysql/init/init.sql docker/mysql/init/init.sql 2>/dev/null || echo "SQL初始化脚本已存在"

# 设置文件权限
echo "设置文件权限..."
chmod +x start-docker-env.sh
chmod 644 docker/mysql/conf/my.cnf
chmod 644 docker/redis/conf/redis.conf
chmod 644 docker/mysql/init/init.sql

# 启动Docker容器
echo "启动Docker容器..."
docker-compose up -d

# 检查容器状态
echo "检查容器状态..."
sleep 5
docker-compose ps

echo "=== 环境启动完成 ==="
echo "MySQL: localhost:3306 (用户名: skyeye, 密码: skyeye123)"
echo "Redis: localhost:6379"
echo "数据库名: skyeye"
echo ""
echo "提示: 首次启动时，MySQL可能需要一些时间来初始化，请耐心等待。"
echo "      可以使用 'docker-compose logs mysql' 查看MySQL的启动日志。" 