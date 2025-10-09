#!/bin/bash

echo "正在启动Spring Boot后端服务（最终版）..."

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

# 查找并添加所有必要的JAR文件
REQUIRED_JARS=(
    # Spring Boot核心
    "$MAVEN_REPO/org/springframework/boot/spring-boot/3.2.2/spring-boot-3.2.2.jar"
    "$MAVEN_REPO/org/springframework/boot/spring-boot-autoconfigure/3.2.2/spring-boot-autoconfigure-3.2.2.jar"
    
    # Spring Framework核心
    "$MAVEN_REPO/org/springframework/spring-context/6.1.3/spring-context-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-core/6.1.3/spring-core-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-beans/6.1.3/spring-beans-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-web/6.1.3/spring-web-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-webmvc/6.1.3/spring-webmvc-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-aop/6.1.3/spring-aop-6.1.3.jar"
    "$MAVEN_REPO/org/springframework/spring-expression/6.1.3/spring-expression-6.1.3.jar"
    
    # 数据库
    "$MAVEN_REPO/com/h2database/h2/2.2.224/h2-2.2.224.jar"
    
    # 日志
    "$MAVEN_REPO/org/slf4j/slf4j-api/2.0.11/slf4j-api-2.0.11.jar"
    "$MAVEN_REPO/ch/qos/logback/logback-classic/1.4.14/logback-classic-1.4.14.jar"
    "$MAVEN_REPO/ch/qos/logback/logback-core/1.4.14/logback-core-1.4.14.jar"
    "$MAVEN_REPO/commons-logging/commons-logging/1.2/commons-logging-1.2.jar"
    
    # Tomcat Embedded（使用实际存在的版本）
    "$MAVEN_REPO/org/apache/tomcat/embed/tomcat-embed-core/10.1.18/tomcat-embed-core-10.1.18.jar"
    "$MAVEN_REPO/org/apache/tomcat/embed/tomcat-embed-websocket/10.1.18/tomcat-embed-websocket-10.1.18.jar"
    "$MAVEN_REPO/org/apache/tomcat/embed/tomcat-embed-el/10.1.18/tomcat-embed-el-10.1.18.jar"
    
    # Jakarta API
    "$MAVEN_REPO/jakarta/annotation/jakarta.annotation-api/2.1.1/jakarta.annotation-api-2.1.1.jar"
)

# 添加存在的JAR到classpath
echo "构建classpath..."
for jar in "${REQUIRED_JARS[@]}"; do
    if [ -f "$jar" ]; then
        CLASSPATH="$CLASSPATH:$jar"
        echo "✓ 添加: $(basename $jar)"
    else
        echo "✗ 缺失: $(basename $jar)"
        # 尝试查找相似版本
        jar_dir=$(dirname "$jar")
        jar_name=$(basename "$jar" .jar)
        jar_base=$(echo "$jar_name" | sed 's/-[0-9].*//')
        
        if [ -d "$(dirname $jar_dir)" ]; then
            similar_jar=$(find "$(dirname $jar_dir)" -name "${jar_base}*.jar" | head -1)
            if [ -n "$similar_jar" ] && [ -f "$similar_jar" ]; then
                CLASSPATH="$CLASSPATH:$similar_jar"
                echo "  → 使用替代版本: $(basename $similar_jar)"
            fi
        fi
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
import org.springframework.web.bind.annotation.CrossOrigin;

@SpringBootApplication
@RestController
@CrossOrigin(origins = "*")
public class SimpleBootApp {
    
    @GetMapping("/")
    public String home() {
        return "Archive Management System Backend is running successfully!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "{\"status\":\"UP\",\"service\":\"archive-management\",\"timestamp\":\"" + 
               java.time.LocalDateTime.now() + "\"}";
    }
    
    @GetMapping("/api/test")
    public String test() {
        return "{\"message\":\"Backend API is working\",\"timestamp\":\"" + 
               java.time.LocalDateTime.now() + "\",\"version\":\"1.0.0\"}";
    }
    
    @GetMapping("/api/status")
    public String status() {
        return "{\"backend\":\"running\",\"database\":\"h2-memory\",\"port\":8080}";
    }
    
    public static void main(String[] args) {
        System.setProperty("spring.profiles.active", "h2");
        System.setProperty("server.port", "8080");
        System.setProperty("spring.datasource.url", "jdbc:h2:mem:testdb");
        System.setProperty("spring.datasource.driver-class-name", "org.h2.Driver");
        System.setProperty("spring.h2.console.enabled", "true");
        
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
     -Dspring.h2.console.enabled=true \
     -Dspring.h2.console.path=/h2-console \
     -Dlogging.level.org.springframework=INFO \
     -Dlogging.level.com.simple=DEBUG \
     com.simple.SimpleBootApp

echo "后端服务已启动，访问地址: http://localhost:8080"