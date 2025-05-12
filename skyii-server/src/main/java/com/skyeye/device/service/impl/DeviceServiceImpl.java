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
        deviceRepository.save(device);
    }

    @Override
    public Optional<Device> findById(Long id) {
        return deviceRepository.findById(id);
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
} 