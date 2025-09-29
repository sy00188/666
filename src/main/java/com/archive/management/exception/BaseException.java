package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 基础异常类
 * 所有自定义异常的基类，提供统一的异常处理机制
 * 
 * @author Archive Management System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public abstract class BaseException extends RuntimeException {

    /**
     * 错误码
     */
    private final ResponseCode responseCode;

    /**
     * 错误消息
     */
    private final String message;

    /**
     * 详细数据
     */
    private final Object data;

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     */
    public BaseException(ResponseCode responseCode) {
        super(responseCode.getMessage());
        this.responseCode = responseCode;
        this.message = responseCode.getMessage();
        this.data = null;
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public BaseException(ResponseCode responseCode, String message) {
        super(message);
        this.responseCode = responseCode;
        this.message = message;
        this.data = null;
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public BaseException(ResponseCode responseCode, String message, Object data) {
        super(message);
        this.responseCode = responseCode;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public BaseException(ResponseCode responseCode, Throwable cause) {
        super(responseCode.getMessage(), cause);
        this.responseCode = responseCode;
        this.message = responseCode.getMessage();
        this.data = null;
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public BaseException(ResponseCode responseCode, String message, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
        this.message = message;
        this.data = null;
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     * @param cause 原因异常
     */
    public BaseException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(message, cause);
        this.responseCode = responseCode;
        this.message = message;
        this.data = data;
    }

    /**
     * 获取错误码
     *
     * @return 错误码
     */
    public int getCode() {
        return responseCode.getCode();
    }

    /**
     * 获取错误消息
     *
     * @return 错误消息
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * 获取响应码
     *
     * @return 响应码
     */
    public ResponseCode getResponseCode() {
        return responseCode;
    }

    /**
     * 获取详细数据
     *
     * @return 详细数据
     */
    public Object getData() {
        return data;
    }

    /**
     * 转换为字符串
     *
     * @return 字符串表示
     */
    @Override
    public String toString() {
        return String.format("%s{code=%d, message='%s', data=%s}", 
                this.getClass().getSimpleName(), 
                getCode(), 
                getMessage(), 
                data);
    }
}