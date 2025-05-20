 package com.skyeye.scheduler.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模板与指标关联关系实体
 */
@Entity
@Table(name = "tb_template_metric_relation")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TemplateMetricRelation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 模板ID
     */
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    
    /**
     * 指标ID
     */
    @Column(name = "metric_id", nullable = false)
    private Long metricId;
    
    /**
     * 指标在模板中的排序位置
     */
    @Column(name = "display_order")
    private Integer displayOrder;
    
    /**
     * 参数覆盖（JSON格式，覆盖默认参数）
     */
    @Column(name = "param_override", columnDefinition = "TEXT")
    private String paramOverride;
}