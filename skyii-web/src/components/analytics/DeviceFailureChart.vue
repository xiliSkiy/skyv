<template>
  <el-card class="chart-container h-full" shadow="hover">
    <div class="d-flex justify-between items-center mb-4">
      <div>
        <h5 class="mb-0">设备故障预测分析</h5>
        <small class="text-muted">
          <el-icon class="mr-1"><setting /></el-icon>
          AI预测准确率: {{ deviceData?.accuracy || 95 }}%
        </small>
      </div>
      <div class="d-flex">
        <el-button size="small" type="success" plain class="mr-2" @click="handleMaintenancePlan">
          <el-icon class="mr-1"><calendar /></el-icon>
          维护计划
        </el-button>
        <el-dropdown trigger="click" @command="handlePeriodChange">
          <el-button size="small" plain>
            {{ currentPeriod }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="今天">今天</el-dropdown-item>
              <el-dropdown-item command="昨天">昨天</el-dropdown-item>
              <el-dropdown-item command="最近7天">最近7天</el-dropdown-item>
              <el-dropdown-item command="最近30天">最近30天</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div ref="chartRef" style="height: 250px;"></div>
    
    <!-- 预测信息卡片 -->
    <div class="prediction-cards mt-3">
      <el-row :gutter="20">
        <el-col :md="12" :sm="24" :xs="24" class="mb-3">
          <div class="prediction-card" :class="{'high-risk': deviceData?.warning}">
            <div class="prediction-header">
              <div class="prediction-icon">
                <el-icon :size="24"><warning /></el-icon>
              </div>
              <div class="prediction-title">故障预警</div>
            </div>
            <div class="prediction-content">
              <template v-if="deviceData?.warning">
                <div class="prediction-text">
                  {{ deviceData.warning.device }}预计将在<span class="text-danger">{{ deviceData.warning.days }}天内</span>发生故障
                </div>
                <div class="prediction-detail mt-2">
                  <div><strong>故障组件:</strong> {{ deviceData.warning.component }}</div>
                  <div><strong>故障概率:</strong> {{ deviceData.warning.probability }}%</div>
                  <div><strong>预计影响:</strong> {{ deviceData.warning.impact }}</div>
                </div>
                <div class="mt-2">
                  <el-button size="small" type="warning" @click="handleCreateMaintenanceOrder">创建维护工单</el-button>
                </div>
              </template>
              <template v-else>
                <div class="prediction-text text-success">
                  <el-icon class="mr-1"><circle-check /></el-icon>
                  当前无设备故障预警
                </div>
                <div class="prediction-detail mt-2">
                  所有设备运行正常，无需紧急维护
                </div>
              </template>
            </div>
          </div>
        </el-col>
        <el-col :md="12" :sm="24" :xs="24" class="mb-3">
          <div class="prediction-card">
            <div class="prediction-header">
              <div class="prediction-icon blue-icon">
                <el-icon :size="24"><data-analysis /></el-icon>
              </div>
              <div class="prediction-title">健康评分</div>
            </div>
            <div class="prediction-content">
              <div class="health-score-container">
                <el-progress 
                  type="dashboard" 
                  :percentage="deviceData?.healthScore || 92" 
                  :color="getHealthScoreColor"
                  :stroke-width="8"
                >
                  <template #default="{ percentage }">
                    <div class="health-score-value">{{ percentage }}</div>
                    <div class="health-score-label">健康评分</div>
                  </template>
                </el-progress>
              </div>
              <div class="health-status mt-2 text-center">
                {{ getHealthStatus(deviceData?.healthScore || 92) }}
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 维护建议 -->
    <div class="mt-3">
      <div class="section-title mb-2">
        <el-icon class="mr-1"><tools /></el-icon>
        预防性维护建议
      </div>
      <el-table
        :data="maintenanceSuggestions"
        style="width: 100%"
        size="small"
        :header-cell-style="{ background: '#f5f7fa' }"
      >
        <el-table-column prop="device" label="设备" width="180" />
        <el-table-column prop="component" label="组件" width="150" />
        <el-table-column prop="action" label="建议操作" />
        <el-table-column prop="deadline" label="建议时间" width="100" />
        <el-table-column label="优先级" width="100" align="center">
          <template #default="scope">
            <el-tag
              :type="scope.row.priority === '高' ? 'danger' : scope.row.priority === '中' ? 'warning' : 'info'"
              size="small"
            >
              {{ scope.row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120" align="center">
          <template #default="scope">
            <el-button type="primary" link size="small" @click="handleCreateTaskForSuggestion(scope.row)">
              创建任务
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    
    <!-- 维护计划对话框 -->
    <el-dialog
      v-model="maintenancePlanVisible"
      title="设备维护计划"
      width="800px"
    >
      <el-calendar v-model="calendarValue">
        <template #dateCell="{ data }">
          <div class="calendar-cell">
            <div class="calendar-day">{{ data.day.split('-')[2] }}</div>
            <div v-if="hasMaintenanceTask(data)" class="calendar-tasks">
              <el-tag size="small" type="success">{{ getMaintenanceCount(data) }}个维护任务</el-tag>
            </div>
          </div>
        </template>
      </el-calendar>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="maintenancePlanVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleViewMaintenanceDetails">查看详情</el-button>
        </div>
      </template>
    </el-dialog>
    
    <!-- 创建工单对话框 -->
    <el-dialog
      v-model="createOrderVisible"
      title="创建维护工单"
      width="600px"
    >
      <el-form :model="maintenanceOrder" label-width="100px">
        <el-form-item label="设备">
          <el-input v-model="maintenanceOrder.device" disabled />
        </el-form-item>
        <el-form-item label="组件">
          <el-input v-model="maintenanceOrder.component" disabled />
        </el-form-item>
        <el-form-item label="维护类型">
          <el-select v-model="maintenanceOrder.type" class="w-full">
            <el-option label="预防性维护" value="preventive" />
            <el-option label="修复性维护" value="corrective" />
            <el-option label="紧急维修" value="emergency" />
          </el-select>
        </el-form-item>
        <el-form-item label="优先级">
          <el-radio-group v-model="maintenanceOrder.priority">
            <el-radio-button label="高">高</el-radio-button>
            <el-radio-button label="中">中</el-radio-button>
            <el-radio-button label="低">低</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="计划日期">
          <el-date-picker v-model="maintenanceOrder.scheduledDate" type="datetime" class="w-full" />
        </el-form-item>
        <el-form-item label="分配给">
          <el-select v-model="maintenanceOrder.assignee" class="w-full">
            <el-option label="张工" value="张工" />
            <el-option label="李工" value="李工" />
            <el-option label="王工" value="王工" />
            <el-option label="赵工" value="赵工" />
          </el-select>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="maintenanceOrder.notes" type="textarea" rows="3" />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="createOrderVisible = false">取消</el-button>
          <el-button type="primary" @click="submitMaintenanceOrder">提交</el-button>
        </div>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Setting, 
  ArrowDown, 
  Warning, 
  Calendar,
  DataAnalysis,
  CircleCheck,
  Tools
} from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必要的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  BarChart,
  LineChart,
  CanvasRenderer
])

const analyticsStore = useAnalyticsStore()
const chartRef = ref(null)
const chart = ref(null)
const currentPeriod = ref('最近7天')
const deviceData = computed(() => analyticsStore.deviceFailure)
const maintenancePlanVisible = ref(false)
const createOrderVisible = ref(false)
const calendarValue = ref(new Date())

// 维护工单表单
const maintenanceOrder = ref({
  device: '',
  component: '',
  type: 'preventive',
  priority: '高',
  scheduledDate: new Date(Date.now() + 24 * 60 * 60 * 1000), // 明天
  assignee: '张工',
  notes: ''
})

// 维护建议数据
const maintenanceSuggestions = ref([
  {
    device: '前门摄像头-01',
    component: '电源模块',
    action: '更换电源适配器',
    deadline: '7天内',
    priority: '中'
  },
  {
    device: '仓库摄像头-03',
    component: '存储卡',
    action: '更换SD卡',
    deadline: '30天内',
    priority: '低'
  },
  {
    device: '办公区监控主机',
    component: '散热风扇',
    action: '清理灰尘并润滑',
    deadline: '14天内',
    priority: '中'
  }
])

// 健康评分颜色
const getHealthScoreColor = computed(() => {
  const score = deviceData.value?.healthScore || 92
  if (score >= 90) return '#67c23a'
  if (score >= 70) return '#e6a23c'
  return '#f56c6c'
})

// 获取健康状态描述
const getHealthStatus = (score) => {
  if (score >= 90) return '状态优良'
  if (score >= 80) return '状态良好'
  if (score >= 70) return '需要关注'
  if (score >= 60) return '需要维护'
  return '需要紧急维修'
}

// 监听数据变化
watch([deviceData, currentPeriod], () => {
  if (deviceData.value) {
    initChart()
  }
}, { deep: true })

// 初始化图表
const initChart = () => {
  if (!chartRef.value) return
  
  if (!chart.value) {
    chart.value = echarts.init(chartRef.value)
    window.addEventListener('resize', () => chart.value.resize())
  }
  
  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        crossStyle: {
          color: '#999'
        }
      }
    },
    legend: {
      data: ['健康状态', '故障风险'],
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: [
      {
        type: 'category',
        data: deviceData.value.xAxis || [],
        axisPointer: {
          type: 'shadow'
        },
        axisLine: {
          lineStyle: {
            color: '#ddd'
          }
        },
        axisLabel: {
          fontSize: 11
        }
      }
    ],
    yAxis: [
      {
        type: 'value',
        name: '健康状态(%)',
        min: 0,
        max: 100,
        interval: 20,
        axisLine: {
          show: true,
          lineStyle: {
            color: '#ddd'
          }
        },
        splitLine: {
          lineStyle: {
            color: '#eee'
          }
        }
      },
      {
        type: 'value',
        name: '故障风险(%)',
        min: 0,
        max: 20,
        interval: 5,
        axisLabel: {
          formatter: '{value}%'
        },
        axisLine: {
          show: true,
          lineStyle: {
            color: '#ddd'
          }
        },
        splitLine: {
          lineStyle: {
            color: '#eee'
          }
        }
      }
    ],
    series: [
      {
        name: '健康状态',
        type: 'bar',
        data: deviceData.value.series?.[0]?.data || [],
        itemStyle: {
          color: '#67c23a'
        }
      },
      {
        name: '故障风险',
        type: 'line',
        yAxisIndex: 1,
        data: deviceData.value.series?.[1]?.data || [],
        itemStyle: {
          color: '#f56c6c'
        },
        lineStyle: {
          width: 3
        },
        symbol: 'circle',
        symbolSize: 8
      }
    ]
  }
  
  chart.value.setOption(option)
}

// 处理时间段变化
const handlePeriodChange = (period) => {
  currentPeriod.value = period
  ElMessage.success(`已切换到${period}数据`)
  
  // 实际项目中可以根据选择的时间段重新请求数据
  analyticsStore.fetchDeviceFailure({
    period: period
  })
}

// 创建维护工单
const handleCreateMaintenanceOrder = () => {
  if (!deviceData.value || !deviceData.value.warning) {
    ElMessage.warning('没有可用的预警信息')
    return
  }
  
  const { device, component } = deviceData.value.warning
  
  // 设置表单初始值
  maintenanceOrder.value = {
    device: device,
    component: component,
    type: 'preventive',
    priority: '高',
    scheduledDate: new Date(Date.now() + 24 * 60 * 60 * 1000), // 明天
    assignee: '张工',
    notes: `预防性维护，预计${deviceData.value.warning.days}天内可能发生故障。`
  }
  
  createOrderVisible.value = true
}

// 为建议创建任务
const handleCreateTaskForSuggestion = (suggestion) => {
  // 设置表单初始值
  maintenanceOrder.value = {
    device: suggestion.device,
    component: suggestion.component,
    type: 'preventive',
    priority: suggestion.priority,
    scheduledDate: new Date(Date.now() + 24 * 60 * 60 * 1000), // 明天
    assignee: '张工',
    notes: `预防性维护: ${suggestion.action}，建议在${suggestion.deadline}完成。`
  }
  
  createOrderVisible.value = true
}

// 提交维护工单
const submitMaintenanceOrder = () => {
  ElMessage.success('正在创建维护工单...')
  
  // 实际项目中可以调用API创建工单
  analyticsStore.createMaintenanceOrder({
    deviceId: maintenanceOrder.value.device,
    component: maintenanceOrder.value.component,
    type: maintenanceOrder.value.type,
    priority: maintenanceOrder.value.priority,
    scheduledDate: maintenanceOrder.value.scheduledDate,
    assignee: maintenanceOrder.value.assignee,
    notes: maintenanceOrder.value.notes
  }).then(res => {
    if (res.success) {
      ElMessage.success(res.message)
      createOrderVisible.value = false
    } else {
      ElMessage.error(res.message)
    }
  })
}

// 查看维护计划
const handleMaintenancePlan = () => {
  maintenancePlanVisible.value = true
}

// 查看维护详情
const handleViewMaintenanceDetails = () => {
  ElMessage.info('正在跳转到维护计划详情页面...')
  // 实际项目中可以跳转到维护计划详情页面
}

// 检查日期是否有维护任务
const hasMaintenanceTask = (date) => {
  // 模拟数据，实际项目中应该从API获取
  const maintenanceDates = ['2023-06-15', '2023-06-20', '2023-06-25', '2023-06-30']
  return maintenanceDates.includes(date.day)
}

// 获取维护任务数量
const getMaintenanceCount = (date) => {
  // 模拟数据，实际项目中应该从API获取
  const maintenanceCounts = {
    '2023-06-15': 2,
    '2023-06-20': 1,
    '2023-06-25': 3,
    '2023-06-30': 1
  }
  return maintenanceCounts[date.day] || 0
}

// 组件挂载时初始化
onMounted(() => {
  if (deviceData.value) {
    initChart()
  } else {
    analyticsStore.fetchDeviceFailure()
  }
})
</script>

<style scoped>
.chart-container {
  height: 100%;
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mb-3 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-0 {
  margin-bottom: 0;
}

.mt-3 {
  margin-top: 0.75rem;
}

.mt-2 {
  margin-top: 0.5rem;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mr-1 {
  margin-right: 0.25rem;
}

.d-flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.items-center {
  align-items: center;
}

.text-muted {
  color: #909399;
}

.text-center {
  text-align: center;
}

.text-success {
  color: #67c23a;
}

.text-danger {
  color: #f56c6c;
  font-weight: bold;
}

.w-full {
  width: 100%;
}

.section-title {
  font-weight: 600;
  font-size: 0.95rem;
  color: #303133;
  display: flex;
  align-items: center;
}

.prediction-cards {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
}

.prediction-card {
  border-radius: 8px;
  padding: 16px;
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
  height: 100%;
}

.prediction-card.high-risk {
  background-color: rgba(245, 108, 108, 0.05);
  border: 1px solid rgba(245, 108, 108, 0.2);
}

.prediction-header {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.prediction-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.prediction-icon.blue-icon {
  background-color: rgba(64, 158, 255, 0.1);
  color: #409eff;
}

.prediction-title {
  font-weight: 600;
  font-size: 1rem;
}

.prediction-text {
  font-size: 0.95rem;
  line-height: 1.5;
}

.prediction-detail {
  font-size: 0.85rem;
  color: #606266;
  line-height: 1.6;
}

.health-score-container {
  display: flex;
  justify-content: center;
  margin-top: 8px;
}

.health-score-value {
  font-size: 1.5rem;
  font-weight: bold;
  color: #303133;
}

.health-score-label {
  font-size: 0.75rem;
  color: #909399;
}

.health-status {
  font-weight: 600;
  color: #67c23a;
}

.calendar-cell {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.calendar-day {
  text-align: right;
  padding: 4px;
}

.calendar-tasks {
  flex: 1;
  display: flex;
  justify-content: center;
  align-items: center;
}
</style> 