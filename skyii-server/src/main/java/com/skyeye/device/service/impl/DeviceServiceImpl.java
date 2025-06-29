package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.device.entity.Device;
import com.skyeye.device.repository.DeviceRepository;
import com.skyeye.device.service.DeviceService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 设备服务实现类
 */
@Slf4j
@Service
public class DeviceServiceImpl implements DeviceService {

    @Autowired
    private DeviceRepository deviceRepository;

    @Override
    @Transactional
    public Device createDevice(Device device) {
        // 检查设备编码是否已存在
        if (deviceRepository.existsByCode(device.getCode())) {
            throw new BusinessException("设备编码已存在");
        }
        
        // 设置初始状态
        device.setStatus(0); // 离线状态
        
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public Device updateDevice(Long id, Device device) {
        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        
        // 检查设备编码是否已被其他设备使用
        if (!existingDevice.getCode().equals(device.getCode()) && 
                deviceRepository.existsByCode(device.getCode())) {
            throw new BusinessException("设备编码已被其他设备使用");
        }
        
        // 更新设备信息
        existingDevice.setName(device.getName());
        existingDevice.setCode(device.getCode());
        existingDevice.setType(device.getType());
        existingDevice.setIpAddress(device.getIpAddress());
        existingDevice.setPort(device.getPort());
        existingDevice.setUsername(device.getUsername());
        
        // 只有当密码不为空时才更新密码
        if (device.getPassword() != null && !device.getPassword().isEmpty()) {
            existingDevice.setPassword(device.getPassword());
        }
        
        existingDevice.setLocation(device.getLocation());
        existingDevice.setDescription(device.getDescription());
        existingDevice.setGroupId(device.getGroupId());
        
        return deviceRepository.save(existingDevice);
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        
        // 执行软删除
        device.setDeletedAt(LocalDateTime.now());
        deviceRepository.delete(device);
    }

    @Override
    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
    }

    @Override
    public List<Device> findAllById(List<Long> ids) {
        return deviceRepository.findAllById(ids);
    }

    @Override
    public Optional<Device> findByCode(String code) {
        return deviceRepository.findByCode(code);
    }

    @Override
    public Page<Device> findAll(Pageable pageable) {
        return deviceRepository.findAll(pageable);
    }

    @Override
    public Page<Device> findByConditions(String name, String type, Integer status, Long groupId, Pageable pageable) {
        return deviceRepository.findByConditions(name, type, status, groupId, pageable);
    }

    @Override
    public List<Device> findByGroupId(Long groupId) {
        return deviceRepository.findByGroupId(groupId);
    }

    @Override
    public Page<Device> findByGroupId(Long groupId, Pageable pageable) {
        return deviceRepository.findByGroupId(groupId, pageable);
    }

    @Override
    public Map<Integer, Long> countByStatus() {
        List<Object[]> results = deviceRepository.countByStatus();
        Map<Integer, Long> statusCounts = new HashMap<>();
        
        for (Object[] result : results) {
            Integer status = (Integer) result[0];
            Long count = (Long) result[1];
            statusCounts.put(status, count);
        }
        
        return statusCounts;
    }

    @Override
    @Transactional
    public Device updateStatus(Long id, Integer status) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        
        device.setStatus(status);
        
        // 如果设备状态为在线，更新最后心跳时间
        if (status == 1) {
            device.setLastHeartbeatTime(LocalDateTime.now());
        }
        
        return deviceRepository.save(device);
    }

    @Override
    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        List<Device> devices = deviceRepository.findAllById(ids);
        
        if (devices.isEmpty()) {
            return;
        }
        
        LocalDateTime now = LocalDateTime.now();
        
        devices.forEach(device -> {
            device.setStatus(status);
            if (status == 1) {
                device.setLastHeartbeatTime(now);
            }
        });
        
        deviceRepository.saveAll(devices);
    }

    @Override
    public boolean checkConnection(Long id) {
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        
        // 这里应该实现真实的设备连接检查逻辑
        // 目前仅作为示例，返回true表示连接成功
        log.info("检查设备连接: {}", device.getName());
        
        // 更新设备状态为在线
        device.setStatus(1);
        device.setLastHeartbeatTime(LocalDateTime.now());
        deviceRepository.save(device);
        
        return true;
    }

    @Override
    public Page<Device> findByCollectorId(Long collectorId, boolean includeAssigned, Pageable pageable) {
        // 这里需要调用采集器-设备关系表查询设备
        // 简化实现方式：返回一个空的分页结果
        log.info("查询采集器[{}]的设备, 包含已分配: {}", collectorId, includeAssigned);
        
        // 在实际实现中，这里应该根据collectorId查询设备列表
        // 模拟返回结果
        return Page.empty(pageable);
    }

    @Override
    public Page<Device> getDevicesByGroupId(Long groupId, Pageable pageable) {
        return deviceRepository.findByGroupId(groupId, pageable);
    }
    
    @Override
    public List<Map<String, Object>> getDeviceTypeTree() {
        // 简化实现，返回一些示例设备类型
        List<Map<String, Object>> typeTree = new ArrayList<>();
        
        Map<String, Object> typeNode1 = new HashMap<>();
        typeNode1.put("id", "server");
        typeNode1.put("name", "服务器");
        typeNode1.put("icon", "server");
        typeNode1.put("deviceCount", 10);
        typeTree.add(typeNode1);
        
        Map<String, Object> typeNode2 = new HashMap<>();
        typeNode2.put("id", "network");
        typeNode2.put("name", "网络设备");
        typeNode2.put("icon", "network");
        typeNode2.put("deviceCount", 8);
        typeTree.add(typeNode2);
        
        Map<String, Object> typeNode3 = new HashMap<>();
        typeNode3.put("id", "camera");
        typeNode3.put("name", "摄像头");
        typeNode3.put("icon", "camera");
        typeNode3.put("deviceCount", 15);
        typeTree.add(typeNode3);
        
        return typeTree;
    }
    
    @Override
    public Page<Device> findByType(String typeId, String keyword, String status, Pageable pageable) {
        // 简化实现，使用现有方法进行查询
        Integer statusCode = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusCode = Integer.parseInt(status);
            } catch (NumberFormatException e) {
                log.warn("Invalid status value: {}", status);
            }
        }
        
        // 使用现有的查询方法，模拟按类型查询
        return findByConditions(keyword, typeId, statusCode, null, pageable);
    }
    
    @Override
    public List<String> getAllTags() {
        // 简化实现，返回一些示例标签
        return Arrays.asList("重要", "核心", "边缘", "测试", "生产");
    }
    
    @Override
    public Page<Device> findByTags(List<String> tags, String tagOperator, String keyword, String status, Pageable pageable) {
        // 简化实现，使用现有方法进行查询
        Integer statusCode = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusCode = Integer.parseInt(status);
            } catch (NumberFormatException e) {
                log.warn("Invalid status value: {}", status);
            }
        }
        
        // 使用现有的查询方法，忽略标签条件
        return findByConditions(keyword, null, statusCode, null, pageable);
    }
    
    @Override
    public List<Device> findByIds(List<Long> ids) {
        return deviceRepository.findAllById(ids);
    }
    
    @Override
    public Map<String, Integer> getDeviceTypeStats(List<Long> deviceIds) {
        // 简化实现，返回一些示例统计数据
        Map<String, Integer> typeStats = new HashMap<>();
        typeStats.put("服务器", 5);
        typeStats.put("网络设备", 3);
        typeStats.put("摄像头", 2);
        return typeStats;
    }
    
    @Override
    public Map<String, Integer> getDeviceStatusStats(List<Long> deviceIds) {
        // 简化实现，返回一些示例统计数据
        Map<String, Integer> statusStats = new HashMap<>();
        statusStats.put("在线", 7);
        statusStats.put("离线", 2);
        statusStats.put("故障", 1);
        return statusStats;
    }
    
    @Override
    public List<Map<String, Object>> getMetricCategories() {
        // 简化实现，返回一些示例指标分类
        List<Map<String, Object>> categories = new ArrayList<>();
        
        Map<String, Object> category1 = new HashMap<>();
        category1.put("id", "system");
        category1.put("name", "系统指标");
        category1.put("description", "系统性能相关指标");
        category1.put("icon", "cpu");
        category1.put("metricCount", 10);
        
        Map<String, Object> category2 = new HashMap<>();
        category2.put("id", "network");
        category2.put("name", "网络指标");
        category2.put("description", "网络性能相关指标");
        category2.put("icon", "network");
        category2.put("metricCount", 8);
        
        categories.add(category1);
        categories.add(category2);
        
        return categories;
    }
    
    @Override
    public List<Map<String, Object>> getMetricsByCategory(String categoryId) {
        // 简化实现，返回一些示例指标
        List<Map<String, Object>> metrics = new ArrayList<>();
        
        if ("system".equals(categoryId)) {
            Map<String, Object> metric1 = new HashMap<>();
            metric1.put("id", 1);
            metric1.put("name", "CPU使用率");
            metric1.put("code", "cpu_usage");
            metric1.put("description", "CPU使用率百分比");
            metric1.put("unit", "%");
            metric1.put("dataType", "gauge");
            metric1.put("collectionMethod", "agent");
            metric1.put("collectionInterval", 60);
            metric1.put("categoryId", categoryId);
            metric1.put("categoryName", "系统指标");
            metric1.put("isSystem", true);
            
            Map<String, Object> metric2 = new HashMap<>();
            metric2.put("id", 2);
            metric2.put("name", "内存使用率");
            metric2.put("code", "memory_usage");
            metric2.put("description", "内存使用率百分比");
            metric2.put("unit", "%");
            metric2.put("dataType", "gauge");
            metric2.put("collectionMethod", "agent");
            metric2.put("collectionInterval", 60);
            metric2.put("categoryId", categoryId);
            metric2.put("categoryName", "系统指标");
            metric2.put("isSystem", true);
            
            metrics.add(metric1);
            metrics.add(metric2);
        } else if ("network".equals(categoryId)) {
            Map<String, Object> metric1 = new HashMap<>();
            metric1.put("id", 3);
            metric1.put("name", "网络流入速率");
            metric1.put("code", "network_in");
            metric1.put("description", "网络流入速率");
            metric1.put("unit", "KB/s");
            metric1.put("dataType", "gauge");
            metric1.put("collectionMethod", "snmp");
            metric1.put("collectionInterval", 60);
            metric1.put("categoryId", categoryId);
            metric1.put("categoryName", "网络指标");
            metric1.put("isSystem", true);
            
            metrics.add(metric1);
        }
        
        return metrics;
    }
    
    @Override
    public List<Map<String, Object>> searchMetrics(String keyword) {
        // 简化实现，返回一些示例指标
        List<Map<String, Object>> metrics = new ArrayList<>();
        
        if (keyword == null || keyword.isEmpty() || 
                "cpu".contains(keyword.toLowerCase()) || 
                "使用率".contains(keyword)) {
            Map<String, Object> metric = new HashMap<>();
            metric.put("id", 1);
            metric.put("name", "CPU使用率");
            metric.put("code", "cpu_usage");
            metric.put("description", "CPU使用率百分比");
            metric.put("unit", "%");
            metric.put("dataType", "gauge");
            metric.put("collectionMethod", "agent");
            metric.put("collectionInterval", 60);
            metric.put("categoryId", "system");
            metric.put("categoryName", "系统指标");
            metric.put("isSystem", true);
            
            metrics.add(metric);
        }
        
        if (keyword == null || keyword.isEmpty() || 
                "memory".contains(keyword.toLowerCase()) || 
                "内存".contains(keyword)) {
            Map<String, Object> metric = new HashMap<>();
            metric.put("id", 2);
            metric.put("name", "内存使用率");
            metric.put("code", "memory_usage");
            metric.put("description", "内存使用率百分比");
            metric.put("unit", "%");
            metric.put("dataType", "gauge");
            metric.put("collectionMethod", "agent");
            metric.put("collectionInterval", 60);
            metric.put("categoryId", "system");
            metric.put("categoryName", "系统指标");
            metric.put("isSystem", true);
            
            metrics.add(metric);
        }
        
        return metrics;
    }
    
    @Override
    public List<Map<String, Object>> getMetricsByIds(List<Long> metricIds) {
        // 简化实现，返回一些示例指标
        List<Map<String, Object>> metrics = new ArrayList<>();
        
        for (Long id : metricIds) {
            if (id == 1) {
                Map<String, Object> metric = new HashMap<>();
                metric.put("id", 1);
                metric.put("name", "CPU使用率");
                metric.put("code", "cpu_usage");
                metric.put("description", "CPU使用率百分比");
                metric.put("unit", "%");
                metric.put("dataType", "gauge");
                metric.put("collectionMethod", "agent");
                metric.put("collectionInterval", 60);
                metric.put("categoryId", "system");
                metric.put("categoryName", "系统指标");
                metric.put("isSystem", true);
                
                metrics.add(metric);
            } else if (id == 2) {
                Map<String, Object> metric = new HashMap<>();
                metric.put("id", 2);
                metric.put("name", "内存使用率");
                metric.put("code", "memory_usage");
                metric.put("description", "内存使用率百分比");
                metric.put("unit", "%");
                metric.put("dataType", "gauge");
                metric.put("collectionMethod", "agent");
                metric.put("collectionInterval", 60);
                metric.put("categoryId", "system");
                metric.put("categoryName", "系统指标");
                metric.put("isSystem", true);
                
                metrics.add(metric);
            }
        }
        
        return metrics;
    }
    
    @Override
    public List<Map<String, Object>> getDeviceGroups() {
        // 简化实现，返回一些示例设备分组
        List<Map<String, Object>> groups = new ArrayList<>();
        
        Map<String, Object> group1 = new HashMap<>();
        group1.put("id", 1);
        group1.put("name", "服务器组");
        group1.put("description", "服务器设备分组");
        group1.put("deviceCount", 5);
        
        Map<String, Object> group2 = new HashMap<>();
        group2.put("id", 2);
        group2.put("name", "网络设备组");
        group2.put("description", "网络设备分组");
        group2.put("deviceCount", 3);
        
        groups.add(group1);
        groups.add(group2);
        
        return groups;
    }

    @Override
    public Page<Device> findByArea(String areaId, String keyword, String status, Pageable pageable) {
        // 简化实现，使用现有方法进行查询
        Integer statusCode = null;
        if (status != null && !status.isEmpty()) {
            try {
                statusCode = Integer.parseInt(status);
            } catch (NumberFormatException e) {
                log.warn("Invalid status value: {}", status);
            }
        }
        
        // 使用现有的查询方法，忽略区域条件
        return findByConditions(keyword, null, statusCode, null, pageable);
    }

    @Override
    public List<Map<String, Object>> getAreaList() {
        // 简化实现，返回一些示例区域
        List<Map<String, Object>> areas = new ArrayList<>();
        
        Map<String, Object> area1 = new HashMap<>();
        area1.put("id", "area1");
        area1.put("name", "北区");
        area1.put("level", 1);
        area1.put("longitude", 116.397428);
        area1.put("latitude", 39.90923);
        area1.put("deviceCount", 10);
        
        Map<String, Object> area2 = new HashMap<>();
        area2.put("id", "area2");
        area2.put("name", "南区");
        area2.put("level", 1);
        area2.put("longitude", 116.397428);
        area2.put("latitude", 39.90923);
        area2.put("deviceCount", 8);
        
        areas.add(area1);
        areas.add(area2);
        
        return areas;
    }
} 