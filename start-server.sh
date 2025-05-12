#!/bin/bash

# 设置Java 11环境
export JAVA_HOME=$(/usr/libexec/java_home -v 11)
echo "已设置Java环境为: $(java -version 2>&1 | head -1)"

# 启动后端服务
echo "正在启动SkyEye后端服务..."
java -jar skyii-server/target/skyii-server-1.0-SNAPSHOT.jar 