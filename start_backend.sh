#!/bin/bash

echo "正在启动档案管理系统后端服务..."

# 设置Java环境变量
export JAVA_HOME=/Library/Java/JavaVirtualMachines/temurin-24.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 设置Spring Boot配置
export SPRING_PROFILES_ACTIVE=h2
export SERVER_PORT=8080

# 编译并启动应用
echo "正在编译项目..."
find src/main/java -name "*.java" > sources.txt

# 创建类路径
CLASSPATH=""
for jar in $(find ~/.m2/repository -name "*.jar" 2>/dev/null | head -50); do
    CLASSPATH="$CLASSPATH:$jar"
done

# 编译Java文件
mkdir -p target/classes
javac -cp "$CLASSPATH" -d target/classes @sources.txt

if [ $? -eq 0 ]; then
    echo "编译成功，正在启动应用..."
    # 复制资源文件
    cp -r src/main/resources/* target/classes/
    
    # 启动应用
    java -cp "target/classes:$CLASSPATH" \
         -Dspring.profiles.active=h2 \
         -Dserver.port=8080 \
         com.archive.management.ArchiveManagementApplication
else
    echo "编译失败，请检查代码错误"
    exit 1
fi