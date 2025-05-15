package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;

/**
 * 任务草稿实体类
 */
@Data
@Entity
@Table(name = "tb_task_drafts")
@EqualsAndHashCode(callSuper = true)
public class TaskDraft extends BaseEntity {

    /**
     * 草稿ID
     */
    @Column(name = "draft_id", nullable = false, unique = true, length = 50)
    private String draftId;

    /**
     * 草稿数据（JSON格式）
     */
    @Column(name = "draft_data", columnDefinition = "TEXT")
    private String draftData;

    /**
     * 当前步骤：0-基本信息，1-设备选择，2-指标配置，3-调度设置
     */
    @Column(name = "step")
    private Integer step;

    /**
     * 创建人ID
     */
    @Column(name = "created_by")
    private Long createdBy;
} 