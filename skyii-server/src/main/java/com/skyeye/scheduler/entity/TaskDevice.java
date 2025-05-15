package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 任务设备关联实体类
 */
@Data
@Entity
@Table(name = "tb_task_devices")
@EqualsAndHashCode(callSuper = true)
public class TaskDevice extends BaseEntity {

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    /**
     * 任务关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    /**
     * 设备ID
     */
    @Column(name = "device_id", nullable = false)
    private Long deviceId;
    
    /**
     * 设备名称（冗余字段，便于查询）
     */
    @Column(name = "device_name", length = 100)
    private String deviceName;
    
    /**
     * 设备类型
     */
    @Column(name = "device_type", length = 50)
    private String deviceType;
    
    /**
     * 设备位置
     */
    @Column(name = "device_location", length = 255)
    private String deviceLocation;
} 