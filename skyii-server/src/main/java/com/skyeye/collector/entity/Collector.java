package com.skyeye.collector.entity;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Data;

/**
 * 采集端实体类
 */
@Data
@Entity
@Table(name = "tb_collectors")
public class Collector {
    
    /**
     * 主键ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 采集端唯一标识
     */
    @Column(name = "collector_id", unique = true, nullable = false)
    private String collectorId;
    
    /**
     * 采集器名称
     */
    @Column(name = "collector_name", unique = true)
    private String collectorName;
    
    /**
     * 采集器类型
     */
    @Column(name = "collector_type")
    private String collectorType;
    
    /**
     * 是否主采集器
     */
    @Column(name = "is_main")
    private Boolean isMain;
    
    /**
     * 状态信息
     */
    @Column(name = "status_info")
    private String statusInfo;
    
    /**
     * API密钥
     */
    @Column(name = "api_key")
    private String apiKey;
    
    /**
     * 主机名
     */
    @Column(name = "hostname", nullable = false)
    private String hostname;
    
    /**
     * IP地址
     */
    @Column(name = "ip_address", nullable = false)
    private String ipAddress;
    
    /**
     * 采集器版本
     */
    @Column(name = "version", nullable = false)
    private String version;
    
    /**
     * 支持的采集类型
     */
    @Column(name = "capabilities", nullable = false, length = 1000)
    private String capabilities;
    
    /**
     * 标签
     */
    @Column(name = "tags", length = 500)
    private String tags;
    
    /**
     * 状态 (注：数据库中存储为字符串，但与现有代码兼容支持Integer)
     */
    @Column(name = "status", nullable = false)
    private String status;
    
    /**
     * 最后心跳时间
     */
    @Column(name = "last_heartbeat_time")
    private LocalDateTime lastHeartbeatTime;
    
    /**
     * 删除时间（用于软删除）
     */
    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
    
    /**
     * CPU使用率
     */
    @Column(name = "cpu_usage")
    private Double cpuUsage;
    
    /**
     * 内存使用率
     */
    @Column(name = "memory_usage")
    private Double memoryUsage;
    
    /**
     * 磁盘使用率
     */
    @Column(name = "disk_usage")
    private Double diskUsage;
    
    /**
     * 运行中任务数
     */
    @Column(name = "running_tasks")
    private Integer runningTasks;
    
    /**
     * 最后错误信息
     */
    @Column(name = "last_error", length = 1000)
    private String lastError;
    
    /**
     * 最后错误时间
     */
    @Column(name = "last_error_time")
    private LocalDateTime lastErrorTime;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    /**
     * 更新时间
     */
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    /**
     * 主机地址（用于连接测试）
     */
    @Column(name = "host")
    private String host;
    
    /**
     * 网络区域
     */
    @Column(name = "network_zone")
    private String networkZone;
    
    /**
     * 端口
     */
    @Column(name = "port")
    private Integer port;
    
    /**
     * 设置状态（支持Integer入参，兼容现有代码）
     */
    public void setStatus(Integer status) {
        if (status != null) {
            this.status = status.toString();
        }
    }
    
    /**
     * 设置状态（支持字符串）
     */
    public void setStatus(String status) {
        this.status = status;
    }
    
    /**
     * 获取上次心跳时间（为兼容现有代码）
     */
    public LocalDateTime getLastHeartbeat() {
        return this.lastHeartbeatTime;
    }
    
    /**
     * 设置上次心跳时间（为兼容现有代码）
     */
    public void setLastHeartbeat(LocalDateTime time) {
        this.lastHeartbeatTime = time;
    }
} 