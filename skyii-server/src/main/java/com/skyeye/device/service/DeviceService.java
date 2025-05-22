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
} 