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
              <el-icon><Calendar /></el-icon> 任务调度设置
            </div>
          </template>
          
          <!-- 配置方式导航标签 -->
          <el-tabs v-model="activeTab" class="demo-tabs">
            <el-tab-pane label="时间调度" name="time">
              <el-icon><Clock /></el-icon> 时间调度
            </el-tab-pane>
            <el-tab-pane label="事件触发" name="event">
              <el-icon><Bolt /></el-icon> 事件触发
            </el-tab-pane>
            <el-tab-pane label="手动执行" name="manual">
              <el-icon><HandPointer /></el-icon> 手动执行
            </el-tab-pane>
          </el-tabs>
          
          <!-- 时间调度内容 -->
          <div v-if="activeTab === 'time'">
            <!-- 简单调度 -->
            <div 
              class="schedule-option" 
              :class="{ active: scheduleForm.scheduleType === 'simple' }"
              @click="selectScheduleType('simple')"
            >
              <el-radio v-model="scheduleForm.scheduleType" label="simple">
                <span class="fw-bold">简单调度</span>
              </el-radio>
                <div class="ms-4 mt-3">
                  <el-row :gutter="20" class="mb-3">
                    <el-col :span="8">
                      <el-form-item label="执行频率">
                        <el-select v-model="scheduleForm.simpleFrequency" @change="updateIntervalUnit">
                          <el-option label="分钟" value="minute" />
                          <el-option label="小时" value="hour" />
                          <el-option label="天" value="day" />
                          <el-option label="周" value="week" />
                          <el-option label="月" value="month" />
                        </el-select>
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="间隔">
                        <el-input-number v-model="scheduleForm.simpleInterval" :min="1" :max="100" />
                        <span class="ms-2">{{getIntervalUnitText()}}</span>
                      </el-form-item>
                    </el-col>
                    <el-col :span="8">
                      <el-form-item label="开始时间">
                        <el-date-picker
                          v-model="scheduleForm.startDateTime"
                          type="datetime"
                          placeholder="选择日期时间"
                        />
                      </el-form-item>
                    </el-col>
                  </el-row>
                  <el-form-item>
                    <el-switch
                      v-model="scheduleForm.noEndTime"
                      active-text="无结束时间"
                    />
                  </el-form-item>
                </div>
              </div>
              
              <!-- Cron表达式 -->
              <div 
                class="schedule-option" 
                :class="{ active: scheduleForm.scheduleType === 'cron' }"
                @click="selectScheduleType('cron')"
              >
                <el-radio v-model="scheduleForm.scheduleType" label="cron">
                  <span class="fw-bold">Cron 表达式</span>
                </el-radio>
                <div class="ms-4 mt-3">
                  <el-alert
                    type="info"
                    :closable="false"
                    show-icon
                  >
                    <template #default>
                      Cron表达式提供更灵活的调度设置，适用于复杂的调度需求。
                    </template>
                  </el-alert>
                  
                  <el-row :gutter="20" class="mt-3">
                    <el-col :span="12">
                      <el-form-item label="Cron 表达式">
                        <div class="d-flex">
                          <el-input v-model="scheduleForm.cronExpression" placeholder="0 0 * * * ?" />
                          <el-button class="ms-2">验证</el-button>
                        </div>
                        <div class="form-text">示例: 0 0 */1 * * ? (每小时执行一次)</div>
                      </el-form-item>
                    </el-col>
                    <el-col :span="12">
                      <el-form-item label="执行说明">
                        <div class="border rounded p-2 bg-light">
                          <p class="mb-0 small">当前表达式将在每小时整点执行，例如：08:00, 09:00, 10:00...</p>
                        </div>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>
              </div>
              
              <!-- 日历调度 -->
              <div 
                class="schedule-option" 
                :class="{ active: scheduleForm.scheduleType === 'calendar' }"
                @click="selectScheduleType('calendar')"
              >
                <el-radio v-model="scheduleForm.scheduleType" label="calendar">
                  <span class="fw-bold">日历调度</span>
                </el-radio>
                <div class="ms-4 mt-3">
                  <el-row :gutter="20" class="mb-3">
                    <el-col :span="12">
                      <el-form-item label="选择日期和时间">
                        <div class="d-flex flex-wrap gap-2 border rounded p-3">
                          <el-checkbox v-model="scheduleForm.weekdays" label="1">星期一</el-checkbox>
                          <el-checkbox v-model="scheduleForm.weekdays" label="2">星期二</el-checkbox>
                          <el-checkbox v-model="scheduleForm.weekdays" label="3">星期三</el-checkbox>
                          <el-checkbox v-model="scheduleForm.weekdays" label="4">星期四</el-checkbox>
                          <el-checkbox v-model="scheduleForm.weekdays" label="5">星期五</el-checkbox>
                          <el-checkbox v-model="scheduleForm.weekdays" label="6">星期六</el-checkbox>
                          <el-checkbox v-model="scheduleForm.weekdays" label="0">星期日</el-checkbox>
                        </div>
                      </el-form-item>
                    </el-col>
                  </el-row>
                  
                  <el-row :gutter="20">
                    <el-col :span="6">
                      <el-form-item label="执行时间">
                        <div class="d-flex align-items-center">
                          <el-time-picker
                            v-model="scheduleForm.execTime"
                            format="HH:mm"
                            placeholder="选择时间"
                          />
                          <el-button class="ms-2" size="small">
                            <el-icon><Plus /></el-icon>
                          </el-button>
                        </div>
                        
                        <div class="mt-2">
                          <el-tag 
                            v-for="(time, index) in scheduleForm.execTimes" 
                            :key="index"
                            closable
                            @close="removeExecTime(index)"
                            class="me-1 mb-1"
                          >
                            {{ formatTime(time) }}
                          </el-tag>
                        </div>
                      </el-form-item>
                    </el-col>
                    
                    <el-col :span="18">
                      <el-form-item label="预览下次执行时间">
                        <div class="border rounded p-2 bg-light" style="height: 120px; overflow-y: auto;">
                          <ul class="mb-0 small ps-3">
                            <li v-for="(time, index) in previewTimes" :key="index">
                              {{ time }}
                            </li>
                          </ul>
                        </div>
                      </el-form-item>
                    </el-col>
                  </el-row>
                </div>
              </div>

            </div>
          </div>
          
          <!-- 事件触发内容 -->
          <div v-if="activeTab === 'event'">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              class="mb-4"
            >
              <template #default>
                事件触发允许您根据系统事件自动执行任务，例如设备上线、报警触发等条件。
              </template>
            </el-alert>
            
            <!-- 设备状态事件 -->
            <div 
              class="schedule-option" 
              :class="{ active: scheduleForm.eventType === 'device' }"
              @click="selectEventType('device')"
            >
              <el-radio v-model="scheduleForm.eventType" label="device">
                <span class="fw-bold">设备状态事件</span>
              </el-radio>
              <div class="ms-4 mt-3">
                <el-row :gutter="20" class="mb-3">
                  <el-col :span="8">
                    <el-form-item label="触发条件">
                      <el-select v-model="scheduleForm.deviceTrigger" style="width: 100%">
                        <el-option label="设备上线时" value="online" />
                        <el-option label="设备离线时" value="offline" />
                        <el-option label="设备重启后" value="restart" />
                        <el-option label="设备固件更新后" value="update" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="应用于">
                      <el-select v-model="scheduleForm.deviceScope" style="width: 100%">
                        <el-option label="所有已选设备" value="all" />
                        <el-option label="部分设备（自定义选择）" value="custom" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
                
                <el-form-item>
                  <el-switch
                    v-model="scheduleForm.enableCoolingPeriod"
                    active-text="设置冷却期"
                  />
                </el-form-item>
                
                <div v-if="scheduleForm.enableCoolingPeriod">
                  <div class="d-flex align-items-center" style="width: 200px;">
                    <el-input-number
                      v-model="scheduleForm.coolingPeriod"
                      :min="1"
                      controls-position="right"
                      style="width: 150px"
                    />
                    <span class="ms-2">分钟</span>
                  </div>
                  <div class="form-text">同一设备触发后的冷却期内不会重复执行任务</div>
                </div>
              </div>
            </div>
            
            <!-- 报警事件 -->
            <div 
              class="schedule-option" 
              :class="{ active: scheduleForm.eventType === 'alarm' }"
              @click="selectEventType('alarm')"
            >
              <el-radio v-model="scheduleForm.eventType" label="alarm">
                <span class="fw-bold">报警事件</span>
              </el-radio>
              <div class="ms-4 mt-3">
                <el-row :gutter="20" class="mb-3">
                  <el-col :span="8">
                    <el-form-item label="报警类型">
                      <el-select v-model="scheduleForm.alarmType" style="width: 100%">
                        <el-option label="任何报警" value="any" />
                        <el-option label="温度异常报警" value="temperature" />
                        <el-option label="湿度异常报警" value="humidity" />
                        <el-option label="设备故障报警" value="fault" />
                        <el-option label="自定义报警..." value="custom" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="报警级别">
                      <el-select v-model="scheduleForm.alarmLevel" style="width: 100%">
                        <el-option label="所有级别" value="all" />
                        <el-option label="低级别" value="low" />
                        <el-option label="中级别" value="medium" />
                        <el-option label="高级别" value="high" />
                        <el-option label="紧急级别" value="urgent" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                  <el-col :span="8">
                    <el-form-item label="来源设备">
                      <el-select v-model="scheduleForm.alarmSource" style="width: 100%">
                        <el-option label="所有已选设备" value="all" />
                        <el-option label="部分设备（自定义选择）" value="custom" />
                      </el-select>
                    </el-form-item>
                  </el-col>
                </el-row>
              </div>
            </div>
            
            <!-- API触发 -->
            <div 
              class="schedule-option" 
              :class="{ active: scheduleForm.eventType === 'api' }"
              @click="selectEventType('api')"
            >
              <el-radio v-model="scheduleForm.eventType" label="api">
                <span class="fw-bold">API触发</span>
              </el-radio>
              <div class="ms-4 mt-3">
                <el-alert
                  type="secondary"
                  :closable="false"
                  class="d-flex"
                >
                  <template #icon>
                    <el-icon class="me-3 fs-4"><Code /></el-icon>
                  </template>
                  <template #default>
                    <div>
                      <p class="mb-2">通过API触发此任务，适用于与第三方系统集成或自定义脚本调用。</p>
                      <div class="input-group">
                        <el-input
                          readonly
                          size="small"
                          value="https://api.skyeye.com/v1/tasks/trigger/T2023051001"
                        />
                        <el-button size="small">
                          <el-icon><CopyDocument /></el-icon>
                        </el-button>
                      </div>
                      <div class="form-text">使用API密钥进行身份验证，详见API文档</div>
                    </div>
                  </template>
                </el-alert>
              </div>
            </div>
          </div>
          
          <!-- 手动执行内容 -->
          <div v-if="activeTab === 'manual'">
            <el-alert
              type="info"
              :closable="false"
              show-icon
              class="mb-4"
            >
              <template #default>
                手动执行模式下，任务不会自动执行，需要用户在任务列表中手动触发执行。
              </template>
            </el-alert>
            
            <div class="text-center py-4">
              <div class="mb-4">
                <el-icon class="text-muted" style="font-size: 4rem;"><HandPointer /></el-icon>
              </div>
              <h5>任务将保持就绪状态</h5>
              <p class="text-muted">您可以随时在任务列表中手动启动此任务，或通过API触发执行。</p>
              <div class="mt-3">
                <el-switch
                  v-model="scheduleForm.addToQuickAccess"
                  active-text="添加到快速访问"
                />
              </div>
            </div>
          </div>
        </el-card>

        <!-- 执行限制 -->
        <el-card class="mb-4">
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
import { Document, Close, Calendar, Stopwatch, Setting, InfoFilled, ArrowLeft, Check, Clock, Bolt, HandPointer, ChevronDown, Cog, InfoCircle } from '@element-plus/icons-vue'
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

// 当前活动的标签页
const activeTab = ref('time')

// 高级选项是否可见
const advancedOptionsVisible = ref(false)

// 预览执行时间列表
const previewTimes = ref([
  '2023-05-10 08:00:00 (星期一)',
  '2023-05-10 12:00:00 (星期一)',
  '2023-05-10 18:00:00 (星期一)',
  '2023-05-11 08:00:00 (星期二)',
  '...'
])

// 任务概要信息
const taskSummary = reactive({
  name: '服务器性能监控',
  description: '定期采集服务器性能数据',
  creator: '管理员',
  id: 'T2023051002',
  deviceCount: 5,
  deviceTypes: '服务器',
  deviceAreas: '数据中心、办公室',
  deviceTags: '生产环境、核心业务',
  metricCount: 8,
  metricTypes: 'CPU、内存、磁盘、网络',
  dataSources: 'SNMP、Agent',
  hasAlertRules: true,
  startTime: '2023-05-10 08:00',
  endTime: '无结束时间'
})

// 调度表单
const scheduleForm = reactive({
  // 调度类型
  scheduleType: 'simple', // simple, cron, calendar
  
  // 简单调度
  simpleFrequency: 'hour',
  simpleInterval: 1,
  startDateTime: new Date(),
  noEndTime: true,
  
  // Cron表达式
  cronExpression: '0 0 */1 * * ?',
  
  // 日历调度
  weekdays: [1, 2, 3, 4, 5], // 默认周一到周五
  execTime: new Date(2023, 0, 1, 8, 0),
  execTimes: [
    new Date(2023, 0, 1, 8, 0),
    new Date(2023, 0, 1, 12, 0),
    new Date(2023, 0, 1, 18, 0)
  ],
  
  // 事件触发
  eventType: 'device', // device, alarm, api
  deviceTrigger: 'online',
  deviceScope: 'all',
  enableCoolingPeriod: true,
  coolingPeriod: 30,
  
  // 报警事件
  alarmType: 'any',
  alarmLevel: 'all',
  alarmSource: 'all',
  
  // 手动执行
  addToQuickAccess: true,
  
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

// 切换高级选项显示
const toggleAdvancedOptions = () => {
  advancedOptionsVisible.value = !advancedOptionsVisible.value
}

// 选择事件类型
const selectEventType = (type) => {
  scheduleForm.eventType = type
}

// 格式化时间
const formatTime = (date) => {
  if (!date) return ''
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  return `${hours}:${minutes}`
}

// 移除执行时间
const removeExecTime = (index) => {
  scheduleForm.execTimes.splice(index, 1)
}

// 获取调度类型文本
const getScheduleTypeText = () => {
  if (activeTab.value === 'time') {
    if (scheduleForm.scheduleType === 'simple') return '时间调度 - 简单调度'
    if (scheduleForm.scheduleType === 'cron') return '时间调度 - Cron表达式'
    if (scheduleForm.scheduleType === 'calendar') return '时间调度 - 日历调度'
  } else if (activeTab.value === 'event') {
    if (scheduleForm.eventType === 'device') return '事件触发 - 设备状态'
    if (scheduleForm.eventType === 'alarm') return '事件触发 - 报警事件'
    if (scheduleForm.eventType === 'api') return '事件触发 - API触发'
  } else if (activeTab.value === 'manual') {
    return '手动执行'
  }
  return '未设置'
}

// 获取执行频率文本
const getExecutionFrequencyText = () => {
  if (activeTab.value === 'time') {
    if (scheduleForm.scheduleType === 'simple') {
      return `每${scheduleForm.simpleInterval}${getIntervalUnitText()}`
    }
    if (scheduleForm.scheduleType === 'cron') {
      return 'Cron表达式定义'
    }
    if (scheduleForm.scheduleType === 'calendar') {
      return '按日历计划执行'
    }
  } else if (activeTab.value === 'event') {
    return '由事件触发'
  } else if (activeTab.value === 'manual') {
    return '手动触发'
  }
  return '未设置'
}

// 获取间隔单位文本
const getIntervalUnitText = () => {
  const unitMap = {
    'minute': '分钟',
    'hour': '小时',
    'day': '天',
    'week': '周',
    'month': '月'
  }
  return unitMap[scheduleForm.simpleFrequency] || '小时'
}

// 更新间隔单位
const updateIntervalUnit = () => {
  // 根据频率自动调整间隔默认值
  if (scheduleForm.simpleFrequency === 'minute') {
    scheduleForm.simpleInterval = 5
  } else if (scheduleForm.simpleFrequency === 'hour') {
    scheduleForm.simpleInterval = 1
  } else if (scheduleForm.simpleFrequency === 'day') {
    scheduleForm.simpleInterval = 1
  } else if (scheduleForm.simpleFrequency === 'week') {
    scheduleForm.simpleInterval = 1
  } else if (scheduleForm.simpleFrequency === 'month') {
    scheduleForm.simpleInterval = 1
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