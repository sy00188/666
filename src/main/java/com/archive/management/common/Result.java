package com.archive.management.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 服务层统一返回结果类
 * 用于Service层方法的返回值封装
 * 
 * @param <T> 数据类型
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> {

    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 响应消息
     */
    private String message;

    /**
     * 响应数据
     */
    private T data;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 默认构造函数
     */
    public Result() {
    }

    /**
     * 构造函数
     * 
     * @param success 是否成功
     * @param message 消息
     * @param data 数据
     */
    public Result(boolean success, String message, T data) {
        this.success = success;
        this.message = message;
        this.data = data;
    }

    /**
     * 构造函数（带错误码）
     * 
     * @param success 是否成功
     * @param message 消息
     * @param data 数据
     * @param errorCode 错误码
     */
    public Result(boolean success, String message, T data, String errorCode) {
        this.success = success;
        this.message = message;
        this.data = data;
        this.errorCode = errorCode;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 成功结果（无数据）
     * 
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success() {
        return new Result<>(true, "操作成功", null);
    }

    /**
     * 成功结果（带数据）
     * 
     * @param data 数据
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(true, "操作成功", data);
    }

    /**
     * 成功结果（带数据和消息）
     * 
     * @param data 数据
     * @param message 消息
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(T data, String message) {
        return new Result<>(true, message, data);
    }

    /**
     * 成功结果（仅消息）
     * 
     * @param message 消息
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(true, message, null);
    }

    /**
     * 失败结果
     * 
     * @param message 错误消息
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> failure(String message) {
        return new Result<>(false, message, null);
    }

    /**
     * 失败结果（带错误码）
     * 
     * @param message 错误消息
     * @param errorCode 错误码
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> failure(String message, String errorCode) {
        return new Result<>(false, message, null, errorCode);
    }

    /**
     * 失败结果（带数据）
     * 
     * @param message 错误消息
     * @param data 数据
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> failure(String message, T data) {
        return new Result<>(false, message, data);
    }

    /**
     * 条件结果
     * 
     * @param condition 条件
     * @param successData 成功时的数据
     * @param failureMessage 失败时的消息
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> condition(boolean condition, T successData, String failureMessage) {
        return condition ? success(successData) : failure(failureMessage);
    }

    /**
     * 条件结果（仅消息）
     * 
     * @param condition 条件
     * @param successMessage 成功消息
     * @param failureMessage 失败消息
     * @param <T> 数据类型
     * @return Result<T>
     */
    public static <T> Result<T> condition(boolean condition, String successMessage, String failureMessage) {
        return condition ? success(successMessage) : failure(failureMessage);
    }

    // ========== 便捷方法 ==========

    /**
     * 判断是否成功
     * 
     * @return 是否成功
     */
    public boolean isSuccess() {
        return success;
    }

    /**
     * 判断是否失败
     * 
     * @return 是否失败
     */
    public boolean isFailure() {
        return !success;
    }

    /**
     * 获取数据，如果失败则返回null
     * 
     * @return 数据或null
     */
    public T getDataOrNull() {
        return success ? data : null;
    }

    /**
     * 获取数据，如果失败则返回默认值
     * 
     * @param defaultValue 默认值
     * @return 数据或默认值
     */
    public T getDataOrDefault(T defaultValue) {
        return success ? data : defaultValue;
    }

    // ========== Getter and Setter ==========

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    @Override
    public String toString() {
        return "Result{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", data=" + data +
                ", errorCode='" + errorCode + '\'' +
                '}';
    }
}