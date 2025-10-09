#!/bin/bash

echo "正在启动Spring Boot后端服务（使用H2内存数据库）..."

# 设置Java环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-24.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 设置Spring Boot配置
export SPRING_PROFILES_ACTIVE=h2
export SERVER_PORT=8080

# 进入项目目录
cd /Users/songyidemac/Desktop/222

# 查找并设置Maven本地仓库路径
MAVEN_REPO="$HOME/.m2/repository"
if [ ! -d "$MAVEN_REPO" ]; then
    echo "Maven本地仓库不存在，创建默认仓库目录..."
    mkdir -p "$MAVEN_REPO"
fi

# 构建classpath
CLASSPATH="src/main/resources"

# 添加所有依赖JAR文件到classpath
for jar in $(find "$MAVEN_REPO" -name "*.jar" 2>/dev/null | head -100); do
    CLASSPATH="$CLASSPATH:$jar"
done

# 编译Java源文件
echo "编译Java源文件..."
find src/main/java -name "*.java" > sources.txt

if [ -s sources.txt ]; then
    javac -cp "$CLASSPATH" -d target/classes @sources.txt
    if [ $? -ne 0 ]; then
        echo "编译失败，请检查代码错误"
        exit 1
    fi
    echo "编译成功"
else
    echo "未找到Java源文件"
    exit 1
fi

# 添加编译后的类文件到classpath
CLASSPATH="target/classes:$CLASSPATH"

# 启动应用
echo "启动Spring Boot应用..."
java -cp "$CLASSPATH" \
     -Dspring.profiles.active=h2 \
     -Dserver.port=8080 \
     -Dspring.datasource.url=jdbc:h2:mem:testdb \
     -Dspring.datasource.driver-class-name=org.h2.Driver \
     -Dspring.jpa.hibernate.ddl-auto=create-drop \
     com.archive.management.ArchiveManagementApplication

echo "后端服务已启动，访问地址: http://localhost:8080"