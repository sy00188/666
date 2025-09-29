package com.archive.management.scheduler;

import com.archive.management.entity.User;
import com.archive.management.service.UserService;
import com.archive.management.service.AuditLogService;
import com.archive.management.service.SystemConfigService;
import com.archive.management.mq.producer.UserMessageProducer;
import com.archive.management.mq.producer.SystemMessageProducer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 用户定时任务调度器
 * 负责处理用户相关的定时任务，包括会话清理、密码过期检查、账户管理等
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UserScheduler {

    private final UserService userService;
    private final AuditLogService auditLogService;
    private final SystemConfigService systemConfigService;
    private final UserMessageProducer userMessageProducer;
    private final SystemMessageProducer systemMessageProducer;

    /**
     * 清理过期用户会话
     * 每30分钟执行一次，清理过期的用户会话
     */
    @Scheduled(fixedRate = 1800000) // 30分钟
    @Transactional
    public void cleanupExpiredSessionsTask() {
        log.debug("开始执行过期会话清理任务");
        
        try {
            // 获取会话超时配置
            Integer sessionTimeoutMinutes = systemConfigService.getIntValue("user.session.timeout.minutes", 120);
            Boolean sessionCleanupEnabled = systemConfigService.getBooleanValue("user.session.cleanup.enabled", true);
            
            if (!sessionCleanupEnabled) {
                log.debug("用户会话清理功能已禁用，跳过任务");
                return;
            }
            
            LocalDateTime expireTime = LocalDateTime.now().minusMinutes(sessionTimeoutMinutes);
            
            // 查找过期会话
            List<String> expiredSessions = userService.findExpiredSessions(expireTime);
            
            if (!expiredSessions.isEmpty()) {
                // 批量清理过期会话
                int cleanedCount = userService.cleanupExpiredSessions(expiredSessions);
                
                log.info("清理了 {} 个过期用户会话", cleanedCount);
                
                // 记录审计日志
                auditLogService.recordOperationLog("SYSTEM", "SESSION_CLEANUP", 
                    "UserSession", "BATCH", "SUCCESS", 
                    String.format("清理了 %d 个过期会话", cleanedCount));
                
                // 发送清理完成通知
                systemMessageProducer.sendSystemMessage("SESSION_CLEANUP_COMPLETE", 
                    Map.of("cleanedCount", cleanedCount, 
                           "expireTime", expireTime.toString()));
            }
            
        } catch (Exception e) {
            log.error("过期会话清理任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "SESSION_CLEANUP", 
                "UserSession", "BATCH", "FAILURE", 
                "会话清理失败: " + e.getMessage());
        }
    }

    /**
     * 密码过期检查任务
     * 每天凌晨6点执行，检查即将过期和已过期的密码
     */
    @Scheduled(cron = "0 0 6 * * ?")
    @Transactional
    public void passwordExpirationCheckTask() {
        log.info("开始执行密码过期检查任务");
        
        try {
            // 获取密码策略配置
            Integer passwordExpirationDays = systemConfigService.getIntValue("password.expiration.days", 90);
            Integer passwordWarningDays = systemConfigService.getIntValue("password.warning.days", 7);
            Boolean passwordExpirationEnabled = systemConfigService.getBooleanValue("password.expiration.enabled", true);
            
            if (!passwordExpirationEnabled) {
                log.info("密码过期检查功能已禁用，跳过任务");
                return;
            }
            
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime expireDate = now.minusDays(passwordExpirationDays);
            LocalDateTime warningDate = now.minusDays(passwordExpirationDays - passwordWarningDays);
            
            // 查找已过期密码的用户
            List<User> expiredPasswordUsers = userService.findUsersWithExpiredPasswords(expireDate);
            
            // 查找即将过期密码的用户
            List<User> warningPasswordUsers = userService.findUsersWithPasswordsNearExpiration(warningDate, expireDate);
            
            // 处理已过期密码的用户
            for (User user : expiredPasswordUsers) {
                try {
                    // 标记密码已过期
                    userService.markPasswordExpired(user.getId());
                    
                    // 发送密码过期通知
                    userMessageProducer.sendPasswordExpirationMessage(user.getId(), "EXPIRED");
                    
                    log.info("用户 {} 的密码已过期", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("处理用户 {} 密码过期失败: {}", user.getUsername(), e.getMessage(), e);
                }
            }
            
            // 处理即将过期密码的用户
            for (User user : warningPasswordUsers) {
                try {
                    // 发送密码即将过期警告
                    userMessageProducer.sendPasswordExpirationMessage(user.getId(), "WARNING");
                    
                    log.debug("已向用户 {} 发送密码即将过期警告", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("向用户 {} 发送密码过期警告失败: {}", user.getUsername(), e.getMessage(), e);
                }
            }
            
            // 记录检查结果
            auditLogService.recordOperationLog("SYSTEM", "PASSWORD_EXPIRATION_CHECK", 
                "User", "BATCH", "SUCCESS", 
                String.format("检查完成，过期密码用户: %d, 即将过期用户: %d", 
                    expiredPasswordUsers.size(), warningPasswordUsers.size()));
            
            log.info("密码过期检查任务完成，过期用户: {}, 警告用户: {}", 
                expiredPasswordUsers.size(), warningPasswordUsers.size());
            
        } catch (Exception e) {
            log.error("密码过期检查任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "PASSWORD_EXPIRATION_CHECK", 
                "User", "BATCH", "FAILURE", 
                "密码过期检查失败: " + e.getMessage());
        }
    }

    /**
     * 账户锁定检查任务
     * 每小时执行一次，检查需要解锁的账户
     */
    @Scheduled(fixedRate = 3600000) // 1小时
    @Transactional
    public void accountLockCheckTask() {
        log.debug("开始执行账户锁定检查任务");
        
        try {
            // 获取账户锁定配置
            Integer lockDurationMinutes = systemConfigService.getIntValue("account.lock.duration.minutes", 30);
            Boolean autoUnlockEnabled = systemConfigService.getBooleanValue("account.auto.unlock.enabled", true);
            
            if (!autoUnlockEnabled) {
                log.debug("账户自动解锁功能已禁用，跳过任务");
                return;
            }
            
            LocalDateTime unlockTime = LocalDateTime.now().minusMinutes(lockDurationMinutes);
            
            // 查找需要解锁的账户
            List<User> lockedUsers = userService.findUsersToUnlock(unlockTime);
            
            int unlockedCount = 0;
            
            for (User user : lockedUsers) {
                try {
                    // 解锁账户
                    userService.unlockAccount(user.getId());
                    
                    // 发送账户解锁通知
                    userMessageProducer.sendAccountStatusMessage(user.getId(), "UNLOCKED", 
                        "账户已自动解锁");
                    
                    unlockedCount++;
                    log.info("用户 {} 账户已自动解锁", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("解锁用户 {} 账户失败: {}", user.getUsername(), e.getMessage(), e);
                }
            }
            
            if (unlockedCount > 0) {
                // 记录解锁结果
                auditLogService.recordOperationLog("SYSTEM", "ACCOUNT_AUTO_UNLOCK", 
                    "User", "BATCH", "SUCCESS", 
                    String.format("自动解锁了 %d 个账户", unlockedCount));
                
                log.info("账户锁定检查任务完成，解锁账户数: {}", unlockedCount);
            }
            
        } catch (Exception e) {
            log.error("账户锁定检查任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "ACCOUNT_AUTO_UNLOCK", 
                "User", "BATCH", "FAILURE", 
                "账户解锁检查失败: " + e.getMessage());
        }
    }

    /**
     * 用户活动统计任务
     * 每天凌晨5点执行，统计用户活动数据
     */
    @Scheduled(cron = "0 0 5 * * ?")
    @Transactional
    public void userActivityStatisticsTask() {
        log.info("开始执行用户活动统计任务");
        
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime startOfDay = yesterday.withHour(0).withMinute(0).withSecond(0).withNano(0);
            LocalDateTime endOfDay = yesterday.withHour(23).withMinute(59).withSecond(59).withNano(999999999);
            
            // 统计昨日活跃用户
            long activeUsersCount = userService.countActiveUsers(startOfDay, endOfDay);
            
            // 统计昨日新注册用户
            long newUsersCount = userService.countNewUsers(startOfDay, endOfDay);
            
            // 统计昨日登录次数
            long loginCount = userService.countLogins(startOfDay, endOfDay);
            
            // 统计用户操作次数
            Map<String, Long> operationCounts = userService.countUserOperations(startOfDay, endOfDay);
            
            // 生成统计报告
            Map<String, Object> statisticsReport = Map.of(
                "date", yesterday.toLocalDate().toString(),
                "activeUsers", activeUsersCount,
                "newUsers", newUsersCount,
                "loginCount", loginCount,
                "operations", operationCounts,
                "timestamp", LocalDateTime.now().toString()
            );
            
            // 保存统计数据
            userService.saveUserActivityStatistics(statisticsReport);
            
            // 发送统计报告
            systemMessageProducer.sendSystemMessage("USER_ACTIVITY_STATISTICS", statisticsReport);
            
            log.info("用户活动统计任务完成，活跃用户: {}, 新用户: {}, 登录次数: {}", 
                activeUsersCount, newUsersCount, loginCount);
            
        } catch (Exception e) {
            log.error("用户活动统计任务执行失败", e);
            systemMessageProducer.sendSystemMessage("USER_ACTIVITY_STATISTICS_ERROR", 
                Map.of("error", e.getMessage()));
        }
    }

    /**
     * 清理无效用户数据任务
     * 每月第一天凌晨执行，清理无效的用户数据
     */
    @Scheduled(cron = "0 0 0 1 * ?")
    @Transactional
    public void cleanupInvalidUserDataTask() {
        log.info("开始执行无效用户数据清理任务");
        
        try {
            // 获取清理配置
            Integer inactiveUserDays = systemConfigService.getIntValue("user.inactive.cleanup.days", 365);
            Boolean userDataCleanupEnabled = systemConfigService.getBooleanValue("user.data.cleanup.enabled", false);
            
            if (!userDataCleanupEnabled) {
                log.info("用户数据清理功能已禁用，跳过任务");
                return;
            }
            
            LocalDateTime inactiveDate = LocalDateTime.now().minusDays(inactiveUserDays);
            
            // 查找长期未活跃的用户
            List<User> inactiveUsers = userService.findInactiveUsers(inactiveDate);
            
            // 查找未验证邮箱的过期注册用户
            Integer unverifiedUserDays = systemConfigService.getIntValue("user.unverified.cleanup.days", 7);
            LocalDateTime unverifiedDate = LocalDateTime.now().minusDays(unverifiedUserDays);
            List<User> unverifiedUsers = userService.findUnverifiedUsers(unverifiedDate);
            
            int inactiveCleanedCount = 0;
            int unverifiedCleanedCount = 0;
            
            // 清理长期未活跃用户的敏感数据
            for (User user : inactiveUsers) {
                try {
                    userService.anonymizeInactiveUser(user.getId());
                    inactiveCleanedCount++;
                    
                    log.debug("已匿名化长期未活跃用户: {}", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("匿名化用户 {} 失败: {}", user.getUsername(), e.getMessage(), e);
                }
            }
            
            // 删除未验证邮箱的过期注册用户
            for (User user : unverifiedUsers) {
                try {
                    userService.deleteUnverifiedUser(user.getId());
                    unverifiedCleanedCount++;
                    
                    log.debug("已删除未验证用户: {}", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("删除未验证用户 {} 失败: {}", user.getUsername(), e.getMessage(), e);
                }
            }
            
            // 记录清理结果
            auditLogService.recordOperationLog("SYSTEM", "USER_DATA_CLEANUP", 
                "User", "BATCH", "SUCCESS", 
                String.format("匿名化长期未活跃用户: %d, 删除未验证用户: %d", 
                    inactiveCleanedCount, unverifiedCleanedCount));
            
            log.info("无效用户数据清理任务完成，匿名化用户: {}, 删除用户: {}", 
                inactiveCleanedCount, unverifiedCleanedCount);
            
        } catch (Exception e) {
            log.error("无效用户数据清理任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "USER_DATA_CLEANUP", 
                "User", "BATCH", "FAILURE", 
                "用户数据清理失败: " + e.getMessage());
        }
    }

    /**
     * 用户权限同步任务
     * 每天凌晨7点执行，同步用户权限变更
     */
    @Scheduled(cron = "0 0 7 * * ?")
    @Transactional
    public void userPermissionSyncTask() {
        log.info("开始执行用户权限同步任务");
        
        try {
            // 获取需要同步权限的用户
            List<User> usersToSync = userService.findUsersWithPendingPermissionSync();
            
            int syncedCount = 0;
            
            for (User user : usersToSync) {
                try {
                    // 同步用户权限
                    userService.syncUserPermissions(user.getId());
                    
                    // 发送权限同步通知
                    userMessageProducer.sendPermissionSyncMessage(user.getId(), "SYNCED");
                    
                    syncedCount++;
                    log.debug("用户 {} 权限同步完成", user.getUsername());
                    
                } catch (Exception e) {
                    log.error("同步用户 {} 权限失败: {}", user.getUsername(), e.getMessage(), e);
                }
            }
            
            if (syncedCount > 0) {
                // 记录同步结果
                auditLogService.recordOperationLog("SYSTEM", "USER_PERMISSION_SYNC", 
                    "User", "BATCH", "SUCCESS", 
                    String.format("同步了 %d 个用户的权限", syncedCount));
                
                log.info("用户权限同步任务完成，同步用户数: {}", syncedCount);
            }
            
        } catch (Exception e) {
            log.error("用户权限同步任务执行失败", e);
            auditLogService.recordOperationLog("SYSTEM", "USER_PERMISSION_SYNC", 
                "User", "BATCH", "FAILURE", 
                "用户权限同步失败: " + e.getMessage());
        }
    }
}