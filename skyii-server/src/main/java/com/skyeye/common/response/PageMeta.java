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
} 