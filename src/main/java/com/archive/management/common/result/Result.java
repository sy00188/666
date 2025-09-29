package com.archive.management.common.result;

import com.archive.management.common.enums.ResponseCode;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 统一响应结果类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "统一响应结果")
public class Result<T> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 响应状态码
     */
    @Schema(description = "响应状态码", example = "200")
    private Integer code;

    /**
     * 响应消息
     */
    @Schema(description = "响应消息", example = "操作成功")
    private String message;

    /**
     * 响应数据
     */
    @Schema(description = "响应数据")
    private T data;

    /**
     * 响应时间戳
     */
    @Schema(description = "响应时间戳")
    private LocalDateTime timestamp;

    /**
     * 请求追踪ID
     */
    @Schema(description = "请求追踪ID")
    private String traceId;

    public Result() {
        this.timestamp = LocalDateTime.now();
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

    public Result(ResponseCode responseCode) {
        this(responseCode.getCode(), responseCode.getMessage());
    }

    public Result(ResponseCode responseCode, T data) {
        this(responseCode.getCode(), responseCode.getMessage(), data);
    }

    /**
     * 成功响应
     */
    public static <T> Result<T> success() {
        return new Result<>(ResponseCode.SUCCESS);
    }

    /**
     * 成功响应，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(ResponseCode.SUCCESS, data);
    }

    /**
     * 成功响应，自定义消息
     */
    public static <T> Result<T> success(String message) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), message);
    }

    /**
     * 成功响应，自定义消息和数据
     */
    public static <T> Result<T> success(String message, T data) {
        return new Result<>(ResponseCode.SUCCESS.getCode(), message, data);
    }

    /**
     * 失败响应
     */
    public static <T> Result<T> error() {
        return new Result<>(ResponseCode.SYSTEM_ERROR);
    }

    /**
     * 失败响应，自定义消息
     */
    public static <T> Result<T> error(String message) {
        return new Result<>(ResponseCode.SYSTEM_ERROR.getCode(), message);
    }

    /**
     * 失败响应，使用响应码枚举
     */
    public static <T> Result<T> error(ResponseCode responseCode) {
        return new Result<>(responseCode);
    }

    /**
     * 失败响应，使用响应码枚举和数据
     */
    public static <T> Result<T> error(ResponseCode responseCode, T data) {
        return new Result<>(responseCode, data);
    }

    /**
     * 失败响应，自定义状态码和消息
     */
    public static <T> Result<T> error(Integer code, String message) {
        return new Result<>(code, message);
    }

    /**
     * 失败响应，自定义状态码、消息和数据
     */
    public static <T> Result<T> error(Integer code, String message, T data) {
        return new Result<>(code, message, data);
    }

    /**
     * 判断是否成功
     */
    public boolean isSuccess() {
        return ResponseCode.SUCCESS.getCode() == this.code;
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

    // 手动添加setter方法以解决Lombok编译问题
    public void setCode(Integer code) {
        this.code = code;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setData(T data) {
        this.data = data;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}