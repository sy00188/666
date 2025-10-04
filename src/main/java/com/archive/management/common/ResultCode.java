package com.archive.management.common;

/**
 * 统一错误码枚举
 * 定义系统中所有的错误码和错误信息
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public enum ResultCode {

    // 成功状态码
    SUCCESS(200, "操作成功"),
    
    // 客户端错误 4xx
    PARAM_ERROR(400, "参数错误"),
    PARAM_VALID_ERROR(400, "参数验证失败"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "访问被拒绝"),
    NOT_FOUND(404, "资源不存在"),
    METHOD_NOT_ALLOWED(405, "请求方法不支持"),
    CONFLICT(409, "资源冲突"),
    FILE_SIZE_EXCEEDED(413, "文件大小超出限制"),
    UNSUPPORTED_MEDIA_TYPE(415, "不支持的媒体类型"),
    TOO_MANY_REQUESTS(429, "请求过于频繁"),
    
    // 服务端错误 5xx
    SYSTEM_ERROR(500, "系统内部错误"),
    DATABASE_ERROR(500, "数据库操作失败"),
    SERVICE_UNAVAILABLE(503, "服务不可用"),
    
    // 业务错误码 6xxx
    BUSINESS_ERROR(600, "业务处理失败"),
    USER_NOT_FOUND(6001, "用户不存在"),
    USER_ALREADY_EXISTS(6002, "用户已存在"),
    USER_DISABLED(6003, "用户已被禁用"),
    USER_LOCKED(6004, "用户已被锁定"),
    PASSWORD_ERROR(6005, "密码错误"),
    PASSWORD_EXPIRED(6006, "密码已过期"),
    ACCOUNT_EXPIRED(6007, "账户已过期"),
    
    ARCHIVE_NOT_FOUND(6101, "档案不存在"),
    ARCHIVE_ALREADY_EXISTS(6102, "档案已存在"),
    ARCHIVE_LOCKED(6103, "档案已被锁定"),
    ARCHIVE_PERMISSION_DENIED(6104, "档案访问权限不足"),
    
    ROLE_NOT_FOUND(6201, "角色不存在"),
    ROLE_ALREADY_EXISTS(6202, "角色已存在"),
    ROLE_IN_USE(6203, "角色正在使用中"),
    
    PERMISSION_NOT_FOUND(6301, "权限不存在"),
    PERMISSION_DENIED(6302, "权限不足"),
    
    DEPARTMENT_NOT_FOUND(6401, "部门不存在"),
    DEPARTMENT_ALREADY_EXISTS(6402, "部门已存在"),
    DEPARTMENT_HAS_CHILDREN(6403, "部门下存在子部门"),
    DEPARTMENT_HAS_USERS(6404, "部门下存在用户"),
    
    FILE_NOT_FOUND(6501, "文件不存在"),
    FILE_UPLOAD_FAILED(6502, "文件上传失败"),
    FILE_DOWNLOAD_FAILED(6503, "文件下载失败"),
    FILE_DELETE_FAILED(6504, "文件删除失败"),
    FILE_TYPE_NOT_SUPPORTED(6505, "文件类型不支持"),
    FILE_SIZE_TOO_LARGE(6506, "文件大小过大"),
    
    // 系统配置错误 7xxx
    CONFIG_ERROR(7001, "配置错误"),
    CONFIG_NOT_FOUND(7002, "配置不存在"),
    
    // 外部服务错误 8xxx
    EXTERNAL_SERVICE_ERROR(8001, "外部服务调用失败"),
    EXTERNAL_SERVICE_TIMEOUT(8002, "外部服务调用超时"),
    EXTERNAL_SERVICE_UNAVAILABLE(8003, "外部服务不可用"),
    
    // 缓存错误 9xxx
    CACHE_ERROR(9001, "缓存操作失败"),
    CACHE_KEY_NOT_FOUND(9002, "缓存键不存在"),
    CACHE_EXPIRED(9003, "缓存已过期"),
    
    // 消息队列错误 10xxx
    MQ_ERROR(10001, "消息队列操作失败"),
    MQ_PRODUCER_ERROR(10002, "消息发送失败"),
    MQ_CONSUMER_ERROR(10003, "消息消费失败"),
    MQ_QUEUE_NOT_FOUND(10004, "消息队列不存在");

    private final int code;
    private final String message;

    ResultCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    /**
     * 根据错误码获取错误信息
     */
    public static String getMessageByCode(int code) {
        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.getCode() == code) {
                return resultCode.getMessage();
            }
        }
        return "未知错误";
    }

    /**
     * 根据错误码获取枚举
     */
    public static ResultCode getByCode(int code) {
        for (ResultCode resultCode : ResultCode.values()) {
            if (resultCode.getCode() == code) {
                return resultCode;
            }
        }
        return SYSTEM_ERROR;
    }
}
