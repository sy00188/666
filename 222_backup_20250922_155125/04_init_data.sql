-- =====================================================
-- 档案管理系统 - 初始化数据脚本
-- 版本: v1.0
-- 创建日期: 2025年9月22日
-- 说明: 插入系统运行必需的基础数据
-- =====================================================

USE `archive_management_system`;

-- 禁用外键检查
SET FOREIGN_KEY_CHECKS = 0;

-- =====================================================
-- 1. 系统配置数据
-- =====================================================

-- 清空系统配置表
TRUNCATE TABLE `sys_config`;

-- 插入系统配置数据
INSERT INTO `sys_config` (`config_key`, `config_value`, `config_desc`, `config_type`, `config_group`, `is_system`, `create_time`, `update_time`) VALUES
('system.name', '档案管理系统', '系统名称', 'string', 'system', 1, NOW(), NOW()),
('system.version', 'v1.0.0', '系统版本', 'string', 'system', 1, NOW(), NOW()),
('system.copyright', '© 2025 档案管理系统', '版权信息', 'string', 'system', 1, NOW(), NOW()),
('system.logo', '/static/images/logo.png', '系统Logo', 'string', 'system', 1, NOW(), NOW()),

-- 文件上传配置
('file.upload.max_size', '100', '文件上传最大大小(MB)', 'number', 'file', 0, NOW(), NOW()),
('file.upload.allowed_types', 'pdf,doc,docx,xls,xlsx,ppt,pptx,txt,jpg,jpeg,png,gif,zip,rar', '允许上传的文件类型', 'string', 'file', 0, NOW(), NOW()),
('file.storage.path', '/data/archives', '文件存储路径', 'string', 'file', 0, NOW(), NOW()),
('file.storage.type', 'local', '文件存储类型(local/oss)', 'file', 13, 1, NOW(), NOW()),

-- 借阅配置
('borrow.max_days', '30', '最大借阅天数', 'borrow', 20, 1, NOW(), NOW()),
('borrow.max_count', '5', '用户最大同时借阅数量', 'borrow', 21, 1, NOW(), NOW()),
('borrow.overdue_notice_days', '3', '逾期提醒提前天数', 'borrow', 22, 1, NOW(), NOW()),
('borrow.auto_return_days', '60', '自动归还天数', 'borrow', 23, 1, NOW(), NOW()),

-- 安全配置
('security.password_min_length', '6', '密码最小长度', 'security', 30, 1, NOW(), NOW()),
('security.password_complexity', '1', '密码复杂度要求(0:无,1:数字字母,2:数字字母符号)', 'security', 31, 1, NOW(), NOW()),
('security.login_max_attempts', '5', '登录最大尝试次数', 'security', 32, 1, NOW(), NOW()),
('security.session_timeout', '120', '会话超时时间(分钟)', 'security', 33, 1, NOW(), NOW()),

-- 邮件配置
('email.smtp_host', 'smtp.163.com', 'SMTP服务器', 'email', 40, 1, NOW(), NOW()),
('email.smtp_port', '587', 'SMTP端口', 'email', 41, 1, NOW(), NOW()),
('email.smtp_username', '', 'SMTP用户名', 'email', 42, 1, NOW(), NOW()),
('email.smtp_password', '', 'SMTP密码', 'email', 43, 1, NOW(), NOW()),
('email.from_address', 'noreply@archive.com', '发件人地址', 'email', 44, 1, NOW(), NOW()),

-- 备份配置
('backup.auto_backup', '1', '自动备份开关(0:关闭,1:开启)', 'backup', 50, 1, NOW(), NOW()),
('backup.backup_time', '02:00', '自动备份时间', 'backup', 51, 1, NOW(), NOW()),
('backup.backup_path', '/data/backup', '备份文件路径', 'backup', 52, 1, NOW(), NOW()),
('backup.keep_days', '30', '备份文件保留天数', 'backup', 53, 1, NOW(), NOW());

-- =====================================================
-- 2. 部门数据
-- =====================================================

-- 清空部门表
TRUNCATE TABLE `sys_department`;

-- 插入部门数据
INSERT INTO `sys_department` (`department_id`, `department_name`, `department_code`, `parent_id`, `sort_order`, `phone`, `email`, `status`, `remark`, `create_time`, `update_time`) VALUES
(1, '总公司', 'ROOT', 0, 1, '010-12345678', 'admin@company.com', 1, '公司总部', NOW(), NOW()),
(2, '行政部', 'ADMIN', 1, 1, '010-12345679', 'admin@company.com', 1, '负责行政管理工作', NOW(), NOW()),
(3, '人事部', 'HR', 1, 2, '010-12345680', 'hr@company.com', 1, '负责人力资源管理', NOW(), NOW()),
(4, '财务部', 'FINANCE', 1, 3, '010-12345681', 'finance@company.com', 1, '负责财务管理工作', NOW(), NOW()),
(5, '技术部', 'TECH', 1, 4, '010-12345682', 'tech@company.com', 1, '负责技术开发工作', NOW(), NOW()),
(6, '档案室', 'ARCHIVE', 2, 1, '010-12345683', 'archive@company.com', 1, '负责档案管理工作', NOW(), NOW()),
(7, '法务部', 'LEGAL', 1, 5, '010-12345684', 'legal@company.com', 1, '负责法律事务', NOW(), NOW()),
(8, '市场部', 'MARKET', 1, 6, '010-12345685', 'market@company.com', 1, '负责市场营销工作', NOW(), NOW());

-- =====================================================
-- 3. 角色数据
-- =====================================================

-- 清空角色表
TRUNCATE TABLE `sys_role`;

-- 插入角色数据
INSERT INTO `sys_role` (`role_id`, `role_name`, `role_code`, `role_type`, `data_scope`, `status`, `remark`, `create_time`, `update_time`) VALUES
(1, '超级管理员', 'SUPER_ADMIN', 1, 1, 1, '系统超级管理员，拥有所有权限', NOW(), NOW()),
(2, '系统管理员', 'SYSTEM_ADMIN', 1, 1, 1, '系统管理员，负责系统配置和用户管理', NOW(), NOW()),
(3, '档案管理员', 'ARCHIVE_ADMIN', 2, 2, 1, '档案管理员，负责档案的录入、审核、管理', NOW(), NOW()),
(4, '档案审核员', 'ARCHIVE_AUDITOR', 2, 3, 1, '档案审核员，负责档案的审核工作', NOW(), NOW()),
(5, '借阅管理员', 'BORROW_ADMIN', 2, 2, 1, '借阅管理员，负责借阅申请的审批和管理', NOW(), NOW()),
(6, '普通用户', 'NORMAL_USER', 2, 4, 1, '普通用户，可以查看档案和提交借阅申请', NOW(), NOW()),
(7, '部门管理员', 'DEPT_ADMIN', 2, 3, 1, '部门管理员，管理本部门的档案和用户', NOW(), NOW()),
(8, '只读用户', 'READ_ONLY', 2, 4, 1, '只读用户，只能查看档案信息', NOW(), NOW());

-- =====================================================
-- 4. 用户数据
-- =====================================================

-- 清空用户相关表
TRUNCATE TABLE `sys_user_role`;
TRUNCATE TABLE `sys_user`;

-- 插入用户数据
INSERT INTO `sys_user` (`user_id`, `username`, `password`, `salt`, `real_name`, `email`, `phone`, `department_id`, `position`, `employee_no`, `status`, `remark`, `create_time`, `update_time`) VALUES
(1, 'admin', '$2a$10$7JB720yubVSOfvVWdBYoOe.PuiKloYAjFYzW6Z6FBFfwtdO3L9Etm', 'salt123', '系统管理员', 'admin@system.com', '13800138000', 2, '系统管理员', 'EMP001', 1, '系统默认管理员账号', NOW(), NOW()),
(2, 'archive_admin', '$2a$10$7JB720yubVSOfvVWdBYoOe.PuiKloYAjFYzW6Z6FBFfwtdO3L9Etm', 'salt124', '档案管理员', 'archive@system.com', '13800138001', 6, '档案管理员', 'EMP002', 1, '档案管理员账号', NOW(), NOW()),
(3, 'auditor', '$2a$10$7JB720yubVSOfvVWdBYoOe.PuiKloYAjFYzW6Z6FBFfwtdO3L9Etm', 'salt125', '档案审核员', 'auditor@system.com', '13800138002', 6, '档案审核员', 'EMP003', 1, '档案审核员账号', NOW(), NOW()),
(4, 'borrow_admin', '$2a$10$7JB720yubVSOfvVWdBYoOe.PuiKloYAjFYzW6Z6FBFfwtdO3L9Etm', 'salt126', '借阅管理员', 'borrow@system.com', '13800138003', 6, '借阅管理员', 'EMP004', 1, '借阅管理员账号', NOW(), NOW()),
(5, 'user001', '$2a$10$7JB720yubVSOfvVWdBYoOe.PuiKloYAjFYzW6Z6FBFfwtdO3L9Etm', 'salt127', '张三', 'zhangsan@company.com', '13800138004', 3, '人事专员', 'EMP005', 1, '普通用户账号', NOW(), NOW()),
(6, 'user002', '$2a$10$7JB720yubVSOfvVWdBYoOe.PuiKloYAjFYzW6Z6FBFfwtdO3L9Etm', 'salt128', '李四', 'lisi@company.com', '13800138005', 4, '财务专员', 'EMP006', 1, '普通用户账号', NOW(), NOW());

-- 插入用户角色关联数据
INSERT INTO `sys_user_role` (`user_id`, `role_id`, `create_time`) VALUES
(1, 1, NOW()), -- admin -> 超级管理员
(1, 2, NOW()), -- admin -> 系统管理员
(2, 3, NOW()), -- archive_admin -> 档案管理员
(3, 4, NOW()), -- auditor -> 档案审核员
(4, 5, NOW()), -- borrow_admin -> 借阅管理员
(5, 6, NOW()), -- user001 -> 普通用户
(6, 6, NOW()); -- user002 -> 普通用户

-- =====================================================
-- 5. 档案分类数据
-- =====================================================

-- 清空档案分类表
TRUNCATE TABLE `arc_category`;

-- 插入档案分类数据
INSERT INTO `arc_category` (`category_id`, `category_name`, `category_code`, `parent_id`, `category_level`, `default_security_level`, `sort_order`, `status`, `create_time`, `update_time`) VALUES
-- 一级分类
(1, '人事档案', 'HR', 0, 1, 2, 1, 1, NOW(), NOW()),
(2, '财务档案', 'FINANCE', 0, 1, 3, 2, 1, NOW(), NOW()),
(3, '合同档案', 'CONTRACT', 0, 1, 2, 3, 1, NOW(), NOW()),
(4, '技术档案', 'TECH', 0, 1, 2, 4, 1, NOW(), NOW()),
(5, '行政档案', 'ADMIN', 0, 1, 1, 5, 1, NOW(), NOW()),
(6, '法务档案', 'LEGAL', 0, 1, 3, 6, 1, NOW(), NOW()),

-- 人事档案二级分类
(11, '员工档案', 'HR_EMPLOYEE', 1, 2, 2, 1, 1, NOW(), NOW()),
(12, '招聘档案', 'HR_RECRUIT', 1, 2, 1, 2, 1, NOW(), NOW()),
(13, '培训档案', 'HR_TRAINING', 1, 2, 1, 3, 1, NOW(), NOW()),
(14, '考核档案', 'HR_ASSESSMENT', 1, 2, 2, 4, 1, NOW(), NOW()),

-- 财务档案二级分类
(21, '会计凭证', 'FIN_VOUCHER', 2, 2, 3, 1, 1, NOW(), NOW()),
(22, '财务报表', 'FIN_REPORT', 2, 2, 3, 2, 1, NOW(), NOW()),
(23, '税务档案', 'FIN_TAX', 2, 2, 3, 3, 1, NOW(), NOW()),
(24, '审计档案', 'FIN_AUDIT', 2, 2, 3, 4, 1, NOW(), NOW()),

-- 合同档案二级分类
(31, '销售合同', 'CON_SALES', 3, 2, 2, 1, 1, NOW(), NOW()),
(32, '采购合同', 'CON_PURCHASE', 3, 2, 2, 2, 1, NOW(), NOW()),
(33, '劳动合同', 'CON_LABOR', 3, 2, 2, 3, 1, NOW(), NOW()),
(34, '服务合同', 'CON_SERVICE', 3, 2, 2, 4, 1, NOW(), NOW()),

-- 技术档案二级分类
(41, '产品文档', 'TECH_PRODUCT', 4, 2, 2, 1, 1, NOW(), NOW()),
(42, '开发文档', 'TECH_DEV', 4, 2, 2, 2, 1, NOW(), NOW()),
(43, '测试文档', 'TECH_TEST', 4, 2, 1, 3, 1, NOW(), NOW()),
(44, '运维文档', 'TECH_OPS', 4, 2, 1, 4, 1, NOW(), NOW()),

-- 行政档案二级分类
(51, '公司制度', 'ADMIN_POLICY', 5, 2, 1, 1, 1, NOW(), NOW()),
(52, '会议纪要', 'ADMIN_MEETING', 5, 2, 1, 2, 1, NOW(), NOW()),
(53, '通知公告', 'ADMIN_NOTICE', 5, 2, 1, 3, 1, NOW(), NOW()),
(54, '办公用品', 'ADMIN_OFFICE', 5, 2, 1, 4, 1, NOW(), NOW()),

-- 法务档案二级分类
(61, '诉讼档案', 'LEGAL_LAWSUIT', 6, 2, 3, 1, 1, NOW(), NOW()),
(62, '知识产权', 'LEGAL_IP', 6, 2, 3, 2, 1, NOW(), NOW()),
(63, '法律意见', 'LEGAL_OPINION', 6, 2, 3, 3, 1, NOW(), NOW()),
(64, '合规档案', 'LEGAL_COMPLIANCE', 6, 2, 3, 4, 1, NOW(), NOW());

-- =====================================================
-- 6. 元数据模板数据
-- =====================================================

-- 清空元数据模板表
TRUNCATE TABLE `arc_metadata_template`;

-- 插入元数据模板数据
INSERT INTO `arc_metadata_template` (`template_id`, `category_id`, `field_name`, `field_code`, `field_type`, `field_options`, `is_required`, `default_value`, `validation_rule`, `sort_order`, `status`, `create_time`, `create_user_id`) VALUES
(1, 11, '员工编号', 'employee_no', 'text', NULL, 1, NULL, 'max_length:20', 1, 1, NOW(), 1),
(2, 11, '身份证号', 'id_card', 'text', NULL, 1, NULL, 'max_length:18', 2, 1, NOW(), 1),
(3, 11, '学历', 'education', 'select', '["高中", "大专", "本科", "硕士", "博士"]', 0, NULL, NULL, 3, 1, NOW(), 1),
(4, 11, '入职日期', 'entry_date', 'date', NULL, 1, NULL, NULL, 4, 1, NOW(), 1),
(5, 11, '紧急联系人', 'emergency_contact', 'text', NULL, 0, NULL, 'max_length:50', 5, 1, NOW(), 1),
(6, 31, '合同编号', 'contract_no', 'text', NULL, 1, NULL, 'max_length:30', 1, 1, NOW(), 1),
(7, 31, '甲方', 'party_a', 'text', NULL, 1, NULL, 'max_length:100', 2, 1, NOW(), 1),
(8, 31, '乙方', 'party_b', 'text', NULL, 1, NULL, 'max_length:100', 3, 1, NOW(), 1),
(9, 31, '合同金额', 'contract_amount', 'number', NULL, 0, NULL, NULL, 4, 1, NOW(), 1),
(10, 31, '签署日期', 'sign_date', 'date', NULL, 1, NULL, NULL, 5, 1, NOW(), 1),
(11, 31, '到期日期', 'expire_date', 'date', NULL, 0, NULL, NULL, 6, 1, NOW(), 1),
(12, 21, '凭证号', 'voucher_no', 'text', NULL, 1, NULL, 'max_length:20', 1, 1, NOW(), 1),
(13, 21, '会计期间', 'accounting_period', 'text', NULL, 1, NULL, 'max_length:10', 2, 1, NOW(), 1),
(14, 21, '金额', 'amount', 'number', NULL, 1, NULL, NULL, 3, 1, NOW(), 1),
(15, 21, '币种', 'currency', 'select', '["人民币", "美元", "欧元"]', 1, NULL, NULL, 4, 1, NOW(), 1),
(16, 21, '制单人', 'accountant', 'text', NULL, 0, NULL, 'max_length:50', 5, 1, NOW(), 1),
(17, 41, '文档版本', 'doc_version', 'text', NULL, 1, NULL, 'max_length:10', 1, 1, NOW(), 1),
(18, 41, '作者', 'author', 'text', NULL, 1, NULL, 'max_length:50', 2, 1, NOW(), 1),
(19, 41, '审核人', 'reviewer', 'text', NULL, 0, NULL, 'max_length:50', 3, 1, NOW(), 1),
(20, 41, '技术栈', 'tech_stack', 'textarea', NULL, 0, NULL, NULL, 4, 1, NOW(), 1),
(21, 41, '更新日志', 'update_log', 'textarea', NULL, 0, NULL, NULL, 5, 1, NOW(), 1);

-- 启用外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 初始化数据脚本执行完成
-- =====================================================
SELECT 'Initial data inserted successfully!' as message;
SELECT 
    '系统配置' as data_type, COUNT(*) as record_count 
FROM sys_config
UNION ALL
SELECT 
    '部门数据' as data_type, COUNT(*) as record_count 
FROM sys_department
UNION ALL
SELECT 
    '角色数据' as data_type, COUNT(*) as record_count 
FROM sys_role
UNION ALL
SELECT 
    '用户数据' as data_type, COUNT(*) as record_count 
FROM sys_user
UNION ALL
SELECT 
    '档案分类' as data_type, COUNT(*) as record_count 
FROM arc_category
UNION ALL
SELECT 
    '元数据模板' as data_type, COUNT(*) as record_count 
FROM arc_metadata_template;