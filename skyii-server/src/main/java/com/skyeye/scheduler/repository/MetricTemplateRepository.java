package com.skyeye.scheduler.repository;

import com.skyeye.scheduler.entity.MetricTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 指标模板数据访问接口
 */
@Repository
public interface MetricTemplateRepository extends JpaRepository<MetricTemplate, Long> {

    /**
     * 根据模板名称查询指标模板
     * @param templateName 模板名称
     * @return 指标模板
     */
    MetricTemplate findByTemplateName(String templateName);

    /**
     * 根据指标类型查询指标模板
     * @param metricType 指标类型
     * @return 指标模板列表
     */
    List<MetricTemplate> findByMetricType(String metricType);

    /**
     * 根据设备类型查询指标模板
     * @param deviceType 设备类型
     * @return 指标模板列表
     */
    List<MetricTemplate> findByDeviceType(String deviceType);

    /**
     * 根据分类查询指标模板
     * @param category 分类
     * @return 指标模板列表
     */
    List<MetricTemplate> findByCategory(String category);

    /**
     * 查询系统内置的指标模板
     * @return 指标模板列表
     */
    List<MetricTemplate> findByIsSystem(Boolean isSystem);

    /**
     * 根据模板名称模糊查询指标模板
     * @param templateName 模板名称
     * @return 指标模板列表
     */
    List<MetricTemplate> findByTemplateNameContaining(String templateName);
} 