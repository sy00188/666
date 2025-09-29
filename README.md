# 档案管理系统 (Archive Management System)

## 项目简介

档案管理系统是一个基于Spring Boot 3.x开发的企业级档案数字化管理平台，提供完整的档案生命周期管理功能，包括档案录入、分类管理、检索查询、权限控制、统计分析等核心功能。

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.2.2
- **安全**: Spring Security 6.x
- **数据访问**: Spring Data JPA + MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **连接池**: Druid 1.2.20
- **缓存**: Spring Cache + Redis
- **消息队列**: RabbitMQ (Spring AMQP)
- **认证**: JWT (jjwt 0.12.3)
- **文件存储**: MinIO 8.5.7
- **API文档**: Knife4j 4.4.0 (OpenAPI 3)
- **对象映射**: MapStruct 1.5.5
- **工具库**: Hutool 5.8.24
- **监控**: Spring Boot Actuator
- **测试**: JUnit 5 + Testcontainers

### 开发工具
- **Java版本**: JDK 17
- **构建工具**: Maven 3.x
- **代码简化**: Lombok 1.18.30
- **代码覆盖**: JaCoCo

## 系统架构

```
├── src/main/java/com/archive/management/
│   ├── ArchiveManagementApplication.java    # 启动类
│   ├── config/                              # 配置类
│   │   ├── DatabaseConfig.java
│   │   ├── SecurityConfig.java
│   │   ├── RedisConfig.java
│   │   └── MinioConfig.java
│   ├── controller/                          # 控制器层
│   │   ├── ArchiveController.java
│   │   └── UserController.java
│   ├── service/                             # 业务逻辑层
│   │   ├── ArchiveService.java
│   │   └── UserService.java
│   ├── repository/                          # 数据访问层
│   │   ├── ArchiveRepository.java
│   │   └── UserRepository.java
│   ├── entity/                              # 实体类
│   │   ├── Archive.java
│   │   ├── User.java
│   │   └── Category.java
│   ├── dto/                                 # 数据传输对象
│   ├── security/                            # 安全模块
│   ├── scheduler/                           # 定时任务
│   ├── interceptor/                         # 拦截器
│   ├── exception/                           # 异常处理
│   ├── util/                                # 工具类
│   └── mq/                                  # 消息队列
└── src/main/resources/
    ├── application.yml                      # 主配置文件
    └── logback-spring.xml                   # 日志配置
```

## 核心功能

### 档案管理
- 档案录入与编辑
- 档案分类管理
- 档案检索与查询
- 档案借阅管理
- 档案统计分析

### 用户管理
- 用户注册与登录
- 角色权限管理
- 用户信息维护
- 操作日志记录

### 系统功能
- 文件上传与存储
- 数据备份与恢复
- 系统监控与告警
- 定时任务调度

## 快速开始

### 环境要求
- JDK 17+
- Maven 3.6+
- MySQL 8.0+
- Redis 6.0+
- RabbitMQ 3.8+
- MinIO (可选)

### 数据库初始化
1. 创建数据库：
```sql
CREATE DATABASE archive_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 执行初始化脚本：
```bash
# 按顺序执行以下SQL文件
mysql -u root -p archive_management < 01_database_init.sql
mysql -u root -p archive_management < 02_constraints.sql
mysql -u root -p archive_management < 03_triggers.sql
mysql -u root -p archive_management < 04_init_data.sql
```

### 配置文件
修改 `src/main/resources/application.yml` 中的数据库连接信息：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/archive_management
    username: your_username
    password: your_password
```

### 启动应用
```bash
# 方式1: Maven启动
mvn spring-boot:run

# 方式2: 打包后启动
mvn clean package
java -jar target/archive-management-system-1.0.0.jar
```

### 访问应用
- 应用地址: http://localhost:8080
- API文档: http://localhost:8080/doc.html
- 健康检查: http://localhost:8080/actuator/health

## 开发指南

### 代码规范
- 遵循阿里巴巴Java开发手册
- 使用Lombok简化代码
- 统一异常处理
- 完善的单元测试

### 测试
```bash
# 运行所有测试
mvn test

# 运行特定测试类
mvn test -Dtest=ArchiveServiceTest

# 生成测试覆盖率报告
mvn jacoco:report
```

### API文档
项目集成了Knife4j，启动应用后访问 http://localhost:8080/doc.html 查看完整的API文档。

## 部署说明

### Docker部署
```bash
# 构建镜像
docker build -t archive-management-system .

# 运行容器
docker run -p 8080:8080 archive-management-system
```

### 生产环境配置
- 配置生产环境数据库连接
- 启用SSL/TLS
- 配置日志级别
- 设置JVM参数
- 配置监控告警

## 项目状态

- ✅ 项目基础架构
- ✅ 数据模型设计
- ✅ 核心业务逻辑
- ✅ API接口开发
- ✅ 安全认证授权
- ✅ 单元测试覆盖
- ✅ 集成测试
- ✅ 性能测试
- ✅ 定时任务调度
- ✅ 系统监控

## 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情。

## 联系方式

- 项目维护者: Archive Management Team
- 邮箱: archive-team@company.com
- 项目地址: https://github.com/company/archive-management-system

## 更新日志

### v1.0.0 (2024-01-XX)
- 初始版本发布
- 完整的档案管理功能
- 用户权限管理
- API文档集成
- 完善的测试覆盖

---

**注意**: 这是一个企业级档案管理系统，请确保在生产环境中正确配置安全设置和数据备份策略。