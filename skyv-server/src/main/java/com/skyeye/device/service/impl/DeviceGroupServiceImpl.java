package com.skyeye.device.service.impl;

import com.skyeye.device.dto.DeviceGroupDto;
import com.skyeye.device.entity.DeviceGroup;
import com.skyeye.device.repository.DeviceGroupRepository;
import com.skyeye.device.service.DeviceGroupService;
import com.skyeye.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 设备分组服务实现类
 *
 * @author SkyEye Team
 * @version 1.0.0
 * @since 2024-01-01
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceGroupServiceImpl implements DeviceGroupService {

    private final DeviceGroupRepository deviceGroupRepository;

    @Override
    public List<DeviceGroupDto> getAllDeviceGroups() {
        log.debug("Getting all device groups");

        List<DeviceGroup> groups = deviceGroupRepository.findAllActiveGroups();
        return groups.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public DeviceGroupDto getDeviceGroupById(Long id) {
        log.debug("Getting device group by id: {}", id);

        DeviceGroup deviceGroup = deviceGroupRepository.findById(id)
                .filter(dg -> dg.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("设备分组不存在"));

        return convertToDto(deviceGroup);
    }

    @Override
    @Transactional
    public DeviceGroupDto createDeviceGroup(DeviceGroupDto deviceGroupDto) {
        log.info("Creating device group: {}", deviceGroupDto.getName());

        // 检查分组名称是否存在
        if (deviceGroupRepository.existsByName(deviceGroupDto.getName())) {
            throw new BusinessException("设备分组名称已存在");
        }

        DeviceGroup deviceGroup = convertToEntity(deviceGroupDto);
        deviceGroup.setCreatedAt(LocalDateTime.now());
        deviceGroup.setUpdatedAt(LocalDateTime.now());

        DeviceGroup savedGroup = deviceGroupRepository.save(deviceGroup);
        log.info("Device group created successfully with id: {}", savedGroup.getId());

        return convertToDto(savedGroup);
    }

    @Override
    @Transactional
    public DeviceGroupDto updateDeviceGroup(Long id, DeviceGroupDto deviceGroupDto) {
        log.info("Updating device group with id: {}", id);

        DeviceGroup existingGroup = deviceGroupRepository.findById(id)
                .filter(dg -> dg.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("设备分组不存在"));

        // 检查分组名称唯一性（排除当前分组）
        if (!existingGroup.getName().equals(deviceGroupDto.getName()) && 
            deviceGroupRepository.existsByName(deviceGroupDto.getName())) {
            throw new BusinessException("设备分组名称已存在");
        }

        // 更新分组信息
        BeanUtils.copyProperties(deviceGroupDto, existingGroup, "id", "createdAt", "createdBy");
        existingGroup.setUpdatedAt(LocalDateTime.now());

        DeviceGroup savedGroup = deviceGroupRepository.save(existingGroup);
        log.info("Device group updated successfully with id: {}", savedGroup.getId());

        return convertToDto(savedGroup);
    }

    @Override
    @Transactional
    public void deleteDeviceGroup(Long id) {
        log.info("Deleting device group with id: {}", id);

        DeviceGroup deviceGroup = deviceGroupRepository.findById(id)
                .filter(dg -> dg.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("设备分组不存在"));

        // 软删除
        deviceGroup.setDeletedAt(LocalDateTime.now());
        deviceGroupRepository.save(deviceGroup);

        log.info("Device group deleted successfully with id: {}", id);
    }

    @Override
    @Transactional
    public void batchDeleteDeviceGroups(List<Long> ids) {
        log.info("Batch deleting device groups with ids: {}", ids);

        List<DeviceGroup> groups = deviceGroupRepository.findByIdInAndNotDeleted(ids);
        if (groups.size() != ids.size()) {
            throw new BusinessException("部分设备分组不存在或已被删除");
        }

        LocalDateTime now = LocalDateTime.now();
        groups.forEach(group -> group.setDeletedAt(now));
        deviceGroupRepository.saveAll(groups);

        log.info("Batch deleted {} device groups successfully", groups.size());
    }

    @Override
    public boolean existsByName(String name, Long excludeId) {
        if (excludeId != null) {
            return deviceGroupRepository.existsByNameAndIdNot(name, excludeId);
        } else {
            return deviceGroupRepository.existsByName(name);
        }
    }

    @Override
    public List<DeviceGroupDto> getDeviceGroupsByType(String type) {
        log.debug("Getting device groups by type: {}", type);

        List<DeviceGroup> groups = deviceGroupRepository.findByType(type);
        return groups.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<DeviceGroupDto> getImportantDeviceGroups() {
        log.debug("Getting important device groups");

        List<DeviceGroup> groups = deviceGroupRepository.findByIsImportant(true);
        return groups.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Override
    public Object getDeviceGroupStats() {
        log.debug("Getting device group statistics");

        long totalGroups = deviceGroupRepository.countEnabledGroups();
        List<DeviceGroup> importantGroups = deviceGroupRepository.findByIsImportant(true);
        List<DeviceGroup> normalGroups = deviceGroupRepository.findByType("normal");
        List<DeviceGroup> smartGroups = deviceGroupRepository.findByType("smart");

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalGroups", totalGroups);
        stats.put("importantGroups", importantGroups.size());
        stats.put("normalGroups", normalGroups.size());
        stats.put("smartGroups", smartGroups.size());

        return stats;
    }

    /**
     * 实体转DTO
     */
    private DeviceGroupDto convertToDto(DeviceGroup entity) {
        DeviceGroupDto dto = new DeviceGroupDto();
        BeanUtils.copyProperties(entity, dto);
        
        // TODO: 计算在线设备数量和报警设备数量
        // 这里需要查询设备表统计相关信息
        dto.setOnlineCount(0);
        dto.setAlertCount(0);
        
        return dto;
    }

    /**
     * DTO转实体
     */
    private DeviceGroup convertToEntity(DeviceGroupDto dto) {
        DeviceGroup entity = new DeviceGroup();
        BeanUtils.copyProperties(dto, entity, "id", "onlineCount", "alertCount", "deviceIds");
        
        // 设置默认值
        if (entity.getType() == null) {
            entity.setType("normal");
        }
        if (entity.getIsImportant() == null) {
            entity.setIsImportant(false);
        }
        if (entity.getIsEnabled() == null) {
            entity.setIsEnabled(true);
        }
        if (entity.getSortOrder() == null) {
            entity.setSortOrder(0);
        }
        if (entity.getDeviceCount() == null) {
            entity.setDeviceCount(0);
        }
        
        return entity;
    }
} 