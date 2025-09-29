package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 业务异常类
 * 用于处理业务逻辑相关的异常情况
 * 
 * @author Archive Management System
 * @since 1.0.0
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class BusinessException extends BaseException {

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     */
    public BusinessException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public BusinessException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public BusinessException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public BusinessException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public BusinessException(ResponseCode responseCode, String message, Throwable cause) {
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
    public BusinessException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    // 为了兼容现有代码，保留原有的构造函数

    /**
      * 构造函数（兼容性）
      *
      * @param code 错误码
      * @param message 错误消息
      */
     public BusinessException(int code, String message) {
         super(ResponseCode.SYSTEM_ERROR, message);
     }

     /**
      * 构造函数（兼容性）
      *
      * @param code 错误码
      * @param message 错误消息
      * @param data 详细数据
      */
     public BusinessException(int code, String message, Object data) {
         super(ResponseCode.SYSTEM_ERROR, message, data);
     }

     /**
      * 构造函数（兼容性）
      *
      * @param message 错误消息
      */
     public BusinessException(String message) {
         super(ResponseCode.SYSTEM_ERROR, message);
     }

     /**
      * 构造函数（兼容性）
      *
      * @param message 错误消息
      * @param cause 原因异常
      */
     public BusinessException(String message, Throwable cause) {
         super(ResponseCode.SYSTEM_ERROR, message, cause);
     }

    // 常用业务异常静态方法

    /**
     * 参数错误异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException paramError(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 数据不存在异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException dataNotFound(String message) {
        return new BusinessException(404, message);
    }

    /**
     * 数据已存在异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException dataExists(String message) {
        return new BusinessException(409, message);
    }

    /**
     * 权限不足异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException accessDenied(String message) {
        return new BusinessException(403, message);
    }

    /**
     * 操作失败异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException operationFailed(String message) {
        return new BusinessException(500, message);
    }

    /**
     * 状态错误异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException statusError(String message) {
        return new BusinessException(400, message);
    }

    /**
     * 验证失败异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException validationFailed(String message) {
        return new BusinessException(422, message);
    }

    /**
     * 系统繁忙异常
     * 
     * @param message 错误消息
     * @return BusinessException
     */
    public static BusinessException systemBusy(String message) {
        return new BusinessException(503, message);
    }

    // 删除重复的getter/setter方法，因为已经继承自BaseException
    // 如果需要特殊的业务逻辑，可以在这里重写父类方法
    
    @Override
    public String toString() {
        return "BusinessException{" +
                "code=" + getCode() +
                ", message='" + getMessage() + '\'' +
                ", data=" + getData() +
                '}';
    }
}