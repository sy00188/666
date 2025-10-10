package com.archive.management.util;

/**
 * 加密工具类（CryptoUtil的别名）
 * 为了兼容性保留此类
 */
public class EncryptionUtil {

    /**
     * 私有构造函数
     */
    private EncryptionUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    /**
     * AES加密
     */
    public static String encryptAES(String plainText, String key) throws Exception {
        return CryptoUtil.encryptAES(plainText, key);
    }

    /**
     * AES解密
     */
    public static String decryptAES(String encryptedText, String key) throws Exception {
        return CryptoUtil.decryptAES(encryptedText, key);
    }

    /**
     * SHA256哈希
     */
    public static String sha256(String input) {
        return CryptoUtil.sha256(input);
    }

    /**
     * MD5哈希
     */
    public static String md5(String input) {
        return CryptoUtil.md5(input);
    }

    /**
     * 生成AES密钥
     */
    public static String generateAESKey() throws Exception {
        return CryptoUtil.generateAESKey();
    }

    // ==================== 简化方法（使用默认密钥） ====================
    
    /**
     * 默认加密密钥（实际项目中应从配置文件读取）
     */
    private static final String DEFAULT_KEY = "ArchiveManagementSystem2024Key!";
    
    /**
     * 加密（使用默认密钥）
     * @param plainText 明文
     * @return 密文
     */
    public static String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            return encryptAES(plainText, DEFAULT_KEY);
        } catch (Exception e) {
            throw new RuntimeException("加密失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 解密（使用默认密钥）
     * @param encryptedText 密文
     * @return 明文
     */
    public static String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        try {
            return decryptAES(encryptedText, DEFAULT_KEY);
        } catch (Exception e) {
            throw new RuntimeException("解密失败: " + e.getMessage(), e);
        }
    }
}

