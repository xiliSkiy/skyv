package com.skyeye.collector.lifecycle.impl;

import com.skyeye.collector.dto.CollectorConfig;
import com.skyeye.collector.lifecycle.PluginLifecycleManager;
import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.plugin.PluginException;
import com.skyeye.collector.plugin.PluginHealthStatus;
import com.skyeye.collector.registry.PluginRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * 默认插件生命周期管理器实现
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DefaultPluginLifecycleManager implements PluginLifecycleManager {

    private final PluginRegistry pluginRegistry;

    /**
     * 插件状态映射
     */
    private final Map<String, PluginLifecycleState> pluginStates = new ConcurrentHashMap<>();

    /**
     * 插件配置映射
     */
    private final Map<String, CollectorConfig> pluginConfigs = new ConcurrentHashMap<>();

    /**
     * 生命周期监听器列表
     */
    private final List<PluginLifecycleListener> listeners = new CopyOnWriteArrayList<>();

    /**
     * 插件状态锁
     */
    private final Map<String, Object> stateLocks = new ConcurrentHashMap<>();

    /**
     * 执行器服务
     */
    private ExecutorService executorService;

    /**
     * 健康检查调度器
     */
    private ScheduledExecutorService healthCheckScheduler;

    /**
     * 管理器状态
     */
    private volatile boolean started = false;

    @Override
    public void start() throws PluginException {
        if (started) {
            log.warn("插件生命周期管理器已经启动");
            return;
        }

        log.info("启动插件生命周期管理器");

        try {
            // 初始化执行器
            executorService = Executors.newCachedThreadPool(r -> {
                Thread thread = new Thread(r, "plugin-lifecycle-" + System.currentTimeMillis());
                thread.setDaemon(true);
                return thread;
            });

            // 初始化健康检查调度器
            healthCheckScheduler = Executors.newScheduledThreadPool(2, r -> {
                Thread thread = new Thread(r, "plugin-health-check-" + System.currentTimeMillis());
                thread.setDaemon(true);
                return thread;
            });

            // 启动定期健康检查
            startPeriodicHealthCheck();

            started = true;
            log.info("插件生命周期管理器启动成功");

        } catch (Exception e) {
            throw new PluginException("LIFECYCLE_MANAGER_START_FAILED", 
                    "启动插件生命周期管理器失败", e);
        }
    }

    @Override
    public void stop() throws PluginException {
        if (!started) {
            log.warn("插件生命周期管理器未启动");
            return;
        }

        log.info("停止插件生命周期管理器");

        try {
            // 停止所有插件
            stopAllPluginsInOrder();

            // 关闭执行器
            if (executorService != null) {
                executorService.shutdown();
                if (!executorService.awaitTermination(30, TimeUnit.SECONDS)) {
                    executorService.shutdownNow();
                }
            }

            // 关闭健康检查调度器
            if (healthCheckScheduler != null) {
                healthCheckScheduler.shutdown();
                if (!healthCheckScheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                    healthCheckScheduler.shutdownNow();
                }
            }

            started = false;
            log.info("插件生命周期管理器停止成功");

        } catch (Exception e) {
            throw new PluginException("LIFECYCLE_MANAGER_STOP_FAILED", 
                    "停止插件生命周期管理器失败", e);
        }
    }

    @Override
    public void initializePlugin(CollectorPlugin plugin, CollectorConfig config) throws PluginException {
        String pluginType = plugin.getPluginType();
        Object lock = stateLocks.computeIfAbsent(pluginType, k -> new Object());

        synchronized (lock) {
            PluginLifecycleState currentState = pluginStates.get(pluginType);
            if (currentState != null && currentState != PluginLifecycleState.CREATED) {
                throw new PluginException("INVALID_STATE_TRANSITION", 
                        String.format("插件 %s 当前状态 %s 不允许初始化", pluginType, currentState));
            }

            try {
                log.info("开始初始化插件: {}", pluginType);
                changeState(pluginType, PluginLifecycleState.INITIALIZING);

                // 保存配置
                pluginConfigs.put(pluginType, config);

                // 初始化插件
                plugin.initialize(config);

                changeState(pluginType, PluginLifecycleState.INITIALIZED);
                log.info("插件初始化成功: {}", pluginType);

                // 触发监听器
                notifyInitialized(pluginType);

            } catch (Exception e) {
                changeState(pluginType, PluginLifecycleState.ERROR);
                throw new PluginException("PLUGIN_INITIALIZATION_FAILED", 
                        "插件初始化失败: " + pluginType, e);
            }
        }
    }

    @Override
    public void startPlugin(String pluginType) throws PluginException {
        Object lock = stateLocks.computeIfAbsent(pluginType, k -> new Object());

        synchronized (lock) {
            PluginLifecycleState currentState = pluginStates.get(pluginType);
            if (currentState != PluginLifecycleState.INITIALIZED && 
                currentState != PluginLifecycleState.STOPPED) {
                throw new PluginException("INVALID_STATE_TRANSITION", 
                        String.format("插件 %s 当前状态 %s 不允许启动", pluginType, currentState));
            }

            try {
                log.info("开始启动插件: {}", pluginType);
                changeState(pluginType, PluginLifecycleState.STARTING);

                // 这里可以添加插件启动的具体逻辑
                // 比如连接资源、启动线程等

                changeState(pluginType, PluginLifecycleState.STARTED);
                log.info("插件启动成功: {}", pluginType);

                // 设置为运行状态
                changeState(pluginType, PluginLifecycleState.RUNNING);

                // 触发监听器
                notifyStarted(pluginType);

            } catch (Exception e) {
                changeState(pluginType, PluginLifecycleState.ERROR);
                notifyError(pluginType, e);
                throw new PluginException("PLUGIN_START_FAILED", 
                        "插件启动失败: " + pluginType, e);
            }
        }
    }

    @Override
    public void stopPlugin(String pluginType) throws PluginException {
        Object lock = stateLocks.computeIfAbsent(pluginType, k -> new Object());

        synchronized (lock) {
            PluginLifecycleState currentState = pluginStates.get(pluginType);
            if (currentState != PluginLifecycleState.RUNNING && 
                currentState != PluginLifecycleState.STARTED) {
                log.warn("插件 {} 当前状态 {} 不需要停止", pluginType, currentState);
                return;
            }

            try {
                log.info("开始停止插件: {}", pluginType);
                changeState(pluginType, PluginLifecycleState.STOPPING);

                // 这里可以添加插件停止的具体逻辑
                // 比如断开连接、停止线程等

                changeState(pluginType, PluginLifecycleState.STOPPED);
                log.info("插件停止成功: {}", pluginType);

                // 触发监听器
                notifyStopped(pluginType);

            } catch (Exception e) {
                changeState(pluginType, PluginLifecycleState.ERROR);
                notifyError(pluginType, e);
                throw new PluginException("PLUGIN_STOP_FAILED", 
                        "插件停止失败: " + pluginType, e);
            }
        }
    }

    @Override
    public void restartPlugin(String pluginType) throws PluginException {
        log.info("重启插件: {}", pluginType);
        
        // 先停止
        if (pluginStates.get(pluginType) == PluginLifecycleState.RUNNING) {
            stopPlugin(pluginType);
        }
        
        // 再启动
        startPlugin(pluginType);
    }

    @Override
    public void destroyPlugin(String pluginType) throws PluginException {
        Object lock = stateLocks.computeIfAbsent(pluginType, k -> new Object());

        synchronized (lock) {
            try {
                log.info("开始销毁插件: {}", pluginType);
                changeState(pluginType, PluginLifecycleState.DESTROYING);

                // 先停止插件
                PluginLifecycleState currentState = pluginStates.get(pluginType);
                if (currentState == PluginLifecycleState.RUNNING || 
                    currentState == PluginLifecycleState.STARTED) {
                    stopPlugin(pluginType);
                }

                // 获取插件并销毁
                Optional<CollectorPlugin> pluginOpt = pluginRegistry.getPlugin(pluginType);
                if (pluginOpt.isPresent()) {
                    pluginOpt.get().destroy();
                }

                // 清理状态和配置
                pluginStates.remove(pluginType);
                pluginConfigs.remove(pluginType);
                stateLocks.remove(pluginType);

                log.info("插件销毁成功: {}", pluginType);

            } catch (Exception e) {
                changeState(pluginType, PluginLifecycleState.ERROR);
                throw new PluginException("PLUGIN_DESTROY_FAILED", 
                        "插件销毁失败: " + pluginType, e);
            }
        }
    }

    @Override
    public PluginLifecycleState getPluginState(String pluginType) {
        return pluginStates.getOrDefault(pluginType, PluginLifecycleState.CREATED);
    }

    @Override
    public Map<String, PluginLifecycleState> getAllPluginStates() {
        return new HashMap<>(pluginStates);
    }

    @Override
    public boolean isPluginReady(String pluginType) {
        PluginLifecycleState state = pluginStates.get(pluginType);
        return state == PluginLifecycleState.RUNNING;
    }

    @Override
    public boolean waitForPluginReady(String pluginType, long timeoutMs) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        while (System.currentTimeMillis() - startTime < timeoutMs) {
            if (isPluginReady(pluginType)) {
                return true;
            }
            Thread.sleep(100);
        }
        return false;
    }

    @Override
    public CollectorConfig getPluginConfig(String pluginType) {
        return pluginConfigs.get(pluginType);
    }

    @Override
    public void updatePluginConfig(String pluginType, CollectorConfig config) throws PluginException {
        if (!pluginRegistry.isPluginRegistered(pluginType)) {
            throw new PluginException("PLUGIN_NOT_FOUND", "插件不存在: " + pluginType);
        }

        // 保存新配置
        pluginConfigs.put(pluginType, config);

        // 如果插件正在运行，需要重启以应用新配置
        PluginLifecycleState currentState = pluginStates.get(pluginType);
        if (currentState == PluginLifecycleState.RUNNING) {
            log.info("插件 {} 配置已更新，准备重启以应用新配置", pluginType);
            restartPlugin(pluginType);
        }
    }

    @Override
    public PluginHealthCheckResult performHealthCheck(String pluginType) {
        Optional<CollectorPlugin> pluginOpt = pluginRegistry.getPlugin(pluginType);
        if (!pluginOpt.isPresent()) {
            return new PluginHealthCheckResult(pluginType, false, "插件不存在");
        }

        CollectorPlugin plugin = pluginOpt.get();
        long startTime = System.currentTimeMillis();

        try {
            PluginHealthStatus healthStatus = plugin.getHealthStatus();
            long responseTime = System.currentTimeMillis() - startTime;

            PluginHealthCheckResult result = new PluginHealthCheckResult(
                    pluginType, 
                    healthStatus.getStatus() == PluginHealthStatus.HealthStatus.HEALTHY,
                    healthStatus.getMessage()
            );
            result.setResponseTime(responseTime);

            Map<String, Object> details = new HashMap<>();
            details.put("healthStatus", healthStatus.getStatus());
            details.put("checkTime", healthStatus.getCheckTime());
            details.put("consecutiveFailures", healthStatus.getConsecutiveFailures());
            result.setDetails(details);

            return result;

        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            PluginHealthCheckResult result = new PluginHealthCheckResult(
                    pluginType, false, "健康检查异常: " + e.getMessage());
            result.setResponseTime(responseTime);
            return result;
        }
    }

    @Override
    public List<PluginHealthCheckResult> performAllHealthChecks() {
        return pluginRegistry.getAllPluginTypes().parallelStream()
                .map(this::performHealthCheck)
                .collect(Collectors.toList());
    }

    @Override
    public void addLifecycleListener(PluginLifecycleListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    @Override
    public void removeLifecycleListener(PluginLifecycleListener listener) {
        listeners.remove(listener);
    }

    @Override
    public Map<String, List<String>> getPluginDependencyGraph() {
        // 这里可以实现插件依赖关系的解析
        // 当前返回空映射，表示没有依赖关系
        return new HashMap<>();
    }

    @Override
    public void startAllPluginsInOrder() throws PluginException {
        log.info("按依赖顺序启动所有插件");

        List<String> pluginTypes = pluginRegistry.getAllPluginTypes();
        
        // 这里可以根据依赖关系进行拓扑排序
        // 当前按优先级排序
        pluginTypes.sort((type1, type2) -> {
            Optional<CollectorPlugin> plugin1 = pluginRegistry.getPlugin(type1);
            Optional<CollectorPlugin> plugin2 = pluginRegistry.getPlugin(type2);
            
            if (plugin1.isPresent() && plugin2.isPresent()) {
                return Integer.compare(plugin1.get().getPriority(), plugin2.get().getPriority());
            }
            return 0;
        });

        for (String pluginType : pluginTypes) {
            try {
                Optional<CollectorPlugin> pluginOpt = pluginRegistry.getPlugin(pluginType);
                if (pluginOpt.isPresent()) {
                    CollectorConfig config = pluginConfigs.get(pluginType);
                    if (config == null) {
                        config = CollectorConfig.builder()
                                .pluginType(pluginType)
                                .enabled(true)
                                .build();
                    }

                    initializePlugin(pluginOpt.get(), config);
                    startPlugin(pluginType);
                }
            } catch (Exception e) {
                log.error("启动插件失败: {}", pluginType, e);
                // 可以选择继续启动其他插件或抛出异常
            }
        }
    }

    @Override
    public void stopAllPluginsInOrder() throws PluginException {
        log.info("按依赖顺序停止所有插件");

        List<String> pluginTypes = new ArrayList<>(pluginRegistry.getAllPluginTypes());
        
        // 按优先级倒序停止（先停止优先级低的）
        pluginTypes.sort((type1, type2) -> {
            Optional<CollectorPlugin> plugin1 = pluginRegistry.getPlugin(type1);
            Optional<CollectorPlugin> plugin2 = pluginRegistry.getPlugin(type2);
            
            if (plugin1.isPresent() && plugin2.isPresent()) {
                return Integer.compare(plugin2.get().getPriority(), plugin1.get().getPriority());
            }
            return 0;
        });

        for (String pluginType : pluginTypes) {
            try {
                stopPlugin(pluginType);
            } catch (Exception e) {
                log.error("停止插件失败: {}", pluginType, e);
                // 继续停止其他插件
            }
        }
    }

    /**
     * 改变插件状态
     */
    private void changeState(String pluginType, PluginLifecycleState newState) {
        PluginLifecycleState oldState = pluginStates.put(pluginType, newState);
        log.debug("插件 {} 状态变化: {} -> {}", pluginType, oldState, newState);
        
        // 触发状态变化监听器
        notifyStateChanged(pluginType, oldState, newState);
    }

    /**
     * 启动定期健康检查
     */
    private void startPeriodicHealthCheck() {
        healthCheckScheduler.scheduleAtFixedRate(() -> {
            try {
                List<PluginHealthCheckResult> results = performAllHealthChecks();
                // 处理健康检查结果
                results.forEach(result -> {
                    // 检查健康状态变化并通知监听器
                    // 这里可以添加健康状态变化的逻辑
                    if (!result.isHealthy()) {
                        log.warn("插件 {} 健康检查失败: {}", result.getPluginType(), result.getMessage());
                    }
                });
            } catch (Exception e) {
                log.error("定期健康检查异常", e);
            }
        }, 60, 60, TimeUnit.SECONDS); // 每分钟检查一次
    }

    /**
     * 通知状态变化
     */
    private void notifyStateChanged(String pluginType, PluginLifecycleState oldState, PluginLifecycleState newState) {
        for (PluginLifecycleListener listener : listeners) {
            try {
                listener.onStateChanged(pluginType, oldState, newState);
            } catch (Exception e) {
                log.error("插件生命周期监听器异常", e);
            }
        }
    }

    /**
     * 通知初始化完成
     */
    private void notifyInitialized(String pluginType) {
        for (PluginLifecycleListener listener : listeners) {
            try {
                listener.onInitialized(pluginType);
            } catch (Exception e) {
                log.error("插件生命周期监听器异常", e);
            }
        }
    }

    /**
     * 通知启动完成
     */
    private void notifyStarted(String pluginType) {
        for (PluginLifecycleListener listener : listeners) {
            try {
                listener.onStarted(pluginType);
            } catch (Exception e) {
                log.error("插件生命周期监听器异常", e);
            }
        }
    }

    /**
     * 通知停止完成
     */
    private void notifyStopped(String pluginType) {
        for (PluginLifecycleListener listener : listeners) {
            try {
                listener.onStopped(pluginType);
            } catch (Exception e) {
                log.error("插件生命周期监听器异常", e);
            }
        }
    }

    /**
     * 通知错误事件
     */
    private void notifyError(String pluginType, Exception error) {
        for (PluginLifecycleListener listener : listeners) {
            try {
                listener.onError(pluginType, error);
            } catch (Exception e) {
                log.error("插件生命周期监听器异常", e);
            }
        }
    }
}
