<template>
  <div class="alert-center-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">
            <el-icon class="title-icon"><Bell /></el-icon>
            智能报警中心
          </h2>
          <p class="page-subtitle">全方位监控报警事件，智能分析与快速响应</p>
        </div>
        <div class="header-actions">
          <el-button-group>
            <el-button type="primary" @click="openAlertSettings">
              <el-icon><Setting /></el-icon>
              报警设置
            </el-button>
            <el-button @click="exportReports">
              <el-icon><Download /></el-icon>
              导出报告
            </el-button>
          </el-button-group>
          <el-button type="danger" @click="handleEmergency">
            <el-icon><Warning /></el-icon>
            紧急处理
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="stats-overview">
      <el-row :gutter="20">
        <el-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-card critical-alerts" @click="filterByStatus('critical')">
            <div class="stat-icon">
              <el-icon><Warning /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ alertStats.critical }}</div>
              <div class="stat-label">紧急报警</div>
              <div class="stat-trend danger">
                <el-icon><ArrowUp /></el-icon>
                +{{ alertStats.criticalTrend }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-card pending-alerts" @click="filterByStatus('pending')">
            <div class="stat-icon">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ alertStats.pending }}</div>
              <div class="stat-label">待处理</div>
              <div class="stat-trend warning">
                <el-icon><Minus /></el-icon>
                {{ alertStats.pendingTrend }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-card processing-alerts" @click="filterByStatus('processing')">
            <div class="stat-icon">
              <el-icon><Loading /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ alertStats.processing }}</div>
              <div class="stat-label">处理中</div>
              <div class="stat-trend info">
                <el-icon><ArrowDown /></el-icon>
                -{{ alertStats.processingTrend }}%
              </div>
            </div>
          </div>
        </el-col>
        <el-col :xs="12" :sm="12" :md="6" :lg="6">
          <div class="stat-card resolved-alerts" @click="filterByStatus('resolved')">
            <div class="stat-icon">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ alertStats.resolved }}</div>
              <div class="stat-label">今日已解决</div>
              <div class="stat-trend success">
                <el-icon><ArrowUp /></el-icon>
                +{{ alertStats.resolvedTrend }}%
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 智能筛选与搜索 -->
    <div class="filter-section">
      <el-card shadow="never" class="filter-card">
        <div class="filter-content">
          <el-row :gutter="16">
            <el-col :xs="24" :sm="12" :md="6" :lg="4">
              <div class="filter-item">
                <label class="filter-label">报警级别</label>
                <el-select v-model="filters.level" placeholder="全部级别" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="紧急" value="critical" />
                  <el-option label="高级" value="high" />
                  <el-option label="中级" value="medium" />
                  <el-option label="低级" value="low" />
                </el-select>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="4">
              <div class="filter-item">
                <label class="filter-label">处理状态</label>
                <el-select v-model="filters.status" placeholder="全部状态" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="未处理" value="pending" />
                  <el-option label="处理中" value="processing" />
                  <el-option label="已解决" value="resolved" />
                  <el-option label="已忽略" value="ignored" />
                </el-select>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="4">
              <div class="filter-item">
                <label class="filter-label">报警类型</label>
                <el-select v-model="filters.type" placeholder="全部类型" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="入侵检测" value="intrusion" />
                  <el-option label="行为异常" value="behavior" />
                  <el-option label="设备故障" value="device" />
                  <el-option label="网络异常" value="network" />
                  <el-option label="AI分析" value="ai" />
                </el-select>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="4">
              <div class="filter-item">
                <label class="filter-label">设备区域</label>
                <el-select v-model="filters.area" placeholder="全部区域" clearable>
                  <el-option label="全部" value="" />
                  <el-option label="主入口" value="main_entrance" />
                  <el-option label="办公区域" value="office" />
                  <el-option label="停车场" value="parking" />
                  <el-option label="仓储区" value="warehouse" />
                </el-select>
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="4">
              <div class="filter-item">
                <label class="filter-label">时间范围</label>
                <el-date-picker
                  v-model="filters.dateRange"
                  type="datetimerange"
                  range-separator="至"
                  start-placeholder="开始时间"
                  end-placeholder="结束时间"
                  format="MM-DD HH:mm"
                  value-format="YYYY-MM-DD HH:mm:ss"
                />
              </div>
            </el-col>
            <el-col :xs="24" :sm="12" :md="6" :lg="4">
              <div class="filter-actions">
                <el-button-group>
                  <el-button type="primary" @click="applyFilters">
                    <el-icon><Search /></el-icon>
                    筛选
                  </el-button>
                  <el-button @click="resetFilters">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-button-group>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 报警列表 -->
    <div class="alert-list-section">
      <div class="list-header">
        <div class="list-tools">
          <el-button-group>
            <el-button 
              :type="viewMode === 'card' ? 'primary' : ''" 
              @click="viewMode = 'card'"
            >
              <el-icon><Grid /></el-icon>
              卡片视图
            </el-button>
            <el-button 
              :type="viewMode === 'table' ? 'primary' : ''" 
              @click="viewMode = 'table'"
            >
              <el-icon><List /></el-icon>
              列表视图
            </el-button>
          </el-button-group>
          <el-button @click="batchProcess" :disabled="selectedAlerts.length === 0">
            <el-icon><Operation /></el-icon>
            批量处理 ({{ selectedAlerts.length }})
          </el-button>
        </div>
        <div class="list-info">
          共 {{ filteredAlerts.length }} 条报警，已选择 {{ selectedAlerts.length }} 条
        </div>
      </div>

      <!-- 卡片视图 -->
      <div v-if="viewMode === 'card'" class="alert-cards">
        <div 
          v-for="alert in paginatedAlerts" 
          :key="alert.id" 
          class="alert-card"
          :class="[`alert-${alert.level}`, { 'selected': selectedAlerts.includes(alert.id) }]"
          @click="toggleSelection(alert.id)"
        >
          <div class="card-header">
            <div class="alert-info">
              <div class="alert-type-icon">
                <el-icon><component :is="getAlertIcon(alert.type)" /></el-icon>
              </div>
              <div class="alert-basic-info">
                <h4 class="alert-title">{{ alert.title }}</h4>
                <div class="alert-meta">
                  <span class="alert-location">
                    <el-icon><Location /></el-icon>
                    {{ alert.location }}
                  </span>
                  <span class="alert-time">
                    <el-icon><Clock /></el-icon>
                    {{ formatTime(alert.time) }}
                  </span>
                </div>
              </div>
            </div>
            <div class="alert-badges">
              <el-tag 
                :type="getLevelTagType(alert.level)" 
                size="small"
                class="level-badge"
              >
                {{ getLevelText(alert.level) }}
              </el-tag>
              <el-tag 
                :type="getStatusTagType(alert.status)" 
                size="small"
                class="status-badge"
              >
                {{ getStatusText(alert.status) }}
              </el-tag>
            </div>
          </div>

          <div class="card-body">
            <div class="alert-content">
              <div class="alert-image" v-if="alert.snapshot">
                <img :src="alert.snapshot" :alt="alert.title" />
                <div class="image-overlay">
                  <el-button size="small" @click.stop="viewSnapshot(alert)">
                    <el-icon><View /></el-icon>
                    查看
                  </el-button>
                </div>
              </div>
              <div class="alert-description">
                <p>{{ alert.description }}</p>
                <div class="alert-tags" v-if="alert.tags">
                  <el-tag 
                    v-for="tag in alert.tags" 
                    :key="tag" 
                    size="small"
                    class="alert-tag"
                  >
                    {{ tag }}
                  </el-tag>
                </div>
              </div>
            </div>
          </div>

          <div class="card-footer">
            <div class="alert-details">
              <span class="confidence" v-if="alert.confidence">
                <el-icon><MagicStick /></el-icon>
                AI置信度: {{ alert.confidence }}%
              </span>
              <span class="severity-score">
                <el-icon><Histogram /></el-icon>
                风险评分: {{ alert.severityScore }}
              </span>
            </div>
            <div class="alert-actions">
              <el-button-group>
                <el-button 
                  size="small" 
                  @click.stop="processAlert(alert)"
                  :disabled="alert.status !== 'pending'"
                >
                  <el-icon><Check /></el-icon>
                  处理
                </el-button>
                <el-button 
                  size="small" 
                  @click.stop="viewAlertDetail(alert)"
                >
                  <el-icon><View /></el-icon>
                  详情
                </el-button>
                <el-button 
                  size="small" 
                  @click.stop="ignoreAlert(alert)"
                  :disabled="alert.status !== 'pending'"
                >
                  <el-icon><Close /></el-icon>
                  忽略
                </el-button>
              </el-button-group>
            </div>
          </div>
        </div>
      </div>

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredAlerts.length"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell, Setting, Download, Warning, Clock, Loading, CircleCheck,
  ArrowUp, ArrowDown, Minus, Search, Refresh, Grid, List, Operation,
  Location, View, Check, Close, MagicStick, Histogram
} from '@element-plus/icons-vue'

// 响应式数据
const loading = ref(false)
const viewMode = ref('card')
const selectedAlerts = ref([])

// 统计数据
const alertStats = reactive({
  critical: 7,
  criticalTrend: 15,
  pending: 23,
  pendingTrend: 8,
  processing: 5,
  processingTrend: 12,
  resolved: 156,
  resolvedTrend: 25
})

// 筛选条件
const filters = reactive({
  level: '',
  status: '',
  type: '',
  area: '',
  dateRange: null
})

// 分页
const pagination = reactive({
  currentPage: 1,
  pageSize: 20,
  total: 0
})

// Mock 数据
const mockAlerts = ref([
  {
    id: 'ALT001',
    title: '未授权人员进入',
    type: 'intrusion',
    level: 'critical',
    status: 'pending',
    location: '主入口大厅',
    time: new Date(Date.now() - 10 * 60 * 1000),
    description: '系统检测到未授权人员在非工作时间进入主入口大厅区域，该人员未佩戴工作证件，行为可疑',
    snapshot: 'https://via.placeholder.com/400x200/ff4757/ffffff?text=未授权入侵',
    confidence: 95,
    severityScore: 8.5,
    tags: ['入侵检测', '非工作时间', '人员识别']
  },
  {
    id: 'ALT002',
    title: '异常聚集行为',
    type: 'behavior',
    level: 'high',
    status: 'processing',
    location: '办公区域A栋',
    time: new Date(Date.now() - 25 * 60 * 1000),
    description: '检测到办公区域有超过10人的异常聚集，持续时间超过15分钟，可能存在安全隐患',
    snapshot: 'https://via.placeholder.com/400x200/2f3542/ffffff?text=异常聚集',
    confidence: 87,
    severityScore: 7.2,
    tags: ['行为分析', '人群聚集', '安全预警']
  },
  {
    id: 'ALT003',
    title: '设备离线警报',
    type: 'device',
    level: 'medium',
    status: 'pending',
    location: '停车场B区',
    time: new Date(Date.now() - 45 * 60 * 1000),
    description: '停车场B区摄像头设备突然离线，无法获取实时监控画面，请检查网络连接',
    snapshot: null,
    confidence: 100,
    severityScore: 5.5,
    tags: ['设备故障', '网络异常', '硬件问题']
  },
  {
    id: 'ALT004',
    title: '可疑物品识别',
    type: 'ai',
    level: 'high',
    status: 'pending',
    location: '仓储区入口',
    time: new Date(Date.now() - 35 * 60 * 1000),
    description: 'AI系统在仓储区入口识别到可疑包裹，物品形状异常，建议立即进行人工检查',
    snapshot: 'https://via.placeholder.com/400x200/ff6b6b/ffffff?text=可疑物品',
    confidence: 78,
    severityScore: 7.8,
    tags: ['AI识别', '可疑物品', '安全检查']
  },
  {
    id: 'ALT005',
    title: '网络连接异常',
    type: 'network',
    level: 'low',
    status: 'resolved',
    location: '办公区域B栋',
    time: new Date(Date.now() - 120 * 60 * 1000),
    description: '办公区域B栋网络连接出现间歇性中断，已自动恢复正常',
    snapshot: null,
    confidence: 95,
    severityScore: 3.2,
    tags: ['网络异常', '自动恢复', '系统维护']
  },
  {
    id: 'ALT006',
    title: '异常徘徊行为',
    type: 'behavior',
    level: 'medium',
    status: 'pending',
    location: '主入口大厅',
    time: new Date(Date.now() - 15 * 60 * 1000),
    description: '检测到有人员在主入口大厅区域徘徊超过5分钟，行为异常',
    snapshot: 'https://via.placeholder.com/400x200/54a0ff/ffffff?text=异常徘徊',
    confidence: 82,
    severityScore: 6.0,
    tags: ['行为分析', '异常徘徊', '人员监控']
  }
])

// 计算属性
const filteredAlerts = computed(() => {
  let alerts = mockAlerts.value

  if (filters.level) {
    alerts = alerts.filter(alert => alert.level === filters.level)
  }
  if (filters.status) {
    alerts = alerts.filter(alert => alert.status === filters.status)
  }
  if (filters.type) {
    alerts = alerts.filter(alert => alert.type === filters.type)
  }
  
  return alerts
})

const paginatedAlerts = computed(() => {
  const start = (pagination.currentPage - 1) * pagination.pageSize
  const end = start + pagination.pageSize
  return filteredAlerts.value.slice(start, end)
})

// 方法
const formatTime = (time) => {
  if (!time) return ''
  const now = new Date()
  const diff = now - new Date(time)
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(minutes / 60)
  const days = Math.floor(hours / 24)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  
  return new Intl.DateTimeFormat('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  }).format(new Date(time))
}

const getAlertIcon = (type) => {
  const icons = {
    intrusion: 'Warning',
    behavior: 'User',
    device: 'Monitor',
    network: 'Connection',
    ai: 'MagicStick'
  }
  return icons[type] || 'Bell'
}

const getLevelTagType = (level) => {
  const types = {
    critical: 'danger',
    high: 'danger',
    medium: 'warning',
    low: 'info'
  }
  return types[level] || 'info'
}

const getLevelText = (level) => {
  const texts = {
    critical: '紧急',
    high: '高级',
    medium: '中级',
    low: '低级'
  }
  return texts[level] || '未知'
}

const getStatusTagType = (status) => {
  const types = {
    pending: 'danger',
    processing: 'warning',
    resolved: 'success',
    ignored: 'info'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    pending: '未处理',
    processing: '处理中',
    resolved: '已解决',
    ignored: '已忽略'
  }
  return texts[status] || '未知'
}

const filterByStatus = (status) => {
  filters.status = status
  applyFilters()
}

const applyFilters = () => {
  pagination.currentPage = 1
  // 筛选逻辑已在计算属性中实现
}

const resetFilters = () => {
  Object.keys(filters).forEach(key => {
    if (key === 'dateRange') {
      filters[key] = null
    } else {
      filters[key] = ''
    }
  })
  pagination.currentPage = 1
}

const toggleSelection = (alertId) => {
  const index = selectedAlerts.value.indexOf(alertId)
  if (index > -1) {
    selectedAlerts.value.splice(index, 1)
  } else {
    selectedAlerts.value.push(alertId)
  }
}

const processAlert = (alert) => {
  ElMessageBox.prompt('请输入处理备注', '处理报警', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '输入处理说明...'
  }).then(({ value }) => {
    alert.status = 'resolved'
    ElMessage.success('报警处理成功')
  }).catch(() => {
    // 用户取消
  })
}

const viewAlertDetail = (alert) => {
  ElMessage.info(`查看报警详情: ${alert.title}`)
}

const ignoreAlert = (alert) => {
  ElMessageBox.confirm(
    '确定要忽略这条报警吗？',
    '确认忽略',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    alert.status = 'ignored'
    ElMessage.success('报警已忽略')
  }).catch(() => {
    // 用户取消
  })
}

const batchProcess = () => {
  ElMessage.info('批量处理功能开发中...')
}

const openAlertSettings = () => {
  ElMessage.info('跳转到报警设置页面')
}

const exportReports = () => {
  ElMessage.success('报告导出中...')
}

const handleEmergency = () => {
  ElMessage.warning('启动紧急处理流程')
}

const viewSnapshot = (alert) => {
  ElMessage.info(`查看${alert.title}的现场快照`)
}

const handleSizeChange = (size) => {
  pagination.pageSize = size
  pagination.currentPage = 1
}

const handleCurrentChange = (page) => {
  pagination.currentPage = page
}

// 初始化
onMounted(() => {
  pagination.total = filteredAlerts.value.length
})
</script>

<style lang="scss" scoped>
.alert-center-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 24px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 24px 32px;
    color: white;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
    
    .header-left {
      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 28px;
        font-weight: 700;
        margin: 0 0 8px 0;
        
        .title-icon {
          color: #ffd700;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
        }
      }
      
      .page-subtitle {
        margin: 0;
        font-size: 16px;
        opacity: 0.9;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
      
      .el-button {
        border-color: rgba(255, 255, 255, 0.3);
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
          border-color: rgba(255, 255, 255, 0.5);
        }
      }
    }
  }
}

.stats-overview {
  margin-bottom: 24px;
  
  .stat-card {
    display: flex;
    align-items: center;
    padding: 20px;
    background: white;
    border-radius: 12px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s;
    cursor: pointer;
    
    &:hover {
      transform: translateY(-2px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 50%;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 24px;
      margin-right: 16px;
      
      .el-icon {
        color: white;
      }
    }
    
    .stat-content {
      flex: 1;
      
      .stat-value {
        font-size: 32px;
        font-weight: 700;
        line-height: 1;
        margin-bottom: 4px;
      }
      
      .stat-label {
        color: #7f8c8d;
        font-size: 14px;
        margin-bottom: 4px;
      }
      
      .stat-trend {
        font-size: 12px;
        display: flex;
        align-items: center;
        gap: 4px;
        
        &.danger { color: #f56c6c; }
        &.warning { color: #e6a23c; }
        &.info { color: #409eff; }
        &.success { color: #67c23a; }
      }
    }
    
    &.critical-alerts .stat-icon {
      background: linear-gradient(135deg, #ff4757 0%, #ff3838 100%);
    }
    
    &.pending-alerts .stat-icon {
      background: linear-gradient(135deg, #ffa502 0%, #ff6348 100%);
    }
    
    &.processing-alerts .stat-icon {
      background: linear-gradient(135deg, #3742fa 0%, #2f3542 100%);
    }
    
    &.resolved-alerts .stat-icon {
      background: linear-gradient(135deg, #2ed573 0%, #1e90ff 100%);
    }
  }
}

.filter-section {
  margin-bottom: 24px;
  
  .filter-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .filter-content {
      .filter-item {
        .filter-label {
          display: block;
          margin-bottom: 6px;
          font-size: 13px;
          color: #606266;
          font-weight: 500;
        }
        
        .el-select,
        .el-date-picker {
          width: 100%;
        }
      }
      
      .filter-actions {
        display: flex;
        align-items: flex-end;
        height: 100%;
        padding-top: 22px;
      }
    }
  }
}

.alert-list-section {
  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 16px;
    
    .list-tools {
      display: flex;
      gap: 12px;
    }
    
    .list-info {
      color: #7f8c8d;
      font-size: 14px;
    }
  }
}

.alert-cards {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.alert-card {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  transition: all 0.3s;
  border: 2px solid transparent;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  cursor: pointer;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
  
  &.selected {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
  
  &.alert-critical {
    border-left: 4px solid #f56c6c;
    
    &:hover {
      border-left-color: #f56c6c;
      box-shadow: 0 8px 25px rgba(245, 108, 108, 0.2);
    }
  }
  
  &.alert-high {
    border-left: 4px solid #e6a23c;
    
    &:hover {
      border-left-color: #e6a23c;
      box-shadow: 0 8px 25px rgba(230, 162, 60, 0.2);
    }
  }
  
  &.alert-medium {
    border-left: 4px solid #409eff;
    
    &:hover {
      border-left-color: #409eff;
      box-shadow: 0 8px 25px rgba(64, 158, 255, 0.2);
    }
  }
  
  &.alert-low {
    border-left: 4px solid #67c23a;
    
    &:hover {
      border-left-color: #67c23a;
      box-shadow: 0 8px 25px rgba(103, 194, 90, 0.2);
    }
  }
  
  .card-header {
    padding: 16px 20px;
    border-bottom: 1px solid #f0f2f5;
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .alert-info {
      display: flex;
      gap: 12px;
      flex: 1;
      
      .alert-type-icon {
        width: 40px;
        height: 40px;
        border-radius: 50%;
        background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
        display: flex;
        align-items: center;
        justify-content: center;
        color: white;
        font-size: 18px;
        flex-shrink: 0;
      }
      
      .alert-basic-info {
        flex: 1;
        
        .alert-title {
          font-size: 16px;
          font-weight: 600;
          color: #2c3e50;
          margin: 0 0 6px 0;
          line-height: 1.3;
        }
        
        .alert-meta {
          display: flex;
          gap: 16px;
          font-size: 12px;
          color: #7f8c8d;
          
          .alert-location,
          .alert-time {
            display: flex;
            align-items: center;
            gap: 4px;
          }
        }
      }
    }
    
    .alert-badges {
      display: flex;
      flex-direction: column;
      gap: 6px;
      align-items: flex-end;
    }
  }
  
  .card-body {
    padding: 16px 20px;
    
    .alert-content {
      display: flex;
      gap: 16px;
      
      .alert-image {
        position: relative;
        width: 120px;
        height: 80px;
        border-radius: 8px;
        overflow: hidden;
        flex-shrink: 0;
        
        img {
          width: 100%;
          height: 100%;
          object-fit: cover;
        }
        
        .image-overlay {
          position: absolute;
          top: 0;
          left: 0;
          right: 0;
          bottom: 0;
          background: rgba(0, 0, 0, 0.5);
          display: flex;
          align-items: center;
          justify-content: center;
          opacity: 0;
          transition: opacity 0.3s;
          
          .el-button {
            color: white;
            border-color: white;
            
            &:hover {
              background-color: rgba(255, 255, 255, 0.2);
            }
          }
        }
        
        &:hover .image-overlay {
          opacity: 1;
        }
      }
      
      .alert-description {
        flex: 1;
        
        p {
          color: #606266;
          font-size: 14px;
          line-height: 1.6;
          margin: 0 0 12px 0;
        }
        
        .alert-tags {
          display: flex;
          flex-wrap: wrap;
          gap: 6px;
          
          .alert-tag {
            font-size: 11px;
            padding: 2px 6px;
          }
        }
      }
    }
  }
  
  .card-footer {
    padding: 12px 20px;
    background: #fafbfc;
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .alert-details {
      display: flex;
      gap: 16px;
      font-size: 12px;
      color: #7f8c8d;
      
      .confidence,
      .severity-score {
        display: flex;
        align-items: center;
        gap: 4px;
      }
    }
    
    .alert-actions {
      .el-button-group .el-button {
        font-size: 12px;
        padding: 4px 8px;
      }
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.alert-detail-content {
  .detail-section {
    margin-bottom: 24px;
    
    h4 {
      color: #2c3e50;
      font-size: 16px;
      font-weight: 600;
      margin: 0 0 16px 0;
      padding-bottom: 8px;
      border-bottom: 1px solid #e4e7ed;
    }
    
    .detail-info {
      .info-item {
        display: flex;
        margin-bottom: 12px;
        
        label {
          width: 100px;
          color: #7f8c8d;
          font-size: 14px;
        }
        
        span {
          color: #2c3e50;
          font-size: 14px;
        }
      }
    }
    
    .snapshot-container {
      img {
        width: 100%;
        max-width: 400px;
        border-radius: 8px;
        box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
      }
    }
    
    .ai-analysis {
      .analysis-item {
        display: flex;
        margin-bottom: 12px;
        
        label {
          width: 80px;
          color: #7f8c8d;
          font-size: 14px;
        }
        
        span {
          color: #2c3e50;
          font-size: 14px;
        }
      }
    }
    
    .timeline-content {
      strong {
        color: #2c3e50;
        font-size: 14px;
      }
      
      p {
        color: #606266;
        font-size: 13px;
        margin: 4px 0;
      }
      
      small {
        color: #7f8c8d;
        font-size: 12px;
      }
    }
  }
}

@media (max-width: 1200px) {
  .alert-cards {
    grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  }
}

@media (max-width: 768px) {
  .alert-center-container {
    padding: 12px;
  }
  
  .page-header .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: center;
    text-align: center;
  }
  
  .stats-overview .el-col {
    margin-bottom: 12px;
  }
  
  .alert-cards {
    grid-template-columns: 1fr;
  }
  
  .list-header {
    flex-direction: column;
    gap: 12px;
    align-items: flex-start;
  }
  
  .filter-content .el-row .el-col {
    margin-bottom: 12px;
  }
}
</style> 