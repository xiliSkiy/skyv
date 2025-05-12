#!/bin/bash

# SkyEye智能监控系统 - Docker环境停止脚本

echo "=== SkyEye智能监控系统 - Docker环境停止脚本 ==="
echo "停止Docker容器..."

# 停止Docker容器
docker-compose down

echo "=== 环境已停止 ===" 