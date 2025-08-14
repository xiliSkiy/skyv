package com.skyeye.device.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 设备区域数据传输对象
 */
@Data
public class DeviceAreaDto {

    private Long id;

    @NotBlank(message = "区域名称不能为空")
    @Size(max = 100, message = "区域名称长度不能超过100个字符")
    private String name;

    @Size(max = 50, message = "区域编码长度不能超过50个字符")
    private String code;

    private Long parentId;

    private String parentName;

    private Integer level;

    private String fullPath;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    private Map<String, Object> locationInfo;

    private Integer sortOrder;

    private Long createdBy;

    private String createdByName;

    private Long updatedBy;

    private String updatedByName;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;

    // 子区域列表
    private List<DeviceAreaDto> children;

    // 设备数量
    private Integer deviceCount;
} 