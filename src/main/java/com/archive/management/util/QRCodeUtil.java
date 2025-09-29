package com.archive.management.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * 二维码工具类
 * 提供二维码生成和解析功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class QRCodeUtil {

    private static final int DEFAULT_WIDTH = 300;
    private static final int DEFAULT_HEIGHT = 300;
    private static final String DEFAULT_FORMAT = "PNG";
    private static final String DEFAULT_CHARSET = "UTF-8";

    // ========== 二维码生成 ==========

    /**
     * 生成二维码到文件
     * 
     * @param content 内容
     * @param filePath 文件路径
     * @throws Exception 异常
     */
    public static void generateQRCodeToFile(String content, String filePath) throws Exception {
        generateQRCodeToFile(content, filePath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码到文件
     * 
     * @param content 内容
     * @param filePath 文件路径
     * @param width 宽度
     * @param height 高度
     * @throws Exception 异常
     */
    public static void generateQRCodeToFile(String content, String filePath, int width, int height) throws Exception {
        BitMatrix bitMatrix = generateBitMatrix(content, width, height);
        Path path = FileSystems.getDefault().getPath(filePath);
        MatrixToImageWriter.writeToPath(bitMatrix, DEFAULT_FORMAT, path);
    }

    /**
     * 生成二维码到输出流
     * 
     * @param content 内容
     * @param outputStream 输出流
     * @throws Exception 异常
     */
    public static void generateQRCodeToStream(String content, OutputStream outputStream) throws Exception {
        generateQRCodeToStream(content, outputStream, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码到输出流
     * 
     * @param content 内容
     * @param outputStream 输出流
     * @param width 宽度
     * @param height 高度
     * @throws Exception 异常
     */
    public static void generateQRCodeToStream(String content, OutputStream outputStream, int width, int height) throws Exception {
        BitMatrix bitMatrix = generateBitMatrix(content, width, height);
        MatrixToImageWriter.writeToStream(bitMatrix, DEFAULT_FORMAT, outputStream);
    }

    /**
     * 生成二维码为BufferedImage
     * 
     * @param content 内容
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateQRCodeImage(String content) throws Exception {
        return generateQRCodeImage(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码为BufferedImage
     * 
     * @param content 内容
     * @param width 宽度
     * @param height 高度
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateQRCodeImage(String content, int width, int height) throws Exception {
        BitMatrix bitMatrix = generateBitMatrix(content, width, height);
        return MatrixToImageWriter.toBufferedImage(bitMatrix);
    }

    /**
     * 生成二维码为字节数组
     * 
     * @param content 内容
     * @return 字节数组
     * @throws Exception 异常
     */
    public static byte[] generateQRCodeBytes(String content) throws Exception {
        return generateQRCodeBytes(content, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成二维码为字节数组
     * 
     * @param content 内容
     * @param width 宽度
     * @param height 高度
     * @return 字节数组
     * @throws Exception 异常
     */
    public static byte[] generateQRCodeBytes(String content, int width, int height) throws Exception {
        BufferedImage image = generateQRCodeImage(content, width, height);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, DEFAULT_FORMAT, baos);
        return baos.toByteArray();
    }

    /**
     * 生成BitMatrix
     * 
     * @param content 内容
     * @param width 宽度
     * @param height 高度
     * @return BitMatrix
     * @throws Exception 异常
     */
    private static BitMatrix generateBitMatrix(String content, int width, int height) throws Exception {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.CHARACTER_SET, DEFAULT_CHARSET);
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.M);
        hints.put(EncodeHintType.MARGIN, 1);

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        return qrCodeWriter.encode(content, BarcodeFormat.QR_CODE, width, height, hints);
    }

    // ========== 带Logo的二维码 ==========

    /**
     * 生成带Logo的二维码
     * 
     * @param content 内容
     * @param logoPath Logo路径
     * @param outputPath 输出路径
     * @throws Exception 异常
     */
    public static void generateQRCodeWithLogo(String content, String logoPath, String outputPath) throws Exception {
        generateQRCodeWithLogo(content, logoPath, outputPath, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成带Logo的二维码
     * 
     * @param content 内容
     * @param logoPath Logo路径
     * @param outputPath 输出路径
     * @param width 宽度
     * @param height 高度
     * @throws Exception 异常
     */
    public static void generateQRCodeWithLogo(String content, String logoPath, String outputPath, int width, int height) throws Exception {
        BufferedImage qrImage = generateQRCodeImage(content, width, height);
        BufferedImage logoImage = ImageIO.read(new File(logoPath));
        
        BufferedImage combinedImage = addLogoToQRCode(qrImage, logoImage);
        
        ImageIO.write(combinedImage, DEFAULT_FORMAT, new File(outputPath));
    }

    /**
     * 生成带Logo的二维码为BufferedImage
     * 
     * @param content 内容
     * @param logoImage Logo图片
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateQRCodeWithLogo(String content, BufferedImage logoImage) throws Exception {
        return generateQRCodeWithLogo(content, logoImage, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成带Logo的二维码为BufferedImage
     * 
     * @param content 内容
     * @param logoImage Logo图片
     * @param width 宽度
     * @param height 高度
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateQRCodeWithLogo(String content, BufferedImage logoImage, int width, int height) throws Exception {
        BufferedImage qrImage = generateQRCodeImage(content, width, height);
        return addLogoToQRCode(qrImage, logoImage);
    }

    /**
     * 在二维码中添加Logo
     * 
     * @param qrImage 二维码图片
     * @param logoImage Logo图片
     * @return 合成后的图片
     */
    private static BufferedImage addLogoToQRCode(BufferedImage qrImage, BufferedImage logoImage) {
        int qrWidth = qrImage.getWidth();
        int qrHeight = qrImage.getHeight();
        
        // Logo大小为二维码的1/5
        int logoWidth = qrWidth / 5;
        int logoHeight = qrHeight / 5;
        
        // 缩放Logo
        Image scaledLogo = logoImage.getScaledInstance(logoWidth, logoHeight, Image.SCALE_SMOOTH);
        BufferedImage scaledLogoImage = new BufferedImage(logoWidth, logoHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2d = scaledLogoImage.createGraphics();
        g2d.drawImage(scaledLogo, 0, 0, null);
        g2d.dispose();
        
        // 创建合成图片
        BufferedImage combinedImage = new BufferedImage(qrWidth, qrHeight, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = combinedImage.createGraphics();
        
        // 绘制二维码
        g.drawImage(qrImage, 0, 0, null);
        
        // 绘制Logo（居中）
        int logoX = (qrWidth - logoWidth) / 2;
        int logoY = (qrHeight - logoHeight) / 2;
        g.drawImage(scaledLogoImage, logoX, logoY, null);
        
        g.dispose();
        return combinedImage;
    }

    // ========== 二维码解析 ==========

    /**
     * 解析二维码文件
     * 
     * @param filePath 文件路径
     * @return 解析结果
     * @throws Exception 异常
     */
    public static String parseQRCodeFromFile(String filePath) throws Exception {
        BufferedImage image = ImageIO.read(new File(filePath));
        return parseQRCodeFromImage(image);
    }

    /**
     * 解析二维码输入流
     * 
     * @param inputStream 输入流
     * @return 解析结果
     * @throws Exception 异常
     */
    public static String parseQRCodeFromStream(InputStream inputStream) throws Exception {
        BufferedImage image = ImageIO.read(inputStream);
        return parseQRCodeFromImage(image);
    }

    /**
     * 解析二维码字节数组
     * 
     * @param bytes 字节数组
     * @return 解析结果
     * @throws Exception 异常
     */
    public static String parseQRCodeFromBytes(byte[] bytes) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage image = ImageIO.read(bais);
        return parseQRCodeFromImage(image);
    }

    /**
     * 解析二维码图片
     * 
     * @param image 图片
     * @return 解析结果
     * @throws Exception 异常
     */
    public static String parseQRCodeFromImage(BufferedImage image) throws Exception {
        if (image == null) {
            throw new IllegalArgumentException("Image cannot be null");
        }

        LuminanceSource source = new BufferedImageLuminanceSource(image);
        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

        Map<DecodeHintType, Object> hints = new HashMap<>();
        hints.put(DecodeHintType.CHARACTER_SET, DEFAULT_CHARSET);

        MultiFormatReader reader = new MultiFormatReader();
        Result result = reader.decode(bitmap, hints);
        
        return result.getText();
    }

    // ========== 批量操作 ==========

    /**
     * 批量生成二维码
     * 
     * @param contents 内容列表
     * @param outputDir 输出目录
     * @param filePrefix 文件前缀
     * @throws Exception 异常
     */
    public static void batchGenerateQRCodes(String[] contents, String outputDir, String filePrefix) throws Exception {
        batchGenerateQRCodes(contents, outputDir, filePrefix, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 批量生成二维码
     * 
     * @param contents 内容列表
     * @param outputDir 输出目录
     * @param filePrefix 文件前缀
     * @param width 宽度
     * @param height 高度
     * @throws Exception 异常
     */
    public static void batchGenerateQRCodes(String[] contents, String outputDir, String filePrefix, int width, int height) throws Exception {
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        for (int i = 0; i < contents.length; i++) {
            String fileName = filePrefix + "_" + (i + 1) + "." + DEFAULT_FORMAT.toLowerCase();
            String filePath = outputDir + File.separator + fileName;
            generateQRCodeToFile(contents[i], filePath, width, height);
        }
    }

    /**
     * 批量解析二维码
     * 
     * @param filePaths 文件路径列表
     * @return 解析结果列表
     */
    public static String[] batchParseQRCodes(String[] filePaths) {
        String[] results = new String[filePaths.length];
        
        for (int i = 0; i < filePaths.length; i++) {
            try {
                results[i] = parseQRCodeFromFile(filePaths[i]);
            } catch (Exception e) {
                results[i] = "解析失败: " + e.getMessage();
            }
        }
        
        return results;
    }

    // ========== 自定义样式 ==========

    /**
     * 生成自定义颜色的二维码
     * 
     * @param content 内容
     * @param foregroundColor 前景色
     * @param backgroundColor 背景色
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateColoredQRCode(String content, Color foregroundColor, Color backgroundColor) throws Exception {
        return generateColoredQRCode(content, foregroundColor, backgroundColor, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成自定义颜色的二维码
     * 
     * @param content 内容
     * @param foregroundColor 前景色
     * @param backgroundColor 背景色
     * @param width 宽度
     * @param height 高度
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateColoredQRCode(String content, Color foregroundColor, Color backgroundColor, int width, int height) throws Exception {
        BitMatrix bitMatrix = generateBitMatrix(content, width, height);
        
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, bitMatrix.get(x, y) ? foregroundColor.getRGB() : backgroundColor.getRGB());
            }
        }
        
        return image;
    }

    /**
     * 生成圆角二维码
     * 
     * @param content 内容
     * @param cornerRadius 圆角半径
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateRoundedQRCode(String content, int cornerRadius) throws Exception {
        return generateRoundedQRCode(content, cornerRadius, DEFAULT_WIDTH, DEFAULT_HEIGHT);
    }

    /**
     * 生成圆角二维码
     * 
     * @param content 内容
     * @param cornerRadius 圆角半径
     * @param width 宽度
     * @param height 高度
     * @return BufferedImage
     * @throws Exception 异常
     */
    public static BufferedImage generateRoundedQRCode(String content, int cornerRadius, int width, int height) throws Exception {
        BufferedImage qrImage = generateQRCodeImage(content, width, height);
        
        BufferedImage roundedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = roundedImage.createGraphics();
        
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setClip(new java.awt.geom.RoundRectangle2D.Float(0, 0, width, height, cornerRadius, cornerRadius));
        g2d.drawImage(qrImage, 0, 0, null);
        g2d.dispose();
        
        return roundedImage;
    }

    // ========== 工具方法 ==========

    /**
     * 检查是否为有效的二维码图片
     * 
     * @param filePath 文件路径
     * @return 是否有效
     */
    public static boolean isValidQRCode(String filePath) {
        try {
            parseQRCodeFromFile(filePath);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 检查是否为有效的二维码图片
     * 
     * @param image 图片
     * @return 是否有效
     */
    public static boolean isValidQRCode(BufferedImage image) {
        try {
            parseQRCodeFromImage(image);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取二维码信息
     * 
     * @param filePath 文件路径
     * @return 二维码信息
     */
    public static QRCodeInfo getQRCodeInfo(String filePath) {
        try {
            BufferedImage image = ImageIO.read(new File(filePath));
            String content = parseQRCodeFromImage(image);
            
            QRCodeInfo info = new QRCodeInfo();
            info.setContent(content);
            info.setWidth(image.getWidth());
            info.setHeight(image.getHeight());
            info.setValid(true);
            
            return info;
        } catch (Exception e) {
            QRCodeInfo info = new QRCodeInfo();
            info.setValid(false);
            info.setErrorMessage(e.getMessage());
            return info;
        }
    }

    /**
     * 二维码信息类
     */
    public static class QRCodeInfo {
        private String content;
        private int width;
        private int height;
        private boolean valid;
        private String errorMessage;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

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
            return "QRCodeInfo{" +
                    "content='" + content + '\'' +
                    ", width=" + width +
                    ", height=" + height +
                    ", valid=" + valid +
                    ", errorMessage='" + errorMessage + '\'' +
                    '}';
        }
    }
}