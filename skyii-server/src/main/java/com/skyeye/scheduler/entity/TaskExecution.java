package com.skyeye.scheduler.entity;

import com.skyeye.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.time.LocalDateTime;

/**
 * 任务执行记录实体类
 */
@Data
@Entity
@Table(name = "tb_task_executions")
@EqualsAndHashCode(callSuper = true)
public class TaskExecution extends BaseEntity {

    /**
     * 执行ID，唯一标识一次执行
     */
    @Column(name = "execution_id", unique = true)
    private String executionId;

    /**
     * 任务ID
     */
    @Column(name = "task_id", nullable = false)
    private Long taskId;
    
    /**
     * 任务关联
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", insertable = false, updatable = false)
    private Task task;

    /**
     * 开始时间
     */
    @Column(name = "start_time")
    private LocalDateTime startTime;
    
    /**
     * 结束时间
     */
    @Column(name = "end_time")
    private LocalDateTime endTime;
    
    /**
     * 执行时长(ms)
     */
    @Column(name = "execution_time")
    private Long executionTime;
    
    /**
     * 执行状态：1-运行中，2-已调度，3-已暂停，4-已完成，5-执行失败
     */
    @Column(name = "status", nullable = false)
    private Integer status;
    
    /**
     * 结果信息
     */
    @Column(name = "result", columnDefinition = "TEXT")
    private String result;
    
    /**
     * 错误信息
     */
    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;
    
    /**
     * 执行次数（重试次数+1）
     */
    @Column(name = "execution_count")
    private Integer executionCount = 1;
    
    /**
     * 执行设备数量
     */
    @Column(name = "device_count")
    private Integer deviceCount = 0;
    
    /**
     * 成功设备数量
     */
    @Column(name = "success_count")
    private Integer successCount = 0;
    
    /**
     * 失败设备数量
     */
    @Column(name = "failed_count")
    private Integer failedCount = 0;
} 