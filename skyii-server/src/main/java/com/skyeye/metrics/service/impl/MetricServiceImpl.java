package com.skyeye.metrics.service.impl;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.persistence.EntityNotFoundException;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skyeye.metrics.dto.MetricDTO;
import com.skyeye.metrics.entity.Metric;
import com.skyeye.metrics.repository.MetricRepository;
import com.skyeye.metrics.service.MetricService;

/**
 * 指标配置服务实现类
 */
@Service
public class MetricServiceImpl implements MetricService {

    @Autowired
    private MetricRepository metricRepository;
    
    @Override
    public MetricDTO getById(Long id) {
        return metricRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new EntityNotFoundException("未找到指标配置：" + id));
    }

    @Override
    public List<MetricDTO> listAll() {
        return metricRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public Page<MetricDTO> page(Pageable pageable) {
        return metricRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    @Override
    public Page<MetricDTO> findByConditions(String metricName, String metricType, Integer status, Pageable pageable) {
        return metricRepository.findByConditions(metricName, metricType, status, pageable)
                .map(this::convertToDTO);
    }

    @Override
    public List<MetricDTO> findByMetricType(String metricType) {
        return metricRepository.findByMetricType(metricType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<MetricDTO> findByApplicableDeviceType(String deviceType) {
        return metricRepository.findByApplicableDeviceType(deviceType).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MetricDTO save(MetricDTO metricDTO) {
        if (isMetricKeyExist(metricDTO.getMetricKey(), null)) {
            throw new IllegalArgumentException("指标Key已存在：" + metricDTO.getMetricKey());
        }
        
        Metric metric = convertToEntity(metricDTO);
        metric.setCreatedAt(LocalDateTime.now());
        metric.setUpdatedAt(LocalDateTime.now());
        
        Metric savedMetric = metricRepository.save(metric);
        return convertToDTO(savedMetric);
    }

    @Override
    @Transactional
    public List<MetricDTO> batchSave(List<MetricDTO> metricDTOs) {
        List<Metric> metrics = metricDTOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
        
        List<Metric> savedMetrics = metricRepository.saveAll(metrics);
        return savedMetrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public MetricDTO update(Long id, MetricDTO metricDTO) {
        Metric existingMetric = metricRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到指标配置：" + id));
        
        // 检查指标Key是否重复（排除当前指标）
        if (!existingMetric.getMetricKey().equals(metricDTO.getMetricKey()) 
                && isMetricKeyExist(metricDTO.getMetricKey(), id)) {
            throw new IllegalArgumentException("指标Key已存在：" + metricDTO.getMetricKey());
        }
        
        // 保持原有的创建时间
        LocalDateTime createdAt = existingMetric.getCreatedAt();
        
        // 复制属性
        BeanUtils.copyProperties(metricDTO, existingMetric);
        
        // 设置ID和更新时间
        existingMetric.setId(id);
        existingMetric.setCreatedAt(createdAt);
        existingMetric.setUpdatedAt(LocalDateTime.now());
        
        Metric updatedMetric = metricRepository.save(existingMetric);
        return convertToDTO(updatedMetric);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!metricRepository.existsById(id)) {
            throw new EntityNotFoundException("未找到指标配置：" + id);
        }
        metricRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void batchDelete(List<Long> ids) {
        List<Metric> metrics = metricRepository.findAllById(ids);
        metricRepository.deleteAll(metrics);
    }

    @Override
    public boolean isMetricKeyExist(String metricKey, Long id) {
        Optional<Metric> existingMetric = metricRepository.findByMetricKey(metricKey);
        return existingMetric.isPresent() && (id == null || !existingMetric.get().getId().equals(id));
    }

    @Override
    @Transactional
    public String importMetrics(List<MetricDTO> metrics) {
        int success = 0;
        int failure = 0;
        
        for (MetricDTO metricDTO : metrics) {
            try {
                if (metricDTO.getId() != null && metricRepository.existsById(metricDTO.getId())) {
                    update(metricDTO.getId(), metricDTO);
                } else {
                    save(metricDTO);
                }
                success++;
            } catch (Exception e) {
                failure++;
            }
        }
        
        return String.format("导入完成，成功：%d，失败：%d", success, failure);
    }

    @Override
    public List<MetricDTO> exportMetrics(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return listAll();
        } else {
            return metricRepository.findAllById(ids).stream()
                    .map(this::convertToDTO)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public Object getMetricStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 指标总数
        statistics.put("totalCount", metricRepository.count());
        
        // 活跃指标数
        statistics.put("activeCount", metricRepository.findByConditions(null, null, 1, Pageable.unpaged()).getTotalElements());
        
        // 各类型指标统计
        Map<String, Long> typeStats = metricRepository.findAll().stream()
                .collect(Collectors.groupingBy(Metric::getMetricType, Collectors.counting()));
        statistics.put("typeStatistics", typeStats);
        
        return statistics;
    }

    @Override
    @Transactional
    public MetricDTO updateStatus(Long id, Integer status) {
        Metric metric = metricRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("未找到指标配置：" + id));
        
        metric.setStatus(status);
        metric.setUpdatedAt(LocalDateTime.now());
        
        Metric updatedMetric = metricRepository.save(metric);
        return convertToDTO(updatedMetric);
    }

    @Override
    @Transactional
    public List<MetricDTO> batchUpdateStatus(List<Long> ids, Integer status) {
        List<Metric> metrics = metricRepository.findAllById(ids);
        
        metrics.forEach(metric -> {
            metric.setStatus(status);
            metric.setUpdatedAt(LocalDateTime.now());
        });
        
        List<Metric> updatedMetrics = metricRepository.saveAll(metrics);
        return updatedMetrics.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public MetricDTO convertToDTO(Metric metric) {
        MetricDTO metricDTO = new MetricDTO();
        BeanUtils.copyProperties(metric, metricDTO);
        return metricDTO;
    }

    @Override
    public Metric convertToEntity(MetricDTO metricDTO) {
        Metric metric = new Metric();
        BeanUtils.copyProperties(metricDTO, metric);
        return metric;
    }

    @Override
    public long count() {
        return metricRepository.count();
    }

    @Override
    public long countByStatus(Integer status) {
        return metricRepository.countByStatus(status);
    }

    @Override
    public long countByHasAlertRule() {
        // 假设有报警规则的指标在数据库中某个字段非空或为特定值
        // 这里需要根据实际数据库设计调整
        return metricRepository.countByHasAlertRule();
    }
} 