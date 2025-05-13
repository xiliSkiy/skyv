<template>
  <div class="monitoring-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">监控管理</div>
          <div class="header-actions">
            <el-button type="outline-secondary" size="small" @click="refreshDevices">
              <el-icon><Refresh /></el-icon> 刷新
            </el-button>
            <el-button type="primary" size="small" @click="$router.push('/device/add')">
              <el-icon><Plus /></el-icon> 添加设备
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选 -->
      <div class="search-filter-box mb-4">
        <div class="filter-header">
          <div class="filter-title">筛选条件</div>
          <div class="view-switcher">
            <span class="view-text">视图：</span>
            <el-radio-group v-model="viewMode" size="small">
              <el-radio-button label="grid">
                <el-icon><Grid /></el-icon>
              </el-radio-button>
              <el-radio-button label="list">
                <el-icon><Menu /></el-icon>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
        
        <el-form :inline="true" :model="queryParams" class="search-form">
          <el-form-item label="关键词">
            <el-input
              v-model="queryParams.name"
              placeholder="设备名称/ID/位置"
              clearable
              prefix-icon="Search"
              @keyup.enter="handleQuery"
            />
          </el-form-item>
          <el-form-item label="区域">
            <el-select v-model="queryParams.location" placeholder="全部区域" clearable>
              <el-option label="全部区域" value="" />
              <el-option label="一楼大厅" value="一楼大厅" />
              <el-option label="后门通道" value="后门通道" />
              <el-option label="二楼办公区" value="二楼办公区" />
              <el-option label="地下停车场" value="地下停车场" />
              <el-option label="仓库区域" value="仓库区域" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="queryParams.status" placeholder="全部状态" clearable>
              <el-option label="全部状态" value="" />
              <el-option label="在线" :value="1" />
              <el-option label="离线" :value="0" />
              <el-option label="故障" :value="2" />
            </el-select>
          </el-form-item>
          <el-form-item class="search-buttons">
            <el-button type="primary" @click="handleQuery">
              <el-icon><Search /></el-icon> 搜索
            </el-button>
            <el-button @click="resetQuery">
              <el-icon><Refresh /></el-icon> 重置
            </el-button>
          </el-form-item>
        </el-form>
      </div>

      <!-- 设备状态统计 -->
      <div class="status-stats mb-4">
        <el-tag class="status-tag" effect="plain">设备总数: {{ deviceStats.total || 0 }}</el-tag>
        <el-tag class="status-tag" type="success" effect="plain">在线: {{ deviceStats.online || 0 }}</el-tag>
        <el-tag class="status-tag" type="danger" effect="plain">离线: {{ deviceStats.offline || 0 }}</el-tag>
        <el-tag class="status-tag" type="warning" effect="plain">故障: {{ deviceStats.fault || 0 }}</el-tag>
      </div>

      <!-- 加载状态 -->
      <div v-loading="loading" class="loading-container">
        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="grid-view-container">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" :lg="8" :xl="6" v-for="device in deviceList" :key="device.id" class="mb-4">
              <div class="camera-card">
                <div class="camera-preview">
                  <div class="camera-placeholder" v-if="!device.streamUrl">
                    <!-- 实际项目中这里应该是视频流或图片 -->
                    <i class="el-icon-video-camera"></i>
                  </div>
                  <VideoPlayer v-else :src="device.streamUrl" :device-id="device.id" />
                  <div class="overlay-controls">
                    <el-button class="overlay-btn" circle @click="handleFullscreen(device)">
                      <el-icon><FullScreen /></el-icon>
                    </el-button>
                    <el-button class="overlay-btn" circle @click="handleSettings(device)">
                      <el-icon><Setting /></el-icon>
                    </el-button>
                  </div>
                </div>
                <div class="camera-info">
                  <div class="camera-title">
                    {{ device.name }}
                    <el-tag :type="getDeviceStatusType(device.status)" size="small" effect="light">
                      {{ getDeviceStatusText(device.status) }}
                    </el-tag>
                  </div>
                  <div class="camera-subtitle">
                    <i class="el-icon-location"></i> {{ device.location || '-' }}
                  </div>
                  <div class="camera-subtitle">
                    <i class="el-icon-info"></i> {{ device.code }} | {{ device.ipAddress }}
                  </div>
                  <div class="camera-actions">
                    <div>
                      <el-button size="small" circle @click="toggleDevicePower(device)">
                        <el-icon><VideoPlay v-if="!device.isMonitoring" /><VideoPause v-else /></el-icon>
                      </el-button>
                      <el-button size="small" circle @click="refreshDevice(device)">
                        <el-icon><Refresh /></el-icon>
                      </el-button>
                      <el-dropdown trigger="click">
                        <el-button size="small" circle>
                          <el-icon><MoreFilled /></el-icon>
                        </el-button>
                        <template #dropdown>
                          <el-dropdown-menu>
                            <el-dropdown-item @click="captureSnapshot(device)">
                              <el-icon><Camera /></el-icon> 截图
                            </el-dropdown-item>
                            <el-dropdown-item @click="toggleRecording(device)">
                              <el-icon><VideoCamera /></el-icon> {{ device.isRecording ? '停止录制' : '开始录制' }}
                            </el-dropdown-item>
                            <el-dropdown-item @click="handleCheck(device)">
                              <el-icon><Connection /></el-icon> 检测连接
                            </el-dropdown-item>
                          </el-dropdown-menu>
                        </template>
                      </el-dropdown>
                    </div>
                    <el-button type="primary" size="small" @click="$router.push(`/device/detail/${device.id}`)">详情</el-button>
                  </div>
                </div>
              </div>
            </el-col>
          </el-row>
        </div>

        <!-- 列表视图 -->
        <div v-else class="list-view-container">
          <el-table :data="deviceList" border style="width: 100%">
            <el-table-column type="index" width="50" align="center" />
            <el-table-column prop="name" label="设备名称" min-width="120" show-overflow-tooltip />
            <el-table-column prop="code" label="设备编码" width="120" show-overflow-tooltip />
            <el-table-column prop="location" label="设备位置" width="120" show-overflow-tooltip />
            <el-table-column prop="ipAddress" label="IP地址" width="120" show-overflow-tooltip />
            <el-table-column label="设备状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getDeviceStatusType(scope.row.status)">
                  {{ getDeviceStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="监控状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="scope.row.isMonitoring ? 'success' : 'info'">
                  {{ scope.row.isMonitoring ? '监控中' : '未监控' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最后心跳时间" width="180" align="center">
              <template #default="scope">
                {{ formatDateTime(scope.row.lastHeartbeatTime) }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="280" fixed="right">
              <template #default="scope">
                <el-button type="primary" link @click="toggleDevicePower(scope.row)">
                  {{ scope.row.isMonitoring ? '停止' : '启动' }}
                </el-button>
                <el-button type="primary" link @click="captureSnapshot(scope.row)">
                  截图
                </el-button>
                <el-button type="primary" link @click="toggleRecording(scope.row)">
                  {{ scope.row.isRecording ? '停止录制' : '录制' }}
                </el-button>
                <el-button type="primary" link @click="$router.push(`/device/detail/${scope.row.id}`)">
                  详情
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 空数据 -->
        <el-empty v-if="deviceList.length === 0 && !loading" description="暂无监控设备" />

        <!-- 分页 -->
        <div class="pagination-container" v-if="deviceList.length > 0">
          <el-pagination
            v-model:current-page="queryParams.page"
            v-model:page-size="queryParams.size"
            :page-sizes="[10, 20, 50, 100]"
            :total="total"
            layout="total, sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import VideoPlayer from '@/components/monitoring/VideoPlayer.vue'
import { 
  getMonitoringDevices, 
  getDeviceStreamUrl, 
  startDeviceMonitoring, 
  stopDeviceMonitoring, 
  getDeviceMonitoringStatus,
  captureSnapshot as captureCameraSnapshot,
  startRecording as startCameraRecording,
  stopRecording as stopCameraRecording
} from '@/api/monitoring'
import { getDeviceStatusStats, checkDeviceConnection } from '@/api/device'
import { formatDateTime } from '@/utils/date'

// 视图模式：grid-网格视图，list-列表视图
const viewMode = ref('grid')

// 加载状态
const loading = ref(false)

// 设备列表数据
const deviceList = ref([])

// 总记录数
const total = ref(0)

// 设备状态统计
const deviceStats = reactive({
  total: 0,
  online: 0,
  offline: 0,
  fault: 0
})

// 查询参数
const queryParams = reactive({
  page: 0,
  size: 12,
  name: '',
  location: '',
  status: '',
  sort: 'id',
  direction: 'desc'
})

// 定时刷新定时器
let refreshTimer = null

// 获取设备状态类型
const getDeviceStatusType = (status) => {
  const typeMap = {
    0: 'danger',  // 离线
    1: 'success', // 在线
    2: 'warning'  // 故障
  }
  return typeMap[status] || 'info'
}

// 获取设备状态文本
const getDeviceStatusText = (status) => {
  const textMap = {
    0: '离线',
    1: '在线',
    2: '故障'
  }
  return textMap[status] || '未知'
}

// 查询设备列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getMonitoringDevices(queryParams)
    if (res.data) {
      deviceList.value = res.data.map(device => ({
        ...device,
        isMonitoring: false,
        isRecording: false,
        streamUrl: null
      }))
      total.value = res.meta?.total || 0
      
      // 获取每个设备的监控状态
      deviceList.value.forEach(async (device) => {
        if (device.status === 1) { // 只查询在线设备的监控状态
          try {
            const statusRes = await getDeviceMonitoringStatus(device.id)
            if (statusRes.data) {
              device.isMonitoring = statusRes.data.isMonitoring
              device.isRecording = statusRes.data.isRecording
              if (device.isMonitoring) {
                const streamRes = await getDeviceStreamUrl(device.id)
                device.streamUrl = streamRes.data
              }
            }
          } catch (error) {
            console.error('获取设备监控状态失败', error)
          }
        }
      })
    }
  } catch (error) {
    console.error('获取设备列表失败', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

// 获取设备状态统计
const getDeviceStats = async () => {
  try {
    const res = await getDeviceStatusStats()
    if (res.data) {
      deviceStats.online = res.data[1] || 0
      deviceStats.offline = res.data[0] || 0
      deviceStats.fault = res.data[2] || 0
      deviceStats.total = deviceStats.online + deviceStats.offline + deviceStats.fault
    }
  } catch (error) {
    console.error('获取设备状态统计失败', error)
  }
}

// 查询按钮点击
const handleQuery = () => {
  queryParams.page = 0
  getList()
}

// 重置查询条件
const resetQuery = () => {
  queryParams.name = ''
  queryParams.location = ''
  queryParams.status = ''
  handleQuery()
}

// 每页条数变更
const handleSizeChange = (size) => {
  queryParams.size = size
  getList()
}

// 页码变更
const handleCurrentChange = (page) => {
  queryParams.page = page - 1
  getList()
}

// 刷新设备列表
const refreshDevices = () => {
  getList()
  getDeviceStats()
}

// 刷新单个设备
const refreshDevice = async (device) => {
  try {
    const statusRes = await getDeviceMonitoringStatus(device.id)
    if (statusRes.data) {
      device.isMonitoring = statusRes.data.isMonitoring
      device.isRecording = statusRes.data.isRecording
      
      if (device.isMonitoring) {
        const streamRes = await getDeviceStreamUrl(device.id)
        device.streamUrl = streamRes.data
      } else {
        device.streamUrl = null
      }
    }
    ElMessage.success('刷新成功')
  } catch (error) {
    console.error('刷新设备失败', error)
    ElMessage.error('刷新设备失败')
  }
}

// 切换设备监控状态
const toggleDevicePower = async (device) => {
  try {
    if (device.isMonitoring) {
      await stopDeviceMonitoring(device.id)
      device.isMonitoring = false
      device.streamUrl = null
      ElMessage.success('已停止监控')
    } else {
      if (device.status !== 1) {
        ElMessage.warning('设备不在线，无法启动监控')
        return
      }
      await startDeviceMonitoring(device.id)
      device.isMonitoring = true
      const streamRes = await getDeviceStreamUrl(device.id)
      device.streamUrl = streamRes.data
      ElMessage.success('已启动监控')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 截图
const captureSnapshot = async (device) => {
  if (!device.isMonitoring) {
    ElMessage.warning('请先启动监控')
    return
  }
  
  try {
    await captureCameraSnapshot(device.id)
    ElMessage.success('截图成功，已保存到服务器')
  } catch (error) {
    console.error('截图失败', error)
    ElMessage.error('截图失败')
  }
}

// 切换录制状态
const toggleRecording = async (device) => {
  if (!device.isMonitoring) {
    ElMessage.warning('请先启动监控')
    return
  }
  
  try {
    if (device.isRecording) {
      await stopCameraRecording(device.id)
      device.isRecording = false
      ElMessage.success('已停止录制')
    } else {
      await startCameraRecording(device.id)
      device.isRecording = true
      ElMessage.success('已开始录制')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败')
  }
}

// 检测设备连接
const handleCheck = async (device) => {
  try {
    loading.value = true
    const res = await checkDeviceConnection(device.id)
    if (res.data) {
      ElMessage.success('设备连接正常')
    } else {
      ElMessage.warning('设备连接失败')
    }
  } catch (error) {
    ElMessage.error('检测连接失败')
  } finally {
    loading.value = false
  }
}

// 全屏显示
const handleFullscreen = (device) => {
  if (!device.isMonitoring) {
    ElMessage.warning('请先启动监控')
    return
  }
  
  // 这里应该实现全屏显示逻辑
  ElMessage.info('全屏功能开发中')
}

// 设置
const handleSettings = (device) => {
  ElMessage.info('设置功能开发中')
}

// 定时刷新
const startRefreshTimer = () => {
  refreshTimer = setInterval(() => {
    // 只刷新在线且正在监控的设备
    const monitoringDevices = deviceList.value.filter(d => d.status === 1 && d.isMonitoring)
    monitoringDevices.forEach(device => {
      refreshDevice(device)
    })
  }, 30000) // 每30秒刷新一次
}

// 初始化
onMounted(() => {
  getList()
  getDeviceStats()
  startRefreshTimer()
})

// 组件销毁前清除定时器
onBeforeUnmount(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="scss" scoped>
.monitoring-container {
  padding: 6px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-title {
    font-size: 16px;
    font-weight: 500;
  }
}

.search-filter-box {
  background-color: #f9f9f9;
  border-radius: 8px;
  padding: 15px;
  margin-bottom: 20px;
  
  .filter-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 15px;
    padding-bottom: 10px;
    border-bottom: 1px solid #eaeaea;
    
    .filter-title {
      font-size: 14px;
      font-weight: 500;
      color: #606266;
    }
    
    .view-text {
      margin-right: 8px;
      color: #606266;
    }
    
    .view-switcher {
      display: flex;
      align-items: center;
      gap: 10px;
    }
  }
  
  .search-form {
    display: flex;
    flex-wrap: wrap;
    gap: 15px;
    align-items: flex-start;
    
    :deep(.el-form-item) {
      margin-bottom: 0;
      margin-right: 0;
    }
    
    :deep(.el-form-item__label) {
      color: #606266;
      font-weight: normal;
    }
    
    .search-buttons {
      margin-left: auto;
    }
  }
}

.status-stats {
  margin-bottom: 15px;
  
  .status-tag {
    margin-right: 10px;
  }
}

.loading-container {
  min-height: 300px;
}

.camera-card {
  background-color: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  transition: transform 0.3s;
  height: 100%;
  
  &:hover {
    transform: translateY(-5px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  }
}

.camera-preview {
  width: 100%;
  height: 200px;
  background-color: #1e1e1e;
  position: relative;
  
  .camera-placeholder {
    position: absolute;
    top: 0;
    left: 0;
    width: 100%;
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.3);
    font-size: 30px;
    background-color: #1e1e1e;
    background-size: cover;
    background-position: center;
  }
  
  .overlay-controls {
    position: absolute;
    top: 10px;
    right: 10px;
    display: flex;
    gap: 5px;
    opacity: 0;
    transition: opacity 0.3s;
  }
  
  &:hover .overlay-controls {
    opacity: 1;
  }
  
  .overlay-btn {
    width: 30px;
    height: 30px;
    padding: 0;
    color: white;
    background-color: rgba(0, 0, 0, 0.5);
    
    &:hover {
      background-color: rgba(0, 0, 0, 0.7);
    }
  }
}

.camera-info {
  padding: 15px;
  
  .camera-title {
    font-weight: 600;
    margin-bottom: 5px;
    font-size: 16px;
    display: flex;
    justify-content: space-between;
    align-items: center;
  }
  
  .camera-subtitle {
    color: #6c757d;
    font-size: 14px;
    margin-bottom: 5px;
    display: flex;
    align-items: center;
    
    i {
      margin-right: 5px;
      width: 14px;
    }
  }
  
  .camera-actions {
    display: flex;
    justify-content: space-between;
    align-items: center;
    border-top: 1px solid #f0f0f0;
    padding-top: 15px;
    margin-top: 5px;
  }
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.mb-4 {
  margin-bottom: 16px;
}

@media screen and (max-width: 768px) {
  .search-filter-box {
    .search-form {
      flex-direction: column;
      align-items: stretch;
      
      .el-form-item {
        width: 100%;
      }
      
      .search-buttons {
        margin-left: 0;
        display: flex;
        justify-content: flex-end;
      }
    }
  }
}
</style> 