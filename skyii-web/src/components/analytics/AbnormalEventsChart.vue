<template>
  <el-card class="chart-container h-full" shadow="hover">
    <div class="d-flex justify-between items-center mb-4">
      <div>
        <h5 class="mb-0">异常事件智能分析</h5>
        <small class="text-muted">
          <el-icon class="mr-1"><warning /></el-icon>
          自动风险评估与原因分析
        </small>
      </div>
      <div>
        <el-button size="small" type="danger" plain class="mr-2" @click="handleSetAlert">
          <el-icon class="mr-1"><bell /></el-icon>
          设置预警
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
    
    <!-- 风险统计 -->
    <div class="mt-3">
      <el-row :gutter="20">
        <el-col :span="12">
          <div class="risk-card high-risk mb-2" @click="handleViewDetails('high')">
            <div class="d-flex justify-between items-center">
              <div class="d-flex items-center">
                <div class="risk-icon">
                  <el-icon :size="20"><circle-plus /></el-icon>
                </div>
                <div class="ml-2">
                  <div class="risk-title">高风险异常</div>
                  <div class="risk-count">{{ abnormalData?.summary?.highRisk || 0 }}起</div>
                </div>
              </div>
              <el-button type="text" size="small">
                <el-icon><arrow-right /></el-icon>
              </el-button>
            </div>
          </div>
        </el-col>
        <el-col :span="12">
          <div class="risk-card medium-risk mb-2" @click="handleViewDetails('medium')">
            <div class="d-flex justify-between items-center">
              <div class="d-flex items-center">
                <div class="risk-icon">
                  <el-icon :size="20"><circle-plus /></el-icon>
                </div>
                <div class="ml-2">
                  <div class="risk-title">中风险异常</div>
                  <div class="risk-count">{{ abnormalData?.summary?.mediumRisk || 0 }}起</div>
                </div>
              </div>
              <el-button type="text" size="small">
                <el-icon><arrow-right /></el-icon>
              </el-button>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>
    
    <!-- 异常原因分析 -->
    <div class="mt-3" v-if="abnormalData?.analysis">
      <div class="section-title mb-2">
        <el-icon class="mr-1"><data-analysis /></el-icon>
        异常原因分析
      </div>
      <el-collapse v-model="activeReason">
        <el-collapse-item 
          v-for="(reason, index) in abnormalData.analysis.reasons" 
          :key="index"
          :title="reason.title"
          :name="index"
        >
          <div class="reason-content">
            <p>{{ reason.description }}</p>
            <div class="reason-stats d-flex mt-2">
              <div class="reason-stat">
                <div class="stat-label">关联事件</div>
                <div class="stat-value">{{ reason.eventCount }}</div>
              </div>
              <div class="reason-stat">
                <div class="stat-label">置信度</div>
                <div class="stat-value">{{ reason.confidence }}%</div>
              </div>
              <div class="reason-stat">
                <div class="stat-label">影响范围</div>
                <div class="stat-value">{{ reason.impact }}</div>
              </div>
            </div>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
    
    <!-- 处理建议 -->
    <div class="mt-3" v-if="abnormalData?.recommendations">
      <div class="section-title mb-2">
        <el-icon class="mr-1"><sunny /></el-icon>
        AI处理建议
      </div>
      <div class="recommendations-container">
        <div 
          v-for="(recommendation, index) in abnormalData.recommendations" 
          :key="index"
          class="recommendation-item"
        >
          <div class="recommendation-header d-flex items-center">
            <el-tag :type="getRecommendationType(recommendation.priority)" size="small" effect="dark" class="mr-2">
              {{ recommendation.priority }}
            </el-tag>
            <div class="recommendation-title">{{ recommendation.title }}</div>
          </div>
          <div class="recommendation-body mt-1">
            {{ recommendation.description }}
          </div>
          <div class="recommendation-actions mt-2">
            <el-button 
              size="small" 
              :type="recommendation.priority === '高' ? 'danger' : recommendation.priority === '中' ? 'warning' : 'info'"
              plain
              @click="handleRecommendationAction(recommendation)"
            >
              执行建议
            </el-button>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 异常详情对话框 -->
    <el-dialog
      v-model="detailsDialogVisible"
      :title="`${currentRiskLevel === 'high' ? '高' : '中'}风险异常详情`"
      width="700px"
    >
      <div v-if="selectedEvents.length > 0">
        <el-table :data="selectedEvents" style="width: 100%" max-height="400">
          <el-table-column prop="time" label="时间" width="180" />
          <el-table-column prop="location" label="位置" width="150" />
          <el-table-column prop="type" label="类型" width="120" />
          <el-table-column prop="description" label="描述" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="scope">
              <el-tag
                :type="scope.row.status === '已处理' ? 'success' : scope.row.status === '处理中' ? 'warning' : 'danger'"
                size="small"
              >
                {{ scope.row.status }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100" fixed="right">
            <template #default="scope">
              <el-button type="primary" link size="small" @click="handleViewEventDetails(scope.row)">
                查看
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <div class="mt-3 text-right">
          <el-button @click="detailsDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleExportEvents">导出列表</el-button>
        </div>
      </div>
      <div v-else class="text-center py-5">
        <el-empty description="暂无异常事件数据" />
      </div>
    </el-dialog>
    
    <!-- 事件详情对话框 -->
    <el-dialog
      v-model="eventDetailDialogVisible"
      title="异常事件详情"
      width="600px"
      append-to-body
    >
      <div v-if="selectedEvent">
        <div class="event-detail-header mb-3">
          <h3 class="mb-1">{{ selectedEvent.type }}</h3>
          <el-tag
            :type="selectedEvent.status === '已处理' ? 'success' : selectedEvent.status === '处理中' ? 'warning' : 'danger'"
            class="mr-2"
          >
            {{ selectedEvent.status }}
          </el-tag>
          <span class="text-muted">{{ selectedEvent.time }}</span>
        </div>
        
        <el-descriptions :column="1" border>
          <el-descriptions-item label="位置">{{ selectedEvent.location }}</el-descriptions-item>
          <el-descriptions-item label="描述">{{ selectedEvent.description }}</el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="selectedEvent.risk === '高' ? 'danger' : 'warning'">{{ selectedEvent.risk }}</el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="关联设备">{{ selectedEvent.device }}</el-descriptions-item>
          <el-descriptions-item label="处理人员">{{ selectedEvent.handler || '未分配' }}</el-descriptions-item>
          <el-descriptions-item label="处理记录">{{ selectedEvent.handlingRecord || '暂无记录' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="event-image mt-3" v-if="selectedEvent.image">
          <img :src="selectedEvent.image" alt="事件截图" style="max-width: 100%;" />
        </div>
        
        <div class="mt-3 text-right">
          <el-button @click="eventDetailDialogVisible = false">关闭</el-button>
          <el-button type="primary" @click="handleProcessEvent" :disabled="selectedEvent.status === '已处理'">
            {{ selectedEvent.status === '处理中' ? '完成处理' : '开始处理' }}
          </el-button>
        </div>
      </div>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Warning, 
  Bell, 
  ArrowDown, 
  CirclePlus,
  ArrowRight,
  DataAnalysis,
  Sunny
} from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { BarChart } from 'echarts/charts'
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
  CanvasRenderer
])

const analyticsStore = useAnalyticsStore()
const chartRef = ref(null)
const chart = ref(null)
const currentPeriod = ref('最近7天')
const abnormalData = computed(() => analyticsStore.abnormalEvents)
const activeReason = ref([0]) // 默认展开第一个原因
const detailsDialogVisible = ref(false)
const eventDetailDialogVisible = ref(false)
const currentRiskLevel = ref('high')
const selectedEvents = ref([])
const selectedEvent = ref(null)

// 监听数据变化
watch([abnormalData, currentPeriod], () => {
  if (abnormalData.value) {
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
        type: 'shadow'
      },
      formatter: function(params) {
        let result = params[0].axisValueLabel + '<br/>';
        params.forEach(param => {
          result += `<span style="display:inline-block;margin-right:5px;border-radius:50%;width:10px;height:10px;background-color:${param.color};"></span>`;
          result += `${param.seriesName}: ${param.value}起<br/>`;
        });
        return result;
      }
    },
    legend: {
      data: ['高风险', '中风险', '低风险'],
      icon: 'circle',
      itemWidth: 10,
      itemHeight: 10,
      textStyle: {
        fontSize: 12
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: abnormalData.value.xAxis || [],
      axisLine: {
        lineStyle: {
          color: '#ddd'
        }
      },
      axisLabel: {
        fontSize: 11
      }
    },
    yAxis: {
      type: 'value',
      name: '事件数',
      nameTextStyle: {
        fontSize: 12,
        padding: [0, 0, 0, 30]
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
    },
    series: [
      {
        name: '高风险',
        type: 'bar',
        stack: 'total',
        emphasis: {
          focus: 'series'
        },
        itemStyle: {
          color: '#f56c6c'
        },
        data: abnormalData.value.series?.[0]?.data || []
      },
      {
        name: '中风险',
        type: 'bar',
        stack: 'total',
        emphasis: {
          focus: 'series'
        },
        itemStyle: {
          color: '#e6a23c'
        },
        data: abnormalData.value.series?.[1]?.data || []
      },
      {
        name: '低风险',
        type: 'bar',
        stack: 'total',
        emphasis: {
          focus: 'series'
        },
        itemStyle: {
          color: '#67c23a'
        },
        data: abnormalData.value.series?.[2]?.data || []
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
  analyticsStore.fetchAbnormalEvents({
    period: period
  })
}

// 设置预警
const handleSetAlert = () => {
  ElMessageBox.prompt('请输入预警阈值', '设置异常事件预警', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPattern: /^\d+$/,
    inputErrorMessage: '请输入数字',
    inputPlaceholder: '例如：10'
  }).then(({ value }) => {
    analyticsStore.setAbnormalAlert({
      threshold: value,
      type: 'abnormal_event'
    }).then(res => {
      if (res.success) {
        ElMessage.success(res.message)
      } else {
        ElMessage.error(res.message)
      }
    })
  }).catch(() => {})
}

// 查看详情
const handleViewDetails = (level) => {
  currentRiskLevel.value = level
  detailsDialogVisible.value = true
  
  // 模拟获取异常事件列表
  // 实际项目中应该从API获取数据
  selectedEvents.value = [
    {
      id: 1,
      time: '2023-06-15 14:32:45',
      location: '前门监控区',
      type: '人员聚集',
      description: '检测到超过10人的异常聚集行为',
      status: '未处理',
      risk: level === 'high' ? '高' : '中',
      device: '前门摄像头-01',
      handler: '',
      handlingRecord: '',
      image: './src/assets/images/event-image-1.jpg'
    },
    {
      id: 2,
      time: '2023-06-15 15:18:22',
      location: '仓库区域',
      type: '未授权访问',
      description: '检测到未授权人员进入仓库区域',
      status: '处理中',
      risk: level === 'high' ? '高' : '中',
      device: '仓库摄像头-03',
      handler: '张工',
      handlingRecord: '已通知保安前往查看',
      image: './src/assets/images/event-image-2.jpg'
    },
    {
      id: 3,
      time: '2023-06-15 16:05:11',
      location: '办公区走廊',
      type: '异常行为',
      description: '检测到可疑行为模式',
      status: '已处理',
      risk: level === 'high' ? '高' : '中',
      device: '走廊摄像头-02',
      handler: '李工',
      handlingRecord: '确认为清洁人员，误报',
      image: './src/assets/images/event-image-3.jpg'
    }
  ].filter(item => item.risk === (level === 'high' ? '高' : '中'))
}

// 查看事件详情
const handleViewEventDetails = (event) => {
  selectedEvent.value = event
  eventDetailDialogVisible.value = true
}

// 处理事件
const handleProcessEvent = () => {
  if (!selectedEvent.value) return
  
  if (selectedEvent.value.status === '未处理') {
    selectedEvent.value.status = '处理中'
    selectedEvent.value.handler = '当前用户'
    ElMessage.success('已开始处理该事件')
  } else if (selectedEvent.value.status === '处理中') {
    selectedEvent.value.status = '已处理'
    selectedEvent.value.handlingRecord = '已确认并处理完成'
    ElMessage.success('事件已标记为处理完成')
  }
}

// 导出事件列表
const handleExportEvents = () => {
  ElMessage.success('正在导出事件列表...')
  setTimeout(() => {
    ElMessage.success('导出成功')
  }, 1500)
}

// 处理建议操作
const handleRecommendationAction = (recommendation) => {
  ElMessageBox.confirm(
    `是否执行建议: ${recommendation.title}?`,
    '执行处理建议',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: recommendation.priority === '高' ? 'warning' : 'info'
    }
  ).then(() => {
    ElMessage.success(`正在执行: ${recommendation.title}`)
    // 实际项目中可以调用API执行相应操作
  }).catch(() => {})
}

// 获取建议类型
const getRecommendationType = (priority) => {
  switch (priority) {
    case '高': return 'danger'
    case '中': return 'warning'
    default: return 'info'
  }
}

// 组件挂载时初始化
onMounted(() => {
  if (abnormalData.value) {
    initChart()
  } else {
    analyticsStore.fetchAbnormalEvents()
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

.mb-1 {
  margin-bottom: 0.25rem;
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

.mt-1 {
  margin-top: 0.25rem;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mr-1 {
  margin-right: 0.25rem;
}

.ml-2 {
  margin-left: 0.5rem;
}

.py-5 {
  padding-top: 3rem;
  padding-bottom: 3rem;
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

.text-right {
  text-align: right;
}

.text-sm {
  font-size: 0.875rem;
}

.section-title {
  font-weight: 600;
  font-size: 0.95rem;
  color: #303133;
  display: flex;
  align-items: center;
}

.risk-card {
  padding: 12px;
  border-radius: 6px;
  transition: all 0.3s;
  cursor: pointer;
}

.risk-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.high-risk {
  background-color: rgba(245, 108, 108, 0.1);
  border: 1px solid rgba(245, 108, 108, 0.2);
}

.medium-risk {
  background-color: rgba(230, 162, 60, 0.1);
  border: 1px solid rgba(230, 162, 60, 0.2);
}

.risk-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}

.high-risk .risk-icon {
  background-color: rgba(245, 108, 108, 0.2);
  color: #f56c6c;
}

.medium-risk .risk-icon {
  background-color: rgba(230, 162, 60, 0.2);
  color: #e6a23c;
}

.risk-title {
  font-size: 0.875rem;
  color: #606266;
}

.risk-count {
  font-size: 1.25rem;
  font-weight: 600;
}

.high-risk .risk-count {
  color: #f56c6c;
}

.medium-risk .risk-count {
  color: #e6a23c;
}

.reason-content {
  padding: 0.5rem 0;
}

.reason-stats {
  display: flex;
  gap: 20px;
}

.reason-stat {
  text-align: center;
  padding: 8px 16px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stat-label {
  font-size: 0.75rem;
  color: #909399;
}

.stat-value {
  font-size: 1rem;
  font-weight: 600;
  color: #303133;
}

.recommendations-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.recommendation-item {
  padding: 12px;
  border-radius: 6px;
  background-color: #f5f7fa;
  border: 1px solid #ebeef5;
}

.recommendation-title {
  font-weight: 600;
}

.recommendation-body {
  font-size: 0.875rem;
  color: #606266;
}

.event-detail-header {
  border-bottom: 1px solid #ebeef5;
  padding-bottom: 0.75rem;
}
</style> 