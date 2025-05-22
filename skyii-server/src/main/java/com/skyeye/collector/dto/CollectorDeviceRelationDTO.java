package com.skyeye.collector.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 采集器与设备关联数据传输对象
 */
@Data
public class CollectorDeviceRelationDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 采集器ID
     */
    @NotNull(message = "采集器ID不能为空")
    private Long collectorId;

    /**
     * 设备ID
     */
    @NotNull(message = "设备ID不能为空")
    private Long deviceId;
    
    /**
     * 设备名称
     */
    private String deviceName;
    
    /**
     * 设备类型
     */
    private String deviceType;
    
    /**
     * 设备IP
     */
    private String deviceIp;

    /**
     * 创建方式：0-手动分配，1-自动分配
     */
    private Integer createMode = 0;

    /**
     * 优先级：数字越大优先级越高
     */
    private Integer priority = 0;

    /**
     * 关联状态：0-不活跃，1-活跃
     */
    private Integer status = 1;

    /**
     * 上次采集时间
     */
    private LocalDateTime lastCollectTime;

    /**
     * 备注
     */
    private String remarks;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 