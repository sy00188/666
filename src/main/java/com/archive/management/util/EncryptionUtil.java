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
}

