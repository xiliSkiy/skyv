package com.skyeye.scheduler.scheduler;

import com.skyeye.scheduler.entity.TaskScheduleTrigger;
import com.skyeye.scheduler.service.TaskScheduleTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务触发器调度器
 * 定期扫描需要触发的任务，并执行它们
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskTriggerScheduler {

    private final TaskScheduleTriggerService taskScheduleTriggerService;

    /**
     * 每30秒扫描一次需要执行的触发器
     */
    @Scheduled(fixedRate = 30000)
    public void scanAndExecuteTriggers() {
        log.debug("开始扫描需要执行的触发器...");
        try {
            // 查询当前时间之前应该触发的触发器
            List<TaskScheduleTrigger> triggers = taskScheduleTriggerService.findTriggerable();
            
            if (triggers.isEmpty()) {
                log.debug("没有需要执行的触发器");
                return;
            }
            
            log.info("找到{}个需要执行的触发器", triggers.size());
            
            // 执行每个触发器
            for (TaskScheduleTrigger trigger : triggers) {
                try {
                    log.info("正在触发任务: ID={}, 名称={}", trigger.getTaskId(), trigger.getTriggerName());
                    boolean success = taskScheduleTriggerService.fireTrigger(trigger.getId());
                    if (success) {
                        log.info("触发任务成功: ID={}", trigger.getTaskId());
                    } else {
                        log.warn("触发任务失败: ID={}", trigger.getTaskId());
                    }
                } catch (Exception e) {
                    log.error("触发任务异常: ID={}, 错误={}", trigger.getTaskId(), e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            log.error("扫描触发器时发生错误: {}", e.getMessage(), e);
        }
    }
} 