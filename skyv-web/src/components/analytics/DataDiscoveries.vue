<template>
  <el-card class="chart-container mb-4" shadow="hover">
    <div class="d-flex justify-between items-center mb-4">
      <div>
        <h5 class="mb-0">智能数据探索</h5>
        <small class="text-muted">
          <el-icon class="mr-1"><search /></el-icon>
          AI自动发现数据关联
        </small>
      </div>
      <el-button type="primary" plain size="small" @click="handleRefresh">
        <el-icon class="mr-1"><refresh /></el-icon>
        刷新分析
      </el-button>
    </div>
    
    <div v-if="!discoveryData || discoveryData.discoveries.length === 0" class="empty-state">
      <el-empty description="暂无数据探索结果">
        <el-button type="primary" @click="handleRefresh">开始探索</el-button>
      </el-empty>
    </div>
    
    <el-tabs v-else v-model="activeTab" type="card">
      <el-tab-pane 
        v-for="(discovery, index) in discoveryData.discoveries" 
        :key="index" 
        :label="discovery.title" 
        :name="String(index)"
      >
        <div class="discovery-card">
          <div class="d-flex justify-between items-center mb-3">
            <div>
              <h6 class="mb-1">{{ discovery.title }}</h6>
              <div class="confidence-badge">
                <el-icon class="mr-1"><document /></el-icon>
                置信度: {{ discovery.confidence }}%
              </div>
            </div>
            <el-button type="text" @click="handleExportDiscovery(discovery)">
              <el-icon class="mr-1"><download /></el-icon>
              导出
            </el-button>
          </div>
          
          <div class="insight-box mb-3">
            <el-icon class="text-primary mr-1"><star /></el-icon>
            <strong>洞察:</strong> {{ discovery.insight }}
          </div>
          
          <!-- 图表区域 -->
          <div :ref="el => { if (el) chartRefs[index] = el }" class="discovery-chart"></div>
        </div>
      </el-tab-pane>
    </el-tabs>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, computed, nextTick } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage } from 'element-plus'
import { 
  Search, 
  Refresh, 
  Document, 
  Download, 
  Star 
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
const discoveryData = computed(() => analyticsStore.dataDiscoveries)
const activeTab = ref('0')
const chartRefs = ref({})
const charts = ref({})

// 监听数据变化
watch([discoveryData, activeTab], () => {
  if (discoveryData.value && discoveryData.value.discoveries.length > 0) {
    nextTick(() => {
      initCharts()
    })
  }
}, { deep: true })

// 初始化图表
const initCharts = () => {
  if (!discoveryData.value || !discoveryData.value.discoveries.length) return
  
  // 清理旧图表
  Object.values(charts.value).forEach(chart => {
    if (chart) {
      chart.dispose()
    }
  })
  charts.value = {}
  
  // 创建新图表
  discoveryData.value.discoveries.forEach((discovery, index) => {
    const chartRef = chartRefs.value[index]
    if (!chartRef) return
    
    const chart = echarts.init(chartRef)
    charts.value[index] = chart
    
    // 根据数据类型选择图表类型
    const chartType = discovery.data.series?.[0]?.type || 'line'
    
    const option = {
      tooltip: {
        trigger: 'axis'
      },
      grid: {
        left: '3%',
        right: '4%',
        bottom: '3%',
        containLabel: true
      },
      xAxis: {
        type: 'category',
        data: discovery.data.xAxis || []
      },
      yAxis: {
        type: 'value'
      },
      series: discovery.data.series || []
    }
    
    chart.setOption(option)
    chart.resize()
    
    // 监听窗口大小变化
    window.addEventListener('resize', () => chart.resize())
  })
}

// 刷新分析
const handleRefresh = () => {
  ElMessage.success('正在进行智能数据探索，请稍候...')
  analyticsStore.fetchDataDiscoveries()
}

// 导出发现
const handleExportDiscovery = (discovery) => {
  ElMessage.success(`正在导出"${discovery.title}"的分析结果...`)
  
  // 实际项目中可以调用API导出分析结果
  analyticsStore.exportReport({
    type: 'discovery',
    title: discovery.title,
    data: discovery
  }).then(res => {
    if (res.success) {
      ElMessage.success(res.message)
    } else {
      ElMessage.error(res.message)
    }
  })
}

// 组件挂载时初始化
onMounted(() => {
  if (!discoveryData.value || !discoveryData.value.discoveries.length) {
    analyticsStore.fetchDataDiscoveries()
  } else {
    nextTick(() => {
      initCharts()
    })
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

.mb-3 {
  margin-bottom: 1rem;
}

.mb-1 {
  margin-bottom: 0.25rem;
}

.mb-0 {
  margin-bottom: 0;
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
  color: var(--el-text-color-secondary);
}

.text-primary {
  color: var(--el-color-primary);
}

.discovery-card {
  padding: 0.5rem;
}

.discovery-chart {
  height: 250px;
  margin-top: 1rem;
}

.confidence-badge {
  display: inline-flex;
  align-items: center;
  background-color: var(--el-color-success-light-9);
  color: var(--el-color-success-dark-2);
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
}

.insight-box {
  background-color: var(--el-fill-color-light);
  padding: 0.75rem;
  border-radius: 4px;
  border-left: 4px solid var(--el-color-primary);
}

.empty-state {
  padding: 3rem 0;
  text-align: center;
}

h5 {
  font-size: 1.25rem;
  font-weight: 600;
}

h6 {
  font-size: 1rem;
  font-weight: 600;
}
</style> 