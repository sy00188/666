package com.archive.management.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.List;
import java.util.Collections;

/**
 * 分页结果封装类
 * 用于封装分页查询的结果数据
 * 
 * @param <T> 数据类型
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-20
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PageResult<T> {

    /**
     * 当前页码（从1开始）
     */
    private int pageNum;

    /**
     * 每页大小
     */
    private int pageSize;

    /**
     * 总记录数
     */
    private long total;

    /**
     * 总页数
     */
    private int pages;

    /**
     * 当前页的数据列表
     */
    private List<T> list;

    /**
     * 是否为第一页
     */
    private boolean isFirstPage;

    /**
     * 是否为最后一页
     */
    private boolean isLastPage;

    /**
     * 是否有前一页
     */
    private boolean hasPreviousPage;

    /**
     * 是否有下一页
     */
    private boolean hasNextPage;

    /**
     * 前一页页码
     */
    private int prePage;

    /**
     * 下一页页码
     */
    private int nextPage;

    /**
     * 默认构造函数
     */
    public PageResult() {
        this.list = Collections.emptyList();
    }

    /**
     * 构造函数
     * 
     * @param pageNum 当前页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param list 数据列表
     */
    public PageResult(int pageNum, int pageSize, long total, List<T> list) {
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list != null ? list : Collections.emptyList();
        
        // 计算总页数
        this.pages = (int) Math.ceil((double) total / pageSize);
        if (this.pages == 0) {
            this.pages = 1;
        }
        
        // 计算分页相关属性
        calculatePageProperties();
    }

    /**
     * 计算分页相关属性
     */
    private void calculatePageProperties() {
        // 是否为第一页
        this.isFirstPage = pageNum == 1;
        
        // 是否为最后一页
        this.isLastPage = pageNum == pages;
        
        // 是否有前一页
        this.hasPreviousPage = pageNum > 1;
        
        // 是否有下一页
        this.hasNextPage = pageNum < pages;
        
        // 前一页页码
        this.prePage = hasPreviousPage ? pageNum - 1 : 0;
        
        // 下一页页码
        this.nextPage = hasNextPage ? pageNum + 1 : 0;
    }

    // ========== 静态工厂方法 ==========

    /**
     * 创建空的分页结果
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param <T> 数据类型
     * @return PageResult<T>
     */
    public static <T> PageResult<T> empty(int pageNum, int pageSize) {
        return new PageResult<>(pageNum, pageSize, 0, Collections.emptyList());
    }

    /**
     * 创建分页结果
     * 
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param total 总记录数
     * @param list 数据列表
     * @param <T> 数据类型
     * @return PageResult<T>
     */
    public static <T> PageResult<T> of(int pageNum, int pageSize, long total, List<T> list) {
        return new PageResult<>(pageNum, pageSize, total, list);
    }
    
    /**
     * 创建分页结果（简化版，从total和list推断）
     * 默认pageNum=1, pageSize=list.size()
     * 
     * @param total 总记录数
     * @param list 数据列表
     * @param <T> 数据类型
     * @return PageResult<T>
     */
    public static <T> PageResult<T> of(long total, List<T> list) {
        int size = (list != null && !list.isEmpty()) ? list.size() : 10;
        return new PageResult<>(1, size, total, list);
    }

    /**
     * 从Spring Data的Page对象创建PageResult
     * 
     * @param page Spring Data Page对象
     * @param <T> 数据类型
     * @return PageResult<T>
     */
    public static <T> PageResult<T> fromPage(org.springframework.data.domain.Page<T> page) {
        return new PageResult<>(
            page.getNumber() + 1, // Spring Data Page从0开始，转换为从1开始
            page.getSize(),
            page.getTotalElements(),
            page.getContent()
        );
    }

    // ========== 便捷方法 ==========

    /**
     * 判断是否有数据
     * 
     * @return 是否有数据
     */
    public boolean hasData() {
        return list != null && !list.isEmpty();
    }

    /**
     * 获取当前页数据数量
     * 
     * @return 当前页数据数量
     */
    public int getCurrentPageSize() {
        return list != null ? list.size() : 0;
    }

    /**
     * 获取起始记录号（从1开始）
     * 
     * @return 起始记录号
     */
    public long getStartRow() {
        return (long) (pageNum - 1) * pageSize + 1;
    }

    /**
     * 获取结束记录号
     * 
     * @return 结束记录号
     */
    public long getEndRow() {
        return Math.min(getStartRow() + getCurrentPageSize() - 1, total);
    }

    /**
     * 判断是否为空结果
     * 
     * @return 是否为空结果
     */
    public boolean isEmpty() {
        return total == 0;
    }

    // ========== Getter and Setter ==========

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
        calculatePageProperties();
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
        calculatePageProperties();
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
        this.pages = (int) Math.ceil((double) total / pageSize);
        if (this.pages == 0) {
            this.pages = 1;
        }
        calculatePageProperties();
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list != null ? list : Collections.emptyList();
    }

    public boolean isFirstPage() {
        return isFirstPage;
    }

    public void setFirstPage(boolean firstPage) {
        isFirstPage = firstPage;
    }

    public boolean isLastPage() {
        return isLastPage;
    }

    public void setLastPage(boolean lastPage) {
        isLastPage = lastPage;
    }

    public boolean isHasPreviousPage() {
        return hasPreviousPage;
    }

    public void setHasPreviousPage(boolean hasPreviousPage) {
        this.hasPreviousPage = hasPreviousPage;
    }

    public boolean isHasNextPage() {
        return hasNextPage;
    }

    public void setHasNextPage(boolean hasNextPage) {
        this.hasNextPage = hasNextPage;
    }

    public int getPrePage() {
        return prePage;
    }

    public void setPrePage(int prePage) {
        this.prePage = prePage;
    }

    public int getNextPage() {
        return nextPage;
    }

    public void setNextPage(int nextPage) {
        this.nextPage = nextPage;
    }

    @Override
    public String toString() {
        return "PageResult{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", total=" + total +
                ", pages=" + pages +
                ", list=" + (list != null ? list.size() + " items" : "null") +
                ", isFirstPage=" + isFirstPage +
                ", isLastPage=" + isLastPage +
                ", hasPreviousPage=" + hasPreviousPage +
                ", hasNextPage=" + hasNextPage +
                ", prePage=" + prePage +
                ", nextPage=" + nextPage +
                '}';
    }
}