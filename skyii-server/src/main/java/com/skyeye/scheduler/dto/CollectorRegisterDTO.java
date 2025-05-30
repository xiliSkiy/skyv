package com.skyeye.scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 采集器注册DTO
 */
@Data
public class CollectorRegisterDTO {
    
    /**
     * 采集器唯一编码
     */
    @NotBlank(message = "采集器编码不能为空")
    private String collectorCode;
    
    /**
     * 采集器名称
     */
    @NotBlank(message = "采集器名称不能为空")
    private String name;
    
    /**
     * 主机名
     */
    private String hostName;
    
    /**
     * IP地址
     */
    private String ipAddress;
    
    /**
     * 端口
     */
    private Integer port;
    
    /**
     * 版本
     */
    @NotBlank(message = "版本信息不能为空")
    private String version;
    
    /**
     * 采集能力(JSON格式)
     */
    private String capabilities;
    
    /**
     * 系统信息(JSON格式)
     */
    private String systemInfo;
    
    /**
     * 额外配置(JSON格式)
     */
    private String config;
} 