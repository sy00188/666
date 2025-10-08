package com.archive.management.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 流式导出服务接口
 * 支持大数据量导出，内存占用可控
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface StreamExportService {
    
    /**
     * 流式导出到Excel
     * 
     * @param outputStream 输出流
     * @param dataProvider 数据提供者（分页查询）
     * @param headers 表头
     * @param fieldNames 字段名列表
     * @param sheetName 工作表名称
     * @param progressCallback 进度回调（可选）
     * @param totalCount 总记录数（用于计算进度）
     * @throws Exception 导出异常
     */
    void exportToExcelStream(
        OutputStream outputStream,
        DataProvider dataProvider,
        List<String> headers,
        List<String> fieldNames,
        String sheetName,
        Consumer<ExportProgress> progressCallback,
        long totalCount
    ) throws Exception;
    
    /**
     * 流式导出到CSV
     * 
     * @param outputStream 输出流
     * @param dataProvider 数据提供者
     * @param headers 表头
     * @param fieldNames 字段名列表
     * @param progressCallback 进度回调
     * @param totalCount 总记录数
     * @throws Exception 导出异常
     */
    void exportToCsvStream(
        OutputStream outputStream,
        DataProvider dataProvider,
        List<String> headers,
        List<String> fieldNames,
        Consumer<ExportProgress> progressCallback,
        long totalCount
    ) throws Exception;
    
    /**
     * 数据提供者接口
     * 用于分批查询数据
     */
    @FunctionalInterface
    interface DataProvider {
        /**
         * 获取指定页的数据
         * 
         * @param pageNum 页码（从1开始）
         * @param pageSize 每页大小
         * @return 数据列表
         */
        List<Map<String, Object>> getData(int pageNum, int pageSize);
    }
    
    /**
     * 导出进度信息
     */
    class ExportProgress {
        private long totalCount;      // 总记录数
        private long processedCount;  // 已处理记录数
        private int percentage;        // 完成百分比
        private String message;        // 进度消息
        private long startTime;        // 开始时间
        private long elapsedTime;      // 已用时间（毫秒）
        private long estimatedTime;    // 预计剩余时间（毫秒）
        
        public ExportProgress(long totalCount, long processedCount) {
            this.totalCount = totalCount;
            this.processedCount = processedCount;
            this.percentage = totalCount > 0 ? (int) ((processedCount * 100) / totalCount) : 0;
        }
        
        // Getters and Setters
        public long getTotalCount() {
            return totalCount;
        }
        
        public void setTotalCount(long totalCount) {
            this.totalCount = totalCount;
        }
        
        public long getProcessedCount() {
            return processedCount;
        }
        
        public void setProcessedCount(long processedCount) {
            this.processedCount = processedCount;
            this.percentage = totalCount > 0 ? (int) ((processedCount * 100) / totalCount) : 0;
        }
        
        public int getPercentage() {
            return percentage;
        }
        
        public void setPercentage(int percentage) {
            this.percentage = percentage;
        }
        
        public String getMessage() {
            return message;
        }
        
        public void setMessage(String message) {
            this.message = message;
        }
        
        public long getStartTime() {
            return startTime;
        }
        
        public void setStartTime(long startTime) {
            this.startTime = startTime;
        }
        
        public long getElapsedTime() {
            return elapsedTime;
        }
        
        public void setElapsedTime(long elapsedTime) {
            this.elapsedTime = elapsedTime;
        }
        
        public long getEstimatedTime() {
            return estimatedTime;
        }
        
        public void setEstimatedTime(long estimatedTime) {
            this.estimatedTime = estimatedTime;
        }
        
        @Override
        public String toString() {
            return String.format("导出进度: %d/%d (%d%%) - %s", 
                processedCount, totalCount, percentage, message);
        }
    }
}

