package com.skyeye.collector.service;

import com.skyeye.collector.dto.BatchAssociateDevicesRequest;
import com.skyeye.collector.dto.CollectorDeviceRelationDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 采集器与设备关联服务接口
 */
public interface CollectorDeviceService {

    /**
     * 关联设备到采集器
     * @param relationDTO 关联信息
     * @return 创建的关联关系
     */
    CollectorDeviceRelationDTO associateDevice(CollectorDeviceRelationDTO relationDTO);
    
    /**
     * 批量关联设备到采集器
     * @param request 批量关联请求
     * @return 成功关联的设备数量
     */
    int batchAssociateDevices(BatchAssociateDevicesRequest request);
    
    /**
     * 取消设备与采集器的关联
     * @param id 关联关系ID
     */
    void disassociateDevice(Long id);
    
    /**
     * 取消设备与采集器的关联
     * @param collectorId 采集器ID
     * @param deviceId 设备ID
     */
    void disassociateDevice(Long collectorId, Long deviceId);
    
    /**
     * 获取采集器关联的设备列表
     * @param collectorId 采集器ID
     * @param params 查询参数
     * @param pageable 分页参数
     * @return 设备列表
     */
    Page<CollectorDeviceRelationDTO> getDevicesByCollectorId(Long collectorId, Map<String, Object> params, Pageable pageable);
    
    /**
     * 获取设备关联的采集器列表
     * @param deviceId 设备ID
     * @return 采集器列表
     */
    List<CollectorDeviceRelationDTO> getCollectorsByDeviceId(Long deviceId);
    
    /**
     * 获取采集器关联的设备数量
     * @param collectorId 采集器ID
     * @return 设备数量
     */
    long countDevicesByCollectorId(Long collectorId);
    
    /**
     * 获取未关联采集器的设备列表
     * @param params 查询参数
     * @param pageable 分页参数
     * @return 未关联采集器的设备列表
     */
    Page<Map<String, Object>> getUnassociatedDevices(Map<String, Object> params, Pageable pageable);
} 