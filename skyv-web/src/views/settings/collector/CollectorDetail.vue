<template>
  <div class="collector-detail">
    <div class="page-header">
      <h4>采集器详情</h4>
    </div>

    <div v-loading="loading">
      <!-- 基本信息卡片 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h5>基本信息</h5>
            <div class="header-actions">
              <el-button type="primary" size="small" @click="handleEdit">
                <el-icon><Edit /></el-icon> 编辑
              </el-button>
              <el-button size="small" @click="handleGoBack">
                <el-icon><Back /></el-icon> 返回
              </el-button>
            </div>
          </div>
        </template>

        <div class="basic-info">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">采集器名称</div>
                <div class="info-value">{{ collectorInfo.collectorName || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">采集器类型</div>
                <div class="info-value">
                  <el-tag effect="plain">{{ collectorInfo.collectorType || '-' }}</el-tag>
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">状态</div>
                <div class="info-value">
                  <el-tag :type="getStatusType(collectorInfo.status)" effect="dark">
                    <span class="status-indicator" :class="getStatusClass(collectorInfo.status)"></span>
                    {{ getStatusText(collectorInfo.status) }}
                  </el-tag>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">主机地址</div>
                <div class="info-value">{{ collectorInfo.host || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">端口</div>
                <div class="info-value">{{ collectorInfo.port || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">网络区域</div>
                <div class="info-value">{{ collectorInfo.networkZone || '-' }}</div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">最后心跳时间</div>
                <div class="info-value">{{ collectorInfo.lastHeartbeat || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">创建时间</div>
                <div class="info-value">{{ collectorInfo.createdAt || '-' }}</div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="info-item">
                <div class="info-label">是否主采集器</div>
                <div class="info-value">
                  <el-tag type="success" v-if="collectorInfo.isMain === 1">是</el-tag>
                  <el-tag type="info" v-else>否</el-tag>
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20">
            <el-col :span="24">
              <div class="info-item">
                <div class="info-label">描述</div>
                <div class="info-value description">{{ collectorInfo.description || '无描述' }}</div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 监控指标卡片 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h5>运行状态</h5>
            <div class="header-actions">
              <el-button size="small" @click="refreshMetrics">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
              <span class="last-refresh-time">最后更新: {{ lastRefreshTime }}</span>
            </div>
          </div>
        </template>

        <div class="metrics-container">
          <el-row :gutter="20">
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-icon cpu">
                  <el-icon><CPU /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-title">CPU 使用率</div>
                  <div class="metric-value">{{ collectorMetrics.cpuUsage || '0' }}%</div>
                  <el-progress :percentage="Number(collectorMetrics.cpuUsage || 0)" :color="getLoadColor(collectorMetrics.cpuUsage)" />
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-icon memory">
                  <el-icon><Coin /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-title">内存使用率</div>
                  <div class="metric-value">{{ collectorMetrics.memoryUsage || '0' }}%</div>
                  <el-progress :percentage="Number(collectorMetrics.memoryUsage || 0)" :color="getLoadColor(collectorMetrics.memoryUsage)" />
                </div>
              </div>
            </el-col>
            <el-col :span="8">
              <div class="metric-card">
                <div class="metric-icon disk">
                  <el-icon><DiskFile /></el-icon>
                </div>
                <div class="metric-content">
                  <div class="metric-title">磁盘使用率</div>
                  <div class="metric-value">{{ collectorMetrics.diskUsage || '0' }}%</div>
                  <el-progress :percentage="Number(collectorMetrics.diskUsage || 0)" :color="getLoadColor(collectorMetrics.diskUsage)" />
                </div>
              </div>
            </el-col>
          </el-row>

          <el-row :gutter="20" class="metrics-bottom-row">
            <el-col :span="12">
              <div class="metric-box">
                <div class="metric-item">
                  <span class="metric-label">运行时长:</span>
                  <span class="metric-data">{{ collectorMetrics.uptime || '-' }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">进程数:</span>
                  <span class="metric-data">{{ collectorMetrics.processes || '0' }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">线程数:</span>
                  <span class="metric-data">{{ collectorMetrics.threads || '0' }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">当前任务数:</span>
                  <span class="metric-data">{{ collectorMetrics.activeTasks || '0' }}</span>
                </div>
              </div>
            </el-col>
            <el-col :span="12">
              <div class="metric-box">
                <div class="metric-item">
                  <span class="metric-label">版本:</span>
                  <span class="metric-data">{{ collectorMetrics.version || '-' }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">网络 IO:</span>
                  <span class="metric-data">{{ collectorMetrics.networkIO || '-' }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">日志级别:</span>
                  <span class="metric-data">{{ collectorMetrics.logLevel || '-' }}</span>
                </div>
                <div class="metric-item">
                  <span class="metric-label">缓存使用:</span>
                  <span class="metric-data">{{ collectorMetrics.cacheUsage || '-' }}</span>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>

      <!-- 设备关联卡片 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h5>关联设备 ({{ assignedDevices.length }})</h5>
            <div class="header-actions">
              <el-button size="small" type="primary" @click="handleAssignDevices">
                <el-icon><Plus /></el-icon> 分配设备
              </el-button>
            </div>
          </div>
        </template>

        <div v-if="assignedDevices.length === 0" class="empty-devices">
          <el-empty description="暂无关联设备" />
        </div>
        <div v-else class="assigned-devices">
          <el-table :data="assignedDevices" border style="width: 100%">
            <el-table-column prop="deviceName" label="设备名称" min-width="150" show-overflow-tooltip />
            <el-table-column prop="ip" label="IP地址" width="140" />
            <el-table-column prop="deviceType" label="设备类型" width="120" align="center">
              <template #default="{ row }">
                <el-tag effect="light" size="small">{{ row.deviceType }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" align="center">
              <template #default="{ row }">
                <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="dark" size="small">
                  {{ row.status === 1 ? '在线' : '离线' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="assignedTime" label="分配时间" width="180" />
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button size="small" type="danger" @click="handleRemoveDevice(row)">
                  解除关联
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </el-card>
      
      <!-- 日志卡片 -->
      <el-card class="detail-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <h5>采集器日志</h5>
            <div class="header-actions">
              <el-radio-group v-model="logLevel" size="small" @change="fetchCollectorLogs">
                <el-radio-button label="all">全部</el-radio-button>
                <el-radio-button label="info">信息</el-radio-button>
                <el-radio-button label="warn">警告</el-radio-button>
                <el-radio-button label="error">错误</el-radio-button>
              </el-radio-group>
              <el-button size="small" @click="fetchCollectorLogs">
                <el-icon><Refresh /></el-icon> 刷新
              </el-button>
            </div>
          </div>
        </template>

        <div class="log-container" v-loading="logsLoading">
          <div v-if="collectorLogs.length === 0" class="empty-logs">
            <el-empty description="暂无日志记录" />
          </div>
          <div v-else class="log-list">
            <div v-for="(log, index) in collectorLogs" :key="index" :class="['log-item', `log-${log.level.toLowerCase()}`]">
              <span class="log-time">{{ log.timestamp }}</span>
              <span class="log-level">{{ log.level }}</span>
              <span class="log-message">{{ log.message }}</span>
            </div>
            
            <el-pagination
              v-if="totalLogs > 0"
              class="log-pagination"
              background
              layout="prev, pager, next"
              :page-size="10"
              :total="totalLogs"
              @current-change="handleLogPageChange"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 设备分配对话框 -->
    <el-dialog v-model="deviceDialog.visible" :title="deviceDialog.title" width="800px">
      <device-selector
        v-if="deviceDialog.visible"
        :collector-id="collectorId"
        @assigned="handleDeviceAssigned"
        @cancel="deviceDialog.visible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getCollectorById, 
  getCollectorMetrics
} from '@/api/collector'
import DeviceSelector from './components/DeviceSelector.vue'

const route = useRoute()
const router = useRouter()
const collectorId = computed(() => route.params.id)

// 状态变量
const loading = ref(false)
const logsLoading = ref(false)
const collectorInfo = ref({})
const collectorMetrics = ref({})
const assignedDevices = ref([])
const collectorLogs = ref([])
const totalLogs = ref(0)
const logLevel = ref('all')
const logPage = ref(1)
const lastRefreshTime = ref('-')

// 设备分配对话框数据
const deviceDialog = reactive({
  visible: false,
  title: '分配设备'
})

// 状态类型和样式映射
const getStatusType = (status) => {
  switch (status) {
    case 1: return 'success'
    case 0: return 'info'
    case 2: return 'danger'
    default: return 'info'
  }
}

const getStatusClass = (status) => {
  switch (status) {
    case 1: return 'online'
    case 0: return 'offline'
    case 2: return 'error'
    default: return 'offline'
  }
}

const getStatusText = (status) => {
  switch (status) {
    case 1: return '在线'
    case 0: return '离线'
    case 2: return '告警'
    default: return '未知'
  }
}

const getLoadColor = (load) => {
  const value = Number(load || 0)
  if (value < 50) return '#67C23A'
  if (value < 80) return '#E6A23C'
  return '#F56C6C'
}

// 获取采集器详情
const fetchCollectorDetail = async () => {
  loading.value = true
  try {
    const res = await getCollectorById(collectorId.value)
    if (res.code === 200 && res.data) {
      collectorInfo.value = res.data
    } else {
      ElMessage.error('获取采集器详情失败')
      router.push({ name: 'CollectorManagement' })
    }
  } catch (error) {
    console.error('获取采集器详情失败:', error)
    ElMessage.error('获取采集器详情失败')
    router.push({ name: 'CollectorManagement' })
  } finally {
    loading.value = false
  }
}

// 获取采集器监控指标
const fetchCollectorMetrics = async () => {
  try {
    const res = await getCollectorMetrics(collectorId.value)
    if (res.code === 200 && res.data) {
      collectorMetrics.value = res.data
      lastRefreshTime.value = new Date().toLocaleTimeString()
    }
  } catch (error) {
    console.error('获取采集器监控指标失败:', error)
  }
}

// 获取采集器日志
const fetchCollectorLogs = async () => {
  logsLoading.value = true
  try {
    const res = await getCollectorLogs({
      collectorId: collectorId.value,
      level: logLevel.value === 'all' ? '' : logLevel.value.toUpperCase(),
      page: logPage.value,
      limit: 10
    })
    if (res.code === 200) {
      collectorLogs.value = res.data || []
      totalLogs.value = res.meta?.total || 0
    } else {
      ElMessage.error('获取采集器日志失败')
    }
  } catch (error) {
    console.error('获取采集器日志失败:', error)
  } finally {
    logsLoading.value = false
  }
}

// 获取已分配设备
const fetchAssignedDevices = async () => {
  try {
    const res = await getAssignedDevices(collectorId.value)
    if (res.code === 200) {
      assignedDevices.value = res.data || []
    }
  } catch (error) {
    console.error('获取已分配设备失败:', error)
  }
}

// 刷新监控指标
const refreshMetrics = () => {
  fetchCollectorMetrics()
}

// 日志分页变化
const handleLogPageChange = (page) => {
  logPage.value = page
  fetchCollectorLogs()
}

// 编辑采集器
const handleEdit = () => {
  router.push({ 
    name: 'CollectorAdd', 
    query: { id: collectorId.value, edit: 'true' } 
  })
}

// 返回列表
const handleGoBack = () => {
  router.push({ name: 'CollectorManagement' })
}

// 分配设备
const handleAssignDevices = () => {
  deviceDialog.visible = true
  deviceDialog.title = `为采集器"${collectorInfo.value.collectorName}"分配设备`
}

// 设备分配完成后的回调
const handleDeviceAssigned = () => {
  ElMessage.success('设备分配成功')
  deviceDialog.visible = false
  fetchAssignedDevices()
}

// 解除设备关联
const handleRemoveDevice = (device) => {
  ElMessageBox.confirm(`确定要解除设备"${device.deviceName}"的关联吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await removeDeviceFromCollector({
        collectorId: collectorId.value,
        deviceId: device.id
      })
      if (res.code === 200) {
        ElMessage.success('解除关联成功')
        fetchAssignedDevices()
      } else {
        ElMessage.error(res.message || '解除关联失败')
      }
    } catch (error) {
      console.error('解除设备关联失败:', error)
      ElMessage.error('解除设备关联失败')
    }
  }).catch(() => {})
}

// 初始化
onMounted(async () => {
  if (!collectorId.value) {
    ElMessage.error('采集器ID不存在')
    router.push({ name: 'CollectorManagement' })
    return
  }
  
  await fetchCollectorDetail()
  fetchCollectorMetrics()
  fetchAssignedDevices()
  fetchCollectorLogs()

  // 定时刷新监控指标
  const metricsTimer = setInterval(() => {
    if (collectorInfo.value.status === 1) {
      fetchCollectorMetrics()
    }
  }, 30000) // 每30秒刷新一次

  // 组件销毁时清除定时器
  onUnmounted(() => {
    clearInterval(metricsTimer)
  })
})
</script>

<style scoped>
.collector-detail {
  padding: 10px;
}

.page-header {
  margin-bottom: 20px;
}

.detail-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h5 {
  margin: 0;
  font-size: 16px;
}

.header-actions {
  display: flex;
  align-items: center;
}

.last-refresh-time {
  margin-left: 10px;
  font-size: 12px;
  color: #909399;
}

.info-item {
  margin-bottom: 15px;
}

.info-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.info-value {
  font-size: 14px;
  color: #303133;
}

.info-value.description {
  white-space: pre-wrap;
  line-height: 1.5;
}

.status-indicator {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: 5px;
}

.status-indicator.online {
  background-color: #67C23A;
  box-shadow: 0 0 5px 1px #67C23A;
}

.status-indicator.offline {
  background-color: #909399;
}

.status-indicator.error {
  background-color: #F56C6C;
  animation: pulse 1.5s infinite;
}

/* 监控指标样式 */
.metrics-container {
  padding: 10px 0;
}

.metric-card {
  display: flex;
  align-items: center;
  padding: 15px;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  margin-bottom: 20px;
}

.metric-icon {
  width: 50px;
  height: 50px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.metric-icon.cpu {
  background-color: #409EFF;
}

.metric-icon.memory {
  background-color: #67C23A;
}

.metric-icon.disk {
  background-color: #E6A23C;
}

.metric-content {
  flex: 1;
}

.metric-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.metric-value {
  font-size: 18px;
  font-weight: bold;
  margin-bottom: 8px;
}

.metrics-bottom-row {
  margin-top: 10px;
}

.metric-box {
  border: 1px solid #EBEEF5;
  border-radius: 4px;
  padding: 15px;
  height: 100%;
}

.metric-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 10px;
}

.metric-item:last-child {
  margin-bottom: 0;
}

.metric-label {
  color: #909399;
}

.metric-data {
  font-weight: 500;
}

/* 设备列表样式 */
.empty-devices {
  padding: 30px 0;
}

.assigned-devices {
  margin-bottom: 10px;
}

/* 日志样式 */
.log-container {
  min-height: 200px;
  max-height: 400px;
  overflow-y: auto;
  border: 1px solid #EBEEF5;
  border-radius: 4px;
}

.empty-logs {
  padding: 50px 0;
}

.log-list {
  padding: 10px;
}

.log-item {
  padding: 8px 10px;
  border-bottom: 1px solid #EBEEF5;
  font-family: monospace;
  font-size: 12px;
  white-space: pre-wrap;
  word-break: break-all;
}

.log-item:last-child {
  border-bottom: none;
}

.log-item.log-error {
  background-color: rgba(245, 108, 108, 0.05);
  border-left: 3px solid #F56C6C;
}

.log-item.log-warn {
  background-color: rgba(230, 162, 60, 0.05);
  border-left: 3px solid #E6A23C;
}

.log-item.log-info {
  border-left: 3px solid #909399;
}

.log-time {
  color: #909399;
  margin-right: 10px;
}

.log-level {
  display: inline-block;
  min-width: 60px;
  font-weight: bold;
  margin-right: 10px;
}

.log-item.log-error .log-level {
  color: #F56C6C;
}

.log-item.log-warn .log-level {
  color: #E6A23C;
}

.log-item.log-info .log-level {
  color: #409EFF;
}

.log-message {
  color: #303133;
}

.log-pagination {
  margin-top: 15px;
  display: flex;
  justify-content: center;
}

@keyframes pulse {
  0% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.7);
  }
  70% {
    box-shadow: 0 0 0 5px rgba(245, 108, 108, 0);
  }
  100% {
    box-shadow: 0 0 0 0 rgba(245, 108, 108, 0);
  }
}
</style> 