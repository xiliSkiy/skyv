package com.skyeye.collector.scheduler.impl;

import com.skyeye.collector.entity.CollectionTask;
import com.skyeye.collector.scheduler.*;
import com.skyeye.collector.service.CollectionTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 任务调度器实现
 * 
 * @author SkyEye Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TaskSchedulerImpl implements TaskScheduler {

    private final CollectionTaskService collectionTaskService;

    @Value("${skyeye.scheduler.thread-pool-size:10}")
    private int threadPoolSize;

    @Value("${skyeye.scheduler.max-queue-size:100}")
    private int maxQueueSize;

    @Value("${skyeye.scheduler.keep-alive-seconds:60}")
    private long keepAliveSeconds;

    @Value("${skyeye.scheduler.cleanup-interval-minutes:30}")
    private long cleanupIntervalMinutes;

    /**
     * 调度器状态
     */
    private volatile SchedulerStatus status = SchedulerStatus.STOPPED;

    /**
     * 调度器启动时间
     */
    private LocalDateTime startTime;

    /**
     * 最后活跃时间
     */
    private LocalDateTime lastActiveTime;

    /**
     * 调度器统计信息
     */
    private final SchedulerStatistics.SchedulerStatisticsBuilder statisticsBuilder = SchedulerStatistics.builder();

    /**
     * 已调度的任务映射
     */
    private final ConcurrentHashMap<Long, ScheduledTask> scheduledTasks = new ConcurrentHashMap<>();

    /**
     * 调度器线程池
     */
    private ScheduledExecutorService scheduler;

    /**
     * 任务执行线程池
     */
    private ThreadPoolExecutor taskExecutor;

    /**
     * 统计计数器
     */
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong totalSuccesses = new AtomicLong(0);
    private final AtomicLong totalFailures = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);

    @PostConstruct
    public void init() {
        log.info("初始化任务调度器...");
        start();
    }

    @PreDestroy
    public void destroy() {
        log.info("销毁任务调度器...");
        stop();
    }

    @Override
    public void start() {
        if (status == SchedulerStatus.RUNNING) {
            log.warn("调度器已经在运行中");
            return;
        }

        try {
            status = SchedulerStatus.STARTING;
            log.info("启动任务调度器...");

            // 初始化线程池
            initializeThreadPools();

            // 加载所有启用的任务
            reloadAllTasks();

            startTime = LocalDateTime.now();
            lastActiveTime = LocalDateTime.now();
            status = SchedulerStatus.RUNNING;

            log.info("任务调度器启动成功，线程池大小: {}", threadPoolSize);

        } catch (Exception e) {
            log.error("启动任务调度器失败", e);
            status = SchedulerStatus.ERROR;
            throw new RuntimeException("启动任务调度器失败", e);
        }
    }

    @Override
    public void stop() {
        if (status == SchedulerStatus.STOPPED) {
            return;
        }

        try {
            status = SchedulerStatus.STOPPING;
            log.info("停止任务调度器...");

            // 取消所有已调度的任务
            scheduledTasks.values().forEach(task -> {
                if (task.getScheduledFuture() != null) {
                    task.getScheduledFuture().cancel(false);
                }
            });

            // 关闭线程池
            if (scheduler != null) {
                scheduler.shutdown();
                if (!scheduler.awaitTermination(30, TimeUnit.SECONDS)) {
                    scheduler.shutdownNow();
                }
            }

            if (taskExecutor != null) {
                taskExecutor.shutdown();
                if (!taskExecutor.awaitTermination(30, TimeUnit.SECONDS)) {
                    taskExecutor.shutdownNow();
                }
            }

            scheduledTasks.clear();
            status = SchedulerStatus.STOPPED;

            log.info("任务调度器已停止");

        } catch (Exception e) {
            log.error("停止任务调度器失败", e);
            status = SchedulerStatus.ERROR;
        }
    }

    @Override
    public void scheduleTask(CollectionTask task) {
        if (!status.canAcceptTasks()) {
            throw new IllegalStateException("调度器当前状态不允许接受新任务: " + status);
        }

        try {
            log.info("调度任务: taskId={}, name={}, scheduleType={}", 
                    task.getId(), task.getName(), task.getScheduleType());

            // 取消之前的调度
            cancelTask(task.getId());

            // 创建调度任务包装器
            ScheduledTask scheduledTask = new ScheduledTask(task);

            // 根据调度类型进行调度
            switch (task.getScheduleType()) {
                case "SIMPLE":
                case "interval":
                    scheduleSimpleTask(scheduledTask);
                    break;
                case "CRON":
                case "cron":
                    scheduleCronTask(scheduledTask);
                    break;
                case "EVENT":
                case "event":
                    scheduleEventTask(scheduledTask);
                    break;
                case "once":
                    scheduleOnceTask(scheduledTask);
                    break;
                default:
                    throw new IllegalArgumentException("不支持的调度类型: " + task.getScheduleType());
            }

            // 保存到已调度任务映射
            scheduledTasks.put(task.getId(), scheduledTask);

            log.info("任务调度成功: taskId={}, nextExecutionTime={}", 
                    task.getId(), scheduledTask.getNextExecutionTime());

        } catch (Exception e) {
            log.error("调度任务失败: taskId={}", task.getId(), e);
            throw new RuntimeException("调度任务失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void cancelTask(Long taskId) {
        ScheduledTask scheduledTask = scheduledTasks.get(taskId);
        if (scheduledTask != null) {
            if (scheduledTask.getScheduledFuture() != null) {
                scheduledTask.getScheduledFuture().cancel(false);
                log.info("取消任务调度: taskId={}", taskId);
            }
            scheduledTasks.remove(taskId);
        }
    }

    @Override
    public void rescheduleTask(CollectionTask task) {
        scheduleTask(task);
    }

    @Override
    public void pauseTask(Long taskId) {
        ScheduledTask scheduledTask = scheduledTasks.get(taskId);
        if (scheduledTask != null) {
            scheduledTask.setStatus(TaskExecutionStatus.PAUSED);
            if (scheduledTask.getScheduledFuture() != null) {
                scheduledTask.getScheduledFuture().cancel(false);
            }
            log.info("暂停任务: taskId={}", taskId);
        }
    }

    @Override
    public void resumeTask(Long taskId) {
        ScheduledTask scheduledTask = scheduledTasks.get(taskId);
        if (scheduledTask != null && scheduledTask.getStatus() == TaskExecutionStatus.PAUSED) {
            scheduledTask.setStatus(TaskExecutionStatus.SCHEDULED);
            // 重新调度任务
            scheduleTask(scheduledTask.getTask());
            log.info("恢复任务: taskId={}", taskId);
        }
    }

    @Override
    public List<Long> getScheduledTaskIds() {
        return List.copyOf(scheduledTasks.keySet());
    }

    @Override
    public boolean isTaskScheduled(Long taskId) {
        return scheduledTasks.containsKey(taskId);
    }

    @Override
    public LocalDateTime getNextExecutionTime(Long taskId) {
        ScheduledTask scheduledTask = scheduledTasks.get(taskId);
        return scheduledTask != null ? scheduledTask.getNextExecutionTime() : null;
    }

    @Override
    public SchedulerStatus getStatus() {
        return status;
    }

    @Override
    public SchedulerStatistics getStatistics() {
        updateStatistics();
        return statisticsBuilder.build();
    }

    @Override
    public void cleanupExpiredTasks() {
        log.info("开始清理过期任务...");
        int cleanedCount = 0;

        scheduledTasks.entrySet().removeIf(entry -> {
            ScheduledTask task = entry.getValue();
            if (task.isExpired()) {
                log.info("清理过期任务: taskId={}, name={}", task.getTaskId(), task.getTaskName());
                if (task.getScheduledFuture() != null) {
                    task.getScheduledFuture().cancel(false);
                }
                return true;
            }
            return false;
        });

        log.info("清理过期任务完成，共清理 {} 个任务", cleanedCount);
    }

    @Override
    public void reloadAllTasks() {
        if (!status.canAcceptTasks()) {
            log.warn("调度器当前状态不允许重新加载任务: {}", status);
            return;
        }

        try {
            log.info("开始重新加载所有启用的任务...");

            // 清除现有调度
            scheduledTasks.values().forEach(task -> {
                if (task.getScheduledFuture() != null) {
                    task.getScheduledFuture().cancel(false);
                }
            });
            scheduledTasks.clear();

            // 重新加载所有启用的任务
            List<CollectionTask> enabledTasks = collectionTaskService.getAllEnabledTasks();
            for (CollectionTask task : enabledTasks) {
                if (task.getStatus() == 1 && task.getIsEnabled()) {
                    scheduleTask(task);
                }
            }

            log.info("重新加载任务完成，共加载 {} 个任务", scheduledTasks.size());

        } catch (Exception e) {
            log.error("重新加载任务失败", e);
        }
    }

    /**
     * 初始化线程池
     */
    private void initializeThreadPools() {
        // 调度器线程池
        scheduler = Executors.newScheduledThreadPool(threadPoolSize, r -> {
            Thread t = new Thread(r, "TaskScheduler-" + r.hashCode());
            t.setDaemon(true);
            return t;
        });

        // 任务执行线程池
        taskExecutor = new ThreadPoolExecutor(
                threadPoolSize,
                threadPoolSize * 2,
                keepAliveSeconds,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>(maxQueueSize),
                r -> {
                    Thread t = new Thread(r, "TaskExecutor-" + r.hashCode());
                    t.setDaemon(true);
                    return t;
                },
                new ThreadPoolExecutor.CallerRunsPolicy()
        );
    }

    /**
     * 调度简单任务（固定间隔）
     */
    private void scheduleSimpleTask(ScheduledTask scheduledTask) {
        CollectionTask task = scheduledTask.getTask();
        Map<String, Object> config = task.getScheduleConfig();
        
        // 支持两种格式：旧格式(frequency+interval)和新格式(intervalUnit+intervalValue)
        String frequency = (String) config.get("frequency");
        Integer interval = (Integer) config.get("interval");
        
        String intervalUnit = (String) config.get("intervalUnit");
        Object intervalValueObj = config.get("intervalValue");
        Integer intervalValue = null;
        
        if (intervalValueObj != null) {
            if (intervalValueObj instanceof Integer) {
                intervalValue = (Integer) intervalValueObj;
            } else if (intervalValueObj instanceof String) {
                try {
                    intervalValue = Integer.parseInt((String) intervalValueObj);
                } catch (NumberFormatException e) {
                    throw new IllegalArgumentException("intervalValue必须是数字: " + intervalValueObj);
                }
            }
        }
        
        // 优先使用新格式，如果没有则使用旧格式
        if (intervalUnit != null && intervalValue != null) {
            frequency = intervalUnit;
            interval = intervalValue;
        } else if (frequency == null || interval == null) {
            throw new IllegalArgumentException("简单调度配置缺少frequency+interval或intervalUnit+intervalValue参数");
        }

        long period;
        switch (frequency.toLowerCase()) {
            case "seconds":
                period = interval;
                break;
            case "minutes":
                period = interval * 60L;
                break;
            case "hours":
                period = interval * 3600L;
                break;
            case "days":
                period = interval * 86400L;
                break;
            default:
                throw new IllegalArgumentException("不支持的频率类型: " + frequency);
        }

        // 计算下次执行时间
        LocalDateTime nextExecutionTime = LocalDateTime.now().plusSeconds(period);
        scheduledTask.updateNextExecutionTime(nextExecutionTime);

        // 调度任务
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> executeTask(scheduledTask),
                0, period, TimeUnit.SECONDS
        );

        scheduledTask.setScheduledFuture(future);
    }

    /**
     * 调度Cron任务
     */
    private void scheduleCronTask(ScheduledTask scheduledTask) {
        CollectionTask task = scheduledTask.getTask();
        Map<String, Object> config = task.getScheduleConfig();
        
        String cronExpression = (String) config.get("cronExpression");
        if (cronExpression == null) {
            throw new IllegalArgumentException("Cron调度配置缺少cronExpression参数");
        }

        // 计算下次执行时间
        LocalDateTime nextExecutionTime = calculateNextCronExecution(cronExpression);
        scheduledTask.updateNextExecutionTime(nextExecutionTime);

        // 调度任务（每分钟检查一次）
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(
                () -> checkAndExecuteCronTask(scheduledTask),
                0, 1, TimeUnit.MINUTES
        );

        scheduledTask.setScheduledFuture(future);
    }

    /**
     * 调度事件任务
     */
    private void scheduleEventTask(ScheduledTask scheduledTask) {
        CollectionTask task = scheduledTask.getTask();
        Map<String, Object> config = task.getScheduleConfig();
        
        String eventType = (String) config.get("eventType");
        if (eventType == null) {
            throw new IllegalArgumentException("事件调度配置缺少eventType参数");
        }

        // 事件任务不立即调度，等待事件触发
        scheduledTask.setStatus(TaskExecutionStatus.WAITING);
        scheduledTask.updateNextExecutionTime(null);

        log.info("事件任务已注册，等待事件触发: taskId={}, eventType={}", 
                task.getId(), eventType);
    }

    /**
     * 调度一次性任务
     */
    private void scheduleOnceTask(ScheduledTask scheduledTask) {
        CollectionTask task = scheduledTask.getTask();
        Map<String, Object> config = task.getScheduleConfig();
        
        // 获取执行时间
        String executionTimeStr = (String) config.get("executionTime");
        if (executionTimeStr == null) {
            executionTimeStr = (String) config.get("startTime");
        }
        
        if (executionTimeStr == null) {
            throw new IllegalArgumentException("一次性任务调度配置缺少executionTime或startTime参数");
        }

        try {
            // 解析执行时间
            LocalDateTime executionTime = LocalDateTime.parse(executionTimeStr.replace(" ", "T"));
            LocalDateTime now = LocalDateTime.now();
            
            if (executionTime.isBefore(now)) {
                throw new IllegalArgumentException("执行时间不能早于当前时间");
            }
            
            // 计算延迟时间（秒）
            long delay = Duration.between(now, executionTime).getSeconds();
            
            scheduledTask.updateNextExecutionTime(executionTime);

            // 调度任务（只执行一次）
            ScheduledFuture<?> future = scheduler.schedule(
                    () -> executeTask(scheduledTask),
                    delay, TimeUnit.SECONDS
            );

            scheduledTask.setScheduledFuture(future);
            
            log.info("一次性任务已调度: taskId={}, executionTime={}, delay={}秒", 
                    task.getId(), executionTime, delay);
                    
        } catch (Exception e) {
            throw new IllegalArgumentException("解析执行时间失败: " + executionTimeStr, e);
        }
    }

    /**
     * 执行任务
     */
    private void executeTask(ScheduledTask scheduledTask) {
        if (!scheduledTask.canExecute()) {
            return;
        }

        long startTime = System.currentTimeMillis();
        boolean success = false;

        try {
            scheduledTask.setStatus(TaskExecutionStatus.RUNNING);
            lastActiveTime = LocalDateTime.now();

            log.debug("开始执行任务: taskId={}, name={}", 
                    scheduledTask.getTaskId(), scheduledTask.getTaskName());

            // 异步执行任务
            collectionTaskService.executeTask(scheduledTask.getTaskId());

            success = true;
            totalSuccesses.incrementAndGet();

            log.debug("任务执行成功: taskId={}, name={}", 
                    scheduledTask.getTaskId(), scheduledTask.getTaskName());

        } catch (Exception e) {
            totalFailures.incrementAndGet();
            scheduledTask.setExecutionError(e.getMessage());
            
            log.error("任务执行失败: taskId={}, name={}", 
                    scheduledTask.getTaskId(), scheduledTask.getTaskName(), e);

        } finally {
            long executionTime = System.currentTimeMillis() - startTime;
            totalExecutions.incrementAndGet();
            totalExecutionTime.addAndGet(executionTime);

            scheduledTask.updateExecutionStatistics(success, executionTime);
            scheduledTask.setStatus(TaskExecutionStatus.SCHEDULED);

            // 更新下次执行时间（对于简单调度）
            if ("SIMPLE".equals(scheduledTask.getScheduleType())) {
                updateNextExecutionTimeForSimpleTask(scheduledTask);
            }
        }
    }

    /**
     * 检查并执行Cron任务
     */
    private void checkAndExecuteCronTask(ScheduledTask scheduledTask) {
        if (!scheduledTask.canExecute()) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime nextExecutionTime = scheduledTask.getNextExecutionTime();

        if (nextExecutionTime != null && now.isAfter(nextExecutionTime)) {
            executeTask(scheduledTask);
            
            // 计算下次执行时间
            Object scheduleConfig = scheduledTask.getScheduleConfig();
            if (scheduleConfig instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> config = (Map<String, Object>) scheduleConfig;
                String cronExpression = (String) config.get("cronExpression");
                if (cronExpression != null) {
                    LocalDateTime nextTime = calculateNextCronExecution(cronExpression);
                    scheduledTask.updateNextExecutionTime(nextTime);
                }
            }
        }
    }

    /**
     * 计算Cron表达式的下次执行时间
     */
    private LocalDateTime calculateNextCronExecution(String cronExpression) {
        // 这里使用简化的Cron解析逻辑
        // 在实际项目中，可以使用Quartz或Spring的CronSequenceGenerator
        
        // 示例：每天凌晨2点执行 "0 0 2 * * ?"
        if ("0 0 2 * * ?".equals(cronExpression)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime next = now.withHour(2).withMinute(0).withSecond(0);
            if (next.isBefore(now)) {
                next = next.plusDays(1);
            }
            return next;
        }

        // 示例：每周一上午9点执行 "0 0 9 ? * MON"
        if ("0 0 9 ? * MON".equals(cronExpression)) {
            LocalDateTime now = LocalDateTime.now();
            LocalDateTime next = now.with(java.time.DayOfWeek.MONDAY)
                    .withHour(9).withMinute(0).withSecond(0);
            if (next.isBefore(now)) {
                next = next.plusWeeks(1);
            }
            return next;
        }

        // 默认：每小时执行一次
        return LocalDateTime.now().plusHours(1);
    }

    /**
     * 更新简单任务的下次执行时间
     */
    private void updateNextExecutionTimeForSimpleTask(ScheduledTask scheduledTask) {
        CollectionTask task = scheduledTask.getTask();
        Map<String, Object> config = task.getScheduleConfig();
        
        String frequency = (String) config.get("frequency");
        Integer interval = (Integer) config.get("interval");
        
        if (frequency != null && interval != null) {
            LocalDateTime nextExecutionTime = scheduledTask.getNextExecutionTime();
            if (nextExecutionTime != null) {
                LocalDateTime newNextExecutionTime;
                switch (frequency.toLowerCase()) {
                    case "minutes":
                        newNextExecutionTime = nextExecutionTime.plusMinutes(interval);
                        break;
                    case "hours":
                        newNextExecutionTime = nextExecutionTime.plusHours(interval);
                        break;
                    case "days":
                        newNextExecutionTime = nextExecutionTime.plusDays(interval);
                        break;
                    default:
                        return;
                }
                scheduledTask.updateNextExecutionTime(newNextExecutionTime);
            }
        }
    }

    /**
     * 更新统计信息
     */
    private void updateStatistics() {
        statisticsBuilder
                .totalTasks(scheduledTasks.size())
                .scheduledTasks((int) scheduledTasks.values().stream()
                        .filter(task -> task.getStatus() == TaskExecutionStatus.SCHEDULED)
                        .count())
                .runningTasks((int) scheduledTasks.values().stream()
                        .filter(task -> task.getStatus() == TaskExecutionStatus.RUNNING)
                        .count())
                .pausedTasks((int) scheduledTasks.values().stream()
                        .filter(task -> task.getStatus() == TaskExecutionStatus.PAUSED)
                        .count())
                .errorTasks((int) scheduledTasks.values().stream()
                        .filter(task -> task.getStatus() == TaskExecutionStatus.FAILED)
                        .count())
                .todayExecutions(totalExecutions.get())
                .todaySuccesses(totalSuccesses.get())
                .todayFailures(totalFailures.get())
                .averageExecutionTime(totalExecutions.get() > 0 ? 
                        totalExecutionTime.get() / totalExecutions.get() : 0)
                .startTime(startTime)
                .lastActiveTime(lastActiveTime)
                .activeThreads(taskExecutor != null ? taskExecutor.getActiveCount() : 0)
                .totalThreads(taskExecutor != null ? taskExecutor.getPoolSize() : 0)
                .queuedTasks(taskExecutor != null ? taskExecutor.getQueue().size() : 0)
                .memoryUsage(Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory())
                .cpuUsage(0.0); // 这里可以实现CPU使用率监控
    }

    /**
     * 定时清理过期任务
     */
    @Scheduled(fixedRate = 30, timeUnit = TimeUnit.MINUTES)
    public void scheduledCleanup() {
        if (status == SchedulerStatus.RUNNING) {
            cleanupExpiredTasks();
        }
    }

    /**
     * 定时更新统计信息
     */
    @Scheduled(fixedRate = 1, timeUnit = TimeUnit.MINUTES)
    public void scheduledStatisticsUpdate() {
        if (status == SchedulerStatus.RUNNING) {
            updateStatistics();
        }
    }
}
