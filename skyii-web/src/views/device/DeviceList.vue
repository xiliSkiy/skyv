<template>
  <div class="device-list-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">设备管理</div>
          <div class="header-actions">
            <el-button type="primary" @click="$router.push('/device/add')">
              <el-icon><Plus /></el-icon>添加设备
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :model="queryParams" ref="queryForm" :inline="true" class="search-form">
        <el-form-item label="设备名称" prop="name">
          <el-input v-model="queryParams.name" placeholder="请输入设备名称" clearable />
        </el-form-item>
        <el-form-item label="设备类型" prop="type">
          <el-select v-model="queryParams.type" placeholder="请选择设备类型" clearable>
            <el-option label="摄像头" value="摄像头" />
            <el-option label="传感器" value="传感器" />
            <el-option label="门禁" value="门禁" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="设备状态" prop="status">
          <el-select v-model="queryParams.status" placeholder="请选择设备状态" clearable>
            <el-option label="在线" :value="1" />
            <el-option label="离线" :value="0" />
            <el-option label="故障" :value="2" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>

      <!-- 状态统计 -->
      <div class="status-stats">
        <el-tag class="status-tag" effect="plain">设备总数: {{ deviceStats.total || 0 }}</el-tag>
        <el-tag class="status-tag" type="success" effect="plain">在线: {{ deviceStats.online || 0 }}</el-tag>
        <el-tag class="status-tag" type="danger" effect="plain">离线: {{ deviceStats.offline || 0 }}</el-tag>
        <el-tag class="status-tag" type="warning" effect="plain">故障: {{ deviceStats.fault || 0 }}</el-tag>
      </div>

      <!-- 设备列表 -->
      <el-table
        v-loading="loading"
        :data="deviceList"
        border
        stripe
        style="width: 100%"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" align="center" />
        <el-table-column prop="name" label="设备名称" min-width="120" show-overflow-tooltip />
        <el-table-column prop="code" label="设备编码" width="120" show-overflow-tooltip />
        <el-table-column prop="type" label="设备类型" width="100" align="center" />
        <el-table-column prop="ipAddress" label="IP地址" width="120" show-overflow-tooltip />
        <el-table-column prop="location" label="设备位置" width="120" show-overflow-tooltip />
        <el-table-column label="设备状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getDeviceStatusType(scope.row.status)">
              {{ getDeviceStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="最后心跳时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.lastHeartbeatTime) }}
          </template>
        </el-table-column>
        <el-table-column label="创建时间" width="180" align="center">
          <template #default="scope">
            {{ formatDateTime(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" align="center" fixed="right">
          <template #default="scope">
            <el-button type="primary" link @click="handleDetail(scope.row)">
              详情
            </el-button>
            <el-button type="primary" link @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="primary" link @click="handleCheck(scope.row)">
              检测
            </el-button>
            <el-button type="danger" link @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getDeviceList, getDeviceStatusStats, deleteDevice, checkDeviceConnection } from '@/api/device'
import { formatDateTime } from '@/utils/date'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 设备列表数据
const deviceList = ref([])

// 总记录数
const total = ref(0)

// 选中的设备
const selectedDevices = ref([])

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
  size: 10,
  name: '',
  type: '',
  status: '',
  sort: 'id',
  direction: 'desc'
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

// 查询设备列表
const getList = async () => {
  loading.value = true
  try {
    const res = await getDeviceList(queryParams)
    deviceList.value = res.data || []
    total.value = res.meta?.total || 0
  } catch (error) {
    console.error('获取设备列表失败', error)
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
  queryParams.type = ''
  queryParams.status = ''
  handleQuery()
}

// 选择变更
const handleSelectionChange = (selection) => {
  selectedDevices.value = selection
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

// 查看详情
const handleDetail = (row) => {
  router.push(`/device/detail/${row.id}`)
}

// 编辑设备
const handleEdit = (row) => {
  router.push(`/device/edit/${row.id}`)
}

// 检测连接
const handleCheck = async (row) => {
  try {
    loading.value = true
    const res = await checkDeviceConnection(row.id)
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
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除设备 ${row.name} 吗?`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await deleteDevice(row.id)
      ElMessage.success('删除成功')
      getList()
      getDeviceStats()
    } catch (error) {
      ElMessage.error('删除失败')
    }
  }).catch(() => {})
}

// 初始化
onMounted(() => {
  getList()
  getDeviceStats()
})
</script>

<style lang="scss" scoped>
.device-list-container {
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

.search-form {
  margin-bottom: 15px;
}

.status-stats {
  margin-bottom: 15px;
  
  .status-tag {
    margin-right: 10px;
  }
}

.pagination-container {
  margin-top: 15px;
  text-align: right;
}
</style> 