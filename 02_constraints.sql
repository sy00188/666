-- =====================================================
-- 档案管理系统 - 外键约束和检查约束脚本
-- 创建时间: 2024-01-22
-- 说明: 创建表间关系约束和数据完整性约束
-- 注意: 如果约束已存在，将跳过创建
-- =====================================================

USE archive_management_system;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- 1. 用户管理模块外键约束
-- =====================================================

-- 检查并创建用户表 -> 部门表外键
SET @constraint_exists = (SELECT COUNT(*) FROM information_schema.KEY_COLUMN_USAGE 
    WHERE CONSTRAINT_SCHEMA = 'archive_management_system' 
    AND TABLE_NAME = 'sys_user' 
    AND CONSTRAINT_NAME = 'fk_user_department');

SET @sql = IF(@constraint_exists = 0, 
    'ALTER TABLE `sys_user` ADD CONSTRAINT `fk_user_department` FOREIGN KEY (`department_id`) REFERENCES `sys_department`(`department_id`) ON DELETE SET NULL ON UPDATE CASCADE;',
    'SELECT "Constraint fk_user_department already exists" as message;');

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 用户表 -> 用户表 (创建人)
SET @sql = CONCAT('ALTER TABLE `sys_user` ADD CONSTRAINT `fk_user_create_user` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE');
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'sys_user' 
     AND CONSTRAINT_NAME = 'fk_user_create_user') = 0,
    @sql,
    'SELECT "Constraint fk_user_create_user already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 用户表 -> 用户表 (更新人)
SET @sql = CONCAT('ALTER TABLE `sys_user` ADD CONSTRAINT `fk_user_update_user` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE');
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'sys_user' 
     AND CONSTRAINT_NAME = 'fk_user_update_user') = 0,
    @sql,
    'SELECT "Constraint fk_user_update_user already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 部门表 -> 部门表 (父部门)
SET @sql = CONCAT('ALTER TABLE `sys_department` ADD CONSTRAINT `fk_department_parent` FOREIGN KEY (`parent_id`) REFERENCES `sys_department`(`department_id`) ON DELETE CASCADE ON UPDATE CASCADE');
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'sys_department' 
     AND CONSTRAINT_NAME = 'fk_department_parent') = 0,
    @sql,
    'SELECT "Constraint fk_department_parent already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 部门表 -> 用户表 (负责人)
SET @sql = CONCAT('ALTER TABLE `sys_department` ADD CONSTRAINT `fk_department_leader` FOREIGN KEY (`leader_user_id`) REFERENCES `sys_user`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE');
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'sys_department' 
     AND CONSTRAINT_NAME = 'fk_department_leader') = 0,
    @sql,
    'SELECT "Constraint fk_department_leader already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 部门表 -> 用户表 (创建人)
SET @sql = CONCAT('ALTER TABLE `sys_department` ADD CONSTRAINT `fk_department_create_user` FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE');
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'sys_department' 
     AND CONSTRAINT_NAME = 'fk_department_create_user') = 0,
    @sql,
    'SELECT "Constraint fk_department_create_user already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 部门表 -> 用户表 (更新人)
SET @sql = CONCAT('ALTER TABLE `sys_department` ADD CONSTRAINT `fk_department_update_user` FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`user_id`) ON DELETE SET NULL ON UPDATE CASCADE');
SET @sql = IF(
    (SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS 
     WHERE CONSTRAINT_SCHEMA = DATABASE() 
     AND TABLE_NAME = 'sys_department' 
     AND CONSTRAINT_NAME = 'fk_department_update_user') = 0,
    @sql,
    'SELECT "Constraint fk_department_update_user already exists" as message'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 角色表 -> 用户表 (创建人)
ALTER TABLE `sys_role` 
ADD CONSTRAINT `fk_role_create_user` 
FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 角色表 -> 用户表 (更新人)
ALTER TABLE `sys_role` 
ADD CONSTRAINT `fk_role_update_user` 
FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 用户角色关联表 -> 用户表
ALTER TABLE `sys_user_role` 
ADD CONSTRAINT `fk_user_role_user` 
FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 用户角色关联表 -> 角色表
ALTER TABLE `sys_user_role` 
ADD CONSTRAINT `fk_user_role_role` 
FOREIGN KEY (`role_id`) REFERENCES `sys_role`(`role_id`) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 用户角色关联表 -> 用户表 (创建人)
ALTER TABLE `sys_user_role` 
ADD CONSTRAINT `fk_user_role_create_user` 
FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 1.2 档案管理模块外键约束

-- 档案分类表 -> 档案分类表 (父分类)
ALTER TABLE `arc_category` 
ADD CONSTRAINT `fk_category_parent` 
FOREIGN KEY (`parent_id`) REFERENCES `arc_category`(`category_id`) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 档案分类表 -> 用户表 (创建人)
ALTER TABLE `arc_category` 
ADD CONSTRAINT `fk_category_create_user` 
FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 档案分类表 -> 用户表 (更新人)
ALTER TABLE `arc_category` 
ADD CONSTRAINT `fk_category_update_user` 
FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 档案表 -> 档案分类表
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `fk_archive_category` 
FOREIGN KEY (`category_id`) REFERENCES `arc_category`(`category_id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 档案表 -> 用户表 (提交人)
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `fk_archive_submit_user` 
FOREIGN KEY (`submit_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 档案表 -> 用户表 (归档人)
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `fk_archive_archive_user` 
FOREIGN KEY (`archive_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 档案文件表 -> 档案表
ALTER TABLE `arc_file` 
ADD CONSTRAINT `fk_file_archive` 
FOREIGN KEY (`archive_id`) REFERENCES `arc_archive`(`archive_id`) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 档案文件表 -> 用户表 (上传人)
ALTER TABLE `arc_file` 
ADD CONSTRAINT `fk_file_upload_user` 
FOREIGN KEY (`upload_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 元数据模板表 -> 档案分类表
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `fk_metadata_template_category` 
FOREIGN KEY (`category_id`) REFERENCES `arc_category`(`category_id`) 
ON DELETE CASCADE ON UPDATE CASCADE;

-- 元数据模板表 -> 用户表 (创建人)
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `fk_metadata_template_create_user` 
FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 元数据模板表 -> 用户表 (更新人)
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `fk_metadata_template_update_user` 
FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 1.3 借阅管理模块外键约束

-- 借阅申请表 -> 档案表
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `fk_borrow_archive` 
FOREIGN KEY (`archive_id`) REFERENCES `arc_archive`(`archive_id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 借阅申请表 -> 用户表 (申请人)
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `fk_borrow_apply_user` 
FOREIGN KEY (`apply_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE RESTRICT ON UPDATE CASCADE;

-- 借阅申请表 -> 用户表 (审批人)
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `fk_borrow_approve_user` 
FOREIGN KEY (`approve_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 1.4 系统管理模块外键约束

-- 系统配置表 -> 用户表 (创建人)
ALTER TABLE `sys_config` 
ADD CONSTRAINT `fk_config_create_user` 
FOREIGN KEY (`create_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 系统配置表 -> 用户表 (更新人)
ALTER TABLE `sys_config` 
ADD CONSTRAINT `fk_config_update_user` 
FOREIGN KEY (`update_user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- 注意：由于操作日志表使用了分区，MySQL不支持分区表的外键约束
-- 操作日志表的用户ID引用完整性将通过应用程序层面保证
-- ALTER TABLE `sys_operation_log` 
-- ADD CONSTRAINT `fk_operation_log_user` 
-- FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`user_id`) 
-- ON DELETE SET NULL ON UPDATE CASCADE;

-- 登录日志表 -> 用户表
ALTER TABLE `sys_login_log` 
ADD CONSTRAINT `fk_login_log_user` 
FOREIGN KEY (`user_id`) REFERENCES `sys_user`(`user_id`) 
ON DELETE SET NULL ON UPDATE CASCADE;

-- =====================================================
-- 2. 检查约束
-- =====================================================

-- 2.1 用户管理模块检查约束

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
CHECK (`role_type` IN (1, 2));

-- 角色状态检查
ALTER TABLE `sys_role` 
ADD CONSTRAINT `chk_role_status` 
CHECK (`status` IN (0, 1));

-- 数据权限范围检查
ALTER TABLE `sys_role` 
ADD CONSTRAINT `chk_role_data_scope` 
CHECK (`data_scope` IN (1, 2, 3, 4));

-- 2.2 档案管理模块检查约束

-- 档案分类状态检查
ALTER TABLE `arc_category` 
ADD CONSTRAINT `chk_category_status` 
CHECK (`status` IN (0, 1));

-- 档案分类层级检查
ALTER TABLE `arc_category` 
ADD CONSTRAINT `chk_category_level` 
CHECK (`category_level` >= 1 AND `category_level` <= 10);

-- 档案分类默认密级检查
ALTER TABLE `arc_category` 
ADD CONSTRAINT `chk_category_security_level` 
CHECK (`default_security_level` IN (1, 2, 3, 4));

-- 档案状态检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_status` 
CHECK (`status` IN (1, 2, 3, 4, 5));

-- 档案密级检查
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

-- 档案文件状态检查
ALTER TABLE `arc_file` 
ADD CONSTRAINT `chk_file_status` 
CHECK (`status` IN (0, 1));

-- 档案文件大小检查
ALTER TABLE `arc_file` 
ADD CONSTRAINT `chk_file_size` 
CHECK (`file_size` > 0);

-- 档案文件是否加密检查
ALTER TABLE `arc_file` 
ADD CONSTRAINT `chk_file_encrypted` 
CHECK (`is_encrypted` IN (0, 1));

-- 元数据模板状态检查
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `chk_metadata_template_status` 
CHECK (`status` IN (0, 1));

-- 元数据模板是否必填检查
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `chk_metadata_template_required` 
CHECK (`is_required` IN (0, 1));

-- 元数据模板字段类型检查
ALTER TABLE `arc_metadata_template` 
ADD CONSTRAINT `chk_metadata_template_field_type` 
CHECK (`field_type` IN ('text', 'textarea', 'number', 'date', 'datetime', 'select', 'boolean', 'file'));

-- 2.3 借阅管理模块检查约束

-- 借阅状态检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_status` 
CHECK (`status` IN (1, 2, 3, 4, 5, 6));

-- 借阅期望天数检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_expected_days` 
CHECK (`expected_days` > 0 AND `expected_days` <= 365);

-- 借阅实际天数检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_actual_days` 
CHECK (`actual_days` IS NULL OR (`actual_days` > 0 AND `actual_days` <= 365));

-- 2.4 系统管理模块检查约束

-- 系统配置是否系统配置检查
ALTER TABLE `sys_config` 
ADD CONSTRAINT `chk_config_is_system` 
CHECK (`is_system` IN (0, 1));

-- 系统配置类型检查
ALTER TABLE `sys_config` 
ADD CONSTRAINT `chk_config_type` 
CHECK (`config_type` IN ('string', 'number', 'boolean', 'json', 'text'));

-- 操作日志状态检查
ALTER TABLE `sys_operation_log` 
ADD CONSTRAINT `chk_operation_log_status` 
CHECK (`status` IN (0, 1));

-- 操作日志执行时间检查
ALTER TABLE `sys_operation_log` 
ADD CONSTRAINT `chk_operation_log_execution_time` 
CHECK (`execution_time` IS NULL OR `execution_time` >= 0);

-- 登录日志状态检查
ALTER TABLE `sys_login_log` 
ADD CONSTRAINT `chk_login_log_status` 
CHECK (`status` IN (0, 1));

-- 登录类型检查
ALTER TABLE `sys_login_log` 
ADD CONSTRAINT `chk_login_log_type` 
CHECK (`login_type` IN ('password', 'sso', 'ldap', 'oauth'));

-- =====================================================
-- 3. 业务逻辑约束
-- =====================================================

-- 3.1 时间逻辑约束

-- 档案归档时间不能早于提交时间
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_time_logic` 
CHECK (`archive_time` IS NULL OR `archive_time` >= `submit_time`);

-- 借阅结束时间不能早于开始时间
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_time_logic` 
CHECK (`borrow_end_time` IS NULL OR `borrow_start_time` IS NULL OR `borrow_end_time` >= `borrow_start_time`);

-- 归还时间不能早于借阅开始时间
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_return_time_logic` 
CHECK (`return_time` IS NULL OR `borrow_start_time` IS NULL OR `return_time` >= `borrow_start_time`);

-- 审批时间不能早于申请时间
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_approve_time_logic` 
CHECK (`approve_time` IS NULL OR `approve_time` >= `apply_time`);

-- 3.2 状态逻辑约束

-- 已归档的档案必须有归档人和归档时间
-- 注意：MySQL不支持复杂的CHECK约束，这类约束通常在应用层或触发器中实现

-- 已批准的借阅申请必须有审批人和审批时间
-- 注意：MySQL不支持复杂的CHECK约束，这类约束通常在应用层或触发器中实现

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 约束脚本执行完成
-- =====================================================
SELECT 'Database constraints created successfully!' as message;