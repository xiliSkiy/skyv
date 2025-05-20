package com.skyeye.metrics.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.skyeye.device.entity.Device;
import com.skyeye.device.repository.DeviceRepository;
import com.skyeye.metrics.dto.MetricHistoryDTO;
import com.skyeye.metrics.entity.Collector;
import com.skyeye.metrics.entity.Metric;
import com.skyeye.metrics.entity.MetricHistory;
import com.skyeye.metrics.repository.CollectorRepository;
import com.skyeye.metrics.repository.MetricHistoryRepository;
import com.skyeye.metrics.repository.MetricRepository;
import com.skyeye.metrics.service.MetricHistoryService;

/**
 * 指标采集历史服务实现类
 */
@Service
public class MetricHistoryServiceImpl implements MetricHistoryService {
    
    @Autowired
    private MetricHistoryRepository metricHistoryRepository;
    
    @Autowired
    private MetricRepository metricRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private CollectorRepository collectorRepository;
    
    @Override
    public Page<MetricHistoryDTO> findByConditions(Long metricId, Long deviceId, Integer status, 
            LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        
        Page<MetricHistory> page = metricHistoryRepository.findByConditions(
                metricId, deviceId, status, startTime, endTime, pageable);
        
        return page.map(this::convertToDTO);
    }
    
    @Override
    public MetricHistoryDTO findById(Long id) {
        Optional<MetricHistory> optional = metricHistoryRepository.findById(id);
        return optional.map(this::convertToDTO).orElse(null);
    }
    
    @Override
    public List<MetricHistoryDTO> findLatestByMetricId(Long metricId) {
        List<MetricHistory> historyList = metricHistoryRepository.findTop10ByMetricIdOrderByCollectionTimeDesc(metricId);
        return historyList.stream().map(this::convertToDTO).collect(Collectors.toList());
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        metricHistoryRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        ids.forEach(this::deleteById);
    }
    
    @Override
    public Object getStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        
        // 统计今日采集次数
        LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime tomorrow = today.plusDays(1);
        long todayCount = metricHistoryRepository.countByCollectionTimeBetween(today, tomorrow);
        statistics.put("todayCount", todayCount);
        
        // 统计本周采集次数
        LocalDateTime weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        LocalDateTime weekEnd = weekStart.plusDays(7);
        long weekCount = metricHistoryRepository.countByCollectionTimeBetween(weekStart, weekEnd);
        statistics.put("weekCount", weekCount);
        
        // 统计本月采集次数
        LocalDateTime monthStart = today.withDayOfMonth(1);
        LocalDateTime monthEnd = monthStart.plusMonths(1);
        long monthCount = metricHistoryRepository.countByCollectionTimeBetween(monthStart, monthEnd);
        statistics.put("monthCount", monthCount);
        
        // 统计不同状态的采集次数
        Map<Integer, Long> statusCounts = new HashMap<>();
        for (int i = 0; i <= 3; i++) {
            long count = metricHistoryRepository.countByStatus(i);
            statusCounts.put(i, count);
        }
        statistics.put("statusCounts", statusCounts);
        
        return statistics;
    }
    
    @Override
    @Transactional
    public MetricHistoryDTO triggerCollection(Long metricId, Long deviceId) {
        // 查询指标信息
        Optional<Metric> metricOptional = metricRepository.findById(metricId);
        if (!metricOptional.isPresent()) {
            throw new IllegalArgumentException("指标不存在");
        }
        
        Metric metric = metricOptional.get();
        
        // 创建采集历史记录
        MetricHistory history = new MetricHistory();
        history.setMetricId(metricId);
        history.setDeviceId(deviceId);
        
        // 查找可用的采集器
        Optional<Collector> mainCollector = collectorRepository.findByIsMainTrue();
        if (mainCollector.isPresent()) {
            history.setCollectorId(mainCollector.get().getId());
        }
        
        // 设置采集时间和状态
        history.setCollectionTime(LocalDateTime.now());
        history.setStatus(MetricHistoryDTO.STATUS_PROCESSING);
        history.setCreatedAt(LocalDateTime.now());
        
        // 模拟采集过程（实际项目中应该使用异步任务）
        // 此处仅作为示例，实际采集逻辑应由采集服务完成
        try {
            Thread.sleep(500); // 模拟采集耗时
            history.setRawValue("mock_value_" + System.currentTimeMillis());
            history.setProcessedValue("23.5"); // 模拟处理后的值
            history.setStatus(MetricHistoryDTO.STATUS_SUCCESS);
            history.setCollectionDuration(500L);
        } catch (Exception e) {
            history.setStatus(MetricHistoryDTO.STATUS_FAILED);
            history.setStatusInfo(e.getMessage());
        }
        
        // 保存采集历史
        MetricHistory savedHistory = metricHistoryRepository.save(history);
        
        // 更新指标最后采集时间
        metric.setLastCollectionTime(history.getCollectionTime());
        metricRepository.save(metric);
        
        return convertToDTO(savedHistory);
    }
    
    @Override
    @Transactional
    public int cleanHistory(Integer days) {
        if (days == null || days <= 0) {
            throw new IllegalArgumentException("保留天数必须大于0");
        }
        
        LocalDateTime threshold = LocalDateTime.now().minusDays(days);
        List<MetricHistory> oldRecords = metricHistoryRepository.findByCollectionTimeBetween(
                LocalDateTime.of(2000, 1, 1, 0, 0), threshold, Pageable.unpaged()).getContent();
        
        int count = oldRecords.size();
        metricHistoryRepository.deleteAll(oldRecords);
        
        return count;
    }
    
    /**
     * 将实体转换为DTO
     * @param history 采集历史实体
     * @return 采集历史DTO
     */
    private MetricHistoryDTO convertToDTO(MetricHistory history) {
        MetricHistoryDTO dto = new MetricHistoryDTO();
        BeanUtils.copyProperties(history, dto);
        
        // 填充指标信息
        Optional<Metric> metricOptional = metricRepository.findById(history.getMetricId());
        if (metricOptional.isPresent()) {
            Metric metric = metricOptional.get();
            dto.setMetricName(metric.getMetricName());
            dto.setMetricKey(metric.getMetricKey());
            dto.setUnit(metric.getUnit());
        }
        
        // 填充设备信息
        if (history.getDeviceId() != null) {
            Optional<Device> deviceOptional = deviceRepository.findById(history.getDeviceId());
            if (deviceOptional.isPresent()) {
                dto.setDeviceName(deviceOptional.get().getName());
            }
        }
        
        // 填充采集器信息
        if (history.getCollectorId() != null) {
            Optional<Collector> collectorOptional = collectorRepository.findById(history.getCollectorId());
            if (collectorOptional.isPresent()) {
                dto.setCollectorName(collectorOptional.get().getCollectorName());
            }
        }
        
        return dto;
    }
} 