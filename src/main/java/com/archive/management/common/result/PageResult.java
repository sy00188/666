package com.archive.management.common.result;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页响应结果类
 * 
 * @author Archive Management System
 * @version 1.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "分页响应结果")
public class PageResult<T> extends Result<List<T>> {

    private static final long serialVersionUID = 1L;

    /**
     * 当前页码
     */
    @Schema(description = "当前页码", example = "1")
    private Integer pageNum;

    /**
     * 每页大小
     */
    @Schema(description = "每页大小", example = "10")
    private Integer pageSize;

    /**
     * 总记录数
     */
    @Schema(description = "总记录数", example = "100")
    private Long total;

    /**
     * 总页数
     */
    @Schema(description = "总页数", example = "10")
    private Integer totalPages;

    /**
     * 是否有下一页
     */
    @Schema(description = "是否有下一页", example = "true")
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
    @Schema(description = "是否有上一页", example = "false")
    private Boolean hasPrevious;

    /**
     * 是否为第一页
     */
    @Schema(description = "是否为第一页", example = "true")
    private Boolean isFirst;

    /**
     * 是否为最后一页
     */
    @Schema(description = "是否为最后一页", example = "false")
    private Boolean isLast;

    public PageResult() {
        super();
    }

    public PageResult(List<T> data, Integer pageNum, Integer pageSize, Long total) {
        super();
        this.setData(data);
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.total = total;
        this.totalPages = (int) Math.ceil((double) total / pageSize);
        this.hasNext = pageNum < totalPages;
        this.hasPrevious = pageNum > 1;
        this.isFirst = pageNum == 1;
        this.isLast = pageNum.equals(totalPages);
    }

    /**
     * 成功分页响应
     */
    public static <T> PageResult<T> success(List<T> data, Integer pageNum, Integer pageSize, Long total) {
        PageResult<T> result = new PageResult<>(data, pageNum, pageSize, total);
        result.setCode(200);
        result.setMessage("操作成功");
        return result;
    }

    /**
     * 成功分页响应，自定义消息
     */
    public static <T> PageResult<T> success(String message, List<T> data, Integer pageNum, Integer pageSize, Long total) {
        PageResult<T> result = new PageResult<>(data, pageNum, pageSize, total);
        result.setCode(200);
        result.setMessage(message);
        return result;
    }

    /**
     * 从Spring Data Page对象创建分页结果
     */
    public static <T> PageResult<T> of(Page<T> page) {
        return success(
            page.getContent(),
            page.getNumber() + 1, // Spring Data Page从0开始，转换为从1开始
            page.getSize(),
            page.getTotalElements()
        );
    }

    /**
     * 从Spring Data Page对象创建分页结果，自定义消息
     */
    public static <T> PageResult<T> of(String message, Page<T> page) {
        return success(
            message,
            page.getContent(),
            page.getNumber() + 1, // Spring Data Page从0开始，转换为从1开始
            page.getSize(),
            page.getTotalElements()
        );
    }

    /**
     * 空分页结果
     */
    public static <T> PageResult<T> empty(Integer pageNum, Integer pageSize) {
        return success(List.of(), pageNum, pageSize, 0L);
    }
}