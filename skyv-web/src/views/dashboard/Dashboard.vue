<template>
  <div class="dashboard-container">
    <!-- AI 智能洞察面板 -->
    <el-card shadow="hover" class="ai-insights-card mb-20">
      <template #header>
        <div class="card-header">
          <div class="ai-header">
            <el-icon class="ai-icon"><CirclePlus /></el-icon>
            <div>
              <div class="ai-title">AI 智能洞察</div>
              <div class="ai-subtitle">基于数据分析为您提供智能建议</div>
            </div>
          </div>
          <el-button type="primary" link @click="handleAIConfig">配置AI</el-button>
        </div>
      </template>
      
      <el-row :gutter="15">
        <el-col :xs="24" :sm="12" :lg="6" v-for="(insight, index) in aiInsights" :key="index">
          <div class="insight-item" @click="handleInsightClick(insight)">
            <div class="insight-icon" :style="{ background: insight.color }">
              <el-icon><component :is="insight.icon" /></el-icon>
            </div>
            <div class="insight-content">
              <div class="insight-title">{{ insight.title }}</div>
              <div class="insight-desc">{{ insight.description }}</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 关键指标 -->
    <el-row :gutter="15" class="mb-20">
      <el-col :xs="24" :sm="12" :md="6" v-for="(metric, index) in metrics" :key="index">
        <el-card shadow="hover" class="metric-card" @click="handleMetricClick(metric)">
          <div class="metric-header">
            <div class="metric-info">
              <div class="metric-value">{{ metric.value }}</div>
              <div class="metric-label">{{ metric.label }}</div>
              <div class="metric-trend" :class="metric.trendClass">
                <el-icon><component :is="metric.trendIcon" /></el-icon>
                <span>{{ metric.trend }}</span>
              </div>
            </div>
            <div class="metric-icon" :class="metric.iconClass">
              <el-icon :size="24"><component :is="metric.icon" /></el-icon>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 主要内容区域 -->
    <el-row :gutter="15">
      <!-- 实时监控 -->
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover" class="monitoring-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">实时监控画面</span>
              <div class="monitoring-actions">
                <el-button-group size="small">
                  <el-button @click="handleFullscreen">
                    <el-icon><FullScreen /></el-icon>
                    全屏
                  </el-button>
                  <el-button @click="handleLayoutChange">
                    <el-icon><Grid /></el-icon>
                    布局
                  </el-button>
                  <el-button @click="handleMonitoringSettings">
                    <el-icon><Setting /></el-icon>
                    设置
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </template>
          
          <div class="camera-grid">
            <div 
              v-for="camera in cameras" 
              :key="camera.id" 
              class="camera-card" 
              @click="handleCameraClick(camera)"
            >
              <div class="camera-view">
                <div class="camera-placeholder">
                  <el-icon :size="32"><VideoCamera /></el-icon>
                  <div class="play-btn" @click.stop="handlePlayVideo(camera)">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                </div>
                <div class="status-indicator" :class="camera.statusClass"></div>
                <div class="camera-controls">
                  <el-button-group size="small">
                    <el-button @click.stop="handleCameraControl(camera, 'ptz')">
                      <el-icon><Compass /></el-icon>
                    </el-button>
                    <el-button @click.stop="handleCameraControl(camera, 'record')">
                      <el-icon><VideoCameraFilled /></el-icon>
                    </el-button>
                    <el-button @click.stop="handleCameraControl(camera, 'snapshot')">
                      <el-icon><Camera /></el-icon>
                    </el-button>
                  </el-button-group>
                </div>
              </div>
              <div class="camera-info">
                <div class="camera-name">{{ camera.name }}</div>
                <div class="camera-location">
                  <el-icon><MapLocation /></el-icon>
                  {{ camera.location }}
                </div>
                <div class="camera-status-bar">
                  <el-tag :type="camera.statusType" size="small">{{ camera.statusText }}</el-tag>
                  <span class="signal-strength">{{ camera.signal }}</span>
                </div>
              </div>
            </div>
          </div>
        </el-card>
      </el-col>

      <!-- 活动面板 -->
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover" class="activity-card">
          <template #header>
            <div class="card-header">
              <span class="card-title">系统活动</span>
              <el-button type="primary" link @click="handleViewAllActivities">查看全部</el-button>
            </div>
          </template>
          
          <el-tabs v-model="activeTab" class="activity-tabs" @tab-change="handleTabChange">
            <el-tab-pane label="最新报警" name="alerts">
              <div class="activity-list">
                <div 
                  v-for="alert in recentAlerts" 
                  :key="alert.id" 
                  class="activity-item"
                  @click="handleAlertClick(alert)"
                >
                  <div class="activity-icon" :class="alert.iconClass">
                    <el-icon><component :is="alert.icon" /></el-icon>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">{{ alert.title }}</div>
                    <div class="activity-desc">{{ alert.description }}</div>
                    <div class="activity-time">{{ alert.time }}</div>
                  </div>
                  <div class="activity-actions">
                    <el-button size="small" @click.stop="handleAlertAction(alert, 'handle')">处理</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="系统事件" name="events">
              <div class="activity-list">
                <div 
                  v-for="event in systemEvents" 
                  :key="event.id" 
                  class="activity-item"
                  @click="handleEventClick(event)"
                >
                  <div class="activity-icon" :class="event.iconClass">
                    <el-icon><component :is="event.icon" /></el-icon>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">{{ event.title }}</div>
                    <div class="activity-desc">{{ event.description }}</div>
                    <div class="activity-time">{{ event.time }}</div>
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="AI检测" name="ai">
              <div class="activity-list">
                <div 
                  v-for="detection in aiDetections" 
                  :key="detection.id" 
                  class="activity-item"
                  @click="handleDetectionClick(detection)"
                >
                  <div class="activity-icon" :class="detection.iconClass">
                    <el-icon><component :is="detection.icon" /></el-icon>
                  </div>
                  <div class="activity-content">
                    <div class="activity-title">{{ detection.title }}</div>
                    <div class="activity-desc">{{ detection.description }}</div>
                    <div class="activity-time">{{ detection.time }}</div>
                  </div>
                  <div class="activity-actions">
                    <el-button size="small" @click.stop="handleDetectionAction(detection, 'view')">查看</el-button>
                  </div>
                </div>
              </div>
            </el-tab-pane>
          </el-tabs>
        </el-card>
      </el-col>
    </el-row>

    <!-- 快速操作面板 -->
    <el-card shadow="hover" class="quick-actions-card mt-20">
      <template #header>
        <div class="card-header">
          <span class="card-title">快速操作</span>
          <el-button type="primary" link @click="handleCustomizeActions">自定义</el-button>
        </div>
      </template>
      
      <el-row :gutter="15">
        <el-col :xs="12" :sm="8" :md="4" v-for="action in quickActions" :key="action.id">
          <div class="quick-action-item" @click="handleQuickAction(action)">
            <div class="action-icon" :style="{ background: action.color }">
              <el-icon :size="24"><component :is="action.icon" /></el-icon>
            </div>
            <div class="action-title">{{ action.title }}</div>
            <div class="action-desc">{{ action.description }}</div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 对话框组件 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px" @close="handleDialogClose">
      <component :is="dialogComponent" :data="dialogData" @close="handleDialogClose" />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  CirclePlus, FullScreen, Grid, Setting, VideoCamera, VideoPlay, VideoCameraFilled, 
  Camera, MapLocation, Compass, Warning, Bell, User, Connection, 
  ArrowUp, ArrowDown, CircleCheck, Monitor, Key,
  Tools, Document, Notification, Histogram
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const activeTab = ref('alerts')
const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogComponent = ref(null)
const dialogData = ref(null)

// AI洞察数据
const aiInsights = ref([
  {
    title: '优化建议',
    description: '建议调整3号摄像头角度以优化监控覆盖范围',
    icon: 'CirclePlus',
    color: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    type: 'optimization'
  },
  {
    title: '趋势预测',
    description: '预计明日人流高峰期为9:00-11:00和14:00-17:00',
    icon: 'Histogram',
    color: 'linear-gradient(135deg, #f093fb 0%, #f5576c 100%)',
    type: 'prediction'
  },
  {
    title: '安全状态',
    description: '当前安全等级良好，所有关键区域监控正常',
    icon: 'Key',
    color: 'linear-gradient(135deg, #4facfe 0%, #00f2fe 100%)',
    type: 'security'
  },
  {
    title: '维护提醒',
    description: '设备B-205建议在本周进行例行维护检查',
    icon: 'Tools',
    color: 'linear-gradient(135deg, #43e97b 0%, #38f9d7 100%)',
    type: 'maintenance'
  }
])

// 关键指标数据
const metrics = ref([
  {
    label: '设备总数',
    value: '156',
    trend: '+8 本月新增',
    trendClass: 'trend-up',
    trendIcon: 'ArrowUp',
    icon: 'VideoCamera',
    iconClass: 'success',
    type: 'devices'
  },
  {
    label: '在线设备',
    value: '142',
    trend: '91.0% 在线率',
    trendClass: 'trend-up',
    trendIcon: 'ArrowUp',
    icon: 'Connection',
    iconClass: 'info',
    type: 'online'
  },
  {
    label: '今日报警',
    value: '23',
    trend: '-15% 较昨日',
    trendClass: 'trend-down',
    trendIcon: 'ArrowDown',
    icon: 'Bell',
    iconClass: 'warning',
    type: 'alerts'
  },
  {
    label: '今日识别',
    value: '1,847',
    trend: '+12% 较昨日',
    trendClass: 'trend-up',
    trendIcon: 'ArrowUp',
    icon: 'User',
    iconClass: 'danger',
    type: 'recognition'
  }
])

// 摄像头数据
const cameras = ref([
  {
    id: 1,
    name: '主入口监控',
    location: '大厅主入口',
    status: 'online',
    statusText: '在线',
    statusType: 'success',
    statusClass: 'online',
    signal: '信号强'
  },
  {
    id: 2,
    name: '停车场监控',
    location: 'B1层停车场',
    status: 'warning',
    statusText: '信号弱',
    statusType: 'warning',
    statusClass: 'warning',
    signal: '信号弱'
  },
  {
    id: 3,
    name: '办公区监控',
    location: '2F办公区域',
    status: 'online',
    statusText: '在线',
    statusType: 'success',
    statusClass: 'online',
    signal: '信号强'
  },
  {
    id: 4,
    name: '后门监控',
    location: '后门安全通道',
    status: 'online',
    statusText: '在线',
    statusType: 'success',
    statusClass: 'online',
    signal: '信号强'
  }
])

// 最新报警数据
const recentAlerts = ref([
  {
    id: 1,
    title: '异常行为检测',
    description: '主入口监控检测到可疑人员徘徊',
    time: '2分钟前',
    icon: 'Warning',
    iconClass: 'alert',
    level: 'high'
  },
  {
    id: 2,
    title: '未授权访问',
    description: '办公区域检测到未授权人员',
    time: '8分钟前',
    icon: 'Warning',
    iconClass: 'alert',
    level: 'high'
  },
  {
    id: 3,
    title: '异常声音',
    description: '后门监控检测到异常噪音',
    time: '35分钟前',
    icon: 'Bell',
    iconClass: 'alert',
    level: 'medium'
  }
])

// 系统事件数据
const systemEvents = ref([
  {
    id: 1,
    title: '设备重连',
    description: '停车场监控重新连接成功',
    time: '15分钟前',
    icon: 'Connection',
    iconClass: 'info'
  },
  {
    id: 2,
    title: '系统更新',
    description: 'AI识别模块更新完成',
    time: '1小时前',
    icon: 'CircleCheck',
    iconClass: 'success'
  }
])

// AI检测数据
const aiDetections = ref([
  {
    id: 1,
    title: '人脸识别',
    description: '员工张三成功通过身份验证',
    time: '23分钟前',
    icon: 'User',
    iconClass: 'success'
  },
  {
    id: 2,
    title: '行为分析',
    description: '检测到聚集行为，已记录',
    time: '45分钟前',
    icon: 'Monitor',
    iconClass: 'info'
  }
])

// 快速操作数据
const quickActions = ref([
  {
    id: 1,
    title: '添加设备',
    description: '接入新设备',
    icon: 'CirclePlus',
    color: '#409EFF'
  },
  {
    id: 2,
    title: '系统报告',
    description: '生成报告',
    icon: 'Document',
    color: '#67C23A'
  },
  {
    id: 3,
    title: '报警设置',
    description: '配置规则',
    icon: 'Notification',
    color: '#E6A23C'
  },
  {
    id: 4,
    title: '系统维护',
    description: '维护模式',
    icon: 'Tools',
    color: '#F56C6C'
  },
  {
    id: 5,
    title: '数据备份',
    description: '备份数据',
    icon: 'Key',
    color: '#909399'
  },
  {
    id: 6,
    title: '用户管理',
    description: '管理用户',
    icon: 'User',
    color: '#606266'
  }
])

// 事件处理函数
const handleAIConfig = () => {
  ElMessage.info('AI配置功能开发中...')
}

const handleInsightClick = (insight) => {
  ElMessage.info(`查看${insight.title}详情`)
}

const handleMetricClick = (metric) => {
  switch(metric.type) {
    case 'devices':
      ElMessage.info('跳转到设备管理页面')
      break
    case 'alerts':
      ElMessage.info('跳转到报警中心')
      break
    default:
      ElMessage.info(`查看${metric.label}详情`)
  }
}

const handleFullscreen = () => {
  ElMessage.info('进入全屏监控模式')
}

const handleLayoutChange = () => {
  ElMessage.info('切换监控布局')
}

const handleMonitoringSettings = () => {
  ElMessage.info('打开监控设置')
}

const handleCameraClick = (camera) => {
  ElMessage.info(`查看${camera.name}详情`)
}

const handlePlayVideo = (camera) => {
  ElMessage.info(`播放${camera.name}视频流`)
}

const handleCameraControl = (camera, action) => {
  const actionMap = {
    ptz: '云台控制',
    record: '开始录制',
    snapshot: '拍照'
  }
  ElMessage.info(`${camera.name} - ${actionMap[action]}`)
}

const handleTabChange = (tabName) => {
  ElMessage.info(`切换到${tabName}标签`)
}

const handleViewAllActivities = () => {
  ElMessage.info('查看所有活动')
}

const handleAlertClick = (alert) => {
  ElMessage.info(`查看报警: ${alert.title}`)
}

const handleAlertAction = (alert, action) => {
  ElMessage.success(`报警已${action === 'handle' ? '处理' : '操作'}`)
}

const handleEventClick = (event) => {
  ElMessage.info(`查看事件: ${event.title}`)
}

const handleDetectionClick = (detection) => {
  ElMessage.info(`查看检测: ${detection.title}`)
}

const handleDetectionAction = (detection, action) => {
  ElMessage.info(`${action === 'view' ? '查看' : '操作'}检测结果`)
}

const handleCustomizeActions = () => {
  ElMessage.info('自定义快速操作')
}

const handleQuickAction = (action) => {
  ElMessage.info(`执行: ${action.title}`)
}

const handleDialogClose = () => {
  dialogVisible.value = false
  dialogComponent.value = null
  dialogData.value = null
}

// 初始化
onMounted(() => {
  ElMessage.success('SkyEye智能监控系统已就绪')
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 20px;
  background-color: #f5f7fa;
}

// AI洞察卡片
.ai-insights-card {
  border-radius: 8px;
  
  .ai-header {
    display: flex;
    align-items: center;
    gap: 15px;
    
    .ai-icon {
      width: 40px;
      height: 40px;
      background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
      border-radius: 10px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
    }
    
    .ai-title {
      font-size: 18px;
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .ai-subtitle {
      font-size: 14px;
      color: #909399;
    }
  }
}

.insight-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #f5f7fa;
    transform: translateY(-2px);
    box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  }
  
  .insight-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    flex-shrink: 0;
  }
  
  .insight-content {
    flex: 1;
    
    .insight-title {
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .insight-desc {
      font-size: 13px;
      color: #606266;
      line-height: 1.4;
    }
  }
}

// 指标卡片
.metric-card {
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  .metric-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
  }
  
  .metric-info {
    flex: 1;
    
    .metric-value {
      font-size: 28px;
      font-weight: 700;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .metric-label {
      font-size: 14px;
      color: #909399;
      margin-bottom: 8px;
    }
    
    .metric-trend {
      font-size: 12px;
      font-weight: 600;
      display: flex;
      align-items: center;
      gap: 4px;
      
      &.trend-up {
        color: #67c23a;
      }
      
      &.trend-down {
        color: #f56c6c;
      }
    }
  }
  
  .metric-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    
    &.success {
      background: linear-gradient(135deg, #67c23a, #85ce61);
    }
    
    &.info {
      background: linear-gradient(135deg, #409eff, #66b1ff);
    }
    
    &.warning {
      background: linear-gradient(135deg, #e6a23c, #ebb563);
    }
    
    &.danger {
      background: linear-gradient(135deg, #f56c6c, #f78989);
    }
  }
}

// 监控卡片
.monitoring-card {
  border-radius: 8px;
  
  .monitoring-actions {
    display: flex;
    gap: 10px;
  }
}

.camera-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
  gap: 16px;
}

.camera-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s;
  cursor: pointer;
  
  &:hover {
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  .camera-view {
    height: 180px;
    background: linear-gradient(135deg, #2c3e50, #34495e);
    position: relative;
    display: flex;
    align-items: center;
    justify-content: center;
    
    .camera-placeholder {
      color: #95a5a6;
      display: flex;
      flex-direction: column;
      align-items: center;
      gap: 8px;
    }
    
    .play-btn {
      position: absolute;
      width: 48px;
      height: 48px;
      background: rgba(64, 158, 255, 0.9);
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      color: white;
      cursor: pointer;
      opacity: 0;
      transition: all 0.3s;
      
      &:hover {
        background: #409eff;
        transform: scale(1.1);
      }
    }
    
    &:hover .play-btn {
      opacity: 1;
    }
    
    .status-indicator {
      position: absolute;
      top: 12px;
      right: 12px;
      width: 12px;
      height: 12px;
      border-radius: 50%;
      
      &.online {
        background: #67c23a;
        animation: pulse 2s infinite;
      }
      
      &.warning {
        background: #e6a23c;
      }
      
      &.offline {
        background: #f56c6c;
      }
    }
    
    .camera-controls {
      position: absolute;
      bottom: 12px;
      right: 12px;
      opacity: 0;
      transition: all 0.3s;
    }
    
    &:hover .camera-controls {
      opacity: 1;
    }
  }
  
  .camera-info {
    padding: 16px;
    
    .camera-name {
      font-weight: 600;
      color: #303133;
      margin-bottom: 8px;
    }
    
    .camera-location {
      font-size: 13px;
      color: #909399;
      display: flex;
      align-items: center;
      gap: 4px;
      margin-bottom: 12px;
    }
    
    .camera-status-bar {
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .signal-strength {
        font-size: 12px;
        color: #606266;
      }
    }
  }
}

// 活动卡片
.activity-card {
  border-radius: 8px;
  
  .activity-tabs {
    :deep(.el-tabs__nav-wrap) {
      background: #f5f7fa;
      border-radius: 6px;
      padding: 4px;
    }
    
    :deep(.el-tabs__item) {
      border-radius: 4px;
      font-size: 13px;
      
      &.is-active {
        background: #409eff;
        color: white;
      }
    }
  }
}

.activity-list {
  max-height: 400px;
  overflow-y: auto;
  
  &::-webkit-scrollbar {
    width: 6px;
  }
  
  &::-webkit-scrollbar-thumb {
    background: #c0c4cc;
    border-radius: 3px;
  }
}

.activity-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 16px 0;
  border-bottom: 1px solid #f0f2f5;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #f5f7fa;
  }
  
  &:last-child {
    border-bottom: none;
  }
  
  .activity-icon {
    width: 36px;
    height: 36px;
    border-radius: 8px;
    display: flex;
    align-items: center;
    justify-content: center;
    flex-shrink: 0;
    
    &.alert {
      background: rgba(245, 108, 108, 0.1);
      color: #f56c6c;
    }
    
    &.info {
      background: rgba(64, 158, 255, 0.1);
      color: #409eff;
    }
    
    &.success {
      background: rgba(103, 194, 58, 0.1);
      color: #67c23a;
    }
  }
  
  .activity-content {
    flex: 1;
    
    .activity-title {
      font-weight: 600;
      color: #303133;
      margin-bottom: 4px;
    }
    
    .activity-desc {
      font-size: 13px;
      color: #606266;
      margin-bottom: 4px;
      line-height: 1.4;
    }
    
    .activity-time {
      font-size: 12px;
      color: #909399;
    }
  }
  
  .activity-actions {
    flex-shrink: 0;
  }
}

// 快速操作卡片
.quick-actions-card {
  border-radius: 8px;
}

.quick-action-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  padding: 20px 16px;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.3s;
  
  &:hover {
    background: #f5f7fa;
    transform: translateY(-4px);
    box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  }
  
  .action-icon {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    margin-bottom: 12px;
  }
  
  .action-title {
    font-weight: 600;
    color: #303133;
    margin-bottom: 4px;
  }
  
  .action-desc {
    font-size: 12px;
    color: #909399;
  }
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

.mb-20 {
  margin-bottom: 20px;
}

.mt-20 {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .card-title {
    font-size: 16px;
    font-weight: 600;
    color: #303133;
  }
}
</style> 