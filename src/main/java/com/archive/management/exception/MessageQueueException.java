package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 消息队列异常类
 * 用于处理消息队列连接、发送、消费等相关异常
 * 
 * @author Archive Management System
 * @since 1.0.0
 */
public class MessageQueueException extends BaseException {

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     */
    public MessageQueueException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public MessageQueueException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public MessageQueueException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public MessageQueueException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public MessageQueueException(ResponseCode responseCode, String message, Throwable cause) {
        super(responseCode, message, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     * @param cause 原因异常
     */
    public MessageQueueException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    // 静态工厂方法，用于快速创建常见的消息队列异常

    /**
     * 消息队列连接失败异常
     *
     * @return MessageQueueException
     */
    public static MessageQueueException connectionError() {
        return new MessageQueueException(ResponseCode.MESSAGE_QUEUE_CONNECTION_ERROR);
    }

    /**
     * 消息队列连接失败异常
     *
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException connectionError(Throwable cause) {
        return new MessageQueueException(ResponseCode.MESSAGE_QUEUE_CONNECTION_ERROR, cause);
    }

    /**
     * 消息队列连接失败异常
     *
     * @param brokerUrl 消息队列地址
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException connectionError(String brokerUrl, Throwable cause) {
        return new MessageQueueException(ResponseCode.MESSAGE_QUEUE_CONNECTION_ERROR, 
                "消息队列连接失败: " + brokerUrl, cause);
    }

    /**
     * 消息发送失败异常
     *
     * @return MessageQueueException
     */
    public static MessageQueueException sendError() {
        return new MessageQueueException(ResponseCode.MESSAGE_SEND_ERROR);
    }

    /**
     * 消息发送失败异常
     *
     * @param topic 主题
     * @return MessageQueueException
     */
    public static MessageQueueException sendError(String topic) {
        return new MessageQueueException(ResponseCode.MESSAGE_SEND_ERROR, 
                "消息发送失败: " + topic);
    }

    /**
     * 消息发送失败异常
     *
     * @param topic 主题
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException sendError(String topic, Throwable cause) {
        return new MessageQueueException(ResponseCode.MESSAGE_SEND_ERROR, 
                "消息发送失败: " + topic, cause);
    }

    /**
     * 消息发送失败异常
     *
     * @param topic 主题
     * @param messageId 消息ID
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException sendError(String topic, String messageId, Throwable cause) {
        return new MessageQueueException(ResponseCode.MESSAGE_SEND_ERROR, 
                String.format("消息发送失败: topic=%s, messageId=%s", topic, messageId), cause);
    }

    /**
     * 消息消费失败异常
     *
     * @return MessageQueueException
     */
    public static MessageQueueException consumeError() {
        return new MessageQueueException(ResponseCode.MESSAGE_CONSUME_ERROR);
    }

    /**
     * 消息消费失败异常
     *
     * @param topic 主题
     * @return MessageQueueException
     */
    public static MessageQueueException consumeError(String topic) {
        return new MessageQueueException(ResponseCode.MESSAGE_CONSUME_ERROR, 
                "消息消费失败: " + topic);
    }

    /**
     * 消息消费失败异常
     *
     * @param topic 主题
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException consumeError(String topic, Throwable cause) {
        return new MessageQueueException(ResponseCode.MESSAGE_CONSUME_ERROR, 
                "消息消费失败: " + topic, cause);
    }

    /**
     * 消息消费失败异常
     *
     * @param topic 主题
     * @param messageId 消息ID
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException consumeError(String topic, String messageId, Throwable cause) {
        return new MessageQueueException(ResponseCode.MESSAGE_CONSUME_ERROR, 
                String.format("消息消费失败: topic=%s, messageId=%s", topic, messageId), cause);
    }

    /**
     * 消息序列化异常
     *
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException serializationError(Throwable cause) {
        return new MessageQueueException(ResponseCode.SERIALIZATION_ERROR, 
                "消息序列化失败", cause);
    }

    /**
     * 消息反序列化异常
     *
     * @param cause 原因异常
     * @return MessageQueueException
     */
    public static MessageQueueException deserializationError(Throwable cause) {
        return new MessageQueueException(ResponseCode.DESERIALIZATION_ERROR, 
                "消息反序列化失败", cause);
    }
}