package com.archive.management.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.io.Serializable;

/**
 * 统一响应结果类
 * 封装API响应的标准格式
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

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
     * 时间戳
     */
    private Long timestamp;

    /**
     * 请求追踪ID
     */
    private String traceId;

    public Result() {
        this.timestamp = System.currentTimeMillis();
    }

    public Result(Integer code, String message) {
        this();
        this.code = code;
        this.message = message;
    }

    public Result(Integer code, String message, T data) {
        this(code, message);
        this.data = data;
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage());
    }

    /**
     * 成功响应带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), ResultCode.SUCCESS.getMessage(), data);
    }

    /**
     * 成功响应带消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResultCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResultCode.SYSTEM_ERROR.getCode(), ResultCode.SYSTEM_ERROR.getMessage());
    }

    /**
     * 失败响应带消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResultCode.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 失败响应带错误码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败响应带错误码枚举
     */
    public static <T> Result<T> error(ResultCode resultCode) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage());
    }

    /**
     * 失败响应带错误码枚举和数据
     */
    public static <T> Result<T> error(ResultCode resultCode, T data) {
        return new Result<>(resultCode.getCode(), resultCode.getMessage(), data);
    }

    /**
     * 失败响应带错误码枚举和自定义消息
     */
    public static <T> Result<T> error(ResultCode resultCode, String message) {
        return new Result<>(resultCode.getCode(), message);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResultCode.SUCCESS.getCode().equals(this.code);
    }

    /**
     * 判断是否失败
     */
    public boolean isError() {
        return !isSuccess();
    }

    /**
     * 设置追踪ID
     */
    public Result<T> traceId(String traceId) {
        this.traceId = traceId;
        return this;
    }

    /**
     * 设置数据
     */
    public Result<T> data(T data) {
        this.data = data;
        return this;
    }

    /**
     * 设置消息
     */
    public Result<T> message(String message) {
        this.message = message;
        return this;
    }

    /**
     * 设置错误码
     */
    public Result<T> code(Integer code) {
        this.code = code;
        return this;
    }
}