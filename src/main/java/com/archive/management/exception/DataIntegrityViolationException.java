package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;

/**
 * 数据完整性违反异常类
 * 用于处理数据库约束违反、重复数据等情况
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class DataIntegrityViolationException extends BaseException {

    /**
     * 违反类型
     */
    private String violationType;

    /**
     * 相关字段
     */
    private String field;

    /**
     * 相关值
     */
    private Object value;

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     */
    public DataIntegrityViolationException(ResponseCode responseCode) {
        super(responseCode);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     */
    public DataIntegrityViolationException(ResponseCode responseCode, String message) {
        super(responseCode, message);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param data 详细数据
     */
    public DataIntegrityViolationException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public DataIntegrityViolationException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
    }

    /**
     * 构造函数
     *
     * @param responseCode 响应码
     * @param message 自定义错误消息
     * @param cause 原因异常
     */
    public DataIntegrityViolationException(ResponseCode responseCode, String message, Throwable cause) {
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
    public DataIntegrityViolationException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param message 错误消息
     */
    public DataIntegrityViolationException(String message) {
        super(ResponseCode.DATABASE_OPERATION_ERROR, message);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param violationType 违反类型
     * @param field 相关字段
     * @param value 相关值
     */
    public DataIntegrityViolationException(String violationType, String field, Object value) {
        super(ResponseCode.DATABASE_OPERATION_ERROR, String.format("数据完整性违反: %s - 字段: %s, 值: %s", violationType, field, value));
        this.violationType = violationType;
        this.field = field;
        this.value = value;
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param message 错误消息
     * @param cause 原因
     */
    public DataIntegrityViolationException(String message, Throwable cause) {
        super(ResponseCode.DATABASE_OPERATION_ERROR, message, cause);
    }

    /**
     * 构造函数（兼容性）
     * 
     * @param violationType 违反类型
     * @param field 相关字段
     * @param value 相关值
     * @param cause 原因
     */
    public DataIntegrityViolationException(String violationType, String field, Object value, Throwable cause) {
        super(ResponseCode.DATABASE_OPERATION_ERROR, String.format("数据完整性违反: %s - 字段: %s, 值: %s", violationType, field, value), cause);
        this.violationType = violationType;
        this.field = field;
        this.value = value;
    }

    // 常用静态方法

    /**
     * 重复键异常
     * 
     * @param field 字段名
     * @param value 重复值
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException duplicateKey(String field, Object value) {
        return new DataIntegrityViolationException("重复键", field, value);
    }

    /**
     * 外键约束违反异常
     * 
     * @param field 字段名
     * @param value 值
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException foreignKeyViolation(String field, Object value) {
        return new DataIntegrityViolationException("外键约束违反", field, value);
    }

    /**
     * 非空约束违反异常
     * 
     * @param field 字段名
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException notNullViolation(String field) {
        return new DataIntegrityViolationException("非空约束违反", field, null);
    }

    /**
     * 唯一约束违反异常
     * 
     * @param field 字段名
     * @param value 重复值
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException uniqueViolation(String field, Object value) {
        return new DataIntegrityViolationException("唯一约束违反", field, value);
    }

    /**
     * 检查约束违反异常
     * 
     * @param field 字段名
     * @param value 值
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException checkViolation(String field, Object value) {
        return new DataIntegrityViolationException("检查约束违反", field, value);
    }

    /**
     * 数据长度超限异常
     * 
     * @param field 字段名
     * @param value 值
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException dataLengthExceeded(String field, Object value) {
        return new DataIntegrityViolationException("数据长度超限", field, value);
    }

    /**
     * 数据类型不匹配异常
     * 
     * @param field 字段名
     * @param value 值
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException dataTypeMismatch(String field, Object value) {
        return new DataIntegrityViolationException("数据类型不匹配", field, value);
    }

    /**
     * 引用完整性违反异常
     * 
     * @param field 字段名
     * @param value 值
     * @return DataIntegrityViolationException
     */
    public static DataIntegrityViolationException referentialIntegrityViolation(String field, Object value) {
        return new DataIntegrityViolationException("引用完整性违反", field, value);
    }

    // Getter 和 Setter 方法

    public String getViolationType() {
        return violationType;
    }

    public void setViolationType(String violationType) {
        this.violationType = violationType;
    }

    public String getField() {
        return field;
    }

    public void setField(String field) {
        this.field = field;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "DataIntegrityViolationException{" +
                "violationType='" + violationType + '\'' +
                ", field='" + field + '\'' +
                ", value=" + value +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}