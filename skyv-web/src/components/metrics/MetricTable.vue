<template>
  <div class="metric-table">
    <!-- 工具栏 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="d-flex">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索指标名称或标识符"
          clearable
          @keyup.enter="handleQuery"
          @clear="handleQuery"
          class="me-3"
          style="width: 280px">
          <template #prefix>
            <i class="fas fa-search"></i>
          </template>
        </el-input>
        
        <el-select
          v-model="queryParams.metricType"
          placeholder="指标类型"
          clearable
          @change="handleQuery"
          style="width: 140px"
          class="me-3">
          <el-option label="服务器指标" value="server"></el-option>
          <el-option label="中间件指标" value="middleware"></el-option>
          <el-option label="数据库指标" value="database"></el-option>
          <el-option label="自定义指标" value="custom"></el-option>
        </el-select>
        
        <el-select
          v-model="queryParams.collectionMethod"
          placeholder="采集方式"
          clearable
          @change="handleQuery"
          style="width: 160px">
          <el-option label="SNMP" value="snmp"></el-option>
          <el-option label="JMX" value="jmx"></el-option>
          <el-option label="WMI" value="wmi"></el-option>
          <el-option label="HTTP API" value="http"></el-option>
          <el-option label="SSH命令" value="ssh"></el-option>
          <el-option label="Agent采集" value="agent"></el-option>
          <el-option label="自定义脚本" value="custom"></el-option>
        </el-select>
      </div>
      
      <div>
        <el-button type="primary" @click="handleAdd">
          <i class="fas fa-plus me-1"></i> 添加指标
        </el-button>
        
        <el-dropdown @command="handleBatchCommand" trigger="click" class="ms-2">
          <el-button type="primary" plain>
            批量操作 <i class="fas fa-chevron-down ms-1"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="enable" :disabled="!hasSelected">批量启用</el-dropdown-item>
              <el-dropdown-item command="disable" :disabled="!hasSelected">批量禁用</el-dropdown-item>
              <el-dropdown-item command="export" :disabled="!hasSelected">导出选中</el-dropdown-item>
              <el-dropdown-item command="delete" :disabled="!hasSelected">
                <span class="text-danger">批量删除</span>
              </el-dropdown-item>
              <el-dropdown-item divided command="import">导入指标</el-dropdown-item>
              <el-dropdown-item command="exportAll">导出全部</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="metricList"
      border
      @selection-change="handleSelectionChange"
      style="width: 100%">
      <el-table-column type="selection" width="55" align="center" />
      
      <el-table-column label="指标名称" prop="metricName" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="d-flex align-items-center">
            <el-tag 
              :type="getTypeTagType(row.metricType)" 
              effect="plain" 
              size="small" 
              class="me-2">
              {{ getMetricTypeLabel(row.metricType) }}
            </el-tag>
            <span class="fw-bold">{{ row.metricName }}</span>
          </div>
          <div class="text-muted small">{{ row.metricKey }}</div>
        </template>
      </el-table-column>
      
      <el-table-column label="采集设置" min-width="180">
        <template #default="{ row }">
          <div>
            <el-tag size="small" effect="plain">
              {{ getCollectionMethodLabel(row.collectionMethod) }}
            </el-tag>
          </div>
          <div class="text-muted small mt-1">
            {{ row.collectionInterval }}{{ row.collectionIntervalUnit }} 间隔
          </div>
        </template>
      </el-table-column>
      
      <el-table-column label="阈值设置" min-width="160">
        <template #default="{ row }">
          <template v-if="row.thresholdEnabled">
            <div>
              <el-tag type="warning" size="small" effect="plain" class="me-1">
                警告: {{ row.warningThreshold }}%
              </el-tag>
            </div>
            <div class="mt-1">
              <el-tag type="danger" size="small" effect="plain">
                严重: {{ row.criticalThreshold }}%
              </el-tag>
            </div>
          </template>
          <span v-else class="text-muted">未设置</span>
        </template>
      </el-table-column>
      
      <el-table-column label="状态" width="100" align="center">
        <template #default="{ row }">
          <el-switch
            v-model="row.status"
            :active-value="1"
            :inactive-value="0"
            @change="handleStatusChange(row)"
            :loading="row.statusLoading">
          </el-switch>
        </template>
      </el-table-column>
      
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            <i class="fas fa-edit"></i> 编辑
          </el-button>
          <el-button type="primary" link @click="handleClone(row)">
            <i class="fas fa-copy"></i> 克隆
          </el-button>
          <el-button type="danger" link @click="handleDelete(row)">
            <i class="fas fa-trash"></i> 删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>
    
    <!-- 分页 -->
    <div class="d-flex justify-content-end mt-3">
      <el-pagination
        v-model:current-page="queryParams.pageNum"
        v-model:page-size="queryParams.pageSize"
        :page-sizes="[10, 20, 50, 100]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange">
      </el-pagination>
    </div>
    
    <!-- 添加/编辑指标对话框 -->
    <metric-dialog
      v-model:visible="dialogVisible"
      :metric="currentMetric"
      :is-edit="isEdit"
      @success="handleDialogSuccess">
    </metric-dialog>
    
    <!-- 导入指标对话框 -->
    <el-dialog v-model="importDialogVisible" title="导入指标" width="500px">
      <el-upload
        class="upload-demo"
        drag
        action="#"
        :http-request="handleImportUpload"
        :show-file-list="false"
        accept=".json,.csv,.xlsx">
        <i class="fas fa-cloud-upload-alt fa-3x mb-2"></i>
        <div class="el-upload__text">
          拖拽文件到此处，或 <em>点击上传</em>
        </div>
        <template #tip>
          <div class="el-upload__tip text-muted">
            支持 .json, .csv, .xlsx 格式，文件大小不超过5MB
          </div>
        </template>
      </el-upload>
    </el-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import MetricDialog from './MetricDialog.vue'
import { 
  getMetricList,
  deleteMetric,
  updateMetricStatus,
  batchDeleteMetrics,
  batchUpdateMetricStatus,
  exportMetrics,
  importMetrics
} from '@/api/metrics'

export default defineComponent({
  name: 'MetricTable',
  components: {
    MetricDialog
  },
  setup() {
    // 查询参数
    const queryParams = reactive({
      pageNum: 1,
      pageSize: 10,
      keyword: '',
      metricType: '',
      collectionMethod: '',
      status: ''
    })
    
    // 数据列表
    const metricList = ref([])
    const total = ref(0)
    const loading = ref(false)
    
    // 选中数据
    const selectedIds = ref([])
    const hasSelected = computed(() => selectedIds.value.length > 0)
    
    // 弹窗控制
    const dialogVisible = ref(false)
    const importDialogVisible = ref(false)
    const isEdit = ref(false)
    const currentMetric = ref({})
    
    // 获取指标列表
    const getList = async () => {
      loading.value = true
      try {
        const response = await getMetricList(queryParams)
        if (response.code === 200) {
          metricList.value = response.data.content || []
          total.value = response.data.totalElements || 0
          
          // 添加状态加载标志
          metricList.value.forEach(item => {
            item.statusLoading = false
          })
        }
      } catch (error) {
        console.error('获取指标列表失败:', error)
        ElMessage.error('获取指标列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 指标类型标签类型映射
    const getTypeTagType = (type) => {
      const map = {
        'server': 'success',
        'middleware': 'warning',
        'database': 'info',
        'custom': 'danger'
      }
      return map[type] || 'info'
    }
    
    // 指标类型名称映射
    const getMetricTypeLabel = (type) => {
      const map = {
        'server': '服务器指标',
        'middleware': '中间件指标',
        'database': '数据库指标',
        'custom': '自定义指标'
      }
      return map[type] || type
    }
    
    // 采集方式名称映射
    const getCollectionMethodLabel = (method) => {
      const map = {
        'snmp': 'SNMP',
        'jmx': 'JMX',
        'wmi': 'WMI',
        'http': 'HTTP API',
        'ssh': 'SSH命令',
        'agent': 'Agent采集',
        'custom': '自定义脚本'
      }
      return map[method] || method
    }
    
    // 处理查询
    const handleQuery = () => {
      queryParams.pageNum = 1
      getList()
    }
    
    // 处理分页大小变化
    const handleSizeChange = (size) => {
      queryParams.pageSize = size
      getList()
    }
    
    // 处理页码变化
    const handleCurrentChange = (page) => {
      queryParams.pageNum = page
      getList()
    }
    
    // 处理选择变化
    const handleSelectionChange = (selection) => {
      selectedIds.value = selection.map(item => item.id)
    }
    
    // 处理添加
    const handleAdd = () => {
      isEdit.value = false
      currentMetric.value = {}
      dialogVisible.value = true
    }
    
    // 处理编辑
    const handleEdit = (row) => {
      isEdit.value = true
      currentMetric.value = JSON.parse(JSON.stringify(row))
      dialogVisible.value = true
    }
    
    // 处理克隆
    const handleClone = (row) => {
      isEdit.value = false
      const clonedMetric = JSON.parse(JSON.stringify(row))
      delete clonedMetric.id
      clonedMetric.metricName = `${clonedMetric.metricName}(复制)`
      clonedMetric.metricKey = `${clonedMetric.metricKey}_copy`
      currentMetric.value = clonedMetric
      dialogVisible.value = true
    }
    
    // 处理状态变更
    const handleStatusChange = async (row) => {
      row.statusLoading = true
      try {
        const response = await updateMetricStatus(row.id, row.status)
        if (response.code === 200) {
          ElMessage.success(`指标已${row.status === 1 ? '启用' : '禁用'}`)
        } else {
          // 恢复原状态
          row.status = row.status === 1 ? 0 : 1
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        // 恢复原状态
        row.status = row.status === 1 ? 0 : 1
        ElMessage.error('更新状态失败')
      } finally {
        row.statusLoading = false
      }
    }
    
    // 处理删除
    const handleDelete = (row) => {
      ElMessageBox.confirm(`确定要删除指标 "${row.metricName}" 吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteMetric(row.id)
          if (response.code === 200) {
            ElMessage.success('删除成功')
            getList()
          } else {
            ElMessage.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除指标失败:', error)
          ElMessage.error('删除失败')
        }
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 处理批量命令
    const handleBatchCommand = (command) => {
      switch (command) {
        case 'enable':
          handleBatchEnable()
          break
        case 'disable':
          handleBatchDisable()
          break
        case 'delete':
          handleBatchDelete()
          break
        case 'export':
          handleExport(false)
          break
        case 'exportAll':
          handleExport(true)
          break
        case 'import':
          importDialogVisible.value = true
          break
      }
    }
    
    // 批量启用
    const handleBatchEnable = async () => {
      try {
        const response = await batchUpdateMetricStatus(selectedIds.value, 1)
        if (response.code === 200) {
          ElMessage.success('批量启用成功')
          getList()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('批量操作失败:', error)
        ElMessage.error('批量操作失败')
      }
    }
    
    // 批量禁用
    const handleBatchDisable = async () => {
      try {
        const response = await batchUpdateMetricStatus(selectedIds.value, 0)
        if (response.code === 200) {
          ElMessage.success('批量禁用成功')
          getList()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        console.error('批量操作失败:', error)
        ElMessage.error('批量操作失败')
      }
    }
    
    // 批量删除
    const handleBatchDelete = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择需要删除的指标')
        return
      }
      
      ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个指标吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await batchDeleteMetrics(selectedIds.value)
          if (response.code === 200) {
            ElMessage.success('批量删除成功')
            getList()
          } else {
            ElMessage.error(response.message || '批量删除失败')
          }
        } catch (error) {
          console.error('批量删除失败:', error)
          ElMessage.error('批量删除失败')
        }
      }).catch(() => {
        // 用户取消操作
      })
    }
    
    // 导出指标
    const handleExport = async (exportAll) => {
      try {
        const ids = exportAll ? [] : selectedIds.value
        const response = await exportMetrics(ids)
        if (response.code === 200) {
          // 创建下载
          const blob = new Blob([JSON.stringify(response.data, null, 2)], { type: 'application/json' })
          const link = document.createElement('a')
          link.href = URL.createObjectURL(blob)
          link.download = `metrics_export_${new Date().getTime()}.json`
          link.click()
          URL.revokeObjectURL(link.href)
          
          ElMessage.success('导出成功')
        } else {
          ElMessage.error(response.message || '导出失败')
        }
      } catch (error) {
        console.error('导出失败:', error)
        ElMessage.error('导出失败')
      }
    }
    
    // 处理导入上传
    const handleImportUpload = async (options) => {
      const file = options.file
      if (file.size > 5 * 1024 * 1024) {
        ElMessage.error('文件大小不能超过5MB')
        return
      }
      
      const formData = new FormData()
      formData.append('file', file)
      
      try {
        const response = await importMetrics(formData)
        if (response.code === 200) {
          ElMessage.success(`成功导入 ${response.data.successCount} 个指标`)
          importDialogVisible.value = false
          getList()
        } else {
          ElMessage.error(response.message || '导入失败')
        }
      } catch (error) {
        console.error('导入失败:', error)
        ElMessage.error('导入失败')
      }
    }
    
    // 对话框处理成功
    const handleDialogSuccess = () => {
      getList()
    }
    
    // 页面加载时获取数据
    onMounted(() => {
      getList()
    })
    
    return {
      queryParams,
      metricList,
      total,
      loading,
      selectedIds,
      hasSelected,
      dialogVisible,
      importDialogVisible,
      isEdit,
      currentMetric,
      getTypeTagType,
      getMetricTypeLabel,
      getCollectionMethodLabel,
      handleQuery,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleAdd,
      handleEdit,
      handleClone,
      handleStatusChange,
      handleDelete,
      handleBatchCommand,
      handleImportUpload,
      handleDialogSuccess,
      getList
    }
  }
})
</script>

<style scoped>
.metric-table .el-table {
  margin-bottom: 20px;
}
</style> 