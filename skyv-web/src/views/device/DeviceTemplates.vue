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
              <el-radio-button label="enabled">启用</el-radio-button>
              <el-radio-button label="disabled">禁用</el-radio-button>
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
              <el-button type="primary" size="small" @click="handleApplyTemplate(template)">使用模板</el-button>
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
        
        <el-form-item label="模板编码" prop="code">
          <el-input v-model="templateForm.code" placeholder="请输入模板编码" />
        </el-form-item>
        
        <el-form-item label="设备类型" prop="deviceTypeId">
          <el-select v-model="templateForm.deviceTypeId" placeholder="请选择设备类型" style="width: 100%">
            <el-option
              v-for="item in deviceTypes"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="协议" prop="protocolId">
          <el-select v-model="templateForm.protocolId" placeholder="请选择协议" style="width: 100%">
            <el-option
              v-for="item in protocols"
              :key="item.id"
              :label="item.name"
              :value="item.id"
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="制造商" prop="manufacturer">
          <el-input v-model="templateForm.manufacturer" placeholder="请输入制造商" />
        </el-form-item>
        
        <el-form-item label="型号" prop="model">
          <el-input v-model="templateForm.model" placeholder="请输入型号" />
        </el-form-item>
        
        <el-form-item label="配置模板" prop="configTemplate">
          <el-input
            v-model="templateForm.configTemplate"
            type="textarea"
            rows="3"
            placeholder="请输入配置模板"
          />
        </el-form-item>
        
        <el-form-item label="默认设置" prop="defaultSettings">
          <el-input
            v-model="templateForm.defaultSettings"
            type="textarea"
            rows="3"
            placeholder="请输入默认设置"
          />
        </el-form-item>
        
        <el-form-item label="指标" prop="metrics">
          <el-input
            v-model="templateForm.metrics"
            type="textarea"
            rows="3"
            placeholder="请输入指标"
          />
        </el-form-item>
        
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        
        <el-form-item label="启用状态" prop="isEnabled">
          <el-switch v-model="templateForm.isEnabled" />
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
  getDeviceTemplates, 
  getAllEnabledTemplates,
  getTemplatesByDeviceType,
  createDeviceTemplate, 
  updateDeviceTemplate, 
  deleteDeviceTemplate,
  validateTemplateCode,
  validateTemplateName,
  getDeviceTypes,
  getAllEnabledProtocols
} from '@/api/device'
import {
  Plus, Edit, Delete, View, Search, List,
  VideoCamera, Odometer, Key, Monitor, Setting, Star, Download
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
const submitting = ref(false)

// 模板数据
const templates = ref([])

// 设备类型列表
const deviceTypes = ref([])

// 协议列表
const protocols = ref([])

// 根据搜索关键词、分类和排序过滤模板
const filteredTemplates = computed(() => {
  let result = templates.value

  // 关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(template => 
      template.name.toLowerCase().includes(keyword) || 
      template.code.toLowerCase().includes(keyword) ||
      (template.description && template.description.toLowerCase().includes(keyword)) ||
      (template.manufacturer && template.manufacturer.toLowerCase().includes(keyword))
    )
  }

  // 启用状态过滤
  if (filterCategory.value === 'enabled') {
    result = result.filter(template => template.isEnabled)
  } else if (filterCategory.value === 'disabled') {
    result = result.filter(template => !template.isEnabled)
  }

  // 排序
  if (sortOption.value === 'usage') {
    result = [...result].sort((a, b) => (b.usageCount || 0) - (a.usageCount || 0))
  } else if (sortOption.value === 'name') {
    result = [...result].sort((a, b) => a.name.localeCompare(b.name))
  } else if (sortOption.value === 'time') {
    result = [...result].sort((a, b) => new Date(b.updatedAt || 0) - new Date(a.updatedAt || 0))
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

// 模板表单
const templateForm = reactive({
  id: null,
  name: '',
  code: '',
  deviceTypeId: null,
  protocolId: null,
  manufacturer: '',
  model: '',
  configTemplate: '',
  defaultSettings: '',
  metrics: '',
  description: '',
  isEnabled: true
})

// 表单验证规则
const templateRules = {
  name: [
    { required: true, message: '请输入模板名称', trigger: 'blur' },
    { min: 1, max: 100, message: '长度在 1 到 100 个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value && value.trim()) {
          try {
            const response = await validateTemplateName(value.trim(), templateForm.id)
            if (response.code === 200 && !response.data.isUnique) {
              callback(new Error('模板名称已存在'))
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
    { required: true, message: '请输入模板编码', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' },
    {
      validator: async (rule, value, callback) => {
        if (value && value.trim()) {
          try {
            const response = await validateTemplateCode(value.trim(), templateForm.id)
            if (response.code === 200 && !response.data.isUnique) {
              callback(new Error('模板编码已存在'))
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
  deviceTypeId: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ],
  description: [
    { max: 500, message: '描述长度不能超过500个字符', trigger: 'blur' }
  ]
}

// 获取模板列表
const getTemplateList = async () => {
  try {
    loading.value = true
    
    const response = await getDeviceTemplates()
    
    if (response.code === 200) {
      templates.value = response.data?.content || []
    } else {
      ElMessage.error(response.message || '获取设备模板失败')
      templates.value = []
    }
  } catch (error) {
    console.error('获取设备模板失败', error)
    ElMessage.error('获取设备模板失败：' + (error.message || '网络错误'))
    templates.value = []
  } finally {
    loading.value = false
  }
}

// 获取设备类型列表
const getDeviceTypeList = async () => {
  try {
    const response = await getDeviceTypes()
    if (response.code === 200) {
      const data = response.data
      deviceTypes.value = Array.isArray(data) ? data : (data?.content || [])
    }
  } catch (error) {
    console.error('获取设备类型失败', error)
  }
}

// 获取协议列表
const getProtocolList = async () => {
  try {
    const response = await getAllEnabledProtocols()
    if (response.code === 200) {
      protocols.value = response.data || []
    }
  } catch (error) {
    console.error('获取协议列表失败', error)
  }
}

// 搜索模板
const handleSearch = () => {
  getTemplateList()
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
  resetForm()
  dialog.visible = true
}

// 编辑模板
const handleEditTemplate = (template) => {
  dialog.title = '编辑模板'
  dialog.type = 'edit'
  Object.assign(templateForm, {
    id: template.id,
    name: template.name,
    code: template.code,
    deviceTypeId: template.deviceTypeId,
    protocolId: template.protocolId,
    manufacturer: template.manufacturer || '',
    model: template.model || '',
    configTemplate: template.configTemplate || '',
    defaultSettings: template.defaultSettings || '',
    metrics: template.metrics || '',
    description: template.description || '',
    isEnabled: template.isEnabled
  })
  dialog.visible = true
}

// 查看模板
const handleViewTemplate = (template) => {
  ElMessage({
    type: 'info',
    message: `查看模板"${template.name}"的详细信息`
  })
}

// 应用模板
const handleApplyTemplate = (template) => {
  ElMessage({
    type: 'info',
    message: `使用模板"${template.name}"创建设备`
  })
  router.push({
    path: '/device/add',
    query: { templateId: template.id }
  })
}

// 删除模板
const handleDeleteTemplate = async (template) => {
  try {
    await ElMessageBox.confirm(
      `确认删除模板"${template.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteDeviceTemplate(template.id)
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: `模板"${template.name}"已删除`,
      })
      getTemplateList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除模板失败', error)
      ElMessage.error('删除失败：' + (error.message || '网络错误'))
    }
  }
}

// 重置表单
const resetForm = () => {
  if (templateFormRef.value) {
    templateFormRef.value.resetFields()
  }
  
  Object.assign(templateForm, {
    id: null,
    name: '',
    code: '',
    deviceTypeId: null,
    protocolId: null,
    manufacturer: '',
    model: '',
    configTemplate: '',
    defaultSettings: '',
    metrics: '',
    description: '',
    isEnabled: true
  })
}

// 提交表单
const submitForm = async () => {
  if (!templateFormRef.value) return
  
  try {
    await templateFormRef.value.validate()
    
    submitting.value = true
    
    let response
    if (dialog.type === 'edit') {
      response = await updateDeviceTemplate(templateForm.id, templateForm)
    } else {
      response = await createDeviceTemplate(templateForm)
    }
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: dialog.type === 'edit' ? '修改成功' : '添加成功',
      })
      dialog.visible = false
      getTemplateList()
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
  getTemplateList()
  getDeviceTypeList()
  getProtocolList()
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