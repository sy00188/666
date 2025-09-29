package com.archive.management.exception;

import com.archive.management.common.enums.ResponseCode;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

/**
 * 参数验证异常
 * 
 * @author archive-management
 * @since 1.0.0
 */
public class ValidationException extends BaseException {

    private List<ValidationError> errors;
    private String field;
    private Object value;
    private String validationType;
    private Map<String, List<String>> errorMap;

    // 基础构造函数
    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     */
    public ValidationException(ResponseCode responseCode) {
        super(responseCode);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 异常消息
     */
    public ValidationException(ResponseCode responseCode, String message) {
        super(responseCode, message);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 异常消息
     * @param data 异常数据
     */
    public ValidationException(ResponseCode responseCode, String message, Object data) {
        super(responseCode, message, data);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param cause 原因异常
     */
    public ValidationException(ResponseCode responseCode, Throwable cause) {
        super(responseCode, cause);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ValidationException(ResponseCode responseCode, String message, Throwable cause) {
        super(responseCode, message, cause);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    /**
     * 构造函数
     * 
     * @param responseCode 响应码
     * @param message 异常消息
     * @param data 异常数据
     * @param cause 原因异常
     */
    public ValidationException(ResponseCode responseCode, String message, Object data, Throwable cause) {
        super(responseCode, message, data, cause);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    // 兼容性构造函数
    /**
     * 兼容性构造函数 - 仅消息
     * 
     * @param message 异常消息
     */
    public ValidationException(String message) {
        super(ResponseCode.PARAM_ERROR, message);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    /**
     * 兼容性构造函数 - 消息和原因
     * 
     * @param message 异常消息
     * @param cause 原因
     */
    public ValidationException(String message, Throwable cause) {
        super(ResponseCode.PARAM_ERROR, message, cause);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    /**
     * 兼容性构造函数 - 单个验证错误
     * 
     * @param field 字段名
     * @param value 字段值
     * @param errorType 错误类型
     */
    public ValidationException(String field, Object value, String errorType) {
        super(ResponseCode.PARAM_ERROR, "参数验证失败: " + field);
        this.errors = new ArrayList<>();
        this.errorMap = new HashMap<>();
        this.field = field;
        this.value = value;
        this.validationType = errorType;
        this.errors.add(new ValidationError(field, value, errorType));
    }

    /**
     * 兼容性构造函数 - 多个验证错误
     * 
     * @param errors 验证错误列表
     */
    public ValidationException(List<ValidationError> errors) {
        super(ResponseCode.PARAM_ERROR, "参数验证失败");
        this.errors = errors != null ? new ArrayList<>(errors) : new ArrayList<>();
        this.errorMap = new HashMap<>();
    }

    // 常用静态方法

    /**
     * 必填字段为空异常
     * 
     * @param field 字段名
     * @return ValidationException
     */
    public static ValidationException required(String field) {
        return new ValidationException(field, null, "REQUIRED");
    }

    /**
     * 字段长度超限异常
     * 
     * @param field 字段名
     * @param maxLength 最大长度
     * @return ValidationException
     */
    public static ValidationException maxLength(String field, int maxLength) {
        return new ValidationException(field, null, "MAX_LENGTH");
    }

    /**
     * 字段长度不足异常
     * 
     * @param field 字段名
     * @param minLength 最小长度
     * @return ValidationException
     */
    public static ValidationException minLength(String field, int minLength) {
        return new ValidationException(field, null, "MIN_LENGTH");
    }

    /**
     * 邮箱格式错误异常
     * 
     * @param field 字段名
     * @param email 邮箱值
     * @return ValidationException
     */
    public static ValidationException invalidEmail(String field, String email) {
        return new ValidationException(field, email, "INVALID_EMAIL");
    }

    /**
     * 手机号格式错误异常
     * 
     * @param field 字段名
     * @param phone 手机号值
     * @return ValidationException
     */
    public static ValidationException invalidPhone(String field, String phone) {
        return new ValidationException(field, phone, "INVALID_PHONE");
    }

    /**
     * 数值范围错误异常
     * 
     * @param field 字段名
     * @param value 数值
     * @param min 最小值
     * @param max 最大值
     * @return ValidationException
     */
    public static ValidationException outOfRange(String field, Object value, Object min, Object max) {
        return new ValidationException(field, value, "OUT_OF_RANGE");
    }

    /**
     * 正则表达式验证失败异常
     * 
     * @param field 字段名
     * @param value 字段值
     * @param pattern 正则表达式
     * @return ValidationException
     */
    public static ValidationException patternMismatch(String field, Object value, String pattern) {
        return new ValidationException(field, value, "PATTERN_MISMATCH");
    }

    /**
     * 枚举值无效异常
     * 
     * @param field 字段名
     * @param value 字段值
     * @param validValues 有效值列表
     * @return ValidationException
     */
    public static ValidationException invalidEnum(String field, Object value, List<String> validValues) {
        return new ValidationException(field, value, "INVALID_ENUM");
    }

    /**
     * 日期格式错误异常
     * 
     * @param field 字段名
     * @param value 日期值
     * @return ValidationException
     */
    public static ValidationException invalidDate(String field, Object value) {
        return new ValidationException(field, value, "INVALID_DATE");
    }

    /**
     * 日期范围错误异常
     * 
     * @param field 字段名
     * @param value 日期值
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return ValidationException
     */
    public static ValidationException dateOutOfRange(String field, Object value, Object startDate, Object endDate) {
        return new ValidationException(field, value, "DATE_OUT_OF_RANGE");
    }

    /**
     * 重复值异常
     * 
     * @param field 字段名
     * @param value 重复值
     * @return ValidationException
     */
    public static ValidationException duplicateValue(String field, Object value) {
        return new ValidationException(field, value, "DUPLICATE_VALUE");
    }

    /**
     * 添加验证错误
     * 
     * @param field 字段名
     * @param error 错误消息
     */
    public void addError(String field, String error) {
        this.errorMap.computeIfAbsent(field, k -> new ArrayList<>()).add(error);
    }

    /**
     * 是否有验证错误
     * 
     * @return boolean
     */
    public boolean hasErrors() {
        return !this.errors.isEmpty() || !this.errorMap.isEmpty();
    }

    /**
     * 获取字段的所有错误
     * 
     * @param field 字段名
     * @return List<String>
     */
    public List<String> getFieldErrors(String field) {
        return this.errorMap.getOrDefault(field, new ArrayList<>());
    }

    /**
     * 获取所有错误字段
     * 
     * @return Set<String>
     */
    public java.util.Set<String> getErrorFields() {
        return this.errorMap.keySet();
    }

    /**
     * 清空所有错误
     */
    public void clearErrors() {
        this.errors.clear();
        this.errorMap.clear();
    }

    // Getter 和 Setter 方法

    public List<ValidationError> getErrors() {
        return errors;
    }

    public void setErrors(List<ValidationError> errors) {
        this.errors = errors != null ? errors : new ArrayList<>();
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

    public String getValidationType() {
        return validationType;
    }

    public void setValidationType(String validationType) {
        this.validationType = validationType;
    }

    public Map<String, List<String>> getErrorMap() {
        return errorMap;
    }

    public void setErrorMap(Map<String, List<String>> errorMap) {
        this.errorMap = errorMap != null ? errorMap : new HashMap<>();
    }

    @Override
    public String toString() {
        return "ValidationException{" +
                "field='" + field + '\'' +
                ", value=" + value +
                ", validationType='" + validationType + '\'' +
                ", errors=" + errors +
                ", errorMap=" + errorMap +
                ", message='" + getMessage() + '\'' +
                '}';
    }
}