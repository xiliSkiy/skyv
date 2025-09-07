<template>
  <div class="task-detail-container">
    <!-- 标题区域 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4>任务详情</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/task' }">采集任务调度</el-breadcrumb-item>
          <el-breadcrumb-item>任务详情</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div>
        <el-button type="primary" plain @click="handleEdit">
          <el-icon><Edit /></el-icon> 编辑任务
        </el-button>
      </div>
    </div>

    <div v-loading="loading">
      <!-- 任务基本信息 -->
      <el-card class="mb-4">
        <template #header>
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <el-icon><InfoFilled /></el-icon> 基本信息
            </div>
            <div>
              <el-tag :type="getStatusType(taskInfo.status)" size="medium">
                {{ getStatusLabel(taskInfo.status) }}
              </el-tag>
            </div>
          </div>
        </template>

        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务名称">{{ taskInfo.taskName }}</el-descriptions-item>
          <el-descriptions-item label="任务ID">{{ taskInfo.id }}</el-descriptions-item>
          <el-descriptions-item label="任务类型">
            <el-tag size="small" :type="getTaskTypeTag(taskInfo.taskType)">
              {{ getTaskTypeLabel(taskInfo.taskType) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="优先级">
            <div class="d-flex align-items-center">
              <span class="task-priority" :class="'priority-' + taskInfo.priority"></span>
              <span>{{ getPriorityLabel(taskInfo.priority) }}</span>
            </div>
          </el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ formatDate(taskInfo.createTime) }}</el-descriptions-item>
          <el-descriptions-item label="负责人">{{ taskInfo.owner }}</el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="2">{{ taskInfo.description }}</el-descriptions-item>
          <el-descriptions-item label="任务标签" :span="2">
            <el-tag 
              v-for="tag in taskInfo.tags" 
              :key="tag" 
              class="me-1"
              size="small"
            >
              {{ tag }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <!-- 任务执行状态 -->
      <el-card class="mb-4">
        <template #header>
          <div>
            <el-icon><Stopwatch /></el-icon> 执行状态
          </div>
        </template>

        <el-row :gutter="20">
          <el-col :span="12">
            <div class="status-item">
              <div class="label">开始时间</div>
              <div class="value">{{ taskInfo.startTime ? formatDate(taskInfo.startTime) : '未开始' }}</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="status-item">
              <div class="label">结束时间</div>
              <div class="value">{{ taskInfo.endTime ? formatDate(taskInfo.endTime) : '未结束' }}</div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="mt-3">
          <el-col :span="12">
            <div class="status-item">
              <div class="label">执行次数</div>
              <div class="value">{{ taskInfo.executionCount || 0 }} 次</div>
            </div>
          </el-col>
          <el-col :span="12">
            <div class="status-item">
              <div class="label">下次执行时间</div>
              <div class="value">{{ taskInfo.nextExecutionTime ? formatDate(taskInfo.nextExecutionTime) : '无' }}</div>
            </div>
          </el-col>
        </el-row>

        <el-row :gutter="20" class="mt-3">
          <el-col :span="24">
            <div class="status-item">
              <div class="label">调度设置</div>
              <div class="value">{{ getScheduleDescription() }}</div>
            </div>
          </el-col>
        </el-row>
      </el-card>

      <!-- 设备列表 -->
      <el-card class="mb-4">
        <template #header>
          <div class="card-header-title">
            <el-icon><VideoCamera /></el-icon> 设备列表
          </div>
        </template>

        <div v-if="(taskInfo.devices && taskInfo.devices.length > 0)">
          <el-table :data="taskInfo.devices" style="width: 100%">
            <el-table-column label="设备名称" min-width="180">
              <template #default="{ row }">
                <div class="device-name-cell">
                  <h6 class="device-name">{{ row.deviceName || '未命名设备' }}</h6>
                  <div class="device-id">ID: {{ row.deviceCode || row.id || '无ID' }}</div>
                </div>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="getDeviceTypeTagType(row.deviceType)">
                  {{ getDeviceTypeLabel(row.deviceType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="getDeviceStatusType(row.status)" effect="light">
                  {{ getDeviceStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="位置" min-width="120">
              <template #default="{ row }">
                {{ row.location || '未设置位置' }}
              </template>
            </el-table-column>
            <el-table-column label="IP地址" min-width="120">
              <template #default="{ row }">
                {{ row.ipAddress || '未设置IP' }}
              </template>
            </el-table-column>
          </el-table>
        </div>
        <el-empty v-else description="暂无设备数据" />
      </el-card>

      <!-- 指标配置 -->
      <el-card class="mb-4">
        <template #header>
          <div class="card-header-title">
            <el-icon><DataLine /></el-icon> 指标配置
          </div>
        </template>

        <div v-if="(taskInfo.metrics && taskInfo.metrics.length > 0)">
          <div v-for="metric in taskInfo.metrics" :key="metric.id || Math.random()" class="metric-card">
            <div class="metric-header">
              <div>
                <h5 class="mb-0">{{ metric.name || '未命名指标' }}</h5>
                <el-tag size="small" :type="getMetricTypeTag(metric.type)" class="me-1">
                  {{ getMetricTypeLabel(metric.type) }}
                </el-tag>
                <small class="text-muted">适用于: {{ metric.deviceCount || 0 }}台{{ getDeviceTypeLabel(metric.deviceType) }}</small>
              </div>
            </div>
            
            <div class="metric-body mt-3">
              <div class="metric-config">
                <!-- 人脸识别指标 -->
                <template v-if="metric.type === 'face_recognition' && metric.config">
                  <div class="config-item">
                    <span class="config-label">识别置信度阈值:</span>
                    <span class="config-value">{{ metric.config.confidence || 0 }}%</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">人脸库选择:</span>
                    <span class="config-value">{{ getFaceDatabaseLabel(metric.config.faceDatabase) }}</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">保存未识别人脸:</span>
                    <span class="config-value">{{ metric.config.storeUnknownFaces ? '是' : '否' }}</span>
                  </div>
                </template>
                
                <!-- 物体检测指标 -->
                <template v-else-if="metric.type === 'object_detection' && metric.config">
                  <div class="config-item">
                    <span class="config-label">检测对象:</span>
                    <span class="config-value">
                      {{ getDetectionObjectsLabel(metric.config) }}
                    </span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">检测置信度阈值:</span>
                    <span class="config-value">{{ metric.config.confidence || 0 }}%</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">检测频率:</span>
                    <span class="config-value">{{ getDetectionFrequencyLabel(metric.config.frequency) }}</span>
                  </div>
                </template>
                
                <!-- 温湿度数据指标 -->
                <template v-else-if="metric.type === 'temperature_humidity' && metric.config">
                  <div class="config-item">
                    <span class="config-label">温度正常范围:</span>
                    <span class="config-value">{{ metric.config.tempMin || 0 }}°C - {{ metric.config.tempMax || 0 }}°C</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">湿度正常范围:</span>
                    <span class="config-value">{{ metric.config.humidityMin || 0 }}% - {{ metric.config.humidityMax || 0 }}%</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">采样间隔:</span>
                    <span class="config-value">{{ getSamplingIntervalLabel(metric.config.samplingInterval) }}</span>
                  </div>
                </template>
                
                <!-- 行为分析指标 -->
                <template v-else-if="metric.type === 'behavior_analysis' && metric.config">
                  <div class="config-item">
                    <span class="config-label">检测徘徊行为:</span>
                    <span class="config-value">{{ metric.config.detectLoitering ? '是' : '否' }}</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">检测人群聚集:</span>
                    <span class="config-value">{{ metric.config.detectCrowding ? '是' : '否' }}</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">检测跌倒行为:</span>
                    <span class="config-value">{{ metric.config.detectFalling ? '是' : '否' }}</span>
                  </div>
                  <div class="config-item">
                    <span class="config-label">灵敏度:</span>
                    <span class="config-value">{{ metric.config.sensitivity || 0 }}%</span>
                  </div>
                </template>
                
                <!-- 默认显示JSON -->
                <template v-else>
                  <div v-if="metric.config && Object.keys(metric.config).length > 0">
                    <pre>{{ typeof metric.config === 'string' ? metric.config : JSON.stringify(metric.config, null, 2) }}</pre>
                  </div>
                  <div v-else class="text-muted">无配置数据</div>
                </template>
              </div>
            </div>
          </div>
        </div>
        <el-empty v-else description="暂无指标配置" />
      </el-card>

      <!-- 操作按钮 -->
      <div class="d-flex justify-content-center mt-4">
        <el-button-group>
          <el-button type="primary" v-if="taskInfo.status === 'paused'" @click="handleStart">
            <el-icon><VideoPlay /></el-icon> 继续
          </el-button>
          <el-button type="warning" v-if="taskInfo.status === 'running'" @click="handlePause">
            <el-icon><VideoPause /></el-icon> 暂停
          </el-button>
          <el-button type="danger" v-if="['running', 'paused'].includes(taskInfo.status)" @click="handleStop">
            <el-icon><CircleClose /></el-icon> 停止
          </el-button>
          <el-button type="success" v-if="taskInfo.status === 'scheduled' || taskInfo.status === 'completed'" @click="handleStart">
            <el-icon><VideoPlay /></el-icon> 启动
          </el-button>
        </el-button-group>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, InfoFilled, Stopwatch, VideoCamera, DataLine, VideoPlay, VideoPause, CircleClose } from '@element-plus/icons-vue'
import { getTaskDetail, startTask, pauseTask, stopTask } from '@/api/task'

const router = useRouter()
const route = useRoute()

// 任务ID
const taskId = route.params.id

// 加载状态
const loading = ref(false)

// 任务信息
const taskInfo = ref({
  id: '',
  taskName: '',
  taskType: '',
  description: '',
  priority: 5,
  tags: [],
  owner: '',
  department: '',
  status: '',
  createTime: '',
  startTime: '',
  endTime: '',
  executionCount: 0,
  nextExecutionTime: '',
  schedule: {},
  devices: [],
  metrics: []
})

// 获取任务详情
const fetchTaskDetail = async () => {
  loading.value = true
  try {
    const res = await getTaskDetail(taskId)
    // 确保数据完整性
    const data = res.data || {}
    
    // 处理字段映射和缺失字段
    // 1. 任务名称（后端返回name，前端期望taskName）
    data.taskName = data.taskName || data.name
    
    // 2. 任务类型（从scheduleType推断或使用默认值）
    data.taskType = data.taskType || data.scheduleType || 'interval'
    
    // 3. 创建时间（使用createdAt字段）
    data.createTime = data.createTime || data.createdAt
    
    // 4. 开始时间（从scheduleConfig中提取）
    if (data.scheduleConfig && data.scheduleConfig.startTime) {
      data.startTime = data.scheduleConfig.startTime
    } else {
      data.startTime = data.startTime || null
    }
    
    // 5. 结束时间（如果后端没有则使用默认值）
    data.endTime = data.endTime || data.expireTime || null
    
    // 6. 负责人（如果后端没有则使用默认值）
    data.owner = data.owner || '系统管理员'
    
    // 7. 其他可能缺失的字段
    data.collectorType = data.collectorType || (data.collectorId ? 'custom' : 'default')
    data.effectiveTime = data.effectiveTime || null
    data.expireTime = data.expireTime || null
    
    // 8. 调度配置处理
    if (data.scheduleConfig) {
      data.schedule = data.scheduleConfig
    } else {
      data.schedule = {}
    }
    
    console.log('处理后的任务数据:', {
      taskName: data.taskName,
      taskType: data.taskType,
      createTime: data.createTime,
      startTime: data.startTime,
      endTime: data.endTime,
      owner: data.owner
    })
    
    // 处理可能缺失的数组字段
    data.tags = data.tags || []
    data.devices = data.devices || []
    data.metrics = data.metrics || []
    
    // 处理指标数据
    if (data.metrics && Array.isArray(data.metrics)) {
      data.metrics = data.metrics.map(metric => {
        // 处理配置数据
        if (metric.config) {
          // 如果配置是字符串，尝试解析为JSON
          if (typeof metric.config === 'string') {
            try {
              metric.config = JSON.parse(metric.config)
            } catch (e) {
              // 如果解析失败，保持原样
              console.warn('无法解析指标配置JSON:', e)
              metric.config = { rawConfig: metric.config }
            }
          }
        } else {
          metric.config = {}
        }
        
        // 确保其他必要字段存在
        metric.name = metric.name || metric.metricName || '未命名指标'
        metric.type = metric.type || metric.metricType || 'unknown'
        metric.deviceType = metric.deviceType || 'camera'
        
        // 计算适用设备数量
        if (data.devices && Array.isArray(data.devices)) {
          metric.deviceCount = data.devices.filter(device => 
            device.deviceType === metric.deviceType
          ).length
        } else {
          metric.deviceCount = 0
        }
        
        // 根据指标类型设置默认配置
        if (metric.type === 'face_recognition' && (!metric.config || Object.keys(metric.config).length === 0)) {
          metric.config = {
            confidence: 80,
            faceDatabase: 'default',
            storeUnknownFaces: true
          }
        } else if (metric.type === 'object_detection' && (!metric.config || Object.keys(metric.config).length === 0)) {
          metric.config = {
            objects: ['person', 'vehicle'],
            confidence: 70,
            frequency: 'medium'
          }
        } else if (metric.type === 'temperature_humidity' && (!metric.config || Object.keys(metric.config).length === 0)) {
          metric.config = {
            tempMin: 18,
            tempMax: 28,
            humidityMin: 30,
            humidityMax: 60,
            samplingInterval: 60
          }
        } else if (metric.type === 'behavior_analysis' && (!metric.config || Object.keys(metric.config).length === 0)) {
          metric.config = {
            detectLoitering: true,
            detectCrowding: true,
            detectFalling: true,
            sensitivity: 75
          }
        }
        
        console.log(`处理指标 ${metric.name}, 类型: ${metric.type}, 配置:`, metric.config)
        return metric
      })
    }
    
    // 更新任务信息
    taskInfo.value = data
    
    console.log('任务详情数据:', taskInfo.value)
  } catch (error) {
    console.error('获取任务详情失败', error)
    ElMessage.error('获取任务详情失败')
  } finally {
    loading.value = false
  }
}

// 格式化日期
const formatDate = (date) => {
  if (!date) return '未设置';
  
  try {
    const d = new Date(date);
    
    // 检查日期是否有效
    if (isNaN(d.getTime())) {
      return '无效日期';
    }
    
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    const day = String(d.getDate()).padStart(2, '0');
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    const seconds = String(d.getSeconds()).padStart(2, '0');
    
    return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
  } catch (error) {
    console.error('日期格式化错误:', error);
    return '日期格式错误';
  }
}

// 获取调度描述
const getScheduleDescription = () => {
  const schedule = taskInfo.value.schedule || {};
  const taskType = taskInfo.value.scheduleType || taskInfo.value.taskType;
  
  if (!schedule || Object.keys(schedule).length === 0) {
    return '未配置调度信息';
  }
  
  // 根据任务的scheduleType来判断调度类型
  switch (taskType) {
    case 'once':
      return `单次执行，开始时间：${schedule.startTime || '未设置'}`;
    case 'interval':
      if (!schedule.intervalValue) return '间隔执行，未设置间隔';
      return `每 ${schedule.intervalValue} ${getIntervalUnitLabel(schedule.intervalUnit)} 执行一次，开始时间：${schedule.startTime || '未设置'}`;
    case 'cron':
      return `Cron调度：${schedule.cronExpression || '未设置表达式'}`;
    case 'realtime':
      return '实时执行，持续采集数据';
    case 'scheduled':
      if (schedule.frequency === 'once') {
        return `单次执行，时间：${formatTime(schedule.scheduleTime) || '未设置'}`;
      } else if (schedule.frequency === 'daily') {
        return `每天 ${formatTime(schedule.scheduleTime) || '未设置'} 执行`;
      } else if (schedule.frequency === 'weekly') {
        const weekdays = schedule.weekdays || [];
        if (weekdays.length === 0) return '每周执行，未设置具体日期';
        const days = weekdays.map(d => ['日', '一', '二', '三', '四', '五', '六'][d]).join('、');
        return `每周${days} ${formatTime(schedule.scheduleTime) || '未设置'} 执行`;
      } else if (schedule.frequency === 'monthly') {
        return `每月执行，时间：${formatTime(schedule.scheduleTime) || '未设置'}`;
      }
      return '定时执行';
    case 'periodic':
      if (!schedule.intervalValue) return '周期执行，未设置间隔';
      return `每 ${schedule.intervalValue} ${getIntervalUnitLabel(schedule.intervalUnit)} 执行一次`;
    case 'triggered':
      if (!schedule.triggerType) return '触发执行，未设置触发条件';
      return `触发执行，触发条件：${getTriggerTypeLabel(schedule.triggerType)} - ${getTriggerEventLabel(schedule.triggerEvent)}`;
    default:
      return `调度类型：${taskType || '未知'}`;
  }
}

// 格式化时间
const formatTime = (time) => {
  if (!time) return '';
  
  try {
    const d = new Date(time);
    
    // 检查时间是否有效
    if (isNaN(d.getTime())) {
      return '无效时间';
    }
    
    const hours = String(d.getHours()).padStart(2, '0');
    const minutes = String(d.getMinutes()).padStart(2, '0');
    
    return `${hours}:${minutes}`;
  } catch (error) {
    console.error('时间格式化错误:', error);
    return '时间格式错误';
  }
}

// 获取间隔单位标签
const getIntervalUnitLabel = (unit) => {
  if (!unit) return '未知单位';
  
  const map = {
    minutes: '分钟',
    hours: '小时',
    days: '天'
  };
  return map[unit] || unit;
}

// 获取触发类型标签
const getTriggerTypeLabel = (type) => {
  if (!type) return '未知触发类型';
  
  const map = {
    event: '事件触发',
    threshold: '阈值触发',
    api: 'API调用触发'
  };
  return map[type] || type;
}

// 获取触发事件标签
const getTriggerEventLabel = (event) => {
  if (!event) return '未知事件';
  
  const map = {
    motion: '运动检测',
    anomaly: '异常行为',
    alarm: '报警触发'
  };
  return map[event] || event;
}

// 获取任务类型标签类型
const getTaskTypeTag = (type) => {
  const map = {
    realtime: 'info',
    scheduled: 'primary',
    periodic: 'success',
    triggered: 'warning',
    interval: 'success',
    once: 'primary',
    cron: 'warning'
  }
  return map[type] || ''
}

// 获取任务类型标签文本
const getTaskTypeLabel = (type) => {
  const map = {
    realtime: '实时采集',
    scheduled: '定时采集',
    periodic: '周期采集',
    triggered: '触发式采集',
    interval: '间隔采集',
    once: '单次采集',
    cron: 'Cron调度'
  }
  return map[type] || type
}

// 获取状态类型
const getStatusType = (status) => {
  const map = {
    running: 'success',
    scheduled: 'primary',
    paused: 'warning',
    completed: 'info',
    failed: 'danger'
  }
  return map[status] || ''
}

// 获取状态标签
const getStatusLabel = (status) => {
  const map = {
    running: '运行中',
    scheduled: '已调度',
    paused: '已暂停',
    completed: '已完成',
    failed: '执行失败'
  }
  return map[status] || status
}

// 获取优先级标签
const getPriorityLabel = (priority) => {
  const map = {
    high: '高优先级',
    medium: '中优先级',
    low: '低优先级'
  }
  return map[priority] || priority
}

// 获取设备类型标签类型
const getDeviceTypeTagType = (type) => {
  if (!type) return '';
  
  const map = {
    camera: 'primary',
    sensor: 'info',
    controller: 'warning'
  }
  return map[type] || '';
}

// 获取设备类型标签文本
const getDeviceTypeLabel = (type) => {
  if (!type) return '未知设备';
  
  const map = {
    camera: '摄像头',
    sensor: '传感器',
    controller: '控制器'
  }
  return map[type] || type;
}

// 获取设备状态类型
const getDeviceStatusType = (status) => {
  if (!status) return '';
  
  const map = {
    online: 'success',
    offline: 'danger',
    maintenance: 'warning'
  }
  return map[status] || '';
}

// 获取设备状态标签
const getDeviceStatusLabel = (status) => {
  if (!status) return '未知状态';
  
  const map = {
    online: '在线',
    offline: '离线',
    maintenance: '维护中'
  }
  return map[status] || status;
}

// 获取指标类型标签
const getMetricTypeTag = (type) => {
  if (!type) return 'default';
  
  if (typeof type === 'string' && (type.includes('recognition') || type.includes('detection') || type.includes('analysis'))) {
    return 'info';
  }
  return 'primary';
}

// 获取指标类型标签文本
const getMetricTypeLabel = (type) => {
  if (!type) return '未知类型';
  
  const map = {
    face_recognition: '人脸识别',
    object_detection: '物体检测',
    behavior_analysis: '行为分析',
    boundary_detection: '越界检测',
    temperature_humidity: '温湿度数据',
    energy_consumption: '能耗数据',
    air_quality: '空气质量'
  }
  return map[type] || type;
}

// 获取人脸库标签
const getFaceDatabaseLabel = (database) => {
  if (!database) return '未选择人脸库';
  
  const map = {
    'default': '默认人脸库',
    'employee': '员工人脸库',
    'visitor': '访客人脸库',
    'blacklist': '黑名单人脸库',
    'staff': '员工人脸库',
    'vip': 'VIP人脸库'
  };
  return map[database] || database;
}

// 获取检测对象标签
const getDetectionObjectsLabel = (config) => {
  if (!config || !config.objects || !Array.isArray(config.objects) || config.objects.length === 0) {
    return '未设置检测对象';
  }
  
  const map = {
    'person': '人员',
    'vehicle': '车辆',
    'animal': '动物',
    'face': '人脸',
    'bag': '包/箱',
    'bicycle': '自行车/电动车'
  };
  
  return config.objects.map(obj => map[obj] || obj).join('、');
}

// 获取检测频率标签
const getDetectionFrequencyLabel = (frequency) => {
  if (!frequency) return '实时检测';
  
  const map = {
    'realtime': '实时检测',
    'high': '高频检测 (每秒多次)',
    'medium': '中频检测 (每秒一次)',
    'low': '低频检测 (每几秒一次)'
  };
  return map[frequency] || frequency;
}

// 获取采样间隔标签
const getSamplingIntervalLabel = (interval) => {
  if (!interval) return '未设置';
  
  // 如果是数字，添加秒单位
  if (!isNaN(interval)) {
    return `${interval}秒`;
  }
  
  return interval;
}

// 编辑任务
const handleEdit = () => {
  router.push(`/task/edit/${taskId}`)
}

// 启动任务
const handleStart = async () => {
  try {
    await ElMessageBox.confirm('确认要启动该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'info'
    })
    
    await startTask(taskId)
    ElMessage.success('启动任务成功')
    fetchTaskDetail()
  } catch (error) {
    if (error === 'cancel') return
    
    console.error('启动任务失败', error)
    ElMessage.error('启动任务失败')
  }
}

// 暂停任务
const handlePause = async () => {
  try {
    await ElMessageBox.confirm('确认要暂停该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await pauseTask(taskId)
    ElMessage.success('暂停任务成功')
    fetchTaskDetail()
  } catch (error) {
    if (error === 'cancel') return
    
    console.error('暂停任务失败', error)
    ElMessage.error('暂停任务失败')
  }
}

// 停止任务
const handleStop = async () => {
  try {
    await ElMessageBox.confirm('确认要停止该任务吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    await stopTask(taskId)
    ElMessage.success('停止任务成功')
    fetchTaskDetail()
  } catch (error) {
    if (error === 'cancel') return
    
    console.error('停止任务失败', error)
    ElMessage.error('停止任务失败')
  }
}

// 页面初始化
onMounted(() => {
  fetchTaskDetail()
})
</script>

<style scoped>
.card-header-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.card-header-title .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.device-name-cell {
  display: flex;
  flex-direction: column;
}

.device-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 5px;
  color: #2c3e50;
}

.device-id {
  font-size: 13px;
  color: #6c757d;
}

.task-priority {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 5px;
}

.priority-high {
  background-color: #dc3545;
}

.priority-medium {
  background-color: #ffc107;
}

.priority-low {
  background-color: #6c757d;
}

.status-item {
  margin-bottom: 10px;
}

.status-item .label {
  font-size: 14px;
  color: #6c757d;
  margin-bottom: 5px;
}

.status-item .value {
  font-size: 16px;
  font-weight: 500;
}

.metric-card {
  border: 1px solid #e9ecef;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 16px;
  transition: all 0.3s ease;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.metric-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.metric-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.metric-config {
  background-color: #f8f9fa;
  border-radius: 4px;
  padding: 10px;
  font-family: monospace;
  overflow-x: auto;
}

.metric-config pre {
  margin: 0;
  white-space: pre-wrap;
}

.config-item {
  margin-bottom: 10px;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", Arial, sans-serif;
  display: flex;
  align-items: center;
}

.config-label {
  font-weight: 600;
  color: #606266;
  width: 150px;
  flex-shrink: 0;
}

.config-value {
  color: #2c3e50;
  flex: 1;
}
</style> 