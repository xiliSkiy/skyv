<template>
  <div class="metrics-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span class="header-title">指标配置</span>
          <el-dropdown @command="handleCommand" trigger="click" style="margin-left: 20px;">
            <el-button type="primary" size="small">
              操作<i class="el-icon-arrow-down el-icon--right"></i>
            </el-button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="refresh">刷新</el-dropdown-item>
                <el-dropdown-item command="export" divided>导出配置</el-dropdown-item>
                <el-dropdown-item command="import">导入配置</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </template>
      
      <el-tabs v-model="activeTab" @tab-click="handleTabClick">
        <el-tab-pane label="指标管理" name="metrics">
          <metrics-management ref="metricsManagement" v-if="activeTab === 'metrics'" />
        </el-tab-pane>
        <el-tab-pane label="指标模板" name="templates">
          <metric-templates ref="metricTemplates" v-if="activeTab === 'templates'" />
        </el-tab-pane>
             </el-tabs>
    </el-card>
    
    <!-- 导入对话框 -->
    <el-dialog title="导入指标配置" v-model="importDialogVisible" width="500px">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :auto-upload="false"
        :on-change="handleFileChange"
        :limit="1"
        accept=".json,.csv">
        <i class="el-icon-upload"></i>
        <div class="el-upload__text">将文件拖到此处，或<em>点击上传</em></div>
        <div class="el-upload__tip">只能上传json或csv文件，且不超过500kb</div>
      </el-upload>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="importDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleImport" :loading="importing">导入</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, onMounted, nextTick, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { importMetrics } from '@/api/metrics'
import MetricsManagement from './MetricsManagement.vue'
import MetricTemplates from './MetricTemplates.vue'
import bus from '@/utils/eventBus'

export default {
  name: 'MetricsView',
  components: {
    MetricsManagement,
    MetricTemplates
  },
  setup() {
    const activeTab = ref('metrics')
    const importDialogVisible = ref(false)
    const importing = ref(false)
    const importFile = ref(null)
    const metricIdForHistory = ref(null)
    
    const metricsManagement = ref(null)
    const metricTemplates = ref(null)
    
    // 处理标签页切换
    const handleTabClick = () => {
      nextTick(() => {
        // 切换标签页时刷新数据
        switch (activeTab.value) {
          case 'metrics':
            metricsManagement.value?.fetchData()
            break
          case 'templates':
            metricTemplates.value?.fetchData()
            break
        }
      })
    }
    
    // 处理下拉菜单命令
    const handleCommand = (command) => {
      switch (command) {
        case 'refresh':
          refreshCurrentTab()
          break
        case 'export':
          handleExport()
          break
        case 'import':
          importDialogVisible.value = true
          break
      }
    }
    
    // 刷新当前标签页
    const refreshCurrentTab = () => {
      switch (activeTab.value) {
        case 'metrics':
          metricsManagement.value?.fetchData()
          break
        case 'templates':
          metricTemplates.value?.fetchData()
          break
      }
      ElMessage.success('刷新成功')
    }
    
    // 导出配置
    const handleExport = () => {
      ElMessageBox.confirm('确定要导出当前配置吗?', '导出确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        switch (activeTab.value) {
          case 'metrics':
            metricsManagement.value?.handleExport()
            break
          case 'templates':
            metricTemplates.value?.handleExport()
            break
          default:
            ElMessage.warning('当前页面不支持导出')
        }
      }).catch(() => {})
    }
    
    // 处理文件选择
    const handleFileChange = (file) => {
      importFile.value = file.raw
    }
    
    // 处理导入
    const handleImport = () => {
      if (!importFile.value) {
        ElMessage.warning('请先选择文件')
        return
      }
      
      const reader = new FileReader()
      reader.readAsText(importFile.value, 'UTF-8')
      reader.onload = (e) => {
        importing.value = true
        try {
          const fileContent = e.target.result
          let data
          
          if (importFile.value.name.endsWith('.json')) {
            data = JSON.parse(fileContent)
          } else if (importFile.value.name.endsWith('.csv')) {
            // 处理CSV数据 (简化示例，实际实现应更复杂)
            data = fileContent.split('\n').map(line => {
              const values = line.split(',')
              return {
                metricName: values[0],
                metricKey: values[1],
                metricType: values[2]
                // 其他字段...
              }
            })
          } else {
            throw new Error('不支持的文件格式')
          }
          
          // 调用导入API
          importMetrics(data).then(() => {
            ElMessage.success('导入成功')
            importDialogVisible.value = false
            refreshCurrentTab()
          }).catch(error => {
            ElMessage.error(`导入失败: ${error.message}`)
          }).finally(() => {
            importing.value = false
          })
        } catch (error) {
          ElMessage.error(`解析文件失败: ${error.message}`)
          importing.value = false
        }
      }
    }
    
    // 监听组件间通信事件
    onMounted(() => {
      // 初始化加载第一个标签页数据
      nextTick(() => {
        metricsManagement.value?.fetchData()
      })
      
      // 监听切换到历史记录标签页的事件
      bus.on('switchToHistory', (params) => {
        activeTab.value = 'history'
        if (params.metricId) {
          metricIdForHistory.value = params.metricId
        }
        nextTick(() => {
          metricHistory.value?.fetchData()
        })
      })
    })
    
    return {
      activeTab,
      importDialogVisible,
      importing,
      metricIdForHistory,
      handleTabClick,
      handleCommand,
      handleFileChange,
      handleImport,
      metricsManagement,
      metricTemplates
    }
  }
}
</script>

<style scoped>
.metrics-container {
  padding: 20px;
}

.box-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  align-items: center;
}

.header-title {
  font-size: 16px;
  font-weight: bold;
}

.el-tabs {
  margin-top: 10px;
}

.el-tab-pane {
  padding: 10px 0;
}

.upload-demo {
  width: 100%;
  margin: 0 auto;
}
</style> 