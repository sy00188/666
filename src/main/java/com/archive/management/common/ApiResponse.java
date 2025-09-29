package com.archive.management.common;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.time.LocalDateTime;

/**
 * 统一API响应结果类
 * 
 * @param <T> 响应数据类型
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    /**
     * 响应状态码
     */
    private Integer code;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 是否成功
     */
    private Boolean success;

    /**
     * 错误码（业务错误码）
     */
    private String errorCode;

    /**
     * 响应时间戳
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;

    /**
     * 默认构造函数
     */
    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数
     * 
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param success 是否成功
     */
    public ApiResponse(Integer code, String message, T data, Boolean success) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }

    /**
     * 构造函数（带错误码）
     * 
     * @param code 状态码
     * @param message 消息
     * @param data 数据
     * @param success 是否成功
     * @param errorCode 错误码
     */
    public ApiResponse(Integer code, String message, T data, Boolean success, String errorCode) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.success = success;
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
    }

    // ========== 静态工厂方法 ==========

    /**
     * 成功响应（无数据）
     * 
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(200, "操作成功", null, true);
    }

    /**
     * 成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(200, "操作成功", data, true);
    }

    /**
     * 成功响应（带数据和消息）
     * 
     * @param data 响应数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(200, message, data, true);
    }

    /**
     * 成功响应（仅消息）
     * 
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(200, message, null, true);
    }

    /**
     * 错误响应
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(500, message, null, false);
    }

    /**
     * 错误响应（带错误码）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> error(Integer code, String message) {
        return new ApiResponse<>(code, message, null, false);
    }

    /**
     * 错误响应（带业务错误码）
     * 
     * @param message 错误消息
     * @param errorCode 业务错误码
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> error(String message, String errorCode) {
        return new ApiResponse<>(500, message, null, false, errorCode);
    }

    /**
     * 错误响应（完整参数）
     * 
     * @param code 状态码
     * @param message 错误消息
     * @param errorCode 业务错误码
     * @param <T> 数据类型
     * @return ApiResponse<T>
     */
    public static <T> ApiResponse<T> error(Integer code, String message, String errorCode) {
        return new ApiResponse<>(code, message, null, false, errorCode);
    }

    // ========== Getter and Setter ==========

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ApiResponse{" +
                "code=" + code +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", success=" + success +
                ", errorCode='" + errorCode + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}