package com.archive.management.util;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * 日期时间工具类
 * 提供日期时间相关的常用操作方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class DateUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private DateUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== 常用日期格式 ==========
    
    /** 标准日期时间格式：yyyy-MM-dd HH:mm:ss */
    public static final String STANDARD_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    
    /** 标准日期格式：yyyy-MM-dd */
    public static final String STANDARD_DATE_FORMAT = "yyyy-MM-dd";
    
    /** 标准时间格式：HH:mm:ss */
    public static final String STANDARD_TIME_FORMAT = "HH:mm:ss";
    
    /** ISO日期时间格式：yyyy-MM-dd'T'HH:mm:ss */
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    
    /** 紧凑日期时间格式：yyyyMMddHHmmss */
    public static final String COMPACT_DATETIME_FORMAT = "yyyyMMddHHmmss";
    
    /** 紧凑日期格式：yyyyMMdd */
    public static final String COMPACT_DATE_FORMAT = "yyyyMMdd";

    // ========== 常用格式化器 ==========
    
    /** 标准日期时间格式化器 */
    public static final DateTimeFormatter STANDARD_DATETIME_FORMATTER = 
            DateTimeFormatter.ofPattern(STANDARD_DATETIME_FORMAT);
    
    /** 标准日期格式化器 */
    public static final DateTimeFormatter STANDARD_DATE_FORMATTER = 
            DateTimeFormatter.ofPattern(STANDARD_DATE_FORMAT);
    
    /** 标准时间格式化器 */
    public static final DateTimeFormatter STANDARD_TIME_FORMATTER = 
            DateTimeFormatter.ofPattern(STANDARD_TIME_FORMAT);

    // ========== 获取当前时间 ==========

    /**
     * 获取当前LocalDateTime
     * 
     * @return 当前LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * 获取当前LocalDate
     * 
     * @return 当前LocalDate
     */
    public static LocalDate today() {
        return LocalDate.now();
    }

    /**
     * 获取当前LocalTime
     * 
     * @return 当前LocalTime
     */
    public static LocalTime currentTime() {
        return LocalTime.now();
    }

    /**
     * 获取当前时间戳（毫秒）
     * 
     * @return 当前时间戳
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * 获取当前时间戳（秒）
     * 
     * @return 当前时间戳（秒）
     */
    public static long currentTimeSeconds() {
        return Instant.now().getEpochSecond();
    }

    // ========== 格式化方法 ==========

    /**
     * 格式化LocalDateTime为标准格式字符串
     * 
     * @param dateTime LocalDateTime对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(STANDARD_DATETIME_FORMATTER);
    }

    /**
     * 格式化LocalDate为标准格式字符串
     * 
     * @param date LocalDate对象
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.format(STANDARD_DATE_FORMATTER);
    }

    /**
     * 格式化LocalTime为标准格式字符串
     * 
     * @param time LocalTime对象
     * @return 格式化后的字符串
     */
    public static String format(LocalTime time) {
        if (time == null) {
            return null;
        }
        return time.format(STANDARD_TIME_FORMATTER);
    }

    /**
     * 使用指定格式格式化LocalDateTime
     * 
     * @param dateTime LocalDateTime对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null || pattern == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 使用指定格式格式化LocalDate
     * 
     * @param date LocalDate对象
     * @param pattern 格式模式
     * @return 格式化后的字符串
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null || pattern == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    // ========== 解析方法 ==========

    /**
     * 解析标准格式的日期时间字符串
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象
     * @throws DateTimeParseException 解析失败时抛出
     */
    public static LocalDateTime parseDateTime(String dateTimeStr) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, STANDARD_DATETIME_FORMATTER);
    }

    /**
     * 解析标准格式的日期字符串
     * 
     * @param dateStr 日期字符串
     * @return LocalDate对象
     * @throws DateTimeParseException 解析失败时抛出
     */
    public static LocalDate parseDate(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateStr, STANDARD_DATE_FORMATTER);
    }

    /**
     * 解析标准格式的时间字符串
     * 
     * @param timeStr 时间字符串
     * @return LocalTime对象
     * @throws DateTimeParseException 解析失败时抛出
     */
    public static LocalTime parseTime(String timeStr) {
        if (timeStr == null || timeStr.trim().isEmpty()) {
            return null;
        }
        return LocalTime.parse(timeStr, STANDARD_TIME_FORMATTER);
    }

    /**
     * 使用指定格式解析日期时间字符串
     * 
     * @param dateTimeStr 日期时间字符串
     * @param pattern 格式模式
     * @return LocalDateTime对象
     * @throws DateTimeParseException 解析失败时抛出
     */
    public static LocalDateTime parseDateTime(String dateTimeStr, String pattern) {
        if (dateTimeStr == null || dateTimeStr.trim().isEmpty() || pattern == null) {
            return null;
        }
        return LocalDateTime.parse(dateTimeStr, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * 使用指定格式解析日期字符串
     * 
     * @param dateStr 日期字符串
     * @param pattern 格式模式
     * @return LocalDate对象
     * @throws DateTimeParseException 解析失败时抛出
     */
    public static LocalDate parseDate(String dateStr, String pattern) {
        if (dateStr == null || dateStr.trim().isEmpty() || pattern == null) {
            return null;
        }
        return LocalDate.parse(dateStr, DateTimeFormatter.ofPattern(pattern));
    }

    // ========== 转换方法 ==========

    /**
     * LocalDateTime转换为Date
     * 
     * @param localDateTime LocalDateTime对象
     * @return Date对象
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    /**
     * LocalDate转换为Date
     * 
     * @param localDate LocalDate对象
     * @return Date对象
     */
    public static Date toDate(LocalDate localDate) {
        if (localDate == null) {
            return null;
        }
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    /**
     * Date转换为LocalDateTime
     * 
     * @param date Date对象
     * @return LocalDateTime对象
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Date转换为LocalDate
     * 
     * @param date Date对象
     * @return LocalDate对象
     */
    public static LocalDate toLocalDate(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * 时间戳（毫秒）转换为LocalDateTime
     * 
     * @param timestamp 时间戳（毫秒）
     * @return LocalDateTime对象
     */
    public static LocalDateTime toLocalDateTime(long timestamp) {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault());
    }

    /**
     * LocalDateTime转换为时间戳（毫秒）
     * 
     * @param localDateTime LocalDateTime对象
     * @return 时间戳（毫秒）
     */
    public static long toTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return 0L;
        }
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    // ========== 日期计算 ==========

    /**
     * 计算两个日期之间的天数差
     * 
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return 天数差
     */
    public static long daysBetween(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            return 0L;
        }
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    /**
     * 计算两个日期时间之间的小时差
     * 
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 小时差
     */
    public static long hoursBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0L;
        }
        return ChronoUnit.HOURS.between(startDateTime, endDateTime);
    }

    /**
     * 计算两个日期时间之间的分钟差
     * 
     * @param startDateTime 开始日期时间
     * @param endDateTime 结束日期时间
     * @return 分钟差
     */
    public static long minutesBetween(LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (startDateTime == null || endDateTime == null) {
            return 0L;
        }
        return ChronoUnit.MINUTES.between(startDateTime, endDateTime);
    }

    /**
     * 给日期添加天数
     * 
     * @param date 原日期
     * @param days 要添加的天数
     * @return 新日期
     */
    public static LocalDate plusDays(LocalDate date, long days) {
        if (date == null) {
            return null;
        }
        return date.plusDays(days);
    }

    /**
     * 给日期时间添加小时
     * 
     * @param dateTime 原日期时间
     * @param hours 要添加的小时数
     * @return 新日期时间
     */
    public static LocalDateTime plusHours(LocalDateTime dateTime, long hours) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusHours(hours);
    }

    /**
     * 给日期时间添加分钟
     * 
     * @param dateTime 原日期时间
     * @param minutes 要添加的分钟数
     * @return 新日期时间
     */
    public static LocalDateTime plusMinutes(LocalDateTime dateTime, long minutes) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.plusMinutes(minutes);
    }

    // ========== 日期判断 ==========

    /**
     * 判断是否为今天
     * 
     * @param date 要判断的日期
     * @return 是否为今天
     */
    public static boolean isToday(LocalDate date) {
        if (date == null) {
            return false;
        }
        return date.equals(LocalDate.now());
    }

    /**
     * 判断是否为今天
     * 
     * @param dateTime 要判断的日期时间
     * @return 是否为今天
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.toLocalDate().equals(LocalDate.now());
    }

    /**
     * 判断是否为周末
     * 
     * @param date 要判断的日期
     * @return 是否为周末
     */
    public static boolean isWeekend(LocalDate date) {
        if (date == null) {
            return false;
        }
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    /**
     * 判断日期是否在指定范围内
     * 
     * @param date 要判断的日期
     * @param startDate 开始日期（包含）
     * @param endDate 结束日期（包含）
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDate date, LocalDate startDate, LocalDate endDate) {
        if (date == null || startDate == null || endDate == null) {
            return false;
        }
        return !date.isBefore(startDate) && !date.isAfter(endDate);
    }

    /**
     * 判断日期时间是否在指定范围内
     * 
     * @param dateTime 要判断的日期时间
     * @param startDateTime 开始日期时间（包含）
     * @param endDateTime 结束日期时间（包含）
     * @return 是否在范围内
     */
    public static boolean isBetween(LocalDateTime dateTime, LocalDateTime startDateTime, LocalDateTime endDateTime) {
        if (dateTime == null || startDateTime == null || endDateTime == null) {
            return false;
        }
        return !dateTime.isBefore(startDateTime) && !dateTime.isAfter(endDateTime);
    }

    // ========== 获取特殊日期 ==========

    /**
     * 获取本月第一天
     * 
     * @return 本月第一天
     */
    public static LocalDate getFirstDayOfMonth() {
        return LocalDate.now().withDayOfMonth(1);
    }

    /**
     * 获取本月最后一天
     * 
     * @return 本月最后一天
     */
    public static LocalDate getLastDayOfMonth() {
        return LocalDate.now().withDayOfMonth(LocalDate.now().lengthOfMonth());
    }

    /**
     * 获取指定日期所在月份的第一天
     * 
     * @param date 指定日期
     * @return 该月第一天
     */
    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(1);
    }

    /**
     * 获取指定日期所在月份的最后一天
     * 
     * @param date 指定日期
     * @return 该月最后一天
     */
    public static LocalDate getLastDayOfMonth(LocalDate date) {
        if (date == null) {
            return null;
        }
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    /**
     * 获取本年第一天
     * 
     * @return 本年第一天
     */
    public static LocalDate getFirstDayOfYear() {
        return LocalDate.now().withDayOfYear(1);
    }

    /**
     * 获取本年最后一天
     * 
     * @return 本年最后一天
     */
    public static LocalDate getLastDayOfYear() {
        return LocalDate.now().withDayOfYear(LocalDate.now().lengthOfYear());
    }

    // ========== 安全解析方法 ==========

    /**
     * 安全解析日期时间字符串，解析失败返回null
     * 
     * @param dateTimeStr 日期时间字符串
     * @return LocalDateTime对象或null
     */
    public static LocalDateTime safeParseDatetime(String dateTimeStr) {
        try {
            return parseDateTime(dateTimeStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 安全解析日期字符串，解析失败返回null
     * 
     * @param dateStr 日期字符串
     * @return LocalDate对象或null
     */
    public static LocalDate safeParseDate(String dateStr) {
        try {
            return parseDate(dateStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    /**
     * 安全解析时间字符串，解析失败返回null
     * 
     * @param timeStr 时间字符串
     * @return LocalTime对象或null
     */
    public static LocalTime safeParseTime(String timeStr) {
        try {
            return parseTime(timeStr);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}