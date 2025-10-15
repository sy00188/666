package com.archive.management.listener;

import com.archive.management.constant.SecurityConstants;
import com.archive.management.event.PermissionChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 权限变更事件监听器
 * 处理权限变更后的缓存刷新、通知推送等操作
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PermissionChangedListener {

    private final CacheManager cacheManager;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * 处理权限变更事件
     */
    @Async
    @EventListener
    public void onPermissionChanged(PermissionChangedEvent event) {
        log.info("权限变更事件触发：permissionId={}, action={}, operator={}", 
                 event.getPermissionId(), event.getAction(), event.getOperatorId());
        
        // 1. 刷新缓存
        refreshPermissionCache(event);
        
        // 2. 推送WebSocket通知
        pushPermissionChangeNotification(event);
        
        // 3. 记录审计日志（可选，如果有审计服务）
        // auditService.log(event);
    }

    /**
     * 刷新权限缓存
     */
    private void refreshPermissionCache(PermissionChangedEvent event) {
        try {
            // 清除权限相关的所有缓存
            if (cacheManager.getCache(SecurityConstants.Cache.PERMISSION_PREFIX) != null) {
                cacheManager.getCache(SecurityConstants.Cache.PERMISSION_PREFIX).clear();
                log.info("权限缓存已清除");
            }
            
            if (cacheManager.getCache(SecurityConstants.Cache.PERMISSION_TREE) != null) {
                cacheManager.getCache(SecurityConstants.Cache.PERMISSION_TREE).clear();
                log.info("权限树缓存已清除");
            }
            
            // 如果是特定权限的变更，可以只清除该权限的缓存
            if (event.getPermissionId() != null && "UPDATE".equals(event.getAction())) {
                // 精准清除
                if (cacheManager.getCache(SecurityConstants.Cache.PERMISSION_PREFIX) != null) {
                    cacheManager.getCache(SecurityConstants.Cache.PERMISSION_PREFIX)
                        .evict(event.getPermissionId());
                }
            }
        } catch (Exception e) {
            log.error("刷新权限缓存失败", e);
        }
    }

    /**
     * 推送权限变更通知到前端
     */
    private void pushPermissionChangeNotification(PermissionChangedEvent event) {
        try {
            Map<String, Object> notification = new HashMap<>();
            notification.put("permissionId", event.getPermissionId());
            notification.put("action", event.getAction());
            notification.put("description", event.getDescription());
            notification.put("timestamp", System.currentTimeMillis());
            
            // 推送到订阅了权限变更主题的所有客户端
            messagingTemplate.convertAndSend("/topic/permission-changes", notification);
            
            log.info("权限变更通知已推送");
        } catch (Exception e) {
            log.error("推送权限变更通知失败", e);
        }
    }
}
