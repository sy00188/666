-- =====================================================
-- 档案管理系统 - 简化约束脚本
-- 创建时间: 2024-01-22
-- 说明: 创建基本的检查约束，跳过外键约束
-- =====================================================

USE archive_management_system;

-- 1. 检查约束
-- =====================================================

-- 用户状态检查
ALTER TABLE `sys_user` 
ADD CONSTRAINT `chk_user_status` 
CHECK (`status` IN (0, 1));

-- 部门状态检查
ALTER TABLE `sys_department` 
ADD CONSTRAINT `chk_dept_status` 
CHECK (`status` IN (0, 1));

-- 角色状态检查
ALTER TABLE `sys_role` 
ADD CONSTRAINT `chk_role_status` 
CHECK (`status` IN (0, 1));

-- 档案状态检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_status` 
CHECK (`status` IN ('draft', 'submitted', 'reviewing', 'approved', 'rejected', 'archived'));

-- 档案密级检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_security_level` 
CHECK (`security_level` IN ('public', 'internal', 'confidential', 'secret'));

-- 借阅状态检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_status` 
CHECK (`status` IN ('pending', 'approved', 'rejected', 'borrowed', 'returned', 'overdue'));

-- 借阅类型检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_type` 
CHECK (`borrow_type` IN ('physical', 'digital', 'copy'));

-- 系统配置状态检查
ALTER TABLE `sys_config` 
ADD CONSTRAINT `chk_config_status` 
CHECK (`status` IN (0, 1));

-- 登录日志状态检查
ALTER TABLE `sys_login_log` 
ADD CONSTRAINT `chk_login_status` 
CHECK (`status` IN (0, 1));

-- 2. 时间逻辑约束
-- =====================================================

-- 借阅时间逻辑检查
ALTER TABLE `arc_borrow` 
ADD CONSTRAINT `chk_borrow_time` 
CHECK (`expected_return_time` >= `apply_time`);

-- 用户创建时间检查
ALTER TABLE `sys_user` 
ADD CONSTRAINT `chk_user_create_time` 
CHECK (`create_time` <= NOW());

-- 档案提交时间检查
ALTER TABLE `arc_archive` 
ADD CONSTRAINT `chk_archive_submit_time` 
CHECK (`submit_time` <= NOW());

SELECT 'Constraints created successfully!' as message;