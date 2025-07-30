<template>
  <div class="device-groups-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">设备分组</div>
          <div class="header-actions">
            <el-button @click="handleDeviceList">
              <el-icon><List /></el-icon>设备列表
            </el-button>
            <el-button type="primary" @click="handleAddGroup">
              <el-icon><Plus /></el-icon>添加分组
            </el-button>
          </div>
        </div>
      </template>

      <!-- 状态统计卡片 -->
      <div class="stats-cards">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="24" :md="8">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <div class="stats-title">设备分组统计</div>
                <div class="stats-value-row">
                  <div class="stats-number">{{ groupStats.total }}</div>
                  <div class="stats-desc">
                    <div>分组总数</div>
                    <div class="stats-detail">共管理 {{ groupStats.deviceCount }} 台设备</div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
          <el-col :xs="24" :sm="24" :md="16">
            <el-card shadow="hover" class="stats-card">
              <div class="stats-content">
                <div class="stats-title">设备状态分布</div>
                <div class="stats-chart-row">
                  <div class="stats-chart">
                    <el-progress type="dashboard" :percentage="groupStats.onlineRate" :color="statusColors.online" :width="100">
                      <template #default>
                        <div class="progress-content">
                          <div class="progress-value">{{ groupStats.onlineRate }}%</div>
                          <div class="progress-label">在线率</div>
                        </div>
                      </template>
                    </el-progress>
                  </div>
                  <div class="stats-legend">
                    <div class="legend-item">
                      <div class="legend-color" style="background-color: var(--el-color-success)"></div>
                      <div class="legend-label">在线: {{ groupStats.onlineCount }}</div>
                    </div>
                    <div class="legend-item">
                      <div class="legend-color" style="background-color: var(--el-color-warning)"></div>
                      <div class="legend-label">离线: {{ groupStats.offlineCount }}</div>
                    </div>
                    <div class="legend-item">
                      <div class="legend-color" style="background-color: var(--el-color-danger)"></div>
                      <div class="legend-label">故障: {{ groupStats.errorCount }}</div>
                    </div>
                    <div class="legend-item">
                      <div class="legend-color" style="background-color: var(--el-color-info)"></div>
                      <div class="legend-label">未知: {{ groupStats.unknownCount }}</div>
                    </div>
                  </div>
                </div>
              </div>
            </el-card>
          </el-col>
        </el-row>
      </div>

      <!-- 搜索条件 -->
      <div class="search-panel">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索分组" 
          clearable 
          @keyup.enter="handleSearch"
          class="search-input">
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
          <template #append>
            <el-button @click="handleSearch">搜索</el-button>
          </template>
        </el-input>

        <div class="filter-actions">
          <el-select v-model="filterType" placeholder="分组类型" clearable>
            <el-option label="所有类型" value="" />
            <el-option label="常规分组" value="normal" />
            <el-option label="智能分组" value="smart" />
            <el-option label="动态分组" value="dynamic" />
          </el-select>
        </div>
      </div>

      <!-- 分组卡片列表 -->
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="group in filteredGroups" :key="group.id" class="mb-20">
          <div class="group-card" :class="`group-type-${group.type}`">
            <div class="group-header">
              <div class="group-title-wrapper">
                <div class="group-icon" :class="`group-icon-${group.type}`">
                  <el-icon v-if="group.type === 'normal'"><FolderOpened /></el-icon>
                  <el-icon v-else-if="group.type === 'smart'"><MagicStick /></el-icon>
                  <el-icon v-else-if="group.type === 'dynamic'"><Connection /></el-icon>
                  <el-icon v-else><Folder /></el-icon>
                </div>
                <div class="group-title">
                  <h3>{{ group.name }}</h3>
                  <el-tag v-if="group.type === 'smart'" size="small" type="success" effect="plain">智能</el-tag>
                  <el-tag v-else-if="group.type === 'dynamic'" size="small" type="warning" effect="plain">动态</el-tag>
                </div>
              </div>
              <div v-if="group.isImportant" class="group-important">
                <el-icon><Star /></el-icon>
              </div>
            </div>

            <div class="group-content">
              <div class="group-desc">{{ group.description || '暂无描述' }}</div>

              <!-- 规则提示（仅智能分组和动态分组显示） -->
              <div v-if="group.type !== 'normal'" class="group-rules">
                <div class="rule-title">
                  <el-icon v-if="group.type === 'smart'"><MagicStick /></el-icon>
                  <el-icon v-else-if="group.type === 'dynamic'"><Connection /></el-icon>
                  {{ group.type === 'smart' ? '智能规则' : '动态条件' }}：
                </div>
                <div class="rule-content">{{ group.rule || '无规则' }}</div>
              </div>

              <div class="group-stats">
                <div class="group-stat-item">
                  <div class="group-stat-icon"><el-icon><Monitor /></el-icon></div>
                  <div class="group-stat-value">{{ group.deviceCount }}</div>
                  <div class="group-stat-label">设备</div>
                </div>
                <div class="group-stat-item">
                  <div class="group-stat-icon" :class="{ 'online': group.onlineCount > 0 }">
                    <el-icon><VideoPlay /></el-icon>
                  </div>
                  <div class="group-stat-value">{{ group.onlineCount }}</div>
                  <div class="group-stat-label">在线</div>
                </div>
                <div class="group-stat-item">
                  <div class="group-stat-icon" :class="{ 'alert': group.alertCount > 0 }">
                    <el-icon><Warning /></el-icon>
                  </div>
                  <div class="group-stat-value">{{ group.alertCount }}</div>
                  <div class="group-stat-label">报警</div>
                </div>
              </div>
            </div>

            <div class="group-actions">
              <el-button-group>
                <el-button size="small" @click="handleViewDevices(group)">
                  <el-icon><View /></el-icon>查看设备
                </el-button>
                <el-button size="small" type="primary" @click="handleEditGroup(group)">
                  <el-icon><Edit /></el-icon>编辑
                </el-button>
                <el-button size="small" type="danger" @click="handleDeleteGroup(group)">
                  <el-icon><Delete /></el-icon>删除
                </el-button>
              </el-button-group>
            </div>
          </div>
        </el-col>
      </el-row>

      <!-- 空状态 -->
      <el-empty v-if="filteredGroups.length === 0" description="暂无设备分组" />
    </el-card>

    <!-- 添加/编辑分组对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="groupFormRef"
        :model="groupForm"
        :rules="groupRules"
        label-width="100px"
      >
        <el-form-item label="分组名称" prop="name">
          <el-input v-model="groupForm.name" placeholder="请输入分组名称" />
        </el-form-item>
        <el-form-item label="分组类型" prop="type">
          <el-radio-group v-model="groupForm.type">
            <el-radio label="normal">常规分组</el-radio>
            <el-radio label="smart">智能分组</el-radio>
            <el-radio label="dynamic">动态分组</el-radio>
          </el-radio-group>
        </el-form-item>
        
        <!-- 智能/动态分组规则 -->
        <template v-if="groupForm.type !== 'normal'">
          <el-form-item label="分组规则" prop="rule">
            <el-input 
              v-model="groupForm.rule" 
              type="textarea" 
              rows="3" 
              placeholder="请输入分组规则，例如：设备类型=摄像头 AND 状态=在线" 
            />
            <div class="rule-tip">{{ groupForm.type === 'smart' ? '智能分组根据规则自动分类设备，规则可以包含设备属性、状态等条件' : '动态分组基于设备实时状态，自动更新组内设备' }}</div>
          </el-form-item>
        </template>
        
        <el-form-item label="重要分组" prop="isImportant">
          <el-switch v-model="groupForm.isImportant" />
          <span class="form-tip">设置为重要分组后会在列表中醒目显示</span>
        </el-form-item>
        
        <!-- 常规分组设备选择 -->
        <el-form-item v-if="groupForm.type === 'normal'" label="选择设备" prop="deviceIds">
          <el-select
            v-model="groupForm.deviceIds"
            multiple
            filterable
            remote
            placeholder="请选择设备"
            :remote-method="searchDevices"
            :loading="deviceSearchLoading"
          >
            <el-option
              v-for="device in deviceOptions"
              :key="device.id"
              :label="device.name"
              :value="device.id"
            >
              <div class="device-option">
                <el-icon v-if="device.type === 'camera'"><VideoCamera /></el-icon>
                <el-icon v-else-if="device.type === 'sensor'"><Odometer /></el-icon>
                <el-icon v-else><Monitor /></el-icon>
                <span>{{ device.name }}</span>
                <el-tag size="small" :type="device.status === 'online' ? 'success' : 'danger'" effect="plain">
                  {{ device.status === 'online' ? '在线' : '离线' }}
                </el-tag>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input v-model="groupForm.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Plus, Edit, Delete, View, Search, List, Connection, MagicStick, Folder, FolderOpened,
  Monitor, VideoPlay, Warning, Star, VideoCamera, Odometer
} from '@element-plus/icons-vue'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 过滤类型
const filterType = ref('')

// 加载状态
const loading = ref(false)

// 分组数据
const groups = ref([])

// 分组统计
const groupStats = reactive({
  total: 12,
  deviceCount: 56,
  onlineCount: 42,
  offlineCount: 10,
  errorCount: 3,
  unknownCount: 1,
  onlineRate: 75
})

// 状态颜色
const statusColors = {
  online: '#67c23a',  // 成功色
  offline: '#e6a23c', // 警告色
  error: '#f56c6c',   // 危险色
  unknown: '#909399'  // 信息色
}

// 根据搜索关键词和类型过滤分组
const filteredGroups = computed(() => {
  let result = groups.value

  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(group => 
      group.name.toLowerCase().includes(keyword) || 
      (group.description && group.description.toLowerCase().includes(keyword))
    )
  }

  if (filterType.value) {
    result = result.filter(group => group.type === filterType.value)
  }

  return result
})

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '添加分组',
  type: 'add' // add, edit
})

// 表单引用
const groupFormRef = ref(null)

// 分组表单
const groupForm = reactive({
  id: null,
  name: '',
  type: 'normal',
  rule: '',
  isImportant: false,
  deviceIds: [],
  description: ''
})

// 表单验证规则
const groupRules = {
  name: [
    { required: true, message: '请输入分组名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  rule: [
    { required: value => ['smart', 'dynamic'].includes(groupForm.type), message: '请输入分组规则', trigger: 'blur' }
  ]
}

// 设备搜索状态
const deviceSearchLoading = ref(false)

// 设备选项
const deviceOptions = ref([])

// 获取分组列表
const getGroupList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    // Mock数据
    groups.value = [
      {
        id: 1,
        name: '前端摄像头',
        type: 'normal',
        description: '负责监控园区前门区域的摄像设备',
        deviceCount: 12,
        onlineCount: 10,
        alertCount: 1,
        isImportant: true
      },
      {
        id: 2,
        name: '后门摄像头',
        type: 'normal',
        description: '负责监控园区后门区域的摄像设备',
        deviceCount: 8,
        onlineCount: 7,
        alertCount: 0,
        isImportant: false
      },
      {
        id: 3,
        name: '离线设备',
        type: 'smart',
        description: '当前所有处于离线状态的设备',
        rule: '设备状态 = 离线',
        deviceCount: 10,
        onlineCount: 0,
        alertCount: 3,
        isImportant: true
      },
      {
        id: 4,
        name: '高清摄像头',
        type: 'smart',
        description: '分辨率在1080p以上的高清摄像头设备',
        rule: '设备类型 = 摄像头 AND 分辨率 >= 1080p',
        deviceCount: 15,
        onlineCount: 15,
        alertCount: 0,
        isImportant: false
      },
      {
        id: 5,
        name: '温湿度传感器',
        type: 'normal',
        description: '所有温湿度传感器设备',
        deviceCount: 6,
        onlineCount: 6,
        alertCount: 1,
        isImportant: false
      },
      {
        id: 6,
        name: '报警设备',
        type: 'dynamic',
        description: '实时显示当前有报警的设备',
        rule: '报警状态 = 活跃',
        deviceCount: 3,
        onlineCount: 3,
        alertCount: 3,
        isImportant: true
      },
      {
        id: 7,
        name: '移动摄像头',
        type: 'normal',
        description: '可移动/便携式摄像设备',
        deviceCount: 4,
        onlineCount: 3,
        alertCount: 0,
        isImportant: false
      },
      {
        id: 8,
        name: '生产线设备',
        type: 'normal',
        description: '生产线上的各类监控设备',
        deviceCount: 8,
        onlineCount: 8,
        alertCount: 0,
        isImportant: false
      }
    ]
    
    loading.value = false
  }, 500)
}

// 搜索
const handleSearch = () => {
  // 使用计算属性自动过滤
}

// 设备列表
const handleDeviceList = () => {
  router.push('/device')
}

// 添加分组
const handleAddGroup = () => {
  dialog.title = '添加分组'
  dialog.type = 'add'
  dialog.visible = true
}

// 编辑分组
const handleEditGroup = (group) => {
  dialog.title = '编辑分组'
  dialog.type = 'edit'
  Object.assign(groupForm, {
    id: group.id,
    name: group.name,
    type: group.type,
    rule: group.rule || '',
    isImportant: group.isImportant,
    deviceIds: [], // 实际中应该获取已有设备
    description: group.description || ''
  })
  dialog.visible = true
}

// 查看分组设备
const handleViewDevices = (group) => {
  ElMessage({
    type: 'info',
    message: `跳转到设备列表，并按分组"${group.name}"过滤`
  })
  // 实际项目中应该跳转到设备列表页面并携带过滤参数
  router.push({
    path: '/device',
    query: { groupId: group.id }
  })
}

// 删除分组
const handleDeleteGroup = (group) => {
  ElMessageBox.confirm(
    `确认删除分组"${group.name}"吗？`,
    '删除确认',
    {
      confirmButtonText: '确认',
      cancelButtonText: '取消',
      type: 'warning',
    }
  )
    .then(() => {
      // 模拟删除
      ElMessage({
        type: 'success',
        message: `分组"${group.name}"已删除`,
      })
      groups.value = groups.value.filter(item => item.id !== group.id)
    })
    .catch(() => {})
}

// 搜索设备
const searchDevices = (query) => {
  if (query) {
    deviceSearchLoading.value = true
    // 模拟API调用
    setTimeout(() => {
      deviceOptions.value = [
        { id: 1, name: '前门摄像头01', type: 'camera', status: 'online' },
        { id: 2, name: '前门摄像头02', type: 'camera', status: 'online' },
        { id: 3, name: '后门摄像头01', type: 'camera', status: 'offline' },
        { id: 4, name: '温湿度传感器01', type: 'sensor', status: 'online' },
        { id: 5, name: '温湿度传感器02', type: 'sensor', status: 'online' }
      ].filter(device => device.name.toLowerCase().includes(query.toLowerCase()))
      deviceSearchLoading.value = false
    }, 500)
  } else {
    deviceOptions.value = []
  }
}

// 重置表单
const resetForm = () => {
  if (groupFormRef.value) {
    groupFormRef.value.resetFields()
  }
  
  Object.assign(groupForm, {
    id: null,
    name: '',
    type: 'normal',
    rule: '',
    isImportant: false,
    deviceIds: [],
    description: ''
  })
}

// 提交表单
const submitForm = async () => {
  if (!groupFormRef.value) return
  
  await groupFormRef.value.validate((valid) => {
    if (valid) {
      // 模拟提交
      setTimeout(() => {
        ElMessage({
          type: 'success',
          message: dialog.type === 'edit' ? '修改成功' : '添加成功',
        })
        dialog.visible = false
        getGroupList()
      }, 500)
    }
  })
}

// 初始化
onMounted(() => {
  getGroupList()
})
</script>

<style scoped>
.device-groups-container {
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

.header-actions {
  display: flex;
  gap: 10px;
}

.stats-cards {
  margin-bottom: 20px;
}

.stats-card {
  height: 100%;
}

.stats-content {
  padding: 5px;
}

.stats-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 16px;
}

.stats-value-row {
  display: flex;
  align-items: center;
}

.stats-number {
  font-size: 36px;
  font-weight: bold;
  margin-right: 16px;
  color: var(--el-color-primary);
}

.stats-desc {
  font-size: 14px;
}

.stats-detail {
  color: var(--el-text-color-secondary);
  font-size: 12px;
  margin-top: 4px;
}

.stats-chart-row {
  display: flex;
  align-items: center;
  justify-content: space-around;
}

.progress-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
}

.progress-value {
  font-size: 16px;
  font-weight: bold;
}

.progress-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.stats-legend {
  margin-left: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  margin-bottom: 8px;
}

.legend-color {
  width: 12px;
  height: 12px;
  border-radius: 2px;
  margin-right: 8px;
}

.search-panel {
  display: flex;
  margin-bottom: 20px;
  gap: 16px;
  align-items: center;
}

.search-input {
  max-width: 300px;
}

.filter-actions {
  display: flex;
  gap: 10px;
}

.mb-20 {
  margin-bottom: 20px;
}

.group-card {
  height: 100%;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.3s;
  position: relative;
}

.group-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.group-type-smart {
  border-left: 3px solid var(--el-color-success);
}

.group-type-dynamic {
  border-left: 3px solid var(--el-color-warning);
}

.group-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.group-title-wrapper {
  display: flex;
  align-items: center;
}

.group-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.group-icon-normal {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
}

.group-icon-smart {
  background-color: var(--el-color-success-light-8);
  color: var(--el-color-success);
}

.group-icon-dynamic {
  background-color: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
}

.group-title {
  display: flex;
  align-items: center;
}

.group-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  line-height: 1.4;
  margin-right: 8px;
}

.group-important {
  color: var(--el-color-warning);
}

.group-desc {
  margin-bottom: 16px;
  color: var(--el-text-color-regular);
  font-size: 14px;
  min-height: 40px;
}

.group-rules {
  padding: 10px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
  margin-bottom: 16px;
  font-size: 14px;
}

.rule-title {
  display: flex;
  align-items: center;
  gap: 4px;
  font-weight: bold;
  color: var(--el-text-color-regular);
  margin-bottom: 4px;
}

.rule-content {
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.rule-tip {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.group-stats {
  display: flex;
  gap: 16px;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.group-stat-item {
  display: flex;
  align-items: center;
  gap: 4px;
}

.group-stat-icon {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--el-fill-color-light);
  color: var(--el-text-color-secondary);
}

.group-stat-icon.online {
  background-color: var(--el-color-success-light-8);
  color: var(--el-color-success);
}

.group-stat-icon.alert {
  background-color: var(--el-color-danger-light-8);
  color: var(--el-color-danger);
}

.group-stat-value {
  font-weight: bold;
  font-size: 14px;
}

.group-stat-label {
  color: var(--el-text-color-secondary);
  font-size: 12px;
}

.group-actions {
  position: absolute;
  left: 0;
  right: 0;
  bottom: 0;
  padding: 10px 16px;
  background: linear-gradient(to top, rgba(255, 255, 255, 0.9), rgba(255, 255, 255, 0));
  display: flex;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
  border-radius: 0 0 4px 4px;
}

.group-card:hover .group-actions {
  opacity: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.form-tip {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.device-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.device-option .el-tag {
  margin-left: auto;
}
</style>