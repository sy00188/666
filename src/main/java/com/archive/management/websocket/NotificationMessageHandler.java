package com.archive.management.websocket;

import com.archive.management.entity.Notification;
import com.archive.management.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.util.Map;

/**
 * WebSocket消息处理器
 * 处理客户端发送的WebSocket消息
 * 
 * @author archive-management-system
 * @since 2024-01-01
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationMessageHandler {

    private final NotificationService notificationService;
    private final NotificationWebSocketHandler webSocketHandler;
    private final ObjectMapper objectMapper;

    /**
     * 处理标记通知为已读的消息
     * 
     * @param payload 消息载荷，包含notificationId
     * @param headerAccessor 消息头访问器
     * @param principal 用户主体
     * @return 处理结果
     */
    @MessageMapping("/notification/markRead")
    @SendToUser("/queue/notification/response")
    public Map<String, Object> markNotificationAsRead(
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            Long notificationId = Long.valueOf(payload.get("notificationId").toString());
            Long userId = Long.valueOf(principal.getName());
            
            log.info("用户 {} 请求标记通知 {} 为已读", userId, notificationId);
            
            // 标记通知为已读
            boolean success = notificationService.markAsRead(notificationId, userId);
            
            if (success) {
                // 更新用户的未读通知数量
                webSocketHandler.updateUnreadCount(userId);
                
                return Map.of(
                    "success", true,
                    "message", "通知已标记为已读",
                    "notificationId", notificationId
                );
            } else {
                return Map.of(
                    "success", false,
                    "message", "标记失败，通知不存在或无权限",
                    "notificationId", notificationId
                );
            }
            
        } catch (Exception e) {
            log.error("标记通知为已读时发生错误", e);
            return Map.of(
                "success", false,
                "message", "处理失败：" + e.getMessage()
            );
        }
    }

    /**
     * 处理批量标记通知为已读的消息
     * 
     * @param payload 消息载荷，包含notificationIds数组
     * @param headerAccessor 消息头访问器
     * @param principal 用户主体
     * @return 处理结果
     */
    @MessageMapping("/notification/markBatchRead")
    @SendToUser("/queue/notification/response")
    public Map<String, Object> markBatchNotificationsAsRead(
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            @SuppressWarnings("unchecked")
            java.util.List<Long> notificationIds = (java.util.List<Long>) payload.get("notificationIds");
            Long userId = Long.valueOf(principal.getName());
            
            log.info("用户 {} 请求批量标记 {} 个通知为已读", userId, notificationIds.size());
            
            // 批量标记通知为已读
            int successCount = notificationService.markBatchAsRead(notificationIds, userId);
            
            // 更新用户的未读通知数量
            webSocketHandler.updateUnreadCount(userId);
            
            return Map.of(
                "success", true,
                "message", String.format("成功标记 %d 个通知为已读", successCount),
                "successCount", successCount,
                "totalCount", notificationIds.size()
            );
            
        } catch (Exception e) {
            log.error("批量标记通知为已读时发生错误", e);
            return Map.of(
                "success", false,
                "message", "处理失败：" + e.getMessage()
            );
        }
    }

    /**
     * 处理标记所有通知为已读的消息
     * 
     * @param payload 消息载荷
     * @param headerAccessor 消息头访问器
     * @param principal 用户主体
     * @return 处理结果
     */
    @MessageMapping("/notification/markAllRead")
    @SendToUser("/queue/notification/response")
    public Map<String, Object> markAllNotificationsAsRead(
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            Long userId = Long.valueOf(principal.getName());
            
            log.info("用户 {} 请求标记所有通知为已读", userId);
            
            // 标记所有通知为已读
            int successCount = notificationService.markAllAsRead(userId);
            
            // 更新用户的未读通知数量
            webSocketHandler.updateUnreadCount(userId);
            
            return Map.of(
                "success", true,
                "message", String.format("成功标记 %d 个通知为已读", successCount),
                "successCount", successCount
            );
            
        } catch (Exception e) {
            log.error("标记所有通知为已读时发生错误", e);
            return Map.of(
                "success", false,
                "message", "处理失败：" + e.getMessage()
            );
        }
    }

    /**
     * 处理获取未读通知数量的消息
     * 
     * @param payload 消息载荷
     * @param headerAccessor 消息头访问器
     * @param principal 用户主体
     * @return 未读通知数量
     */
    @MessageMapping("/notification/getUnreadCount")
    @SendToUser("/queue/notification/response")
    public Map<String, Object> getUnreadNotificationCount(
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            Long userId = Long.valueOf(principal.getName());
            
            log.debug("用户 {} 请求获取未读通知数量", userId);
            
            // 获取未读通知数量
            long unreadCount = notificationService.getUnreadCount(userId);
            
            return Map.of(
                "success", true,
                "unreadCount", unreadCount,
                "message", "获取成功"
            );
            
        } catch (Exception e) {
            log.error("获取未读通知数量时发生错误", e);
            return Map.of(
                "success", false,
                "message", "获取失败：" + e.getMessage(),
                "unreadCount", 0
            );
        }
    }

    /**
     * 处理用户连接事件
     * 
     * @param payload 消息载荷
     * @param headerAccessor 消息头访问器
     * @param principal 用户主体
     * @return 连接结果
     */
    @MessageMapping("/notification/connect")
    @SendToUser("/queue/notification/response")
    public Map<String, Object> handleUserConnect(
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            Long userId = Long.valueOf(principal.getName());
            String sessionId = headerAccessor.getSessionId();
            
            log.info("用户 {} 建立WebSocket连接，会话ID: {}", userId, sessionId);
            
            // 注册用户连接
            webSocketHandler.addUserSession(userId, sessionId);
            
            // 获取未读通知数量
            long unreadCount = notificationService.getUnreadCount(userId);
            
            return Map.of(
                "success", true,
                "message", "连接成功",
                "unreadCount", unreadCount,
                "sessionId", sessionId
            );
            
        } catch (Exception e) {
            log.error("处理用户连接时发生错误", e);
            return Map.of(
                "success", false,
                "message", "连接失败：" + e.getMessage()
            );
        }
    }

    /**
     * 处理用户断开连接事件
     * 
     * @param payload 消息载荷
     * @param headerAccessor 消息头访问器
     * @param principal 用户主体
     * @return 断开连接结果
     */
    @MessageMapping("/notification/disconnect")
    @SendToUser("/queue/notification/response")
    public Map<String, Object> handleUserDisconnect(
            @Payload Map<String, Object> payload,
            SimpMessageHeaderAccessor headerAccessor,
            Principal principal) {
        
        try {
            Long userId = Long.valueOf(principal.getName());
            String sessionId = headerAccessor.getSessionId();
            
            log.info("用户 {} 断开WebSocket连接，会话ID: {}", userId, sessionId);
            
            // 移除用户连接
            webSocketHandler.removeUserSession(userId, sessionId);
            
            return Map.of(
                "success", true,
                "message", "断开连接成功",
                "sessionId", sessionId
            );
            
        } catch (Exception e) {
            log.error("处理用户断开连接时发生错误", e);
            return Map.of(
                "success", false,
                "message", "断开连接失败：" + e.getMessage()
            );
        }
    }
}