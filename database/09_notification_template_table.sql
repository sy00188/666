-- 通知模板表
CREATE TABLE IF NOT EXISTS `sys_notification_templates` (
    `id` BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '模板ID',
    `template_code` VARCHAR(100) NOT NULL UNIQUE COMMENT '模板代码',
    `template_name` VARCHAR(200) NOT NULL COMMENT '模板名称',
    `template_type` VARCHAR(50) NOT NULL COMMENT '模板类型:email,sms,system,wechat',
    `content` TEXT NOT NULL COMMENT '模板内容',
    `title_template` VARCHAR(500) COMMENT '标题模板',
    `variables` VARCHAR(2000) COMMENT '模板变量(JSON数组)',
    `description` VARCHAR(500) COMMENT '模板描述',
    `enabled` BOOLEAN NOT NULL DEFAULT TRUE COMMENT '是否启用',
    `priority` VARCHAR(50) DEFAULT 'normal' COMMENT '优先级:low,normal,high,urgent',
    `channel_config` VARCHAR(1000) COMMENT '发送渠道配置(JSON)',
    `created_by` BIGINT COMMENT '创建人ID',
    `updated_by` BIGINT COMMENT '更新人ID',
    `created_at` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` BOOLEAN DEFAULT FALSE COMMENT '是否删除',
    INDEX `idx_template_code` (`template_code`),
    INDEX `idx_template_type` (`template_type`),
    INDEX `idx_enabled` (`enabled`),
    INDEX `idx_deleted` (`deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='通知模板表';

-- 插入默认模板
INSERT INTO `sys_notification_templates` (`template_code`, `template_name`, `template_type`, `content`, `title_template`, `variables`, `description`, `priority`) VALUES
('BORROW_APPLY', '借阅申请通知', 'system', '您有新的借阅申请待审批。\n申请人：${applicantName}\n档案名称：${archiveName}\n申请时间：${applyTime}', '新的借阅申请', '["applicantName","archiveName","applyTime"]', '借阅申请提醒', 'normal'),
('BORROW_APPROVE', '借阅审批通知', 'system', '您的借阅申请已通过审批。\n档案名称：${archiveName}\n借阅期限：${borrowPeriod}\n请及时取件。', '借阅申请已通过', '["archiveName","borrowPeriod"]', '借阅审批结果通知', 'normal'),
('BORROW_REJECT', '借阅驳回通知', 'system', '您的借阅申请被驳回。\n档案名称：${archiveName}\n驳回原因：${rejectReason}', '借阅申请被驳回', '["archiveName","rejectReason"]', '借阅驳回通知', 'normal'),
('BORROW_OVERDUE', '归还逾期提醒', 'system', '您有档案即将逾期，请及时归还。\n档案名称：${archiveName}\n应还日期：${dueDate}\n逾期天数：${overdueDays}天', '档案逾期提醒', '["archiveName","dueDate","overdueDays"]', '逾期提醒', 'high'),
('ARCHIVE_CREATED', '档案创建通知', 'system', '新档案已创建。\n档案名称：${archiveName}\n创建人：${creatorName}\n创建时间：${createTime}', '新档案创建', '["archiveName","creatorName","createTime"]', '档案创建通知', 'normal'),
('SYSTEM_BACKUP', '系统备份通知', 'system', '系统备份${backupStatus}。\n备份名称：${backupName}\n备份时间：${backupTime}\n备份大小：${backupSize}', '系统备份通知', '["backupStatus","backupName","backupTime","backupSize"]', '备份完成通知', 'normal');

