package com.archive.management.util;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Base64;

/**
 * 图片处理工具类
 * 提供图片缩放、裁剪、格式转换、水印等功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class ImageUtil {

    private static final String[] SUPPORTED_FORMATS = {"jpg", "jpeg", "png", "gif", "bmp", "webp"};
    private static final int DEFAULT_QUALITY = 85;

    // ========== 图片基础操作 ==========

    /**
     * 读取图片
     * 
     * @param filePath 文件路径
     * @return BufferedImage
     * @throws IOException IO异常
     */
    public static BufferedImage readImage(String filePath) throws IOException {
        return ImageIO.read(new File(filePath));
    }

    /**
     * 读取图片
     * 
     * @param inputStream 输入流
     * @return BufferedImage
     * @throws IOException IO异常
     */
    public static BufferedImage readImage(InputStream inputStream) throws IOException {
        return ImageIO.read(inputStream);
    }

    /**
     * 读取图片
     * 
     * @param bytes 字节数组
     * @return BufferedImage
     * @throws IOException IO异常
     */
    public static BufferedImage readImage(byte[] bytes) throws IOException {
        return ImageIO.read(new ByteArrayInputStream(bytes));
    }

    /**
     * 保存图片
     * 
     * @param image 图片
     * @param format 格式
     * @param filePath 文件路径
     * @throws IOException IO异常
     */
    public static void saveImage(BufferedImage image, String format, String filePath) throws IOException {
        ImageIO.write(image, format, new File(filePath));
    }

    /**
     * 保存图片到输出流
     * 
     * @param image 图片
     * @param format 格式
     * @param outputStream 输出流
     * @throws IOException IO异常
     */
    public static void saveImage(BufferedImage image, String format, OutputStream outputStream) throws IOException {
        ImageIO.write(image, format, outputStream);
    }

    /**
     * 图片转字节数组
     * 
     * @param image 图片
     * @param format 格式
     * @return 字节数组
     * @throws IOException IO异常
     */
    public static byte[] imageToBytes(BufferedImage image, String format) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, format, baos);
        return baos.toByteArray();
    }

    // ========== 图片缩放 ==========

    /**
     * 按比例缩放图片
     * 
     * @param image 原图片
     * @param scale 缩放比例
     * @return 缩放后的图片
     */
    public static BufferedImage scaleImage(BufferedImage image, double scale) {
        int newWidth = (int) (image.getWidth() * scale);
        int newHeight = (int) (image.getHeight() * scale);
        return resizeImage(image, newWidth, newHeight);
    }

    /**
     * 缩放图片到指定尺寸
     * 
     * @param image 原图片
     * @param width 目标宽度
     * @param height 目标高度
     * @return 缩放后的图片
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height) {
        return resizeImage(image, width, height, true);
    }

    /**
     * 缩放图片到指定尺寸
     * 
     * @param image 原图片
     * @param width 目标宽度
     * @param height 目标高度
     * @param smooth 是否平滑缩放
     * @return 缩放后的图片
     */
    public static BufferedImage resizeImage(BufferedImage image, int width, int height, boolean smooth) {
        int scaleType = smooth ? Image.SCALE_SMOOTH : Image.SCALE_FAST;
        Image scaledImage = image.getScaledInstance(width, height, scaleType);
        
        BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = bufferedImage.createGraphics();
        g2d.drawImage(scaledImage, 0, 0, null);
        g2d.dispose();
        
        return bufferedImage;
    }

    /**
     * 按最大尺寸等比缩放
     * 
     * @param image 原图片
     * @param maxWidth 最大宽度
     * @param maxHeight 最大高度
     * @return 缩放后的图片
     */
    public static BufferedImage resizeImageKeepRatio(BufferedImage image, int maxWidth, int maxHeight) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        
        double widthRatio = (double) maxWidth / originalWidth;
        double heightRatio = (double) maxHeight / originalHeight;
        double ratio = Math.min(widthRatio, heightRatio);
        
        int newWidth = (int) (originalWidth * ratio);
        int newHeight = (int) (originalHeight * ratio);
        
        return resizeImage(image, newWidth, newHeight);
    }

    // ========== 图片裁剪 ==========

    /**
     * 裁剪图片
     * 
     * @param image 原图片
     * @param x 起始X坐标
     * @param y 起始Y坐标
     * @param width 宽度
     * @param height 高度
     * @return 裁剪后的图片
     */
    public static BufferedImage cropImage(BufferedImage image, int x, int y, int width, int height) {
        return image.getSubimage(x, y, width, height);
    }

    /**
     * 居中裁剪图片
     * 
     * @param image 原图片
     * @param width 目标宽度
     * @param height 目标高度
     * @return 裁剪后的图片
     */
    public static BufferedImage cropImageCenter(BufferedImage image, int width, int height) {
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        
        int x = (originalWidth - width) / 2;
        int y = (originalHeight - height) / 2;
        
        return cropImage(image, x, y, width, height);
    }

    /**
     * 圆形裁剪
     * 
     * @param image 原图片
     * @param diameter 直径
     * @return 圆形图片
     */
    public static BufferedImage cropCircle(BufferedImage image, int diameter) {
        BufferedImage circleImage = new BufferedImage(diameter, diameter, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = circleImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setClip(new java.awt.geom.Ellipse2D.Float(0, 0, diameter, diameter));
        
        // 居中绘制原图片
        int x = (diameter - image.getWidth()) / 2;
        int y = (diameter - image.getHeight()) / 2;
        g2d.drawImage(image, x, y, null);
        g2d.dispose();
        
        return circleImage;
    }

    // ========== 图片旋转和翻转 ==========

    /**
     * 旋转图片
     * 
     * @param image 原图片
     * @param angle 旋转角度（度）
     * @return 旋转后的图片
     */
    public static BufferedImage rotateImage(BufferedImage image, double angle) {
        double radians = Math.toRadians(angle);
        double sin = Math.abs(Math.sin(radians));
        double cos = Math.abs(Math.cos(radians));
        
        int originalWidth = image.getWidth();
        int originalHeight = image.getHeight();
        
        int newWidth = (int) Math.floor(originalWidth * cos + originalHeight * sin);
        int newHeight = (int) Math.floor(originalHeight * cos + originalWidth * sin);
        
        BufferedImage rotatedImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = rotatedImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2d.translate((newWidth - originalWidth) / 2, (newHeight - originalHeight) / 2);
        g2d.rotate(radians, originalWidth / 2.0, originalHeight / 2.0);
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        
        return rotatedImage;
    }

    /**
     * 水平翻转
     * 
     * @param image 原图片
     * @return 翻转后的图片
     */
    public static BufferedImage flipHorizontal(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = flippedImage.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, width, 0, 0, height, null);
        g2d.dispose();
        
        return flippedImage;
    }

    /**
     * 垂直翻转
     * 
     * @param image 原图片
     * @return 翻转后的图片
     */
    public static BufferedImage flipVertical(BufferedImage image) {
        int width = image.getWidth();
        int height = image.getHeight();
        
        BufferedImage flippedImage = new BufferedImage(width, height, image.getType());
        Graphics2D g2d = flippedImage.createGraphics();
        g2d.drawImage(image, 0, 0, width, height, 0, height, width, 0, null);
        g2d.dispose();
        
        return flippedImage;
    }

    // ========== 水印功能 ==========

    /**
     * 添加文字水印
     * 
     * @param image 原图片
     * @param text 水印文字
     * @param font 字体
     * @param color 颜色
     * @param x X坐标
     * @param y Y坐标
     * @param alpha 透明度(0.0-1.0)
     * @return 添加水印后的图片
     */
    public static BufferedImage addTextWatermark(BufferedImage image, String text, Font font, Color color, int x, int y, float alpha) {
        BufferedImage watermarkedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarkedImage.createGraphics();
        
        g2d.drawImage(image, 0, 0, null);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.setColor(color);
        g2d.setFont(font);
        g2d.drawString(text, x, y);
        g2d.dispose();
        
        return watermarkedImage;
    }

    /**
     * 添加图片水印
     * 
     * @param image 原图片
     * @param watermark 水印图片
     * @param x X坐标
     * @param y Y坐标
     * @param alpha 透明度(0.0-1.0)
     * @return 添加水印后的图片
     */
    public static BufferedImage addImageWatermark(BufferedImage image, BufferedImage watermark, int x, int y, float alpha) {
        BufferedImage watermarkedImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = watermarkedImage.createGraphics();
        
        g2d.drawImage(image, 0, 0, null);
        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
        g2d.drawImage(watermark, x, y, null);
        g2d.dispose();
        
        return watermarkedImage;
    }

    // ========== 图片合成 ==========

    /**
     * 水平拼接图片
     * 
     * @param images 图片数组
     * @return 拼接后的图片
     */
    public static BufferedImage combineImagesHorizontally(BufferedImage... images) {
        if (images == null || images.length == 0) {
            return null;
        }
        
        int totalWidth = 0;
        int maxHeight = 0;
        
        for (BufferedImage image : images) {
            totalWidth += image.getWidth();
            maxHeight = Math.max(maxHeight, image.getHeight());
        }
        
        BufferedImage combinedImage = new BufferedImage(totalWidth, maxHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combinedImage.createGraphics();
        
        int x = 0;
        for (BufferedImage image : images) {
            g2d.drawImage(image, x, 0, null);
            x += image.getWidth();
        }
        g2d.dispose();
        
        return combinedImage;
    }

    /**
     * 垂直拼接图片
     * 
     * @param images 图片数组
     * @return 拼接后的图片
     */
    public static BufferedImage combineImagesVertically(BufferedImage... images) {
        if (images == null || images.length == 0) {
            return null;
        }
        
        int maxWidth = 0;
        int totalHeight = 0;
        
        for (BufferedImage image : images) {
            maxWidth = Math.max(maxWidth, image.getWidth());
            totalHeight += image.getHeight();
        }
        
        BufferedImage combinedImage = new BufferedImage(maxWidth, totalHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = combinedImage.createGraphics();
        
        int y = 0;
        for (BufferedImage image : images) {
            g2d.drawImage(image, 0, y, null);
            y += image.getHeight();
        }
        g2d.dispose();
        
        return combinedImage;
    }

    // ========== 图片效果 ==========

    /**
     * 转为灰度图
     * 
     * @param image 原图片
     * @return 灰度图
     */
    public static BufferedImage toGrayscale(BufferedImage image) {
        BufferedImage grayImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g2d = grayImage.createGraphics();
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();
        return grayImage;
    }

    /**
     * 调整亮度
     * 
     * @param image 原图片
     * @param brightness 亮度调整值(-100到100)
     * @return 调整后的图片
     */
    public static BufferedImage adjustBrightness(BufferedImage image, int brightness) {
        BufferedImage adjustedImage = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
        
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                Color color = new Color(image.getRGB(x, y));
                
                int r = Math.max(0, Math.min(255, color.getRed() + brightness));
                int g = Math.max(0, Math.min(255, color.getGreen() + brightness));
                int b = Math.max(0, Math.min(255, color.getBlue() + brightness));
                
                adjustedImage.setRGB(x, y, new Color(r, g, b).getRGB());
            }
        }
        
        return adjustedImage;
    }

    // ========== Base64编码 ==========

    /**
     * 图片转Base64
     * 
     * @param image 图片
     * @param format 格式
     * @return Base64字符串
     * @throws IOException IO异常
     */
    public static String imageToBase64(BufferedImage image, String format) throws IOException {
        byte[] bytes = imageToBytes(image, format);
        return Base64.getEncoder().encodeToString(bytes);
    }

    /**
     * Base64转图片
     * 
     * @param base64 Base64字符串
     * @return 图片
     * @throws IOException IO异常
     */
    public static BufferedImage base64ToImage(String base64) throws IOException {
        byte[] bytes = Base64.getDecoder().decode(base64);
        return readImage(bytes);
    }

    // ========== 工具方法 ==========

    /**
     * 获取图片信息
     * 
     * @param filePath 文件路径
     * @return 图片信息
     */
    public static ImageInfo getImageInfo(String filePath) {
        try {
            BufferedImage image = readImage(filePath);
            File file = new File(filePath);
            
            ImageInfo info = new ImageInfo();
            info.setWidth(image.getWidth());
            info.setHeight(image.getHeight());
            info.setFormat(getImageFormat(filePath));
            info.setSize(file.length());
            info.setValid(true);
            
            return info;
        } catch (Exception e) {
            ImageInfo info = new ImageInfo();
            info.setValid(false);
            info.setErrorMessage(e.getMessage());
            return info;
        }
    }

    /**
     * 获取图片格式
     * 
     * @param filePath 文件路径
     * @return 图片格式
     */
    public static String getImageFormat(String filePath) {
        int lastDotIndex = filePath.lastIndexOf('.');
        if (lastDotIndex > 0) {
            return filePath.substring(lastDotIndex + 1).toLowerCase();
        }
        return "unknown";
    }

    /**
     * 检查是否为支持的图片格式
     * 
     * @param format 格式
     * @return 是否支持
     */
    public static boolean isSupportedFormat(String format) {
        if (format == null) {
            return false;
        }
        
        String lowerFormat = format.toLowerCase();
        for (String supportedFormat : SUPPORTED_FORMATS) {
            if (supportedFormat.equals(lowerFormat)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 检查是否为有效的图片文件
     * 
     * @param filePath 文件路径
     * @return 是否有效
     */
    public static boolean isValidImageFile(String filePath) {
        try {
            BufferedImage image = readImage(filePath);
            return image != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 图片信息类
     */
    public static class ImageInfo {
        private int width;
        private int height;
        private String format;
        private long size;
        private boolean valid;
        private String errorMessage;

        public int getWidth() {
            return width;
        }

        public void setWidth(int width) {
            this.width = width;
        }

        public int getHeight() {
            return height;
        }

        public void setHeight(int height) {
            this.height = height;
        }

        public String getFormat() {
            return format;
        }

        public void setFormat(String format) {
            this.format = format;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }

        @Override
        public String toString() {
            return "ImageInfo{" +
                    "width=" + width +
                    ", height=" + height +
                    ", format='" + format + '\'' +
                    ", size=" + size +
                    ", valid=" + valid +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}