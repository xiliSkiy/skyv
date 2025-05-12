package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 设备实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "devices")
public class Device extends BaseEntity {

    /**
     * 设备名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 设备编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 设备类型
     */
    @Column(nullable = false, length = 50)
    private String type;

    /**
     * 设备IP地址
     */
    @Column(length = 50)
    private String ipAddress;

    /**
     * 设备端口
     */
    @Column
    private Integer port;

    /**
     * 设备用户名
     */
    @Column(length = 50)
    private String username;

    /**
     * 设备密码
     */
    @Column(length = 100)
    private String password;

    /**
     * 设备状态（0-离线，1-在线，2-故障）
     */
    @Column(nullable = false)
    private Integer status = 0;

    /**
     * 设备位置
     */
    @Column(length = 200)
    private String location;

    /**
     * 设备描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 所属分组ID
     */
    @Column(name = "group_id")
    private Long groupId;

    /**
     * 最后心跳时间
     */
    @Column(name = "last_heartbeat_time")
    private java.time.LocalDateTime lastHeartbeatTime;
} 