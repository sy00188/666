package com.archive.management.service;

import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * 多格式导出服务接口
 * 支持同时导出多种格式（Excel、PDF、CSV）
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
public interface MultiFormatExportService {
    
    /**
     * 导出到多种格式并打包为ZIP
     * 
     * @param data 导出数据
     * @param headers 表头
     * @param fieldNames 字段名
     * @param fileName 文件名前缀
     * @param formats 需要导出的格式列表（excel/csv/pdf）
     * @param outputStream 输出流
     * @throws Exception 导出异常
     */
    void exportToMultipleFormats(
        List<Map<String, Object>> data,
        List<String> headers,
        List<String> fieldNames,
        String fileName,
        List<String> formats,
        OutputStream outputStream
    ) throws Exception;
    
    /**
     * 导出配置类
     */
    class ExportConfig {
        private List<Map<String, Object>> data;
        private List<String> headers;
        private List<String> fieldNames;
        private String title;
        private String sheetName;
        
        // Getters and Setters
        public List<Map<String, Object>> getData() { return data; }
        public void setData(List<Map<String, Object>> data) { this.data = data; }
        
        public List<String> getHeaders() { return headers; }
        public void setHeaders(List<String> headers) { this.headers = headers; }
        
        public List<String> getFieldNames() { return fieldNames; }
        public void setFieldNames(List<String> fieldNames) { this.fieldNames = fieldNames; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public String getSheetName() { return sheetName; }
        public void setSheetName(String sheetName) { this.sheetName = sheetName; }
    }
}

