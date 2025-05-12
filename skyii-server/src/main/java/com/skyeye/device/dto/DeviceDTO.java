package com.skyeye.device.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 设备DTO
 */
@Data
public class DeviceDTO {
    
    /**
     * 设备名称
     */
    @NotBlank(message = "设备名称不能为空")
    @Size(max = 100, message = "设备名称长度不能超过100个字符")
    private String name;
    
    /**
     * 设备编码
     */
    @NotBlank(message = "设备编码不能为空")
    @Size(max = 50, message = "设备编码长度不能超过50个字符")
    private String code;
    
    /**
     * 设备类型
     */
    @NotBlank(message = "设备类型不能为空")
    @Size(max = 50, message = "设备类型长度不能超过50个字符")
    private String type;
    
    /**
     * 设备IP地址
     */
    @Size(max = 50, message = "IP地址长度不能超过50个字符")
    private String ipAddress;
    
    /**
     * 设备端口
     */
    private Integer port;
    
    /**
     * 设备用户名
     */
    @Size(max = 50, message = "用户名长度不能超过50个字符")
    private String username;
    
    /**
     * 设备密码
     */
    @Size(max = 100, message = "密码长度不能超过100个字符")
    private String password;
    
    /**
     * 设备位置
     */
    @Size(max = 200, message = "位置长度不能超过200个字符")
    private String location;
    
    /**
     * 设备描述
     */
    @Size(max = 500, message = "描述长度不能超过500个字符")
    private String description;
    
    /**
     * 所属分组ID
     */
    private Long groupId;
} 