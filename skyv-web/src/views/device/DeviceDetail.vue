<template>
  <div class="device-detail-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">设备详情</div>
          <div class="header-actions">
            <el-button @click="$router.push('/device')">
              <el-icon><Back /></el-icon>返回
            </el-button>
            <el-button type="primary" @click="$router.push(`/device/edit/${deviceId}`)">
              <el-icon><Edit /></el-icon>编辑设备
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!device.id" description="未找到设备信息" />
        <template v-else>
          <!-- 设备头部信息 -->
          <div class="device-header">
            <div class="device-icon">
              <el-icon v-if="device.type === 'CAMERA'"><VideoCamera /></el-icon>
              <el-icon v-else-if="device.type === 'SENSOR'"><Odometer /></el-icon>
              <el-icon v-else-if="device.type === 'ACCESS'"><Key /></el-icon>
              <el-icon v-else><Monitor /></el-icon>
            </div>
            <div class="device-title">
              <h2>{{ device.name }}</h2>
              <div class="device-id">ID: {{ device.id }} | 编码: {{ device.code }}</div>
              <div class="device-status">
                <el-tag :type="getDeviceStatusType(device.status)">
                  {{ getDeviceStatusText(device.status) }}
                </el-tag>
                <span class="last-heartbeat">最后心跳: {{ formatDateTime(device.lastHeartbeatTime) || '-' }}</span>
              </div>
            </div>
          </div>

          <!-- 设备操作按钮 -->
          <div class="device-actions">
            <el-button type="success" @click="$router.push(`/monitoring?deviceId=${deviceId}`)">
              <el-icon><VideoCamera /></el-icon>实时监控
            </el-button>
            <el-button type="primary" @click="handleCheckConnection">
              <el-icon><Connection /></el-icon>检测连接
            </el-button>
            <el-button type="warning" @click="$router.push(`/history?deviceId=${deviceId}`)">
              <el-icon><Histogram /></el-icon>历史数据
            </el-button>
            <el-button type="danger" @click="handleDelete">
              <el-icon><Delete /></el-icon>删除设备
            </el-button>
          </div>

          <!-- 设备详情标签页 -->
          <el-tabs v-model="activeTab" class="device-tabs">
            <el-tab-pane label="基本信息" name="basic">
              <div class="tab-content">
                <el-row :gutter="20">
                  <el-col :span="12">
                    <el-card class="device-info-card">
                      <template #header>
                        <div class="card-title">设备基本信息</div>
                      </template>
                      <el-descriptions :column="1" border>
                        <el-descriptions-item label="设备名称">{{ device.name }}</el-descriptions-item>
                        <el-descriptions-item label="设备编码">{{ device.code }}</el-descriptions-item>
                        <el-descriptions-item label="设备类型">{{ getDeviceTypeText(device.type) }}</el-descriptions-item>
                        <el-descriptions-item label="设备型号">{{ device.model || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="描述">{{ device.description || '暂无描述' }}</el-descriptions-item>
                      </el-descriptions>
                    </el-card>
                  </el-col>
                  <el-col :span="12">
                    <el-card class="device-info-card">
                      <template #header>
                        <div class="card-title">分类与标签</div>
                      </template>
                      <el-descriptions :column="1" border>
                        <el-descriptions-item label="所属分组">{{ device.group || '默认分组' }}</el-descriptions-item>
                        <el-descriptions-item label="所属区域">{{ device.area || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="标签">
                          <el-tag 
                            v-for="(tag, index) in device.tags" 
                            :key="index"
                            :type="getTagType(index)" 
                            class="tag-item"
                            effect="plain"
                          >
                            {{ tag }}
                          </el-tag>
                          <span v-if="!device.tags || device.tags.length === 0">-</span>
                        </el-descriptions-item>
                        <el-descriptions-item label="创建时间">{{ formatDateTime(device.createdAt) || '-' }}</el-descriptions-item>
                        <el-descriptions-item label="更新时间">{{ formatDateTime(device.updatedAt) || '-' }}</el-descriptions-item>
                      </el-descriptions>
                    </el-card>
                  </el-col>
                </el-row>

                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-title">厂商信息</div>
                  </template>
                  <el-descriptions :column="3" border>
                    <el-descriptions-item label="厂商名称">{{ device.manufacturer || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="联系电话">{{ device.manufacturerPhone || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="联系邮箱">{{ device.manufacturerEmail || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="序列号">{{ device.serialNumber || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="出厂日期">{{ device.manufactureDate || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="保修期">{{ device.warrantyPeriod || '-' }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="网络信息" name="network">
              <div class="tab-content">
                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-title">网络配置</div>
                  </template>
                  <el-descriptions :column="3" border>
                    <el-descriptions-item label="IP地址">{{ device.ipAddress || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="端口">{{ device.port || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="MAC地址">{{ device.macAddress || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="子网掩码">{{ device.subnetMask || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="默认网关">{{ device.gateway || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="DNS">{{ device.dns || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="连接方式">{{ getConnectionTypeText(device.connectionType) }}</el-descriptions-item>
                    <el-descriptions-item label="连接状态">
                      <el-tag :type="device.status === 1 ? 'success' : 'danger'">
                        {{ device.status === 1 ? '已连接' : '未连接' }}
                      </el-tag>
                    </el-descriptions-item>
                  </el-descriptions>
                </el-card>

                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-title">认证信息</div>
                  </template>
                  <el-descriptions :column="3" border>
                    <el-descriptions-item label="用户名">{{ device.username || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="密码">******</el-descriptions-item>
                    <el-descriptions-item label="认证方式">{{ getAuthTypeText(device.authType) }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="位置信息" name="location">
              <div class="tab-content">
                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-title">位置信息</div>
                  </template>
                  <el-descriptions :column="3" border>
                    <el-descriptions-item label="所在建筑">{{ device.building || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="楼层">{{ device.floor || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="房间号">{{ device.room || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="地理位置">{{ device.location || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="GPS坐标">{{ device.gpsCoordinates || '-' }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
                
                <div class="device-map">
                  <div ref="mapContainer" style="height: 300px; margin-top: 15px; background: #f8f9fa; border-radius: 4px;">
                    <el-empty description="地图加载中..." />
                  </div>
                </div>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="协议参数" name="protocol">
              <div class="tab-content">
                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-title">协议参数</div>
                  </template>
                  <el-descriptions :column="3" border>
                    <el-descriptions-item label="协议类型">{{ device.protocol || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="协议版本">{{ device.protocolVersion || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="传输方式">{{ device.transportProtocol || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="API路径">{{ device.apiPath || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="视频路径">{{ device.videoPath || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="控制路径">{{ device.controlPath || '-' }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>

                <el-card v-if="device.type === 'CAMERA'" class="device-info-card">
                  <template #header>
                    <div class="card-title">摄像头参数</div>
                  </template>
                  <el-descriptions :column="3" border>
                    <el-descriptions-item label="分辨率">{{ device.resolution || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="帧率">{{ device.frameRate || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="码率">{{ device.bitRate || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="图像格式">{{ device.imageFormat || '-' }}</el-descriptions-item>
                    <el-descriptions-item label="云台控制">{{ device.ptzControl ? '支持' : '不支持' }}</el-descriptions-item>
                    <el-descriptions-item label="红外功能">{{ device.infrared ? '支持' : '不支持' }}</el-descriptions-item>
                  </el-descriptions>
                </el-card>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="设备指标" name="metrics">
              <div class="tab-content">
                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-header-with-action">
                      <div class="card-title">实时指标</div>
                      <div class="card-actions">
                        <el-button size="small" @click="refreshMetrics">
                          <el-icon><Refresh /></el-icon>刷新
                        </el-button>
                      </div>
                    </div>
                  </template>
                  <el-table :data="deviceMetrics" v-loading="metricsLoading" border stripe>
                    <el-table-column prop="name" label="指标名称" />
                    <el-table-column prop="value" label="当前值" />
                    <el-table-column prop="unit" label="单位" />
                    <el-table-column prop="updateTime" label="更新时间" />
                    <el-table-column label="状态">
                      <template #default="{ row }">
                        <el-tag :type="getMetricStatusType(row.status)" effect="plain">
                          {{ getMetricStatusText(row.status) }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                </el-card>

                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-title">指标趋势</div>
                  </template>
                  <div style="height: 300px; background: #f8f9fa; border-radius: 4px;">
                    <el-empty description="图表加载中..." />
                  </div>
                </el-card>
              </div>
            </el-tab-pane>
            
            <el-tab-pane label="设备日志" name="logs">
              <div class="tab-content">
                <el-card class="device-info-card">
                  <template #header>
                    <div class="card-header-with-action">
                      <div class="card-title">操作日志</div>
                      <div class="card-actions">
                        <el-button size="small" @click="refreshLogs">
                          <el-icon><Refresh /></el-icon>刷新
                        </el-button>
                      </div>
                    </div>
                  </template>
                  <el-table :data="deviceLogs" v-loading="logsLoading" border stripe>
                    <el-table-column prop="timestamp" label="时间" width="180" />
                    <el-table-column prop="type" label="类型" width="100" />
                    <el-table-column prop="message" label="详情" min-width="200" show-overflow-tooltip />
                    <el-table-column prop="operator" label="操作人" width="120" />
                    <el-table-column label="级别" width="100">
                      <template #default="{ row }">
                        <el-tag :type="getLogLevelType(row.level)" size="small">
                          {{ row.level }}
                        </el-tag>
                      </template>
                    </el-table-column>
                  </el-table>
                  <div class="pagination-container">
                    <el-pagination
                      v-model:current-page="logsQueryParams.page"
                      v-model:page-size="logsQueryParams.size"
                      :page-sizes="[10, 20, 50, 100]"
                      :total="logsTotal"
                      layout="total, sizes, prev, pager, next, jumper"
                      @size-change="handleLogsSizeChange"
                      @current-change="handleLogsCurrentChange"
                    />
                  </div>
                </el-card>
              </div>
            </el-tab-pane>
          </el-tabs>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { formatDateTime } from '@/utils/date'
import {
  Back, Edit, VideoCamera, Connection, Histogram, Delete,
  Odometer, Key, Monitor, Refresh
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const deviceId = route.params.id
const loading = ref(false)
const activeTab = ref('basic')
const device = reactive({
  tags: [],
  status: 0
})

// 设备指标相关
const metricsLoading = ref(false)
const deviceMetrics = ref([])

// 设备日志相关
const logsLoading = ref(false)
const deviceLogs = ref([])
const logsTotal = ref(0)
const logsQueryParams = reactive({
  page: 1,
  size: 10
})

// 获取设备状态类型
const getDeviceStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
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

// 获取设备类型文本
const getDeviceTypeText = (type) => {
  const typeMap = {
    'CAMERA': '摄像头',
    'SENSOR': '传感器',
    'ACCESS': '门禁',
    'OTHER': '其他'
  }
  return typeMap[type] || type
}

// 获取连接方式文本
const getConnectionTypeText = (type) => {
  const typeMap = {
    'WIRED': '有线',
    'WIRELESS': '无线',
    'CELLULAR': '蜂窝网络'
  }
  return typeMap[type] || type || '-'
}

// 获取认证方式文本
const getAuthTypeText = (type) => {
  const typeMap = {
    'BASIC': '基本认证',
    'DIGEST': '摘要认证',
    'TOKEN': '令牌认证',
    'NONE': '无认证'
  }
  return typeMap[type] || type || '-'
}

// 获取指标状态类型
const getMetricStatusType = (status) => {
  const typeMap = {
    'NORMAL': 'success',
    'WARNING': 'warning',
    'CRITICAL': 'danger',
    'UNKNOWN': 'info'
  }
  return typeMap[status] || 'info'
}

// 获取指标状态文本
const getMetricStatusText = (status) => {
  const textMap = {
    'NORMAL': '正常',
    'WARNING': '警告',
    'CRITICAL': '严重',
    'UNKNOWN': '未知'
  }
  return textMap[status] || '未知'
}

// 获取日志级别类型
const getLogLevelType = (level) => {
  const typeMap = {
    'INFO': 'info',
    'WARNING': 'warning',
    'ERROR': 'danger',
    'DEBUG': ''
  }
  return typeMap[level] || ''
}

// 获取标签类型
const getTagType = (index) => {
  const types = ['', 'success', 'warning', 'danger', 'info']
  return types[index % types.length]
}

// Mock数据：获取设备详情
const fetchDeviceDetail = async (deviceId) => {
  loading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      // Mock数据
      Object.assign(device, {
        id: deviceId,
        name: '前门摄像头',
        code: 'CAM-FRONT-001',
        type: 'CAMERA',
        model: 'HK-DS2CD2032-I',
        description: '前门安全监控摄像头，24小时工作',
        status: 1,
        lastHeartbeatTime: new Date().toISOString(),
        createdAt: '2023-05-10T10:00:00Z',
        updatedAt: '2023-06-15T08:30:00Z',
        group: '安防监控',
        area: '北区 > 一层',
        tags: ['重要', '入口', '室外'],
        manufacturer: '海康威视',
        manufacturerPhone: '400-000-1234',
        manufacturerEmail: 'support@example.com',
        serialNumber: 'SN20230510001',
        manufactureDate: '2023-01-15',
        warrantyPeriod: '3年',
        ipAddress: '192.168.1.101',
        port: 554,
        macAddress: '00:11:22:33:44:55',
        subnetMask: '255.255.255.0',
        gateway: '192.168.1.1',
        dns: '8.8.8.8',
        connectionType: 'WIRED',
        username: 'admin',
        authType: 'DIGEST',
        building: '主楼',
        floor: '1楼',
        room: '大厅',
        location: '前门入口',
        gpsCoordinates: '39.9042° N, 116.4074° E',
        protocol: 'ONVIF',
        protocolVersion: '2.0',
        transportProtocol: 'RTSP',
        apiPath: '/onvif/device_service',
        videoPath: '/Streaming/Channels/1',
        controlPath: '/PTZ/Control',
        resolution: '1920x1080',
        frameRate: '25fps',
        bitRate: '4Mbps',
        imageFormat: 'H.264',
        ptzControl: true,
        infrared: true
      })
      loading.value = false
    }, 500)
  } catch (error) {
    console.error('获取设备详情失败', error)
    ElMessage.error('获取设备详情失败')
    loading.value = false
  }
}

// Mock数据：刷新设备指标
const refreshMetrics = async () => {
  metricsLoading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      deviceMetrics.value = [
        {
          name: 'CPU使用率',
          value: '45',
          unit: '%',
          updateTime: '2023-06-28 10:30:15',
          status: 'NORMAL'
        },
        {
          name: '内存使用率',
          value: '65',
          unit: '%',
          updateTime: '2023-06-28 10:30:15',
          status: 'WARNING'
        },
        {
          name: '存储空间',
          value: '85',
          unit: '%',
          updateTime: '2023-06-28 10:30:15',
          status: 'CRITICAL'
        },
        {
          name: '网络延迟',
          value: '15',
          unit: 'ms',
          updateTime: '2023-06-28 10:30:15',
          status: 'NORMAL'
        },
        {
          name: '温度',
          value: '42',
          unit: '°C',
          updateTime: '2023-06-28 10:30:15',
          status: 'WARNING'
        }
      ]
      metricsLoading.value = false
    }, 500)
  } catch (error) {
    console.error('获取设备指标失败', error)
    metricsLoading.value = false
  }
}

// Mock数据：刷新设备日志
const refreshLogs = async () => {
  logsLoading.value = true
  try {
    // 模拟API调用
    setTimeout(() => {
      deviceLogs.value = [
        {
          timestamp: '2023-06-28 10:30:15',
          type: '系统',
          message: '设备重启',
          operator: '系统',
          level: 'INFO'
        },
        {
          timestamp: '2023-06-28 09:15:20',
          type: '操作',
          message: '修改设备参数',
          operator: 'admin',
          level: 'INFO'
        },
        {
          timestamp: '2023-06-28 08:45:10',
          type: '警告',
          message: '设备温度过高',
          operator: '系统',
          level: 'WARNING'
        },
        {
          timestamp: '2023-06-27 22:30:00',
          type: '错误',
          message: '网络连接断开',
          operator: '系统',
          level: 'ERROR'
        },
        {
          timestamp: '2023-06-27 18:20:30',
          type: '调试',
          message: '执行连接测试',
          operator: 'admin',
          level: 'DEBUG'
        }
      ]
      logsTotal.value = 25
      logsLoading.value = false
    }, 500)
  } catch (error) {
    console.error('获取设备日志失败', error)
    logsLoading.value = false
  }
}

// 日志分页大小变化
const handleLogsSizeChange = (size) => {
  logsQueryParams.size = size
  refreshLogs()
}

// 日志页码变化
const handleLogsCurrentChange = (page) => {
  logsQueryParams.page = page
  refreshLogs()
}

// 检测连接
const handleCheckConnection = () => {
  ElMessage({
    type: 'info',
    message: `正在检测设备 ${device.name} 的连接状态...`
  })
  // 模拟检测过程
  setTimeout(() => {
    ElMessage({
      type: 'success',
      message: `设备 ${device.name} 连接正常`
    })
  }, 1500)
}

// 删除设备
const handleDelete = () => {
  ElMessageBox.confirm(
    `确定要删除设备"${device.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(() => {
    // 模拟删除操作
    setTimeout(() => {
      ElMessage({
        type: 'success',
        message: `设备"${device.name}"已删除`
      })
      router.push('/device')
    }, 500)
  }).catch(() => {})
}

// 生命周期钩子
onMounted(() => {
  fetchDeviceDetail(deviceId)
  refreshMetrics()
  refreshLogs()
})
</script>

<style scoped>
.device-detail-container {
  padding: 6px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-title {
  font-size: 18px;
  font-weight: bold;
}

.device-header {
  display: flex;
  align-items: center;
  margin-bottom: 20px;
}

.device-icon {
  width: 80px;
  height: 80px;
  background-color: #f8f9fa;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  margin-right: 20px;
  color: var(--el-color-primary);
}

.device-title h2 {
  margin: 0;
  font-size: 22px;
}

.device-id {
  color: #909399;
  font-size: 14px;
  margin-top: 5px;
}

.device-status {
  margin-top: 10px;
}

.last-heartbeat {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.device-actions {
  margin-bottom: 20px;
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.device-tabs {
  margin-top: 5px;
}

.tab-content {
  padding: 20px 0;
}

.device-info-card {
  margin-bottom: 20px;
}

.card-title {
  font-size: 16px;
  font-weight: bold;
}

.card-header-with-action {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-item {
  margin-right: 5px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 