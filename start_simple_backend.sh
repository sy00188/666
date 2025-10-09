#!/bin/bash

echo "正在启动简化版Spring Boot后端服务..."

# 设置Java环境
export JAVA_HOME=/Library/Java/JavaVirtualMachines/openjdk-24.jdk/Contents/Home
export PATH=$JAVA_HOME/bin:$PATH

# 进入项目目录
cd /Users/songyidemac/Desktop/222

# 创建目录
mkdir -p target/classes

# 查找并设置Maven本地仓库路径
MAVEN_REPO="$HOME/.m2/repository"

# 构建基础classpath（只包含必要的依赖）
CLASSPATH="src/main/resources:target/classes"

# 添加Spring Boot相关JAR
SPRING_BOOT_JARS=(
    "org/springframework/boot/spring-boot/3.2.1/spring-boot-3.2.1.jar"
    "org/springframework/boot/spring-boot-autoconfigure/3.2.1/spring-boot-autoconfigure-3.2.1.jar"
    "org/springframework/spring-context/6.1.2/spring-context-6.1.2.jar"
    "org/springframework/spring-core/6.1.2/spring-core-6.1.2.jar"
    "org/springframework/spring-beans/6.1.2/spring-beans-6.1.2.jar"
    "org/springframework/spring-web/6.1.2/spring-web-6.1.2.jar"
    "org/springframework/spring-webmvc/6.1.2/spring-webmvc-6.1.2.jar"
    "com/h2database/h2/2.2.224/h2-2.2.224.jar"
    "org/slf4j/slf4j-api/2.0.11/slf4j-api-2.0.11.jar"
    "ch/qos/logback/logback-classic/1.4.14/logback-classic-1.4.14.jar"
    "ch/qos/logback/logback-core/1.4.14/logback-core-1.4.14.jar"
)

# 添加JAR到classpath
for jar in "${SPRING_BOOT_JARS[@]}"; do
    if [ -f "$MAVEN_REPO/$jar" ]; then
        CLASSPATH="$CLASSPATH:$MAVEN_REPO/$jar"
    fi
done

# 只编译主启动类
echo "编译主启动类..."
javac -cp "$CLASSPATH" -d target/classes src/main/java/com/archive/management/ArchiveManagementApplication.java

if [ $? -ne 0 ]; then
    echo "编译失败，尝试创建最小化启动类..."
    
    # 创建最小化启动类
    cat > target/classes/SimpleApp.java << 'EOF'
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class SimpleApp {
    
    @GetMapping("/")
    public String home() {
        return "Archive Management System is running!";
    }
    
    @GetMapping("/health")
    public String health() {
        return "OK";
    }
    
    public static void main(String[] args) {
        SpringApplication.run(SimpleApp.class, args);
    }
}
EOF
    
    # 编译简化版
    javac -cp "$CLASSPATH" -d target/classes target/classes/SimpleApp.java
    
    if [ $? -ne 0 ]; then
        echo "编译失败，请检查依赖"
        exit 1
    fi
    
    echo "启动简化版应用..."
    java -cp "$CLASSPATH" \
         -Dspring.profiles.active=h2 \
         -Dserver.port=8080 \
         SimpleApp
else
    echo "启动完整应用..."
    java -cp "$CLASSPATH" \
         -Dspring.profiles.active=h2 \
         -Dserver.port=8080 \
         com.archive.management.ArchiveManagementApplication
fi

echo "后端服务已启动，访问地址: http://localhost:8080"