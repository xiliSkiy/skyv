package com.skyeye.collector.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import lombok.Data;

/**
 * 采集端心跳请求DTO
 */
@Data
public class HeartbeatDTO {
    
    /**
     * 采集端ID
     */
    private String collectorId;
    
    /**
     * 时间戳
     */
    @NotBlank(message = "时间戳不能为空")
    private String timestamp;
    
    /**
     * 状态(ONLINE/BUSY/ERROR)
     */
    @NotBlank(message = "状态不能为空")
    private String status;
    
    /**
     * 性能指标
     */
    @NotNull(message = "性能指标不能为空")
    @Valid
    private MetricsDTO metrics;
    
    /**
     * 错误信息(可选)
     */
    private String error;
    
    /**
     * 版本号
     */
    private String version;
    
    /**
     * 为兼容现有代码，获取采集器编码
     */
    public String getCollectorCode() {
        return this.collectorId;
    }
    
    /**
     * 性能指标DTO
     */
    @Data
    public static class MetricsDTO {
        /**
         * CPU使用率
         */
        private double cpu;
        
        /**
         * 内存使用率
         */
        private double memory;
        
        /**
         * 磁盘使用率
         */
        private double disk;
        
        /**
         * 运行中任务数
         */
        private int runningTasks;
    }
} 