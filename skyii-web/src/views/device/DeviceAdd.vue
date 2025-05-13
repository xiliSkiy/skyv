<template>
  <div class="device-add-container">
    <el-card shadow="hover">
      <template #header>
        <div class="card-header">
          <div class="header-title">{{ isEdit ? '编辑设备' : '添加设备' }}</div>
          <div class="header-actions">
            <el-button @click="$router.push('/device')">返回设备列表</el-button>
          </div>
        </div>
      </template>

      <!-- 步骤条 -->
      <el-steps :active="activeStep" finish-status="success" simple style="margin-bottom: 30px;">
        <el-step title="基本信息" />
        <el-step title="网络设置" />
        <el-step title="高级配置" />
        <el-step title="确认信息" />
      </el-steps>

      <!-- 表单内容 -->
      <div v-loading="loading">
        <!-- 步骤1：基本信息 -->
        <div v-show="activeStep === 0">
          <el-form ref="basicFormRef" :model="deviceForm" :rules="basicRules" label-width="100px">
            <el-form-item label="设备名称" prop="name">
              <el-input v-model="deviceForm.name" placeholder="请输入设备名称" />
            </el-form-item>
            <el-form-item label="设备编码" prop="code">
              <el-input v-model="deviceForm.code" placeholder="请输入设备编码" />
            </el-form-item>
            <el-form-item label="设备类型" prop="type">
              <el-select v-model="deviceForm.type" placeholder="请选择设备类型" style="width: 100%;">
                <el-option label="摄像头" value="摄像头" />
                <el-option label="传感器" value="传感器" />
                <el-option label="门禁" value="门禁" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
            <el-form-item label="设备位置" prop="location">
              <el-input v-model="deviceForm.location" placeholder="请输入设备位置" />
            </el-form-item>
            <el-form-item label="设备描述" prop="description">
              <el-input v-model="deviceForm.description" type="textarea" rows="3" placeholder="请输入设备描述" />
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤2：网络设置 -->
        <div v-show="activeStep === 1">
          <el-form ref="networkFormRef" :model="deviceForm" :rules="networkRules" label-width="100px">
            <el-form-item label="IP地址" prop="ipAddress">
              <el-input v-model="deviceForm.ipAddress" placeholder="请输入IP地址" />
            </el-form-item>
            <el-form-item label="端口" prop="port">
              <el-input-number v-model="deviceForm.port" :min="1" :max="65535" placeholder="请输入端口" style="width: 100%;" />
            </el-form-item>
            <el-form-item label="用户名" prop="username">
              <el-input v-model="deviceForm.username" placeholder="请输入用户名" />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="deviceForm.password" type="password" placeholder="请输入密码" show-password />
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤3：高级配置 -->
        <div v-show="activeStep === 2">
          <el-form ref="advancedFormRef" :model="deviceForm" label-width="100px">
            <el-form-item label="设备分组" prop="groupId">
              <el-select v-model="deviceForm.groupId" placeholder="请选择设备分组" style="width: 100%;">
                <el-option label="默认分组" :value="1" />
                <el-option label="重要设备" :value="2" />
                <el-option label="办公区域" :value="3" />
              </el-select>
            </el-form-item>
            <el-form-item label="设备状态" prop="status">
              <el-radio-group v-model="deviceForm.status">
                <el-radio :label="0">离线</el-radio>
                <el-radio :label="1">在线</el-radio>
                <el-radio :label="2">故障</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-form>
        </div>

        <!-- 步骤4：确认信息 -->
        <div v-show="activeStep === 3">
          <el-descriptions title="设备信息确认" :column="2" border>
            <el-descriptions-item label="设备名称">{{ deviceForm.name }}</el-descriptions-item>
            <el-descriptions-item label="设备编码">{{ deviceForm.code }}</el-descriptions-item>
            <el-descriptions-item label="设备类型">{{ deviceForm.type }}</el-descriptions-item>
            <el-descriptions-item label="设备位置">{{ deviceForm.location || '-' }}</el-descriptions-item>
            <el-descriptions-item label="IP地址">{{ deviceForm.ipAddress }}</el-descriptions-item>
            <el-descriptions-item label="端口">{{ deviceForm.port }}</el-descriptions-item>
            <el-descriptions-item label="用户名">{{ deviceForm.username || '-' }}</el-descriptions-item>
            <el-descriptions-item label="设备状态">
              <el-tag :type="getDeviceStatusType(deviceForm.status)">
                {{ getDeviceStatusText(deviceForm.status) }}
              </el-tag>
            </el-descriptions-item>
            <el-descriptions-item label="设备描述" :span="2">{{ deviceForm.description || '-' }}</el-descriptions-item>
          </el-descriptions>
        </div>

        <!-- 步骤按钮 -->
        <div class="step-actions">
          <el-button @click="prevStep" v-if="activeStep > 0">上一步</el-button>
          <el-button type="primary" @click="nextStep" v-if="activeStep < 3">下一步</el-button>
          <el-button type="success" @click="submitForm" v-if="activeStep === 3">{{ isEdit ? '保存修改' : '提交' }}</el-button>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createDevice, getDeviceDetail, updateDevice } from '@/api/device'
import { formatDateTime } from '@/utils/date'

const route = useRoute()
const router = useRouter()

// 是否为编辑模式
const isEdit = computed(() => route.meta.isEdit || false)

// 当前步骤
const activeStep = ref(0)

// 加载状态
const loading = ref(false)

// 表单引用
const basicFormRef = ref(null)
const networkFormRef = ref(null)
const advancedFormRef = ref(null)

// 设备表单数据
const deviceForm = reactive({
  name: '',
  code: '',
  type: '',
  location: '',
  description: '',
  ipAddress: '',
  port: 554,
  username: '',
  password: '',
  groupId: 1,
  status: 0
})

// 基本信息验证规则
const basicRules = {
  name: [
    { required: true, message: '请输入设备名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  code: [
    { required: true, message: '请输入设备编码', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择设备类型', trigger: 'change' }
  ]
}

// 网络设置验证规则
const networkRules = {
  ipAddress: [
    { required: true, message: '请输入IP地址', trigger: 'blur' },
    { pattern: /^(\d{1,3}\.){3}\d{1,3}$/, message: 'IP地址格式不正确', trigger: 'blur' }
  ],
  port: [
    { required: true, message: '请输入端口号', trigger: 'blur' },
    { type: 'number', min: 1, max: 65535, message: '端口范围为1-65535', trigger: 'blur' }
  ]
}

// 获取设备状态类型
const getDeviceStatusType = (status) => {
  const typeMap = {
    0: 'danger',
    1: 'success',
    2: 'warning'
  }
  return typeMap[status] || 'info'
}

// 获取设备状态文本
const getDeviceStatusText = (status) => {
  const textMap = {
    0: '离线',
    1: '在线',
    2: '故障'
  }
  return textMap[status] || '未知'
}

// 上一步
const prevStep = () => {
  if (activeStep.value > 0) {
    activeStep.value--
  }
}

// 下一步
const nextStep = async () => {
  if (activeStep.value === 0) {
    // 验证基本信息
    await basicFormRef.value.validate((valid) => {
      if (valid) {
        activeStep.value++
      }
    })
  } else if (activeStep.value === 1) {
    // 验证网络设置
    await networkFormRef.value.validate((valid) => {
      if (valid) {
        activeStep.value++
      }
    })
  } else if (activeStep.value < 3) {
    activeStep.value++
  }
}

// 提交表单
const submitForm = async () => {
  loading.value = true
  try {
    if (isEdit.value) {
      const deviceId = route.params.id
      await updateDevice(deviceId, deviceForm)
      ElMessage.success('设备更新成功')
    } else {
      await createDevice(deviceForm)
      ElMessage.success('设备添加成功')
    }
    router.push('/device')
  } catch (error) {
    console.error('提交设备数据失败', error)
    ElMessage.error(isEdit.value ? '设备更新失败' : '设备添加失败')
  } finally {
    loading.value = false
  }
}

// 重置表单数据
const resetForm = () => {
  deviceForm.name = ''
  deviceForm.code = ''
  deviceForm.type = ''
  deviceForm.location = ''
  deviceForm.description = ''
  deviceForm.ipAddress = ''
  deviceForm.port = 554
  deviceForm.username = ''
  deviceForm.password = ''
  deviceForm.groupId = 1
  deviceForm.status = 0
  
  // 重置步骤
  activeStep.value = 0
  
  // 重置表单校验
  if (basicFormRef.value) basicFormRef.value.resetFields()
  if (networkFormRef.value) networkFormRef.value.resetFields()
  if (advancedFormRef.value) advancedFormRef.value.resetFields()
}

// 获取设备详情
const getDevice = async (id) => {
  loading.value = true
  try {
    const res = await getDeviceDetail(id)
    if (res.data) {
      Object.assign(deviceForm, res.data)
    }
  } catch (error) {
    console.error('获取设备详情失败', error)
    ElMessage.error('获取设备详情失败')
  } finally {
    loading.value = false
  }
}

// 初始化
const initForm = () => {
  if (isEdit.value && route.params.id) {
    getDevice(route.params.id)
  } else {
    resetForm()
  }
}

// 监听路由变化
watch(
  () => route.path,
  () => {
    initForm()
  }
)

// 组件挂载时初始化
onMounted(() => {
  initForm()
})
</script>

<style lang="scss" scoped>
.device-add-container {
  padding: 6px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  
  .header-title {
    font-size: 16px;
    font-weight: 500;
  }
}

.step-actions {
  margin-top: 30px;
  display: flex;
  justify-content: center;
  gap: 10px;
}
</style> 