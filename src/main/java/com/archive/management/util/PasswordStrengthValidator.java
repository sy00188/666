package com.archive.management.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * 密码强度验证工具类
 * 提供密码强度检查、密码策略验证等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class PasswordStrengthValidator {

    // 密码强度等级
    public enum PasswordStrength {
        VERY_WEAK(0, "非常弱"),
        WEAK(1, "弱"),
        MEDIUM(2, "中等"),
        STRONG(3, "强"),
        VERY_STRONG(4, "非常强");

        private final int level;
        private final String description;

        PasswordStrength(int level, String description) {
            this.level = level;
            this.description = description;
        }

        public int getLevel() {
            return level;
        }

        public String getDescription() {
            return description;
        }
    }

    // 密码验证结果
    public static class ValidationResult {
        private boolean valid;
        private PasswordStrength strength;
        private int score;
        private List<String> errors;
        private List<String> suggestions;

        public ValidationResult() {
            this.errors = new ArrayList<>();
            this.suggestions = new ArrayList<>();
        }

        // Getters and Setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public PasswordStrength getStrength() { return strength; }
        public void setStrength(PasswordStrength strength) { this.strength = strength; }
        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        public List<String> getSuggestions() { return suggestions; }
        public void setSuggestions(List<String> suggestions) { this.suggestions = suggestions; }

        public void addError(String error) { this.errors.add(error); }
        public void addSuggestion(String suggestion) { this.suggestions.add(suggestion); }
    }

    // 正则表达式模式
    private static final Pattern LOWERCASE_PATTERN = Pattern.compile("[a-z]");
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\/?]");
    private static final Pattern SEQUENTIAL_PATTERN = Pattern.compile("(012|123|234|345|456|567|678|789|890|abc|bcd|cde|def|efg|fgh|ghi|hij|ijk|jkl|klm|lmn|mno|nop|opq|pqr|qrs|rst|stu|tuv|uvw|vwx|wxy|xyz)");
    private static final Pattern REPEATED_PATTERN = Pattern.compile("(.)\\1{2,}");

    // 常见弱密码列表
    private static final String[] COMMON_PASSWORDS = {
        "123456", "password", "123456789", "12345678", "12345", "1234567", "1234567890",
        "qwerty", "abc123", "111111", "123123", "admin", "letmein", "welcome", "monkey",
        "dragon", "pass", "master", "hello", "freedom", "whatever", "qazwsx", "trustno1"
    };

    // 键盘模式
    private static final String[] KEYBOARD_PATTERNS = {
        "qwerty", "asdfgh", "zxcvbn", "qwertyuiop", "asdfghjkl", "zxcvbnm",
        "1234567890", "!@#$%^&*()", "qaz", "wsx", "edc", "rfv", "tgb", "yhn", "ujm"
    };

    /**
     * 验证密码强度
     * 
     * @param password 待验证的密码
     * @return 验证结果
     */
    public ValidationResult validatePassword(String password) {
        ValidationResult result = new ValidationResult();
        
        if (password == null || password.isEmpty()) {
            result.addError("密码不能为空");
            result.setValid(false);
            result.setStrength(PasswordStrength.VERY_WEAK);
            result.setScore(0);
            return result;
        }

        // 基本长度检查
        if (password.length() < 8) {
            result.addError("密码长度至少需要8个字符");
            result.addSuggestion("建议使用8-20个字符的密码");
        }

        if (password.length() > 128) {
            result.addError("密码长度不能超过128个字符");
        }

        // 计算密码强度分数
        int score = calculatePasswordScore(password, result);
        result.setScore(score);

        // 确定密码强度等级
        PasswordStrength strength = determinePasswordStrength(score);
        result.setStrength(strength);

        // 检查是否满足最低要求
        boolean isValid = result.getErrors().isEmpty() && strength.getLevel() >= PasswordStrength.MEDIUM.getLevel();
        result.setValid(isValid);

        // 添加改进建议
        addImprovementSuggestions(password, result);

        log.debug("密码强度验证完成，强度等级: {}, 分数: {}, 有效性: {}", 
                 strength.getDescription(), score, isValid);

        return result;
    }

    /**
     * 计算密码强度分数
     * 
     * @param password 密码
     * @param result 验证结果对象
     * @return 强度分数 (0-100)
     */
    private int calculatePasswordScore(String password, ValidationResult result) {
        int score = 0;

        // 长度分数 (最多25分)
        int lengthScore = Math.min(25, password.length() * 2);
        score += lengthScore;

        // 字符类型多样性 (最多40分)
        int varietyScore = 0;
        if (LOWERCASE_PATTERN.matcher(password).find()) {
            varietyScore += 10;
        } else {
            result.addSuggestion("建议包含小写字母");
        }

        if (UPPERCASE_PATTERN.matcher(password).find()) {
            varietyScore += 10;
        } else {
            result.addSuggestion("建议包含大写字母");
        }

        if (DIGIT_PATTERN.matcher(password).find()) {
            varietyScore += 10;
        } else {
            result.addSuggestion("建议包含数字");
        }

        if (SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            varietyScore += 10;
        } else {
            result.addSuggestion("建议包含特殊字符");
        }

        score += varietyScore;

        // 复杂度奖励 (最多20分)
        if (password.length() >= 12) score += 5;
        if (password.length() >= 16) score += 5;
        if (hasUniqueCharacters(password)) score += 5;
        if (!containsPersonalInfo(password)) score += 5;

        // 扣分项 (最多扣35分)
        if (isCommonPassword(password)) {
            score -= 15;
            result.addError("密码过于常见，容易被破解");
        }

        if (SEQUENTIAL_PATTERN.matcher(password.toLowerCase()).find()) {
            score -= 10;
            result.addError("密码包含连续字符序列");
        }

        if (REPEATED_PATTERN.matcher(password).find()) {
            score -= 10;
            result.addError("密码包含重复字符");
        }

        if (containsKeyboardPattern(password)) {
            score -= 10;
            result.addError("密码包含键盘模式");
        }

        return Math.max(0, Math.min(100, score));
    }

    /**
     * 根据分数确定密码强度等级
     */
    private PasswordStrength determinePasswordStrength(int score) {
        if (score >= 80) return PasswordStrength.VERY_STRONG;
        if (score >= 60) return PasswordStrength.STRONG;
        if (score >= 40) return PasswordStrength.MEDIUM;
        if (score >= 20) return PasswordStrength.WEAK;
        return PasswordStrength.VERY_WEAK;
    }

    /**
     * 检查密码是否包含唯一字符
     */
    private boolean hasUniqueCharacters(String password) {
        return password.chars().distinct().count() >= password.length() * 0.7;
    }

    /**
     * 检查是否包含个人信息（简单检查）
     */
    private boolean containsPersonalInfo(String password) {
        String lowerPassword = password.toLowerCase();
        // 简单的个人信息模式检查
        return lowerPassword.contains("admin") || 
               lowerPassword.contains("user") || 
               lowerPassword.contains("test") ||
               lowerPassword.matches(".*\\d{4}.*"); // 可能的年份
    }

    /**
     * 检查是否为常见密码
     */
    private boolean isCommonPassword(String password) {
        String lowerPassword = password.toLowerCase();
        for (String common : COMMON_PASSWORDS) {
            if (lowerPassword.equals(common) || lowerPassword.contains(common)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否包含键盘模式
     */
    private boolean containsKeyboardPattern(String password) {
        String lowerPassword = password.toLowerCase();
        for (String pattern : KEYBOARD_PATTERNS) {
            if (lowerPassword.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 添加改进建议
     */
    private void addImprovementSuggestions(String password, ValidationResult result) {
        if (result.getStrength().getLevel() < PasswordStrength.STRONG.getLevel()) {
            if (password.length() < 12) {
                result.addSuggestion("建议使用12个字符以上的密码");
            }
            
            if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
                result.addSuggestion("建议添加特殊字符，如 !@#$%^&*");
            }
            
            result.addSuggestion("避免使用常见单词、个人信息或键盘模式");
            result.addSuggestion("建议使用密码短语或随机生成的密码");
        }
    }

    /**
     * 检查密码是否满足最低安全要求
     * 
     * @param password 密码
     * @return 是否满足要求
     */
    public boolean meetsMinimumRequirements(String password) {
        ValidationResult result = validatePassword(password);
        return result.isValid();
    }

    /**
     * 获取密码强度等级
     * 
     * @param password 密码
     * @return 强度等级
     */
    public PasswordStrength getPasswordStrength(String password) {
        ValidationResult result = validatePassword(password);
        return result.getStrength();
    }

    /**
     * 生成密码强度报告
     * 
     * @param password 密码
     * @return 强度报告文本
     */
    public String generateStrengthReport(String password) {
        ValidationResult result = validatePassword(password);
        StringBuilder report = new StringBuilder();
        
        report.append("密码强度: ").append(result.getStrength().getDescription())
              .append(" (").append(result.getScore()).append("/100)\n");
        
        if (!result.getErrors().isEmpty()) {
            report.append("\n问题:\n");
            for (String error : result.getErrors()) {
                report.append("- ").append(error).append("\n");
            }
        }
        
        if (!result.getSuggestions().isEmpty()) {
            report.append("\n建议:\n");
            for (String suggestion : result.getSuggestions()) {
                report.append("- ").append(suggestion).append("\n");
            }
        }
        
        return report.toString();
    }
}