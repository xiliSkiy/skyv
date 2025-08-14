package com.skyeye.common.constant;

/**
 * 系统常量定义
 * 
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
public class Constants {

    /**
     * 系统相关常量
     */
    public static final class System {
        /** 系统名称 */
        public static final String SYSTEM_NAME = "SkyEye";
        /** 系统版本 */
        public static final String SYSTEM_VERSION = "1.0.0";
        /** 默认用户 */
        public static final String DEFAULT_USER = "system";
        /** 超级管理员角色 */
        public static final String ADMIN_ROLE = "ADMIN";
        /** 普通用户角色 */
        public static final String USER_ROLE = "USER";
    }

    /**
     * 设备状态常量
     */
    public static final class DeviceStatus {
        /** 离线 */
        public static final Integer OFFLINE = 0;
        /** 在线 */
        public static final Integer ONLINE = 1;
        /** 故障 */
        public static final Integer FAULT = 2;
        /** 维护中 */
        public static final Integer MAINTENANCE = 3;
    }

    /**
     * 任务状态常量
     */
    public static final class TaskStatus {
        /** 禁用 */
        public static final Integer DISABLED = 0;
        /** 启用 */
        public static final Integer ENABLED = 1;
        /** 暂停 */
        public static final Integer PAUSED = 2;
        /** 运行中 */
        public static final Integer RUNNING = 3;
    }

    /**
     * 报警级别常量
     */
    public static final class AlertLevel {
        /** 低级 */
        public static final String LOW = "LOW";
        /** 中级 */
        public static final String MEDIUM = "MEDIUM";
        /** 高级 */
        public static final String HIGH = "HIGH";
        /** 严重 */
        public static final String CRITICAL = "CRITICAL";
    }

    /**
     * 报警状态常量
     */
    public static final class AlertStatus {
        /** 待处理 */
        public static final String PENDING = "PENDING";
        /** 处理中 */
        public static final String PROCESSING = "PROCESSING";
        /** 已解决 */
        public static final String RESOLVED = "RESOLVED";
        /** 已忽略 */
        public static final String IGNORED = "IGNORED";
    }

    /**
     * 操作类型常量
     */
    public static final class OperationType {
        /** 创建 */
        public static final String CREATE = "CREATE";
        /** 更新 */
        public static final String UPDATE = "UPDATE";
        /** 删除 */
        public static final String DELETE = "DELETE";
        /** 查询 */
        public static final String SELECT = "SELECT";
        /** 执行 */
        public static final String EXECUTE = "EXECUTE";
        /** 登录 */
        public static final String LOGIN = "LOGIN";
        /** 登出 */
        public static final String LOGOUT = "LOGOUT";
    }

    /**
     * 缓存相关常量
     */
    public static final class Cache {
        /** 设备缓存 */
        public static final String DEVICES = "devices";
        /** 用户缓存 */
        public static final String USERS = "users";
        /** 系统设置缓存 */
        public static final String SYSTEM_SETTINGS = "system_settings";
        /** 设备统计缓存 */
        public static final String DEVICE_STATISTICS = "device_statistics";
        /** 报警统计缓存 */
        public static final String ALERT_STATISTICS = "alert_statistics";
    }

    /**
     * JWT相关常量
     */
    public static final class Jwt {
        /** Token前缀 */
        public static final String TOKEN_PREFIX = "Bearer ";
        /** Token头名称 */
        public static final String TOKEN_HEADER = "Authorization";
        /** 用户ID声明 */
        public static final String USER_ID_CLAIM = "userId";
        /** 用户名声明 */
        public static final String USERNAME_CLAIM = "username";
        /** 角色声明 */
        public static final String ROLES_CLAIM = "roles";
    }

    /**
     * WebSocket相关常量
     */
    public static final class WebSocket {
        /** 设备状态更新 */
        public static final String DEVICE_STATUS_UPDATE = "DEVICE_STATUS_UPDATE";
        /** 采集数据推送 */
        public static final String COLLECTION_DATA = "COLLECTION_DATA";
        /** 报警推送 */
        public static final String ALERT_PUSH = "ALERT_PUSH";
        /** 连接建立 */
        public static final String CONNECTION_ESTABLISHED = "CONNECTION_ESTABLISHED";
        /** 心跳消息 */
        public static final String HEARTBEAT = "HEARTBEAT";
    }

    /**
     * 文件相关常量
     */
    public static final class File {
        /** 最大文件大小 */
        public static final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
        /** 允许的图片格式 */
        public static final String[] IMAGE_EXTENSIONS = {".jpg", ".jpeg", ".png", ".gif", ".bmp"};
        /** 允许的文档格式 */
        public static final String[] DOCUMENT_EXTENSIONS = {".pdf", ".doc", ".docx", ".xls", ".xlsx", ".txt"};
    }

    /**
     * 时间相关常量
     */
    public static final class Time {
        /** 默认日期格式 */
        public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
        /** 默认时间格式 */
        public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";
        /** 默认日期时间格式 */
        public static final String DEFAULT_DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
        /** ISO日期时间格式 */
        public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    }
} 