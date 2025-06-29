<template>
  <div class="analytics-test-container">
    <h2>AI分析助手测试页面</h2>
    <div class="test-description">
      <p>这是一个测试页面，用于验证AI分析助手组件的输入框是否正常显示。</p>
    </div>
    
    <!-- AI分析助手 -->
    <ai-assistant />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'
import { useAnalyticsStore } from '@/store/modules/analytics'
import AIAssistant from '@/components/analytics/AIAssistant.vue'

const analyticsStore = useAnalyticsStore()

// 组件挂载时初始化数据
onMounted(() => {
  analyticsStore.loadAIAssistant()
})
</script>

<style scoped>
.analytics-test-container {
  padding: 20px;
  max-width: 1000px;
  margin: 0 auto;
}

.test-description {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border-left: 4px solid #409eff;
}
</style> 