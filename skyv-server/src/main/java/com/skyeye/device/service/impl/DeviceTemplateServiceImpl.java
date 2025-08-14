package com.skyeye.device.service.impl;

import com.skyeye.common.exception.BusinessException;
import com.skyeye.device.dto.DeviceTemplateDto;
import com.skyeye.device.entity.DeviceTemplate;
import com.skyeye.device.repository.DeviceTemplateRepository;
import com.skyeye.device.repository.DeviceTypeRepository;
import com.skyeye.device.repository.DeviceProtocolRepository;
import com.skyeye.device.service.DeviceTemplateService;
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
 * 设备模板服务实现类
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class DeviceTemplateServiceImpl implements DeviceTemplateService {

    private final DeviceTemplateRepository deviceTemplateRepository;
    private final DeviceTypeRepository deviceTypeRepository;
    private final DeviceProtocolRepository deviceProtocolRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<DeviceTemplateDto> getDeviceTemplates(String name, String code, Long deviceTypeId,
                                                      Long protocolId, String manufacturer, Boolean isEnabled,
                                                      Pageable pageable) {
        Page<DeviceTemplate> page = deviceTemplateRepository.findByConditions(
                name, code, deviceTypeId, protocolId, manufacturer, isEnabled, pageable);
        return page.map(this::convertToDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTemplateDto> getAllEnabledTemplates() {
        List<DeviceTemplate> templates = deviceTemplateRepository.findByIsEnabledTrueOrderByUsageCountDesc();
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTemplateDto> getTemplatesByDeviceType(Long deviceTypeId) {
        List<DeviceTemplate> templates = deviceTemplateRepository.findByDeviceTypeIdAndIsEnabledTrue(deviceTypeId);
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DeviceTemplateDto> getTemplatesByProtocol(Long protocolId) {
        List<DeviceTemplate> templates = deviceTemplateRepository.findByProtocolIdAndIsEnabledTrue(protocolId);
        return templates.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceTemplateDto getDeviceTemplateById(Long id) {
        DeviceTemplate template = deviceTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备模板不存在，ID: " + id));
        return convertToDto(template);
    }

    @Override
    public DeviceTemplateDto createDeviceTemplate(DeviceTemplateDto deviceTemplateDto) {
        // 验证编码唯一性
        if (!isCodeUnique(deviceTemplateDto.getCode(), null)) {
            throw new BusinessException("模板编码已存在");
        }

        // 验证名称唯一性
        if (!isNameUnique(deviceTemplateDto.getName(), null)) {
            throw new BusinessException("模板名称已存在");
        }

        DeviceTemplate template = new DeviceTemplate();
        BeanUtils.copyProperties(deviceTemplateDto, template);

        // 规范化 JSONB 字段，避免空字符串写入导致 JSON 解析错误
        sanitizeJsonFields(template);

        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        template.setUsageCount(0);
        
        if (template.getIsEnabled() == null) {
            template.setIsEnabled(true);
        }

        DeviceTemplate savedTemplate = deviceTemplateRepository.save(template);
        return convertToDto(savedTemplate);
    }

    @Override
    public DeviceTemplateDto updateDeviceTemplate(Long id, DeviceTemplateDto deviceTemplateDto) {
        DeviceTemplate existingTemplate = deviceTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备模板不存在，ID: " + id));

        // 验证编码唯一性
        if (!isCodeUnique(deviceTemplateDto.getCode(), id)) {
            throw new BusinessException("模板编码已存在");
        }

        // 验证名称唯一性
        if (!isNameUnique(deviceTemplateDto.getName(), id)) {
            throw new BusinessException("模板名称已存在");
        }

        BeanUtils.copyProperties(deviceTemplateDto, existingTemplate, "id", "createdAt", "usageCount");
        // 规范化 JSONB 字段
        sanitizeJsonFields(existingTemplate);
        existingTemplate.setUpdatedAt(LocalDateTime.now());

        DeviceTemplate savedTemplate = deviceTemplateRepository.save(existingTemplate);
        return convertToDto(savedTemplate);
    }

    @Override
    public void deleteDeviceTemplate(Long id) {
        DeviceTemplate template = deviceTemplateRepository.findById(id)
                .orElseThrow(() -> new BusinessException("设备模板不存在，ID: " + id));

        // 检查是否有设备在使用该模板
        Long deviceCount = deviceTemplateRepository.countDevicesByTemplateId(id);
        if (deviceCount > 0) {
            throw new BusinessException("该模板正在被 " + deviceCount + " 个设备使用，不能删除");
        }

        deviceTemplateRepository.delete(template);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isCodeUnique(String code, Long id) {
        return deviceTemplateRepository.countByCodeAndIdNot(code, id) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isNameUnique(String name, Long id) {
        return deviceTemplateRepository.countByNameAndIdNot(name, id) == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public DeviceTemplateDto getTemplateStats(Long id) {
        DeviceTemplateDto dto = getDeviceTemplateById(id);
        
        // 获取使用该模板的设备数量
        Long deviceCount = deviceTemplateRepository.countDevicesByTemplateId(id);
        dto.setUsageCount(deviceCount.intValue());
        
        return dto;
    }

    /**
     * 将 JSONB 字段的空字符串规范为 null，避免 PostgreSQL json 解析错误
     */
    private void sanitizeJsonFields(DeviceTemplate template) {
        if (template.getConfigTemplate() != null && template.getConfigTemplate().trim().isEmpty()) {
            template.setConfigTemplate(null);
        }
        if (template.getDefaultSettings() != null && template.getDefaultSettings().trim().isEmpty()) {
            template.setDefaultSettings(null);
        }
        if (template.getMetrics() != null && template.getMetrics().trim().isEmpty()) {
            template.setMetrics(null);
        }
    }

    /**
     * 转换为DTO
     */
    private DeviceTemplateDto convertToDto(DeviceTemplate template) {
        DeviceTemplateDto dto = new DeviceTemplateDto();
        BeanUtils.copyProperties(template, dto);

        // 获取设备类型名称
        if (template.getDeviceTypeId() != null) {
            deviceTypeRepository.findById(template.getDeviceTypeId())
                    .ifPresent(deviceType -> dto.setDeviceTypeName(deviceType.getName()));
        }

        // 获取协议名称
        if (template.getProtocolId() != null) {
            deviceProtocolRepository.findById(template.getProtocolId())
                    .ifPresent(protocol -> dto.setProtocolName(protocol.getName()));
        }

        return dto;
    }
} 