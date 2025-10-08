package com.archive.management.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Excel工具类
 * 支持导入导出功能
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class ExcelUtil {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 导出数据到Excel
     */
    public byte[] exportToExcel(List<Map<String, Object>> dataList, List<String> headers, List<String> keys, String sheetName) {
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            Sheet sheet = workbook.createSheet(sheetName);
            
            // 创建标题样式
            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle dataStyle = createDataStyle(workbook);
            
            // 创建标题行
            Row headerRow = sheet.createRow(0);
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 4000); // 设置列宽
            }
            
            // 填充数据
            for (int i = 0; i < dataList.size(); i++) {
                Row row = sheet.createRow(i + 1);
                Map<String, Object> data = dataList.get(i);
                
                for (int j = 0; j < keys.size(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = data.get(keys.get(j));
                    
                    if (value != null) {
                        if (value instanceof Number) {
                            cell.setCellValue(((Number) value).doubleValue());
                        } else if (value instanceof LocalDateTime) {
                            cell.setCellValue(((LocalDateTime) value).format(DATE_FORMATTER));
                        } else {
                            cell.setCellValue(value.toString());
                        }
                    }
                    
                    cell.setCellStyle(dataStyle);
                }
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (Exception e) {
            log.error("导出Excel失败", e);
            throw new RuntimeException("导出Excel失败: " + e.getMessage());
        }
    }

    /**
     * 从Excel导入数据
     */
    public List<Map<String, Object>> importFromExcel(InputStream inputStream, List<String> keys) {
        List<Map<String, Object>> dataList = new ArrayList<>();
        
        try (Workbook workbook = WorkbookFactory.create(inputStream)) {
            Sheet sheet = workbook.getSheetAt(0);
            
            // 跳过标题行
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }
                
                Map<String, Object> data = new HashMap<>();
                boolean hasData = false;
                
                for (int j = 0; j < keys.size(); j++) {
                    Cell cell = row.getCell(j);
                    Object value = getCellValue(cell);
                    
                    if (value != null && !value.toString().trim().isEmpty()) {
                        data.put(keys.get(j), value);
                        hasData = true;
                    }
                }
                
                if (hasData) {
                    dataList.add(data);
                }
            }
            
            log.info("从Excel导入数据成功: 共{}条", dataList.size());
            return dataList;
            
        } catch (Exception e) {
            log.error("导入Excel失败", e);
            throw new RuntimeException("导入Excel失败: " + e.getMessage());
        }
    }

    /**
     * 获取单元格值
     */
    private Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }
        
        switch (cell.getCellType()) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                }
                return cell.getNumericCellValue();
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            default:
                return null;
        }
    }

    /**
     * 创建标题样式
     */
    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 背景色
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 居中
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        // 字体
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        
        return style;
    }

    /**
     * 创建数据样式
     */
    private CellStyle createDataStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        // 边框
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        
        // 垂直居中
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        return style;
    }

    /**
     * 验证Excel数据
     */
    public Map<String, Object> validateExcelData(List<Map<String, Object>> dataList, List<String> requiredKeys) {
        Map<String, Object> result = new HashMap<>();
        List<String> errors = new ArrayList<>();
        
        for (int i = 0; i < dataList.size(); i++) {
            Map<String, Object> data = dataList.get(i);
            
            for (String key : requiredKeys) {
                if (!data.containsKey(key) || data.get(key) == null || data.get(key).toString().trim().isEmpty()) {
                    errors.add(String.format("第%d行缺少必填字段: %s", i + 2, key));
                }
            }
        }
        
        result.put("valid", errors.isEmpty());
        result.put("errors", errors);
        result.put("totalRecords", dataList.size());
        
        return result;
    }
    
    // ==================== 高级Excel功能 ====================
    
    /**
     * 高级导出配置类
     */
    public static class AdvancedExportConfig {
        private List<Map<String, Object>> dataList;
        private List<String> headers;
        private List<String> keys;
        private String sheetName = "Sheet1";
        private String title; // 顶部标题
        private boolean autoColumnWidth = true; // 自动列宽
        private boolean freezeHeader = true; // 冻结表头
        private Map<Integer, Integer> columnWidths = new HashMap<>(); // 自定义列宽
        private Map<String, CellStyleConfig> columnStyles = new HashMap<>(); // 列样式配置
        private List<MergeRegion> mergeRegions = new ArrayList<>(); // 合并单元格
        private List<FormulaCell> formulaCells = new ArrayList<>(); // 公式单元格
        private List<DataValidationConfig> validations = new ArrayList<>(); // 数据验证
        private List<ConditionalFormattingRule> conditionalFormatting = new ArrayList<>(); // 条件格式
        
        // Getters and Setters
        public List<Map<String, Object>> getDataList() { return dataList; }
        public void setDataList(List<Map<String, Object>> dataList) { this.dataList = dataList; }
        
        public List<String> getHeaders() { return headers; }
        public void setHeaders(List<String> headers) { this.headers = headers; }
        
        public List<String> getKeys() { return keys; }
        public void setKeys(List<String> keys) { this.keys = keys; }
        
        public String getSheetName() { return sheetName; }
        public void setSheetName(String sheetName) { this.sheetName = sheetName; }
        
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        
        public boolean isAutoColumnWidth() { return autoColumnWidth; }
        public void setAutoColumnWidth(boolean autoColumnWidth) { this.autoColumnWidth = autoColumnWidth; }
        
        public boolean isFreezeHeader() { return freezeHeader; }
        public void setFreezeHeader(boolean freezeHeader) { this.freezeHeader = freezeHeader; }
        
        public Map<Integer, Integer> getColumnWidths() { return columnWidths; }
        public void setColumnWidths(Map<Integer, Integer> columnWidths) { this.columnWidths = columnWidths; }
        
        public Map<String, CellStyleConfig> getColumnStyles() { return columnStyles; }
        public void setColumnStyles(Map<String, CellStyleConfig> columnStyles) { this.columnStyles = columnStyles; }
        
        public List<MergeRegion> getMergeRegions() { return mergeRegions; }
        public void setMergeRegions(List<MergeRegion> mergeRegions) { this.mergeRegions = mergeRegions; }
        
        public List<FormulaCell> getFormulaCells() { return formulaCells; }
        public void setFormulaCells(List<FormulaCell> formulaCells) { this.formulaCells = formulaCells; }
        
        public List<DataValidationConfig> getValidations() { return validations; }
        public void setValidations(List<DataValidationConfig> validations) { this.validations = validations; }
        
        public List<ConditionalFormattingRule> getConditionalFormatting() { return conditionalFormatting; }
        public void setConditionalFormatting(List<ConditionalFormattingRule> conditionalFormatting) { 
            this.conditionalFormatting = conditionalFormatting; 
        }
    }
    
    /**
     * 单元格样式配置
     */
    public static class CellStyleConfig {
        private Short backgroundColor;
        private Short fontColor;
        private Boolean bold;
        private Short fontSize;
        private HorizontalAlignment alignment;
        private String numberFormat; // 数字格式，如 "#,##0.00"
        
        // Getters and Setters
        public Short getBackgroundColor() { return backgroundColor; }
        public void setBackgroundColor(Short backgroundColor) { this.backgroundColor = backgroundColor; }
        
        public Short getFontColor() { return fontColor; }
        public void setFontColor(Short fontColor) { this.fontColor = fontColor; }
        
        public Boolean getBold() { return bold; }
        public void setBold(Boolean bold) { this.bold = bold; }
        
        public Short getFontSize() { return fontSize; }
        public void setFontSize(Short fontSize) { this.fontSize = fontSize; }
        
        public HorizontalAlignment getAlignment() { return alignment; }
        public void setAlignment(HorizontalAlignment alignment) { this.alignment = alignment; }
        
        public String getNumberFormat() { return numberFormat; }
        public void setNumberFormat(String numberFormat) { this.numberFormat = numberFormat; }
    }
    
    /**
     * 合并单元格区域
     */
    public static class MergeRegion {
        private int firstRow;
        private int lastRow;
        private int firstCol;
        private int lastCol;
        
        public MergeRegion(int firstRow, int lastRow, int firstCol, int lastCol) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
        }
        
        public int getFirstRow() { return firstRow; }
        public int getLastRow() { return lastRow; }
        public int getFirstCol() { return firstCol; }
        public int getLastCol() { return lastCol; }
    }
    
    /**
     * 公式单元格
     */
    public static class FormulaCell {
        private int row;
        private int col;
        private String formula;
        
        public FormulaCell(int row, int col, String formula) {
            this.row = row;
            this.col = col;
            this.formula = formula;
        }
        
        public int getRow() { return row; }
        public int getCol() { return col; }
        public String getFormula() { return formula; }
    }
    
    /**
     * 数据验证配置
     */
    public static class DataValidationConfig {
        private int firstRow;
        private int lastRow;
        private int firstCol;
        private int lastCol;
        private String[] options; // 下拉选项
        
        public DataValidationConfig(int firstRow, int lastRow, int firstCol, int lastCol, String[] options) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
            this.options = options;
        }
        
        public int getFirstRow() { return firstRow; }
        public int getLastRow() { return lastRow; }
        public int getFirstCol() { return firstCol; }
        public int getLastCol() { return lastCol; }
        public String[] getOptions() { return options; }
    }
    
    /**
     * 条件格式规则
     */
    public static class ConditionalFormattingRule {
        private int firstRow;
        private int lastRow;
        private int firstCol;
        private int lastCol;
        private String condition; // 条件表达式
        private Short backgroundColor;
        private Short fontColor;
        
        public ConditionalFormattingRule(int firstRow, int lastRow, int firstCol, int lastCol, 
                                        String condition, Short backgroundColor, Short fontColor) {
            this.firstRow = firstRow;
            this.lastRow = lastRow;
            this.firstCol = firstCol;
            this.lastCol = lastCol;
            this.condition = condition;
            this.backgroundColor = backgroundColor;
            this.fontColor = fontColor;
        }
        
        public int getFirstRow() { return firstRow; }
        public int getLastRow() { return lastRow; }
        public int getFirstCol() { return firstCol; }
        public int getLastCol() { return lastCol; }
        public String getCondition() { return condition; }
        public Short getBackgroundColor() { return backgroundColor; }
        public Short getFontColor() { return fontColor; }
    }
    
    /**
     * 高级导出功能
     */
    public byte[] advancedExport(AdvancedExportConfig config) {
        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            
            XSSFSheet sheet = workbook.createSheet(config.getSheetName());
            int currentRow = 0;
            
            // 1. 添加标题（如果有）
            if (config.getTitle() != null && !config.getTitle().isEmpty()) {
                Row titleRow = sheet.createRow(currentRow++);
                Cell titleCell = titleRow.createCell(0);
                titleCell.setCellValue(config.getTitle());
                titleCell.setCellStyle(createTitleStyle(workbook));
                
                // 合并标题行
                sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, config.getHeaders().size() - 1));
                currentRow++; // 空一行
            }
            
            // 2. 创建表头
            Row headerRow = sheet.createRow(currentRow);
            CellStyle headerStyle = createHeaderStyle(workbook);
            for (int i = 0; i < config.getHeaders().size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(config.getHeaders().get(i));
                cell.setCellStyle(headerStyle);
            }
            
            int headerRowIndex = currentRow;
            currentRow++;
            
            // 3. 填充数据
            for (int i = 0; i < config.getDataList().size(); i++) {
                Row row = sheet.createRow(currentRow + i);
                Map<String, Object> data = config.getDataList().get(i);
                
                for (int j = 0; j < config.getKeys().size(); j++) {
                    Cell cell = row.createCell(j);
                    Object value = data.get(config.getKeys().get(j));
                    
                    // 设置单元格值
                    setCellValue(cell, value);
                    
                    // 应用列样式
                    if (config.getColumnStyles().containsKey(config.getKeys().get(j))) {
                        CellStyle customStyle = applyCustomStyle(workbook, config.getColumnStyles().get(config.getKeys().get(j)));
                        cell.setCellStyle(customStyle);
                    } else {
                        cell.setCellStyle(createDataStyle(workbook));
                    }
                }
            }
            
            // 4. 设置列宽
            if (config.isAutoColumnWidth()) {
                for (int i = 0; i < config.getHeaders().size(); i++) {
                    sheet.autoSizeColumn(i);
                    // 增加一点额外宽度
                    int width = sheet.getColumnWidth(i) + 500;
                    sheet.setColumnWidth(i, Math.min(width, 255 * 256));
                }
            }
            
            // 应用自定义列宽
            config.getColumnWidths().forEach((col, width) -> sheet.setColumnWidth(col, width));
            
            // 5. 冻结窗格（冻结表头）
            if (config.isFreezeHeader()) {
                sheet.createFreezePane(0, headerRowIndex + 1);
            }
            
            // 6. 合并单元格
            for (MergeRegion region : config.getMergeRegions()) {
                sheet.addMergedRegion(new CellRangeAddress(
                    region.getFirstRow(), region.getLastRow(),
                    region.getFirstCol(), region.getLastCol()
                ));
            }
            
            // 7. 添加公式
            for (FormulaCell formulaCell : config.getFormulaCells()) {
                Row row = sheet.getRow(formulaCell.getRow());
                if (row == null) {
                    row = sheet.createRow(formulaCell.getRow());
                }
                Cell cell = row.createCell(formulaCell.getCol());
                cell.setCellFormula(formulaCell.getFormula());
            }
            
            // 8. 添加数据验证（下拉列表）
            XSSFDataValidationHelper validationHelper = new XSSFDataValidationHelper(sheet);
            for (DataValidationConfig validation : config.getValidations()) {
                CellRangeAddressList addressList = new CellRangeAddressList(
                    validation.getFirstRow(), validation.getLastRow(),
                    validation.getFirstCol(), validation.getLastCol()
                );
                DataValidationConstraint constraint = validationHelper.createExplicitListConstraint(validation.getOptions());
                DataValidation dataValidation = validationHelper.createValidation(constraint, addressList);
                dataValidation.setSuppressDropDownArrow(true);
                sheet.addValidationData(dataValidation);
            }
            
            // 9. 应用条件格式
            XSSFSheetConditionalFormatting sheetCF = sheet.getSheetConditionalFormatting();
            for (ConditionalFormattingRule rule : config.getConditionalFormatting()) {
                CellRangeAddress[] regions = {new CellRangeAddress(
                    rule.getFirstRow(), rule.getLastRow(),
                    rule.getFirstCol(), rule.getLastCol()
                )};
                
                XSSFConditionalFormattingRule cfRule = sheetCF.createConditionalFormattingRule(rule.getCondition());
                XSSFFontFormatting fontFmt = cfRule.createFontFormatting();
                if (rule.getFontColor() != null) {
                    fontFmt.setFontColorIndex(rule.getFontColor());
                }
                
                XSSFPatternFormatting patternFmt = cfRule.createPatternFormatting();
                if (rule.getBackgroundColor() != null) {
                    patternFmt.setFillBackgroundColor(new XSSFColor(new byte[]{
                        (byte)rule.getBackgroundColor().intValue(), 0, 0
                    }, null));
                }
                
                sheetCF.addConditionalFormatting(regions, cfRule);
            }
            
            workbook.write(out);
            return out.toByteArray();
            
        } catch (Exception e) {
            log.error("高级Excel导出失败", e);
            throw new RuntimeException("高级Excel导出失败: " + e.getMessage());
        }
    }
    
    /**
     * 创建标题样式
     */
    private CellStyle createTitleStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        style.setFont(font);
        
        return style;
    }
    
    /**
     * 设置单元格值（支持多种类型）
     */
    private void setCellValue(Cell cell, Object value) {
        if (value == null) {
            cell.setCellValue("");
            return;
        }
        
        if (value instanceof Number) {
            cell.setCellValue(((Number) value).doubleValue());
        } else if (value instanceof LocalDateTime) {
            cell.setCellValue(((LocalDateTime) value).format(DATE_FORMATTER));
        } else if (value instanceof Boolean) {
            cell.setCellValue((Boolean) value);
        } else {
            cell.setCellValue(value.toString());
        }
    }
    
    /**
     * 应用自定义样式
     */
    private CellStyle applyCustomStyle(Workbook workbook, CellStyleConfig config) {
        CellStyle style = createDataStyle(workbook);
        
        if (config.getBackgroundColor() != null) {
            style.setFillForegroundColor(config.getBackgroundColor());
            style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        }
        
        if (config.getAlignment() != null) {
            style.setAlignment(config.getAlignment());
        }
        
        if (config.getNumberFormat() != null) {
            DataFormat format = workbook.createDataFormat();
            style.setDataFormat(format.getFormat(config.getNumberFormat()));
        }
        
        Font font = workbook.createFont();
        if (config.getFontColor() != null) {
            font.setColor(config.getFontColor());
        }
        if (config.getBold() != null && config.getBold()) {
            font.setBold(true);
        }
        if (config.getFontSize() != null) {
            font.setFontHeightInPoints(config.getFontSize());
        }
        style.setFont(font);
        
        return style;
    }
}
