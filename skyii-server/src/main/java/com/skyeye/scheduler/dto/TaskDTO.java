package com.skyeye.scheduler.dto;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务数据传输对象
 */
@Data
public class TaskDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 任务ID
     */
    private Long id;

    /**
     * 任务名称
     */
    private String taskName;

    /**
     * 任务类型
     */
    private String taskType;

    /**
     * 任务描述
     */
    private String description;

    /**
     * 任务状态：running-运行中，scheduled-已调度，paused-已暂停，completed-已完成，failed-执行失败
     */
    private String status;

    /**
     * 创建时间
     */
    private LocalDateTime createdAt;

    /**
     * 更新时间
     */
    private LocalDateTime updatedAt;

    /**
     * 开始时间
     */
    private LocalDateTime startTime;

    /**
     * 结束时间
     */
    private LocalDateTime endTime;

    /**
     * 下次执行时间
     */
    private LocalDateTime nextExecutionTime;

    /**
     * 任务优先级：high-高，medium-中，low-低
     */
    private String priority;

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
     * 设备数量
     */
    private Integer deviceCount;

    /**
     * 执行次数
     */
    private Integer executionCount;

    /**
     * 任务设备列表
     */
    private List<TaskDeviceDTO> devices;

    /**
     * 任务指标列表
     */
    private List<TaskMetricDTO> metrics;

    /**
     * 任务调度信息
     */
    private TaskScheduleDTO schedule;
} 