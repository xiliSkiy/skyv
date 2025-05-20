package com.skyeye.metrics.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 * 指标采集历史数据传输对象
 */
@Data
public class MetricHistoryDTO {
    private Long id;
    private Long metricId;
    private String metricName;
    private String metricKey;
    private Long deviceId;
    private String deviceName;
    private Long collectorId;
    private String collectorName;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime collectionTime;
    
    private String rawValue;
    private String processedValue;
    private Integer status;
    private String statusInfo;
    private Long collectionDuration;
    private String unit;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    // 状态枚举
    public static final Integer STATUS_SUCCESS = 0;
    public static final Integer STATUS_FAILED = 1;
    public static final Integer STATUS_TIMEOUT = 2;
    public static final Integer STATUS_PROCESSING = 3;
    
    // 状态文字描述
    public String getStatusText() {
        if (status == null) {
            return "未知";
        }
        switch (status) {
            case 0:
                return "成功";
            case 1:
                return "失败";
            case 2:
                return "超时";
            case 3:
                return "处理中";
            default:
                return "未知";
        }
    }
} 