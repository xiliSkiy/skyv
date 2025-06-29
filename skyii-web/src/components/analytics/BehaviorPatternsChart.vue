<template>
  <el-card class="chart-container h-full" shadow="hover">
    <div class="d-flex justify-between items-center mb-4">
      <div>
        <h5 class="mb-0">人员行为模式分析</h5>
        <small class="text-muted">
          <el-icon class="mr-1"><cpu /></el-icon>
          基于深度学习
        </small>
      </div>
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
    
    <!-- 图表区域 -->
    <div ref="chartRef" style="height: 250px;"></div>
    
    <!-- 异常检测提示 -->
    <div class="mt-2 p-2 bg-light rounded">
      <small class="text-muted">
        <el-icon class="mr-1 text-danger"><warning /></el-icon>
        <strong>异常检测:</strong> {{ behaviorData?.abnormalDetection || '正在分析行为模式...' }}
      </small>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage } from 'element-plus'
import { Cpu, Warning, ArrowDown } from '@element-plus/icons-vue'
import * as echarts from 'echarts/core'
import { PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent
} from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

// 注册必要的组件
echarts.use([
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  PieChart,
  CanvasRenderer
])

const analyticsStore = useAnalyticsStore()
const chartRef = ref(null)
const chart = ref(null)
const currentPeriod = ref('最近7天')
const behaviorData = computed(() => analyticsStore.behaviorPatterns)

// 监听数据变化
watch([behaviorData, currentPeriod], () => {
  if (behaviorData.value) {
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
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      right: 10,
      top: 'center',
      data: behaviorData.value.data.map(item => item.name)
    },
    series: [
      {
        name: '行为模式',
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: false,
          position: 'center'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: '16',
            fontWeight: 'bold'
          }
        },
        labelLine: {
          show: false
        },
        data: behaviorData.value.data || []
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
  analyticsStore.fetchBehaviorPatterns({
    period: period
  })
}

// 组件挂载时初始化
onMounted(() => {
  if (behaviorData.value) {
    initChart()
  } else {
    analyticsStore.fetchBehaviorPatterns()
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

.mb-0 {
  margin-bottom: 0;
}

.mt-2 {
  margin-top: 0.5rem;
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

.text-danger {
  color: var(--el-color-danger);
}

.bg-light {
  background-color: var(--el-fill-color-light);
}

.rounded {
  border-radius: 4px;
}

h5 {
  font-size: 1.25rem;
  font-weight: 600;
}
</style> 