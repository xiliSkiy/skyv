package com.skyeye.collector.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集器与设备关联实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_collector_device_relations")
public class CollectorDeviceRelation extends BaseEntity {

    /**
     * 采集器ID
     */
    @Column(name = "collector_id", nullable = false)
    private Long collectorId;

    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    /**
     * 创建方式：0-手动分配，1-自动分配
     */
    @Column(name = "create_mode", nullable = false)
    private Integer createMode = 0;

    /**
     * 优先级：数字越大优先级越高
     */
    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    /**
     * 关联状态：0-不活跃，1-活跃
     */
    @Column(name = "status", nullable = false)
    private Integer status = 1;

    /**
     * 上次采集时间
     */
    @Column(name = "last_collect_time")
    private LocalDateTime lastCollectTime;

    /**
     * 备注
     */
    @Column(name = "remarks", length = 255)
    private String remarks;
} 