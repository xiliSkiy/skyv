<template>
  <el-card class="chart-container h-full" shadow="hover">
    <div class="d-flex justify-between items-center mb-4">
      <div>
        <h5 class="mb-0">人流量趋势与预测分析</h5>
        <small class="text-muted">
          <el-icon class="mr-1"><cpu /></el-icon>
          AI预测准确率: {{ trafficData?.accuracy || 0 }}%
        </small>
      </div>
      <div class="d-flex">
        <el-radio-group v-model="timeRange" size="small" class="mr-2">
          <el-radio-button label="day">日</el-radio-button>
          <el-radio-button label="week">周</el-radio-button>
          <el-radio-button label="month">月</el-radio-button>
        </el-radio-group>
        <el-button size="small" type="primary" plain @click="handleAIAnalysis">
          <el-icon class="mr-1"><magic-stick /></el-icon>
          智能分析
        </el-button>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="chart-area position-relative">
      <div ref="chartRef" class="chart-canvas" style="height: 300px;"></div>
      
      <!-- 图例 -->
      <div class="chart-legend">
        <div class="d-flex items-center mb-1">
          <div class="legend-line bg-primary mr-2"></div>
          <small>历史数据</small>
        </div>
        <div class="d-flex items-center mb-1">
          <div class="legend-line bg-danger mr-2"></div>
          <small>AI预测</small>
        </div>
        <div class="d-flex items-center">
          <div class="legend-area bg-danger-light mr-2"></div>
          <small>置信区间</small>
        </div>
      </div>
    </div>
    
    <!-- AI洞察 -->
    <div class="mt-2 p-2 bg-light rounded">
      <small class="text-muted">
        <el-icon class="mr-1 text-warning"><star /></el-icon>
        <strong>AI洞察:</strong> {{ trafficData?.insight || '正在分析数据...' }}
      </small>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage } from 'element-plus'
import { Cpu, MagicStick, Star } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  ToolboxComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必要的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  GridComponent,
  LegendComponent,
  DataZoomComponent,
  ToolboxComponent,
  LineChart,
  CanvasRenderer
])

const analyticsStore = useAnalyticsStore()
const chartRef = ref(null)
const chart = ref(null)
const timeRange = ref('week')
const trafficData = computed(() => analyticsStore.trafficTrends)

// 监听数据变化
watch([trafficData, timeRange], () => {
  if (trafficData.value) {
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
        label: {
          backgroundColor: '#6a7985'
        }
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
      boundaryGap: false,
      data: trafficData.value.xAxis || []
    },
    yAxis: {
      type: 'value',
      name: '人流量',
      nameTextStyle: {
        padding: [0, 0, 0, 30]
      }
    },
    series: generateSeries()
  }
  
  chart.value.setOption(option)
}

// 生成系列数据
const generateSeries = () => {
  if (!trafficData.value || !trafficData.value.series) {
    return []
  }
  
  return trafficData.value.series.map(item => {
    return {
      name: item.name,
      type: 'line',
      stack: item.stack || '',
      smooth: true,
      symbol: item.symbol === 'none' ? 'none' : 'circle',
      symbolSize: 5,
      sampling: 'average',
      areaStyle: item.areaStyle ? {
        color: item.areaStyle.color || 'rgba(255,107,107,0.2)'
      } : null,
      itemStyle: {
        color: item.itemStyle?.color
      },
      lineStyle: item.lineStyle || {},
      data: item.data
    }
  })
}

// 智能分析
const handleAIAnalysis = () => {
  ElMessage.success('正在进行智能分析，请稍候...')
  
  // 在实际项目中，这里可以调用API进行更深入的分析
  setTimeout(() => {
    ElMessage.success('分析完成')
  }, 1500)
}

// 组件挂载时初始化
onMounted(() => {
  if (trafficData.value) {
    initChart()
  } else {
    analyticsStore.fetchTrafficTrends()
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

.mb-1 {
  margin-bottom: 0.25rem;
}

.mb-0 {
  margin-bottom: 0;
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

.p-2 {
  padding: 0.5rem;
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

.text-warning {
  color: var(--el-color-warning);
}

.bg-light {
  background-color: var(--el-fill-color-light);
}

.bg-primary {
  background-color: var(--el-color-primary);
}

.bg-danger {
  background-color: var(--el-color-danger);
}

.bg-danger-light {
  background-color: rgba(255, 107, 107, 0.2);
}

.rounded {
  border-radius: 4px;
}

.position-relative {
  position: relative;
}

.chart-legend {
  position: absolute;
  top: 10px;
  right: 10px;
  background: rgba(255, 255, 255, 0.9);
  padding: 8px;
  border-radius: 4px;
  border: 1px solid #ddd;
  font-size: 12px;
}

.legend-line {
  width: 12px;
  height: 3px;
}

.legend-area {
  width: 12px;
  height: 12px;
}

h5 {
  font-size: 1.25rem;
  font-weight: 600;
}
</style> 