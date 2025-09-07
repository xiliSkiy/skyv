package com.skyeye.collector.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 任务状态更新请求DTO
 * 
 * @author SkyEye Team
 */
@Data
public class TaskStatusUpdateRequest {

    /**
     * 任务状态
     * 1: 启用, 0: 禁用
     */
    @NotNull(message = "任务状态不能为空")
    private Integer status;

    /**
     * 更新原因
     */
    private String reason;

    /**
     * 操作人ID
     */
    private Long operatorId;

    /**
     * 操作人姓名
     */
    private String operatorName;

    /**
     * 备注
     */
    private String remarks;
}
