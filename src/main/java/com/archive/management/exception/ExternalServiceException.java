package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 外部服务异常类
 * 用于处理外部API调用、第三方服务等相关异常
 * 
 * @author Archive Management System
 * @since 1.0.0
 */
public class ExternalServiceException extends BaseException {

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     */
    public ExternalServiceException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public ExternalServiceException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public ExternalServiceException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public ExternalServiceException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public ExternalServiceException(ResponseCode responseCode, String message, Throwable cause) {
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
    public ExternalServiceException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    // 静态工厂方法，用于快速创建常见的外部服务异常

    /**
     * 外部服务调用失败异常
     *
     * @return ExternalServiceException
     */
    public static ExternalServiceException serviceError() {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_ERROR);
    }

    /**
     * 外部服务调用失败异常
     *
     * @param serviceName 服务名称
     * @return ExternalServiceException
     */
    public static ExternalServiceException serviceError(String serviceName) {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_ERROR, 
                "外部服务调用失败: " + serviceName);
    }

    /**
     * 外部服务调用失败异常
     *
     * @param serviceName 服务名称
     * @param cause 原因异常
     * @return ExternalServiceException
     */
    public static ExternalServiceException serviceError(String serviceName, Throwable cause) {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_ERROR, 
                "外部服务调用失败: " + serviceName, cause);
    }

    /**
     * 外部服务超时异常
     *
     * @return ExternalServiceException
     */
    public static ExternalServiceException timeout() {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_TIMEOUT);
    }

    /**
     * 外部服务超时异常
     *
     * @param serviceName 服务名称
     * @return ExternalServiceException
     */
    public static ExternalServiceException timeout(String serviceName) {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_TIMEOUT, 
                "外部服务调用超时: " + serviceName);
    }

    /**
     * 外部服务超时异常
     *
     * @param serviceName 服务名称
     * @param timeoutMs 超时时间（毫秒）
     * @return ExternalServiceException
     */
    public static ExternalServiceException timeout(String serviceName, long timeoutMs) {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_TIMEOUT, 
                String.format("外部服务调用超时: %s (超时时间: %dms)", serviceName, timeoutMs));
    }

    /**
     * 外部服务不可用异常
     *
     * @return ExternalServiceException
     */
    public static ExternalServiceException unavailable() {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_UNAVAILABLE);
    }

    /**
     * 外部服务不可用异常
     *
     * @param serviceName 服务名称
     * @return ExternalServiceException
     */
    public static ExternalServiceException unavailable(String serviceName) {
        return new ExternalServiceException(ResponseCode.EXTERNAL_SERVICE_UNAVAILABLE, 
                "外部服务不可用: " + serviceName);
    }

    /**
     * 网络连接异常
     *
     * @return ExternalServiceException
     */
    public static ExternalServiceException networkError() {
        return new ExternalServiceException(ResponseCode.NETWORK_ERROR);
    }

    /**
     * 网络连接异常
     *
     * @param cause 原因异常
     * @return ExternalServiceException
     */
    public static ExternalServiceException networkError(Throwable cause) {
        return new ExternalServiceException(ResponseCode.NETWORK_ERROR, cause);
    }

    /**
     * 网络连接异常
     *
     * @param url 请求URL
     * @param cause 原因异常
     * @return ExternalServiceException
     */
    public static ExternalServiceException networkError(String url, Throwable cause) {
        return new ExternalServiceException(ResponseCode.NETWORK_ERROR, 
                "网络连接异常: " + url, cause);
    }
}