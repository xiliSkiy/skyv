package com.skyeye.scheduler.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * 任务基本信息DTO
 */
@Data
public class TaskBasicInfoDTO {
    
    /**
     * 任务ID（更新时使用）
     */
    private Long id;
    
    /**
     * 任务名称
     */
    @NotBlank(message = "任务名称不能为空")
    @Size(max = 100, message = "任务名称长度不能超过100个字符")
    private String name;
    
    /**
     * 任务类型
     */
    @NotBlank(message = "任务类型不能为空")
    private String type;
    
    /**
     * 任务描述
     */
    @Size(max = 500, message = "任务描述长度不能超过500个字符")
    private String description;
    
    /**
     * 任务标签
     */
    private List<String> tags;
    
    /**
     * 任务优先级
     */
    private Integer priority;
    
    /**
     * 任务所有者ID
     */
    @NotNull(message = "任务所有者不能为空")
    private Long ownerId;
    
    /**
     * 是否启用报警
     */
    private Boolean enableAlert;
    
    /**
     * 报警级别
     */
    private String alertLevel;
    
    /**
     * 报警通知方式
     */
    private List<String> alertNotifyTypes;
    
    /**
     * 报警通知接收人
     */
    private List<Long> alertReceivers;
} 