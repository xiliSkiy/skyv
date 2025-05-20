<template>
  <div class="collector-table">
    <!-- 工具栏 -->
    <div class="d-flex justify-content-between align-items-center mb-3">
      <div class="d-flex">
        <el-input
          v-model="queryParams.keyword"
          placeholder="搜索采集器名称或主机地址"
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
          v-model="queryParams.collectorType"
          placeholder="采集器类型"
          clearable
          @change="handleQuery"
          style="width: 140px"
          class="me-3">
          <el-option label="中心节点" value="master"></el-option>
          <el-option label="边缘节点" value="edge"></el-option>
          <el-option label="代理节点" value="proxy"></el-option>
          <el-option label="备份节点" value="backup"></el-option>
        </el-select>
        
        <el-select
          v-model="queryParams.collectionZone"
          placeholder="采集区域"
          clearable
          @change="handleQuery"
          style="width: 160px">
          <el-option 
            v-for="zone in zoneOptions" 
            :key="zone" 
            :label="zone" 
            :value="zone">
          </el-option>
        </el-select>
      </div>
      
      <div>
        <el-button type="primary" @click="handleAdd">
          <i class="fas fa-plus me-1"></i> 添加采集器
        </el-button>
        
        <el-dropdown @command="handleBatchCommand" trigger="click" class="ms-2">
          <el-button type="primary" plain>
            批量操作 <i class="fas fa-chevron-down ms-1"></i>
          </el-button>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="enable" :disabled="!hasSelected">批量启用</el-dropdown-item>
              <el-dropdown-item command="disable" :disabled="!hasSelected">批量禁用</el-dropdown-item>
              <el-dropdown-item command="delete" :disabled="!hasSelected">
                <span class="text-danger">批量删除</span>
              </el-dropdown-item>
              <el-dropdown-item command="testConnections" :disabled="!hasSelected">测试连接</el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </div>
    
    <!-- 数据表格 -->
    <el-table
      v-loading="loading"
      :data="collectorList"
      border
      @selection-change="handleSelectionChange"
      style="width: 100%">
      <el-table-column type="selection" width="55" align="center" />
      
      <el-table-column label="采集器名称" prop="collectorName" min-width="180" show-overflow-tooltip>
        <template #default="{ row }">
          <div class="d-flex align-items-center">
            <el-tag 
              :type="getTypeTagType(row.collectorType)" 
              effect="plain" 
              size="small" 
              class="me-2">
              {{ getCollectorTypeLabel(row.collectorType) }}
            </el-tag>
            <span class="fw-bold">{{ row.collectorName }}</span>
            <span class="ms-2" v-if="row.isPrimary === 1">
              <el-tag type="success" size="small">主采集器</el-tag>
            </span>
          </div>
          <div class="text-muted small">{{ row.hostname }}:{{ row.port }}</div>
        </template>
      </el-table-column>
      
      <el-table-column label="采集区域" prop="collectionZone" width="120">
        <template #default="{ row }">
          <el-tag size="small" effect="plain">
            {{ row.collectionZone }}
          </el-tag>
        </template>
      </el-table-column>
      
      <el-table-column label="连接状态" width="140">
        <template #default="{ row }">
          <template v-if="row.connectionState === 'connected'">
            <span class="d-flex align-items-center text-success">
              <i class="fas fa-circle me-1 connection-status-dot"></i>
              已连接
              <span class="ms-1 small" v-if="row.lastHeartbeat">
                <el-tooltip :content="formatDate(row.lastHeartbeat)">
                  <span class="text-muted">{{ formatTimeAgo(row.lastHeartbeat) }}</span>
                </el-tooltip>
              </span>
            </span>
          </template>
          
          <template v-else-if="row.connectionState === 'disconnected'">
            <span class="d-flex align-items-center text-danger">
              <i class="fas fa-circle me-1 connection-status-dot"></i>
              断开连接
              <span class="ms-1 small" v-if="row.lastHeartbeat">
                <el-tooltip :content="formatDate(row.lastHeartbeat)">
                  <span class="text-muted">{{ formatTimeAgo(row.lastHeartbeat) }}</span>
                </el-tooltip>
              </span>
            </span>
          </template>
          
          <template v-else-if="row.connectionState === 'warning'">
            <span class="d-flex align-items-center text-warning">
              <i class="fas fa-circle me-1 connection-status-dot"></i>
              连接不稳定
              <span class="ms-1 small" v-if="row.lastHeartbeat">
                <el-tooltip :content="formatDate(row.lastHeartbeat)">
                  <span class="text-muted">{{ formatTimeAgo(row.lastHeartbeat) }}</span>
                </el-tooltip>
              </span>
            </span>
          </template>
          
          <template v-else>
            <span class="d-flex align-items-center text-muted">
              <i class="fas fa-circle me-1 connection-status-dot"></i>
              未知状态
            </span>
          </template>
        </template>
      </el-table-column>
      
      <el-table-column label="版本" prop="version" width="110">
        <template #default="{ row }">
          <span v-if="row.version">{{ row.version }}</span>
          <span v-else class="text-muted">未知</span>
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
      
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" link @click="handleEdit(row)">
            <i class="fas fa-edit"></i> 编辑
          </el-button>
          <el-button type="primary" link @click="handleTest(row)">
            <i class="fas fa-link"></i> 测试连接
          </el-button>
          <el-button type="success" link @click="handleSetPrimary(row)" v-if="row.isPrimary !== 1">
            <i class="fas fa-star"></i> 设为主采集器
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
    
    <!-- 添加/编辑采集器对话框 -->
    <collector-dialog
      v-model:visible="dialogVisible"
      :collector="currentCollector"
      :is-edit="isEdit"
      @success="handleDialogSuccess">
    </collector-dialog>
  </div>
</template>

<script>
import { defineComponent, ref, reactive, computed, onMounted, onBeforeUnmount } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import CollectorDialog from './CollectorDialog.vue'
import { 
  getCollectorList,
  deleteCollector,
  updateCollectorStatus,
  batchDeleteCollectors,
  batchUpdateCollectorStatus,
  testCollectorConnection,
  batchTestCollectorConnections,
  setPrimaryCollector,
  getCollectionZones
} from '@/api/metrics'

export default defineComponent({
  name: 'CollectorTable',
  components: {
    CollectorDialog
  },
  setup() {
    // 查询参数
    const queryParams = reactive({
      pageNum: 1,
      pageSize: 10,
      keyword: '',
      collectorType: '',
      collectionZone: '',
      status: ''
    })
    
    // 数据列表
    const collectorList = ref([])
    const total = ref(0)
    const loading = ref(false)
    const zoneOptions = ref([])
    
    // 选中数据
    const selectedIds = ref([])
    const hasSelected = computed(() => selectedIds.value.length > 0)
    
    // 弹窗控制
    const dialogVisible = ref(false)
    const isEdit = ref(false)
    const currentCollector = ref({})
    
    // 状态轮询定时器
    let statusTimer = null
    
    // 获取采集器列表
    const getList = async () => {
      loading.value = true
      try {
        const response = await getCollectorList(queryParams)
        if (response.code === 200) {
          collectorList.value = response.data.content || []
          total.value = response.data.totalElements || 0
          
          // 添加状态加载标志
          collectorList.value.forEach(item => {
            item.statusLoading = false
          })
        }
      } catch (error) {
        console.error('获取采集器列表失败:', error)
        ElMessage.error('获取采集器列表失败')
      } finally {
        loading.value = false
      }
    }
    
    // 获取采集区域列表
    const getZones = async () => {
      try {
        const response = await getCollectionZones()
        if (response.code === 200) {
          zoneOptions.value = response.data || []
        }
      } catch (error) {
        console.error('获取采集区域列表失败:', error)
      }
    }
    
    // 采集器类型标签类型映射
    const getTypeTagType = (type) => {
      const map = {
        'master': 'danger',
        'edge': 'success',
        'proxy': 'warning',
        'backup': 'info'
      }
      return map[type] || 'info'
    }
    
    // 采集器类型名称映射
    const getCollectorTypeLabel = (type) => {
      const map = {
        'master': '中心节点',
        'edge': '边缘节点',
        'proxy': '代理节点',
        'backup': '备份节点'
      }
      return map[type] || type
    }
    
    // 格式化日期
    const formatDate = (dateStr) => {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString()
    }
    
    // 格式化相对时间
    const formatTimeAgo = (dateStr) => {
      if (!dateStr) return ''
      
      const date = new Date(dateStr)
      const now = new Date()
      const diffSeconds = Math.floor((now - date) / 1000)
      
      if (diffSeconds < 60) {
        return `${diffSeconds}秒前`
      } else if (diffSeconds < 3600) {
        return `${Math.floor(diffSeconds / 60)}分钟前`
      } else if (diffSeconds < 86400) {
        return `${Math.floor(diffSeconds / 3600)}小时前`
      } else {
        return `${Math.floor(diffSeconds / 86400)}天前`
      }
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
      currentCollector.value = {}
      dialogVisible.value = true
    }
    
    // 处理编辑
    const handleEdit = (row) => {
      isEdit.value = true
      currentCollector.value = JSON.parse(JSON.stringify(row))
      dialogVisible.value = true
    }
    
    // 处理测试连接
    const handleTest = async (row) => {
      try {
        ElMessage.info(`正在测试与采集器 ${row.collectorName} 的连接...`)
        
        const params = {
          hostname: row.hostname,
          port: row.port,
          authType: row.authType || 'none',
          username: row.username,
          password: row.password,
          apiKey: row.apiKey,
          certificatePath: row.certificatePath,
          privateKeyPath: row.privateKeyPath,
          connectionTimeout: row.connectionTimeout || 30
        }
        
        const response = await testCollectorConnection(params)
        
        if (response.code === 200) {
          ElMessage.success('连接测试成功')
          
          // 如果返回了版本信息，更新表格中的版本
          if (response.data && response.data.version) {
            const index = collectorList.value.findIndex(item => item.id === row.id)
            if (index !== -1) {
              collectorList.value[index].version = response.data.version
              collectorList.value[index].connectionState = 'connected'
              collectorList.value[index].lastHeartbeat = new Date().toISOString()
            }
          }
        } else {
          ElMessage.error(`连接测试失败: ${response.message}`)
          
          // 更新连接状态
          const index = collectorList.value.findIndex(item => item.id === row.id)
          if (index !== -1) {
            collectorList.value[index].connectionState = 'disconnected'
          }
        }
      } catch (error) {
        ElMessage.error(`连接测试出错: ${error.message || '未知错误'}`)
      }
    }
    
    // 设置为主采集器
    const handleSetPrimary = async (row) => {
      try {
        const response = await setPrimaryCollector(row.id)
        if (response.code === 200) {
          ElMessage.success(`已将 ${row.collectorName} 设置为主采集器`)
          getList()
        } else {
          ElMessage.error(response.message || '操作失败')
        }
      } catch (error) {
        ElMessage.error('设置主采集器失败')
      }
    }
    
    // 处理状态变更
    const handleStatusChange = async (row) => {
      row.statusLoading = true
      try {
        const response = await updateCollectorStatus(row.id, row.status)
        if (response.code === 200) {
          ElMessage.success(`采集器已${row.status === 1 ? '启用' : '禁用'}`)
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
      ElMessageBox.confirm(`确定要删除采集器 "${row.collectorName}" 吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await deleteCollector(row.id)
          if (response.code === 200) {
            ElMessage.success('删除成功')
            getList()
          } else {
            ElMessage.error(response.message || '删除失败')
          }
        } catch (error) {
          console.error('删除采集器失败:', error)
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
        case 'testConnections':
          handleBatchTest()
          break
      }
    }
    
    // 批量启用
    const handleBatchEnable = async () => {
      try {
        const response = await batchUpdateCollectorStatus(selectedIds.value, 1)
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
        const response = await batchUpdateCollectorStatus(selectedIds.value, 0)
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
        ElMessage.warning('请选择需要删除的采集器')
        return
      }
      
      ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个采集器吗？`, '确认删除', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          const response = await batchDeleteCollectors(selectedIds.value)
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
    
    // 批量测试连接
    const handleBatchTest = async () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择需要测试的采集器')
        return
      }
      
      try {
        ElMessage.info(`正在测试 ${selectedIds.value.length} 个采集器连接...`)
        
        const response = await batchTestCollectorConnections(selectedIds.value)
        if (response.code === 200) {
          const results = response.data || {}
          const success = results.success || []
          const failed = results.failed || []
          
          if (success.length > 0) {
            ElMessage.success(`${success.length} 个采集器连接测试成功`)
          }
          
          if (failed.length > 0) {
            ElMessage.error(`${failed.length} 个采集器连接测试失败`)
          }
          
          getList()
        } else {
          ElMessage.error(response.message || '批量测试连接失败')
        }
      } catch (error) {
        console.error('批量测试连接失败:', error)
        ElMessage.error('批量测试连接失败')
      }
    }
    
    // 对话框处理成功
    const handleDialogSuccess = () => {
      getList()
    }
    
    // 定时刷新状态
    const startStatusRefresh = () => {
      stopStatusRefresh()
      statusTimer = setInterval(() => {
        if (!loading.value) {
          getList()
        }
      }, 30000) // 每30秒刷新一次
    }
    
    // 停止状态刷新
    const stopStatusRefresh = () => {
      if (statusTimer) {
        clearInterval(statusTimer)
        statusTimer = null
      }
    }
    
    // 页面加载时获取数据
    onMounted(() => {
      getList()
      getZones()
      startStatusRefresh()
    })
    
    // 页面卸载时清理
    onBeforeUnmount(() => {
      stopStatusRefresh()
    })
    
    return {
      queryParams,
      collectorList,
      total,
      loading,
      zoneOptions,
      selectedIds,
      hasSelected,
      dialogVisible,
      isEdit,
      currentCollector,
      getTypeTagType,
      getCollectorTypeLabel,
      formatDate,
      formatTimeAgo,
      handleQuery,
      handleSizeChange,
      handleCurrentChange,
      handleSelectionChange,
      handleAdd,
      handleEdit,
      handleTest,
      handleSetPrimary,
      handleStatusChange,
      handleDelete,
      handleBatchCommand,
      handleDialogSuccess,
      getList
    }
  }
})
</script>

<style scoped>
.collector-table .el-table {
  margin-bottom: 20px;
}

.connection-status-dot {
  font-size: 10px;
}
</style> 