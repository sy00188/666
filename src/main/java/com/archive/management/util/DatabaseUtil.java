package com.archive.management.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.*;
import java.lang.reflect.Field;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;

/**
 * 数据库工具类
 * 提供数据库操作的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class DatabaseUtil {

    /**
     * SQL注入检测正则表达式
     */
    private static final Pattern SQL_INJECTION_PATTERN = Pattern.compile(
        "(?i)(\\b(SELECT|INSERT|UPDATE|DELETE|DROP|CREATE|ALTER|EXEC|UNION|SCRIPT)\\b)|" +
        "(--|#|/\\*|\\*/|'|\")|" +
        "(\\b(OR|AND)\\s+\\d+\\s*=\\s*\\d+)|" +
        "(\\b(OR|AND)\\s+'[^']*'\\s*=\\s*'[^']*')"
    );

    /**
     * 数据库字段名正则表达式
     */
    private static final Pattern FIELD_NAME_PATTERN = Pattern.compile("^[a-zA-Z_][a-zA-Z0-9_]*$");

    // ========== SQL安全检查 ==========

    /**
     * 检查SQL注入
     * 
     * @param input 输入字符串
     * @return 是否包含SQL注入
     */
    public static boolean containsSqlInjection(String input) {
        if (StringUtil.isEmpty(input)) {
            return false;
        }
        
        return SQL_INJECTION_PATTERN.matcher(input).find();
    }

    /**
     * 验证字段名安全性
     * 
     * @param fieldName 字段名
     * @return 是否安全
     */
    public static boolean isValidFieldName(String fieldName) {
        if (StringUtil.isEmpty(fieldName)) {
            return false;
        }
        
        return FIELD_NAME_PATTERN.matcher(fieldName).matches();
    }

    /**
     * 清理SQL输入
     * 
     * @param input 输入字符串
     * @return 清理后的字符串
     */
    public static String sanitizeSqlInput(String input) {
        if (StringUtil.isEmpty(input)) {
            return input;
        }
        
        // 转义单引号
        return input.replace("'", "''");
    }

    /**
     * 验证排序字段
     * 
     * @param sortField 排序字段
     * @param allowedFields 允许的字段列表
     * @return 是否有效
     */
    public static boolean isValidSortField(String sortField, Set<String> allowedFields) {
        if (StringUtil.isEmpty(sortField) || allowedFields == null) {
            return false;
        }
        
        return isValidFieldName(sortField) && allowedFields.contains(sortField);
    }

    // ========== 分页工具 ==========

    /**
     * 创建分页对象
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size) {
        return PageRequest.of(Math.max(0, page), Math.max(1, size));
    }

    /**
     * 创建带排序的分页对象
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sort 排序对象
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size, Sort sort) {
        return PageRequest.of(Math.max(0, page), Math.max(1, size), sort != null ? sort : Sort.unsorted());
    }

    /**
     * 创建带排序的分页对象
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sortField 排序字段
     * @param sortDirection 排序方向
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size, String sortField, Sort.Direction sortDirection) {
        Sort sort = Sort.unsorted();
        
        if (StringUtil.isNotEmpty(sortField) && isValidFieldName(sortField)) {
            sort = Sort.by(sortDirection != null ? sortDirection : Sort.Direction.ASC, sortField);
        }
        
        return createPageable(page, size, sort);
    }

    /**
     * 创建多字段排序的分页对象
     * 
     * @param page 页码（从0开始）
     * @param size 每页大小
     * @param sortOrders 排序规则列表
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size, List<Sort.Order> sortOrders) {
        Sort sort = Sort.unsorted();
        
        if (sortOrders != null && !sortOrders.isEmpty()) {
            sort = Sort.by(sortOrders);
        }
        
        return createPageable(page, size, sort);
    }

    /**
     * 验证分页参数
     * 
     * @param page 页码
     * @param size 每页大小
     * @param maxSize 最大每页大小
     * @return 验证结果
     */
    public static Map<String, Object> validatePageParams(int page, int size, int maxSize) {
        Map<String, Object> result = new HashMap<>();
        
        int validPage = Math.max(0, page);
        int validSize = Math.max(1, Math.min(size, maxSize));
        
        result.put("page", validPage);
        result.put("size", validSize);
        result.put("valid", page >= 0 && size > 0 && size <= maxSize);
        
        return result;
    }

    // ========== 动态查询 ==========

    /**
     * 创建等值查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    public static <T> Specification<T> equal(String fieldName, Object value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.equal(root.get(fieldName), value);
        };
    }

    /**
     * 创建不等值查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    public static <T> Specification<T> notEqual(String fieldName, Object value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.notEqual(root.get(fieldName), value);
        };
    }

    /**
     * 创建模糊查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    public static <T> Specification<T> like(String fieldName, String value) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(value) || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get(fieldName), "%" + value + "%");
        };
    }

    /**
     * 创建左模糊查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    public static <T> Specification<T> likeLeft(String fieldName, String value) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(value) || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get(fieldName), "%" + value);
        };
    }

    /**
     * 创建右模糊查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    public static <T> Specification<T> likeRight(String fieldName, String value) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(value) || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get(fieldName), value + "%");
        };
    }

    /**
     * 创建IN查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param values 值列表
     * @return Specification
     */
    public static <T> Specification<T> in(String fieldName, Collection<?> values) {
        return (root, query, criteriaBuilder) -> {
            if (values == null || values.isEmpty() || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return root.get(fieldName).in(values);
        };
    }

    /**
     * 创建NOT IN查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param values 值列表
     * @return Specification
     */
    public static <T> Specification<T> notIn(String fieldName, Collection<?> values) {
        return (root, query, criteriaBuilder) -> {
            if (values == null || values.isEmpty() || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.not(root.get(fieldName).in(values));
        };
    }

    /**
     * 创建大于查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> greaterThan(String fieldName, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThan(root.get(fieldName), value);
        };
    }

    /**
     * 创建大于等于查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> greaterThanOrEqual(String fieldName, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.greaterThanOrEqualTo(root.get(fieldName), value);
        };
    }

    /**
     * 创建小于查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> lessThan(String fieldName, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThan(root.get(fieldName), value);
        };
    }

    /**
     * 创建小于等于查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param value 值
     * @return Specification
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> lessThanOrEqual(String fieldName, Comparable value) {
        return (root, query, criteriaBuilder) -> {
            if (value == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.lessThanOrEqualTo(root.get(fieldName), value);
        };
    }

    /**
     * 创建范围查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param start 开始值
     * @param end 结束值
     * @return Specification
     */
    @SuppressWarnings("unchecked")
    public static <T> Specification<T> between(String fieldName, Comparable start, Comparable end) {
        return (root, query, criteriaBuilder) -> {
            if (start == null || end == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get(fieldName), start, end);
        };
    }

    /**
     * 创建日期范围查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @return Specification
     */
    public static <T> Specification<T> dateBetween(String fieldName, LocalDateTime startDate, LocalDateTime endDate) {
        return (root, query, criteriaBuilder) -> {
            if (startDate == null || endDate == null || StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.between(root.get(fieldName), startDate, endDate);
        };
    }

    /**
     * 创建IS NULL查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @return Specification
     */
    public static <T> Specification<T> isNull(String fieldName) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.isNull(root.get(fieldName));
        };
    }

    /**
     * 创建IS NOT NULL查询条件
     * 
     * @param <T> 实体类型
     * @param fieldName 字段名
     * @return Specification
     */
    public static <T> Specification<T> isNotNull(String fieldName) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(fieldName)) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.isNotNull(root.get(fieldName));
        };
    }

    // ========== 条件组合 ==========

    /**
     * AND条件组合
     * 
     * @param <T> 实体类型
     * @param specifications 条件列表
     * @return 组合后的Specification
     */
    @SafeVarargs
    public static <T> Specification<T> and(Specification<T>... specifications) {
        if (specifications == null || specifications.length == 0) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        
        Specification<T> result = specifications[0];
        for (int i = 1; i < specifications.length; i++) {
            if (specifications[i] != null) {
                result = result.and(specifications[i]);
            }
        }
        
        return result;
    }

    /**
     * OR条件组合
     * 
     * @param <T> 实体类型
     * @param specifications 条件列表
     * @return 组合后的Specification
     */
    @SafeVarargs
    public static <T> Specification<T> or(Specification<T>... specifications) {
        if (specifications == null || specifications.length == 0) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        
        Specification<T> result = specifications[0];
        for (int i = 1; i < specifications.length; i++) {
            if (specifications[i] != null) {
                result = result.or(specifications[i]);
            }
        }
        
        return result;
    }

    /**
     * NOT条件
     * 
     * @param <T> 实体类型
     * @param specification 条件
     * @return 取反后的Specification
     */
    public static <T> Specification<T> not(Specification<T> specification) {
        if (specification == null) {
            return (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();
        }
        
        return Specification.not(specification);
    }

    // ========== 实体工具 ==========

    /**
     * 获取实体的所有字段名
     * 
     * @param entityClass 实体类
     * @return 字段名集合
     */
    public static Set<String> getEntityFieldNames(Class<?> entityClass) {
        Set<String> fieldNames = new HashSet<>();
        
        if (entityClass == null) {
            return fieldNames;
        }
        
        Field[] fields = entityClass.getDeclaredFields();
        for (Field field : fields) {
            fieldNames.add(field.getName());
        }
        
        return fieldNames;
    }

    /**
     * 检查实体是否包含指定字段
     * 
     * @param entityClass 实体类
     * @param fieldName 字段名
     * @return 是否包含
     */
    public static boolean hasField(Class<?> entityClass, String fieldName) {
        if (entityClass == null || StringUtil.isEmpty(fieldName)) {
            return false;
        }
        
        try {
            entityClass.getDeclaredField(fieldName);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /**
     * 获取字段类型
     * 
     * @param entityClass 实体类
     * @param fieldName 字段名
     * @return 字段类型
     */
    public static Class<?> getFieldType(Class<?> entityClass, String fieldName) {
        if (entityClass == null || StringUtil.isEmpty(fieldName)) {
            return null;
        }
        
        try {
            Field field = entityClass.getDeclaredField(fieldName);
            return field.getType();
        } catch (NoSuchFieldException e) {
            return null;
        }
    }

    // ========== 批量操作 ==========

    /**
     * 批量处理分页数据
     * 
     * @param <T> 数据类型
     * @param page 分页数据
     * @param processor 处理器
     */
    public static <T> void processBatch(Page<T> page, BatchProcessor<T> processor) {
        if (page == null || processor == null) {
            return;
        }
        
        List<T> content = page.getContent();
        if (content != null && !content.isEmpty()) {
            processor.process(content);
        }
    }

    /**
     * 批量处理器接口
     * 
     * @param <T> 数据类型
     */
    @FunctionalInterface
    public interface BatchProcessor<T> {
        void process(List<T> batch);
    }

    // ========== 查询优化 ==========

    /**
     * 创建去重查询
     * 
     * @param <T> 实体类型
     * @return Specification
     */
    public static <T> Specification<T> distinct() {
        return (root, query, criteriaBuilder) -> {
            query.distinct(true);
            return criteriaBuilder.conjunction();
        };
    }

    /**
     * 创建计数查询优化
     * 
     * @param <T> 实体类型
     * @return Specification
     */
    public static <T> Specification<T> optimizeForCount() {
        return (root, query, criteriaBuilder) -> {
            // 对于计数查询，不需要排序
            if (Long.class.equals(query.getResultType())) {
                query.orderBy();
            }
            return criteriaBuilder.conjunction();
        };
    }

    // ========== 常用查询模式 ==========

    /**
     * 创建软删除过滤条件
     * 
     * @param <T> 实体类型
     * @param deletedField 删除标记字段名
     * @return Specification
     */
    public static <T> Specification<T> notDeleted(String deletedField) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(deletedField)) {
                return criteriaBuilder.conjunction();
            }
            
            // 假设删除标记为Boolean类型，true表示已删除
            return criteriaBuilder.or(
                criteriaBuilder.isNull(root.get(deletedField)),
                criteriaBuilder.equal(root.get(deletedField), false)
            );
        };
    }

    /**
     * 创建状态过滤条件
     * 
     * @param <T> 实体类型
     * @param statusField 状态字段名
     * @param activeStatuses 有效状态列表
     * @return Specification
     */
    public static <T> Specification<T> activeStatus(String statusField, Collection<?> activeStatuses) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(statusField) || activeStatuses == null || activeStatuses.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            
            return root.get(statusField).in(activeStatuses);
        };
    }

    /**
     * 创建时间范围过滤条件（今天）
     * 
     * @param <T> 实体类型
     * @param dateField 日期字段名
     * @return Specification
     */
    public static <T> Specification<T> today(String dateField) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(dateField)) {
                return criteriaBuilder.conjunction();
            }
            
            LocalDateTime startOfDay = DateUtil.getStartOfDay(LocalDateTime.now());
            LocalDateTime endOfDay = DateUtil.getEndOfDay(LocalDateTime.now());
            
            return criteriaBuilder.between(root.get(dateField), startOfDay, endOfDay);
        };
    }

    /**
     * 创建时间范围过滤条件（本周）
     * 
     * @param <T> 实体类型
     * @param dateField 日期字段名
     * @return Specification
     */
    public static <T> Specification<T> thisWeek(String dateField) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(dateField)) {
                return criteriaBuilder.conjunction();
            }
            
            LocalDateTime startOfWeek = DateUtil.getStartOfWeek(LocalDateTime.now());
            LocalDateTime endOfWeek = DateUtil.getEndOfWeek(LocalDateTime.now());
            
            return criteriaBuilder.between(root.get(dateField), startOfWeek, endOfWeek);
        };
    }

    /**
     * 创建时间范围过滤条件（本月）
     * 
     * @param <T> 实体类型
     * @param dateField 日期字段名
     * @return Specification
     */
    public static <T> Specification<T> thisMonth(String dateField) {
        return (root, query, criteriaBuilder) -> {
            if (StringUtil.isEmpty(dateField)) {
                return criteriaBuilder.conjunction();
            }
            
            LocalDateTime startOfMonth = DateUtil.getStartOfMonth(LocalDateTime.now());
            LocalDateTime endOfMonth = DateUtil.getEndOfMonth(LocalDateTime.now());
            
            return criteriaBuilder.between(root.get(dateField), startOfMonth, endOfMonth);
        };
    }
}