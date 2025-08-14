package com.skyeye.device.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 设备数据传输对象
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class DeviceDto {

    /**
     * 设备ID
     */
    private Long id;

    /**
     * 设备名称
     */
    private String name;

    /**
     * 设备类型ID
     */
    private Long deviceTypeId;

    /**
     * 设备类型名称
     */
    private String deviceTypeName;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 区域名称
     */
    private String areaName;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 分组名称
     */
    private String groupName;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 端口号
     */
    private Integer port;

    /**
     * MAC地址
     */
    private String macAddress;

    /**
     * 协议类型
     */
    private String protocol;

    /**
     * 设备状态
     */
    private Integer status;

    /**
     * 设备状态名称
     */
    private String statusName;

    /**
     * 健康评分
     */
    private Integer healthScore;

    /**
     * 位置描述
     */
    private String location;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 型号
     */
    private String model;

    /**
     * 序列号
     */
    private String serialNumber;

    /**
     * 固件版本
     */
    private String firmwareVersion;

    /**
     * 购买日期
     */
    private LocalDate purchaseDate;

    /**
     * 保修日期
     */
    private LocalDate warrantyDate;

    /**
     * 描述
     */
    private String description;

    /**
     * 备注
     */
    private String remark;

    /**
     * 最后在线时间
     */
    private LocalDateTime lastOnlineAt;

    /**
     * 最后采集时间
     */
    private LocalDateTime lastCollectAt;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人
     */
    private Long createdBy;

    /**
     * 更新人
     */
    private Long updatedBy;
} 