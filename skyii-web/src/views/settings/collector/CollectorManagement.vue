<template>
  <div class="collector-management">
    <!-- 页面标题 -->
    <div class="page-header">
      <h4>采集器管理</h4>
    </div>

    <!-- 状态统计卡片 -->
    <el-row :gutter="20">
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card primary">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.total || 0 }}</div>
            <div class="stat-label">总数量</div>
          </div>
          <div class="stat-icon">
            <el-icon :size="36"><Cpu /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card success">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.online || 0 }}</div>
            <div class="stat-label">在线</div>
          </div>
          <div class="stat-icon">
            <el-icon :size="36"><Check /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card warning">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.offline || 0 }}</div>
            <div class="stat-label">离线</div>
          </div>
          <div class="stat-icon">
            <el-icon :size="36"><Close /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card shadow="hover" class="stat-card danger">
          <div class="stat-content">
            <div class="stat-value">{{ statistics.error || 0 }}</div>
            <div class="stat-label">告警</div>
          </div>
          <div class="stat-icon">
            <el-icon :size="36"><WarningFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 操作与搜索栏 -->
    <el-card class="filter-container" shadow="never">
      <!-- 搜索表单 -->
      <el-form :inline="true" :model="queryParams" @submit.prevent>
        <el-form-item label="名称">
          <el-input v-model="queryParams.collectorName" placeholder="采集器名称" clearable @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="类型">
          <el-select v-model="queryParams.collectorType" placeholder="采集器类型" clearable>
            <el-option v-for="type in collectorTypes" :key="type.value" :label="type.label" :value="type.value" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable>
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="告警" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item label="网络区域">
          <el-select v-model="queryParams.networkZone" placeholder="网络区域" clearable>
            <el-option v-for="zone in networkZones" :key="zone" :label="zone" :value="zone" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon> 查询
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon> 重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <div class="button-group">
        <el-button type="primary" @click="handleNavigateToAdd">
          <el-icon><Plus /></el-icon> 新增采集器
        </el-button>
        <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button :disabled="selectedIds.length === 0" @click="handleBatchTest">
          <el-icon><Connection /></el-icon> 批量测试连接
        </el-button>
        <el-button @click="fetchData">
          <el-icon><RefreshRight /></el-icon> 刷新
        </el-button>
        <el-button type="primary" plain @click="showTopology">
          <el-icon><Share /></el-icon> 拓扑视图
        </el-button>
      </div>
    </el-card>

    <!-- 表格 -->
    <el-card class="table-container">
      <el-table
        v-loading="loading" 
        :data="collectorList" 
        border 
        @selection-change="handleSelectionChange"
        style="width: 100%">
        <el-table-column type="selection" width="55" />
        <el-table-column label="#" type="index" width="60" />
        <el-table-column prop="collectorName" label="名称" min-width="120" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark">
              <span class="status-indicator" :class="getStatusClass(row.status)"></span>
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collectorType" label="类型" width="100" align="center">
          <template #default="{ row }">
            <el-tag effect="plain">{{ row.collectorType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="version" label="版本" width="100" />
        <el-table-column prop="host" label="IP地址" width="150" />
        <el-table-column label="负载" width="150" align="center">
          <template #default="{ row }">
            <div v-if="row.status === 1">
              <el-progress :percentage="row.load || 0" :color="getLoadColor(row.load)" :stroke-width="10" />
              <span class="load-text">{{ row.load || 0 }}%</span>
            </div>
            <span v-else>--</span>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述" min-width="180" show-overflow-tooltip />
        <el-table-column prop="lastHeartbeat" label="最后心跳时间" width="180" />
        <el-table-column label="操作" fixed="right" width="240">
          <template #default="{ row }">
            <el-button size="small" @click="handleDetail(row)">查看详情</el-button>
            <el-button size="small" type="primary" @click="handleEdit(row)">编辑</el-button>
            <el-dropdown trigger="click" @command="(command) => handleCommand(command, row)">
              <el-button size="small" type="info">
                更多<el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item command="test">测试连接</el-dropdown-item>
                  <el-dropdown-item command="restart">重启采集器</el-dropdown-item>
                  <el-dropdown-item command="setPrimary" :disabled="row.isMain === 1">设为主采集器</el-dropdown-item>
                  <el-dropdown-item command="assign">分配设备</el-dropdown-item>
                  <el-dropdown-item command="delete" divided>删除</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          :current-page="queryParams.page"
          :page-size="queryParams.limit"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 分配设备对话框 -->
    <el-dialog v-model="deviceDialog.visible" :title="deviceDialog.title" width="800px">
      <device-selector
        v-if="deviceDialog.visible"
        :collector-id="deviceDialog.collectorId"
        @assigned="handleDeviceAssigned"
        @cancel="deviceDialog.visible = false"
      />
    </el-dialog>

    <!-- 测试连接结果对话框 -->
    <el-dialog v-model="testDialog.visible" title="测试连接结果" width="500px">
      <div v-if="testDialog.loading" class="test-loading">
        <el-icon class="is-loading"><Loading /></el-icon>
        <span>正在测试连接...</span>
      </div>
      <div v-else class="test-result">
        <div class="test-status" :class="{ 'success': testDialog.success, 'failed': !testDialog.success }">
          <el-icon v-if="testDialog.success"><CircleCheckFilled /></el-icon>
          <el-icon v-else><CircleCloseFilled /></el-icon>
          <span>{{ testDialog.success ? '连接成功' : '连接失败' }}</span>
        </div>
        <div class="test-message">{{ testDialog.message }}</div>
        <div v-if="testDialog.details" class="test-details">
          <pre>{{ testDialog.details }}</pre>
        </div>
      </div>
      <template #footer>
        <el-button @click="testDialog.visible = false">关闭</el-button>
        <el-button v-if="!testDialog.success" type="primary" @click="handleRetryTest">重试</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getCollectors,
  getCollectorStatusStatistics,
  getCollectorNetworkZones,
  deleteCollector,
  batchDeleteCollectors,
  testCollectorConnection,
  batchTestCollectorConnections,
  setPrimaryCollector,
  restartCollector
} from '@/api/collector'
import DeviceSelector from './components/DeviceSelector.vue'

// 路由
const router = useRouter()

// 数据定义
const loading = ref(false)
const collectorList = ref([])
const total = ref(0)
const selectedIds = ref([])
const statistics = reactive({
  total: 0,
  online: 0,
  offline: 0,
  error: 0
})

// 采集器类型列表
const collectorTypes = ref([
  { label: 'SNMP', value: 'snmp' },
  { label: 'HTTP/API', value: 'http' },
  { label: 'JMX', value: 'jmx' },
  { label: 'SSH', value: 'ssh' },
  { label: 'WMI', value: 'wmi' },
  { label: '自定义', value: 'custom' }
])

// 网络区域列表
const networkZones = ref([
  '主数据中心A区',
  '主数据中心B区',
  '备份数据中心',
  '边缘节点',
  '云端节点'
])

// 查询参数
const queryParams = reactive({
  page: 1,
  limit: 10,
  collectorName: '',
  collectorType: '',
  status: null,
  networkZone: ''
})

// 设备分配对话框数据
const deviceDialog = reactive({
  visible: false,
  title: '分配设备',
  collectorId: null
})

// 测试连接对话框数据
const testDialog = reactive({
  visible: false,
  loading: false,
  success: false,
  message: '',
  details: '',
  collectorId: null
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
  if (load < 50) return '#67C23A'
  if (load < 80) return '#E6A23C'
  return '#F56C6C'
}

// 选择行变化事件
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 查询列表数据
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getCollectors(queryParams)
    if (res && res.code === 200) {
      // 确保数据是数组类型
      collectorList.value = Array.isArray(res.data) ? res.data : []
      total.value = res.meta?.total || 0
    } else {
      ElMessage.error((res && res.message) || '获取采集器列表失败')
      collectorList.value = [] // 确保失败时也设置为空数组
    }
  } catch (error) {
    console.error('获取采集器列表出错:', error)
    ElMessage.error('获取采集器列表出错')
    collectorList.value = [] // 确保出错时也设置为空数组
  } finally {
    loading.value = false
  }
}

// 获取状态统计
const fetchStatistics = async () => {
  try {
    const res = await getCollectorStatusStatistics()
    if (res.code === 200) {
      Object.assign(statistics, res.data)
    }
  } catch (error) {
    console.error('获取统计信息失败:', error)
  }
}

// 获取网络区域
const fetchNetworkZones = async () => {
  try {
    const res = await getCollectorNetworkZones()
    if (res.code === 200) {
      networkZones.value = Array.isArray(res.data) ? res.data : []
    }
  } catch (error) {
    console.error('获取网络区域失败:', error)
  }
}

// 查询按钮点击事件
const handleQuery = () => {
  queryParams.page = 1
  fetchData()
}

// 重置查询表单
const resetQuery = () => {
  queryParams.collectorName = ''
  queryParams.collectorType = ''
  queryParams.status = null
  queryParams.networkZone = ''
  queryParams.page = 1
  fetchData()
}

// 分页事件
const handleSizeChange = (val) => {
  queryParams.limit = val
  fetchData()
}

const handleCurrentChange = (val) => {
  queryParams.page = val
  fetchData()
}

// 跳转到添加页面
const handleNavigateToAdd = () => {
  router.push({ name: 'CollectorAdd' })
}

// 查看详情
const handleDetail = (row) => {
  router.push({ name: 'CollectorDetail', params: { id: row.id } })
}

// 编辑采集器
const handleEdit = (row) => {
  router.push({ name: 'CollectorAdd', query: { id: row.id, edit: 'true' } })
}

// 删除采集器
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除采集器"${row.collectorName}"吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await deleteCollector(row.id)
      if (res.code === 200) {
        ElMessage.success('删除成功')
        fetchData()
        fetchStatistics()
      } else {
        ElMessage.error(res.message || '删除失败')
      }
    } catch (error) {
      console.error('删除采集器失败:', error)
      ElMessage.error('删除采集器失败')
    }
  }).catch(() => {})
}

// 批量删除
const handleBatchDelete = () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个采集器吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await batchDeleteCollectors(selectedIds.value)
      if (res.code === 200) {
        ElMessage.success('批量删除成功')
        fetchData()
        fetchStatistics()
      } else {
        ElMessage.error(res.message || '批量删除失败')
      }
    } catch (error) {
      console.error('批量删除采集器失败:', error)
      ElMessage.error('批量删除采集器失败')
    }
  }).catch(() => {})
}

// 设为主采集器
const handleSetPrimary = async (row) => {
  try {
    const res = await setPrimaryCollector(row.id)
    if (res.code === 200) {
      ElMessage.success(`成功将"${row.collectorName}"设置为主采集器`)
      fetchData()
    } else {
      ElMessage.error(res.message || '设置主采集器失败')
    }
  } catch (error) {
    console.error('设置主采集器失败:', error)
    ElMessage.error('设置主采集器失败')
  }
}

// 测试连接
const handleTest = async (row) => {
  testDialog.visible = true
  testDialog.loading = true
  testDialog.collectorId = row.id
  
  try {
    const res = await testCollectorConnection({ id: row.id })
    if (res.code === 200) {
      testDialog.success = true
      testDialog.message = '与采集器连接成功'
      testDialog.details = res.data?.details || '连接测试通过'
    } else {
      testDialog.success = false
      testDialog.message = res.message || '连接失败'
      testDialog.details = res.data?.details || '无详细信息'
    }
  } catch (error) {
    testDialog.success = false
    testDialog.message = '测试连接发生错误'
    testDialog.details = error.message || '未知错误'
  } finally {
    testDialog.loading = false
  }
}

// 重试测试连接
const handleRetryTest = () => {
  if (testDialog.collectorId) {
    handleTest({ id: testDialog.collectorId })
  }
}

// 批量测试连接
const handleBatchTest = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请至少选择一条记录')
    return
  }
  
  try {
    const res = await batchTestCollectorConnections(selectedIds.value)
    if (res.code === 200) {
      const successCount = res.data?.successCount || 0
      const failCount = res.data?.failCount || 0
      
      ElMessage.success(`测试完成: ${successCount}个成功, ${failCount}个失败`)
      fetchData() // 刷新数据
    } else {
      ElMessage.error(res.message || '批量测试失败')
    }
  } catch (error) {
    console.error('批量测试连接失败:', error)
    ElMessage.error('批量测试连接失败')
  }
}

// 重启采集器
const handleRestart = async (row) => {
  ElMessageBox.confirm(`确定要重启采集器"${row.collectorName}"吗?`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      const res = await restartCollector(row.id)
      if (res.code === 200) {
        ElMessage.success('重启命令已发送')
        setTimeout(() => {
          fetchData()
        }, 3000)
      } else {
        ElMessage.error(res.message || '重启失败')
      }
    } catch (error) {
      console.error('重启采集器失败:', error)
      ElMessage.error('重启采集器失败')
    }
  }).catch(() => {})
}

// 分配设备
const handleAssignDevices = (row) => {
  deviceDialog.visible = true
  deviceDialog.collectorId = row.id
  deviceDialog.title = `为采集器"${row.collectorName}"分配设备`
}

// 设备分配完成后的回调
const handleDeviceAssigned = () => {
  ElMessage.success('设备分配成功')
  deviceDialog.visible = false
}

// 查看拓扑图
const showTopology = () => {
  ElMessage.info('拓扑图功能正在开发中')
}

// 更多操作处理
const handleCommand = (command, row) => {
  switch (command) {
    case 'test':
      handleTest(row)
      break
    case 'restart':
      handleRestart(row)
      break
    case 'setPrimary':
      handleSetPrimary(row)
      break
    case 'assign':
      handleAssignDevices(row)
      break
    case 'delete':
      handleDelete(row)
      break
    default:
      break
  }
}

// 初始化
onMounted(() => {
  fetchData()
  fetchStatistics()
  fetchNetworkZones()
})

// 导出公共方法
defineExpose({
  fetchData
})
</script>

<style scoped>
.collector-management {
  padding: 10px;
}

.page-header {
  margin-bottom: 20px;
}

.stat-card {
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 20px;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.stat-icon {
  color: rgba(0, 0, 0, 0.15);
}

.stat-card.primary {
  border-left: 4px solid #409EFF;
}

.stat-card.success {
  border-left: 4px solid #67C23A;
}

.stat-card.warning {
  border-left: 4px solid #E6A23C;
}

.stat-card.danger {
  border-left: 4px solid #F56C6C;
}

.filter-container {
  margin-bottom: 20px;
}

.button-group {
  margin-top: 15px;
  display: flex;
  justify-content: flex-start;
}

.table-container {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
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

.load-text {
  font-size: 12px;
  color: #606266;
}

.test-loading {
  text-align: center;
  padding: 30px 0;
}

.test-loading .el-icon {
  font-size: 36px;
  margin-bottom: 10px;
}

.test-result {
  padding: 20px;
}

.test-status {
  display: flex;
  align-items: center;
  font-size: 18px;
  margin-bottom: 20px;
}

.test-status .el-icon {
  font-size: 24px;
  margin-right: 10px;
}

.test-status.success {
  color: #67C23A;
}

.test-status.failed {
  color: #F56C6C;
}

.test-message {
  margin-bottom: 10px;
  font-size: 16px;
}

.test-details {
  background-color: #f5f7fa;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
  padding: 10px;
  max-height: 200px;
  overflow-y: auto;
}

.test-details pre {
  margin: 0;
  font-family: monospace;
  white-space: pre-wrap;
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