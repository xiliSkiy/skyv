package com.skyeye.scheduler.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.skyeye.auth.entity.User;
import com.skyeye.auth.repository.UserRepository;
import com.skyeye.auth.service.AuthService;
import com.skyeye.device.entity.Device;
import com.skyeye.device.repository.DeviceRepository;
import com.skyeye.metrics.entity.Metric;
import com.skyeye.metrics.repository.MetricRepository;
import com.skyeye.scheduler.dto.ApplyTemplateRequest;
import com.skyeye.scheduler.dto.MetricDTO;
import com.skyeye.scheduler.dto.MetricTemplateDTO;
import com.skyeye.scheduler.dto.MetricTemplateApplicationDTO;
import com.skyeye.scheduler.entity.MetricTemplate;
import com.skyeye.scheduler.entity.MetricTemplateApplication;
import com.skyeye.scheduler.entity.TemplateMetricRelation;
import com.skyeye.scheduler.repository.MetricTemplateApplicationRepository;
import com.skyeye.scheduler.repository.MetricTemplateRepository;
import com.skyeye.scheduler.repository.TemplateMetricRelationRepository;
import com.skyeye.scheduler.service.MetricTemplateService;

/**
 * 指标模板服务实现类
 */
@Service
public class MetricTemplateServiceImpl implements MetricTemplateService {
    
    @Autowired
    private MetricTemplateRepository metricTemplateRepository;
    
    @Autowired
    private MetricRepository metricRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private MetricTemplateApplicationRepository applicationRepository;
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    @Autowired
    private TemplateMetricRelationRepository templateMetricRelationRepository;
    
    @Autowired(required = false)
    private AuthService authService;
    
    @Override
    public Page<MetricTemplateDTO> findByConditions(String templateName, String metricType, 
            String category, String deviceType, Boolean isSystem, Pageable pageable) {
        
        // 构建查询条件
        Specification<MetricTemplate> spec = (root, query, criteriaBuilder) -> {
            List<javax.persistence.criteria.Predicate> predicates = new ArrayList<>();
            
            if (StringUtils.hasText(templateName)) {
                predicates.add(criteriaBuilder.like(root.get("templateName"), "%" + templateName + "%"));
            }
            
            if (StringUtils.hasText(metricType)) {
                predicates.add(criteriaBuilder.equal(root.get("metricType"), metricType));
            }
            
            if (StringUtils.hasText(category)) {
                predicates.add(criteriaBuilder.equal(root.get("category"), category));
            }
            
            if (StringUtils.hasText(deviceType)) {
                predicates.add(criteriaBuilder.equal(root.get("deviceType"), deviceType));
            }
            
            if (isSystem != null) {
                predicates.add(criteriaBuilder.equal(root.get("isSystem"), isSystem));
            }
            
            return criteriaBuilder.and(predicates.toArray(new javax.persistence.criteria.Predicate[0]));
        };
        
        Page<MetricTemplate> page = metricTemplateRepository.findAll(spec, pageable);
        List<MetricTemplateDTO> dtoList = page.getContent().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
        
        return new PageImpl<>(dtoList, pageable, page.getTotalElements());
    }
    
    @Override
    public List<MetricTemplateDTO> findAll() {
        List<MetricTemplate> templates = metricTemplateRepository.findAll();
        return templates.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    @Override
    public MetricTemplateDTO findById(Long id) {
        Optional<MetricTemplate> optional = metricTemplateRepository.findById(id);
        return optional.map(this::convertToDTO).orElse(null);
    }
    
    @Override
    @Transactional
    public MetricTemplateDTO create(MetricTemplateDTO templateDTO) {
        MetricTemplate template = new MetricTemplate();
        BeanUtils.copyProperties(templateDTO, template);
        
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        
        if (template.getIsSystem() == null) {
            template.setIsSystem(false);
        }
        
        // 获取当前用户
        try {
            if (authService != null) {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    template.setCreatedBy(currentUser.getId());
                }
            }
        } catch (Exception e) {
            // 忽略获取当前用户错误
        }
        
        MetricTemplate saved = metricTemplateRepository.save(template);
        
        // 保存模板与指标的关联关系
        if (templateDTO.getMetricIds() != null && !templateDTO.getMetricIds().isEmpty()) {
            List<TemplateMetricRelation> relations = templateDTO.getMetricIds().stream()
                .map(metricId -> TemplateMetricRelation.builder()
                        .templateId(saved.getId())
                        .metricId(metricId)
                        .build())
                .collect(Collectors.toList());
            
            templateMetricRelationRepository.saveAll(relations);
        }
        
        return convertToDTO(saved);
    }
    
    @Override
    @Transactional
    public MetricTemplateDTO update(MetricTemplateDTO templateDTO) {
        Optional<MetricTemplate> optional = metricTemplateRepository.findById(templateDTO.getId());
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("指标模板不存在");
        }
        
        MetricTemplate template = optional.get();
        
        // 系统内置模板不允许修改某些字段
        if (template.getIsSystem() != null && template.getIsSystem()) {
            templateDTO.setIsSystem(true);
            templateDTO.setCategory(template.getCategory());
            templateDTO.setMetricType(template.getMetricType());
        }
        
        // 确保DTO中的关键属性不为null
        if (templateDTO.getTemplateName() == null) {
            templateDTO.setTemplateName(template.getTemplateName());
        }
        if (templateDTO.getCategory() == null) {
            templateDTO.setCategory(template.getCategory());
        }
        if (templateDTO.getMetricType() == null) {
            templateDTO.setMetricType(template.getMetricType());
        }
        if (templateDTO.getDeviceType() == null) {
            templateDTO.setDeviceType(template.getDeviceType());
        }
        if (templateDTO.getIsSystem() == null) {
            templateDTO.setIsSystem(template.getIsSystem() != null ? template.getIsSystem() : false);
        }
        
        BeanUtils.copyProperties(templateDTO, template);
        template.setUpdatedAt(LocalDateTime.now());
        
        MetricTemplate updated = metricTemplateRepository.save(template);
        
        // 更新模板与指标的关联关系
        if (templateDTO.getMetricIds() != null) {
            // 删除原有关联
            templateMetricRelationRepository.deleteByTemplateId(updated.getId());
            
            // 创建新的关联
            if (!templateDTO.getMetricIds().isEmpty()) {
                List<TemplateMetricRelation> relations = templateDTO.getMetricIds().stream()
                    .map(metricId -> TemplateMetricRelation.builder()
                            .templateId(updated.getId())
                            .metricId(metricId)
                            .build())
                    .collect(Collectors.toList());
                
                templateMetricRelationRepository.saveAll(relations);
            }
        }
        
        return convertToDTO(updated);
    }
    
    @Override
    @Transactional
    public void deleteById(Long id) {
        Optional<MetricTemplate> optional = metricTemplateRepository.findById(id);
        if (!optional.isPresent()) {
            return;
        }
        
        MetricTemplate template = optional.get();
        // 系统内置模板不允许删除
        if (template.getIsSystem()) {
            throw new IllegalArgumentException("系统内置模板不允许删除");
        }
        
        // 删除关联关系
        templateMetricRelationRepository.deleteByTemplateId(id);
        
        metricTemplateRepository.deleteById(id);
    }
    
    @Override
    @Transactional
    public void deleteByIds(List<Long> ids) {
        // 过滤掉系统内置模板
        List<MetricTemplate> templates = metricTemplateRepository.findAllById(ids);
        List<MetricTemplate> toDelete = templates.stream()
                .filter(t -> !t.getIsSystem())
                .collect(Collectors.toList());
        
        // 删除关联关系
        for (MetricTemplate template : toDelete) {
            templateMetricRelationRepository.deleteByTemplateId(template.getId());
        }
        
        metricTemplateRepository.deleteAll(toDelete);
    }
    
    @Override
    @Transactional
    public Long createMetricFromTemplate(Long templateId) {
        Optional<MetricTemplate> optional = metricTemplateRepository.findById(templateId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("指标模板不存在");
        }
        
        MetricTemplate template = optional.get();
        Metric metric = new Metric();
        
        // 设置基本字段
        metric.setMetricName(template.getTemplateName());
        metric.setMetricKey("metric_" + System.currentTimeMillis());
        metric.setMetricType(template.getMetricType());
        metric.setDescription(template.getDescription());
        metric.setApplicableDeviceType(template.getDeviceType());
        
        // 从模板参数中解析其他字段
        try {
            if (StringUtils.hasText(template.getDefaultParams())) {
                Map<String, Object> params = objectMapper.readValue(template.getDefaultParams(), Map.class);
                
                if (params.containsKey("collectionMethod")) {
                    metric.setCollectionMethod(params.get("collectionMethod").toString());
                }
                
                if (params.containsKey("collectionInterval")) {
                    metric.setCollectionInterval(Integer.valueOf(params.get("collectionInterval").toString()));
                }
                
                if (params.containsKey("dataType")) {
                    metric.setDataType(params.get("dataType").toString());
                }
                
                if (params.containsKey("unit")) {
                    metric.setUnit(params.get("unit").toString());
                }
                
                // 其他字段...
            }
        } catch (Exception e) {
            throw new RuntimeException("解析模板参数失败", e);
        }
        
        // 设置默认值
        metric.setStatus(0);
        metric.setCreatedAt(LocalDateTime.now());
        metric.setUpdatedAt(LocalDateTime.now());
        
        Metric saved = metricRepository.save(metric);
        return saved.getId();
    }
    
    @Override
    @Transactional
    public Long createTemplateFromMetric(Long metricId, String templateName) {
        Optional<Metric> optional = metricRepository.findById(metricId);
        if (!optional.isPresent()) {
            throw new IllegalArgumentException("指标不存在");
        }
        
        Metric metric = optional.get();
        MetricTemplate template = new MetricTemplate();
        
        // 设置基本字段
        template.setTemplateName(templateName);
        template.setDescription(metric.getDescription());
        template.setMetricType(metric.getMetricType());
        template.setDeviceType(metric.getApplicableDeviceType());
        
        // 根据指标类型设置分类
        if (metric.getMetricType() != null) {
            if (metric.getMetricType().startsWith("video")) {
                template.setCategory(MetricTemplateDTO.CATEGORY_VIDEO);
            } else if (metric.getMetricType().startsWith("sensor")) {
                template.setCategory(MetricTemplateDTO.CATEGORY_SENSOR);
            } else if (metric.getMetricType().startsWith("system")) {
                template.setCategory(MetricTemplateDTO.CATEGORY_SYSTEM);
            } else {
                template.setCategory(MetricTemplateDTO.CATEGORY_CUSTOM);
            }
        } else {
            template.setCategory(MetricTemplateDTO.CATEGORY_CUSTOM);
        }
        
        // 构建默认参数
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("collectionMethod", metric.getCollectionMethod());
            params.put("collectionInterval", metric.getCollectionInterval());
            params.put("dataType", metric.getDataType());
            params.put("unit", metric.getUnit());
            
            // 其他参数...
            
            template.setDefaultParams(objectMapper.writeValueAsString(params));
        } catch (Exception e) {
            throw new RuntimeException("构建模板参数失败", e);
        }
        
        template.setIsSystem(false);
        template.setCreatedBy(metric.getCreatedBy());
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        
        MetricTemplate saved = metricTemplateRepository.save(template);
        
        // 创建模板与指标的关联关系
        TemplateMetricRelation relation = TemplateMetricRelation.builder()
                .templateId(saved.getId())
                .metricId(metricId)
                .build();
        templateMetricRelationRepository.save(relation);
        
        return saved.getId();
    }
    
    @Override
    @PostConstruct
    @Transactional
    public void initSystemTemplates() {
        // 检查是否已存在系统模板
        List<MetricTemplate> systemTemplates = metricTemplateRepository.findByIsSystem(true);
        if (!systemTemplates.isEmpty()) {
            return; // 已初始化过，跳过
        }
        
        // 创建一些系统内置模板
        createSystemTemplate("CPU使用率监控", "监控设备CPU使用率", "system_perf", 
                MetricTemplateDTO.CATEGORY_SYSTEM, "server", createCpuTemplateParams());
        
        createSystemTemplate("内存使用率监控", "监控设备内存使用率", "system_perf", 
                MetricTemplateDTO.CATEGORY_SYSTEM, "server", createMemoryTemplateParams());
        
        createSystemTemplate("视频流检测", "监控视频流状态", "video_status", 
                MetricTemplateDTO.CATEGORY_VIDEO, "camera", createVideoTemplateParams());
        
        createSystemTemplate("运动检测", "检测视频中的运动目标", "video_analysis", 
                MetricTemplateDTO.CATEGORY_VIDEO, "camera", createMotionTemplateParams());
    }
    
    /**
     * 创建系统内置模板
     */
    private void createSystemTemplate(String name, String description, String metricType, 
            String category, String deviceType, String params) {
        
        MetricTemplate template = new MetricTemplate();
        template.setTemplateName(name);
        template.setDescription(description);
        template.setMetricType(metricType);
        template.setCategory(category);
        template.setDeviceType(deviceType);
        template.setDefaultParams(params);
        template.setIsSystem(true);
        template.setCreatedAt(LocalDateTime.now());
        template.setUpdatedAt(LocalDateTime.now());
        
        metricTemplateRepository.save(template);
    }
    
    /**
     * 创建CPU模板参数
     */
    private String createCpuTemplateParams() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("collectionMethod", "snmp");
            params.put("collectionInterval", 60);
            params.put("dataType", "number");
            params.put("unit", "%");
            params.put("protocolConfig", "{\"oid\":\"1.3.6.1.4.1.2021.11.9.0\"}");
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * 创建内存模板参数
     */
    private String createMemoryTemplateParams() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("collectionMethod", "snmp");
            params.put("collectionInterval", 60);
            params.put("dataType", "number");
            params.put("unit", "%");
            params.put("protocolConfig", "{\"oid\":\"1.3.6.1.4.1.2021.4.6.0\"}");
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * 创建视频流模板参数
     */
    private String createVideoTemplateParams() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("collectionMethod", "http");
            params.put("collectionInterval", 30);
            params.put("dataType", "boolean");
            params.put("protocolConfig", "{\"url\":\"${streamUrl}\",\"method\":\"GET\"}");
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * 创建运动检测模板参数
     */
    private String createMotionTemplateParams() {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("collectionMethod", "ai");
            params.put("collectionInterval", 5);
            params.put("dataType", "json");
            params.put("processingScript", "function process(data) {\n  return data.objects.length > 0;\n}");
            return objectMapper.writeValueAsString(params);
        } catch (Exception e) {
            return "{}";
        }
    }
    
    /**
     * 将实体转换为DTO
     */
    private MetricTemplateDTO convertToDTO(MetricTemplate template) {
        if (template == null) {
            return null;
        }
        
        MetricTemplateDTO dto = new MetricTemplateDTO();
        BeanUtils.copyProperties(template, dto);
        
        // 查询创建人信息
        if (template.getCreatedBy() != null) {
            Optional<User> userOptional = userRepository.findById(template.getCreatedBy());
            userOptional.ifPresent(user -> dto.setCreatedByName(user.getUsername()));
        }
        
        // 查询模板关联的指标列表
        List<TemplateMetricRelation> relations = templateMetricRelationRepository.findByTemplateId(template.getId());
        if (relations != null && !relations.isEmpty()) {
            List<Long> metricIds = relations.stream()
                .map(TemplateMetricRelation::getMetricId)
                .filter(id -> id != null)  // 过滤掉null的ID
                .collect(Collectors.toList());
            
            if (!metricIds.isEmpty()) {
                List<Metric> metrics = metricRepository.findAllById(metricIds);
                List<MetricDTO> metricDTOs = metrics.stream()
                    .filter(m -> m != null)  // 过滤掉null的Metric
                    .map(this::convertMetricToDTO)
                    .filter(m -> m != null)  // 过滤掉转换失败的DTO
                    .collect(Collectors.toList());
                
                dto.setMetrics(metricDTOs);
                dto.setMetricCount(metricDTOs.size());
            } else {
                dto.setMetrics(new ArrayList<>());
                dto.setMetricCount(0);
            }
        } else {
            dto.setMetrics(new ArrayList<>());
            dto.setMetricCount(0);
        }
        
        // 查询模板使用次数
        try {
            long usageCount = applicationRepository.countByTemplateId(template.getId());
            dto.setUsageCount((int) usageCount);
        } catch (Exception e) {
            dto.setUsageCount(0);
            // 忽略统计错误
        }
        
        return dto;
    }
    
    /**
     * 将指标实体转换为DTO
     */
    private MetricDTO convertMetricToDTO(Metric metric) {
        if (metric == null) {
            return null;
        }
        
        MetricDTO dto = new MetricDTO();
        BeanUtils.copyProperties(metric, dto);
        return dto;
    }
    
    /**
     * 应用指标模板到设备
     * 
     * @param templateId 模板ID
     * @param request 应用请求参数
     * @return 应用历史ID
     */
    @Override
    @Transactional
    public Long applyTemplate(Long templateId, ApplyTemplateRequest request) {
        // 获取模板信息
        MetricTemplate template = metricTemplateRepository.findById(templateId)
            .orElseThrow(() -> new IllegalArgumentException("指标模板不存在"));
        
        // 获取应用用户
        Long currentUserId = null;
        String currentUsername = "系统";
        
        try {
            if (authService != null) {
                User currentUser = authService.getCurrentUser();
                if (currentUser != null) {
                    currentUserId = currentUser.getId();
                    currentUsername = currentUser.getUsername();
                }
            }
        } catch (Exception e) {
            // 忽略获取当前用户错误
        }
        
        // 确定设备列表
        List<Device> deviceList = new ArrayList<>();
        if ("all".equals(request.getApplyMode())) {
            // 应用到所有适用类型设备
            if (StringUtils.hasText(template.getDeviceType())) {
                deviceList = deviceRepository.findByTypeAndStatus(template.getDeviceType(), 1);
            } else {
                deviceList = deviceRepository.findByStatus(1);
            }
        } else {
            // 应用到选定设备
            if (request.getDeviceIds() != null && !request.getDeviceIds().isEmpty()) {
                deviceList = deviceRepository.findAllById(request.getDeviceIds());
            }
        }
        
        if (deviceList.isEmpty()) {
            throw new IllegalArgumentException("没有找到适用的设备");
        }
        
        // 应用模板到设备
        int successCount = 0;
        List<String> errors = new ArrayList<>();
        
        try {
            // 从模板参数中获取指标配置
            Map<String, Object> templateParams = new HashMap<>();
            if (StringUtils.hasText(template.getDefaultParams())) {
                templateParams = objectMapper.readValue(
                    template.getDefaultParams(), 
                    new TypeReference<Map<String, Object>>() {}
                );
            }
            
            // 为每个设备创建或更新指标
            for (Device device : deviceList) {
                try {
                    // TODO: 根据模板为设备创建或更新指标
                    // 这里是简化的实现，实际应用中需要根据项目需求进行更详细的实现
                    
                    // 检查设备是否已经有该类型的指标配置
                    // 根据更新策略决定是合并还是覆盖
                    
                    successCount++;
                } catch (Exception e) {
                    errors.add("设备 " + device.getName() + " 应用失败: " + e.getMessage());
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("应用模板失败: " + e.getMessage(), e);
        }
        
        // 创建应用记录
        String status = (successCount == deviceList.size()) ? "SUCCESS" : 
                        (successCount > 0) ? "PARTIAL" : "FAILED";
        
        MetricTemplateApplication application = MetricTemplateApplication.builder()
            .templateId(template.getId())
            .templateName(template.getTemplateName())
            .applyTime(LocalDateTime.now())
            .applyUserId(currentUserId)
            .applyUser(currentUsername)
            .affectedCount(successCount)
            .status(status)
            .resultInfo(String.join("\n", errors))
            .build();
        
        try {
            // 保存设备ID列表
            List<Long> deviceIds = deviceList.stream()
                .map(Device::getId)
                .collect(Collectors.toList());
            application.setDeviceIds(objectMapper.writeValueAsString(deviceIds));
        } catch (JsonProcessingException e) {
            application.setDeviceIds("[]");
        }
        
        MetricTemplateApplication saved = applicationRepository.save(application);
        return saved.getId();
    }
    
    /**
     * 获取模板应用历史
     * 
     * @param templateId 模板ID
     * @return 应用历史列表
     */
    @Override
    public List<MetricTemplateApplicationDTO> getApplicationHistory(Long templateId) {
        List<MetricTemplateApplication> applications = 
            applicationRepository.findByTemplateIdOrderByApplyTimeDesc(templateId);
        
        return applications.stream()
            .map(this::convertApplicationToDTO)
            .collect(Collectors.toList());
    }
    
    /**
     * 将应用历史实体转换为DTO
     */
    private MetricTemplateApplicationDTO convertApplicationToDTO(MetricTemplateApplication application) {
        MetricTemplateApplicationDTO dto = new MetricTemplateApplicationDTO();
        BeanUtils.copyProperties(application, dto);
        
        // 转换设备ID列表
        if (StringUtils.hasText(application.getDeviceIds())) {
            try {
                List<Long> deviceIds = objectMapper.readValue(
                    application.getDeviceIds(), 
                    new TypeReference<List<Long>>() {}
                );
                dto.setDeviceIds(deviceIds);
            } catch (Exception e) {
                dto.setDeviceIds(new ArrayList<>());
            }
        } else {
            dto.setDeviceIds(new ArrayList<>());
        }
        
        return dto;
    }
}