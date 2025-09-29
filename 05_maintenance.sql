-- =====================================================
-- 档案管理系统 - 数据库维护脚本
-- 版本: v1.0
-- 创建日期: 2025年9月22日
-- 说明: 数据库维护、备份、分区管理脚本
-- =====================================================

USE `archive_management_system`;

-- =====================================================
-- 1. 数据库备份相关存储过程
-- =====================================================

-- 设置分隔符
DELIMITER $$

-- 删除已存在的存储过程
DROP PROCEDURE IF EXISTS `sp_backup_database`$$

-- 创建数据库备份存储过程
CREATE PROCEDURE `sp_backup_database`(
    IN backup_path VARCHAR(500),
    IN backup_type ENUM('full', 'data_only', 'structure_only')
)
BEGIN
    DECLARE backup_file VARCHAR(600);
    DECLARE backup_sql TEXT;
    
    -- 生成备份文件名
    SET backup_file = CONCAT(backup_path, '/archive_system_', 
                            DATE_FORMAT(NOW(), '%Y%m%d_%H%i%s'), '.sql');
    
    -- 根据备份类型生成不同的mysqldump命令
    CASE backup_type
        WHEN 'full' THEN
            SET backup_sql = CONCAT('mysqldump -u root -p --single-transaction --routines --triggers ',
                                  '--databases archive_management_system > ', backup_file);
        WHEN 'data_only' THEN
            SET backup_sql = CONCAT('mysqldump -u root -p --single-transaction --no-create-info ',
                                  '--databases archive_management_system > ', backup_file);
        WHEN 'structure_only' THEN
            SET backup_sql = CONCAT('mysqldump -u root -p --no-data --routines --triggers ',
                                  '--databases archive_management_system > ', backup_file);
    END CASE;
    
    -- 记录备份日志
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        1, 'BACKUP', CONCAT('数据库备份: ', backup_type, ' -> ', backup_file), 
        'DATABASE', 0, 1, NOW()
    );
    
    -- 输出备份命令（需要在应用层执行）
    SELECT backup_sql as backup_command, backup_file as backup_file_path;
END$$

-- =====================================================
-- 2. 分区管理相关存储过程
-- =====================================================

-- 删除已存在的存储过程
DROP PROCEDURE IF EXISTS `sp_manage_log_partitions`$$

-- 创建日志表分区管理存储过程
CREATE PROCEDURE `sp_manage_log_partitions`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE partition_name VARCHAR(50);
    DECLARE partition_date DATE;
    DECLARE partition_sql TEXT;
    DECLARE cur CURSOR FOR 
        SELECT PARTITION_NAME, 
               STR_TO_DATE(SUBSTRING(PARTITION_NAME, 2), '%Y%m%d') as part_date
        FROM INFORMATION_SCHEMA.PARTITIONS 
        WHERE TABLE_SCHEMA = 'archive_management_system' 
          AND TABLE_NAME = 'sys_operation_log' 
          AND PARTITION_NAME IS NOT NULL
          AND PARTITION_NAME != 'p_max';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 删除90天前的分区
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO partition_name, partition_date;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 如果分区日期超过90天，删除该分区
        IF partition_date < DATE_SUB(CURDATE(), INTERVAL 90 DAY) THEN
            SET partition_sql = CONCAT('ALTER TABLE sys_operation_log DROP PARTITION ', partition_name);
            SET @sql = partition_sql;
            PREPARE stmt FROM @sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
            
            -- 记录分区删除日志
            INSERT INTO `sys_operation_log` (
                `user_id`, `operation_type`, `operation_desc`, 
                `target_type`, `target_id`, `status`, `create_time`
            ) VALUES (
                1, 'PARTITION', CONCAT('删除过期分区: ', partition_name), 
                'PARTITION', 0, 1, NOW()
            );
        END IF;
    END LOOP;
    CLOSE cur;
    
    -- 创建未来30天的分区
    SET @i = 0;
    WHILE @i < 30 DO
        SET @future_date = DATE_ADD(CURDATE(), INTERVAL @i DAY);
        SET @partition_name = CONCAT('p', DATE_FORMAT(@future_date, '%Y%m%d'));
        SET @partition_value = DATE_FORMAT(DATE_ADD(@future_date, INTERVAL 1 DAY), '%Y-%m-%d');
        
        -- 检查分区是否已存在
        SET @partition_exists = 0;
        SELECT COUNT(*) INTO @partition_exists
        FROM INFORMATION_SCHEMA.PARTITIONS 
        WHERE TABLE_SCHEMA = 'archive_management_system' 
          AND TABLE_NAME = 'sys_operation_log' 
          AND PARTITION_NAME = @partition_name;
        
        -- 如果分区不存在，则创建
        IF @partition_exists = 0 THEN
            SET @partition_sql = CONCAT(
                'ALTER TABLE sys_operation_log ADD PARTITION (',
                'PARTITION ', @partition_name, 
                ' VALUES LESS THAN (TO_DAYS(''', @partition_value, ''')))'
            );
            PREPARE stmt FROM @partition_sql;
            EXECUTE stmt;
            DEALLOCATE PREPARE stmt;
        END IF;
        
        SET @i = @i + 1;
    END WHILE;
END$$

-- =====================================================
-- 3. 数据清理相关存储过程
-- =====================================================

-- 删除已存在的存储过程
DROP PROCEDURE IF EXISTS `sp_cleanup_old_data`$$

-- 创建数据清理存储过程
CREATE PROCEDURE `sp_cleanup_old_data`(
    IN cleanup_days INT
)
BEGIN
    DECLARE deleted_count INT DEFAULT 0;
    DECLARE cleanup_date DATE;
    
    SET cleanup_date = DATE_SUB(CURDATE(), INTERVAL cleanup_days DAY);
    
    -- 清理过期的登录日志
    DELETE FROM `sys_login_log` 
    WHERE `login_time` < cleanup_date;
    SET deleted_count = ROW_COUNT();
    
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        1, 'CLEANUP', CONCAT('清理登录日志: ', deleted_count, ' 条记录'), 
        'LOGIN_LOG', 0, 1, NOW()
    );
    
    -- 清理过期的操作日志（保留最近的记录）
    DELETE FROM `sys_operation_log` 
    WHERE `create_time` < cleanup_date 
      AND `operation_type` NOT IN ('BACKUP', 'CLEANUP', 'PARTITION');
    SET deleted_count = ROW_COUNT();
    
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        1, 'CLEANUP', CONCAT('清理操作日志: ', deleted_count, ' 条记录'), 
        'OPERATION_LOG', 0, 1, NOW()
    );
    
    -- 清理已删除的档案文件记录
    DELETE FROM `arc_file` 
    WHERE `status` = 0 AND `update_time` < cleanup_date;
    SET deleted_count = ROW_COUNT();
    
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        1, 'CLEANUP', CONCAT('清理已删除文件记录: ', deleted_count, ' 条记录'), 
        'FILE', 0, 1, NOW()
    );
END$$

-- =====================================================
-- 4. 数据库优化相关存储过程
-- =====================================================

-- 删除已存在的存储过程
DROP PROCEDURE IF EXISTS `sp_optimize_database`$$

-- 创建数据库优化存储过程
CREATE PROCEDURE `sp_optimize_database`()
BEGIN
    DECLARE done INT DEFAULT FALSE;
    DECLARE table_name VARCHAR(100);
    DECLARE optimize_sql TEXT;
    DECLARE cur CURSOR FOR 
        SELECT TABLE_NAME 
        FROM INFORMATION_SCHEMA.TABLES 
        WHERE TABLE_SCHEMA = 'archive_management_system' 
          AND TABLE_TYPE = 'BASE TABLE';
    DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;
    
    -- 优化所有表
    OPEN cur;
    read_loop: LOOP
        FETCH cur INTO table_name;
        IF done THEN
            LEAVE read_loop;
        END IF;
        
        -- 执行表优化
        SET optimize_sql = CONCAT('OPTIMIZE TABLE ', table_name);
        SET @sql = optimize_sql;
        PREPARE stmt FROM @sql;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
        
        -- 记录优化日志
        INSERT INTO `sys_operation_log` (
            `user_id`, `operation_type`, `operation_desc`, 
            `target_type`, `target_id`, `status`, `create_time`
        ) VALUES (
            1, 'OPTIMIZE', CONCAT('优化表: ', table_name), 
            'TABLE', 0, 1, NOW()
        );
    END LOOP;
    CLOSE cur;
    
    -- 更新表统计信息
    ANALYZE TABLE 
        `sys_user`, `sys_role`, `sys_department`, `sys_config`,
        `arc_category`, `arc_archive`, `arc_file`, `arc_borrow`,
        `sys_operation_log`, `sys_login_log`;
END$$

-- =====================================================
-- 5. 数据统计相关存储过程
-- =====================================================

-- 删除已存在的存储过程
DROP PROCEDURE IF EXISTS `sp_generate_statistics`$$

-- 创建数据统计存储过程
CREATE PROCEDURE `sp_generate_statistics`()
BEGIN
    -- 创建临时统计表
    DROP TEMPORARY TABLE IF EXISTS temp_statistics;
    CREATE TEMPORARY TABLE temp_statistics (
        stat_type VARCHAR(50),
        stat_name VARCHAR(100),
        stat_value BIGINT,
        stat_date DATE
    );
    
    -- 用户统计
    INSERT INTO temp_statistics 
    SELECT 'USER', '总用户数', COUNT(*), CURDATE() FROM sys_user WHERE status = 1;
    
    INSERT INTO temp_statistics 
    SELECT 'USER', '活跃用户数', COUNT(DISTINCT user_id), CURDATE() 
    FROM sys_login_log 
    WHERE login_time >= DATE_SUB(CURDATE(), INTERVAL 30 DAY);
    
    -- 档案统计
    INSERT INTO temp_statistics 
    SELECT 'ARCHIVE', '总档案数', COUNT(*), CURDATE() FROM arc_archive;
    
    INSERT INTO temp_statistics 
    SELECT 'ARCHIVE', '已归档数', COUNT(*), CURDATE() 
    FROM arc_archive WHERE status = 2;
    
    INSERT INTO temp_statistics 
    SELECT 'ARCHIVE', '借阅中数', COUNT(*), CURDATE() 
    FROM arc_archive WHERE status = 3;
    
    -- 文件统计
    INSERT INTO temp_statistics 
    SELECT 'FILE', '总文件数', COUNT(*), CURDATE() FROM arc_file WHERE status = 1;
    
    INSERT INTO temp_statistics 
    SELECT 'FILE', '总文件大小(MB)', ROUND(SUM(file_size)/1024/1024), CURDATE() 
    FROM arc_file WHERE status = 1;
    
    -- 借阅统计
    INSERT INTO temp_statistics 
    SELECT 'BORROW', '总借阅次数', COUNT(*), CURDATE() FROM arc_borrow;
    
    INSERT INTO temp_statistics 
    SELECT 'BORROW', '当前借阅中', COUNT(*), CURDATE() 
    FROM arc_borrow WHERE status = 4;
    
    INSERT INTO temp_statistics 
    SELECT 'BORROW', '逾期未还', COUNT(*), CURDATE() 
    FROM arc_borrow 
    WHERE status = 4 AND borrow_end_time < CURDATE();
    
    -- 返回统计结果
    SELECT * FROM temp_statistics ORDER BY stat_type, stat_name;
    
    -- 记录统计日志
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        1, 'STATISTICS', '生成系统统计报告', 
        'SYSTEM', 0, 1, NOW()
    );
END$$

-- =====================================================
-- 6. 定时任务相关事件
-- =====================================================

-- 启用事件调度器
SET GLOBAL event_scheduler = ON;

-- 删除已存在的事件
DROP EVENT IF EXISTS `evt_daily_maintenance`$$
DROP EVENT IF EXISTS `evt_weekly_optimization`$$
DROP EVENT IF EXISTS `evt_monthly_cleanup`$$

-- 创建每日维护事件
CREATE EVENT `evt_daily_maintenance`
ON SCHEDULE EVERY 1 DAY
STARTS TIMESTAMP(CURDATE() + INTERVAL 1 DAY, '02:00:00')
DO
BEGIN
    -- 管理分区
    CALL sp_manage_log_partitions();
    
    -- 生成统计报告
    CALL sp_generate_statistics();
END$$

-- 创建每周优化事件
CREATE EVENT `evt_weekly_optimization`
ON SCHEDULE EVERY 1 WEEK
STARTS TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL (7 - WEEKDAY(CURDATE())) DAY), '03:00:00')
DO
BEGIN
    -- 优化数据库
    CALL sp_optimize_database();
END$$

-- 创建每月清理事件
CREATE EVENT `evt_monthly_cleanup`
ON SCHEDULE EVERY 1 MONTH
STARTS TIMESTAMP(LAST_DAY(CURDATE()) + INTERVAL 1 DAY, '04:00:00')
DO
BEGIN
    -- 清理过期数据
    CALL sp_cleanup_old_data(365);
END$$

-- 恢复分隔符
DELIMITER ;

-- =====================================================
-- 7. 数据库健康检查视图
-- =====================================================

-- 创建数据库健康检查视图
CREATE OR REPLACE VIEW `v_database_health` AS
SELECT 
    'Table Size' as check_type,
    TABLE_NAME as check_item,
    CONCAT(ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2), ' MB') as check_value,
    CASE 
        WHEN (DATA_LENGTH + INDEX_LENGTH) > 1024*1024*1024 THEN 'WARNING'
        ELSE 'OK'
    END as status
FROM INFORMATION_SCHEMA.TABLES 
WHERE TABLE_SCHEMA = 'archive_management_system'
  AND TABLE_TYPE = 'BASE TABLE'

UNION ALL

SELECT 
    'Index Usage' as check_type,
    CONCAT(TABLE_NAME, '.', INDEX_NAME) as check_item,
    CONCAT('Cardinality: ', CARDINALITY) as check_value,
    CASE 
        WHEN CARDINALITY = 0 THEN 'WARNING'
        ELSE 'OK'
    END as status
FROM INFORMATION_SCHEMA.STATISTICS 
WHERE TABLE_SCHEMA = 'archive_management_system'
  AND NON_UNIQUE = 1

UNION ALL

SELECT 
    'Connection' as check_type,
    'Active Connections' as check_item,
    '0' as check_value,
    'OK' as status

UNION ALL

SELECT 
    'Performance' as check_type,
    'Slow Queries' as check_item,
    '0' as check_value,
    'OK' as status;

-- =====================================================
-- 8. 创建维护脚本使用说明
-- =====================================================

-- 创建维护脚本使用说明表
CREATE TABLE IF NOT EXISTS `maintenance_instructions` (
    `id` INT AUTO_INCREMENT PRIMARY KEY,
    `script_name` VARCHAR(100) NOT NULL,
    `description` TEXT NOT NULL,
    `usage_example` TEXT,
    `frequency` VARCHAR(50),
    `create_time` TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 插入使用说明
INSERT INTO `maintenance_instructions` (`script_name`, `description`, `usage_example`, `frequency`) VALUES
('sp_backup_database', '数据库备份存储过程', 'CALL sp_backup_database(''/backup/path'', ''full'');', '每日'),
('sp_manage_log_partitions', '日志表分区管理', 'CALL sp_manage_log_partitions();', '每日'),
('sp_cleanup_old_data', '清理过期数据', 'CALL sp_cleanup_old_data(365);', '每月'),
('sp_optimize_database', '数据库优化', 'CALL sp_optimize_database();', '每周'),
('sp_generate_statistics', '生成统计报告', 'CALL sp_generate_statistics();', '每日'),
('v_database_health', '数据库健康检查视图', 'SELECT * FROM v_database_health;', '按需'),
('evt_daily_maintenance', '每日维护事件', '自动执行，每天凌晨2点', '每日'),
('evt_weekly_optimization', '每周优化事件', '自动执行，每周日凌晨3点', '每周'),
('evt_monthly_cleanup', '每月清理事件', '自动执行，每月最后一天凌晨4点', '每月');

-- =====================================================
-- 维护脚本执行完成
-- =====================================================
SELECT 'Database maintenance scripts created successfully!' as message;
SELECT 'Use the following commands to manage database maintenance:' as instruction;
SELECT script_name, description, usage_example, frequency 
FROM maintenance_instructions 
ORDER BY 
    CASE frequency 
        WHEN '每日' THEN 1 
        WHEN '每周' THEN 2 
        WHEN '每月' THEN 3 
        ELSE 4 
    END;