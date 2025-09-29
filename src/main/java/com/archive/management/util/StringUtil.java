package com.archive.management.util;

import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 字符串工具类
 * 提供字符串相关的常用操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class StringUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private StringUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== 常用正则表达式 ==========
    
    /** 邮箱正则表达式 */
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    
    /** 手机号正则表达式（中国大陆） */
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^1[3-9]\\d{9}$");
    
    /** 身份证号正则表达式（中国大陆） */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile(
            "^[1-9]\\d{5}(18|19|20)\\d{2}((0[1-9])|(1[0-2]))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$");
    
    /** 数字正则表达式 */
    private static final Pattern NUMBER_PATTERN = Pattern.compile("^\\d+$");
    
    /** 字母正则表达式 */
    private static final Pattern LETTER_PATTERN = Pattern.compile("^[a-zA-Z]+$");
    
    /** 字母数字正则表达式 */
    private static final Pattern ALPHANUMERIC_PATTERN = Pattern.compile("^[a-zA-Z0-9]+$");

    // ========== 基础判断方法 ==========

    /**
     * 判断字符串是否为空（null或空字符串）
     * 
     * @param str 字符串
     * @return 是否为空
     */
    public static boolean isEmpty(String str) {
        return str == null || str.isEmpty();
    }

    /**
     * 判断字符串是否不为空
     * 
     * @param str 字符串
     * @return 是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 判断字符串是否为空白（null、空字符串或只包含空白字符）
     * 
     * @param str 字符串
     * @return 是否为空白
     */
    public static boolean isBlank(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 判断字符串是否不为空白
     * 
     * @param str 字符串
     * @return 是否不为空白
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断所有字符串是否都不为空
     * 
     * @param strings 字符串数组
     * @return 是否都不为空
     */
    public static boolean areAllNotEmpty(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        for (String str : strings) {
            if (isEmpty(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断所有字符串是否都不为空白
     * 
     * @param strings 字符串数组
     * @return 是否都不为空白
     */
    public static boolean areAllNotBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return false;
        }
        for (String str : strings) {
            if (isBlank(str)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 判断是否有任何字符串为空
     * 
     * @param strings 字符串数组
     * @return 是否有任何字符串为空
     */
    public static boolean hasEmpty(String... strings) {
        if (strings == null || strings.length == 0) {
            return true;
        }
        for (String str : strings) {
            if (isEmpty(str)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断是否有任何字符串为空白
     * 
     * @param strings 字符串数组
     * @return 是否有任何字符串为空白
     */
    public static boolean hasBlank(String... strings) {
        if (strings == null || strings.length == 0) {
            return true;
        }
        for (String str : strings) {
            if (isBlank(str)) {
                return true;
            }
        }
        return false;
    }

    // ========== 默认值方法 ==========

    /**
     * 如果字符串为空则返回默认值
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 字符串或默认值
     */
    public static String defaultIfEmpty(String str, String defaultValue) {
        return isEmpty(str) ? defaultValue : str;
    }

    /**
     * 如果字符串为空白则返回默认值
     * 
     * @param str 字符串
     * @param defaultValue 默认值
     * @return 字符串或默认值
     */
    public static String defaultIfBlank(String str, String defaultValue) {
        return isBlank(str) ? defaultValue : str;
    }

    /**
     * 如果字符串为空则返回空字符串
     * 
     * @param str 字符串
     * @return 字符串或空字符串
     */
    public static String defaultString(String str) {
        return defaultIfEmpty(str, "");
    }

    // ========== 字符串操作方法 ==========

    /**
     * 安全的trim操作，null返回null
     * 
     * @param str 字符串
     * @return trim后的字符串
     */
    public static String trim(String str) {
        return str == null ? null : str.trim();
    }

    /**
     * 安全的trim操作，null和空白字符串返回null
     * 
     * @param str 字符串
     * @return trim后的字符串或null
     */
    public static String trimToNull(String str) {
        String trimmed = trim(str);
        return isEmpty(trimmed) ? null : trimmed;
    }

    /**
     * 安全的trim操作，null返回空字符串
     * 
     * @param str 字符串
     * @return trim后的字符串或空字符串
     */
    public static String trimToEmpty(String str) {
        return str == null ? "" : str.trim();
    }

    /**
     * 截取字符串，超出长度用省略号表示
     * 
     * @param str 字符串
     * @param maxLength 最大长度
     * @return 截取后的字符串
     */
    public static String truncate(String str, int maxLength) {
        if (isEmpty(str) || maxLength <= 0) {
            return str;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    /**
     * 截取字符串，超出长度用指定后缀表示
     * 
     * @param str 字符串
     * @param maxLength 最大长度
     * @param suffix 后缀
     * @return 截取后的字符串
     */
    public static String truncate(String str, int maxLength, String suffix) {
        if (isEmpty(str) || maxLength <= 0) {
            return str;
        }
        if (str.length() <= maxLength) {
            return str;
        }
        suffix = defaultString(suffix);
        return str.substring(0, maxLength - suffix.length()) + suffix;
    }

    /**
     * 重复字符串
     * 
     * @param str 字符串
     * @param count 重复次数
     * @return 重复后的字符串
     */
    public static String repeat(String str, int count) {
        if (str == null || count <= 0) {
            return "";
        }
        return str.repeat(count);
    }

    /**
     * 左填充字符串到指定长度
     * 
     * @param str 字符串
     * @param size 目标长度
     * @param padChar 填充字符
     * @return 填充后的字符串
     */
    public static String leftPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return repeat(String.valueOf(padChar), pads) + str;
    }

    /**
     * 右填充字符串到指定长度
     * 
     * @param str 字符串
     * @param size 目标长度
     * @param padChar 填充字符
     * @return 填充后的字符串
     */
    public static String rightPad(String str, int size, char padChar) {
        if (str == null) {
            return null;
        }
        int pads = size - str.length();
        if (pads <= 0) {
            return str;
        }
        return str + repeat(String.valueOf(padChar), pads);
    }

    // ========== 字符串比较方法 ==========

    /**
     * 安全的字符串相等比较
     * 
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 是否相等
     */
    public static boolean equals(String str1, String str2) {
        return Objects.equals(str1, str2);
    }

    /**
     * 安全的字符串相等比较（忽略大小写）
     * 
     * @param str1 字符串1
     * @param str2 字符串2
     * @return 是否相等
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == str2) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }

    /**
     * 字符串是否包含指定子串
     * 
     * @param str 字符串
     * @param searchStr 搜索字符串
     * @return 是否包含
     */
    public static boolean contains(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.contains(searchStr);
    }

    /**
     * 字符串是否包含指定子串（忽略大小写）
     * 
     * @param str 字符串
     * @param searchStr 搜索字符串
     * @return 是否包含
     */
    public static boolean containsIgnoreCase(String str, String searchStr) {
        if (str == null || searchStr == null) {
            return false;
        }
        return str.toLowerCase().contains(searchStr.toLowerCase());
    }

    // ========== 字符串转换方法 ==========

    /**
     * 首字母大写
     * 
     * @param str 字符串
     * @return 首字母大写的字符串
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * 首字母小写
     * 
     * @param str 字符串
     * @return 首字母小写的字符串
     */
    public static String uncapitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toLowerCase() + str.substring(1);
    }

    /**
     * 驼峰命名转下划线命名
     * 
     * @param str 驼峰命名字符串
     * @return 下划线命名字符串
     */
    public static String camelToSnake(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }

    /**
     * 下划线命名转驼峰命名
     * 
     * @param str 下划线命名字符串
     * @return 驼峰命名字符串
     */
    public static String snakeToCamel(String str) {
        if (isEmpty(str)) {
            return str;
        }
        StringBuilder result = new StringBuilder();
        boolean capitalizeNext = false;
        
        for (char c : str.toCharArray()) {
            if (c == '_') {
                capitalizeNext = true;
            } else {
                if (capitalizeNext) {
                    result.append(Character.toUpperCase(c));
                    capitalizeNext = false;
                } else {
                    result.append(Character.toLowerCase(c));
                }
            }
        }
        
        return result.toString();
    }

    // ========== 集合操作方法 ==========

    /**
     * 将字符串数组连接成字符串
     * 
     * @param array 字符串数组
     * @param separator 分隔符
     * @return 连接后的字符串
     */
    public static String join(String[] array, String separator) {
        if (array == null || array.length == 0) {
            return "";
        }
        return String.join(separator, array);
    }

    /**
     * 将字符串集合连接成字符串
     * 
     * @param collection 字符串集合
     * @param separator 分隔符
     * @return 连接后的字符串
     */
    public static String join(Collection<String> collection, String separator) {
        if (collection == null || collection.isEmpty()) {
            return "";
        }
        return String.join(separator, collection);
    }

    /**
     * 分割字符串为列表
     * 
     * @param str 字符串
     * @param separator 分隔符
     * @return 字符串列表
     */
    public static List<String> split(String str, String separator) {
        if (isEmpty(str)) {
            return new ArrayList<>();
        }
        return Arrays.asList(str.split(Pattern.quote(separator)));
    }

    /**
     * 分割字符串为列表并去除空白
     * 
     * @param str 字符串
     * @param separator 分隔符
     * @return 字符串列表
     */
    public static List<String> splitAndTrim(String str, String separator) {
        if (isEmpty(str)) {
            return new ArrayList<>();
        }
        return Arrays.stream(str.split(Pattern.quote(separator)))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // ========== 验证方法 ==========

    /**
     * 验证是否为有效邮箱
     * 
     * @param email 邮箱字符串
     * @return 是否为有效邮箱
     */
    public static boolean isValidEmail(String email) {
        if (isEmpty(email)) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证是否为有效手机号（中国大陆）
     * 
     * @param phone 手机号字符串
     * @return 是否为有效手机号
     */
    public static boolean isValidPhone(String phone) {
        if (isEmpty(phone)) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证是否为有效身份证号（中国大陆）
     * 
     * @param idCard 身份证号字符串
     * @return 是否为有效身份证号
     */
    public static boolean isValidIdCard(String idCard) {
        if (isEmpty(idCard)) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard).matches();
    }

    /**
     * 验证是否为纯数字
     * 
     * @param str 字符串
     * @return 是否为纯数字
     */
    public static boolean isNumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return NUMBER_PATTERN.matcher(str).matches();
    }

    /**
     * 验证是否为纯字母
     * 
     * @param str 字符串
     * @return 是否为纯字母
     */
    public static boolean isAlpha(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return LETTER_PATTERN.matcher(str).matches();
    }

    /**
     * 验证是否为字母数字组合
     * 
     * @param str 字符串
     * @return 是否为字母数字组合
     */
    public static boolean isAlphanumeric(String str) {
        if (isEmpty(str)) {
            return false;
        }
        return ALPHANUMERIC_PATTERN.matcher(str).matches();
    }

    // ========== 编码方法 ==========

    /**
     * 生成随机字符串
     * 
     * @param length 长度
     * @return 随机字符串
     */
    public static String randomString(int length) {
        if (length <= 0) {
            return "";
        }
        
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return sb.toString();
    }

    /**
     * 生成随机数字字符串
     * 
     * @param length 长度
     * @return 随机数字字符串
     */
    public static String randomNumeric(int length) {
        if (length <= 0) {
            return "";
        }
        
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < length; i++) {
            sb.append(random.nextInt(10));
        }
        
        return sb.toString();
    }

    /**
     * 掩码处理（隐藏中间部分）
     * 
     * @param str 原字符串
     * @param start 开始保留位数
     * @param end 结束保留位数
     * @param maskChar 掩码字符
     * @return 掩码后的字符串
     */
    public static String mask(String str, int start, int end, char maskChar) {
        if (isEmpty(str) || start < 0 || end < 0) {
            return str;
        }
        
        int length = str.length();
        if (start + end >= length) {
            return str;
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(str, 0, start);
        
        int maskLength = length - start - end;
        for (int i = 0; i < maskLength; i++) {
            sb.append(maskChar);
        }
        
        sb.append(str.substring(length - end));
        return sb.toString();
    }

    /**
     * 手机号掩码处理
     * 
     * @param phone 手机号
     * @return 掩码后的手机号
     */
    public static String maskPhone(String phone) {
        return mask(phone, 3, 4, '*');
    }

    /**
     * 邮箱掩码处理
     * 
     * @param email 邮箱
     * @return 掩码后的邮箱
     */
    public static String maskEmail(String email) {
        if (isEmpty(email) || !email.contains("@")) {
            return email;
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.length() <= 2) {
            return email;
        }
        
        String maskedLocal = mask(localPart, 1, 1, '*');
        return maskedLocal + "@" + domainPart;
    }

    /**
     * 身份证号掩码处理
     * 
     * @param idCard 身份证号
     * @return 掩码后的身份证号
     */
    public static String maskIdCard(String idCard) {
        return mask(idCard, 6, 4, '*');
    }
}