package com.archive.management.util;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.*;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.Date;

/**
 * Excel工具类
 * 提供Excel文件读写的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class ExcelUtil {

    /**
     * Excel文件扩展名
     */
    public static final String EXCEL_XLS = ".xls";
    public static final String EXCEL_XLSX = ".xlsx";

    /**
     * 默认日期格式
     */
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    /**
     * 最大行数限制
     */
    public static final int MAX_ROWS_XLS = 65536;
    public static final int MAX_ROWS_XLSX = 1048576;

    // ========== 文件类型检查 ==========

    /**
     * 检查是否是Excel文件
     * 
     * @param fileName 文件名
     * @return 是否是Excel文件
     */
    public static boolean isExcelFile(String fileName) {
        if (StringUtil.isEmpty(fileName)) {
            return false;
        }
        
        String lowerName = fileName.toLowerCase();
        return lowerName.endsWith(EXCEL_XLS) || lowerName.endsWith(EXCEL_XLSX);
    }

    /**
     * 检查是否是Excel文件
     * 
     * @param file 上传文件
     * @return 是否是Excel文件
     */
    public static boolean isExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        
        String fileName = file.getOriginalFilename();
        String contentType = file.getContentType();
        
        return isExcelFile(fileName) && (
            "application/vnd.ms-excel".equals(contentType) ||
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet".equals(contentType)
        );
    }

    /**
     * 判断是否是xlsx格式
     * 
     * @param fileName 文件名
     * @return 是否是xlsx格式
     */
    public static boolean isXlsxFile(String fileName) {
        return StringUtil.isNotEmpty(fileName) && fileName.toLowerCase().endsWith(EXCEL_XLSX);
    }

    // ========== 工作簿创建 ==========

    /**
     * 创建工作簿
     * 
     * @param isXlsx 是否创建xlsx格式
     * @return 工作簿对象
     */
    public static Workbook createWorkbook(boolean isXlsx) {
        return isXlsx ? new XSSFWorkbook() : new HSSFWorkbook();
    }

    /**
     * 创建工作簿（根据文件名判断格式）
     * 
     * @param fileName 文件名
     * @return 工作簿对象
     */
    public static Workbook createWorkbook(String fileName) {
        return createWorkbook(isXlsxFile(fileName));
    }

    /**
     * 打开工作簿
     * 
     * @param inputStream 输入流
     * @return 工作簿对象
     * @throws IOException IO异常
     */
    public static Workbook openWorkbook(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            throw new IllegalArgumentException("输入流不能为空");
        }
        
        return WorkbookFactory.create(inputStream);
    }

    /**
     * 打开工作簿
     * 
     * @param file 文件
     * @return 工作簿对象
     * @throws IOException IO异常
     */
    public static Workbook openWorkbook(File file) throws IOException {
        if (file == null || !file.exists()) {
            throw new IllegalArgumentException("文件不存在");
        }
        
        return WorkbookFactory.create(file);
    }

    /**
     * 打开工作簿
     * 
     * @param multipartFile 上传文件
     * @return 工作簿对象
     * @throws IOException IO异常
     */
    public static Workbook openWorkbook(MultipartFile multipartFile) throws IOException {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalArgumentException("上传文件不能为空");
        }
        
        if (!isExcelFile(multipartFile)) {
            throw new IllegalArgumentException("不是有效的Excel文件");
        }
        
        return openWorkbook(multipartFile.getInputStream());
    }

    // ========== 工作表操作 ==========

    /**
     * 创建工作表
     * 
     * @param workbook 工作簿
     * @param sheetName 工作表名称
     * @return 工作表对象
     */
    public static Sheet createSheet(Workbook workbook, String sheetName) {
        if (workbook == null) {
            throw new IllegalArgumentException("工作簿不能为空");
        }
        
        return StringUtil.isNotEmpty(sheetName) ? 
            workbook.createSheet(sheetName) : workbook.createSheet();
    }

    /**
     * 获取工作表
     * 
     * @param workbook 工作簿
     * @param sheetIndex 工作表索引
     * @return 工作表对象
     */
    public static Sheet getSheet(Workbook workbook, int sheetIndex) {
        if (workbook == null) {
            return null;
        }
        
        return sheetIndex >= 0 && sheetIndex < workbook.getNumberOfSheets() ? 
            workbook.getSheetAt(sheetIndex) : null;
    }

    /**
     * 获取工作表
     * 
     * @param workbook 工作簿
     * @param sheetName 工作表名称
     * @return 工作表对象
     */
    public static Sheet getSheet(Workbook workbook, String sheetName) {
        if (workbook == null || StringUtil.isEmpty(sheetName)) {
            return null;
        }
        
        return workbook.getSheet(sheetName);
    }

    /**
     * 获取第一个工作表
     * 
     * @param workbook 工作簿
     * @return 工作表对象
     */
    public static Sheet getFirstSheet(Workbook workbook) {
        return getSheet(workbook, 0);
    }

    // ========== 单元格操作 ==========

    /**
     * 创建单元格
     * 
     * @param row 行对象
     * @param columnIndex 列索引
     * @return 单元格对象
     */
    public static Cell createCell(Row row, int columnIndex) {
        if (row == null) {
            return null;
        }
        
        return row.createCell(columnIndex);
    }

    /**
     * 获取单元格
     * 
     * @param sheet 工作表
     * @param rowIndex 行索引
     * @param columnIndex 列索引
     * @return 单元格对象
     */
    public static Cell getCell(Sheet sheet, int rowIndex, int columnIndex) {
        if (sheet == null) {
            return null;
        }
        
        Row row = sheet.getRow(rowIndex);
        if (row == null) {
            return null;
        }
        
        return row.getCell(columnIndex);
    }

    /**
     * 设置单元格值
     * 
     * @param cell 单元格
     * @param value 值
     */
    public static void setCellValue(Cell cell, Object value) {
        if (cell == null) {
            return;
        }
        
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        
        if (value instanceof String) {
            cell.setCellValue((String) value);
        } else if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else if (value instanceof Date) {
            cell.setCellValue((Date) value);
        } else if (value instanceof LocalDate) {
            cell.setCellValue(((LocalDate) value).format(DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT)));
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT)));
        } else {
            cell.setCellValue(value.toString());
        }
    }

    /**
     * 获取单元格值
     * 
     * @param cell 单元格
     * @return 单元格值
     */
    public static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    double numericValue = cell.getNumericCellValue();
                    // 判断是否为整数
                    if (numericValue == Math.floor(numericValue)) {
                        return (long) numericValue;
                    } else {
                        return numericValue;
                    }
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
            default:
                return null;
        }
    }

    /**
     * 获取单元格字符串值
     * 
     * @param cell 单元格
     * @return 字符串值
     */
    public static String getCellStringValue(Cell cell) {
        Object value = getCellValue(cell);
        return value != null ? value.toString().trim() : "";
    }

    // ========== 样式设置 ==========

    /**
     * 创建标题样式
     * 
     * @param workbook 工作簿
     * @return 样式对象
     */
    public static CellStyle createHeaderStyle(Workbook workbook) {
        if (workbook == null) {
            return null;
        }
        
        CellStyle style = workbook.createCellStyle();
        
        // 设置字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        style.setFont(font);
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 设置背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        return style;
    }

    /**
     * 创建数据样式
     * 
     * @param workbook 工作簿
     * @return 样式对象
     */
    public static CellStyle createDataStyle(Workbook workbook) {
        if (workbook == null) {
            return null;
        }
        
        CellStyle style = workbook.createCellStyle();
        
        // 设置对齐方式
        style.setAlignment(HorizontalAlignment.LEFT);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 设置边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        return style;
    }

    /**
     * 创建日期样式
     * 
     * @param workbook 工作簿
     * @param format 日期格式
     * @return 样式对象
     */
    public static CellStyle createDateStyle(Workbook workbook, String format) {
        if (workbook == null) {
            return null;
        }
        
        CellStyle style = createDataStyle(workbook);
        
        DataFormat dataFormat = workbook.createDataFormat();
        style.setDataFormat(dataFormat.getFormat(StringUtil.isNotEmpty(format) ? format : DEFAULT_DATE_FORMAT));
        
        return style;
    }

    // ========== 数据导出 ==========

    /**
     * 导出数据到Excel
     * 
     * @param <T> 数据类型
     * @param data 数据列表
     * @param headers 表头
     * @param fieldNames 字段名列表
     * @param fileName 文件名
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    public static <T> void exportToExcel(List<T> data, String[] headers, String[] fieldNames, 
                                        String fileName, HttpServletResponse response) throws IOException {
        
        if (headers == null || fieldNames == null || headers.length != fieldNames.length) {
            throw new IllegalArgumentException("表头和字段名数量不匹配");
        }
        
        // 创建工作簿
        Workbook workbook = createWorkbook(isXlsxFile(fileName));
        Sheet sheet = createSheet(workbook, "数据");
        
        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = createCell(headerRow, i);
            setCellValue(cell, headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        if (data != null && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                T item = data.get(i);
                
                for (int j = 0; j < fieldNames.length; j++) {
                    Cell cell = createCell(dataRow, j);
                    Object value = getFieldValue(item, fieldNames[j]);
                    setCellValue(cell, value);
                    cell.setCellStyle(dataStyle);
                }
            }
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 输出到响应
        outputToResponse(workbook, fileName, response);
    }

    /**
     * 导出简单数据到Excel
     * 
     * @param data 数据列表（Map格式）
     * @param headers 表头
     * @param keys 键名列表
     * @param fileName 文件名
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    public static void exportMapToExcel(List<Map<String, Object>> data, String[] headers, String[] keys,
                                       String fileName, HttpServletResponse response) throws IOException {
        
        if (headers == null || keys == null || headers.length != keys.length) {
            throw new IllegalArgumentException("表头和键名数量不匹配");
        }
        
        // 创建工作簿
        Workbook workbook = createWorkbook(isXlsxFile(fileName));
        Sheet sheet = createSheet(workbook, "数据");
        
        // 创建样式
        CellStyle headerStyle = createHeaderStyle(workbook);
        CellStyle dataStyle = createDataStyle(workbook);
        
        // 创建表头
        Row headerRow = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = createCell(headerRow, i);
            setCellValue(cell, headers[i]);
            cell.setCellStyle(headerStyle);
        }
        
        // 填充数据
        if (data != null && !data.isEmpty()) {
            for (int i = 0; i < data.size(); i++) {
                Row dataRow = sheet.createRow(i + 1);
                Map<String, Object> item = data.get(i);
                
                for (int j = 0; j < keys.length; j++) {
                    Cell cell = createCell(dataRow, j);
                    Object value = item.get(keys[j]);
                    setCellValue(cell, value);
                    cell.setCellStyle(dataStyle);
                }
            }
        }
        
        // 自动调整列宽
        for (int i = 0; i < headers.length; i++) {
            sheet.autoSizeColumn(i);
        }
        
        // 输出到响应
        outputToResponse(workbook, fileName, response);
    }

    // ========== 数据导入 ==========

    /**
     * 从Excel导入数据
     * 
     * @param <T> 数据类型
     * @param multipartFile 上传文件
     * @param clazz 数据类型
     * @param fieldNames 字段名列表
     * @param startRow 开始行（0为第一行）
     * @return 数据列表
     * @throws IOException IO异常
     */
    public static <T> List<T> importFromExcel(MultipartFile multipartFile, Class<T> clazz, 
                                             String[] fieldNames, int startRow) throws IOException {
        
        if (!isExcelFile(multipartFile)) {
            throw new IllegalArgumentException("不是有效的Excel文件");
        }
        
        List<T> result = new ArrayList<>();
        
        try (Workbook workbook = openWorkbook(multipartFile)) {
            Sheet sheet = getFirstSheet(workbook);
            if (sheet == null) {
                return result;
            }
            
            int lastRowNum = sheet.getLastRowNum();
            for (int i = Math.max(startRow, 0); i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                try {
                    T item = clazz.newInstance();
                    boolean hasData = false;
                    
                    for (int j = 0; j < fieldNames.length; j++) {
                        Cell cell = row.getCell(j);
                        Object value = getCellValue(cell);
                        
                        if (value != null && StringUtil.isNotEmpty(value.toString())) {
                            setFieldValue(item, fieldNames[j], value);
                            hasData = true;
                        }
                    }
                    
                    if (hasData) {
                        result.add(item);
                    }
                    
                } catch (Exception e) {
                    // 跳过无法处理的行
                }
            }
        }
        
        return result;
    }

    /**
     * 从Excel导入数据（Map格式）
     * 
     * @param multipartFile 上传文件
     * @param keys 键名列表
     * @param startRow 开始行
     * @return 数据列表
     * @throws IOException IO异常
     */
    public static List<Map<String, Object>> importMapFromExcel(MultipartFile multipartFile, 
                                                              String[] keys, int startRow) throws IOException {
        
        if (!isExcelFile(multipartFile)) {
            throw new IllegalArgumentException("不是有效的Excel文件");
        }
        
        List<Map<String, Object>> result = new ArrayList<>();
        
        try (Workbook workbook = openWorkbook(multipartFile)) {
            Sheet sheet = getFirstSheet(workbook);
            if (sheet == null) {
                return result;
            }
            
            int lastRowNum = sheet.getLastRowNum();
            for (int i = Math.max(startRow, 0); i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                Map<String, Object> item = new HashMap<>();
                boolean hasData = false;
                
                for (int j = 0; j < keys.length; j++) {
                    Cell cell = row.getCell(j);
                    Object value = getCellValue(cell);
                    
                    item.put(keys[j], value);
                    
                    if (value != null && StringUtil.isNotEmpty(value.toString())) {
                        hasData = true;
                    }
                }
                
                if (hasData) {
                    result.add(item);
                }
            }
        }
        
        return result;
    }

    // ========== 辅助方法 ==========

    /**
     * 输出工作簿到HTTP响应
     * 
     * @param workbook 工作簿
     * @param fileName 文件名
     * @param response HTTP响应
     * @throws IOException IO异常
     */
    private static void outputToResponse(Workbook workbook, String fileName, HttpServletResponse response) throws IOException {
        if (workbook == null || response == null) {
            return;
        }
        
        try {
            // 设置响应头
            HttpUtil.setDownloadHeaders(response, fileName);
            
            // 输出文件
            try (OutputStream outputStream = response.getOutputStream()) {
                workbook.write(outputStream);
                outputStream.flush();
            }
            
        } finally {
            workbook.close();
        }
    }

    /**
     * 获取对象字段值
     * 
     * @param obj 对象
     * @param fieldName 字段名
     * @return 字段值
     */
    private static Object getFieldValue(Object obj, String fieldName) {
        if (obj == null || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 设置对象字段值
     * 
     * @param obj 对象
     * @param fieldName 字段名
     * @param value 字段值
     */
    private static void setFieldValue(Object obj, String fieldName, Object value) {
        if (obj == null || StringUtil.isEmpty(fieldName)) {
            return;
        }
        
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            
            Class<?> fieldType = field.getType();
            Object convertedValue = convertValue(value, fieldType);
            
            field.set(obj, convertedValue);
        } catch (Exception e) {
            // 忽略转换失败的字段
        }
    }

    /**
     * 转换值类型
     * 
     * @param value 原值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType == null) {
            return null;
        }
        
        String stringValue = value.toString().trim();
        if (StringUtil.isEmpty(stringValue)) {
            return null;
        }
        
        try {
            if (targetType == String.class) {
                return stringValue;
            } else if (targetType == Integer.class || targetType == int.class) {
                return Integer.valueOf(stringValue);
            } else if (targetType == Long.class || targetType == long.class) {
                return Long.valueOf(stringValue);
            } else if (targetType == Double.class || targetType == double.class) {
                return Double.valueOf(stringValue);
            } else if (targetType == Float.class || targetType == float.class) {
                return Float.valueOf(stringValue);
            } else if (targetType == BigDecimal.class) {
                return new BigDecimal(stringValue);
            } else if (targetType == Boolean.class || targetType == boolean.class) {
                return Boolean.valueOf(stringValue);
            } else if (targetType == LocalDate.class) {
                return LocalDate.parse(stringValue, DateTimeFormatter.ofPattern(DEFAULT_DATE_FORMAT));
            } else if (targetType == LocalDateTime.class) {
                return LocalDateTime.parse(stringValue, DateTimeFormatter.ofPattern(DEFAULT_DATETIME_FORMAT));
            }
        } catch (Exception e) {
            // 转换失败返回null
        }
        
        return null;
    }

    /**
     * 验证Excel文件大小
     * 
     * @param multipartFile 上传文件
     * @param maxSize 最大大小（字节）
     * @return 是否有效
     */
    public static boolean validateFileSize(MultipartFile multipartFile, long maxSize) {
        return multipartFile != null && multipartFile.getSize() <= maxSize;
    }

    /**
     * 获取Excel文件行数
     * 
     * @param multipartFile 上传文件
     * @return 行数
     * @throws IOException IO异常
     */
    public static int getRowCount(MultipartFile multipartFile) throws IOException {
        if (!isExcelFile(multipartFile)) {
            return 0;
        }
        
        try (Workbook workbook = openWorkbook(multipartFile)) {
            Sheet sheet = getFirstSheet(workbook);
            return sheet != null ? sheet.getLastRowNum() + 1 : 0;
        }
    }
}