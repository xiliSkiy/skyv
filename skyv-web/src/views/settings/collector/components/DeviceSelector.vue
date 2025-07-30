<template>
  <div class="device-selector">
    <div class="selector-header">
      <el-alert :closable="false" type="info">
        请从下列设备中选择需要分配给当前采集器的设备。已分配的设备将显示为选中状态。
      </el-alert>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="search"
        placeholder="搜索设备名称或IP"
        clearable
        @input="filterDevices"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <div class="filter-buttons">
        <el-button-group>
          <el-button 
            :type="deviceFilter === 'all' ? 'primary' : ''" 
            @click="deviceFilter = 'all'"
          >
            全部
          </el-button>
          <el-button 
            :type="deviceFilter === 'assigned' ? 'primary' : ''" 
            @click="deviceFilter = 'assigned'"
          >
            已分配
          </el-button>
          <el-button 
            :type="deviceFilter === 'unassigned' ? 'primary' : ''" 
            @click="deviceFilter = 'unassigned'"
          >
            未分配
          </el-button>
        </el-button-group>
      </div>
    </div>

    <!-- 设备列表 -->
    <div class="device-list" v-loading="loading">
      <el-table
        ref="deviceTableRef"
        :data="filteredDevices"
        @selection-change="handleSelectionChange"
        border
        style="width: 100%"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="deviceName" label="设备名称" min-width="150" show-overflow-tooltip />
        <el-table-column prop="ip" label="IP地址" width="140" show-overflow-tooltip />
        <el-table-column prop="deviceType" label="设备类型" width="120" align="center">
          <template #default="{ row }">
            <el-tag effect="light" size="small">{{ row.deviceType }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'info'" effect="dark" size="small">
              {{ row.status === 1 ? '在线' : '离线' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="collectorName" label="当前采集器" min-width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <span v-if="row.collectorId === collectorId">
              <el-tag type="success" size="small" effect="light">当前采集器</el-tag>
            </span>
            <span v-else-if="row.collectorId">{{ row.collectorName }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          background
          :current-page="page"
          :page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </div>

    <!-- 底部按钮 -->
    <div class="selector-footer">
      <el-button @click="$emit('cancel')">取消</el-button>
      <el-button type="primary" :disabled="selectedIds.length === 0" @click="handleAssignDevices" :loading="submitting">
        确认分配 ({{ selectedIds.length }})
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getDeviceList } from '@/api/device'
import { assignDevicesToCollector } from '@/api/collector'

const props = defineProps({
  collectorId: {
    type: [String, Number],
    required: true
  }
})

const emit = defineEmits(['assigned', 'cancel'])

// 状态变量
const loading = ref(false)
const submitting = ref(false)
const search = ref('')
const deviceFilter = ref('all')
const deviceList = ref([])
const selectedIds = ref([])
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)

// 表格引用
const deviceTableRef = ref(null)

// 根据筛选条件过滤设备
const filteredDevices = computed(() => {
  let result = [...deviceList.value]
  
  // 搜索过滤
  if (search.value) {
    const searchLower = search.value.toLowerCase()
    result = result.filter(item => 
      (item.deviceName && item.deviceName.toLowerCase().includes(searchLower)) || 
      (item.ip && item.ip.toLowerCase().includes(searchLower))
    )
  }
  
  // 按分配状态过滤
  if (deviceFilter.value === 'assigned') {
    result = result.filter(item => item.collectorId === props.collectorId)
  } else if (deviceFilter.value === 'unassigned') {
    result = result.filter(item => !item.collectorId || item.collectorId !== props.collectorId)
  }
  
  return result
})

// 监听筛选条件变化，重置选中状态
watch([search, deviceFilter], () => {
  if (deviceTableRef.value) {
    deviceTableRef.value.clearSelection()
    selectedIds.value = []
  }
})

// 加载设备列表
const fetchDevices = async () => {
  loading.value = true
  try {
    const res = await getDeviceList({
      page: page.value,
      limit: pageSize.value,
      collectorId: props.collectorId,
      includeAssigned: true
    })
    
    if (res && res.code === 200) {
      // 确保数据是数组类型
      deviceList.value = Array.isArray(res.data) ? res.data : []
      total.value = res.meta?.total || 0
      
      // 自动选中已分配给当前采集器的设备
      setTimeout(() => {
        if (deviceTableRef.value) {
          deviceList.value.forEach(row => {
            if (row.collectorId === props.collectorId) {
              deviceTableRef.value.toggleRowSelection(row, true)
            }
          })
        }
      }, 100)
    } else {
      ElMessage.error((res && res.message) || '获取设备列表失败')
      deviceList.value = []
    }
  } catch (error) {
    console.error('获取设备列表失败:', error)
    ElMessage.error('获取设备列表失败')
    deviceList.value = []
  } finally {
    loading.value = false
  }
}

// 搜索设备
const filterDevices = () => {
  if (deviceTableRef.value) {
    deviceTableRef.value.clearSelection()
  }
}

// 处理选择变化
const handleSelectionChange = (selection) => {
  selectedIds.value = selection.map(item => item.id)
}

// 分页事件
const handleSizeChange = (val) => {
  pageSize.value = val
  page.value = 1
  fetchDevices()
}

const handleCurrentChange = (val) => {
  page.value = val
  fetchDevices()
}

// 分配设备
const handleAssignDevices = async () => {
  if (selectedIds.value.length === 0) {
    ElMessage.warning('请选择需要分配的设备')
    return
  }
  
  submitting.value = true
  try {
    const res = await assignDevicesToCollector({
      collectorId: props.collectorId,
      deviceIds: selectedIds.value
    })
    
    if (res && res.code === 200) {
      ElMessage.success('设备分配成功')
      emit('assigned')
    } else {
      ElMessage.error((res && res.message) || '设备分配失败')
    }
  } catch (error) {
    console.error('设备分配失败:', error)
    ElMessage.error('设备分配失败')
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  fetchDevices()
})
</script>

<style scoped>
.device-selector {
  padding: 10px 0;
}

.selector-header {
  margin-bottom: 20px;
}

.search-bar {
  display: flex;
  margin-bottom: 15px;
  gap: 10px;
}

.search-bar .el-input {
  max-width: 240px;
}

.filter-buttons {
  margin-left: 10px;
}

.device-list {
  margin-bottom: 20px;
  min-height: 300px;
}

.pagination-container {
  margin: 15px 0;
  display: flex;
  justify-content: flex-end;
}

.selector-footer {
  display: flex;
  justify-content: flex-end;
  padding-top: 15px;
  border-top: 1px solid #EBEEF5;
}
</style> 