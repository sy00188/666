package com.archive.management.service.impl;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.alibaba.excel.write.style.column.LongestMatchColumnWidthStyleStrategy;
import com.archive.management.service.StreamExportService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.BufferedWriter;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/**
 * 流式导出服务实现
 * 使用EasyExcel实现大数据量导出，内存占用低
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Slf4j
@Service
public class StreamExportServiceImpl implements StreamExportService {
    
    private static final int DEFAULT_BATCH_SIZE = 1000; // 默认每批处理1000条
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void exportToExcelStream(
            OutputStream outputStream,
            DataProvider dataProvider,
            List<String> headers,
            List<String> fieldNames,
            String sheetName,
            Consumer<ExportProgress> progressCallback,
            long totalCount) throws Exception {
        
        log.info("开始流式导出Excel，总记录数: {}", totalCount);
        long startTime = System.currentTimeMillis();
        
        try {
            // 创建ExcelWriter
            ExcelWriter excelWriter = EasyExcel.write(outputStream)
                    .autoCloseStream(false)
                    .registerWriteHandler(new LongestMatchColumnWidthStyleStrategy())
                    .build();
            
            // 创建Sheet
            WriteSheet writeSheet = EasyExcel.writerSheet(0, sheetName)
                    .head(buildHead(headers))
                    .build();
            
            // 分批查询并写入
            int pageNum = 1;
            long processedCount = 0;
            boolean hasMoreData = true;
            
            while (hasMoreData) {
                // 查询一批数据
                List<Map<String, Object>> batchData = dataProvider.getData(pageNum, DEFAULT_BATCH_SIZE);
                
                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }
                
                // 转换为EasyExcel需要的List<List<Object>>格式
                List<List<Object>> excelData = convertToExcelData(batchData, fieldNames);
                
                // 写入数据
                excelWriter.write(excelData, writeSheet);
                
                // 更新进度
                processedCount += batchData.size();
                if (progressCallback != null) {
                    ExportProgress progress = new ExportProgress(totalCount, processedCount);
                    progress.setStartTime(startTime);
                    progress.setElapsedTime(System.currentTimeMillis() - startTime);
                    
                    // 估算剩余时间
                    if (processedCount > 0) {
                        long avgTimePerRecord = progress.getElapsedTime() / processedCount;
                        progress.setEstimatedTime(avgTimePerRecord * (totalCount - processedCount));
                    }
                    
                    progress.setMessage(String.format("正在导出第 %d 批数据...", pageNum));
                    progressCallback.accept(progress);
                }
                
                // 检查是否还有更多数据
                if (batchData.size() < DEFAULT_BATCH_SIZE) {
                    hasMoreData = false;
                } else {
                    pageNum++;
                }
                
                // 手动触发内存释放
                batchData.clear();
                excelData.clear();
            }
            
            // 关闭ExcelWriter
            excelWriter.finish();
            
            long endTime = System.currentTimeMillis();
            log.info("Excel导出完成，总记录数: {}，耗时: {} ms", processedCount, (endTime - startTime));
            
            // 发送完成进度
            if (progressCallback != null) {
                ExportProgress finalProgress = new ExportProgress(totalCount, processedCount);
                finalProgress.setPercentage(100);
                finalProgress.setMessage("导出完成");
                finalProgress.setElapsedTime(endTime - startTime);
                progressCallback.accept(finalProgress);
            }
            
        } catch (Exception e) {
            log.error("Excel流式导出失败", e);
            throw new RuntimeException("Excel导出失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public void exportToCsvStream(
            OutputStream outputStream,
            DataProvider dataProvider,
            List<String> headers,
            List<String> fieldNames,
            Consumer<ExportProgress> progressCallback,
            long totalCount) throws Exception {
        
        log.info("开始流式导出CSV，总记录数: {}", totalCount);
        long startTime = System.currentTimeMillis();
        
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(outputStream, StandardCharsets.UTF_8))) {
            
            // 写入UTF-8 BOM（Excel打开时正确显示中文）
            writer.write('\ufeff');
            
            // 写入表头
            writer.write(String.join(",", headers));
            writer.newLine();
            
            // 分批查询并写入
            int pageNum = 1;
            long processedCount = 0;
            boolean hasMoreData = true;
            
            while (hasMoreData) {
                // 查询一批数据
                List<Map<String, Object>> batchData = dataProvider.getData(pageNum, DEFAULT_BATCH_SIZE);
                
                if (batchData == null || batchData.isEmpty()) {
                    hasMoreData = false;
                    break;
                }
                
                // 写入数据行
                for (Map<String, Object> row : batchData) {
                    List<String> values = new ArrayList<>();
                    for (String fieldName : fieldNames) {
                        Object value = row.get(fieldName);
                        values.add(formatCsvValue(value));
                    }
                    writer.write(String.join(",", values));
                    writer.newLine();
                }
                
                // 更新进度
                processedCount += batchData.size();
                if (progressCallback != null) {
                    ExportProgress progress = new ExportProgress(totalCount, processedCount);
                    progress.setStartTime(startTime);
                    progress.setElapsedTime(System.currentTimeMillis() - startTime);
                    
                    if (processedCount > 0) {
                        long avgTimePerRecord = progress.getElapsedTime() / processedCount;
                        progress.setEstimatedTime(avgTimePerRecord * (totalCount - processedCount));
                    }
                    
                    progress.setMessage(String.format("正在导出第 %d 批数据...", pageNum));
                    progressCallback.accept(progress);
                }
                
                // 检查是否还有更多数据
                if (batchData.size() < DEFAULT_BATCH_SIZE) {
                    hasMoreData = false;
                } else {
                    pageNum++;
                }
                
                // 手动触发内存释放
                batchData.clear();
            }
            
            writer.flush();
            
            long endTime = System.currentTimeMillis();
            log.info("CSV导出完成，总记录数: {}，耗时: {} ms", processedCount, (endTime - startTime));
            
            // 发送完成进度
            if (progressCallback != null) {
                ExportProgress finalProgress = new ExportProgress(totalCount, processedCount);
                finalProgress.setPercentage(100);
                finalProgress.setMessage("导出完成");
                finalProgress.setElapsedTime(endTime - startTime);
                progressCallback.accept(finalProgress);
            }
            
        } catch (Exception e) {
            log.error("CSV流式导出失败", e);
            throw new RuntimeException("CSV导出失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 构建EasyExcel表头
     */
    private List<List<String>> buildHead(List<String> headers) {
        List<List<String>> head = new ArrayList<>();
        for (String header : headers) {
            List<String> headColumn = new ArrayList<>();
            headColumn.add(header);
            head.add(headColumn);
        }
        return head;
    }
    
    /**
     * 转换数据为EasyExcel格式
     */
    private List<List<Object>> convertToExcelData(List<Map<String, Object>> dataList, List<String> fieldNames) {
        List<List<Object>> excelData = new ArrayList<>();
        
        for (Map<String, Object> row : dataList) {
            List<Object> rowData = new ArrayList<>();
            for (String fieldName : fieldNames) {
                Object value = row.get(fieldName);
                rowData.add(formatExcelValue(value));
            }
            excelData.add(rowData);
        }
        
        return excelData;
    }
    
    /**
     * 格式化Excel单元格值
     */
    private Object formatExcelValue(Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_FORMATTER);
        }
        
        return value;
    }
    
    /**
     * 格式化CSV值（处理逗号、引号等特殊字符）
     */
    private String formatCsvValue(Object value) {
        if (value == null) {
            return "";
        }
        
        String strValue;
        if (value instanceof LocalDateTime) {
            strValue = ((LocalDateTime) value).format(DATE_FORMATTER);
        } else {
            strValue = value.toString();
        }
        
        // 如果包含逗号、引号或换行符，需要用引号包裹并转义引号
        if (strValue.contains(",") || strValue.contains("\"") || strValue.contains("\n")) {
            strValue = "\"" + strValue.replace("\"", "\"\"") + "\"";
        }
        
        return strValue;
    }
}

