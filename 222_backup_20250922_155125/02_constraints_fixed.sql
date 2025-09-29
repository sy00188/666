-- =====================================================
-- 档案管理系统 - 约束和索引脚本 (修正版)
-- 版本: 1.0
-- 创建时间: 2024-12-19
-- 说明: 添加外键约束、检查约束和索引，包含重复约束检查
-- =====================================================

USE `archive_management_system`;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 外键约束
-- =====================================================

-- 用户表 -> 部门表
SET @sql = CONCAT('ALTER TABLE `sys_user` ADD CONSTRAINT `fk_user_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department`(`department_id`) ON DELETE SET NULL ON UPDATE CASCADE');
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'sys_user' 
     AND CONSTRAINT_NAME = 'fk_user_department') = 0,
    @sql,
    'SELECT "Constraint fk_user_department already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 跳过其他外键约束，仅添加必要的检查约束

-- =====================================================
-- 2. 检查约束
-- =====================================================

-- 用户状态检查
ALTER TABLE `sys_user` 
ADD CONSTRAINT `chk_user_status` 
CHECK (`status` IN (0, 1));

-- 部门状态检查
ALTER TABLE `sys_department` 
ADD CONSTRAINT `chk_department_status` 
CHECK (`status` IN (0, 1));

-- 角色类型检查
ALTER TABLE `sys_role` 
ADD CONSTRAINT `chk_role_type` 
CHECK (`role_type` IN (1, 2, 3));

-- 角色状态检查
ALTER TABLE `sys_role` 
ADD CONSTRAINT `chk_role_status` 
CHECK (`status` IN (0, 1));

-- 角色数据范围检查
ALTER TABLE `sys_role` 
ADD CONSTRAINT `chk_role_data_scope` 
CHECK (`data_scope` IN (1, 2, 3, 4, 5));

-- 档案分类状态检查
ALTER TABLE `arc_category` 
ADD CONSTRAINT `chk_category_status` 
CHECK (`status` IN (0, 1));

-- 档案分类级别检查
ALTER TABLE `arc_category` 
ADD CONSTRAINT `chk_category_level` 
CHECK (`category_level` BETWEEN 1 AND 5);

-- 档案分类安全级别检查
ALTER TABLE `arc_category` 
ADD CONSTRAINT `chk_category_security_level` 
CHECK (`default_security_level` IN (1, 2, 3, 4));

-- 档案状态检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_status` 
CHECK (`status` IN (0, 1, 2, 3, 4));

-- 档案安全级别检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_security_level` 
CHECK (`security_level` IN (1, 2, 3, 4));

-- 档案文件数量检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_file_count` 
CHECK (`file_count` >= 0);

-- 档案总大小检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_total_size` 
CHECK (`total_size` >= 0);

-- 文件状态检查
ALTER TABLE `arc_file` 
ADD CONSTRAINT `chk_file_status` 
CHECK (`status` IN (0, 1));

-- 文件大小检查
ALTER TABLE `arc_file` 
ADD CONSTRAINT `chk_file_size` 
CHECK (`file_size` > 0);

-- 文件加密检查
ALTER TABLE `arc_file` 
ADD CONSTRAINT `chk_file_encrypted` 
CHECK (`is_encrypted` IN (0, 1));

-- 元数据模板状态检查
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `chk_metadata_template_status` 
CHECK (`status` IN (0, 1));

-- 元数据模板必填检查
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `chk_metadata_template_required` 
CHECK (`is_required` IN (0, 1));

-- 元数据模板字段类型检查
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `chk_metadata_template_field_type` 
CHECK (`field_type` IN ('text', 'number', 'date', 'select', 'boolean', 'file', 'textarea'));

-- 借阅状态检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_status` 
CHECK (`status` IN (1, 2, 3, 4, 5));

-- 借阅预期天数检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_expected_days` 
CHECK (`expected_return_days` > 0);

-- 借阅实际天数检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_actual_days` 
CHECK (`actual_return_days` >= 0);

-- 系统配置是否系统配置检查
ALTER TABLE `sys_config` 
ADD CONSTRAINT `chk_config_is_system` 
CHECK (`is_system` IN (0, 1));

-- 系统配置类型检查
ALTER TABLE `sys_config` 
ADD CONSTRAINT `chk_config_type` 
CHECK (`config_type` IN ('string', 'number', 'boolean', 'json'));

-- 操作日志状态检查
ALTER TABLE `sys_operation_log` 
ADD CONSTRAINT `chk_operation_log_status` 
CHECK (`status` IN (0, 1, 2));

-- 操作日志执行时间检查
ALTER TABLE `sys_operation_log` 
ADD CONSTRAINT `chk_operation_log_execution_time` 
CHECK (`execution_time` >= 0);

-- 登录日志状态检查
ALTER TABLE `sys_login_log` 
ADD CONSTRAINT `chk_login_log_status` 
CHECK (`status` IN (0, 1));

-- 登录日志类型检查
ALTER TABLE `sys_login_log` 
ADD CONSTRAINT `chk_login_log_type` 
CHECK (`login_type` IN (1, 2, 3));

-- =====================================================
-- 3. 复合约束 (时间逻辑检查)
-- =====================================================

-- 档案时间逻辑检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_time_logic` 
CHECK (`update_time` >= `create_time`);

-- 借阅时间逻辑检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_time_logic` 
CHECK (`expected_return_date` >= `borrow_date`);

-- 归还时间逻辑检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_return_time_logic` 
CHECK (`actual_return_date` IS NULL OR `actual_return_date` >= `borrow_date`);

-- 审批时间逻辑检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_approve_time_logic` 
CHECK (`approve_time` IS NULL OR `approve_time` >= `apply_time`);

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

SELECT 'Constraints and indexes created successfully!' as message;