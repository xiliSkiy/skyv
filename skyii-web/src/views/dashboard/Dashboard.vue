<template>
  <div class="dashboard-container">
    <el-row :gutter="15">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-header">
            <div>
              <div class="card-title">设备总数</div>
              <div class="card-value">{{ stats.deviceTotal || 0 }}</div>
            </div>
            <el-icon class="card-icon" :size="40"><VideoCamera /></el-icon>
          </div>
          <div class="card-footer">
            <span>在线: {{ stats.deviceOnline || 0 }}</span>
            <span>离线: {{ stats.deviceOffline || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-header">
            <div>
              <div class="card-title">报警事件</div>
              <div class="card-value">{{ stats.alertTotal || 0 }}</div>
            </div>
            <el-icon class="card-icon" :size="40"><Bell /></el-icon>
          </div>
          <div class="card-footer">
            <span>待处理: {{ stats.alertPending || 0 }}</span>
            <span>已处理: {{ stats.alertHandled || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-header">
            <div>
              <div class="card-title">用户数量</div>
              <div class="card-value">{{ stats.userTotal || 0 }}</div>
            </div>
            <el-icon class="card-icon" :size="40"><User /></el-icon>
          </div>
          <div class="card-footer">
            <span>在线: {{ stats.userOnline || 0 }}</span>
            <span>活跃: {{ stats.userActive || 0 }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="dashboard-card">
          <div class="card-header">
            <div>
              <div class="card-title">系统状态</div>
              <div class="card-value">{{ stats.systemStatus || '正常' }}</div>
            </div>
            <el-icon class="card-icon" :size="40"><Connection /></el-icon>
          </div>
          <div class="card-footer">
            <span>CPU: {{ stats.cpuUsage || '0%' }}</span>
            <span>内存: {{ stats.memoryUsage || '0%' }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="15" class="mt-15">
      <el-col :xs="24" :lg="16">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-title">
              <span>报警趋势</span>
              <el-radio-group v-model="timeRange" size="small">
                <el-radio-button label="today">今日</el-radio-button>
                <el-radio-button label="week">本周</el-radio-button>
                <el-radio-button label="month">本月</el-radio-button>
              </el-radio-group>
            </div>
          </template>
          <div class="chart-container">
            <div class="placeholder-chart">
              <el-empty description="暂无数据" />
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :lg="8">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-title">
              <span>最近报警</span>
              <el-button type="primary" link size="small">查看全部</el-button>
            </div>
          </template>
          <div class="recent-alerts">
            <el-empty v-if="recentAlerts.length === 0" description="暂无报警" />
            <el-timeline v-else>
              <el-timeline-item
                v-for="(alert, index) in recentAlerts"
                :key="index"
                :type="getAlertTypeClass(alert.level)"
                :timestamp="alert.time"
                size="small"
              >
                {{ alert.content }}
              </el-timeline-item>
            </el-timeline>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="15" class="mt-15">
      <el-col :xs="24">
        <el-card shadow="hover">
          <template #header>
            <div class="card-header-title">
              <span>设备状态</span>
              <el-button type="primary" link size="small" @click="$router.push('/device')">
                查看全部
              </el-button>
            </div>
          </template>
          <el-table :data="deviceList" style="width: 100%" v-loading="loading">
            <el-table-column prop="name" label="设备名称" min-width="120" />
            <el-table-column prop="ip" label="IP地址" width="150" />
            <el-table-column prop="type" label="设备类型" width="120" />
            <el-table-column label="状态" width="100" align="center">
              <template #default="scope">
                <el-tag :type="getDeviceStatusType(scope.row.status)">
                  {{ getDeviceStatusText(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="lastActiveTime" label="最后活动时间" width="180" />
            <el-table-column label="操作" width="150" align="center">
              <template #default="scope">
                <el-button type="primary" link size="small" @click="$router.push(`/device/${scope.row.id}`)">
                  详情
                </el-button>
                <el-button type="primary" link size="small" @click="$router.push(`/monitoring?deviceId=${scope.row.id}`)">
                  监控
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'

// 加载状态
const loading = ref(false)

// 时间范围选择
const timeRange = ref('week')

// 统计数据
const stats = ref({
  deviceTotal: 18,
  deviceOnline: 15,
  deviceOffline: 3,
  alertTotal: 24,
  alertPending: 5,
  alertHandled: 19,
  userTotal: 12,
  userOnline: 8,
  userActive: 10,
  systemStatus: '正常',
  cpuUsage: '32%',
  memoryUsage: '45%'
})

// 最近报警
const recentAlerts = ref([
  { level: 'warning', time: '2023-11-20 10:23', content: '摄像头1离线' },
  { level: 'danger', time: '2023-11-20 09:15', content: '检测到异常行为' },
  { level: 'info', time: '2023-11-20 08:30', content: '设备状态恢复正常' },
  { level: 'warning', time: '2023-11-19 22:45', content: '摄像头3信号弱' }
])

// 设备列表
const deviceList = ref([
  { id: 1, name: '前门摄像头', ip: '192.168.1.101', type: '摄像头', status: 1, lastActiveTime: '2023-11-20 10:30:45' },
  { id: 2, name: '后门摄像头', ip: '192.168.1.102', type: '摄像头', status: 1, lastActiveTime: '2023-11-20 10:28:12' },
  { id: 3, name: '仓库摄像头', ip: '192.168.1.103', type: '摄像头', status: 0, lastActiveTime: '2023-11-19 18:45:22' },
  { id: 4, name: '办公室传感器', ip: '192.168.1.104', type: '传感器', status: 1, lastActiveTime: '2023-11-20 10:29:55' },
  { id: 5, name: '车库门禁', ip: '192.168.1.105', type: '门禁', status: 1, lastActiveTime: '2023-11-20 10:15:33' }
])

// 获取报警类型样式
const getAlertTypeClass = (level) => {
  const typeMap = {
    'info': 'info',
    'warning': 'warning',
    'danger': 'danger'
  }
  return typeMap[level] || 'info'
}

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
    2: '异常'
  }
  return textMap[status] || '未知'
}

// 初始化
onMounted(() => {
  // 这里可以添加获取实际数据的API调用
})
</script>

<style lang="scss" scoped>
.dashboard-container {
  padding: 6px;
}

.dashboard-card {
  height: 100%;
  border-radius: 6px;
  transition: all 0.3s;
  
  &:hover {
    transform: translateY(-2px);
    box-shadow: 0 5px 15px rgba(0, 0, 0, 0.08);
  }
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    
    .card-title {
      font-size: 14px;
      color: #909399;
      margin-bottom: 6px;
    }
    
    .card-value {
      font-size: 24px;
      font-weight: bold;
      color: #303133;
    }
    
    .card-icon {
      color: #409EFF;
      opacity: 0.8;
    }
  }
  
  .card-footer {
    margin-top: 10px;
    display: flex;
    justify-content: space-between;
    font-size: 13px;
    color: #606266;
  }
}

.mt-15 {
  margin-top: 10px;
}

.card-header-title {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 500;
}

.chart-container {
  height: 300px;
  
  .placeholder-chart {
    height: 100%;
    display: flex;
    align-items: center;
    justify-content: center;
  }
}

.recent-alerts {
  height: 300px;
  overflow-y: auto;
  padding-right: 4px;
  
  &::-webkit-scrollbar {
    width: 4px;
  }
  
  &::-webkit-scrollbar-thumb {
    background-color: rgba(144, 147, 153, 0.3);
    border-radius: 2px;
  }
  
  &::-webkit-scrollbar-track {
    background-color: transparent;
  }
}
</style> 