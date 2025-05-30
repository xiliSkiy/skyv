# SkyEye 任务调度系统详细文档

## 1. 系统架构概览

SkyEye系统采用分布式任务调度架构，将服务端(skyii-server)和采集端(skyii-collector)分离，实现高效、可靠的监控数据采集体系。任务调度系统是整个监控系统的核心组件，负责管理各类监控任务的创建、调度和执行。

### 1.1 总体架构

```
┌───────────────────────────┐                  ┌───────────────────────────┐
│       skyii-server        │                  │      skyii-collector      │
│    (任务管理与调度中心)    │◄─────────────────►│      (任务执行终端)       │
└───────────────────────────┘                  └───────────────────────────┘
           │                                               │
           │ 创建/调度任务                                   │ 执行采集
           ▼                                               ▼
┌───────────────────────────┐                  ┌───────────────────────────┐
│        数据库存储          │                  │        目标设备           │
│   (任务、批次、结果存储)    │                  │  (服务器、网络设备等)     │
└───────────────────────────┘                  └───────────────────────────┘
```

### 1.2 核心组件

1. **平台端核心组件**
   - **TaskService**: 负责任务的创建、更新、删除等基本操作
   - **TaskScheduleTriggerService**: 管理任务触发器，负责计算触发时间和触发任务
   - **CollectionTaskService**: 负责创建和管理具体的采集任务
   - **TaskTriggerScheduler**: 定时扫描需要执行的触发器，是任务自动执行的入口

2. **采集端核心组件**
   - **Collector**: 采集器主程序，负责接收和执行采集任务
   - **Scheduler**: 任务调度器，管理任务队列和并发执行
   - **TaskExecutor**: 任务执行器，负责执行具体的采集逻辑
   - **DataSender**: 数据发送器，负责将采集结果回传到服务端

## 2. 核心数据模型

### 2.1 关键实体

1. **Task (任务)**
   - 系统中的任务基本单元，包含任务基本信息和配置
   - 主要字段：id, taskName, taskType, description, status, isCollectionTask, collectType, collectParams
   - 与设备和指标存在多对多关系

2. **TaskScheduleTrigger (任务触发器)**
   - 定义任务的触发条件和计划
   - 主要字段：id, taskId, triggerName, triggerType, cronExpression, enabled, firedCount, lastFireTime, nextFireTime
   - 支持多种触发方式：cron表达式、简单定时、事件触发

3. **TaskBatch (任务批次)**
   - 每次任务触发产生的执行批次
   - 主要字段：id, taskId, collectorId, batchName, status, scheduledTime, totalTasks, completedTasks
   - 一个任务可以有多个批次，一个批次包含多个采集任务

4. **CollectionTask (采集任务)**
   - 具体的执行单元，由采集器执行
   - 主要字段：id, batchId, deviceId, metricId, deviceName, metricName, status, targetAddress, targetPort, collectType, collectParams
   - 每个采集任务针对特定设备的特定指标

5. **CollectionResult (采集结果)**
   - 存储采集任务的执行结果
   - 主要字段：id, taskId, deviceId, metricId, resultValue, resultType, status, executionTime

### 2.2 实体关系图

```
                                ┌───────────────────┐
                                │       Task        │
                                └───────────────────┘
                                         │
                    ┌───────────────────┴────────────────────┐
                    │                                        │
          ┌─────────▼──────────┐                 ┌───────────▼────────┐
          │  TaskScheduleTrigger │                 │      TaskBatch      │
          └─────────────────────┘                 └────────────────────┘
                                                           │
                                               ┌───────────▼────────┐
                                               │   CollectionTask   │
                                               └────────────────────┘
                                                           │
                                               ┌───────────▼────────┐
                                               │  CollectionResult  │
                                               └────────────────────┘
```

## 3. 任务生命周期

### 3.1 任务创建阶段

1. **创建任务基本信息**
   - 客户端通过API调用`TaskController.createTask()`方法
   - 请求传入`TaskCreateDTO`对象，包含任务名称、类型、描述等信息
   - 服务端调用`TaskServiceImpl.createTask()`方法处理请求

   ```java
   // TaskServiceImpl.java
   @Override
   @Transactional
   public TaskDTO createTask(TaskCreateDTO createDTO) {
       // 创建任务实体
       Task task = new Task();
       task.setTaskName(createDTO.getTaskName());
       task.setTaskType(createDTO.getTaskType());
       task.setDescription(createDTO.getDescription());
       task.setPriority(convertPriorityToCode(createDTO.getPriority()));
       
       // 处理标签
       if (createDTO.getTags() != null && !createDTO.getTags().isEmpty()) {
           task.setTags(String.join(",", createDTO.getTags()));
       }
       
       // 设置默认状态为已调度
       task.setStatus(2); // 2-已调度
       
       // 保存任务
       final Task savedTask = taskRepository.save(task);
       
       // 保存任务设备关联...
       // 保存任务指标关联...
       // 处理调度信息...
       
       return convertToDTO(savedTask);
   }
   ```

2. **配置任务设备和指标**
   - 任务关联多个设备和指标
   - 设备和指标的组合决定了采集任务的具体执行对象
   - 系统创建TaskDevice和TaskMetric实体存储关联关系

3. **配置任务触发器**
   - 通过`TaskScheduleTriggerService.createTrigger()`创建触发器
   - 触发器定义了任务的执行计划，支持多种触发方式
   - 系统计算并设置下次执行时间

   ```java
   // TaskScheduleTriggerServiceImpl.java
   @Override
   @Transactional
   public TaskScheduleTrigger createTrigger(TaskScheduleTriggerDTO triggerDTO) {
       // 检查任务是否存在
       Task task = taskRepository.findById(triggerDTO.getTaskId())
               .orElseThrow(() -> new RuntimeException("任务不存在"));

       // 创建触发器
       TaskScheduleTrigger trigger = new TaskScheduleTrigger();
       BeanUtils.copyProperties(triggerDTO, trigger);
       
       // 设置创建时间和默认值
       trigger.setCreatedAt(LocalDateTime.now());
       trigger.setUpdatedAt(LocalDateTime.now());
       
       if (trigger.getEnabled() == null) {
           trigger.setEnabled(true);
       }
       
       // 计算下次执行时间
       if (trigger.getTriggerType() == 1 && trigger.getCronExpression() != null) {
           trigger.setNextFireTime(calculateNextFireTime(trigger));
       }
       
       return triggerRepository.save(trigger);
   }
   ```

### 3.2 任务调度阶段

1. **定时扫描触发器**
   - `TaskTriggerScheduler`组件定期扫描触发器，默认每30秒一次
   - 查询当前时间点应该触发的触发器
   - 核心实现在`scanAndExecuteTriggers()`方法

   ```java
   // TaskTriggerScheduler.java
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
   ```

2. **触发器执行**
   - 调用`TaskScheduleTriggerService.fireTrigger()`方法
   - 更新触发器状态（上次执行时间、触发次数等）
   - 计算下次执行时间
   - 根据任务类型创建相应的执行批次

   ```java
   // TaskScheduleTriggerServiceImpl.java
   @Override
   @Transactional
   public boolean fireTrigger(Long triggerId) {
       // 查找触发器
       TaskScheduleTrigger trigger = triggerRepository.findById(triggerId)
               .orElseThrow(() -> new RuntimeException("触发器不存在"));
       
       // 更新触发器状态
       trigger.setLastFireTime(LocalDateTime.now());
       trigger.setFiredCount(trigger.getFiredCount() + 1);
       trigger.setNextFireTime(calculateNextFireTime(trigger));
       triggerRepository.save(trigger);
       
       // 获取任务信息
       Long taskId = trigger.getTaskId();
       Task task = taskRepository.findById(taskId)
               .orElseThrow(() -> new RuntimeException("任务不存在"));
       
       // 根据任务类型处理
       if (Boolean.TRUE.equals(task.getIsCollectionTask())) {
           // 采集任务处理逻辑...
       } else {
           // 非采集任务处理逻辑...
       }
   }
   ```

### 3.3 任务准备阶段

1. **创建任务批次**
   - 通过`CollectionTaskService.createTaskBatch()`创建批次
   - 设置批次基本信息，状态初始为PENDING
   - 关联任务ID和采集器ID

   ```java
   // CollectionTaskServiceImpl.java
   @Override
   @Transactional
   public TaskBatch createTaskBatch(Long taskId, Long collectorId, String batchName, String description) {
       TaskBatch batch = new TaskBatch();
       batch.setTaskId(taskId);
       batch.setCollectorId(collectorId);
       batch.setBatchName(batchName);
       batch.setDescription(description);
       batch.setStatus(TaskStatus.PENDING.name());
       batch.setScheduledTime(LocalDateTime.now());
       return taskBatchRepository.save(batch);
   }
   ```

2. **创建采集任务**
   - 根据任务关联的设备和指标组合生成采集任务
   - 每个设备和指标的组合生成一个采集任务
   - 通过`CollectionTaskService.createCollectionTasks()`方法创建

   ```java
   // TaskScheduleTriggerServiceImpl.java中的任务生成逻辑
   List<CollectionTaskDTO> collectionTasks = new ArrayList<>();
   for (TaskDevice device : taskDevices) {
       for (TaskMetric metric : taskMetrics) {
           CollectionTaskDTO collectionTask = new CollectionTaskDTO();
           collectionTask.setDeviceId(device.getDeviceId());
           collectionTask.setMetricId(metric.getMetricId());
           collectionTask.setDeviceName(device.getDeviceName());
           collectionTask.setMetricName(metric.getMetricName());
           collectionTask.setTargetAddress(""); // 采集时通过设备ID查询
           collectionTask.setTargetPort(0);     // 采集时通过设备ID查询
           collectionTask.setCollectType(task.getCollectType());
           collectionTask.setCollectParams(task.getCollectParams());
           collectionTasks.add(collectionTask);
       }
   }
   ```

3. **提交批次**
   - 调用`CollectionTaskService.submitBatch()`提交批次
   - 将批次状态更新为SCHEDULED，表示已调度，等待采集器获取
   - 批次提交后即可被采集器获取和执行

   ```java
   // CollectionTaskServiceImpl.java
   @Override
   @Transactional
   public boolean submitBatch(Long batchId) {
       Optional<TaskBatch> optionalBatch = taskBatchRepository.findById(batchId);
       if (!optionalBatch.isPresent()) {
           return false;
       }
       
       TaskBatch batch = optionalBatch.get();
       if (!TaskStatus.PENDING.name().equals(batch.getStatus())) {
           return false;
       }
       
       batch.setStatus(TaskStatus.SCHEDULED.name());
       taskBatchRepository.save(batch);
       return true;
   }
   ```

### 3.4 任务执行阶段

1. **采集器获取任务**
   - 采集器定期查询待处理的任务批次
   - 根据批次ID获取具体的采集任务

2. **任务执行**
   - 采集器将获取的任务加入本地任务队列
   - 任务调度器根据优先级和计划时间调度任务

3. **任务执行**
   - 任务执行器执行具体的采集逻辑
   - 根据任务类型选择合适的采集插件
   - 生成采集结果并缓存

4. **结果回传**
   - 定期将采集结果回传到服务端
   - 服务端接收结果并更新任务状态

4. **任务状态更新**
   - 服务端接收到结果后更新采集任务状态
   - 当批次中所有任务完成后，更新批次状态
   - 必要时更新任务主体状态

   ```java
   // CollectionTaskServiceImpl.java
   @Override
   @Transactional
   public boolean updateBatchStatus(Long batchId) {
       Optional<CollectionTask> optionalTask = collectionTaskRepository.findById(batchId);
       if (!optionalTask.isPresent()) {
           return false;
       }
       
       CollectionTask task = optionalTask.get();
       Long batchIdFromTask = task.getBatchId();
       
       Optional<TaskBatch> optionalBatch = taskBatchRepository.findById(batchIdFromTask);
       if (!optionalBatch.isPresent()) {
           return false;
       }
       
       TaskBatch batch = optionalBatch.get();
       
       // 统计已完成任务数
       Long completedCount = collectionTaskRepository.countByBatchIdAndStatus(batchIdFromTask, TaskStatus.COMPLETED.name());
       batch.setCompletedTasks(completedCount.intValue());
       
       // 检查是否所有任务都已完成
       if (batch.getCompletedTasks() >= batch.getTotalTasks()) {
           batch.setStatus(TaskStatus.COMPLETED.name());
       } else {
           // 检查是否有运行中的任务
           Long runningCount = collectionTaskRepository.countByBatchIdAndStatus(batchIdFromTask, TaskStatus.RUNNING.name());
           if (runningCount > 0) {
               batch.setStatus(TaskStatus.RUNNING.name());
           }
       }
       
       batch.setLastExecutionTime(LocalDateTime.now());
       taskBatchRepository.save(batch);
       return true;
   }
   ```

## 4. 数据流转详解

### 4.1 任务创建流转

```
用户界面 ──► TaskController.createTask() ──► TaskService.createTask()
                                             │
                                             ▼
                                        创建Task实体
                                             │
                                             ▼
                                    创建TaskDevice关联
                                             │
                                             ▼
                                    创建TaskMetric关联
                                             │
                                             ▼
                              创建TaskScheduleTrigger触发器
                                             │
                                             ▼
                                          数据库
```

### 4.2 任务调度流转

```
TaskTriggerScheduler.scanAndExecuteTriggers()
              │
              ▼
TaskScheduleTriggerService.findTriggerable()
              │
              ▼
TaskScheduleTriggerService.fireTrigger()
              │
              ▼
      更新触发器状态和下次执行时间
              │
              ▼
              │
       ┌──────┴──────┐
       │             │
       ▼             ▼
  采集任务处理     其他任务处理
       │
       ▼
CollectionTaskService.createTaskBatch()
       │
       ▼
CollectionTaskService.createCollectionTasks()
       │
       ▼
CollectionTaskService.submitBatch()
```

### 4.3 任务执行流转

```
采集器 ──► 查询待处理批次 ──► 获取采集任务 ──► 加入本地任务队列
                                                  │
                                                  ▼
                                           执行具体采集操作
                                                  │
                                                  ▼
                                            生成采集结果
                                                  │
                                                  ▼
服务端 ◄── 接收采集结果 ◄── 回传采集结果 ◄─────────┘
  │
  ▼
更新任务状态
  │
  ▼
更新批次状态
  │
  ▼
更新任务主体状态
```

## 5. 关键组件解析

### 5.1 任务触发器 (TaskScheduleTrigger)

任务触发器是决定任务何时执行的关键组件，支持以下触发方式：

1. **Cron触发器** (triggerType=1)
   - 使用cron表达式定义复杂的执行计划
   - 适合定期执行的任务
   - 通过cronExpression字段配置

2. **简单触发器** (triggerType=2)
   - 通过repeatInterval和repeatCount定义重复执行
   - 适合固定间隔执行的任务
   - 通过repeatInterval和repeatCount字段配置

3. **每日触发器** (triggerType=3)
   - 每天固定时间执行
   - 适合日常例行任务
   - 通过startTime和endTime字段配置

4. **事件触发器** (triggerType=4)
   - 由特定事件触发执行
   - 适合响应式任务
   - 通过eventCondition字段配置

### 5.2 任务批次 (TaskBatch)

任务批次代表一次任务执行，是任务实际执行的容器，具有以下特点：

1. **批次状态管理**
   - PENDING: 已创建，等待提交
   - SCHEDULED: 已调度，等待执行
   - RUNNING: 执行中
   - COMPLETED: 已完成
   - FAILED: 执行失败
   - CANCELLED: 已取消

2. **批次进度跟踪**
   - totalTasks: 总任务数
   - completedTasks: 已完成任务数
   - 用于计算执行进度和判断批次是否完成

3. **执行时间管理**
   - scheduledTime: 计划执行时间
   - lastExecutionTime: 最后执行时间
   - 用于任务执行时间控制和统计

### 5.3 采集任务 (CollectionTask)

采集任务是最小的执行单元，具有以下特点：

1. **任务标识**
   - batchId: 所属批次ID
   - deviceId: 设备ID
   - metricId: 指标ID
   - 唯一确定一个采集操作

2. **执行参数**
   - targetAddress: 目标地址
   - targetPort: 目标端口
   - collectType: 采集类型
   - collectParams: 采集参数
   - 定义如何执行采集

3. **状态管理**
   - status: 任务状态
   - startTime: 开始时间
   - endTime: 结束时间
   - executionTime: 执行耗时
   - 跟踪任务执行情况

## 6. 核心接口说明

### 6.1 TaskService

```java
public interface TaskService {
    // 创建任务
    TaskDTO createTask(TaskCreateDTO createDTO);
    
    // 更新任务
    TaskDTO updateTask(Long id, TaskCreateDTO createDTO);
    
    // 删除任务
    void deleteTask(Long id);
    
    // 查询任务
    TaskDTO findTaskById(Long id);
    
    // 分页查询任务
    Page<TaskDTO> findTasksByPage(TaskQueryDTO queryDTO);
    
    // 启动任务
    TaskDTO startTask(Long id);
    
    // 暂停任务
    TaskDTO pauseTask(Long id);
    
    // 停止任务
    TaskDTO stopTask(Long id);
}
```

### 6.2 TaskScheduleTriggerService

```java
public interface TaskScheduleTriggerService {
    // 创建触发器
    TaskScheduleTrigger createTrigger(TaskScheduleTriggerDTO triggerDTO);
    
    // 更新触发器
    TaskScheduleTrigger updateTrigger(Long triggerId, TaskScheduleTriggerDTO triggerDTO);
    
    // 删除触发器
    void deleteTrigger(Long triggerId);
    
    // 启用/禁用触发器
    boolean updateTriggerStatus(Long triggerId, boolean enabled);
    
    // 触发任务
    boolean fireTrigger(Long triggerId);
    
    // 查询可触发的触发器
    List<TaskScheduleTrigger> findTriggerable();
    
    // 计算下次执行时间
    LocalDateTime calculateNextFireTime(TaskScheduleTrigger trigger);
}
```

### 6.3 CollectionTaskService

```java
public interface CollectionTaskService {
    // 创建任务批次
    TaskBatch createTaskBatch(Long taskId, Long collectorId, String batchName, String description);
    
    // 创建采集任务
    List<CollectionTask> createCollectionTasks(Long batchId, List<CollectionTaskDTO> tasks);
    
    // 提交批次
    boolean submitBatch(Long batchId);
    
    // 取消批次
    boolean cancelBatch(Long batchId);
    
    // 获取待处理批次
    List<TaskBatch> getPendingBatches(Long collectorId);
    
    // 获取待处理任务
    List<CollectionTask> getPendingTasks(Long batchId);
    
    // 更新任务状态
    boolean updateTaskStatus(Long taskId, String status);
    
    // 更新批次状态
    boolean updateBatchStatus(Long batchId);
    
    // 保存采集结果
    CollectionResult saveResult(CollectionResultDTO resultDTO);
}
```

## 7. 重要流程详解

### 7.1 触发器执行流程

触发器执行是整个任务调度的核心，完整流程如下：

1. **定时扫描触发器**
   - TaskTriggerScheduler.scanAndExecuteTriggers()定期执行
   - 调用TaskScheduleTriggerService.findTriggerable()获取需要触发的触发器

2. **触发器执行**
   - 调用TaskScheduleTriggerService.fireTrigger()执行触发器
   - 更新触发器状态和下次执行时间
   - 获取任务信息并根据任务类型进行处理

3. **采集任务处理**
   - 创建任务批次
   - 根据设备和指标组合创建采集任务
   - 提交批次等待执行

4. **任务状态更新**
   - 采集任务执行完成后更新状态
   - 批次中所有任务完成后更新批次状态
   - 任务执行完成后更新任务状态

### 7.2 任务批次处理流程

任务批次处理是连接任务调度和任务执行的桥梁，流程如下：

1. **批次创建**
   - 通过TaskScheduleTriggerService.fireTrigger()创建批次
   - 设置批次属性并保存到数据库

2. **采集任务创建**
   - 根据设备和指标组合创建采集任务
   - 每个组合创建一个采集任务实体
   - 设置任务属性并保存到数据库

3. **批次提交**
   - 将批次状态设置为SCHEDULED
   - 批次可以被采集器获取执行

4. **批次状态管理**
   - 批次开始执行时状态更新为RUNNING
   - 所有任务完成时状态更新为COMPLETED
   - 出现错误时状态更新为FAILED

### 7.3 采集器执行流程

采集器是实际执行采集任务的组件，执行流程如下：

1. **获取任务**
   - 采集器定期查询待处理的任务批次
   - 根据批次ID获取具体的采集任务

2. **任务调度**
   - 将任务加入本地任务队列
   - 任务调度器根据优先级和计划时间调度任务

3. **任务执行**
   - 任务执行器执行具体的采集逻辑
   - 根据任务类型选择合适的采集插件
   - 生成采集结果并缓存

4. **结果回传**
   - 定期将采集结果回传到服务端
   - 服务端接收结果并更新任务状态

## 8. 常见问题与解决方案

### 8.1 触发器执行问题

**问题**: 触发器没有按预期时间执行

**解决方案**:
- 检查触发器的enabled状态是否为true
- 检查cronExpression表达式是否正确
- 检查nextFireTime是否已正确计算
- 确保TaskTriggerScheduler正常运行

### 8.2 任务批次状态不更新

**问题**: 任务执行完成后批次状态没有更新

**解决方案**:
- 检查updateBatchStatus方法是否被正确调用
- 确认采集结果是否正确回传到服务端
- 检查批次中的任务状态是否正确更新
- 排查任务执行过程中的异常情况

### 8.3 采集器连接问题

**问题**: 采集器无法获取或执行任务

**解决方案**:
- 检查采集器与服务端的网络连接
- 确认采集器ID是否正确配置
- 检查服务端API接口是否正常工作
- 排查采集器本地错误日志

### 8.4 性能优化建议

- 对触发器表添加NextFireTime和Enabled的复合索引，提高查询效率
- 批量处理采集任务，减少数据库操作次数
- 实现任务优先级调度，保证重要任务优先执行
- 采用异步处理结果，避免阻塞主流程
- 实现任务执行超时检测和自动取消机制

## 9. 扩展点

### 9.1 新增触发器类型

系统支持添加新的触发器类型，需要执行以下步骤：

1. 在TaskScheduleTrigger实体中添加新触发类型的相关字段
2. 在TaskScheduleTriggerServiceImpl的calculateNextFireTime方法中添加新类型的时间计算逻辑
3. 在fireTrigger方法中添加新类型的处理逻辑

### 9.2 新增任务类型

系统支持添加新的任务类型，需要执行以下步骤：

1. 在Task实体中添加新任务类型的相关字段
2. 在TaskServiceImpl中添加新类型的处理逻辑
3. 在TaskScheduleTriggerServiceImpl的fireTrigger方法中添加新类型的触发逻辑

### 9.3 采集器插件扩展

采集器支持通过插件机制扩展采集能力，需要执行以下步骤：

1. 实现Collector.Plugin接口
2. 在采集器的PluginManager中注册新插件
3. 确保服务端的任务配置支持新的采集类型

## 10. 总结

SkyEye任务调度系统采用分层设计，实现了从任务创建、调度、执行到结果处理的完整生命周期管理。系统通过任务触发器实现灵活的调度策略，通过任务批次和采集任务实现高效的任务执行，通过结果回传和状态更新实现完整的闭环管理。

系统的核心流程包括：
1. 任务创建与配置
2. 任务调度与触发
3. 任务批次管理
4. 采集任务执行
5. 结果处理与状态更新

通过这种设计，系统能够实现高效、可靠、可扩展的任务调度与执行，满足各种监控场景的需求。
