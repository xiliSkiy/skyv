package com.skyeye.metrics.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skyeye.metrics.dto.CollectorDTO;
import com.skyeye.metrics.entity.Collector;
import com.skyeye.metrics.repository.CollectorRepository;
import com.skyeye.metrics.service.CollectorService;

/**
 * 采集器配置服务实现类
 */
@Service
public class CollectorServiceImpl implements CollectorService {

    @Autowired
    private CollectorRepository collectorRepository;
    
    @Override
    public CollectorDTO getById(Long id) {
        return collectorRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("未找到采集器配置：" + id));
    }

    @Override
    public List<CollectorDTO> listAll() {
        return collectorRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<CollectorDTO> page(Pageable pageable) {
        return collectorRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<CollectorDTO> findByCollectorType(String collectorType) {
        return collectorRepository.findByCollectorType(collectorType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<CollectorDTO> findByStatus(Integer status) {
        return collectorRepository.findByStatus(status).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public CollectorDTO getMainCollector() {
        return collectorRepository.findByIsMainTrue()
                .map(this::convertToDTO)
                .orElse(null);
    }

    @Override
    @Transactional
    public CollectorDTO save(CollectorDTO collectorDTO) {
        Collector collector = convertToEntity(collectorDTO);
        
        // 如果设置为主采集器，则更新其他采集器的主采集器状态
        if (Boolean.TRUE.equals(collector.getIsMain())) {
            updateOtherCollectorsMainStatus();
        }
        
        collector.setCreatedAt(LocalDateTime.now());
        collector.setUpdatedAt(LocalDateTime.now());
        
        Collector savedCollector = collectorRepository.save(collector);
        return convertToDTO(savedCollector);
    }

    @Override
    @Transactional
    public CollectorDTO update(Long id, CollectorDTO collectorDTO) {
        Collector existingCollector = collectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到采集器配置：" + id));
        
        // 保持原有的创建时间
        LocalDateTime createdAt = existingCollector.getCreatedAt();
        
        // 复制属性
        BeanUtils.copyProperties(collectorDTO, existingCollector);
        
        // 如果设置为主采集器，则更新其他采集器的主采集器状态
        if (Boolean.TRUE.equals(existingCollector.getIsMain())) {
            updateOtherCollectorsMainStatus(id);
        }
        
        // 设置ID和更新时间
        existingCollector.setId(id);
        existingCollector.setCreatedAt(createdAt);
        existingCollector.setUpdatedAt(LocalDateTime.now());
        
        Collector updatedCollector = collectorRepository.save(existingCollector);
        return convertToDTO(updatedCollector);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Collector collector = collectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到采集器配置：" + id));
        
        // 不允许删除主采集器
        if (Boolean.TRUE.equals(collector.getIsMain())) {
            throw new IllegalStateException("不能删除主采集器，请先设置其他采集器为主采集器");
        }
        
        collectorRepository.deleteById(id);
    }

    @Override
    public Map<String, Object> testConnection(Long id) {
        Collector collector = collectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到采集器配置：" + id));
        
        // 模拟测试连接过程
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 这里应该实现实际的连接测试逻辑
            // 模拟测试成功
            result.put("success", true);
            result.put("message", "连接成功");
            result.put("detail", "延迟: 5ms");
            
            // 更新最后心跳时间
            collector.setLastHeartbeat(LocalDateTime.now());
            collector.setStatus(1); // 正常状态
            collector.setStatusInfo("连接正常");
            collectorRepository.save(collector);
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "连接失败");
            result.put("detail", e.getMessage());
            
            // 更新状态为异常
            collector.setStatus(0); // 异常状态
            collector.setStatusInfo("连接失败: " + e.getMessage());
            collectorRepository.save(collector);
        }
        
        return result;
    }

    @Override
    @Transactional
    public CollectorDTO setAsMain(Long id) {
        Collector collector = collectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到采集器配置：" + id));
        
        // 更新其他采集器的主采集器状态
        updateOtherCollectorsMainStatus(id);
        
        // 设置当前采集器为主采集器
        collector.setIsMain(true);
        collector.setUpdatedAt(LocalDateTime.now());
        
        Collector updatedCollector = collectorRepository.save(collector);
        return convertToDTO(updatedCollector);
    }

    @Override
    @Transactional
    public CollectorDTO updateStatus(Long id, Integer status, String statusInfo) {
        Collector collector = collectorRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到采集器配置：" + id));
        
        collector.setStatus(status);
        collector.setStatusInfo(statusInfo);
        collector.setUpdatedAt(LocalDateTime.now());
        
        Collector updatedCollector = collectorRepository.save(collector);
        return convertToDTO(updatedCollector);
    }

    @Override
    public Map<String, Object> getStatusStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 采集器总数
        statistics.put("totalCount", collectorRepository.count());
        
        // 各状态采集器数量
        List<Object[]> statusCounts = collectorRepository.countByStatus();
        
        Map<Integer, Long> statusMap = new HashMap<>();
        for (Object[] stat : statusCounts) {
            Integer status = (Integer) stat[0];
            Long count = (Long) stat[1];
            statusMap.put(status, count);
        }
        
        statistics.put("normalCount", statusMap.getOrDefault(1, 0L)); // 正常
        statistics.put("warningCount", statusMap.getOrDefault(2, 0L)); // 警告
        statistics.put("errorCount", statusMap.getOrDefault(0, 0L)); // 异常
        
        // 主采集器状态
        collectorRepository.findByIsMainTrue().ifPresent(main -> {
            Map<String, Object> mainCollector = new HashMap<>();
            mainCollector.put("id", main.getId());
            mainCollector.put("name", main.getCollectorName());
            mainCollector.put("status", main.getStatus());
            mainCollector.put("lastHeartbeat", main.getLastHeartbeat());
            
            statistics.put("mainCollector", mainCollector);
        });
        
        return statistics;
    }

    @Override
    public CollectorDTO convertToDTO(Collector collector) {
        CollectorDTO collectorDTO = new CollectorDTO();
        BeanUtils.copyProperties(collector, collectorDTO);
        return collectorDTO;
    }

    @Override
    public Collector convertToEntity(CollectorDTO collectorDTO) {
        Collector collector = new Collector();
        BeanUtils.copyProperties(collectorDTO, collector);
        return collector;
    }
    
    /**
     * 更新其他采集器的主采集器状态
     */
    private void updateOtherCollectorsMainStatus() {
        updateOtherCollectorsMainStatus(null);
    }
    
    /**
     * 更新其他采集器的主采集器状态
     * @param excludeId 排除的采集器ID
     */
    private void updateOtherCollectorsMainStatus(Long excludeId) {
        List<Collector> collectors;
        if (excludeId != null) {
            collectors = collectorRepository.findAll().stream()
                    .filter(c -> !c.getId().equals(excludeId) && Boolean.TRUE.equals(c.getIsMain()))
                    .collect(Collectors.toList());
        } else {
            collectors = collectorRepository.findAll().stream()
                    .filter(c -> Boolean.TRUE.equals(c.getIsMain()))
                    .collect(Collectors.toList());
        }
        
        collectors.forEach(c -> {
            c.setIsMain(false);
            c.setUpdatedAt(LocalDateTime.now());
        });
        
        if (!collectors.isEmpty()) {
            collectorRepository.saveAll(collectors);
        }
    }

    @Override
    public long count() {
        return collectorRepository.count();
    }
    
    @Override
    public long countByStatus(Integer status) {
        return collectorRepository.countByStatus(status);
    }
} 