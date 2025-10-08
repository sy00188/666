package com.archive.management.service.impl;

import com.archive.management.service.MultiFormatExportService;
import com.archive.management.util.ExcelUtil;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * 多格式导出服务实现
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-15
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MultiFormatExportServiceImpl implements MultiFormatExportService {
    
    private final ExcelUtil excelUtil;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public void exportToMultipleFormats(
            List<Map<String, Object>> data,
            List<String> headers,
            List<String> fieldNames,
            String fileName,
            List<String> formats,
            OutputStream outputStream) throws Exception {
        
        log.info("开始多格式导出: fileName={}, formats={}, dataSize={}",
            fileName, formats, data.size());
        
        try (ZipOutputStream zipOut = new ZipOutputStream(outputStream)) {
            
            for (String format : formats) {
                byte[] fileData = null;
                String fileExtension = "";
                
                switch (format.toLowerCase()) {
                    case "excel":
                        fileData = exportToExcel(data, headers, fieldNames);
                        fileExtension = ".xlsx";
                        break;
                    case "csv":
                        fileData = exportToCsv(data, headers, fieldNames);
                        fileExtension = ".csv";
                        break;
                    case "pdf":
                        fileData = exportToPdf(data, headers, fieldNames, fileName);
                        fileExtension = ".pdf";
                        break;
                    default:
                        log.warn("不支持的导出格式: {}", format);
                        continue;
                }
                
                if (fileData != null) {
                    // 添加到ZIP文件
                    ZipEntry zipEntry = new ZipEntry(fileName + fileExtension);
                    zipOut.putNextEntry(zipEntry);
                    zipOut.write(fileData);
                    zipOut.closeEntry();
                    
                    log.debug("已添加文件到ZIP: {}{}", fileName, fileExtension);
                }
            }
            
            zipOut.finish();
            log.info("多格式导出完成: {} 个格式", formats.size());
            
        } catch (Exception e) {
            log.error("多格式导出失败", e);
            throw new RuntimeException("多格式导出失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 导出到Excel
     */
    private byte[] exportToExcel(List<Map<String, Object>> data, List<String> headers, List<String> fieldNames) {
        try {
            return excelUtil.exportToExcel(data, headers, fieldNames, "数据导出");
        } catch (Exception e) {
            log.error("Excel导出失败", e);
            throw new RuntimeException("Excel导出失败", e);
        }
    }
    
    /**
     * 导出到CSV
     */
    private byte[] exportToCsv(List<Map<String, Object>> data, List<String> headers, List<String> fieldNames) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter writer = new OutputStreamWriter(out, "UTF-8")) {
            
            // 写入UTF-8 BOM
            writer.write('\ufeff');
            
            // 写入表头
            writer.write(String.join(",", headers));
            writer.write("\n");
            
            // 写入数据
            for (Map<String, Object> row : data) {
                StringBuilder line = new StringBuilder();
                for (int i = 0; i < fieldNames.size(); i++) {
                    if (i > 0) {
                        line.append(",");
                    }
                    Object value = row.get(fieldNames.get(i));
                    line.append(formatCsvValue(value));
                }
                writer.write(line.toString());
                writer.write("\n");
            }
            
            writer.flush();
            return out.toByteArray();
            
        } catch (Exception e) {
            log.error("CSV导出失败", e);
            throw new RuntimeException("CSV导出失败", e);
        }
    }
    
    /**
     * 导出到PDF
     */
    private byte[] exportToPdf(List<Map<String, Object>> data, List<String> headers, 
                              List<String> fieldNames, String title) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Document document = new Document(PageSize.A4.rotate()); // 横向
            PdfWriter.getInstance(document, out);
            document.open();
            
            // 设置中文字体
            BaseFont bfChinese = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H", BaseFont.NOT_EMBEDDED);
            Font titleFont = new Font(bfChinese, 16, Font.BOLD);
            Font headerFont = new Font(bfChinese, 10, Font.BOLD);
            Font dataFont = new Font(bfChinese, 9, Font.NORMAL);
            
            // 添加标题
            if (title != null && !title.isEmpty()) {
                Paragraph titlePara = new Paragraph(title, titleFont);
                titlePara.setAlignment(Element.ALIGN_CENTER);
                document.add(titlePara);
                document.add(Chunk.NEWLINE);
            }
            
            // 创建表格
            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);
            
            // 添加表头
            for (String header : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
                cell.setBackgroundColor(BaseColor.LIGHT_GRAY);
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setPadding(5);
                table.addCell(cell);
            }
            
            // 添加数据
            for (Map<String, Object> row : data) {
                for (String fieldName : fieldNames) {
                    Object value = row.get(fieldName);
                    String strValue = formatPdfValue(value);
                    PdfPCell cell = new PdfPCell(new Phrase(strValue, dataFont));
                    cell.setHorizontalAlignment(Element.ALIGN_LEFT);
                    cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                    cell.setPadding(3);
                    table.addCell(cell);
                }
            }
            
            document.add(table);
            document.close();
            
            return out.toByteArray();
            
        } catch (Exception e) {
            log.error("PDF导出失败", e);
            throw new RuntimeException("PDF导出失败", e);
        }
    }
    
    /**
     * 格式化CSV值
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
        
        // 处理特殊字符
        if (strValue.contains(",") || strValue.contains("\"") || strValue.contains("\n")) {
            strValue = "\"" + strValue.replace("\"", "\"\"") + "\"";
        }
        
        return strValue;
    }
    
    /**
     * 格式化PDF值
     */
    private String formatPdfValue(Object value) {
        if (value == null) {
            return "";
        }
        
        if (value instanceof LocalDateTime) {
            return ((LocalDateTime) value).format(DATE_FORMATTER);
        }
        
        return value.toString();
    }
}

