package com.skyeye.collector.plugin.impl;

import com.skyeye.collector.dto.MetricConfig;
import com.skyeye.collector.plugin.AvailableMetric;
import com.skyeye.device.entity.Device;
import lombok.extern.slf4j.Slf4j;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.security.SecurityLevel;
import org.snmp4j.smi.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * SNMP采集插件辅助类
 * 包含SNMP操作的具体实现方法
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
public class SnmpCollectorPluginHelper {

    private static final Pattern OID_PATTERN = Pattern.compile("^(\\d+\\.)*\\d+$");

    /**
     * 创建SNMP目标
     */
    public Target createTarget(Device device, Map<String, Object> credentials) {
        String version = (String) credentials.getOrDefault("version", "v2c");
        Integer port = (Integer) credentials.getOrDefault("port", 161);
        
        Address targetAddress = GenericAddress.parse("udp:" + device.getIpAddress() + "/" + port);
        
        if ("v3".equals(version)) {
            UserTarget target = new UserTarget();
            target.setAddress(targetAddress);
            target.setVersion(SnmpConstants.version3);
            target.setSecurityLevel(SecurityLevel.AUTH_PRIV);
            
            @SuppressWarnings("unchecked")
            Map<String, Object> v3Config = (Map<String, Object>) credentials.get("v3Config");
            if (v3Config != null) {
                String username = (String) v3Config.get("username");
                if (username != null) {
                    target.setSecurityName(new OctetString(username));
                }
            }
            
            target.setRetries(2);
            target.setTimeout(15000);
            
            return target;
        } else {
            CommunityTarget target = new CommunityTarget();
            target.setAddress(targetAddress);
            
            if ("v1".equals(version)) {
                target.setVersion(SnmpConstants.version1);
            } else {
                target.setVersion(SnmpConstants.version2c);
            }
            
            String community = (String) credentials.getOrDefault("community", "public");
            target.setCommunity(new OctetString(community));
            target.setRetries(2);
            target.setTimeout(15000);
            
            return target;
        }
    }

    /**
     * 创建PDU
     */
    public PDU createPDU(Map<String, Object> credentials) {
        String version = (String) credentials.getOrDefault("version", "v2c");
        
        if ("v1".equals(version)) {
            return new PDUv1();
        } else {
            return new PDU();
        }
    }

    /**
     * 收集指标数据
     */
    public Map<String, Object> collectMetrics(Device device, MetricConfig metricConfig, 
                                            Snmp snmp, Map<String, Object> credentials) throws IOException {
        
        String metricType = metricConfig.getMetricType();
        Map<String, Object> parameters = metricConfig.getParameters();
        
        switch (metricType) {
            case "system_info":
                return collectSystemInfo(snmp, device, credentials);
            case "interface_stats":
                return collectInterfaceStats(snmp, device, credentials, parameters);
            case "cpu_usage":
                return collectCpuUsage(snmp, device, credentials, parameters);
            case "memory_usage":
                return collectMemoryUsage(snmp, device, credentials, parameters);
            case "storage_usage":
                return collectStorageUsage(snmp, device, credentials, parameters);
            case "network_traffic":
                return collectNetworkTraffic(snmp, device, credentials, parameters);
            case "custom_oid":
                return collectCustomOid(snmp, device, credentials, parameters);
            default:
                throw new IllegalArgumentException("不支持的指标类型: " + metricType);
        }
    }

    /**
     * 收集系统信息
     */
    private Map<String, Object> collectSystemInfo(Snmp snmp, Device device, Map<String, Object> credentials) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        Target target = createTarget(device, credentials);
        
        // 系统描述
        String sysDescr = getSingleOidValue(snmp, target, "1.3.6.1.2.1.1.1.0", credentials);
        if (sysDescr != null) {
            metrics.put("sysDescr", sysDescr);
        }
        
        // 系统运行时间
        String sysUpTime = getSingleOidValue(snmp, target, "1.3.6.1.2.1.1.3.0", credentials);
        if (sysUpTime != null) {
            try {
                long upTime = Long.parseLong(sysUpTime) / 100; // 转换为秒
                metrics.put("sysUpTime", upTime);
                metrics.put("sysUpTimeFormatted", formatUpTime(upTime));
            } catch (NumberFormatException e) {
                metrics.put("sysUpTime", sysUpTime);
            }
        }
        
        // 系统名称
        String sysName = getSingleOidValue(snmp, target, "1.3.6.1.2.1.1.5.0", credentials);
        if (sysName != null) {
            metrics.put("sysName", sysName);
        }
        
        // 系统位置
        String sysLocation = getSingleOidValue(snmp, target, "1.3.6.1.2.1.1.6.0", credentials);
        if (sysLocation != null) {
            metrics.put("sysLocation", sysLocation);
        }
        
        // 系统联系人
        String sysContact = getSingleOidValue(snmp, target, "1.3.6.1.2.1.1.4.0", credentials);
        if (sysContact != null) {
            metrics.put("sysContact", sysContact);
        }
        
        return metrics;
    }

    /**
     * 收集接口统计信息
     */
    private Map<String, Object> collectInterfaceStats(Snmp snmp, Device device, 
                                                    Map<String, Object> credentials, 
                                                    Map<String, Object> parameters) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        Target target = createTarget(device, credentials);
        
        // 获取接口数量
        String ifNumber = getSingleOidValue(snmp, target, "1.3.6.1.2.1.2.1.0", credentials);
        if (ifNumber != null) {
            metrics.put("ifNumber", Integer.parseInt(ifNumber));
        }
        
        // 获取特定接口的统计信息
        String interfaceIndex = (String) parameters.get("interfaceIndex");
        if (interfaceIndex != null) {
            // 接口入站字节数
            String ifInOctets = getSingleOidValue(snmp, target, 
                    "1.3.6.1.2.1.2.2.1.10." + interfaceIndex, credentials);
            if (ifInOctets != null) {
                metrics.put("ifInOctets", Long.parseLong(ifInOctets));
            }
            
            // 接口出站字节数
            String ifOutOctets = getSingleOidValue(snmp, target, 
                    "1.3.6.1.2.1.2.2.1.16." + interfaceIndex, credentials);
            if (ifOutOctets != null) {
                metrics.put("ifOutOctets", Long.parseLong(ifOutOctets));
            }
            
            // 接口入站包数
            String ifInUcastPkts = getSingleOidValue(snmp, target, 
                    "1.3.6.1.2.1.2.2.1.11." + interfaceIndex, credentials);
            if (ifInUcastPkts != null) {
                metrics.put("ifInUcastPkts", Long.parseLong(ifInUcastPkts));
            }
            
            // 接口出站包数
            String ifOutUcastPkts = getSingleOidValue(snmp, target, 
                    "1.3.6.1.2.1.2.2.1.17." + interfaceIndex, credentials);
            if (ifOutUcastPkts != null) {
                metrics.put("ifOutUcastPkts", Long.parseLong(ifOutUcastPkts));
            }
        }
        
        return metrics;
    }

    /**
     * 收集CPU使用率
     */
    private Map<String, Object> collectCpuUsage(Snmp snmp, Device device, 
                                              Map<String, Object> credentials, 
                                              Map<String, Object> parameters) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        Target target = createTarget(device, credentials);
        
        // 尝试多种CPU使用率OID
        String[] cpuOids = {
                "1.3.6.1.2.1.25.3.3.1.2",     // hrProcessorLoad
                "1.3.6.1.4.1.2021.11.9.0",    // UCD-SNMP CPU idle
                "1.3.6.1.4.1.9.2.1.56.0"      // Cisco CPU 5min
        };
        
        for (String baseOid : cpuOids) {
            try {
                String cpuValue = getSingleOidValue(snmp, target, baseOid, credentials);
                if (cpuValue != null) {
                    double usage = Double.parseDouble(cpuValue);
                    metrics.put("cpuUsage", usage);
                    break;
                }
            } catch (Exception e) {
                log.debug("CPU OID {} 查询失败", baseOid);
            }
        }
        
        return metrics;
    }

    /**
     * 收集内存使用率
     */
    private Map<String, Object> collectMemoryUsage(Snmp snmp, Device device, 
                                                  Map<String, Object> credentials, 
                                                  Map<String, Object> parameters) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        Target target = createTarget(device, credentials);
        
        // 尝试不同的内存OID
        try {
            // 物理内存总量
            String memTotal = getSingleOidValue(snmp, target, "1.3.6.1.4.1.2021.4.5.0", credentials);
            // 可用内存
            String memAvailable = getSingleOidValue(snmp, target, "1.3.6.1.4.1.2021.4.6.0", credentials);
            
            if (memTotal != null && memAvailable != null) {
                long total = Long.parseLong(memTotal);
                long available = Long.parseLong(memAvailable);
                long used = total - available;
                double usagePercent = (double) used / total * 100;
                
                metrics.put("memTotal", total);
                metrics.put("memUsed", used);
                metrics.put("memAvailable", available);
                metrics.put("memUsagePercent", usagePercent);
            }
        } catch (Exception e) {
            log.debug("内存使用率查询失败", e);
        }
        
        return metrics;
    }

    /**
     * 收集存储使用率
     */
    private Map<String, Object> collectStorageUsage(Snmp snmp, Device device, 
                                                   Map<String, Object> credentials, 
                                                   Map<String, Object> parameters) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        Target target = createTarget(device, credentials);
        
        String storageIndex = (String) parameters.get("storageIndex");
        if (storageIndex != null) {
            try {
                // 存储总大小
                String storageSize = getSingleOidValue(snmp, target, 
                        "1.3.6.1.2.1.25.2.3.1.5." + storageIndex, credentials);
                // 已使用大小
                String storageUsed = getSingleOidValue(snmp, target, 
                        "1.3.6.1.2.1.25.2.3.1.6." + storageIndex, credentials);
                // 分配单元大小
                String allocationUnits = getSingleOidValue(snmp, target, 
                        "1.3.6.1.2.1.25.2.3.1.4." + storageIndex, credentials);
                
                if (storageSize != null && storageUsed != null && allocationUnits != null) {
                    long size = Long.parseLong(storageSize);
                    long used = Long.parseLong(storageUsed);
                    long units = Long.parseLong(allocationUnits);
                    
                    long totalBytes = size * units;
                    long usedBytes = used * units;
                    double usagePercent = (double) used / size * 100;
                    
                    metrics.put("storageSize", totalBytes);
                    metrics.put("storageUsed", usedBytes);
                    metrics.put("storageUsagePercent", usagePercent);
                }
            } catch (Exception e) {
                log.debug("存储使用率查询失败", e);
            }
        }
        
        return metrics;
    }

    /**
     * 收集网络流量
     */
    private Map<String, Object> collectNetworkTraffic(Snmp snmp, Device device, 
                                                     Map<String, Object> credentials, 
                                                     Map<String, Object> parameters) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        Target target = createTarget(device, credentials);
        
        String interfaceIndex = (String) parameters.get("interfaceIndex");
        if (interfaceIndex != null) {
            try {
                // 接口速度
                String ifSpeed = getSingleOidValue(snmp, target, 
                        "1.3.6.1.2.1.2.2.1.5." + interfaceIndex, credentials);
                
                // 接口状态
                String ifOperStatus = getSingleOidValue(snmp, target, 
                        "1.3.6.1.2.1.2.2.1.8." + interfaceIndex, credentials);
                
                // 接口描述
                String ifDescr = getSingleOidValue(snmp, target, 
                        "1.3.6.1.2.1.2.2.1.2." + interfaceIndex, credentials);
                
                if (ifSpeed != null) {
                    metrics.put("ifSpeed", Long.parseLong(ifSpeed));
                }
                if (ifOperStatus != null) {
                    metrics.put("ifOperStatus", Integer.parseInt(ifOperStatus));
                }
                if (ifDescr != null) {
                    metrics.put("ifDescr", ifDescr);
                }
                
                // 获取流量统计（需要与接口统计结合计算速率）
                collectInterfaceStats(snmp, device, credentials, parameters)
                        .forEach(metrics::put);
                
            } catch (Exception e) {
                log.debug("网络流量查询失败", e);
            }
        }
        
        return metrics;
    }

    /**
     * 收集自定义OID数据
     */
    private Map<String, Object> collectCustomOid(Snmp snmp, Device device, 
                                                Map<String, Object> credentials, 
                                                Map<String, Object> parameters) throws IOException {
        Map<String, Object> metrics = new HashMap<>();
        Target target = createTarget(device, credentials);
        
        String oid = (String) parameters.get("oid");
        String dataType = (String) parameters.getOrDefault("dataType", "STRING");
        
        if (oid != null) {
            String value = getSingleOidValue(snmp, target, oid, credentials);
            if (value != null) {
                Object parsedValue = parseValueByType(value, dataType);
                metrics.put("value", parsedValue);
                metrics.put("rawValue", value);
                metrics.put("oid", oid);
            }
        }
        
        return metrics;
    }

    /**
     * 获取单个OID的值
     */
    private String getSingleOidValue(Snmp snmp, Target target, String oid, Map<String, Object> credentials) throws IOException {
        PDU pdu = createPDU(credentials);
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(PDU.GET);
        
        ResponseEvent response = snmp.send(pdu, target);
        
        if (response.getResponse() != null && response.getResponse().getErrorStatus() == PDU.noError) {
            VariableBinding vb = response.getResponse().get(0);
            if (vb.getVariable() != Null.instance) {
                return vb.getVariable().toString();
            }
        }
        
        return null;
    }

    /**
     * 根据数据类型解析值
     */
    private Object parseValueByType(String value, String dataType) {
        try {
            switch (dataType.toUpperCase()) {
                case "INTEGER":
                    return Integer.parseInt(value);
                case "LONG":
                    return Long.parseLong(value);
                case "DOUBLE":
                    return Double.parseDouble(value);
                case "BOOLEAN":
                    return "1".equals(value) || "true".equalsIgnoreCase(value);
                case "STRING":
                default:
                    return value;
            }
        } catch (NumberFormatException e) {
            log.warn("数据类型转换失败: {} -> {}", value, dataType);
            return value;
        }
    }

    /**
     * 格式化运行时间
     */
    private String formatUpTime(long seconds) {
        long days = seconds / 86400;
        long hours = (seconds % 86400) / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        return String.format("%d天 %d小时 %d分钟 %d秒", days, hours, minutes, secs);
    }

    /**
     * 测试OID是否可访问
     */
    public boolean testOid(Snmp snmp, Target target, String oid, Map<String, Object> credentials) {
        try {
            String value = getSingleOidValue(snmp, target, oid, credentials);
            return value != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 验证OID格式
     */
    public boolean isValidOid(String oid) {
        return oid != null && OID_PATTERN.matcher(oid.trim()).matches();
    }

    /**
     * 计算数据质量评分
     */
    public int calculateQualityScore(Map<String, Object> metrics) {
        if (metrics == null || metrics.isEmpty()) {
            return 0;
        }
        
        int score = 100;
        int nullValues = 0;
        
        for (Object value : metrics.values()) {
            if (value == null) {
                nullValues++;
            }
        }
        
        // 根据空值比例扣分
        if (metrics.size() > 0) {
            double nullRatio = (double) nullValues / metrics.size();
            score -= (int) (nullRatio * 50); // 最多扣50分
        }
        
        return Math.max(score, 0);
    }

    /**
     * 确定错误代码
     */
    public String determineErrorCode(Exception e) {
        if (e instanceof IOException) {
            return "NETWORK_ERROR";
        } else if (e instanceof NumberFormatException) {
            return "DATA_FORMAT_ERROR";
        } else if (e.getMessage() != null && e.getMessage().contains("timeout")) {
            return "TIMEOUT_ERROR";
        } else if (e.getMessage() != null && e.getMessage().contains("authentication")) {
            return "AUTH_ERROR";
        } else {
            return "UNKNOWN_ERROR";
        }
    }

    /**
     * 构建会话键
     */
    public String buildSessionKey(Device device, Map<String, Object> credentials) {
        String version = (String) credentials.getOrDefault("version", "v2c");
        String community = (String) credentials.getOrDefault("community", "public");
        Integer port = (Integer) credentials.getOrDefault("port", 161);
        
        return String.format("snmp_%s_%s_%s_%s", device.getIpAddress(), port, version, community);
    }

    /**
     * 获取认证协议OID
     */
    public OID getAuthProtocolOID(String protocol) {
        switch (protocol.toUpperCase()) {
            case "SHA":
                return AuthSHA.ID;
            case "MD5":
            default:
                return AuthMD5.ID;
        }
    }

    /**
     * 获取隐私协议OID
     */
    public OID getPrivProtocolOID(String protocol) {
        switch (protocol.toUpperCase()) {
            case "AES":
                return PrivAES128.ID;
            case "DES":
            default:
                return PrivDES.ID;
        }
    }

    /**
     * 创建系统信息指标
     */
    public AvailableMetric createSystemInfoMetric() {
        return AvailableMetric.builder()
                .name("system_info")
                .displayName("系统信息")
                .description("获取设备系统基本信息，包括描述、名称、位置等")
                .type("system")
                .dataType(AvailableMetric.DataType.OBJECT)
                .category("system")
                .core(true)
                .complexity(1)
                .recommendedInterval(300)
                .build();
    }

    /**
     * 创建接口统计指标
     */
    public AvailableMetric createInterfaceStatsMetric() {
        return AvailableMetric.builder()
                .name("interface_stats")
                .displayName("接口统计")
                .description("获取网络接口流量统计信息")
                .type("network")
                .dataType(AvailableMetric.DataType.OBJECT)
                .unit("bytes/packets")
                .category("network")
                .core(true)
                .complexity(2)
                .recommendedInterval(60)
                .build();
    }

    /**
     * 创建CPU使用率指标
     */
    public AvailableMetric createCpuUsageMetric() {
        return AvailableMetric.builder()
                .name("cpu_usage")
                .displayName("CPU使用率")
                .description("获取设备CPU使用百分比")
                .type("performance")
                .dataType(AvailableMetric.DataType.FLOAT)
                .unit("%")
                .category("performance")
                .core(true)
                .complexity(2)
                .recommendedInterval(60)
                .build();
    }

    /**
     * 创建内存使用率指标
     */
    public AvailableMetric createMemoryUsageMetric() {
        return AvailableMetric.builder()
                .name("memory_usage")
                .displayName("内存使用率")
                .description("获取设备内存使用情况")
                .type("performance")
                .dataType(AvailableMetric.DataType.OBJECT)
                .unit("bytes/%")
                .category("performance")
                .core(true)
                .complexity(2)
                .recommendedInterval(60)
                .build();
    }
}
