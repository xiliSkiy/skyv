package com.skyeye.scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 任务批量操作参数数据传输对象
 */
@Data
public class TaskBatchActionDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID列表
     */
    @NotEmpty(message = "任务ID列表不能为空")
    @Size(min = 1, message = "至少选择一个任务")
    private List<Long> ids;
} 