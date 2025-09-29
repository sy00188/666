package com.archive.management.util;

import com.archive.management.common.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

/**
 * 响应工具类
 * 提供统一的API响应构建方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class ResponseUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private ResponseUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== 成功响应 ==========

    /**
     * 构建成功响应（无数据）
     * 
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> success() {
        return ResponseEntity.ok(ApiResponse.success());
    }

    /**
     * 构建成功响应（带数据）
     * 
     * @param data 响应数据
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data) {
        return ResponseEntity.ok(ApiResponse.success(data));
    }

    /**
     * 构建成功响应（带数据和消息）
     * 
     * @param data 响应数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> success(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    /**
     * 构建成功响应（仅消息）
     * 
     * @param message 响应消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> success(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ========== 错误响应 ==========

    /**
     * 构建错误响应
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> error(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(message));
    }

    /**
     * 构建错误响应（带错误码）
     * 
     * @param code 错误码
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> error(int code, String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(code, message));
    }

    /**
     * 构建错误响应（带HTTP状态码）
     * 
     * @param status HTTP状态码
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(status.value(), message));
    }

    /**
     * 构建错误响应（带HTTP状态码和错误码）
     * 
     * @param status HTTP状态码
     * @param code 错误码
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> error(HttpStatus status, int code, String message) {
        return ResponseEntity.status(status)
                .body(ApiResponse.error(code, message));
    }

    // ========== 特定状态响应 ==========

    /**
     * 构建400 Bad Request响应
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> badRequest(String message) {
        return ResponseEntity.badRequest()
                .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), message));
    }

    /**
     * 构建401 Unauthorized响应
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> unauthorized(String message) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error(HttpStatus.UNAUTHORIZED.value(), message));
    }

    /**
     * 构建403 Forbidden响应
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> forbidden(String message) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(HttpStatus.FORBIDDEN.value(), message));
    }

    /**
     * 构建404 Not Found响应
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> notFound(String message) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.error(HttpStatus.NOT_FOUND.value(), message));
    }

    /**
     * 构建409 Conflict响应
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> conflict(String message) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.error(HttpStatus.CONFLICT.value(), message));
    }

    /**
     * 构建422 Unprocessable Entity响应（参数验证失败）
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> unprocessableEntity(String message) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), message));
    }

    /**
     * 构建422 Unprocessable Entity响应（带验证错误详情）
     * 
     * @param message 错误消息
     * @param errors 验证错误详情
     * @return ResponseEntity<ApiResponse<Map<String, List<String>>>>
     */
    public static ResponseEntity<ApiResponse<Map<String, List<String>>>> unprocessableEntity(
            String message, Map<String, List<String>> errors) {
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
                .body(ApiResponse.error(HttpStatus.UNPROCESSABLE_ENTITY.value(), message, errors));
    }

    /**
     * 构建500 Internal Server Error响应
     * 
     * @param message 错误消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> internalServerError(String message) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.error(HttpStatus.INTERNAL_SERVER_ERROR.value(), message));
    }

    // ========== 创建响应 ==========

    /**
     * 构建201 Created响应
     * 
     * @param data 创建的数据
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, "创建成功"));
    }

    /**
     * 构建201 Created响应（带消息）
     * 
     * @param data 创建的数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> created(T data, String message) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(data, message));
    }

    // ========== 更新响应 ==========

    /**
     * 构建更新成功响应
     * 
     * @param data 更新后的数据
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> updated(T data) {
        return ResponseEntity.ok(ApiResponse.success(data, "更新成功"));
    }

    /**
     * 构建更新成功响应（带消息）
     * 
     * @param data 更新后的数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> updated(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    // ========== 删除响应 ==========

    /**
     * 构建删除成功响应
     * 
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> deleted() {
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }

    /**
     * 构建删除成功响应（带消息）
     * 
     * @param message 响应消息
     * @return ResponseEntity<ApiResponse<Void>>
     */
    public static ResponseEntity<ApiResponse<Void>> deleted(String message) {
        return ResponseEntity.ok(ApiResponse.success(message));
    }

    // ========== 分页响应 ==========

    /**
     * 构建分页响应
     * 
     * @param data 分页数据
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> page(T data) {
        return ResponseEntity.ok(ApiResponse.success(data, "查询成功"));
    }

    /**
     * 构建分页响应（带消息）
     * 
     * @param data 分页数据
     * @param message 响应消息
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> page(T data, String message) {
        return ResponseEntity.ok(ApiResponse.success(data, message));
    }

    // ========== 条件响应 ==========

    /**
     * 根据条件构建响应
     * 
     * @param condition 条件
     * @param successData 成功时的数据
     * @param errorMessage 失败时的错误消息
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> conditional(
            boolean condition, T successData, String errorMessage) {
        if (condition) {
            return success(successData);
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.error(HttpStatus.BAD_REQUEST.value(), errorMessage));
        }
    }

    /**
     * 根据数据是否为空构建响应
     * 
     * @param data 数据
     * @param notFoundMessage 数据为空时的错误消息
     * @param <T> 数据类型
     * @return ResponseEntity<ApiResponse<T>>
     */
    public static <T> ResponseEntity<ApiResponse<T>> ofNullable(T data, String notFoundMessage) {
        if (data != null) {
            return success(data);
        } else {
            return notFound(notFoundMessage);
        }
    }

    /**
     * 根据列表是否为空构建响应
     * 
     * @param list 列表数据
     * @param emptyMessage 列表为空时的消息
     * @param <T> 列表元素类型
     * @return ResponseEntity<ApiResponse<List<T>>>
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> ofList(List<T> list, String emptyMessage) {
        if (list != null && !list.isEmpty()) {
            return success(list);
        } else {
            return success(list, emptyMessage);
        }
    }
}