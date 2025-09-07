package com.skyeye.collector.controller;

import com.skyeye.collector.lifecycle.PluginLifecycleManager;
import com.skyeye.collector.plugin.CollectorPlugin;
import com.skyeye.collector.plugin.PluginStatistics;
import com.skyeye.collector.registry.PluginRegistry;
import com.skyeye.common.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 插件管理控制器
 * 
 * @author SkyEye Team
 */
@Slf4j
@RestController
@RequestMapping("/api/plugins")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class PluginManagementController {

    private final PluginRegistry pluginRegistry;
    private final PluginLifecycleManager lifecycleManager;

    /**
     * 获取所有插件信息
     */
    @GetMapping
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<Map<String, PluginRegistry.PluginInfo>> getAllPlugins() {
        Map<String, PluginRegistry.PluginInfo> pluginInfos = pluginRegistry.getPluginInfos();
        return ApiResponse.success(pluginInfos);
    }

    /**
     * 获取插件详细信息
     */
    @GetMapping("/{pluginType}")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<PluginDetailInfo> getPluginDetail(@PathVariable String pluginType) {
        Optional<CollectorPlugin> pluginOpt = pluginRegistry.getPlugin(pluginType);
        if (!pluginOpt.isPresent()) {
            return ApiResponse.error(404, "插件不存在");
        }

        CollectorPlugin plugin = pluginOpt.get();
        PluginDetailInfo detail = PluginDetailInfo.builder()
                .pluginType(plugin.getPluginType())
                .pluginName(plugin.getPluginName())
                .pluginVersion(plugin.getPluginVersion())
                .description(plugin.getPluginDescription())
                .supportedProtocols(plugin.getSupportedProtocols())
                .supportedMetricTypes(plugin.getSupportedMetricTypes())
                .configTemplate(plugin.getConfigTemplate())
                .supportsConcurrentCollection(plugin.supportsConcurrentCollection())
                .recommendedConcurrency(plugin.getRecommendedConcurrency())
                .priority(plugin.getPriority())
                .lifecycleState(lifecycleManager.getPluginState(pluginType))
                .statistics(plugin.getStatistics())
                .healthStatus(plugin.getHealthStatus())
                .build();

        return ApiResponse.success(detail);
    }

    /**
     * 获取插件状态
     */
    @GetMapping("/{pluginType}/status")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<PluginStatusInfo> getPluginStatus(@PathVariable String pluginType) {
        Optional<PluginRegistry.PluginStatus> registryStatus = pluginRegistry.getPluginStatus(pluginType);
        PluginLifecycleManager.PluginLifecycleState lifecycleState = lifecycleManager.getPluginState(pluginType);

        PluginStatusInfo statusInfo = PluginStatusInfo.builder()
                .pluginType(pluginType)
                .registryStatus(registryStatus.orElse(null))
                .lifecycleState(lifecycleState)
                .isReady(lifecycleManager.isPluginReady(pluginType))
                .build();

        return ApiResponse.success(statusInfo);
    }

    /**
     * 启用插件
     */
    @PostMapping("/{pluginType}/enable")
    @PreAuthorize("hasPermission('plugin', 'manage')")
    public ApiResponse<Void> enablePlugin(@PathVariable String pluginType) {
        try {
            boolean success = pluginRegistry.enablePlugin(pluginType);
            if (success) {
                lifecycleManager.startPlugin(pluginType);
                return ApiResponse.success(null);
            } else {
                return ApiResponse.error(500, "启用插件失败");
            }
        } catch (Exception e) {
            log.error("启用插件失败: {}", pluginType, e);
            return ApiResponse.error(500, "启用插件异常: " + e.getMessage());
        }
    }

    /**
     * 禁用插件
     */
    @PostMapping("/{pluginType}/disable")
    @PreAuthorize("hasPermission('plugin', 'manage')")
    public ApiResponse<Void> disablePlugin(@PathVariable String pluginType) {
        try {
            lifecycleManager.stopPlugin(pluginType);
            boolean success = pluginRegistry.disablePlugin(pluginType);
            if (success) {
                return ApiResponse.success(null);
            } else {
                return ApiResponse.error(500, "禁用插件失败");
            }
        } catch (Exception e) {
            log.error("禁用插件失败: {}", pluginType, e);
            return ApiResponse.error(500, "禁用插件异常: " + e.getMessage());
        }
    }

    /**
     * 重启插件
     */
    @PostMapping("/{pluginType}/restart")
    @PreAuthorize("hasPermission('plugin', 'manage')")
    public ApiResponse<Void> restartPlugin(@PathVariable String pluginType) {
        try {
            lifecycleManager.restartPlugin(pluginType);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("重启插件失败: {}", pluginType, e);
            return ApiResponse.error(500, "重启插件异常: " + e.getMessage());
        }
    }

    /**
     * 重载插件
     */
    @PostMapping("/{pluginType}/reload")
    @PreAuthorize("hasPermission('plugin', 'manage')")
    public ApiResponse<Void> reloadPlugin(@PathVariable String pluginType) {
        try {
            pluginRegistry.reloadPlugin(pluginType);
            return ApiResponse.success(null);
        } catch (Exception e) {
            log.error("重载插件失败: {}", pluginType, e);
            return ApiResponse.error(500, "重载插件异常: " + e.getMessage());
        }
    }

    /**
     * 获取插件统计信息
     */
    @GetMapping("/{pluginType}/statistics")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<PluginStatistics> getPluginStatistics(@PathVariable String pluginType) {
        Optional<CollectorPlugin> pluginOpt = pluginRegistry.getPlugin(pluginType);
        if (!pluginOpt.isPresent()) {
            return ApiResponse.error(404, "插件不存在");
        }

        PluginStatistics statistics = pluginOpt.get().getStatistics();
        return ApiResponse.success(statistics);
    }

    /**
     * 执行插件健康检查
     */
    @PostMapping("/{pluginType}/health-check")
    @PreAuthorize("hasPermission('plugin', 'manage')")
    public ApiResponse<PluginLifecycleManager.PluginHealthCheckResult> performHealthCheck(@PathVariable String pluginType) {
        PluginLifecycleManager.PluginHealthCheckResult result = lifecycleManager.performHealthCheck(pluginType);
        return ApiResponse.success(result);
    }

    /**
     * 执行所有插件健康检查
     */
    @PostMapping("/health-check")
    @PreAuthorize("hasPermission('plugin', 'manage')")
    public ApiResponse<List<PluginLifecycleManager.PluginHealthCheckResult>> performAllHealthChecks() {
        List<PluginLifecycleManager.PluginHealthCheckResult> results = lifecycleManager.performAllHealthChecks();
        return ApiResponse.success(results);
    }

    /**
     * 获取支持指定协议的插件
     */
    @GetMapping("/by-protocol/{protocol}")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<List<String>> getPluginsByProtocol(@PathVariable String protocol) {
        List<CollectorPlugin> plugins = pluginRegistry.getPluginsForProtocol(protocol);
        List<String> pluginTypes = plugins.stream()
                .map(CollectorPlugin::getPluginType)
                .collect(java.util.stream.Collectors.toList());
        return ApiResponse.success(pluginTypes);
    }

    /**
     * 获取插件配置模板
     */
    @GetMapping("/{pluginType}/config-template")
    @PreAuthorize("hasPermission('plugin', 'view')")
    public ApiResponse<Map<String, Object>> getConfigTemplate(@PathVariable String pluginType) {
        Optional<CollectorPlugin> pluginOpt = pluginRegistry.getPlugin(pluginType);
        if (!pluginOpt.isPresent()) {
            return ApiResponse.error(404, "插件不存在");
        }

        Map<String, Object> template = pluginOpt.get().getConfigTemplate();
        return ApiResponse.success(template);
    }

    /**
     * 插件详细信息DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PluginDetailInfo {
        private String pluginType;
        private String pluginName;
        private String pluginVersion;
        private String description;
        private List<String> supportedProtocols;
        private List<String> supportedMetricTypes;
        private Map<String, Object> configTemplate;
        private boolean supportsConcurrentCollection;
        private int recommendedConcurrency;
        private int priority;
        private PluginLifecycleManager.PluginLifecycleState lifecycleState;
        private PluginStatistics statistics;
        private com.skyeye.collector.plugin.PluginHealthStatus healthStatus;
    }

    /**
     * 插件状态信息DTO
     */
    @lombok.Data
    @lombok.Builder
    public static class PluginStatusInfo {
        private String pluginType;
        private PluginRegistry.PluginStatus registryStatus;
        private PluginLifecycleManager.PluginLifecycleState lifecycleState;
        private boolean isReady;
    }
}
