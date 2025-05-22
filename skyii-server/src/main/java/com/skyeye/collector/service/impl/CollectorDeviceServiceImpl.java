package com.skyeye.collector.service.impl;

import com.skyeye.collector.dto.BatchAssociateDevicesRequest;
import com.skyeye.collector.dto.CollectorDeviceRelationDTO;
import com.skyeye.collector.entity.Collector;
import com.skyeye.collector.entity.CollectorDeviceRelation;
import com.skyeye.collector.repository.DeviceCollectorRepository;
import com.skyeye.collector.repository.CollectorDeviceRelationRepository;
import com.skyeye.collector.service.CollectorDeviceService;
import com.skyeye.common.exception.BusinessException;
import com.skyeye.device.entity.Device;
import com.skyeye.device.repository.DeviceRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 采集器与设备关联服务实现类
 */
@Slf4j
@Service
public class CollectorDeviceServiceImpl implements CollectorDeviceService {

    @Autowired
    private CollectorDeviceRelationRepository relationRepository;
    
    @Autowired
    private DeviceCollectorRepository collectorRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @PersistenceContext
    private EntityManager entityManager;
    
    @Override
    @Transactional
    public CollectorDeviceRelationDTO associateDevice(CollectorDeviceRelationDTO relationDTO) {
        // 检查采集器是否存在
        Collector collector = collectorRepository.findById(relationDTO.getCollectorId())
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        // 检查设备是否存在
        Device device = deviceRepository.findById(relationDTO.getDeviceId())
                .orElseThrow(() -> new BusinessException("设备不存在"));
        
        // 检查关联关系是否已存在
        if (relationRepository.findByCollectorIdAndDeviceId(
                relationDTO.getCollectorId(), relationDTO.getDeviceId()).isPresent()) {
            throw new BusinessException("设备已关联到该采集器");
        }
        
        // 创建关联关系
        CollectorDeviceRelation relation = new CollectorDeviceRelation();
        BeanUtils.copyProperties(relationDTO, relation);
        
        relation = relationRepository.save(relation);
        
        // 构建返回DTO
        CollectorDeviceRelationDTO result = new CollectorDeviceRelationDTO();
        BeanUtils.copyProperties(relation, result);
        result.setDeviceName(device.getName());
        result.setDeviceType(device.getType());
        result.setDeviceIp(device.getIpAddress());
        
        return result;
    }

    @Override
    @Transactional
    public int batchAssociateDevices(BatchAssociateDevicesRequest request) {
        // 检查采集器是否存在
        Collector collector = collectorRepository.findById(request.getCollectorId())
                .orElseThrow(() -> new BusinessException("采集器不存在"));
        
        // 获取已关联该采集器的设备ID列表
        List<CollectorDeviceRelation> existingRelations = relationRepository.findByCollectorId(request.getCollectorId());
        List<Long> existingDeviceIds = existingRelations.stream()
                .map(CollectorDeviceRelation::getDeviceId)
                .collect(Collectors.toList());
        
        // 过滤出未关联的设备ID
        List<Long> deviceIdsToAssociate = request.getDeviceIds().stream()
                .filter(id -> !existingDeviceIds.contains(id))
                .collect(Collectors.toList());
        
        int successCount = 0;
        
        // 批量创建关联关系
        for (Long deviceId : deviceIdsToAssociate) {
            try {
                // 检查设备是否存在
                if (!deviceRepository.existsById(deviceId)) {
                    log.warn("设备不存在, ID: {}", deviceId);
                    continue;
                }
                
                CollectorDeviceRelation relation = new CollectorDeviceRelation();
                relation.setCollectorId(request.getCollectorId());
                relation.setDeviceId(deviceId);
                relation.setCreateMode(request.getCreateMode());
                relation.setStatus(1);
                
                relationRepository.save(relation);
                successCount++;
            } catch (Exception e) {
                log.error("关联设备失败, 采集器ID: {}, 设备ID: {}, 错误: {}",
                        request.getCollectorId(), deviceId, e.getMessage());
            }
        }
        
        // TODO: 如果需要应用指标模板，可以在此处添加相关逻辑
        
        return successCount;
    }

    @Override
    @Transactional
    public void disassociateDevice(Long id) {
        CollectorDeviceRelation relation = relationRepository.findById(id)
                .orElseThrow(() -> new BusinessException("关联关系不存在"));
        
        relationRepository.delete(relation);
    }

    @Override
    @Transactional
    public void disassociateDevice(Long collectorId, Long deviceId) {
        CollectorDeviceRelation relation = relationRepository.findByCollectorIdAndDeviceId(collectorId, deviceId)
                .orElseThrow(() -> new BusinessException("关联关系不存在"));
        
        relationRepository.delete(relation);
    }

    @Override
    public Page<CollectorDeviceRelationDTO> getDevicesByCollectorId(Long collectorId, Map<String, Object> params, Pageable pageable) {
        // 确认采集器存在
        if (!collectorRepository.existsById(collectorId)) {
            throw new BusinessException("采集器不存在");
        }
        
        // 设备类型过滤
        String deviceType = params.get("deviceType") != null ? params.get("deviceType").toString() : null;
        
        Page<CollectorDeviceRelation> relationsPage;
        if (deviceType != null && !deviceType.isEmpty()) {
            relationsPage = relationRepository.findByCollectorIdAndDeviceType(collectorId, deviceType, pageable);
        } else {
            relationsPage = relationRepository.findByCollectorId(collectorId, pageable);
        }
        
        // 转换为DTO并填充设备信息
        List<CollectorDeviceRelationDTO> relationDTOs = new ArrayList<>();
        for (CollectorDeviceRelation relation : relationsPage) {
            CollectorDeviceRelationDTO dto = new CollectorDeviceRelationDTO();
            BeanUtils.copyProperties(relation, dto);
            
            // 获取设备信息
            deviceRepository.findById(relation.getDeviceId()).ifPresent(device -> {
                dto.setDeviceName(device.getName());
                dto.setDeviceType(device.getType());
                dto.setDeviceIp(device.getIpAddress());
            });
            
            relationDTOs.add(dto);
        }
        
        return new PageImpl<>(relationDTOs, pageable, relationsPage.getTotalElements());
    }

    @Override
    public List<CollectorDeviceRelationDTO> getCollectorsByDeviceId(Long deviceId) {
        // 确认设备存在
        if (!deviceRepository.existsById(deviceId)) {
            throw new BusinessException("设备不存在");
        }
        
        List<CollectorDeviceRelation> relations = relationRepository.findByDeviceId(deviceId);
        
        // 转换为DTO并填充采集器信息
        return relations.stream().map(relation -> {
            CollectorDeviceRelationDTO dto = new CollectorDeviceRelationDTO();
            BeanUtils.copyProperties(relation, dto);
            
            // 获取采集器信息
            collectorRepository.findById(relation.getCollectorId()).ifPresent(collector -> {
                // 此处可以填充更多采集器信息
            });
            
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public long countDevicesByCollectorId(Long collectorId) {
        return relationRepository.countByCollectorId(collectorId);
    }

    @Override
    public Page<Map<String, Object>> getUnassociatedDevices(Map<String, Object> params, Pageable pageable) {
        // 构建查询SQL
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT d.id, d.device_name, d.device_type, d.ip_address, d.status ");
        sql.append("FROM tb_devices d ");
        sql.append("WHERE d.deleted_at IS NULL ");
        sql.append("AND d.id NOT IN (SELECT device_id FROM tb_collector_device_relations) ");
        
        // 添加过滤条件
        List<Object> paramValues = new ArrayList<>();
        int paramIndex = 0;
        
        if (params.containsKey("deviceName") && params.get("deviceName") != null) {
            sql.append("AND d.device_name LIKE ?").append(++paramIndex).append(" ");
            paramValues.add("%" + params.get("deviceName") + "%");
        }
        
        if (params.containsKey("deviceType") && params.get("deviceType") != null) {
            sql.append("AND d.device_type = ?").append(++paramIndex).append(" ");
            paramValues.add(params.get("deviceType"));
        }
        
        if (params.containsKey("status") && params.get("status") != null) {
            sql.append("AND d.status = ?").append(++paramIndex).append(" ");
            paramValues.add(params.get("status"));
        }
        
        // 获取总数
        String countSql = "SELECT COUNT(1) FROM (" + sql.toString() + ") AS cnt";
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (int i = 0; i < paramValues.size(); i++) {
            countQuery.setParameter(i + 1, paramValues.get(i));
        }
        
        long total = ((Number) countQuery.getSingleResult()).longValue();
        
        // 添加分页和排序
        sql.append("ORDER BY d.created_at DESC ");
        sql.append("LIMIT ").append(pageable.getPageSize()).append(" ");
        sql.append("OFFSET ").append(pageable.getOffset());
        
        Query query = entityManager.createNativeQuery(sql.toString());
        for (int i = 0; i < paramValues.size(); i++) {
            query.setParameter(i + 1, paramValues.get(i));
        }
        
        List<Object[]> rows = query.getResultList();
        List<Map<String, Object>> devices = new ArrayList<>();
        
        for (Object[] row : rows) {
            Map<String, Object> device = new HashMap<>();
            device.put("id", row[0]);
            device.put("deviceName", row[1]);
            device.put("deviceType", row[2]);
            device.put("ipAddress", row[3]);
            device.put("status", row[4]);
            devices.add(device);
        }
        
        return new PageImpl<>(devices, pageable, total);
    }
}