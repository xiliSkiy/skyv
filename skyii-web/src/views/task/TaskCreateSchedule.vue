<template>
  <div class="task-create-schedule-container">
    <!-- 标题区域 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4>创建采集任务</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/task' }">采集任务调度</el-breadcrumb-item>
          <el-breadcrumb-item>创建任务</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="action-buttons">
        <el-button plain size="small" @click="saveDraft" class="me-2">
          <el-icon><Document /></el-icon> 保存草稿
        </el-button>
        <el-button plain size="small" @click="cancel" type="danger">
          <el-icon><Close /></el-icon> 取消
        </el-button>
      </div>
    </div>

    <!-- 步骤指示器 -->
    <div class="step-indicator mb-4">
      <el-steps :active="currentStep" finish-status="success">
        <el-step title="基本信息" />
        <el-step title="设备选择" />
        <el-step title="指标配置" />
        <el-step title="调度设置" />
      </el-steps>
    </div>

    <!-- 调度设置内容 -->
    <el-row :gutter="20">
      <el-col :span="16">
        <!-- 调度类型 -->
        <el-card class="mb-4">
          <template #header>
            <div>
              <el-icon><Calendar /></el-icon> 调度类型
            </div>
          </template>

          <!-- 实时执行 -->
          <div 
            class="schedule-option" 
            :class="{ active: scheduleForm.scheduleType === 'realtime' }"
            @click="selectScheduleType('realtime')"
          >
            <el-radio v-model="scheduleForm.scheduleType" label="realtime">
              <span class="fw-bold">实时执行</span>
            </el-radio>
            <div class="ms-4 mt-2">
              <p class="text-muted mb-0">任务将立即开始执行，持续采集数据直到手动停止</p>
            </div>
          </div>

          <!-- 定时执行 -->
          <div 
            class="schedule-option" 
            :class="{ active: scheduleForm.scheduleType === 'scheduled' }"
            @click="selectScheduleType('scheduled')"
          >
            <el-radio v-model="scheduleForm.scheduleType" label="scheduled">
              <span class="fw-bold">定时执行</span>
            </el-radio>
            <div class="ms-4">
              <p class="text-muted mb-3">在指定的时间执行任务，可设置重复周期</p>
              
              <div class="mb-3">
                <el-form-item label="执行频率">
                  <el-select v-model="scheduleForm.frequency" style="width: 100%">
                    <el-option label="单次执行" value="once" />
                    <el-option label="每天" value="daily" />
                    <el-option label="每周" value="weekly" />
                    <el-option label="每月" value="monthly" />
                    <el-option label="自定义" value="custom" />
                  </el-select>
                </el-form-item>
              </div>
              
              <div class="mb-3">
                <el-form-item label="执行时间">
                  <el-time-picker
                    v-model="scheduleForm.scheduleTime"
                    format="HH:mm"
                    placeholder="选择时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </div>
              
              <div class="mb-3" v-if="scheduleForm.frequency === 'weekly'">
                <el-form-item label="重复周期">
                  <div class="weekday-selector">
                    <el-button
                      v-for="(day, index) in weekdays"
                      :key="index"
                      :class="{ selected: scheduleForm.weekdays.includes(index) }"
                      class="weekday-btn"
                      @click="toggleWeekday(index)"
                    >
                      {{ day }}
                    </el-button>
                  </div>
                </el-form-item>
              </div>
            </div>
          </div>

          <!-- 周期执行 -->
          <div 
            class="schedule-option" 
            :class="{ active: scheduleForm.scheduleType === 'periodic' }"
            @click="selectScheduleType('periodic')"
          >
            <el-radio v-model="scheduleForm.scheduleType" label="periodic">
              <span class="fw-bold">周期执行</span>
            </el-radio>
            <div class="ms-4">
              <p class="text-muted mb-3">按固定时间间隔循环执行任务</p>
              
              <div class="mb-3">
                <el-form-item label="间隔时间">
                  <div class="d-flex align-items-center">
                    <el-input-number
                      v-model="scheduleForm.intervalValue"
                      :min="1"
                      controls-position="right"
                      style="width: 150px"
                    />
                    <el-select
                      v-model="scheduleForm.intervalUnit"
                      style="width: 120px; margin-left: 10px"
                    >
                      <el-option label="分钟" value="minutes" />
                      <el-option label="小时" value="hours" />
                      <el-option label="天" value="days" />
                    </el-select>
                  </div>
                </el-form-item>
              </div>
            </div>
          </div>

          <!-- 触发执行 -->
          <div 
            class="schedule-option" 
            :class="{ active: scheduleForm.scheduleType === 'triggered' }"
            @click="selectScheduleType('triggered')"
          >
            <el-radio v-model="scheduleForm.scheduleType" label="triggered">
              <span class="fw-bold">触发执行</span>
            </el-radio>
            <div class="ms-4">
              <p class="text-muted mb-3">当满足特定条件时执行任务</p>
              
              <div class="mb-3">
                <el-form-item label="触发类型">
                  <el-select v-model="scheduleForm.triggerType" style="width: 100%">
                    <el-option label="事件触发" value="event" />
                    <el-option label="阈值触发" value="threshold" />
                    <el-option label="API调用触发" value="api" />
                  </el-select>
                </el-form-item>
              </div>
              
              <div class="mb-3">
                <el-form-item label="触发事件">
                  <el-select v-model="scheduleForm.triggerEvent" style="width: 100%">
                    <el-option label="运动检测" value="motion" />
                    <el-option label="异常行为" value="anomaly" />
                    <el-option label="报警触发" value="alarm" />
                  </el-select>
                </el-form-item>
              </div>
            </div>
          </div>
        </el-card>

        <!-- 执行限制 -->
        <el-card>
          <template #header>
            <div>
              <el-icon><Stopwatch /></el-icon> 执行限制
            </div>
          </template>

          <div class="mb-4">
            <el-form-item>
              <el-switch
                v-model="scheduleForm.enableDateRange"
                active-text="设置有效期"
              />
            </el-form-item>
            
            <el-row :gutter="20" v-if="scheduleForm.enableDateRange">
              <el-col :span="12">
                <el-form-item label="开始日期">
                  <el-date-picker
                    v-model="scheduleForm.startDate"
                    type="date"
                    placeholder="选择开始日期"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束日期">
                  <el-date-picker
                    v-model="scheduleForm.endDate"
                    type="date"
                    placeholder="选择结束日期"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          
          <div class="mb-4">
            <el-form-item>
              <el-switch
                v-model="scheduleForm.enableTimeWindow"
                active-text="设置每日执行时间窗口"
              />
            </el-form-item>
            
            <el-row :gutter="20" v-if="scheduleForm.enableTimeWindow">
              <el-col :span="12">
                <el-form-item label="开始时间">
                  <el-time-picker
                    v-model="scheduleForm.startTime"
                    format="HH:mm"
                    placeholder="选择开始时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
              <el-col :span="12">
                <el-form-item label="结束时间">
                  <el-time-picker
                    v-model="scheduleForm.endTime"
                    format="HH:mm"
                    placeholder="选择结束时间"
                    style="width: 100%"
                  />
                </el-form-item>
              </el-col>
            </el-row>
          </div>
          
          <div>
            <el-form-item>
              <el-switch
                v-model="scheduleForm.enableMaxExecutions"
                active-text="限制最大执行次数"
              />
            </el-form-item>
            
            <div v-if="scheduleForm.enableMaxExecutions">
              <el-form-item label="最大执行次数">
                <el-input-number
                  v-model="scheduleForm.maxExecutions"
                  :min="1"
                  controls-position="right"
                  style="width: 150px"
                />
              </el-form-item>
            </div>
          </div>
        </el-card>
      </el-col>

      <el-col :span="8">
        <!-- 高级设置 -->
        <el-card class="mb-4">
          <template #header>
            <div>
              <el-icon><Setting /></el-icon> 高级设置
            </div>
          </template>

          <div class="mb-3">
            <el-form-item label="任务超时设置">
              <div class="d-flex align-items-center">
                <el-input-number
                  v-model="scheduleForm.timeout"
                  :min="1"
                  controls-position="right"
                  style="width: 150px"
                />
                <span class="ms-2">分钟</span>
              </div>
              <div class="form-text">任务执行超过此时间将被自动终止</div>
            </el-form-item>
          </div>
          
          <div class="mb-3">
            <el-form-item label="失败重试策略">
              <el-select v-model="scheduleForm.retryStrategy" style="width: 100%">
                <el-option label="不重试" value="none" />
                <el-option label="立即重试" value="immediate" />
                <el-option label="递增间隔重试" value="incremental" />
              </el-select>
            </el-form-item>
          </div>
          
          <div class="mb-3">
            <el-form-item label="最大重试次数">
              <el-input-number
                v-model="scheduleForm.maxRetries"
                :min="0"
                controls-position="right"
                style="width: 150px"
              />
            </el-form-item>
          </div>
          
          <div class="mb-3">
            <el-form-item label="调度优先级">
              <el-select v-model="scheduleForm.priorityLevel" style="width: 100%">
                <el-option label="高" value="high" />
                <el-option label="中" value="normal" />
                <el-option label="低" value="low" />
              </el-select>
              <div class="form-text">高优先级任务将优先分配资源</div>
            </el-form-item>
          </div>
          
          <div class="mb-3">
            <el-form-item>
              <el-switch
                v-model="scheduleForm.enableNotifications"
                active-text="启用任务状态通知"
              />
            </el-form-item>
          </div>
          
          <div class="mb-3">
            <el-form-item>
              <el-switch
                v-model="scheduleForm.enableAutoRestart"
                active-text="失败自动重启"
              />
            </el-form-item>
          </div>
        </el-card>

        <!-- 调度建议 -->
        <el-card>
          <template #header>
            <div>
              <el-icon><InfoFilled /></el-icon> 调度建议
            </div>
          </template>

          <ul class="small text-muted mb-0">
            <li class="mb-2">视频分析类任务建议选择固定时间段执行，避免系统资源持续占用</li>
            <li class="mb-2">传感器数据采集适合使用周期执行，间隔时间根据数据变化频率设定</li>
            <li class="mb-2">对于重要场所监控，建议使用触发执行结合实时执行的方式</li>
            <li>设置合理的超时时间和重试策略，避免长时间任务阻塞系统</li>
          </ul>
        </el-card>
      </el-col>
    </el-row>

    <!-- 底部按钮 -->
    <div class="action-footer d-flex justify-content-end mt-4">
      <el-button @click="prevStep" class="me-2">
        <el-icon class="el-icon--left"><ArrowLeft /></el-icon> 上一步
      </el-button>
      <el-button type="primary" @click="submitTask">
        提交任务 <el-icon class="el-icon--right"><Check /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Close, Calendar, Stopwatch, Setting, InfoFilled, ArrowLeft, Check } from '@element-plus/icons-vue'
import { createTask, saveTaskDraft, updateTask } from '@/api/task'

const router = useRouter()
const route = useRoute()

// 判断是否为编辑模式
const isEdit = ref(false)
const taskId = ref(null)

// 当前步骤
const currentStep = ref(3)

// 周日到周六
const weekdays = ['日', '一', '二', '三', '四', '五', '六']

// 调度表单
const scheduleForm = reactive({
  // 调度类型
  scheduleType: 'realtime', // realtime, scheduled, periodic, triggered
  
  // 定时执行
  frequency: 'daily', // once, daily, weekly, monthly, custom
  scheduleTime: new Date(2023, 0, 1, 8, 30), // 默认8:30
  weekdays: [1, 2, 3, 4, 5], // 默认周一到周五
  
  // 周期执行
  intervalValue: 30,
  intervalUnit: 'minutes', // minutes, hours, days
  
  // 触发执行
  triggerType: 'event', // event, threshold, api
  triggerEvent: 'motion', // motion, anomaly, alarm
  
  // 执行限制
  enableDateRange: true,
  startDate: new Date(),
  endDate: new Date(new Date().setMonth(new Date().getMonth() + 3)), // 默认3个月后
  
  enableTimeWindow: false,
  startTime: null,
  endTime: null,
  
  enableMaxExecutions: false,
  maxExecutions: 10,
  
  // 高级设置
  timeout: 30,
  retryStrategy: 'incremental', // none, immediate, incremental
  maxRetries: 3,
  priorityLevel: 'normal', // high, normal, low
  enableNotifications: true,
  enableAutoRestart: false
})

// 选择调度类型
const selectScheduleType = (type) => {
  scheduleForm.scheduleType = type
}

// 切换周几
const toggleWeekday = (day) => {
  const index = scheduleForm.weekdays.indexOf(day)
  if (index === -1) {
    scheduleForm.weekdays.push(day)
  } else {
    scheduleForm.weekdays.splice(index, 1)
  }
}

// 上一步
const prevStep = () => {
  // 保存当前调度设置
  saveCurrentStepData()
  router.push('/task/create/metrics')
}

// 提交任务
const submitTask = async () => {
  // 保存当前调度设置
  saveCurrentStepData()
  
  // 从本地存储获取完整的任务数据
  const taskData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  
  // 检查是否包含设备信息
  if (!taskData.devices || taskData.devices.length === 0) {
    ElMessage.error('请先选择设备')
    return
  }
  
  // 确认设备数据格式正确
  const hasInvalidDeviceData = taskData.devices.some(device => !device.deviceId);
  if (hasInvalidDeviceData) {
    ElMessage.error('设备数据格式不正确，请重新选择设备')
    return
  }
  
  // 确认提交
  try {
    const confirmMessage = isEdit.value ? '确认更新任务吗？' : '确认创建任务吗？'
    await ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    // 提交任务
    console.log('提交的任务数据:', taskData)
    
    if (isEdit.value && taskId.value) {
      // 编辑模式：更新任务
      await updateTask(taskId.value, taskData)
      ElMessage.success('任务更新成功')
    } else {
      // 创建模式：创建新任务
      await createTask(taskData)
      ElMessage.success('任务创建成功')
    }
    
    // 清除本地存储
    localStorage.removeItem('taskCreateData')
    localStorage.removeItem('taskDraftId')
    localStorage.removeItem('taskEditData')
    
    router.push('/task')
  } catch (error) {
    if (error === 'cancel') return
    
    console.error(isEdit.value ? '更新任务失败' : '创建任务失败', error)
    ElMessage.error(isEdit.value ? '更新任务失败' : '创建任务失败')
  }
}

// 保存当前步骤数据
const saveCurrentStepData = () => {
  // 从本地存储获取之前的数据
  const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  
  // 合并数据并保存
  const currentData = {
    ...prevData,
    schedule: {
      scheduleType: scheduleForm.scheduleType,
      
      // 根据调度类型保存相关配置
      ...getScheduleConfig(),
      
      // 执行限制
      limits: {
        enableDateRange: scheduleForm.enableDateRange,
        startDate: scheduleForm.enableDateRange ? scheduleForm.startDate : null,
        endDate: scheduleForm.enableDateRange ? scheduleForm.endDate : null,
        
        enableTimeWindow: scheduleForm.enableTimeWindow,
        startTime: scheduleForm.enableTimeWindow ? scheduleForm.startTime : null,
        endTime: scheduleForm.enableTimeWindow ? scheduleForm.endTime : null,
        
        enableMaxExecutions: scheduleForm.enableMaxExecutions,
        maxExecutions: scheduleForm.enableMaxExecutions ? scheduleForm.maxExecutions : null
      },
      
      // 高级设置
      advanced: {
        timeout: scheduleForm.timeout,
        retryStrategy: scheduleForm.retryStrategy,
        maxRetries: scheduleForm.maxRetries,
        priorityLevel: scheduleForm.priorityLevel,
        enableNotifications: scheduleForm.enableNotifications,
        enableAutoRestart: scheduleForm.enableAutoRestart
      }
    },
    step: 4
  }
  
  localStorage.setItem('taskCreateData', JSON.stringify(currentData))
}

// 获取调度配置
const getScheduleConfig = () => {
  switch (scheduleForm.scheduleType) {
    case 'scheduled':
      return {
        frequency: scheduleForm.frequency,
        scheduleTime: scheduleForm.scheduleTime,
        weekdays: scheduleForm.frequency === 'weekly' ? scheduleForm.weekdays : []
      }
    case 'periodic':
      return {
        intervalValue: scheduleForm.intervalValue,
        intervalUnit: scheduleForm.intervalUnit
      }
    case 'triggered':
      return {
        triggerType: scheduleForm.triggerType,
        triggerEvent: scheduleForm.triggerEvent
      }
    default:
      return {}
  }
}

// 保存草稿
const saveDraft = async () => {
  try {
    // 从本地存储获取之前的数据
    const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
    
    // 合并数据
    const draftData = {
      ...prevData,
      schedule: {
        scheduleType: scheduleForm.scheduleType,
        ...getScheduleConfig(),
        limits: {
          enableDateRange: scheduleForm.enableDateRange,
          startDate: scheduleForm.enableDateRange ? scheduleForm.startDate : null,
          endDate: scheduleForm.enableDateRange ? scheduleForm.endDate : null,
          enableTimeWindow: scheduleForm.enableTimeWindow,
          startTime: scheduleForm.enableTimeWindow ? scheduleForm.startTime : null,
          endTime: scheduleForm.enableTimeWindow ? scheduleForm.endTime : null,
          enableMaxExecutions: scheduleForm.enableMaxExecutions,
          maxExecutions: scheduleForm.enableMaxExecutions ? scheduleForm.maxExecutions : null
        },
        advanced: {
          timeout: scheduleForm.timeout,
          retryStrategy: scheduleForm.retryStrategy,
          maxRetries: scheduleForm.maxRetries,
          priorityLevel: scheduleForm.priorityLevel,
          enableNotifications: scheduleForm.enableNotifications,
          enableAutoRestart: scheduleForm.enableAutoRestart
        }
      },
      step: 3
    }
    
    const res = await saveTaskDraft(draftData)
    ElMessage.success('草稿保存成功')
    
    // 保存草稿ID，用于后续恢复
    if (res.data && res.data.draftId) {
      localStorage.setItem('taskDraftId', res.data.draftId)
    }
  } catch (error) {
    console.error('保存草稿失败', error)
    ElMessage.error('保存草稿失败')
  }
}

// 取消
const cancel = () => {
  ElMessage.info('已取消创建任务')
  router.push('/task')
}

// 恢复之前的配置
const restorePreviousConfig = () => {
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      if (data.schedule) {
        // 恢复调度类型
        scheduleForm.scheduleType = data.schedule.scheduleType || 'realtime'
        
        // 根据调度类型恢复相关配置
        if (data.schedule.frequency) scheduleForm.frequency = data.schedule.frequency
        if (data.schedule.scheduleTime) scheduleForm.scheduleTime = new Date(data.schedule.scheduleTime)
        if (data.schedule.weekdays) scheduleForm.weekdays = data.schedule.weekdays
        
        if (data.schedule.intervalValue) scheduleForm.intervalValue = data.schedule.intervalValue
        if (data.schedule.intervalUnit) scheduleForm.intervalUnit = data.schedule.intervalUnit
        
        if (data.schedule.triggerType) scheduleForm.triggerType = data.schedule.triggerType
        if (data.schedule.triggerEvent) scheduleForm.triggerEvent = data.schedule.triggerEvent
        
        // 恢复执行限制
        if (data.schedule.limits) {
          scheduleForm.enableDateRange = data.schedule.limits.enableDateRange || false
          if (data.schedule.limits.startDate) scheduleForm.startDate = new Date(data.schedule.limits.startDate)
          if (data.schedule.limits.endDate) scheduleForm.endDate = new Date(data.schedule.limits.endDate)
          
          scheduleForm.enableTimeWindow = data.schedule.limits.enableTimeWindow || false
          if (data.schedule.limits.startTime) scheduleForm.startTime = new Date(data.schedule.limits.startTime)
          if (data.schedule.limits.endTime) scheduleForm.endTime = new Date(data.schedule.limits.endTime)
          
          scheduleForm.enableMaxExecutions = data.schedule.limits.enableMaxExecutions || false
          if (data.schedule.limits.maxExecutions) scheduleForm.maxExecutions = data.schedule.limits.maxExecutions
        }
        
        // 恢复高级设置
        if (data.schedule.advanced) {
          if (data.schedule.advanced.timeout) scheduleForm.timeout = data.schedule.advanced.timeout
          if (data.schedule.advanced.retryStrategy) scheduleForm.retryStrategy = data.schedule.advanced.retryStrategy
          if (data.schedule.advanced.maxRetries) scheduleForm.maxRetries = data.schedule.advanced.maxRetries
          if (data.schedule.advanced.priorityLevel) scheduleForm.priorityLevel = data.schedule.advanced.priorityLevel
          scheduleForm.enableNotifications = data.schedule.advanced.enableNotifications !== undefined ? 
            data.schedule.advanced.enableNotifications : true
          scheduleForm.enableAutoRestart = data.schedule.advanced.enableAutoRestart || false
        }
      }
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }
}

// 修复本地存储中的设备数据格式
const fixLocalStorageDeviceData = () => {
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      if (data.devices && Array.isArray(data.devices)) {
        // 检查并修复设备数据格式
        const fixedDevices = data.devices.map(device => {
          // 如果有id但没有deviceId，则将id转为deviceId
          if (device.id && !device.deviceId) {
            return {
              ...device,
              deviceId: device.id
            }
          }
          return device
        })
        
        // 更新本地存储
        localStorage.setItem('taskCreateData', JSON.stringify({
          ...data,
          devices: fixedDevices
        }))
      }
    } catch (error) {
      console.error('修复本地存储数据失败', error)
    }
  }
}

// 页面初始化
onMounted(() => {
  // 从本地存储获取之前步骤的数据
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      currentStep.value = data.step || 3
      
      // 检查是否是编辑模式
      isEdit.value = data.isEdit || false
      taskId.value = data.taskId || null
      
      console.log('当前模式:', isEdit.value ? '编辑任务' : '创建任务', '任务ID:', taskId.value)
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }
  
  // 恢复之前的配置
  restorePreviousConfig()
})
</script>

<style scoped>
.schedule-option {
  padding: 15px;
  border: 1px solid #e9ecef;
  border-radius: 8px;
  margin-bottom: 15px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.schedule-option:hover {
  border-color: #adb5bd;
}

.schedule-option.active {
  border-color: #409EFF;
  background-color: rgba(64, 158, 255, 0.05);
}

.weekday-selector {
  display: flex;
  gap: 5px;
  margin-top: 10px;
}

.weekday-btn {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 500;
}

.weekday-btn.selected {
  background-color: #409EFF;
  color: white;
}

.form-text {
  font-size: 12px;
  color: #6c757d;
  margin-top: 5px;
}

.action-buttons {
  display: flex;
  align-items: center;
}

.action-footer {
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 30px;
}
</style> 