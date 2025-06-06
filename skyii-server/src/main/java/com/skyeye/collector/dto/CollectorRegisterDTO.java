package com.skyeye.collector.dto;

import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import lombok.Data;

/**
 * 采集端注册请求DTO
 */
@Data
public class CollectorRegisterDTO {
    
    /**
     * 采集端ID，首次注册为空
     */
    private String collectorId;
    
    /**
     * 主机名
     */
    @NotBlank(message = "主机名不能为空")
    private String hostname;
    
    /**
     * IP地址
     */
    @NotBlank(message = "IP地址不能为空")
    private String ip;
    
    /**
     * 采集器版本
     */
    @NotBlank(message = "版本号不能为空")
    private String version;
    
    /**
     * 支持的采集类型
     */
    @NotEmpty(message = "支持的采集类型不能为空")
    private List<String> capabilities;
    
    /**
     * 标签
     */
    private List<String> tags;
    
    /**
     * 端口
     */
    private Integer port;
    
    /**
     * 为兼容现有代码，获取名称（返回hostname）
     */
    public String getName() {
        return this.hostname;
    }
    
    /**
     * 为兼容现有代码，获取IP地址
     */
    public String getIpAddress() {
        return this.ip;
    }
} 