 package com.skyeye.scheduler.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.skyeye.scheduler.entity.TemplateMetricRelation;

@Repository
public interface TemplateMetricRelationRepository extends JpaRepository<TemplateMetricRelation, Long> {
    
    /**
     * 根据模板ID查找关联关系
     */
    List<TemplateMetricRelation> findByTemplateId(Long templateId);
    
    /**
     * 根据指标ID查找关联关系
     */
    List<TemplateMetricRelation> findByMetricId(Long metricId);
    
    /**
     * 根据模板ID删除关联关系
     */
    @Modifying
    void deleteByTemplateId(Long templateId);
    
    /**
     * 根据指标ID删除关联关系
     */
    @Modifying
    void deleteByMetricId(Long metricId);
    
    /**
     * 查询模板包含的指标ID列表
     */
    @Query("SELECT r.metricId FROM TemplateMetricRelation r WHERE r.templateId = :templateId ORDER BY r.displayOrder")
    List<Long> findMetricIdsByTemplateId(@Param("templateId") Long templateId);
}