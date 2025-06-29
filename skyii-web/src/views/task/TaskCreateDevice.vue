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

    <!-- 设备选择内容 -->
    <el-row :gutter="20">
      <!-- 左侧设备选择面板 -->
      <el-col :span="12">
        <div class="card selection-panel">
          <div class="card-header">
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <el-icon><Monitor /></el-icon> 设备选择方式
              </div>
            </div>
          </div>
          
          <!-- 选择方式导航标签 -->
          <el-tabs v-model="activeSelectionTab" class="demo-tabs">
            <el-tab-pane label="设备类型" name="deviceType">
              <template #label>
                <div class="d-flex align-items-center">
                  <el-icon class="me-1"><PriceTag /></el-icon> 设备类型
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane label="区域位置" name="location">
              <template #label>
                <div class="d-flex align-items-center">
                  <el-icon class="me-1"><MapLocation /></el-icon> 区域位置
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane label="设备标签" name="tag">
              <template #label>
                <div class="d-flex align-items-center">
                  <el-icon class="me-1"><Collection /></el-icon> 设备标签
                </div>
              </template>
            </el-tab-pane>
            <el-tab-pane label="具体设备" name="devices">
              <template #label>
                <div class="d-flex align-items-center">
                  <el-icon class="me-1"><List /></el-icon> 具体设备
                </div>
              </template>
            </el-tab-pane>
          </el-tabs>
          
          <!-- 选择方式内容 -->
          <div class="selection-content">
            <!-- 根据设备类型选择 -->
            <div v-if="activeSelectionTab === 'deviceType'">
              <div class="mb-3">
                <el-input
                  v-model="typeSearchQuery"
                  placeholder="搜索设备类型..."
                  clearable
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                </el-input>
              </div>
              
              <div class="tree-view">
                <!-- 设备类型树 -->
                <el-tree
                  ref="deviceTypeTreeRef"
                  :data="deviceTypeTree"
                  :props="defaultProps"
                  node-key="id"
                  @check="handleTypeCheck"
                  show-checkbox
                  check-strictly
                >
                  <template #default="{ node, data }">
                    <div class="d-flex align-items-center">
                      <el-icon v-if="!node.isLeaf" class="me-1">
                        <FolderOpened v-if="node.expanded" />
                        <Folder v-else />
                      </el-icon>
                      <el-icon v-else class="me-1">
                        <component :is="getDeviceIcon(data.type)" />
                      </el-icon>
                      <span>{{ node.label }}</span>
                      <el-tag size="small" class="ms-2" type="info" v-if="data.count !== undefined">
                        {{ data.count }}
                      </el-tag>
                    </div>
                  </template>
                </el-tree>
              </div>
            </div>
            
            <!-- 根据区域位置选择 -->
            <div v-if="activeSelectionTab === 'location'">
              <!-- 区域选择地图 -->
              <div class="location-map">
                <div class="map-placeholder">
                  <!-- 园区地图 -->
                  <div v-for="marker in locationMarkers" :key="marker.id" 
                       :class="['map-marker', `marker-${marker.type}`, {'marker-selected': marker.selected}]" 
                       :style="{top: marker.top, left: marker.left}"
                       :title="marker.title"
                       @click="toggleMarkerSelection(marker)">
                  </div>
                </div>
                
                <!-- 地图控制区域 -->
                <div class="map-overlay">
                  <div class="mb-2 fw-bold">设备类型过滤:</div>
                  <el-checkbox v-model="showAllDevicesOnMap" label="显示所有设备" @change="filterMapMarkers" />
                  <el-checkbox v-model="showCamerasOnMap" label="摄像头" @change="filterMapMarkers" />
                  <el-checkbox v-model="showSensorsOnMap" label="传感器" @change="filterMapMarkers" />
                  <el-checkbox v-model="showControllersOnMap" label="控制器" @change="filterMapMarkers" />
                </div>
                
                <!-- 地图图例 -->
                <div class="map-legend">
                  <div class="mb-2 fw-bold">区域选择:</div>
                  <div v-for="zone in locationZones" :key="zone.id" class="mb-2">
                    <el-checkbox v-model="zone.selected" @change="handleZoneSelection(zone)">
                      {{ zone.name }} ({{ zone.deviceCount }}设备)
                    </el-checkbox>
                  </div>
                  <el-button type="primary" size="small" class="w-100 mt-2" @click="confirmLocationSelection">
                    确认选择
                  </el-button>
                </div>
              </div>
            </div>
            
            <!-- 根据设备标签选择 -->
            <div v-if="activeSelectionTab === 'tag'">
              <div class="mb-3">
                <el-text class="mb-2 d-block">选择标签筛选设备</el-text>
                <div class="tag-cloud">
                  <el-tag
                    v-for="tag in availableTags"
                    :key="tag.name"
                    :class="{ 'tag-selected': tag.selected }"
                    @click="toggleTagSelection(tag)"
                    class="me-1 mb-1 tag-item"
                  >
                    {{ tag.name }} <span class="ms-1">({{ tag.count }})</span>
                  </el-tag>
                </div>
              </div>
              
              <div class="mb-3">
                <el-text class="mb-2 d-block">标签组合方式</el-text>
                <el-radio-group v-model="tagCombination">
                  <el-radio label="or">或关系（任一标签匹配即选中设备）</el-radio>
                  <el-radio label="and">与关系（所有标签都匹配才选中设备）</el-radio>
                </el-radio-group>
              </div>
              
              <el-alert
                type="info"
                :closable="false"
                show-icon
              >
                当前已选择 <strong>{{ selectedTags.length }}</strong> 个标签，符合条件的设备有 <strong>{{ matchedDevicesByTags.length }}</strong> 台
              </el-alert>
              
              <div class="mt-4">
                <el-text class="mb-2 d-block">已选标签</el-text>
                <div class="tag-cloud">
                  <template v-if="selectedTags.length > 0">
                    <el-tag
                      v-for="tag in selectedTags"
                      :key="tag.name"
                      closable
                      @close="removeSelectedTag(tag)"
                      type="primary"
                      class="me-1 mb-1"
                    >
                      {{ tag.name }}
                    </el-tag>
                  </template>
                  <el-alert
                    v-else
                    type="info"
                    :closable="false"
                    class="w-100 mb-0"
                  >
                    尚未选择任何标签
                  </el-alert>
                </div>
              </div>
              
              <div class="d-grid gap-2 mt-4">
                <el-button type="primary" @click="applyTagSelection">
                  <el-icon class="me-1"><Check /></el-icon> 应用标签选择
                </el-button>
                <el-button @click="clearTagSelection">
                  <el-icon class="me-1"><Close /></el-icon> 清除选择
                </el-button>
              </div>
            </div>
            
            <!-- 选择具体设备 -->
            <div v-if="activeSelectionTab === 'devices'">
              <div class="mb-3">
                <div class="d-flex">
                  <el-input
                    v-model="searchQuery"
                    placeholder="搜索设备名称、ID或IP地址..."
                    clearable
                    @input="filterDevices"
                    class="me-2"
                  >
                    <template #prefix>
                      <el-icon><Search /></el-icon>
                    </template>
                  </el-input>
                  <el-button @click="filterDevices">
                    <el-icon><Search /></el-icon>
                  </el-button>
                </div>
              </div>
              
              <div class="d-flex justify-content-between align-items-center mb-3">
                <div>
                  <el-radio-group v-model="deviceViewMode" size="small">
                    <el-radio-button label="card">
                      <el-icon><Grid /></el-icon> 卡片
                    </el-radio-button>
                    <el-radio-button label="list">
                      <el-icon><List /></el-icon> 列表
                    </el-radio-button>
                  </el-radio-group>
                </div>
                <div>
                  <el-select v-model="deviceSortOrder" size="small" placeholder="排序方式">
                    <el-option label="按名称排序" value="name" />
                    <el-option label="按状态排序" value="status" />
                    <el-option label="按类型排序" value="type" />
                    <el-option label="按最后上线时间排序" value="lastOnline" />
                  </el-select>
                </div>
              </div>
              
              <!-- 卡片视图 -->
              <div v-if="deviceViewMode === 'card'" class="row device-grid">
                <div v-for="device in filteredDevices" :key="device.id" class="col-md-6 mb-3">
                  <div 
                    :class="['device-card', {'device-selected': isDeviceSelected(device)}]"
                    @click="toggleDeviceSelection(device)"
                  >
                    <div :class="['device-status', `status-${device.status}`]">
                      {{ getStatusLabel(device.status) }}
                    </div>
                    <div class="device-img">
                      <el-icon :size="36"><component :is="getDeviceIcon(device.type)" /></el-icon>
                    </div>
                    <div class="device-info">
                      <h6>{{ device.name }}</h6>
                      <div class="small text-muted">类型: {{ getDeviceTypeLabel(device.type) }}</div>
                      <div class="small text-muted">IP: {{ device.ip }}</div>
                      <div class="small text-muted mb-2">最后在线: {{ device.lastOnline || '未知' }}</div>
                      <div>
                        <el-checkbox 
                          v-model="device.selected" 
                          @change="(val) => handleDeviceCheckboxChange(device, val)"
                        >
                          选择此设备
                        </el-checkbox>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <!-- 列表视图 -->
              <div v-else>
                <el-table
                  ref="deviceTableRef"
                  v-loading="loading"
                  :data="filteredDevices"
                  @selection-change="handleSelectionChange"
                  border
                  style="width: 100%"
                >
                  <el-table-column type="selection" width="55" />
                  <el-table-column prop="id" label="设备ID" width="80" />
                  <el-table-column prop="name" label="设备名称" min-width="120">
                    <template #default="scope">
                      <div class="d-flex align-items-center">
                        <el-icon :size="18" class="me-2">
                          <component :is="getDeviceIcon(scope.row.type)" />
                        </el-icon>
                        <span>{{ scope.row.name }}</span>
                      </div>
                    </template>
                  </el-table-column>
                  <el-table-column prop="type" label="设备类型" width="100">
                    <template #default="scope">
                      <el-tag :type="getDeviceTypeTag(scope.row.type)">
                        {{ getDeviceTypeLabel(scope.row.type) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                  <el-table-column prop="ip" label="IP地址" width="130" />
                  <el-table-column prop="location" label="位置" min-width="120" />
                  <el-table-column prop="status" label="状态" width="80">
                    <template #default="scope">
                      <el-tag :type="getStatusType(scope.row.status)">
                        {{ getStatusLabel(scope.row.status) }}
                      </el-tag>
                    </template>
                  </el-table-column>
                </el-table>
              </div>
              
              <!-- 分页 -->
              <div class="pagination-container mt-3">
                <el-pagination
                  v-model:current-page="currentPage"
                  v-model:page-size="pageSize"
                  :page-sizes="[10, 20, 50, 100]"
                  layout="total, sizes, prev, pager, next, jumper"
                  :total="totalDevices"
                  @size-change="handleSizeChange"
                  @current-change="handleCurrentChange"
                />
              </div>
            </div>
          </div>
        </div>
      </el-col>

      <!-- 右侧已选设备信息 -->
      <el-col :span="12">
        <el-card>
          <template #header>
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <el-icon><Check /></el-icon> 已选设备
              </div>
              <div>
                <el-button plain size="small" type="danger" @click="clearSelection">
                  <el-icon class="me-1"><Delete /></el-icon> 清除所有选择
                </el-button>
              </div>
            </div>
          </template>
          
          <el-alert
            type="info"
            class="mb-3"
            :closable="false"
          >
            <div class="d-flex justify-content-between align-items-center">
              <div>
                <el-icon class="me-1"><InfoFilled /></el-icon> 已选择 <strong>{{ selectedDevices.length }}</strong> 台设备
              </div>
              <div>
                <el-button size="small" plain>
                  <el-icon class="me-1"><Download /></el-icon> 导出设备列表
                </el-button>
              </div>
            </div>
          </el-alert>
          
          <!-- 设备类型统计 -->
          <el-row class="mb-4">
            <el-col :span="8">
              <el-card shadow="never" class="bg-light text-center">
                <div class="fw-bold text-primary mb-2">
                  <el-icon size="24"><VideoCamera /></el-icon>
                </div>
                <h5>{{ getDeviceTypeCount('camera') }}</h5>
                <div class="small text-muted">摄像头</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never" class="bg-light text-center">
                <div class="fw-bold text-success mb-2">
                  <el-icon size="24"><Connection /></el-icon>
                </div>
                <h5>{{ getDeviceTypeCount('sensor') }}</h5>
                <div class="small text-muted">传感器</div>
              </el-card>
            </el-col>
            <el-col :span="8">
              <el-card shadow="never" class="bg-light text-center">
                <div class="fw-bold text-warning mb-2">
                  <el-icon size="24"><Link /></el-icon>
                </div>
                <h5>{{ getDeviceTypeCount('controller') }}</h5>
                <div class="small text-muted">控制器</div>
              </el-card>
            </el-col>
          </el-row>
          
          <!-- 已选设备表格 -->
          <el-table
            :data="selectedDevices"
            style="width: 100%"
            size="small"
            max-height="400"
          >
            <el-table-column label="#" type="index" width="50" />
            <el-table-column prop="name" label="设备名称" min-width="120" />
            <el-table-column prop="type" label="设备类型" width="100">
              <template #default="scope">
                <el-tag :type="getDeviceTypeTag(scope.row.type)" size="small">
                  {{ getDeviceTypeLabel(scope.row.type) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="80">
              <template #default="scope">
                <el-tag :type="getStatusType(scope.row.status)" size="small">
                  {{ getStatusLabel(scope.row.status) }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="location" label="位置" min-width="120" />
            <el-table-column label="操作" width="80">
              <template #default="scope">
                <el-button
                  circle
                  plain
                  size="small"
                  type="danger"
                  @click="removeSelectedDevice(scope.row)"
                >
                  <el-icon><Close /></el-icon>
                </el-button>
              </template>
            </el-table-column>
          </el-table>
          
          <el-alert
            v-if="offlineDevicesCount > 0"
            type="warning"
            class="mt-3"
            :closable="false"
          >
            <el-icon class="me-1"><Warning /></el-icon> 注意：已选设备中有 <strong>{{ offlineDevicesCount }}</strong> 台设备处于离线状态，可能会影响任务执行
          </el-alert>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 已选设备计数器 -->
    <div class="selected-device-counter" v-if="selectedDevices.length > 0">
      <el-icon><Check /></el-icon>
      <span class="number">{{ selectedDevices.length }}</span> 已选设备
      <el-button size="small" @click="nextStep">下一步</el-button>
    </div>

    <!-- 底部按钮 -->
    <div class="action-footer d-flex justify-content-between mt-4">
      <el-button @click="prevStep">
        <el-icon class="el-icon--left"><ArrowLeft /></el-icon> 上一步
      </el-button>
      <el-button type="primary" @click="nextStep" :disabled="selectedDevices.length === 0">
        下一步 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Document, Close, Monitor, Search, Select, FolderOpened, Folder,
  ArrowLeft, ArrowRight, Camera, Link, Connection, PriceTag, MapLocation, Collection,
  List, Check, Grid, Warning, Delete, Download, InfoFilled, VideoCamera } from '@element-plus/icons-vue'
import { getDeviceList, getDeviceGroups, getDevicesByGroupId, saveTaskDraft, getDevicesByType, getDevicesByTypeParam } from '@/api/device'

const router = useRouter()

// 判断是否为编辑模式
const isEdit = ref(false)
const taskId = ref(null)

// 当前步骤
const currentStep = ref(1)

// 设备列表相关
const loading = ref(false)
const deviceTableRef = ref(null)
const deviceGroupTreeRef = ref(null)
const devices = ref([])
const selectedDevices = ref([])
const deviceGroups = ref([])

// 分页相关
const currentPage = ref(1)
const pageSize = ref(10)
const totalDevices = ref(0)

// 选择方式标签页
const activeSelectionTab = ref('deviceType')
const deviceViewMode = ref('card')
const deviceSortOrder = ref('name')

// 搜索和过滤
const searchQuery = ref('')
const typeSearchQuery = ref('')
const filterType = ref('')
const filterStatus = ref('')
const groupSearchQuery = ref('')

// 设备类型树
const deviceTypeTree = ref([
  {
    id: 'all',
    label: '所有设备',
    count: 56,
    children: [
      {
        id: 'camera',
        label: '摄像头',
        type: 'camera',
        count: 24,
        children: [
          { id: 'camera-hd', label: '高清摄像头', type: 'camera', count: 12 },
          { id: 'camera-ir', label: '红外摄像头', type: 'camera', count: 8 },
          { id: 'camera-ptz', label: '云台摄像头', type: 'camera', count: 4 }
        ]
      },
      {
        id: 'sensor',
        label: '传感器',
        type: 'sensor',
        count: 18,
        children: [
          { id: 'sensor-temp', label: '温湿度传感器', type: 'sensor', count: 8 },
          { id: 'sensor-smoke', label: '烟雾传感器', type: 'sensor', count: 6 },
          { id: 'sensor-door', label: '门禁传感器', type: 'sensor', count: 4 }
        ]
      },
      {
        id: 'controller',
        label: '控制器',
        type: 'controller',
        count: 14,
        children: []
      }
    ]
  }
])
const deviceTypeTreeRef = ref(null)

// 区域位置选择
const showAllDevicesOnMap = ref(true)
const showCamerasOnMap = ref(true)
const showSensorsOnMap = ref(true)
const showControllersOnMap = ref(true)

const locationMarkers = ref([
  // 区域A
  { id: 'cam-a1', type: 'camera', top: '30%', left: '25%', title: '摄像头区域A-1', selected: false },
  { id: 'cam-a2', type: 'camera', top: '35%', left: '30%', title: '摄像头区域A-2', selected: false },
  { id: 'sensor-a1', type: 'sensor', top: '25%', left: '20%', title: '温度传感器区域A-1', selected: false },
  
  // 区域B
  { id: 'cam-b1', type: 'camera', top: '45%', left: '60%', title: '摄像头区域B-1', selected: false },
  { id: 'cam-b2', type: 'camera', top: '50%', left: '65%', title: '摄像头区域B-2', selected: false },
  { id: 'controller-b1', type: 'controller', top: '55%', left: '55%', title: '门禁控制器区域B-1', selected: false },
  
  // 区域C
  { id: 'cam-c1', type: 'camera', top: '70%', left: '40%', title: '摄像头区域C-1', selected: false },
  { id: 'sensor-c1', type: 'sensor', top: '75%', left: '35%', title: '烟雾传感器区域C-1', selected: false },
  { id: 'controller-c1', type: 'controller', top: '65%', left: '45%', title: '环境控制器区域C-1', selected: false }
])

const locationZones = ref([
  { id: 'zone-a', name: '区域A', deviceCount: 3, selected: false },
  { id: 'zone-b', name: '区域B', deviceCount: 3, selected: false },
  { id: 'zone-c', name: '区域C', deviceCount: 3, selected: false }
])

// 设备标签选择
const tagCombination = ref('or')
const availableTags = ref([
  { name: '高清', count: 12, selected: false },
  { name: '红外', count: 8, selected: false },
  { name: '360度', count: 4, selected: false },
  { name: '前门', count: 6, selected: false },
  { name: '后门', count: 5, selected: false },
  { name: '仓库', count: 10, selected: false },
  { name: '办公区', count: 8, selected: false },
  { name: '车库', count: 4, selected: false },
  { name: '走廊', count: 6, selected: false },
  { name: '门禁', count: 4, selected: false },
  { name: '温度', count: 8, selected: false },
  { name: '湿度', count: 8, selected: false },
  { name: '烟雾', count: 6, selected: false },
  { name: '重要', count: 15, selected: false },
  { name: '备用', count: 5, selected: false }
])

// 树形控件配置
const defaultProps = {
  children: 'children',
  label: 'name'
}

// 过滤后的设备列表
const filteredDevices = computed(() => {
  let result = [...devices.value]
  
  // 搜索过滤
  if (searchQuery.value) {
    const query = searchQuery.value.toLowerCase()
    result = result.filter(device => 
      device.name.toLowerCase().includes(query) ||
      (device.ip && device.ip.toLowerCase().includes(query)) ||
      (device.id && device.id.toString().includes(query))
    )
  }
  
  // 类型过滤
  if (filterType.value) {
    result = result.filter(device => device.type === filterType.value)
  }
  
  // 状态过滤
  if (filterStatus.value) {
    result = result.filter(device => device.status === filterStatus.value)
  }
  
  // 排序
  if (deviceSortOrder.value) {
    result.sort((a, b) => {
      switch (deviceSortOrder.value) {
        case 'name':
          return a.name.localeCompare(b.name)
        case 'status':
          return a.status.localeCompare(b.status)
        case 'type':
          return a.type.localeCompare(b.type)
        case 'lastOnline':
          return (b.lastOnline || '').localeCompare(a.lastOnline || '')
        default:
          return 0
      }
    })
  }
  
  return result
})

// 已选标签列表
const selectedTags = computed(() => {
  return availableTags.value.filter(tag => tag.selected)
})

// 根据标签匹配的设备
const matchedDevicesByTags = computed(() => {
  if (selectedTags.value.length === 0) return []
  
  return devices.value.filter(device => {
    if (!device.tags || !Array.isArray(device.tags)) return false
    
    if (tagCombination.value === 'or') {
      // 任一标签匹配
      return selectedTags.value.some(tag => device.tags.includes(tag.name))
    } else {
      // 所有标签都匹配
      return selectedTags.value.every(tag => device.tags.includes(tag.name))
    }
  })
})

// 离线设备数量
const offlineDevicesCount = computed(() => {
  return selectedDevices.value.filter(device => device.status === 'offline' || device.status === 'fault').length
})

// 获取设备类型图标
const getDeviceIcon = (type) => {
  switch (type) {
    case 'camera': return Camera
    case 'sensor': return Connection
    case 'controller': return Link
    default: return Monitor
  }
}

// 获取设备类型标签样式
const getDeviceTypeTag = (type) => {
  switch (type) {
    case 'camera': return 'success'
    case 'sensor': return 'primary'
    case 'controller': return 'warning'
    default: return 'info'
  }
}

// 获取设备类型标签文本
const getDeviceTypeLabel = (type) => {
  switch (type) {
    case 'camera': return '摄像头'
    case 'sensor': return '传感器'
    case 'controller': return '控制器'
    default: return '其他'
  }
}

// 获取状态标签样式
const getStatusType = (status) => {
  switch (status) {
    case 'online': return 'success'
    case 'offline': return 'info'
    case 'fault': return 'danger'
    default: return 'warning'
  }
}

// 获取状态标签文本
const getStatusLabel = (status) => {
  switch (status) {
    case 'online': return '在线'
    case 'offline': return '离线'
    case 'fault': return '故障'
    default: return '未知'
  }
}

// 获取特定类型的设备数量
const getDeviceTypeCount = (type) => {
  return selectedDevices.value.filter(device => device.type === type).length
}

// 处理表格选择变更
const handleSelectionChange = (selection) => {
  selectedDevices.value = selection
}

// 全选设备
const selectAllDevices = () => {
  filteredDevices.value.forEach(row => {
    deviceTableRef.value.toggleRowSelection(row, true)
  })
}

// 清空选择
const clearSelection = () => {
  deviceTableRef.value.clearSelection()
  selectedDevices.value = []
}

// 移除已选设备
const removeSelectedDevice = (device) => {
  const index = selectedDevices.value.findIndex(item => item.id === device.id)
  if (index !== -1) {
    selectedDevices.value.splice(index, 1)
    
    // 同步表格选择状态
    if (deviceTableRef.value) {
      const foundDevice = devices.value.find(d => d.id === device.id)
      if (foundDevice) {
        deviceTableRef.value.toggleRowSelection(foundDevice, false)
      }
    }
    
    // 同步地图标记选择状态
    const marker = locationMarkers.value.find(m => m.id === `cam-${device.id}` || m.id === `sensor-${device.id}` || m.id === `controller-${device.id}`)
    if (marker) {
      marker.selected = false
    }
    
    // 同步设备卡片选择状态
    const foundDevice = devices.value.find(d => d.id === device.id)
    if (foundDevice) {
      foundDevice.selected = false
    }
  }
}

// 检查设备是否已选择
const isDeviceSelected = (device) => {
  return selectedDevices.value.some(d => d.id === device.id)
}

// 切换设备选择状态
const toggleDeviceSelection = (device) => {
  if (isDeviceSelected(device)) {
    removeSelectedDevice(device)
  } else {
    selectedDevices.value.push(device)
    device.selected = true
  }
}

// 处理设备复选框变化
const handleDeviceCheckboxChange = (device, checked) => {
  if (checked && !isDeviceSelected(device)) {
    selectedDevices.value.push(device)
  } else if (!checked && isDeviceSelected(device)) {
    removeSelectedDevice(device)
  }
}

// 处理设备类型树选择
const handleTypeCheck = (data, checked) => {
  if (checked.checkedKeys.includes(data.id)) {
    // 如果是父节点，选择所有子节点
    if (data.children && data.children.length > 0) {
      addDevicesByType(data.type || data.id)
    } else {
      // 叶子节点，直接添加对应类型的设备
      addDevicesByType(data.type || data.id)
    }
  } else {
    // 取消选择
    if (data.children && data.children.length > 0) {
      removeDevicesByType(data.type || data.id)
    } else {
      removeDevicesByType(data.type || data.id)
    }
  }
}

// 根据类型添加设备
const addDevicesByType = async (type) => {
  try {
    loading.value = true
    // 获取指定类型的设备
    let res
    if (type === 'camera' || type === 'sensor' || type === 'controller') {
      // 使用路径参数的API
      res = await getDevicesByType(type)
    } else {
      // 使用查询参数的API
      res = await getDevicesByTypeParam(type)
    }
    
    if (res.data && res.data.length > 0) {
      res.data.forEach(device => {
        if (!selectedDevices.value.some(d => d.id === device.id)) {
          selectedDevices.value.push(device)
          
          // 同步设备卡片选择状态
          const foundDevice = devices.value.find(d => d.id === device.id)
          if (foundDevice) {
            foundDevice.selected = true
          }
        }
      })
    }
  } catch (error) {
    console.error(`获取${type}类型设备失败`, error)
    ElMessage.error(`获取${type}类型设备失败`)
  } finally {
    loading.value = false
  }
}

// 根据类型移除设备
const removeDevicesByType = (type) => {
  const devicesToRemove = selectedDevices.value.filter(device => 
    device.type === type || 
    (type === 'all' && device)
  )
  
  devicesToRemove.forEach(device => {
    removeSelectedDevice(device)
  })
}

// 切换地图标记选择
const toggleMarkerSelection = (marker) => {
  marker.selected = !marker.selected
  
  // 获取对应的设备并添加/移除
  const deviceId = marker.id.split('-')[1]
  const device = devices.value.find(d => d.id === deviceId)
  
  if (device) {
    if (marker.selected) {
      if (!isDeviceSelected(device)) {
        selectedDevices.value.push(device)
        device.selected = true
      }
    } else {
      removeSelectedDevice(device)
    }
  }
}

// 过滤地图标记
const filterMapMarkers = () => {
  locationMarkers.value.forEach(marker => {
    let visible = showAllDevicesOnMap.value
    
    if (!showAllDevicesOnMap.value) {
      if (marker.type === 'camera' && showCamerasOnMap.value) {
        visible = true
      } else if (marker.type === 'sensor' && showSensorsOnMap.value) {
        visible = true
      } else if (marker.type === 'controller' && showControllersOnMap.value) {
        visible = true
      } else {
        visible = false
      }
    }
    
    marker.visible = visible
  })
}

// 处理区域选择
const handleZoneSelection = (zone) => {
  // 获取该区域下的所有标记
  const zoneMarkers = locationMarkers.value.filter(marker => 
    marker.id.startsWith(zone.id.replace('zone-', ''))
  )
  
  // 设置标记选中状态
  zoneMarkers.forEach(marker => {
    marker.selected = zone.selected
    
    // 同步设备选择
    const deviceId = marker.id.split('-')[1]
    const device = devices.value.find(d => d.id === deviceId)
    
    if (device) {
      if (zone.selected) {
        if (!isDeviceSelected(device)) {
          selectedDevices.value.push(device)
          device.selected = true
        }
      } else {
        removeSelectedDevice(device)
      }
    }
  })
}

// 确认位置选择
const confirmLocationSelection = () => {
  ElMessage.success(`已选择 ${selectedDevices.length} 台设备`)
}

// 切换标签选择
const toggleTagSelection = (tag) => {
  tag.selected = !tag.selected
}

// 移除已选标签
const removeSelectedTag = (tag) => {
  tag.selected = false
}

// 应用标签选择
const applyTagSelection = () => {
  if (selectedTags.value.length === 0) {
    ElMessage.warning('请至少选择一个标签')
    return
  }
  
  // 将匹配的设备添加到已选设备列表
  matchedDevicesByTags.value.forEach(device => {
    if (!isDeviceSelected(device)) {
      selectedDevices.value.push(device)
      device.selected = true
    }
  })
  
  ElMessage.success(`已添加 ${matchedDevicesByTags.value.length} 台设备`)
}

// 清除标签选择
const clearTagSelection = () => {
  availableTags.value.forEach(tag => {
    tag.selected = false
  })
}

// 处理分组选择
const handleGroupCheck = async (data, checked) => {
  if (checked.checkedKeys.includes(data.id)) {
    // 选中分组，添加该分组下的所有设备
    if (data.devices && data.devices.length > 0) {
      // 如果分组已包含设备列表
      data.devices.forEach(device => {
        const found = devices.value.find(d => d.id === device.id)
        if (found && !selectedDevices.value.some(d => d.id === found.id)) {
          deviceTableRef.value.toggleRowSelection(found, true)
        }
      })
    } else {
      // 需要从后端获取该分组下的设备
      try {
        loading.value = true
        const res = await getDevicesByGroupId(data.id)
        if (res.data && res.data.length > 0) {
          res.data.forEach(device => {
            const found = devices.value.find(d => d.id === device.id)
            if (found && !selectedDevices.value.some(d => d.id === found.id)) {
              deviceTableRef.value.toggleRowSelection(found, true)
            }
          })
        }
      } catch (error) {
        console.error('获取分组设备失败', error)
        ElMessage.error('获取分组设备失败')
      } finally {
        loading.value = false
      }
    }
  } else {
    // 取消选中分组，移除该分组下的所有设备
    // 实际实现可能需要根据后端API调整
  }
}

// 过滤设备
const filterDevices = () => {
  // 分页重置到第一页
  currentPage.value = 1
}

// 处理页面大小变更
const handleSizeChange = (val) => {
  pageSize.value = val
  fetchDevices()
}

// 处理页码变更
const handleCurrentChange = (val) => {
  currentPage.value = val
  fetchDevices()
}

// 获取设备列表
const fetchDevices = async () => {
  try {
    loading.value = true
    const res = await getDeviceList({
      page: currentPage.value,
      limit: pageSize.value,
      search: searchQuery.value,
      type: filterType.value,
      status: filterStatus.value
    })
    
    if (res.data) {
      devices.value = res.data.items || []
      totalDevices.value = res.data.total || 0
      
      // 恢复已选设备的选中状态
      nextTick(() => {
        selectedDevices.value.forEach(device => {
          const found = devices.value.find(d => d.id === device.id)
          if (found) {
            deviceTableRef.value.toggleRowSelection(found, true)
          }
        })
      })
    }
  } catch (error) {
    console.error('获取设备列表失败', error)
    ElMessage.error('获取设备列表失败')
  } finally {
    loading.value = false
  }
}

// 获取设备分组
const fetchDeviceGroups = async () => {
  try {
    const res = await getDeviceGroups()
    if (res.data) {
      deviceGroups.value = res.data
    }
  } catch (error) {
    console.error('获取设备分组失败', error)
    ElMessage.error('获取设备分组失败')
  }
}

// 上一步
const prevStep = () => {
  // 保存当前数据到本地存储
  saveCurrentData()
  
  // 跳转到上一步
  router.push('/task/create')
}

// 下一步
const nextStep = () => {
  if (selectedDevices.value.length === 0) {
    ElMessage.warning('请至少选择一个设备')
    return
  }
  
  // 保存当前数据到本地存储
  saveCurrentData()
  
  // 跳转到下一步
  router.push('/task/create/metrics')
}

// 保存当前数据到本地存储
const saveCurrentData = () => {
  // 获取上一步的数据
  const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  
  // 合并数据并保存
  localStorage.setItem('taskCreateData', JSON.stringify({
    ...prevData,
    step: 2,
    selectedDevices: selectedDevices.value
  }))
}

// 保存草稿
const saveDraft = async () => {
  try {
    // 获取上一步的数据
    const prevData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
    
    const draftData = {
      ...prevData,
      step: 1,
      selectedDevices: selectedDevices.value
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
  ElMessageBox.confirm('确定要取消任务创建吗？已填写的数据将不会保存。', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    router.push('/task')
  }).catch(() => {})
}

// 恢复数据
const restoreData = () => {
  const storedData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
  
  // 恢复编辑状态和任务ID
  isEdit.value = storedData.isEdit || false
  taskId.value = storedData.taskId || null
  
  // 恢复已选设备
  if (storedData.selectedDevices && Array.isArray(storedData.selectedDevices)) {
    selectedDevices.value = storedData.selectedDevices
  }
}

// 页面初始化
onMounted(() => {
  // 恢复数据
  restoreData()
  
  // 获取设备列表和分组
  fetchDevices()
  fetchDeviceGroups()
})

// 监听搜索和过滤条件变化
watch([searchQuery, filterType, filterStatus], () => {
  filterDevices()
})
</script>

<style scoped>
.search-filter-container {
  padding: 15px;
  background-color: #f8f9fa;
  border-radius: 4px;
}

.selected-device-item {
  padding: 10px;
  border-bottom: 1px solid #ebeef5;
}

.selected-device-item:last-child {
  border-bottom: none;
}

.selection-summary {
  padding-top: 10px;
  border-top: 1px solid #ebeef5;
  font-size: 14px;
}

.empty-selection {
  padding: 20px 0;
}

.action-footer {
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 30px;
}

/* 设备选择页面特有样式 */
.selection-panel {
  min-height: 600px;
  display: flex;
  flex-direction: column;
}

.selection-content {
  flex-grow: 1;
  border: 1px solid #e9ecef;
  border-top: none;
  border-radius: 0 0 10px 10px;
  padding: 20px;
  background-color: white;
  min-height: 500px;
}

.tree-view {
  max-height: 450px;
  overflow-y: auto;
}

.location-map {
  height: 450px;
  background-color: #f8f9fa;
  border-radius: 10px;
  position: relative;
  overflow: hidden;
}

.map-placeholder {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background: #f8f9fa;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.map-overlay {
  position: absolute;
  top: 10px;
  right: 10px;
  background-color: white;
  padding: 10px;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.map-legend {
  position: absolute;
  bottom: 10px;
  left: 10px;
  background-color: white;
  padding: 10px;
  border-radius: 5px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
  z-index: 10;
  min-width: 200px;
}

.map-marker {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  position: absolute;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  font-weight: bold;
  cursor: pointer;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
  z-index: 5;
}

.marker-camera {
  background-color: #409eff;
}

.marker-sensor {
  background-color: #67c23a;
}

.marker-controller {
  background-color: #e6a23c;
}

.marker-selected {
  border: 3px solid white;
  transform: scale(1.2);
  z-index: 6;
}

.tag-cloud {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-top: 10px;
}

.tag-item {
  cursor: pointer;
  transition: all 0.2s;
}

.tag-item:hover {
  background-color: #ecf5ff;
}

.tag-selected {
  background-color: #409eff;
  color: white;
}

.device-card {
  border: 1px solid #e9ecef;
  border-radius: 8px;
  transition: all 0.2s ease;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.device-card:hover {
  border-color: #409eff;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
}

.device-selected {
  border-color: #409eff;
  background-color: rgba(64, 158, 255, 0.05);
}

.device-selected::after {
  content: '✓';
  position: absolute;
  top: 10px;
  right: 10px;
  width: 24px;
  height: 24px;
  border-radius: 50%;
  background-color: #409eff;
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  z-index: 5;
}

.device-status {
  position: absolute;
  top: 10px;
  left: 10px;
  padding: 4px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  z-index: 5;
}

.status-online {
  background-color: rgba(103, 194, 58, 0.1);
  color: #67c23a;
}

.status-offline {
  background-color: rgba(144, 147, 153, 0.1);
  color: #909399;
}

.status-fault {
  background-color: rgba(245, 108, 108, 0.1);
  color: #f56c6c;
}

.device-img {
  height: 120px;
  background-color: #f8f9fa;
  display: flex;
  align-items: center;
  justify-content: center;
}

.device-info {
  padding: 12px;
}

.selected-device-counter {
  position: fixed;
  bottom: 20px;
  right: 20px;
  background-color: #409eff;
  color: white;
  border-radius: 30px;
  padding: 12px 20px;
  font-weight: 600;
  box-shadow: 0 4px 10px rgba(0, 0, 0, 0.2);
  z-index: 1000;
  display: flex;
  align-items: center;
}

.selected-device-counter .number {
  margin: 0 8px;
  font-size: 18px;
}
</style> 