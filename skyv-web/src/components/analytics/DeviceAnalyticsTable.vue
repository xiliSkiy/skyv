<template>
  <el-card class="chart-container mb-4" shadow="hover">
    <div class="d-flex justify-between items-center mb-4">
      <div>
        <h5 class="mb-0">设备分析明细</h5>
        <small class="text-muted">
          <el-icon class="mr-1"><video-camera /></el-icon>
          设备性能与使用分析
        </small>
      </div>
      <div>
        <el-input
          v-model="searchQuery"
          placeholder="搜索设备"
          class="search-input mr-2"
          clearable
          @clear="handleSearch"
          @input="handleSearch"
        >
          <template #prefix>
            <el-icon><search /></el-icon>
          </template>
        </el-input>
        <el-button type="primary" plain size="default" @click="handleExportTable">
          <el-icon class="mr-1"><download /></el-icon>
          导出
        </el-button>
      </div>
    </div>
    
    <el-table
      :data="filteredTableData"
      style="width: 100%"
      border
      stripe
      :header-cell-style="{ background: '#f5f7fa' }"
      v-loading="!deviceData"
    >
      <el-table-column prop="name" label="设备名称" min-width="120">
        <template #default="scope">
          <div class="d-flex items-center">
            <el-icon class="mr-1"><camera /></el-icon>
            <span>{{ scope.row.name }}</span>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="location" label="位置" min-width="120" />
      
      <el-table-column prop="traffic" label="人流量" min-width="100">
        <template #default="scope">
          <div>
            {{ scope.row.traffic }}
            <small :class="getTrendClass(scope.row.trafficTrend)">
              {{ formatTrend(scope.row.trafficTrend) }}
            </small>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="alerts" label="报警事件" min-width="100">
        <template #default="scope">
          <div class="d-flex items-center">
            <span>{{ scope.row.alerts }}</span>
            <el-tag 
              v-if="scope.row.alertLevel" 
              :type="getAlertLevelType(scope.row.alertLevel)" 
              size="small" 
              class="ml-1"
            >
              {{ scope.row.alertLevel }}
            </el-tag>
          </div>
        </template>
      </el-table-column>
      
      <el-table-column prop="peakTime" label="高峰时段" min-width="120" />
      
      <el-table-column prop="status" label="状态" min-width="100">
        <template #default="scope">
          <el-tag 
            :type="getStatusType(scope.row.status)" 
            effect="light"
          >
            {{ scope.row.status }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column prop="aiScore" label="AI评分" min-width="100">
        <template #default="scope">
          <el-progress 
            :percentage="scope.row.aiScore" 
            :color="getScoreColor(scope.row.aiScore)"
            :stroke-width="10"
            :show-text="false"
          />
          <span class="ml-1">{{ scope.row.aiScore }}</span>
          <el-tooltip 
            v-if="scope.row.aiWarning" 
            :content="scope.row.aiWarning" 
            placement="top"
          >
            <el-icon class="text-warning ml-1"><warning /></el-icon>
          </el-tooltip>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="150" fixed="right">
        <template #default="scope">
          <el-button type="primary" link size="small" @click="handleViewDetail(scope.row)">
            详情
          </el-button>
          <el-button type="success" link size="small" @click="handleAnalyzeDevice(scope.row)">
            分析
          </el-button>
          <el-button 
            v-if="scope.row.aiWarning" 
            type="warning" 
            link 
            size="small" 
            @click="handleCreateMaintenance(scope.row)"
          >
            维护
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <div class="pagination-container mt-3 d-flex justify-end">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="totalItems"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
  </el-card>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  VideoCamera, 
  Search, 
  Download, 
  Camera, 
  Warning 
} from '@element-plus/icons-vue'

const analyticsStore = useAnalyticsStore()
const deviceData = computed(() => analyticsStore.deviceAnalytics)
const searchQuery = ref('')
const currentPage = ref(1)
const pageSize = ref(5)
const totalItems = computed(() => deviceData.value?.total || 0)

// 过滤后的表格数据
const filteredTableData = computed(() => {
  if (!deviceData.value || !deviceData.value.data) return []
  
  let data = deviceData.value.data
  
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    data = data.filter(item => 
      item.name.toLowerCase().includes(query) || 
      item.location.toLowerCase().includes(query)
    )
  }
  
  return data
})

// 格式化趋势
const formatTrend = (trend) => {
  if (!trend) return ''
  const sign = trend > 0 ? '+' : ''
  return `${sign}${trend}%`
}

// 获取趋势样式
const getTrendClass = (trend) => {
  if (!trend) return ''
  return trend > 0 ? 'text-success' : 'text-danger'
}

// 获取报警级别类型
const getAlertLevelType = (level) => {
  switch (level) {
    case '高':
      return 'danger'
    case '中':
      return 'warning'
    case '低':
      return 'info'
    default:
      return 'info'
  }
}

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case '正常':
      return 'success'
    case '离线':
      return 'danger'
    case '信号弱':
      return 'warning'
    case '维护中':
      return 'info'
    default:
      return 'info'
  }
}

// 获取评分颜色
const getScoreColor = (score) => {
  if (score >= 90) return '#67c23a'
  if (score >= 75) return '#409eff'
  if (score >= 60) return '#e6a23c'
  return '#f56c6c'
}

// 处理页面大小变化
const handleSizeChange = (size) => {
  pageSize.value = size
  fetchDeviceData()
}

// 处理页码变化
const handleCurrentChange = (page) => {
  currentPage.value = page
  fetchDeviceData()
}

// 处理搜索
const handleSearch = () => {
  currentPage.value = 1
  fetchDeviceData()
}

// 获取设备数据
const fetchDeviceData = () => {
  analyticsStore.fetchDeviceAnalytics({
    page: currentPage.value,
    pageSize: pageSize.value,
    query: searchQuery.value
  })
}

// 导出表格
const handleExportTable = () => {
  ElMessage.success('正在导出设备分析明细...')
  
  // 实际项目中可以调用API导出表格数据
  analyticsStore.exportReport({
    type: 'device_analytics',
    format: 'excel'
  }).then(res => {
    if (res.success) {
      ElMessage.success(res.message)
    } else {
      ElMessage.error(res.message)
    }
  })
}

// 查看设备详情
const handleViewDetail = (device) => {
  ElMessage.info(`正在查看设备 ${device.name} 的详情`)
  // 实际项目中可以跳转到设备详情页面
}

// 分析设备
const handleAnalyzeDevice = (device) => {
  ElMessage.info(`正在分析设备 ${device.name} 的数据`)
  // 实际项目中可以跳转到设备分析页面或打开分析对话框
}

// 创建维护工单
const handleCreateMaintenance = (device) => {
  ElMessageBox.confirm(
    `是否为设备 ${device.name} 创建维护工单？`,
    '创建维护工单',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    analyticsStore.createMaintenanceOrder({
      deviceId: device.name,
      issue: device.aiWarning,
      priority: device.alertLevel || '中'
    }).then(res => {
      if (res.success) {
        ElMessage.success(res.message)
      } else {
        ElMessage.error(res.message)
      }
    })
  }).catch(() => {})
}

// 组件挂载时初始化
onMounted(() => {
  if (!deviceData.value) {
    fetchDeviceData()
  }
})
</script>

<style scoped>
.chart-container {
  margin-bottom: 1.5rem;
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mb-0 {
  margin-bottom: 0;
}

.mt-3 {
  margin-top: 0.75rem;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mr-1 {
  margin-right: 0.25rem;
}

.ml-1 {
  margin-left: 0.25rem;
}

.d-flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.justify-end {
  justify-content: flex-end;
}

.items-center {
  align-items: center;
}

.text-muted {
  color: var(--el-text-color-secondary);
}

.text-success {
  color: var(--el-color-success);
}

.text-danger {
  color: var(--el-color-danger);
}

.text-warning {
  color: var(--el-color-warning);
}

.search-input {
  width: 200px;
}

.pagination-container {
  padding-top: 15px;
}

h5 {
  font-size: 1.25rem;
  font-weight: 600;
}
</style> 