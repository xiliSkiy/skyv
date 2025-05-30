package com.skyeye.common.response;

import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 分页结果包装类
 * @param <T> 数据类型
 */
@Data
public class PageResult<T> {
    /**
     * 当前页码
     */
    private int pageNum;
    
    /**
     * 每页数量
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
     * 数据列表
     */
    private List<T> list;
    
    /**
     * 是否有下一页
     */
    private boolean hasNext;
    
    /**
     * 是否有上一页
     */
    private boolean hasPrevious;
    
    /**
     * 从Spring Data Page转换
     * @param page Spring Data Page对象
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(Page<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(page.getNumber() + 1);  // Spring Data Page从0开始
        result.setPageSize(page.getSize());
        result.setTotal(page.getTotalElements());
        result.setPages(page.getTotalPages());
        result.setList(page.getContent());
        result.setHasNext(page.hasNext());
        result.setHasPrevious(page.hasPrevious());
        return result;
    }
    
    /**
     * 创建分页结果
     * @param list 数据列表
     * @param pageNum 当前页码
     * @param pageSize 每页数量
     * @param total 总记录数
     * @param <T> 数据类型
     * @return 分页结果
     */
    public static <T> PageResult<T> of(List<T> list, int pageNum, int pageSize, long total) {
        PageResult<T> result = new PageResult<>();
        result.setPageNum(pageNum);
        result.setPageSize(pageSize);
        result.setTotal(total);
        result.setPages((int) Math.ceil((double) total / pageSize));
        result.setList(list);
        result.setHasNext(pageNum < result.getPages());
        result.setHasPrevious(pageNum > 1);
        return result;
    }
} 