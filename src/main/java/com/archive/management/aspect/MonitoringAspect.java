package com.archive.management.aspect;

import com.archive.management.service.MonitoringService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * 性能监控切面类
 * 自动监控各种业务操作的性能指标
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Aspect
@Component
public class MonitoringAspect {

    @Autowired
    private MonitoringService monitoringService;

    /**
     * 用户相关操作切点
     */
    @Pointcut("execution(* com.archive.management.service.UserService.createUser(..))")
    public void userCreationPointcut() {}

    @Pointcut("execution(* com.archive.management.service.UserService.updateUser(..))")
    public void userUpdatePointcut() {}

    @Pointcut("execution(* com.archive.management.service.UserService.deleteUser(..))")
    public void userDeletionPointcut() {}

    @Pointcut("execution(* com.archive.management.service.AuthService.login(..))")
    public void userLoginPointcut() {}

    @Pointcut("execution(* com.archive.management.service.AuthService.logout(..))")
    public void userLogoutPointcut() {}

    /**
     * 档案相关操作切点
     */
    @Pointcut("execution(* com.archive.management.service.ArchiveService.createArchive(..))")
    public void archiveCreationPointcut() {}

    @Pointcut("execution(* com.archive.management.service.ArchiveService.updateArchive(..))")
    public void archiveUpdatePointcut() {}

    @Pointcut("execution(* com.archive.management.service.ArchiveService.deleteArchive(..))")
    public void archiveDeletionPointcut() {}

    @Pointcut("execution(* com.archive.management.service.ArchiveService.searchArchives(..))")
    public void archiveSearchPointcut() {}

    @Pointcut("execution(* com.archive.management.service.ArchiveService.downloadArchive(..))")
    public void archiveDownloadPointcut() {}

    /**
     * 批量操作切点
     */
    @Pointcut("execution(* com.archive.management.service.ArchiveService.batchUpdateArchiveStatus(..))")
    public void batchUpdatePointcut() {}

    @Pointcut("execution(* com.archive.management.service.ArchiveService.batchDeleteArchives(..))")
    public void batchDeletePointcut() {}

    /**
     * 用户创建监控
     */
    @Around("userCreationPointcut()")
    public Object monitorUserCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordUserRegistration();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("user_creation", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 用户登录监控
     */
    @Around("userLoginPointcut()")
    public Object monitorUserLogin(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordUserLogin();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("user_login", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 用户登出监控
     */
    @Around("userLogoutPointcut()")
    public Object monitorUserLogout(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordUserLogout();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("user_logout", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 档案创建监控
     */
    @Around("archiveCreationPointcut()")
    public Object monitorArchiveCreation(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordDocumentCreated();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("archive_creation", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 档案更新监控
     */
    @Around("archiveUpdatePointcut()")
    public Object monitorArchiveUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordDocumentUpdated();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("archive_update", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 档案删除监控
     */
    @Around("archiveDeletionPointcut()")
    public Object monitorArchiveDeletion(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordDocumentDeleted();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("archive_deletion", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 档案搜索监控
     */
    @Around("archiveSearchPointcut()")
    public Object monitorArchiveSearch(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordDocumentSearched();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("archive_search", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 档案下载监控
     */
    @Around("archiveDownloadPointcut()")
    public Object monitorArchiveDownload(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordDocumentDownloaded();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("archive_download", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 批量更新监控
     */
    @Around("batchUpdatePointcut()")
    public Object monitorBatchUpdate(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordBatchOperation();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("batch_update", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 批量删除监控
     */
    @Around("batchDeletePointcut()")
    public Object monitorBatchDelete(ProceedingJoinPoint joinPoint) throws Throwable {
        monitoringService.incrementConcurrentOperations();
        try {
            Object result = joinPoint.proceed();
            monitoringService.recordBatchOperation();
            return result;
        } catch (Exception e) {
            monitoringService.recordError("batch_delete", e.getMessage());
            throw e;
        } finally {
            monitoringService.decrementConcurrentOperations();
        }
    }

    /**
     * 通用错误监控
     */
    @Around("execution(* com.archive.management.service.*.*(..))")
    public Object monitorServiceErrors(ProceedingJoinPoint joinPoint) throws Throwable {
        try {
            return joinPoint.proceed();
        } catch (Exception e) {
            String methodName = joinPoint.getSignature().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            monitoringService.recordError("service_error", className + "." + methodName + ": " + e.getMessage());
            throw e;
        }
    }
}
