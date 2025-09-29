package com.archive.management.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket配置类
 * 用于实现实时通知推送功能
 * 
 * @author Archive Management System
 * @since 2024-01-01
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * 配置消息代理
     * 
     * @param config 消息代理注册器
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 启用简单消息代理，用于向客户端发送消息
        config.enableSimpleBroker("/topic", "/queue");
        
        // 设置应用程序目的地前缀，客户端发送消息时使用
        config.setApplicationDestinationPrefixes("/app");
        
        // 设置用户目的地前缀，用于点对点消息
        config.setUserDestinationPrefix("/user");
    }

    /**
     * 注册STOMP端点
     * 
     * @param registry STOMP端点注册器
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // 注册WebSocket端点，客户端通过此端点连接WebSocket服务
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*") // 允许跨域
                .withSockJS(); // 启用SockJS支持，提供WebSocket的降级选项
        
        // 注册纯WebSocket端点（不使用SockJS）
        registry.addEndpoint("/ws-native")
                .setAllowedOriginPatterns("*");
    }
}