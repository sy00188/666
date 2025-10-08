# 档案管理系统 - 部署指南

## 🚀 快速启动

### 方式一：一键启动脚本（推荐）

```bash
# 启动所有服务
./start-all.sh

# 检查服务状态
./check-status.sh

# 停止所有服务
./stop-all.sh
```

### 方式二：Docker 容器化部署

```bash
# 使用Docker启动
./docker-start.sh

# 或者手动使用docker-compose
docker-compose up -d

# 停止Docker服务
docker-compose down
```

## 📋 服务说明

| 服务 | 端口 | 访问地址 | 说明 |
|------|------|----------|------|
| 前端 | 5173/80 | http://localhost:5173 | Vue.js 前端应用 |
| 后端 | 8080 | http://localhost:8080 | Spring Boot API |
| MySQL | 3306 | localhost:3306 | 数据库服务 |
| Redis | 6379 | localhost:6379 | 缓存服务（Docker模式） |

## 🛠️ 环境要求

### 本地部署
- Java 17+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### Docker部署
- Docker 20.10+
- Docker Compose 2.0+

## 📁 项目结构

```
archive-management/
├── frontend/                 # 前端Vue应用
│   ├── src/
│   ├── package.json
│   ├── Dockerfile
│   └── nginx.conf
├── src/                     # 后端Spring Boot源码
├── *.sql                    # 数据库初始化脚本
├── start-all.sh            # 一键启动脚本
├── stop-all.sh             # 一键停止脚本
├── check-status.sh         # 状态检查脚本
├── docker-start.sh         # Docker启动脚本
├── docker-compose.yml      # Docker编排文件
├── Dockerfile.backend      # 后端Docker镜像
└── README-DEPLOYMENT.md    # 本文档
```

## 🔧 配置说明

### 数据库配置
- 默认用户：root
- 默认密码：需要在启动时输入
- 数据库名：archive_management_system

### 应用配置
- 后端端口：8080
- 前端端口：5173（开发模式）/ 80（Docker模式）
- 上传目录：./upload

## 🚨 故障排除

### 常见问题

1. **MySQL启动失败**
   ```bash
   # 检查MySQL进程
   ps aux | grep mysql
   
   # 查看MySQL错误日志
   tail -f /usr/local/mysql/data/*.err
   ```

2. **后端编译失败**
   ```bash
   # 检查Java版本
   java -version
   
   # 清理并重新编译
   mvn clean compile
   ```

3. **前端启动失败**
   ```bash
   # 重新安装依赖
   cd frontend
   rm -rf node_modules
   npm install
   ```

4. **端口被占用**
   ```bash
   # 查看端口占用
   lsof -i :8080
   lsof -i :5173
   lsof -i :3306
   
   # 杀死占用进程
   kill -9 <PID>
   ```

### 日志查看

```bash
# 查看后端日志
tail -f backend.log

# 查看前端日志
tail -f frontend.log

# 查看Docker日志
docker-compose logs -f [service_name]
```

## 🔄 更新部署

### 更新代码
```bash
# 停止服务
./stop-all.sh

# 拉取最新代码
git pull

# 重新启动
./start-all.sh
```

### 更新Docker镜像
```bash
# 停止并删除容器
docker-compose down

# 重新构建镜像
docker-compose build --no-cache

# 启动服务
docker-compose up -d
```

## 📊 监控和维护

### 健康检查
```bash
# 使用状态检查脚本
./check-status.sh

# 手动检查各服务
curl http://localhost:8080/auth/captcha  # 后端健康检查
curl http://localhost:5173               # 前端健康检查
```

### 数据备份
```bash
# 备份MySQL数据库
mysqldump -u root -p archive_management_system > backup_$(date +%Y%m%d).sql

# Docker环境备份
docker exec archive_mysql mysqldump -u root -proot123456 archive_management_system > backup_$(date +%Y%m%d).sql
```

## 🔐 安全建议

1. **修改默认密码**
   - MySQL root密码
   - Redis密码（Docker模式）
   - 应用管理员密码

2. **网络安全**
   - 生产环境建议使用HTTPS
   - 配置防火墙规则
   - 限制数据库访问IP

3. **文件权限**
   ```bash
   # 设置适当的文件权限
   chmod 600 .env.docker
   chmod 755 *.sh
   ```

## 📞 技术支持

如遇到问题，请：
1. 查看相关日志文件
2. 运行 `./check-status.sh` 检查服务状态
3. 参考本文档的故障排除部分
4. 联系技术支持团队

---

**注意**：首次部署时，请确保所有依赖都已正确安装，并根据实际环境调整配置参数。