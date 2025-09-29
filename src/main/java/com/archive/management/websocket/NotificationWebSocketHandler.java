package com.archive.management.websocket;

import com.archive.management.entity.Notification;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * WebSocket通知消息处理器
 * 处理实时通知推送相关的WebSocket消息
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class NotificationWebSocketHandler {

    private final SimpMessagingTemplate messagingTemplate;
    private final ObjectMapper objectMapper;
    
    // 存储用户会话信息
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

    /**
     * 处理用户连接消息
     * 
     * @param message 连接消息
     * @param headerAccessor 消息头访问器
     * @return 连接确认消息
     */
    @MessageMapping("/notification.connect")
    @SendTo("/topic/notifications")
    public Map<String, Object> handleConnect(@Payload Map<String, Object> message,
                                           SimpMessageHeaderAccessor headerAccessor) {
        try {
            String userId = (String) message.get("userId");
            String sessionId = headerAccessor.getSessionId();
            
            if (userId != null && sessionId != null) {
                userSessions.put(sessionId, userId);
                log.info("用户 {} 连接到通知WebSocket，会话ID: {}", userId, sessionId);
                
                return Map.of(
                    "type", "CONNECT_ACK",
                    "message", "连接成功",
                    "timestamp", LocalDateTime.now(),
                    "userId", userId
                );
            }
        } catch (Exception e) {
            log.error("处理WebSocket连接时发生错误", e);
        }
        
        return Map.of(
            "type", "CONNECT_ERROR",
            "message", "连接失败",
            "timestamp", LocalDateTime.now()
        );
    }

    /**
     * 处理用户断开连接
     * 
     * @param message 断开连接消息
     * @param headerAccessor 消息头访问器
     */
    @MessageMapping("/notification.disconnect")
    public void handleDisconnect(@Payload Map<String, Object> message,
                                SimpMessageHeaderAccessor headerAccessor) {
        String sessionId = headerAccessor.getSessionId();
        String userId = userSessions.remove(sessionId);
        
        if (userId != null) {
            log.info("用户 {} 断开通知WebSocket连接，会话ID: {}", userId, sessionId);
        }
    }

    /**
     * 处理通知已读标记
     * 
     * @param message 已读消息
     * @param principal 用户主体
     */
    @MessageMapping("/notification.markRead")
    public void handleMarkAsRead(@Payload Map<String, Object> message, Principal principal) {
        try {
            String userId = principal.getName();
            Object notificationIdObj = message.get("notificationId");
            
            if (notificationIdObj != null) {
                Long notificationId = Long.valueOf(notificationIdObj.toString());
                log.info("用户 {} 标记通知 {} 为已读", userId, notificationId);
                
                // 向用户发送确认消息
                messagingTemplate.convertAndSendToUser(
                    userId,
                    "/queue/notification-ack",
                    Map.of(
                        "type", "MARK_READ_ACK",
                        "notificationId", notificationId,
                        "timestamp", LocalDateTime.now()
                    )
                );
            }
        } catch (Exception e) {
            log.error("处理通知已读标记时发生错误", e);
        }
    }

    /**
     * 向指定用户发送通知
     * 
     * @param userId 用户ID
     * @param notification 通知对象
     */
    public void sendNotificationToUser(String userId, Notification notification) {
        try {
            Map<String, Object> message = Map.of(
                "type", "NEW_NOTIFICATION",
                "notification", convertNotificationToMap(notification),
                "timestamp", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSendToUser(userId, "/queue/notifications", message);
            log.info("向用户 {} 发送WebSocket通知: {}", userId, notification.getTitle());
            
        } catch (Exception e) {
            log.error("发送WebSocket通知给用户 {} 时发生错误", userId, e);
        }
    }

    /**
     * 向所有用户广播通知
     * 
     * @param notification 通知对象
     */
    public void broadcastNotification(Notification notification) {
        try {
            Map<String, Object> message = Map.of(
                "type", "BROADCAST_NOTIFICATION",
                "notification", convertNotificationToMap(notification),
                "timestamp", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSend("/topic/notifications", message);
            log.info("广播WebSocket通知: {}", notification.getTitle());
            
        } catch (Exception e) {
            log.error("广播WebSocket通知时发生错误", e);
        }
    }

    /**
     * 向指定用户发送通知数量更新
     * 
     * @param userId 用户ID
     * @param unreadCount 未读通知数量
     */
    public void sendUnreadCountUpdate(String userId, Long unreadCount) {
        try {
            Map<String, Object> message = Map.of(
                "type", "UNREAD_COUNT_UPDATE",
                "unreadCount", unreadCount,
                "timestamp", LocalDateTime.now()
            );
            
            messagingTemplate.convertAndSendToUser(userId, "/queue/notification-count", message);
            log.debug("向用户 {} 发送未读通知数量更新: {}", userId, unreadCount);
            
        } catch (Exception e) {
            log.error("发送未读通知数量更新给用户 {} 时发生错误", userId, e);
        }
    }

    /**
     * 将通知对象转换为Map
     * 
     * @param notification 通知对象
     * @return Map格式的通知数据
     */
    private Map<String, Object> convertNotificationToMap(Notification notification) {
        Map<String, Object> result = new java.util.HashMap<>();
        result.put("notificationId", notification.getNotificationId());
        result.put("title", notification.getTitle());
        result.put("content", notification.getContent());
        result.put("notificationType", notification.getNotificationType());
        result.put("notificationTypeDesc", notification.getNotificationTypeDesc());
        result.put("priority", notification.getPriority());
        result.put("priorityDesc", notification.getPriorityDesc());
        result.put("status", notification.getStatus());
        result.put("isRead", notification.isRead());
        result.put("createTime", notification.getCreateTime());
        result.put("expireTime", notification.getExpireTime());
        result.put("businessId", notification.getBusinessId() != null ? notification.getBusinessId() : "");
        result.put("businessType", notification.getBusinessType() != null ? notification.getBusinessType() : "");
        return result;
    }

    /**
     * 获取当前在线用户数量
     * 
     * @return 在线用户数量
     */
    public int getOnlineUserCount() {
        return userSessions.size();
    }

    /**
     * 检查用户是否在线
     * 
     * @param userId 用户ID
     * @return 是否在线
     */
    public boolean isUserOnline(String userId) {
        return userSessions.containsValue(userId);
    }
}