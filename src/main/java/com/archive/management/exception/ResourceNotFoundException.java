package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 资源未找到异常
 * 用于处理各种资源不存在的情况
 *
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public class ResourceNotFoundException extends BaseException {

    private String resourceType;
    private Object resourceId;

    // 基础构造函数
    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     */
    public ResourceNotFoundException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public ResourceNotFoundException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public ResourceNotFoundException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public ResourceNotFoundException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public ResourceNotFoundException(ResponseCode responseCode, String message, Throwable cause) {
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
    public ResourceNotFoundException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    // 兼容性构造函数
    /**
     * 兼容性构造函数 - 仅消息
     * 
     * @param message 异常消息
     */
    public ResourceNotFoundException(String message) {
        super(ResponseCode.NOT_FOUND, message);
    }

    /**
     * 兼容性构造函数 - 消息和原因
     * 
     * @param message 异常消息
     * @param cause 原因
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(ResponseCode.NOT_FOUND, message, cause);
    }

    // 静态方法专用构造函数
    /**
     * 资源类型和ID构造函数 - Long类型ID
     * 
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     */
    public ResourceNotFoundException(String resourceType, Long resourceId) {
        super(ResponseCode.NOT_FOUND, resourceType + "未找到，ID: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    /**
     * 资源类型和ID构造函数 - String类型ID
     * 
     * @param resourceType 资源类型
     * @param resourceId 资源ID
     */
    public ResourceNotFoundException(String resourceType, String resourceId) {
        super(ResponseCode.NOT_FOUND, resourceType + "未找到，标识: " + resourceId);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }

    // 常用静态方法

    /**
     * 用户未找到异常
     * 
     * @param userId 用户ID
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException userNotFound(Long userId) {
        return new ResourceNotFoundException("用户", userId);
    }

    /**
     * 用户未找到异常
     * 
     * @param username 用户名
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException userNotFound(String username) {
        return new ResourceNotFoundException("用户", username);
    }

    /**
     * 角色未找到异常
     * 
     * @param roleId 角色ID
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException roleNotFound(Long roleId) {
        return new ResourceNotFoundException("角色", roleId);
    }

    /**
     * 权限未找到异常
     * 
     * @param permissionId 权限ID
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException permissionNotFound(Long permissionId) {
        return new ResourceNotFoundException("权限", permissionId);
    }

    /**
     * 档案未找到异常
     * 
     * @param archiveId 档案ID
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException archiveNotFound(Long archiveId) {
        return new ResourceNotFoundException("档案", archiveId);
    }

    /**
     * 文件未找到异常
     * 
     * @param fileId 文件ID
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException fileNotFound(Long fileId) {
        return new ResourceNotFoundException("文件", fileId);
    }

    /**
     * 分类未找到异常
     * 
     * @param categoryId 分类ID
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException categoryNotFound(Long categoryId) {
        return new ResourceNotFoundException("分类", categoryId);
    }

    /**
     * 配置未找到异常
     * 
     * @param configKey 配置键
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException configNotFound(String configKey) {
        return new ResourceNotFoundException("配置", configKey);
    }

    /**
     * 日志未找到异常
     * 
     * @param logId 日志ID
     * @return ResourceNotFoundException
     */
    public static ResourceNotFoundException logNotFound(Long logId) {
        return new ResourceNotFoundException("日志", logId);
    }

    // Getter 和 Setter 方法

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public Object getResourceId() {
        return resourceId;
    }

    public void setResourceId(Object resourceId) {
        this.resourceId = resourceId;
    }

    @Override
    public String toString() {
        return "ResourceNotFoundException{" +
                "resourceType='" + resourceType + '\'' +
                ", resourceId=" + resourceId +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}