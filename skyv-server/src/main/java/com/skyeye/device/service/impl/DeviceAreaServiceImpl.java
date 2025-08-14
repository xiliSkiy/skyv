package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.device.dto.DeviceAreaDto;
import com.skyeye.device.entity.DeviceArea;
import com.skyeye.device.repository.DeviceAreaRepository;
import com.skyeye.device.service.DeviceAreaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 设备区域服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceAreaServiceImpl implements DeviceAreaService {

    private final DeviceAreaRepository deviceAreaRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceAreaDto> getDeviceAreas(String name, String code, Long parentId, 
                                              Integer level, Pageable pageable) {
        Page<DeviceArea> page = deviceAreaRepository.findByConditions(name, code, parentId, level, pageable);
        return page.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceAreaDto> getDeviceAreaTree() {
        List<DeviceArea> allAreas = deviceAreaRepository.findAll();
        Map<Long, DeviceAreaDto> areaMap = allAreas.stream()
                .collect(Collectors.toMap(
                        DeviceArea::getId,
                        this::convertToDto
                ));

        List<DeviceAreaDto> rootAreas = new ArrayList<>();
        
        for (DeviceAreaDto area : areaMap.values()) {
            if (area.getParentId() == null) {
                rootAreas.add(area);
            } else {
                DeviceAreaDto parent = areaMap.get(area.getParentId());
                if (parent != null) {
                    if (parent.getChildren() == null) {
                        parent.setChildren(new ArrayList<>());
                    }
                    parent.getChildren().add(area);
                }
            }
        }

        return rootAreas;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceAreaDto getDeviceAreaById(Long id) {
        DeviceArea area = deviceAreaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备区域不存在，ID: " + id));
        
        DeviceAreaDto dto = convertToDto(area);
        
        // 获取设备数量
        Long deviceCount = deviceAreaRepository.countDevicesByAreaId(id);
        dto.setDeviceCount(deviceCount.intValue());
        
        return dto;
    }

    @Override
    public DeviceAreaDto createDeviceArea(DeviceAreaDto deviceAreaDto) {
        // 验证名称唯一性
        if (!isNameUnique(deviceAreaDto.getName(), null)) {
            throw new BusinessException("区域名称已存在");
        }

        // 验证编码唯一性（如果提供了编码）
        if (StringUtils.hasText(deviceAreaDto.getCode()) && 
            !isCodeUnique(deviceAreaDto.getCode(), null)) {
            throw new BusinessException("区域编码已存在");
        }

        DeviceArea area = new DeviceArea();
        BeanUtils.copyProperties(deviceAreaDto, area);
        
        // 设置层级和完整路径
        setLevelAndFullPath(area);
        
        area.setCreatedAt(LocalDateTime.now());
        area.setUpdatedAt(LocalDateTime.now());
        
        DeviceArea savedArea = deviceAreaRepository.save(area);
        return convertToDto(savedArea);
    }

    @Override
    public DeviceAreaDto updateDeviceArea(Long id, DeviceAreaDto deviceAreaDto) {
        DeviceArea existingArea = deviceAreaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备区域不存在，ID: " + id));

        // 验证名称唯一性
        if (!isNameUnique(deviceAreaDto.getName(), id)) {
            throw new BusinessException("区域名称已存在");
        }

        // 验证编码唯一性（如果提供了编码）
        if (StringUtils.hasText(deviceAreaDto.getCode()) && 
            !isCodeUnique(deviceAreaDto.getCode(), id)) {
            throw new BusinessException("区域编码已存在");
        }

        // 检查是否会造成循环引用
        if (deviceAreaDto.getParentId() != null && 
            wouldCreateCircularReference(id, deviceAreaDto.getParentId())) {
            throw new BusinessException("不能将区域设置为自己或子区域的父区域");
        }

        BeanUtils.copyProperties(deviceAreaDto, existingArea, "id", "createdAt", "createdBy");
        
        // 重新计算层级和完整路径
        setLevelAndFullPath(existingArea);
        
        existingArea.setUpdatedAt(LocalDateTime.now());
        
        DeviceArea savedArea = deviceAreaRepository.save(existingArea);
        return convertToDto(savedArea);
    }

    @Override
    public void deleteDeviceArea(Long id) {
        DeviceArea area = deviceAreaRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备区域不存在，ID: " + id));

        // 检查是否有子区域
        List<DeviceArea> children = deviceAreaRepository.findByParentIdOrderBySortOrderAsc(id);
        if (!children.isEmpty()) {
            throw new BusinessException("该区域下还有子区域，不能删除");
        }

        // 检查是否有设备
        Long deviceCount = deviceAreaRepository.countDevicesByAreaId(id);
        if (deviceCount > 0) {
            throw new BusinessException("该区域下还有设备，不能删除");
        }

        deviceAreaRepository.delete(area);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNameUnique(String name, Long id) {
        return deviceAreaRepository.countByNameAndIdNot(name, id) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodeUnique(String code, Long id) {
        if (!StringUtils.hasText(code)) {
            return true;
        }
        return deviceAreaRepository.countByCodeAndIdNot(code, id) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceAreaDto getAreaStats(Long id) {
        DeviceAreaDto dto = getDeviceAreaById(id);
        
        // 获取子区域数量
        List<DeviceArea> children = deviceAreaRepository.findByParentIdOrderBySortOrderAsc(id);
        dto.setChildren(children.stream().map(this::convertToDto).collect(Collectors.toList()));
        
        return dto;
    }

    /**
     * 设置层级和完整路径
     */
    private void setLevelAndFullPath(DeviceArea area) {
        if (area.getParentId() == null) {
            area.setLevel(0);
            area.setFullPath(area.getName());
        } else {
            DeviceArea parent = deviceAreaRepository.findById(area.getParentId())
                    .orElseThrow(() -> new BusinessException("父区域不存在"));
            area.setLevel(parent.getLevel() + 1);
            area.setFullPath(parent.getFullPath() + "/" + area.getName());
        }
    }

    /**
     * 检查是否会造成循环引用
     */
    private boolean wouldCreateCircularReference(Long currentId, Long newParentId) {
        if (currentId.equals(newParentId)) {
            return true;
        }
        
        List<Long> childIds = deviceAreaRepository.findChildAreaIds(currentId);
        return childIds.contains(newParentId);
    }

    /**
     * 转换为DTO
     */
    private DeviceAreaDto convertToDto(DeviceArea area) {
        DeviceAreaDto dto = new DeviceAreaDto();
        BeanUtils.copyProperties(area, dto);
        
        // 获取父区域名称
        if (area.getParentId() != null) {
            deviceAreaRepository.findById(area.getParentId())
                    .ifPresent(parent -> dto.setParentName(parent.getName()));
        }
        
        return dto;
    }
} 