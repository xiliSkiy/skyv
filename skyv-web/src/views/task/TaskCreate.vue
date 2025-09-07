<template>
  <div class="task-create-container">
    <!-- 标题区域 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4>{{ isEdit ? '编辑采集任务' : '创建采集任务' }}</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/task' }">采集任务调度</el-breadcrumb-item>
          <el-breadcrumb-item>{{ isEdit ? '编辑任务' : '创建任务' }}</el-breadcrumb-item>
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
        <el-step title="基本信息与调度配置" />
        <el-step title="设备选择" />
      </el-steps>
    </div>

    <!-- 表单内容 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon> 基本信息
        </div>
      </template>

      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="rules"
        label-width="120px"
        label-position="left"
        @submit.prevent
      >
        <el-row :gutter="20">
          <el-col :span="16">
            <el-form-item label="任务名称" prop="taskName">
              <el-input 
                v-model="taskForm.taskName" 
                placeholder="例如：服务器机房摄像头监控采集"
                maxlength="100"
                show-word-limit
                @blur="checkTaskName"
                :suffix-icon="taskNameCheckStatus.icon"
                :class="taskNameCheckStatus.class"
              />
              <div class="form-text">
                <span v-if="!taskNameCheckStatus.message">建议格式：[区域/设备类型]+[监控目的]+采集</span>
                <span v-else :class="taskNameCheckStatus.messageClass">{{ taskNameCheckStatus.message }}</span>
              </div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="任务编号" prop="taskCode">
              <el-input 
                v-model="taskForm.taskCode" 
                placeholder="自动生成"
                :disabled="!taskForm.enableCustomCode"
              />
              <div class="form-text">
                <el-checkbox v-model="taskForm.enableCustomCode" size="small">
                  自定义编号
                </el-checkbox>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="采集类型" prop="taskType">
              <el-select v-model="taskForm.taskType" placeholder="选择采集类型" style="width: 100%" @change="onTaskTypeChange">
                <el-option-group label="基础采集">
                  <el-option label="设备状态监控" value="device_status">
                    <div>
                      <div>设备状态监控</div>
                      <div style="font-size: 12px; color: #8492a6;">监控设备在线状态、健康状态</div>
                    </div>
                  </el-option>
                  <el-option label="性能指标采集" value="performance">
                    <div>
                      <div>性能指标采集</div>
                      <div style="font-size: 12px; color: #8492a6;">采集CPU、内存、网络等性能数据</div>
                    </div>
                  </el-option>
                  <el-option label="视频流监控" value="video_stream">
                    <div>
                      <div>视频流监控</div>
                      <div style="font-size: 12px; color: #8492a6;">监控摄像头视频流状态</div>
                    </div>
                  </el-option>
                </el-option-group>
                <el-option-group label="高级采集">
                  <el-option label="事件触发采集" value="event_triggered">
                    <div>
                      <div>事件触发采集</div>
                      <div style="font-size: 12px; color: #8492a6;">基于特定事件触发的数据采集</div>
                    </div>
                  </el-option>
                  <el-option label="智能分析采集" value="ai_analysis">
                    <div>
                      <div>智能分析采集</div>
                      <div style="font-size: 12px; color: #8492a6;">结合AI分析的高级数据采集</div>
                    </div>
                  </el-option>
                  <el-option label="自定义采集" value="custom">
                    <div>
                      <div>自定义采集</div>
                      <div style="font-size: 12px; color: #8492a6;">用户自定义的采集规则</div>
                    </div>
                  </el-option>
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="数据保存" prop="dataRetention">
              <el-row :gutter="10">
                <el-col :span="12">
                  <el-input
                    v-model.number="taskForm.dataRetentionDays"
                    type="number"
                    :min="1"
                    :max="365"
                    placeholder="输入保存天数"
                    style="width: 100%"
                    @input="handleDataRetentionInput"
                  />
                </el-col>
                <el-col :span="12">
                  <el-select v-model="taskForm.dataRetentionUnit" style="width: 100%">
                    <el-option label="天" value="days" />
                    <el-option label="周" value="weeks" />
                    <el-option label="月" value="months" />
                  </el-select>
                </el-col>
              </el-row>
              <div class="form-text">采集数据的保存期限</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="3"
            placeholder="输入任务描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="任务优先级" prop="priority">
              <el-select v-model="taskForm.priority" placeholder="选择优先级" style="width: 100%">
                <el-option label="🔴 紧急" :value="1">
                  <div>
                    <span style="color: #f56c6c;">🔴 紧急</span>
                    <div style="font-size: 12px; color: #8492a6;">立即执行，最高资源分配</div>
                  </div>
                </el-option>
                <el-option label="🟠 重要" :value="2">
                  <div>
                    <span style="color: #e6a23c;">🟠 重要</span>
                    <div style="font-size: 12px; color: #8492a6;">优先执行，高资源分配</div>
                  </div>
                </el-option>
                <el-option label="🟡 普通" :value="5">
                  <div>
                    <span style="color: #409eff;">🟡 普通</span>
                    <div style="font-size: 12px; color: #8492a6;">正常执行，标准资源分配</div>
                  </div>
                </el-option>
                <el-option label="🔵 较低" :value="8">
                  <div>
                    <span style="color: #909399;">🔵 较低</span>
                    <div style="font-size: 12px; color: #8492a6;">延后执行，低资源分配</div>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="生效时间" prop="effectiveTime">
              <el-date-picker
                v-model="taskForm.effectiveTime"
                type="datetime"
                placeholder="选择生效时间"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
              <div class="form-text">任务开始生效的时间</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="失效时间">
              <el-date-picker
                v-model="taskForm.expireTime"
                type="datetime"
                placeholder="选择失效时间(可选)"
                style="width: 100%"
                format="YYYY-MM-DD HH:mm:ss"
                value-format="YYYY-MM-DD HH:mm:ss"
              />
              <div class="form-text">任务自动停止的时间</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="owner">
              <el-select 
                v-model="taskForm.owner" 
                placeholder="选择任务负责人" 
                style="width: 100%"
                filterable
                allow-create
              >
                <el-option-group label="当前用户">
                  <el-option label="当前登录用户" value="current" />
                </el-option-group>
                <el-option-group label="系统角色">
                  <el-option label="系统管理员" value="admin" />
                  <el-option label="安全管理员" value="security_admin" />
                  <el-option label="设备管理员" value="device_admin" />
                  <el-option label="监控值班员" value="monitor_operator" />
                </el-option-group>
                <el-option-group label="部门负责人">
                  <el-option label="安防部门负责人" value="security_manager" />
                  <el-option label="IT部门负责人" value="it_manager" />
                  <el-option label="运维部门负责人" value="ops_manager" />
                </el-option-group>
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="department">
              <el-select 
                v-model="taskForm.department" 
                placeholder="选择所属部门" 
                style="width: 100%"
                filterable
                allow-create
              >
                <el-option label="安全保卫部" value="security" />
                <el-option label="信息技术部" value="it" />
                <el-option label="运维管理部" value="operations" />
                <el-option label="设施管理部" value="facilities" />
                <el-option label="综合管理部" value="administration" />
                <el-option label="其他部门" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务标签">
          <div class="tag-input-container">
            <el-tag
              v-for="tag in taskForm.tags"
              :key="tag"
              closable
              :disable-transitions="false"
              @close="handleTagClose(tag)"
              class="tag-item"
              :type="getTagType(tag)"
            >
              {{ tag }}
            </el-tag>
            <el-input
              v-if="inputTagVisible"
              ref="tagInputRef"
              v-model="inputTagValue"
              class="tag-input"
              size="small"
              @keyup.enter="handleTagConfirm"
              @blur="handleTagConfirm"
              placeholder="添加标签..."
            />
            <el-button v-else class="tag-button" plain size="small" @click="showTagInput">
              + 添加标签
            </el-button>
          </div>
          <div class="form-text">
            推荐标签：
            <el-button 
              v-for="suggestedTag in suggestedTags" 
              :key="suggestedTag"
              size="small" 
              text 
              @click="addSuggestedTag(suggestedTag)"
              style="margin-right: 5px; padding: 0 5px;"
            >
              {{ suggestedTag }}
            </el-button>
          </div>
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="通知设置">
              <el-checkbox-group v-model="taskForm.notificationSettings">
                <el-checkbox label="task_start">任务启动</el-checkbox>
                <el-checkbox label="task_complete">任务完成</el-checkbox>
                <el-checkbox label="task_error">任务错误</el-checkbox>
                <el-checkbox label="data_anomaly">数据异常</el-checkbox>
              </el-checkbox-group>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="其他选项">
              <div>
                <el-checkbox v-model="taskForm.enableAutoRetry">
                  启用自动重试
                </el-checkbox>
              </div>
              <div style="margin-top: 8px;">
                <el-checkbox v-model="taskForm.enableDataBackup">
                  启用数据备份
                </el-checkbox>
              </div>
              <div style="margin-top: 8px;">
                <el-checkbox v-model="taskForm.enablePerformanceMonitor">
                  启用性能监控
                </el-checkbox>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

      </el-form>
    </el-card>

    <!-- 调度配置区域 -->
    <el-card class="mt-4">
      <template #header>
        <div class="card-header">
          <el-icon><Timer /></el-icon> 调度配置
        </div>
      </template>

      <el-form
        ref="scheduleFormRef"
        :model="scheduleForm"
        :rules="scheduleRules"
        label-width="120px"
        label-position="left"
      >
        <!-- 调度类型选择 -->
        <el-form-item label="调度类型" prop="scheduleType">
          <el-radio-group v-model="scheduleForm.scheduleType" @change="onScheduleTypeChange">
            <el-radio value="once" class="schedule-radio">
              <div class="radio-content">
                <el-icon><Clock /></el-icon>
                <div class="radio-text">
                  <div class="radio-title">一次性执行</div>
                  <div class="radio-desc">在指定时间执行一次</div>
                </div>
              </div>
            </el-radio>
            <el-radio value="interval" class="schedule-radio">
              <div class="radio-content">
                <el-icon><Timer /></el-icon>
                <div class="radio-text">
                  <div class="radio-title">周期执行</div>
                  <div class="radio-desc">按设定间隔重复执行</div>
                </div>
              </div>
            </el-radio>
            <el-radio value="cron" class="schedule-radio">
              <div class="radio-content">
                <el-icon><Setting /></el-icon>
                <div class="radio-text">
                  <div class="radio-title">Cron表达式</div>
                  <div class="radio-desc">使用Cron精确控制</div>
                </div>
              </div>
            </el-radio>
          </el-radio-group>
        </el-form-item>

        <!-- 一次性执行配置 -->
        <template v-if="scheduleForm.scheduleType === 'once'">
          <el-form-item label="执行时间" prop="executionTime">
            <el-date-picker
              v-model="scheduleForm.executionTime"
              type="datetime"
              placeholder="选择执行时间"
              style="width: 100%"
              :disabled-date="disablePastDate"
              format="YYYY-MM-DD HH:mm:ss"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
            <div class="form-text">任务将在指定时间执行一次，不能早于当前时间</div>
          </el-form-item>
        </template>

        <!-- 周期执行配置 -->
        <template v-if="scheduleForm.scheduleType === 'interval'">
          <el-row :gutter="20">
            <el-col :span="12">
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
            </el-col>
            <el-col :span="12">
              <el-form-item label="执行间隔" prop="intervalValue">
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
                <div class="form-text">
                  任务将每隔 {{ scheduleForm.intervalValue }} {{ getIntervalUnitText(scheduleForm.intervalUnit) }} 执行一次
                </div>
              </el-form-item>
            </el-col>
          </el-row>
        </template>

        <!-- Cron表达式配置 -->
        <template v-if="scheduleForm.scheduleType === 'cron'">
          <el-form-item label="Cron表达式" prop="cronExpression">
            <el-input
              v-model="scheduleForm.cronExpression"
              placeholder="例如: 0 0 12 * * ? (每天中午12点执行)"
              style="width: 100%"
            />
            <div class="form-text">
              <div class="mb-2">Cron表达式格式：秒 分 时 日 月 周 [年]</div>
              <div class="cron-examples">
                <span class="example-item"><code>0 0 12 * * ?</code> 每天中午12点</span>
                <span class="example-item"><code>0 */15 * * * ?</code> 每15分钟</span>
                <span class="example-item"><code>0 0 9-18 * * MON-FRI</code> 工作日9-18点</span>
              </div>
            </div>
          </el-form-item>
        </template>
      </el-form>
    </el-card>

    <!-- 高级选项区域 -->
    <el-card class="mt-4">
      <template #header>
        <div class="card-header">
          <el-icon><Setting /></el-icon> 高级选项
        </div>
      </template>

      <el-form
        :model="scheduleForm"
        label-width="120px"
        label-position="left"
      >
        <el-row :gutter="20">
          <el-col :span="8">
            <el-form-item label="超时时间">
              <el-input-number
                v-model="scheduleForm.timeout"
                :min="30"
                :max="3600"
                style="width: 100%"
              />
              <div class="form-text">任务执行超时时间（秒）</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="并发执行数">
              <el-input-number
                v-model="scheduleForm.maxConcurrency"
                :min="1"
                :max="20"
                style="width: 100%"
              />
              <div class="form-text">同时执行的任务数量</div>
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="重试次数">
              <el-input-number
                v-model="scheduleForm.retryTimes"
                :min="0"
                :max="10"
                style="width: 100%"
              />
              <div class="form-text">任务失败后的重试次数</div>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <el-alert
      type="info"
      show-icon
      :closable="false"
      class="mt-4"
    >
      填写完成后点击"下一步"进入设备选择页面
    </el-alert>

    <div class="action-footer d-flex justify-content-end mt-4">
      <el-button type="primary" @click="nextStep" size="large">
        下一步 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Close, InfoFilled, ArrowRight, Timer, Clock, Setting } from '@element-plus/icons-vue'
import { createTask, saveTaskDraft, getTaskDraft, getTaskDetail, updateTask, validateTaskConfig, checkTaskNameExists } from '@/api/task'

const router = useRouter()
const route = useRoute()

// 判断是否为编辑模式
const isEdit = ref(route.meta.isEdit || false)
const taskId = ref(isEdit.value ? route.params.id : null)

// 表单引用
const taskFormRef = ref(null)
const scheduleFormRef = ref(null)

// 当前步骤
const currentStep = ref(0)

// 任务表单数据
const taskForm = reactive({
  taskName: '',
  taskCode: '',
  enableCustomCode: false,
  taskType: '',
  description: '',
  priority: 5,
  tags: [],
  owner: 'current',
  department: '',
  dataRetentionDays: 30,
  dataRetentionUnit: 'days',
  effectiveTime: null,
  expireTime: null,
  notificationSettings: ['task_error'],
  enableAutoRetry: true,
  enableDataBackup: false,
  enablePerformanceMonitor: true
})

// 任务名称检查状态
const taskNameCheckStatus = reactive({
  checking: false,
  exists: false,
  message: '',
  icon: '',
  class: '',
  messageClass: ''
})

// 调度表单数据
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
  timeout: 300,
  maxConcurrency: 5,
  retryTimes: 3
})

// 表单验证规则
const rules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  taskCode: [
    { pattern: /^[A-Z]{2}\d{6}\d{3}$/, message: '任务编号格式不正确', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择采集类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择任务优先级', trigger: 'change' }
  ],
  owner: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ],
  dataRetentionDays: [
    { required: true, message: '请设置数据保存期限', trigger: 'blur' },
    { type: 'number', min: 1, max: 365, message: '保存期限必须在1-365之间', trigger: 'blur' }
  ],
  effectiveTime: [
    { required: true, message: '请选择任务生效时间', trigger: 'change' }
  ]
}

// 调度验证规则
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

// 标签输入相关
const inputTagVisible = ref(false)
const inputTagValue = ref('')
const tagInputRef = ref(null)

// 推荐标签
const suggestedTags = ref(['监控', '安防', '实时', '定时', '重要', '日常', '测试', '系统'])

// 根据任务类型生成推荐标签
const getTaskTypeRecommendedTags = (taskType) => {
  const typeTagMap = {
    'device_status': ['设备监控', '状态检查', '健康监测'],
    'performance': ['性能监控', '系统指标', '资源监控'],
    'video_stream': ['视频监控', '实时流', '画面监控'],
    'event_triggered': ['事件触发', '异常检测', '报警'],
    'ai_analysis': ['AI分析', '智能识别', '自动化'],
    'custom': ['自定义', '特殊需求', '定制化']
  }
  return typeTagMap[taskType] || []
}

// 显示标签输入框
const showTagInput = () => {
  inputTagVisible.value = true
  nextTick(() => {
    tagInputRef.value.focus()
  })
}

// 处理标签确认
const handleTagConfirm = () => {
  const value = inputTagValue.value.trim()
  if (value && !taskForm.tags.includes(value)) {
    taskForm.tags.push(value)
  }
  inputTagVisible.value = false
  inputTagValue.value = ''
}

// 处理标签关闭
const handleTagClose = (tag) => {
  taskForm.tags = taskForm.tags.filter(t => t !== tag)
}

// 获取标签类型样式
const getTagType = (tag) => {
  const typeMap = {
    '监控': 'primary',
    '安防': 'success',
    '实时': 'warning',
    '定时': 'info',
    '重要': 'danger',
    '测试': '',
    '系统': 'primary'
  }
  return typeMap[tag] || ''
}

// 添加推荐标签
const addSuggestedTag = (tag) => {
  if (!taskForm.tags.includes(tag)) {
    taskForm.tags.push(tag)
  }
}

// 任务类型变更处理
const onTaskTypeChange = (taskType) => {
  // 更新推荐标签
  const recommendedTags = getTaskTypeRecommendedTags(taskType)
  suggestedTags.value = [...new Set([...suggestedTags.value, ...recommendedTags])]
  
  // 自动生成任务编号（如果启用自定义编号）
  if (!taskForm.enableCustomCode && taskType) {
    generateTaskCode(taskType)
  }
}

// 生成任务编号
const generateTaskCode = (taskType) => {
  const typeCode = {
    'device_status': 'DS',
    'performance': 'PF',
    'video_stream': 'VS',
    'event_triggered': 'ET',
    'ai_analysis': 'AI',
    'custom': 'CS'
  }
  
  const prefix = typeCode[taskType] || 'TK'
  const timestamp = new Date().toISOString().slice(2, 10).replace(/-/g, '')
  const randomNum = Math.floor(Math.random() * 1000).toString().padStart(3, '0')
  
  taskForm.taskCode = `${prefix}${timestamp}${randomNum}`
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

// 处理数据保存天数输入
const handleDataRetentionInput = (value) => {
  console.log('数据保存天数输入:', value, typeof value)
  const numValue = parseInt(value)
  if (!isNaN(numValue) && numValue >= 1 && numValue <= 365) {
    taskForm.dataRetentionDays = numValue
    console.log('设置数据保存天数:', numValue)
  } else if (isNaN(numValue) || numValue < 1) {
    taskForm.dataRetentionDays = 1
    console.log('数据保存天数太小，设置为1')
  } else if (numValue > 365) {
    taskForm.dataRetentionDays = 365
    console.log('数据保存天数太大，设置为365')
  }
}

// 处理间隔值输入
const handleIntervalValueInput = (value) => {
  console.log('执行间隔输入:', value, typeof value)
  const numValue = parseInt(value)
  if (!isNaN(numValue) && numValue >= 1 && numValue <= 9999) {
    scheduleForm.intervalValue = numValue
    console.log('设置执行间隔:', numValue)
  } else if (isNaN(numValue) || numValue < 1) {
    scheduleForm.intervalValue = 1
    console.log('执行间隔太小，设置为1')
  } else if (numValue > 9999) {
    scheduleForm.intervalValue = 9999
    console.log('执行间隔太大，设置为9999')
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

// 检查任务名称是否存在
const checkTaskName = async () => {
  if (!taskForm.taskName || taskForm.taskName.trim().length < 2) {
    resetTaskNameStatus()
    return
  }

  taskNameCheckStatus.checking = true
  taskNameCheckStatus.icon = 'Loading'
  taskNameCheckStatus.class = 'checking'
  taskNameCheckStatus.message = '检查中...'
  taskNameCheckStatus.messageClass = 'text-info'

  try {
    const result = await checkTaskNameExists(taskForm.taskName.trim(), isEdit.value ? taskId.value : null)
    
    if (result.code === 200) {
      taskNameCheckStatus.exists = result.data.exists
      
      if (result.data.exists) {
        taskNameCheckStatus.icon = 'CircleCloseFilled'
        taskNameCheckStatus.class = 'error'
        taskNameCheckStatus.message = '任务名称已存在，请使用其他名称'
        taskNameCheckStatus.messageClass = 'text-danger'
      } else {
        taskNameCheckStatus.icon = 'CircleCheckFilled'
        taskNameCheckStatus.class = 'success'
        taskNameCheckStatus.message = '任务名称可用'
        taskNameCheckStatus.messageClass = 'text-success'
      }
    } else {
      taskNameCheckStatus.icon = 'Warning'
      taskNameCheckStatus.class = 'warning'
      taskNameCheckStatus.message = '检查失败，请稍后重试'
      taskNameCheckStatus.messageClass = 'text-warning'
    }
  } catch (error) {
    console.error('检查任务名称失败:', error)
    taskNameCheckStatus.icon = 'Warning'
    taskNameCheckStatus.class = 'warning'
    taskNameCheckStatus.message = '检查失败，请稍后重试'
    taskNameCheckStatus.messageClass = 'text-warning'
  } finally {
    taskNameCheckStatus.checking = false
  }
}

// 重置任务名称状态
const resetTaskNameStatus = () => {
  taskNameCheckStatus.checking = false
  taskNameCheckStatus.exists = false
  taskNameCheckStatus.message = ''
  taskNameCheckStatus.icon = ''
  taskNameCheckStatus.class = ''
  taskNameCheckStatus.messageClass = ''
}

// 下一步
const nextStep = async () => {
  try {
    // 验证基本信息表单
    const taskValid = await taskFormRef.value.validate()
    if (!taskValid) {
      ElMessage.warning('请完善基本信息')
      return
    }

    // 检查任务名称是否重复
    if (taskNameCheckStatus.exists) {
      ElMessage.error('任务名称已存在，请使用其他名称')
      return
    }

    // 验证调度配置表单
    const scheduleValid = await scheduleFormRef.value.validate()
    if (!scheduleValid) {
      ElMessage.warning('请完善调度配置信息')
      return
    }

    // 验证任务配置
    const validationData = {
      name: taskForm.taskName,
      taskType: taskForm.taskType,
      priority: taskForm.priority,
      description: taskForm.description
    }
    
    console.log('发送的验证数据:', validationData)
    
    const validationResult = await validateTaskConfig(validationData)
    if (validationResult.data && validationResult.data.valid) {
      // 配置验证通过
      ElMessage.success('配置验证通过')
      
      // 构建调度配置
      const scheduleConfig = buildScheduleConfig()
      
      // 保存所有数据到本地存储，用于后续步骤
      localStorage.setItem('taskCreateData', JSON.stringify({
        ...taskForm,
        scheduleType: scheduleForm.scheduleType,
        scheduleConfig: scheduleConfig,
        timeout: scheduleForm.timeout,
        maxConcurrency: scheduleForm.maxConcurrency,
        retryTimes: scheduleForm.retryTimes,
        step: 1,
        isEdit: isEdit.value,
        taskId: taskId.value
      }))

      // 跳转到设备选择页面
      router.push('/task/create/device')
    } else {
      // 配置验证失败
      const errorMsg = validationResult.data?.message || '配置验证失败'
      ElMessage.error(errorMsg)
    }
  } catch (error) {
    console.error('验证失败', error)
    ElMessage.error('验证失败，请检查配置信息')
  }
}

// 保存草稿
const saveDraft = async () => {
  try {
    const draftData = {
      ...taskForm,
      step: 0,
      isEdit: isEdit.value,
      taskId: taskId.value
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
  ElMessage.info(isEdit.value ? '已取消编辑任务' : '已取消创建任务')
  router.push('/task')
}

// 获取任务详情数据（编辑模式）
const fetchTaskDetail = async () => {
  if (!isEdit.value || !taskId.value) return
  
  try {
    const res = await getTaskDetail(taskId.value)
    if (!res.data) {
      ElMessage.error('获取任务详情失败')
      return
    }
    
    const taskData = res.data
    console.log('获取到的任务详情:', taskData)
    
    // 填充基本信息
    taskForm.taskName = taskData.name || ''
    taskForm.taskCode = taskData.taskCode || ''
    taskForm.enableCustomCode = !!taskData.taskCode
    taskForm.description = taskData.description || ''
    
    // 处理优先级
    let priority = taskData.priority || 5
    if (typeof priority === 'string') {
      switch (priority) {
        case 'high': priority = 1; break
        case 'medium': priority = 5; break
        case 'low': priority = 9; break
        default: priority = 5; break
      }
    }
    taskForm.priority = priority
    
    // 处理标签
    taskForm.tags = Array.isArray(taskData.tags) ? [...taskData.tags] : []
    
    // 处理采集类型（表单中使用taskType字段）
    taskForm.taskType = taskData.taskType || taskData.collectorType || (taskData.collectorId ? 'custom' : 'device_status')
    
    // 处理采集器类型（如果需要的话）
    taskForm.collectorType = taskData.collectorType || (taskData.collectorId ? 'custom' : 'default')
    
    // 处理时间相关字段
    // 1. 创建时间（从createdAt字段获取，只读显示）
    taskForm.createTime = taskData.createTime || taskData.createdAt || ''
    
    // 2. 生效时间和失效时间（如果后端没有这些字段，使用默认值）
    taskForm.effectiveTime = taskData.effectiveTime || ''
    taskForm.expireTime = taskData.expireTime || ''
    
    // 3. 开始时间（从scheduleConfig中提取）
    if (taskData.scheduleConfig && taskData.scheduleConfig.startTime) {
      taskForm.startTime = taskData.scheduleConfig.startTime
    } else {
      taskForm.startTime = taskData.startTime || ''
    }
    
    // 4. 结束时间（如果后端没有则使用默认值）
    taskForm.endTime = taskData.endTime || taskData.expireTime || ''
    
    // 处理数据保存设置（如果后端没有这些字段，使用默认值）
    taskForm.dataRetentionDays = taskData.dataRetentionDays || 30
    taskForm.dataRetentionUnit = taskData.dataRetentionUnit || 'days'
    
    // 处理通知设置
    taskForm.enableNotification = taskData.enableNotification !== false
    taskForm.notificationSettings = taskData.notificationSettings || {
      email: false,
      sms: false,
      webhook: false
    }
    
    // 处理其他设置
    taskForm.enableAutoRetry = taskData.enableRetry !== false
    taskForm.enableDataBackup = taskData.enableDataBackup !== false
    taskForm.enablePerformanceMonitor = taskData.enablePerformanceMonitor !== false
    taskForm.owner = taskData.owner || 'current'
    taskForm.department = taskData.department || ''
    taskForm.remarks = taskData.remarks || ''
    
    // 处理调度配置
    if (taskData.scheduleConfig) {
      // 填充基本调度信息
      taskForm.scheduleType = taskData.scheduleType || 'once'
      taskForm.scheduleConfig = { ...taskData.scheduleConfig }
      
      // 同时填充scheduleForm（用于表单显示）
      scheduleForm.scheduleType = taskData.scheduleType || 'interval'
      
      // 处理开始时间
      if (taskData.scheduleConfig.startTime) {
        taskForm.startTime = taskData.scheduleConfig.startTime
        scheduleForm.startTime = taskData.scheduleConfig.startTime
      }
      
      // 处理间隔执行配置
      if (taskData.scheduleType === 'interval') {
        taskForm.intervalValue = taskData.scheduleConfig.intervalValue || 1
        taskForm.intervalUnit = taskData.scheduleConfig.intervalUnit || 'minutes'
        scheduleForm.intervalValue = taskData.scheduleConfig.intervalValue || 5
        scheduleForm.intervalUnit = taskData.scheduleConfig.intervalUnit || 'minutes'
      }
      
      // 处理Cron表达式
      if (taskData.scheduleType === 'cron') {
        taskForm.cronExpression = taskData.scheduleConfig.cronExpression || ''
        scheduleForm.cronExpression = taskData.scheduleConfig.cronExpression || ''
      }
      
      // 处理一次性执行
      if (taskData.scheduleType === 'once') {
        scheduleForm.executionTime = taskData.scheduleConfig.executionTime || taskData.scheduleConfig.startTime || null
      }
      
      // 处理高级选项
      scheduleForm.timeout = taskData.timeout || 300
      scheduleForm.maxConcurrency = taskData.maxConcurrency || 5
      scheduleForm.retryTimes = taskData.retryTimes || 3
    }
    
    // 处理重试配置
    taskForm.retryTimes = taskData.retryTimes || 3
    taskForm.retryInterval = taskData.retryInterval || 1000
    taskForm.timeout = taskData.timeout || 300
    taskForm.maxConcurrency = taskData.maxConcurrency || 5
    
    // 保存完整的任务数据到本地存储，供后续步骤使用
    localStorage.setItem('taskEditData', JSON.stringify(taskData))
    
    ElMessage.success('任务数据加载成功')
  } catch (error) {
    console.error('获取任务详情失败', error)
    ElMessage.error('获取任务详情失败，请刷新重试')
  }
}

// 恢复草稿数据
const restoreDraftData = async () => {
  // 如果是编辑模式，优先获取任务详情
  if (isEdit.value && taskId.value) {
    await fetchTaskDetail()
    return
  }
  
  // 检查本地存储中是否有数据
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      // 处理优先级转换
      if (data.priority && typeof data.priority === 'string') {
        switch (data.priority) {
          case 'high': data.priority = 1; break
          case 'medium': data.priority = 5; break
          case 'low': data.priority = 9; break
          default: data.priority = 5; break
        }
      }
      Object.assign(taskForm, data)
      return
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }

  // 检查是否有草稿ID
  const draftId = localStorage.getItem('taskDraftId')
  if (draftId) {
    try {
      const res = await getTaskDraft(draftId)
      if (res.data) {
        // 处理优先级转换
        if (res.data.priority && typeof res.data.priority === 'string') {
          switch (res.data.priority) {
            case 'high': res.data.priority = 1; break
            case 'medium': res.data.priority = 5; break
            case 'low': res.data.priority = 9; break
            default: res.data.priority = 5; break
          }
        }
        Object.assign(taskForm, res.data)
      }
    } catch (error) {
      console.error('获取草稿数据失败', error)
    }
  }
}

// 页面初始化
onMounted(() => {
  console.log('TaskCreate组件挂载，编辑模式:', isEdit.value, '任务ID:', taskId.value)
  
  // 如果不是编辑模式，则清理本地存储中的任务数据
  if (!isEdit.value) {
    // 清除可能存在的历史数据
    localStorage.removeItem('taskCreateData')
    localStorage.removeItem('taskEditData')
    localStorage.removeItem('taskDraftId')
    console.log('已清理本地存储中的历史任务数据')
  } else {
    // 编辑模式：检查本地存储中是否已有任务数据
    const editData = localStorage.getItem('taskEditData')
    if (!editData) {
      // 如果没有，则从服务器获取任务详情
      console.log('本地存储中无任务数据，从服务器获取任务详情')
      fetchTaskDetail()
    } else {
      console.log('从本地存储中恢复任务编辑数据')
      // 确保taskCreateData中包含isEdit和taskId信息
      const taskCreateData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
      localStorage.setItem('taskCreateData', JSON.stringify({
        ...taskCreateData,
        isEdit: true,
        taskId: taskId.value
      }))
    }
  }
  
  // 然后再尝试恢复草稿数据
  restoreDraftData()
})
</script>

<style scoped>
.step-indicator {
  margin-bottom: 30px;
}

.form-text {
  font-size: 12px;
  color: #6c757d;
  margin-top: 5px;
}

/* 任务名称检查状态样式 */
.el-input.checking {
  border-color: #409eff;
}

.el-input.success {
  border-color: #67c23a;
}

.el-input.error {
  border-color: #f56c6c;
}

.el-input.warning {
  border-color: #e6a23c;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
}

.text-warning {
  color: #e6a23c;
}

.text-info {
  color: #409eff;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header .el-icon {
  margin-right: 8px;
}

.tag-input-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 5px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 38px;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.tag-input {
  width: 100px;
  margin-right: 8px;
  margin-bottom: 8px;
  vertical-align: bottom;
}

.tag-button {
  margin-bottom: 8px;
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

/* 调度相关样式 */
.schedule-radio {
  display: flex;
  align-items: center;
  margin-right: 20px;
  margin-bottom: 15px;
  padding: 12px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  transition: all 0.3s;
}

.schedule-radio:hover {
  border-color: #409eff;
  background-color: #f0f9ff;
}

.schedule-radio.is-checked {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.radio-content {
  display: flex;
  align-items: center;
  width: 100%;
}

.radio-text {
  margin-left: 8px;
}

.radio-title {
  font-weight: 500;
  font-size: 14px;
  color: #303133;
}

.radio-desc {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.cron-examples {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
  margin-top: 8px;
}

.example-item {
  background: #f8f9fa;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  border-left: 3px solid #409eff;
}

.example-item code {
  background: #e1f5fe;
  padding: 2px 4px;
  border-radius: 3px;
  font-family: 'Courier New', monospace;
  font-size: 11px;
  color: #1976d2;
  margin-right: 5px;
}

.mb-2 {
  margin-bottom: 8px;
}

.mt-4 {
  margin-top: 20px;
}
</style> 