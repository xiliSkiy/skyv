<template>
  <div class="device-protocols-container">
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <div class="header-title">设备协议管理</div>
          <div class="header-actions">
            <el-button type="primary" :icon="Plus" @click="handleAddProtocol">添加协议</el-button>
            <el-button :icon="List" @click="handleDeviceList">设备列表</el-button>
          </div>
        </div>
      </template>

      <!-- 过滤器 -->
      <div class="protocol-filter">
        <div class="filter-section">
          <div class="filter-title">协议分类</div>
          <div class="filter-categories">
            <el-radio-group v-model="filterCategory" size="small">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="standard">标准协议</el-radio-button>
              <el-radio-button label="proprietary">私有协议</el-radio-button>
              <el-radio-button label="custom">自定义协议</el-radio-button>
            </el-radio-group>
          </div>
        </div>
        
        <div class="filter-controls">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索协议名称或描述"
            class="search-input"
            :prefix-icon="Search"
            clearable
            @input="handleSearch"
          />
          
          <el-select v-model="sortOption" placeholder="排序方式" size="default">
            <el-option label="默认排序" value="default" />
            <el-option label="按设备数量" value="devices" />
            <el-option label="按名称" value="name" />
            <el-option label="按创建时间" value="time" />
          </el-select>
          
          <el-button type="info" plain size="default" @click="clearFilter">清除过滤</el-button>
        </div>
      </div>
    </el-card>

    <!-- 协议列表 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" :lg="6" v-for="protocol in filteredProtocols" :key="protocol.id" class="mb-20">
        <el-card class="protocol-card" :style="{ borderLeftColor: protocol.color }">
          <div class="protocol-header">
            <div class="protocol-title">
              <el-tag size="small" :type="getProtocolTagType(protocol.category)">{{ getProtocolCategoryLabel(protocol.category) }}</el-tag>
              <span>{{ protocol.name }}</span>
            </div>
            <div class="protocol-actions">
              <el-button type="primary" :icon="Edit" circle size="small" @click="handleEditProtocol(protocol)" />
              <el-button type="danger" :icon="Delete" circle size="small" @click="handleDeleteProtocol(protocol)" />
            </div>
          </div>
          
          <div class="protocol-content">
            <div class="protocol-description">{{ protocol.description || '暂无描述' }}</div>
            
            <div class="protocol-stats">
              <div class="protocol-stat">
                <el-icon><Monitor /></el-icon>
                <span>关联设备: {{ protocol.deviceCount }}个</span>
              </div>
              <div class="protocol-stat">
                <el-icon><Timer /></el-icon>
                <span>创建时间: {{ protocol.createdTime }}</span>
              </div>
            </div>
            
            <div class="protocol-details">
              <div class="detail-item">
                <span class="detail-label">版本:</span>
                <span class="detail-value">{{ protocol.version }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">端口:</span>
                <span class="detail-value">{{ protocol.port || 'N/A' }}</span>
              </div>
              <div class="detail-item">
                <span class="detail-label">传输方式:</span>
                <span class="detail-value">{{ getTransportLabel(protocol.transport) }}</span>
              </div>
            </div>
            
            <div class="section-title">设备类型支持</div>
            <div class="device-types">
              <el-tag 
                v-for="type in protocol.supportedTypes" 
                :key="type" 
                size="small" 
                class="device-type-tag"
              >
                {{ getDeviceTypeLabel(type) }}
              </el-tag>
              <div v-if="!protocol.supportedTypes || protocol.supportedTypes.length === 0" class="no-types">
                暂无支持的设备类型
              </div>
            </div>
            
            <div class="protocol-footer">
              <el-button type="primary" link size="small" @click="handleViewDevices(protocol)">
                查看关联设备
                <el-icon class="el-icon--right"><View /></el-icon>
              </el-button>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <!-- 空状态 -->
    <el-empty v-if="filteredProtocols.length === 0" description="暂无符合条件的协议" />

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 协议表单对话框 -->
    <el-dialog
      v-model="dialog.visible"
      :title="dialog.title"
      width="600px"
      destroy-on-close
      @close="resetForm"
    >
      <el-form
        ref="protocolFormRef"
        :model="protocolForm"
        :rules="protocolRules"
        label-width="100px"
        label-position="right"
      >
        <el-form-item label="协议名称" prop="name">
          <el-input v-model="protocolForm.name" placeholder="请输入协议名称" />
        </el-form-item>

        <el-form-item label="协议编码" prop="code">
          <el-input v-model="protocolForm.code" placeholder="请输入协议编码" />
        </el-form-item>
        
        <el-form-item label="协议分类" prop="category">
          <el-select v-model="protocolForm.category" placeholder="请选择协议分类" style="width: 100%">
            <el-option label="标准协议" value="standard" />
            <el-option label="私有协议" value="proprietary" />
            <el-option label="自定义协议" value="custom" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="协议版本" prop="version">
          <el-input v-model="protocolForm.version" placeholder="请输入协议版本" />
        </el-form-item>
        
        <el-form-item label="传输方式" prop="transport">
          <el-select v-model="protocolForm.transport" placeholder="请选择传输方式" style="width: 100%">
            <el-option label="TCP" value="tcp" />
            <el-option label="UDP" value="udp" />
            <el-option label="HTTP" value="http" />
            <el-option label="HTTPS" value="https" />
            <el-option label="串口" value="serial" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="端口号" prop="port">
          <el-input-number v-model="protocolForm.port" :min="0" :max="65535" placeholder="请输入端口号" style="width: 100%" />
        </el-form-item>
        
        <el-form-item label="协议颜色">
          <el-color-picker v-model="protocolForm.color" show-alpha />
        </el-form-item>
        
        <el-form-item label="支持设备类型">
          <el-select
            v-model="protocolForm.supportedTypes"
            multiple
            collapse-tags
            placeholder="请选择支持的设备类型"
            style="width: 100%"
          >
            <el-option label="摄像头" value="camera" />
            <el-option label="门禁" value="access" />
            <el-option label="传感器" value="sensor" />
            <el-option label="报警器" value="alarm" />
            <el-option label="录像机" value="nvr" />
            <el-option label="其他" value="other" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="协议描述" prop="description">
          <el-input
            v-model="protocolForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入协议描述"
          />
        </el-form-item>
        
        <el-form-item label="协议文档">
          <el-upload
            class="protocol-upload"
            action="#"
            :auto-upload="false"
            :limit="1"
          >
            <el-button type="primary">选择文件</el-button>
            <template #tip>
              <div class="el-upload__tip">
                仅支持PDF、WORD、TXT格式文件，大小不超过10MB
              </div>
            </template>
          </el-upload>
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
  getDeviceProtocols, 
  getAllEnabledProtocols,
  createDeviceProtocol, 
  updateDeviceProtocol, 
  deleteDeviceProtocol,
  validateProtocolCode,
  validateProtocolName
} from '@/api/device'
import { Plus, Edit, Delete, View, Search, List, Monitor, Timer,
  Connection, Document, Setting, Link
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

// 协议数据
const protocols = ref([])

// 根据搜索关键词、分类和排序过滤协议
const filteredProtocols = computed(() => {
  let result = protocols.value

  // 关键词过滤
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(protocol => 
      protocol.name.toLowerCase().includes(keyword) || 
      (protocol.description && protocol.description.toLowerCase().includes(keyword))
    )
  }

  // 协议分类过滤
  if (filterCategory.value) {
    result = result.filter(protocol => protocol.category === filterCategory.value)
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

// 对话框状态
const dialog = reactive({
  visible: false,
  title: '添加协议',
  type: 'add' // add, edit
})

// 表单引用
const protocolFormRef = ref(null)

// 协议表单
const protocolForm = reactive({
  id: null,
  name: '',
  code: '',
  category: 'standard',
  version: '1.0',
  transport: 'tcp',
  port: 0,
  color: '#409EFF',
  supportedTypes: [],
  description: ''
})

// 表单验证规则
const protocolRules = {
  name: [
    { required: true, message: '请输入协议名称', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入协议编码', trigger: 'blur' },
    { min: 1, max: 50, message: '长度在 1 到 50 个字符', trigger: 'blur' },
    {
      validator: async (_rule, value, callback) => {
        try {
          if (!value) return callback()
          const res = await validateProtocolCode(value, protocolForm.id)
          if (res.code === 200 && res.data && res.data.isUnique === false) {
            callback(new Error('协议编码已存在'))
          } else {
            callback()
          }
        } catch (e) {
          callback(new Error('编码校验失败'))
        }
      },
      trigger: 'blur'
    }
  ],
  category: [
    { required: true, message: '请选择协议分类', trigger: 'change' }
  ],
  version: [
    { required: true, message: '请输入协议版本', trigger: 'blur' }
  ],
  transport: [
    { required: true, message: '请选择传输方式', trigger: 'change' }
  ]
}

// 获取协议分类标签
const getProtocolCategoryLabel = (category) => {
  const categories = {
    'standard': '标准协议',
    'proprietary': '私有协议',
    'custom': '自定义协议'
  }
  return categories[category] || '未知'
}

// 获取协议标签类型
const getProtocolTagType = (category) => {
  const types = {
    'standard': 'success',
    'proprietary': 'warning',
    'custom': 'info'
  }
  return types[category] || ''
}

// 获取传输方式标签
const getTransportLabel = (transport) => {
  const transports = {
    'tcp': 'TCP',
    'udp': 'UDP',
    'http': 'HTTP',
    'https': 'HTTPS',
    'serial': '串口',
    'other': '其他'
  }
  return transports[transport] || transport
}

// 获取设备类型标签
const getDeviceTypeLabel = (type) => {
  const types = {
    'camera': '摄像头',
    'access': '门禁',
    'sensor': '传感器',
    'alarm': '报警器',
    'nvr': '录像机',
    'other': '其他'
  }
  return types[type] || type
}

// 获取协议列表
const getProtocolList = async () => {
  try {
    loading.value = true
    
    const response = await getDeviceProtocols({ page: 1, limit: 100 })
    
    if (response.code === 200) {
      const pageData = response.data || {}
      const content = Array.isArray(pageData.content) ? pageData.content : (Array.isArray(pageData) ? pageData : [])
      protocols.value = content.map(dto => ({
        id: dto.id,
        name: dto.name,
        description: dto.description,
        version: dto.version,
        port: dto.port,
        // 将后端 usageCount 映射为前端展示的“关联设备数”
        deviceCount: dto.usageCount || 0,
        // 使用后端创建时间
        createdTime: dto.createdAt,
        // 以下字段为前端展示所需的占位/默认值（后端未提供）
        category: 'standard',
        transport: 'tcp',
        color: '#409EFF',
        supportedTypes: []
      }))
    } else {
      ElMessage.error(response.message || '获取设备协议失败')
      protocols.value = []
    }
  } catch (error) {
    console.error('获取设备协议失败', error)
    ElMessage.error('获取设备协议失败：' + (error.message || '网络错误'))
    protocols.value = []
  } finally {
    loading.value = false
  }
}

// 搜索协议
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

// 添加协议
const handleAddProtocol = () => {
  dialog.title = '添加协议'
  dialog.type = 'add'
  dialog.visible = true
}

// 编辑协议
const handleEditProtocol = (protocol) => {
  dialog.title = '编辑协议'
  dialog.type = 'edit'
  Object.assign(protocolForm, {
    id: protocol.id,
    name: protocol.name,
    code: protocol.code,
    category: protocol.category,
    version: protocol.version,
    transport: protocol.transport,
    port: protocol.port,
    color: protocol.color,
    supportedTypes: protocol.supportedTypes ? [...protocol.supportedTypes] : [],
    description: protocol.description || ''
  })
  dialog.visible = true
}

// 查看设备
const handleViewDevices = (protocol) => {
  ElMessage({
    type: 'info',
    message: `跳转到设备列表，并按协议"${protocol.name}"过滤`
  })
  // 实际项目中应该跳转到设备列表页面并携带过滤参数
  router.push({
    path: '/device',
    query: { protocolId: protocol.id }
  })
}

// 删除协议
const handleDeleteProtocol = async (protocol) => {
  try {
    await ElMessageBox.confirm(
      `确认删除协议"${protocol.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确认',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )
    
    const response = await deleteDeviceProtocol(protocol.id)
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: `协议"${protocol.name}"已删除`,
      })
      getProtocolList()
    } else {
      ElMessage.error(response.message || '删除失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除协议失败', error)
      ElMessage.error('删除失败：' + (error.message || '网络错误'))
    }
  }
}

// 重置表单
const resetForm = () => {
  if (protocolFormRef.value) {
    protocolFormRef.value.resetFields()
  }
  
  Object.assign(protocolForm, {
    id: null,
    name: '',
    code: '',
    category: 'standard',
    version: '1.0',
    transport: 'tcp',
    port: 0,
    color: '#409EFF',
    supportedTypes: [],
    description: ''
  })
}

// 提交表单
const submitForm = async () => {
  if (!protocolFormRef.value) return
  
  try {
    await protocolFormRef.value.validate()
    
    submitting.value = true
    
    let response
    if (dialog.type === 'edit') {
      response = await updateDeviceProtocol(protocolForm.id, protocolForm)
    } else {
      response = await createDeviceProtocol(protocolForm)
    }
    
    if (response.code === 200) {
      ElMessage({
        type: 'success',
        message: dialog.type === 'edit' ? '修改成功' : '添加成功',
      })
      dialog.visible = false
      getProtocolList()
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
  getProtocolList()
})
</script>

<style scoped>
.device-protocols-container {
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

.protocol-filter {
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

.protocol-card {
  height: 100%;
  border: 1px solid var(--el-border-color-light);
  border-left: 4px solid var(--el-color-primary);
  border-radius: 4px;
  box-shadow: 0 1px 3px rgba(0,0,0,0.05);
  transition: all 0.3s;
}

.protocol-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}

.protocol-header {
  padding: 12px 16px;
  border-bottom: 1px solid var(--el-border-color-lighter);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.protocol-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.protocol-actions {
  opacity: 0;
  transition: opacity 0.3s;
}

.protocol-card:hover .protocol-actions {
  opacity: 1;
}

.protocol-content {
  padding: 16px;
}

.protocol-description {
  min-height: 40px;
  margin-bottom: 16px;
  color: var(--el-text-color-regular);
  font-size: 14px;
}

.protocol-stats {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-bottom: 16px;
}

.protocol-stat {
  display: flex;
  align-items: center;
  gap: 8px;
  color: var(--el-text-color-secondary);
  font-size: 13px;
}

.protocol-details {
  background-color: var(--el-fill-color-lighter);
  padding: 12px;
  border-radius: 4px;
  margin-bottom: 16px;
}

.detail-item {
  display: flex;
  margin-bottom: 8px;
  font-size: 13px;
}

.detail-item:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: var(--el-text-color-secondary);
  width: 80px;
}

.detail-value {
  color: var(--el-text-color-primary);
  font-weight: 500;
}

.section-title {
  font-size: 14px;
  font-weight: 600;
  margin-bottom: 8px;
}

.device-types {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.device-type-tag {
  margin-right: 0;
}

.no-types {
  color: var(--el-text-color-secondary);
  font-size: 13px;
  padding: 4px 0;
}

.protocol-footer {
  display: flex;
  justify-content: flex-end;
}

.loading-container {
  padding: 20px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.protocol-upload {
  width: 100%;
}
</style> 