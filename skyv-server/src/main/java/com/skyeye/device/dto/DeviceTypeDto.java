package com.skyeye.device.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

/**
 * 设备类型DTO
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class DeviceTypeDto {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 类型名称
     */
    private String name;

    /**
     * 类型编码
     */
    private String code;

    /**
     * 父类型ID
     */
    private Long parentId;

    /**
     * 图标
     */
    private String icon;

    /**
     * 支持的协议列表
     */
    private List<String> protocols;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 描述
     */
    private String description;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 设备数量
     */
    private Integer deviceCount;

    /**
     * 创建时间
     */
    private Timestamp createdAt;

    /**
     * 更新时间
     */
    private Timestamp updatedAt;

    /**
     * 子类型列表
     */
    private List<DeviceTypeDto> children;
} 