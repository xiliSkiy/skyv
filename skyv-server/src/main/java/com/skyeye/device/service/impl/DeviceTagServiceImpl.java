package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.device.dto.DeviceTagDto;
import com.skyeye.device.entity.DeviceTag;
import com.skyeye.device.repository.DeviceTagRepository;
import com.skyeye.device.service.DeviceTagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备标签服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceTagServiceImpl implements DeviceTagService {

    private final DeviceTagRepository deviceTagRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceTagDto> getDeviceTags(String name, String description, Pageable pageable) {
        Page<DeviceTag> page = deviceTagRepository.findByConditions(name, description, pageable);
        return page.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTagDto> getAllDeviceTags() {
        List<DeviceTag> tags = deviceTagRepository.findAll();
        return tags.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceTagDto getDeviceTagById(Long id) {
        DeviceTag tag = deviceTagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备标签不存在，ID: " + id));
        return convertToDto(tag);
    }

    @Override
    public DeviceTagDto createDeviceTag(DeviceTagDto deviceTagDto) {
        // 验证名称唯一性
        if (!isNameUnique(deviceTagDto.getName(), null)) {
            throw new BusinessException("标签名称已存在");
        }

        DeviceTag tag = new DeviceTag();
        BeanUtils.copyProperties(deviceTagDto, tag);
        
        // 设置默认颜色
        if (tag.getColor() == null || tag.getColor().trim().isEmpty()) {
            tag.setColor("#409EFF");
        }
        
        tag.setCreatedAt(LocalDateTime.now());
        tag.setUsageCount(0);
        
        DeviceTag savedTag = deviceTagRepository.save(tag);
        return convertToDto(savedTag);
    }

    @Override
    public DeviceTagDto updateDeviceTag(Long id, DeviceTagDto deviceTagDto) {
        DeviceTag existingTag = deviceTagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备标签不存在，ID: " + id));

        // 验证名称唯一性
        if (!isNameUnique(deviceTagDto.getName(), id)) {
            throw new BusinessException("标签名称已存在");
        }

        BeanUtils.copyProperties(deviceTagDto, existingTag, "id", "createdAt", "createdBy", "usageCount");
        
        DeviceTag savedTag = deviceTagRepository.save(existingTag);
        return convertToDto(savedTag);
    }

    @Override
    public void deleteDeviceTag(Long id) {
        DeviceTag tag = deviceTagRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备标签不存在，ID: " + id));

        // 检查是否有设备在使用该标签
        Long deviceCount = deviceTagRepository.countDevicesByTagId(id);
        if (deviceCount > 0) {
            throw new BusinessException("该标签正在被 " + deviceCount + " 个设备使用，不能删除");
        }

        deviceTagRepository.delete(tag);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNameUnique(String name, Long id) {
        return deviceTagRepository.countByNameAndIdNot(name, id) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTagDto> getPopularTags() {
        List<DeviceTag> popularTags = deviceTagRepository.findTop10ByOrderByUsageCountDesc();
        return popularTags.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceTagDto getTagStats(Long id) {
        DeviceTagDto dto = getDeviceTagById(id);
        
        // 获取使用该标签的设备数量
        Long deviceCount = deviceTagRepository.countDevicesByTagId(id);
        dto.setUsageCount(deviceCount.intValue());
        
        return dto;
    }

    /**
     * 转换为DTO
     */
    private DeviceTagDto convertToDto(DeviceTag tag) {
        DeviceTagDto dto = new DeviceTagDto();
        BeanUtils.copyProperties(tag, dto);
        return dto;
    }
} 