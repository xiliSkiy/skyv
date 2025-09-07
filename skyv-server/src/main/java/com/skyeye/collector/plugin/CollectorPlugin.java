package com.skyeye.collector.plugin;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.CollectorConfig;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.device.entity.Device;
import com.skyeye.device.entity.DeviceType;

import java.util.List;
import java.util.Map;

/**
 * 数据采集插件接口
 * 定义了可扩展的数据采集插件规范
 * 
 * @author SkyEye Team
 */
public interface CollectorPlugin {

    /**
     * 获取插件类型标识
     * 
     * @return 插件类型（如：SNMP、HTTP、SSH等）
     */
    String getPluginType();

    /**
     * 获取插件名称
     * 
     * @return 插件显示名称
     */
    String getPluginName();

    /**
     * 获取插件版本
     * 
     * @return 插件版本号
     */
    String getPluginVersion();

    /**
     * 获取插件描述
     * 
     * @return 插件功能描述
     */
    String getPluginDescription();

    /**
     * 检查插件是否支持指定的设备类型
     * 
     * @param deviceType 设备类型
     * @return 是否支持
     */
    boolean supports(DeviceType deviceType);

    /**
     * 检查插件是否支持指定的协议
     * 
     * @param protocol 协议类型
     * @return 是否支持
     */
    boolean supportsProtocol(String protocol);

    /**
     * 获取插件支持的所有协议列表
     * 
     * @return 支持的协议列表
     */
    List<String> getSupportedProtocols();

    /**
     * 获取插件支持的指标类型
     * 
     * @return 支持的指标类型列表
     */
    List<String> getSupportedMetricTypes();

    /**
     * 执行数据采集
     * 
     * @param device 目标设备
     * @param metricConfig 指标配置
     * @param context 采集上下文
     * @return 采集结果
     */
    CollectionResult collect(Device device, MetricConfig metricConfig, CollectionContext context);

    /**
     * 批量执行数据采集
     * 
     * @param device 目标设备
     * @param metricConfigs 指标配置列表
     * @param context 采集上下文
     * @return 采集结果列表
     */
    List<CollectionResult> collectBatch(Device device, List<MetricConfig> metricConfigs, CollectionContext context);

    /**
     * 初始化插件
     * 
     * @param config 插件配置
     * @throws PluginException 初始化异常
     */
    void initialize(CollectorConfig config) throws PluginException;

    /**
     * 销毁插件，释放资源
     */
    void destroy();

    /**
     * 检查插件健康状态
     * 
     * @return 健康状态信息
     */
    PluginHealthStatus getHealthStatus();

    /**
     * 获取插件统计信息
     * 
     * @return 统计信息
     */
    PluginStatistics getStatistics();

    /**
     * 验证设备连接
     * 
     * @param device 设备信息
     * @param context 采集上下文
     * @return 连接测试结果
     */
    ConnectionTestResult testConnection(Device device, CollectionContext context);

    /**
     * 获取设备可用的指标列表
     * 
     * @param device 设备信息
     * @param context 采集上下文
     * @return 可用指标列表
     */
    List<AvailableMetric> discoverMetrics(Device device, CollectionContext context);

    /**
     * 验证指标配置
     * 
     * @param metricConfig 指标配置
     * @return 验证结果
     */
    ConfigValidationResult validateConfig(MetricConfig metricConfig);

    /**
     * 获取插件配置模板
     * 
     * @return 配置模板
     */
    Map<String, Object> getConfigTemplate();

    /**
     * 是否支持并发采集
     * 
     * @return 是否支持并发
     */
    boolean supportsConcurrentCollection();

    /**
     * 获取建议的并发度
     * 
     * @return 建议的并发线程数
     */
    int getRecommendedConcurrency();

    /**
     * 获取插件优先级（数值越小优先级越高）
     * 
     * @return 优先级
     */
    int getPriority();
}
