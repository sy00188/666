package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 数据库异常类
 * 用于处理数据库连接、操作、事务等相关异常
 * 
 * @author Archive Management System
 * @since 1.0.0
 */
public class DatabaseException extends BaseException {

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     */
    public DatabaseException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public DatabaseException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public DatabaseException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public DatabaseException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public DatabaseException(ResponseCode responseCode, String message, Throwable cause) {
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
    public DatabaseException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    // 静态工厂方法，用于快速创建常见的数据库异常

    /**
     * 数据库连接失败异常
     *
     * @return DatabaseException
     */
    public static DatabaseException connectionError() {
        return new DatabaseException(ResponseCode.DATABASE_CONNECTION_ERROR);
    }

    /**
     * 数据库连接失败异常
     *
     * @param cause 原因异常
     * @return DatabaseException
     */
    public static DatabaseException connectionError(Throwable cause) {
        return new DatabaseException(ResponseCode.DATABASE_CONNECTION_ERROR, cause);
    }

    /**
     * 数据库操作失败异常
     *
     * @return DatabaseException
     */
    public static DatabaseException operationError() {
        return new DatabaseException(ResponseCode.DATABASE_OPERATION_ERROR);
    }

    /**
     * 数据库操作失败异常
     *
     * @param message 自定义错误消息
     * @return DatabaseException
     */
    public static DatabaseException operationError(String message) {
        return new DatabaseException(ResponseCode.DATABASE_OPERATION_ERROR, message);
    }

    /**
     * 数据库操作失败异常
     *
     * @param cause 原因异常
     * @return DatabaseException
     */
    public static DatabaseException operationError(Throwable cause) {
        return new DatabaseException(ResponseCode.DATABASE_OPERATION_ERROR, cause);
    }

    /**
     * 事务处理失败异常
     *
     * @return DatabaseException
     */
    public static DatabaseException transactionError() {
        return new DatabaseException(ResponseCode.TRANSACTION_ERROR);
    }

    /**
     * 事务处理失败异常
     *
     * @param cause 原因异常
     * @return DatabaseException
     */
    public static DatabaseException transactionError(Throwable cause) {
        return new DatabaseException(ResponseCode.TRANSACTION_ERROR, cause);
    }

    /**
     * SQL执行异常
     *
     * @return DatabaseException
     */
    public static DatabaseException sqlExecutionError() {
        return new DatabaseException(ResponseCode.SQL_EXECUTION_ERROR);
    }

    /**
     * SQL执行异常
     *
     * @param sql SQL语句
     * @return DatabaseException
     */
    public static DatabaseException sqlExecutionError(String sql) {
        return new DatabaseException(ResponseCode.SQL_EXECUTION_ERROR, "SQL执行失败: " + sql);
    }

    /**
     * SQL执行异常
     *
     * @param sql SQL语句
     * @param cause 原因异常
     * @return DatabaseException
     */
    public static DatabaseException sqlExecutionError(String sql, Throwable cause) {
        return new DatabaseException(ResponseCode.SQL_EXECUTION_ERROR, "SQL执行失败: " + sql, cause);
    }
}