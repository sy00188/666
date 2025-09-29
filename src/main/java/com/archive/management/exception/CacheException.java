package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 缓存异常类
 * 用于处理Redis缓存操作相关异常
 * 
 * @author Archive Management System
 * @since 1.0.0
 */
public class CacheException extends BaseException {

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     */
    public CacheException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public CacheException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public CacheException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public CacheException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public CacheException(ResponseCode responseCode, String message, Throwable cause) {
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
    public CacheException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    // 静态工厂方法，用于快速创建常见的缓存异常

    /**
     * 缓存操作失败异常
     *
     * @return CacheException
     */
    public static CacheException operationError() {
        return new CacheException(ResponseCode.CACHE_OPERATION_ERROR);
    }

    /**
     * 缓存操作失败异常
     *
     * @param operation 操作类型
     * @return CacheException
     */
    public static CacheException operationError(String operation) {
        return new CacheException(ResponseCode.CACHE_OPERATION_ERROR, 
                "缓存操作失败: " + operation);
    }

    /**
     * 缓存操作失败异常
     *
     * @param operation 操作类型
     * @param cause 原因异常
     * @return CacheException
     */
    public static CacheException operationError(String operation, Throwable cause) {
        return new CacheException(ResponseCode.CACHE_OPERATION_ERROR, 
                "缓存操作失败: " + operation, cause);
    }

    /**
     * 缓存连接失败异常
     *
     * @return CacheException
     */
    public static CacheException connectionError() {
        return new CacheException(ResponseCode.CACHE_CONNECTION_ERROR);
    }

    /**
     * 缓存连接失败异常
     *
     * @param cause 原因异常
     * @return CacheException
     */
    public static CacheException connectionError(Throwable cause) {
        return new CacheException(ResponseCode.CACHE_CONNECTION_ERROR, cause);
    }

    /**
     * 缓存数据过期异常
     *
     * @return CacheException
     */
    public static CacheException dataExpired() {
        return new CacheException(ResponseCode.CACHE_DATA_EXPIRED);
    }

    /**
     * 缓存数据过期异常
     *
     * @param key 缓存键
     * @return CacheException
     */
    public static CacheException dataExpired(String key) {
        return new CacheException(ResponseCode.CACHE_DATA_EXPIRED, 
                "缓存数据过期: " + key);
    }

    /**
     * 缓存键不存在异常
     *
     * @param key 缓存键
     * @return CacheException
     */
    public static CacheException keyNotFound(String key) {
        return new CacheException(ResponseCode.CACHE_OPERATION_ERROR, 
                "缓存键不存在: " + key);
    }

    /**
     * 缓存序列化异常
     *
     * @param cause 原因异常
     * @return CacheException
     */
    public static CacheException serializationError(Throwable cause) {
        return new CacheException(ResponseCode.SERIALIZATION_ERROR, 
                "缓存数据序列化失败", cause);
    }

    /**
     * 缓存反序列化异常
     *
     * @param cause 原因异常
     * @return CacheException
     */
    public static CacheException deserializationError(Throwable cause) {
        return new CacheException(ResponseCode.DESERIALIZATION_ERROR, 
                "缓存数据反序列化失败", cause);
    }
}