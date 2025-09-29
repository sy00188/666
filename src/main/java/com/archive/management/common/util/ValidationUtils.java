package com.archive.management.common.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
public final class ValidationUtils {

    private ValidationUtils() {
        throw new UnsupportedOperationException("Utility class");
    }

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$"
    );

    /**
     * 手机号正则表达式（中国大陆）
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
        "^1[3-9]\\d{9}$"
    );

    /**
     * 身份证号正则表达式（18位）
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
        "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$"
    );

    /**
     * 用户名正则表达式（4-20位字母、数字、下划线）
     */
    private static final Pattern USERNAME_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_]{4,20}$"
    );

    /**
     * 密码正则表达式（8-20位，包含字母和数字）
     */
    private static final Pattern PASSWORD_PATTERN = Pattern.compile(
        "^(?=.*[a-zA-Z])(?=.*\\d)[a-zA-Z\\d@$!%*?&]{8,20}$"
    );

    /**
     * 档案编号正则表达式（字母数字组合）
     */
    private static final Pattern ARCHIVE_CODE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{6,20}$"
    );

    /**
     * 部门编码正则表达式（字母数字组合）
     */
    private static final Pattern DEPARTMENT_CODE_PATTERN = Pattern.compile(
        "^[A-Z0-9]{2,10}$"
    );

    /**
     * 验证邮箱格式
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证手机号格式
     */
    public static boolean isValidPhone(String phone) {
        return phone != null && PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证身份证号格式
     */
    public static boolean isValidIdCard(String idCard) {
        return idCard != null && ID_CARD_PATTERN.matcher(idCard).matches();
    }

    /**
     * 验证用户名格式
     */
    public static boolean isValidUsername(String username) {
        return username != null && USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 验证密码格式
     */
    public static boolean isValidPassword(String password) {
        return password != null && PASSWORD_PATTERN.matcher(password).matches();
    }

    /**
     * 验证档案编号格式
     */
    public static boolean isValidArchiveCode(String archiveCode) {
        return archiveCode != null && ARCHIVE_CODE_PATTERN.matcher(archiveCode).matches();
    }

    /**
     * 验证部门编码格式
     */
    public static boolean isValidDepartmentCode(String departmentCode) {
        return departmentCode != null && DEPARTMENT_CODE_PATTERN.matcher(departmentCode).matches();
    }

    /**
     * 验证字符串是否为空或null
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 验证字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 验证字符串长度是否在指定范围内
     */
    public static boolean isLengthValid(String str, int minLength, int maxLength) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        return length >= minLength && length <= maxLength;
    }

    /**
     * 验证数字是否在指定范围内
     */
    public static boolean isNumberInRange(Number number, Number min, Number max) {
        if (number == null || min == null || max == null) {
            return false;
        }
        double value = number.doubleValue();
        double minValue = min.doubleValue();
        double maxValue = max.doubleValue();
        return value >= minValue && value <= maxValue;
    }

    /**
     * 验证是否为正整数
     */
    public static boolean isPositiveInteger(Number number) {
        if (number == null) {
            return false;
        }
        return number.longValue() > 0;
    }

    /**
     * 验证是否为非负整数
     */
    public static boolean isNonNegativeInteger(Number number) {
        if (number == null) {
            return false;
        }
        return number.longValue() >= 0;
    }
}