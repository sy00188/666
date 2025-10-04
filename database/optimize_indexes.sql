-- 档案管理系统数据库索引优化脚本
-- 用于优化数据库查询性能

-- 用户表索引优化
-- 用户名索引（唯一）
CREATE INDEX IF NOT EXISTS idx_users_username ON users(username);

-- 邮箱索引（唯一）
CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

-- 手机号索引（唯一）
CREATE INDEX IF NOT EXISTS idx_users_phone ON users(phone);

-- 状态索引
CREATE INDEX IF NOT EXISTS idx_users_status ON users(status);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_users_create_time ON users(create_time);

-- 部门ID索引
CREATE INDEX IF NOT EXISTS idx_users_department_id ON users(department_id);

-- 复合索引：状态+创建时间
CREATE INDEX IF NOT EXISTS idx_users_status_create_time ON users(status, create_time);

-- 档案表索引优化
-- 档案编码索引（唯一）
CREATE INDEX IF NOT EXISTS idx_archives_code ON archives(code);

-- 档案标题索引
CREATE INDEX IF NOT EXISTS idx_archives_title ON archives(title);

-- 档案状态索引
CREATE INDEX IF NOT EXISTS idx_archives_status ON archives(status);

-- 创建时间索引
CREATE INDEX IF NOT EXISTS idx_archives_create_time ON archives(create_time);

-- 更新时间索引
CREATE INDEX IF NOT EXISTS idx_archives_update_time ON archives(update_time);

-- 创建者ID索引
CREATE INDEX IF NOT EXISTS idx_archives_creator_id ON archives(creator_id);

-- 复合索引：状态+创建时间
CREATE INDEX IF NOT EXISTS idx_archives_status_create_time ON archives(status, create_time);

-- 复合索引：状态+更新时间
CREATE INDEX IF NOT EXISTS idx_archives_status_update_time ON archives(status, update_time);

-- 全文搜索索引
CREATE FULLTEXT INDEX IF NOT EXISTS idx_archives_fulltext ON archives(title, description);

-- 部门表索引优化
-- 部门编码索引（唯一）
CREATE INDEX IF NOT EXISTS idx_departments_code ON departments(code);

-- 部门名称索引
CREATE INDEX IF NOT EXISTS idx_departments_name ON departments(name);

-- 部门状态索引
CREATE INDEX IF NOT EXISTS idx_departments_status ON departments(status);

-- 父部门ID索引
CREATE INDEX IF NOT EXISTS idx_departments_parent_id ON departments(parent_id);

-- 角色表索引优化
-- 角色编码索引（唯一）
CREATE INDEX IF NOT EXISTS idx_roles_code ON roles(code);

-- 角色名称索引
CREATE INDEX IF NOT EXISTS idx_roles_name ON roles(name);

-- 角色状态索引
CREATE INDEX IF NOT EXISTS idx_roles_status ON roles(status);

-- 权限表索引优化
-- 权限编码索引（唯一）
CREATE INDEX IF NOT EXISTS idx_permissions_code ON permissions(code);

-- 权限名称索引
CREATE INDEX IF NOT EXISTS idx_permissions_name ON permissions(name);

-- 权限类型索引
CREATE INDEX IF NOT EXISTS idx_permissions_type ON permissions(type);

-- 权限状态索引
CREATE INDEX IF NOT EXISTS idx_permissions_status ON permissions(status);

-- 用户角色关联表索引优化
-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_user_roles_user_id ON user_roles(user_id);

-- 角色ID索引
CREATE INDEX IF NOT EXISTS idx_user_roles_role_id ON user_roles(role_id);

-- 复合索引：用户ID+角色ID
CREATE INDEX IF NOT EXISTS idx_user_roles_user_role ON user_roles(user_id, role_id);

-- 角色权限关联表索引优化
-- 角色ID索引
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_id ON role_permissions(role_id);

-- 权限ID索引
CREATE INDEX IF NOT EXISTS idx_role_permissions_permission_id ON role_permissions(permission_id);

-- 复合索引：角色ID+权限ID
CREATE INDEX IF NOT EXISTS idx_role_permissions_role_permission ON role_permissions(role_id, permission_id);

-- 档案借阅表索引优化
-- 档案ID索引
CREATE INDEX IF NOT EXISTS idx_archive_borrows_archive_id ON archive_borrows(archive_id);

-- 借阅人ID索引
CREATE INDEX IF NOT EXISTS idx_archive_borrows_borrower_id ON archive_borrows(borrower_id);

-- 借阅状态索引
CREATE INDEX IF NOT EXISTS idx_archive_borrows_status ON archive_borrows(status);

-- 借阅时间索引
CREATE INDEX IF NOT EXISTS idx_archive_borrows_borrow_time ON archive_borrows(borrow_time);

-- 归还时间索引
CREATE INDEX IF NOT EXISTS idx_archive_borrows_return_time ON archive_borrows(return_time);

-- 复合索引：档案ID+状态
CREATE INDEX IF NOT EXISTS idx_archive_borrows_archive_status ON archive_borrows(archive_id, status);

-- 复合索引：借阅人ID+状态
CREATE INDEX IF NOT EXISTS idx_archive_borrows_borrower_status ON archive_borrows(borrower_id, status);

-- 档案文件表索引优化
-- 档案ID索引
CREATE INDEX IF NOT EXISTS idx_archive_files_archive_id ON archive_files(archive_id);

-- 文件类型索引
CREATE INDEX IF NOT EXISTS idx_archive_files_file_type ON archive_files(file_type);

-- 文件大小索引
CREATE INDEX IF NOT EXISTS idx_archive_files_file_size ON archive_files(file_size);

-- 上传时间索引
CREATE INDEX IF NOT EXISTS idx_archive_files_upload_time ON archive_files(upload_time);

-- 复合索引：档案ID+文件类型
CREATE INDEX IF NOT EXISTS idx_archive_files_archive_type ON archive_files(archive_id, file_type);

-- 系统日志表索引优化
-- 用户ID索引
CREATE INDEX IF NOT EXISTS idx_system_logs_user_id ON system_logs(user_id);

-- 操作类型索引
CREATE INDEX IF NOT EXISTS idx_system_logs_operation_type ON system_logs(operation_type);

-- 操作时间索引
CREATE INDEX IF NOT EXISTS idx_system_logs_operation_time ON system_logs(operation_time);

-- IP地址索引
CREATE INDEX IF NOT EXISTS idx_system_logs_ip_address ON system_logs(ip_address);

-- 复合索引：用户ID+操作时间
CREATE INDEX IF NOT EXISTS idx_system_logs_user_time ON system_logs(user_id, operation_time);

-- 复合索引：操作类型+操作时间
CREATE INDEX IF NOT EXISTS idx_system_logs_type_time ON system_logs(operation_type, operation_time);

-- 性能优化建议
-- 1. 定期分析表统计信息
ANALYZE TABLE users;
ANALYZE TABLE archives;
ANALYZE TABLE departments;
ANALYZE TABLE roles;
ANALYZE TABLE permissions;
ANALYZE TABLE user_roles;
ANALYZE TABLE role_permissions;
ANALYZE TABLE archive_borrows;
ANALYZE TABLE archive_files;
ANALYZE TABLE system_logs;

-- 2. 优化查询缓存
SET GLOBAL query_cache_size = 32M;
SET GLOBAL query_cache_type = 1;

-- 3. 优化InnoDB缓冲池
SET GLOBAL innodb_buffer_pool_size = 256M;

-- 4. 优化连接数
SET GLOBAL max_connections = 200;

-- 5. 优化慢查询日志
SET GLOBAL slow_query_log = 1;
SET GLOBAL long_query_time = 1;
SET GLOBAL log_queries_not_using_indexes = 1;

-- 6. 创建性能监控视图
CREATE OR REPLACE VIEW v_performance_stats AS
SELECT 
    'users' as table_name,
    COUNT(*) as total_rows,
    COUNT(CASE WHEN status = 1 THEN 1 END) as active_rows,
    COUNT(CASE WHEN status = 0 THEN 1 END) as inactive_rows
FROM users
UNION ALL
SELECT 
    'archives' as table_name,
    COUNT(*) as total_rows,
    COUNT(CASE WHEN status = 1 THEN 1 END) as active_rows,
    COUNT(CASE WHEN status = 0 THEN 1 END) as inactive_rows
FROM archives
UNION ALL
SELECT 
    'departments' as table_name,
    COUNT(*) as total_rows,
    COUNT(CASE WHEN status = 1 THEN 1 END) as active_rows,
    COUNT(CASE WHEN status = 0 THEN 1 END) as inactive_rows
FROM departments;

-- 7. 创建慢查询监控视图
CREATE OR REPLACE VIEW v_slow_queries AS
SELECT 
    user,
    host,
    db,
    command,
    time,
    state,
    info
FROM information_schema.PROCESSLIST
WHERE time > 1
ORDER BY time DESC;

-- 8. 创建索引使用统计视图
CREATE OR REPLACE VIEW v_index_usage AS
SELECT 
    table_name,
    index_name,
    cardinality,
    sub_part,
    packed,
    nullable,
    index_type
FROM information_schema.STATISTICS
WHERE table_schema = DATABASE()
ORDER BY table_name, index_name;
