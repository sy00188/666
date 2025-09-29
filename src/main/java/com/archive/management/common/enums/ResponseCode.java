package com.archive.management.common.enums;

/**
 * 响应状态码枚举
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public enum ResponseCode {

    /**
     * 成功
     */
    SUCCESS(200, "操作成功"),

    /**
     * 系统错误
     */
    SYSTEM_ERROR(500, "系统内部错误"),

    /**
     * 参数错误
     */
    PARAM_ERROR(400, "参数错误"),

    /**
     * 未授权
     */
    UNAUTHORIZED(401, "未授权访问"),

    /**
     * 禁止访问
     */
    FORBIDDEN(403, "禁止访问"),

    /**
     * 资源未找到
     */
    NOT_FOUND(404, "资源未找到"),

    /**
     * 方法不允许
     */
    METHOD_NOT_ALLOWED(405, "请求方法不允许"),

    /**
     * 请求超时
     */
    REQUEST_TIMEOUT(408, "请求超时"),

    /**
     * 数据冲突
     */
    CONFLICT(409, "数据冲突"),

    /**
     * 请求实体过大
     */
    PAYLOAD_TOO_LARGE(413, "请求实体过大"),

    /**
     * 请求过于频繁
     */
    TOO_MANY_REQUESTS(429, "请求过于频繁"),

    // 业务相关错误码 (1000-1999)
    /**
     * 用户不存在
     */
    USER_NOT_FOUND(1001, "用户不存在"),

    /**
     * 用户名或密码错误
     */
    INVALID_CREDENTIALS(1002, "用户名或密码错误"),

    /**
     * 用户已被禁用
     */
    USER_DISABLED(1003, "用户已被禁用"),

    /**
     * 用户名已存在
     */
    USERNAME_EXISTS(1004, "用户名已存在"),

    /**
     * 邮箱已存在
     */
    EMAIL_EXISTS(1005, "邮箱已存在"),

    /**
     * 手机号已存在
     */
    PHONE_EXISTS(1006, "手机号已存在"),

    /**
     * 验证码错误
     */
    INVALID_CAPTCHA(1007, "验证码错误"),

    /**
     * 验证码已过期
     */
    CAPTCHA_EXPIRED(1008, "验证码已过期"),

    /**
     * Token无效
     */
    INVALID_TOKEN(1009, "Token无效"),

    /**
     * Token已过期
     */
    TOKEN_EXPIRED(1010, "Token已过期"),

    /**
     * 权限不足
     */
    INSUFFICIENT_PERMISSIONS(1011, "权限不足"),

    // 档案相关错误码 (2000-2999)
    /**
     * 档案不存在
     */
    ARCHIVE_NOT_FOUND(2001, "档案不存在"),

    /**
     * 档案编号已存在
     */
    ARCHIVE_CODE_EXISTS(2002, "档案编号已存在"),

    /**
     * 档案状态不允许此操作
     */
    ARCHIVE_STATUS_NOT_ALLOWED(2003, "档案状态不允许此操作"),

    /**
     * 档案正在被借阅
     */
    ARCHIVE_IN_BORROWING(2004, "档案正在被借阅"),

    /**
     * 档案分类不存在
     */
    CATEGORY_NOT_FOUND(2005, "档案分类不存在"),

    /**
     * 分类编码已存在
     */
    CATEGORY_CODE_EXISTS(2006, "分类编码已存在"),

    /**
     * 分类下存在档案，无法删除
     */
    CATEGORY_HAS_ARCHIVES(2007, "分类下存在档案，无法删除"),

    // 借阅相关错误码 (3000-3999)
    /**
     * 借阅记录不存在
     */
    BORROW_RECORD_NOT_FOUND(3001, "借阅记录不存在"),

    /**
     * 档案已被借出
     */
    ARCHIVE_ALREADY_BORROWED(3002, "档案已被借出"),

    /**
     * 借阅申请已存在
     */
    BORROW_APPLICATION_EXISTS(3003, "借阅申请已存在"),

    /**
     * 借阅申请状态不允许此操作
     */
    BORROW_STATUS_NOT_ALLOWED(3004, "借阅申请状态不允许此操作"),

    /**
     * 超出借阅期限
     */
    BORROW_OVERDUE(3005, "超出借阅期限"),

    /**
     * 借阅数量超出限制
     */
    BORROW_LIMIT_EXCEEDED(3006, "借阅数量超出限制"),

    // 文件相关错误码 (4000-4999)
    /**
     * 文件不存在
     */
    FILE_NOT_FOUND(4001, "文件不存在"),

    /**
     * 文件格式不支持
     */
    FILE_FORMAT_NOT_SUPPORTED(4002, "文件格式不支持"),

    /**
     * 文件大小超出限制
     */
    FILE_SIZE_EXCEEDED(4003, "文件大小超出限制"),

    /**
     * 文件上传失败
     */
    FILE_UPLOAD_FAILED(4004, "文件上传失败"),

    /**
     * 文件下载失败
     */
    FILE_DOWNLOAD_FAILED(4005, "文件下载失败"),

    /**
     * 文件删除失败
     */
    FILE_DELETE_FAILED(4006, "文件删除失败"),

    // 部门相关错误码 (5000-5999)
    /**
     * 部门不存在
     */
    DEPARTMENT_NOT_FOUND(5001, "部门不存在"),

    /**
     * 部门编码已存在
     */
    DEPARTMENT_CODE_EXISTS(5002, "部门编码已存在"),

    /**
     * 部门下存在子部门，无法删除
     */
    DEPARTMENT_HAS_CHILDREN(5003, "部门下存在子部门，无法删除"),

    /**
     * 部门下存在用户，无法删除
     */
    DEPARTMENT_HAS_USERS(5004, "部门下存在用户，无法删除"),

    // 角色相关错误码 (6000-6999)
    /**
     * 角色不存在
     */
    ROLE_NOT_FOUND(6001, "角色不存在"),

    /**
     * 角色编码已存在
     */
    ROLE_CODE_EXISTS(6002, "角色编码已存在"),

    /**
     * 角色已被分配，无法删除
     */
    ROLE_ASSIGNED(6003, "角色已被分配，无法删除"),

    /**
     * 权限不存在
     */
    PERMISSION_NOT_FOUND(6004, "权限不存在"),

    // 系统级错误码 (7000-7999)
    /**
     * 数据库连接失败
     */
    DATABASE_CONNECTION_ERROR(7001, "数据库连接失败"),

    /**
     * 数据库操作失败
     */
    DATABASE_OPERATION_ERROR(7002, "数据库操作失败"),

    /**
     * 事务处理失败
     */
    TRANSACTION_ERROR(7003, "事务处理失败"),

    /**
     * SQL执行异常
     */
    SQL_EXECUTION_ERROR(7004, "SQL执行异常"),

    /**
     * 缓存操作失败
     */
    CACHE_OPERATION_ERROR(7101, "缓存操作失败"),

    /**
     * 缓存连接失败
     */
    CACHE_CONNECTION_ERROR(7102, "缓存连接失败"),

    /**
     * 缓存数据过期
     */
    CACHE_DATA_EXPIRED(7103, "缓存数据过期"),

    /**
     * 消息队列连接失败
     */
    MESSAGE_QUEUE_CONNECTION_ERROR(7201, "消息队列连接失败"),

    /**
     * 消息发送失败
     */
    MESSAGE_SEND_ERROR(7202, "消息发送失败"),

    /**
     * 消息消费失败
     */
    MESSAGE_CONSUME_ERROR(7203, "消息消费失败"),

    /**
     * 外部服务调用失败
     */
    EXTERNAL_SERVICE_ERROR(7301, "外部服务调用失败"),

    /**
     * 外部服务超时
     */
    EXTERNAL_SERVICE_TIMEOUT(7302, "外部服务调用超时"),

    /**
     * 外部服务不可用
     */
    EXTERNAL_SERVICE_UNAVAILABLE(7303, "外部服务不可用"),

    /**
     * 网络连接异常
     */
    NETWORK_ERROR(7401, "网络连接异常"),

    /**
     * 配置错误
     */
    CONFIG_ERROR(7501, "系统配置错误"),

    /**
     * 加密解密失败
     */
    ENCRYPTION_ERROR(7502, "加密解密失败"),

    /**
     * 序列化失败
     */
    SERIALIZATION_ERROR(7503, "数据序列化失败"),

    /**
     * 反序列化失败
     */
    DESERIALIZATION_ERROR(7504, "数据反序列化失败");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}