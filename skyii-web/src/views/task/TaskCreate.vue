<template>
  <div class="task-create-container">
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

    <!-- 表单内容 -->
    <el-card>
      <template #header>
        <div class="card-header">
          <el-icon><InfoFilled /></el-icon> 基本信息
        </div>
      </template>

      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="rules"
        label-width="120px"
        label-position="left"
        @submit.prevent
      >
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务名称" prop="taskName">
              <el-input v-model="taskForm.taskName" placeholder="输入任务名称" />
              <div class="form-text">名称应简洁明了，表明任务用途</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务类型" prop="taskType">
              <el-select v-model="taskForm.taskType" placeholder="选择任务类型" style="width: 100%">
                <el-option label="实时采集" value="realtime" />
                <el-option label="定时采集" value="scheduled" />
                <el-option label="触发式采集" value="triggered" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="3"
            placeholder="输入任务描述"
          />
        </el-form-item>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="任务优先级" prop="priority">
              <el-select v-model="taskForm.priority" placeholder="选择优先级" style="width: 100%">
                <el-option label="高" value="high" />
                <el-option label="中" value="medium" />
                <el-option label="低" value="low" />
              </el-select>
              <div class="form-text">高优先级任务会优先分配资源</div>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="任务标签">
              <div class="tag-input-container">
                <el-tag
                  v-for="tag in taskForm.tags"
                  :key="tag"
                  closable
                  :disable-transitions="false"
                  @close="handleTagClose(tag)"
                  class="tag-item"
                >
                  {{ tag }}
                </el-tag>
                <el-input
                  v-if="inputTagVisible"
                  ref="tagInputRef"
                  v-model="inputTagValue"
                  class="tag-input"
                  size="small"
                  @keyup.enter="handleTagConfirm"
                  @blur="handleTagConfirm"
                  placeholder="添加标签..."
                />
                <el-button v-else class="tag-button" plain size="small" @click="showTagInput">
                  + 添加标签
                </el-button>
              </div>
              <div class="form-text">标签用于分类和快速筛选</div>
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="负责人" prop="owner">
              <el-select v-model="taskForm.owner" placeholder="选择负责人" style="width: 100%">
                <el-option label="当前用户" value="current" />
                <el-option label="安全主管" value="security" />
                <el-option label="系统管理员" value="admin" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="department">
              <el-select v-model="taskForm.department" placeholder="选择部门" style="width: 100%">
                <el-option label="安防部" value="security" />
                <el-option label="信息技术部" value="it" />
                <el-option label="运维部" value="ops" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item>
          <el-switch
            v-model="taskForm.enableNotification"
            active-text="启用任务状态通知"
          />
          <div class="form-text">任务状态变更时发送通知</div>
        </el-form-item>

        <el-alert
          type="info"
          show-icon
          :closable="false"
        >
          填写完成后点击"下一步"进入设备选择页面
        </el-alert>
      </el-form>
      <div class="action-footer d-flex justify-content-end mt-4">
        <el-button type="primary" @click="nextStep" size="large">
          下一步 <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, nextTick, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Document, Close, InfoFilled, ArrowRight } from '@element-plus/icons-vue'
import { createTask, saveTaskDraft, getTaskDraft, getTaskDetail, updateTask } from '@/api/task'

const router = useRouter()
const route = useRoute()

// 判断是否为编辑模式
const isEdit = ref(route.meta.isEdit || false)
const taskId = ref(isEdit.value ? route.params.id : null)

// 表单引用
const taskFormRef = ref(null)

// 当前步骤
const currentStep = ref(0)

// 任务表单数据
const taskForm = reactive({
  taskName: '',
  taskType: '',
  description: '',
  priority: 'medium',
  tags: [],
  owner: 'current',
  department: '',
  enableNotification: true
})

// 表单验证规则
const rules = {
  taskName: [
    { required: true, message: '请输入任务名称', trigger: 'blur' },
    { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
  ],
  taskType: [
    { required: true, message: '请选择任务类型', trigger: 'change' }
  ],
  priority: [
    { required: true, message: '请选择任务优先级', trigger: 'change' }
  ],
  owner: [
    { required: true, message: '请选择负责人', trigger: 'change' }
  ]
}

// 标签输入相关
const inputTagVisible = ref(false)
const inputTagValue = ref('')
const tagInputRef = ref(null)

// 显示标签输入框
const showTagInput = () => {
  inputTagVisible.value = true
  nextTick(() => {
    tagInputRef.value.focus()
  })
}

// 处理标签确认
const handleTagConfirm = () => {
  const value = inputTagValue.value.trim()
  if (value && !taskForm.tags.includes(value)) {
    taskForm.tags.push(value)
  }
  inputTagVisible.value = false
  inputTagValue.value = ''
}

// 处理标签关闭
const handleTagClose = (tag) => {
  taskForm.tags = taskForm.tags.filter(t => t !== tag)
}

// 下一步
const nextStep = async () => {
  // 表单验证
  await taskFormRef.value.validate((valid) => {
    if (!valid) {
      ElMessage.warning('请完善表单信息')
      return
    }

    // 保存到本地存储，用于后续步骤
    localStorage.setItem('taskCreateData', JSON.stringify({
      ...taskForm,
      step: 1,
      isEdit: isEdit.value,
      taskId: taskId.value
    }))

    // 跳转到下一步
    router.push('/task/create/device')
  })
}

// 保存草稿
const saveDraft = async () => {
  try {
    const draftData = {
      ...taskForm,
      step: 0,
      isEdit: isEdit.value,
      taskId: taskId.value
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
  ElMessage.info(isEdit.value ? '已取消编辑任务' : '已取消创建任务')
  router.push('/task')
}

// 获取任务详情数据（编辑模式）
const fetchTaskDetail = async () => {
  if (!isEdit.value || !taskId.value) return
  
  try {
    const res = await getTaskDetail(taskId.value)
    if (!res.data) {
      ElMessage.error('获取任务详情失败')
      return
    }
    
    const taskData = res.data
    console.log('获取到的任务详情:', taskData)
    
    // 填充表单数据
    taskForm.taskName = taskData.taskName || taskData.name || ''
    taskForm.taskType = taskData.taskType || taskData.type || ''
    taskForm.description = taskData.description || ''
    taskForm.priority = taskData.priority || 'medium'
    taskForm.tags = Array.isArray(taskData.tags) ? [...taskData.tags] : []
    taskForm.owner = taskData.owner || 'current'
    taskForm.department = taskData.department || ''
    taskForm.enableNotification = taskData.enableNotification !== false
    
    // 保存完整的任务数据到本地存储，供后续步骤使用
    localStorage.setItem('taskEditData', JSON.stringify(taskData))
    
    ElMessage.success('任务数据加载成功')
  } catch (error) {
    console.error('获取任务详情失败', error)
    ElMessage.error('获取任务详情失败，请刷新重试')
  }
}

// 恢复草稿数据
const restoreDraftData = async () => {
  // 如果是编辑模式，优先获取任务详情
  if (isEdit.value && taskId.value) {
    await fetchTaskDetail()
    return
  }
  
  // 检查本地存储中是否有数据
  const storedData = localStorage.getItem('taskCreateData')
  if (storedData) {
    try {
      const data = JSON.parse(storedData)
      Object.assign(taskForm, data)
      return
    } catch (error) {
      console.error('解析本地存储数据失败', error)
    }
  }

  // 检查是否有草稿ID
  const draftId = localStorage.getItem('taskDraftId')
  if (draftId) {
    try {
      const res = await getTaskDraft(draftId)
      if (res.data) {
        Object.assign(taskForm, res.data)
      }
    } catch (error) {
      console.error('获取草稿数据失败', error)
    }
  }
}

// 页面初始化
onMounted(() => {
  console.log('TaskCreate组件挂载，编辑模式:', isEdit.value, '任务ID:', taskId.value)
  
  // 如果不是编辑模式，则清理本地存储中的任务数据
  if (!isEdit.value) {
    // 清除可能存在的历史数据
    localStorage.removeItem('taskCreateData')
    localStorage.removeItem('taskEditData')
    localStorage.removeItem('taskDraftId')
    console.log('已清理本地存储中的历史任务数据')
  } else {
    // 编辑模式：检查本地存储中是否已有任务数据
    const editData = localStorage.getItem('taskEditData')
    if (!editData) {
      // 如果没有，则从服务器获取任务详情
      console.log('本地存储中无任务数据，从服务器获取任务详情')
      fetchTaskDetail()
    } else {
      console.log('从本地存储中恢复任务编辑数据')
      // 确保taskCreateData中包含isEdit和taskId信息
      const taskCreateData = JSON.parse(localStorage.getItem('taskCreateData') || '{}')
      localStorage.setItem('taskCreateData', JSON.stringify({
        ...taskCreateData,
        isEdit: true,
        taskId: taskId.value
      }))
    }
  }
  
  // 然后再尝试恢复草稿数据
  restoreDraftData()
})
</script>

<style scoped>
.step-indicator {
  margin-bottom: 30px;
}

.form-text {
  font-size: 12px;
  color: #6c757d;
  margin-top: 5px;
}

.card-header {
  display: flex;
  align-items: center;
}

.card-header .el-icon {
  margin-right: 8px;
}

.tag-input-container {
  display: flex;
  flex-wrap: wrap;
  align-items: center;
  padding: 5px 10px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  min-height: 38px;
}

.tag-item {
  margin-right: 8px;
  margin-bottom: 8px;
}

.tag-input {
  width: 100px;
  margin-right: 8px;
  margin-bottom: 8px;
  vertical-align: bottom;
}

.tag-button {
  margin-bottom: 8px;
}

.action-buttons {
  display: flex;
  align-items: center;
}

.action-footer {
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
  margin-top: 30px;
}
</style> 