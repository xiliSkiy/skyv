package com.skyeye.scheduler.dto;

import lombok.Data;

import java.io.Serializable;

/**
 * 任务查询参数数据传输对象
 */
@Data
public class TaskQueryDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 页码
     */
    private Integer pageNum = 1;

    /**
     * 每页记录数
     */
    private Integer pageSize = 10;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务状态
     */
    private String status;

    /**
     * 优先级
     */
    private String priority;

    /**
     * 标签
     */
    private String tag;

    /**
     * 关键字（任务名称/ID/负责人）
     */
    private String keyword;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    /**
     * 排序字段
     */
    private String sortBy;

    /**
     * 排序方向：asc-升序，desc-降序
     */
    private String order = "desc";
} 