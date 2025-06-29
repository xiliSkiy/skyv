<template>
  <div class="device-template-add-container">
    <el-card class="mb-20">
      <template #header>
        <div class="card-header">
          <div class="header-title">{{ isEdit ? '编辑设备模板' : '添加设备模板' }}</div>
          <div class="header-actions">
            <el-button :icon="Back" @click="handleBack">返回</el-button>
            <el-button type="primary" :icon="Check" @click="handleSave">保存</el-button>
          </div>
        </div>
      </template>

      <el-form
        ref="formRef"
        :model="templateForm"
        :rules="rules"
        label-width="120px"
        label-position="right"
        class="template-form"
      >
        <el-divider content-position="left">基本信息</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="模板名称" prop="name">
              <el-input v-model="templateForm.name" placeholder="请输入模板名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="模板分类" prop="category">
              <el-select v-model="templateForm.category" placeholder="请选择模板分类" style="width: 100%">
                <el-option label="官方模板" value="official" />
                <el-option label="自定义模板" value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
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
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备厂商" prop="manufacturer">
              <el-select 
                v-model="templateForm.manufacturer" 
                placeholder="请选择设备厂商"
                filterable
                allow-create
                style="width: 100%"
              >
                <el-option label="海康威视" value="hikvision" />
                <el-option label="大华" value="dahua" />
                <el-option label="宇视" value="uniview" />
                <el-option label="华为" value="huawei" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="模板描述" prop="description">
          <el-input
            v-model="templateForm.description"
            type="textarea"
            rows="3"
            placeholder="请输入模板描述"
          />
        </el-form-item>
        
        <el-row :gutter="20">
          <el-col :span="12">
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
          </el-col>
          <el-col :span="12">
            <el-form-item label="图标颜色">
              <div class="color-pickers">
                <div class="color-picker-item">
                  <span class="color-label">图标颜色</span>
                  <el-color-picker v-model="templateForm.iconColor" />
                </div>
                <div class="color-picker-item">
                  <span class="color-label">背景颜色</span>
                  <el-color-picker v-model="templateForm.iconBg" />
                </div>
                <div class="color-preview" :style="{ backgroundColor: templateForm.iconBg }">
                  <el-icon :style="{ color: templateForm.iconColor }">
                    <component :is="getDeviceTypeIcon(templateForm.deviceType)" />
                  </el-icon>
                </div>
              </div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-divider content-position="left">网络参数</el-divider>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="连接协议" prop="protocol">
              <el-select v-model="templateForm.protocol" placeholder="请选择连接协议" style="width: 100%">
                <el-option label="ONVIF" value="onvif" />
                <el-option label="RTSP" value="rtsp" />
                <el-option label="HTTP" value="http" />
                <el-option label="GB/T 28181" value="gb28181" />
                <el-option label="私有SDK" value="sdk" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="传输方式" prop="transport">
              <el-select v-model="templateForm.transport" placeholder="请选择传输方式" style="width: 100%">
                <el-option label="TCP" value="tcp" />
                <el-option label="UDP" value="udp" />
                <el-option label="HTTP" value="http" />
                <el-option label="HTTPS" value="https" />
                <el-option label="其他" value="other" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="默认端口" prop="port">
              <el-input-number v-model="templateForm.port" :min="1" :max="65535" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="默认用户名" prop="username">
              <el-input v-model="templateForm.username" placeholder="请输入默认用户名" />
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-form-item label="URL模式" prop="urlPattern">
          <el-input v-model="templateForm.urlPattern" placeholder="例如: rtsp://{username}:{password}@{ip}:{port}/cam/realmonitor" />
        </el-form-item>

        <el-divider content-position="left">高级配置</el-divider>
        
        <el-form-item label="参数配置" prop="parameters">
          <el-table :data="templateForm.parameters" style="width: 100%" border>
            <el-table-column prop="name" label="参数名称" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.name" placeholder="参数名称" />
              </template>
            </el-table-column>
            <el-table-column prop="value" label="默认值" min-width="150">
              <template #default="scope">
                <el-input v-model="scope.row.value" placeholder="默认值" />
              </template>
            </el-table-column>
            <el-table-column prop="description" label="说明" min-width="200">
              <template #default="scope">
                <el-input v-model="scope.row.description" placeholder="参数说明" />
              </template>
            </el-table-column>
            <el-table-column label="操作" width="100" align="center">
              <template #default="scope">
                <el-button 
                  type="danger" 
                  :icon="Delete" 
                  circle 
                  size="small" 
                  @click="removeParameter(scope.$index)"
                />
              </template>
            </el-table-column>
          </el-table>
          <div class="add-parameter">
            <el-button type="primary" :icon="Plus" @click="addParameter">添加参数</el-button>
          </div>
        </el-form-item>
        
        <el-form-item label="模板文档">
          <el-upload
            class="template-upload"
            action="#"
            :auto-upload="false"
            :limit="1"
            :file-list="fileList"
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
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Back, Check, Plus, Delete, VideoCamera, Connection,
  Setting, Document, Monitor, Timer, View
} from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()

// 是否为编辑模式
const isEdit = computed(() => {
  return route.query.id !== undefined
})

// 表单引用
const formRef = ref(null)

// 可用标签
const availableTags = ref([
  'RTSP', 'ONVIF', 'H.264', 'H.265', 'GB28181', 'TCP', 'UDP', 'HTTP', 'HTTPS', 'WiFi', '4G', '5G'
])

// 上传文件列表
const fileList = ref([])

// 模板表单
const templateForm = reactive({
  id: null,
  name: '',
  category: 'custom',
  deviceType: 'camera',
  manufacturer: '',
  description: '',
  tags: [],
  iconColor: '#FFFFFF',
  iconBg: '#409EFF',
  protocol: 'onvif',
  transport: 'tcp',
  port: 80,
  username: 'admin',
  urlPattern: '',
  parameters: []
})

// 表单验证规则
const rules = {
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
  ],
  protocol: [
    { required: true, message: '请选择连接协议', trigger: 'change' }
  ],
  transport: [
    { required: true, message: '请选择传输方式', trigger: 'change' }
  ],
  port: [
    { required: true, message: '请输入默认端口', trigger: 'blur' }
  ]
}

// 根据设备类型获取图标
const getDeviceTypeIcon = (type) => {
  const icons = {
    'camera': 'VideoCamera',
    'access': 'Connection',
    'sensor': 'Setting',
    'alarm': 'Bell',
    'nvr': 'Document',
    'other': 'Monitor'
  }
  return icons[type] || 'Monitor'
}

// 添加参数
const addParameter = () => {
  templateForm.parameters.push({
    name: '',
    value: '',
    description: ''
  })
}

// 删除参数
const removeParameter = (index) => {
  templateForm.parameters.splice(index, 1)
}

// 返回
const handleBack = () => {
  router.push('/device/templates')
}

// 保存
const handleSave = async () => {
  if (!formRef.value) return
  
  await formRef.value.validate((valid) => {
    if (valid) {
      // 模拟保存
      setTimeout(() => {
        ElMessage({
          type: 'success',
          message: isEdit.value ? '模板修改成功' : '模板添加成功',
        })
        router.push('/device/templates')
      }, 500)
    }
  })
}

// 获取模板详情
const getTemplateDetail = (id) => {
  // 模拟API调用
  setTimeout(() => {
    // Mock数据
    Object.assign(templateForm, {
      id: 1,
      name: '海康摄像头标准模板',
      category: 'official',
      deviceType: 'camera',
      manufacturer: 'hikvision',
      description: '海康威视网络摄像头标准配置模板，包含基本参数和网络设置',
      tags: ['RTSP', 'ONVIF', 'H.264'],
      iconColor: '#FFFFFF',
      iconBg: '#409EFF',
      protocol: 'onvif',
      transport: 'tcp',
      port: 80,
      username: 'admin',
      urlPattern: 'rtsp://{username}:{password}@{ip}:{port}/Streaming/Channels/101',
      parameters: [
        { name: 'streamType', value: 'main', description: '流类型：main(主码流)，sub(子码流)' },
        { name: 'channel', value: '1', description: '通道号' },
        { name: 'videoCodec', value: 'H.264', description: '视频编码格式' }
      ]
    })
  }, 500)
}

// 初始化
onMounted(() => {
  // 添加一个默认参数行
  if (templateForm.parameters.length === 0) {
    addParameter()
  }
  
  // 如果是编辑模式，获取模板详情
  if (isEdit.value && route.query.id) {
    getTemplateDetail(route.query.id)
  }
})
</script>

<style scoped>
.device-template-add-container {
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

.template-form {
  padding: 10px;
}

.color-pickers {
  display: flex;
  align-items: center;
  gap: 16px;
}

.color-picker-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.color-label {
  font-size: 12px;
  color: var(--el-text-color-secondary);
}

.color-preview {
  width: 40px;
  height: 40px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  margin-left: 10px;
}

.add-parameter {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

.template-upload {
  width: 100%;
}
</style> 