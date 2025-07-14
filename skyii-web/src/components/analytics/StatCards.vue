<template>
  <el-row :gutter="20" class="mb-4">
    <el-col :xl="6" :lg="6" :md="12" :sm="24" :xs="24" class="mb-3">
      <el-card class="stat-card h-full" shadow="hover">
        <div class="d-flex justify-between items-center">
          <div>
            <h6 class="text-muted mb-2">总人流量</h6>
            <h3 class="mb-0">{{ formatNumber(overview?.totalTraffic?.value || 0) }}</h3>
            <div class="d-flex items-center">
              <small :class="getTrendClass(overview?.totalTraffic?.trend)">
                {{ formatTrend(overview?.totalTraffic?.trend) }}
                <el-icon v-if="overview?.totalTraffic?.trend > 0"><arrow-up /></el-icon>
                <el-icon v-else-if="overview?.totalTraffic?.trend < 0"><arrow-down /></el-icon>
              </small>
            </div>
            <div class="text-primary mt-1 text-sm">
              <el-icon class="mr-1"><histogram /></el-icon>
              {{ overview?.totalTraffic?.prediction }}
            </div>
          </div>
          <div class="bg-primary bg-opacity-10 p-3 rounded text-primary">
            <el-icon :size="30"><user /></el-icon>
          </div>
        </div>
      </el-card>
    </el-col>
    
    <el-col :xl="6" :lg="6" :md="12" :sm="24" :xs="24" class="mb-3">
      <el-card class="stat-card h-full" shadow="hover">
        <div class="d-flex justify-between items-center">
          <div>
            <h6 class="text-muted mb-2">报警事件</h6>
            <h3 class="mb-0">{{ formatNumber(overview?.alertEvents?.value || 0) }}</h3>
            <div class="d-flex items-center">
              <small :class="getTrendClass(overview?.alertEvents?.trend, true)">
                {{ formatTrend(overview?.alertEvents?.trend) }}
                <el-icon v-if="overview?.alertEvents?.trend > 0"><arrow-up /></el-icon>
                <el-icon v-else-if="overview?.alertEvents?.trend < 0"><arrow-down /></el-icon>
              </small>
            </div>
            <div class="text-danger mt-1 text-sm">
              <el-icon class="mr-1"><warning /></el-icon>
              {{ overview?.alertEvents?.warning }}
            </div>
          </div>
          <div class="bg-danger bg-opacity-10 p-3 rounded text-danger">
            <el-icon :size="30"><bell /></el-icon>
          </div>
        </div>
      </el-card>
    </el-col>
    
    <el-col :xl="6" :lg="6" :md="12" :sm="24" :xs="24" class="mb-3">
      <el-card class="stat-card h-full" shadow="hover">
        <div class="d-flex justify-between items-center">
          <div>
            <h6 class="text-muted mb-2">平均停留时间</h6>
            <h3 class="mb-0">
              {{ formatNumber(overview?.avgStayTime?.value || 0) }}
              <small>{{ overview?.avgStayTime?.unit }}</small>
            </h3>
            <div class="d-flex items-center">
              <small :class="getTrendClass(overview?.avgStayTime?.trend)">
                {{ formatTrend(overview?.avgStayTime?.trend) }}
                <el-icon v-if="overview?.avgStayTime?.trend > 0"><arrow-up /></el-icon>
                <el-icon v-else-if="overview?.avgStayTime?.trend < 0"><arrow-down /></el-icon>
              </small>
            </div>
            <div class="text-info mt-1 text-sm">
              <el-icon class="mr-1"><check /></el-icon>
              {{ overview?.avgStayTime?.status }}
            </div>
          </div>
          <div class="bg-info bg-opacity-10 p-3 rounded text-info">
            <el-icon :size="30"><timer /></el-icon>
          </div>
        </div>
      </el-card>
    </el-col>
    
    <el-col :xl="6" :lg="6" :md="12" :sm="24" :xs="24" class="mb-3">
      <el-card class="stat-card h-full" shadow="hover">
        <div class="d-flex justify-between items-center">
          <div>
            <h6 class="text-muted mb-2">设备健康率</h6>
            <h3 class="mb-0">
              {{ formatNumber(overview?.deviceHealth?.value || 0) }}
              <small>{{ overview?.deviceHealth?.unit }}</small>
            </h3>
            <div class="d-flex items-center">
              <small :class="getTrendClass(overview?.deviceHealth?.trend)">
                {{ formatTrend(overview?.deviceHealth?.trend) }}
                <el-icon v-if="overview?.deviceHealth?.trend > 0"><arrow-up /></el-icon>
                <el-icon v-else-if="overview?.deviceHealth?.trend < 0"><arrow-down /></el-icon>
              </small>
            </div>
            <div class="text-warning mt-1 text-sm">
              <el-icon class="mr-1"><tools /></el-icon>
              {{ overview?.deviceHealth?.warning }}
            </div>
          </div>
          <div class="bg-success bg-opacity-10 p-3 rounded text-success">
            <el-icon :size="30"><monitor /></el-icon>
          </div>
        </div>
      </el-card>
    </el-col>
  </el-row>
</template>

<script setup>
import { computed } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import {
  User,
  Bell,
  Timer,
  Monitor,
  ArrowUp,
  ArrowDown,
  Histogram,
  Warning,
  Check,
  Tools
} from '@element-plus/icons-vue'

const analyticsStore = useAnalyticsStore()
const overview = computed(() => analyticsStore.overview)

// 格式化数字
const formatNumber = (num) => {
  return new Intl.NumberFormat().format(num)
}

// 格式化趋势
const formatTrend = (trend) => {
  if (!trend) return '0%'
  const sign = trend > 0 ? '+' : ''
  return `${sign}${trend}% `
}

// 获取趋势样式
const getTrendClass = (trend, inverse = false) => {
  if (!trend) return ''
  
  if (inverse) {
    // 对于报警事件，增加是不好的
    return trend > 0 ? 'text-danger' : 'text-success'
  }
  
  // 对于其他指标，增加是好的
  return trend > 0 ? 'text-success' : 'text-danger'
}
</script>

<style scoped>
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

.mt-1 {
  margin-top: 0.25rem;
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

.text-sm {
  font-size: 0.875rem;
}

.text-muted {
  color: var(--el-text-color-secondary);
}

.text-primary {
  color: var(--el-color-primary);
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

.text-info {
  color: var(--el-color-info);
}

.bg-primary {
  background-color: var(--el-color-primary);
}

.bg-success {
  background-color: var(--el-color-success);
}

.bg-danger {
  background-color: var(--el-color-danger);
}

.bg-warning {
  background-color: var(--el-color-warning);
}

.bg-info {
  background-color: var(--el-color-info);
}

.bg-opacity-10 {
  opacity: 0.1;
}

.rounded {
  border-radius: 4px;
}

.p-3 {
  padding: 0.75rem;
}

.h-full {
  height: 100%;
}

.stat-card {
  transition: transform 0.3s;
}

.stat-card:hover {
  transform: translateY(-5px);
}

h3 {
  font-size: 1.75rem;
  font-weight: 600;
}

h6 {
  font-size: 0.875rem;
  font-weight: 400;
}
</style> 