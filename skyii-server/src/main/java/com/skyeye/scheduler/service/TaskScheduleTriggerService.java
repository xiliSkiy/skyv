package com.skyeye.scheduler.service;

import com.skyeye.common.response.PageResult;
import com.skyeye.scheduler.dto.TaskScheduleTriggerDTO;
import com.skyeye.scheduler.entity.TaskScheduleTrigger;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * 任务调度触发器服务接口
 */
public interface TaskScheduleTriggerService {

    /**
     * 创建触发器
     * @param triggerDTO 触发器信息
     * @return 触发器
     */
    TaskScheduleTrigger createTrigger(TaskScheduleTriggerDTO triggerDTO);

    /**
     * 更新触发器
     * @param triggerId 触发器ID
     * @param triggerDTO 触发器信息
     * @return 触发器
     */
    TaskScheduleTrigger updateTrigger(Long triggerId, TaskScheduleTriggerDTO triggerDTO);

    /**
     * 删除触发器
     * @param triggerId 触发器ID
     */
    void deleteTrigger(Long triggerId);

    /**
     * 启用/禁用触发器
     * @param triggerId 触发器ID
     * @param enabled 是否启用
     * @return 是否成功
     */
    boolean updateTriggerStatus(Long triggerId, boolean enabled);

    /**
     * 根据ID查询触发器
     * @param triggerId 触发器ID
     * @return 触发器
     */
    TaskScheduleTrigger findById(Long triggerId);

    /**
     * 根据任务ID查询触发器列表
     * @param taskId 任务ID
     * @return 触发器列表
     */
    List<TaskScheduleTrigger> findByTaskId(Long taskId);

    /**
     * 计算触发器下次执行时间
     * @param trigger 触发器
     * @return 下次执行时间
     */
    LocalDateTime calculateNextFireTime(TaskScheduleTrigger trigger);

    /**
     * 触发任务
     * @param triggerId 触发器ID
     * @return 是否成功
     */
    boolean fireTrigger(Long triggerId);

    /**
     * 查询需要执行的触发器
     * @param now 当前时间
     * @return 触发器列表
     */
    List<TaskScheduleTrigger> findTriggersToExecute(LocalDateTime now);

    /**
     * 更新触发器执行时间
     * @param triggerId 触发器ID
     * @param lastFireTime 上次执行时间
     * @param nextFireTime 下次执行时间
     * @return 触发器
     */
    TaskScheduleTrigger updateTriggerFireTime(Long triggerId, LocalDateTime lastFireTime, LocalDateTime nextFireTime);

    /**
     * 分页查询触发器
     * @param pageable 分页参数
     * @return 分页结果
     */
    PageResult<TaskScheduleTrigger> findAll(Pageable pageable);
    
    /**
     * 查询可触发的触发器
     * @return 可触发的触发器列表
     */
    List<TaskScheduleTrigger> findTriggerable();
    
    /**
     * 更新触发器的下次执行时间
     * @param id 触发器ID
     * @param nextFireTime 下次执行时间
     * @return 是否成功
     */
    boolean updateNextFireTime(Long id, LocalDateTime nextFireTime);
    
    /**
     * 更新触发器的触发信息
     * @param id 触发器ID
     * @param lastFireTime 上次执行时间
     * @param increaseFiredCount 是否增加已触发次数
     * @return 是否成功
     */
    boolean updateFireInfo(Long id, LocalDateTime lastFireTime, boolean increaseFiredCount);
    
    /**
     * 根据事件类型查询触发器
     * @param eventType 事件类型
     * @return 触发器列表
     */
    List<TaskScheduleTrigger> findByEventType(String eventType);
    
    /**
     * 根据采集器ID查询触发器
     * @param collectorId 采集器ID
     * @return 触发器列表
     */
    List<TaskScheduleTrigger> findByCollectorId(Long collectorId);
} 