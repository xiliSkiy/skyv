package com.skyeye.device.dto;

import lombok.Data;

import java.util.List;

/**
 * 设备查询请求DTO
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class DeviceQueryRequest {

    /**
     * 设备名称（模糊查询）
     */
    private String name;

    /**
     * 设备类型ID
     */
    private Long deviceTypeId;

    /**
     * 区域ID
     */
    private Long areaId;

    /**
     * 分组ID
     */
    private Long groupId;

    /**
     * 设备状态
     */
    private Integer status;

    /**
     * 协议类型
     */
    private String protocol;

    /**
     * IP地址
     */
    private String ipAddress;

    /**
     * 制造商
     */
    private String manufacturer;

    /**
     * 标签列表
     */
    private List<String> tags;

    /**
     * 页码（前端传入从1开始，后端需要转换为从0开始）
     */
    private Integer page = 1;

    /**
     * 每页大小
     */
    private Integer size = 20;

    /**
     * 排序字段
     */
    private String sortBy = "updatedAt";

    /**
     * 排序方向（asc/desc）
     */
    private String sortDir = "desc";
} 