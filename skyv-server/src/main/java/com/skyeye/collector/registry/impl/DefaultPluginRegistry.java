package com.skyeye.collector.registry.impl;

import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.plugin.PluginException;
import com.skyeye.collector.registry.PluginRegistry;
import com.skyeye.device.entity.DeviceType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * 默认插件注册表实现
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
public class DefaultPluginRegistry implements PluginRegistry {

    /**
     * 插件存储映射
     */
    private final Map<String, CollectorPlugin> plugins = new ConcurrentHashMap<>();

    /**
     * 插件状态映射
     */
    private final Map<String, PluginStatus> pluginStatuses = new ConcurrentHashMap<>();

    /**
     * 插件信息映射
     */
    private final Map<String, PluginInfo> pluginInfos = new ConcurrentHashMap<>();

    /**
     * 插件监听器列表
     */
    private final List<PluginRegistryListener> listeners = new CopyOnWriteArrayList<>();

    @Override
    public void registerPlugin(CollectorPlugin plugin) throws PluginException {
        if (plugin == null) {
            throw new PluginException("INVALID_PLUGIN", "插件不能为空");
        }

        String pluginType = plugin.getPluginType();
        if (pluginType == null || pluginType.trim().isEmpty()) {
            throw new PluginException("INVALID_PLUGIN_TYPE", "插件类型不能为空");
        }

        // 验证插件
        PluginValidationResult validationResult = validatePlugin(plugin);
        if (!validationResult.isValid()) {
            throw new PluginException("PLUGIN_VALIDATION_FAILED", 
                    "插件验证失败: " + validationResult.getMessage());
        }

        // 检查是否已注册
        if (plugins.containsKey(pluginType)) {
            log.warn("插件 {} 已存在，将被替换", pluginType);
            unregisterPlugin(pluginType);
        }

        try {
            // 注册插件
            plugins.put(pluginType, plugin);
            pluginStatuses.put(pluginType, PluginStatus.REGISTERED);

            // 创建插件信息
            PluginInfo info = createPluginInfo(plugin);
            pluginInfos.put(pluginType, info);

            // 启用插件
            enablePlugin(pluginType);

            log.info("插件注册成功: {} - {}", pluginType, plugin.getPluginName());

            // 触发监听器
            notifyPluginRegistered(pluginType, plugin);

        } catch (Exception e) {
            // 注册失败时清理
            plugins.remove(pluginType);
            pluginStatuses.remove(pluginType);
            pluginInfos.remove(pluginType);
            
            throw new PluginException("PLUGIN_REGISTRATION_FAILED", 
                    "插件注册失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void registerPlugins(List<CollectorPlugin> plugins) throws PluginException {
        if (plugins == null || plugins.isEmpty()) {
            return;
        }

        List<String> failedPlugins = new ArrayList<>();
        List<Exception> exceptions = new ArrayList<>();

        for (CollectorPlugin plugin : plugins) {
            try {
                registerPlugin(plugin);
            } catch (Exception e) {
                failedPlugins.add(plugin.getPluginType());
                exceptions.add(e);
                log.error("批量注册插件失败: {}", plugin.getPluginType(), e);
            }
        }

        if (!failedPlugins.isEmpty()) {
            throw new PluginException("BATCH_REGISTRATION_FAILED", 
                    String.format("批量注册失败，失败插件: %s", String.join(", ", failedPlugins)));
        }
    }

    @Override
    public boolean unregisterPlugin(String pluginType) {
        CollectorPlugin plugin = plugins.get(pluginType);
        if (plugin == null) {
            log.warn("尝试注销不存在的插件: {}", pluginType);
            return false;
        }

        try {
            // 禁用插件
            disablePlugin(pluginType);

            // 销毁插件
            plugin.destroy();

            // 移除插件
            plugins.remove(pluginType);
            pluginStatuses.remove(pluginType);
            pluginInfos.remove(pluginType);

            log.info("插件注销成功: {}", pluginType);

            // 触发监听器
            notifyPluginUnregistered(pluginType);

            return true;

        } catch (Exception e) {
            log.error("注销插件失败: {}", pluginType, e);
            return false;
        }
    }

    @Override
    public Optional<CollectorPlugin> getPlugin(String pluginType) {
        updateLastAccessTime(pluginType);
        return Optional.ofNullable(plugins.get(pluginType));
    }

    @Override
    public List<CollectorPlugin> getAllPlugins() {
        return new ArrayList<>(plugins.values());
    }

    @Override
    public List<String> getAllPluginTypes() {
        return new ArrayList<>(plugins.keySet());
    }

    @Override
    public List<CollectorPlugin> getPluginsForDeviceType(DeviceType deviceType) {
        return plugins.values().stream()
                .filter(plugin -> plugin.supports(deviceType))
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectorPlugin> getPluginsForProtocol(String protocol) {
        return plugins.values().stream()
                .filter(plugin -> plugin.supportsProtocol(protocol))
                .collect(Collectors.toList());
    }

    @Override
    public boolean isPluginRegistered(String pluginType) {
        return plugins.containsKey(pluginType);
    }

    @Override
    public int getPluginCount() {
        return plugins.size();
    }

    @Override
    public int getEnabledPluginCount() {
        return (int) pluginStatuses.values().stream()
                .filter(status -> status == PluginStatus.ENABLED)
                .count();
    }

    @Override
    public Map<String, PluginInfo> getPluginInfos() {
        return new HashMap<>(pluginInfos);
    }

    @Override
    public Optional<PluginStatus> getPluginStatus(String pluginType) {
        return Optional.ofNullable(pluginStatuses.get(pluginType));
    }

    @Override
    public boolean enablePlugin(String pluginType) {
        CollectorPlugin plugin = plugins.get(pluginType);
        if (plugin == null) {
            log.warn("尝试启用不存在的插件: {}", pluginType);
            return false;
        }

        PluginStatus currentStatus = pluginStatuses.get(pluginType);
        if (currentStatus == PluginStatus.ENABLED) {
            log.debug("插件 {} 已处于启用状态", pluginType);
            return true;
        }

        try {
            pluginStatuses.put(pluginType, PluginStatus.LOADING);

            // 这里可以添加插件启用的具体逻辑
            // 比如检查依赖、加载资源等

            pluginStatuses.put(pluginType, PluginStatus.ENABLED);
            log.info("插件启用成功: {}", pluginType);

            // 触发监听器
            notifyPluginEnabled(pluginType);

            return true;

        } catch (Exception e) {
            pluginStatuses.put(pluginType, PluginStatus.ERROR);
            log.error("启用插件失败: {}", pluginType, e);
            notifyPluginError(pluginType, e);
            return false;
        }
    }

    @Override
    public boolean disablePlugin(String pluginType) {
        CollectorPlugin plugin = plugins.get(pluginType);
        if (plugin == null) {
            log.warn("尝试禁用不存在的插件: {}", pluginType);
            return false;
        }

        PluginStatus currentStatus = pluginStatuses.get(pluginType);
        if (currentStatus == PluginStatus.DISABLED) {
            log.debug("插件 {} 已处于禁用状态", pluginType);
            return true;
        }

        try {
            pluginStatuses.put(pluginType, PluginStatus.UNLOADING);

            // 这里可以添加插件禁用的具体逻辑
            // 比如停止正在运行的任务、释放资源等

            pluginStatuses.put(pluginType, PluginStatus.DISABLED);
            log.info("插件禁用成功: {}", pluginType);

            // 触发监听器
            notifyPluginDisabled(pluginType);

            return true;

        } catch (Exception e) {
            pluginStatuses.put(pluginType, PluginStatus.ERROR);
            log.error("禁用插件失败: {}", pluginType, e);
            notifyPluginError(pluginType, e);
            return false;
        }
    }

    @Override
    public void reloadPlugin(String pluginType) throws PluginException {
        CollectorPlugin plugin = plugins.get(pluginType);
        if (plugin == null) {
            throw new PluginException("PLUGIN_NOT_FOUND", "插件不存在: " + pluginType);
        }

        log.info("开始重载插件: {}", pluginType);

        try {
            // 禁用插件
            disablePlugin(pluginType);

            // 重新初始化插件
            PluginInfo info = pluginInfos.get(pluginType);
            if (info != null) {
                // 可以在这里重新加载配置等
            }

            // 启用插件
            enablePlugin(pluginType);

            log.info("插件重载成功: {}", pluginType);

        } catch (Exception e) {
            pluginStatuses.put(pluginType, PluginStatus.ERROR);
            throw new PluginException("PLUGIN_RELOAD_FAILED", 
                    "插件重载失败: " + e.getMessage(), e);
        }
    }

    @Override
    public PluginValidationResult validatePlugin(CollectorPlugin plugin) {
        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // 基本验证
        if (plugin.getPluginType() == null || plugin.getPluginType().trim().isEmpty()) {
            errors.add("插件类型不能为空");
        }

        if (plugin.getPluginName() == null || plugin.getPluginName().trim().isEmpty()) {
            errors.add("插件名称不能为空");
        }

        if (plugin.getPluginVersion() == null || plugin.getPluginVersion().trim().isEmpty()) {
            warnings.add("插件版本为空");
        }

        // 检查支持的协议
        List<String> protocols = plugin.getSupportedProtocols();
        if (protocols == null || protocols.isEmpty()) {
            warnings.add("插件未声明支持的协议");
        }

        // 检查支持的指标类型
        List<String> metricTypes = plugin.getSupportedMetricTypes();
        if (metricTypes == null || metricTypes.isEmpty()) {
            warnings.add("插件未声明支持的指标类型");
        }

        boolean valid = errors.isEmpty();
        String message = valid ? "插件验证通过" : "插件验证失败: " + String.join(", ", errors);

        PluginValidationResult result = new PluginValidationResult(valid, message);
        result.setErrors(errors);
        result.setWarnings(warnings);

        return result;
    }

    @Override
    public void clear() {
        log.info("开始清空所有插件");

        // 注销所有插件
        List<String> pluginTypes = new ArrayList<>(plugins.keySet());
        for (String pluginType : pluginTypes) {
            unregisterPlugin(pluginType);
        }

        // 清空容器
        plugins.clear();
        pluginStatuses.clear();
        pluginInfos.clear();

        log.info("所有插件已清空");
    }

    @Override
    public void addPluginListener(PluginRegistryListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removePluginListener(PluginRegistryListener listener) {
        listeners.remove(listener);
    }

    /**
     * 创建插件信息
     */
    private PluginInfo createPluginInfo(CollectorPlugin plugin) {
        PluginInfo info = new PluginInfo();
        info.setPluginType(plugin.getPluginType());
        info.setPluginName(plugin.getPluginName());
        info.setPluginVersion(plugin.getPluginVersion());
        info.setDescription(plugin.getPluginDescription());
        info.setSupportedProtocols(plugin.getSupportedProtocols());
        info.setSupportedMetricTypes(plugin.getSupportedMetricTypes());
        info.setStatus(PluginStatus.REGISTERED);
        info.setRegistrationTime(System.currentTimeMillis());
        info.setLastAccessTime(System.currentTimeMillis());
        info.setAccessCount(0);
        return info;
    }

    /**
     * 更新最后访问时间
     */
    private void updateLastAccessTime(String pluginType) {
        PluginInfo info = pluginInfos.get(pluginType);
        if (info != null) {
            info.setLastAccessTime(System.currentTimeMillis());
            info.setAccessCount(info.getAccessCount() + 1);
        }
    }

    /**
     * 通知插件注册
     */
    private void notifyPluginRegistered(String pluginType, CollectorPlugin plugin) {
        for (PluginRegistryListener listener : listeners) {
            try {
                listener.onPluginRegistered(pluginType, plugin);
            } catch (Exception e) {
                log.error("插件监听器异常: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 通知插件注销
     */
    private void notifyPluginUnregistered(String pluginType) {
        for (PluginRegistryListener listener : listeners) {
            try {
                listener.onPluginUnregistered(pluginType);
            } catch (Exception e) {
                log.error("插件监听器异常: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 通知插件启用
     */
    private void notifyPluginEnabled(String pluginType) {
        for (PluginRegistryListener listener : listeners) {
            try {
                listener.onPluginEnabled(pluginType);
            } catch (Exception e) {
                log.error("插件监听器异常: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 通知插件禁用
     */
    private void notifyPluginDisabled(String pluginType) {
        for (PluginRegistryListener listener : listeners) {
            try {
                listener.onPluginDisabled(pluginType);
            } catch (Exception e) {
                log.error("插件监听器异常: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }

    /**
     * 通知插件错误
     */
    private void notifyPluginError(String pluginType, Exception error) {
        for (PluginRegistryListener listener : listeners) {
            try {
                listener.onPluginError(pluginType, error);
            } catch (Exception e) {
                log.error("插件监听器异常: {}", listener.getClass().getSimpleName(), e);
            }
        }
    }
}
