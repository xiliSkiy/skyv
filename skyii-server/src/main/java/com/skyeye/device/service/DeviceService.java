package com.skyeye.device.service;

import com.skyeye.device.entity.Device;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 设备服务接口
 */
public interface DeviceService {
    
    /**
     * 创建设备
     */
    Device createDevice(Device device);
    
    /**
     * 更新设备
     */
    Device updateDevice(Long id, Device device);
    
    /**
     * 删除设备
     */
    void deleteDevice(Long id);
    
    /**
     * 根据ID查询设备
     */
    Optional<Device> findById(Long id);
    
    /**
     * 根据ID列表查询设备
     * @param ids 设备ID列表
     * @return 设备列表
     */
    List<Device> findAllById(List<Long> ids);
    
    /**
     * 根据编码查询设备
     */
    Optional<Device> findByCode(String code);
    
    /**
     * 分页查询设备
     */
    Page<Device> findAll(Pageable pageable);
    
    /**
     * 根据条件分页查询设备
     */
    Page<Device> findByConditions(String name, String type, Integer status, Long groupId, Pageable pageable);
    
    /**
     * 根据分组ID查询设备
     */
    List<Device> findByGroupId(Long groupId);
    
    /**
     * 根据分组ID分页查询设备
     */
    Page<Device> findByGroupId(Long groupId, Pageable pageable);
    
    /**
     * 统计设备状态
     */
    Map<Integer, Long> countByStatus();
    
    /**
     * 更新设备状态
     */
    Device updateStatus(Long id, Integer status);
    
    /**
     * 批量更新设备状态
     */
    void batchUpdateStatus(List<Long> ids, Integer status);
    
    /**
     * 检查设备连接
     */
    boolean checkConnection(Long id);
    
    /**
     * 根据采集器ID查询设备
     * @param collectorId 采集器ID
     * @param includeAssigned 是否包含已分配的设备
     * @param pageable 分页参数
     * @return 设备分页结果
     */
    Page<Device> findByCollectorId(Long collectorId, boolean includeAssigned, Pageable pageable);
    
    /**
     * 获取设备类型树
     * @return 设备类型树
     */
    List<Map<String, Object>> getDeviceTypeTree();
    
    /**
     * 根据类型查询设备
     * @param typeId 类型ID
     * @param keyword 关键字
     * @param status 状态
     * @param pageable 分页参数
     * @return 设备分页结果
     */
    Page<Device> findByType(String typeId, String keyword, String status, Pageable pageable);
    
    /**
     * 获取区域列表
     * @return 区域列表
     */
    List<Map<String, Object>> getAreaList();
    
    /**
     * 根据区域查询设备
     * @param areaId 区域ID
     * @param keyword 关键字
     * @param status 状态
     * @param pageable 分页参数
     * @return 设备分页结果
     */
    Page<Device> findByArea(String areaId, String keyword, String status, Pageable pageable);
    
    /**
     * 获取所有标签
     * @return 标签列表
     */
    List<String> getAllTags();
    
    /**
     * 根据标签查询设备
     * @param tags 标签列表
     * @param tagOperator 标签操作符（AND/OR）
     * @param keyword 关键字
     * @param status 状态
     * @param pageable 分页参数
     * @return 设备分页结果
     */
    Page<Device> findByTags(List<String> tags, String tagOperator, String keyword, String status, Pageable pageable);
    
    /**
     * 根据ID列表查询设备
     * @param ids 设备ID列表
     * @return 设备列表
     */
    List<Device> findByIds(List<Long> ids);
    
    /**
     * 获取设备类型统计
     * @param deviceIds 设备ID列表
     * @return 类型统计数据
     */
    Map<String, Integer> getDeviceTypeStats(List<Long> deviceIds);
    
    /**
     * 获取设备状态统计
     * @param deviceIds 设备ID列表
     * @return 状态统计数据
     */
    Map<String, Integer> getDeviceStatusStats(List<Long> deviceIds);
    
    /**
     * 获取指标分类
     * @return 指标分类列表
     */
    List<Map<String, Object>> getMetricCategories();
    
    /**
     * 根据分类获取指标
     * @param categoryId 分类ID
     * @return 指标列表
     */
    List<Map<String, Object>> getMetricsByCategory(String categoryId);
    
    /**
     * 搜索指标
     * @param keyword 关键字
     * @return 指标列表
     */
    List<Map<String, Object>> searchMetrics(String keyword);
    
    /**
     * 根据ID列表获取指标
     * @param metricIds 指标ID列表
     * @return 指标列表
     */
    List<Map<String, Object>> getMetricsByIds(List<Long> metricIds);
    
    /**
     * 获取设备分组列表
     * @return 设备分组列表
     */
    List<Map<String, Object>> getDeviceGroups();
    
    /**
     * 根据分组ID获取设备列表
     * @param groupId 分组ID
     * @param pageable 分页参数
     * @return 设备分页结果
     */
    Page<Device> getDevicesByGroupId(Long groupId, Pageable pageable);
} 