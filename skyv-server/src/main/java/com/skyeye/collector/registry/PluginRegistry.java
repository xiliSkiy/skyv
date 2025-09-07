package com.skyeye.collector.registry;

import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.plugin.PluginException;
import com.skyeye.device.entity.DeviceType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 插件注册表接口
 * 管理所有已注册的采集插件
 * 
 * @author SkyEye Team
 */
public interface PluginRegistry {

    /**
     * 注册插件
     * 
     * @param plugin 插件实例
     * @throws PluginException 注册异常
     */
    void registerPlugin(CollectorPlugin plugin) throws PluginException;

    /**
     * 批量注册插件
     * 
     * @param plugins 插件列表
     * @throws PluginException 注册异常
     */
    void registerPlugins(List<CollectorPlugin> plugins) throws PluginException;

    /**
     * 注销插件
     * 
     * @param pluginType 插件类型
     * @return 是否注销成功
     */
    boolean unregisterPlugin(String pluginType);

    /**
     * 获取插件
     * 
     * @param pluginType 插件类型
     * @return 插件实例
     */
    Optional<CollectorPlugin> getPlugin(String pluginType);

    /**
     * 获取所有插件
     * 
     * @return 所有已注册的插件
     */
    List<CollectorPlugin> getAllPlugins();

    /**
     * 获取所有插件类型
     * 
     * @return 插件类型列表
     */
    List<String> getAllPluginTypes();

    /**
     * 获取支持指定设备类型的插件
     * 
     * @param deviceType 设备类型
     * @return 支持的插件列表
     */
    List<CollectorPlugin> getPluginsForDeviceType(DeviceType deviceType);

    /**
     * 获取支持指定协议的插件
     * 
     * @param protocol 协议类型
     * @return 支持的插件列表
     */
    List<CollectorPlugin> getPluginsForProtocol(String protocol);

    /**
     * 检查插件是否已注册
     * 
     * @param pluginType 插件类型
     * @return 是否已注册
     */
    boolean isPluginRegistered(String pluginType);

    /**
     * 获取插件数量
     * 
     * @return 已注册的插件数量
     */
    int getPluginCount();

    /**
     * 获取启用的插件数量
     * 
     * @return 启用的插件数量
     */
    int getEnabledPluginCount();

    /**
     * 获取插件信息
     * 
     * @return 插件信息映射
     */
    Map<String, PluginInfo> getPluginInfos();

    /**
     * 获取插件状态
     * 
     * @param pluginType 插件类型
     * @return 插件状态
     */
    Optional<PluginStatus> getPluginStatus(String pluginType);

    /**
     * 启用插件
     * 
     * @param pluginType 插件类型
     * @return 是否启用成功
     */
    boolean enablePlugin(String pluginType);

    /**
     * 禁用插件
     * 
     * @param pluginType 插件类型
     * @return 是否禁用成功
     */
    boolean disablePlugin(String pluginType);

    /**
     * 重载插件
     * 
     * @param pluginType 插件类型
     * @throws PluginException 重载异常
     */
    void reloadPlugin(String pluginType) throws PluginException;

    /**
     * 验证插件
     * 
     * @param plugin 插件实例
     * @return 验证结果
     */
    PluginValidationResult validatePlugin(CollectorPlugin plugin);

    /**
     * 清空所有插件
     */
    void clear();

    /**
     * 添加插件监听器
     * 
     * @param listener 监听器
     */
    void addPluginListener(PluginRegistryListener listener);

    /**
     * 移除插件监听器
     * 
     * @param listener 监听器
     */
    void removePluginListener(PluginRegistryListener listener);

    /**
     * 插件信息
     */
    class PluginInfo {
        private String pluginType;
        private String pluginName;
        private String pluginVersion;
        private String description;
        private List<String> supportedProtocols;
        private List<String> supportedMetricTypes;
        private PluginStatus status;
        private long registrationTime;
        private long lastAccessTime;
        private int accessCount;

        // getters and setters
        public String getPluginType() { return pluginType; }
        public void setPluginType(String pluginType) { this.pluginType = pluginType; }
        
        public String getPluginName() { return pluginName; }
        public void setPluginName(String pluginName) { this.pluginName = pluginName; }
        
        public String getPluginVersion() { return pluginVersion; }
        public void setPluginVersion(String pluginVersion) { this.pluginVersion = pluginVersion; }
        
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        
        public List<String> getSupportedProtocols() { return supportedProtocols; }
        public void setSupportedProtocols(List<String> supportedProtocols) { this.supportedProtocols = supportedProtocols; }
        
        public List<String> getSupportedMetricTypes() { return supportedMetricTypes; }
        public void setSupportedMetricTypes(List<String> supportedMetricTypes) { this.supportedMetricTypes = supportedMetricTypes; }
        
        public PluginStatus getStatus() { return status; }
        public void setStatus(PluginStatus status) { this.status = status; }
        
        public long getRegistrationTime() { return registrationTime; }
        public void setRegistrationTime(long registrationTime) { this.registrationTime = registrationTime; }
        
        public long getLastAccessTime() { return lastAccessTime; }
        public void setLastAccessTime(long lastAccessTime) { this.lastAccessTime = lastAccessTime; }
        
        public int getAccessCount() { return accessCount; }
        public void setAccessCount(int accessCount) { this.accessCount = accessCount; }
    }

    /**
     * 插件状态枚举
     */
    enum PluginStatus {
        REGISTERED,     // 已注册
        ENABLED,        // 已启用
        DISABLED,       // 已禁用
        ERROR,          // 错误状态
        LOADING,        // 加载中
        UNLOADING      // 卸载中
    }

    /**
     * 插件验证结果
     */
    class PluginValidationResult {
        private boolean valid;
        private String message;
        private List<String> errors;
        private List<String> warnings;

        public PluginValidationResult(boolean valid, String message) {
            this.valid = valid;
            this.message = message;
        }

        // getters and setters
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
        
        public List<String> getErrors() { return errors; }
        public void setErrors(List<String> errors) { this.errors = errors; }
        
        public List<String> getWarnings() { return warnings; }
        public void setWarnings(List<String> warnings) { this.warnings = warnings; }
    }

    /**
     * 插件注册表监听器
     */
    interface PluginRegistryListener {
        /**
         * 插件注册事件
         */
        void onPluginRegistered(String pluginType, CollectorPlugin plugin);

        /**
         * 插件注销事件
         */
        void onPluginUnregistered(String pluginType);

        /**
         * 插件启用事件
         */
        void onPluginEnabled(String pluginType);

        /**
         * 插件禁用事件
         */
        void onPluginDisabled(String pluginType);

        /**
         * 插件错误事件
         */
        void onPluginError(String pluginType, Exception error);
    }
}
