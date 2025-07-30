<template>
  <div class="device-templates-container">
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <div class="header-title">设备模板管理</div>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAddTemplate">添加模板</el-button>
            <el-button :icon="List" @click="handleDeviceList">设备列表</el-button>
          </div>
        </div>
      </template>

      <!-- 过滤器 -->
      <div class="template-filter">
        <div class="filter-section">
          <div class="filter-title">模板分类</div>
          <div class="filter-categories">
            <el-radio-group v-model="filterCategory" size="small">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="official">官方模板</el-radio-button>
              <el-radio-button label="custom">自定义模板</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        
        <div class="filter-controls">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索模板名称或描述"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
          
          <el-select v-model="sortOption" placeholder="排序方式" size="default">
            <el-option label="默认排序" value="default" />
            <el-option label="按使用次数" value="usage" />
            <el-option label="按名称" value="name" />
            <el-option label="按更新时间" value="time" />
          </el-select>
          
          <el-button type="info" plain size="default" @click="clearFilter">清除过滤</el-button>
        </div>
      </div>
    </el-card>

    <!-- 模板列表 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="template in filteredTemplates" :key="template.id" class="mb-20">
        <el-card class="template-card">
          <div class="template-header">
            <div class="template-icon" :style="{ backgroundColor: template.iconBg, color: template.iconColor }">
              <el-icon><component :is="template.icon" /></el-icon>
            </div>
            <div class="template-info">
              <div class="template-title">
                {{ template.name }}
                <el-tag v-if="template.isOfficial" size="small" type="primary" class="ml-5">官方</el-tag>
              </div>
              <div class="template-subtitle">{{ template.subtitle }}</div>
            </div>
          </div>
          
          <div class="template-content">
            <div class="template-description">{{ template.description }}</div>
            
            <div class="template-stats">
              <div class="template-stat">
                <el-icon><Monitor /></el-icon>
                <span>使用次数: {{ template.usageCount }}</span>
              </div>
              <div class="template-stat">
                <el-icon><Timer /></el-icon>
                <span>更新时间: {{ template.updateTime }}</span>
              </div>
            </div>
            
            <div class="template-tags">
              <el-tag 
                v-for="tag in template.tags" 
                :key="tag" 
                size="small" 
                class="template-tag"
              >
                {{ tag }}
              </el-tag>
            </div>
            
            <div class="template-actions">
              <el-button type="primary" size="small" @click="handleUseTemplate(template)">使用模板</el-button>
              <el-button type="info" size="small" @click="handleEditTemplate(template)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDeleteTemplate(template)">删除</el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 空状态 -->
    <el-empty v-if="filteredTemplates.length === 0" description="暂无符合条件的模板" />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 模板表单对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form
        ref="templateFormRef"
        :model="templateForm"
        :rules="templateRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="模板名称" prop="name">
          <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
        </el-form-item>
        
        <el-form-item label="模板分类" prop="category">
          <el-select v-model="templateForm.category" placeholder="请选择模板分类" style="width: 100%">
            <el-option label="官方模板" value="official" />
            <el-option label="自定义模板" value="custom" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="适用设备类型" prop="deviceType">
          <el-select v-model="templateForm.deviceType" placeholder="请选择设备类型" style="width: 100%">
            <el-option label="摄像头" value="camera" />
            <el-option label="门禁" value="access" />
            <el-option label="传感器" value="sensor" />
            <el-option label="报警器" value="alarm" />
            <el-option label="录像机" value="nvr" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        
        <el-form-item label="模板标签">
          <el-select
            v-model="templateForm.tags"
            multiple
            filterable
            allow-create
            default-first-option
            placeholder="请输入或选择标签"
            style="width: 100%"
          >
            <el-option v-for="tag in availableTags" :key="tag" :label="tag" :value="tag" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="图标颜色">
          <el-color-picker v-model="templateForm.iconColor" />
          <el-color-picker v-model="templateForm.iconBg" />
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
  Plus, Edit, Delete, View, Search, List, Monitor, Timer,
  VideoCamera, Setting, Document, Connection
} from '@element-plus/icons-vue'

const router = useRouter()

// 搜索关键词
const searchKeyword = ref('')

// 过滤分类
const filterCategory = ref('')

// 排序选项
const sortOption = ref('default')

// 加载状态
const loading = ref(false)

// 模板数据
const templates = ref([])

// 根据搜索关键词、分类和排序过滤模板
const filteredTemplates = computed(() => {
  let result = templates.value

  // 关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(template => 
      template.name.toLowerCase().includes(keyword) || 
      (template.description && template.description.toLowerCase().includes(keyword))
    )
  }

  // 模板分类过滤
  if (filterCategory.value) {
    result = result.filter(template => 
      filterCategory.value === 'official' ? template.isOfficial : !template.isOfficial
    )
  }

  // 排序
  if (sortOption.value === 'usage') {
    result = [...result].sort((a, b) => b.usageCount - a.usageCount)
  } else if (sortOption.value === 'name') {
    result = [...result].sort((a, b) => a.name.localeCompare(b.name))
  } else if (sortOption.value === 'time') {
    result = [...result].sort((a, b) => new Date(b.updateTime) - new Date(a.updateTime))
  }

  return result
})

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '添加模板',
  type: 'add' // add, edit
})

// 表单引用
const templateFormRef = ref(null)

// 可用标签
const availableTags = ref([
  'RTSP', 'ONVIF', 'H.264', 'H.265', 'GB28181', 'TCP', 'UDP', 'HTTP', 'HTTPS', 'WiFi', '4G', '5G'
])

// 模板表单
const templateForm = reactive({
  id: null,
  name: '',
  category: 'custom',
  deviceType: '',
  description: '',
  tags: [],
  iconColor: '#FFFFFF',
  iconBg: '#409EFF'
})

// 表单验证规则
const templateRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  category: [
    { required: true, message: '请选择模板分类', trigger: 'change' }
  ],
  deviceType: [
    { required: true, message: '请选择适用设备类型', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入模板描述', trigger: 'blur' }
  ]
}

// 获取模板列表
const getTemplateList = () => {
  loading.value = true
  // 模拟API调用
  setTimeout(() => {
    // Mock数据
    templates.value = [
      {
        id: 1,
        name: '海康摄像头标准模板',
        subtitle: '适用于海康威视网络摄像头',
        description: '海康威视网络摄像头标准配置模板，包含基本参数和网络设置',
        isOfficial: true,
        icon: 'VideoCamera',
        iconBg: '#ecf5ff',
        iconColor: '#409EFF',
        usageCount: 15,
        updateTime: '2023-03-10',
        tags: ['RTSP', 'ONVIF', 'H.264']
      },
      {
        id: 2,
        name: '大华摄像头标准模板',
        subtitle: '适用于大华网络摄像头',
        description: '大华网络摄像头标准配置模板，包含基本参数和网络设置',
        isOfficial: true,
        icon: 'VideoCamera',
        iconBg: '#f0f9eb',
        iconColor: '#67C23A',
        usageCount: 8,
        updateTime: '2023-03-12',
        tags: ['RTSP', 'ONVIF', 'H.264']
      },
      {
        id: 3,
        name: '宇视摄像头标准模板',
        subtitle: '适用于宇视网络摄像头',
        description: '宇视网络摄像头标准配置模板，包含基本参数和网络设置',
        isOfficial: true,
        icon: 'VideoCamera',
        iconBg: '#fdf6ec',
        iconColor: '#E6A23C',
        usageCount: 6,
        updateTime: '2023-03-15',
        tags: ['RTSP', 'ONVIF', 'H.264']
      },
      {
        id: 4,
        name: '海康NVR标准模板',
        subtitle: '适用于海康威视网络录像机',
        description: '海康威视网络录像机标准配置模板，包含基本参数和网络设置',
        isOfficial: true,
        icon: 'Document',
        iconBg: '#fef0f0',
        iconColor: '#F56C6C',
        usageCount: 10,
        updateTime: '2023-03-20',
        tags: ['RTSP', 'ONVIF', 'H.264', 'NVR']
      },
      {
        id: 5,
        name: '温湿度传感器模板',
        subtitle: '适用于通用温湿度传感器',
        description: '通用温湿度传感器配置模板，包含基本参数和数据采集设置',
        isOfficial: true,
        icon: 'Setting',
        iconBg: '#f4f4f5',
        iconColor: '#909399',
        usageCount: 12,
        updateTime: '2023-03-25',
        tags: ['TCP', 'HTTP', 'Modbus']
      },
      {
        id: 6,
        name: '门禁控制器模板',
        subtitle: '适用于通用门禁控制器',
        description: '通用门禁控制器配置模板，包含基本参数和控制设置',
        isOfficial: true,
        icon: 'Connection',
        iconBg: '#f0f9eb',
        iconColor: '#67C23A',
        usageCount: 7,
        updateTime: '2023-04-01',
        tags: ['TCP', 'HTTP', 'RS485']
      },
      {
        id: 7,
        name: '自定义摄像头模板',
        subtitle: '用户自定义摄像头配置',
        description: '用户自定义的摄像头配置模板，适用于特定场景',
        isOfficial: false,
        icon: 'VideoCamera',
        iconBg: '#ecf5ff',
        iconColor: '#409EFF',
        usageCount: 3,
        updateTime: '2023-04-10',
        tags: ['RTSP', 'H.265', 'WiFi']
      },
      {
        id: 8,
        name: '4G摄像头模板',
        subtitle: '适用于4G网络摄像头',
        description: '4G网络摄像头配置模板，包含网络参数和流量控制',
        isOfficial: false,
        icon: 'VideoCamera',
        iconBg: '#fdf6ec',
        iconColor: '#E6A23C',
        usageCount: 5,
        updateTime: '2023-04-15',
        tags: ['RTSP', 'H.264', '4G']
      }
    ]
    loading.value = false
  }, 500)
}

// 搜索模板
const handleSearch = () => {
  // 使用计算属性自动过滤
}

// 清除过滤
const clearFilter = () => {
  searchKeyword.value = ''
  filterCategory.value = ''
  sortOption.value = 'default'
}

// 设备列表
const handleDeviceList = () => {
  router.push('/device')
}

// 添加模板
const handleAddTemplate = () => {
  dialog.title = '添加模板'
  dialog.type = 'add'
  dialog.visible = true
}

// 编辑模板
const handleEditTemplate = (template) => {
  dialog.title = '编辑模板'
  dialog.type = 'edit'
  Object.assign(templateForm, {
    id: template.id,
    name: template.name,
    category: template.isOfficial ? 'official' : 'custom',
    deviceType: template.deviceType || '',
    description: template.description || '',
    tags: template.tags ? [...template.tags] : [],
    iconColor: template.iconColor,
    iconBg: template.iconBg
  })
  dialog.visible = true
}

// 使用模板
const handleUseTemplate = (template) => {
  ElMessage({
    type: 'info',
    message: `使用模板"${template.name}"创建设备`,
  })
  // 实际项目中应该跳转到设备添加页面并携带模板参数
  router.push({
    path: '/device/add',
    query: { templateId: template.id }
  })
}

// 删除模板
const handleDeleteTemplate = (template) => {
  ElMessageBox.confirm(
    `确认删除模板"${template.name}"吗？`,
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
        message: `模板"${template.name}"已删除`,
      })
      templates.value = templates.value.filter(item => item.id !== template.id)
    })
    .catch(() => {})
}

// 重置表单
const resetForm = () => {
  if (templateFormRef.value) {
    templateFormRef.value.resetFields()
  }
  
  Object.assign(templateForm, {
    id: null,
    name: '',
    category: 'custom',
    deviceType: '',
    description: '',
    tags: [],
    iconColor: '#FFFFFF',
    iconBg: '#409EFF'
  })
}

// 提交表单
const submitForm = async () => {
  if (!templateFormRef.value) return
  
  await templateFormRef.value.validate((valid) => {
    if (valid) {
      // 模拟提交
      setTimeout(() => {
        ElMessage({
          type: 'success',
          message: dialog.type === 'edit' ? '修改成功' : '添加成功',
        })
        dialog.visible = false
        getTemplateList()
      }, 500)
    }
  })
}

// 初始化
onMounted(() => {
  getTemplateList()
})
</script>

<style scoped>
.device-templates-container {
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

.template-filter {
  background-color: var(--el-fill-color-light);
  padding: 16px;
  border-radius: 4px;
  margin-bottom: 20px;
}

.filter-section {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.filter-title {
  font-weight: 600;
  margin-right: 16px;
  min-width: 80px;
}

.filter-categories {
  flex: 1;
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

.ml-5 {
  margin-left: 5px;
}

.template-card {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.3s;
}

.template-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.template-header {
  padding: 16px;
  display: flex;
  align-items: center;
  border-bottom: 1px solid var(--el-border-color-lighter);
}

.template-icon {
  width: 48px;
  height: 48px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: 12px;
  font-size: 24px;
}

.template-info {
  flex: 1;
}

.template-title {
  font-size: 16px;
  font-weight: 600;
  display: flex;
  align-items: center;
}

.template-subtitle {
  font-size: 13px;
  color: var(--el-text-color-secondary);
  margin-top: 4px;
}

.template-content {
  padding: 16px;
}

.template-description {
  color: var(--el-text-color-regular);
  font-size: 14px;
  margin-bottom: 16px;
  min-height: 40px;
}

.template-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.template-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.template-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.template-tag {
  margin-right: 0;
}

.template-actions {
  display: flex;
  justify-content: space-between;
  border-top: 1px solid var(--el-border-color-lighter);
  padding-top: 16px;
}

.loading-container {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}
</style> 