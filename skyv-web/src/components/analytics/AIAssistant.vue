<template>
  <el-card class="mb-4">
    <template #header>
      <div class="d-flex justify-between items-center">
        <div class="d-flex items-center">
          <el-icon class="text-primary mr-2"><cpu /></el-icon>
          <span class="font-bold">AI分析助手</span>
          <el-tag size="small" type="success" effect="plain" class="ml-2">大模型增强</el-tag>
        </div>
        <div>
          <el-button type="primary" plain size="small" @click="handleNewAnalysis" class="mr-2">
            <el-icon class="mr-1"><plus /></el-icon>新建分析
          </el-button>
          <el-button type="danger" plain size="small" @click="clearConversation" v-if="aiStore.aiAssistant.conversations.length > 0">
            <el-icon class="mr-1"><delete /></el-icon>清空对话
          </el-button>
        </div>
      </div>
    </template>
    
    <div class="mb-3">
      <el-row>
        <el-col :span="24">
          <el-input
            v-model="query"
            placeholder="请输入您想了解的数据分析问题..."
            :disabled="aiStore.aiAssistant.loading"
            @keyup.enter="handleSubmitQuery"
            clearable
          >
            <template #append>
              <el-button @click="handleSubmitQuery" type="primary" :loading="aiStore.aiAssistant.loading">
                <el-icon><position /></el-icon>
              </el-button>
            </template>
          </el-input>
        </el-col>
      </el-row>
      <div class="form-text text-gray">例如：分析最近一周的异常事件趋势、预测下周人流量、比较各区域报警频率</div>
    </div>
    
    <div class="ai-conversation p-3 bg-light rounded mb-3" style="max-height: 300px; overflow-y: auto;" ref="conversationContainer">
      <template v-if="aiStore.aiAssistant.conversations.length > 0">
        <div v-for="message in aiStore.aiAssistant.conversations" :key="message.id" class="mb-3">
          <!-- AI消息 -->
          <div v-if="message.role === 'ai'" class="ai-message">
            <div class="d-flex">
              <div class="mr-2">
                <el-avatar :size="32" :src="aiAvatar" class="ai-avatar">AI</el-avatar>
              </div>
              <div class="message-content ai-message-content">
                <template v-if="typeof message.content === 'string'">
                  {{ message.content }}
                </template>
                <template v-else>
                  <p class="mb-1"><strong>{{ message.content.text }}</strong></p>
                  <ol class="mb-2 pl-4">
                    <li v-for="(point, index) in message.content.points" :key="index" class="mb-1">
                      {{ point }}
                    </li>
                  </ol>
                  <div v-if="message.content.chart" class="mini-chart mb-2" ref="miniChart"></div>
                  <div v-if="message.content.actions && message.content.actions.length" class="action-buttons">
                    <el-button 
                      v-for="(action, index) in message.content.actions" 
                      :key="index"
                      size="small" 
                      type="primary" 
                      plain
                      class="mr-1 mb-1"
                      @click="handleActionClick(action)"
                    >
                      {{ action }}
                    </el-button>
                  </div>
                </template>
              </div>
            </div>
          </div>
          
          <!-- 用户消息 -->
          <div v-else class="user-message">
            <div class="d-flex justify-end">
              <div class="message-content user-message-content text-right">
                {{ message.content }}
              </div>
              <div class="ml-2">
                <el-avatar :size="32" :src="userAvatar" class="user-avatar">您</el-avatar>
              </div>
            </div>
          </div>
        </div>
        
        <!-- 输入指示器 -->
        <div v-if="aiStore.aiAssistant.loading" class="typing-indicator">
          <span></span>
          <span></span>
          <span></span>
        </div>
      </template>
      <div v-else class="text-center text-gray empty-conversation">
        <img src="@/assets/images/ai-assistant.svg" alt="AI助手" class="ai-assistant-image mb-3" />
        <h4>AI智能分析助手</h4>
        <p>输入您的问题，AI助手将帮您分析数据、发现洞察并提供决策支持</p>
        <p class="text-sm">支持自然语言交互，可以询问复杂的分析问题</p>
      </div>
    </div>
    
    <!-- 快捷问题建议 -->
    <div v-if="aiStore.aiAssistant.suggestions.length && !aiStore.aiAssistant.conversations.length" class="quick-suggestions">
      <div class="suggestion-title mb-2">推荐问题：</div>
      <el-tag
        v-for="(suggestion, index) in aiStore.aiAssistant.suggestions"
        :key="index"
        class="mr-2 mb-2 cursor-pointer suggestion-tag"
        effect="plain"
        @click="handleSuggestionClick(suggestion)"
      >
        <el-icon class="mr-1"><chat-dot-round /></el-icon>
        {{ suggestion }}
      </el-tag>
    </div>
    
    <!-- 筛选条件 -->
    <el-collapse v-model="activeCollapse" class="mt-3">
      <el-collapse-item title="高级筛选" name="filters">
        <el-row :gutter="20">
          <el-col :md="8" :sm="24" class="mb-3 mb-md-0">
            <el-form-item label="时间范围" class="mb-0">
              <el-select v-model="filters.timeRange" size="default" class="w-full">
                <el-option label="今天" value="today" />
                <el-option label="昨天" value="yesterday" />
                <el-option label="最近7天" value="last7days" />
                <el-option label="最近30天" value="last30days" />
                <el-option label="本月" value="thisMonth" />
                <el-option label="上月" value="lastMonth" />
                <el-option label="自定义..." value="custom" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8" :sm="24" class="mb-3 mb-md-0">
            <el-form-item label="设备组" class="mb-0">
              <el-select v-model="filters.deviceGroup" size="default" class="w-full">
                <el-option label="所有设备" value="all" />
                <el-option label="入口监控组" value="entrance" />
                <el-option label="办公区监控组" value="office" />
                <el-option label="仓库监控组" value="warehouse" />
                <el-option label="停车场监控组" value="parking" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :md="8" :sm="24">
            <el-form-item label="分析类型" class="mb-0">
              <el-select v-model="filters.analysisType" size="default" class="w-full">
                <el-option label="全部" value="all" />
                <el-option label="人员检测" value="person" />
                <el-option label="行为分析" value="behavior" />
                <el-option label="事件统计" value="event" />
                <el-option label="报警分析" value="alert" />
                <el-option label="预测分析" value="prediction" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-collapse-item>
    </el-collapse>
    
    <!-- 语音输入对话框 -->
    <el-dialog
      v-model="voiceInputVisible"
      title="语音输入"
      width="400px"
      center
    >
      <div class="text-center">
        <div class="voice-animation" :class="{ 'is-recording': isRecording }">
          <el-icon :size="60" class="mb-3"><microphone /></el-icon>
        </div>
        <p>{{ isRecording ? '正在聆听...' : '准备开始' }}</p>
        <p class="text-sm text-muted" v-if="isRecording">请对着麦克风说话</p>
        <div class="mt-3">
          <el-button type="primary" @click="toggleRecording">
            {{ isRecording ? '停止' : '开始录音' }}
          </el-button>
        </div>
        <div class="mt-2" v-if="recognizedText">
          <p class="text-left"><strong>识别结果:</strong></p>
          <p class="text-left">{{ recognizedText }}</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="voiceInputVisible = false">取消</el-button>
          <el-button type="primary" @click="useRecognizedText" :disabled="!recognizedText">
            使用此文本
          </el-button>
        </span>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, watch, nextTick } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import { ElMessage } from 'element-plus'
import { 
  Plus, 
  Position, 
  ChatDotRound, 
  Cpu, 
  Delete, 
  Microphone 
} from '@element-plus/icons-vue'

// 导入图片资源
import aiAvatarImg from '@/assets/images/ai-avatar.png'
import userAvatarImg from '@/assets/images/user-avatar.png'
import miniChartImg from '@/assets/images/mini-chart.png'

const aiStore = useAnalyticsStore()
const query = ref('')
const conversationContainer = ref(null)
const activeCollapse = ref([])
const voiceInputVisible = ref(false)
const isRecording = ref(false)
const recognizedText = ref('')
const userAvatar = ref(userAvatarImg)
const aiAvatar = ref(aiAvatarImg)

// 筛选条件
const filters = ref({
  timeRange: 'last7days',
  deviceGroup: 'all',
  analysisType: 'all'
})

// 监听筛选条件变化
watch(filters, (newFilters) => {
  aiStore.updateFilters(newFilters)
}, { deep: true })

// 监听对话变化，自动滚动到底部
watch(() => aiStore.aiAssistant.conversations.length, () => {
  nextTick(() => {
    if (conversationContainer.value) {
      conversationContainer.value.scrollTop = conversationContainer.value.scrollHeight
    }
  })
})

// 提交问题
const handleSubmitQuery = () => {
  if (!query.value.trim()) {
    ElMessage.warning('请输入您的问题')
    return
  }
  
  aiStore.submitAIQuery(query.value).then(() => {
    query.value = ''
  })
}

// 点击建议问题
const handleSuggestionClick = (suggestion) => {
  query.value = suggestion
  handleSubmitQuery()
}

// 点击AI回复中的操作按钮
const handleActionClick = (action) => {
  if (action === '查看详细分析') {
    ElMessage.info('正在生成详细分析报告...')
    // 这里可以跳转到详细分析页面或打开详细分析对话框
    setTimeout(() => {
      ElMessage.success('详细分析已生成')
      // 模拟打开详细分析
      window.open('#/analytics/detail', '_blank')
    }, 1500)
  } else if (action === '导出报告') {
    ElMessage.info('正在导出报告...')
    // 这里可以调用导出报告的API
    setTimeout(() => {
      ElMessage.success('报告已导出到您的下载文件夹')
    }, 1500)
  } else if (action === '设置预警') {
    ElMessage.info('正在打开预警设置...')
    // 这里可以打开预警设置对话框
    setTimeout(() => {
      // 模拟打开预警设置对话框
      ElMessage.success('预警已设置')
    }, 1000)
  }
}

// 新建分析
const handleNewAnalysis = () => {
  ElMessage.info('正在打开新建分析对话框...')
}

// 清空对话
const clearConversation = () => {
  ElMessage.info('对话已清空')
  aiStore.clearAIConversation()
}

// 语音输入相关
const startVoiceInput = () => {
  voiceInputVisible.value = true
  recognizedText.value = ''
}

// 切换录音状态
const toggleRecording = () => {
  isRecording.value = !isRecording.value
  
  if (isRecording.value) {
    // 模拟语音识别
    setTimeout(() => {
      recognizedText.value = '分析最近一周的异常事件趋势和可能的原因'
      isRecording.value = false
    }, 3000)
  }
}

// 使用识别的文本
const useRecognizedText = () => {
  if (recognizedText.value) {
    query.value = recognizedText.value
    voiceInputVisible.value = false
    // 可以选择自动提交
    // handleSubmitQuery()
  }
}
</script>

<style scoped>
.mb-4 {
  margin-bottom: 1.5rem;
}

.mb-3 {
  margin-bottom: 1rem;
}

.mb-2 {
  margin-bottom: 0.5rem;
}

.mb-1 {
  margin-bottom: 0.25rem;
}

.mb-0 {
  margin-bottom: 0;
}

.mr-2 {
  margin-right: 0.5rem;
}

.mr-1 {
  margin-right: 0.25rem;
}

.ml-2 {
  margin-left: 0.5rem;
}

.mt-3 {
  margin-top: 1rem;
}

.mt-2 {
  margin-top: 0.5rem;
}

.pl-4 {
  padding-left: 1rem;
}

.p-3 {
  padding: 0.75rem;
}

.w-full {
  width: 100%;
}

.d-flex {
  display: flex;
}

.justify-between {
  justify-content: space-between;
}

.justify-end {
  justify-content: flex-end;
}

.items-center {
  align-items: center;
}

.text-right {
  text-align: right;
}

.text-center {
  text-align: center;
}

.text-left {
  text-align: left;
}

.text-gray {
  color: #909399;
}

.text-primary {
  color: var(--el-color-primary);
}

.text-sm {
  font-size: 0.875rem;
}

.text-muted {
  color: #909399;
}

.font-bold {
  font-weight: bold;
}

.cursor-pointer {
  cursor: pointer;
}

/* 输入框样式 */
.el-input {
  width: 100%;
}

.form-text {
  margin-top: 5px;
  font-size: 0.875rem;
}

/* 背景样式 */
.bg-light {
  background-color: #f8f9fa;
}

.rounded {
  border-radius: 4px;
}

.ai-conversation {
  border: 1px solid #ebeef5;
  border-radius: 4px;
}

.empty-conversation {
  padding: 2rem 0;
}

.ai-assistant-image {
  width: 80px;
  height: 80px;
}

.message-content {
  padding: 0.75rem;
  border-radius: 8px;
  max-width: 80%;
}

.ai-message-content {
  background-color: #f0f9ff;
  border: 1px solid #e1f3ff;
}

.user-message-content {
  background-color: #f2f6fc;
  border: 1px solid #ebeef5;
}

.ai-avatar {
  background-color: #409eff;
  color: white;
}

.user-avatar {
  background-color: #67c23a;
  color: white;
}

.suggestion-tag {
  cursor: pointer;
  transition: all 0.3s;
}

.suggestion-tag:hover {
  transform: translateY(-2px);
}

.suggestion-title {
  color: #606266;
  font-size: 0.875rem;
}

.typing-indicator {
  display: flex;
  padding: 8px;
}

.typing-indicator span {
  height: 8px;
  width: 8px;
  margin: 0 2px;
  background-color: #409eff;
  border-radius: 50%;
  display: inline-block;
  animation: typing 1.5s infinite ease-in-out;
}

.typing-indicator span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-indicator span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0% { transform: translateY(0px); }
  28% { transform: translateY(-5px); }
  44% { transform: translateY(0px); }
}

.voice-animation {
  display: inline-block;
  padding: 20px;
  border-radius: 50%;
  background-color: #f0f9ff;
  transition: all 0.3s;
}

.voice-animation.is-recording {
  animation: pulse 1.5s infinite;
  background-color: #f56c6c20;
  color: #f56c6c;
}

@keyframes pulse {
  0% { transform: scale(1); box-shadow: 0 0 0 0 rgba(245, 108, 108, 0.4); }
  70% { transform: scale(1.1); box-shadow: 0 0 0 10px rgba(245, 108, 108, 0); }
  100% { transform: scale(1); box-shadow: 0 0 0 0 rgba(245, 108, 108, 0); }
}
</style> 