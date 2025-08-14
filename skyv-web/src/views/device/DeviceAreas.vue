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
          placeholder="搜索区域名称或编码" 
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
      
      <!-- 加载状态 -->
      <div v-loading="loading">
        <!-- 卡片视图 -->
        <template v-if="viewType === 'card'">
          <el-row :gutter="20">
            <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="area in filteredAreas" :key="area.id" class="mb-20">
              <div class="area-card">
                <div class="area-header">
                  <div class="area-title-wrapper">
                    <div class="area-icon" :class="`area-icon-level-${area.level || 0}`">
                      <el-icon v-if="area.level === 0"><OfficeBuilding /></el-icon>
                      <el-icon v-else-if="area.level === 1"><CopyDocument /></el-icon>
                      <el-icon v-else-if="area.level === 2"><House /></el-icon>
                      <el-icon v-else><Location /></el-icon>
                    </div>
                    <div class="area-title">
                      <h3>{{ area.name }}</h3>
                      <div class="area-path">{{ area.fullPath || area.name }}</div>
                    </div>
                  </div>
                  <div class="area-status">
                    <el-tag size="small" type="success">正常</el-tag>
                  </div>
                </div>
                
                <div class="area-content">
                  <div class="area-desc">{{ area.description || '暂无描述' }}</div>
                  <div class="area-stats">
                    <div class="area-stat-item">
                      <div class="area-stat-value">{{ area.deviceCount || 0 }}</div>
                      <div class="area-stat-label">设备数</div>
                    </div>
                    <div class="area-stat-item">
                      <div class="area-stat-value">{{ area.level || 0 }}</div>
                      <div class="area-stat-label">层级</div>
                    </div>
                    <div class="area-stat-item">
                      <div class="area-stat-value">{{ area.children?.length || 0 }}</div>
                      <div class="area-stat-label">子区域</div>
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
          
          <!-- 空状态 -->
          <el-empty v-if="!loading && filteredAreas.length === 0" description="暂无区域数据" />
        </template>
        
        <!-- 树形视图 -->
        <template v-if="viewType === 'tree'">
          <el-tree 
            :data="areaTree" 
            :props="{ children: 'children', label: 'name' }"
            node-key="id"
            default-expand-all
            class="area-tree">
            <template #default="{ node, data }">
              <div class="custom-tree-node">
                <div class="tree-node-content">
                  <el-icon class="tree-icon">
                    <OfficeBuilding v-if="data.level === 0" />
                    <CopyDocument v-else-if="data.level === 1" />
                    <House v-else-if="data.level === 2" />
                    <Location v-else />
                  </el-icon>
                  <span>{{ data.name }}</span>
                  <span class="area-count">({{ data.deviceCount || 0 }}台设备)</span>
                  <el-tag size="small" type="success" class="area-status-tag">正常</el-tag>
                </div>
                <div class="tree-node-actions">
                  <el-button-group>
                    <el-button size="small" @click.stop="handleAddSubArea(data)">
                      <el-icon><Plus /></el-icon>
                    </el-button>
                    <el-button size="small" type="primary" @click.stop="handleEditArea(data)">
                      <el-icon><Edit /></el-icon>
                    </el-button>
                    <el-button size="small" type="danger" @click.stop="handleDeleteArea(data)">
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </el-button-group>
                </div>
              </div>
            </template>
          </el-tree>
          
          <!-- 空状态 -->
          <el-empty v-if="!loading && areaTree.length === 0" description="暂无区域数据" />
        </template>
      </div>
    </el-card>

    <!-- 区域表单对话框 -->
    <el-dialog v-model="dialog.visible" :title="dialog.title" width="600px" @close="resetForm">
      <el-form ref="areaFormRef" :model="areaForm" :rules="areaRules" label-width="100px">
        <el-form-item label="区域名称" prop="name">
          <el-input v-model="areaForm.name" placeholder="请输入区域名称" />
        </el-form-item>
        
        <el-form-item label="区域编码" prop="code">
          <el-input v-model="areaForm.code" placeholder="请输入区域编码（可选）" />
        </el-form-item>
        
        <el-form-item label="父区域" prop="parentId" v-if="dialog.type !== 'addSub'">
          <el-cascader
            v-model="areaForm.parentId"
            :options="areaOptions"
            :props="{ value: 'id', label: 'name', checkStrictly: true }"
            placeholder="请选择父区域（可选）"
            clearable
            style="width: 100%"
          />
        </el-form-item>
        
        <el-form-item label="排序" prop="sortOrder">
          <el-input-number v-model="areaForm.sortOrder" :min="0" placeholder="排序值" />
        </el-form-item>
        
        <el-form-item label="位置信息" prop="locationInfo">
          <el-input 
            v-model="areaForm.locationInfo" 
            type="textarea" 
            :rows="2"
            placeholder="请输入位置信息JSON（可选）" 
          />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input 
            v-model="areaForm.description" 
            type="textarea" 
            :rows="3"
            placeholder="请输入区域描述" 
          />
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 区域详情对话框 -->
    <el-dialog v-model="detailDialog.visible" title="区域详情" width="800px">
      <div class="area-detail">
        <div class="area-detail-title">
          <el-icon><Location /></el-icon>
          <span>{{ currentArea.name }}</span>
        </div>
        
        <el-descriptions :column="2" border>
          <el-descriptions-item label="区域名称">{{ currentArea.name }}</el-descriptions-item>
          <el-descriptions-item label="区域编码">{{ currentArea.code || '-' }}</el-descriptions-item>
          <el-descriptions-item label="完整路径">{{ currentArea.fullPath || '-' }}</el-descriptions-item>
          <el-descriptions-item label="层级">{{ currentArea.level || 0 }}</el-descriptions-item>
          <el-descriptions-item label="排序">{{ currentArea.sortOrder || 0 }}</el-descriptions-item>
          <el-descriptions-item label="创建时间">{{ currentArea.createdAt || '-' }}</el-descriptions-item>
          <el-descriptions-item label="描述" :span="2">{{ currentArea.description || '-' }}</el-descriptions-item>
        </el-descriptions>
        
        <div class="area-detail-stats">
          <div class="stats-cards">
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Monitor /></el-icon>
              </div>
              <div>
                <div class="stat-value">{{ currentArea.deviceCount || 0 }}</div>
                <div class="stat-label">设备总数</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><VideoCamera /></el-icon>
              </div>
              <div>
                <div class="stat-value">{{ currentArea.children?.length || 0 }}</div>
                <div class="stat-label">子区域</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Odometer /></el-icon>
              </div>
              <div>
                <div class="stat-value">{{ currentArea.level || 0 }}</div>
                <div class="stat-label">层级</div>
              </div>
            </div>
            <div class="stat-card">
              <div class="stat-icon">
                <el-icon><Key /></el-icon>
              </div>
              <div>
                <div class="stat-value">正常</div>
                <div class="stat-label">状态</div>
              </div>
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
  getDeviceAreas, 
  getDeviceAreaTree, 
  createDeviceArea, 
  updateDeviceArea, 
  deleteDeviceArea,
  validateAreaName,
  validateAreaCode
} from '@/api/device'
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
const submitting = ref(false)

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
  code: '',
  parentId: null,
  description: '',
  locationInfo: '',
  sortOrder: 0
})

// 表单验证规则
const areaRules = {
  name: [
    { required: true, message: '请输入区域名称', trigger: 'blur' },
    { min: 2, max: 100, message: '长度在 2 到 100 个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value && value.trim()) {
          try {
            const response = await validateAreaName(value.trim(), areaForm.id)
            if (response.code === 200 && !response.data.isUnique) {
              callback(new Error('区域名称已存在'))
            } else {
              callback()
            }
          } catch (error) {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  code: [
    { max: 50, message: '编码长度不能超过50个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value && value.trim()) {
          try {
            const response = await validateAreaCode(value.trim(), areaForm.id)
            if (response.code === 200 && !response.data.isUnique) {
              callback(new Error('区域编码已存在'))
            } else {
              callback()
            }
          } catch (error) {
            callback()
          }
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
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
    (area.code && area.code.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
    (area.fullPath && area.fullPath.toLowerCase().includes(searchKeyword.value.toLowerCase())) ||
    (area.description && area.description.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  )
})

// 获取区域列表
const getAreaList = async () => {
  try {
    loading.value = true
    
    const response = await getDeviceAreas()
    
    if (response.code === 200) {
      areas.value = response.data?.content || []
    } else {
      ElMessage.error(response.message || '获取设备区域失败')
      areas.value = []
    }
  } catch (error) {
    console.error('获取设备区域失败', error)
    ElMessage.error('获取设备区域失败：' + (error.message || '网络错误'))
    areas.value = []
  } finally {
    loading.value = false
  }
}

// 获取区域树形结构
const getAreaTreeData = async () => {
  try {
    const response = await getDeviceAreaTree()
    
    if (response.code === 200) {
      areaTree.value = response.data || []
      buildAreaOptions()
    } else {
      ElMessage.error(response.message || '获取区域树形结构失败')
      areaTree.value = []
    }
  } catch (error) {
    console.error('获取区域树形结构失败', error)
    ElMessage.error('获取区域树形结构失败：' + (error.message || '网络错误'))
    areaTree.value = []
  }
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
  getAreaList()
}

// 添加区域
const handleAddArea = () => {
  dialog.title = '添加区域'
  dialog.type = 'add'
  resetForm()
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
    code: area.code || '',
    parentId: area.parentId,
    description: area.description || '',
    locationInfo: area.locationInfo || '',
    sortOrder: area.sortOrder || 0
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
const handleDeleteArea = async (area) => {
  try {
    await ElMessageBox.confirm(
      `确认删除区域"${area.name}"吗？删除后其下所有子区域将一并删除！`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteDeviceArea(area.id)
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: `区域"${area.name}"已删除`,
      })
      getAreaList()
      getAreaTreeData()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除区域失败', error)
      ElMessage.error('删除失败：' + (error.message || '网络错误'))
    }
  }
}

// 重置表单
const resetForm = () => {
  if (areaFormRef.value) {
    areaFormRef.value.resetFields()
  }
  
  Object.assign(areaForm, {
    id: null,
    name: '',
    code: '',
    parentId: null,
    description: '',
    locationInfo: '',
    sortOrder: 0
  })
}

// 提交表单
const submitForm = async () => {
  if (!areaFormRef.value) return
  
  try {
    await areaFormRef.value.validate()
    
    submitting.value = true
    
    let response
    if (dialog.type === 'edit') {
      response = await updateDeviceArea(areaForm.id, areaForm)
    } else {
      response = await createDeviceArea(areaForm)
    }
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: dialog.type === 'edit' ? '修改成功' : '添加成功',
      })
      dialog.visible = false
      getAreaList()
      getAreaTreeData()
    } else {
      ElMessage.error(response.message || '操作失败')
    }
  } catch (error) {
    console.error('操作失败', error)
    ElMessage.error('操作失败：' + (error.message || '网络错误'))
  } finally {
    submitting.value = false
  }
}

// 初始化
onMounted(() => {
  getAreaList()
  getAreaTreeData()
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

.area-icon-level-0 {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
}

.area-icon-level-1 {
  background-color: var(--el-color-success-light-8);
  color: var(--el-color-success);
}

.area-icon-level-2 {
  background-color: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
}

.area-icon-level-3 {
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
  margin-bottom: 20px;
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