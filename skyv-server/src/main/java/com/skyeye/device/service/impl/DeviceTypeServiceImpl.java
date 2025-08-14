package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.device.dto.DeviceTypeDto;
import com.skyeye.device.entity.DeviceType;
import com.skyeye.device.repository.DeviceTypeRepository;
import com.skyeye.device.service.DeviceTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备类型服务实现类
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceTypeServiceImpl implements DeviceTypeService {

    private final DeviceTypeRepository deviceTypeRepository;
    // 保留：若未来需要复杂 JSON 转换可注入 ObjectMapper

    @Override
    public List<DeviceTypeDto> getDeviceTypeTree(String name) {
        log.debug("Getting device type tree with name filter: {}", name);

        List<DeviceType> allTypes;
        if (StringUtils.hasText(name)) {
            allTypes = deviceTypeRepository.findByNameContaining(name);
        } else {
            allTypes = deviceTypeRepository.findAllActiveTypes();
        }

        // 构建树形结构
        return buildTree(allTypes);
    }

    @Override
    public List<DeviceTypeDto> getAllDeviceTypes() {
        log.debug("Getting all device types");

        List<DeviceType> types = deviceTypeRepository.findAllActiveTypes();
        return types.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceTypeDto getDeviceTypeById(Long id) {
        log.debug("Getting device type by id: {}", id);

        DeviceType deviceType = deviceTypeRepository.findById(id)
                .filter(dt -> dt.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("设备类型不存在"));

        return convertToDto(deviceType);
    }

    @Override
    @Transactional
    public DeviceTypeDto createDeviceType(DeviceTypeDto deviceTypeDto) {
        log.info("Creating device type: {}", deviceTypeDto.getName());

        // 检查编码是否存在
        if (deviceTypeRepository.existsByCode(deviceTypeDto.getCode())) {
            throw new BusinessException("设备类型编码已存在");
        }

        // 如果有父类型，检查父类型是否存在
        if (deviceTypeDto.getParentId() != null) {
            if (!deviceTypeRepository.existsById(deviceTypeDto.getParentId())) {
                throw new BusinessException("父类型不存在");
            }
        }

        DeviceType deviceType = convertToEntity(deviceTypeDto);
        // 时间字段将由@CreatedDate和@LastModifiedDate自动设置

        deviceType = deviceTypeRepository.save(deviceType);
        return convertToDto(deviceType);
    }

    @Override
    @Transactional
    public DeviceTypeDto updateDeviceType(Long id, DeviceTypeDto deviceTypeDto) {
        log.info("Updating device type with id: {}", id);

        DeviceType existingType = deviceTypeRepository.findById(id)
                .filter(dt -> dt.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("设备类型不存在"));

        // 检查编码是否被其他类型使用
        if (deviceTypeRepository.existsByCodeAndIdNot(deviceTypeDto.getCode(), id)) {
            throw new BusinessException("设备类型编码已存在");
        }

        // 如果有父类型，检查父类型是否存在且不是自己
        if (deviceTypeDto.getParentId() != null) {
            if (deviceTypeDto.getParentId().equals(id)) {
                throw new BusinessException("不能将自己设置为父类型");
            }
            if (!deviceTypeRepository.existsById(deviceTypeDto.getParentId())) {
                throw new BusinessException("父类型不存在");
            }
        }

        // 更新字段
        existingType.setName(deviceTypeDto.getName());
        existingType.setCode(deviceTypeDto.getCode());
        existingType.setParentId(deviceTypeDto.getParentId());
        existingType.setIcon(deviceTypeDto.getIcon());
        existingType.setProtocols(deviceTypeDto.getProtocols());
        existingType.setSortOrder(deviceTypeDto.getSortOrder());
        existingType.setDescription(deviceTypeDto.getDescription());
        existingType.setIsEnabled(deviceTypeDto.getIsEnabled());
        // 更新时间将由@LastModifiedDate自动设置

        existingType = deviceTypeRepository.save(existingType);
        return convertToDto(existingType);
    }

    @Override
    @Transactional
    public void deleteDeviceType(Long id) {
        log.info("Deleting device type with id: {}", id);

        DeviceType deviceType = deviceTypeRepository.findById(id)
                .filter(dt -> dt.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("设备类型不存在"));

        // 检查是否有子类型
        List<DeviceType> childTypes = deviceTypeRepository.findByParentId(id);
        if (!childTypes.isEmpty()) {
            throw new BusinessException("该类型下存在子类型，无法删除");
        }

        // 软删除
        deviceType.setDeletedAt(LocalDateTime.now());
        deviceTypeRepository.save(deviceType);
    }

    @Override
    @Transactional
    public void batchDeleteDeviceTypes(List<Long> ids) {
        log.info("Batch deleting device types with ids: {}", ids);

        for (Long id : ids) {
            deleteDeviceType(id);
        }
    }

    @Override
    public boolean existsByCode(String code, Long excludeId) {
        if (excludeId != null) {
            return deviceTypeRepository.existsByCodeAndIdNot(code, excludeId);
        } else {
            return deviceTypeRepository.existsByCode(code);
        }
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        if (excludeId != null) {
            return deviceTypeRepository.existsByNameAndIdNot(name, excludeId);
        } else {
            return deviceTypeRepository.existsByName(name);
        }
    }

    @Override
    public Object getDeviceTypeStats() {
        log.debug("Getting device type statistics");

        long totalTypes = deviceTypeRepository.countEnabledTypes();
        List<DeviceType> rootTypes = deviceTypeRepository.findRootTypes();

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalTypes", totalTypes);
        stats.put("rootTypes", rootTypes.size());
        stats.put("avgChildTypes", rootTypes.isEmpty() ? 0 : 
                rootTypes.stream().mapToLong(rt -> 
                        deviceTypeRepository.findByParentId(rt.getId()).size()).average().orElse(0));

        return stats;
    }

    @Override
    public List<DeviceTypeDto> getChildTypes(Long parentId) {
        log.debug("Getting child types for parent id: {}", parentId);

        List<DeviceType> childTypes = deviceTypeRepository.findByParentId(parentId);
        return childTypes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * 构建树形结构
     */
    private List<DeviceTypeDto> buildTree(List<DeviceType> types) {
        Map<Long, DeviceTypeDto> typeMap = new HashMap<>();
        List<DeviceTypeDto> rootTypes = new ArrayList<>();

        // 转换为DTO并建立映射
        for (DeviceType type : types) {
            DeviceTypeDto dto = convertToDto(type);
            dto.setChildren(new ArrayList<>());
            typeMap.put(type.getId(), dto);
        }

        // 构建树形结构
        for (DeviceType type : types) {
            DeviceTypeDto dto = typeMap.get(type.getId());
            if (type.getParentId() == null) {
                rootTypes.add(dto);
            } else {
                DeviceTypeDto parent = typeMap.get(type.getParentId());
                if (parent != null) {
                    parent.getChildren().add(dto);
                }
            }
        }

        return rootTypes;
    }

    /**
     * 实体转DTO
     */
    private DeviceTypeDto convertToDto(DeviceType entity) {
        DeviceTypeDto dto = new DeviceTypeDto();
        dto.setId(entity.getId());
        dto.setName(entity.getName());
        dto.setCode(entity.getCode());
        dto.setParentId(entity.getParentId());
        dto.setIcon(entity.getIcon());
        dto.setProtocols(parseProtocols(entity.getProtocols()));
        dto.setSortOrder(entity.getSortOrder());
        dto.setDescription(entity.getDescription());
        dto.setIsEnabled(entity.getIsEnabled());
        dto.setDeviceCount(entity.getDeviceCount());
        dto.setCreatedAt(entity.getCreatedAt() != null ? Timestamp.valueOf(entity.getCreatedAt()) : null);
        dto.setUpdatedAt(entity.getUpdatedAt() != null ? Timestamp.valueOf(entity.getUpdatedAt()) : null);
        return dto;
    }

    /**
     * DTO转实体
     */
    private DeviceType convertToEntity(DeviceTypeDto dto) {
        DeviceType entity = new DeviceType();
        entity.setName(dto.getName());
        entity.setCode(dto.getCode());
        entity.setParentId(dto.getParentId());
        entity.setIcon(dto.getIcon());
        entity.setProtocols(dto.getProtocols());
        entity.setSortOrder(dto.getSortOrder());
        entity.setDescription(dto.getDescription());
        entity.setIsEnabled(dto.getIsEnabled() != null ? dto.getIsEnabled() : true);
        entity.setDeviceCount(0);
        return entity;
    }

    /**
     * 解析协议列表
     */
    private List<String> parseProtocols(List<String> protocols) {
        return protocols == null ? new ArrayList<>() : protocols;
    }

    /**
     * 转换协议列表为JSON
     */
    // 移除未使用的旧转换方法，避免编译告警（保留 parse 方法用于空值兜底）
} 