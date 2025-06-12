<template>
  <div class="device-detail-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">设备详情</div>
          <div class="header-actions">
            <el-button @click="$router.push('/device')">返回设备列表</el-button>
            <el-button type="primary" @click="$router.push(`/device/edit/${deviceId}`)">
              编辑设备
            </el-button>
          </div>
        </div>
      </template>

      <div v-loading="loading">
        <el-empty v-if="!device.id" description="未找到设备信息" />
        <template v-else>
          <!-- 设备基本信息 -->
          <el-descriptions title="基本信息" :column="3" border>
            <el-descriptions-item label="设备名称">{{ device.name }}</el-descriptions-item>
            <el-descriptions-item label="设备编码">{{ device.code }}</el-descriptions-item>
            <el-descriptions-item label="设备类型">{{ device.type }}</el-descriptions-item>
            <el-descriptions-item label="设备位置">{{ device.location || '-' }}</el-descriptions-item>
            <el-descriptions-item label="创建时间">{{ formatDateTime(device.createdAt) }}</el-descriptions-item>
            <el-descriptions-item label="更新时间">{{ formatDateTime(device.updatedAt) }}</el-descriptions-item>
            <el-descriptions-item label="设备状态">
              <el-tag :type="getDeviceStatusType(device.status)">
                {{ getDeviceStatusText(device.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="最后心跳时间">{{ formatDateTime(device.lastHeartbeatTime) || '-' }}</el-descriptions-item>
            <el-descriptions-item label="设备分组">{{ device.groupId || '默认分组' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 网络信息 -->
          <el-descriptions title="网络信息" :column="3" border class="mt-15">
            <el-descriptions-item label="IP地址">{{ device.ipAddress }}</el-descriptions-item>
            <el-descriptions-item label="端口">{{ device.port }}</el-descriptions-item>
            <el-descriptions-item label="连接状态">
              <el-tag :type="device.status === 1 ? 'success' : 'danger'">
                {{ device.status === 1 ? '已连接' : '未连接' }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="用户名">{{ device.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="密码">******</el-descriptions-item>
          </el-descriptions>

          <!-- 设备描述 -->
          <el-descriptions title="设备描述" :column="1" border class="mt-15">
            <el-descriptions-item>{{ device.description || '暂无描述' }}</el-descriptions-item>
          </el-descriptions>

          <!-- 操作按钮 -->
          <div class="action-buttons">
            <el-button type="primary" @click="handleCheckConnection">
              <el-icon><Connection /></el-icon>检测连接
            </el-button>
            <el-button type="primary" @click="$router.push(`/monitoring?deviceId=${deviceId}`)">
              <el-icon><VideoCamera /></el-icon>实时监控
            </el-button>
            <el-button type="danger" @click="handleDelete">
              <el-icon><Delete /></el-icon>删除设备
            </el-button>
          </div>
        </template>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceById, deleteDevice, checkDeviceConnection } from '@/api/device'
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()
const deviceId = route.params.id
const loading = ref(false)
const device = reactive({})

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

// 获取设备详情
const fetchDeviceDetail = async (deviceId) => {
  loading.value = true
  try {
    const res = await getDeviceById(deviceId)
    if (res.data) {
      Object.assign(device, res.data)
    }
  } catch (error) {
    console.error('获取设备详情失败', error)
    ElMessage.error('获取设备详情失败')
  } finally {
    loading.value = false
  }
}

// 检测连接
const handleCheckConnection = async () => {
  try {
    loading.value = true
    const res = await checkDeviceConnection(deviceId)
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

// 删除设备
const handleDelete = () => {
  ElMessageBox.confirm(`确定要删除设备 ${device.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDevice(deviceId)
      ElMessage.success('删除成功')
      router.push('/device')
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  fetchDeviceDetail(deviceId)
})
</script>

<style lang="scss" scoped>
.device-detail-container {
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

.mt-15 {
  margin-top: 15px;
}

.action-buttons {
  margin-top: 20px;
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style> 