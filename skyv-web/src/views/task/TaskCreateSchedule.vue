<template>
  <div class="task-create-schedule-container">
    <h4>创建采集任务 - 调度设置</h4>
    
    <!-- 步骤指示器 -->
    <div class="step-indicator mb-4">
      <el-steps :active="2" finish-status="success">
        <el-step title="基本信息" />
        <el-step title="设备选择" />
        <el-step title="调度设置" />
      </el-steps>
    </div>

    <!-- 调度设置内容 -->
    <el-card>
      <template #header>
        <span>调度设置</span>
      </template>

      <el-form ref="scheduleFormRef" :model="scheduleForm" :rules="scheduleRules" label-width="120px">
        <!-- 调度类型 -->
        <el-form-item label="调度类型" prop="scheduleType">
          <el-radio-group v-model="scheduleForm.scheduleType" @change="onScheduleTypeChange">
            <el-radio value="once">
              <el-icon><Clock /></el-icon>
              一次性执行
            </el-radio>
            <el-radio value="interval">
              <el-icon><Timer /></el-icon>
              周期执行
            </el-radio>
            <el-radio value="cron">
              <el-icon><Setting /></el-icon>
              Cron表达式
            </el-radio>
          </el-radio-group>
          <div class="form-help mt-2">
            <span v-if="scheduleForm.scheduleType === 'once'" class="text-muted">
              任务将在指定时间执行一次
            </span>
            <span v-else-if="scheduleForm.scheduleType === 'interval'" class="text-muted">
              任务将按照设定的间隔时间重复执行
            </span>
            <span v-else-if="scheduleForm.scheduleType === 'cron'" class="text-muted">
              使用Cron表达式来精确控制执行时间
            </span>
          </div>
        </el-form-item>

        <!-- 一次性执行配置 -->
        <template v-if="scheduleForm.scheduleType === 'once'">
          <el-form-item label="执行时间" prop="executionTime" required>
          <el-date-picker
            v-model="scheduleForm.executionTime"
            type="datetime"
            placeholder="选择执行时间"
            style="width: 100%"
              :disabled-date="disablePastDate"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
            <div class="form-help mt-1 text-muted">
              请选择任务执行的具体时间，不能早于当前时间
            </div>
          </el-form-item>
        </template>

        <!-- 周期执行配置 -->
        <template v-if="scheduleForm.scheduleType === 'interval'">
          <el-form-item label="开始时间" prop="startTime">
            <el-date-picker
              v-model="scheduleForm.startTime"
              type="datetime"
              placeholder="选择开始时间"
              style="width: 100%"
              :disabled-date="disablePastDate"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          
          <el-form-item label="执行间隔" prop="intervalValue" required>
            <el-row :gutter="10">
              <el-col :span="12">
                <el-input
                  v-model.number="scheduleForm.intervalValue"
                  type="number"
                  :min="1"
                  :max="9999"
                  placeholder="输入间隔值"
                  style="width: 100%"
                  @input="handleIntervalValueInput"
                />
              </el-col>
              <el-col :span="12">
                <el-select v-model="scheduleForm.intervalUnit" style="width: 100%">
                  <el-option label="秒" value="seconds" />
                  <el-option label="分钟" value="minutes" />
                  <el-option label="小时" value="hours" />
                  <el-option label="天" value="days" />
                </el-select>
              </el-col>
            </el-row>
            <div class="form-help mt-1 text-muted">
              任务将每隔 {{ scheduleForm.intervalValue }} {{ getIntervalUnitText(scheduleForm.intervalUnit) }} 执行一次
            </div>
          </el-form-item>

          <el-form-item label="结束时间">
            <el-row :gutter="10" style="align-items: center;">
              <el-col :span="4">
                <el-checkbox v-model="scheduleForm.hasEndTime">设置结束时间</el-checkbox>
              </el-col>
              <el-col :span="20">
                <el-date-picker
                  v-if="scheduleForm.hasEndTime"
                  v-model="scheduleForm.endTime"
                  type="datetime"
                  placeholder="选择结束时间"
                  style="width: 100%"
                  format="YYYY-MM-DD HH:mm:ss"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </el-col>
            </el-row>
            <div v-if="scheduleForm.hasEndTime" class="form-help mt-1 text-muted">
              任务将在结束时间后停止执行
            </div>
          </el-form-item>
        </template>

        <!-- Cron表达式配置 -->
        <template v-if="scheduleForm.scheduleType === 'cron'">
          <el-form-item label="Cron表达式" prop="cronExpression" required>
            <el-input
              v-model="scheduleForm.cronExpression"
              placeholder="例如: 0 0 12 * * ? (每天中午12点执行)"
              style="width: 100%"
            />
            <div class="form-help mt-1">
              <div class="text-muted mb-2">
                Cron表达式格式：秒 分 时 日 月 周 [年]
              </div>
              <div class="cron-examples">
                <div class="mb-1"><code>0 0 12 * * ?</code> - 每天中午12点执行</div>
                <div class="mb-1"><code>0 */15 * * * ?</code> - 每15分钟执行一次</div>
                <div class="mb-1"><code>0 0 9-18 * * MON-FRI</code> - 工作日9-18点每小时执行</div>
                <div class="mb-1"><code>0 0 0 1 * ?</code> - 每月1号零点执行</div>
              </div>
            </div>
          </el-form-item>

          <el-form-item label="开始时间">
            <el-date-picker
              v-model="scheduleForm.startTime"
              type="datetime"
              placeholder="选择开始时间（可选）"
              style="width: 100%"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
            <div class="form-help mt-1 text-muted">
              如不设置，任务将立即按Cron表达式开始调度
            </div>
          </el-form-item>
        </template>

        <!-- 高级选项 -->
        <el-form-item>
          <el-collapse v-model="activeAdvanced">
            <el-collapse-item title="高级选项" name="advanced">
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="任务优先级" label-width="100px">
                    <el-select v-model="scheduleForm.priority" style="width: 100%">
                      <el-option label="最高" :value="1" />
                      <el-option label="高" :value="2" />
                      <el-option label="普通" :value="5" />
                      <el-option label="低" :value="8" />
                      <el-option label="最低" :value="10" />
                    </el-select>
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="超时时间(秒)" label-width="100px">
                    <el-input-number
                      v-model="scheduleForm.timeout"
                      :min="30"
                      :max="3600"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>
              
              <el-row :gutter="20">
                <el-col :span="12">
                  <el-form-item label="并发执行数" label-width="100px">
                    <el-input-number
                      v-model="scheduleForm.maxConcurrency"
                      :min="1"
                      :max="20"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
                <el-col :span="12">
                  <el-form-item label="重试次数" label-width="100px">
                    <el-input-number
                      v-model="scheduleForm.retryTimes"
                      :min="0"
                      :max="10"
                      style="width: 100%"
                    />
                  </el-form-item>
                </el-col>
              </el-row>

              <el-form-item label="任务描述" label-width="100px">
                <el-input
                  v-model="scheduleForm.remarks"
                  type="textarea"
                  :rows="3"
                  placeholder="输入任务描述或备注信息"
                  maxlength="500"
                  show-word-limit
                />
              </el-form-item>
            </el-collapse-item>
          </el-collapse>
        </el-form-item>

        <!-- 底部按钮 -->
        <div class="action-footer d-flex justify-content-between mt-4">
          <el-button @click="prevStep">上一步</el-button>
          <el-button type="primary" @click="handleCreateTask">创建任务</el-button>
        </div>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Clock, Timer, Setting } from '@element-plus/icons-vue'
import { createTask } from '@/api/task'

const router = useRouter()

// 表单引用
const scheduleFormRef = ref(null)

// 高级选项展开状态
const activeAdvanced = ref([])

// 表单数据
const scheduleForm = reactive({
  scheduleType: 'interval',
  // 一次性执行
  executionTime: null,
  // 周期执行
  startTime: null,
  intervalValue: 5,
  intervalUnit: 'minutes',
  hasEndTime: false,
  endTime: null,
  // Cron表达式
  cronExpression: '',
  // 高级选项
  priority: 5,
  timeout: 300,
  maxConcurrency: 5,
  retryTimes: 3,
  remarks: ''
})

// 调试：监听intervalValue变化
watch(() => scheduleForm.intervalValue, (newVal) => {
  console.log('intervalValue changed:', newVal)
})

// 表单验证规则
const scheduleRules = {
  scheduleType: [
    { required: true, message: '请选择调度类型', trigger: 'change' }
  ],
  executionTime: [
    { required: true, message: '请选择执行时间', trigger: 'change' }
  ],
  intervalValue: [
    { required: true, message: '请输入执行间隔', trigger: 'blur' },
    { type: 'number', min: 1, max: 9999, message: '间隔值必须在1-9999之间', trigger: 'blur' }
  ],
  cronExpression: [
    { required: true, message: '请输入Cron表达式', trigger: 'blur' },
    { pattern: /^(\S+\s+){5}\S+$/, message: 'Cron表达式格式不正确', trigger: 'blur' }
  ]
}

// 禁用过去的日期
const disablePastDate = (time) => {
  return time.getTime() < Date.now() - 8.64e7 // 禁用昨天之前的日期
}

// 获取间隔单位文本
const getIntervalUnitText = (unit) => {
  const unitMap = {
    seconds: '秒',
    minutes: '分钟',
    hours: '小时',
    days: '天'
  }
  return unitMap[unit] || unit
}

// 处理间隔值输入
const handleIntervalValueInput = (value) => {
  // 确保输入的是有效数字
  const numValue = parseInt(value)
  if (!isNaN(numValue) && numValue >= 1 && numValue <= 9999) {
    scheduleForm.intervalValue = numValue
  } else if (isNaN(numValue) || numValue < 1) {
    scheduleForm.intervalValue = 1
  } else if (numValue > 9999) {
    scheduleForm.intervalValue = 9999
  }
}

// 调度类型变更处理
const onScheduleTypeChange = (type) => {
  // 清空其他类型的数据
  if (type === 'once') {
    scheduleForm.startTime = null
    scheduleForm.cronExpression = ''
    // 重置间隔相关字段
    scheduleForm.intervalValue = 5
    scheduleForm.intervalUnit = 'minutes'
    scheduleForm.hasEndTime = false
    scheduleForm.endTime = null
  } else if (type === 'interval') {
    scheduleForm.executionTime = null
    scheduleForm.cronExpression = ''
    // 确保间隔字段有默认值
    if (!scheduleForm.intervalValue || scheduleForm.intervalValue <= 0) {
      scheduleForm.intervalValue = 5
    }
    if (!scheduleForm.intervalUnit) {
      scheduleForm.intervalUnit = 'minutes'
    }
  } else if (type === 'cron') {
    scheduleForm.executionTime = null
    scheduleForm.startTime = null
    // 重置间隔相关字段
    scheduleForm.intervalValue = 5
    scheduleForm.intervalUnit = 'minutes'
    scheduleForm.hasEndTime = false
    scheduleForm.endTime = null
  }
}

// 上一步
const prevStep = () => {
  router.push('/task/create/device')
}

// 创建任务
const handleCreateTask = async () => {
  try {
    // 表单验证
    const valid = await scheduleFormRef.value.validate()
    if (!valid) {
      ElMessage.warning('请完善调度配置信息')
      return
    }

    // 构建调度配置
    const scheduleConfig = buildScheduleConfig()
    
    // 获取所有步骤的数据
    const allData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
    
    // 构建任务创建请求（排除不需要的metricsConfig）
    const { metricsConfig, ...cleanData } = allData
    const taskRequest = {
      ...cleanData,
      scheduleType: scheduleForm.scheduleType,
      scheduleConfig: scheduleConfig,
      priority: scheduleForm.priority,
      timeout: scheduleForm.timeout,
      maxConcurrency: scheduleForm.maxConcurrency,
      retryTimes: scheduleForm.retryTimes,
      remarks: scheduleForm.remarks
    }
    
    console.log('创建任务请求数据:', taskRequest)
    
    // 调用创建任务API
    const result = await createTask(taskRequest)
    
    if (result.code === 200) {
      ElMessage.success('任务创建成功')
      localStorage.removeItem('taskCreateData')
      router.push('/task')
    } else {
      ElMessage.error(result.message || '任务创建失败')
    }
  } catch (error) {
    console.error('创建任务失败', error)
    ElMessage.error('创建任务失败')
  }
}

// 构建调度配置
const buildScheduleConfig = () => {
  const config = {}
  
  if (scheduleForm.scheduleType === 'once') {
    config.executionTime = scheduleForm.executionTime
  } else if (scheduleForm.scheduleType === 'interval') {
    config.startTime = scheduleForm.startTime
    config.intervalValue = scheduleForm.intervalValue
    config.intervalUnit = scheduleForm.intervalUnit
    if (scheduleForm.hasEndTime && scheduleForm.endTime) {
      config.endTime = scheduleForm.endTime
    }
  } else if (scheduleForm.scheduleType === 'cron') {
    config.cronExpression = scheduleForm.cronExpression
    if (scheduleForm.startTime) {
      config.startTime = scheduleForm.startTime
    }
  }
  
  return config
}
</script>

<style scoped>
.task-create-schedule-container {
  padding: 20px;
}

.step-indicator {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.action-footer {
  border-top: 1px solid #eee;
  padding-top: 20px;
}

.form-help {
  font-size: 12px;
  line-height: 1.4;
}

.text-muted {
  color: #909399;
}

.cron-examples {
  background: #f8f9fa;
  padding: 12px;
  border-radius: 4px;
  border-left: 3px solid #409eff;
}

.cron-examples code {
  background: #e1f5fe;
  padding: 2px 6px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 12px;
  color: #1976d2;
}

.mb-1 {
  margin-bottom: 4px;
}

.mb-2 {
  margin-bottom: 8px;
}

.mt-1 {
  margin-top: 4px;
}

.mt-2 {
  margin-top: 8px;
}

:deep(.el-radio) {
  margin-right: 20px;
  margin-bottom: 10px;
}

:deep(.el-radio__label) {
  display: flex;
  align-items: center;
  padding-left: 8px;
}

:deep(.el-collapse-item__header) {
  font-weight: 500;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}
</style>
