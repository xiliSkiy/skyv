<template>
  <div class="device-tags-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">标签管理</div>
          <div class="header-actions">
            <el-button @click="handleDeviceList">
              <el-icon><List /></el-icon>设备列表
            </el-button>
            <el-button type="primary" @click="handleAddTag">
              <el-icon><Plus /></el-icon>添加标签
            </el-button>
          </div>
        </div>
      </template>
      
      <!-- 标签过滤栏 -->
      <div class="tag-filter">
        <div class="filter-section">
          <div class="filter-tags">
            <el-tag
              v-for="tag in filterTags"
              :key="tag.id"
              :color="tag.bgColor"
              :style="{ color: tag.textColor }"
              class="filter-tag"
              @click="handleFilterByTag(tag)"
              :effect="selectedTags.includes(tag.id) ? 'dark' : 'plain'"
            >
              {{ tag.name }}
            </el-tag>
          </div>
          <el-button size="small" @click="clearFilter" :disabled="selectedTags.length === 0">
            清除筛选
          </el-button>
        </div>
        
        <div class="filter-controls">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索标签" 
            class="search-input"
            @keyup.enter="handleSearch"
            clearable
          >
            <template #prefix>
              <el-icon><Search /></el-icon>
            </template>
          </el-input>
          
          <el-select v-model="filterCategory" placeholder="标签类别" clearable>
            <el-option label="全部类别" value="" />
            <el-option label="状态标签" value="status" />
            <el-option label="特性标签" value="feature" />
            <el-option label="位置标签" value="location" />
            <el-option label="自定义标签" value="custom" />
          </el-select>
          
          <el-select v-model="sortOption" placeholder="排序方式">
            <el-option label="默认排序" value="default" />
            <el-option label="按设备数量" value="devices" />
            <el-option label="按名称" value="name" />
            <el-option label="按创建时间" value="time" />
          </el-select>
        </div>
      </div>
      
      <!-- 标签列表 -->
      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="tag in filteredTags" :key="tag.id" class="mb-20">
          <div class="tag-card" :style="{ borderLeftColor: tag.bgColor }">
            <div class="tag-header">
              <div class="tag-title">
                <div class="tag-color" :style="{ backgroundColor: tag.bgColor }"></div>
                <span>{{ tag.name }}</span>
                <el-tag 
                  v-if="tag.category" 
                  size="small" 
                  effect="plain" 
                  class="tag-category"
                >
                  {{ getCategoryLabel(tag.category) }}
                </el-tag>
              </div>
              <div class="tag-actions">
                <el-button-group>
                  <el-button size="small" @click="handleViewDevices(tag)">
                    <el-icon><View /></el-icon>
                  </el-button>
                  <el-button size="small" type="primary" @click="handleEditTag(tag)">
                    <el-icon><Edit /></el-icon>
                  </el-button>
                  <el-button size="small" type="danger" @click="handleDeleteTag(tag)">
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </el-button-group>
              </div>
            </div>
            <div class="tag-content">
              <div class="tag-description">{{ tag.description || '暂无描述' }}</div>
              
              <div class="tag-stats">
                <div class="tag-stat">
                  <el-icon><Monitor /></el-icon>
                  <span>{{ tag.deviceCount }} 台设备</span>
                </div>
                <div class="tag-stat">
                  <el-icon><Timer /></el-icon>
                  <span>{{ tag.createdTime }}</span>
                </div>
              </div>
              
              <!-- 标签样式预览 -->
              <div class="tag-preview">
                <span class="preview-label">样式预览：</span>
                <el-tag 
                  :color="tag.bgColor" 
                  :style="{ color: tag.textColor }"
                >
                  {{ tag.name }}
                </el-tag>
              </div>
              
              <!-- 最近设备列表 -->
              <div class="recent-devices" v-if="tag.recentDevices && tag.recentDevices.length > 0">
                <div class="section-title">最近关联设备</div>
                <div class="device-list">
                  <div v-for="device in tag.recentDevices" :key="device.id" class="device-item">
                    <div class="device-icon" :class="getDeviceTypeClass(device.type)">
                      <el-icon v-if="device.type === 'camera'"><VideoCamera /></el-icon>
                      <el-icon v-else-if="device.type === 'sensor'"><Odometer /></el-icon>
                      <el-icon v-else-if="device.type === 'access'"><Key /></el-icon>
                      <el-icon v-else><Monitor /></el-icon>
                    </div>
                    <div class="device-info">
                      <div class="device-name">{{ device.name }}</div>
                      <div class="device-status" :class="getStatusClass(device.status)">{{ getStatusText(device.status) }}</div>
                    </div>
                  </div>
                </div>
                <div class="view-more" v-if="tag.deviceCount > tag.recentDevices.length">
                  <el-button link type="primary" @click="handleViewDevices(tag)">
                    查看全部 {{ tag.deviceCount }} 台设备
                  </el-button>
                </div>
              </div>
              
              <div v-else class="no-devices">
                暂无关联设备
              </div>
            </div>
          </div>
        </el-col>
        
        <!-- 空状态 -->
        <el-col v-if="filteredTags.length === 0" :span="24">
          <el-empty description="暂无符合条件的标签" />
        </el-col>
      </el-row>
    </el-card>
    
    <!-- 添加/编辑标签对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="500px"
      destroy-on-close
      @closed="resetForm"
    >
      <el-form
        ref="tagFormRef"
        :model="tagForm"
        :rules="tagRules"
        label-width="80px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="tagForm.name" placeholder="请输入标签名称" />
        </el-form-item>
        
        <el-form-item label="类别" prop="category">
          <el-select v-model="tagForm.category" placeholder="请选择标签类别">
            <el-option label="状态标签" value="status" />
            <el-option label="特性标签" value="feature" />
            <el-option label="位置标签" value="location" />
            <el-option label="自定义标签" value="custom" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="颜色" prop="color">
          <div class="color-selector">
            <div 
              v-for="color in presetColors" 
              :key="color"
              :style="{ backgroundColor: color }"
              class="color-option"
              :class="{ selected: tagForm.color === color }"
              @click="selectColor(color)"
            ></div>
          </div>
          <el-color-picker v-model="tagForm.color" show-alpha />
        </el-form-item>
        
        <el-form-item label="描述" prop="description">
          <el-input v-model="tagForm.description" type="textarea" rows="3" placeholder="请输入描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="submitting">确认</el-button>
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
  getDeviceTags, 
  getAllDeviceTags,
  getPopularTags,
  createDeviceTag, 
  updateDeviceTag, 
  deleteDeviceTag,
  validateTagName
} from '@/api/device'
import { Plus, Edit, Delete, Search, List, Monitor, Timer,
  VideoCamera, Odometer, Key
} from '@element-plus/icons-vue'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 过滤分类
const filterCategory = ref('')

// 排序选项
const sortOption = ref('default')

// 选中的标签过滤器
const selectedTags = ref([])

// 加载状态
const loading = ref(false)
const submitting = ref(false)

// 标签数据
const tags = ref([])

// 热门标签
const popularTags = ref([])

// 当前标签
const currentTag = ref({})

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '添加标签',
  type: 'add' // add, edit
})

// 表单引用
const tagFormRef = ref(null)

// 标签表单
const tagForm = reactive({
  id: null,
  name: '',
  color: '#409EFF',
  description: ''
})

// 表单验证规则
const tagRules = {
  name: [
    { required: true, message: '请输入标签名称', trigger: 'blur' },
    { min: 1, max: 20, message: '长度在 1 到 20 个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value && value.trim()) {
          try {
            const response = await validateTagName(value.trim(), tagForm.id)
            if (response.code === 200 && !response.data.isUnique) {
              callback(new Error('标签名称已存在'))
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
  category: [
    { required: true, message: '请选择标签类别', trigger: 'change' }
  ],
  bgColor: [
    { required: true, message: '请选择标签颜色', trigger: 'change' }
  ],
  textColor: [
    { required: true, message: '请选择文本颜色', trigger: 'change' }
  ],
  description: [
    { max: 200, message: '描述长度不能超过200个字符', trigger: 'blur' }
  ]
}

// 预定义的颜色
const predefinedColors = [
  { bg: '#e3f2fd', text: '#0d47a1' }, // 蓝色
  { bg: '#e8f5e9', text: '#1b5e20' }, // 绿色
  { bg: '#fff3e0', text: '#e65100' }, // 橙色
  { bg: '#fce4ec', text: '#880e4f' }, // 粉色
  { bg: '#f3e5f5', text: '#4a148c' }, // 紫色
  { bg: '#e0f7fa', text: '#006064' }, // 青色
  { bg: '#fffde7', text: '#f57f17' }, // 黄色
  { bg: '#efebe9', text: '#3e2723' }, // 棕色
  { bg: '#fafafa', text: '#212121' }, // 灰色
  { bg: '#ffebee', text: '#b71c1c' }  // 红色
]

// 根据搜索关键词、分类和排序过滤标签
const filteredTags = computed(() => {
  let result = tags.value

  // 关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(tag => 
      tag.name.toLowerCase().includes(keyword) || 
      (tag.description && tag.description.toLowerCase().includes(keyword))
    )
  }

  // 标签分类过滤
  if (filterCategory.value) {
    result = result.filter(tag => tag.category === filterCategory.value)
  }
  
  // 标签过滤器筛选
  if (selectedTags.length > 0) {
    const selectedTagIds = selectedTags.value
    result = result.filter(tag => selectedTagIds.includes(tag.id))
  }

  // 排序
  if (sortOption.value === 'devices') {
    result = [...result].sort((a, b) => b.deviceCount - a.deviceCount)
  } else if (sortOption.value === 'name') {
    result = [...result].sort((a, b) => a.name.localeCompare(b.name))
  } else if (sortOption.value === 'time') {
    result = [...result].sort((a, b) => new Date(b.createdTime) - new Date(a.createdTime))
  }

  return result
})

// 获取类别标签
const getCategoryLabel = (category) => {
  const categories = {
    'status': '状态标签',
    'feature': '特性标签',
    'location': '位置标签',
    'custom': '自定义标签'
  }
  return categories[category] || '其他标签'
}

// 设备状态文本
const getStatusText = (status) => {
  const statusMap = {
    'online': '在线',
    'offline': '离线',
    'error': '故障',
    'maintenance': '维护中'
  }
  return statusMap[status] || status
}

// 设备状态类
const getStatusClass = (status) => {
  return `status-${status}`
}

// 设备类型类
const getDeviceTypeClass = (type) => {
  return `device-type-${type}`
}

// 获取标签列表
const getTagList = async () => {
  try {
    loading.value = true
    
    const response = await getDeviceTags()
    
    if (response.code === 200) {
      tags.value = response.data?.content || []
    } else {
      ElMessage.error(response.message || '获取设备标签失败')
      tags.value = []
    }
  } catch (error) {
    console.error('获取设备标签失败', error)
    ElMessage.error('获取设备标签失败：' + (error.message || '网络错误'))
    tags.value = []
  } finally {
    loading.value = false
  }
}

// 获取热门标签
const getPopularTagList = async () => {
  try {
    const response = await getPopularTags()
    
    if (response.code === 200) {
      popularTags.value = response.data || []
    } else {
      popularTags.value = []
    }
  } catch (error) {
    console.error('获取热门标签失败', error)
    popularTags.value = []
  }
}

// 搜索
const handleSearch = () => {
  getTagList()
}

// 标签过滤
const handleFilterByTag = (tag) => {
  const index = selectedTags.value.indexOf(tag.id)
  if (index === -1) {
    selectedTags.value.push(tag.id)
  } else {
    selectedTags.value.splice(index, 1)
  }
}

// 清除过滤
const clearFilter = () => {
  selectedTags.value = []
  searchKeyword.value = ''
  filterCategory.value = ''
}

// 设备列表
const handleDeviceList = () => {
  router.push('/device')
}

// 添加标签
const handleAddTag = () => {
  dialog.title = '添加标签'
  dialog.type = 'add'
  resetForm()
  dialog.visible = true
}

// 编辑标签
const handleEditTag = (tag) => {
  dialog.title = '编辑标签'
  dialog.type = 'edit'
  Object.assign(tagForm, {
    id: tag.id,
    name: tag.name,
    category: tag.category,
    bgColor: tag.bgColor,
    textColor: tag.textColor,
    description: tag.description || ''
  })
  dialog.visible = true
}

// 查看设备
const handleViewDevices = (tag) => {
  ElMessage({
    type: 'info',
    message: `跳转到设备列表，并按标签"${tag.name}"过滤`
  })
  // 实际项目中应该跳转到设备列表页面并携带过滤参数
  router.push({
    path: '/device',
    query: { tagId: tag.id }
  })
}

// 删除标签
const handleDeleteTag = async (tag) => {
  try {
    await ElMessageBox.confirm(
      `确认删除标签"${tag.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteDeviceTag(tag.id)
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: `标签"${tag.name}"已删除`,
      })
      getTagList()
      getPopularTagList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除标签失败', error)
      ElMessage.error('删除失败：' + (error.message || '网络错误'))
    }
  }
}

// 选择颜色
const selectColor = (color) => {
  tagForm.color = color
}

// 重置表单
const resetForm = () => {
  if (tagFormRef.value) {
    tagFormRef.value.resetFields()
  }
  
  Object.assign(tagForm, {
    id: null,
    name: '',
    category: 'custom',
    bgColor: '#e3f2fd',
    textColor: '#0d47a1',
    description: ''
  })
}

// 提交表单
const submitForm = async () => {
  if (!tagFormRef.value) return
  
  try {
    await tagFormRef.value.validate()
    
    submitting.value = true
    
    let response
    if (dialog.type === 'edit') {
      response = await updateDeviceTag(tagForm.id, tagForm)
    } else {
      response = await createDeviceTag(tagForm)
    }
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: dialog.type === 'edit' ? '修改成功' : '添加成功',
      })
      dialog.visible = false
      getTagList()
      getPopularTagList()
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
  getTagList()
  getPopularTagList()
})
</script>

<style scoped>
.device-tags-container {
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

.tag-filter {
  background-color: var(--el-fill-color-light);
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.filter-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.filter-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.filter-tag:hover {
  transform: scale(1.05);
}

.filter-controls {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  align-items: center;
}

.search-input {
  width: 240px;
}

.mb-20 {
  margin-bottom: 20px;
}

.tag-card {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
  border-left: 4px solid var(--el-color-primary);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.3s;
}

.tag-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.tag-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.tag-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.tag-color {
  width: 16px;
  height: 16px;
  border-radius: 4px;
}

.tag-category {
  margin-left: 4px;
}

.tag-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.tag-card:hover .tag-actions {
  opacity: 1;
}

.tag-content {
  padding: 16px;
}

.tag-description {
  min-height: 40px;
  margin-bottom: 16px;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.tag-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.tag-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.tag-preview {
  margin-bottom: 16px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.preview-label {
  font-size: 13px;
  color: var(--el-text-color-secondary);
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.device-list {
  background-color: var(--el-fill-color-lighter);
  border-radius: 4px;
  padding: 8px;
}

.device-item {
  display: flex;
  align-items: center;
  padding: 8px;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.device-item:last-child {
  border-bottom: none;
}

.device-icon {
  width: 32px;
  height: 32px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 8px;
  background-color: var(--el-fill-color-light);
}

.device-type-camera {
  background-color: var(--el-color-primary-light-8);
  color: var(--el-color-primary);
}

.device-type-sensor {
  background-color: var(--el-color-success-light-8);
  color: var(--el-color-success);
}

.device-type-access {
  background-color: var(--el-color-warning-light-8);
  color: var(--el-color-warning);
}

.device-info {
  flex: 1;
}

.device-name {
  font-size: 14px;
}

.device-status {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.status-online {
  color: var(--el-color-success);
}

.status-offline {
  color: var(--el-color-warning);
}

.status-error {
  color: var(--el-color-danger);
}

.status-maintenance {
  color: var(--el-color-info);
}

.view-more {
  margin-top: 8px;
  text-align: center;
}

.no-devices {
  text-align: center;
  padding: 16px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 4px;
  color: var(--el-text-color-secondary);
}

.color-selector {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 8px;
}

.color-option {
  width: 24px;
  height: 24px;
  border-radius: 4px;
  cursor: pointer;
  border: 2px solid transparent;
  transition: all 0.2s;
}

.color-option:hover {
  transform: scale(1.1);
}

.color-option.selected {
  border: 2px solid var(--el-color-primary);
  transform: scale(1.1);
}

.preview-box {
  margin-top: 8px;
  padding: 8px;
  background-color: var(--el-fill-color-lighter);
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style>