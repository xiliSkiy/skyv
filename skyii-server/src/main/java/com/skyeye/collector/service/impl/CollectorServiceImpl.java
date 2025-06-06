package com.skyeye.collector.service.impl;

import com.skyeye.collector.dto.CollectorDTO;
import com.skyeye.collector.dto.CollectorMetricsDTO;
import com.skyeye.collector.dto.CollectorRegisterDTO;
import com.skyeye.collector.dto.HeartbeatDTO;
import com.skyeye.collector.dto.CollectorRegisterResponseDTO;
import com.skyeye.collector.dto.HeartbeatResponseDTO;
import com.skyeye.collector.entity.Collector;
import com.skyeye.collector.repository.DeviceCollectorRepository;
import com.skyeye.collector.service.CollectorService;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.collector.repository.CollectorRepository;
import com.skyeye.collector.util.JwtUtil;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 采集器服务实现类
 */
@Slf4j
@Service
public class CollectorServiceImpl implements CollectorService {

    @Autowired
    private DeviceCollectorRepository deviceCollectorRepository;
    @Autowired
    private CollectorRepository collectorRepository;

    @Value("${skyeye.collector.heartbeat.timeout:60}")
    private int heartbeatTimeout;
    @Value("${jwt.secret:defaultSecretKey}")
    private String jwtSecret;

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    @Transactional
    public CollectorDTO createCollector(CollectorDTO collectorDTO) {
        // 检查采集器名称是否已存在
        if (deviceCollectorRepository.findByCollectorName(collectorDTO.getCollectorName()).isPresent()) {
            throw new BusinessException("采集器名称已存在");
        }

        Collector collector = new Collector();
        BeanUtils.copyProperties(collectorDTO, collector);
        
        // 设置初始状态
        collector.setStatus(0); // 创建后默认为离线状态
        collector.setStatusInfo("等待连接");
        
        // 生成API密钥
        collector.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        
        Collector savedCollector = deviceCollectorRepository.save(collector);
        CollectorDTO resultDTO = new CollectorDTO();
        BeanUtils.copyProperties(savedCollector, resultDTO);
        
        return resultDTO;
    }

    @Override
    @Transactional
    public CollectorDTO updateCollector(Long id, CollectorDTO collectorDTO) {
        Collector collector = deviceCollectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        // 检查名称是否冲突
        if (!collector.getCollectorName().equals(collectorDTO.getCollectorName()) &&
                deviceCollectorRepository.findByCollectorName(collectorDTO.getCollectorName()).isPresent()) {
            throw new BusinessException("采集器名称已存在");
        }
        
        BeanUtils.copyProperties(collectorDTO, collector);
        collector.setId(id); // 确保ID不变
        
        Collector updatedCollector = deviceCollectorRepository.save(collector);
        CollectorDTO resultDTO = new CollectorDTO();
        BeanUtils.copyProperties(updatedCollector, resultDTO);
        
        return resultDTO;
    }

    @Override
    @Transactional
    public void deleteCollector(Long id) {
        Collector collector = deviceCollectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        // 执行逻辑删除而不是物理删除
        collector.setDeletedAt(LocalDateTime.now());
        deviceCollectorRepository.save(collector);
    }

    @Override
    public CollectorDTO getCollectorById(Long id) {
        Collector collector = deviceCollectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        CollectorDTO resultDTO = new CollectorDTO();
        BeanUtils.copyProperties(collector, resultDTO);
        
        return resultDTO;
    }      
          
    @Override
    public Page<CollectorDTO> findCollectors(Map<String, Object> params, Pageable pageable) {
        log.info("查询采集器列表, 参数: {}, 分页: {}", params, pageable);
        // 直接获取所有数据，先不过滤，看能否找到数据
        List<Collector> allCollectors = deviceCollectorRepository.findAll();
        log.info("数据库中总记录数: {}", allCollectors.size());
        for (Collector collector : allCollectors) {
            log.info("采集器记录: id={}, name={}, deletedAt={}, status={}",
            collector.getId(), collector.getCollectorName(), collector.getDeletedAt(), collector.getStatus());
        }
        // 使用Specification查询
        Page<Collector> collectorsPage = deviceCollectorRepository.findAll((root, query, cb) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            // 暂时注释掉删除条件，检查是否有数据
            // predicates.add(cb.isNull(root.get("deletedAt")));
            // 根据参数添加条件
            if (params != null) {
                if (params.containsKey("collectorName") && params.get("collectorName") != null
                        && !params.get("collectorName").toString().isEmpty()) {
                    predicates.add(cb.like(root.get("collectorName"),"%" + params.get("collectorName") + "%"));
                }
                if (params.containsKey("collectorType") && params.get("collectorType") != null
                        && !params.get("collectorType").toString().isEmpty()) {
                    predicates.add(cb.equal(root.get("collectorType"),
                            params.get("collectorType")));
                }
                if (params.containsKey("status") && params.get("status") != null) {
                    predicates.add(cb.equal(root.get("status"), params.get("status")));
                }
                if (params.containsKey("networkZone") && params.get("networkZone") != null
                        && !params.get("networkZone").toString().isEmpty()) {
                    predicates.add(cb.equal(root.get("networkZone"),
                            params.get("networkZone")));
                }
            }
            return predicates.isEmpty() ? cb.conjunction() :
                    cb.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
            }, pageable);
        log.info("查询结果总数: {}", collectorsPage.getTotalElements());
        log.info("当前页记录数: {}", collectorsPage.getContent().size());
        List<CollectorDTO> collectorDTOs = collectorsPage.getContent().stream().map(collector -> {
            CollectorDTO dto = new CollectorDTO();
            BeanUtils.copyProperties(collector, dto);
            return dto;
        }).collect(Collectors.toList());
        return new PageImpl<>(collectorDTOs, pageable, collectorsPage.getTotalElements());
    }
                
    
    @Override
    public CollectorMetricsDTO getCollectorMetrics(Long id) {
        // 此处应调用采集器API获取实时指标，这里为模拟数据
        Collector collector = deviceCollectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        CollectorMetricsDTO metrics = new CollectorMetricsDTO();
        metrics.setCollectorId(id);
        
        Random random = new Random();
        metrics.setCpuUsage(random.nextInt(80) + 20 + "%");  // 20-99%
        metrics.setMemoryUsage(random.nextInt(70) + 30 + "%"); // 30-99%
        metrics.setDiskUsage(random.nextInt(50) + 10 + "%");   // 10-59%
        metrics.setUptime(random.nextInt(20) + 1 + "天 " + random.nextInt(24) + "小时");
        metrics.setProcesses(random.nextInt(20) + 10);
        metrics.setThreads(random.nextInt(100) + 50);
        metrics.setActiveTasks(random.nextInt(10));
        metrics.setVersion(collector.getVersion());
        metrics.setNetworkIO(random.nextInt(500) + " KB/s");
        metrics.setLogLevel("INFO");
        metrics.setCacheUsage(random.nextInt(50) + "%");
        
        return metrics;
    }

    @Override
    public String generateRegistrationToken(Long id, Integer expireHours) {
        Collector collector = deviceCollectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + 1000 * 60 * 60 * expireHours); // expireHours小时后过期
        
        return Jwts.builder()
                .setSubject(collector.getId().toString())
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .claim("collectorName", collector.getCollectorName())
                .claim("collectorType", collector.getCollectorType())
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    @Override
    @Transactional
    public CollectorDTO activateCollector(String token, Map<String, Object> collectorInfo) {
        // 验证令牌
        try {
            String collectorId = Jwts.parser()
                    .setSigningKey(jwtSecret)
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
            
            Long id = Long.parseLong(collectorId);
            Collector collector = deviceCollectorRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("采集器不存在"));
            
            // 更新采集器信息
            if (collectorInfo.containsKey("host")) {
                collector.setHost(collectorInfo.get("host").toString());
            }
            if (collectorInfo.containsKey("version")) {
                collector.setVersion(collectorInfo.get("version").toString());
            }
            
            // 更新状态
            collector.setStatus(1); // 在线状态
            collector.setStatusInfo("正常运行");
            collector.setLastHeartbeat(LocalDateTime.now());
            
            Collector updatedCollector = deviceCollectorRepository.save(collector);
            CollectorDTO resultDTO = new CollectorDTO();
            BeanUtils.copyProperties(updatedCollector, resultDTO);
            
            return resultDTO;
        } catch (Exception e) {
            log.error("激活采集器失败: {}", e.getMessage());
            throw new BusinessException("令牌无效或已过期");
        }
    }

    @Override
    public boolean restartCollector(Long id) {
        // 这里应该通过采集器API发送重启命令，这里为模拟操作
        try {
            Collector collector = deviceCollectorRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("采集器不存在"));
            
            log.info("发送重启命令到采集器: {}", collector.getCollectorName());
            
            // 假设我们更新了状态来反映重启操作
            collector.setStatusInfo("正在重启...");
            deviceCollectorRepository.save(collector);
            
            return true;
        } catch (Exception e) {
            log.error("重启采集器失败: {}", e.getMessage());
            return false;
        }
    }

    @Override
    public Map<String, Long> getCollectorStatusCount() {
        // 统计各状态的采集器数量
        Map<String, Long> counts = new HashMap<>();
        
        long totalCount = deviceCollectorRepository.count();
        counts.put("total", totalCount);
        
        long onlineCount = deviceCollectorRepository.findByStatus(1).size(); // 在线
        counts.put("online", onlineCount);
        
        long offlineCount = deviceCollectorRepository.findByStatus(0).size(); // 离线
        counts.put("offline", offlineCount);
        
        long warningCount = deviceCollectorRepository.findByStatus(2).size(); // 警告
        counts.put("warning", warningCount);
        
        return counts;
    }

    @Override
    public List<CollectorDTO> getAllCollectors() {
        List<Collector> collectors = deviceCollectorRepository.findAll();
        
        return collectors.stream()
                .map(collector -> {
                    CollectorDTO dto = new CollectorDTO();
                    BeanUtils.copyProperties(collector, dto);
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public List<String> getNetworkZones() {
        // 查询所有采集器的网络区域并去重
        List<String> zones = deviceCollectorRepository.findAll().stream()
                .map(Collector::getNetworkZone)
                .filter(Objects::nonNull)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
        
        // 确保返回一个空列表而不是null
        return zones != null ? zones : new ArrayList<>();
    }

    @Override
    public Map<String, Object> testConnection(Long id) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 获取采集器信息
            Collector collector = deviceCollectorRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("采集器不存在"));
            
            // 测试TCP连接（简单的端口连通性测试）
            boolean isConnected = testTcpConnection(collector.getHost(), collector.getPort());
            
            // 创建结果
            result.put("success", isConnected);
            result.put("time", new Date().toString());
            
            if (isConnected) {
                result.put("message", "连接成功");
                result.put("details", String.format("成功连接到 %s:%d", collector.getHost(), collector.getPort()));
                
                // 更新采集器状态
                collector.setStatus(1); // 在线
                collector.setStatusInfo("正常运行");
                collector.setLastHeartbeat(LocalDateTime.now());
                deviceCollectorRepository.save(collector);
            } else {
                result.put("message", "连接失败");
                result.put("details", String.format("无法连接到 %s:%d", collector.getHost(), collector.getPort()));
                
                // 更新采集器状态
                collector.setStatus(0); // 离线
                collector.setStatusInfo("连接失败");
                deviceCollectorRepository.save(collector);
            }
        } catch (Exception e) {
            log.error("测试采集器连接失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "连接测试错误");
            result.put("details", e.getMessage());
        }
        
        return result;
    }
    
    @Override
    public Map<String, Object> testConnectionWithParams(Map<String, Object> params) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            String host = params.get("host") != null ? params.get("host").toString() : null;
            Integer port = params.get("port") != null ? 
                    (params.get("port") instanceof Number ? 
                            ((Number) params.get("port")).intValue() : 
                            Integer.parseInt(params.get("port").toString())) : 
                    null;
            
            if (host == null || port == null) {
                result.put("success", false);
                result.put("message", "缺少必要的连接参数");
                result.put("details", "主机地址和端口是必需的");
                return result;
            }
            
            // 测试TCP连接
            boolean isConnected = testTcpConnection(host, port);
            
            // 创建结果
            result.put("success", isConnected);
            result.put("time", new Date().toString());
            
            if (isConnected) {
                result.put("message", "连接成功");
                result.put("details", String.format("成功连接到 %s:%d", host, port));
            } else {
                result.put("message", "连接失败");
                result.put("details", String.format("无法连接到 %s:%d", host, port));
            }
        } catch (Exception e) {
            log.error("测试采集器连接失败: {}", e.getMessage(), e);
            result.put("success", false);
            result.put("message", "连接测试错误");
            result.put("details", e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 测试TCP连接
     * @param host 主机地址
     * @param port 端口
     * @return 是否连接成功
     */
    private boolean testTcpConnection(String host, int port) {
        Socket socket = null;
        try {
            // 设置连接超时为5秒
            socket = new Socket();
            socket.connect(new InetSocketAddress(host, port), 5000);
            return true;
        } catch (SocketTimeoutException e) {
            log.warn("连接到 {}:{} 超时", host, port);
            return false;
        } catch (Exception e) {
            log.warn("连接到 {}:{} 失败: {}", host, port, e.getMessage());
            return false;
        } finally {
            if (socket != null && !socket.isClosed()) {
                try {
                    socket.close();
                } catch (Exception e) {
                    log.warn("关闭socket失败", e);
                }
            }
        }
    }

    @Override
    @Transactional
    public Collector registerCollector(CollectorRegisterDTO registerDTO) {
        // 由于我们现在使用collectorName作为查询条件，但是DTO中有name属性
        Optional<Collector> existingCollector = collectorRepository.findByCollectorName(registerDTO.getName());

        Collector collector;
        if (existingCollector.isPresent()) {
            collector = existingCollector.get();
            // 更新基本属性，但保留ID、创建时间等
            collector.setCollectorName(registerDTO.getName());
            collector.setHost(registerDTO.getIpAddress());
            collector.setPort(registerDTO.getPort());
            collector.setVersion(registerDTO.getVersion());
            collector.setStatus(1); // ONLINE状态
            collector.setLastHeartbeat(LocalDateTime.now());
        } else {
            collector = new Collector();
            collector.setCollectorName(registerDTO.getName());
            collector.setCollectorType("STANDARD"); // 设置默认类型
            collector.setHost(registerDTO.getIpAddress());
            collector.setPort(registerDTO.getPort());
            collector.setVersion(registerDTO.getVersion());
            collector.setStatus(1); // ONLINE状态
            collector.setLastHeartbeat(LocalDateTime.now());
        }

        return collectorRepository.save(collector);
    }

    @Override
    @Transactional
    public boolean updateHeartbeat(HeartbeatDTO heartbeatDTO) {
        // 由于HeartbeatDTO中使用collectorCode属性，但实际上我们需要使用它作为collectorName查询
        Optional<Collector> optionalCollector = collectorRepository.findByCollectorName(heartbeatDTO.getCollectorCode());
        if (!optionalCollector.isPresent()) {
            log.warn("Heartbeat received from unknown collector: {}", heartbeatDTO.getCollectorCode());
            return false;
        }

        Collector collector = optionalCollector.get();

        // 更新心跳时间和状态
        collector.setLastHeartbeat(LocalDateTime.now());
        collector.setStatus(1); // 在线状态为1

        // 更新其他可能变化的信息
        if (heartbeatDTO.getVersion() != null) {
            collector.setVersion(heartbeatDTO.getVersion());
        }

        collectorRepository.save(collector);
        return true;
    }

    @Override
    public Optional<Collector> findById(Long id) {
        return collectorRepository.findById(id);
    }

    @Override
    public Optional<Collector> findByCode(String collectorCode) {
        return collectorRepository.findByCollectorName(collectorCode);
    }

    @Override
    public List<Collector> findAll() {
        return collectorRepository.findAll();
    }

    @Override
    public List<Collector> findByStatus(String status) {
        // 转换状态字符串为对应的整数值
        Integer statusCode = convertStatusToCode(status);
        return collectorRepository.findByStatus(statusCode);
    }

    @Override
    @Transactional
    public boolean updateStatus(Long id, String status) {
        Optional<Collector> optionalCollector = collectorRepository.findById(id);
        if (!optionalCollector.isPresent()) {
            return false;
        }

        // 转换状态字符串为对应的整数值
        Integer statusCode = convertStatusToCode(status);
        
        Collector collector = optionalCollector.get();
        collector.setStatus(statusCode);
        collectorRepository.save(collector);
        return true;
    }

    /**
     * 将状态字符串转换为整数状态码
     * @param status 状态字符串
     * @return 状态码
     */
    private Integer convertStatusToCode(String status) {
        if (status == null) {
            return null;
        }
        
        switch (status.toUpperCase()) {
            case "ONLINE":
                return 1; // 正常
            case "OFFLINE":
                return 0; // 异常
            case "WARNING":
                return 2; // 警告
            default:
                return 0; // 默认为异常
        }
    }

    @Override
    @Transactional
    public void checkTimeoutCollectors() {
        LocalDateTime timeoutThreshold = LocalDateTime.now().minusMinutes(heartbeatTimeout);
        // 注意：这里1表示"ONLINE"状态
        List<Collector> timeoutCollectors = collectorRepository.findTimeoutCollectors(timeoutThreshold, 1);

        for (Collector collector : timeoutCollectors) {
            log.info("Collector timeout detected: {} ({})", collector.getCollectorName(), collector.getId());
            collector.setStatus(0); // 0表示"OFFLINE"状态
            collectorRepository.save(collector);
        }
    }

    /**
     * 注册采集端并返回响应DTO
     *
     * @param registerDTO 注册请求DTO
     * @return 注册响应DTO
     */
    @Override
    @Transactional
    public CollectorRegisterResponseDTO registerCollectorWithResponse(CollectorRegisterDTO registerDTO) {
        log.info("注册采集端: {}", registerDTO);
        
        Collector collector;
        
        // 检查是否首次注册
        if (registerDTO.getCollectorId() == null || registerDTO.getCollectorId().isEmpty()) {
            // 新建采集器
            collector = new Collector();
            collector.setCollectorId(UUID.randomUUID().toString());
        } else {
            // 已有采集器重新注册
            collector = collectorRepository.findByCollectorId(registerDTO.getCollectorId())
                    .orElseGet(() -> {
                        Collector newCollector = new Collector();
                        newCollector.setCollectorId(registerDTO.getCollectorId());
                        return newCollector;
                    });
        }
        
        // 更新采集器信息
        collector.setHostname(registerDTO.getHostname());
        collector.setIpAddress(registerDTO.getIp());
        collector.setVersion(registerDTO.getVersion());
        collector.setCapabilities(String.join(",", registerDTO.getCapabilities()));
        if (registerDTO.getTags() != null) {
            collector.setTags(String.join(",", registerDTO.getTags()));
        }
        collector.setStatus("ONLINE"); // 直接设置为字符串
        collector.setLastHeartbeatTime(LocalDateTime.now());
        collector.setUpdatedAt(LocalDateTime.now());
        
        if (collector.getCreatedAt() == null) {
            collector.setCreatedAt(LocalDateTime.now());
        }
        
        collector = collectorRepository.save(collector);
        
        // 生成令牌（如果没有jwtUtil则创建一个临时的JWT token）
        String token;
        if (jwtUtil != null) {
            token = jwtUtil.generateToken(collector.getCollectorId());
        } else {
            // 临时生成一个简单token，生产环境应使用注入的jwtUtil
            token = generateSimpleToken(collector.getCollectorId());
        }
        
        // 构造响应
        CollectorRegisterResponseDTO response = new CollectorRegisterResponseDTO();
        response.setCollectorId(collector.getCollectorId());
        response.setToken(token);
        response.setServerTime(LocalDateTime.now().toString());
        
        return response;
    }
    
    /**
     * 生成简单Token (临时使用，应由JwtUtil替代)
     */
    private String generateSimpleToken(String collectorId) {
        return Jwts.builder()
                .setSubject(collectorId)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000)) // 24小时有效期
                .signWith(SignatureAlgorithm.HS512, jwtSecret)
                .compact();
    }

    /**
     * 处理心跳
     *
     * @param collectorId 采集端ID
     * @param heartbeatDTO 心跳请求DTO
     * @return 心跳响应DTO
     */
    @Override
    @Transactional
    public HeartbeatResponseDTO processHeartbeat(String collectorId, HeartbeatDTO heartbeatDTO) {
        log.debug("处理采集端[{}]心跳: {}", collectorId, heartbeatDTO);
        
        Collector collector = collectorRepository.findByCollectorId(collectorId)
                .orElseThrow(() -> new RuntimeException("采集端不存在: " + collectorId));
        
        // 更新状态
        collector.setStatus(heartbeatDTO.getStatus());
        collector.setLastHeartbeatTime(LocalDateTime.now());
        
        // 更新性能指标
        collector.setCpuUsage(heartbeatDTO.getMetrics().getCpu());
        collector.setMemoryUsage(heartbeatDTO.getMetrics().getMemory());
        collector.setDiskUsage(heartbeatDTO.getMetrics().getDisk());
        collector.setRunningTasks(heartbeatDTO.getMetrics().getRunningTasks());
        
        if (heartbeatDTO.getError() != null && !heartbeatDTO.getError().isEmpty()) {
            collector.setLastError(heartbeatDTO.getError());
            collector.setLastErrorTime(LocalDateTime.now());
        }
        
        collector.setUpdatedAt(LocalDateTime.now());
        collectorRepository.save(collector);
        
        // 构造响应
        HeartbeatResponseDTO response = new HeartbeatResponseDTO();
        response.setServerTime(LocalDateTime.now().toString());
        response.setAction("CONTINUE"); // 默认继续工作，可根据系统状态返回不同指令
        
        return response;
    }
} 