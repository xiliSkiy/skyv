package com.skyeye.scheduler.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 任务草稿数据传输对象
 */
@Data
public class TaskDraftDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 草稿ID
     */
    private String draftId;

    /**
     * 草稿数据（JSON格式）
     */
    private String draftData;

    /**
     * 当前步骤：0-基本信息，1-设备选择，2-指标配置，3-调度设置
     */
    private Integer step;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 创建人ID
     */
    private Long createdBy;
} 