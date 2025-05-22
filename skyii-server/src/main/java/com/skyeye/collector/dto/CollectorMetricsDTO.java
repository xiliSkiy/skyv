package com.skyeye.collector.dto;

import lombok.Data;

/**
 * 采集器监控指标数据传输对象
 */
@Data
public class CollectorMetricsDTO {

    /**
     * 采集器ID
     */
    private Long collectorId;

    /**
     * CPU使用率
     */
    private String cpuUsage;

    /**
     * 内存使用率
     */
    private String memoryUsage;

    /**
     * 磁盘使用率
     */
    private String diskUsage;

    /**
     * 运行时长
     */
    private String uptime;

    /**
     * 进程数
     */
    private Integer processes;

    /**
     * 线程数
     */
    private Integer threads;

    /**
     * 当前活动任务数
     */
    private Integer activeTasks;

    /**
     * 版本
     */
    private String version;

    /**
     * 网络IO
     */
    private String networkIO;

    /**
     * 日志级别
     */
    private String logLevel;

    /**
     * 缓存使用情况
     */
    private String cacheUsage;
} 