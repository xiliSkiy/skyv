package com.skyeye.metrics.service;

import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.skyeye.metrics.dto.CollectorDTO;
import com.skyeye.metrics.entity.Collector;

/**
 * 采集器配置服务接口
 */
public interface CollectorService {
    
    /**
     * 根据ID获取采集器
     * @param id 采集器ID
     * @return 采集器信息
     */
    CollectorDTO getById(Long id);
    
    /**
     * 获取所有采集器列表
     * @return 采集器列表
     */
    List<CollectorDTO> listAll();
    
    /**
     * 分页查询采集器
     * @param pageable 分页参数
     * @return 分页采集器列表
     */
    Page<CollectorDTO> page(Pageable pageable);
    
    /**
     * 根据采集器类型获取采集器列表
     * @param collectorType 采集器类型
     * @return 采集器列表
     */
    List<CollectorDTO> findByCollectorType(String collectorType);
    
    /**
     * 根据状态获取采集器列表
     * @param status 状态
     * @return 采集器列表
     */
    List<CollectorDTO> findByStatus(Integer status);
    
    /**
     * 获取主采集器信息
     * @return 主采集器
     */
    CollectorDTO getMainCollector();
    
    /**
     * 保存采集器配置
     * @param collectorDTO 采集器信息
     * @return 保存后的采集器信息
     */
    CollectorDTO save(CollectorDTO collectorDTO);
    
    /**
     * 更新采集器配置
     * @param id 采集器ID
     * @param collectorDTO 采集器信息
     * @return 更新后的采集器信息
     */
    CollectorDTO update(Long id, CollectorDTO collectorDTO);
    
    /**
     * 删除采集器配置
     * @param id 采集器ID
     */
    void delete(Long id);
    
    /**
     * 测试采集器连接
     * @param id 采集器ID
     * @return 测试结果
     */
    Map<String, Object> testConnection(Long id);
    
    /**
     * 设置为主采集器
     * @param id 采集器ID
     * @return 更新后的采集器信息
     */
    CollectorDTO setAsMain(Long id);
    
    /**
     * 更新采集器状态
     * @param id 采集器ID
     * @param status 状态
     * @param statusInfo 状态信息
     * @return 更新后的采集器信息
     */
    CollectorDTO updateStatus(Long id, Integer status, String statusInfo);
    
    /**
     * 获取采集器状态统计
     * @return 状态统计信息
     */
    Map<String, Object> getStatusStatistics();
    
    /**
     * 实体转DTO
     * @param collector 实体对象
     * @return DTO对象
     */
    CollectorDTO convertToDTO(Collector collector);
    
    /**
     * DTO转实体
     * @param collectorDTO DTO对象
     * @return 实体对象
     */
    Collector convertToEntity(CollectorDTO collectorDTO);
    
    /**
     * 统计采集器总数
     * @return 采集器总数
     */
    long count();
    
    /**
     * 按状态统计采集器数量
     * @param status 状态值
     * @return 指定状态的采集器数量
     */
    long countByStatus(Integer status);
} 