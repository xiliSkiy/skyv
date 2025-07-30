<template>
  <div class="device-areas-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">区域管理</div>
          <div class="header-actions">
            <el-button @click="handleViewMap">
              <el-icon><MapLocation /></el-icon>区域地图
            </el-button>
            <el-button type="primary" @click="handleAddArea">
              <el-icon><Plus /></el-icon>添加区域
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 区域视图切换 -->
      <div class="view-toggle mb-20">
        <el-radio-group v-model="viewType" size="large">
          <el-radio-button label="card">
            <el-icon><Grid /></el-icon>卡片视图
          </el-radio-button>
          <el-radio-button label="tree">
            <el-icon><Histogram /></el-icon>层级视图
          </el-radio-button>
        </el-radio-group>
      </div>

      <!-- 搜索条件 -->
      <div class="search-panel mb-20">
        <el-input 
          v-model="searchKeyword" 
          placeholder="搜索区域" 
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
      </div>
      
      <!-- 卡片视图 -->
      <template v-if="viewType === 'card'">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="area in filteredAreas" :key="area.id" class="mb-20">
            <div class="area-card">
              <div class="area-header">
                <div class="area-title-wrapper">
                  <div class="area-icon" :class="`area-icon-${area.type}`">
                    <el-icon v-if="area.type === 'building'"><OfficeBuilding /></el-icon>
                    <el-icon v-else-if="area.type === 'floor'"><CopyDocument /></el-icon>
                    <el-icon v-else-if="area.type === 'room'"><House /></el-icon>
                    <el-icon v-else><Location /></el-icon>
                  </div>
                  <div class="area-title">
                    <h3>{{ area.name }}</h3>
                    <div class="area-path">{{ area.path }}</div>
                  </div>
                </div>
                <div class="area-status">
                  <el-tag size="small" :type="area.status === '正常' ? 'success' : 'danger'">{{ area.status }}</el-tag>
                </div>
              </div>
              
              <div class="area-content">
                <div class="area-desc">{{ area.description || '暂无描述' }}</div>
                <div class="area-stats">
                  <div class="area-stat-item">
                    <div class="area-stat-value">{{ area.deviceCount }}</div>
                    <div class="area-stat-label">设备数</div>
                  </div>
                  <div class="area-stat-item">
                    <div class="area-stat-value">{{ area.onlineCount }}</div>
                    <div class="area-stat-label">在线</div>
                  </div>
                  <div class="area-stat-item">
                    <div class="area-stat-value">{{ area.alertCount }}</div>
                    <div class="area-stat-label">报警</div>
                  </div>
                </div>
              </div>
              
              <div class="area-actions">
                <el-button-group>
                  <el-button size="small" @click="handleViewArea(area)">
                    <el-icon><View /></el-icon>查看
                  </el-button>
                  <el-button size="small" type="primary" @click="handleEditArea(area)">
                    <el-icon><Edit /></el-icon>编辑
                  </el-button>
                  <el-button size="small" type="danger" @click="handleDeleteArea(area)">
                    <el-icon><Delete /></el-icon>删除
                  </el-button>
                </el-button-group>
              </div>
            </div>
          </el-col>
        </el-row>
      </template>

      <!-- 树形层级视图 -->
      <template v-else>
        <el-tree
          v-loading="loading"
          :data="areaTree"
          node-key="id"
          :props="{ label: 'name', children: 'children' }"
          default-expand-all
          :expand-on-click-node="false"
          highlight-current
        >
          <template #default="{ node, data }">
            <div class="custom-tree-node">
              <div class="tree-node-content">
                <el-icon v-if="data.type === 'building'" class="tree-icon"><OfficeBuilding /></el-icon>
                <el-icon v-else-if="data.type === 'floor'" class="tree-icon"><CopyDocument /></el-icon>
                <el-icon v-else-if="data.type === 'room'" class="tree-icon"><House /></el-icon>
                <el-icon v-else class="tree-icon"><Location /></el-icon>
                <span>{{ node.label }}</span>
                <span class="area-count">({{ data.deviceCount }})</span>
                <el-tag v-if="data.status !== '正常'" size="small" type="danger" class="area-status-tag">{{ data.status }}</el-tag>
              </div>
              
              <div class="tree-node-actions">
                <el-button size="small" text @click.stop="handleAddSubArea(data)">
                  <el-icon><Plus /></el-icon>添加子区域
                </el-button>
                <el-button size="small" text @click.stop="handleViewArea(data)">
                  <el-icon><View /></el-icon>
                </el-button>
                <el-button size="small" text type="primary" @click.stop="handleEditArea(data)">
                  <el-icon><Edit /></el-icon>
                </el-button>
                <el-button size="small" text type="danger" @click.stop="handleDeleteArea(data)">
                  <el-icon><Delete /></el-icon>
                </el-button>
              </div>
            </div>
          </template>
        </el-tree>
      </template>
    </el-card>

    <!-- 添加/编辑区域对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="areaFormRef"
        :model="areaForm"
        :rules="areaRules"
        label-width="100px"
      >
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="areaForm.name" placeholder="请输入区域名称" />
        </el-form-item>
        <el-form-item label="区域类型" prop="type">
          <el-select v-model="areaForm.type" placeholder="请选择区域类型">
            <el-option label="建筑" value="building">
              <div class="icon-option">
                <el-icon><OfficeBuilding /></el-icon>
                <span>建筑</span>
              </div>
            </el-option>
            <el-option label="楼层" value="floor">
              <div class="icon-option">
                <el-icon><CopyDocument /></el-icon>
                <span>楼层</span>
              </div>
            </el-option>
            <el-option label="房间" value="room">
              <div class="icon-option">
                <el-icon><House /></el-icon>
                <span>房间</span>
              </div>
            </el-option>
            <el-option label="区域" value="area">
              <div class="icon-option">
                <el-icon><Location /></el-icon>
                <span>区域</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="父级区域" prop="parentId">
          <el-cascader
            v-model="areaForm.parentId"
            :options="areaOptions"
            :props="{ checkStrictly: true, value: 'id', label: 'name', emitPath: false }"
            placeholder="顶级区域"
            clearable
          />
        </el-form-item>
        <el-form-item label="区域状态" prop="status">
          <el-select v-model="areaForm.status" placeholder="请选择区域状态">
            <el-option label="正常" value="正常" />
            <el-option label="维护中" value="维护中" />
            <el-option label="故障" value="故障" />
          </el-select>
        </el-form-item>
        <el-form-item label="区域地址" prop="address">
          <el-input v-model="areaForm.address" placeholder="请输入区域地址" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="areaForm.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确认</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 区域详情对话框 -->
    <el-dialog
      v-model="detailDialog.visible"
      title="区域详情"
      width="800px"
      destroy-on-close
    >
      <el-descriptions :column="2" border>
        <el-descriptions-item label="区域名称" :span="2">
          <div class="area-detail-title">
            <el-icon v-if="currentArea.type === 'building'"><OfficeBuilding /></el-icon>
            <el-icon v-else-if="currentArea.type === 'floor'"><CopyDocument /></el-icon>
            <el-icon v-else-if="currentArea.type === 'room'"><House /></el-icon>
            <el-icon v-else><Location /></el-icon>
            {{ currentArea.name }}
            <el-tag size="small" :type="currentArea.status === '正常' ? 'success' : 'danger'">
              {{ currentArea.status }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="区域类型">
          {{ {building: '建筑', floor: '楼层', room: '房间', area: '区域'}[currentArea.type] || '区域' }}
        </el-descriptions-item>
        <el-descriptions-item label="区域路径">{{ currentArea.path || '-' }}</el-descriptions-item>
        <el-descriptions-item label="设备数量">{{ currentArea.deviceCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="在线设备">{{ currentArea.onlineCount || 0 }}</el-descriptions-item>
        <el-descriptions-item label="区域地址" :span="2">{{ currentArea.address || '-' }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ currentArea.createdTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="更新时间">{{ currentArea.updatedTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="描述" :span="2">{{ currentArea.description || '暂无描述' }}</el-descriptions-item>
      </el-descriptions>
      
      <div class="area-detail-stats">
        <h4>设备统计</h4>
        <div class="stats-cards">
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><VideoCamera /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ currentArea.deviceTypeStats?.camera || 0 }}</div>
              <div class="stat-label">摄像头</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Odometer /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ currentArea.deviceTypeStats?.sensor || 0 }}</div>
              <div class="stat-label">传感器</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Key /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ currentArea.deviceTypeStats?.access || 0 }}</div>
              <div class="stat-label">门禁</div>
            </div>
          </div>
          <div class="stat-card">
            <div class="stat-icon">
              <el-icon><Monitor /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ currentArea.deviceTypeStats?.other || 0 }}</div>
              <div class="stat-label">其他设备</div>
            </div>
          </div>
        </div>
      </div>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="handleViewDevices(currentArea)">查看设备</el-button>
          <el-button type="primary" @click="detailDialog.visible = false">确定</el-button>
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
  Plus, Edit, Delete, View, Search, MapLocation, Grid, Histogram,
  OfficeBuilding, CopyDocument, House, Location, VideoCamera, Odometer, Key, Monitor
} from '@element-plus/icons-vue'

const router = useRouter()

// 视图类型：卡片/树形
const viewType = ref('card')

// 搜索关键词
const searchKeyword = ref('')

// 加载状态
const loading = ref(false)

// 区域数据
const areas = ref([])

// 区域树形数据
const areaTree = ref([])

// 当前区域
const currentArea = ref({})

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '添加区域',
  type: 'add' // add, edit, addSub
})

// 详情对话框状态
const detailDialog = reactive({
  visible: false
})

// 表单引用
const areaFormRef = ref(null)

// 区域表单
const areaForm = reactive({
  id: null,
  name: '',
  type: 'area',
  parentId: null,
  status: '正常',
  address: '',
  description: ''
})

// 表单验证规则
const areaRules = {
  name: [
    { required: true, message: '请输入区域名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择区域类型', trigger: 'change' }
  ]
}

// 区域选项（用于级联选择器）
const areaOptions = ref([])

// 根据搜索关键词过滤区域
const filteredAreas = computed(() => {
  if (!searchKeyword.value) {
    return areas.value
  }
  return areas.value.filter(area => 
    area.name.toLowerCase().includes(searchKeyword.value.toLowerCase()) || 
    (area.path && area.path.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
    (area.description && area.description.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

// 获取区域列表
const getAreaList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    // Mock数据
    areas.value = [
      {
        id: 1,
        name: '总部大楼',
        type: 'building',
        path: '/总部大楼',
        status: '正常',
        address: '北京市朝阳区科技园区88号',
        deviceCount: 120,
        onlineCount: 115,
        alertCount: 2,
        description: '公司总部办公大楼',
        createdTime: '2023-04-01 08:00:00',
        updatedTime: '2023-06-15 14:30:00',
        deviceTypeStats: {
          camera: 60,
          sensor: 40,
          access: 15,
          other: 5
        }
      },
      {
        id: 2,
        name: '研发中心',
        type: 'building',
        path: '/研发中心',
        status: '正常',
        address: '北京市海淀区中关村南大街5号',
        deviceCount: 80,
        onlineCount: 78,
        alertCount: 0,
        description: '研发团队办公场所',
        createdTime: '2023-04-15 09:00:00',
        updatedTime: '2023-06-20 11:20:00',
        deviceTypeStats: {
          camera: 30,
          sensor: 35,
          access: 10,
          other: 5
        }
      },
      {
        id: 3,
        name: '生产基地',
        type: 'building',
        path: '/生产基地',
        status: '维护中',
        address: '河北省廊坊市开发区创业路18号',
        deviceCount: 150,
        onlineCount: 130,
        alertCount: 5,
        description: '产品生产和装配工厂',
        createdTime: '2023-05-01 08:30:00',
        updatedTime: '2023-06-25 16:40:00',
        deviceTypeStats: {
          camera: 80,
          sensor: 50,
          access: 15,
          other: 5
        }
      },
      {
        id: 4,
        name: '数据中心',
        type: 'building',
        path: '/数据中心',
        status: '正常',
        address: '北京市顺义区后沙峪镇安福街10号',
        deviceCount: 200,
        onlineCount: 198,
        alertCount: 1,
        description: '公司核心数据存储与处理中心',
        createdTime: '2023-05-10 10:00:00',
        updatedTime: '2023-06-28 09:15:00',
        deviceTypeStats: {
          camera: 50,
          sensor: 120,
          access: 20,
          other: 10
        }
      },
      {
        id: 5,
        name: '物流仓库',
        type: 'building',
        path: '/物流仓库',
        status: '故障',
        address: '天津市武清区高村科技园区23号',
        deviceCount: 60,
        onlineCount: 48,
        alertCount: 8,
        description: '产品仓储和物流配送中心',
        createdTime: '2023-05-20 09:45:00',
        updatedTime: '2023-07-01 15:30:00',
        deviceTypeStats: {
          camera: 30,
          sensor: 20,
          access: 8,
          other: 2
        }
      }
    ]
    
    // 构建树形结构
    buildAreaTree()
    
    // 构建区域选项
    buildAreaOptions()
    
    loading.value = false
  }, 500)
}

// 构建树形结构
const buildAreaTree = () => {
  const treeData = [
    {
      id: 1,
      name: '总部大楼',
      type: 'building',
      status: '正常',
      deviceCount: 120,
      children: [
        {
          id: 11,
          name: '1层',
          type: 'floor',
          status: '正常',
          deviceCount: 30,
          children: [
            {
              id: 111,
              name: '大厅',
              type: 'room',
              status: '正常',
              deviceCount: 15
            },
            {
              id: 112,
              name: '安保室',
              type: 'room',
              status: '正常',
              deviceCount: 8
            },
            {
              id: 113,
              name: '会议室',
              type: 'room',
              status: '故障',
              deviceCount: 7
            }
          ]
        },
        {
          id: 12,
          name: '2层',
          type: 'floor',
          status: '正常',
          deviceCount: 40,
          children: [
            {
              id: 121,
              name: '办公区',
              type: 'room',
              status: '正常',
              deviceCount: 25
            },
            {
              id: 122,
              name: '休息区',
              type: 'room',
              status: '正常',
              deviceCount: 15
            }
          ]
        },
        {
          id: 13,
          name: '3层',
          type: 'floor',
          status: '维护中',
          deviceCount: 50,
          children: [
            {
              id: 131,
              name: '机房',
              type: 'room',
              status: '维护中',
              deviceCount: 35
            },
            {
              id: 132,
              name: '监控中心',
              type: 'room',
              status: '正常',
              deviceCount: 15
            }
          ]
        }
      ]
    },
    {
      id: 2,
      name: '研发中心',
      type: 'building',
      status: '正常',
      deviceCount: 80,
      children: [
        {
          id: 21,
          name: '1层',
          type: 'floor',
          status: '正常',
          deviceCount: 40
        },
        {
          id: 22,
          name: '2层',
          type: 'floor',
          status: '正常',
          deviceCount: 40
        }
      ]
    },
    {
      id: 3,
      name: '生产基地',
      type: 'building',
      status: '维护中',
      deviceCount: 150
    },
    {
      id: 4,
      name: '数据中心',
      type: 'building',
      status: '正常',
      deviceCount: 200
    },
    {
      id: 5,
      name: '物流仓库',
      type: 'building',
      status: '故障',
      deviceCount: 60
    }
  ]
  
  areaTree.value = treeData
}

// 构建区域选项（用于级联选择器）
const buildAreaOptions = () => {
  const options = []
  
  const buildOptions = (areas, options) => {
    areas.forEach(area => {
      const option = {
        id: area.id,
        name: area.name,
        children: []
      }
      
      if (area.children && area.children.length > 0) {
        buildOptions(area.children, option.children)
      }
      
      options.push(option)
    })
  }
  
  buildOptions(areaTree.value, options)
  areaOptions.value = options
}

// 搜索
const handleSearch = () => {
  // 实际项目中应该调用API
  console.log('搜索关键词:', searchKeyword.value)
}

// 添加区域
const handleAddArea = () => {
  dialog.title = '添加区域'
  dialog.type = 'add'
  dialog.visible = true
}

// 添加子区域
const handleAddSubArea = (parentArea) => {
  dialog.title = '添加子区域'
  dialog.type = 'addSub'
  resetForm()
  areaForm.parentId = parentArea.id
  dialog.visible = true
}

// 编辑区域
const handleEditArea = (area) => {
  dialog.title = '编辑区域'
  dialog.type = 'edit'
  Object.assign(areaForm, {
    id: area.id,
    name: area.name,
    type: area.type,
    parentId: null, // 实际中应该是父ID
    status: area.status,
    address: area.address || '',
    description: area.description || ''
  })
  dialog.visible = true
}

// 查看区域
const handleViewArea = (area) => {
  currentArea.value = area
  detailDialog.visible = true
}

// 查看区域设备
const handleViewDevices = (area) => {
  detailDialog.visible = false
  ElMessage({
    type: 'info',
    message: `跳转到设备列表，并按区域"${area.name}"过滤`
  })
  // 实际项目中应该跳转到设备列表页面并携带过滤参数
  router.push({
    path: '/device',
    query: { areaId: area.id }
  })
}

// 查看地图
const handleViewMap = () => {
  ElMessage({
    type: 'info',
    message: '区域地图功能开发中'
  })
}

// 删除区域
const handleDeleteArea = (area) => {
  ElMessageBox.confirm(
    `确认删除区域"${area.name}"吗？删除后其下所有子区域将一并删除！`,
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
        message: `区域"${area.name}"已删除`,
      })
      getAreaList()
    })
    .catch(() => {})
}

// 重置表单
const resetForm = () => {
  if (areaFormRef.value) {
    areaFormRef.value.resetFields()
  }
  
  Object.assign(areaForm, {
    id: null,
    name: '',
    type: 'area',
    parentId: null,
    status: '正常',
    address: '',
    description: ''
  })
}

// 提交表单
const submitForm = async () => {
  if (!areaFormRef.value) return
  
  await areaFormRef.value.validate((valid) => {
    if (valid) {
      // 模拟提交
      setTimeout(() => {
        ElMessage({
          type: 'success',
          message: dialog.type === 'edit' ? '修改成功' : '添加成功',
        })
        dialog.visible = false
        getAreaList()
      }, 500)
    }
  })
}

// 初始化
onMounted(() => {
  getAreaList()
})
</script>

<style scoped>
.device-areas-container {
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

.mb-20 {
  margin-bottom: 20px;
}

.search-input {
  max-width: 400px;
}

.view-toggle {
  margin-bottom: 20px;
}

.area-card {
  height: 100%;
  padding: 16px;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.3s;
  position: relative;
}

.area-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.area-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 16px;
}

.area-title-wrapper {
  display: flex;
  align-items: center;
}

.area-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--el-fill-color-light);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
}

.area-icon-building {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
}

.area-icon-floor {
  background-color: var(--el-color-success-light-8);
  color: var(--el-color-success);
}

.area-icon-room {
  background-color: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
}

.area-icon-area {
  background-color: var(--el-color-info-light-8);
  color: var(--el-color-info);
}

.area-title h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  line-height: 1.4;
}

.area-path {
  font-size: 12px;
  color: var(--el-text-color-secondary);
  margin-top: 2px;
}

.area-desc {
  margin-bottom: 16px;
  color: var(--el-text-color-regular);
  font-size: 14px;
  min-height: 40px;
}

.area-stats {
  display: flex;
  justify-content: space-between;
  margin-top: 16px;
  padding-top: 16px;
  border-top: 1px solid var(--el-border-color-lighter);
}

.area-stat-item {
  text-align: center;
  flex: 1;
}

.area-stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.area-stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.area-actions {
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

.area-card:hover .area-actions {
  opacity: 1;
}

.custom-tree-node {
  width: 100%;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
}

.tree-node-content {
  display: flex;
  align-items: center;
}

.tree-icon {
  margin-right: 6px;
}

.area-count {
  margin-left: 8px;
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.area-status-tag {
  margin-left: 8px;
}

.tree-node-actions {
  display: none;
}

.custom-tree-node:hover .tree-node-actions {
  display: block;
}

.area-detail-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: bold;
}

.area-detail-stats {
  margin-top: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
  margin-top: 10px;
}

.stat-card {
  display: flex;
  align-items: center;
  padding: 16px;
  background-color: var(--el-fill-color-light);
  border-radius: 4px;
}

.stat-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 18px;
}

.stat-value {
  font-size: 18px;
  font-weight: 600;
  color: var(--el-text-color-primary);
}

.stat-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.icon-option {
  display: flex;
  align-items: center;
  gap: 8px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>