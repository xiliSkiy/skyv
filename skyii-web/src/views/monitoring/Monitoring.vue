<template>
  <div class="monitoring-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">
            <el-icon class="title-icon"><Monitor /></el-icon>
            实时监控中心
          </h2>
          <p class="page-subtitle">全方位视频监控，智能分析与预警</p>
        </div>
        <div class="header-actions">
          <el-button-group>
            <el-button @click="refreshAll">
              <el-icon><Refresh /></el-icon>
              刷新全部
            </el-button>
            <el-button @click="batchControl">
              <el-icon><Operation /></el-icon>
              批量控制
            </el-button>
            <el-button @click="layoutSettings">
              <el-icon><Grid /></el-icon>
              布局设置
            </el-button>
          </el-button-group>
          <el-button type="primary" @click="enterFullscreen">
            <el-icon><FullScreen /></el-icon>
            全屏监控
          </el-button>
        </div>
      </div>
    </div>

    <!-- 智能控制面板 -->
    <div class="control-panel">
      <el-card shadow="never" class="control-card">
        <div class="control-content">
          <el-row :gutter="20">
            <!-- 快速操作 -->
            <el-col :xs="24" :sm="12" :md="8" :lg="6">
              <div class="control-section">
                <h4 class="section-title">快速操作</h4>
                <div class="quick-actions">
                  <el-button-group class="action-group">
                    <el-button 
                      :type="isAllPlaying ? 'danger' : 'success'" 
                      @click="toggleAllCameras"
                    >
                      <el-icon><component :is="isAllPlaying ? 'VideoPause' : 'VideoPlay'" /></el-icon>
                      {{ isAllPlaying ? '停止全部' : '启动全部' }}
                    </el-button>
                    <el-button @click="captureAllSnapshots">
                      <el-icon><Camera /></el-icon>
                      全部截图
                    </el-button>
                  </el-button-group>
                </div>
              </div>
            </el-col>
            
            <!-- 监控统计 -->
            <el-col :xs="24" :sm="12" :md="8" :lg="6">
              <div class="control-section">
                <h4 class="section-title">监控统计</h4>
                <div class="monitor-stats">
                  <div class="stat-item">
                    <span class="stat-label">在线设备</span>
                    <span class="stat-value online">{{ monitorStats.online }}/{{ monitorStats.total }}</span>
                  </div>
                  <div class="stat-item">
                    <span class="stat-label">监控中</span>
                    <span class="stat-value monitoring">{{ monitorStats.monitoring }}</span>
                  </div>
                </div>
              </div>
            </el-col>
            
            <!-- 布局控制 -->
            <el-col :xs="24" :sm="12" :md="8" :lg="6">
              <div class="control-section">
                <h4 class="section-title">显示布局</h4>
                <div class="layout-controls">
                  <el-radio-group v-model="layoutMode" size="small">
                    <el-radio-button value="1x1">1x1</el-radio-button>
                    <el-radio-button value="2x2">2x2</el-radio-button>
                    <el-radio-button value="3x3">3x3</el-radio-button>
                    <el-radio-button value="4x4">4x4</el-radio-button>
                  </el-radio-group>
                </div>
              </div>
            </el-col>
            
            <!-- 筛选条件 -->
            <el-col :xs="24" :sm="12" :md="8" :lg="6">
              <div class="control-section">
                <h4 class="section-title">筛选条件</h4>
                <div class="filter-controls">
                  <el-select v-model="filterArea" placeholder="选择区域" size="small" style="width: 100%">
                    <el-option label="全部区域" value="" />
                    <el-option label="主入口" value="main_entrance" />
                    <el-option label="办公区域" value="office" />
                    <el-option label="停车场" value="parking" />
                    <el-option label="仓储区" value="warehouse" />
                  </el-select>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>
      </el-card>
    </div>

    <!-- 监控画面网格 -->
    <div class="monitoring-grid">
      <div class="grid-container" :class="`layout-${layoutMode}`">
        <div 
          v-for="camera in displayedCameras" 
          :key="camera.id"
          class="camera-container"
          :class="{ 'active': selectedCamera?.id === camera.id }"
          @click="selectCamera(camera)"
        >
          <div class="camera-wrapper">
            <!-- 视频画面 -->
            <div class="video-area">
              <div class="video-placeholder" v-if="!camera.isPlaying">
                <div class="placeholder-content">
                  <el-icon class="camera-icon"><VideoCamera /></el-icon>
                  <div class="camera-name">{{ camera.name }}</div>
                  <div class="camera-status">
                    <el-tag :type="getStatusType(camera.status)" size="small">
                      {{ getStatusText(camera.status) }}
                    </el-tag>
                  </div>
                  <el-button 
                    v-if="camera.status === 'online'" 
                    type="primary" 
                    size="small" 
                    @click.stop="playCamera(camera)"
                  >
                    <el-icon><VideoPlay /></el-icon>
                    播放
                  </el-button>
                </div>
              </div>
              
              <!-- 模拟视频流 -->
              <div v-else class="video-stream">
                <img :src="camera.snapshot" :alt="camera.name" />
                <div class="video-overlay">
                  <div class="recording-indicator" v-if="camera.isRecording">
                    <el-icon class="recording-icon"><VideoCameraFilled /></el-icon>
                    <span>录制中</span>
                  </div>
                  <div class="ai-detection" v-if="camera.aiDetection">
                    <el-tag type="warning" size="small">
                      <el-icon><MagicStick /></el-icon>
                      {{ camera.aiDetection }}
                    </el-tag>
                  </div>
                </div>
              </div>
              
              <!-- 状态指示器 -->
              <div class="status-indicators">
                <div class="connection-status" :class="camera.status">
                  <span class="status-dot"></span>
                  <span class="status-text">{{ getStatusText(camera.status) }}</span>
                </div>
                <div class="signal-strength" v-if="camera.status === 'online'">
                  <el-icon><Connection /></el-icon>
                  <span>{{ camera.signalStrength }}%</span>
                </div>
              </div>
            </div>
            
            <!-- 控制面板 -->
            <div class="camera-controls" v-if="camera.isPlaying">
              <div class="control-left">
                <div class="camera-info">
                  <div class="camera-title">{{ camera.name }}</div>
                  <div class="camera-location">
                    <el-icon><Location /></el-icon>
                    {{ camera.location }}
                  </div>
                </div>
              </div>
              
              <div class="control-right">
                <el-button-group size="small">
                  <el-button @click.stop="captureSnapshot(camera)">
                    <el-icon><Camera /></el-icon>
                  </el-button>
                  <el-button 
                    :type="camera.isRecording ? 'danger' : ''" 
                    @click.stop="toggleRecording(camera)"
                  >
                    <el-icon><component :is="camera.isRecording ? 'VideoPause' : 'VideoCamera'" /></el-icon>
                  </el-button>
                  <el-button @click.stop="showPTZControl(camera)">
                    <el-icon><Compass /></el-icon>
                  </el-button>
                  <el-button @click.stop="stopCamera(camera)">
                    <el-icon><Close /></el-icon>
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 空位填充 -->
        <div 
          v-for="i in emptySlots" 
          :key="`empty-${i}`"
          class="camera-container empty-slot"
        >
          <div class="empty-content">
            <el-icon class="empty-icon"><Plus /></el-icon>
            <div class="empty-text">添加设备</div>
          </div>
        </div>
      </div>
    </div>

    <!-- 设备管理侧边栏 -->
    <div class="device-sidebar" :class="{ 'expanded': sidebarExpanded }">
      <div class="sidebar-header">
        <h4>设备列表</h4>
        <el-button 
          text 
          @click="sidebarExpanded = !sidebarExpanded"
        >
          <el-icon><component :is="sidebarExpanded ? 'Fold' : 'Expand'" /></el-icon>
        </el-button>
      </div>
      
      <div class="sidebar-content" v-if="sidebarExpanded">
        <div class="device-search">
          <el-input 
            v-model="deviceSearch" 
            placeholder="搜索设备" 
            size="small"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
        </div>
        
        <div class="device-list">
          <div 
            v-for="camera in filteredCameras" 
            :key="camera.id"
            class="device-item"
            :class="{ 'active': selectedCamera?.id === camera.id }"
            @click="selectCamera(camera)"
          >
            <div class="device-info">
              <div class="device-name">{{ camera.name }}</div>
              <div class="device-location">{{ camera.location }}</div>
            </div>
            <div class="device-status">
              <el-tag :type="getStatusType(camera.status)" size="small">
                {{ getStatusText(camera.status) }}
              </el-tag>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- PTZ 控制弹窗 -->
    <el-dialog 
      v-model="ptzDialogVisible" 
      title="云台控制" 
      width="400px"
      align-center
    >
      <div class="ptz-control">
        <div class="ptz-directions">
          <div class="direction-row">
            <el-button @click="ptzMove('up-left')">↖</el-button>
            <el-button @click="ptzMove('up')">↑</el-button>
            <el-button @click="ptzMove('up-right')">↗</el-button>
          </div>
          <div class="direction-row">
            <el-button @click="ptzMove('left')">←</el-button>
            <el-button @click="ptzMove('center')">●</el-button>
            <el-button @click="ptzMove('right')">→</el-button>
          </div>
          <div class="direction-row">
            <el-button @click="ptzMove('down-left')">↙</el-button>
            <el-button @click="ptzMove('down')">↓</el-button>
            <el-button @click="ptzMove('down-right')">↘</el-button>
          </div>
        </div>
        
        <div class="ptz-zoom">
          <div class="zoom-control">
            <span>缩放</span>
            <el-button-group>
              <el-button @click="ptzZoom('in')">
                <el-icon><ZoomIn /></el-icon>
              </el-button>
              <el-button @click="ptzZoom('out')">
                <el-icon><ZoomOut /></el-icon>
              </el-button>
            </el-button-group>
          </div>
          
          <div class="preset-control">
            <span>预置位</span>
            <el-select v-model="currentPreset" placeholder="选择预置位">
              <el-option label="预置位1" value="1" />
              <el-option label="预置位2" value="2" />
              <el-option label="预置位3" value="3" />
            </el-select>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Monitor, Refresh, Operation, Grid, FullScreen, VideoPlay, VideoPause,
  Camera, VideoCamera, VideoCameraFilled, MagicStick, Connection, Location,
  Compass, Close, Plus, Fold, Expand, Search, ZoomIn, ZoomOut
} from '@element-plus/icons-vue'

// 响应式数据
const layoutMode = ref('2x2')
const filterArea = ref('')
const isAllPlaying = ref(false)
const selectedCamera = ref(null)
const sidebarExpanded = ref(true)
const deviceSearch = ref('')
const ptzDialogVisible = ref(false)
const currentPreset = ref('')

// 监控统计
const monitorStats = reactive({
  total: 12,
  online: 10,
  offline: 2,
  monitoring: 6
})

// Mock 摄像头数据
const cameras = ref([
  {
    id: 'CAM001',
    name: '主入口摄像头',
    location: '主入口大厅',
    area: 'main_entrance',
    status: 'online',
    isPlaying: false,
    isRecording: false,
    signalStrength: 95,
    snapshot: 'https://via.placeholder.com/640x360/667eea/ffffff?text=主入口监控',
    aiDetection: null
  },
  {
    id: 'CAM002',
    name: '办公区域A摄像头',
    location: '办公区域A栋',
    area: 'office',
    status: 'online',
    isPlaying: true,
    isRecording: true,
    signalStrength: 88,
    snapshot: 'https://via.placeholder.com/640x360/764ba2/ffffff?text=办公区域A',
    aiDetection: '检测到异常聚集'
  },
  {
    id: 'CAM003',
    name: '停车场B区摄像头',
    location: '停车场B区',
    area: 'parking',
    status: 'offline',
    isPlaying: false,
    isRecording: false,
    signalStrength: 0,
    snapshot: 'https://via.placeholder.com/640x360/e74c3c/ffffff?text=设备离线',
    aiDetection: null
  },
  {
    id: 'CAM004',
    name: '仓储区入口摄像头',
    location: '仓储区入口',
    area: 'warehouse',
    status: 'online',
    isPlaying: true,
    isRecording: false,
    signalStrength: 92,
    snapshot: 'https://via.placeholder.com/640x360/3498db/ffffff?text=仓储区入口',
    aiDetection: '检测到可疑物品'
  },
  {
    id: 'CAM005',
    name: '办公区域B摄像头',
    location: '办公区域B栋',
    area: 'office',
    status: 'online',
    isPlaying: false,
    isRecording: false,
    signalStrength: 85,
    snapshot: 'https://via.placeholder.com/640x360/9b59b6/ffffff?text=办公区域B',
    aiDetection: null
  },
  {
    id: 'CAM006',
    name: '会议室摄像头',
    location: '大会议室',
    area: 'office',
    status: 'online',
    isPlaying: true,
    isRecording: false,
    signalStrength: 90,
    snapshot: 'https://via.placeholder.com/640x360/f39c12/ffffff?text=大会议室',
    aiDetection: null
  },
  {
    id: 'CAM007',
    name: '后门通道摄像头',
    location: '后门安全通道',
    area: 'main_entrance',
    status: 'online',
    isPlaying: false,
    isRecording: false,
    signalStrength: 78,
    snapshot: 'https://via.placeholder.com/640x360/1abc9c/ffffff?text=后门通道',
    aiDetection: null
  },
  {
    id: 'CAM008',
    name: '电梯厅摄像头',
    location: '一楼电梯厅',
    area: 'office',
    status: 'maintenance',
    isPlaying: false,
    isRecording: false,
    signalStrength: 0,
    snapshot: 'https://via.placeholder.com/640x360/f1c40f/ffffff?text=维护中',
    aiDetection: null
  }
])

// 计算属性
const filteredCameras = computed(() => {
  let filtered = cameras.value
  
  if (filterArea.value) {
    filtered = filtered.filter(camera => camera.area === filterArea.value)
  }
  
  if (deviceSearch.value) {
    filtered = filtered.filter(camera => 
      camera.name.toLowerCase().includes(deviceSearch.value.toLowerCase()) ||
      camera.location.toLowerCase().includes(deviceSearch.value.toLowerCase())
    )
  }
  
  return filtered
})

const displayedCameras = computed(() => {
  const layoutCounts = {
    '1x1': 1,
    '2x2': 4,
    '3x3': 9,
    '4x4': 16
  }
  
  return filteredCameras.value.slice(0, layoutCounts[layoutMode.value])
})

const emptySlots = computed(() => {
  const layoutCounts = {
    '1x1': 1,
    '2x2': 4,
    '3x3': 9,
    '4x4': 16
  }
  
  const maxSlots = layoutCounts[layoutMode.value]
  const usedSlots = displayedCameras.value.length
  return Math.max(0, maxSlots - usedSlots)
})

// 方法
const getStatusType = (status) => {
  const types = {
    online: 'success',
    offline: 'danger',
    maintenance: 'warning'
  }
  return types[status] || 'info'
}

const getStatusText = (status) => {
  const texts = {
    online: '在线',
    offline: '离线',
    maintenance: '维护中'
  }
  return texts[status] || '未知'
}

const selectCamera = (camera) => {
  selectedCamera.value = camera
}

const playCamera = (camera) => {
  if (camera.status !== 'online') {
    ElMessage.warning('设备不在线，无法播放')
    return
  }
  
  camera.isPlaying = true
  ElMessage.success(`开始播放 ${camera.name}`)
  updateMonitorStats()
}

const stopCamera = (camera) => {
  camera.isPlaying = false
  camera.isRecording = false
  ElMessage.info(`停止播放 ${camera.name}`)
  updateMonitorStats()
}

const toggleAllCameras = () => {
  const onlineCameras = cameras.value.filter(c => c.status === 'online')
  
  if (isAllPlaying.value) {
    // 停止全部
    onlineCameras.forEach(camera => {
      camera.isPlaying = false
      camera.isRecording = false
    })
    isAllPlaying.value = false
    ElMessage.info('已停止全部监控')
  } else {
    // 启动全部
    onlineCameras.forEach(camera => {
      camera.isPlaying = true
    })
    isAllPlaying.value = true
    ElMessage.success('已启动全部监控')
  }
  updateMonitorStats()
}

const captureSnapshot = (camera) => {
  ElMessage.success(`${camera.name} 截图已保存`)
}

const captureAllSnapshots = () => {
  const playingCameras = cameras.value.filter(c => c.isPlaying)
  if (playingCameras.length === 0) {
    ElMessage.warning('没有正在播放的摄像头')
    return
  }
  
  ElMessage.success(`已对 ${playingCameras.length} 个摄像头进行截图`)
}

const toggleRecording = (camera) => {
  camera.isRecording = !camera.isRecording
  ElMessage.success(`${camera.name} ${camera.isRecording ? '开始' : '停止'}录制`)
}

const showPTZControl = (camera) => {
  selectedCamera.value = camera
  ptzDialogVisible.value = true
}

const ptzMove = (direction) => {
  ElMessage.info(`云台移动: ${direction}`)
}

const ptzZoom = (action) => {
  ElMessage.info(`云台缩放: ${action}`)
}

const refreshAll = () => {
  ElMessage.success('刷新完成')
  updateMonitorStats()
}

const batchControl = () => {
  ElMessage.info('批量控制功能开发中...')
}

const layoutSettings = () => {
  ElMessage.info('布局设置功能开发中...')
}

const enterFullscreen = () => {
  ElMessage.info('全屏监控功能开发中...')
}

const updateMonitorStats = () => {
  monitorStats.monitoring = cameras.value.filter(c => c.isPlaying).length
  monitorStats.online = cameras.value.filter(c => c.status === 'online').length
  monitorStats.offline = cameras.value.filter(c => c.status === 'offline').length
}

// 初始化
onMounted(() => {
  updateMonitorStats()
  // 模拟AI检测更新
  setInterval(() => {
    cameras.value.forEach(camera => {
      if (camera.isPlaying && Math.random() < 0.1) {
        const detections = ['检测到异常行为', '识别到可疑物品', '发现未授权人员', null, null, null]
        camera.aiDetection = detections[Math.floor(Math.random() * detections.length)]
      }
    })
  }, 5000)
})
</script>

<style lang="scss" scoped>
.monitoring-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
  position: relative;
}

.page-header {
  margin-bottom: 24px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 24px 32px;
    color: white;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
    
    .header-left {
      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 28px;
        font-weight: 700;
        margin: 0 0 8px 0;
        
        .title-icon {
          color: #ffd700;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
        }
      }
      
      .page-subtitle {
        margin: 0;
        font-size: 16px;
        opacity: 0.9;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
      
      .el-button {
        border-color: rgba(255, 255, 255, 0.3);
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
          border-color: rgba(255, 255, 255, 0.5);
        }
      }
    }
  }
}

.control-panel {
  margin-bottom: 24px;
  
  .control-card {
    border-radius: 12px;
    border: none;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    
    .control-content {
      .section-title {
        color: #606266;
        font-size: 14px;
        font-weight: 600;
        margin: 0 0 12px 0;
        border-bottom: 1px solid #e4e7ed;
        padding-bottom: 6px;
      }
      
      .quick-actions {
        .action-group {
          width: 100%;
          
          .el-button {
            flex: 1;
          }
        }
      }
      
      .monitor-stats {
        .stat-item {
          display: flex;
          justify-content: space-between;
          align-items: center;
          margin-bottom: 8px;
          
          .stat-label {
            color: #909399;
            font-size: 13px;
          }
          
          .stat-value {
            font-weight: 600;
            
            &.online { color: #67c23a; }
            &.monitoring { color: #409eff; }
          }
        }
      }
      
      .layout-controls {
        .el-radio-group {
          width: 100%;
          
          .el-radio-button {
            flex: 1;
          }
        }
      }
    }
  }
}

.monitoring-grid {
  margin-bottom: 24px;
  
  .grid-container {
    display: grid;
    gap: 16px;
    
    &.layout-1x1 {
      grid-template-columns: 1fr;
    }
    
    &.layout-2x2 {
      grid-template-columns: repeat(2, 1fr);
    }
    
    &.layout-3x3 {
      grid-template-columns: repeat(3, 1fr);
    }
    
    &.layout-4x4 {
      grid-template-columns: repeat(4, 1fr);
    }
  }
}

.camera-container {
  background: white;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  transition: all 0.3s;
  cursor: pointer;
  border: 2px solid transparent;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
  }
  
  &.active {
    border-color: #409eff;
    box-shadow: 0 4px 12px rgba(64, 158, 255, 0.2);
  }
  
  .camera-wrapper {
    height: 100%;
    display: flex;
    flex-direction: column;
  }
  
  .video-area {
    flex: 1;
    position: relative;
    aspect-ratio: 16/9;
    background: #000;
    
    .video-placeholder {
      position: absolute;
      top: 0;
      left: 0;
      right: 0;
      bottom: 0;
      display: flex;
      align-items: center;
      justify-content: center;
      background: linear-gradient(135deg, #2c3e50 0%, #34495e 100%);
      
      .placeholder-content {
        text-align: center;
        color: white;
        
        .camera-icon {
          font-size: 48px;
          margin-bottom: 12px;
          opacity: 0.7;
        }
        
        .camera-name {
          font-size: 16px;
          font-weight: 600;
          margin-bottom: 8px;
        }
        
        .camera-status {
          margin-bottom: 16px;
        }
      }
    }
    
    .video-stream {
      position: relative;
      width: 100%;
      height: 100%;
      
      img {
        width: 100%;
        height: 100%;
        object-fit: cover;
      }
      
      .video-overlay {
        position: absolute;
        top: 12px;
        left: 12px;
        right: 12px;
        display: flex;
        justify-content: space-between;
        align-items: flex-start;
        
        .recording-indicator {
          display: flex;
          align-items: center;
          gap: 6px;
          background: rgba(245, 108, 108, 0.9);
          color: white;
          padding: 4px 8px;
          border-radius: 6px;
          font-size: 12px;
          
          .recording-icon {
            animation: blink 1s infinite;
          }
        }
        
        .ai-detection {
          background: rgba(0, 0, 0, 0.7);
          border-radius: 6px;
          padding: 2px;
        }
      }
    }
    
    .status-indicators {
      position: absolute;
      bottom: 12px;
      left: 12px;
      right: 12px;
      display: flex;
      justify-content: space-between;
      align-items: center;
      
      .connection-status {
        display: flex;
        align-items: center;
        gap: 6px;
        background: rgba(0, 0, 0, 0.7);
        color: white;
        padding: 4px 8px;
        border-radius: 6px;
        font-size: 12px;
        
        .status-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          
          .online & { background: #67c23a; }
          .offline & { background: #f56c6c; }
          .maintenance & { background: #e6a23c; }
        }
      }
      
      .signal-strength {
        display: flex;
        align-items: center;
        gap: 4px;
        background: rgba(0, 0, 0, 0.7);
        color: white;
        padding: 4px 8px;
        border-radius: 6px;
        font-size: 12px;
      }
    }
  }
  
  .camera-controls {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 12px 16px;
    background: #fafbfc;
    border-top: 1px solid #e4e7ed;
    
    .control-left {
      flex: 1;
      
      .camera-info {
        .camera-title {
          font-size: 14px;
          font-weight: 600;
          color: #2c3e50;
          margin-bottom: 4px;
        }
        
        .camera-location {
          display: flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: #7f8c8d;
        }
      }
    }
    
    .control-right {
      .el-button-group .el-button {
        padding: 4px 8px;
        font-size: 12px;
      }
    }
  }
}

.empty-slot {
  border: 2px dashed #ddd;
  background: #f9f9f9;
  
  .empty-content {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: #ccc;
    
    .empty-icon {
      font-size: 48px;
      margin-bottom: 12px;
    }
    
    .empty-text {
      font-size: 14px;
    }
  }
  
  &:hover {
    border-color: #409eff;
    background: #f0f7ff;
    
    .empty-content {
      color: #409eff;
    }
  }
}

.device-sidebar {
  position: fixed;
  top: 20px;
  right: 20px;
  bottom: 20px;
  width: 60px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transition: width 0.3s;
  z-index: 100;
  
  &.expanded {
    width: 320px;
  }
  
  .sidebar-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    padding: 16px;
    border-bottom: 1px solid #e4e7ed;
    
    h4 {
      margin: 0;
      font-size: 16px;
      color: #2c3e50;
    }
  }
  
  .sidebar-content {
    padding: 16px;
    height: calc(100% - 57px);
    overflow-y: auto;
    
    .device-search {
      margin-bottom: 16px;
    }
    
    .device-list {
      .device-item {
        display: flex;
        justify-content: space-between;
        align-items: center;
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 8px;
        cursor: pointer;
        transition: background-color 0.3s;
        
        &:hover {
          background: #f5f7fa;
        }
        
        &.active {
          background: #e6f7ff;
          border: 1px solid #409eff;
        }
        
        .device-info {
          flex: 1;
          
          .device-name {
            font-size: 14px;
            font-weight: 600;
            color: #2c3e50;
            margin-bottom: 4px;
          }
          
          .device-location {
            font-size: 12px;
            color: #7f8c8d;
          }
        }
      }
    }
  }
}

.ptz-control {
  .ptz-directions {
    display: flex;
    flex-direction: column;
    gap: 8px;
    margin-bottom: 24px;
    
    .direction-row {
      display: flex;
      justify-content: center;
      gap: 8px;
      
      .el-button {
        width: 50px;
        height: 50px;
        font-size: 18px;
      }
    }
  }
  
  .ptz-zoom {
    .zoom-control,
    .preset-control {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;
      
      span {
        color: #606266;
        font-weight: 500;
      }
    }
  }
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0.3; }
}

@media (max-width: 1200px) {
  .monitoring-grid .grid-container {
    &.layout-4x4 {
      grid-template-columns: repeat(3, 1fr);
    }
  }
}

@media (max-width: 768px) {
  .monitoring-container {
    padding: 12px;
  }
  
  .page-header .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: center;
    text-align: center;
  }
  
  .monitoring-grid .grid-container {
    &.layout-2x2,
    &.layout-3x3,
    &.layout-4x4 {
      grid-template-columns: 1fr;
    }
  }
  
  .device-sidebar {
    position: relative;
    width: 100%;
    height: auto;
    margin-top: 20px;
    
    &.expanded {
      width: 100%;
    }
  }
  
  .control-panel .control-content .el-row .el-col {
    margin-bottom: 16px;
  }
}
</style> 