<template>
  <div class="task-list-container">
    <!-- 标题区域 -->
    <div class="page-header mb-4">
      <div>
        <h4 class="page-title">采集任务调度</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item>采集任务调度</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div>
        <el-button type="primary" class="create-btn" @click="goToCreateTask">
          <el-icon><Plus /></el-icon> 创建任务
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="12" :sm="6" :md="6" :lg="6" v-for="(stat, index) in taskStats" :key="index">
        <el-card class="stat-card" shadow="hover">
          <div class="stat-card-content">
            <div class="stat-icon" :class="stat.bgClass">
              <el-icon><component :is="stat.icon" /></el-icon>
            </div>
            <div class="stat-info">
              <h3>{{ stat.count }}</h3>
              <p>{{ stat.label }}</p>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 任务筛选 -->
    <el-card class="filter-card mb-4">
      <template #header>
        <div class="card-header">
          <el-icon><Filter /></el-icon> 任务筛选
        </div>
      </template>
      <el-form :model="queryParams" label-width="100px" size="default">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="任务类型">
              <el-select v-model="queryParams.taskType" placeholder="全部类型" clearable style="width: 100%">
                <el-option label="全部类型" value="" />
                <el-option label="实时采集" value="realtime" />
                <el-option label="定时采集" value="scheduled" />
                <el-option label="周期采集" value="periodic" />
                <el-option label="触发式采集" value="triggered" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="任务状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="全部状态" value="" />
                <el-option label="运行中" value="running" />
                <el-option label="已调度" value="scheduled" />
                <el-option label="已暂停" value="paused" />
                <el-option label="已完成" value="completed" />
                <el-option label="执行失败" value="failed" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="优先级">
              <el-select v-model="queryParams.priority" placeholder="全部优先级" clearable style="width: 100%">
                <el-option label="全部优先级" value="" />
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="8" :lg="6">
            <el-form-item label="任务标签">
              <el-select v-model="queryParams.tag" placeholder="全部标签" clearable style="width: 100%">
                <el-option label="全部标签" value="" />
                <el-option label="安全监控" value="security" />
                <el-option label="数据分析" value="analysis" />
                <el-option label="系统维护" value="maintenance" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="创建日期">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="12" :lg="12">
            <el-form-item label="搜索">
              <el-input
                v-model="queryParams.keyword"
                placeholder="任务名称/ID/负责人"
                clearable
                style="width: 100%"
              >
                <template #append>
                  <el-button :icon="Search" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>

        <div class="filter-actions">
          <el-button @click="resetQuery" class="reset-btn">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
          <el-button type="primary" @click="handleQuery" class="apply-btn">
            <el-icon><Filter /></el-icon> 应用筛选
          </el-button>
        </div>
      </el-form>
    </el-card>

    <!-- 任务列表 -->
    <el-card class="task-list-card">
      <template #header>
        <div class="list-header">
          <div class="list-title">
            <el-icon><List /></el-icon> 任务列表
          </div>
          <div class="list-actions">
            <el-button-group>
              <el-button :icon="Download" size="small">导出</el-button>
              <el-button :icon="Refresh" size="small" @click="fetchTaskList">刷新</el-button>
            </el-button-group>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="loading-container">
        <div v-if="!loading" class="task-container">
          <div class="list-summary">
            <div class="summary-counts">
              <span class="total-count">共 <strong>{{ total }}</strong> 个任务</span>
              <span class="selected-count">选中 <strong>{{ selectedTasks.length }}</strong> 个任务</span>
            </div>
            <div>
              <el-dropdown @command="handleBatchCommand" class="batch-actions">
                <el-button type="primary" plain size="small">
                  批量操作 <el-icon class="el-icon--right"><arrow-down /></el-icon>
                </el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item command="start">
                      <el-icon><VideoPlay /></el-icon> 启动
                    </el-dropdown-item>
                    <el-dropdown-item command="pause">
                      <el-icon><VideoPause /></el-icon> 暂停
                    </el-dropdown-item>
                    <el-dropdown-item command="stop">
                      <el-icon><CircleClose /></el-icon> 停止
                    </el-dropdown-item>
                    <el-dropdown-item divided command="delete">
                      <el-icon><Delete /></el-icon> 删除
                    </el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </div>
          </div>

          <!-- 任务卡片列表表头 -->
          <div class="task-list-header">
            <div class="table-header-row">
              <div class="table-cell name-cell">
                <div class="column-label">任务名称/类型</div>
              </div>
              <div class="table-cell status-cell">
                <div class="column-label">状态/时间</div>
              </div>
              <div class="table-cell priority-cell">
                <div class="column-label">优先级/设备</div>
              </div>
              <div class="table-cell action-cell">
                <div class="column-label">操作</div>
              </div>
            </div>
          </div>

          <!-- 任务卡片列表 -->
          <div class="task-list" v-if="taskList.length > 0">
            <div v-for="task in taskList" :key="task.id" class="task-card" :class="{'task-card-running': task.status === 'running', 'task-card-paused': task.status === 'paused', 'task-card-failed': task.status === 'failed'}">
              <div class="task-card-content">
                <div class="table-row">
                  <div class="table-cell name-cell">
                    <div class="task-info-section">
                      <el-checkbox v-model="task.selected" @change="updateSelection" class="task-checkbox"></el-checkbox>
                      <div class="task-main-info">
                        <div class="task-name">{{ task.taskName || task.name || '未命名任务' }}</div>
                        <div class="task-meta">
                          <span class="task-id">ID: {{ task.id }}</span>
                          <el-tag size="small" :type="getTaskTypeTag(task.taskType || task.type)" effect="plain" class="task-type-tag">
                            {{ getTaskTypeLabel(task.taskType || task.type) }}
                          </el-tag>
                        </div>
                      </div>
                    </div>
                  </div>
                  <div class="table-cell status-cell">
                    <div class="task-status-section">
                      <div class="status-badge">
                        <el-tag :type="getStatusType(task.status)" size="small" effect="dark" class="status-tag">
                          {{ getStatusLabel(task.status) }}
                        </el-tag>
                      </div>
                      <div class="task-time">
                        <el-icon><Clock /></el-icon>
                        <span>{{ (task.startTime || task.createTime) ? formatDate(task.startTime || task.createTime) : formatSchedule(task) }}</span>
                      </div>
                    </div>
                  </div>
                  <div class="table-cell priority-cell">
                    <div class="task-priority-section">
                      <div class="priority-indicator">
                        <span class="task-priority-dot" :class="'priority-' + (task.priority || task.priorityLevel || 'medium')"></span>
                        <span class="priority-text">{{ getPriorityLabel(task.priority || task.priorityLevel || 'medium') }}</span>
                      </div>
                      <div class="device-count">
                        <el-icon><VideoCamera /></el-icon>
                        <span>{{ task.deviceCount || (task.devices ? task.devices.length : 0) }}台设备</span>
                      </div>
                    </div>
                  </div>
                  <div class="table-cell action-cell">
                    <div class="task-actions">
                      <el-dropdown trigger="click" @command="(command) => handleCommand(command, task)">
                        <el-button type="primary" plain size="small" class="action-btn">
                          操作 <el-icon class="el-icon--right"><arrow-down /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item command="view">
                              <el-icon><View /></el-icon> 查看详情
                            </el-dropdown-item>
                            <el-dropdown-item v-if="task.status === 'paused'" command="start">
                              <el-icon><VideoPlay /></el-icon> 继续
                            </el-dropdown-item>
                            <el-dropdown-item v-if="task.status === 'running'" command="pause">
                              <el-icon><VideoPause /></el-icon> 暂停
                            </el-dropdown-item>
                            <el-dropdown-item v-if="task.status === 'running'" command="stop">
                              <el-icon><CircleClose /></el-icon> 停止
                            </el-dropdown-item>
                            <el-dropdown-item command="edit">
                              <el-icon><Edit /></el-icon> 编辑
                            </el-dropdown-item>
                            <el-dropdown-item divided command="delete">
                              <el-icon><Delete /></el-icon> 删除
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 进度条 -->
              <div v-if="task.status === 'running'" class="progress-container">
                <div class="progress">
                  <div 
                    class="progress-bar" 
                    :class="getProgressBarClass(task)" 
                    role="progressbar" 
                    :style="{ width: getTaskProgress(task) + '%' }" 
                    :aria-valuenow="getTaskProgress(task)" 
                    aria-valuemin="0" 
                    aria-valuemax="100">
                  </div>
                </div>
              </div>
            </div>
          </div>
          
          <div v-else class="empty-task-list">
            <el-empty description="暂无任务数据" />
          </div>

          <!-- 分页控件 -->
          <div class="pagination-container">
            <div class="pagination-info">
              显示 {{ (queryParams.pageNum - 1) * queryParams.pageSize + 1 }} 至 
              {{ Math.min(queryParams.pageNum * queryParams.pageSize, total) }}，共 {{ total }} 个任务
            </div>
            <el-pagination
              v-model:current-page="queryParams.pageNum"
              v-model:page-size="queryParams.pageSize"
              :page-sizes="[5, 10, 20, 50]"
              layout="sizes, prev, pager, next"
              :total="total"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              background
            />
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Filter, Search, Refresh, Download, List, Clock, VideoCamera, 
  VideoPlay, VideoPause, CircleClose, Delete, View, Edit, ArrowDown, InfoFilled } from '@element-plus/icons-vue'
import { getTaskList, getTaskStats, startTask, pauseTask, stopTask, deleteTask, batchTaskAction } from '@/api/task'

const router = useRouter()

// 数据定义
const loading = ref(false)
const taskList = ref([])
const total = ref(0)
const selectedTasks = ref([])
const dateRange = ref([])

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 10,
  taskType: '',
  status: '',
  priority: '',
  tag: '',
  keyword: '',
  startDate: '',
  endDate: ''
})

// 任务统计数据
const taskStats = ref([
  { label: '总任务数', count: 0, icon: 'List', bgClass: 'bg-primary' },
  { label: '运行中任务', count: 0, icon: 'VideoPlay', bgClass: 'bg-success' },
  { label: '已调度任务', count: 0, icon: 'Calendar', bgClass: 'bg-warning' },
  { label: '异常任务', count: 0, icon: 'Warning', bgClass: 'bg-danger' }
])

// 监听日期范围变化
const watchDateRange = computed(() => {
  if (dateRange.value && dateRange.value.length === 2) {
    queryParams.startDate = formatDate(dateRange.value[0], 'YYYY-MM-DD')
    queryParams.endDate = formatDate(dateRange.value[1], 'YYYY-MM-DD')
  } else {
    queryParams.startDate = ''
    queryParams.endDate = ''
  }
  return dateRange.value
})

// 获取任务列表
const fetchTaskList = async () => {
  loading.value = true
  try {
    const res = await getTaskList(queryParams)
    console.log('任务列表响应数据:', res)
    
    // 检查返回的数据结构
    if (res.data) {
      // 处理不同的数据结构可能性
      if (Array.isArray(res.data)) {
        // 如果直接返回数组
        taskList.value = res.data
        total.value = res.data.length
      } else if (res.data.content && Array.isArray(res.data.content)) {
        // Spring Data JPA 分页格式
        taskList.value = res.data.content
        total.value = res.data.totalElements || res.data.content.length
      } else if (res.data.list && Array.isArray(res.data.list)) {
        // 自定义分页格式
        taskList.value = res.data.list
        total.value = res.data.total || res.data.list.length
      } else if (res.data.records && Array.isArray(res.data.records)) {
        // MyBatis-Plus 分页格式
        taskList.value = res.data.records
        total.value = res.data.total || res.data.records.length
      } else {
        console.error('未知的数据结构:', res.data)
        taskList.value = []
        total.value = 0
      }
    } else {
      console.error('API返回数据格式错误:', res)
      taskList.value = []
      total.value = 0
    }
    
    console.log('处理后的任务列表:', taskList.value)
    console.log('总数:', total.value)
  } catch (error) {
    console.error('获取任务列表失败', error)
    ElMessage.error('获取任务列表失败')
    taskList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取任务统计数据
const fetchTaskStats = async () => {
  try {
    const res = await getTaskStats()
    if (res.data) {
      taskStats.value[0].count = res.data.total || 0
      taskStats.value[1].count = res.data.running || 0
      taskStats.value[2].count = res.data.scheduled || 0
      taskStats.value[3].count = res.data.failed || 0
    }
  } catch (error) {
    console.error('获取任务统计数据失败', error)
  }
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  fetchTaskList()
}

// 重置查询
const resetQuery = () => {
  queryParams.taskType = ''
  queryParams.status = ''
  queryParams.priority = ''
  queryParams.tag = ''
  queryParams.keyword = ''
  dateRange.value = []
  queryParams.startDate = ''
  queryParams.endDate = ''
  handleQuery()
}

// 处理页面大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  fetchTaskList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  fetchTaskList()
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedTasks.value = selection
}

// 处理单个任务操作
const handleCommand = async (command, row) => {
  console.log('执行操作:', command, '任务ID:', row.id);
  
  switch (command) {
    case 'view':
      router.push(`/task/detail/${row.id}`).catch(err => {
        console.error('导航到任务详情页失败:', err);
        ElMessage.error('导航失败，请稍后再试');
      });
      break;
    case 'start':
      await handleStartTask(row.id);
      break;
    case 'pause':
      await handlePauseTask(row.id);
      break;
    case 'stop':
      await handleStopTask(row.id);
      break;
    case 'edit':
      router.push(`/task/edit/${row.id}`).catch(err => {
        console.error('导航到任务编辑页失败:', err);
        ElMessage.error('导航失败，请稍后再试');
      });
      break;
    case 'delete':
      await handleDeleteTask(row.id);
      break;
    default:
      break;
  }
}

// 处理批量操作
const handleBatchCommand = async (command) => {
  if (selectedTasks.value.length === 0) {
    ElMessage.warning('请至少选择一个任务')
    return
  }

  const taskIds = selectedTasks.value.map(task => task.id)
  let confirmMessage = ''
  let successMessage = ''

  switch (command) {
    case 'start':
      confirmMessage = '确认要启动选中的任务吗？'
      successMessage = '批量启动任务成功'
      break
    case 'pause':
      confirmMessage = '确认要暂停选中的任务吗？'
      successMessage = '批量暂停任务成功'
      break
    case 'stop':
      confirmMessage = '确认要停止选中的任务吗？'
      successMessage = '批量停止任务成功'
      break
    case 'delete':
      confirmMessage = '确认要删除选中的任务吗？此操作不可恢复！'
      successMessage = '批量删除任务成功'
      break
    default:
      return
  }

  try {
    await ElMessageBox.confirm(confirmMessage, '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    await batchTaskAction(command, taskIds)
    ElMessage.success(successMessage)
    fetchTaskList()
    fetchTaskStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error(`批量${command}任务失败`, error)
      ElMessage.error(`批量${command}任务失败`)
    }
  }
}

// 启动任务
const handleStartTask = async (id) => {
  try {
    await startTask(id)
    ElMessage.success('启动任务成功')
    fetchTaskList()
    fetchTaskStats()
  } catch (error) {
    console.error('启动任务失败', error)
    ElMessage.error('启动任务失败')
  }
}

// 暂停任务
const handlePauseTask = async (id) => {
  try {
    await pauseTask(id)
    ElMessage.success('暂停任务成功')
    fetchTaskList()
    fetchTaskStats()
  } catch (error) {
    console.error('暂停任务失败', error)
    ElMessage.error('暂停任务失败')
  }
}

// 停止任务
const handleStopTask = async (id) => {
  try {
    await stopTask(id)
    ElMessage.success('停止任务成功')
    fetchTaskList()
    fetchTaskStats()
  } catch (error) {
    console.error('停止任务失败', error)
    ElMessage.error('停止任务失败')
  }
}

// 删除任务
const handleDeleteTask = async (id) => {
  try {
    await ElMessageBox.confirm('确认要删除该任务吗？此操作不可恢复！', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await deleteTask(id)
    ElMessage.success('删除任务成功')
    fetchTaskList()
    fetchTaskStats()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除任务失败', error)
      ElMessage.error('删除任务失败')
    }
  }
}

// 跳转到创建任务页面
const goToCreateTask = () => {
  router.push('/task/create')
}

// 格式化日期
const formatDate = (date, format = 'YYYY-MM-DD HH:mm') => {
  if (!date) return '未设置时间';
  
  // 检查是否为有效日期
  const d = new Date(date);
  if (isNaN(d.getTime())) {
    return '无效日期';
  }
  
  const year = d.getFullYear();
  const month = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  const hours = String(d.getHours()).padStart(2, '0');
  const minutes = String(d.getMinutes()).padStart(2, '0');
  
  if (format === 'YYYY-MM-DD') {
    return `${year}-${month}-${day}`;
  }
  return `${year}-${month}-${day} ${hours}:${minutes}`;
}

// 格式化调度信息
const formatSchedule = (task) => {
  const taskType = task.taskType || task.type || '';
  
  if (taskType === 'scheduled') {
    return task.cronExpression || task.scheduleTime ? `每天 ${task.scheduleTime || '00:00'} 执行` : '定时执行';
  } else if (taskType === 'periodic') {
    const interval = task.interval || task.intervalValue || '';
    const unit = task.intervalUnit || '分钟';
    return interval ? `每${interval}${unit}执行一次` : '周期执行';
  } else if (taskType === 'triggered') {
    return task.triggerEvent ? `${task.triggerEvent}触发执行` : '触发式执行';
  } else if (taskType === 'realtime') {
    return '实时执行';
  }
  return '';
}

// 获取任务类型标签类型
const getTaskTypeTag = (type) => {
  if (!type) return '';
  const map = {
    realtime: 'info',
    scheduled: 'primary',
    periodic: 'success',
    triggered: 'warning'
  }
  return map[type] || ''
}

// 获取任务类型标签文本
const getTaskTypeLabel = (type) => {
  if (!type) return '未知类型';
  const map = {
    realtime: '实时采集',
    scheduled: '定时采集',
    periodic: '周期采集',
    triggered: '触发式采集'
  }
  return map[type] || type
}

// 获取状态类型
const getStatusType = (status) => {
  if (!status) return '';
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
  if (!status) return '未知状态';
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
  if (!priority) return '中优先级';
  const map = {
    high: '高优先级',
    medium: '中优先级',
    low: '低优先级'
  }
  return map[priority] || priority
}

// 获取任务进度
const getTaskProgress = (task) => {
  // 如果任务有进度字段，则直接使用
  if (task.progress !== undefined) {
    return task.progress;
  }
  
  // 如果没有进度字段，根据任务类型生成模拟进度
  if (task.status === 'running') {
    // 使用任务ID的最后两位数字作为随机进度值
    const idStr = String(task.id);
    const lastTwoDigits = idStr.substring(idStr.length - 2);
    return Math.min(Math.max(parseInt(lastTwoDigits, 10) || 65, 10), 95);
  }
  
  return 0;
}

// 获取进度条样式类
const getProgressBarClass = (task) => {
  const progress = getTaskProgress(task);
  
  if (progress < 30) {
    return 'bg-danger';
  } else if (progress < 70) {
    return 'bg-warning';
  } else {
    return 'bg-success';
  }
}

// 更新选中的任务
const updateSelection = () => {
  selectedTasks.value = taskList.value.filter(task => task.selected);
}

// 页面初始化
onMounted(() => {
  console.log('组件挂载，开始获取数据')
  fetchTaskList()
  fetchTaskStats()
  
  // 模拟一些运行中的任务，用于测试
  setTimeout(() => {
    if (taskList.value && taskList.value.length > 0) {
      // 将前两个任务设置为运行中状态，并添加有效的时间
      if (taskList.value[0]) {
        taskList.value[0].status = 'running';
        taskList.value[0].startTime = new Date().toISOString();
      }
      if (taskList.value.length > 1 && taskList.value[1]) {
        taskList.value[1].status = 'running';
        taskList.value[1].startTime = new Date().toISOString();
      }
    }
  }, 1000);
})
</script>

<style scoped>
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 1.5rem;
  font-weight: 600;
  margin-bottom: 0.5rem;
  color: #2c3e50;
}

.create-btn {
  border-radius: 6px;
  padding: 10px 20px;
  font-weight: 500;
  transition: all 0.3s;
}

.create-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 统计卡片样式 */
.stat-card {
  position: relative;
  overflow: hidden;
  border-radius: 10px;
  transition: all 0.3s;
  height: 100%;
}

.stat-card-content {
  display: flex;
  align-items: center;
  padding: 5px;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.12);
}

.stat-icon {
  display: inline-flex;
  width: 60px;
  height: 60px;
  border-radius: 12px;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  margin-right: 15px;
  color: white;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.1);
}

.stat-info {
  flex: 1;
}

.bg-primary {
  background: linear-gradient(135deg, #1a73e8, #2b5876);
}

.bg-success {
  background: linear-gradient(135deg, #28a745, #20c997);
}

.bg-warning {
  background: linear-gradient(135deg, #ffc107, #ff9800);
}

.bg-danger {
  background: linear-gradient(135deg, #dc3545, #ff4b2b);
}

.stat-card h3 {
  font-size: 28px;
  margin-bottom: 5px;
  font-weight: 700;
  color: #2c3e50;
}

.stat-card p {
  margin-bottom: 0;
  color: #6c757d;
  font-size: 14px;
}

/* 筛选卡片样式 */
.filter-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  margin-bottom: 20px;
}

.filter-actions {
  display: flex;
  justify-content: flex-end;
  padding-top: 10px;
  gap: 10px;
}

.reset-btn, .apply-btn {
  padding: 8px 16px;
  border-radius: 6px;
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
}

.card-header .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

/* 任务列表卡片样式 */
.task-list-card {
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  color: #2c3e50;
  font-size: 16px;
}

.list-title .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.list-summary {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
  padding: 0 10px;
}

.summary-counts {
  display: flex;
  align-items: center;
}

.total-count {
  margin-right: 20px;
  color: #2c3e50;
}

.selected-count {
  color: #606266;
}

.batch-actions .el-button {
  border-radius: 6px;
}

.task-list-header {
  padding: 12px 15px;
  border-bottom: 1px solid #ebeef5;
  background-color: #f9fafc;
  border-radius: 6px 6px 0 0;
  margin-bottom: 15px;
}

.table-header-row, .table-row {
  display: flex;
  width: 100%;
  align-items: center;
}

.table-cell {
  padding: 0 10px;
  position: relative;
}

.table-cell:not(:last-child)::after {
  content: '';
  position: absolute;
  right: 0;
  top: 15%;
  height: 70%;
  width: 1px;
  background-color: #ebeef5;
}

.name-cell {
  flex: 5;
  min-width: 200px;
}

.status-cell {
  flex: 3;
  min-width: 150px;
}

.priority-cell {
  flex: 2;
  min-width: 120px;
}

.action-cell {
  flex: 2;
  min-width: 100px;
  display: flex;
  justify-content: flex-end;
}

.column-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
}

/* 任务卡片样式 */
.task-card {
  padding: 0;
  border-radius: 10px;
  margin-bottom: 16px;
  transition: all 0.2s ease;
  background-color: #fff;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  border-left: 4px solid #e9ecef;
  overflow: hidden;
  position: relative;
}

.task-card-running {
  border-left-color: #28a745;
}

.task-card-paused {
  border-left-color: #ffc107;
}

.task-card-failed {
  border-left-color: #dc3545;
}

.task-card-content {
  padding: 16px;
}

.task-card:hover {
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

.task-info-section {
  display: flex;
  align-items: flex-start;
}

.task-checkbox {
  margin-right: 12px;
  margin-top: 2px;
}

.task-main-info {
  flex: 1;
}

.task-name {
  font-weight: 600;
  font-size: 15px;
  color: #2c3e50;
  margin-bottom: 6px;
  display: -webkit-box;
  -webkit-line-clamp: 1;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.task-meta {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 8px;
}

.task-id {
  color: #909399;
  font-size: 13px;
}

.task-type-tag {
  font-size: 12px;
}

.task-status-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.status-badge {
  margin-bottom: 4px;
}

.status-tag {
  padding: 2px 8px;
  border-radius: 4px;
  font-weight: 500;
}

.task-time {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 13px;
  gap: 4px;
}

.task-priority-section {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.priority-indicator {
  display: flex;
  align-items: center;
}

.task-priority-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 6px;
}

.priority-high {
  background-color: #f56c6c;
}

.priority-medium {
  background-color: #e6a23c;
}

.priority-low {
  background-color: #909399;
}

.priority-text {
  font-size: 13px;
  color: #606266;
}

.device-count {
  display: flex;
  align-items: center;
  color: #909399;
  font-size: 13px;
  gap: 4px;
}

.task-actions {
  display: flex;
  justify-content: flex-end;
}

.action-btn {
  border-radius: 6px;
  transition: all 0.2s;
}

.action-btn:hover {
  transform: translateY(-2px);
}

/* 进度条样式 */
.progress-container {
  padding: 0 16px 16px;
  margin-top: 0;
}

.progress {
  height: 8px;
  border-radius: 4px;
  background-color: #e9ecef;
  overflow: hidden;
  margin: 0;
}

.progress-bar {
  height: 100%;
  border-radius: 4px;
  transition: width 0.8s ease;
}

.bg-success {
  background-color: #28a745 !important;
}

.bg-warning {
  background-color: #ffc107 !important;
}

.bg-danger {
  background-color: #dc3545 !important;
}

/* 空状态和加载样式 */
.empty-task-list {
  padding: 40px 0;
  text-align: center;
  background-color: #fafafa;
  border-radius: 8px;
}

.loading-container {
  min-height: 200px;
  position: relative;
}

/* 分页样式 */
.pagination-container {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 15px;
}

.pagination-info {
  color: #909399;
  font-size: 13px;
}

@media (max-width: 768px) {
  .table-header-row, .table-row {
    flex-direction: column;
  }
  
  .table-cell {
    width: 100%;
    padding: 8px 0;
  }
  
  .table-cell:not(:last-child)::after {
    display: none;
  }
  
  .table-cell:not(:last-child) {
    border-bottom: 1px dashed #ebeef5;
  }
  
  .name-cell, .status-cell, .priority-cell, .action-cell {
    min-width: 100%;
  }
  
  .action-cell {
    justify-content: flex-start;
  }
}
</style> 