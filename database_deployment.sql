-- =====================================================
-- 档案管理系统 - 数据库部署脚本
-- 创建时间: 2025年9月22日
-- 说明: 用于创建数据库用户和设置访问权限
-- =====================================================

-- 1. 创建数据库用户
-- 注意：请根据实际需要修改密码

-- 创建应用程序数据库用户
CREATE USER IF NOT EXISTS 'archive_app'@'localhost' IDENTIFIED BY 'Archive@2024!';
CREATE USER IF NOT EXISTS 'archive_app'@'%' IDENTIFIED BY 'Archive@2024!';

-- 创建只读用户（用于报表和查询）
CREATE USER IF NOT EXISTS 'archive_readonly'@'localhost' IDENTIFIED BY 'ReadOnly@2024!';
CREATE USER IF NOT EXISTS 'archive_readonly'@'%' IDENTIFIED BY 'ReadOnly@2024!';

-- 创建备份用户
CREATE USER IF NOT EXISTS 'archive_backup'@'localhost' IDENTIFIED BY 'Backup@2024!';

-- 2. 授予权限

-- 应用程序用户权限（完整的CRUD权限）
GRANT SELECT, INSERT, UPDATE, DELETE ON archive_management_system.* TO 'archive_app'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON archive_management_system.* TO 'archive_app'@'%';

-- 授予创建临时表权限（用于复杂查询）
GRANT CREATE TEMPORARY TABLES ON archive_management_system.* TO 'archive_app'@'localhost';
GRANT CREATE TEMPORARY TABLES ON archive_management_system.* TO 'archive_app'@'%';

-- 授予执行存储过程权限
GRANT EXECUTE ON archive_management_system.* TO 'archive_app'@'localhost';
GRANT EXECUTE ON archive_management_system.* TO 'archive_app'@'%';

-- 只读用户权限
GRANT SELECT ON archive_management_system.* TO 'archive_readonly'@'localhost';
GRANT SELECT ON archive_management_system.* TO 'archive_readonly'@'%';

-- 备份用户权限
GRANT SELECT, LOCK TABLES, SHOW VIEW, EVENT, TRIGGER ON archive_management_system.* TO 'archive_backup'@'localhost';

-- 3. 安全配置

-- 设置密码策略（如果需要）
-- SET GLOBAL validate_password.policy = MEDIUM;
-- SET GLOBAL validate_password.length = 8;

-- 4. 刷新权限
FLUSH PRIVILEGES;

-- 5. 显示创建的用户
SELECT User, Host FROM mysql.user WHERE User LIKE 'archive_%';

-- 6. 验证权限设置
SHOW GRANTS FOR 'archive_app'@'localhost';
SHOW GRANTS FOR 'archive_readonly'@'localhost';
SHOW GRANTS FOR 'archive_backup'@'localhost';

-- =====================================================
-- 部署说明：
-- 1. 使用root用户或具有用户管理权限的用户执行此脚本
-- 2. 执行命令：mysql -u root -p < database_deployment.sql
-- 3. 记录用户密码信息，妥善保管
-- 4. 建议在生产环境中修改默认密码
-- =====================================================