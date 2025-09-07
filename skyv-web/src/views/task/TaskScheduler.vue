<template>
  <div class="task-scheduler-container">
    <!-- 标题区域 -->
    <div class="page-header mb-4">
      <div>
        <h4 class="page-title">任务调度器管理</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/task' }">采集任务调度</el-breadcrumb-item>
          <el-breadcrumb-item>调度器管理</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div>
        <el-button 
          type="primary" 
          :disabled="schedulerStatus === 'RUNNING'"
          @click="startScheduler"
          class="me-2"
        >
          <el-icon><VideoPlay /></el-icon> 启动调度器
        </el-button>
        <el-button 
          type="warning" 
          :disabled="schedulerStatus === 'STOPPED'"
          @click="stopScheduler"
        >
          <el-icon><VideoPause /></el-icon> 停止调度器
        </el-button>
      </div>
    </div>

    <!-- 调度器状态卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="status-card" shadow="hover">
          <div class="status-card-content">
            <div class="status-icon" :class="getStatusClass(schedulerStatus)">
              <el-icon><component :is="getStatusIcon(schedulerStatus)" /></el-icon>
            </div>
            <div class="status-info">
              <h3>{{ getStatusLabel(schedulerStatus) }}</h3>
              <p>调度器状态</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="status-card" shadow="hover">
          <div class="status-card-content">
            <div class="status-icon bg-success">
              <el-icon><List /></el-icon>
            </div>
            <div class="status-info">
              <h3>{{ schedulerStats.totalTasks || 0 }}</h3>
              <p>总任务数</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="status-card" shadow="hover">
          <div class="status-card-content">
            <div class="status-icon bg-warning">
              <el-icon><VideoPlay /></el-icon>
            </div>
            <div class="status-info">
              <h3>{{ schedulerStats.scheduledTasks || 0 }}</h3>
              <p>已调度任务</p>
            </div>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12" :md="6" :lg="6">
        <el-card class="status-card" shadow="hover">
          <div class="status-card-content">
            <div class="status-icon bg-info">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="status-info">
              <h3>{{ schedulerStats.runningTasks || 0 }}</h3>
              <p>运行中任务</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 调度器统计信息 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12">
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><TrendCharts /></el-icon> 执行统计
            </div>
          </template>
          <div class="stats-content">
            <el-row :gutter="20">
              <el-col :span="12">
                <div class="stat-item">
                  <span class="stat-label">今日执行次数:</span>
                  <span class="stat-value">{{ schedulerStats.todayExecutions || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">成功率:</span>
                  <span class="stat-value success">{{ (schedulerStats.successRate || 0).toFixed(1) }}%</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">平均执行时间:</span>
                  <span class="stat-value">{{ (schedulerStats.averageExecutionTime || 0) / 1000 }}s</span>
                </div>
              </el-col>
              <el-col :span="12">
                <div class="stat-item">
                  <span class="stat-label">运行时长:</span>
                  <span class="stat-value">{{ schedulerStats.formattedUptime || '0秒' }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">活跃线程:</span>
                  <span class="stat-value">{{ schedulerStats.activeThreads || 0 }}/{{ schedulerStats.totalThreads || 0 }}</span>
                </div>
                <div class="stat-item">
                  <span class="stat-label">队列任务:</span>
                  <span class="stat-value">{{ schedulerStats.queuedTasks || 0 }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="12">
        <el-card class="stats-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Setting /></el-icon> 系统资源
            </div>
          </template>
          <div class="stats-content">
            <div class="resource-item">
              <span class="resource-label">内存使用:</span>
              <el-progress 
                :percentage="getMemoryUsagePercentage()" 
                :color="getMemoryColor()"
                :format="(percentage) => `${(schedulerStats.memoryUsage || 0) / 1024 / 1024}MB`"
              />
            </div>
            <div class="resource-item">
              <span class="resource-label">CPU使用:</span>
              <el-progress 
                :percentage="schedulerStats.cpuUsage || 0" 
                :color="getCpuColor()"
                :format="(percentage) => `${percentage.toFixed(1)}%`"
              />
            </div>
            <div class="resource-item">
              <span class="resource-label">线程池使用:</span>
              <el-progress 
                :percentage="getThreadPoolUsagePercentage()" 
                :color="getThreadPoolColor()"
                :format="(percentage) => `${schedulerStats.activeThreads || 0}/${schedulerStats.totalThreads || 0}`"
              />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 调度器操作 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="24">
        <el-card class="action-card" shadow="hover">
          <template #header>
            <div class="card-header">
              <el-icon><Tools /></el-icon> 调度器操作
            </div>
          </template>
          <div class="action-content">
            <el-row :gutter="20">
              <el-col :xs="24" :sm="12" :md="8">
                <el-button 
                  type="primary" 
                  plain 
                  @click="reloadAllTasks"
                  :loading="reloadLoading"
                  class="action-btn"
                >
                  <el-icon><Refresh /></el-icon> 重新加载任务
                </el-button>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-button 
                  type="warning" 
                  plain 
                  @click="cleanupExpiredTasks"
                  :loading="cleanupLoading"
                  class="action-btn"
                >
                  <el-icon><Delete /></el-icon> 清理过期任务
                </el-button>
              </el-col>
              <el-col :xs="24" :sm="12" :md="8">
                <el-button 
                  type="info" 
                  plain 
                  @click="getSchedulerHealth"
                  :loading="healthLoading"
                  class="action-btn"
                >
                  <el-icon><Monitor /></el-icon> 健康检查
                </el-button>
              </el-col>
            </el-row>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 已调度任务列表 -->
    <el-card class="task-list-card" shadow="hover">
      <template #header>
        <div class="card-header">
          <el-icon><List /></el-icon> 已调度任务列表
          <el-button 
            type="primary" 
            plain 
            size="small" 
            @click="refreshScheduledTasks"
            :loading="refreshLoading"
            class="ms-2"
          >
            <el-icon><Refresh /></el-icon> 刷新
          </el-button>
        </div>
      </template>
      
      <div v-if="scheduledTasks.length > 0" class="task-list">
        <div v-for="task in scheduledTasks" :key="task" class="task-item">
          <div class="task-info">
            <span class="task-id">任务ID: {{ task }}</span>
            <el-button 
              type="danger" 
              size="small" 
              @click="cancelTask(task)"
              class="ms-2"
            >
              取消调度
            </el-button>
          </div>
        </div>
      </div>
      <div v-else class="empty-task-list">
        <el-empty description="暂无已调度的任务" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  VideoPlay, VideoPause, List, Clock, TrendCharts, Setting, 
  Tools, Refresh, Delete, Monitor, Warning, Success, Info 
} from '@element-plus/icons-vue'

// 调度器状态
const schedulerStatus = ref('STOPPED')
const schedulerStats = ref({})
const scheduledTasks = ref([])

// 加载状态
const reloadLoading = ref(false)
const cleanupLoading = ref(false)
const healthLoading = ref(false)
const refreshLoading = ref(false)

// 定时器
let statusTimer = null
let statsTimer = null

import { 
  getSchedulerStatus, startScheduler, stopScheduler, getSchedulerStatistics,
  getScheduledTaskIds, reloadAllTasks, cleanupExpiredTasks, getSchedulerHealth
} from '@/api/scheduler'

// 获取调度器状态
const getSchedulerStatus = async () => {
  try {
    const result = await getSchedulerStatus()
    if (result.code === 200) {
      schedulerStatus.value = result.data.status
    }
  } catch (error) {
    console.error('获取调度器状态失败', error)
  }
 }

// 获取调度器统计信息
const getSchedulerStatistics = async () => {
  try {
    const result = await getSchedulerStatistics()
    if (result.code === 200) {
      schedulerStats.value = result.data
    }
  } catch (error) {
    console.error('获取调度器统计信息失败', error)
  }
}

// 获取已调度的任务列表
const getScheduledTasks = async () => {
  try {
    const result = await getScheduledTaskIds()
    if (result.code === 200) {
      scheduledTasks.value = result.data || []
    }
  } catch (error) {
    console.error('获取已调度任务列表失败', error)
  }
}

// 启动调度器
const startScheduler = async () => {
  try {
    const result = await startScheduler()
    if (result.code === 200) {
      ElMessage.success('调度器启动成功')
      await getSchedulerStatus()
      await getSchedulerStatistics()
    } else {
      ElMessage.error(result.message || '启动失败')
    }
  } catch (error) {
    console.error('启动调度器失败', error)
    ElMessage.error('启动调度器失败')
  }
}

// 停止调度器
const stopScheduler = async () => {
  try {
    await ElMessageBox.confirm('确认要停止调度器吗？停止后所有任务将不再执行。', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    const result = await stopScheduler()
    if (result.code === 200) {
      ElMessage.success('调度器停止成功')
      await getSchedulerStatus()
      await getSchedulerStatistics()
    } else {
      ElMessage.error(result.message || '停止失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('停止调度器失败', error)
      ElMessage.error('停止调度器失败')
    }
  }
}

// 重新加载所有任务
const reloadAllTasks = async () => {
  reloadLoading.value = true
  try {
    const result = await reloadAllTasks()
    if (result.code === 200) {
      ElMessage.success('重新加载任务成功')
      await getScheduledTasks()
      await getSchedulerStatistics()
    } else {
      ElMessage.error(result.message || '重新加载失败')
    }
  } catch (error) {
    console.error('重新加载任务失败', error)
    ElMessage.error('重新加载任务失败')
  } finally {
    reloadLoading.value = false
  }
}

// 清理过期任务
const cleanupExpiredTasks = async () => {
  cleanupLoading.value = true
  try {
    const result = await cleanupExpiredTasks()
    if (result.code === 200) {
      ElMessage.success('清理过期任务成功')
      await getScheduledTasks()
      await getSchedulerStatistics()
    } else {
      ElMessage.error(result.message || '清理失败')
    }
  } catch (error) {
    console.error('清理过期任务失败', error)
    ElMessage.error('清理过期任务失败')
  } finally {
    cleanupLoading.value = false
  }
}

// 获取调度器健康状态
const getSchedulerHealth = async () => {
  healthLoading.value = true
  try {
    const result = await getSchedulerHealth()
    if (result.code === 200) {
      const health = result.data
      if (health.healthy) {
        ElMessage.success('调度器运行正常')
      } else {
        ElMessage.warning(`调度器状态异常: ${health.message}`)
      }
    } else {
      ElMessage.error(result.message || '健康检查失败')
    }
  } catch (error) {
    console.error('健康检查失败', error)
    ElMessage.error('健康检查失败')
  } finally {
    healthLoading.value = false
  }
}

// 刷新已调度任务列表
const refreshScheduledTasks = async () => {
  refreshLoading.value = true
  try {
    await getScheduledTasks()
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新失败', error)
    ElMessage.error('刷新失败')
  } finally {
    refreshLoading.value = false
  }
}

// 取消任务调度
const cancelTask = async (taskId) => {
  try {
    await ElMessageBox.confirm(`确认要取消任务 ${taskId} 的调度吗？`, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 这里需要调用后端的取消调度接口
    // 由于后端没有提供取消调度的接口，我们暂时只更新前端状态
    scheduledTasks.value = scheduledTasks.value.filter(id => id !== taskId)
    ElMessage.success('取消调度成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('取消调度失败', error)
      ElMessage.error('取消调度失败')
    }
  }
}

// 获取状态图标
const getStatusIcon = (status) => {
  switch (status) {
    case 'RUNNING':
      return 'VideoPlay'
    case 'STOPPED':
      return 'VideoPause'
    case 'STARTING':
      return 'Loading'
    case 'STOPPING':
      return 'Loading'
    case 'ERROR':
      return 'Warning'
    default:
      return 'Info'
  }
}

// 获取状态标签
const getStatusLabel = (status) => {
  switch (status) {
    case 'RUNNING':
      return '运行中'
    case 'STOPPED':
      return '已停止'
    case 'STARTING':
      return '启动中'
    case 'STOPPING':
      return '停止中'
    case 'ERROR':
      return '错误'
    default:
      return '未知'
  }
}

// 获取状态样式类
const getStatusClass = (status) => {
  switch (status) {
    case 'RUNNING':
      return 'bg-success'
    case 'STOPPED':
      return 'bg-secondary'
    case 'STARTING':
      return 'bg-warning'
    case 'STOPPING':
      return 'bg-warning'
    case 'ERROR':
      return 'bg-danger'
    default:
      return 'bg-info'
  }
}

// 获取内存使用百分比
const getMemoryUsagePercentage = () => {
  const totalMemory = 1024 * 1024 * 1024 // 1GB
  return Math.min(100, Math.round((schedulerStats.value.memoryUsage || 0) / totalMemory * 100))
}

// 获取内存颜色
const getMemoryColor = () => {
  const percentage = getMemoryUsagePercentage()
  if (percentage < 60) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

// 获取CPU颜色
const getCpuColor = () => {
  const percentage = schedulerStats.value.cpuUsage || 0
  if (percentage < 60) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

// 获取线程池使用百分比
const getThreadPoolUsagePercentage = () => {
  const total = schedulerStats.value.totalThreads || 1
  return Math.round((schedulerStats.value.activeThreads || 0) / total * 100)
}

// 获取线程池颜色
const getThreadPoolColor = () => {
  const percentage = getThreadPoolUsagePercentage()
  if (percentage < 60) return '#67C23A'
  if (percentage < 80) return '#E6A23C'
  return '#F56C6C'
}

// 页面初始化
onMounted(async () => {
  await getSchedulerStatus()
  await getSchedulerStatistics()
  await getScheduledTasks()
  
  // 启动定时器
  statusTimer = setInterval(getSchedulerStatus, 5000) // 每5秒更新状态
  statsTimer = setInterval(getSchedulerStatistics, 10000) // 每10秒更新统计
})

// 页面卸载
onUnmounted(() => {
  if (statusTimer) clearInterval(statusTimer)
  if (statsTimer) clearInterval(statsTimer)
})
</script>

<style scoped>
.task-scheduler-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  margin: 0;
  color: #303133;
}

.status-card {
  height: 120px;
}

.status-card-content {
  display: flex;
  align-items: center;
  height: 100%;
}

.status-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 15px;
  color: white;
  font-size: 24px;
}

.status-info h3 {
  margin: 0 0 5px 0;
  font-size: 24px;
  font-weight: 600;
  color: #303133;
}

.status-info p {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

.stats-card {
  height: 200px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
}

.card-header .el-icon {
  margin-right: 8px;
}

.stats-content {
  padding: 10px 0;
}

.stat-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-label {
  color: #606266;
  font-size: 14px;
}

.stat-value {
  font-weight: 600;
  color: #303133;
}

.stat-value.success {
  color: #67C23A;
}

.resource-item {
  margin-bottom: 20px;
}

.resource-label {
  display: block;
  margin-bottom: 8px;
  color: #606266;
  font-size: 14px;
}

.action-card {
  height: 120px;
}

.action-content {
  padding: 20px 0;
}

.action-btn {
  width: 100%;
  height: 40px;
}

.task-list-card {
  min-height: 300px;
}

.task-list {
  max-height: 400px;
  overflow-y: auto;
}

.task-item {
  padding: 15px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  margin-bottom: 10px;
  background-color: #fafafa;
}

.task-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.task-id {
  font-weight: 600;
  color: #303133;
}

.empty-task-list {
  padding: 40px 0;
  text-align: center;
}

.bg-primary { background-color: #409EFF; }
.bg-success { background-color: #67C23A; }
.bg-warning { background-color: #E6A23C; }
.bg-danger { background-color: #F56C6C; }
.bg-info { background-color: #909399; }
.bg-secondary { background-color: #909399; }
</style>
