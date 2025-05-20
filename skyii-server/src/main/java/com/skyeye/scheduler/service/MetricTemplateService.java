package com.skyeye.scheduler.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.skyeye.scheduler.dto.ApplyTemplateRequest;
import com.skyeye.scheduler.dto.MetricTemplateDTO;
import com.skyeye.scheduler.dto.MetricTemplateApplicationDTO;

/**
 * 指标模板服务接口
 */
public interface MetricTemplateService {
    
    /**
     * 根据条件查询指标模板
     * @param templateName 模板名称（模糊查询）
     * @param metricType 指标类型
     * @param category 模板分类
     * @param deviceType 适用设备类型
     * @param isSystem 是否系统内置
     * @param pageable 分页参数
     * @return 指标模板分页数据
     */
    Page<MetricTemplateDTO> findByConditions(String templateName, String metricType, 
            String category, String deviceType, Boolean isSystem, Pageable pageable);
    
    /**
     * 查询所有指标模板
     * @return 指标模板列表
     */
    List<MetricTemplateDTO> findAll();
    
    /**
     * 根据ID查询指标模板
     * @param id 模板ID
     * @return 指标模板
     */
    MetricTemplateDTO findById(Long id);
    
    /**
     * 创建指标模板
     * @param template 指标模板数据
     * @return 创建后的指标模板
     */
    MetricTemplateDTO create(MetricTemplateDTO template);
    
    /**
     * 更新指标模板
     * @param template 指标模板数据
     * @return 更新后的指标模板
     */
    MetricTemplateDTO update(MetricTemplateDTO template);
    
    /**
     * 删除指标模板
     * @param id 模板ID
     */
    void deleteById(Long id);
    
    /**
     * 批量删除指标模板
     * @param ids 模板ID列表
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 从指标模板创建指标
     * @param templateId 模板ID
     * @return 创建的指标ID
     */
    Long createMetricFromTemplate(Long templateId);
    
    /**
     * 从指标创建模板
     * @param metricId 指标ID
     * @param templateName 模板名称
     * @return 创建的模板ID
     */
    Long createTemplateFromMetric(Long metricId, String templateName);
    
    /**
     * 初始化系统内置模板
     */
    void initSystemTemplates();
    
    /**
     * 应用指标模板
     * @param templateId 模板ID
     * @param request 应用请求参数
     * @return 应用历史ID
     */
    Long applyTemplate(Long templateId, ApplyTemplateRequest request);
    
    /**
     * 获取模板应用历史
     * @param templateId 模板ID
     * @return 应用历史列表
     */
    List<MetricTemplateApplicationDTO> getApplicationHistory(Long templateId);
} 