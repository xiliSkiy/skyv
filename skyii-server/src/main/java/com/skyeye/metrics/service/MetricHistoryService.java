package com.skyeye.metrics.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.skyeye.metrics.dto.MetricHistoryDTO;

/**
 * 指标采集历史服务接口
 */
public interface MetricHistoryService {
    
    /**
     * 根据条件查询采集历史
     * @param metricId 指标ID
     * @param deviceId 设备ID
     * @param status 状态
     * @param startTime 开始时间
     * @param endTime 结束时间
     * @param pageable 分页参数
     * @return 采集历史分页数据
     */
    Page<MetricHistoryDTO> findByConditions(Long metricId, Long deviceId, Integer status, 
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable);
    
    /**
     * Der
     * @param id 采集历史ID
     * @return 采集历史详情
     */
    MetricHistoryDTO findById(Long id);
    
    /**
     * 根据指标ID查询最近采集历史
     * @param metricId 指标ID
     * @return 采集历史列表
     */
    List<MetricHistoryDTO> findLatestByMetricId(Long metricId);
    
    /**
     * 删除采集历史
     * @param id 采集历史ID
     */
    void deleteById(Long id);
    
    /**
     * 批量删除采集历史
     * @param ids 采集历史ID列表
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 获取采集历史统计信息
     * @return 统计信息
     */
    Object getStatistics();
    
    /**
     * 手动触发指标采集
     * @param metricId 指标ID
     * @param deviceId 设备ID
     * @return 采集结果
     */
    MetricHistoryDTO triggerCollection(Long metricId, Long deviceId);
    
    /**
     * 清理历史数据
     * @param days 保留天数
     * @return 清理数量
     */
    int cleanHistory(Integer days);
} 