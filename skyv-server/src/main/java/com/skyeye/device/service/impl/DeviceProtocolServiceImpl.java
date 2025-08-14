package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.device.dto.DeviceProtocolDto;
import com.skyeye.device.entity.DeviceProtocol;
import com.skyeye.device.repository.DeviceProtocolRepository;
import com.skyeye.device.service.DeviceProtocolService;
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
 * 设备协议服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceProtocolServiceImpl implements DeviceProtocolService {

    private final DeviceProtocolRepository deviceProtocolRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceProtocolDto> getDeviceProtocols(String name, String code, String version,
                                                      Boolean isEnabled, Pageable pageable) {
        Page<DeviceProtocol> page = deviceProtocolRepository.findByConditions(name, code, version, isEnabled, pageable);
        return page.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceProtocolDto> getAllEnabledProtocols() {
        List<DeviceProtocol> protocols = deviceProtocolRepository.findByIsEnabledTrueOrderByUsageCountDesc();
        return protocols.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceProtocolDto getDeviceProtocolById(Long id) {
        DeviceProtocol protocol = deviceProtocolRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备协议不存在，ID: " + id));
        return convertToDto(protocol);
    }

    @Override
    public DeviceProtocolDto createDeviceProtocol(DeviceProtocolDto deviceProtocolDto) {
        // 验证编码唯一性
        if (!isCodeUnique(deviceProtocolDto.getCode(), null)) {
            throw new BusinessException("协议编码已存在");
        }

        // 验证名称唯一性
        if (!isNameUnique(deviceProtocolDto.getName(), null)) {
            throw new BusinessException("协议名称已存在");
        }

        DeviceProtocol protocol = new DeviceProtocol();
        BeanUtils.copyProperties(deviceProtocolDto, protocol);

        protocol.setCreatedAt(LocalDateTime.now());
        protocol.setUpdatedAt(LocalDateTime.now());
        protocol.setUsageCount(0);
        
        if (protocol.getIsEnabled() == null) {
            protocol.setIsEnabled(true);
        }

        DeviceProtocol savedProtocol = deviceProtocolRepository.save(protocol);
        return convertToDto(savedProtocol);
    }

    @Override
    public DeviceProtocolDto updateDeviceProtocol(Long id, DeviceProtocolDto deviceProtocolDto) {
        DeviceProtocol existingProtocol = deviceProtocolRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备协议不存在，ID: " + id));

        // 验证编码唯一性
        if (!isCodeUnique(deviceProtocolDto.getCode(), id)) {
            throw new BusinessException("协议编码已存在");
        }

        // 验证名称唯一性
        if (!isNameUnique(deviceProtocolDto.getName(), id)) {
            throw new BusinessException("协议名称已存在");
        }

        BeanUtils.copyProperties(deviceProtocolDto, existingProtocol, "id", "createdAt", "usageCount");
        existingProtocol.setUpdatedAt(LocalDateTime.now());

        DeviceProtocol savedProtocol = deviceProtocolRepository.save(existingProtocol);
        return convertToDto(savedProtocol);
    }

    @Override
    public void deleteDeviceProtocol(Long id) {
        DeviceProtocol protocol = deviceProtocolRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备协议不存在，ID: " + id));

        // 检查是否有设备在使用该协议
        Long deviceCount = deviceProtocolRepository.countDevicesByProtocolId(id);
        if (deviceCount > 0) {
            throw new BusinessException("该协议正在被 " + deviceCount + " 个设备使用，不能删除");
        }

        // 检查是否有模板在使用该协议
        Long templateCount = deviceProtocolRepository.countTemplatesByProtocolId(id);
        if (templateCount > 0) {
            throw new BusinessException("该协议正在被 " + templateCount + " 个模板使用，不能删除");
        }

        deviceProtocolRepository.delete(protocol);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodeUnique(String code, Long id) {
        return deviceProtocolRepository.countByCodeAndIdNot(code, id) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNameUnique(String name, Long id) {
        return deviceProtocolRepository.countByNameAndIdNot(name, id) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceProtocolDto getProtocolStats(Long id) {
        DeviceProtocolDto dto = getDeviceProtocolById(id);
        
        // 获取使用该协议的设备数量
        Long deviceCount = deviceProtocolRepository.countDevicesByProtocolId(id);
        Long templateCount = deviceProtocolRepository.countTemplatesByProtocolId(id);
        dto.setUsageCount(Math.toIntExact(deviceCount + templateCount));
        
        return dto;
    }

    /**
     * 转换为DTO
     */
    private DeviceProtocolDto convertToDto(DeviceProtocol protocol) {
        DeviceProtocolDto dto = new DeviceProtocolDto();
        BeanUtils.copyProperties(protocol, dto);
        return dto;
    }
} 