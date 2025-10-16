package com.archive.management.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * 审计日志工具类
 * 提供简化的审计日志记录功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Component
public class AuditLogUtil {

    private static final Logger logger = LoggerFactory.getLogger(AuditLogUtil.class);
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 记录审计日志
     * 
     * @param action 操作类型
     * @param description 操作描述
     * @param resourceId 资源ID
     */
    public static void log(String action, String description, Long resourceId) {
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            String logMessage = String.format("[AUDIT] %s | Action: %s | Description: %s | ResourceId: %s", 
                timestamp, action, description, resourceId);
            
            logger.info(logMessage);
            
            // 这里可以扩展为将审计日志保存到数据库
            // 目前使用日志记录，实际项目中可以集成审计日志服务
            log.info("审计日志: operation={}, userId={}, targetId={}, details={}", 
                    operation, userId, targetId, details);
            // 目前先使用日志记录，后续可以集成AuditLogService
            
        } catch (Exception e) {
            logger.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 记录审计日志（带用户信息）
     * 
     * @param action 操作类型
     * @param description 操作描述
     * @param resourceId 资源ID
     * @param userId 用户ID
     */
    public static void log(String action, String description, Long resourceId, Long userId) {
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            String logMessage = String.format("[AUDIT] %s | Action: %s | Description: %s | ResourceId: %s | UserId: %s", 
                timestamp, action, description, resourceId, userId);
            
            logger.info(logMessage);
            
        } catch (Exception e) {
            logger.error("记录审计日志失败: {}", e.getMessage(), e);
        }
    }

    /**
     * 记录错误审计日志
     * 
     * @param action 操作类型
     * @param description 操作描述
     * @param error 错误信息
     */
    public static void logError(String action, String description, String error) {
        try {
            String timestamp = LocalDateTime.now().format(formatter);
            String logMessage = String.format("[AUDIT_ERROR] %s | Action: %s | Description: %s | Error: %s", 
                timestamp, action, description, error);
            
            logger.error(logMessage);
            
        } catch (Exception e) {
            logger.error("记录错误审计日志失败: {}", e.getMessage(), e);
        }
    }
}