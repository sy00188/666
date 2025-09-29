-- =====================================================
-- 档案管理系统 - 数据库初始化脚本
-- 版本: v1.0
-- 创建日期: 2025年9月22日
-- 说明: 创建数据库、表结构和基础索引
-- =====================================================

-- 设置字符集和时区
SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;
SET time_zone = '+08:00';

-- =====================================================
-- 1. 创建数据库
-- =====================================================
DROP DATABASE IF EXISTS `archive_management_system`;
CREATE DATABASE `archive_management_system` 
    DEFAULT CHARACTER SET utf8mb4 
    COLLATE utf8mb4_unicode_ci;

USE `archive_management_system`;

-- =====================================================
-- 2. 用户管理模块表结构
-- =====================================================

-- 2.1 部门表
DROP TABLE IF EXISTS `sys_department`;
CREATE TABLE `sys_department` (
  `department_id` bigint NOT NULL AUTO_INCREMENT COMMENT '部门ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父部门ID',
  `department_name` varchar(100) NOT NULL COMMENT '部门名称',
  `department_code` varchar(50) NOT NULL COMMENT '部门编码',
  `leader_user_id` bigint DEFAULT NULL COMMENT '部门负责人ID',
  `phone` varchar(20) DEFAULT NULL COMMENT '联系电话',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`department_id`),
  UNIQUE KEY `uk_department_code` (`department_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='部门表';

-- 2.2 角色表
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
  `role_id` bigint NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(100) NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) NOT NULL COMMENT '角色编码',
  `role_type` tinyint NOT NULL DEFAULT '2' COMMENT '角色类型(1:系统内置 2:自定义)',
  `data_scope` tinyint NOT NULL DEFAULT '4' COMMENT '数据权限(1:全部 2:本部门及下级 3:本部门 4:仅本人)',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`role_id`),
  UNIQUE KEY `uk_role_code` (`role_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 2.3 用户表
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
  `user_id` bigint NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(255) NOT NULL COMMENT '密码(加密)',
  `salt` varchar(50) NOT NULL COMMENT '密码盐值',
  `real_name` varchar(100) NOT NULL COMMENT '真实姓名',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `avatar` varchar(255) DEFAULT NULL COMMENT '头像URL',
  `department_id` bigint DEFAULT NULL COMMENT '部门ID',
  `employee_no` varchar(50) DEFAULT NULL COMMENT '员工编号',
  `position` varchar(100) DEFAULT NULL COMMENT '职位',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
  `login_count` int DEFAULT '0' COMMENT '登录次数',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`user_id`),
  UNIQUE KEY `uk_username` (`username`),
  UNIQUE KEY `uk_employee_no` (`employee_no`),
  KEY `idx_department_id` (`department_id`),
  KEY `idx_status` (`status`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- 2.4 用户角色关联表
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `role_id` bigint NOT NULL COMMENT '角色ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_user_role` (`user_id`,`role_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_role_id` (`role_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =====================================================
-- 3. 档案管理模块表结构
-- =====================================================

-- 3.1 档案分类表
DROP TABLE IF EXISTS `arc_category`;
CREATE TABLE `arc_category` (
  `category_id` bigint NOT NULL AUTO_INCREMENT COMMENT '分类ID',
  `parent_id` bigint DEFAULT '0' COMMENT '父分类ID',
  `category_name` varchar(100) NOT NULL COMMENT '分类名称',
  `category_code` varchar(50) NOT NULL COMMENT '分类编码',
  `category_level` tinyint NOT NULL DEFAULT '1' COMMENT '分类层级',
  `default_security_level` tinyint DEFAULT '1' COMMENT '默认密级(1:公开 2:内部 3:机密 4:绝密)',
  `retention_period` varchar(20) DEFAULT '长期' COMMENT '保存期限',
  `business_type` varchar(100) DEFAULT NULL COMMENT '业务类型',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`category_id`),
  UNIQUE KEY `uk_category_code` (`category_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='档案分类表';

-- 3.2 档案表
DROP TABLE IF EXISTS `arc_archive`;
CREATE TABLE `arc_archive` (
  `archive_id` bigint NOT NULL AUTO_INCREMENT COMMENT '档案ID',
  `archive_no` varchar(50) NOT NULL COMMENT '档案编号',
  `title` varchar(200) NOT NULL COMMENT '档案标题',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `security_level` tinyint NOT NULL DEFAULT '1' COMMENT '密级(1:公开 2:内部 3:机密 4:绝密)',
  `business_id` varchar(100) DEFAULT NULL COMMENT '关联业务ID',
  `keywords` varchar(500) DEFAULT NULL COMMENT '关键词',
  `abstract` text COMMENT '摘要',
  `metadata_json` json DEFAULT NULL COMMENT '元数据JSON',
  `file_count` int DEFAULT '0' COMMENT '文件数量',
  `total_size` bigint DEFAULT '0' COMMENT '总文件大小(字节)',
  `retention_period` varchar(20) DEFAULT '长期' COMMENT '保存期限',
  `expiry_date` date DEFAULT NULL COMMENT '到期日期',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1:待审核 2:已归档 3:已借出 4:审核驳回 5:已销毁)',
  `submit_user_id` bigint NOT NULL COMMENT '提交人ID',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `archive_user_id` bigint DEFAULT NULL COMMENT '归档人ID',
  `archive_time` datetime DEFAULT NULL COMMENT '归档时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`archive_id`),
  UNIQUE KEY `uk_archive_no` (`archive_no`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_security_level` (`security_level`),
  KEY `idx_business_id` (`business_id`),
  KEY `idx_status` (`status`),
  KEY `idx_submit_user_id` (`submit_user_id`),
  KEY `idx_submit_time` (`submit_time`),
  KEY `idx_archive_time` (`archive_time`),
  FULLTEXT KEY `ft_title_keywords` (`title`,`keywords`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='档案表';

-- 3.3 档案文件表
DROP TABLE IF EXISTS `arc_file`;
CREATE TABLE `arc_file` (
  `file_id` bigint NOT NULL AUTO_INCREMENT COMMENT '文件ID',
  `archive_id` bigint NOT NULL COMMENT '档案ID',
  `file_name` varchar(255) NOT NULL COMMENT '文件名',
  `original_name` varchar(255) NOT NULL COMMENT '原始文件名',
  `file_path` varchar(500) NOT NULL COMMENT '文件路径',
  `file_size` bigint NOT NULL COMMENT '文件大小(字节)',
  `file_type` varchar(50) NOT NULL COMMENT '文件类型',
  `mime_type` varchar(100) NOT NULL COMMENT 'MIME类型',
  `file_hash` varchar(64) NOT NULL COMMENT '文件哈希值(SHA256)',
  `thumbnail_path` varchar(500) DEFAULT NULL COMMENT '缩略图路径',
  `page_count` int DEFAULT NULL COMMENT '页数(PDF等)',
  `is_encrypted` tinyint DEFAULT '0' COMMENT '是否加密(0:否 1:是)',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:删除 1:正常)',
  `upload_user_id` bigint NOT NULL COMMENT '上传人ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`file_id`),
  KEY `idx_archive_id` (`archive_id`),
  KEY `idx_file_hash` (`file_hash`),
  KEY `idx_upload_user_id` (`upload_user_id`),
  KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='档案文件表';

-- 3.4 元数据模板表
DROP TABLE IF EXISTS `arc_metadata_template`;
CREATE TABLE `arc_metadata_template` (
  `template_id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `category_id` bigint NOT NULL COMMENT '分类ID',
  `field_name` varchar(100) NOT NULL COMMENT '字段名称',
  `field_code` varchar(50) NOT NULL COMMENT '字段编码',
  `field_type` varchar(20) NOT NULL COMMENT '字段类型(text/number/date/select/boolean/file)',
  `field_options` json DEFAULT NULL COMMENT '字段选项(下拉框等)',
  `is_required` tinyint DEFAULT '0' COMMENT '是否必填(0:否 1:是)',
  `default_value` varchar(500) DEFAULT NULL COMMENT '默认值',
  `validation_rule` varchar(200) DEFAULT NULL COMMENT '验证规则',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`template_id`),
  KEY `idx_category_id` (`category_id`),
  KEY `idx_field_code` (`field_code`),
  KEY `idx_sort_order` (`sort_order`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='元数据模板表';

-- =====================================================
-- 4. 借阅管理模块表结构
-- =====================================================

-- 4.1 借阅申请表
DROP TABLE IF EXISTS `arc_borrow`;
CREATE TABLE `arc_borrow` (
  `borrow_id` bigint NOT NULL AUTO_INCREMENT COMMENT '借阅ID',
  `borrow_no` varchar(50) NOT NULL COMMENT '借阅编号',
  `archive_id` bigint NOT NULL COMMENT '档案ID',
  `apply_user_id` bigint NOT NULL COMMENT '申请人ID',
  `purpose` varchar(500) NOT NULL COMMENT '借阅用途',
  `expected_days` int NOT NULL DEFAULT '7' COMMENT '期望借阅天数',
  `actual_days` int DEFAULT NULL COMMENT '实际批准天数',
  `borrow_start_time` datetime DEFAULT NULL COMMENT '借阅开始时间',
  `borrow_end_time` datetime DEFAULT NULL COMMENT '借阅结束时间',
  `return_time` datetime DEFAULT NULL COMMENT '归还时间',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(1:待审批 2:已批准 3:已拒绝 4:借阅中 5:已归还 6:已逾期)',
  `approve_user_id` bigint DEFAULT NULL COMMENT '审批人ID',
  `approve_time` datetime DEFAULT NULL COMMENT '审批时间',
  `approve_remark` varchar(500) DEFAULT NULL COMMENT '审批备注',
  `apply_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '申请时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`borrow_id`),
  UNIQUE KEY `uk_borrow_no` (`borrow_no`),
  KEY `idx_archive_id` (`archive_id`),
  KEY `idx_apply_user_id` (`apply_user_id`),
  KEY `idx_status` (`status`),
  KEY `idx_apply_time` (`apply_time`),
  KEY `idx_borrow_end_time` (`borrow_end_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='借阅申请表';

-- =====================================================
-- 5. 系统管理模块表结构
-- =====================================================

-- 5.1 系统配置表
DROP TABLE IF EXISTS `sys_config`;
CREATE TABLE `sys_config` (
  `config_id` bigint NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) NOT NULL COMMENT '配置键',
  `config_value` text NOT NULL COMMENT '配置值',
  `config_type` varchar(20) NOT NULL DEFAULT 'string' COMMENT '配置类型',
  `config_group` varchar(50) DEFAULT 'default' COMMENT '配置分组',
  `config_desc` varchar(200) DEFAULT NULL COMMENT '配置描述',
  `is_system` tinyint DEFAULT '0' COMMENT '是否系统配置(0:否 1:是)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`config_id`),
  UNIQUE KEY `uk_config_key` (`config_key`),
  KEY `idx_config_group` (`config_group`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='系统配置表';

-- 5.2 操作日志表
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) DEFAULT NULL COMMENT '用户名',
  `operation_type` varchar(50) NOT NULL COMMENT '操作类型',
  `operation_desc` varchar(200) DEFAULT NULL COMMENT '操作描述',
  `target_type` varchar(50) DEFAULT NULL COMMENT '目标类型',
  `target_id` varchar(100) DEFAULT NULL COMMENT '目标ID',
  `request_method` varchar(10) DEFAULT NULL COMMENT '请求方法',
  `request_url` varchar(500) DEFAULT NULL COMMENT '请求URL',
  `request_params` text COMMENT '请求参数',
  `response_result` text COMMENT '响应结果',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '客户端IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `execution_time` int DEFAULT NULL COMMENT '执行时间(毫秒)',
  `status` tinyint DEFAULT '1' COMMENT '状态(0:失败 1:成功)',
  `error_msg` text COMMENT '错误信息',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`log_id`, `create_time`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_operation_type` (`operation_type`),
  KEY `idx_target_type_id` (`target_type`,`target_id`),
  KEY `idx_create_time` (`create_time`),
  KEY `idx_client_ip` (`client_ip`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';

-- 5.3 登录日志表
DROP TABLE IF EXISTS `sys_login_log`;
CREATE TABLE `sys_login_log` (
  `log_id` bigint NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `login_type` varchar(20) DEFAULT 'password' COMMENT '登录类型(password/sso/ldap)',
  `client_ip` varchar(50) NOT NULL COMMENT '客户端IP',
  `client_location` varchar(100) DEFAULT NULL COMMENT '客户端位置',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `browser` varchar(50) DEFAULT NULL COMMENT '浏览器',
  `os` varchar(50) DEFAULT NULL COMMENT '操作系统',
  `status` tinyint NOT NULL COMMENT '状态(0:失败 1:成功)',
  `error_msg` varchar(200) DEFAULT NULL COMMENT '错误信息',
  `login_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  PRIMARY KEY (`log_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_username` (`username`),
  KEY `idx_client_ip` (`client_ip`),
  KEY `idx_login_time` (`login_time`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='登录日志表';

-- =====================================================
-- 6. 复合索引优化
-- =====================================================

-- 档案表复合查询索引
ALTER TABLE `arc_archive` ADD KEY `idx_category_status_time` (`category_id`, `status`, `submit_time`);
ALTER TABLE `arc_archive` ADD KEY `idx_user_status_time` (`submit_user_id`, `status`, `submit_time`);

-- 借阅表复合查询索引
ALTER TABLE `arc_borrow` ADD KEY `idx_user_status_time` (`apply_user_id`, `status`, `apply_time`);
ALTER TABLE `arc_borrow` ADD KEY `idx_archive_status` (`archive_id`, `status`);

-- =====================================================
-- 7. 操作日志表分区(按月分区)
-- =====================================================

-- 为操作日志表创建分区
ALTER TABLE `sys_operation_log` PARTITION BY RANGE (YEAR(create_time)*100 + MONTH(create_time)) (
    PARTITION p202501 VALUES LESS THAN (202502),
    PARTITION p202502 VALUES LESS THAN (202503),
    PARTITION p202503 VALUES LESS THAN (202504),
    PARTITION p202504 VALUES LESS THAN (202505),
    PARTITION p202505 VALUES LESS THAN (202506),
    PARTITION p202506 VALUES LESS THAN (202507),
    PARTITION p202507 VALUES LESS THAN (202508),
    PARTITION p202508 VALUES LESS THAN (202509),
    PARTITION p202509 VALUES LESS THAN (202510),
    PARTITION p202510 VALUES LESS THAN (202511),
    PARTITION p202511 VALUES LESS THAN (202512),
    PARTITION p202512 VALUES LESS THAN (202513),
    PARTITION p_max VALUES LESS THAN MAXVALUE
);

-- 恢复外键检查
SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================
-- 初始化脚本执行完成
-- =====================================================
SELECT 'Database initialization completed successfully!' as message;