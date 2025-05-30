package com.skyeye.collector.service;

import com.skyeye.collector.dto.CollectorDTO;
import com.skyeye.collector.dto.CollectorMetricsDTO;
import com.skyeye.collector.entity.Collector;
import com.skyeye.scheduler.dto.CollectorRegisterDTO;
import com.skyeye.scheduler.dto.HeartbeatDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 采集器服务接口
 */
public interface CollectorService {

    /**
     * 创建采集器
     * @param collectorDTO 采集器信息
     * @return 创建的采集器
     */
    CollectorDTO createCollector(CollectorDTO collectorDTO);

    /**
     * 更新采集器
     * @param id 采集器ID
     * @param collectorDTO 采集器信息
     * @return 更新后的采集器
     */
    CollectorDTO updateCollector(Long id, CollectorDTO collectorDTO);

    /**
     * 删除采集器
     * @param id 采集器ID
     */
    void deleteCollector(Long id);

    /**
     * 获取采集器详情
     * @param id 采集器ID
     * @return 采集器详情
     */
    CollectorDTO getCollectorById(Long id);

    /**
     * 条件分页查询采集器
     * @param params 查询参数
     * @param pageable 分页参数
     * @return 分页结果
     */
    Page<CollectorDTO> findCollectors(Map<String, Object> params, Pageable pageable);

    /**
     * 获取采集器监控指标
     * @param id 采集器ID
     * @return 监控指标
     */
    CollectorMetricsDTO getCollectorMetrics(Long id);

    /**
     * 生成采集器注册令牌
     * @param id 采集器ID
     * @param expireHours 过期小时数
     * @return 注册令牌
     */
    String generateRegistrationToken(Long id, Integer expireHours);

    /**
     * 根据注册令牌激活采集器
     * @param token 注册令牌
     * @param collectorInfo 采集器连接信息
     * @return 更新后的采集器
     */
    CollectorDTO activateCollector(String token, Map<String, Object> collectorInfo);

    /**
     * 重启采集器
     * @param id 采集器ID
     * @return 操作结果
     */
    boolean restartCollector(Long id);

    /**
     * 获取采集器状态统计
     * @return 状态统计 
     */
    Map<String, Long> getCollectorStatusCount();
    
    /**
     * 获取所有采集器
     * @return 采集器列表
     */
    List<CollectorDTO> getAllCollectors();
    
    /**
     * 获取网络区域列表
     * @return 网络区域列表
     */
    List<String> getNetworkZones();
    
    /**
     * 测试与采集器的连接
     * @param id 采集器ID
     * @return 测试结果
     */
    Map<String, Object> testConnection(Long id);
    
    /**
     * 使用自定义参数测试采集器连接
     * @param params 包含连接参数的Map
     * @return 测试结果
     */
    Map<String, Object> testConnectionWithParams(Map<String, Object> params);

    /**
     * 注册采集器
     * @param registerDTO 注册信息
     * @return 采集器信息
     */
    Collector registerCollector(CollectorRegisterDTO registerDTO);

    /**
     * 更新采集器心跳
     * @param heartbeatDTO 心跳信息
     * @return 是否更新成功
     */
    boolean updateHeartbeat(HeartbeatDTO heartbeatDTO);

    /**
     * 根据ID查询采集器
     * @param id 采集器ID
     * @return 采集器信息
     */
    Optional<Collector> findById(Long id);

    /**
     * 根据编码查询采集器
     * @param collectorCode 采集器编码
     * @return 采集器信息
     */
    Optional<Collector> findByCode(String collectorCode);

    /**
     * 查询所有采集器
     * @return 采集器列表
     */
    List<Collector> findAll();

    /**
     * 根据状态查询采集器
     * @param status 状态
     * @return 采集器列表
     */
    List<Collector> findByStatus(String status);

    /**
     * 更新采集器状态
     * @param id 采集器ID
     * @param status 状态
     * @return 是否更新成功
     */
    boolean updateStatus(Long id, String status);

    /**
     * 检查超时采集器
     * 超过指定时间未心跳的采集器将被标记为离线
     */
    void checkTimeoutCollectors();
} 