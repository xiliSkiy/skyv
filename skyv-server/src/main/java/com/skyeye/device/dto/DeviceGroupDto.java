package com.skyeye.device.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 设备分组数据传输对象
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Data
public class DeviceGroupDto {

    /**
     * 分组ID
     */
    private Long id;

    /**
     * 分组名称
     */
    private String name;

    /**
     * 分组类型
     */
    private String type;

    /**
     * 分组描述
     */
    private String description;

    /**
     * 分组规则
     */
    private String rule;

    /**
     * 是否重要分组
     */
    private Boolean isImportant;

    /**
     * 排序号
     */
    private Integer sortOrder;

    /**
     * 是否启用
     */
    private Boolean isEnabled;

    /**
     * 设备数量
     */
    private Integer deviceCount;

    /**
     * 在线设备数量
     */
    private Integer onlineCount;

    /**
     * 报警设备数量
     */
    private Integer alertCount;

    /**
     * 设备ID列表（用于分组设备关联）
     */
    private List<Long> deviceIds;

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