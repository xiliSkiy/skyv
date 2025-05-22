package com.skyeye.collector.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 采集器实体类
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Entity(name = "DeviceCollector")
@Table(name = "tb_collectors")
public class Collector extends BaseEntity {

    /**
     * 采集器名称
     */
    @Column(name = "collector_name", nullable = false, length = 100)
    private String collectorName;

    /**
     * 采集器类型
     */
    @Column(name = "collector_type", nullable = false, length = 50)
    private String collectorType;

    /**
     * 主机地址
     */
    @Column(name = "host", length = 100)
    private String host;

    /**
     * 端口
     */
    @Column(name = "port")
    private Integer port;

    /**
     * 是否主采集器：0-否，1-是
     */
    @Column(name = "is_main", nullable = false, columnDefinition = "TINYINT DEFAULT 0")
    private Integer isMain = 0;

    /**
     * 描述
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * 配置参数（JSON格式）
     */
    @Column(name = "config_params", columnDefinition = "TEXT")
    private String configParams;

    /**
     * 状态：0-异常，1-正常，2-警告
     */
    @Column(name = "status", nullable = false, columnDefinition = "TINYINT DEFAULT 1")
    private Integer status = 1;

    /**
     * 状态信息
     */
    @Column(name = "status_info", length = 255)
    private String statusInfo;

    /**
     * 最后心跳时间
     */
    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    /**
     * 网络区域
     */
    @Column(name = "network_zone", length = 100)
    private String networkZone;

    /**
     * 用户名（用于认证）
     */
    @Column(name = "username", length = 100)
    private String username;

    /**
     * 密码（用于认证）
     */
    @Column(name = "password", length = 255)
    private String password;

    /**
     * API密钥（用于认证）
     */
    @Column(name = "api_key", length = 255)
    private String apiKey;

    /**
     * 版本
     */
    @Column(name = "version", length = 50)
    private String version;
} 