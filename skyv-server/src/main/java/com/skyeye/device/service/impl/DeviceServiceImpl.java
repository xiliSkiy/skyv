package com.skyeye.device.service.impl;

import com.skyeye.device.dto.DeviceDto;
import com.skyeye.device.dto.DeviceQueryRequest;
import com.skyeye.device.entity.Device;
import com.skyeye.device.repository.DeviceRepository;
import com.skyeye.device.service.DeviceService;
import com.skyeye.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import jakarta.persistence.criteria.Predicate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备服务实现类
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceServiceImpl implements DeviceService {

    private final DeviceRepository deviceRepository;

    @Override
    public Page<DeviceDto> getDeviceList(DeviceQueryRequest request) {
        log.debug("Getting device list with request: {}", request);

        // 构建查询规格
        Specification<Device> spec = buildSpecification(request);

        // 构建分页和排序
        Sort sort = Sort.by(
            "desc".equalsIgnoreCase(request.getSortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC,
            request.getSortBy()
        );
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize(), sort);

        // 执行查询
        Page<Device> devicePage = deviceRepository.findAll(spec, pageable);

        // 转换为DTO
        return devicePage.map(this::convertToDto);
    }

    @Override
    public List<DeviceDto> getAllDevices() {
        log.debug("Getting all devices");
        
        List<Device> devices = deviceRepository.findAll();
        return devices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDto getDeviceById(Long id) {
        log.debug("Getting device by id: {}", id);
        
        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        
        return convertToDto(device);
    }

    @Override
    @Transactional
    public DeviceDto createDevice(DeviceDto deviceDto) {
        log.info("Creating device: {}", deviceDto.getName());

        // 验证设备名称唯一性
        if (existsByName(deviceDto.getName())) {
            throw new BusinessException("设备名称已存在");
        }

        // 验证IP地址唯一性（如果提供）
        if (StringUtils.hasText(deviceDto.getIpAddress()) && existsByIpAddress(deviceDto.getIpAddress())) {
            throw new BusinessException("IP地址已被使用");
        }

        // 验证MAC地址唯一性（如果提供）
        if (StringUtils.hasText(deviceDto.getMacAddress()) && existsByMacAddress(deviceDto.getMacAddress())) {
            throw new BusinessException("MAC地址已被使用");
        }

        Device device = convertToEntity(deviceDto);
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());

        Device savedDevice = deviceRepository.save(device);
        log.info("Device created successfully with id: {}", savedDevice.getId());

        return convertToDto(savedDevice);
    }

    @Override
    @Transactional
    public DeviceDto updateDevice(Long id, DeviceDto deviceDto) {
        log.info("Updating device with id: {}", id);

        Device existingDevice = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));

        // 验证设备名称唯一性（排除当前设备）
        if (!existingDevice.getName().equals(deviceDto.getName()) && existsByName(deviceDto.getName())) {
            throw new BusinessException("设备名称已存在");
        }

        // 更新设备信息
        BeanUtils.copyProperties(deviceDto, existingDevice, "id", "createdAt", "createdBy");
        existingDevice.setUpdatedAt(LocalDateTime.now());

        Device savedDevice = deviceRepository.save(existingDevice);
        log.info("Device updated successfully with id: {}", savedDevice.getId());

        return convertToDto(savedDevice);
    }

    @Override
    @Transactional
    public void deleteDevice(Long id) {
        log.info("Deleting device with id: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));

        // 软删除
        device.setDeletedAt(LocalDateTime.now());
        deviceRepository.save(device);

        log.info("Device deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteDevices(List<Long> ids) {
        log.info("Batch deleting devices with ids: {}", ids);

        List<Device> devices = deviceRepository.findByIdInAndNotDeleted(ids);
        if (devices.size() != ids.size()) {
            throw new BusinessException("部分设备不存在或已被删除");
        }

        LocalDateTime now = LocalDateTime.now();
        devices.forEach(device -> device.setDeletedAt(now));
        deviceRepository.saveAll(devices);

        log.info("Batch deleted {} devices successfully", devices.size());
    }

    @Override
    public Map<String, Object> testDeviceConnection(Long id) {
        log.info("Testing device connection for id: {}", id);

        Device device = deviceRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备不存在"));

        Map<String, Object> result = new HashMap<>();
        
        // 模拟连接测试
        // 实际项目中这里会调用具体的设备连接测试逻辑
        boolean connected = testConnection(device);
        
        result.put("connected", connected);
        result.put("deviceId", id);
        result.put("deviceName", device.getName());
        result.put("testTime", LocalDateTime.now());
        
        if (connected) {
            result.put("message", "设备连接正常");
            // 更新设备在线状态
            device.setStatus(1); // 在线
            device.setLastOnlineAt(LocalDateTime.now());
            deviceRepository.save(device);
        } else {
            result.put("message", "设备连接失败");
            device.setStatus(2); // 离线
            deviceRepository.save(device);
        }

        return result;
    }

    @Override
    public Map<String, Object> getDeviceStats() {
        log.debug("Getting device statistics");

        Map<String, Object> stats = new HashMap<>();
        
        long onlineCount = deviceRepository.countOnlineDevices();
        long offlineCount = deviceRepository.countOfflineDevices();
        long faultCount = deviceRepository.countFaultDevices();
        long totalCount = deviceRepository.count();

        stats.put("online", onlineCount);
        stats.put("offline", offlineCount);
        stats.put("fault", faultCount);
        stats.put("total", totalCount);
        stats.put("maintenance", totalCount - onlineCount - offlineCount - faultCount);

        return stats;
    }

    @Override
    public boolean existsByName(String name) {
        return deviceRepository.findByName(name).isPresent();
    }

    @Override
    public boolean existsByIpAddress(String ipAddress) {
        return deviceRepository.findByIpAddress(ipAddress).isPresent();
    }

    @Override
    public boolean existsByMacAddress(String macAddress) {
        return deviceRepository.findByMacAddress(macAddress).isPresent();
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return deviceRepository.findBySerialNumber(serialNumber).isPresent();
    }

    /**
     * 构建查询规格
     */
    private Specification<Device> buildSpecification(DeviceQueryRequest request) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 设备名称模糊查询
            if (StringUtils.hasText(request.getName())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("name")),
                    "%" + request.getName().toLowerCase() + "%"
                ));
            }

            // 设备类型
            if (request.getDeviceTypeId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("deviceTypeId"), request.getDeviceTypeId()));
            }

            // 区域
            if (request.getAreaId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("areaId"), request.getAreaId()));
            }

            // 分组
            if (request.getGroupId() != null) {
                predicates.add(criteriaBuilder.equal(root.get("groupId"), request.getGroupId()));
            }

            // 状态
            if (request.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), request.getStatus()));
            }

            // 协议
            if (StringUtils.hasText(request.getProtocol())) {
                predicates.add(criteriaBuilder.equal(root.get("protocol"), request.getProtocol()));
            }

            // IP地址
            if (StringUtils.hasText(request.getIpAddress())) {
                predicates.add(criteriaBuilder.like(root.get("ipAddress"), "%" + request.getIpAddress() + "%"));
            }

            // 制造商
            if (StringUtils.hasText(request.getManufacturer())) {
                predicates.add(criteriaBuilder.like(
                    criteriaBuilder.lower(root.get("manufacturer")),
                    "%" + request.getManufacturer().toLowerCase() + "%"
                ));
            }

            // 排除软删除的数据
            predicates.add(criteriaBuilder.isNull(root.get("deletedAt")));

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    /**
     * 实体转DTO
     */
    private DeviceDto convertToDto(Device device) {
        DeviceDto dto = new DeviceDto();
        BeanUtils.copyProperties(device, dto);
        
        // 设置状态名称
        dto.setStatusName(getStatusName(device.getStatus()));
        
        // 这里可以设置关联对象的名称
        // 例如：设备类型名称、区域名称、分组名称等
        // 实际项目中需要查询关联表获取名称
        
        return dto;
    }

    /**
     * DTO转实体
     */
    private Device convertToEntity(DeviceDto dto) {
        Device device = new Device();
        BeanUtils.copyProperties(dto, device, "id", "statusName", "deviceTypeName", "areaName", "groupName");
        return device;
    }

    /**
     * 获取状态名称
     */
    private String getStatusName(Integer status) {
        if (status == null) return "未知";
        
        return switch (status) {
            case 1 -> "在线";
            case 2 -> "离线";
            case 3 -> "故障";
            case 4 -> "维护";
            default -> "未知";
        };
    }

    /**
     * 测试设备连接
     * 实际项目中这里需要根据设备类型和协议实现具体的连接测试逻辑
     */
    private boolean testConnection(Device device) {
        // 模拟连接测试
        // 这里可以根据设备的协议类型进行不同的连接测试
        try {
            Thread.sleep(1000); // 模拟网络延迟
            // 随机返回成功或失败，实际项目中替换为真实的连接测试
            return Math.random() > 0.3; // 70%成功率
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        }
    }
} 