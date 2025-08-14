package com.skyeye.device.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 设备模板数据传输对象
 */
@Data
public class DeviceTemplateDto {

    private Long id;

    @NotBlank(message = "模板名称不能为空")
    @Size(max = 100, message = "模板名称长度不能超过100个字符")
    private String name;

    @NotBlank(message = "模板编码不能为空")
    @Size(max = 50, message = "模板编码长度不能超过50个字符")
    private String code;

    private Long deviceTypeId;

    private String deviceTypeName;

    private Long protocolId;

    private String protocolName;

    @Size(max = 100, message = "制造商长度不能超过100个字符")
    private String manufacturer;

    @Size(max = 100, message = "设备型号长度不能超过100个字符")
    private String model;

    private String configTemplate;

    private String defaultSettings;

    private String metrics;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    private Boolean isEnabled;

    private Integer usageCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 