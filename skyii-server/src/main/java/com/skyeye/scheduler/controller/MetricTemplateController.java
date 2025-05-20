package com.skyeye.scheduler.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.skyeye.common.response.ApiResponse;
import com.skyeye.scheduler.dto.ApplyTemplateRequest;
import com.skyeye.scheduler.dto.MetricTemplateDTO;
import com.skyeye.scheduler.dto.MetricTemplateApplicationDTO;
import com.skyeye.scheduler.entity.MetricTemplate;
import com.skyeye.scheduler.service.MetricTemplateService;

/**
 * 指标模板控制器
 */
@RestController
@RequestMapping("/v1/metric-templates")
public class MetricTemplateController {
    
    @Autowired
    private MetricTemplateService metricTemplateService;
    
    /**
     * 获取指标模板列表
     * @param templateName 模板名称（模糊查询）
     * @param metricType 指标类型
     * @param category 模板分类
     * @param deviceType 适用设备类型
     * @param isSystem 是否系统内置
     * @param pageable 分页参数
     * @return 指标模板分页列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<MetricTemplateDTO>>> getMetricTemplates(
            @RequestParam(required = false) String templateName,
            @RequestParam(required = false) String metricType,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) Boolean isSystem,
            @PageableDefault(size = 10) Pageable pageable) {
        
        Page<MetricTemplateDTO> page = metricTemplateService.findByConditions(
                templateName, metricType, category, deviceType, isSystem, pageable);
        
        return ResponseEntity.ok(ApiResponse.success(page));
    }
    
    /**
     * 获取所有指标模板
     * @return 指标模板列表
     */
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<MetricTemplateDTO>>> getAllMetricTemplates() {
        List<MetricTemplateDTO> templates = metricTemplateService.findAll();
        return ResponseEntity.ok(ApiResponse.success(templates));
    }
    
    /**
     * 根据ID获取指标模板
     * @param id 模板ID
     * @return 指标模板详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MetricTemplateDTO>> getMetricTemplateById(@PathVariable Long id) {
        MetricTemplateDTO template = metricTemplateService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(template));
    }
    
    /**
     * 创建指标模板
     * @param template 指标模板数据
     * @return 创建后的指标模板
     */
    @PostMapping
    public ResponseEntity<ApiResponse<MetricTemplateDTO>> createMetricTemplate(@RequestBody MetricTemplateDTO template) {
        MetricTemplateDTO created = metricTemplateService.create(template);
        return ResponseEntity.ok(ApiResponse.success(created));
    }
    
    /**
     * 更新指标模板
     * @param id 模板ID
     * @param template 指标模板数据
     * @return 更新后的指标模板
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MetricTemplateDTO>> updateMetricTemplate(
            @PathVariable Long id, @RequestBody MetricTemplateDTO template) {
        
        template.setId(id);
        MetricTemplateDTO updated = metricTemplateService.update(template);
        return ResponseEntity.ok(ApiResponse.success(updated));
    }
    
    /**
     * 删除指标模板
     * @param id 模板ID
     * @return 响应结果
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMetricTemplate(@PathVariable Long id) {
        metricTemplateService.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 批量删除指标模板
     * @param ids ID列表
     * @return 响应结果
     */
    @DeleteMapping("/batch")
    public ResponseEntity<ApiResponse<Void>> batchDeleteTemplates(@RequestBody List<Long> ids) {
        metricTemplateService.deleteByIds(ids);
        return ResponseEntity.ok(ApiResponse.success());
    }
    
    /**
     * 从指标模板创建指标
     * @param templateId 模板ID
     * @return 创建的指标ID
     */
    @PostMapping("/{templateId}/create-metric")
    public ResponseEntity<ApiResponse<Long>> createMetricFromTemplate(@PathVariable Long templateId) {
        Long metricId = metricTemplateService.createMetricFromTemplate(templateId);
        return ResponseEntity.ok(ApiResponse.success(metricId));
    }
    
    /**
     * 从指标创建模板
     * @param metricId 指标ID
     * @param templateName 模板名称
     * @return 创建的模板ID
     */
    @PostMapping("/create-from-metric")
    public ResponseEntity<ApiResponse<Long>> createTemplateFromMetric(
            @RequestParam Long metricId, @RequestParam String templateName) {
        
        Long templateId = metricTemplateService.createTemplateFromMetric(metricId, templateName);
        return ResponseEntity.ok(ApiResponse.success(templateId));
    }

    /**
     * 应用指标模板
     * @param id 模板ID
     * @param request 应用请求参数
     * @return 应用历史ID
     */
    @PostMapping("/{id}/apply")
    public ResponseEntity<ApiResponse<Long>> applyTemplate(
            @PathVariable Long id, @RequestBody ApplyTemplateRequest request) {
        
        Long applicationId = metricTemplateService.applyTemplate(id, request);
        return ResponseEntity.ok(ApiResponse.success(applicationId));
    }
    
    /**
     * 获取模板应用历史
     * @param templateId 模板ID
     * @return 应用历史列表
     */
    @GetMapping("/{templateId}/application-history")
    public ResponseEntity<ApiResponse<List<MetricTemplateApplicationDTO>>> getApplicationHistory(
            @PathVariable Long templateId) {
        
        List<MetricTemplateApplicationDTO> history = metricTemplateService.getApplicationHistory(templateId);
        return ResponseEntity.ok(ApiResponse.success(history));
    }
} 