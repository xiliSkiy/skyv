<template>
  <div class="device-list-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">设备管理</div>
          <div class="header-actions">
            <el-dropdown>
              <el-button type="primary">
                <el-icon><Plus /></el-icon>添加设备
                <el-icon class="el-icon--right"><ArrowDown /></el-icon>
              </el-button>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item @click="$router.push('/device/add')">手动添加</el-dropdown-item>
                  <el-dropdown-item>从模板创建</el-dropdown-item>
                  <el-dropdown-item>批量导入</el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </div>
        </div>
      </template>

      <!-- 搜索区域 -->
      <el-form :model="queryParams" ref="queryForm" :inline="false" class="search-form filter-section">
        <el-row :gutter="20">
          <el-col :span="6">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="queryParams.name" placeholder="请输入设备名称" clearable />
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="设备类型" prop="type">
              <el-select v-model="queryParams.type" placeholder="全部类型" clearable style="width: 100%">
                <el-option label="摄像头" value="CAMERA" />
                <el-option label="传感器" value="SENSOR" />
                <el-option label="门禁" value="ACCESS" />
                <el-option label="其他" value="OTHER" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="设备状态" prop="status">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="在线" :value="1" />
                <el-option label="离线" :value="0" />
                <el-option label="故障" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="6">
            <el-form-item label="快速创建" prop="template">
              <el-select v-model="queryParams.template" placeholder="请选择模板" clearable style="width: 100%">
                <el-option label="会议室摄像头" :value="1" />
                <el-option label="温湿度传感器" :value="2" />
                <el-option label="门禁控制器" :value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <!-- 高级筛选 -->
        <el-collapse v-model="activeCollapse">
          <el-collapse-item name="advanced" class="advanced-filter">
            <template #title>
              <span><el-icon><Filter /></el-icon> 高级筛选</span>
            </template>
            <el-row :gutter="20">
              <el-col :span="6">
                <el-form-item label="所属区域" prop="area">
                  <el-select v-model="queryParams.area" placeholder="全部区域" clearable style="width: 100%">
                    <el-option label="北区" value="NORTH" />
                    <el-option label="南区" value="SOUTH" />
                    <el-option label="东区" value="EAST" />
                    <el-option label="西区" value="WEST" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="设备分组" prop="group">
                  <el-select v-model="queryParams.group" placeholder="全部分组" clearable style="width: 100%">
                    <el-option label="安防监控" :value="1" />
                    <el-option label="环境监测" :value="2" />
                    <el-option label="门禁管理" :value="3" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="设备标签" prop="tags">
                  <el-select v-model="queryParams.tags" placeholder="选择标签" clearable multiple style="width: 100%">
                    <el-option label="重要" value="IMPORTANT" />
                    <el-option label="室外" value="OUTDOOR" />
                    <el-option label="室内" value="INDOOR" />
                    <el-option label="测试" value="TEST" />
                  </el-select>
                </el-form-item>
              </el-col>
              <el-col :span="6">
                <el-form-item label="设备协议" prop="protocol">
                  <el-select v-model="queryParams.protocol" placeholder="全部协议" clearable style="width: 100%">
                    <el-option label="RTSP" value="RTSP" />
                    <el-option label="ONVIF" value="ONVIF" />
                    <el-option label="MODBUS" value="MODBUS" />
                    <el-option label="HTTP" value="HTTP" />
                  </el-select>
                </el-form-item>
              </el-col>
            </el-row>
          </el-collapse-item>
        </el-collapse>

        <div class="filter-actions">
          <el-button @click="resetQuery">重置</el-button>
          <el-button type="primary" @click="handleQuery">搜索</el-button>
        </div>
      </el-form>

      <!-- 设备统计 -->
      <div class="device-stats">
        <div class="device-stat-item">
          设备总数: <el-tag effect="plain">{{ deviceStats.total || 0 }}</el-tag>
        </div>
        <div class="device-stat-item">
          在线: <el-tag type="success" effect="plain">{{ deviceStats.online || 0 }}</el-tag>
        </div>
        <div class="device-stat-item">
          离线: <el-tag type="danger" effect="plain">{{ deviceStats.offline || 0 }}</el-tag>
        </div>
        <div class="device-stat-item">
          故障: <el-tag type="warning" effect="plain">{{ deviceStats.fault || 0 }}</el-tag>
        </div>
      </div>

      <!-- 视图切换 -->
      <div class="view-toggle">
        <el-radio-group v-model="viewType" size="small">
          <el-radio-button label="table">
            <el-icon><List /></el-icon> 列表
          </el-radio-button>
          <el-radio-button label="map">
            <el-icon><Location /></el-icon> 地图
          </el-radio-button>
          <el-radio-button label="chart">
            <el-icon><PieChart /></el-icon> 统计
          </el-radio-button>
        </el-radio-group>
        <div class="view-actions">
          <el-tooltip content="刷新数据">
            <el-button link type="primary" :icon="Refresh" circle @click="getList" />
          </el-tooltip>
          <el-dropdown>
            <el-button link type="primary" :icon="MoreFilled" circle />
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item>批量启用</el-dropdown-item>
                <el-dropdown-item>批量禁用</el-dropdown-item>
                <el-dropdown-item divided>批量删除</el-dropdown-item>
                <el-dropdown-item>导出设备</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 表格视图 -->
      <div v-show="viewType === 'table'" class="table-view">
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
          <el-table-column prop="type" label="设备类型" width="100" align="center">
            <template #default="{ row }">
              {{ getDeviceTypeText(row.type) }}
            </template>
          </el-table-column>
          <el-table-column prop="ipAddress" label="IP地址" width="120" show-overflow-tooltip />
          <el-table-column prop="area" label="所属区域" width="100" align="center" />
          <el-table-column label="设备标签" width="150">
            <template #default="{ row }">
              <div class="device-tags">
                <el-tag v-for="tag in row.tags" :key="tag" size="small" effect="plain" class="tag-item">
                  {{ tag }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
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
          <el-table-column label="操作" width="230" align="center" fixed="right">
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
      </div>

      <!-- 地图视图 -->
      <div v-show="viewType === 'map'" class="map-view">
        <div id="device-map" style="height: 500px">
          <!-- 地图视图将在此渲染 -->
          <el-empty description="地图加载中..." />
        </div>
      </div>

      <!-- 统计视图 -->
      <div v-show="viewType === 'chart'" class="chart-view">
        <div id="device-chart" style="height: 500px">
          <!-- 统计图表将在此渲染 -->
          <el-empty description="图表加载中..." />
        </div>
      </div>

      <!-- 分页 -->
      <div v-show="viewType === 'table'" class="pagination-container">
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
import { 
  getDeviceList, 
  getDeviceStatsData as fetchDeviceStatsData, 
  deleteDevice, 
  batchDeleteDevices, 
  testDeviceConnection 
} from '@/api/device'
import { formatDateTime } from '@/utils/date'
import { 
  Plus, ArrowDown, Filter, List, Location, PieChart, 
  Refresh, MoreFilled
} from '@element-plus/icons-vue'

const router = useRouter()

// 加载状态
const loading = ref(false)

// 高级筛选展开状态
const activeCollapse = ref([])

// 视图类型：table, map, chart
const viewType = ref('table')

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
  template: '',
  area: '',
  group: '',
  tags: [],
  protocol: '',
  sort: 'id',
  direction: 'desc'
})

// 获取设备状态类型
const getDeviceStatusType = (status) => {
  const typeMap = {
    1: 'success',  // 在线
    2: 'danger',   // 离线
    3: 'warning',  // 故障
    4: 'info'      // 维护
  }
  return typeMap[status] || 'info'
}

// 获取设备状态文本
const getDeviceStatusText = (status) => {
  const textMap = {
    1: '在线',
    2: '离线', 
    3: '故障',
    4: '维护'
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

// 获取设备列表
const getList = async () => {
  try {
    loading.value = true
    
    // 构建查询参数
    const params = {
      page: queryParams.page,
      size: queryParams.size,
      sortBy: queryParams.sort,
      sortDir: queryParams.direction
    }
    
    // 添加查询条件
    if (queryParams.name) params.name = queryParams.name
    if (queryParams.type) params.deviceTypeId = queryParams.type
    if (queryParams.status !== '') params.status = queryParams.status
    if (queryParams.area) params.areaId = queryParams.area
    if (queryParams.group) params.groupId = queryParams.group
    if (queryParams.protocol) params.protocol = queryParams.protocol

    // 调用API
    const response = await getDeviceList(params)
    
    if (response.code === 200) {
      deviceList.value = response.data.content || []
      total.value = response.data.totalElements || 0
    } else {
      ElMessage.error(response.message || '获取设备列表失败')
      deviceList.value = []
      total.value = 0
    }
  } catch (error) {
    console.error('获取设备列表失败', error)
    ElMessage.error('获取设备列表失败：' + (error.message || '网络错误'))
    deviceList.value = []
    total.value = 0
  } finally {
    loading.value = false
  }
}

// 获取设备状态统计
const getDeviceStatsData = async () => {
  try {
    const response = await fetchDeviceStatsData()
    
    if (response.code === 200) {
      const stats = response.data
      deviceStats.online = stats.online || 0
      deviceStats.offline = stats.offline || 0
      deviceStats.fault = stats.fault || 0
      deviceStats.total = stats.total || 0
    } else {
      ElMessage.error(response.message || '获取设备统计失败')
    }
  } catch (error) {
    console.error('获取设备状态统计失败', error)
    ElMessage.error('获取设备统计失败：' + (error.message || '网络错误'))
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
  queryParams.template = ''
  queryParams.area = ''
  queryParams.group = ''
  queryParams.tags = []
  queryParams.protocol = ''
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
    ElMessage({
      type: 'info',
      message: `正在检测设备 ${row.name} 的连接状态...`
    })
    
    const response = await testDeviceConnection(row.id)
    
    if (response.code === 200) {
      const result = response.data
      if (result.connected) {
        ElMessage({
          type: 'success',
          message: `设备 ${row.name} 连接正常`
        })
        // 更新设备状态
        row.status = 1
      } else {
        ElMessage({
          type: 'error',
          message: `设备 ${row.name} 连接失败：${result.message || '连接超时'}`
        })
        row.status = 2
      }
    } else {
      ElMessage.error(response.message || '连接测试失败')
    }
  } catch (error) {
    console.error('设备连接测试失败', error)
    ElMessage.error('连接测试失败：' + (error.message || '网络错误'))
  }
}

// 删除设备
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除设备 "${row.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteDevice(row.id)
    
    if (response.code === 200) {
      ElMessage.success('删除成功')
      await getList() // 重新加载列表
      await getDeviceStatsData() // 重新加载统计
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除设备失败', error)
      ElMessage.error('删除失败：' + (error.message || '网络错误'))
    }
  }
}

// 批量删除设备
const handleBatchDelete = async () => {
  if (selectedDevices.value.length === 0) {
    ElMessage.warning('请选择要删除的设备')
    return
  }
  
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedDevices.value.length} 个设备吗？`,
      '确认批量删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const ids = selectedDevices.value.map(device => device.id)
    const response = await batchDeleteDevices(ids)
    
    if (response.code === 200) {
      ElMessage.success(`成功删除 ${selectedDevices.value.length} 个设备`)
      selectedDevices.value = []
      await getList() // 重新加载列表
      await getDeviceStatsData() // 重新加载统计
    } else {
      ElMessage.error(response.message || '批量删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除设备失败', error)
      ElMessage.error('批量删除失败：' + (error.message || '网络错误'))
    }
  }
}

// 组件挂载时加载数据
onMounted(async () => {
  await Promise.all([
    getList(),
    getDeviceStatsData()
  ])
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.header-title {
  font-size: 18px;
  font-weight: bold;
}
.search-form {
  margin-bottom: 20px;
  padding: 20px;
  background-color: #f8f9fa;
  border-radius: 4px;
}
.filter-section .el-row {
  margin-bottom: 10px;
}
.advanced-filter {
  border-top: 1px dashed #ddd;
  padding-top: 15px;
  margin-top: 5px;
}
.filter-actions {
  display: flex;
  justify-content: flex-end;
  margin-top: 15px;
}
.device-stats {
  display: flex;
  flex-wrap: wrap;
  gap: 20px;
  margin-bottom: 20px;
  padding: 10px 15px;
  background-color: #fff;
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,.05);
}
.device-stat-item {
  display: flex;
  align-items: center;
  gap: 5px;
}
.view-toggle {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}
.view-actions {
  display: flex;
  gap: 10px;
}
.tag-item {
  margin: 2px;
}
.device-tags {
  display: flex;
  flex-wrap: wrap;
}
.map-view, .chart-view {
  margin-bottom: 20px;
  border-radius: 4px;
  overflow: hidden;
}
.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style> 