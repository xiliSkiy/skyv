package com.skyeye.collector.dto;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 日志查询请求DTO
 * 
 * @author SkyEye Team
 */
@Data
public class LogQueryRequest {

    /**
     * 页码
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;

    /**
     * 任务ID
     */
    private Long taskId;

    /**
     * 执行状态
     */
    private String status;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 执行结果
     */
    private String result;

    /**
     * 错误信息（模糊查询）
     */
    private String errorMessage;

    /**
     * 排序字段
     */
    private String sortBy = "startTime";

    /**
     * 排序方向
     */
    private String sortOrder = "DESC";

    /**
     * 是否包含详细信息
     */
    private Boolean includeDetails = false;

    /**
     * 是否包含错误详情
     */
    private Boolean includeErrorDetails = false;

    /**
     * 设备ID
     */
    private Long deviceId;

    /**
     * 采集器ID
     */
    private Long collectorId;

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 执行时长范围（毫秒）
     */
    private Long minExecutionTime;

    /**
     * 执行时长范围（毫秒）
     */
    private Long maxExecutionTime;

    /**
     * 数据量范围
     */
    private Integer minDataCount;

    /**
     * 数据量范围
     */
    private Integer maxDataCount;
}
