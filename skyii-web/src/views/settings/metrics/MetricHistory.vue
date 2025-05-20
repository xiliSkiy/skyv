<template>
  <div class="metric-history">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="指标">
          <el-select 
            v-model="queryParams.metricId" 
            placeholder="选择指标" 
            clearable 
            filterable 
            style="width: 200px;">
            <el-option 
              v-for="item in metricOptions" 
              :key="item.id" 
              :label="item.metricName" 
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="设备">
          <el-select 
            v-model="queryParams.deviceId" 
            placeholder="选择设备" 
            clearable 
            filterable 
            style="width: 200px;">
            <el-option 
              v-for="item in deviceOptions" 
              :key="item.id" 
              :label="item.deviceName" 
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable style="width: 120px;">
            <el-option label="成功" :value="0"></el-option>
            <el-option label="失败" :value="1"></el-option>
            <el-option label="超时" :value="2"></el-option>
            <el-option label="处理中" :value="3"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="采集时间">
          <el-date-picker
            v-model="dateRange"
            type="datetimerange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            :shortcuts="dateShortcuts"
            format="YYYY-MM-DD HH:mm:ss"
            value-format="YYYY-MM-DD HH:mm:ss"
            style="width: 380px;">
          </el-date-picker>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 统计信息 -->
    <div class="stat-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">今日采集次数</div>
            <div class="stat-value">{{ statistics.todayCount || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">本周采集次数</div>
            <div class="stat-value">{{ statistics.weekCount || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">本月采集次数</div>
            <div class="stat-value">{{ statistics.monthCount || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">成功率</div>
            <div class="stat-value">{{ successRate }}%</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作按钮 -->
    <div class="action-container">
      <el-button type="primary" @click="handleTriggerCollection">手动触发采集</el-button>
      <el-button type="danger" @click="handleCleanHistory">清理历史数据</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      <el-button type="primary" @click="handleRefresh"><i class="el-icon-refresh"></i> 刷新</el-button>
    </div>

    <!-- 表格区域 -->
    <el-table 
      v-loading="loading" 
      :data="historyList" 
      border 
      stripe 
      @selection-change="handleSelectionChange"
      style="width: 100%; margin-top: 15px;">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="metricName" label="指标名称" width="160" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="deviceName" label="设备名称" width="150" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="collectorName" label="采集器" width="120" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="collectionTime" label="采集时间" width="180" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="processedValue" label="采集值" width="120">
        <template #default="scope">
          {{ scope.row.processedValue }} {{ scope.row.unit || '' }}
        </template>
      </el-table-column>
      <el-table-column prop="collectionDuration" label="耗时(毫秒)" width="120"></el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ scope.row.statusText }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" @click="handleViewDetail(scope.row)">详情</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页区域 -->
    <div class="pagination-container">
      <el-pagination
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
        :current-page="queryParams.page"
        :page-sizes="[10, 20, 50, 100]"
        :page-size="queryParams.limit"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total">
      </el-pagination>
    </div>

    <!-- 详情对话框 -->
    <el-dialog title="采集历史详情" v-model="detailDialog.visible" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="指标名称" :span="2">{{ detail.metricName }}</el-descriptions-item>
        <el-descriptions-item label="设备名称">{{ detail.deviceName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采集器">{{ detail.collectorName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="采集时间">{{ detail.collectionTime }}</el-descriptions-item>
        <el-descriptions-item label="耗时">{{ detail.collectionDuration }}毫秒</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusType(detail.status)">{{ detail.statusText }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="处理结果">{{ detail.processedValue }} {{ detail.unit || '' }}</el-descriptions-item>
        <el-descriptions-item label="错误信息" :span="2" v-if="detail.statusInfo">
          <div class="error-info">{{ detail.statusInfo }}</div>
        </el-descriptions-item>
        <el-descriptions-item label="原始值" :span="2">
          <pre class="raw-value">{{ formatRawValue(detail.rawValue) }}</pre>
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialog.visible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 手动触发采集对话框 -->
    <el-dialog title="手动触发采集" v-model="triggerDialog.visible" width="500px">
      <el-form ref="triggerForm" :model="triggerForm" :rules="triggerRules" label-width="100px">
        <el-form-item label="指标" prop="metricId">
          <el-select v-model="triggerForm.metricId" placeholder="请选择指标" filterable style="width: 100%;">
            <el-option 
              v-for="item in metricOptions" 
              :key="item.id" 
              :label="item.metricName" 
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="设备" prop="deviceId">
          <el-select v-model="triggerForm.deviceId" placeholder="请选择设备(可选)" filterable clearable style="width: 100%;">
            <el-option 
              v-for="item in deviceOptions" 
              :key="item.id" 
              :label="item.deviceName" 
              :value="item.id">
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="triggerDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitTriggerForm" :loading="triggerDialog.loading">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 清理历史数据对话框 -->
    <el-dialog title="清理历史数据" v-model="cleanDialog.visible" width="400px">
      <el-form ref="cleanForm" :model="cleanForm" :rules="cleanRules" label-width="120px">
        <el-form-item label="保留天数" prop="days">
          <el-input-number v-model="cleanForm.days" :min="1" :max="365"></el-input-number>
        </el-form-item>
        <div class="clean-tip">
          将删除 {{ cleanForm.days }} 天前的所有历史数据，此操作不可恢复，请谨慎操作！
        </div>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="cleanDialog.visible = false">取消</el-button>
          <el-button type="danger" @click="submitCleanForm" :loading="cleanDialog.loading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getMetricHistory, 
  getMetricHistoryById,
  batchDeleteMetricHistory,
  getMetricHistoryStatistics,
  triggerMetricCollection,
  cleanMetricHistory
} from '@/api/metrics'
import { getAllMetrics } from '@/api/metrics'
import { getDeviceList } from '@/api/device'

export default {  name: 'MetricHistory',  props: {    metricId: {      type: Number,      default: null    }  },  setup(props) {
    // 查询参数
    const queryParams = reactive({
      page: 1,
      limit: 10,
      metricId: null,
      deviceId: null,
      status: null,
      startTime: null,
      endTime: null
    })

    // 日期范围
    const dateRange = ref(null)

    // 日期快捷选项
    const dateShortcuts = [
      {
        text: '最近一小时',
        value: () => {
          const end = new Date()
          const start = new Date()
          start.setTime(start.getTime() - 3600 * 1000)
          return [start, end]
        }
      },
      {
        text: '今天',
        value: () => {
          const end = new Date()
          const start = new Date(new Date().toDateString())
          return [start, end]
        }
      },
      {
        text: '昨天',
        value: () => {
          const now = new Date()
          const start = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1, 0, 0, 0)
          const end = new Date(now.getFullYear(), now.getMonth(), now.getDate() - 1, 23, 59, 59)
          return [start, end]
        }
      },
      {
        text: '最近一周',
        value: () => {
          const end = new Date()
          const start = new Date()
          start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
          return [start, end]
        }
      }
    ]

    // 手动触发采集表单
    const triggerForm = reactive({
      metricId: null,
      deviceId: null
    })

    // 清理历史数据表单
    const cleanForm = reactive({
      days: 30
    })

    // 表单校验规则
    const triggerRules = {
      metricId: [
        { required: true, message: '请选择指标', trigger: 'change' }
      ]
    }

    const cleanRules = {
      days: [
        { required: true, message: '请输入保留天数', trigger: 'blur' },
        { type: 'number', min: 1, max: 365, message: '保留天数范围为1-365', trigger: 'blur' }
      ]
    }

    // 数据
    const loading = ref(false)
    const historyList = ref([])
    const total = ref(0)
    const statistics = ref({})
    const selectedIds = ref([])
    const metricOptions = ref([])
    const deviceOptions = ref([])
    const detail = ref({})

    // 对话框
    const detailDialog = reactive({
      visible: false
    })

    const triggerDialog = reactive({
      visible: false,
      loading: false
    })

    const cleanDialog = reactive({
      visible: false,
      loading: false
    })

    // 计算属性
    const successRate = computed(() => {
      const statusCounts = statistics.value.statusCounts || {}
      const successCount = statusCounts[0] || 0
      const totalCount = Object.values(statusCounts).reduce((sum, count) => sum + count, 0)
      return totalCount === 0 ? 100 : Math.round((successCount / totalCount) * 100)
    })

    // 获取状态类型
    const getStatusType = (status) => {
      switch (status) {
        case 0:
          return 'success'
        case 1:
          return 'danger'
        case 2:
          return 'warning'
        case 3:
          return 'info'
        default:
          return 'info'
      }
    }

    // 格式化原始值
    const formatRawValue = (rawValue) => {
      if (!rawValue) return ''
      
      try {
        // 尝试解析为JSON格式化
        const obj = JSON.parse(rawValue)
        return JSON.stringify(obj, null, 2)
      } catch (e) {
        // 非JSON格式，直接返回
        return rawValue
      }
    }

    // 查询历史数据    
    const fetchData = async () => {      
      loading.value = true            
      // 处理日期范围      
      if (dateRange.value && dateRange.value.length === 2) {        
        queryParams.startTime = dateRange.value[0]        
        queryParams.endTime = dateRange.value[1]      
      } else {        
          queryParams.startTime = null        
          queryParams.endTime = null      
      }            
      // 如果指定了特定的指标ID（从父组件传入），则优先使用它      
      if (props.metricId) {        
        queryParams.metricId = props.metricId      
      }            
      try {        
        const params = {          
          page: queryParams.page - 1,          
          size: queryParams.limit,          
          metricId: queryParams.metricId,          
          deviceId: queryParams.deviceId,          
          status: queryParams.status,          
          startTime: queryParams.startTime,          
          endTime: queryParams.endTime        
        }                
        const response = await getMetricHistory(params)        
        historyList.value = response.data.content        
        total.value = response.data.totalElements      
      } catch (error) {        
        console.error('获取历史数据失败', error)        
        ElMessage.error('获取历史数据失败')      
      } finally {        
        loading.value = false      
      }    
    }

    // 查询统计数据
    const fetchStatistics = async () => {
      try {
        const response = await getMetricHistoryStatistics()
        statistics.value = response.data
      } catch (error) {
        console.error('获取统计数据失败', error)
      }
    }

    // 获取所有指标
    const fetchMetrics = async () => {
      try {
        const response = await getAllMetrics()
        metricOptions.value = response.data
      } catch (error) {
        console.error('获取指标列表失败', error)
      }
    }

    // 获取设备列表    
    const fetchDevices = async () => {      
      try {        
        const response = await getDeviceList({ page: 0, size: 1000 })        
        deviceOptions.value = response.data.content      
      } catch (error) {        
        console.error('获取设备列表失败', error)      }    
      }    
      // 表格选择变化    
      const handleSelectionChange = (selection) => {      
        selectedIds.value = selection.map(item => item.id)    
      }    
      // 分页大小变化    
      const handleSizeChange = (size) => {      
        queryParams.limit = size      
        fetchData()    
      }    
      // 当前页变化    
      const handleCurrentChange = (page) => {      
        queryParams.page = page      
        fetchData()    
      }    
      // 查看详情    
      const handleViewDetail = async (row) => {      
        try {        
          const response = await getMetricHistoryById(row.id)        
          detail.value = response.data        
          detailDialog.visible = true      
          } catch (error) {        
            console.error('获取详情失败', error)        
            ElMessage.error('获取详情失败')      
          }    
        }    
        // 删除单条记录    
        const handleDelete = (row) => {      
          ElMessageBox.confirm('确定要删除该记录吗？', '删除确认', {        
            confirmButtonText: '确定',        
            cancelButtonText: '取消',        
            type: 'warning'      
          }).then(() => {        
            batchDeleteMetricHistory([row.id]).then(() => {          
              ElMessage.success('删除成功')          
              fetchData()          
              fetchStatistics()        
            }).catch(error => {          
              console.error('删除失败', error)          
              ElMessage.error('删除失败')        
              })      
            }).catch(() => {})    
          }    
        // 查询    
        const handleQuery = () => {      
          queryParams.page = 1      
          fetchData()    
        }        
        // 重置查询    
        const resetQuery = () => {      
          queryParams.metricId = null      
          queryParams.deviceId = null      
          queryParams.status = null      
          dateRange.value = null      
          queryParams.startTime = null      
          queryParams.endTime = null      
          queryParams.page = 1      
          fetchData()    
        }

    

    // 批量删除
    const handleBatchDelete = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择要删除的记录')
        return
      }
      
      ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 条记录吗？该操作不可逆`, '批量删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        batchDeleteMetricHistory(selectedIds.value).then(() => {
          ElMessage.success('批量删除成功')
          fetchData()
          fetchStatistics()
        }).catch(error => {
          console.error('批量删除失败', error)
          ElMessage.error('批量删除失败')
        })
      }).catch(() => {})
    }

    // 手动触发采集
    const handleTriggerCollection = () => {
      triggerForm.metricId = null
      triggerForm.deviceId = null
      triggerDialog.visible = true
      
      if (metricOptions.value.length === 0) {
        fetchMetrics()
      }
      
      if (deviceOptions.value.length === 0) {
        fetchDevices()
      }
    }

    // 提交手动触发采集表单
    const submitTriggerForm = () => {
      triggerDialog.loading = true
      
      triggerMetricCollection(triggerForm.metricId, triggerForm.deviceId).then(response => {
        ElMessage.success('触发采集成功')
        triggerDialog.visible = false
        fetchData()
        fetchStatistics()
      }).catch(error => {
        console.error('触发采集失败', error)
        ElMessage.error('触发采集失败: ' + error.message)
      }).finally(() => {
        triggerDialog.loading = false
      })
    }

    // 清理历史数据
    const handleCleanHistory = () => {
      cleanDialog.visible = true
    }

    // 提交清理历史数据表单
    const submitCleanForm = () => {
      ElMessageBox.confirm(`确定要删除 ${cleanForm.days} 天前的所有历史数据吗？该操作不可逆`, '清理确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        cleanDialog.loading = true
        
        cleanMetricHistory(cleanForm.days).then(response => {
          const count = response.data
          ElMessage.success(`清理成功，共删除 ${count} 条历史数据`)
          cleanDialog.visible = false
          fetchData()
          fetchStatistics()
        }).catch(error => {
          console.error('清理失败', error)
          ElMessage.error('清理失败: ' + error.message)
        }).finally(() => {
          cleanDialog.loading = false
        })
      }).catch(() => {})
    }

    // 刷新
    const handleRefresh = () => {
      fetchData()
      fetchStatistics()
      ElMessage.success('刷新成功')
    }

    // 监听props变化    watch(() => props.metricId, (newVal) => {      if (newVal) {        queryParams.metricId = newVal        fetchData()      }    })        onMounted(() => {      // 如果指定了指标ID，初始化时设置查询参数      if (props.metricId) {        queryParams.metricId = props.metricId      }            fetchData()      fetchStatistics()      fetchMetrics()      fetchDevices()    })

    return {
      queryParams,
      dateRange,
      dateShortcuts,
      triggerForm,
      cleanForm,
      triggerRules,
      cleanRules,
      loading,
      historyList,
      total,
      statistics,
      selectedIds,
      metricOptions,
      deviceOptions,
      detail,
      detailDialog,
      triggerDialog,
      cleanDialog,
      successRate,
      getStatusType,
      formatRawValue,
      fetchData,
      handleQuery,
      resetQuery,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange,
      handleViewDetail,
      handleDelete,
      handleBatchDelete,
      handleTriggerCollection,
      submitTriggerForm,
      handleCleanHistory,
      submitCleanForm,
      handleRefresh
    }
  }
}
</script>

<style scoped>
.metric-history {
  padding: 10px;
}

.search-container {
  margin-bottom: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.stat-container {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 10px;
}

.stat-title {
  font-size: 14px;
  color: #606266;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
}

.action-container {
  margin-bottom: 15px;
}

.pagination-container {
  margin-top: 15px;
  text-align: right;
}

.raw-value {
  margin: 0;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  max-height: 200px;
  overflow: auto;
  white-space: pre-wrap;
  word-break: break-all;
  font-family: monospace;
}

.error-info {
  color: #F56C6C;
  word-break: break-all;
}

.clean-tip {
  color: #F56C6C;
  margin-top: 10px;
  font-size: 14px;
}
</style> 