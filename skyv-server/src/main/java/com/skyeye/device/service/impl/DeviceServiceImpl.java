package com.skyeye.device.service.impl;

import com.skyeye.device.dto.DeviceDto;
import com.skyeye.device.dto.DeviceQueryRequest;
import com.skyeye.device.entity.Device;
import com.skyeye.device.entity.DeviceType;
import com.skyeye.device.entity.DeviceArea;
import com.skyeye.device.entity.DeviceGroup;
import com.skyeye.device.repository.DeviceRepository;
import com.skyeye.device.repository.DeviceTypeRepository;
import com.skyeye.device.repository.DeviceAreaRepository;
import com.skyeye.device.repository.DeviceGroupRepository;
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
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceAreaRepository deviceAreaRepository;
    private final DeviceGroupRepository deviceGroupRepository;

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
        
        // 前端传入的页码从1开始，需要转换为从0开始
        int pageNumber = Math.max(0, request.getPage() - 1);
        Pageable pageable = PageRequest.of(pageNumber, request.getSize(), sort);

        // 执行查询
        Page<Device> devicePage = deviceRepository.findAll(spec, pageable);

        // 转换为DTO
        return devicePage.map(this::convertToDto);
    }

    @Override
    public List<DeviceDto> getAllDevices() {
        log.debug("Getting all devices");
        
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNull(root.get("deletedAt"));
        
        List<Device> devices = deviceRepository.findAll(spec);
        return devices.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceDto getDeviceById(Long id) {
        log.debug("Getting device by id: {}", id);
        
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), id),
                criteriaBuilder.isNull(root.get("deletedAt"))
            );
        
        Device device = deviceRepository.findOne(spec)
                .orElseThrow(() -> new BusinessException("设备不存在"));
        
        return convertToDto(device);
    }

    @Override
    public Device getDeviceEntityById(Long id) {
        log.debug("Getting device entity by id: {}", id);
        
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("id"), id),
                criteriaBuilder.isNull(root.get("deletedAt"))
            );
        
        return deviceRepository.findOne(spec)
                .orElseThrow(() -> new BusinessException("设备不存在"));
    }

    @Override
    @Transactional
    public DeviceDto createDevice(DeviceDto deviceDto) {
        log.info("Creating device: {}", deviceDto.getName());

        // 验证设备名称唯一性
        if (existsByName(deviceDto.getName())) {
            throw new BusinessException("设备名称已存在");
        }

        // 检查IP地址唯一性
        if (StringUtils.hasText(deviceDto.getIpAddress()) && existsByIpAddress(deviceDto.getIpAddress())) {
            throw new BusinessException("IP地址已被使用");
        }

        // 检查MAC地址唯一性
        if (StringUtils.hasText(deviceDto.getMacAddress()) && existsByMacAddress(deviceDto.getMacAddress())) {
            throw new BusinessException("MAC地址已被使用");
        }

        Device device = convertToEntity(deviceDto);
        
        // 为JSON字段设置默认值，避免JSONB类型问题
        if (device.getConfig() == null) {
            device.setConfig("{}");
        }
        if (device.getCredentials() == null) {
            device.setCredentials("{}");
        }
        
        device.setCreatedAt(LocalDateTime.now());
        device.setUpdatedAt(LocalDateTime.now());

        Device savedDevice = deviceRepository.save(device);
        
        // 更新设备类型统计
        updateDeviceTypeCount(device.getDeviceTypeId());
        
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

        // 更新设备类型统计
        updateDeviceTypeCount(savedDevice.getDeviceTypeId());

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
        
        // 更新设备类型统计
        updateDeviceTypeCount(device.getDeviceTypeId());

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
        
        // 更新相关设备类型的统计
        Set<Long> deviceTypeIds = devices.stream()
                .map(Device::getDeviceTypeId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        deviceTypeIds.forEach(this::updateDeviceTypeCount);

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
        
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.isNull(root.get("deletedAt"));
        
        long totalCount = deviceRepository.count(spec);
        long onlineCount = deviceRepository.count(spec.and((root, query, cb) -> cb.equal(root.get("status"), 1)));
        long offlineCount = deviceRepository.count(spec.and((root, query, cb) -> cb.equal(root.get("status"), 2)));
        long faultCount = deviceRepository.count(spec.and((root, query, cb) -> cb.equal(root.get("status"), 3)));

        stats.put("online", onlineCount);
        stats.put("offline", offlineCount);
        stats.put("fault", faultCount);
        stats.put("total", totalCount);
        stats.put("maintenance", totalCount - onlineCount - offlineCount - faultCount);

        return stats;
    }

    @Override
    public boolean existsByName(String name) {
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("name"), name),
                criteriaBuilder.isNull(root.get("deletedAt"))
            );
        
        return deviceRepository.findOne(spec).isPresent();
    }

    @Override
    public boolean existsByIpAddress(String ipAddress) {
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("ipAddress"), ipAddress),
                criteriaBuilder.isNull(root.get("deletedAt"))
            );
        
        return deviceRepository.findOne(spec).isPresent();
    }

    @Override
    public boolean existsByMacAddress(String macAddress) {
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("macAddress"), macAddress),
                criteriaBuilder.isNull(root.get("deletedAt"))
            );
        
        return deviceRepository.findOne(spec).isPresent();
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        // 使用JPA Specification来排除软删除的设备
        Specification<Device> spec = (root, query, criteriaBuilder) -> 
            criteriaBuilder.and(
                criteriaBuilder.equal(root.get("serialNumber"), serialNumber),
                criteriaBuilder.isNull(root.get("deletedAt"))
            );
        
        return deviceRepository.findOne(spec).isPresent();
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
        
        // 填充关联对象的名称
        fillAssociatedData(dto, device);
        
        return dto;
    }
    
    /**
     * 填充关联数据
     */
    private void fillAssociatedData(DeviceDto dto, Device device) {
        try {
            // 填充设备类型名称
            if (device.getDeviceTypeId() != null) {
                Optional<DeviceType> deviceTypeOpt = deviceTypeRepository.findById(device.getDeviceTypeId());
                if (deviceTypeOpt.isPresent()) {
                    DeviceType deviceType = deviceTypeOpt.get();
                    dto.setDeviceTypeName(deviceType.getName());
                }
            }
            
            // 填充区域名称
            if (device.getAreaId() != null) {
                Optional<DeviceArea> areaOpt = deviceAreaRepository.findById(device.getAreaId());
                if (areaOpt.isPresent()) {
                    DeviceArea area = areaOpt.get();
                    dto.setAreaName(area.getName());
                }
            }
            
            // 填充分组名称
            if (device.getGroupId() != null) {
                Optional<DeviceGroup> groupOpt = deviceGroupRepository.findById(device.getGroupId());
                if (groupOpt.isPresent()) {
                    DeviceGroup group = groupOpt.get();
                    dto.setGroupName(group.getName());
                }
            }
        } catch (Exception e) {
            log.warn("填充关联数据时出错: {}", e.getMessage());
            // 不抛出异常，继续处理其他数据
        }
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

    /**
     * 更新设备类型统计
     * 当设备被创建、更新或删除时，同步更新设备类型表中的设备数量
     */
    private void updateDeviceTypeCount(Long deviceTypeId) {
        try {
            if (deviceTypeId != null) {
                // 统计该类型下的活跃设备数量
                long activeCount = deviceRepository.countByDeviceTypeId(deviceTypeId);
                
                // 更新设备类型表中的设备数量
                deviceTypeRepository.findById(deviceTypeId).ifPresent(deviceType -> {
                    deviceType.setDeviceCount((int) activeCount);
                    deviceTypeRepository.save(deviceType);
                    log.debug("更新设备类型 {} 的设备数量为: {}", deviceType.getName(), activeCount);
                });
            }
        } catch (Exception e) {
            log.warn("更新设备类型统计时出错: {}", e.getMessage());
        }
    }
} 