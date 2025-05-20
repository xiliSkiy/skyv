package com.skyeye.metrics.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.skyeye.metrics.dto.MetricDTO;
import com.skyeye.metrics.entity.Metric;

/**
 * 指标配置服务接口
 */
public interface MetricService {
    
    /**
     * 根据ID获取指标
     * @param id 指标ID
     * @return 指标信息
     */
    MetricDTO getById(Long id);
    
    /**
     * 获取所有指标列表
     * @return 指标列表
     */
    List<MetricDTO> listAll();
    
    /**
     * 分页查询指标
     * @param pageable 分页参数
     * @return 分页指标列表
     */
    Page<MetricDTO> page(Pageable pageable);
    
    /**
     * 条件查询指标
     * @param metricName 指标名称（模糊查询）
     * @param metricType 指标类型
     * @param status 状态
     * @param pageable 分页参数
     * @return 分页指标列表
     */
    Page<MetricDTO> findByConditions(String metricName, String metricType, Integer status, Pageable pageable);
    
    /**
     * 根据指标类型获取指标列表
     * @param metricType 指标类型
     * @return 指标列表
     */
    List<MetricDTO> findByMetricType(String metricType);
    
    /**
     * 根据适用设备类型获取指标列表
     * @param deviceType 设备类型
     * @return 指标列表
     */
    List<MetricDTO> findByApplicableDeviceType(String deviceType);
    
    /**
     * 保存指标配置
     * @param metricDTO 指标信息
     * @return 保存后的指标信息
     */
    MetricDTO save(MetricDTO metricDTO);
    
    /**
     * 批量保存指标配置
     * @param metricDTOs 指标信息列表
     * @return 保存后的指标信息列表
     */
    List<MetricDTO> batchSave(List<MetricDTO> metricDTOs);
    
    /**
     * 更新指标配置
     * @param id 指标ID
     * @param metricDTO 指标信息
     * @return 更新后的指标信息
     */
    MetricDTO update(Long id, MetricDTO metricDTO);
    
    /**
     * 删除指标配置
     * @param id 指标ID
     */
    void delete(Long id);
    
    /**
     * 批量删除指标配置
     * @param ids 指标ID列表
     */
    void batchDelete(List<Long> ids);
    
    /**
     * 检查指标Key是否已存在
     * @param metricKey 指标Key
     * @param id 排除的指标ID（用于更新时检查）
     * @return 是否存在
     */
    boolean isMetricKeyExist(String metricKey, Long id);
    
    /**
     * 导入指标配置
     * @param metrics 指标列表
     * @return 导入结果信息
     */
    String importMetrics(List<MetricDTO> metrics);
    
    /**
     * 导出指标配置
     * @param ids 指标ID列表，为空则导出所有
     * @return 导出的指标列表
     */
    List<MetricDTO> exportMetrics(List<Long> ids);
    
    /**
     * 获取指标统计信息
     * @return 统计信息
     */
    Object getMetricStatistics();
    
    /**
     * 更新指标状态
     * @param id 指标ID
     * @param status 状态
     * @return 更新后的指标信息
     */
    MetricDTO updateStatus(Long id, Integer status);
    
    /**
     * 批量更新指标状态
     * @param ids 指标ID列表
     * @param status 状态
     * @return 更新后的指标信息列表
     */
    List<MetricDTO> batchUpdateStatus(List<Long> ids, Integer status);

    /**
     * 实体转DTO
     * @param metric 实体对象
     * @return DTO对象
     */
    MetricDTO convertToDTO(Metric metric);
    
    /**
     * DTO转实体
     * @param metricDTO DTO对象
     * @return 实体对象
     */
    Metric convertToEntity(MetricDTO metricDTO);
    
    /**
     * 统计指标总数
     * @return 指标总数
     */
    long count();
    
    /**
     * 按状态统计指标数量
     * @param status 状态值
     * @return 指定状态的指标数量
     */
    long countByStatus(Integer status);
    
    /**
     * 统计配置了报警规则的指标数量
     * @return 配置了报警规则的指标数量
     */
    long countByHasAlertRule();
} 