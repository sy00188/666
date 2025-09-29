# 档案管理系统 - 数据库脚本使用指南

## 概述

本文档提供了档案管理系统数据库脚本的详细使用说明，包括安装、配置和维护指导。

## 脚本文件说明

### 1. 核心脚本文件

| 文件名 | 说明 | 执行顺序 |
|--------|------|----------|
| `01_database_init.sql` | 数据库初始化脚本（建库、建表、索引） | 1 |
| `02_constraints.sql` | 外键约束和检查约束脚本 | 2 |
| `03_triggers.sql` | 触发器脚本（业务逻辑自动化） | 3 |
| `04_init_data.sql` | 初始化数据脚本（基础数据） | 4 |
| `05_maintenance.sql` | 数据库维护脚本（备份、优化） | 5 |

## 安装步骤

### 前置条件

- MySQL 8.0+ 数据库服务器
- 具有创建数据库权限的MySQL用户
- 足够的磁盘空间（建议至少10GB）

### 1. 数据库初始化

```bash
# 连接到MySQL服务器
mysql -u root -p

# 执行数据库初始化脚本
source /path/to/01_database_init.sql
```

### 2. 创建约束

```bash
# 执行约束脚本
source /path/to/02_constraints.sql
```

### 3. 创建触发器

```bash
# 执行触发器脚本
source /path/to/03_triggers.sql
```

### 4. 插入初始数据

```bash
# 执行初始化数据脚本
source /path/to/04_init_data.sql
```

### 5. 配置维护功能

```bash
# 执行维护脚本
source /path/to/05_maintenance.sql
```

## 默认账户信息

### 系统管理员账户

| 用户名 | 密码 | 角色 | 说明 |
|--------|------|------|------|
| admin | 123456 | 超级管理员 | 系统默认管理员 |
| archive_admin | 123456 | 档案管理员 | 档案管理专用账户 |
| auditor | 123456 | 档案审核员 | 档案审核专用账户 |
| borrow_admin | 123456 | 借阅管理员 | 借阅管理专用账户 |

> **安全提醒**: 首次登录后请立即修改默认密码！

## 数据库配置

### 1. 字符集配置

```sql
-- 确认数据库字符集
SHOW CREATE DATABASE archive_management_system;

-- 应该显示：
-- CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci
```

### 2. 存储引擎配置

所有表均使用InnoDB存储引擎，支持：
- 事务处理
- 外键约束
- 行级锁定
- 崩溃恢复

### 3. 分区配置

系统自动为以下表配置分区：
- `sys_operation_log` - 按日期分区
- `sys_login_log` - 按日期分区

## 维护功能

### 1. 自动备份

```sql
-- 执行完整备份
CALL sp_backup_database('/backup/path', 'full');

-- 执行数据备份
CALL sp_backup_database('/backup/path', 'data_only');

-- 执行结构备份
CALL sp_backup_database('/backup/path', 'structure_only');
```

### 2. 分区管理

```sql
-- 管理日志分区（自动删除90天前的分区，创建未来30天的分区）
CALL sp_manage_log_partitions();
```

### 3. 数据清理

```sql
-- 清理365天前的过期数据
CALL sp_cleanup_old_data(365);

-- 清理180天前的过期数据
CALL sp_cleanup_old_data(180);
```

### 4. 数据库优化

```sql
-- 优化所有表
CALL sp_optimize_database();
```

### 5. 统计报告

```sql
-- 生成系统统计报告
CALL sp_generate_statistics();
```

### 6. 健康检查

```sql
-- 查看数据库健康状态
SELECT * FROM v_database_health;
```

## 自动化任务

系统已配置以下自动化任务：

### 每日任务（凌晨2:00）
- 分区管理
- 统计报告生成

### 每周任务（周日凌晨3:00）
- 数据库优化
- 索引重建

### 每月任务（月末凌晨4:00）
- 过期数据清理
- 完整备份

## 性能优化建议

### 1. 索引优化

```sql
-- 查看索引使用情况
SELECT 
    TABLE_NAME,
    INDEX_NAME,
    CARDINALITY,
    NULLABLE
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'archive_management_system'
ORDER BY TABLE_NAME, SEQ_IN_INDEX;
```

### 2. 查询优化

```sql
-- 启用慢查询日志
SET GLOBAL slow_query_log = 'ON';
SET GLOBAL long_query_time = 2;

-- 查看慢查询
SHOW VARIABLES LIKE 'slow_query%';
```

### 3. 内存配置

建议的MySQL配置参数：

```ini
[mysqld]
# InnoDB配置
innodb_buffer_pool_size = 2G
innodb_log_file_size = 256M
innodb_flush_log_at_trx_commit = 2

# 查询缓存
query_cache_size = 128M
query_cache_type = 1

# 连接配置
max_connections = 200
max_connect_errors = 10000
```

## 备份策略

### 1. 完整备份

```bash
# 每日完整备份
mysqldump -u root -p --single-transaction --routines --triggers \
  --databases archive_management_system > backup_$(date +%Y%m%d).sql
```

### 2. 增量备份

```bash
# 启用二进制日志
# 在my.cnf中添加：
# log-bin=mysql-bin
# binlog-format=ROW

# 增量备份
mysqlbinlog mysql-bin.000001 > incremental_backup.sql
```

### 3. 数据恢复

```bash
# 恢复完整备份
mysql -u root -p < backup_20250101.sql

# 恢复增量备份
mysql -u root -p < incremental_backup.sql
```

## 监控指标

### 关键监控指标

1. **连接数监控**
   ```sql
   SHOW STATUS LIKE 'Threads_connected';
   ```

2. **查询性能监控**
   ```sql
   SHOW STATUS LIKE 'Slow_queries';
   ```

3. **锁等待监控**
   ```sql
   SHOW STATUS LIKE 'Innodb_row_lock_waits';
   ```

4. **缓冲池命中率**
   ```sql
   SHOW STATUS LIKE 'Innodb_buffer_pool_read_requests';
   SHOW STATUS LIKE 'Innodb_buffer_pool_reads';
   ```

## 故障排除

### 常见问题

1. **外键约束错误**
   ```sql
   -- 临时禁用外键检查
   SET FOREIGN_KEY_CHECKS = 0;
   -- 执行操作
   -- 重新启用外键检查
   SET FOREIGN_KEY_CHECKS = 1;
   ```

2. **分区表问题**
   ```sql
   -- 查看分区信息
   SELECT * FROM INFORMATION_SCHEMA.PARTITIONS 
   WHERE TABLE_SCHEMA = 'archive_management_system';
   ```

3. **触发器问题**
   ```sql
   -- 查看触发器
   SHOW TRIGGERS FROM archive_management_system;
   ```

### 日志查看

```sql
-- 查看错误日志位置
SHOW VARIABLES LIKE 'log_error';

-- 查看二进制日志
SHOW BINARY LOGS;

-- 查看慢查询日志
SHOW VARIABLES LIKE 'slow_query_log_file';
```

## 安全建议

### 1. 用户权限管理

```sql
-- 创建应用专用用户
CREATE USER 'archive_app'@'localhost' IDENTIFIED BY 'strong_password';

-- 授予必要权限
GRANT SELECT, INSERT, UPDATE, DELETE ON archive_management_system.* 
TO 'archive_app'@'localhost';

-- 刷新权限
FLUSH PRIVILEGES;
```

### 2. 数据加密

```sql
-- 启用表空间加密（MySQL 8.0+）
ALTER TABLE arc_archive ENCRYPTION='Y';
ALTER TABLE arc_file ENCRYPTION='Y';
```

### 3. 审计日志

系统已内置审计功能：
- 所有档案操作记录在 `sys_operation_log`
- 用户登录记录在 `sys_login_log`
- 触发器自动记录关键业务操作

## 联系支持

如遇到问题，请检查：
1. MySQL错误日志
2. 系统操作日志表
3. 数据库健康检查视图

---

**版本**: v1.0  
**更新日期**: 2025年1月  
**兼容性**: MySQL 8.0+