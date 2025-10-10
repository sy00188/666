package com.archive.management.util;

import java.util.regex.Pattern;
import java.util.Collection;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * 数据验证工具类
 * 提供各种数据验证功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class ValidationUtil {

    // ========== 常用正则表达式 ==========
    
    /** 邮箱正则 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );
    
    /** 手机号正则（中国大陆） */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
    );
    
    /** 身份证号正则（18位） */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"
    );
    
    /** IP地址正则 */
    private static final Pattern IP_PATTERN = Pattern.compile(
        "^((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)$"
    );
    
    /** URL正则 */
    private static final Pattern URL_PATTERN = Pattern.compile(
        "^(https?|ftp)://[^\\s/$.?#].[^\\s]*$"
    );
    
    /** 中文字符正则 */
    private static final Pattern CHINESE_PATTERN = Pattern.compile(
        "[\\u4e00-\\u9fa5]+"
    );
    
    /** 数字正则 */
    private static final Pattern NUMBER_PATTERN = Pattern.compile(
        "^-?\\d+(\\.\\d+)?$"
    );
    
    /** 整数正则 */
    private static final Pattern INTEGER_PATTERN = Pattern.compile(
        "^-?\\d+$"
    );
    
    /** 正整数正则 */
    private static final Pattern POSITIVE_INTEGER_PATTERN = Pattern.compile(
        "^[1-9]\\d*$"
    );

    // ========== 基础验证 ==========

    /**
     * 检查对象是否为空
     * 
     * @param obj 对象
     * @return 是否为空
     */
    public static boolean isEmpty(Object obj) {
        if (obj == null) {
            return true;
        }
        if (obj instanceof String) {
            return ((String) obj).trim().isEmpty();
        }
        if (obj instanceof Collection) {
            return ((Collection<?>) obj).isEmpty();
        }
        if (obj instanceof Map) {
            return ((Map<?, ?>) obj).isEmpty();
        }
        if (obj.getClass().isArray()) {
            return ((Object[]) obj).length == 0;
        }
        return false;
    }

    /**
     * 检查对象是否不为空
     * 
     * @param obj 对象
     * @return 是否不为空
     */
    public static boolean isNotEmpty(Object obj) {
        return !isEmpty(obj);
    }

    /**
     * 检查字符串是否为空白
     * 
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 检查字符串是否不为空白
     * 
     * @param str 字符串
     * @return 是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    // ========== 格式验证 ==========

    /**
     * 验证邮箱格式
     * 
     * @param email 邮箱
     * @return 是否有效
     */
    public static boolean isValidEmail(String email) {
        return isNotBlank(email) && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     * 
     * @param phone 手机号
     * @return 是否有效
     */
    public static boolean isValidPhone(String phone) {
        return isNotBlank(phone) && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证身份证号格式
     * 
     * @param idCard 身份证号
     * @return 是否有效
     */
    public static boolean isValidIdCard(String idCard) {
        if (isBlank(idCard)) {
            return false;
        }
        
        if (!ID_CARD_PATTERN.matcher(idCard).matches()) {
            return false;
        }
        
        // 验证校验码
        return validateIdCardChecksum(idCard);
    }

    /**
     * 验证身份证校验码
     * 
     * @param idCard 身份证号
     * @return 是否有效
     */
    private static boolean validateIdCardChecksum(String idCard) {
        int[] weights = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        char[] checksums = {'1', '0', 'X', '9', '8', '7', '6', '5', '4', '3', '2'};
        
        int sum = 0;
        for (int i = 0; i < 17; i++) {
            sum += Character.getNumericValue(idCard.charAt(i)) * weights[i];
        }
        
        char expectedChecksum = checksums[sum % 11];
        char actualChecksum = Character.toUpperCase(idCard.charAt(17));
        
        return expectedChecksum == actualChecksum;
    }

    /**
     * 验证IP地址格式
     * 
     * @param ip IP地址
     * @return 是否有效
     */
    public static boolean isValidIp(String ip) {
        return isNotBlank(ip) && IP_PATTERN.matcher(ip).matches();
    }

    /**
     * 验证URL格式
     * 
     * @param url URL
     * @return 是否有效
     */
    public static boolean isValidUrl(String url) {
        return isNotBlank(url) && URL_PATTERN.matcher(url).matches();
    }

    /**
     * 验证是否为中文
     * 
     * @param str 字符串
     * @return 是否为中文
     */
    public static boolean isChinese(String str) {
        return isNotBlank(str) && CHINESE_PATTERN.matcher(str).matches();
    }

    /**
     * 验证是否包含中文
     * 
     * @param str 字符串
     * @return 是否包含中文
     */
    public static boolean containsChinese(String str) {
        return isNotBlank(str) && CHINESE_PATTERN.matcher(str).find();
    }

    // ========== 数字验证 ==========

    /**
     * 验证是否为数字
     * 
     * @param str 字符串
     * @return 是否为数字
     */
    public static boolean isNumber(String str) {
        return isNotBlank(str) && NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 验证是否为整数
     * 
     * @param str 字符串
     * @return 是否为整数
     */
    public static boolean isInteger(String str) {
        return isNotBlank(str) && INTEGER_PATTERN.matcher(str).matches();
    }

    /**
     * 验证是否为正整数
     * 
     * @param str 字符串
     * @return 是否为正整数
     */
    public static boolean isPositiveInteger(String str) {
        return isNotBlank(str) && POSITIVE_INTEGER_PATTERN.matcher(str).matches();
    }

    /**
     * 验证数字范围
     * 
     * @param value 值
     * @param min 最小值
     * @param max 最大值
     * @return 是否在范围内
     */
    public static boolean isInRange(Number value, Number min, Number max) {
        if (value == null) {
            return false;
        }
        double val = value.doubleValue();
        double minVal = min != null ? min.doubleValue() : Double.MIN_VALUE;
        double maxVal = max != null ? max.doubleValue() : Double.MAX_VALUE;
        return val >= minVal && val <= maxVal;
    }

    // ========== 长度验证 ==========

    /**
     * 验证字符串长度
     * 
     * @param str 字符串
     * @param minLength 最小长度
     * @param maxLength 最大长度
     * @return 是否符合长度要求
     */
    public static boolean isValidLength(String str, int minLength, int maxLength) {
        if (str == null) {
            return minLength <= 0;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 验证集合大小
     * 
     * @param collection 集合
     * @param minSize 最小大小
     * @param maxSize 最大大小
     * @return 是否符合大小要求
     */
    public static boolean isValidSize(Collection<?> collection, int minSize, int maxSize) {
        if (collection == null) {
            return minSize <= 0;
        }
        int size = collection.size();
        return size >= minSize && size <= maxSize;
    }

    // ========== 日期验证 ==========

    /**
     * 验证日期格式
     * 
     * @param dateStr 日期字符串
     * @param pattern 日期格式
     * @return 是否有效
     */
    public static boolean isValidDate(String dateStr, String pattern) {
        if (isBlank(dateStr) || isBlank(pattern)) {
            return false;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDate.parse(dateStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证日期时间格式
     * 
     * @param dateTimeStr 日期时间字符串
     * @param pattern 日期时间格式
     * @return 是否有效
     */
    public static boolean isValidDateTime(String dateTimeStr, String pattern) {
        if (isBlank(dateTimeStr) || isBlank(pattern)) {
            return false;
        }
        
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            LocalDateTime.parse(dateTimeStr, formatter);
            return true;
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * 验证日期范围
     * 
     * @param date 日期
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 是否在范围内
     */
    public static boolean isDateInRange(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null) {
            return false;
        }
        
        if (startDate != null && date.isBefore(startDate)) {
            return false;
        }
        
        if (endDate != null && date.isAfter(endDate)) {
            return false;
        }
        
        return true;
    }

    // ========== 密码验证 ==========

    /**
     * 验证密码强度
     * 
     * @param password 密码
     * @return 密码强度等级（0-4）
     */
    public static int getPasswordStrength(String password) {
        if (isBlank(password)) {
            return 0;
        }
        
        int strength = 0;
        
        // 长度检查
        if (password.length() >= 8) {
            strength++;
        }
        
        // 包含小写字母
        if (password.matches(".*[a-z].*")) {
            strength++;
        }
        
        // 包含大写字母
        if (password.matches(".*[A-Z].*")) {
            strength++;
        }
        
        // 包含数字
        if (password.matches(".*\\d.*")) {
            strength++;
        }
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            strength++;
        }
        
        return strength;
    }

    /**
     * 验证密码是否符合要求
     * 
     * @param password 密码
     * @param minLength 最小长度
     * @param requireUppercase 是否需要大写字母
     * @param requireLowercase 是否需要小写字母
     * @param requireDigit 是否需要数字
     * @param requireSpecialChar 是否需要特殊字符
     * @return 是否符合要求
     */
    public static boolean isValidPassword(String password, int minLength, 
                                        boolean requireUppercase, boolean requireLowercase,
                                        boolean requireDigit, boolean requireSpecialChar) {
        if (isBlank(password)) {
            return false;
        }
        
        // 长度检查
        if (password.length() < minLength) {
            return false;
        }
        
        // 大写字母检查
        if (requireUppercase && !password.matches(".*[A-Z].*")) {
            return false;
        }
        
        // 小写字母检查
        if (requireLowercase && !password.matches(".*[a-z].*")) {
            return false;
        }
        
        // 数字检查
        if (requireDigit && !password.matches(".*\\d.*")) {
            return false;
        }
        
        // 特殊字符检查
        if (requireSpecialChar && !password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) {
            return false;
        }
        
        return true;
    }

    // ========== 自定义验证 ==========

    /**
     * 使用正则表达式验证
     * 
     * @param str 字符串
     * @param regex 正则表达式
     * @return 是否匹配
     */
    public static boolean matches(String str, String regex) {
        return isNotBlank(str) && isNotBlank(regex) && Pattern.matches(regex, str);
    }

    /**
     * 验证字符串是否只包含指定字符
     * 
     * @param str 字符串
     * @param allowedChars 允许的字符
     * @return 是否只包含指定字符
     */
    public static boolean containsOnly(String str, String allowedChars) {
        if (isBlank(str) || isBlank(allowedChars)) {
            return false;
        }
        
        for (char c : str.toCharArray()) {
            if (allowedChars.indexOf(c) == -1) {
                return false;
            }
        }
        
        return true;
    }

    // ========== 批量验证 ==========

    /**
     * 验证结果
     */
    public static class ValidationResult {
        private boolean valid;
        private List<String> errors;

        public ValidationResult() {
            this.valid = true;
            this.errors = new ArrayList<>();
        }

        public boolean isValid() {
            return valid;
        }

        public List<String> getErrors() {
            return errors;
        }

        public void addError(String error) {
            this.valid = false;
            this.errors.add(error);
        }

        public void addErrors(List<String> errors) {
            this.valid = false;
            this.errors.addAll(errors);
        }

        @Override
        public String toString() {
            return "ValidationResult{" +
                    "valid=" + valid +
                    ", errors=" + errors +
                    '}';
        }
    }

    /**
     * 创建验证结果
     * 
     * @return 验证结果
     */
    public static ValidationResult createValidationResult() {
        return new ValidationResult();
    }

    /**
     * 验证必填字段
     * 
     * @param result 验证结果
     * @param value 值
     * @param fieldName 字段名
     */
    public static void validateRequired(ValidationResult result, Object value, String fieldName) {
        if (isEmpty(value)) {
            result.addError(fieldName + "不能为空");
        }
    }

    /**
     * 验证字符串长度
     * 
     * @param result 验证结果
     * @param value 值
     * @param fieldName 字段名
     * @param minLength 最小长度
     * @param maxLength 最大长度
     */
    public static void validateLength(ValidationResult result, String value, String fieldName, 
                                    int minLength, int maxLength) {
        if (value != null && !isValidLength(value, minLength, maxLength)) {
            result.addError(fieldName + "长度必须在" + minLength + "-" + maxLength + "之间");
        }
    }

    /**
     * 验证邮箱
     * 
     * @param result 验证结果
     * @param email 邮箱
     * @param fieldName 字段名
     */
    public static void validateEmail(ValidationResult result, String email, String fieldName) {
        if (isNotBlank(email) && !isValidEmail(email)) {
            result.addError(fieldName + "格式不正确");
        }
    }

    /**
     * 验证手机号
     * 
     * @param result 验证结果
     * @param phone 手机号
     * @param fieldName 字段名
     */
    public static void validatePhone(ValidationResult result, String phone, String fieldName) {
        if (isNotBlank(phone) && !isValidPhone(phone)) {
            result.addError(fieldName + "格式不正确");
        }
    }

    /**
     * 验证数字范围
     * 
     * @param result 验证结果
     * @param value 值
     * @param fieldName 字段名
     * @param min 最小值
     * @param max 最大值
     */
    public static void validateRange(ValidationResult result, Number value, String fieldName, 
                                   Number min, Number max) {
        if (value != null && !isInRange(value, min, max)) {
            result.addError(fieldName + "必须在" + min + "-" + max + "之间");
        }
    }

    /**
     * 验证对象不为null
     * 
     * @param result 验证结果
     * @param value 值
     * @param message 错误消息
     */
    public static void validateNotNull(ValidationResult result, Object value, String message) {
        if (value == null) {
            result.addError(message);
        }
    }

    /**
     * 验证字符串不为空白
     * 
     * @param result 验证结果
     * @param value 值
     * @param message 错误消息
     */
    public static void validateNotBlank(ValidationResult result, String value, String message) {
        if (isBlank(value)) {
            result.addError(message);
        }
    }

    /**
     * 如果有错误则抛出异常
     * 
     * @param result 验证结果
     * @throws IllegalArgumentException 如果有验证错误
     */
    public static void throwIfHasErrors(ValidationResult result) {
        if (!result.isValid()) {
            throw new IllegalArgumentException(String.join(", ", result.getErrors()));
        }
    }

    // ========== 工具方法 ==========

    /**
     * 断言对象不为空
     * 
     * @param obj 对象
     * @param message 错误消息
     * @throws IllegalArgumentException 如果对象为空
     */
    public static void assertNotEmpty(Object obj, String message) {
        if (isEmpty(obj)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言字符串不为空白
     * 
     * @param str 字符串
     * @param message 错误消息
     * @throws IllegalArgumentException 如果字符串为空白
     */
    public static void assertNotBlank(String str, String message) {
        if (isBlank(str)) {
            throw new IllegalArgumentException(message);
        }
    }

    /**
     * 断言条件为真
     * 
     * @param condition 条件
     * @param message 错误消息
     * @throws IllegalArgumentException 如果条件为假
     */
    public static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new IllegalArgumentException(message);
        }
    }
}