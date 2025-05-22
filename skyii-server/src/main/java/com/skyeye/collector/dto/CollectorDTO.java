package com.skyeye.collector.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

/**
 * 采集器数据传输对象
 */
@Data
public class CollectorDTO {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 采集器名称
     */
    @NotBlank(message = "采集器名称不能为空")
    @Size(min = 2, max = 50, message = "采集器名称长度必须在2-50个字符之间")
    private String collectorName;

    /**
     * 采集器类型
     */
    @NotBlank(message = "采集器类型不能为空")
    private String collectorType;

    /**
     * 主机地址
     */
    @NotBlank(message = "主机地址不能为空")
    @Pattern(regexp = "^([a-zA-Z0-9][-a-zA-Z0-9]{0,62}(\\.[a-zA-Z0-9][-a-zA-Z0-9]{0,62})+\\.?|((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?))$", 
            message = "主机地址格式不正确，请输入有效的域名或IP地址")
    private String host;

    /**
     * 端口
     */
    @NotNull(message = "端口不能为空")
    private Integer port;

    /**
     * 是否主采集器：0-否，1-是
     */
    private Integer isMain = 0;

    /**
     * 描述
     */
    private String description;

    /**
     * 配置参数（JSON格式）
     */
    private String configParams;

    /**
     * 状态：0-异常，1-正常，2-警告
     */
    private Integer status;

    /**
     * 状态信息
     */
    private String statusInfo;

    /**
     * 最后心跳时间
     */
    private LocalDateTime lastHeartbeat;

    /**
     * 网络区域
     */
    private String networkZone;

    /**
     * 用户名（用于认证）
     */
    private String username;

    /**
     * 密码（用于认证）
     */
    private String password;

    /**
     * API密钥（用于认证）
     */
    private String apiKey;

    /**
     * 版本
     */
    private String version;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;
} 