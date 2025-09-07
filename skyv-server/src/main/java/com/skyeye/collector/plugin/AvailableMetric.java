package com.skyeye.collector.plugin;

import lombok.Data;
import lombok.Builder;

import java.util.Map;
import java.util.List;

/**
 * 可用指标信息
 * 
 * @author SkyEye Team
 */
@Data
@Builder
public class AvailableMetric {

    /**
     * 指标名称
     */
    private String name;

    /**
     * 指标显示名称
     */
    private String displayName;

    /**
     * 指标描述
     */
    private String description;

    /**
     * 指标类型
     */
    private String type;

    /**
     * 数据类型
     */
    private DataType dataType;

    /**
     * 指标单位
     */
    private String unit;

    /**
     * 指标分类
     */
    private String category;

    /**
     * 指标标签
     */
    private Map<String, String> tags;

    /**
     * 是否为核心指标
     */
    private boolean core;

    /**
     * 采集复杂度（1-5，5最复杂）
     */
    private int complexity;

    /**
     * 建议采集间隔（秒）
     */
    private int recommendedInterval;

    /**
     * 指标参数
     */
    private Map<String, Object> parameters;

    /**
     * 依赖的其他指标
     */
    private List<String> dependencies;

    /**
     * 值范围
     */
    private ValueRange valueRange;

    /**
     * 示例值
     */
    private Object sampleValue;

    /**
     * 指标状态
     */
    private MetricStatus status;

    /**
     * 数据类型枚举
     */
    public enum DataType {
        INTEGER,    // 整数
        FLOAT,      // 浮点数
        STRING,     // 字符串
        BOOLEAN,    // 布尔值
        TIMESTAMP,  // 时间戳
        OBJECT,     // 对象
        ARRAY       // 数组
    }

    /**
     * 指标状态枚举
     */
    public enum MetricStatus {
        AVAILABLE,      // 可用
        DEPRECATED,     // 已弃用
        EXPERIMENTAL,   // 实验性
        UNSTABLE       // 不稳定
    }

    /**
     * 值范围
     */
    @Data
    @Builder
    public static class ValueRange {
        /**
         * 最小值
         */
        private Object minValue;

        /**
         * 最大值
         */
        private Object maxValue;

        /**
         * 默认值
         */
        private Object defaultValue;

        /**
         * 可选值列表
         */
        private List<Object> allowedValues;

        /**
         * 值格式
         */
        private String format;

        /**
         * 正则表达式
         */
        private String pattern;
    }
}
