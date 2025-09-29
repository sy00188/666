package com.archive.management.util;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.Random;
import javax.imageio.ImageIO;

/**
 * 验证码生成工具类
 * 提供图形验证码生成功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class CaptchaGenerator {

    private static final int DEFAULT_WIDTH = 120;
    private static final int DEFAULT_HEIGHT = 40;
    private static final int DEFAULT_CODE_LENGTH = 4;
    
    // 验证码字符集（去除容易混淆的字符）
    private static final String CODE_CHARS = "23456789ABCDEFGHJKLMNPQRSTUVWXYZ";
    
    // 干扰线数量
    private static final int INTERFERENCE_LINE_COUNT = 5;
    
    // 噪点数量
    private static final int NOISE_POINT_COUNT = 50;
    
    private static final Random random = new Random();

    /**
     * 生成验证码图片（默认参数）
     * 
     * @return CaptchaResult 包含验证码文本和base64图片
     */
    public static CaptchaResult generateCaptcha() {
        return generateCaptcha(DEFAULT_WIDTH, DEFAULT_HEIGHT, DEFAULT_CODE_LENGTH);
    }

    /**
     * 生成验证码图片
     * 
     * @param width 图片宽度
     * @param height 图片高度
     * @param codeLength 验证码长度
     * @return CaptchaResult 包含验证码文本和base64图片
     */
    public static CaptchaResult generateCaptcha(int width, int height, int codeLength) {
        // 生成验证码文本
        String code = generateRandomCode(codeLength);
        
        // 创建图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = image.createGraphics();
        
        try {
            // 设置抗锯齿
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            // 填充背景
            g2d.setColor(getRandomLightColor());
            g2d.fillRect(0, 0, width, height);
            
            // 绘制干扰线
            drawInterferenceLines(g2d, width, height);
            
            // 绘制验证码文字
            drawCodeText(g2d, code, width, height);
            
            // 添加噪点
            drawNoisePoints(g2d, width, height);
            
            // 转换为base64
            String base64Image = imageToBase64(image);
            
            return new CaptchaResult(code, base64Image);
            
        } finally {
            g2d.dispose();
        }
    }

    /**
     * 生成随机验证码文本
     * 
     * @param length 长度
     * @return 验证码文本
     */
    private static String generateRandomCode(int length) {
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < length; i++) {
            code.append(CODE_CHARS.charAt(random.nextInt(CODE_CHARS.length())));
        }
        return code.toString();
    }

    /**
     * 绘制验证码文字
     * 
     * @param g2d 图形上下文
     * @param code 验证码文本
     * @param width 图片宽度
     * @param height 图片高度
     */
    private static void drawCodeText(Graphics2D g2d, String code, int width, int height) {
        int fontSize = height - 10;
        Font font = new Font("Arial", Font.BOLD, fontSize);
        g2d.setFont(font);
        
        FontMetrics fm = g2d.getFontMetrics();
        int charWidth = width / code.length();
        
        for (int i = 0; i < code.length(); i++) {
            char c = code.charAt(i);
            
            // 随机颜色
            g2d.setColor(getRandomDarkColor());
            
            // 计算字符位置
            int x = i * charWidth + (charWidth - fm.charWidth(c)) / 2;
            int y = height / 2 + fm.getAscent() / 2 - 2;
            
            // 随机旋转角度
            double angle = (random.nextDouble() - 0.5) * 0.4; // -0.2 到 0.2 弧度
            g2d.rotate(angle, x + fm.charWidth(c) / 2, y);
            
            // 绘制字符
            g2d.drawString(String.valueOf(c), x, y);
            
            // 恢复旋转
            g2d.rotate(-angle, x + fm.charWidth(c) / 2, y);
        }
    }

    /**
     * 绘制干扰线
     * 
     * @param g2d 图形上下文
     * @param width 图片宽度
     * @param height 图片高度
     */
    private static void drawInterferenceLines(Graphics2D g2d, int width, int height) {
        for (int i = 0; i < INTERFERENCE_LINE_COUNT; i++) {
            g2d.setColor(getRandomMediumColor());
            g2d.setStroke(new BasicStroke(1.0f + random.nextFloat()));
            
            int x1 = random.nextInt(width);
            int y1 = random.nextInt(height);
            int x2 = random.nextInt(width);
            int y2 = random.nextInt(height);
            
            g2d.drawLine(x1, y1, x2, y2);
        }
    }

    /**
     * 绘制噪点
     * 
     * @param g2d 图形上下文
     * @param width 图片宽度
     * @param height 图片高度
     */
    private static void drawNoisePoints(Graphics2D g2d, int width, int height) {
        for (int i = 0; i < NOISE_POINT_COUNT; i++) {
            g2d.setColor(getRandomMediumColor());
            int x = random.nextInt(width);
            int y = random.nextInt(height);
            g2d.fillOval(x, y, 1, 1);
        }
    }

    /**
     * 获取随机浅色（背景色）
     * 
     * @return Color
     */
    private static Color getRandomLightColor() {
        int r = 200 + random.nextInt(56); // 200-255
        int g = 200 + random.nextInt(56);
        int b = 200 + random.nextInt(56);
        return new Color(r, g, b);
    }

    /**
     * 获取随机深色（文字色）
     * 
     * @return Color
     */
    private static Color getRandomDarkColor() {
        int r = random.nextInt(100); // 0-99
        int g = random.nextInt(100);
        int b = random.nextInt(100);
        return new Color(r, g, b);
    }

    /**
     * 获取随机中等色（干扰线和噪点）
     * 
     * @return Color
     */
    private static Color getRandomMediumColor() {
        int r = 100 + random.nextInt(100); // 100-199
        int g = 100 + random.nextInt(100);
        int b = 100 + random.nextInt(100);
        return new Color(r, g, b);
    }

    /**
     * 将BufferedImage转换为base64字符串
     * 
     * @param image 图片
     * @return base64字符串
     */
    private static String imageToBase64(BufferedImage image) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            ImageIO.write(image, "png", baos);
            byte[] imageBytes = baos.toByteArray();
            return "data:image/png;base64," + Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new RuntimeException("转换图片为base64失败", e);
        }
    }

    /**
     * 验证码结果类
     */
    public static class CaptchaResult {
        private final String code;
        private final String base64Image;

        public CaptchaResult(String code, String base64Image) {
            this.code = code;
            this.base64Image = base64Image;
        }

        public String getCode() {
            return code;
        }

        public String getBase64Image() {
            return base64Image;
        }

        @Override
        public String toString() {
            return "CaptchaResult{" +
                    "code='" + code + '\'' +
                    ", base64Image='" + (base64Image != null ? base64Image.substring(0, Math.min(50, base64Image.length())) + "..." : "null") + '\'' +
                    '}';
        }
    }
}