<template>
  <div class="task-create-device-container">
    <!-- 标题区域 -->
    <div class="d-flex justify-content-between align-items-center mb-4">
      <div>
        <h4>创建采集任务</h4>
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/dashboard' }">控制台</el-breadcrumb-item>
          <el-breadcrumb-item :to="{ path: '/task' }">采集任务调度</el-breadcrumb-item>
          <el-breadcrumb-item>创建任务</el-breadcrumb-item>
        </el-breadcrumb>
      </div>
      <div class="action-buttons">
        <el-button plain size="small" @click="saveDraft" class="me-2">
          <el-icon><Document /></el-icon> 保存草稿
        </el-button>
        <el-button plain size="small" @click="cancel" type="danger">
          <el-icon><Close /></el-icon> 取消
        </el-button>
      </div>
    </div>

    <!-- 步骤指示器 -->
    <div class="step-indicator mb-4">
      <el-steps :active="currentStep" finish-status="success">
        <el-step title="基本信息" />
        <el-step title="设备选择" />
        <el-step title="指标配置" />
        <el-step title="调度设置" />
      </el-steps>
    </div>

    <!-- 设备筛选栏 -->
    <el-card class="device-filter-bar mb-4">
      <el-form :model="queryParams" label-width="100px" size="default">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6" :lg="6">
            <el-form-item label="设备类型">
              <el-select v-model="queryParams.deviceType" placeholder="全部类型" clearable style="width: 100%">
                <el-option label="全部类型" value="" />
                <el-option label="摄像头" value="camera" />
                <el-option label="传感器" value="sensor" />
                <el-option label="控制器" value="controller" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="6">
            <el-form-item label="设备位置">
              <el-select v-model="queryParams.location" placeholder="全部位置" clearable style="width: 100%">
                <el-option label="全部位置" value="" />
                <el-option label="入口区域" value="entrance" />
                <el-option label="停车场" value="parking" />
                <el-option label="走廊" value="corridor" />
                <el-option label="办公区" value="office" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="6">
            <el-form-item label="设备状态">
              <el-select v-model="queryParams.status" placeholder="全部状态" clearable style="width: 100%">
                <el-option label="全部状态" value="" />
                <el-option label="在线" value="online" />
                <el-option label="离线" value="offline" />
                <el-option label="维护中" value="maintenance" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6" :lg="6">
            <el-form-item label="搜索设备">
              <el-input
                v-model="queryParams.keyword"
                placeholder="设备名称/ID"
                clearable
              >
                <template #append>
                  <el-button :icon="Search" @click="handleQuery" />
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </el-card>

    <!-- 选中设备计数 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div>
        <span class="me-2">当前筛选结果: <strong>{{ total }}</strong> 台设备</span>
      </div>
      <div class="d-flex align-items-center">
        <span class="me-2">已选择</span>
        <span class="selected-count">{{ selectedDevices.length }}</span>
        <el-button type="text" @click="clearSelection" class="clear-selection-btn">清除选择</el-button>
      </div>
    </div>

    <!-- 设备列表 -->
    <el-card>
      <template #header>
        <div class="d-flex justify-content-between align-items-center">
          <div class="card-header-title">
            <el-icon><VideoCamera /></el-icon> 设备列表
          </div>
          <div>
            <el-radio-group v-model="viewMode" size="small" class="view-mode-toggle">
              <el-radio-button label="grid">
                <el-icon><Grid /></el-icon>
              </el-radio-button>
              <el-radio-button label="list">
                <el-icon><List /></el-icon>
              </el-radio-button>
            </el-radio-group>
          </div>
        </div>
      </template>

      <div v-loading="loading" class="device-list-container">
        <!-- 网格视图 -->
        <div v-if="viewMode === 'grid'" class="device-grid">
          <div v-for="device in deviceList" :key="device.id" class="mb-4">
            <div 
              class="device-card" 
              :class="{ selected: isDeviceSelected(device.id) }"
              @click="toggleDeviceSelection(device)"
            >
              <div class="device-status" :class="'status-' + device.status">
                {{ getStatusLabel(device.status) }}
              </div>
              <el-image 
                class="device-image" 
                :src="getDeviceImage(device)" 
                fit="cover"
                :preview-src-list="[getDeviceImage(device)]"
              >
                <template #error>
                  <div class="device-image-placeholder">
                    <el-icon><Picture /></el-icon>
                  </div>
                </template>
              </el-image>
              <div class="device-info">
                <div class="device-id small text-muted mb-2">ID: {{ device.deviceCode || device.id || '未知ID' }}</div>
                <h6 class="device-name mb-1">{{ device.deviceName || '未命名设备' }}</h6>
                <div class="d-flex justify-content-between align-items-center">
                  <el-tag size="small" :type="getDeviceTypeTagType(device.deviceType)">
                    {{ getDeviceTypeLabel(device.deviceType) }}
                  </el-tag>
                  <span class="small text-muted">{{ device.location || '未设置位置' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 列表视图 -->
        <div v-else>
          <el-table
            :data="deviceList"
            @selection-change="handleSelectionChange"
            style="width: 100%"
            ref="deviceTableRef"
          >
            <el-table-column type="selection" width="50" />
            <el-table-column label="设备名称" min-width="150">
              <template #default="{ row }">
                <div class="device-name">{{ row.deviceName }}</div>
              </template>
            </el-table-column>
            <el-table-column label="设备ID" min-width="120">
              <template #default="{ row }">
                <div class="device-id">{{ row.deviceCode }}</div>
              </template>
            </el-table-column>
            <el-table-column label="类型" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="getDeviceTypeTagType(row.deviceType)">
                  {{ getDeviceTypeLabel(row.deviceType) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="120">
              <template #default="{ row }">
                <el-tag size="small" :type="getStatusTagType(row.status)" effect="light">
                  {{ getStatusLabel(row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="位置" min-width="120">
              <template #default="{ row }">
                {{ row.location }}
              </template>
            </el-table-column>
            <el-table-column label="IP地址" min-width="120">
              <template #default="{ row }">
                {{ row.ipAddress }}
              </template>
            </el-table-column>
          </el-table>
        </div>

        <!-- 分页 -->
        <div class="pagination-container">
          <el-pagination
            v-model:current-page="queryParams.pageNum"
            v-model:page-size="queryParams.pageSize"
            :page-sizes="[8, 16, 24, 32]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="total"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </div>
    </el-card>

    <!-- 操作按钮 -->
    <div class="action-footer d-flex justify-content-end mt-4">
      <el-button @click="prevStep" class="me-2">
        <el-icon class="el-icon--left"><ArrowLeft /></el-icon> 上一步
      </el-button>
      <el-button type="primary" @click="nextStep">
        下一步 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Close, VideoCamera, Search, Picture, Grid, List, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { getAvailableDevices, saveTaskDraft } from '@/api/task'

const router = useRouter()

// 当前步骤
const currentStep = ref(1)

// 编辑模式标识
const isEdit = ref(false)
const taskId = ref(null)

// 设备列表
const deviceList = ref([])
const total = ref(0)
const loading = ref(false)
const viewMode = ref('grid')
const selectedDevices = ref([])
const savedSelectedDevices = ref([]) // 保存从编辑模式获取的设备列表
const deviceTableRef = ref(null) // 表格引用

// 查询参数
const queryParams = reactive({
  pageNum: 1,
  pageSize: 8,
  deviceType: '',
  location: '',
  status: '',
  keyword: ''
})

// 获取设备列表
const getDeviceList = async () => {
  loading.value = true
  try {
    const res = await getAvailableDevices(queryParams)
    console.log('设备数据:', res.data)
    deviceList.value = res.data.content || []
    total.value = res.data.totalElements || 0
    
    // 设备列表加载完成后，处理已保存的设备选择
    nextTick(() => {
      restoreSavedDeviceSelection()
    })
  } catch (error) {
    console.error('获取设备列表失败', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleQuery = () => {
  queryParams.pageNum = 1
  getDeviceList()
}

// 处理页面大小变化
const handleSizeChange = (val) => {
  queryParams.pageSize = val
  getDeviceList()
}

// 处理页码变化
const handleCurrentChange = (val) => {
  queryParams.pageNum = val
  getDeviceList()
}

// 处理表格选择变化
const handleSelectionChange = (selection) => {
  selectedDevices.value = selection
}

// 切换设备选择状态（网格视图）
const toggleDeviceSelection = (device) => {
  const index = selectedDevices.value.findIndex(d => d.id === device.id)
  if (index === -1) {
    selectedDevices.value.push(device)
  } else {
    selectedDevices.value.splice(index, 1)
  }
}

// 检查设备是否被选中
const isDeviceSelected = (deviceId) => {
  return selectedDevices.value.some(device => device.id === deviceId)
}

// 清除选择
const clearSelection = () => {
  selectedDevices.value = []
}

// 获取设备图片
const getDeviceImage = (device) => {
  if (device.imageUrl) {
    return device.imageUrl
  }
  // 使用本地资源而不是外部URL
  const typeMap = {
    camera: '/images/device-camera.png',
    sensor: '/images/device-sensor.png',
    controller: '/images/device-controller.png'
  }
  return typeMap[device.deviceType] || '/images/device-default.png'
}

// 获取设备类型标签类型
const getDeviceTypeTagType = (type) => {
  const map = {
    camera: 'primary',
    sensor: 'info',
    controller: 'warning'
  }
  return map[type] || ''
}

// 获取设备类型标签文本
const getDeviceTypeLabel = (type) => {
  const map = {
    camera: '摄像头',
    sensor: '传感器',
    controller: '控制器'
  }
  return map[type] || type
}

// 获取状态标签类型
const getStatusTagType = (status) => {
  const map = {
    online: 'success',
    offline: 'danger',
    maintenance: 'warning'
  }
  return map[status] || ''
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  const map = {
    online: '在线',
    offline: '离线',
    maintenance: '维护中'
  }
  return map[status] || status
}

// 上一步
const prevStep = () => {
  // 保存当前选择的设备
  saveCurrentStepData()
  
  // 根据是否为编辑模式决定跳转路径
  if (isEdit.value && taskId.value) {
    router.push(`/task/edit/${taskId.value}`)
  } else {
    router.push('/task/create')
  }
}

// 下一步
const nextStep = () => {
  if (selectedDevices.value.length === 0) {
    ElMessage.warning('请至少选择一个设备')
    return
  }

  // 保存当前选择的设备
  saveCurrentStepData()
  router.push('/task/create/metrics')
}

// 保存当前步骤数据
const saveCurrentStepData = () => {
  // 从本地存储获取之前的数据
  const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  
  // 合并数据并保存
  const currentData = {
    ...prevData,
    devices: selectedDevices.value.map(device => {
      // 确保设备类型是正确的值（camera, sensor, controller）
      let deviceType = device.deviceType;
      if (deviceType === '摄像头' || deviceType.includes('camera')) {
        deviceType = 'camera';
      } else if (deviceType === '传感器' || deviceType.includes('sensor')) {
        deviceType = 'sensor';
      } else if (deviceType === '控制器' || deviceType.includes('controller')) {
        deviceType = 'controller';
      }
      
      return {
        deviceId: device.id,
        deviceCode: device.deviceCode,
        deviceName: device.deviceName,
        deviceType: deviceType
      };
    }),
    step: 2
  }
  
  localStorage.setItem('taskCreateData', JSON.stringify(currentData))
}

// 保存草稿
const saveDraft = async () => {
  try {
    // 从本地存储获取之前的数据
    const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
    
    // 合并数据
    const draftData = {
      ...prevData,
      devices: selectedDevices.value.map(device => {
        // 确保设备类型是正确的值（camera, sensor, controller）
        let deviceType = device.deviceType;
        if (deviceType === '摄像头' || deviceType.includes('camera')) {
          deviceType = 'camera';
        } else if (deviceType === '传感器' || deviceType.includes('sensor')) {
          deviceType = 'sensor';
        } else if (deviceType === '控制器' || deviceType.includes('controller')) {
          deviceType = 'controller';
        }
        
        return {
          deviceId: device.id,
          deviceCode: device.deviceCode,
          deviceName: device.deviceName,
          deviceType: deviceType
        };
      }),
      step: 1
    }
    
    const res = await saveTaskDraft(draftData)
    ElMessage.success('草稿保存成功')
    
    // 保存草稿ID，用于后续恢复
    if (res.data && res.data.draftId) {
      localStorage.setItem('taskDraftId', res.data.draftId)
    }
  } catch (error) {
    console.error('保存草稿失败', error)
    ElMessage.error('保存草稿失败')
  }
}

// 取消
const cancel = () => {
  ElMessage.info('已取消创建任务')
  router.push('/task')
}

// 恢复之前的选择
const restorePreviousSelection = () => {
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      if (data.devices && Array.isArray(data.devices)) {
        // 将存储的设备ID与当前列表匹配
        const storedDeviceIds = data.devices.map(d => d.deviceId)
        selectedDevices.value = deviceList.value.filter(device => 
          storedDeviceIds.includes(device.id)
        )
      }
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }
}

// 修复本地存储中的设备数据格式
const fixLocalStorageDeviceData = () => {
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      if (data.devices && Array.isArray(data.devices)) {
        // 检查并修复设备数据格式
        const fixedDevices = data.devices.map(device => {
          // 如果有id但没有deviceId，则将id转为deviceId
          if (device.id && !device.deviceId) {
            return {
              ...device,
              deviceId: device.id
            }
          }
          return device
        })
        
        // 更新本地存储
        localStorage.setItem('taskCreateData', JSON.stringify({
          ...data,
          devices: fixedDevices
        }))
      }
    } catch (error) {
      console.error('修复本地存储数据失败', error)
    }
  }
}

// 恢复已保存的设备选择
const restoreSavedDeviceSelection = () => {
  if (!savedSelectedDevices.value || savedSelectedDevices.value.length === 0) return;
  
  console.log('正在恢复已保存的设备选择...')
  
  // 如果是表格视图，需要通过ref获取表格实例并调用toggleRowSelection方法
  if (viewMode.value === 'list' && deviceTableRef.value) {
    deviceList.value.forEach(device => {
      const isSaved = savedSelectedDevices.value.some(savedDevice => 
        savedDevice.id === device.id || 
        savedDevice.deviceId === device.id ||
        savedDevice.deviceCode === device.deviceCode
      );
      if (isSaved) {
        deviceTableRef.value.toggleRowSelection(device, true);
      }
    });
  } else {
    // 如果是网格视图，直接更新selectedDevices数组
    const matchedDevices = deviceList.value.filter(device => 
      savedSelectedDevices.value.some(savedDevice => 
        savedDevice.id === device.id || 
        savedDevice.deviceId === device.id ||
        savedDevice.deviceCode === device.deviceCode
      )
    );
    
    if (matchedDevices.length > 0) {
      selectedDevices.value = [...matchedDevices];
      console.log('已恢复选中的设备:', selectedDevices.value);
    }
  }
}

// 页面初始化
onMounted(() => {
  // 从本地存储获取之前步骤的数据
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      currentStep.value = data.step || 1
      
      // 检查是否是编辑模式
      isEdit.value = data.isEdit || false
      taskId.value = data.taskId || null
      
      // 如果是编辑模式，尝试从taskEditData中获取设备数据
      if (isEdit.value && taskId.value) {
        const editData = localStorage.getItem('taskEditData')
        if (editData) {
          try {
            const taskData = JSON.parse(editData)
            console.log('编辑模式，获取到的任务详情数据:', taskData)
            
            // 如果有设备数据，预先选中这些设备
            if (taskData.devices && Array.isArray(taskData.devices) && taskData.devices.length > 0) {
              // 保存已选择的设备，等待设备列表加载完成后进行匹配
              savedSelectedDevices.value = taskData.devices
              console.log('已保存的设备列表:', savedSelectedDevices.value)
            }
          } catch (error) {
            console.error('解析任务编辑数据失败', error)
          }
        }
      }
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }

  // 加载设备列表
  getDeviceList()
})
</script>

<style scoped>
.device-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  width: 100%;
}

@media (max-width: 1400px) {
  .device-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}

@media (max-width: 992px) {
  .device-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 576px) {
  .device-grid {
    grid-template-columns: 1fr;
  }
}

.device-card {
  border: 1px solid #e9ecef;
  border-radius: 10px;
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  height: 100%;
  width: 100%;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
  background-color: #fff;
}

.device-card:hover {
  border-color: var(--el-color-primary);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  transform: translateY(-3px);
}

.device-card.selected {
  border-color: var(--el-color-primary);
  background-color: rgba(64, 158, 255, 0.05);
}

.device-card.selected::after {
  content: '✓';
  position: absolute;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: var(--el-color-primary);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  z-index: 2;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}

.device-image {
  height: 160px;
  width: 100%;
  object-fit: cover;
}

.device-image-placeholder {
  height: 160px;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  color: #909399;
  font-size: 24px;
}

.device-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  z-index: 1;
}

.status-online {
  background-color: rgba(40, 167, 69, 0.1);
  color: #28a745;
}

.status-offline {
  background-color: rgba(220, 53, 69, 0.1);
  color: #dc3545;
}

.status-maintenance {
  background-color: rgba(255, 193, 7, 0.1);
  color: #ffc107;
}

.device-info {
  padding: 15px;
  flex: 1;
  display: flex;
  flex-direction: column;
}

.device-name {
  font-size: 15px;
  font-weight: 600;
  margin-bottom: 5px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  color: #2c3e50;
}

.device-id {
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  margin-bottom: 10px;
  color: #6c757d;
}

.action-buttons {
  display: flex;
  align-items: center;
}

.action-footer {
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 30px;
}

.action-footer .el-button {
  padding: 12px 20px;
  font-weight: 500;
  transition: all 0.3s;
}

.action-footer .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

/* 添加选中计数样式 */
.selected-count {
  background-color: var(--el-color-primary);
  color: white;
  padding: 5px 10px;
  border-radius: 20px;
  font-size: 14px;
  font-weight: 600;
  min-width: 30px;
  text-align: center;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.clear-selection-btn {
  margin-left: 10px;
  font-size: 14px;
  color: #606266;
  text-decoration: none;
}

.clear-selection-btn:hover {
  color: var(--el-color-primary);
  text-decoration: underline;
}

.device-filter-bar {
  background-color: white;
  border-radius: 10px;
  padding: 15px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.05);
}

.device-filter-bar .el-form-item__label {
  font-weight: 500;
}

.card-header-title {
  display: flex;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  color: #2c3e50;
}

.card-header-title .el-icon {
  margin-right: 8px;
  font-size: 18px;
}

.view-mode-toggle {
  border-radius: 4px;
  overflow: hidden;
}

.device-list-container {
  min-height: 400px;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}
</style> 