package com.archive.management.util;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.*;

/**
 * 压缩解压工具类
 * 提供ZIP文件的压缩和解压功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class ZipUtil {

    private static final int BUFFER_SIZE = 8192;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    // ========== 压缩功能 ==========

    /**
     * 压缩文件或目录
     * 
     * @param sourcePath 源路径
     * @param zipPath ZIP文件路径
     * @throws IOException IO异常
     */
    public static void zip(String sourcePath, String zipPath) throws IOException {
        zip(sourcePath, zipPath, DEFAULT_CHARSET);
    }

    /**
     * 压缩文件或目录
     * 
     * @param sourcePath 源路径
     * @param zipPath ZIP文件路径
     * @param charset 字符编码
     * @throws IOException IO异常
     */
    public static void zip(String sourcePath, String zipPath, Charset charset) throws IOException {
        Path source = Paths.get(sourcePath);
        Path zip = Paths.get(zipPath);
        
        // 确保父目录存在
        Files.createDirectories(zip.getParent());
        
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip), charset)) {
            if (Files.isDirectory(source)) {
                zipDirectory(source, source, zos);
            } else {
                zipFile(source, source.getFileName().toString(), zos);
            }
        }
    }

    /**
     * 压缩多个文件
     * 
     * @param filePaths 文件路径列表
     * @param zipPath ZIP文件路径
     * @throws IOException IO异常
     */
    public static void zipFiles(List<String> filePaths, String zipPath) throws IOException {
        zipFiles(filePaths, zipPath, DEFAULT_CHARSET);
    }

    /**
     * 压缩多个文件
     * 
     * @param filePaths 文件路径列表
     * @param zipPath ZIP文件路径
     * @param charset 字符编码
     * @throws IOException IO异常
     */
    public static void zipFiles(List<String> filePaths, String zipPath, Charset charset) throws IOException {
        Path zip = Paths.get(zipPath);
        Files.createDirectories(zip.getParent());
        
        try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(zip), charset)) {
            for (String filePath : filePaths) {
                Path file = Paths.get(filePath);
                if (Files.exists(file)) {
                    zipFile(file, file.getFileName().toString(), zos);
                }
            }
        }
    }

    /**
     * 压缩目录
     * 
     * @param sourceDir 源目录
     * @param baseDir 基础目录
     * @param zos ZIP输出流
     * @throws IOException IO异常
     */
    private static void zipDirectory(Path sourceDir, Path baseDir, ZipOutputStream zos) throws IOException {
        Files.walkFileTree(sourceDir, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                String entryName = baseDir.relativize(file).toString().replace('\\', '/');
                zipFile(file, entryName, zos);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                if (!dir.equals(sourceDir)) {
                    String entryName = baseDir.relativize(dir).toString().replace('\\', '/') + "/";
                    ZipEntry entry = new ZipEntry(entryName);
                    zos.putNextEntry(entry);
                    zos.closeEntry();
                }
                return FileVisitResult.CONTINUE;
            }
        });
    }

    /**
     * 压缩单个文件
     * 
     * @param file 文件路径
     * @param entryName 条目名称
     * @param zos ZIP输出流
     * @throws IOException IO异常
     */
    private static void zipFile(Path file, String entryName, ZipOutputStream zos) throws IOException {
        ZipEntry entry = new ZipEntry(entryName);
        entry.setTime(Files.getLastModifiedTime(file).toMillis());
        zos.putNextEntry(entry);
        
        try (InputStream is = Files.newInputStream(file)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = is.read(buffer)) > 0) {
                zos.write(buffer, 0, length);
            }
        }
        
        zos.closeEntry();
    }

    // ========== 解压功能 ==========

    /**
     * 解压ZIP文件
     * 
     * @param zipPath ZIP文件路径
     * @param destPath 目标路径
     * @throws IOException IO异常
     */
    public static void unzip(String zipPath, String destPath) throws IOException {
        unzip(zipPath, destPath, DEFAULT_CHARSET);
    }

    /**
     * 解压ZIP文件
     * 
     * @param zipPath ZIP文件路径
     * @param destPath 目标路径
     * @param charset 字符编码
     * @throws IOException IO异常
     */
    public static void unzip(String zipPath, String destPath, Charset charset) throws IOException {
        Path zip = Paths.get(zipPath);
        Path dest = Paths.get(destPath);
        
        if (!Files.exists(zip)) {
            throw new FileNotFoundException("ZIP文件不存在: " + zipPath);
        }
        
        Files.createDirectories(dest);
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = dest.resolve(entry.getName());
                
                // 安全检查，防止目录遍历攻击
                if (!entryPath.normalize().startsWith(dest.normalize())) {
                    throw new IOException("不安全的ZIP条目: " + entry.getName());
                }
                
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    extractFile(zis, entryPath);
                }
                
                zis.closeEntry();
            }
        }
    }

    /**
     * 解压指定文件
     * 
     * @param zipPath ZIP文件路径
     * @param fileName 文件名
     * @param destPath 目标路径
     * @throws IOException IO异常
     */
    public static void unzipFile(String zipPath, String fileName, String destPath) throws IOException {
        unzipFile(zipPath, fileName, destPath, DEFAULT_CHARSET);
    }

    /**
     * 解压指定文件
     * 
     * @param zipPath ZIP文件路径
     * @param fileName 文件名
     * @param destPath 目标路径
     * @param charset 字符编码
     * @throws IOException IO异常
     */
    public static void unzipFile(String zipPath, String fileName, String destPath, Charset charset) throws IOException {
        Path zip = Paths.get(zipPath);
        Path dest = Paths.get(destPath);
        
        Files.createDirectories(dest.getParent());
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(fileName) && !entry.isDirectory()) {
                    extractFile(zis, dest);
                    return;
                }
                zis.closeEntry();
            }
        }
        
        throw new FileNotFoundException("ZIP文件中未找到指定文件: " + fileName);
    }

    /**
     * 提取文件
     * 
     * @param zis ZIP输入流
     * @param destPath 目标路径
     * @throws IOException IO异常
     */
    private static void extractFile(ZipInputStream zis, Path destPath) throws IOException {
        try (OutputStream os = Files.newOutputStream(destPath)) {
            byte[] buffer = new byte[BUFFER_SIZE];
            int length;
            while ((length = zis.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        }
    }

    // ========== 信息查询 ==========

    /**
     * 获取ZIP文件信息
     * 
     * @param zipPath ZIP文件路径
     * @return ZIP文件信息
     * @throws IOException IO异常
     */
    public static ZipInfo getZipInfo(String zipPath) throws IOException {
        return getZipInfo(zipPath, DEFAULT_CHARSET);
    }

    /**
     * 获取ZIP文件信息
     * 
     * @param zipPath ZIP文件路径
     * @param charset 字符编码
     * @return ZIP文件信息
     * @throws IOException IO异常
     */
    public static ZipInfo getZipInfo(String zipPath, Charset charset) throws IOException {
        Path zip = Paths.get(zipPath);
        
        if (!Files.exists(zip)) {
            throw new FileNotFoundException("ZIP文件不存在: " + zipPath);
        }
        
        ZipInfo info = new ZipInfo();
        info.setZipPath(zipPath);
        info.setZipSize(Files.size(zip));
        
        List<ZipEntryInfo> entries = new ArrayList<>();
        long totalUncompressedSize = 0;
        int fileCount = 0;
        int dirCount = 0;
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                ZipEntryInfo entryInfo = new ZipEntryInfo();
                entryInfo.setName(entry.getName());
                entryInfo.setSize(entry.getSize());
                entryInfo.setCompressedSize(entry.getCompressedSize());
                entryInfo.setDirectory(entry.isDirectory());
                entryInfo.setLastModified(entry.getTime());
                
                entries.add(entryInfo);
                
                if (entry.isDirectory()) {
                    dirCount++;
                } else {
                    fileCount++;
                    totalUncompressedSize += entry.getSize();
                }
                
                zis.closeEntry();
            }
        }
        
        info.setEntries(entries);
        info.setTotalUncompressedSize(totalUncompressedSize);
        info.setFileCount(fileCount);
        info.setDirectoryCount(dirCount);
        
        return info;
    }

    /**
     * 列出ZIP文件中的所有条目
     * 
     * @param zipPath ZIP文件路径
     * @return 条目名称列表
     * @throws IOException IO异常
     */
    public static List<String> listZipEntries(String zipPath) throws IOException {
        return listZipEntries(zipPath, DEFAULT_CHARSET);
    }

    /**
     * 列出ZIP文件中的所有条目
     * 
     * @param zipPath ZIP文件路径
     * @param charset 字符编码
     * @return 条目名称列表
     * @throws IOException IO异常
     */
    public static List<String> listZipEntries(String zipPath, Charset charset) throws IOException {
        List<String> entries = new ArrayList<>();
        Path zip = Paths.get(zipPath);
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                entries.add(entry.getName());
                zis.closeEntry();
            }
        }
        
        return entries;
    }

    /**
     * 检查ZIP文件中是否包含指定文件
     * 
     * @param zipPath ZIP文件路径
     * @param fileName 文件名
     * @return 是否包含
     * @throws IOException IO异常
     */
    public static boolean containsFile(String zipPath, String fileName) throws IOException {
        return containsFile(zipPath, fileName, DEFAULT_CHARSET);
    }

    /**
     * 检查ZIP文件中是否包含指定文件
     * 
     * @param zipPath ZIP文件路径
     * @param fileName 文件名
     * @param charset 字符编码
     * @return 是否包含
     * @throws IOException IO异常
     */
    public static boolean containsFile(String zipPath, String fileName, Charset charset) throws IOException {
        Path zip = Paths.get(zipPath);
        
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip), charset)) {
            ZipEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                if (entry.getName().equals(fileName)) {
                    return true;
                }
                zis.closeEntry();
            }
        }
        
        return false;
    }

    // ========== 工具方法 ==========

    /**
     * 检查是否为有效的ZIP文件
     * 
     * @param zipPath ZIP文件路径
     * @return 是否有效
     */
    public static boolean isValidZipFile(String zipPath) {
        try {
            Path zip = Paths.get(zipPath);
            if (!Files.exists(zip)) {
                return false;
            }
            
            try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
                zis.getNextEntry();
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 计算压缩率
     * 
     * @param originalSize 原始大小
     * @param compressedSize 压缩后大小
     * @return 压缩率（百分比）
     */
    public static double calculateCompressionRatio(long originalSize, long compressedSize) {
        if (originalSize == 0) {
            return 0.0;
        }
        return (1.0 - (double) compressedSize / originalSize) * 100;
    }

    /**
     * 格式化文件大小
     * 
     * @param size 文件大小（字节）
     * @return 格式化后的大小
     */
    public static String formatFileSize(long size) {
        if (size < 1024) {
            return size + " B";
        } else if (size < 1024 * 1024) {
            return String.format("%.2f KB", size / 1024.0);
        } else if (size < 1024 * 1024 * 1024) {
            return String.format("%.2f MB", size / (1024.0 * 1024));
        } else {
            return String.format("%.2f GB", size / (1024.0 * 1024 * 1024));
        }
    }

    // ========== 内部类 ==========

    /**
     * ZIP文件信息
     */
    public static class ZipInfo {
        private String zipPath;
        private long zipSize;
        private long totalUncompressedSize;
        private int fileCount;
        private int directoryCount;
        private List<ZipEntryInfo> entries;

        public String getZipPath() {
            return zipPath;
        }

        public void setZipPath(String zipPath) {
            this.zipPath = zipPath;
        }

        public long getZipSize() {
            return zipSize;
        }

        public void setZipSize(long zipSize) {
            this.zipSize = zipSize;
        }

        public long getTotalUncompressedSize() {
            return totalUncompressedSize;
        }

        public void setTotalUncompressedSize(long totalUncompressedSize) {
            this.totalUncompressedSize = totalUncompressedSize;
        }

        public int getFileCount() {
            return fileCount;
        }

        public void setFileCount(int fileCount) {
            this.fileCount = fileCount;
        }

        public int getDirectoryCount() {
            return directoryCount;
        }

        public void setDirectoryCount(int directoryCount) {
            this.directoryCount = directoryCount;
        }

        public List<ZipEntryInfo> getEntries() {
            return entries;
        }

        public void setEntries(List<ZipEntryInfo> entries) {
            this.entries = entries;
        }

        public double getCompressionRatio() {
            return calculateCompressionRatio(totalUncompressedSize, zipSize);
        }

        @Override
        public String toString() {
            return "ZipInfo{" +
                    "zipPath='" + zipPath + '\'' +
                    ", zipSize=" + formatFileSize(zipSize) +
                    ", totalUncompressedSize=" + formatFileSize(totalUncompressedSize) +
                    ", fileCount=" + fileCount +
                    ", directoryCount=" + directoryCount +
                    ", compressionRatio=" + String.format("%.2f%%", getCompressionRatio()) +
                    '}';
        }
    }

    /**
     * ZIP条目信息
     */
    public static class ZipEntryInfo {
        private String name;
        private long size;
        private long compressedSize;
        private boolean directory;
        private long lastModified;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public long getSize() {
            return size;
        }

        public void setSize(long size) {
            this.size = size;
        }

        public long getCompressedSize() {
            return compressedSize;
        }

        public void setCompressedSize(long compressedSize) {
            this.compressedSize = compressedSize;
        }

        public boolean isDirectory() {
            return directory;
        }

        public void setDirectory(boolean directory) {
            this.directory = directory;
        }

        public long getLastModified() {
            return lastModified;
        }

        public void setLastModified(long lastModified) {
            this.lastModified = lastModified;
        }

        public double getCompressionRatio() {
            return calculateCompressionRatio(size, compressedSize);
        }

        @Override
        public String toString() {
            return "ZipEntryInfo{" +
                    "name='" + name + '\'' +
                    ", size=" + formatFileSize(size) +
                    ", compressedSize=" + formatFileSize(compressedSize) +
                    ", directory=" + directory +
                    ", compressionRatio=" + String.format("%.2f%%", getCompressionRatio()) +
                    '}';
        }
    }
}