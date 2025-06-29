package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.time.LocalDateTime;

/**
 * 设备实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_devices")
public class Device extends BaseEntity {

    /**
     * 设备名称
     */
    @Column(name = "device_name", nullable = false, length = 100)
    private String name;

    /**
     * 设备编码
     */
    @Column(name = "device_code", nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 设备类型
     */
    @Column(name = "device_type", nullable = false, length = 50)
    private String type;

    /**
     * 设备型号
     */
    @Column(name = "device_model", length = 100)
    private String deviceModel;

    /**
     * 设备IP地址
     */
    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    /**
     * 设备端口
     */
    @Column(name = "port")
    private Integer port;

    /**
     * 设备用户名
     */
    @Column(name = "username", length = 50)
    private String username;

    /**
     * 设备密码
     */
    @Column(name = "password", length = 100)
    private String password;

    /**
     * 设备状态（0-离线，1-在线，2-故障）
     */
    @Column(name = "status", nullable = false)
    private Integer status = 0;

    /**
     * 设备位置
     */
    @Column(name = "location", length = 255)
    private String location;

    /**
     * 设备描述
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 所属分组ID
     */
    @Column(name = "group_id")
    private Long groupId;

    /**
     * RTSP地址
     */
    @Column(name = "rtsp_url", length = 255)
    private String rtspUrl;

    /**
     * 最后心跳时间
     */
    @Column(name = "last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;

    /**
     * 最后在线时间
     */
    @Column(name = "last_online_time")
    private LocalDateTime lastOnlineTime;

    /**
     * 设备标签，多个标签用逗号分隔
     */
    @Column(name = "tags", length = 255)
    private String tags;

    /**
     * 设备名称（用于兼容）
     */
    public String getDeviceName() {
        return this.name;
    }

    /**
     * 设备类型（用于兼容）
     */
    public String getDeviceType() {
        return this.type;
    }
} 