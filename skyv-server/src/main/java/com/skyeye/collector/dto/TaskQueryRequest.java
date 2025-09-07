package com.skyeye.collector.dto;

import lombok.Data;

import java.util.List;

/**
 * 任务查询请求
 * 
 * @author SkyEye Team
 */
@Data
public class TaskQueryRequest {

    /**
     * 页码（从1开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;

    /**
     * 任务名称（模糊查询）
     */
    private String name;

    /**
     * 任务状态
     */
    private Integer status;

    /**
     * 调度类型
     */
    private String scheduleType;

    /**
     * 采集器ID
     */
    private Long collectorId;

    /**
     * 目标设备ID
     */
    private Long targetDeviceId;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 优先级范围
     */
    private Integer minPriority;
    private Integer maxPriority;

    /**
     * 创建时间范围
     */
    private String startTime;
    private String endTime;

    /**
     * 标签过滤
     */
    private List<String> tags;

    /**
     * 排序字段
     */
    private String sortBy = "createdAt";

    /**
     * 排序方向（asc, desc）
     */
    private String sortOrder = "desc";
}
