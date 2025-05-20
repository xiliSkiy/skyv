package com.skyeye.scheduler.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.skyeye.scheduler.entity.MetricTemplateApplication;

/**
 * 指标模板应用历史仓库接口
 */
@Repository
public interface MetricTemplateApplicationRepository extends JpaRepository<MetricTemplateApplication, Long> {
    
    /**
     * 根据模板ID查询应用历史
     * @param templateId 模板ID
     * @return 应用历史列表
     */
    List<MetricTemplateApplication> findByTemplateIdOrderByApplyTimeDesc(Long templateId);
    
    /**
     * 根据模板ID查询应用历史（分页）
     * @param templateId 模板ID
     * @param pageable 分页参数
     * @return 应用历史分页数据
     */
    Page<MetricTemplateApplication> findByTemplateIdOrderByApplyTimeDesc(Long templateId, Pageable pageable);
    
    /**
     * 根据应用用户ID查询应用历史
     * @param applyUserId 应用用户ID
     * @return 应用历史列表
     */
    List<MetricTemplateApplication> findByApplyUserIdOrderByApplyTimeDesc(Long applyUserId);
    
    /**
     * 根据模板ID统计应用历史数量
     * @param templateId 模板ID
     * @return 应用历史数量
     */
    Long countByTemplateId(Long templateId);
}