package com.archive.management.exception;

/**
 * 验证错误详情类
 * 
 * @author archive-management
 * @since 1.0.0
 */
public class ValidationError {
    
    private String field;
    private Object value;
    private String errorType;
    private String message;

    /**
     * 构造函数
     */
    public ValidationError() {
    }

    /**
     * 构造函数
     * 
     * @param field 字段名
     * @param value 字段值
     * @param errorType 错误类型
     */
    public ValidationError(String field, Object value, String errorType) {
        this.field = field;
        this.value = value;
        this.errorType = errorType;
        this.message = generateMessage(field, value, errorType);
    }

    /**
     * 构造函数
     * 
     * @param field 字段名
     * @param value 字段值
     * @param errorType 错误类型
     * @param message 自定义错误消息
     */
    public ValidationError(String field, Object value, String errorType, String message) {
        this.field = field;
        this.value = value;
        this.errorType = errorType;
        this.message = message;
    }

    /**
     * 生成默认错误消息
     * 
     * @param field 字段名
     * @param value 字段值
     * @param errorType 错误类型
     * @return 错误消息
     */
    private String generateMessage(String field, Object value, String errorType) {
        switch (errorType) {
            case "REQUIRED":
                return String.format("字段 [%s] 不能为空", field);
            case "MAX_LENGTH":
                return String.format("字段 [%s] 长度超出限制", field);
            case "MIN_LENGTH":
                return String.format("字段 [%s] 长度不足", field);
            case "INVALID_EMAIL":
                return String.format("字段 [%s] 邮箱格式不正确", field);
            case "INVALID_PHONE":
                return String.format("字段 [%s] 手机号格式不正确", field);
            case "OUT_OF_RANGE":
                return String.format("字段 [%s] 数值超出范围", field);
            case "PATTERN_MISMATCH":
                return String.format("字段 [%s] 格式不符合要求", field);
            case "INVALID_ENUM":
                return String.format("字段 [%s] 枚举值无效", field);
            case "INVALID_DATE":
                return String.format("字段 [%s] 日期格式错误", field);
            case "DUPLICATE_VALUE":
                return String.format("字段 [%s] 值重复", field);
            default:
                return String.format("字段 [%s] 验证失败: %s", field, errorType);
        }
    }

    // Getter 和 Setter 方法

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

    public String getErrorType() {
        return errorType;
    }

    public void setErrorType(String errorType) {
        this.errorType = errorType;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "ValidationError{" +
                "field='" + field + '\'' +
                ", value=" + value +
                ", errorType='" + errorType + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}