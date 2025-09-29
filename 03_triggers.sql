-- =====================================================
-- 档案管理系统 - 触发器脚本
-- 版本: v1.0
-- 创建日期: 2025年9月22日  
-- 说明: 创建触发器实现业务逻辑自动化
-- =====================================================

USE `archive_management_system`;

-- 设置分隔符
DELIMITER $$

-- =====================================================
-- 1. 档案编号自动生成触发器
-- =====================================================

-- 删除已存在的触发器
DROP TRIGGER IF EXISTS `tr_archive_no_generate`$$

-- 创建档案编号自动生成触发器
CREATE TRIGGER `tr_archive_no_generate` 
    BEFORE INSERT ON `arc_archive`
    FOR EACH ROW
BEGIN
    DECLARE next_seq INT DEFAULT 1;
    DECLARE year_prefix VARCHAR(10);
    
    -- 如果档案编号为空或NULL，则自动生成
    IF NEW.archive_no IS NULL OR NEW.archive_no = '' THEN
        -- 获取当前年份前缀
        SET year_prefix = CONCAT('DA-', YEAR(NOW()), '-');
        
        -- 获取当年的下一个序号
        SELECT IFNULL(MAX(CAST(SUBSTRING(archive_no, -6) AS UNSIGNED)), 0) + 1 
        INTO next_seq
        FROM arc_archive 
        WHERE archive_no LIKE CONCAT(year_prefix, '%');
        
        -- 生成新的档案编号：DA-2025-000001
        SET NEW.archive_no = CONCAT(year_prefix, LPAD(next_seq, 6, '0'));
    END IF;
END$$

-- =====================================================
-- 2. 借阅编号自动生成触发器
-- =====================================================

-- 删除已存在的触发器
DROP TRIGGER IF EXISTS `tr_borrow_no_generate`$$

-- 创建借阅编号自动生成触发器
CREATE TRIGGER `tr_borrow_no_generate` 
    BEFORE INSERT ON `arc_borrow`
    FOR EACH ROW
BEGIN
    DECLARE next_seq INT DEFAULT 1;
    DECLARE year_month_prefix VARCHAR(15);
    
    -- 如果借阅编号为空或NULL，则自动生成
    IF NEW.borrow_no IS NULL OR NEW.borrow_no = '' THEN
        -- 获取当前年月前缀
        SET year_month_prefix = CONCAT('BR-', DATE_FORMAT(NOW(), '%Y%m'), '-');
        
        -- 获取当月的下一个序号
        SELECT IFNULL(MAX(CAST(SUBSTRING(borrow_no, -4) AS UNSIGNED)), 0) + 1 
        INTO next_seq
        FROM arc_borrow 
        WHERE borrow_no LIKE CONCAT(year_month_prefix, '%');
        
        -- 生成新的借阅编号：BR-202501-0001
        SET NEW.borrow_no = CONCAT(year_month_prefix, LPAD(next_seq, 4, '0'));
    END IF;
END$$

-- =====================================================
-- 3. 档案文件统计更新触发器
-- =====================================================

-- 3.1 文件插入时更新档案统计
DROP TRIGGER IF EXISTS `tr_file_insert_update_archive`$$

CREATE TRIGGER `tr_file_insert_update_archive` 
    AFTER INSERT ON `arc_file`
    FOR EACH ROW
BEGIN
    -- 更新档案的文件数量和总大小
    UPDATE `arc_archive` 
    SET 
        `file_count` = (
            SELECT COUNT(*) 
            FROM `arc_file` 
            WHERE `archive_id` = NEW.archive_id AND `status` = 1
        ),
        `total_size` = (
            SELECT IFNULL(SUM(`file_size`), 0) 
            FROM `arc_file` 
            WHERE `archive_id` = NEW.archive_id AND `status` = 1
        ),
        `update_time` = NOW()
    WHERE `archive_id` = NEW.archive_id;
END$$

-- 3.2 文件更新时更新档案统计
DROP TRIGGER IF EXISTS `tr_file_update_update_archive`$$

CREATE TRIGGER `tr_file_update_update_archive` 
    AFTER UPDATE ON `arc_file`
    FOR EACH ROW
BEGIN
    -- 如果文件状态或大小发生变化，更新档案统计
    IF OLD.status != NEW.status OR OLD.file_size != NEW.file_size THEN
        UPDATE `arc_archive` 
        SET 
            `file_count` = (
                SELECT COUNT(*) 
                FROM `arc_file` 
                WHERE `archive_id` = NEW.archive_id AND `status` = 1
            ),
            `total_size` = (
                SELECT IFNULL(SUM(`file_size`), 0) 
                FROM `arc_file` 
                WHERE `archive_id` = NEW.archive_id AND `status` = 1
            ),
            `update_time` = NOW()
        WHERE `archive_id` = NEW.archive_id;
    END IF;
END$$

-- 3.3 文件删除时更新档案统计
DROP TRIGGER IF EXISTS `tr_file_delete_update_archive`$$

CREATE TRIGGER `tr_file_delete_update_archive` 
    AFTER DELETE ON `arc_file`
    FOR EACH ROW
BEGIN
    -- 更新档案的文件数量和总大小
    UPDATE `arc_archive` 
    SET 
        `file_count` = (
            SELECT COUNT(*) 
            FROM `arc_file` 
            WHERE `archive_id` = OLD.archive_id AND `status` = 1
        ),
        `total_size` = (
            SELECT IFNULL(SUM(`file_size`), 0) 
            FROM `arc_file` 
            WHERE `archive_id` = OLD.archive_id AND `status` = 1
        ),
        `update_time` = NOW()
    WHERE `archive_id` = OLD.archive_id;
END$$

-- =====================================================
-- 4. 借阅状态自动更新触发器
-- =====================================================

-- 4.1 借阅申请审批通过时自动设置借阅时间
DROP TRIGGER IF EXISTS `tr_borrow_approve_set_time`$$

CREATE TRIGGER `tr_borrow_approve_set_time` 
    BEFORE UPDATE ON `arc_borrow`
    FOR EACH ROW
BEGIN
    -- 如果状态从待审批变为已批准，设置借阅开始和结束时间
    IF OLD.status = 1 AND NEW.status = 2 THEN
        -- 设置借阅开始时间为当前时间
        IF NEW.borrow_start_time IS NULL THEN
            SET NEW.borrow_start_time = NOW();
        END IF;
        
        -- 设置借阅结束时间
        IF NEW.borrow_end_time IS NULL AND NEW.actual_days IS NOT NULL THEN
            SET NEW.borrow_end_time = DATE_ADD(NEW.borrow_start_time, INTERVAL NEW.actual_days DAY);
        END IF;
    END IF;
    
    -- 如果状态变为借阅中，更新档案状态
    IF OLD.status != 4 AND NEW.status = 4 THEN
        UPDATE `arc_archive` 
        SET `status` = 3, `update_time` = NOW() 
        WHERE `archive_id` = NEW.archive_id;
    END IF;
    
    -- 如果状态变为已归还，更新档案状态和归还时间
    IF OLD.status != 5 AND NEW.status = 5 THEN
        -- 设置归还时间
        IF NEW.return_time IS NULL THEN
            SET NEW.return_time = NOW();
        END IF;
        
        -- 更新档案状态为已归档
        UPDATE `arc_archive` 
        SET `status` = 2, `update_time` = NOW() 
        WHERE `archive_id` = NEW.archive_id;
    END IF;
END$$

-- =====================================================
-- 5. 档案状态变更触发器
-- =====================================================

-- 5.1 档案状态变更时记录归档时间
DROP TRIGGER IF EXISTS `tr_archive_status_change`$$

CREATE TRIGGER `tr_archive_status_change` 
    BEFORE UPDATE ON `arc_archive`
    FOR EACH ROW
BEGIN
    -- 如果状态从待审核变为已归档，设置归档时间
    IF OLD.status = 1 AND NEW.status = 2 THEN
        IF NEW.archive_time IS NULL THEN
            SET NEW.archive_time = NOW();
        END IF;
    END IF;
END$$

-- =====================================================
-- 6. 用户登录统计触发器
-- =====================================================

-- 6.1 登录成功时更新用户登录信息
DROP TRIGGER IF EXISTS `tr_login_success_update_user`$$

CREATE TRIGGER `tr_login_success_update_user` 
    AFTER INSERT ON `sys_login_log`
    FOR EACH ROW
BEGIN
    -- 如果登录成功，更新用户的登录信息
    IF NEW.status = 1 THEN
        UPDATE `sys_user` 
        SET 
            `last_login_time` = NEW.login_time,
            `last_login_ip` = NEW.client_ip,
            `login_count` = `login_count` + 1,
            `update_time` = NOW()
        WHERE `user_id` = NEW.user_id;
    END IF;
END$$

-- =====================================================
-- 7. 数据完整性检查触发器
-- =====================================================

-- 7.1 部门删除前检查是否有用户
DROP TRIGGER IF EXISTS `tr_department_delete_check`$$

CREATE TRIGGER `tr_department_delete_check` 
    BEFORE DELETE ON `sys_department`
    FOR EACH ROW
BEGIN
    DECLARE user_count INT DEFAULT 0;
    
    -- 检查是否有用户属于该部门
    SELECT COUNT(*) INTO user_count 
    FROM `sys_user` 
    WHERE `department_id` = OLD.department_id;
    
    -- 如果有用户，则不允许删除
    IF user_count > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = '该部门下还有用户，不能删除';
    END IF;
END$$

-- 7.2 角色删除前检查是否有用户关联
DROP TRIGGER IF EXISTS `tr_role_delete_check`$$

CREATE TRIGGER `tr_role_delete_check` 
    BEFORE DELETE ON `sys_role`
    FOR EACH ROW
BEGIN
    DECLARE user_count INT DEFAULT 0;
    
    -- 检查是否有用户关联该角色
    SELECT COUNT(*) INTO user_count 
    FROM `sys_user_role` 
    WHERE `role_id` = OLD.role_id;
    
    -- 如果有用户关联，则不允许删除
    IF user_count > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = '该角色下还有用户，不能删除';
    END IF;
END$$

-- 7.3 档案分类删除前检查是否有档案
DROP TRIGGER IF EXISTS `tr_category_delete_check`$$

CREATE TRIGGER `tr_category_delete_check` 
    BEFORE DELETE ON `arc_category`
    FOR EACH ROW
BEGIN
    DECLARE archive_count INT DEFAULT 0;
    DECLARE child_count INT DEFAULT 0;
    
    -- 检查是否有档案属于该分类
    SELECT COUNT(*) INTO archive_count 
    FROM `arc_archive` 
    WHERE `category_id` = OLD.category_id;
    
    -- 检查是否有子分类
    SELECT COUNT(*) INTO child_count 
    FROM `arc_category` 
    WHERE `parent_id` = OLD.category_id;
    
    -- 如果有档案或子分类，则不允许删除
    IF archive_count > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = '该分类下还有档案，不能删除';
    END IF;
    
    IF child_count > 0 THEN
        SIGNAL SQLSTATE '45000' 
        SET MESSAGE_TEXT = '该分类下还有子分类，不能删除';
    END IF;
END$$

-- =====================================================
-- 8. 审计日志触发器
-- =====================================================

-- 8.1 档案操作审计日志
DROP TRIGGER IF EXISTS `tr_archive_audit_insert`$$

CREATE TRIGGER `tr_archive_audit_insert` 
    AFTER INSERT ON `arc_archive`
    FOR EACH ROW
BEGIN
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        NEW.submit_user_id, 'CREATE', '创建档案', 
        'ARCHIVE', NEW.archive_id, 1, NOW()
    );
END$$

DROP TRIGGER IF EXISTS `tr_archive_audit_update`$$

CREATE TRIGGER `tr_archive_audit_update` 
    AFTER UPDATE ON `arc_archive`
    FOR EACH ROW
BEGIN
    DECLARE operation_desc VARCHAR(200) DEFAULT '更新档案';
    
    -- 根据状态变化生成不同的操作描述
    IF OLD.status != NEW.status THEN
        CASE NEW.status
            WHEN 2 THEN SET operation_desc = '档案审核通过';
            WHEN 3 THEN SET operation_desc = '档案被借出';
            WHEN 4 THEN SET operation_desc = '档案审核驳回';
            WHEN 5 THEN SET operation_desc = '档案已销毁';
            ELSE SET operation_desc = '更新档案状态';
        END CASE;
    END IF;
    
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        COALESCE(NEW.archive_user_id, NEW.submit_user_id), 'UPDATE', operation_desc, 
        'ARCHIVE', NEW.archive_id, 1, NOW()
    );
END$$

-- 8.2 借阅操作审计日志
DROP TRIGGER IF EXISTS `tr_borrow_audit_insert`$$

CREATE TRIGGER `tr_borrow_audit_insert` 
    AFTER INSERT ON `arc_borrow`
    FOR EACH ROW
BEGIN
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        NEW.apply_user_id, 'CREATE', '提交借阅申请', 
        'BORROW', NEW.borrow_id, 1, NOW()
    );
END$$

DROP TRIGGER IF EXISTS `tr_borrow_audit_update`$$

CREATE TRIGGER `tr_borrow_audit_update` 
    AFTER UPDATE ON `arc_borrow`
    FOR EACH ROW
BEGIN
    DECLARE operation_desc VARCHAR(200) DEFAULT '更新借阅申请';
    DECLARE audit_user_id BIGINT DEFAULT NEW.apply_user_id;
    
    -- 根据状态变化生成不同的操作描述
    IF OLD.status != NEW.status THEN
        CASE NEW.status
            WHEN 2 THEN 
                SET operation_desc = '借阅申请审批通过';
                SET audit_user_id = NEW.approve_user_id;
            WHEN 3 THEN 
                SET operation_desc = '借阅申请审批拒绝';
                SET audit_user_id = NEW.approve_user_id;
            WHEN 4 THEN SET operation_desc = '开始借阅档案';
            WHEN 5 THEN SET operation_desc = '归还借阅档案';
            WHEN 6 THEN SET operation_desc = '借阅档案逾期';
            ELSE SET operation_desc = '更新借阅状态';
        END CASE;
    END IF;
    
    INSERT INTO `sys_operation_log` (
        `user_id`, `operation_type`, `operation_desc`, 
        `target_type`, `target_id`, `status`, `create_time`
    ) VALUES (
        audit_user_id, 'UPDATE', operation_desc, 
        'BORROW', NEW.borrow_id, 1, NOW()
    );
END$$

-- 恢复分隔符
DELIMITER ;

-- =====================================================
-- 触发器脚本执行完成
-- =====================================================
SELECT 'Database triggers created successfully!' as message;