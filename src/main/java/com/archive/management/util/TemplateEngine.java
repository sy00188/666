package com.archive.management.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.ConcurrentHashMap;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

/**
 * 模板引擎工具类
 * 用于处理消息模板的渲染和变量替换
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class TemplateEngine {

    /**
     * 变量占位符模式 ${variable}
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");
    
    /**
     * 条件语句模式 #{if condition}...#{endif}
     */
    private static final Pattern CONDITION_PATTERN = Pattern.compile("#\\{if\\s+([^}]+)\\}(.*?)#\\{endif\\}", Pattern.DOTALL);
    
    /**
     * 循环语句模式 #{foreach item in items}...#{endforeach}
     */
    private static final Pattern FOREACH_PATTERN = Pattern.compile("#\\{foreach\\s+(\\w+)\\s+in\\s+(\\w+)\\}(.*?)#\\{endforeach\\}", Pattern.DOTALL);
    
    /**
     * 模板缓存
     */
    private final Map<String, CompiledTemplate> templateCache = new ConcurrentHashMap<>();
    
    /**
     * 渲染模板
     * 
     * @param template 模板内容
     * @param variables 变量映射
     * @return 渲染后的内容
     */
    public String render(String template, Map<String, Object> variables) {
        if (!StringUtils.hasText(template)) {
            return "";
        }
        
        if (variables == null) {
            variables = new ConcurrentHashMap<>();
        }
        
        try {
            // 添加系统变量
            addSystemVariables(variables);
            
            // 处理条件语句
            String processed = processConditions(template, variables);
            
            // 处理循环语句
            processed = processForeach(processed, variables);
            
            // 处理变量替换
            processed = processVariables(processed, variables);
            
            return processed;
        } catch (Exception e) {
            log.error("模板渲染失败: {}", e.getMessage(), e);
            return template; // 返回原模板
        }
    }
    
    /**
     * 编译模板（用于缓存优化）
     * 
     * @param templateId 模板ID
     * @param template 模板内容
     * @return 编译后的模板
     */
    public CompiledTemplate compile(String templateId, String template) {
        if (!StringUtils.hasText(template)) {
            return new CompiledTemplate(templateId, "", new ArrayList<>());
        }
        
        try {
            // 提取模板中的变量
            List<String> variables = extractVariables(template);
            
            CompiledTemplate compiledTemplate = new CompiledTemplate(templateId, template, variables);
            templateCache.put(templateId, compiledTemplate);
            
            log.debug("模板编译完成: {}, 变量数量: {}", templateId, variables.size());
            return compiledTemplate;
        } catch (Exception e) {
            log.error("模板编译失败: {}", e.getMessage(), e);
            return new CompiledTemplate(templateId, template, new ArrayList<>());
        }
    }
    
    /**
     * 使用编译后的模板渲染
     * 
     * @param templateId 模板ID
     * @param variables 变量映射
     * @return 渲染后的内容
     */
    public String renderCompiled(String templateId, Map<String, Object> variables) {
        CompiledTemplate compiledTemplate = templateCache.get(templateId);
        if (compiledTemplate == null) {
            log.warn("未找到编译后的模板: {}", templateId);
            return "";
        }
        
        return render(compiledTemplate.getTemplate(), variables);
    }
    
    /**
     * 验证模板语法
     * 
     * @param template 模板内容
     * @return 验证结果
     */
    public TemplateValidationResult validateTemplate(String template) {
        TemplateValidationResult result = new TemplateValidationResult();
        
        if (!StringUtils.hasText(template)) {
            result.addError("模板内容不能为空");
            return result;
        }
        
        try {
            // 检查变量语法
            validateVariableSyntax(template, result);
            
            // 检查条件语句语法
            validateConditionSyntax(template, result);
            
            // 检查循环语句语法
            validateForeachSyntax(template, result);
            
            // 检查嵌套结构
            validateNestingStructure(template, result);
            
        } catch (Exception e) {
            result.addError("模板验证异常: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 提取模板中的变量
     * 
     * @param template 模板内容
     * @return 变量列表
     */
    public List<String> extractVariables(String template) {
        List<String> variables = new ArrayList<>();
        
        if (!StringUtils.hasText(template)) {
            return variables;
        }
        
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            if (!variables.contains(variable)) {
                variables.add(variable);
            }
        }
        
        return variables;
    }
    
    /**
     * 清除模板缓存
     */
    public void clearCache() {
        templateCache.clear();
        log.info("模板缓存已清除");
    }
    
    /**
     * 获取缓存统计信息
     * 
     * @return 缓存统计
     */
    public CacheStats getCacheStats() {
        return new CacheStats(templateCache.size(), templateCache.keySet());
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 添加系统变量
     */
    private void addSystemVariables(Map<String, Object> variables) {
        variables.put("currentTime", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        variables.put("currentDate", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")));
        variables.put("timestamp", System.currentTimeMillis());
    }
    
    /**
     * 处理条件语句
     */
    private String processConditions(String template, Map<String, Object> variables) {
        Matcher matcher = CONDITION_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String condition = matcher.group(1).trim();
            String content = matcher.group(2);
            
            boolean conditionResult = evaluateCondition(condition, variables);
            String replacement = conditionResult ? content : "";
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 处理循环语句
     */
    private String processForeach(String template, Map<String, Object> variables) {
        Matcher matcher = FOREACH_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String itemVar = matcher.group(1);
            String collectionVar = matcher.group(2);
            String content = matcher.group(3);
            
            String replacement = processForeachLoop(itemVar, collectionVar, content, variables);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 处理变量替换
     */
    private String processVariables(String template, Map<String, Object> variables) {
        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuffer result = new StringBuffer();
        
        while (matcher.find()) {
            String variable = matcher.group(1).trim();
            Object value = getVariableValue(variable, variables);
            String replacement = value != null ? value.toString() : "";
            
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        
        matcher.appendTail(result);
        return result.toString();
    }
    
    /**
     * 评估条件表达式
     */
    private boolean evaluateCondition(String condition, Map<String, Object> variables) {
        try {
            // 简单的条件评估逻辑
            if (condition.contains("==")) {
                String[] parts = condition.split("==");
                if (parts.length == 2) {
                    Object left = getVariableValue(parts[0].trim(), variables);
                    String right = parts[1].trim().replace("\"", "").replace("'", "");
                    return Objects.equals(String.valueOf(left), right);
                }
            } else if (condition.contains("!=")) {
                String[] parts = condition.split("!=");
                if (parts.length == 2) {
                    Object left = getVariableValue(parts[0].trim(), variables);
                    String right = parts[1].trim().replace("\"", "").replace("'", "");
                    return !Objects.equals(String.valueOf(left), right);
                }
            } else {
                // 简单的存在性检查
                Object value = getVariableValue(condition, variables);
                return value != null && !value.toString().isEmpty();
            }
        } catch (Exception e) {
            log.warn("条件评估失败: {}", condition, e);
        }
        
        return false;
    }
    
    /**
     * 处理foreach循环
     */
    private String processForeachLoop(String itemVar, String collectionVar, String content, Map<String, Object> variables) {
        Object collection = variables.get(collectionVar);
        if (!(collection instanceof List)) {
            return "";
        }
        
        @SuppressWarnings("unchecked")
        List<Object> items = (List<Object>) collection;
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < items.size(); i++) {
            Map<String, Object> loopVariables = new ConcurrentHashMap<>(variables);
            loopVariables.put(itemVar, items.get(i));
            loopVariables.put(itemVar + "_index", i);
            loopVariables.put(itemVar + "_first", i == 0);
            loopVariables.put(itemVar + "_last", i == items.size() - 1);
            
            String processedContent = processVariables(content, loopVariables);
            result.append(processedContent);
        }
        
        return result.toString();
    }
    
    /**
     * 获取变量值
     */
    private Object getVariableValue(String variable, Map<String, Object> variables) {
        if (variable.contains(".")) {
            // 支持嵌套属性访问，如 user.name
            String[] parts = variable.split("\\.");
            Object current = variables.get(parts[0]);
            
            for (int i = 1; i < parts.length && current != null; i++) {
                if (current instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) current;
                    current = map.get(parts[i]);
                } else {
                    // 可以扩展支持反射访问对象属性
                    current = null;
                }
            }
            
            return current;
        } else {
            return variables.get(variable);
        }
    }
    
    /**
     * 验证变量语法
     */
    private void validateVariableSyntax(String template, TemplateValidationResult result) {
        // 检查未闭合的变量
        int openCount = 0;
        int closeCount = 0;
        
        for (int i = 0; i < template.length() - 1; i++) {
            if (template.charAt(i) == '$' && template.charAt(i + 1) == '{') {
                openCount++;
            } else if (template.charAt(i) == '}') {
                closeCount++;
            }
        }
        
        if (openCount != closeCount) {
            result.addError("变量语法错误：未闭合的变量占位符");
        }
    }
    
    /**
     * 验证条件语句语法
     */
    private void validateConditionSyntax(String template, TemplateValidationResult result) {
        int ifCount = countOccurrences(template, "#{if");
        int endifCount = countOccurrences(template, "#{endif}");
        
        if (ifCount != endifCount) {
            result.addError("条件语句语法错误：if和endif数量不匹配");
        }
    }
    
    /**
     * 验证循环语句语法
     */
    private void validateForeachSyntax(String template, TemplateValidationResult result) {
        int foreachCount = countOccurrences(template, "#{foreach");
        int endforeachCount = countOccurrences(template, "#{endforeach}");
        
        if (foreachCount != endforeachCount) {
            result.addError("循环语句语法错误：foreach和endforeach数量不匹配");
        }
    }
    
    /**
     * 验证嵌套结构
     */
    private void validateNestingStructure(String template, TemplateValidationResult result) {
        // 简单的嵌套检查，可以扩展更复杂的逻辑
        if (template.contains("#{if") && template.contains("#{foreach")) {
            // 检查嵌套是否正确
            // 这里可以实现更复杂的嵌套验证逻辑
        }
    }
    
    /**
     * 计算字符串出现次数
     */
    private int countOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length();
        }
        return count;
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 编译后的模板
     */
    public static class CompiledTemplate {
        private final String id;
        private final String template;
        private final List<String> variables;
        private final LocalDateTime compiledAt;
        
        public CompiledTemplate(String id, String template, List<String> variables) {
            this.id = id;
            this.template = template;
            this.variables = new ArrayList<>(variables);
            this.compiledAt = LocalDateTime.now();
        }
        
        // Getters
        public String getId() { return id; }
        public String getTemplate() { return template; }
        public List<String> getVariables() { return new ArrayList<>(variables); }
        public LocalDateTime getCompiledAt() { return compiledAt; }
    }
    
    /**
     * 模板验证结果
     */
    public static class TemplateValidationResult {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        
        public void addError(String error) {
            errors.add(error);
        }
        
        public void addWarning(String warning) {
            warnings.add(warning);
        }
        
        public boolean isValid() {
            return errors.isEmpty();
        }
        
        public List<String> getErrors() { return new ArrayList<>(errors); }
        public List<String> getWarnings() { return new ArrayList<>(warnings); }
    }
    
    /**
     * 缓存统计信息
     */
    public static class CacheStats {
        private final int size;
        private final List<String> templateIds;
        
        public CacheStats(int size, java.util.Set<String> templateIds) {
            this.size = size;
            this.templateIds = new ArrayList<>(templateIds);
        }
        
        public int getSize() { return size; }
        public List<String> getTemplateIds() { return new ArrayList<>(templateIds); }
    }
}