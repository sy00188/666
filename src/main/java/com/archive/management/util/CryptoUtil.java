package com.archive.management.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * 加密工具类
 * 提供常用的加密、解密、哈希和签名方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class CryptoUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private CryptoUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== 常量定义 ==========

    /** AES算法 */
    private static final String AES_ALGORITHM = "AES";
    
    /** AES/GCM/NoPadding算法 */
    private static final String AES_GCM_ALGORITHM = "AES/GCM/NoPadding";
    
    /** HMAC-SHA256算法 */
    private static final String HMAC_SHA256_ALGORITHM = "HmacSHA256";
    
    /** SHA-256算法 */
    private static final String SHA256_ALGORITHM = "SHA-256";
    
    /** SHA-512算法 */
    private static final String SHA512_ALGORITHM = "SHA-512";
    
    /** MD5算法 */
    private static final String MD5_ALGORITHM = "MD5";
    
    /** GCM标签长度 */
    private static final int GCM_TAG_LENGTH = 16;
    
    /** GCM IV长度 */
    private static final int GCM_IV_LENGTH = 12;
    
    /** AES密钥长度 */
    private static final int AES_KEY_LENGTH = 256;

    /** 安全随机数生成器 */
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    // ========== 哈希方法 ==========

    /**
     * MD5哈希
     * 
     * @param input 输入字符串
     * @return MD5哈希值（十六进制），计算失败返回null
     */
    public static String md5(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(MD5_ALGORITHM);
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * SHA-256哈希
     * 
     * @param input 输入字符串
     * @return SHA-256哈希值（十六进制），计算失败返回null
     */
    public static String sha256(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(SHA256_ALGORITHM);
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * SHA-512哈希
     * 
     * @param input 输入字符串
     * @return SHA-512哈希值（十六进制），计算失败返回null
     */
    public static String sha512(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        
        try {
            MessageDigest md = MessageDigest.getInstance(SHA512_ALGORITHM);
            byte[] hashBytes = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * 带盐的SHA-256哈希
     * 
     * @param input 输入字符串
     * @param salt 盐值
     * @return 带盐的SHA-256哈希值（十六进制），计算失败返回null
     */
    public static String sha256WithSalt(String input, String salt) {
        if (StringUtil.isEmpty(input) || StringUtil.isEmpty(salt)) {
            return null;
        }
        
        return sha256(input + salt);
    }

    /**
     * 生成随机盐值
     * 
     * @param length 盐值长度
     * @return 随机盐值（Base64编码），生成失败返回null
     */
    public static String generateSalt(int length) {
        if (length <= 0) {
            return null;
        }
        
        byte[] salt = new byte[length];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * 生成默认长度的随机盐值（16字节）
     * 
     * @return 随机盐值（Base64编码），生成失败返回null
     */
    public static String generateSalt() {
        return generateSalt(16);
    }

    // ========== HMAC方法 ==========

    /**
     * HMAC-SHA256签名
     * 
     * @param data 数据
     * @param key 密钥
     * @return HMAC-SHA256签名（十六进制），计算失败返回null
     */
    public static String hmacSha256(String data, String key) {
        if (StringUtil.isEmpty(data) || StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * HMAC-SHA256签名（Base64编码）
     * 
     * @param data 数据
     * @param key 密钥
     * @return HMAC-SHA256签名（Base64编码），计算失败返回null
     */
    public static String hmacSha256Base64(String data, String key) {
        if (StringUtil.isEmpty(data) || StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256_ALGORITHM);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), HMAC_SHA256_ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hashBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 验证HMAC-SHA256签名
     * 
     * @param data 数据
     * @param key 密钥
     * @param signature 签名（十六进制）
     * @return 验证结果
     */
    public static boolean verifyHmacSha256(String data, String key, String signature) {
        if (StringUtil.isEmpty(data) || StringUtil.isEmpty(key) || StringUtil.isEmpty(signature)) {
            return false;
        }
        
        String computedSignature = hmacSha256(data, key);
        return signature.equals(computedSignature);
    }

    /**
     * 验证HMAC-SHA256签名（Base64编码）
     * 
     * @param data 数据
     * @param key 密钥
     * @param signature 签名（Base64编码）
     * @return 验证结果
     */
    public static boolean verifyHmacSha256Base64(String data, String key, String signature) {
        if (StringUtil.isEmpty(data) || StringUtil.isEmpty(key) || StringUtil.isEmpty(signature)) {
            return false;
        }
        
        String computedSignature = hmacSha256Base64(data, key);
        return signature.equals(computedSignature);
    }

    // ========== AES加密方法 ==========

    /**
     * 生成AES密钥
     * 
     * @return AES密钥（Base64编码），生成失败返回null
     */
    public static String generateAESKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(AES_ALGORITHM);
            keyGenerator.init(AES_KEY_LENGTH);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    /**
     * AES-GCM加密
     * 
     * @param plaintext 明文
     * @param key 密钥（Base64编码）
     * @return 加密结果（Base64编码，包含IV），加密失败返回null
     */
    public static String aesGcmEncrypt(String plaintext, String key) {
        if (StringUtil.isEmpty(plaintext) || StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            // 解码密钥
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            
            // 生成随机IV
            byte[] iv = new byte[GCM_IV_LENGTH];
            SECURE_RANDOM.nextBytes(iv);
            
            // 初始化Cipher
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, gcmParameterSpec);
            
            // 加密
            byte[] ciphertext = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
            
            // 组合IV和密文
            byte[] encryptedData = new byte[iv.length + ciphertext.length];
            System.arraycopy(iv, 0, encryptedData, 0, iv.length);
            System.arraycopy(ciphertext, 0, encryptedData, iv.length, ciphertext.length);
            
            return Base64.getEncoder().encodeToString(encryptedData);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * AES-GCM解密
     * 
     * @param ciphertext 密文（Base64编码，包含IV）
     * @param key 密钥（Base64编码）
     * @return 明文，解密失败返回null
     */
    public static String aesGcmDecrypt(String ciphertext, String key) {
        if (StringUtil.isEmpty(ciphertext) || StringUtil.isEmpty(key)) {
            return null;
        }
        
        try {
            // 解码密钥和密文
            byte[] keyBytes = Base64.getDecoder().decode(key);
            byte[] encryptedData = Base64.getDecoder().decode(ciphertext);
            
            if (encryptedData.length < GCM_IV_LENGTH) {
                return null;
            }
            
            // 提取IV和密文
            byte[] iv = new byte[GCM_IV_LENGTH];
            byte[] ciphertextBytes = new byte[encryptedData.length - GCM_IV_LENGTH];
            System.arraycopy(encryptedData, 0, iv, 0, GCM_IV_LENGTH);
            System.arraycopy(encryptedData, GCM_IV_LENGTH, ciphertextBytes, 0, ciphertextBytes.length);
            
            // 初始化Cipher
            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_GCM_ALGORITHM);
            GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(GCM_TAG_LENGTH * 8, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, gcmParameterSpec);
            
            // 解密
            byte[] plaintext = cipher.doFinal(ciphertextBytes);
            return new String(plaintext, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    // ========== Base64编码方法 ==========

    /**
     * Base64编码
     * 
     * @param input 输入字符串
     * @return Base64编码结果，编码失败返回null
     */
    public static String base64Encode(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        
        try {
            return Base64.getEncoder().encodeToString(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Base64解码
     * 
     * @param input Base64编码的字符串
     * @return 解码结果，解码失败返回null
     */
    public static String base64Decode(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(input);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * URL安全的Base64编码
     * 
     * @param input 输入字符串
     * @return URL安全的Base64编码结果，编码失败返回null
     */
    public static String base64UrlEncode(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        
        try {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(input.getBytes(StandardCharsets.UTF_8));
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * URL安全的Base64解码
     * 
     * @param input URL安全的Base64编码字符串
     * @return 解码结果，解码失败返回null
     */
    public static String base64UrlDecode(String input) {
        if (StringUtil.isEmpty(input)) {
            return null;
        }
        
        try {
            byte[] decodedBytes = Base64.getUrlDecoder().decode(input);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            return null;
        }
    }

    // ========== 密码强度验证 ==========

    /**
     * 验证密码强度
     * 
     * @param password 密码
     * @return 密码强度等级（0-4，0最弱，4最强）
     */
    public static int getPasswordStrength(String password) {
        if (StringUtil.isEmpty(password)) {
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
        if (password.matches(".*[0-9].*")) {
            strength++;
        }
        
        // 包含特殊字符
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?].*")) {
            strength++;
        }
        
        return Math.min(strength, 4);
    }

    /**
     * 验证密码是否符合强度要求
     * 
     * @param password 密码
     * @param minStrength 最小强度要求（1-4）
     * @return 是否符合要求
     */
    public static boolean isPasswordStrong(String password, int minStrength) {
        if (minStrength < 1 || minStrength > 4) {
            return false;
        }
        
        return getPasswordStrength(password) >= minStrength;
    }

    /**
     * 验证密码是否符合默认强度要求（强度≥3）
     * 
     * @param password 密码
     * @return 是否符合要求
     */
    public static boolean isPasswordStrong(String password) {
        return isPasswordStrong(password, 3);
    }

    // ========== 随机数生成 ==========

    /**
     * 生成随机字符串
     * 
     * @param length 长度
     * @param includeUppercase 是否包含大写字母
     * @param includeLowercase 是否包含小写字母
     * @param includeNumbers 是否包含数字
     * @param includeSpecialChars 是否包含特殊字符
     * @return 随机字符串，生成失败返回null
     */
    public static String generateRandomString(int length, boolean includeUppercase, 
                                            boolean includeLowercase, boolean includeNumbers, 
                                            boolean includeSpecialChars) {
        if (length <= 0) {
            return null;
        }
        
        StringBuilder chars = new StringBuilder();
        
        if (includeUppercase) {
            chars.append("ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        }
        if (includeLowercase) {
            chars.append("abcdefghijklmnopqrstuvwxyz");
        }
        if (includeNumbers) {
            chars.append("0123456789");
        }
        if (includeSpecialChars) {
            chars.append("!@#$%^&*()_+-=[]{}|;:,.<>?");
        }
        
        if (chars.length() == 0) {
            return null;
        }
        
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < length; i++) {
            int index = SECURE_RANDOM.nextInt(chars.length());
            result.append(chars.charAt(index));
        }
        
        return result.toString();
    }

    /**
     * 生成随机密码（包含大小写字母、数字和特殊字符）
     * 
     * @param length 长度
     * @return 随机密码，生成失败返回null
     */
    public static String generateRandomPassword(int length) {
        return generateRandomString(length, true, true, true, true);
    }

    /**
     * 生成随机令牌（包含大小写字母和数字）
     * 
     * @param length 长度
     * @return 随机令牌，生成失败返回null
     */
    public static String generateRandomToken(int length) {
        return generateRandomString(length, true, true, true, false);
    }

    // ========== 工具方法 ==========

    /**
     * 字节数组转十六进制字符串
     * 
     * @param bytes 字节数组
     * @return 十六进制字符串
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    /**
     * 十六进制字符串转字节数组
     * 
     * @param hex 十六进制字符串
     * @return 字节数组，转换失败返回null
     */
    public static byte[] hexToBytes(String hex) {
        if (StringUtil.isEmpty(hex) || hex.length() % 2 != 0) {
            return null;
        }
        
        try {
            byte[] bytes = new byte[hex.length() / 2];
            for (int i = 0; i < hex.length(); i += 2) {
                bytes[i / 2] = (byte) Integer.parseInt(hex.substring(i, i + 2), 16);
            }
            return bytes;
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 安全比较两个字符串（防止时序攻击）
     * 
     * @param a 字符串A
     * @param b 字符串B
     * @return 是否相等
     */
    public static boolean safeEquals(String a, String b) {
        if (a == null && b == null) {
            return true;
        }
        if (a == null || b == null) {
            return false;
        }
        
        byte[] bytesA = a.getBytes(StandardCharsets.UTF_8);
        byte[] bytesB = b.getBytes(StandardCharsets.UTF_8);
        
        if (bytesA.length != bytesB.length) {
            return false;
        }
        
        int result = 0;
        for (int i = 0; i < bytesA.length; i++) {
            result |= bytesA[i] ^ bytesB[i];
        }
        
        return result == 0;
    }
}