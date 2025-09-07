package com.skyeye.collector.plugin.impl;

import com.skyeye.collector.dto.CollectionContext;
import com.skyeye.collector.dto.CollectionResult;
import com.skyeye.collector.dto.CollectorConfig;
import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.plugin.*;
import com.skyeye.device.entity.Device;
import com.skyeye.device.entity.DeviceType;
import com.skyeye.device.service.DeviceCredentialService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.*;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * SNMP协议数据采集插件
 * 支持SNMPv1、SNMPv2c、SNMPv3协议
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SnmpCollectorPlugin extends AbstractCollectorPlugin {

    private final DeviceCredentialService credentialService;
    private final SnmpCollectorPluginHelper helper;

    /**
     * SNMP会话缓存
     */
    private final Map<String, Snmp> snmpSessions = new ConcurrentHashMap<>();

    /**
     * 常用系统OID
     */
    private static final Map<String, String> SYSTEM_OIDS = new HashMap<>();

    static {
        // 系统基本信息OID
        SYSTEM_OIDS.put("sysDescr", "1.3.6.1.2.1.1.1.0");
        SYSTEM_OIDS.put("sysObjectID", "1.3.6.1.2.1.1.2.0");
        SYSTEM_OIDS.put("sysUpTime", "1.3.6.1.2.1.1.3.0");
        SYSTEM_OIDS.put("sysContact", "1.3.6.1.2.1.1.4.0");
        SYSTEM_OIDS.put("sysName", "1.3.6.1.2.1.1.5.0");
        SYSTEM_OIDS.put("sysLocation", "1.3.6.1.2.1.1.6.0");
        SYSTEM_OIDS.put("sysServices", "1.3.6.1.2.1.1.7.0");

        // 接口相关OID
        SYSTEM_OIDS.put("ifNumber", "1.3.6.1.2.1.2.1.0");
        SYSTEM_OIDS.put("ifInOctets", "1.3.6.1.2.1.2.2.1.10");
        SYSTEM_OIDS.put("ifOutOctets", "1.3.6.1.2.1.2.2.1.16");
        SYSTEM_OIDS.put("ifInUcastPkts", "1.3.6.1.2.1.2.2.1.11");
        SYSTEM_OIDS.put("ifOutUcastPkts", "1.3.6.1.2.1.2.2.1.17");

        // CPU和内存相关OID (需要根据具体设备调整)
        SYSTEM_OIDS.put("hrProcessorLoad", "1.3.6.1.2.1.25.3.3.1.2");
        SYSTEM_OIDS.put("hrMemoryUsed", "1.3.6.1.2.1.25.2.3.1.6");
        SYSTEM_OIDS.put("hrStorageUsed", "1.3.6.1.2.1.25.2.3.1.6");
    }

    @Override
    public String getPluginType() {
        return "SNMP";
    }

    @Override
    public String getPluginName() {
        return "SNMP数据采集插件";
    }

    @Override
    public String getPluginVersion() {
        return "1.0.0";
    }

    @Override
    public String getPluginDescription() {
        return "支持SNMPv1/v2c/v3协议的设备数据采集插件，可采集系统信息、网络接口、性能指标等数据";
    }

    @Override
    public boolean supports(DeviceType deviceType) {
        if (deviceType.getProtocols() == null) {
            return false;
        }
        return deviceType.getProtocols().contains("SNMP");
    }

    @Override
    public boolean supportsProtocol(String protocol) {
        return "SNMP".equalsIgnoreCase(protocol);
    }

    @Override
    public List<String> getSupportedProtocols() {
        return Arrays.asList("SNMP");
    }

    @Override
    public List<String> getSupportedMetricTypes() {
        return Arrays.asList(
                "system_info",      // 系统信息
                "interface_stats",  // 接口统计
                "cpu_usage",        // CPU使用率
                "memory_usage",     // 内存使用率
                "storage_usage",    // 存储使用率
                "network_traffic",  // 网络流量
                "custom_oid"        // 自定义OID查询
        );
    }

    @Override
    protected void doInitialize(CollectorConfig config) throws PluginException {
        try {
            log.info("初始化SNMP采集插件");
            
            // 初始化SNMP4J安全模块
            MessageDispatcher messageDispatcher = new MessageDispatcherImpl();
            messageDispatcher.addMessageProcessingModel(new MPv1());
            messageDispatcher.addMessageProcessingModel(new MPv3());
            
            // 初始化安全协议
            SecurityProtocols.getInstance().addDefaultProtocols();
            
            log.info("SNMP采集插件初始化完成");
        } catch (Exception e) {
            throw new PluginException("SNMP_INIT_FAILED", "SNMP插件初始化失败", e);
        }
    }

    @Override
    protected void doDestroy() {
        log.info("销毁SNMP采集插件");
        
        // 关闭所有SNMP会话
        snmpSessions.values().forEach(snmp -> {
            try {
                snmp.close();
            } catch (IOException e) {
                log.warn("关闭SNMP会话失败", e);
            }
        });
        snmpSessions.clear();
        
        log.info("SNMP采集插件销毁完成");
    }

    @Override
    protected CollectionResult doCollect(Device device, MetricConfig metricConfig, CollectionContext context) {
        LocalDateTime startTime = LocalDateTime.now();
        
        try {
            // 1. 获取设备SNMP凭据
            Map<String, Object> credentials = getDeviceCredentials(device, context);
            
            // 2. 获取或创建SNMP会话
            Snmp snmp = getOrCreateSnmpSession(device, credentials);
            
            // 3. 根据指标类型执行相应的采集
            Map<String, Object> metrics = helper.collectMetrics(device, metricConfig, snmp, credentials);
            
            return CollectionResult.builder()
                    .success(true)
                    .metrics(metrics)
                    .timestamp(System.currentTimeMillis())
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .qualityScore(helper.calculateQualityScore(metrics))
                    .pluginType(getPluginType())
                    .deviceId(device.getId())
                    .metricName(metricConfig.getMetricName())
                    .sessionId(context.getSessionId())
                    .build();
                    
        } catch (Exception e) {
            log.error("SNMP采集失败: device={}, metric={}", 
                    device.getId(), metricConfig.getMetricName(), e);
            
            return CollectionResult.builder()
                    .success(false)
                    .errorMessage("SNMP采集失败: " + e.getMessage())
                    .errorCode(helper.determineErrorCode(e))
                    .timestamp(System.currentTimeMillis())
                    .startTime(startTime)
                    .endTime(LocalDateTime.now())
                    .pluginType(getPluginType())
                    .deviceId(device.getId())
                    .metricName(metricConfig.getMetricName())
                    .sessionId(context.getSessionId())
                    .build();
        }
    }

    @Override
    protected ConnectionTestResult doTestConnection(Device device, CollectionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // 获取设备凭据
            Map<String, Object> credentials = getDeviceCredentials(device, context);
            
            // 创建测试会话
            Snmp snmp = createSnmpSession(device, credentials);
            Target target = helper.createTarget(device, credentials);
            
            // 发送系统描述查询测试连接
            PDU pdu = helper.createPDU(credentials);
            pdu.add(new VariableBinding(new OID(SYSTEM_OIDS.get("sysDescr"))));
            
            ResponseEvent response = snmp.send(pdu, target);
            
            // 关闭测试会话
            snmp.close();
            
            long responseTime = System.currentTimeMillis() - startTime;
            
            if (response.getResponse() != null && response.getResponse().getErrorStatus() == PDU.noError) {
                return ConnectionTestResult.success("SNMP连接测试成功", responseTime);
            } else {
                String errorMsg = response.getResponse() != null ? 
                        "SNMP错误: " + response.getResponse().getErrorStatusText() : "SNMP响应超时";
                return ConnectionTestResult.failure("SNMP_ERROR", errorMsg);
            }
            
        } catch (Exception e) {
            long responseTime = System.currentTimeMillis() - startTime;
            log.error("SNMP连接测试失败: device={}", device.getId(), e);
            return ConnectionTestResult.failure("CONNECTION_FAILED", 
                    "SNMP连接测试失败: " + e.getMessage());
        }
    }

    @Override
    protected List<AvailableMetric> doDiscoverMetrics(Device device, CollectionContext context) {
        List<AvailableMetric> metrics = new ArrayList<>();
        
        try {
            Map<String, Object> credentials = getDeviceCredentials(device, context);
            Snmp snmp = getOrCreateSnmpSession(device, credentials);
            Target target = helper.createTarget(device, credentials);
            
            // 发现系统信息指标
            if (helper.testOid(snmp, target, SYSTEM_OIDS.get("sysDescr"), credentials)) {
                metrics.add(helper.createSystemInfoMetric());
            }
            
            // 发现接口统计指标
            if (helper.testOid(snmp, target, SYSTEM_OIDS.get("ifNumber"), credentials)) {
                metrics.add(helper.createInterfaceStatsMetric());
            }
            
            // 发现CPU指标
            if (helper.testOid(snmp, target, SYSTEM_OIDS.get("hrProcessorLoad"), credentials)) {
                metrics.add(helper.createCpuUsageMetric());
            }
            
            // 发现内存指标
            if (helper.testOid(snmp, target, SYSTEM_OIDS.get("hrMemoryUsed"), credentials)) {
                metrics.add(helper.createMemoryUsageMetric());
            }
            
        } catch (Exception e) {
            log.error("SNMP指标发现失败: device={}", device.getId(), e);
        }
        
        return metrics;
    }

    @Override
    protected ConfigValidationResult doValidateConfig(MetricConfig metricConfig) {
        List<ConfigValidationResult.ValidationError> errors = new ArrayList<>();
        List<ConfigValidationResult.ValidationWarning> warnings = new ArrayList<>();
        
        Map<String, Object> parameters = metricConfig.getParameters();
        if (parameters == null) {
            errors.add(ConfigValidationResult.ValidationError.builder()
                    .field("parameters")
                    .errorCode("MISSING_PARAMETERS")
                    .message("SNMP采集参数不能为空")
                    .severity(ConfigValidationResult.ValidationError.Severity.HIGH)
                    .build());
        } else {
            // 验证OID
            String oid = (String) parameters.get("oid");
            if (oid == null || oid.trim().isEmpty()) {
                errors.add(ConfigValidationResult.ValidationError.builder()
                        .field("oid")
                        .errorCode("MISSING_OID")
                        .message("OID参数不能为空")
                        .severity(ConfigValidationResult.ValidationError.Severity.HIGH)
                        .build());
            } else if (!helper.isValidOid(oid)) {
                errors.add(ConfigValidationResult.ValidationError.builder()
                        .field("oid")
                        .errorCode("INVALID_OID")
                        .message("OID格式无效")
                        .currentValue(oid)
                        .severity(ConfigValidationResult.ValidationError.Severity.HIGH)
                        .build());
            }
            
            // 验证超时时间
            if (metricConfig.getTimeout() <= 0) {
                warnings.add(ConfigValidationResult.ValidationWarning.builder()
                        .field("timeout")
                        .warningCode("INVALID_TIMEOUT")
                        .message("超时时间应大于0")
                        .currentValue(metricConfig.getTimeout())
                        .suggestedValue(30)
                        .build());
            }
        }
        
        boolean valid = errors.isEmpty();
        String message = valid ? "SNMP配置验证通过" : "SNMP配置验证失败";
        
        return ConfigValidationResult.builder()
                .valid(valid)
                .message(message)
                .errors(errors)
                .warnings(warnings)
                .build();
    }

    @Override
    public Map<String, Object> getConfigTemplate() {
        Map<String, Object> template = new HashMap<>();
        template.put("pluginType", "SNMP");
        template.put("timeout", 30);
        template.put("retryTimes", 3);
        template.put("retryInterval", 1000);
        
        // SNMP特定配置
        Map<String, Object> snmpConfig = new HashMap<>();
        snmpConfig.put("version", "v2c");  // v1, v2c, v3
        snmpConfig.put("community", "public");
        snmpConfig.put("port", 161);
        snmpConfig.put("maxSizeRequestPDU", 65535);
        
        // SNMPv3配置
        Map<String, Object> v3Config = new HashMap<>();
        v3Config.put("username", "");
        v3Config.put("authProtocol", "MD5");  // MD5, SHA
        v3Config.put("authPassword", "");
        v3Config.put("privProtocol", "DES");  // DES, AES
        v3Config.put("privPassword", "");
        snmpConfig.put("v3Config", v3Config);
        
        template.put("snmpConfig", snmpConfig);
        
        // 指标配置示例
        Map<String, Object> metricExample = new HashMap<>();
        metricExample.put("oid", "1.3.6.1.2.1.1.1.0");
        metricExample.put("dataType", "STRING");
        metricExample.put("instance", "0");
        template.put("metricExample", metricExample);
        
        return template;
    }

    /**
     * 获取设备SNMP凭据
     */
    private Map<String, Object> getDeviceCredentials(Device device, CollectionContext context) {
        try {
            Map<String, Object> credentials = context.getCredentials();
            if (credentials != null && !credentials.isEmpty()) {
                return credentials;
            }
            
            // 从凭据服务获取
            return credentialService.getDefaultCredential(device.getId(), "SNMP");
        } catch (Exception e) {
            log.warn("获取SNMP凭据失败，使用默认配置: device={}", device.getId());
            
            // 返回默认凭据
            Map<String, Object> defaultCredentials = new HashMap<>();
            defaultCredentials.put("version", "v2c");
            defaultCredentials.put("community", "public");
            defaultCredentials.put("port", 161);
            return defaultCredentials;
        }
    }

    /**
     * 获取或创建SNMP会话
     */
    private Snmp getOrCreateSnmpSession(Device device, Map<String, Object> credentials) throws IOException {
        String sessionKey = helper.buildSessionKey(device, credentials);
        
        return snmpSessions.computeIfAbsent(sessionKey, key -> {
            try {
                return createSnmpSession(device, credentials);
            } catch (IOException e) {
                log.error("创建SNMP会话失败: device={}", device.getId(), e);
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 创建SNMP会话
     */
    private Snmp createSnmpSession(Device device, Map<String, Object> credentials) throws IOException {
        DefaultUdpTransportMapping transport = new DefaultUdpTransportMapping();
        Snmp snmp = new Snmp(transport);
        
        String version = (String) credentials.getOrDefault("version", "v2c");
        
        if ("v3".equals(version)) {
            // 配置SNMPv3用户
            configureSnmpV3User(snmp, credentials);
        }
        
        transport.listen();
        
        log.debug("创建SNMP会话成功: device={}, version={}", device.getId(), version);
        return snmp;
    }

    /**
     * 配置SNMPv3用户
     */
    private void configureSnmpV3User(Snmp snmp, Map<String, Object> credentials) {
        @SuppressWarnings("unchecked")
        Map<String, Object> v3Config = (Map<String, Object>) credentials.get("v3Config");
        if (v3Config == null) return;
        
        String username = (String) v3Config.get("username");
        String authProtocol = (String) v3Config.getOrDefault("authProtocol", "MD5");
        String authPassword = (String) v3Config.get("authPassword");
        String privProtocol = (String) v3Config.getOrDefault("privProtocol", "DES");
        String privPassword = (String) v3Config.get("privPassword");
        
        USM usm = new USM(SecurityProtocols.getInstance(), new OctetString(MPv3.createLocalEngineID()), 0);
        SecurityModels.getInstance().addSecurityModel(usm);
        
        // 添加用户
        OID authOID = helper.getAuthProtocolOID(authProtocol);
        OID privOID = helper.getPrivProtocolOID(privProtocol);
        
        UsmUser user = new UsmUser(
                new OctetString(username),
                authOID,
                authPassword != null ? new OctetString(authPassword) : null,
                privOID,
                privPassword != null ? new OctetString(privPassword) : null
        );
        
        snmp.getUSM().addUser(user);
    }
}
