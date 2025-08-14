package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备实体类
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "tb_devices")
public class Device extends BaseEntity {

    /**
     * 设备名称
     */
    @Column(name = "name", length = 100, nullable = false)
    private String name;

    /**
     * 设备类型ID
     */
    @Column(name = "device_type_id", nullable = false)
    private Long deviceTypeId;

    /**
     * 区域ID
     */
    @Column(name = "area_id")
    private Long areaId;

    /**
     * 分组ID
     */
    @Column(name = "group_id")
    private Long groupId;

    /**
     * 设备模板ID
     */
    @Column(name = "template_id")
    private Long templateId;

    /**
     * 设备协议ID
     */
    @Column(name = "protocol_id")
    private Long protocolId;

    /**
     * IP地址
     */
    @Column(name = "ip_address", columnDefinition = "INET")
    private String ipAddress;

    /**
     * 端口号
     */
    @Column(name = "port")
    private Integer port;

    /**
     * MAC地址
     */
    @Column(name = "mac_address", columnDefinition = "MACADDR")
    private String macAddress;

    /**
     * 协议类型
     */
    @Column(name = "protocol", length = 20)
    private String protocol;

    /**
     * 设备状态 (1:在线, 2:离线, 3:故障, 4:维护)
     */
    @Column(name = "status")
    private Integer status = 2; // 默认离线

    /**
     * 健康评分 (0-100)
     */
    @Column(name = "health_score")
    private Integer healthScore = 100;

    /**
     * 设备配置 (JSON格式)
     */
    @Column(name = "config", columnDefinition = "JSONB")
    private String config;

    /**
     * 设备凭据 (JSON格式，加密存储)
     */
    @Column(name = "credentials", columnDefinition = "JSONB")
    private String credentials;

    /**
     * 位置描述
     */
    @Column(name = "location", length = 200)
    private String location;

    /**
     * 制造商
     */
    @Column(name = "manufacturer", length = 100)
    private String manufacturer;

    /**
     * 型号
     */
    @Column(name = "model", length = 100)
    private String model;

    /**
     * 序列号
     */
    @Column(name = "serial_number", length = 100)
    private String serialNumber;

    /**
     * 固件版本
     */
    @Column(name = "firmware_version", length = 50)
    private String firmwareVersion;

    /**
     * 购买日期
     */
    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    /**
     * 保修日期
     */
    @Column(name = "warranty_date")
    private LocalDate warrantyDate;

    /**
     * 描述
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /**
     * 备注
     */
    @Column(name = "remark", columnDefinition = "TEXT")
    private String remark;

    /**
     * 最后在线时间
     */
    @Column(name = "last_online_at")
    private LocalDateTime lastOnlineAt;

    /**
     * 最后采集时间
     */
    @Column(name = "last_collect_at")
    private LocalDateTime lastCollectAt;
} 