#!/bin/bash

echo "正在启动Spring Boot后端服务（修复版）..."

# 设置Java环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-24.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 进入项目目录
cd /Users/songyidemac/Desktop/222

# 创建目录
mkdir -p target/classes

# Maven本地仓库路径
MAVEN_REPO="$HOME/.m2/repository"

# 构建classpath，使用实际存在的JAR文件
CLASSPATH="src/main/resources:target/classes"

# 查找并添加Spring Boot相关JAR（使用实际存在的版本）
SPRING_BOOT_JARS=(
    "$MAVEN_REPO/org/springframework/boot/spring-boot/3.2.2/spring-boot-3.2.2.jar"
    "$MAVEN_REPO/org/springframework/boot/spring-boot-autoconfigure/3.2.2/spring-boot-autoconfigure-3.2.2.jar"
    "$MAVEN_REPO/org/springframework/spring-context/6.1.3/spring-context-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-core/6.1.3/spring-core-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-beans/6.1.3/spring-beans-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-web/6.1.3/spring-web-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-webmvc/6.1.3/spring-webmvc-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-aop/6.1.3/spring-aop-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-expression/6.1.3/spring-expression-6.1.3.jar"
    "$MAVEN_REPO/com/h2database/h2/2.2.224/h2-2.2.224.jar"
    "$MAVEN_REPO/org/slf4j/slf4j-api/2.0.11/slf4j-api-2.0.11.jar"
    "$MAVEN_REPO/ch/qos/logback/logback-classic/1.4.14/logback-classic-1.4.14.jar"
    "$MAVEN_REPO/ch/qos/logback/logback-core/1.4.14/logback-core-1.4.14.jar"
    "$MAVEN_REPO/org/apache/tomcat/embed/tomcat-embed-core/10.1.17/tomcat-embed-core-10.1.17.jar"
    "$MAVEN_REPO/org/apache/tomcat/embed/tomcat-embed-websocket/10.1.17/tomcat-embed-websocket-10.1.17.jar"
    "$MAVEN_REPO/org/apache/tomcat/embed/tomcat-embed-el/10.1.17/tomcat-embed-el-10.1.17.jar"
    "$MAVEN_REPO/jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar"
)

# 添加存在的JAR到classpath
for jar in "${SPRING_BOOT_JARS[@]}"; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
        echo "添加JAR: $(basename $jar)"
    else
        echo "警告: JAR文件不存在: $jar"
    fi
done

echo "Classpath构建完成"

# 创建最小化的Spring Boot应用
echo "创建最小化Spring Boot应用..."
mkdir -p target/classes/com/simple

cat > target/classes/com/simple/SimpleBootApp.java << 'EOF'
package com.simple;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SimpleBootApp {
    
    @GetMapping("/")
    public String home() {
        return "Archive Management System Backend is running!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"archive-management\"}";
    }
    
    @GetMapping("/api/test")
    public String test() {
        return "{\"message\":\"Backend API is working\",\"timestamp\":\"" + 
               java.time.LocalDateTime.now() + "\"}";
    }
    
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "h2");
        System.setProperty("server.port", "8080");
        SpringApplication.run(SimpleBootApp.class, args);
    }
}
EOF

# 编译简化版应用
echo "编译简化版应用..."
javac -cp "$CLASSPATH" -d target/classes target/classes/com/simple/SimpleBootApp.java

if [ $? -ne 0 ]; then
    echo "编译失败，请检查依赖"
    exit 1
fi

echo "启动Spring Boot应用..."
java -cp "$CLASSPATH" \
     -Dspring.profiles.active=h2 \
     -Dserver.port=8080 \
     -Dspring.datasource.url=jdbc:h2:mem:testdb \
     -Dspring.datasource.driver-class-name=org.h2.Driver \
     -Dspring.jpa.hibernate.ddl-auto=create-drop \
     -Dlogging.level.org.springframework=INFO \
     com.simple.SimpleBootApp

echo "后端服务已启动，访问地址: http://localhost:8080"