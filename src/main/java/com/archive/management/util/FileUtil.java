package com.archive.management.util;

import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * 文件工具类
 * 提供文件操作相关的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class FileUtil {

    /**
     * 私有构造函数，防止实例化
     */
    private FileUtil() {
        throw new UnsupportedOperationException("Utility class cannot be instantiated");
    }

    // ========== 常量定义 ==========
    
    /** 默认缓冲区大小 */
    private static final int DEFAULT_BUFFER_SIZE = 8192;
    
    /** 文件大小单位 */
    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};
    
    /** 允许的图片文件扩展名 */
    private static final Set<String> IMAGE_EXTENSIONS = Set.of(
            "jpg", "jpeg", "png", "gif", "bmp", "webp", "svg", "ico"
    );
    
    /** 允许的文档文件扩展名 */
    private static final Set<String> DOCUMENT_EXTENSIONS = Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "rtf", "odt", "ods", "odp"
    );
    
    /** 允许的音频文件扩展名 */
    private static final Set<String> AUDIO_EXTENSIONS = Set.of(
            "mp3", "wav", "flac", "aac", "ogg", "wma", "m4a"
    );
    
    /** 允许的视频文件扩展名 */
    private static final Set<String> VIDEO_EXTENSIONS = Set.of(
            "mp4", "avi", "mkv", "mov", "wmv", "flv", "webm", "m4v"
    );
    
    /** 允许的压缩文件扩展名 */
    private static final Set<String> ARCHIVE_EXTENSIONS = Set.of(
            "zip", "rar", "7z", "tar", "gz", "bz2", "xz"
    );

    // ========== 文件基础操作 ==========

    /**
     * 检查文件是否存在
     * 
     * @param filePath 文件路径
     * @return 是否存在
     */
    public static boolean exists(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        return Files.exists(Paths.get(filePath));
    }

    /**
     * 检查文件是否存在
     * 
     * @param file 文件对象
     * @return 是否存在
     */
    public static boolean exists(File file) {
        return file != null && file.exists();
    }

    /**
     * 检查路径是否为文件
     * 
     * @param filePath 文件路径
     * @return 是否为文件
     */
    public static boolean isFile(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return false;
        }
        return Files.isRegularFile(Paths.get(filePath));
    }

    /**
     * 检查路径是否为目录
     * 
     * @param dirPath 目录路径
     * @return 是否为目录
     */
    public static boolean isDirectory(String dirPath) {
        if (StringUtil.isEmpty(dirPath)) {
            return false;
        }
        return Files.isDirectory(Paths.get(dirPath));
    }

    /**
     * 创建目录（包括父目录）
     * 
     * @param dirPath 目录路径
     * @return 是否创建成功
     */
    public static boolean createDirectories(String dirPath) {
        if (StringUtil.isEmpty(dirPath)) {
            return false;
        }
        try {
            Files.createDirectories(Paths.get(dirPath));
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 删除文件或目录
     * 
     * @param path 文件或目录路径
     * @return 是否删除成功
     */
    public static boolean delete(String path) {
        if (StringUtil.isEmpty(path)) {
            return false;
        }
        try {
            Path pathObj = Paths.get(path);
            if (Files.isDirectory(pathObj)) {
                return deleteDirectory(pathObj);
            } else {
                Files.deleteIfExists(pathObj);
                return true;
            }
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 递归删除目录
     * 
     * @param directory 目录路径
     * @return 是否删除成功
     */
    private static boolean deleteDirectory(Path directory) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);
            return true;
        }
    }

    /**
     * 复制文件
     * 
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否复制成功
     */
    public static boolean copyFile(String sourcePath, String targetPath) {
        if (StringUtil.isEmpty(sourcePath) || StringUtil.isEmpty(targetPath)) {
            return false;
        }
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            
            // 确保目标目录存在
            Files.createDirectories(target.getParent());
            
            Files.copy(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 移动文件
     * 
     * @param sourcePath 源文件路径
     * @param targetPath 目标文件路径
     * @return 是否移动成功
     */
    public static boolean moveFile(String sourcePath, String targetPath) {
        if (StringUtil.isEmpty(sourcePath) || StringUtil.isEmpty(targetPath)) {
            return false;
        }
        try {
            Path source = Paths.get(sourcePath);
            Path target = Paths.get(targetPath);
            
            // 确保目标目录存在
            Files.createDirectories(target.getParent());
            
            Files.move(source, target, StandardCopyOption.REPLACE_EXISTING);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ========== 文件信息获取 ==========

    /**
     * 获取文件大小
     * 
     * @param filePath 文件路径
     * @return 文件大小（字节），-1表示获取失败
     */
    public static long getFileSize(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return -1;
        }
        try {
            return Files.size(Paths.get(filePath));
        } catch (IOException e) {
            return -1;
        }
    }

    /**
     * 获取文件大小（人类可读格式）
     * 
     * @param filePath 文件路径
     * @return 文件大小字符串
     */
    public static String getFileSizeFormatted(String filePath) {
        long size = getFileSize(filePath);
        return formatFileSize(size);
    }

    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的文件大小
     */
    public static String formatFileSize(long size) {
        if (size < 0) {
            return "Unknown";
        }
        if (size == 0) {
            return "0 B";
        }
        
        int unitIndex = (int) (Math.log(size) / Math.log(1024));
        unitIndex = Math.min(unitIndex, SIZE_UNITS.length - 1);
        
        double value = size / Math.pow(1024, unitIndex);
        DecimalFormat df = new DecimalFormat("#.##");
        
        return df.format(value) + " " + SIZE_UNITS[unitIndex];
    }

    /**
     * 获取文件扩展名
     * 
     * @param fileName 文件名
     * @return 文件扩展名（小写，不包含点）
     */
    public static String getFileExtension(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return "";
        }
        
        return fileName.substring(lastDotIndex + 1).toLowerCase();
    }

    /**
     * 获取不带扩展名的文件名
     * 
     * @param fileName 文件名
     * @return 不带扩展名的文件名
     */
    public static String getFileNameWithoutExtension(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return "";
        }
        
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return fileName;
        }
        
        return fileName.substring(0, lastDotIndex);
    }

    /**
     * 获取文件的MIME类型
     * 
     * @param filePath 文件路径
     * @return MIME类型
     */
    public static String getMimeType(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return "application/octet-stream";
        }
        
        try {
            String mimeType = Files.probeContentType(Paths.get(filePath));
            return mimeType != null ? mimeType : "application/octet-stream";
        } catch (IOException e) {
            return "application/octet-stream";
        }
    }

    /**
     * 获取文件最后修改时间
     * 
     * @param filePath 文件路径
     * @return 最后修改时间
     */
    public static LocalDateTime getLastModifiedTime(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        
        try {
            BasicFileAttributes attrs = Files.readAttributes(Paths.get(filePath), BasicFileAttributes.class);
            return LocalDateTime.ofInstant(attrs.lastModifiedTime().toInstant(), 
                    java.time.ZoneId.systemDefault());
        } catch (IOException e) {
            return null;
        }
    }

    // ========== 文件类型判断 ==========

    /**
     * 判断是否为图片文件
     * 
     * @param fileName 文件名
     * @return 是否为图片文件
     */
    public static boolean isImageFile(String fileName) {
        String extension = getFileExtension(fileName);
        return IMAGE_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为文档文件
     * 
     * @param fileName 文件名
     * @return 是否为文档文件
     */
    public static boolean isDocumentFile(String fileName) {
        String extension = getFileExtension(fileName);
        return DOCUMENT_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为音频文件
     * 
     * @param fileName 文件名
     * @return 是否为音频文件
     */
    public static boolean isAudioFile(String fileName) {
        String extension = getFileExtension(fileName);
        return AUDIO_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为视频文件
     * 
     * @param fileName 文件名
     * @return 是否为视频文件
     */
    public static boolean isVideoFile(String fileName) {
        String extension = getFileExtension(fileName);
        return VIDEO_EXTENSIONS.contains(extension);
    }

    /**
     * 判断是否为压缩文件
     * 
     * @param fileName 文件名
     * @return 是否为压缩文件
     */
    public static boolean isArchiveFile(String fileName) {
        String extension = getFileExtension(fileName);
        return ARCHIVE_EXTENSIONS.contains(extension);
    }

    /**
     * 获取文件类型分类
     * 
     * @param fileName 文件名
     * @return 文件类型分类
     */
    public static String getFileCategory(String fileName) {
        if (isImageFile(fileName)) {
            return "image";
        } else if (isDocumentFile(fileName)) {
            return "document";
        } else if (isAudioFile(fileName)) {
            return "audio";
        } else if (isVideoFile(fileName)) {
            return "video";
        } else if (isArchiveFile(fileName)) {
            return "archive";
        } else {
            return "other";
        }
    }

    // ========== 文件内容操作 ==========

    /**
     * 读取文件内容为字符串
     * 
     * @param filePath 文件路径
     * @return 文件内容
     */
    public static String readFileToString(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return null;
        }
        
        try {
            return Files.readString(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return null;
        }
    }

    /**
     * 读取文件内容为字符串列表
     * 
     * @param filePath 文件路径
     * @return 文件内容行列表
     */
    public static List<String> readFileToLines(String filePath) {
        if (StringUtil.isEmpty(filePath)) {
            return new ArrayList<>();
        }
        
        try {
            return Files.readAllLines(Paths.get(filePath), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 写入字符串到文件
     * 
     * @param filePath 文件路径
     * @param content 文件内容
     * @return 是否写入成功
     */
    public static boolean writeStringToFile(String filePath, String content) {
        if (StringUtil.isEmpty(filePath) || content == null) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 写入字符串列表到文件
     * 
     * @param filePath 文件路径
     * @param lines 文件内容行列表
     * @return 是否写入成功
     */
    public static boolean writeLinesToFile(String filePath, List<String> lines) {
        if (StringUtil.isEmpty(filePath) || lines == null) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.write(path, lines, StandardCharsets.UTF_8);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 追加字符串到文件
     * 
     * @param filePath 文件路径
     * @param content 追加内容
     * @return 是否追加成功
     */
    public static boolean appendStringToFile(String filePath, String content) {
        if (StringUtil.isEmpty(filePath) || content == null) {
            return false;
        }
        
        try {
            Path path = Paths.get(filePath);
            Files.createDirectories(path.getParent());
            Files.writeString(path, content, StandardCharsets.UTF_8, 
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    // ========== MultipartFile 操作 ==========

    /**
     * 保存上传文件
     * 
     * @param file 上传文件
     * @param targetPath 目标路径
     * @return 是否保存成功
     */
    public static boolean saveUploadFile(MultipartFile file, String targetPath) {
        if (file == null || file.isEmpty() || StringUtil.isEmpty(targetPath)) {
            return false;
        }
        
        try {
            Path path = Paths.get(targetPath);
            Files.createDirectories(path.getParent());
            file.transferTo(path);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 生成唯一文件名
     * 
     * @param originalFileName 原始文件名
     * @return 唯一文件名
     */
    public static String generateUniqueFileName(String originalFileName) {
        if (StringUtil.isEmpty(originalFileName)) {
            return UUID.randomUUID().toString();
        }
        
        String extension = getFileExtension(originalFileName);
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String randomStr = StringUtil.randomString(6);
        
        if (StringUtil.isNotEmpty(extension)) {
            return timestamp + "_" + randomStr + "." + extension;
        } else {
            return timestamp + "_" + randomStr;
        }
    }

    /**
     * 验证上传文件
     * 
     * @param file 上传文件
     * @param allowedExtensions 允许的扩展名集合
     * @param maxSize 最大文件大小（字节）
     * @return 验证结果消息，null表示验证通过
     */
    public static String validateUploadFile(MultipartFile file, Set<String> allowedExtensions, long maxSize) {
        if (file == null || file.isEmpty()) {
            return "文件不能为空";
        }
        
        String originalFileName = file.getOriginalFilename();
        if (StringUtil.isEmpty(originalFileName)) {
            return "文件名不能为空";
        }
        
        String extension = getFileExtension(originalFileName);
        if (allowedExtensions != null && !allowedExtensions.isEmpty() && !allowedExtensions.contains(extension)) {
            return "不支持的文件类型：" + extension;
        }
        
        if (maxSize > 0 && file.getSize() > maxSize) {
            return "文件大小超过限制：" + formatFileSize(file.getSize()) + "，最大允许：" + formatFileSize(maxSize);
        }
        
        return null; // 验证通过
    }

    // ========== 文件哈希计算 ==========

    /**
     * 计算文件MD5哈希值
     * 
     * @param filePath 文件路径
     * @return MD5哈希值
     */
    public static String calculateMD5(String filePath) {
        return calculateHash(filePath, "MD5");
    }

    /**
     * 计算文件SHA-256哈希值
     * 
     * @param filePath 文件路径
     * @return SHA-256哈希值
     */
    public static String calculateSHA256(String filePath) {
        return calculateHash(filePath, "SHA-256");
    }

    /**
     * 计算文件哈希值
     * 
     * @param filePath 文件路径
     * @param algorithm 哈希算法
     * @return 哈希值
     */
    public static String calculateHash(String filePath, String algorithm) {
        if (StringUtil.isEmpty(filePath) || StringUtil.isEmpty(algorithm)) {
            return null;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            
            try (InputStream fis = Files.newInputStream(Paths.get(filePath));
                 BufferedInputStream bis = new BufferedInputStream(fis)) {
                
                byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                int bytesRead;
                
                while ((bytesRead = bis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }
            
            byte[] hashBytes = digest.digest();
            StringBuilder sb = new StringBuilder();
            
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            
            return sb.toString();
            
        } catch (NoSuchAlgorithmException | IOException e) {
            return null;
        }
    }

    // ========== 目录操作 ==========

    /**
     * 列出目录下的所有文件
     * 
     * @param dirPath 目录路径
     * @param recursive 是否递归
     * @return 文件路径列表
     */
    public static List<String> listFiles(String dirPath, boolean recursive) {
        if (StringUtil.isEmpty(dirPath) || !isDirectory(dirPath)) {
            return new ArrayList<>();
        }
        
        try {
            Path dir = Paths.get(dirPath);
            
            if (recursive) {
                try (Stream<Path> paths = Files.walk(dir)) {
                    return paths.filter(Files::isRegularFile)
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            } else {
                try (Stream<Path> paths = Files.list(dir)) {
                    return paths.filter(Files::isRegularFile)
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 列出目录下的所有子目录
     * 
     * @param dirPath 目录路径
     * @param recursive 是否递归
     * @return 目录路径列表
     */
    public static List<String> listDirectories(String dirPath, boolean recursive) {
        if (StringUtil.isEmpty(dirPath) || !isDirectory(dirPath)) {
            return new ArrayList<>();
        }
        
        try {
            Path dir = Paths.get(dirPath);
            
            if (recursive) {
                try (Stream<Path> paths = Files.walk(dir)) {
                    return paths.filter(Files::isDirectory)
                            .filter(path -> !path.equals(dir)) // 排除根目录本身
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            } else {
                try (Stream<Path> paths = Files.list(dir)) {
                    return paths.filter(Files::isDirectory)
                            .map(Path::toString)
                            .collect(Collectors.toList());
                }
            }
        } catch (IOException e) {
            return new ArrayList<>();
        }
    }

    /**
     * 计算目录大小
     * 
     * @param dirPath 目录路径
     * @return 目录大小（字节）
     */
    public static long calculateDirectorySize(String dirPath) {
        if (StringUtil.isEmpty(dirPath) || !isDirectory(dirPath)) {
            return 0;
        }
        
        try (Stream<Path> paths = Files.walk(Paths.get(dirPath))) {
            return paths.filter(Files::isRegularFile)
                    .mapToLong(path -> {
                        try {
                            return Files.size(path);
                        } catch (IOException e) {
                            return 0;
                        }
                    })
                    .sum();
        } catch (IOException e) {
            return 0;
        }
    }

    // ========== 压缩和解压缩 ==========

    /**
     * 压缩文件或目录为ZIP
     * 
     * @param sourcePath 源路径
     * @param zipPath ZIP文件路径
     * @return 是否压缩成功
     */
    public static boolean zipFile(String sourcePath, String zipPath) {
        if (StringUtil.isEmpty(sourcePath) || StringUtil.isEmpty(zipPath)) {
            return false;
        }
        
        try (FileOutputStream fos = new FileOutputStream(zipPath);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            Path source = Paths.get(sourcePath);
            
            if (Files.isRegularFile(source)) {
                zipSingleFile(source, source.getFileName().toString(), zos);
            } else if (Files.isDirectory(source)) {
                zipDirectory(source, source, zos);
            }
            
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 压缩单个文件
     */
    private static void zipSingleFile(Path file, String entryName, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        zos.putNextEntry(entry);
        
        try (InputStream fis = Files.newInputStream(file)) {
            byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                zos.write(buffer, 0, bytesRead);
            }
        }
        
        zos.closeEntry();
    }

    /**
     * 压缩目录
     */
    private static void zipDirectory(Path directory, Path basePath, ZipOutputStream zos) throws IOException {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .forEach(file -> {
                        try {
                            String entryName = basePath.relativize(file).toString().replace('\\', '/');
                            zipSingleFile(file, entryName, zos);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
        }
    }

    /**
     * 解压ZIP文件
     * 
     * @param zipPath ZIP文件路径
     * @param targetDir 目标目录
     * @return 是否解压成功
     */
    public static boolean unzipFile(String zipPath, String targetDir) {
        if (StringUtil.isEmpty(zipPath) || StringUtil.isEmpty(targetDir)) {
            return false;
        }
        
        try (FileInputStream fis = new FileInputStream(zipPath);
             ZipInputStream zis = new ZipInputStream(fis)) {
            
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path targetPath = Paths.get(targetDir, entry.getName());
                
                // 安全检查，防止目录遍历攻击
                if (!targetPath.normalize().startsWith(Paths.get(targetDir).normalize())) {
                    continue;
                }
                
                if (entry.isDirectory()) {
                    Files.createDirectories(targetPath);
                } else {
                    Files.createDirectories(targetPath.getParent());
                    
                    try (OutputStream fos = Files.newOutputStream(targetPath)) {
                        byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
                        int bytesRead;
                        while ((bytesRead = zis.read(buffer)) != -1) {
                            fos.write(buffer, 0, bytesRead);
                        }
                    }
                }
                
                zis.closeEntry();
            }
            
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}