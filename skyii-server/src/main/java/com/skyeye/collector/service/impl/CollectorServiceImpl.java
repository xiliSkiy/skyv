package com.skyeye.collector.service.impl;

import com.skyeye.collector.dto.CollectorDTO;
import com.skyeye.collector.dto.CollectorMetricsDTO;
import com.skyeye.collector.entity.Collector;
import com.skyeye.collector.repository.DeviceCollectorRepository;
import com.skyeye.collector.service.CollectorService;
import com.skyeye.common.exception.BusinessException;
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
    private DeviceCollectorRepository collectorRepository;

    @Value("${jwt.secret:defaultSecretKey}")
    private String jwtSecret;

    @Override
    @Transactional
    public CollectorDTO createCollector(CollectorDTO collectorDTO) {
        // 检查采集器名称是否已存在
        if (collectorRepository.findByCollectorName(collectorDTO.getCollectorName()).isPresent()) {
            throw new BusinessException("采集器名称已存在");
        }

        Collector collector = new Collector();
        BeanUtils.copyProperties(collectorDTO, collector);
        
        // 设置初始状态
        collector.setStatus(0); // 创建后默认为离线状态
        collector.setStatusInfo("等待连接");
        
        // 生成API密钥
        collector.setApiKey(UUID.randomUUID().toString().replace("-", ""));
        
        Collector savedCollector = collectorRepository.save(collector);
        CollectorDTO resultDTO = new CollectorDTO();
        BeanUtils.copyProperties(savedCollector, resultDTO);
        
        return resultDTO;
    }

    @Override
    @Transactional
    public CollectorDTO updateCollector(Long id, CollectorDTO collectorDTO) {
        Collector collector = collectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        // 检查名称是否冲突
        if (!collector.getCollectorName().equals(collectorDTO.getCollectorName()) &&
                collectorRepository.findByCollectorName(collectorDTO.getCollectorName()).isPresent()) {
            throw new BusinessException("采集器名称已存在");
        }
        
        BeanUtils.copyProperties(collectorDTO, collector);
        collector.setId(id); // 确保ID不变
        
        Collector updatedCollector = collectorRepository.save(collector);
        CollectorDTO resultDTO = new CollectorDTO();
        BeanUtils.copyProperties(updatedCollector, resultDTO);
        
        return resultDTO;
    }

    @Override
    @Transactional
    public void deleteCollector(Long id) {
        Collector collector = collectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        // 执行逻辑删除而不是物理删除
        collector.setDeletedAt(LocalDateTime.now());
        collectorRepository.save(collector);
    }

    @Override
    public CollectorDTO getCollectorById(Long id) {
        Collector collector = collectorRepository.findById(id)
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        CollectorDTO resultDTO = new CollectorDTO();
        BeanUtils.copyProperties(collector, resultDTO);
        
        return resultDTO;
    }      
          
    @Override
    public Page<CollectorDTO> findCollectors(Map<String, Object> params, Pageable pageable) {
        log.info("查询采集器列表, 参数: {}, 分页: {}", params, pageable);
        // 直接获取所有数据，先不过滤，看能否找到数据
        List<Collector> allCollectors = collectorRepository.findAll();
        log.info("数据库中总记录数: {}", allCollectors.size());
        for (Collector collector : allCollectors) {
            log.info("采集器记录: id={}, name={}, deletedAt={}, status={}",
            collector.getId(), collector.getCollectorName(), collector.getDeletedAt(), collector.getStatus());
        }
        // 使用Specification查询
        Page<Collector> collectorsPage = collectorRepository.findAll((root, query, cb) -> {
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
        Collector collector = collectorRepository.findById(id)
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
        Collector collector = collectorRepository.findById(id)
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
            Collector collector = collectorRepository.findById(id)
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
            
            Collector updatedCollector = collectorRepository.save(collector);
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
            Collector collector = collectorRepository.findById(id)
                    .orElseThrow(() -> new BusinessException("采集器不存在"));
            
            log.info("发送重启命令到采集器: {}", collector.getCollectorName());
            
            // 假设我们更新了状态来反映重启操作
            collector.setStatusInfo("正在重启...");
            collectorRepository.save(collector);
            
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
        
        long totalCount = collectorRepository.count();
        counts.put("total", totalCount);
        
        long onlineCount = collectorRepository.findByStatus(1).size(); // 在线
        counts.put("online", onlineCount);
        
        long offlineCount = collectorRepository.findByStatus(0).size(); // 离线
        counts.put("offline", offlineCount);
        
        long warningCount = collectorRepository.findByStatus(2).size(); // 警告
        counts.put("warning", warningCount);
        
        return counts;
    }

    @Override
    public List<CollectorDTO> getAllCollectors() {
        List<Collector> collectors = collectorRepository.findAll();
        
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
        List<String> zones = collectorRepository.findAll().stream()
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
            Collector collector = collectorRepository.findById(id)
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
                collectorRepository.save(collector);
            } else {
                result.put("message", "连接失败");
                result.put("details", String.format("无法连接到 %s:%d", collector.getHost(), collector.getPort()));
                
                // 更新采集器状态
                collector.setStatus(0); // 离线
                collector.setStatusInfo("连接失败");
                collectorRepository.save(collector);
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
} 