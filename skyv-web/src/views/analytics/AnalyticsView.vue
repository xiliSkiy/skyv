<template>
  <div class="analytics-container">
    <page-header title="AI增强数据分析" subtitle="由人工智能大模型提供支持">
      <template #actions>
        <el-button type="primary" plain @click="handleCreateAnalysis">
          <el-icon class="mr-1"><plus /></el-icon>创建分析
        </el-button>
        <el-button type="primary" plain @click="handleExportReport" class="ml-2">
          <el-icon class="mr-1"><document /></el-icon>导出报表
        </el-button>
        <el-button type="primary" @click="handleRefreshData" class="ml-2">
          <el-icon class="mr-1"><refresh /></el-icon>刷新数据
        </el-button>
      </template>
    </page-header>
     
    
    <!-- 完整AI分析助手 -->
    <div class="mb-4">
      <AIAssistant />
    </div>
    
    <!-- 统计卡片 -->
    <stat-cards />
    
    <!-- 主要图表区域 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xl="16" :lg="16" :md="24" :sm="24" :xs="24">
        <traffic-trends-chart />
      </el-col>
      <el-col :xl="8" :lg="8" :md="24" :sm="24" :xs="24">
        <behavior-patterns-chart />
      </el-col>
    </el-row>
    
    <!-- 异常分析与预测 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xl="12" :lg="12" :md="24" :sm="24" :xs="24">
        <abnormal-events-chart />
      </el-col>
      <el-col :xl="12" :lg="12" :md="24" :sm="24" :xs="24">
        <device-failure-chart />
      </el-col>
    </el-row>
    
    <!-- 热力图分析 -->
    <heatmap-analysis />
    
    <!-- 智能数据探索 -->
    <data-discoveries />
    
    <!-- 设备分析明细表格 -->
    <device-analytics-table />
    
    <!-- 页脚 -->
    <div class="text-center mt-4 text-muted small">
      <p>© {{ new Date().getFullYear() }} SkyEye 智能监控系统 | AI分析引擎版本: 2.5.3 | 上次数据更新: {{ lastUpdateTime }}</p>
    </div>
    
    <!-- 创建分析对话框 -->
    <el-dialog
      v-model="createAnalysisDialogVisible"
      title="创建自定义分析"
      width="600px"
    >
      <el-form label-position="top">
        <el-form-item label="分析名称">
          <el-input v-model="newAnalysis.name" placeholder="请输入分析名称" />
        </el-form-item>
        <el-form-item label="分析类型">
          <el-select v-model="newAnalysis.type" class="w-full">
            <el-option label="人流量分析" value="traffic" />
            <el-option label="行为模式分析" value="behavior" />
            <el-option label="异常事件分析" value="abnormal" />
            <el-option label="设备健康分析" value="device" />
            <el-option label="自定义分析" value="custom" />
          </el-select>
        </el-form-item>
        <el-form-item label="分析时间范围">
          <el-date-picker
            v-model="newAnalysis.timeRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            class="w-full"
          />
        </el-form-item>
        <el-form-item label="分析目标">
          <el-select
            v-model="newAnalysis.targets"
            multiple
            collapse-tags
            class="w-full"
            placeholder="请选择分析目标"
          >
            <el-option label="所有设备" value="all" />
            <el-option label="前门监控" value="front-door" />
            <el-option label="后门监控" value="back-door" />
            <el-option label="办公区监控" value="office" />
            <el-option label="停车场监控" value="parking" />
            <el-option label="仓库监控" value="warehouse" />
          </el-select>
        </el-form-item>
        <el-form-item label="AI分析深度">
          <el-radio-group v-model="newAnalysis.aiLevel">
            <el-radio-button label="basic">基础</el-radio-button>
            <el-radio-button label="standard">标准</el-radio-button>
            <el-radio-button label="deep">深度</el-radio-button>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="createAnalysisDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitAnalysis">创建</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Refresh, Setting, Plus, Position } from '@element-plus/icons-vue'

// 导入组件
import PageHeader from '@/components/common/PageHeader.vue'
import AIAssistant from '@/components/analytics/AIAssistant.vue'
import StatCards from '@/components/analytics/StatCards.vue'
import TrafficTrendsChart from '@/components/analytics/TrafficTrendsChart.vue'
import BehaviorPatternsChart from '@/components/analytics/BehaviorPatternsChart.vue'
import AbnormalEventsChart from '@/components/analytics/AbnormalEventsChart.vue'
import DeviceFailureChart from '@/components/analytics/DeviceFailureChart.vue'
import HeatmapAnalysis from '@/components/analytics/HeatmapAnalysis.vue'
import DataDiscoveries from '@/components/analytics/DataDiscoveries.vue'
import DeviceAnalyticsTable from '@/components/analytics/DeviceAnalyticsTable.vue'

const analyticsStore = useAnalyticsStore()
const createAnalysisDialogVisible = ref(false)
const queryText = ref('') // 简化版AI助手的输入文本

// 格式化当前时间
const lastUpdateTime = computed(() => {
  const now = new Date()
  const year = now.getFullYear()
  const month = String(now.getMonth() + 1).padStart(2, '0')
  const day = String(now.getDate()).padStart(2, '0')
  const hours = String(now.getHours()).padStart(2, '0')
  const minutes = String(now.getMinutes()).padStart(2, '0')
  const seconds = String(now.getSeconds()).padStart(2, '0')
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`
})

// 新建分析
const newAnalysis = ref({
  name: '',
  type: 'traffic',
  timeRange: '',
  targets: ['all'],
  aiLevel: 'standard'
})

// 测试Element组件
const testElementComponents = () => {
  ElMessage.success('Element Plus组件测试成功!')
}

// 导出报告
const handleExportReport = () => {
  ElMessageBox.prompt('请输入报告名称', '导出分析报告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '例如：每周数据分析报告'
  }).then(({ value }) => {
    ElMessage.success('正在生成报告，请稍候...')
    
    // 实际项目中可以调用API导出报告
    analyticsStore.exportReport({
      name: value,
      type: 'full_report'
    }).then(res => {
      if (res.success) {
        ElMessage.success(res.message)
      } else {
        ElMessage.error(res.message)
      }
    })
  }).catch(() => {})
}

// 刷新数据
const handleRefreshData = () => {
  ElMessage.success('正在刷新数据...')
  analyticsStore.initAllData()
}

// 创建分析
const handleCreateAnalysis = () => {
  createAnalysisDialogVisible.value = true
}

// 提交分析
const handleSubmitAnalysis = () => {
  if (!newAnalysis.value.name) {
    ElMessage.warning('请输入分析名称')
    return
  }
  
  ElMessage.success('正在创建分析任务，请稍候...')
  
  // 实际项目中可以调用API创建分析任务
  analyticsStore.createCustomAnalysis({
    name: newAnalysis.value.name,
    type: newAnalysis.value.type,
    timeRange: newAnalysis.value.timeRange,
    targets: newAnalysis.value.targets,
    aiLevel: newAnalysis.value.aiLevel
  }).then(res => {
    if (res.success) {
      ElMessage.success(res.message)
      createAnalysisDialogVisible.value = false
      
      // 重置表单
      newAnalysis.value = {
        name: '',
        type: 'traffic',
        timeRange: '',
        targets: ['all'],
        aiLevel: 'standard'
      }
    } else {
      ElMessage.error(res.message)
    }
  })
}

// 组件挂载时初始化数据
onMounted(() => {
  analyticsStore.initAllData()
})
</script>

<style scoped>
.analytics-container {
  padding: 20px;
}

.ml-2 {
  margin-left: 0.5rem;
}

.mr-1 {
  margin-right: 0.25rem;
}

.mb-4 {
  margin-bottom: 1.5rem;
}

.mt-4 {
  margin-top: 1.5rem;
}

.w-full {
  width: 100%;
}

.text-center {
  text-align: center;
}

.text-muted {
  color: #909399;
}

.small {
  font-size: 0.875rem;
}
</style>