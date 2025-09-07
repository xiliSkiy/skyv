package com.skyeye.collector.dto;

import lombok.Data;
import lombok.Builder;

import java.util.Map;

/**
 * 指标配置
 * 定义了数据采集的指标参数
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class MetricConfig {

    /**
     * 指标名称
     */
    private String metricName;

    /**
     * 指标类型（如：cpu_usage, memory_usage, network_traffic等）
     */
    private String metricType;

    /**
     * 插件类型（指定使用哪个插件进行采集）
     */
    private String pluginType;

    /**
     * 指标显示名称
     */
    private String displayName;

    /**
     * 指标描述
     */
    private String description;

    /**
     * 指标单位
     */
    private String unit;

    /**
     * 数据类型（如：GAUGE、COUNTER、HISTOGRAM等）
     */
    private MetricDataType dataType;

    /**
     * 指标参数配置
     * 不同插件的特定参数（如SNMP的OID、HTTP的URL等）
     */
    private Map<String, Object> parameters;

    /**
     * 采集超时时间（秒）
     */
    private int timeout = 30;

    /**
     * 重试次数
     */
    private int retryTimes = 3;

    /**
     * 重试间隔（毫秒）
     */
    private long retryInterval = 1000;

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = false;

    /**
     * 缓存时间（秒）
     */
    private int cacheTtl = 60;

    /**
     * 采集间隔（秒）
     */
    private int interval = 60;

    /**
     * 优先级（数值越小优先级越高）
     */
    private int priority = 5;

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 标签
     */
    private Map<String, String> tags;

    /**
     * 数据转换规则
     */
    private DataTransform transform;

    /**
     * 阈值配置
     */
    private ThresholdConfig thresholds;

    /**
     * 依赖的其他指标
     */
    private String[] dependencies;

    /**
     * 指标分组
     */
    private String group;

    /**
     * 指标数据类型枚举
     */
    public enum MetricDataType {
        GAUGE,      // 瞬时值
        COUNTER,    // 计数器
        HISTOGRAM,  // 直方图
        SUMMARY,    // 摘要
        STRING,     // 字符串
        BOOLEAN,    // 布尔值
        TIMESTAMP   // 时间戳
    }

    /**
     * 数据转换配置
     */
    @Data
    @Builder
    public static class DataTransform {
        /**
         * 转换表达式
         */
        private String expression;

        /**
         * 数值乘数
         */
        private Double multiplier;

        /**
         * 数值偏移
         */
        private Double offset;

        /**
         * 数据格式化
         */
        private String format;

        /**
         * 值映射
         */
        private Map<String, Object> valueMapping;
    }

    /**
     * 阈值配置
     */
    @Data
    @Builder
    public static class ThresholdConfig {
        /**
         * 警告阈值
         */
        private Double warningThreshold;

        /**
         * 严重阈值
         */
        private Double criticalThreshold;

        /**
         * 最小值
         */
        private Double minValue;

        /**
         * 最大值
         */
        private Double maxValue;

        /**
         * 阈值比较操作符
         */
        private ComparisonOperator operator = ComparisonOperator.GREATER_THAN;

        public enum ComparisonOperator {
            GREATER_THAN,       // >
            GREATER_EQUAL,      // >=
            LESS_THAN,          // <
            LESS_EQUAL,         // <=
            EQUAL,              // ==
            NOT_EQUAL,          // !=
            BETWEEN,            // between
            NOT_BETWEEN         // not between
        }
    }
}
