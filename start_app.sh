#!/bin/bash

# 设置项目根目录
PROJECT_ROOT="/Users/songyidemac/Desktop/222"
cd "$PROJECT_ROOT"

# 创建target/classes目录
mkdir -p target/classes

# 构建类路径
CLASSPATH="target/classes:src/main/resources"

# 添加Spring Boot相关依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/boot/spring-boot-starter-web/3.2.2/spring-boot-starter-web-3.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/boot/spring-boot-starter/3.2.2/spring-boot-starter-3.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/boot/spring-boot/3.2.2/spring-boot-3.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/boot/spring-boot-autoconfigure/3.2.2/spring-boot-autoconfigure-3.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-core/6.1.3/spring-core-6.1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-context/6.1.3/spring-context-6.1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-beans/6.1.3/spring-beans-6.1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-web/6.1.3/spring-web-6.1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-webmvc/6.1.3/spring-webmvc-6.1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-aop/6.1.3/spring-aop-6.1.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-expression/6.1.3/spring-expression-6.1.3.jar"

# 添加其他必要依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/tomcat/embed/tomcat-embed-core/10.1.18/tomcat-embed-core-10.1.18.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/tomcat/embed/tomcat-embed-websocket/10.1.18/tomcat-embed-websocket-10.1.18.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/tomcat/embed/tomcat-embed-el/10.1.18/tomcat-embed-el-10.1.18.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/ch/qos/logback/logback-classic/1.4.14/logback-classic-1.4.14.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/ch/qos/logback/logback-core/1.4.14/logback-core-1.4.14.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/slf4j/slf4j-api/2.0.9/slf4j-api-2.0.9.jar"

# 添加Lombok
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/projectlombok/lombok/1.18.30/lombok-1.18.30.jar"

# 添加Jakarta Validation
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar"

# 添加Swagger Annotations
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/io/swagger/core/v3/swagger-annotations-jakarta/2.2.15/swagger-annotations-jakarta-2.2.15.jar"

# 添加Spring Boot Starter Validation
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/boot/spring-boot-starter-validation/3.2.2/spring-boot-starter-validation-3.2.2.jar"

# 添加Spring Boot Starter Data JPA
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/boot/spring-boot-starter-data-jpa/3.2.2/spring-boot-starter-data-jpa-3.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/data/spring-data-jpa/3.2.2/spring-data-jpa-3.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/data/spring-data-commons/3.2.2/spring-data-commons-3.2.2.jar"

# 添加Hibernate
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/hibernate/orm/hibernate-core/6.4.1.Final/hibernate-core-6.4.1.Final.jar"

# 添加Jackson
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-core/2.15.3/jackson-core-2.15.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-databind/2.15.3/jackson-databind-2.15.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/fasterxml/jackson/core/jackson-annotations/2.15.3/jackson-annotations-2.15.3.jar"

# 添加Spring Security相关依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/boot/spring-boot-starter-security/3.2.2/spring-boot-starter-security-3.2.2.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/security/spring-security-config/6.2.1/spring-security-config-6.2.1.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/security/spring-security-core/6.2.1/spring-security-core-6.2.1.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/security/spring-security-web/6.2.1/spring-security-web-6.2.1.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/security/spring-security-crypto/6.2.1/spring-security-crypto-6.2.1.jar"

# 添加MyBatis相关依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/mybatis/mybatis/3.5.14/mybatis-3.5.14.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/mybatis/mybatis-spring/3.0.3/mybatis-spring-3.0.3.jar"

# 添加MyBatis Plus相关依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/baomidou/mybatis-plus-boot-starter/3.5.5/mybatis-plus-boot-starter-3.5.5.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/baomidou/mybatis-plus/3.5.5/mybatis-plus-3.5.5.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/baomidou/mybatis-plus-core/3.5.5/mybatis-plus-core-3.5.5.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/baomidou/mybatis-plus-annotation/3.5.5/mybatis-plus-annotation-3.5.5.jar"

# 添加Apache POI相关依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/poi/poi/5.2.4/poi-5.2.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/poi/poi-ooxml/5.2.4/poi-ooxml-5.2.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/poi/poi-scratchpad/5.2.4/poi-scratchpad-5.2.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/poi/poi-ooxml-lite/5.2.4/poi-ooxml-lite-5.2.4.jar"

# 添加Apache Commons相关依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/commons/commons-collections4/4.4/commons-collections4-4.4.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/commons/commons-compress/1.21/commons-compress-1.21.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/commons-codec/commons-codec/1.15/commons-codec-1.15.jar"

# 添加XML相关依赖
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/apache/xmlbeans/xmlbeans/5.1.1/xmlbeans-5.1.1.jar"

# 添加Jakarta Validation API
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/jakarta/validation/jakarta.validation-api/3.0.2/jakarta.validation-api-3.0.2.jar"

# 添加Hibernate Validator
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/hibernate/validator/hibernate-validator/6.2.5.Final/hibernate-validator-6.2.5.Final.jar"

# 添加Jakarta Persistence API
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/jakarta/persistence/jakarta.persistence-api/3.1.0/jakarta.persistence-api-3.1.0.jar"

# 添加Spring Messaging
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/spring-messaging/6.1.3/spring-messaging-6.1.3.jar"

# 添加Spring Data Redis
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/org/springframework/data/spring-data-redis/3.1.5/spring-data-redis-3.1.5.jar"

# 添加ZXing二维码库
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/google/zxing/core/3.5.3/core-3.5.3.jar"
CLASSPATH="$CLASSPATH:$HOME/.m2/repository/com/google/zxing/javase/3.5.3/javase-3.5.3.jar"

echo "正在编译Java源代码..."

# 编译所有Java源文件（暂时不使用Lombok注解处理器）
find src/main/java -name "*.java" -exec javac -cp "$CLASSPATH" -d target/classes {} +

if [ $? -eq 0 ]; then
    echo "编译成功！正在启动应用..."
    # 启动应用
    java -cp "$CLASSPATH" com.archive.management.ArchiveManagementApplication
else
    echo "编译失败，请检查错误信息"
    exit 1
fi