package com.skyeye.device.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 设备协议数据传输对象
 */
@Data
public class DeviceProtocolDto {

    private Long id;

    @NotBlank(message = "协议名称不能为空")
    @Size(max = 50, message = "协议名称长度不能超过50个字符")
    private String name;

    @NotBlank(message = "协议编码不能为空")
    @Size(max = 50, message = "协议编码长度不能超过50个字符")
    private String code;

    @Size(max = 20, message = "协议版本长度不能超过20个字符")
    private String version;

    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;

    private Integer port;

    private String configSchema;

    private Boolean isEnabled;

    private Integer usageCount;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
} 