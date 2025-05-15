package com.skyeye.scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;
import java.util.List;

/**
 * 任务创建参数数据传输对象
 */
@Data
public class TaskCreateDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(min = 2, max = 50, message = "任务名称长度必须在2-50个字符之间")
    private String taskName;

    /**
     * 任务类型
     */
    @NotBlank(message = "任务类型不能为空")
    private String taskType;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务优先级：high-高，medium-中，low-低
     */
    private String priority = "medium";

    /**
     * 任务标签列表
     */
    private List<String> tags;

    /**
     * 创建人
     */
    private String owner;

    /**
     * 所属部门
     */
    private String department;

    /**
     * 是否启用通知
     */
    private Boolean enableNotification = true;

    /**
     * 任务设备列表
     */
    @NotNull(message = "任务设备不能为空")
    @Size(min = 1, message = "至少选择一个设备")
    private List<TaskDeviceDTO> devices;

    /**
     * 任务指标列表
     */
    @NotNull(message = "任务指标不能为空")
    @Size(min = 1, message = "至少配置一个指标")
    private List<TaskMetricDTO> metrics;

    /**
     * 任务调度信息
     */
    @NotNull(message = "调度信息不能为空")
    private TaskScheduleDTO schedule;
} 