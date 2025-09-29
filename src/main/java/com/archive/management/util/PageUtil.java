package com.archive.management.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 分页工具类
 * 提供分页相关的常用方法
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
public class PageUtil {

    /**
     * 默认页码
     */
    public static final int DEFAULT_PAGE = 0;

    /**
     * 默认页面大小
     */
    public static final int DEFAULT_SIZE = 20;

    /**
     * 最大页面大小
     */
    public static final int MAX_SIZE = 1000;

    /**
     * 最小页面大小
     */
    public static final int MIN_SIZE = 1;

    // ========== Pageable创建 ==========

    /**
     * 创建Pageable对象
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size) {
        return createPageable(page, size, null);
    }

    /**
     * 创建Pageable对象（带排序）
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @param sort 排序
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size, Sort sort) {
        // 参数校验和修正
        page = Math.max(page, 0);
        size = Math.max(Math.min(size, MAX_SIZE), MIN_SIZE);
        
        if (sort != null) {
            return PageRequest.of(page, size, sort);
        } else {
            return PageRequest.of(page, size);
        }
    }

    /**
     * 创建Pageable对象（带排序字段）
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @param sortBy 排序字段
     * @param direction 排序方向
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size, String sortBy, Sort.Direction direction) {
        if (StringUtil.isEmpty(sortBy)) {
            return createPageable(page, size);
        }
        
        Sort sort = Sort.by(direction != null ? direction : Sort.Direction.ASC, sortBy);
        return createPageable(page, size, sort);
    }

    /**
     * 创建Pageable对象（多字段排序）
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @param sortFields 排序字段数组
     * @return Pageable对象
     */
    public static Pageable createPageable(int page, int size, String... sortFields) {
        if (sortFields == null || sortFields.length == 0) {
            return createPageable(page, size);
        }
        
        Sort sort = Sort.by(sortFields);
        return createPageable(page, size, sort);
    }

    // ========== 分页信息提取 ==========

    /**
     * 提取分页信息
     * 
     * @param page Page对象
     * @return 分页信息Map
     */
    public static Map<String, Object> extractPageInfo(Page<?> page) {
        Map<String, Object> pageInfo = new HashMap<>();
        
        if (page == null) {
            pageInfo.put("currentPage", 0);
            pageInfo.put("pageSize", DEFAULT_SIZE);
            pageInfo.put("totalElements", 0L);
            pageInfo.put("totalPages", 0);
            pageInfo.put("hasNext", false);
            pageInfo.put("hasPrevious", false);
            pageInfo.put("isFirst", true);
            pageInfo.put("isLast", true);
            pageInfo.put("numberOfElements", 0);
            return pageInfo;
        }
        
        pageInfo.put("currentPage", page.getNumber());
        pageInfo.put("pageSize", page.getSize());
        pageInfo.put("totalElements", page.getTotalElements());
        pageInfo.put("totalPages", page.getTotalPages());
        pageInfo.put("hasNext", page.hasNext());
        pageInfo.put("hasPrevious", page.hasPrevious());
        pageInfo.put("isFirst", page.isFirst());
        pageInfo.put("isLast", page.isLast());
        pageInfo.put("numberOfElements", page.getNumberOfElements());
        
        return pageInfo;
    }

    /**
     * 创建分页响应
     * 
     * @param page Page对象
     * @return 分页响应Map
     */
    public static Map<String, Object> createPageResponse(Page<?> page) {
        Map<String, Object> response = new HashMap<>();
        
        if (page == null) {
            response.put("content", new ArrayList<>());
            response.put("page", extractPageInfo(null));
            return response;
        }
        
        response.put("content", page.getContent());
        response.put("page", extractPageInfo(page));
        
        return response;
    }

    /**
     * 创建分页响应（转换内容）
     * 
     * @param page Page对象
     * @param converter 内容转换器
     * @param <T> 原始类型
     * @param <R> 目标类型
     * @return 分页响应Map
     */
    public static <T, R> Map<String, Object> createPageResponse(Page<T> page, Function<T, R> converter) {
        Map<String, Object> response = new HashMap<>();
        
        if (page == null || converter == null) {
            response.put("content", new ArrayList<>());
            response.put("page", extractPageInfo(null));
            return response;
        }
        
        List<R> convertedContent = page.getContent().stream()
                .map(converter)
                .collect(Collectors.toList());
        
        response.put("content", convertedContent);
        response.put("page", extractPageInfo(page));
        
        return response;
    }

    // ========== 分页参数处理 ==========

    /**
     * 标准化分页参数
     * 
     * @param page 页码
     * @param size 页面大小
     * @return 标准化后的参数数组 [page, size]
     */
    public static int[] normalizePageParams(Integer page, Integer size) {
        int normalizedPage = page != null ? Math.max(page, 0) : DEFAULT_PAGE;
        int normalizedSize = size != null ? Math.max(Math.min(size, MAX_SIZE), MIN_SIZE) : DEFAULT_SIZE;
        
        return new int[]{normalizedPage, normalizedSize};
    }

    /**
     * 从请求参数中提取分页参数
     * 
     * @param pageParam 页码参数
     * @param sizeParam 页面大小参数
     * @param sortParam 排序参数
     * @return Pageable对象
     */
    public static Pageable extractPageable(String pageParam, String sizeParam, String sortParam) {
        int page = DEFAULT_PAGE;
        int size = DEFAULT_SIZE;
        
        // 解析页码
        if (StringUtil.isNotEmpty(pageParam)) {
            try {
                page = Math.max(Integer.parseInt(pageParam), 0);
            } catch (NumberFormatException e) {
                page = DEFAULT_PAGE;
            }
        }
        
        // 解析页面大小
        if (StringUtil.isNotEmpty(sizeParam)) {
            try {
                size = Math.max(Math.min(Integer.parseInt(sizeParam), MAX_SIZE), MIN_SIZE);
            } catch (NumberFormatException e) {
                size = DEFAULT_SIZE;
            }
        }
        
        // 解析排序
        Sort sort = parseSort(sortParam);
        
        return createPageable(page, size, sort);
    }

    /**
     * 解析排序参数
     * 
     * @param sortParam 排序参数（格式：field1,asc;field2,desc）
     * @return Sort对象
     */
    public static Sort parseSort(String sortParam) {
        if (StringUtil.isEmpty(sortParam)) {
            return null;
        }
        
        List<Sort.Order> orders = new ArrayList<>();
        
        // 分割多个排序字段
        String[] sortFields = sortParam.split(";");
        
        for (String sortField : sortFields) {
            if (StringUtil.isEmpty(sortField)) {
                continue;
            }
            
            String[] parts = sortField.split(",");
            if (parts.length == 0 || StringUtil.isEmpty(parts[0])) {
                continue;
            }
            
            String field = parts[0].trim();
            Sort.Direction direction = Sort.Direction.ASC;
            
            if (parts.length > 1 && StringUtil.isNotEmpty(parts[1])) {
                String directionStr = parts[1].trim().toLowerCase();
                if ("desc".equals(directionStr) || "descending".equals(directionStr)) {
                    direction = Sort.Direction.DESC;
                }
            }
            
            orders.add(new Sort.Order(direction, field));
        }
        
        return orders.isEmpty() ? null : Sort.by(orders);
    }

    // ========== 分页计算 ==========

    /**
     * 计算总页数
     * 
     * @param totalElements 总元素数
     * @param pageSize 页面大小
     * @return 总页数
     */
    public static int calculateTotalPages(long totalElements, int pageSize) {
        if (totalElements <= 0 || pageSize <= 0) {
            return 0;
        }
        
        return (int) Math.ceil((double) totalElements / pageSize);
    }

    /**
     * 计算偏移量
     * 
     * @param page 页码（从0开始）
     * @param size 页面大小
     * @return 偏移量
     */
    public static long calculateOffset(int page, int size) {
        return (long) Math.max(page, 0) * Math.max(size, 1);
    }

    /**
     * 判断是否有下一页
     * 
     * @param currentPage 当前页码（从0开始）
     * @param totalPages 总页数
     * @return 是否有下一页
     */
    public static boolean hasNext(int currentPage, int totalPages) {
        return currentPage >= 0 && currentPage < totalPages - 1;
    }

    /**
     * 判断是否有上一页
     * 
     * @param currentPage 当前页码（从0开始）
     * @return 是否有上一页
     */
    public static boolean hasPrevious(int currentPage) {
        return currentPage > 0;
    }

    /**
     * 判断是否是第一页
     * 
     * @param currentPage 当前页码（从0开始）
     * @return 是否是第一页
     */
    public static boolean isFirst(int currentPage) {
        return currentPage <= 0;
    }

    /**
     * 判断是否是最后一页
     * 
     * @param currentPage 当前页码（从0开始）
     * @param totalPages 总页数
     * @return 是否是最后一页
     */
    public static boolean isLast(int currentPage, int totalPages) {
        return totalPages <= 0 || currentPage >= totalPages - 1;
    }

    // ========== 分页范围计算 ==========

    /**
     * 计算分页范围
     * 
     * @param currentPage 当前页码（从1开始显示）
     * @param totalPages 总页数
     * @param displayCount 显示的页码数量
     * @return 页码范围数组 [start, end]
     */
    public static int[] calculatePageRange(int currentPage, int totalPages, int displayCount) {
        if (totalPages <= 0 || displayCount <= 0) {
            return new int[]{1, 1};
        }
        
        // 确保当前页在有效范围内
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        
        int start, end;
        
        if (totalPages <= displayCount) {
            // 总页数不超过显示数量，显示所有页码
            start = 1;
            end = totalPages;
        } else {
            // 计算显示范围
            int half = displayCount / 2;
            start = Math.max(1, currentPage - half);
            end = Math.min(totalPages, start + displayCount - 1);
            
            // 调整起始位置
            if (end - start + 1 < displayCount) {
                start = Math.max(1, end - displayCount + 1);
            }
        }
        
        return new int[]{start, end};
    }

    /**
     * 生成分页导航信息
     * 
     * @param currentPage 当前页码（从1开始显示）
     * @param totalPages 总页数
     * @param displayCount 显示的页码数量
     * @return 分页导航信息
     */
    public static Map<String, Object> generatePagination(int currentPage, int totalPages, int displayCount) {
        Map<String, Object> pagination = new HashMap<>();
        
        if (totalPages <= 0) {
            pagination.put("currentPage", 1);
            pagination.put("totalPages", 0);
            pagination.put("hasPrevious", false);
            pagination.put("hasNext", false);
            pagination.put("pages", new ArrayList<>());
            return pagination;
        }
        
        // 确保当前页在有效范围内
        currentPage = Math.max(1, Math.min(currentPage, totalPages));
        
        int[] range = calculatePageRange(currentPage, totalPages, displayCount);
        int start = range[0];
        int end = range[1];
        
        List<Integer> pages = new ArrayList<>();
        for (int i = start; i <= end; i++) {
            pages.add(i);
        }
        
        pagination.put("currentPage", currentPage);
        pagination.put("totalPages", totalPages);
        pagination.put("hasPrevious", currentPage > 1);
        pagination.put("hasNext", currentPage < totalPages);
        pagination.put("pages", pages);
        pagination.put("startPage", start);
        pagination.put("endPage", end);
        
        return pagination;
    }

    // ========== 分页数据转换 ==========

    /**
     * 转换分页数据
     * 
     * @param sourcePage 源分页对象
     * @param converter 转换器
     * @param <S> 源类型
     * @param <T> 目标类型
     * @return 转换后的分页对象
     */
    public static <S, T> Page<T> convertPage(Page<S> sourcePage, Function<S, T> converter) {
        if (sourcePage == null || converter == null) {
            return Page.empty();
        }
        
        List<T> convertedContent = sourcePage.getContent().stream()
                .map(converter)
                .collect(Collectors.toList());
        
        return new PageImpl<>(convertedContent, sourcePage.getPageable(), sourcePage.getTotalElements());
    }

    /**
     * 创建空分页对象
     * 
     * @param pageable 分页参数
     * @param <T> 元素类型
     * @return 空分页对象
     */
    public static <T> Page<T> emptyPage(Pageable pageable) {
        return new PageImpl<>(new ArrayList<>(), pageable, 0);
    }

    /**
     * 创建单页分页对象
     * 
     * @param content 内容列表
     * @param <T> 元素类型
     * @return 分页对象
     */
    public static <T> Page<T> singlePage(List<T> content) {
        if (content == null) {
            content = new ArrayList<>();
        }
        
        Pageable pageable = PageRequest.of(0, Math.max(content.size(), 1));
        return new PageImpl<>(content, pageable, content.size());
    }

    // ========== 内部类 ==========

    /**
     * 简单的Page实现
     */
    private static class PageImpl<T> implements Page<T> {
        private final List<T> content;
        private final Pageable pageable;
        private final long total;

        public PageImpl(List<T> content, Pageable pageable, long total) {
            this.content = content != null ? content : new ArrayList<>();
            this.pageable = pageable;
            this.total = total;
        }

        @Override
        public int getTotalPages() {
            return getSize() == 0 ? 1 : (int) Math.ceil((double) total / (double) getSize());
        }

        @Override
        public long getTotalElements() {
            return total;
        }

        @Override
        public <U> Page<U> map(Function<? super T, ? extends U> converter) {
            List<U> convertedContent = content.stream()
                    .map(converter)
                    .collect(Collectors.toList());
            return new PageImpl<>(convertedContent, pageable, total);
        }

        @Override
        public int getNumber() {
            return pageable.getPageNumber();
        }

        @Override
        public int getSize() {
            return pageable.getPageSize();
        }

        @Override
        public int getNumberOfElements() {
            return content.size();
        }

        @Override
        public List<T> getContent() {
            return content;
        }

        @Override
        public boolean hasContent() {
            return !content.isEmpty();
        }

        @Override
        public Sort getSort() {
            return pageable.getSort();
        }

        @Override
        public boolean isFirst() {
            return !hasPrevious();
        }

        @Override
        public boolean isLast() {
            return !hasNext();
        }

        @Override
        public boolean hasNext() {
            return getNumber() + 1 < getTotalPages();
        }

        @Override
        public boolean hasPrevious() {
            return getNumber() > 0;
        }

        @Override
        public Pageable getPageable() {
            return pageable;
        }

        @Override
        public Pageable nextPageable() {
            return hasNext() ? pageable.next() : Pageable.unpaged();
        }

        @Override
        public Pageable previousPageable() {
            return hasPrevious() ? pageable.previousOrFirst() : Pageable.unpaged();
        }

        @Override
        public java.util.Iterator<T> iterator() {
            return content.iterator();
        }
    }
}