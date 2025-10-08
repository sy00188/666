-- =====================================================
-- 导出任务表创建脚本
-- 用于管理异步导出任务的生命周期
-- 创建时间: 2024-01-15
-- =====================================================

-- 导出任务表
CREATE TABLE IF NOT EXISTS `export_tasks` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `task_id` VARCHAR(64) NOT NULL COMMENT '任务唯一标识符',
    `task_name` VARCHAR(200) NOT NULL COMMENT '任务名称',
    `export_type` VARCHAR(50) NOT NULL COMMENT '导出类型(archive/borrow/user/custom等)',
    `format` VARCHAR(20) NOT NULL COMMENT '导出格式(excel/csv/pdf)',
    `status` VARCHAR(20) NOT NULL DEFAULT 'pending' COMMENT '任务状态(pending/processing/completed/failed/cancelled/paused)',
    `progress` INT NOT NULL DEFAULT 0 COMMENT '进度百分比(0-100)',
    `total_count` BIGINT COMMENT '总记录数',
    `processed_count` BIGINT DEFAULT 0 COMMENT '已处理记录数',
    `file_path` VARCHAR(500) COMMENT '文件路径',
    `file_name` VARCHAR(200) COMMENT '文件名',
    `file_size` BIGINT COMMENT '文件大小(字节)',
    `parameters` TEXT COMMENT '导出参数(JSON格式)',
    `error_message` TEXT COMMENT '错误信息',
    `created_by` BIGINT NOT NULL COMMENT '创建人ID',
    `created_by_name` VARCHAR(100) COMMENT '创建人姓名',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `started_at` DATETIME COMMENT '开始时间',
    `completed_at` DATETIME COMMENT '完成时间',
    `expire_at` DATETIME COMMENT '过期时间',
    `estimated_time` BIGINT COMMENT '预计剩余时间(毫秒)',
    `pausable` TINYINT(1) DEFAULT 0 COMMENT '是否可暂停',
    `paused` TINYINT(1) DEFAULT 0 COMMENT '是否已暂停',
    `priority` INT DEFAULT 5 COMMENT '优先级(1-10，数字越大优先级越高)',
    `remark` VARCHAR(500) COMMENT '备注',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_task_id` (`task_id`),
    KEY `idx_created_by` (`created_by`),
    KEY `idx_status` (`status`),
    KEY `idx_created_at` (`created_at`),
    KEY `idx_expire_at` (`expire_at`),
    KEY `idx_status_created_by` (`status`, `created_by`),
    KEY `idx_status_priority` (`status`, `priority`, `created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='导出任务表';

-- 插入测试数据（可选）
INSERT INTO `export_tasks` (
    `task_id`, `task_name`, `export_type`, `format`, `status`, 
    `progress`, `total_count`, `processed_count`, `created_by`, 
    `created_by_name`, `created_at`, `completed_at`, `expire_at`, 
    `file_path`, `file_name`, `file_size`, `priority`
) VALUES 
(
    'EXPORT_1705315200000_abc12345',
    '档案数据导出_2024年度',
    'archive',
    'excel',
    'completed',
    100,
    1500,
    1500,
    1,
    '系统管理员',
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_SUB(NOW(), INTERVAL 2 DAY),
    DATE_ADD(NOW(), INTERVAL 5 DAY),
    '/exports/archive_2024_20240115.xlsx',
    'archive_2024_20240115.xlsx',
    2048576,
    5
),
(
    'EXPORT_1705401600000_def67890',
    '借阅记录导出_近30天',
    'borrow',
    'csv',
    'processing',
    45,
    3000,
    1350,
    1,
    '系统管理员',
    NOW(),
    NULL,
    DATE_ADD(NOW(), INTERVAL 7 DAY),
    NULL,
    NULL,
    NULL,
    7
);

-- 创建定时清理过期任务的存储过程
DELIMITER $$

CREATE PROCEDURE IF NOT EXISTS `sp_clean_expired_export_tasks`()
BEGIN
    DECLARE deleted_count INT DEFAULT 0;
    
    -- 删除已过期且已完成的任务
    DELETE FROM `export_tasks` 
    WHERE `status` = 'completed' 
    AND `expire_at` < NOW();
    
    SET deleted_count = ROW_COUNT();
    
    -- 记录清理日志（如果有日志表）
    IF deleted_count > 0 THEN
        SELECT CONCAT('清理了 ', deleted_count, ' 个过期导出任务') AS result;
    END IF;
END$$

DELIMITER ;

-- 创建定时事件（每天凌晨2点执行清理）
-- 注意：需要先启用事件调度器 SET GLOBAL event_scheduler = ON;
CREATE EVENT IF NOT EXISTS `evt_clean_expired_export_tasks`
ON SCHEDULE EVERY 1 DAY
STARTS (TIMESTAMP(CURRENT_DATE) + INTERVAL 1 DAY + INTERVAL 2 HOUR)
DO CALL sp_clean_expired_export_tasks();

-- 查询语句示例
-- 1. 查询用户的所有任务
-- SELECT * FROM export_tasks WHERE created_by = 1 ORDER BY created_at DESC;

-- 2. 查询进行中的任务
-- SELECT * FROM export_tasks WHERE status IN ('pending', 'processing') ORDER BY priority DESC, created_at ASC;

-- 3. 查询已过期的任务
-- SELECT * FROM export_tasks WHERE status = 'completed' AND expire_at < NOW();

-- 4. 统计各状态任务数量
-- SELECT status, COUNT(*) as count FROM export_tasks GROUP BY status;

-- 5. 查询用户的活动任务数量
-- SELECT COUNT(*) FROM export_tasks WHERE created_by = 1 AND status IN ('pending', 'processing');

