<template>
  <el-card class="chart-container mb-4" shadow="hover">
    <div class="d-flex justify-between items-center mb-4">
      <div>
        <h5 class="mb-0">AI增强热力图分析</h5>
        <small class="text-muted">
          <el-icon class="mr-1"><document /></el-icon>
          智能识别关注区域
        </small>
      </div>
      <div>
        <el-radio-group v-model="currentMode" size="small" class="mr-2" @change="handleModeChange">
          <el-radio-button v-for="mode in heatmapData?.modes" :key="mode" :label="mode">{{ mode }}</el-radio-button>
        </el-radio-group>
        <el-dropdown trigger="click" @command="handleAreaChange">
          <el-button size="small" plain>
            {{ currentArea }}
            <el-icon class="el-icon--right"><arrow-down /></el-icon>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item 
                v-for="area in heatmapData?.areas" 
                :key="area" 
                :command="area"
                :class="{ 'is-active': area === currentArea }"
              >
                {{ area }}
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 热力图区域 -->
    <div class="heatmap-container position-relative" style="height: 400px;">
      <div class="heatmap-placeholder" v-if="!heatmapData">
        <el-skeleton animated :rows="10" />
      </div>
      <template v-else>
        <!-- 热力图图像 -->
        <div class="heatmap-image">
          <img :src="getHeatmapImageUrl()" alt="热力图" class="w-full h-full object-cover" />
          
          <!-- 热点标记 -->
          <div 
            v-for="hotspot in heatmapData.hotspots" 
            :key="hotspot.id" 
            class="hotspot" 
            :class="{ 'high-risk': hotspot.level === '高' }"
            :style="{
              left: `${hotspot.x}%`,
              top: `${hotspot.y}%`,
              width: `${hotspot.radius}px`,
              height: `${hotspot.radius}px`
            }"
          ></div>
          
          <!-- 热点注释 -->
          <div 
            class="hotspot-annotation"
            :style="{
              left: `${heatmapData.hotspots[0]?.x + 3}%`,
              top: `${heatmapData.hotspots[0]?.y - 5}%`
            }"
            v-if="heatmapData.hotspots && heatmapData.hotspots.length > 0"
          >
            <strong>异常聚集点 #{{ heatmapData.hotspots[0]?.id }}</strong><br>
            风险等级: {{ heatmapData.hotspots[0]?.level }}<br>
            持续时间: {{ heatmapData.hotspots[0]?.duration }}
          </div>
        </div>
      </template>
    </div>
    
    <!-- 分析结果 -->
    <el-row class="mt-3" v-if="heatmapData?.analysis">
      <el-col :md="8">
        <el-card class="bg-light border-0">
          <div class="d-flex items-center">
            <el-icon class="text-danger mr-2"><location /></el-icon>
            <div>
              <h6 class="mb-0">热点区域</h6>
              <small>{{ heatmapData.analysis.hotspots }}</small>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :md="8">
        <el-card class="bg-light border-0">
          <div class="d-flex items-center">
            <el-icon class="text-warning mr-2"><timer /></el-icon>
            <div>
              <h6 class="mb-0">高峰时段</h6>
              <small>{{ heatmapData.analysis.peakTimes }}</small>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :md="8">
        <el-card class="bg-light border-0">
          <div class="d-flex items-center">
            <el-icon class="text-primary mr-2"><star /></el-icon>
            <div>
              <h6 class="mb-0">AI建议</h6>
              <small>{{ heatmapData.analysis.suggestion }}</small>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage } from 'element-plus'
import { 
  Refresh,
  Download,
  Document,
  FullScreen,
  Setting,
  InfoFilled,
  VideoCamera,
  MapLocation,
  Warning
} from '@element-plus/icons-vue'

const analyticsStore = useAnalyticsStore()
const heatmapData = computed(() => analyticsStore.heatmap)
const currentArea = ref('一楼大厅')
const currentMode = ref('停留时间')

// 监听数据变化
watch([currentArea, currentMode], () => {
  fetchHeatmapData()
})

// 获取热力图数据
const fetchHeatmapData = () => {
  analyticsStore.fetchHeatmap({
    area: currentArea.value,
    mode: currentMode.value
  })
}

// 处理区域变化
const handleAreaChange = (area) => {
  currentArea.value = area
  ElMessage.success(`已切换到${area}区域`)
}

// 处理模式变化
const handleModeChange = (mode) => {
  ElMessage.success(`已切换到${mode}模式`)
}

// 获取热力图图片URL
const getHeatmapImageUrl = () => {
  // 实际项目中应该返回后端生成的热力图URL
  // 这里使用占位图片
  return `https://via.placeholder.com/1200x400/f8f9fa/777777?text=AI增强热力图分析-${currentArea.value}-${currentMode.value}`
}

// 组件挂载时初始化
onMounted(() => {
  if (!heatmapData.value) {
    fetchHeatmapData()
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

.text-warning {
  color: var(--el-color-warning);
}

.text-primary {
  color: var(--el-color-primary);
}

.bg-light {
  background-color: var(--el-fill-color-light);
}

.border-0 {
  border: none;
}

.w-full {
  width: 100%;
}

.h-full {
  height: 100%;
}

.position-relative {
  position: relative;
}

.object-cover {
  object-fit: cover;
}

.heatmap-container {
  position: relative;
  overflow: hidden;
  border-radius: 4px;
  background-color: var(--el-fill-color-light);
}

.heatmap-image {
  position: relative;
  width: 100%;
  height: 100%;
}

.hotspot {
  position: absolute;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  background-color: rgba(255, 0, 0, 0.2);
  border: 2px solid red;
  animation: pulse 2s infinite;
  z-index: 10;
}

.high-risk {
  background-color: rgba(255, 0, 0, 0.3);
  border: 3px solid red;
}

.hotspot-annotation {
  position: absolute;
  background: rgba(255, 255, 255, 0.9);
  padding: 5px 10px;
  border-radius: 4px;
  border: 1px solid #ddd;
  font-size: 12px;
  z-index: 20;
}

@keyframes pulse {
  0% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
  50% {
    transform: translate(-50%, -50%) scale(1.1);
    opacity: 0.7;
  }
  100% {
    transform: translate(-50%, -50%) scale(1);
    opacity: 1;
  }
}

h5 {
  font-size: 1.25rem;
  font-weight: 600;
}

h6 {
  font-size: 0.875rem;
  font-weight: 600;
}
</style> 