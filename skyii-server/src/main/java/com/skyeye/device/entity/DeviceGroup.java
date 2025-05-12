package com.skyeye.device.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * 设备分组实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity
@Table(name = "device_groups")
public class DeviceGroup extends BaseEntity {

    /**
     * 分组名称
     */
    @Column(nullable = false, length = 100)
    private String name;

    /**
     * 分组编码
     */
    @Column(nullable = false, unique = true, length = 50)
    private String code;

    /**
     * 父分组ID
     */
    @Column(name = "parent_id")
    private Long parentId;

    /**
     * 分组描述
     */
    @Column(length = 500)
    private String description;

    /**
     * 排序号
     */
    @Column
    private Integer sort = 0;
} 