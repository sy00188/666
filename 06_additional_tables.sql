-- =====================================================
-- 档案管理系统 - 附加功能表结构
-- 版本: v1.1
-- 创建日期: 2025年9月22日
-- 说明: 密码管理、通知模板、权限组等高级功能表结构
-- =====================================================

USE `archive_management_system`;

-- =====================================================
-- 1. 密码重置令牌表
-- =====================================================
DROP TABLE IF EXISTS `sys_password_reset_token`;
CREATE TABLE `sys_password_reset_token` (
  `token_id` bigint NOT NULL AUTO_INCREMENT COMMENT '令牌ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `token` varchar(255) NOT NULL COMMENT 'JWT重置令牌',
  `token_hash` varchar(64) NOT NULL COMMENT '令牌哈希值(用于快速查找)',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `used` tinyint NOT NULL DEFAULT '0' COMMENT '是否已使用(0:未使用 1:已使用)',
  `used_time` datetime DEFAULT NULL COMMENT '使用时间',
  `client_ip` varchar(50) DEFAULT NULL COMMENT '请求IP',
  `user_agent` varchar(500) DEFAULT NULL COMMENT '用户代理',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`token_id`),
  UNIQUE KEY `uk_token_hash` (`token_hash`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_expire_time` (`expire_time`),
  KEY `idx_used` (`used`),
  CONSTRAINT `fk_password_reset_token_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码重置令牌表';

-- =====================================================
-- 2. 密码历史记录表
-- =====================================================
DROP TABLE IF EXISTS `sys_password_history`;
CREATE TABLE `sys_password_history` (
  `history_id` bigint NOT NULL AUTO_INCREMENT COMMENT '历史记录ID',
  `user_id` bigint NOT NULL COMMENT '用户ID',
  `password` varchar(255) NOT NULL COMMENT '历史密码(加密)',
  `salt` varchar(50) NOT NULL COMMENT '密码盐值',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`history_id`),
  KEY `idx_user_id` (`user_id`),
  KEY `idx_create_time` (`create_time`),
  CONSTRAINT `fk_password_history_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`user_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='密码历史记录表';

-- =====================================================
-- 3. 通知模板表
-- =====================================================
DROP TABLE IF EXISTS `sys_notification_template`;
CREATE TABLE `sys_notification_template` (
  `template_id` bigint NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_code` varchar(100) NOT NULL COMMENT '模板编码',
  `template_name` varchar(200) NOT NULL COMMENT '模板名称',
  `template_type` varchar(50) NOT NULL COMMENT '模板类型(EMAIL,SMS,SYSTEM,PUSH)',
  `subject` varchar(500) DEFAULT NULL COMMENT '主题(邮件/推送标题)',
  `content` text NOT NULL COMMENT '模板内容(支持Freemarker语法)',
  `variables` json DEFAULT NULL COMMENT '模板变量定义(JSON格式)',
  `version` int NOT NULL DEFAULT '1' COMMENT '版本号',
  `is_active` tinyint NOT NULL DEFAULT '1' COMMENT '是否激活(0:否 1:是)',
  `category` varchar(100) DEFAULT NULL COMMENT '模板分类',
  `description` varchar(1000) DEFAULT NULL COMMENT '模板描述',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_code_version` (`template_code`, `version`),
  KEY `idx_template_type` (`template_type`),
  KEY `idx_is_active` (`is_active`),
  KEY `idx_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- =====================================================
-- 4. 权限组表
-- =====================================================
DROP TABLE IF EXISTS `sys_permission_group`;
CREATE TABLE `sys_permission_group` (
  `group_id` bigint NOT NULL AUTO_INCREMENT COMMENT '权限组ID',
  `group_name` varchar(100) NOT NULL COMMENT '权限组名称',
  `group_code` varchar(50) NOT NULL COMMENT '权限组编码',
  `parent_id` bigint DEFAULT '0' COMMENT '父权限组ID',
  `group_type` tinyint NOT NULL DEFAULT '1' COMMENT '组类型(1:功能组 2:数据组)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `sort_order` int DEFAULT '0' COMMENT '排序',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`group_id`),
  UNIQUE KEY `uk_group_code` (`group_code`),
  KEY `idx_parent_id` (`parent_id`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限组表';

-- =====================================================
-- 5. 权限组关联表
-- =====================================================
DROP TABLE IF EXISTS `sys_permission_group_relation`;
CREATE TABLE `sys_permission_group_relation` (
  `relation_id` bigint NOT NULL AUTO_INCREMENT COMMENT '关联ID',
  `group_id` bigint NOT NULL COMMENT '权限组ID',
  `permission_id` bigint NOT NULL COMMENT '权限ID',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`relation_id`),
  UNIQUE KEY `uk_group_permission` (`group_id`, `permission_id`),
  KEY `idx_group_id` (`group_id`),
  KEY `idx_permission_id` (`permission_id`),
  CONSTRAINT `fk_permission_group_relation_group` FOREIGN KEY (`group_id`) REFERENCES `sys_permission_group` (`group_id`) ON DELETE CASCADE,
  CONSTRAINT `fk_permission_group_relation_permission` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`permission_id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限组关联表';

-- =====================================================
-- 6. 权限表达式表
-- =====================================================
DROP TABLE IF EXISTS `sys_permission_expression`;
CREATE TABLE `sys_permission_expression` (
  `expression_id` bigint NOT NULL AUTO_INCREMENT COMMENT '表达式ID',
  `expression_name` varchar(100) NOT NULL COMMENT '表达式名称',
  `expression_code` varchar(50) NOT NULL COMMENT '表达式编码',
  `expression_content` text NOT NULL COMMENT '表达式内容(SpEL语法)',
  `description` varchar(500) DEFAULT NULL COMMENT '描述',
  `status` tinyint NOT NULL DEFAULT '1' COMMENT '状态(0:禁用 1:启用)',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `create_user_id` bigint DEFAULT NULL COMMENT '创建人ID',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `update_user_id` bigint DEFAULT NULL COMMENT '更新人ID',
  PRIMARY KEY (`expression_id`),
  UNIQUE KEY `uk_expression_code` (`expression_code`),
  KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='权限表达式表';

-- =====================================================
-- 7. 创建索引优化
-- =====================================================

-- 密码重置令牌表复合索引
CREATE INDEX `idx_user_expire_used` ON `sys_password_reset_token` (`user_id`, `expire_time`, `used`);

-- 通知模板表复合索引
CREATE INDEX `idx_type_active_category` ON `sys_notification_template` (`template_type`, `is_active`, `category`);

-- 权限组表复合索引
CREATE INDEX `idx_parent_status_sort` ON `sys_permission_group` (`parent_id`, `status`, `sort_order`);