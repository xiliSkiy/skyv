package com.skyeye.scheduler.entity;

import java.time.LocalDateTime;
import javax.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import com.skyeye.common.entity.BaseEntity;

/**
 * 指标模板应用历史实体类
 */
@Data
@Entity
@Table(name = "tb_metric_template_applications")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MetricTemplateApplication extends BaseEntity {

    /**
     * 模板ID
     */
    @Column(name = "template_id", nullable = false)
    private Long templateId;
    
    /**
     * 模板名称
     */
    @Column(name = "template_name", length = 100)
    private String templateName;
    
    /**
     * 应用时间
     */
    @Column(name = "apply_time", nullable = false)
    private LocalDateTime applyTime;
    
    /**
     * 应用用户ID
     */
    @Column(name = "apply_user_id")
    private Long applyUserId;
    
    /**
     * 应用用户名
     */
    @Column(name = "apply_user", length = 50)
    private String applyUser;
    
    /**
     * 影响指标数
     */
    @Column(name = "affected_count")
    private Integer affectedCount;
    
    /**
     * 状态：SUCCESS-成功，PARTIAL-部分成功，FAILED-失败
     */
    @Column(name = "status", length = 20, nullable = false)
    private String status;
    
    /**
     * 应用结果信息
     */
    @Column(name = "result_info", columnDefinition = "TEXT")
    private String resultInfo;
    
    /**
     * 应用设备ID集合，JSON数组格式
     */
    @Column(name = "device_ids", columnDefinition = "TEXT")
    private String deviceIds;
} 