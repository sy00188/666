package com.archive.management.util;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ID生成工具类
 * 提供各种类型的ID生成方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class IdUtil {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    
    // 雪花算法相关常量
    private static final long EPOCH = 1640995200000L; // 2022-01-01 00:00:00
    private static final long WORKER_ID_BITS = 5L;
    private static final long DATACENTER_ID_BITS = 5L;
    private static final long SEQUENCE_BITS = 12L;
    
    private static final long MAX_WORKER_ID = ~(-1L << WORKER_ID_BITS);
    private static final long MAX_DATACENTER_ID = ~(-1L << DATACENTER_ID_BITS);
    private static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);
    
    private static final long WORKER_ID_SHIFT = SEQUENCE_BITS;
    private static final long DATACENTER_ID_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS;
    private static final long TIMESTAMP_LEFT_SHIFT = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;
    
    private static long workerId = 1L;
    private static long datacenterId = 1L;
    private static long lastTimestamp = -1L;
    private static long sequence = 0L;
    
    static {
        // 自动获取机器ID
        try {
            workerId = getWorkerId();
            datacenterId = getDatacenterId();
        } catch (Exception e) {
            // 使用默认值
        }
    }

    // ========== UUID相关 ==========

    /**
     * 生成标准UUID
     * 
     * @return UUID字符串
     */
    public static String uuid() {
        return UUID.randomUUID().toString();
    }

    /**
     * 生成简化UUID（去掉横线）
     * 
     * @return 简化UUID字符串
     */
    public static String simpleUuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }

    /**
     * 生成大写UUID
     * 
     * @return 大写UUID字符串
     */
    public static String upperUuid() {
        return UUID.randomUUID().toString().toUpperCase();
    }

    /**
     * 生成大写简化UUID
     * 
     * @return 大写简化UUID字符串
     */
    public static String upperSimpleUuid() {
        return UUID.randomUUID().toString().replace("-", "").toUpperCase();
    }

    // ========== 雪花算法 ==========

    /**
     * 生成雪花ID
     * 
     * @return 雪花ID
     */
    public static synchronized long snowflakeId() {
        long timestamp = timeGen();

        if (timestamp < lastTimestamp) {
            throw new RuntimeException(String.format(
                "Clock moved backwards. Refusing to generate id for %d milliseconds", 
                lastTimestamp - timestamp));
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;

        return ((timestamp - EPOCH) << TIMESTAMP_LEFT_SHIFT) |
               (datacenterId << DATACENTER_ID_SHIFT) |
               (workerId << WORKER_ID_SHIFT) |
               sequence;
    }

    /**
     * 生成雪花ID字符串
     * 
     * @return 雪花ID字符串
     */
    public static String snowflakeIdStr() {
        return String.valueOf(snowflakeId());
    }

    /**
     * 等待下一毫秒
     * 
     * @param lastTimestamp 上次时间戳
     * @return 下一毫秒时间戳
     */
    private static long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }

    /**
     * 获取当前时间戳
     * 
     * @return 当前时间戳
     */
    private static long timeGen() {
        return System.currentTimeMillis();
    }

    /**
     * 获取工作机器ID
     * 
     * @return 工作机器ID
     */
    private static long getWorkerId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            long id;
            if (network == null) {
                id = 1;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    id = ((0x000000FF & (long) mac[mac.length - 2]) |
                          (0x0000FF00 & (((long) mac[mac.length - 1]) << 8))) >> 6;
                    id = id % (MAX_WORKER_ID + 1);
                } else {
                    id = 1;
                }
            }
            return id;
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * 获取数据中心ID
     * 
     * @return 数据中心ID
     */
    private static long getDatacenterId() {
        try {
            InetAddress ip = InetAddress.getLocalHost();
            NetworkInterface network = NetworkInterface.getByInetAddress(ip);
            long id;
            if (network == null) {
                id = 1;
            } else {
                byte[] mac = network.getHardwareAddress();
                if (mac != null) {
                    id = ((0x000000FF & (long) mac[mac.length - 4]) |
                          (0x0000FF00 & (((long) mac[mac.length - 3]) << 8))) >> 6;
                    id = id % (MAX_DATACENTER_ID + 1);
                } else {
                    id = 1;
                }
            }
            return id;
        } catch (Exception e) {
            return 1;
        }
    }

    // ========== 时间戳ID ==========

    /**
     * 生成时间戳ID
     * 
     * @return 时间戳ID
     */
    public static long timestampId() {
        return System.currentTimeMillis();
    }

    /**
     * 生成时间戳ID字符串
     * 
     * @return 时间戳ID字符串
     */
    public static String timestampIdStr() {
        return String.valueOf(System.currentTimeMillis());
    }

    /**
     * 生成纳秒时间戳ID
     * 
     * @return 纳秒时间戳ID
     */
    public static long nanoTimestampId() {
        return System.nanoTime();
    }

    /**
     * 生成纳秒时间戳ID字符串
     * 
     * @return 纳秒时间戳ID字符串
     */
    public static String nanoTimestampIdStr() {
        return String.valueOf(System.nanoTime());
    }

    // ========== 序列号ID ==========

    /**
     * 生成递增序列号
     * 
     * @return 序列号
     */
    public static long sequenceId() {
        return SEQUENCE.incrementAndGet();
    }

    /**
     * 生成递增序列号字符串
     * 
     * @return 序列号字符串
     */
    public static String sequenceIdStr() {
        return String.valueOf(SEQUENCE.incrementAndGet());
    }

    /**
     * 获取当前序列号
     * 
     * @return 当前序列号
     */
    public static long getCurrentSequence() {
        return SEQUENCE.get();
    }

    /**
     * 重置序列号
     */
    public static void resetSequence() {
        SEQUENCE.set(0);
    }

    /**
     * 设置序列号起始值
     * 
     * @param startValue 起始值
     */
    public static void setSequenceStart(long startValue) {
        SEQUENCE.set(startValue);
    }

    // ========== 随机ID ==========

    /**
     * 生成随机长整型ID
     * 
     * @return 随机ID
     */
    public static long randomId() {
        return ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE);
    }

    /**
     * 生成随机长整型ID字符串
     * 
     * @return 随机ID字符串
     */
    public static String randomIdStr() {
        return String.valueOf(ThreadLocalRandom.current().nextLong(1, Long.MAX_VALUE));
    }

    /**
     * 生成指定范围的随机ID
     * 
     * @param min 最小值
     * @param max 最大值
     * @return 随机ID
     */
    public static long randomId(long min, long max) {
        return ThreadLocalRandom.current().nextLong(min, max);
    }

    /**
     * 生成安全随机ID
     * 
     * @return 安全随机ID
     */
    public static long secureRandomId() {
        byte[] bytes = new byte[8];
        SECURE_RANDOM.nextBytes(bytes);
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result = (result << 8) | (bytes[i] & 0xFF);
        }
        return Math.abs(result);
    }

    // ========== 字符串ID ==========

    /**
     * 生成随机字符串ID
     * 
     * @param length 长度
     * @return 随机字符串ID
     */
    public static String randomStringId(int length) {
        return randomStringId(length, false);
    }

    /**
     * 生成随机字符串ID
     * 
     * @param length 长度
     * @param includeSymbols 是否包含符号
     * @return 随机字符串ID
     */
    public static String randomStringId(int length, boolean includeSymbols) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        if (includeSymbols) {
            chars += "!@#$%^&*()_+-=[]{}|;:,.<>?";
        }
        
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成数字字符串ID
     * 
     * @param length 长度
     * @return 数字字符串ID
     */
    public static String randomNumericId(int length) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 生成字母字符串ID
     * 
     * @param length 长度
     * @return 字母字符串ID
     */
    public static String randomAlphaId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成大写字母字符串ID
     * 
     * @param length 长度
     * @return 大写字母字符串ID
     */
    public static String randomUpperAlphaId(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成小写字母字符串ID
     * 
     * @param length 长度
     * @return 小写字母字符串ID
     */
    public static String randomLowerAlphaId(int length) {
        String chars = "abcdefghijklmnopqrstuvwxyz";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    // ========== 业务ID ==========

    /**
     * 生成订单号
     * 
     * @param prefix 前缀
     * @return 订单号
     */
    public static String generateOrderNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = randomNumericId(6);
        return (prefix != null ? prefix : "ORD") + timestamp + random;
    }

    /**
     * 生成订单号（默认前缀）
     * 
     * @return 订单号
     */
    public static String generateOrderNo() {
        return generateOrderNo("ORD");
    }

    /**
     * 生成用户编号
     * 
     * @param prefix 前缀
     * @return 用户编号
     */
    public static String generateUserNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String random = randomNumericId(8);
        return (prefix != null ? prefix : "U") + timestamp + random;
    }

    /**
     * 生成用户编号（默认前缀）
     * 
     * @return 用户编号
     */
    public static String generateUserNo() {
        return generateUserNo("U");
    }

    /**
     * 生成流水号
     * 
     * @param prefix 前缀
     * @return 流水号
     */
    public static String generateSerialNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"));
        String sequence = String.format("%06d", SEQUENCE.incrementAndGet() % 1000000);
        return (prefix != null ? prefix : "SN") + timestamp + sequence;
    }

    /**
     * 生成流水号（默认前缀）
     * 
     * @return 流水号
     */
    public static String generateSerialNo() {
        return generateSerialNo("SN");
    }

    /**
     * 生成批次号
     * 
     * @param prefix 前缀
     * @return 批次号
     */
    public static String generateBatchNo(String prefix) {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"));
        String random = randomUpperAlphaId(4);
        return (prefix != null ? prefix : "BATCH") + timestamp + random;
    }

    /**
     * 生成批次号（默认前缀）
     * 
     * @return 批次号
     */
    public static String generateBatchNo() {
        return generateBatchNo("BATCH");
    }

    // ========== 验证码 ==========

    /**
     * 生成数字验证码
     * 
     * @param length 长度
     * @return 验证码
     */
    public static String generateVerifyCode(int length) {
        return randomNumericId(length);
    }

    /**
     * 生成数字验证码（默认6位）
     * 
     * @return 验证码
     */
    public static String generateVerifyCode() {
        return generateVerifyCode(6);
    }

    /**
     * 生成字母数字验证码
     * 
     * @param length 长度
     * @return 验证码
     */
    public static String generateAlphaNumericCode(int length) {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = ThreadLocalRandom.current().nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        return sb.toString();
    }

    /**
     * 生成字母数字验证码（默认6位）
     * 
     * @return 验证码
     */
    public static String generateAlphaNumericCode() {
        return generateAlphaNumericCode(6);
    }

    /**
     * 生成验证码ID
     * 格式：captcha_时间戳_随机数
     * 
     * @return 验证码ID
     */
    public static String generateCaptchaId() {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String random = randomNumericId(6);
        return "captcha_" + timestamp + "_" + random;
    }

    // ========== 工具方法 ==========

    /**
     * 检查ID是否为雪花ID格式
     * 
     * @param id ID值
     * @return 是否为雪花ID
     */
    public static boolean isSnowflakeId(long id) {
        // 雪花ID的长度通常在15-19位之间
        String idStr = String.valueOf(id);
        return idStr.length() >= 15 && idStr.length() <= 19;
    }

    /**
     * 检查字符串是否为UUID格式
     * 
     * @param str 字符串
     * @return 是否为UUID
     */
    public static boolean isUuid(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            UUID.fromString(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    /**
     * 检查字符串是否为简化UUID格式
     * 
     * @param str 字符串
     * @return 是否为简化UUID
     */
    public static boolean isSimpleUuid(String str) {
        if (str == null || str.length() != 32) {
            return false;
        }
        return str.matches("[0-9a-fA-F]{32}");
    }

    /**
     * 格式化简化UUID为标准UUID
     * 
     * @param simpleUuid 简化UUID
     * @return 标准UUID
     */
    public static String formatSimpleUuid(String simpleUuid) {
        if (simpleUuid == null || simpleUuid.length() != 32) {
            throw new IllegalArgumentException("Invalid simple UUID format");
        }
        return simpleUuid.substring(0, 8) + "-" +
               simpleUuid.substring(8, 12) + "-" +
               simpleUuid.substring(12, 16) + "-" +
               simpleUuid.substring(16, 20) + "-" +
               simpleUuid.substring(20, 32);
    }

    /**
     * 从雪花ID中提取时间戳
     * 
     * @param snowflakeId 雪花ID
     * @return 时间戳
     */
    public static long extractTimestampFromSnowflake(long snowflakeId) {
        return (snowflakeId >> TIMESTAMP_LEFT_SHIFT) + EPOCH;
    }

    /**
     * 从雪花ID中提取工作机器ID
     * 
     * @param snowflakeId 雪花ID
     * @return 工作机器ID
     */
    public static long extractWorkerIdFromSnowflake(long snowflakeId) {
        return (snowflakeId >> WORKER_ID_SHIFT) & MAX_WORKER_ID;
    }

    /**
     * 从雪花ID中提取数据中心ID
     * 
     * @param snowflakeId 雪花ID
     * @return 数据中心ID
     */
    public static long extractDatacenterIdFromSnowflake(long snowflakeId) {
        return (snowflakeId >> DATACENTER_ID_SHIFT) & MAX_DATACENTER_ID;
    }

    /**
     * 从雪花ID中提取序列号
     * 
     * @param snowflakeId 雪花ID
     * @return 序列号
     */
    public static long extractSequenceFromSnowflake(long snowflakeId) {
        return snowflakeId & SEQUENCE_MASK;
    }
}