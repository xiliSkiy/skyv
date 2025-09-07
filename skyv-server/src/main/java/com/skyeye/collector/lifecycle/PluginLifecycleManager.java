package com.skyeye.collector.lifecycle;

import com.skyeye.collector.dto.CollectorConfig;
import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.plugin.PluginException;

import java.util.List;
import java.util.Map;

/**
 * 插件生命周期管理器接口
 * 负责管理插件的完整生命周期
 * 
 * @author SkyEye Team
 */
public interface PluginLifecycleManager {

    /**
     * 启动插件管理器
     * 
     * @throws PluginException 启动异常
     */
    void start() throws PluginException;

    /**
     * 停止插件管理器
     * 
     * @throws PluginException 停止异常
     */
    void stop() throws PluginException;

    /**
     * 初始化插件
     * 
     * @param plugin 插件实例
     * @param config 插件配置
     * @throws PluginException 初始化异常
     */
    void initializePlugin(CollectorPlugin plugin, CollectorConfig config) throws PluginException;

    /**
     * 启动插件
     * 
     * @param pluginType 插件类型
     * @throws PluginException 启动异常
     */
    void startPlugin(String pluginType) throws PluginException;

    /**
     * 停止插件
     * 
     * @param pluginType 插件类型
     * @throws PluginException 停止异常
     */
    void stopPlugin(String pluginType) throws PluginException;

    /**
     * 重启插件
     * 
     * @param pluginType 插件类型
     * @throws PluginException 重启异常
     */
    void restartPlugin(String pluginType) throws PluginException;

    /**
     * 销毁插件
     * 
     * @param pluginType 插件类型
     * @throws PluginException 销毁异常
     */
    void destroyPlugin(String pluginType) throws PluginException;

    /**
     * 获取插件生命周期状态
     * 
     * @param pluginType 插件类型
     * @return 生命周期状态
     */
    PluginLifecycleState getPluginState(String pluginType);

    /**
     * 获取所有插件的生命周期状态
     * 
     * @return 插件状态映射
     */
    Map<String, PluginLifecycleState> getAllPluginStates();

    /**
     * 检查插件是否就绪
     * 
     * @param pluginType 插件类型
     * @return 是否就绪
     */
    boolean isPluginReady(String pluginType);

    /**
     * 等待插件就绪
     * 
     * @param pluginType 插件类型
     * @param timeoutMs 超时时间（毫秒）
     * @return 是否在超时时间内就绪
     * @throws InterruptedException 等待中断异常
     */
    boolean waitForPluginReady(String pluginType, long timeoutMs) throws InterruptedException;

    /**
     * 获取插件配置
     * 
     * @param pluginType 插件类型
     * @return 插件配置
     */
    CollectorConfig getPluginConfig(String pluginType);

    /**
     * 更新插件配置
     * 
     * @param pluginType 插件类型
     * @param config 新配置
     * @throws PluginException 更新异常
     */
    void updatePluginConfig(String pluginType, CollectorConfig config) throws PluginException;

    /**
     * 执行插件健康检查
     * 
     * @param pluginType 插件类型
     * @return 健康检查结果
     */
    PluginHealthCheckResult performHealthCheck(String pluginType);

    /**
     * 执行所有插件健康检查
     * 
     * @return 健康检查结果列表
     */
    List<PluginHealthCheckResult> performAllHealthChecks();

    /**
     * 添加生命周期监听器
     * 
     * @param listener 监听器
     */
    void addLifecycleListener(PluginLifecycleListener listener);

    /**
     * 移除生命周期监听器
     * 
     * @param listener 监听器
     */
    void removeLifecycleListener(PluginLifecycleListener listener);

    /**
     * 获取插件依赖图
     * 
     * @return 依赖关系图
     */
    Map<String, List<String>> getPluginDependencyGraph();

    /**
     * 按依赖顺序启动所有插件
     * 
     * @throws PluginException 启动异常
     */
    void startAllPluginsInOrder() throws PluginException;

    /**
     * 按依赖顺序停止所有插件
     * 
     * @throws PluginException 停止异常
     */
    void stopAllPluginsInOrder() throws PluginException;

    /**
     * 插件生命周期状态枚举
     */
    enum PluginLifecycleState {
        CREATED,        // 已创建
        INITIALIZING,   // 初始化中
        INITIALIZED,    // 已初始化
        STARTING,       // 启动中
        STARTED,        // 已启动
        RUNNING,        // 运行中
        STOPPING,       // 停止中
        STOPPED,        // 已停止
        DESTROYING,     // 销毁中
        DESTROYED,      // 已销毁
        ERROR,          // 错误状态
        SUSPENDED       // 暂停状态
    }

    /**
     * 插件健康检查结果
     */
    class PluginHealthCheckResult {
        private String pluginType;
        private boolean healthy;
        private String message;
        private long checkTime;
        private long responseTime;
        private Map<String, Object> details;

        public PluginHealthCheckResult(String pluginType, boolean healthy, String message) {
            this.pluginType = pluginType;
            this.healthy = healthy;
            this.message = message;
            this.checkTime = System.currentTimeMillis();
        }

        // getters and setters
        public String getPluginType() { return pluginType; }
        public void setPluginType(String pluginType) { this.pluginType = pluginType; }
        
        public boolean isHealthy() { return healthy; }
        public void setHealthy(boolean healthy) { this.healthy = healthy; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public long getCheckTime() { return checkTime; }
        public void setCheckTime(long checkTime) { this.checkTime = checkTime; }
        
        public long getResponseTime() { return responseTime; }
        public void setResponseTime(long responseTime) { this.responseTime = responseTime; }
        
        public Map<String, Object> getDetails() { return details; }
        public void setDetails(Map<String, Object> details) { this.details = details; }
    }

    /**
     * 插件生命周期监听器
     */
    interface PluginLifecycleListener {
        /**
         * 插件状态变化事件
         */
        void onStateChanged(String pluginType, PluginLifecycleState oldState, PluginLifecycleState newState);

        /**
         * 插件初始化完成事件
         */
        void onInitialized(String pluginType);

        /**
         * 插件启动完成事件
         */
        void onStarted(String pluginType);

        /**
         * 插件停止完成事件
         */
        void onStopped(String pluginType);

        /**
         * 插件错误事件
         */
        void onError(String pluginType, Exception error);

        /**
         * 插件健康状态变化事件
         */
        void onHealthChanged(String pluginType, boolean healthy);
    }
}
