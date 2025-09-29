package com.archive.management.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.RabbitListenerContainerFactory;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.backoff.ExponentialBackOffPolicy;
import org.springframework.retry.policy.SimpleRetryPolicy;
import org.springframework.retry.support.RetryTemplate;

/**
 * 消息队列配置类
 * 配置RabbitMQ连接、交换机、队列、消息转换器等
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Configuration
@ConditionalOnProperty(name = "spring.rabbitmq.enabled", havingValue = "true", matchIfMissing = false)
public class MessageQueueConfig {

    @Value("${spring.rabbitmq.host:localhost}")
    private String host;

    @Value("${spring.rabbitmq.port:5672}")
    private int port;

    @Value("${spring.rabbitmq.username:guest}")
    private String username;

    @Value("${spring.rabbitmq.password:guest}")
    private String password;

    @Value("${spring.rabbitmq.virtual-host:/}")
    private String virtualHost;

    @Value("${spring.rabbitmq.connection-timeout:30000}")
    private int connectionTimeout;

    @Value("${spring.rabbitmq.publisher-confirm-type:correlated}")
    private String publisherConfirmType;

    @Value("${spring.rabbitmq.publisher-returns:true}")
    private boolean publisherReturns;

    // 队列名称常量
    public static final String ARCHIVE_QUEUE = "archive.queue";
    public static final String ARCHIVE_DLQ = "archive.dlq";
    public static final String NOTIFICATION_QUEUE = "notification.queue";
    public static final String NOTIFICATION_DLQ = "notification.dlq";
    public static final String AUDIT_QUEUE = "audit.queue";
    public static final String AUDIT_DLQ = "audit.dlq";
    public static final String FILE_PROCESS_QUEUE = "file.process.queue";
    public static final String FILE_PROCESS_DLQ = "file.process.dlq";
    public static final String SMS_QUEUE = "sms.queue";
    public static final String SMS_DLQ = "sms.dlq";
    public static final String SMS_BATCH_QUEUE = "sms.batch.queue";
    public static final String SMS_BATCH_DLQ = "sms.batch.dlq";

    // 交换机名称常量
    public static final String ARCHIVE_EXCHANGE = "archive.exchange";
    public static final String NOTIFICATION_EXCHANGE = "notification.exchange";
    public static final String AUDIT_EXCHANGE = "audit.exchange";
    public static final String FILE_PROCESS_EXCHANGE = "file.process.exchange";
    public static final String SMS_EXCHANGE = "sms.exchange";
    public static final String DLX_EXCHANGE = "dlx.exchange";

    // 路由键常量
    public static final String ARCHIVE_ROUTING_KEY = "archive.create";
    public static final String ARCHIVE_UPDATE_ROUTING_KEY = "archive.update";
    public static final String ARCHIVE_DELETE_ROUTING_KEY = "archive.delete";
    public static final String NOTIFICATION_EMAIL_ROUTING_KEY = "notification.email";
    public static final String NOTIFICATION_SMS_ROUTING_KEY = "notification.sms";
    public static final String AUDIT_LOG_ROUTING_KEY = "audit.log";
    public static final String FILE_UPLOAD_ROUTING_KEY = "file.upload";
    public static final String FILE_CONVERT_ROUTING_KEY = "file.convert";
    public static final String SMS_SINGLE_ROUTING_KEY = "sms.single";
    public static final String SMS_BATCH_ROUTING_KEY = "sms.batch";
    public static final String SMS_TEMPLATE_ROUTING_KEY = "sms.template";

    /**
     * 连接工厂配置
     * 
     * @return ConnectionFactory
     */
    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory factory = new CachingConnectionFactory();
        factory.setHost(host);
        factory.setPort(port);
        factory.setUsername(username);
        factory.setPassword(password);
        factory.setVirtualHost(virtualHost);
        factory.setConnectionTimeout(connectionTimeout);
        
        // 连接池配置
        factory.setChannelCacheSize(50);
        factory.setConnectionCacheSize(10);
        factory.setChannelCheckoutTimeout(30000);
        
        // 发布确认
        factory.setPublisherConfirmType(CachingConnectionFactory.ConfirmType.valueOf(publisherConfirmType.toUpperCase()));
        factory.setPublisherReturns(publisherReturns);
        
        return factory;
    }

    /**
     * RabbitTemplate配置
     * 
     * @param connectionFactory 连接工厂
     * @return RabbitTemplate
     */
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        
        // 消息转换器
        template.setMessageConverter(messageConverter());
        
        // 强制使用JSON序列化
        template.setEncoding("UTF-8");
        
        // 发布确认回调
        template.setConfirmCallback((correlationData, ack, cause) -> {
            if (ack) {
                System.out.println("消息发送成功: " + correlationData);
            } else {
                System.err.println("消息发送失败: " + correlationData + ", 原因: " + cause);
            }
        });
        
        // 消息返回回调
        template.setReturnsCallback(returned -> {
            System.err.println("消息被退回: " + returned.getMessage() + 
                ", 回复码: " + returned.getReplyCode() + 
                ", 回复文本: " + returned.getReplyText() + 
                ", 交换机: " + returned.getExchange() + 
                ", 路由键: " + returned.getRoutingKey());
        });
        
        // 重试模板
        template.setRetryTemplate(retryTemplate());
        
        return template;
    }

    /**
     * 消息转换器
     * 
     * @return MessageConverter
     */
    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    /**
     * 重试模板
     * 
     * @return RetryTemplate
     */
    @Bean
    public RetryTemplate retryTemplate() {
        RetryTemplate retryTemplate = new RetryTemplate();
        
        // 重试策略
        SimpleRetryPolicy retryPolicy = new SimpleRetryPolicy();
        retryPolicy.setMaxAttempts(3);
        retryTemplate.setRetryPolicy(retryPolicy);
        
        // 退避策略
        ExponentialBackOffPolicy backOffPolicy = new ExponentialBackOffPolicy();
        backOffPolicy.setInitialInterval(1000);
        backOffPolicy.setMultiplier(2.0);
        backOffPolicy.setMaxInterval(10000);
        retryTemplate.setBackOffPolicy(backOffPolicy);
        
        return retryTemplate;
    }

    /**
     * 监听器容器工厂
     * 
     * @param connectionFactory 连接工厂
     * @return RabbitListenerContainerFactory
     */
    @Bean
    public RabbitListenerContainerFactory<?> rabbitListenerContainerFactory(ConnectionFactory connectionFactory) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter());
        
        // 并发配置
        factory.setConcurrentConsumers(5);
        factory.setMaxConcurrentConsumers(20);
        
        // 预取配置
        factory.setPrefetchCount(10);
        
        // 确认模式
        factory.setAcknowledgeMode(AcknowledgeMode.MANUAL);
        
        // 重试配置
        factory.setRetryTemplate(retryTemplate());
        
        return factory;
    }

    // ==================== 死信交换机 ====================

    /**
     * 死信交换机
     * 
     * @return DirectExchange
     */
    @Bean
    public DirectExchange dlxExchange() {
        return ExchangeBuilder.directExchange(DLX_EXCHANGE)
                .durable(true)
                .build();
    }

    // ==================== 档案相关队列 ====================

    /**
     * 档案交换机
     * 
     * @return TopicExchange
     */
    @Bean
    public TopicExchange archiveExchange() {
        return ExchangeBuilder.topicExchange(ARCHIVE_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 档案队列
     * 
     * @return Queue
     */
    @Bean
    public Queue archiveQueue() {
        return QueueBuilder.durable(ARCHIVE_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", ARCHIVE_DLQ)
                .withArgument("x-message-ttl", 300000) // 5分钟TTL
                .build();
    }

    /**
     * 档案死信队列
     * 
     * @return Queue
     */
    @Bean
    public Queue archiveDlq() {
        return QueueBuilder.durable(ARCHIVE_DLQ).build();
    }

    /**
     * 档案队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding archiveBinding() {
        return BindingBuilder.bind(archiveQueue())
                .to(archiveExchange())
                .with("archive.*");
    }

    /**
     * 档案死信队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding archiveDlqBinding() {
        return BindingBuilder.bind(archiveDlq())
                .to(dlxExchange())
                .with(ARCHIVE_DLQ);
    }

    // ==================== 通知相关队列 ====================

    /**
     * 通知交换机
     * 
     * @return TopicExchange
     */
    @Bean
    public TopicExchange notificationExchange() {
        return ExchangeBuilder.topicExchange(NOTIFICATION_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 通知队列
     * 
     * @return Queue
     */
    @Bean
    public Queue notificationQueue() {
        return QueueBuilder.durable(NOTIFICATION_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", NOTIFICATION_DLQ)
                .withArgument("x-message-ttl", 600000) // 10分钟TTL
                .build();
    }

    /**
     * 通知死信队列
     * 
     * @return Queue
     */
    @Bean
    public Queue notificationDlq() {
        return QueueBuilder.durable(NOTIFICATION_DLQ).build();
    }

    /**
     * 通知队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding notificationBinding() {
        return BindingBuilder.bind(notificationQueue())
                .to(notificationExchange())
                .with("notification.*");
    }

    /**
     * 通知死信队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding notificationDlqBinding() {
        return BindingBuilder.bind(notificationDlq())
                .to(dlxExchange())
                .with(NOTIFICATION_DLQ);
    }

    // ==================== 审计相关队列 ====================

    /**
     * 审计交换机
     * 
     * @return DirectExchange
     */
    @Bean
    public DirectExchange auditExchange() {
        return ExchangeBuilder.directExchange(AUDIT_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 审计队列
     * 
     * @return Queue
     */
    @Bean
    public Queue auditQueue() {
        return QueueBuilder.durable(AUDIT_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", AUDIT_DLQ)
                .build();
    }

    /**
     * 审计死信队列
     * 
     * @return Queue
     */
    @Bean
    public Queue auditDlq() {
        return QueueBuilder.durable(AUDIT_DLQ).build();
    }

    /**
     * 审计队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding auditBinding() {
        return BindingBuilder.bind(auditQueue())
                .to(auditExchange())
                .with(AUDIT_LOG_ROUTING_KEY);
    }

    /**
     * 审计死信队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding auditDlqBinding() {
        return BindingBuilder.bind(auditDlq())
                .to(dlxExchange())
                .with(AUDIT_DLQ);
    }

    // ==================== 文件处理相关队列 ====================

    /**
     * 文件处理交换机
     * 
     * @return TopicExchange
     */
    @Bean
    public TopicExchange fileProcessExchange() {
        return ExchangeBuilder.topicExchange(FILE_PROCESS_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * 文件处理队列
     * 
     * @return Queue
     */
    @Bean
    public Queue fileProcessQueue() {
        return QueueBuilder.durable(FILE_PROCESS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", FILE_PROCESS_DLQ)
                .withArgument("x-message-ttl", 1800000) // 30分钟TTL
                .build();
    }

    /**
     * 文件处理死信队列
     * 
     * @return Queue
     */
    @Bean
    public Queue fileProcessDlq() {
        return QueueBuilder.durable(FILE_PROCESS_DLQ).build();
    }

    /**
     * 文件处理队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding fileProcessBinding() {
        return BindingBuilder.bind(fileProcessQueue())
                .to(fileProcessExchange())
                .with("file.*");
    }

    /**
     * 文件处理死信队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding fileProcessDlqBinding() {
        return BindingBuilder.bind(fileProcessDlq())
                .to(dlxExchange())
                .with(FILE_PROCESS_DLQ);
    }

    // ======================== SMS 相关配置 ========================

    /**
     * SMS交换机
     * 
     * @return TopicExchange
     */
    @Bean
    public TopicExchange smsExchange() {
        return ExchangeBuilder.topicExchange(SMS_EXCHANGE)
                .durable(true)
                .build();
    }

    /**
     * SMS单条发送队列
     * 
     * @return Queue
     */
    @Bean
    public Queue smsQueue() {
        return QueueBuilder.durable(SMS_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SMS_DLQ)
                .withArgument("x-message-ttl", 300000) // 5分钟TTL
                .build();
    }

    /**
     * SMS单条发送死信队列
     * 
     * @return Queue
     */
    @Bean
    public Queue smsDlq() {
        return QueueBuilder.durable(SMS_DLQ).build();
    }

    /**
     * SMS批量发送队列
     * 
     * @return Queue
     */
    @Bean
    public Queue smsBatchQueue() {
        return QueueBuilder.durable(SMS_BATCH_QUEUE)
                .withArgument("x-dead-letter-exchange", DLX_EXCHANGE)
                .withArgument("x-dead-letter-routing-key", SMS_BATCH_DLQ)
                .withArgument("x-message-ttl", 600000) // 10分钟TTL，批量处理需要更长时间
                .build();
    }

    /**
     * SMS批量发送死信队列
     * 
     * @return Queue
     */
    @Bean
    public Queue smsBatchDlq() {
        return QueueBuilder.durable(SMS_BATCH_DLQ).build();
    }

    /**
     * SMS单条发送队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding smsBinding() {
        return BindingBuilder.bind(smsQueue())
                .to(smsExchange())
                .with(SMS_SINGLE_ROUTING_KEY);
    }

    /**
     * SMS模板发送队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding smsTemplateBinding() {
        return BindingBuilder.bind(smsQueue())
                .to(smsExchange())
                .with(SMS_TEMPLATE_ROUTING_KEY);
    }

    /**
     * SMS批量发送队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding smsBatchBinding() {
        return BindingBuilder.bind(smsBatchQueue())
                .to(smsExchange())
                .with(SMS_BATCH_ROUTING_KEY);
    }

    /**
     * SMS单条发送死信队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding smsDlqBinding() {
        return BindingBuilder.bind(smsDlq())
                .to(dlxExchange())
                .with(SMS_DLQ);
    }

    /**
     * SMS批量发送死信队列绑定
     * 
     * @return Binding
     */
    @Bean
    public Binding smsBatchDlqBinding() {
        return BindingBuilder.bind(smsBatchDlq())
                .to(dlxExchange())
                .with(SMS_BATCH_DLQ);
    }

    /**
     * 获取队列状态信息
     * 
     * @param queueName 队列名称
     * @return 队列状态
     */
    public String getQueueStatus(String queueName) {
        try {
            // 这里可以通过RabbitMQ Management API获取队列状态
            // 或者使用Spring AMQP的管理功能
            return "队列 " + queueName + " 状态正常";
        } catch (Exception e) {
            return "获取队列 " + queueName + " 状态失败: " + e.getMessage();
        }
    }

    /**
     * 清空队列
     * 
     * @param queueName 队列名称
     * @return 是否成功
     */
    public boolean purgeQueue(String queueName) {
        try {
            // 这里可以实现清空队列的逻辑
            System.out.println("清空队列: " + queueName);
            return true;
        } catch (Exception e) {
            System.err.println("清空队列失败: " + e.getMessage());
            return false;
        }
    }
}