package com.archive.management.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 权限表达式解析器
 * 用于解析和评估复杂的权限表达式
 * 
 * 支持的表达式语法：
 * - 基本权限：hasPermission('user:read')
 * - 角色检查：hasRole('ADMIN')
 * - 逻辑运算：AND, OR, NOT
 * - 分组：()
 * - 资源权限：hasResourcePermission('document', 'read')
 * - 条件权限：hasConditionalPermission('user:edit', 'ownerId', userId)
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Slf4j
@Component
public class PermissionExpressionParser {

    /**
     * 权限函数模式
     */
    private static final Pattern PERMISSION_FUNCTION_PATTERN = 
        Pattern.compile("(hasPermission|hasRole|hasResourcePermission|hasConditionalPermission)\\s*\\(([^)]+)\\)");
    
    /**
     * 逻辑运算符模式
     */
    private static final Pattern LOGICAL_OPERATOR_PATTERN = 
        Pattern.compile("\\s+(AND|OR|NOT)\\s+", Pattern.CASE_INSENSITIVE);
    
    /**
     * 括号模式
     */
    private static final Pattern PARENTHESES_PATTERN = Pattern.compile("\\(([^()]+)\\)");
    
    /**
     * 支持的权限函数
     */
    public enum PermissionFunction {
        HAS_PERMISSION("hasPermission"),
        HAS_ROLE("hasRole"),
        HAS_RESOURCE_PERMISSION("hasResourcePermission"),
        HAS_CONDITIONAL_PERMISSION("hasConditionalPermission");
        
        private final String functionName;
        
        PermissionFunction(String functionName) {
            this.functionName = functionName;
        }
        
        public String getFunctionName() {
            return functionName;
        }
        
        public static PermissionFunction fromString(String functionName) {
            for (PermissionFunction func : values()) {
                if (func.functionName.equals(functionName)) {
                    return func;
                }
            }
            throw new IllegalArgumentException("未知的权限函数: " + functionName);
        }
    }
    
    /**
     * 逻辑运算符
     */
    public enum LogicalOperator {
        AND, OR, NOT
    }
    
    /**
     * 解析权限表达式
     * 
     * @param expression 权限表达式
     * @return 解析后的表达式树
     */
    public ExpressionNode parseExpression(String expression) {
        if (!StringUtils.hasText(expression)) {
            throw new IllegalArgumentException("权限表达式不能为空");
        }
        
        try {
            // 预处理表达式
            String normalizedExpression = normalizeExpression(expression);
            
            // 验证表达式语法
            validateExpression(normalizedExpression);
            
            // 解析表达式
            return parseExpressionRecursive(normalizedExpression);
        } catch (Exception e) {
            log.error("权限表达式解析失败: {}", expression, e);
            throw new RuntimeException("权限表达式解析失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 评估权限表达式
     * 
     * @param expression 权限表达式
     * @param context 评估上下文
     * @return 评估结果
     */
    public boolean evaluateExpression(String expression, PermissionContext context) {
        ExpressionNode node = parseExpression(expression);
        return evaluateNode(node, context);
    }
    
    /**
     * 评估表达式节点
     * 
     * @param node 表达式节点
     * @param context 评估上下文
     * @return 评估结果
     */
    public boolean evaluateNode(ExpressionNode node, PermissionContext context) {
        if (node == null) {
            return false;
        }
        
        switch (node.getType()) {
            case PERMISSION_FUNCTION:
                return evaluatePermissionFunction(node, context);
            case LOGICAL_OPERATOR:
                return evaluateLogicalOperator(node, context);
            default:
                log.warn("未知的节点类型: {}", node.getType());
                return false;
        }
    }
    
    /**
     * 提取表达式中的所有权限
     * 
     * @param expression 权限表达式
     * @return 权限列表
     */
    public Set<String> extractPermissions(String expression) {
        Set<String> permissions = new HashSet<>();
        
        if (!StringUtils.hasText(expression)) {
            return permissions;
        }
        
        Matcher matcher = PERMISSION_FUNCTION_PATTERN.matcher(expression);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String parameters = matcher.group(2);
            
            if ("hasPermission".equals(functionName) || "hasResourcePermission".equals(functionName)) {
                String[] params = parseParameters(parameters);
                if (params.length > 0) {
                    permissions.add(params[0].replace("'", "").replace("\"", ""));
                }
            }
        }
        
        return permissions;
    }
    
    /**
     * 提取表达式中的所有角色
     * 
     * @param expression 权限表达式
     * @return 角色列表
     */
    public Set<String> extractRoles(String expression) {
        Set<String> roles = new HashSet<>();
        
        if (!StringUtils.hasText(expression)) {
            return roles;
        }
        
        Matcher matcher = PERMISSION_FUNCTION_PATTERN.matcher(expression);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String parameters = matcher.group(2);
            
            if ("hasRole".equals(functionName)) {
                String[] params = parseParameters(parameters);
                if (params.length > 0) {
                    roles.add(params[0].replace("'", "").replace("\"", ""));
                }
            }
        }
        
        return roles;
    }
    
    /**
     * 验证表达式语法
     * 
     * @param expression 权限表达式
     * @return 验证结果
     */
    public ExpressionValidationResult validateExpressionSyntax(String expression) {
        ExpressionValidationResult result = new ExpressionValidationResult();
        
        if (!StringUtils.hasText(expression)) {
            result.addError("权限表达式不能为空");
            return result;
        }
        
        try {
            // 检查括号匹配
            validateParentheses(expression, result);
            
            // 检查权限函数语法
            validatePermissionFunctions(expression, result);
            
            // 检查逻辑运算符
            validateLogicalOperators(expression, result);
            
            // 尝试解析表达式
            parseExpression(expression);
            
        } catch (Exception e) {
            result.addError("表达式解析失败: " + e.getMessage());
        }
        
        return result;
    }
    
    // ==================== 私有方法 ====================
    
    /**
     * 标准化表达式
     */
    private String normalizeExpression(String expression) {
        // 移除多余的空格
        expression = expression.replaceAll("\\s+", " ").trim();
        
        // 标准化逻辑运算符
        expression = expression.replaceAll("(?i)\\s+and\\s+", " AND ");
        expression = expression.replaceAll("(?i)\\s+or\\s+", " OR ");
        expression = expression.replaceAll("(?i)\\s+not\\s+", " NOT ");
        
        return expression;
    }
    
    /**
     * 验证表达式
     */
    private void validateExpression(String expression) {
        // 检查括号匹配
        int openCount = 0;
        int closeCount = 0;
        
        for (char c : expression.toCharArray()) {
            if (c == '(') openCount++;
            if (c == ')') closeCount++;
        }
        
        if (openCount != closeCount) {
            throw new IllegalArgumentException("括号不匹配");
        }
    }
    
    /**
     * 递归解析表达式
     */
    private ExpressionNode parseExpressionRecursive(String expression) {
        expression = expression.trim();
        
        // 处理最外层括号
        if (expression.startsWith("(") && expression.endsWith(")")) {
            return parseExpressionRecursive(expression.substring(1, expression.length() - 1));
        }
        
        // 查找最低优先级的逻辑运算符
        LogicalOperatorInfo operatorInfo = findLowestPriorityOperator(expression);
        
        if (operatorInfo != null) {
            // 创建逻辑运算符节点
            ExpressionNode node = new ExpressionNode(ExpressionNode.NodeType.LOGICAL_OPERATOR);
            node.setOperator(operatorInfo.operator);
            
            if (operatorInfo.operator == LogicalOperator.NOT) {
                // NOT是一元运算符
                String rightExpression = expression.substring(operatorInfo.position + 3).trim();
                node.setRight(parseExpressionRecursive(rightExpression));
            } else {
                // AND和OR是二元运算符
                String leftExpression = expression.substring(0, operatorInfo.position).trim();
                String rightExpression = expression.substring(operatorInfo.position + operatorInfo.length).trim();
                
                node.setLeft(parseExpressionRecursive(leftExpression));
                node.setRight(parseExpressionRecursive(rightExpression));
            }
            
            return node;
        }
        
        // 解析权限函数
        return parsePermissionFunction(expression);
    }
    
    /**
     * 查找最低优先级的逻辑运算符
     */
    private LogicalOperatorInfo findLowestPriorityOperator(String expression) {
        int parenthesesLevel = 0;
        LogicalOperatorInfo result = null;
        
        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);
            
            if (c == '(') {
                parenthesesLevel++;
            } else if (c == ')') {
                parenthesesLevel--;
            } else if (parenthesesLevel == 0) {
                // 检查OR运算符（优先级最低）
                if (i <= expression.length() - 2 && expression.substring(i, i + 2).equals("OR")) {
                    if (i == 0 || expression.charAt(i - 1) == ' ') {
                        if (i + 2 == expression.length() || expression.charAt(i + 2) == ' ') {
                            result = new LogicalOperatorInfo(LogicalOperator.OR, i, 2);
                        }
                    }
                }
                // 检查AND运算符
                else if (i <= expression.length() - 3 && expression.substring(i, i + 3).equals("AND")) {
                    if (i == 0 || expression.charAt(i - 1) == ' ') {
                        if (i + 3 == expression.length() || expression.charAt(i + 3) == ' ') {
                            if (result == null || result.operator != LogicalOperator.OR) {
                                result = new LogicalOperatorInfo(LogicalOperator.AND, i, 3);
                            }
                        }
                    }
                }
                // 检查NOT运算符（优先级最高）
                else if (i <= expression.length() - 3 && expression.substring(i, i + 3).equals("NOT")) {
                    if (i == 0 || expression.charAt(i - 1) == ' ') {
                        if (i + 3 == expression.length() || expression.charAt(i + 3) == ' ') {
                            if (result == null) {
                                result = new LogicalOperatorInfo(LogicalOperator.NOT, i, 3);
                            }
                        }
                    }
                }
            }
        }
        
        return result;
    }
    
    /**
     * 解析权限函数
     */
    private ExpressionNode parsePermissionFunction(String expression) {
        Matcher matcher = PERMISSION_FUNCTION_PATTERN.matcher(expression);
        
        if (matcher.matches()) {
            String functionName = matcher.group(1);
            String parameters = matcher.group(2);
            
            ExpressionNode node = new ExpressionNode(ExpressionNode.NodeType.PERMISSION_FUNCTION);
            node.setFunction(PermissionFunction.fromString(functionName));
            node.setParameters(parseParameters(parameters));
            
            return node;
        }
        
        throw new IllegalArgumentException("无效的权限函数: " + expression);
    }
    
    /**
     * 解析函数参数
     */
    private String[] parseParameters(String parameters) {
        List<String> paramList = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        char quoteChar = 0;
        
        for (char c : parameters.toCharArray()) {
            if (!inQuotes && (c == '\'' || c == '"')) {
                inQuotes = true;
                quoteChar = c;
                current.append(c);
            } else if (inQuotes && c == quoteChar) {
                inQuotes = false;
                current.append(c);
            } else if (!inQuotes && c == ',') {
                paramList.add(current.toString().trim());
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }
        
        if (current.length() > 0) {
            paramList.add(current.toString().trim());
        }
        
        return paramList.toArray(new String[0]);
    }
    
    /**
     * 评估权限函数
     */
    private boolean evaluatePermissionFunction(ExpressionNode node, PermissionContext context) {
        PermissionFunction function = node.getFunction();
        String[] parameters = node.getParameters();
        
        switch (function) {
            case HAS_PERMISSION:
                return evaluateHasPermission(parameters, context);
            case HAS_ROLE:
                return evaluateHasRole(parameters, context);
            case HAS_RESOURCE_PERMISSION:
                return evaluateHasResourcePermission(parameters, context);
            case HAS_CONDITIONAL_PERMISSION:
                return evaluateHasConditionalPermission(parameters, context);
            default:
                return false;
        }
    }
    
    /**
     * 评估逻辑运算符
     */
    private boolean evaluateLogicalOperator(ExpressionNode node, PermissionContext context) {
        LogicalOperator operator = node.getOperator();
        
        switch (operator) {
            case AND:
                return evaluateNode(node.getLeft(), context) && evaluateNode(node.getRight(), context);
            case OR:
                return evaluateNode(node.getLeft(), context) || evaluateNode(node.getRight(), context);
            case NOT:
                return !evaluateNode(node.getRight(), context);
            default:
                return false;
        }
    }
    
    /**
     * 评估hasPermission函数
     */
    private boolean evaluateHasPermission(String[] parameters, PermissionContext context) {
        if (parameters.length < 1) {
            return false;
        }
        
        String permission = parameters[0].replace("'", "").replace("\"", "");
        return context.hasPermission(permission);
    }
    
    /**
     * 评估hasRole函数
     */
    private boolean evaluateHasRole(String[] parameters, PermissionContext context) {
        if (parameters.length < 1) {
            return false;
        }
        
        String role = parameters[0].replace("'", "").replace("\"", "");
        return context.hasRole(role);
    }
    
    /**
     * 评估hasResourcePermission函数
     */
    private boolean evaluateHasResourcePermission(String[] parameters, PermissionContext context) {
        if (parameters.length < 2) {
            return false;
        }
        
        String resource = parameters[0].replace("'", "").replace("\"", "");
        String action = parameters[1].replace("'", "").replace("\"", "");
        
        return context.hasResourcePermission(resource, action);
    }
    
    /**
     * 评估hasConditionalPermission函数
     */
    private boolean evaluateHasConditionalPermission(String[] parameters, PermissionContext context) {
        if (parameters.length < 3) {
            return false;
        }
        
        String permission = parameters[0].replace("'", "").replace("\"", "");
        String conditionKey = parameters[1].replace("'", "").replace("\"", "");
        String conditionValue = parameters[2].replace("'", "").replace("\"", "");
        
        return context.hasConditionalPermission(permission, conditionKey, conditionValue);
    }
    
    /**
     * 验证括号
     */
    private void validateParentheses(String expression, ExpressionValidationResult result) {
        int count = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') count++;
            if (c == ')') count--;
            if (count < 0) {
                result.addError("括号不匹配：多余的右括号");
                return;
            }
        }
        if (count > 0) {
            result.addError("括号不匹配：多余的左括号");
        }
    }
    
    /**
     * 验证权限函数
     */
    private void validatePermissionFunctions(String expression, ExpressionValidationResult result) {
        Matcher matcher = PERMISSION_FUNCTION_PATTERN.matcher(expression);
        while (matcher.find()) {
            String functionName = matcher.group(1);
            String parameters = matcher.group(2);
            
            try {
                PermissionFunction.fromString(functionName);
                parseParameters(parameters);
            } catch (Exception e) {
                result.addError("权限函数语法错误: " + functionName + "(" + parameters + ")");
            }
        }
    }
    
    /**
     * 验证逻辑运算符
     */
    private void validateLogicalOperators(String expression, ExpressionValidationResult result) {
        // 检查逻辑运算符的使用是否正确
        String[] tokens = expression.split("\\s+");
        for (int i = 0; i < tokens.length; i++) {
            String token = tokens[i];
            if ("AND".equals(token) || "OR".equals(token)) {
                if (i == 0 || i == tokens.length - 1) {
                    result.addError("逻辑运算符 " + token + " 位置错误");
                }
            } else if ("NOT".equals(token)) {
                if (i == tokens.length - 1) {
                    result.addError("NOT运算符后缺少操作数");
                }
            }
        }
    }
    
    // ==================== 内部类 ====================
    
    /**
     * 表达式节点
     */
    public static class ExpressionNode {
        public enum NodeType {
            PERMISSION_FUNCTION,
            LOGICAL_OPERATOR
        }
        
        private NodeType type;
        private PermissionFunction function;
        private LogicalOperator operator;
        private String[] parameters;
        private ExpressionNode left;
        private ExpressionNode right;
        
        public ExpressionNode(NodeType type) {
            this.type = type;
        }
        
        // Getters and Setters
        public NodeType getType() { return type; }
        public PermissionFunction getFunction() { return function; }
        public void setFunction(PermissionFunction function) { this.function = function; }
        public LogicalOperator getOperator() { return operator; }
        public void setOperator(LogicalOperator operator) { this.operator = operator; }
        public String[] getParameters() { return parameters; }
        public void setParameters(String[] parameters) { this.parameters = parameters; }
        public ExpressionNode getLeft() { return left; }
        public void setLeft(ExpressionNode left) { this.left = left; }
        public ExpressionNode getRight() { return right; }
        public void setRight(ExpressionNode right) { this.right = right; }
    }
    
    /**
     * 逻辑运算符信息
     */
    private static class LogicalOperatorInfo {
        private final LogicalOperator operator;
        private final int position;
        private final int length;
        
        public LogicalOperatorInfo(LogicalOperator operator, int position, int length) {
            this.operator = operator;
            this.position = position;
            this.length = length;
        }
    }
    
    /**
     * 权限上下文接口
     */
    public interface PermissionContext {
        boolean hasPermission(String permission);
        boolean hasRole(String role);
        boolean hasResourcePermission(String resource, String action);
        boolean hasConditionalPermission(String permission, String conditionKey, String conditionValue);
    }
    
    /**
     * 表达式验证结果
     */
    public static class ExpressionValidationResult {
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
}