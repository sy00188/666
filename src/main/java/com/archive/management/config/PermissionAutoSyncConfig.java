package com.archive.management.config;

import com.archive.management.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 权限自动同步配置
 * 在应用启动完成后自动同步权限
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionAutoSyncConfig {

    private final PermissionService permissionService;

    /**
     * 应用启动完成后自动同步权限
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady() {
        log.info("应用启动完成，开始自动同步权限...");
        
        try {
            // 从所有来源同步权限（注解 + 配置文件）
            int syncCount = permissionService.syncPermissions("all", 1L); // 系统用户ID为1
            
            log.info("应用启动权限自动同步完成，共同步{}条权限", syncCount);
        } catch (Exception e) {
            log.error("应用启动权限自动同步失败，但不影响应用启动", e);
        }
    }
}
