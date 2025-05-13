package com.skyeye.common.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分页元数据
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageMeta {
    /**
     * 总记录数
     */
    private long total;
    
    /**
     * 当前页码
     */
    private int page;
    
    /**
     * 每页记录数
     */
    private int limit;
    
    /**
     * 总页数
     */
    private int totalPages;
    
    /**
     * 兼容原有代码的构造函数
     * @param total 总记录数
     * @param page 当前页码
     * @param limit 每页记录数
     */
    public PageMeta(long total, int page, int limit) {
        this.total = total;
        this.page = page;
        this.limit = limit;
        // 计算总页数
        this.totalPages = limit > 0 ? (int) Math.ceil((double) total / limit) : 0;
    }
} 