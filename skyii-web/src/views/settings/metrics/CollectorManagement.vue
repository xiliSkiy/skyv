<template>
  <div class="collector-management">
    <!-- 搜索区域 -->
    <div class="search-container">
      <el-form :inline="true" :model="queryParams" class="demo-form-inline">
        <el-form-item label="采集器名称">
          <el-input v-model="queryParams.collectorName" placeholder="采集器名称" clearable></el-input>
        </el-form-item>
        <el-form-item label="采集器类型">
          <el-select v-model="queryParams.collectorType" placeholder="采集器类型" clearable>
            <el-option label="SNMP" value="snmp"></el-option>
            <el-option label="HTTP" value="http"></el-option>
            <el-option label="MQTT" value="mqtt"></el-option>
            <el-option label="Modbus" value="modbus"></el-option>
            <el-option label="自定义" value="custom"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="queryParams.status" placeholder="状态" clearable>
            <el-option label="在线" :value="1"></el-option>
            <el-option label="离线" :value="0"></el-option>
            <el-option label="异常" :value="2"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">查询</el-button>
          <el-button @click="resetQuery">重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 状态统计 -->
    <div class="stat-container">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card">
            <div class="stat-title">采集器总数</div>
            <div class="stat-value">{{ collectorStatus.total || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card success-card">
            <div class="stat-title">在线</div>
            <div class="stat-value">{{ collectorStatus.online || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card warning-card">
            <div class="stat-title">离线</div>
            <div class="stat-value">{{ collectorStatus.offline || 0 }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-card danger-card">
            <div class="stat-title">异常</div>
            <div class="stat-value">{{ collectorStatus.error || 0 }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 操作按钮 -->
    <div class="action-container">
      <el-button type="primary" @click="handleAdd">新增采集器</el-button>
      <el-button type="danger" :disabled="selectedIds.length === 0" @click="handleBatchDelete">批量删除</el-button>
      <el-button :disabled="selectedIds.length === 0" @click="handleBatchTest">批量测试连接</el-button>
      <el-button type="primary" @click="handleRefresh"><i class="el-icon-refresh"></i> 刷新</el-button>
    </div>

    <!-- 表格区域 -->
    <el-table 
      v-loading="loading" 
      :data="collectorList" 
      border 
      stripe 
      @selection-change="handleSelectionChange"
      style="width: 100%; margin-top: 15px;">
      <el-table-column type="selection" width="55"></el-table-column>
      <el-table-column prop="id" label="ID" width="80"></el-table-column>
      <el-table-column prop="collectorName" label="采集器名称" width="150"></el-table-column>
      <el-table-column prop="collectorType" label="类型" width="100">
        <template #default="scope">
          <el-tag>{{ scope.row.collectorType }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="host" label="主机地址" width="150"></el-table-column>
      <el-table-column prop="port" label="端口" width="100"></el-table-column>
      <el-table-column prop="description" label="描述" :show-overflow-tooltip="true"></el-table-column>
      <el-table-column prop="isMain" label="主采集器" width="100">
        <template #default="scope">
          <el-tag type="success" v-if="scope.row.isMain">是</el-tag>
          <el-tag type="info" v-else>否</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">
          <el-tag :type="getStatusType(scope.row.status)">
            {{ getStatusText(scope.row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="lastHeartbeat" label="最后心跳" width="180"></el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="success" @click="handleTest(scope.row)">测试连接</el-button>
          <el-button size="small" type="info" @click="handleSetPrimary(scope.row)" :disabled="scope.row.isMain">设为主要</el-button>
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

    <!-- 新增/编辑对话框 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="600px">
      <el-form ref="collectorForm_ref" :model="collectorForm" :rules="rules" label-width="100px">
        <el-form-item label="采集器名称" prop="collectorName">
          <el-input v-model="collectorForm.collectorName" placeholder="请输入采集器名称"></el-input>
        </el-form-item>
        <el-form-item label="采集器类型" prop="collectorType">
          <el-select v-model="collectorForm.collectorType" placeholder="请选择采集器类型" style="width: 100%;">
            <el-option label="SNMP" value="snmp"></el-option>
            <el-option label="HTTP" value="http"></el-option>
            <el-option label="MQTT" value="mqtt"></el-option>
            <el-option label="Modbus" value="modbus"></el-option>
            <el-option label="自定义" value="custom"></el-option>
          </el-select>
        </el-form-item>
        <el-form-item label="主机地址" prop="host">
          <el-input v-model="collectorForm.host" placeholder="请输入主机地址"></el-input>
        </el-form-item>
        <el-form-item label="端口" prop="port">
          <el-input-number v-model="collectorForm.port" :min="1" :max="65535" style="width: 100%;"></el-input-number>
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input v-model="collectorForm.description" type="textarea" placeholder="请输入描述"></el-input>
        </el-form-item>
        <el-form-item label="是否主要" prop="isMain">
          <el-switch v-model="collectorForm.isMain"></el-switch>
        </el-form-item>
        <el-form-item label="配置参数" prop="configParams">
          <el-input 
            v-model="collectorForm.configParams" 
            type="textarea" 
            :rows="6" 
            placeholder="请输入JSON格式的配置参数"></el-input>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialog.visible = false">取消</el-button>
          <el-button type="primary" @click="submitForm" :loading="dialog.loading">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  getCollectors, 
  getCollectorById, 
  createCollector, 
  updateCollector, 
  deleteCollector, 
  batchDeleteCollectors,
  testCollectorConnection,
  batchTestCollectorConnections,
  setPrimaryCollector,
  getCollectorStatusStatistics
} from '@/api/metrics'

export default {
  name: 'CollectorManagement',
  setup() {
    // 查询参数
    const queryParams = reactive({
      page: 1,
      limit: 10,
      collectorName: '',
      collectorType: '',
      status: null
    })

    // 采集器表单
    const collectorForm = reactive({
      id: null,
      collectorName: '',
      collectorType: '',
      host: '',
      port: null,
      description: '',
      isMain: false,
      configParams: '{}'
    })

    // 表单校验规则
    const rules = {
      collectorName: [
        { required: true, message: '请输入采集器名称', trigger: 'blur' },
        { min: 2, max: 50, message: '长度在 2 到 50 个字符', trigger: 'blur' }
      ],
      collectorType: [
        { required: true, message: '请选择采集器类型', trigger: 'change' }
      ],
      host: [
        { required: true, message: '请输入主机地址', trigger: 'blur' }
      ],
      port: [
        { required: true, message: '请输入端口号', trigger: 'blur' },
        { type: 'number', message: '端口号必须为数字', trigger: 'blur' }
      ],
      configParams: [
        { 
          validator: (rule, value, callback) => {
            try {
              if (value) {
                JSON.parse(value)
              }
              callback()
            } catch (error) {
              callback(new Error('请输入有效的JSON格式'))
            }
          }, 
          trigger: 'blur' 
        }
      ]
    }

    // 数据
    const loading = ref(false)
    const collectorList = ref([])
    const total = ref(0)
    const selectedIds = ref([])
    const collectorStatus = reactive({
      total: 0,
      online: 0,
      offline: 0,
      error: 0
    })

    // 对话框
    const dialog = reactive({
      visible: false,
      title: '',
      loading: false
    })

    // 表单引用
    const collectorForm_ref = ref(null)

    // 获取状态类型
    const getStatusType = (status) => {
      switch (status) {
        case 1:
          return 'success'
        case 0:
          return 'warning'
        case 2:
          return 'danger'
        default:
          return 'info'
      }
    }

    // 获取状态文本
    const getStatusText = (status) => {
      switch (status) {
        case 1:
          return '在线'
        case 0:
          return '离线'
        case 2:
          return '异常'
        default:
          return '未知'
      }
    }

    // 查询采集器列表
    const fetchData = async () => {
      loading.value = true
      try {
        const params = {
          page: queryParams.page - 1,
          size: queryParams.limit,
          collectorName: queryParams.collectorName || null,
          collectorType: queryParams.collectorType || null,
          status: queryParams.status
        }
        const response = await getCollectors(params)
        collectorList.value = response.data.content
        total.value = response.data.totalElements
      } catch (error) {
        console.error('获取采集器列表失败', error)
        ElMessage.error('获取采集器列表失败')
      } finally {
        loading.value = false
      }
    }

    // 查询采集器状态统计
    const fetchCollectorStatus = async () => {
      try {
        const response = await getCollectorStatusStatistics()
        const statusData = response.data
        
        collectorStatus.total = statusData.total || 0
        collectorStatus.online = statusData.status1 || 0
        collectorStatus.offline = statusData.status0 || 0
        collectorStatus.error = statusData.status2 || 0
      } catch (error) {
        console.error('获取采集器状态统计失败', error)
      }
    }

    // 查询
    const handleQuery = () => {
      queryParams.page = 1
      fetchData()
    }

    // 重置查询
    const resetQuery = () => {
      queryParams.collectorName = ''
      queryParams.collectorType = ''
      queryParams.status = null
      handleQuery()
    }

    // 表格选择变化
    const handleSelectionChange = (selection) => {
      selectedIds.value = selection.map(item => item.id)
    }

    // 每页数量变化
    const handleSizeChange = (val) => {
      queryParams.limit = val
      fetchData()
    }

    // 当前页变化
    const handleCurrentChange = (val) => {
      queryParams.page = val
      fetchData()
    }

    // 新增采集器
    const handleAdd = () => {
      dialog.title = '新增采集器'
      dialog.visible = true
      Object.keys(collectorForm).forEach(key => {
        collectorForm[key] = key === 'configParams' ? '{}' : (key === 'port' ? null : '')
      })
      collectorForm.isMain = false
      collectorForm.id = null
    }

    // 编辑采集器
    const handleEdit = async (row) => {
      dialog.title = '编辑采集器'
      dialog.visible = true
      dialog.loading = true
      
      try {
        const response = await getCollectorById(row.id)
        Object.keys(collectorForm).forEach(key => {
          collectorForm[key] = response.data[key]
        })
      } catch (error) {
        console.error('获取采集器详情失败', error)
        ElMessage.error('获取采集器详情失败')
      } finally {
        dialog.loading = false
      }
    }

    // 删除采集器
    const handleDelete = (row) => {
      ElMessageBox.confirm('确定要删除此采集器吗？该操作不可逆', '删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        deleteCollector(row.id).then(() => {
          ElMessage.success('删除成功')
          fetchData()
          fetchCollectorStatus()
        }).catch(error => {
          console.error('删除失败', error)
          ElMessage.error('删除失败')
        })
      }).catch(() => {})
    }

    // 批量删除
    const handleBatchDelete = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择要删除的采集器')
        return
      }
      
      ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个采集器吗？该操作不可逆`, '批量删除确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        batchDeleteCollectors(selectedIds.value).then(() => {
          ElMessage.success('批量删除成功')
          fetchData()
          fetchCollectorStatus()
        }).catch(error => {
          console.error('批量删除失败', error)
          ElMessage.error('批量删除失败')
        })
      }).catch(() => {})
    }

    // 提交表单
    const submitForm = () => {
      collectorForm_ref.value?.validate((valid) => {
        if (valid) {
          dialog.loading = true
          
          const isEdit = collectorForm.id !== null
          const api = isEdit ? updateCollector(collectorForm.id, collectorForm) : createCollector(collectorForm)
          
          api.then(() => {
            ElMessage.success(isEdit ? '编辑成功' : '新增成功')
            dialog.visible = false
            fetchData()
            fetchCollectorStatus()
          }).catch(error => {
            console.error(isEdit ? '编辑失败' : '新增失败', error)
            ElMessage.error(isEdit ? '编辑失败' : '新增失败')
          }).finally(() => {
            dialog.loading = false
          })
        }
      })
    }

    // 测试连接
    const handleTest = (row) => {
      const loading = ElMessage({
        message: '正在测试连接...',
        type: 'info',
        duration: 0
      })
      
      testCollectorConnection({
        host: row.host,
        port: row.port,
        collectorType: row.collectorType,
        configParams: row.configParams
      }).then(() => {
        ElMessage.success('连接测试成功')
      }).catch(error => {
        console.error('连接测试失败', error)
        ElMessage.error('连接测试失败: ' + error.message)
      }).finally(() => {
        loading.close()
      })
    }

    // 批量测试连接
    const handleBatchTest = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择要测试的采集器')
        return
      }
      
      const loading = ElMessage({
        message: '正在测试连接...',
        type: 'info',
        duration: 0
      })
      
      batchTestCollectorConnections(selectedIds.value).then(response => {
        const results = response.data
        const success = results.filter(r => r.success).length
        const failed = results.length - success
        
        ElMessage.success(`测试完成: ${success}个成功, ${failed}个失败`)
      }).catch(error => {
        console.error('批量测试连接失败', error)
        ElMessage.error('批量测试连接失败')
      }).finally(() => {
        loading.close()
      })
    }

    // 设置为主采集器
    const handleSetPrimary = (row) => {
      if (row.isMain) {
        ElMessage.warning('该采集器已经是主采集器')
        return
      }
      
      ElMessageBox.confirm(`确定要将"${row.collectorName}"设置为主采集器吗？`, '设置确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(() => {
        setPrimaryCollector(row.id).then(() => {
          ElMessage.success('设置成功')
          fetchData()
        }).catch(error => {
          console.error('设置失败', error)
          ElMessage.error('设置失败')
        })
      }).catch(() => {})
    }

    // 刷新
    const handleRefresh = () => {
      fetchData()
      fetchCollectorStatus()
      ElMessage.success('刷新成功')
    }

    onMounted(() => {
      fetchData()
      fetchCollectorStatus()
    })

    return {
      queryParams,
      collectorForm,
      rules,
      loading,
      collectorList,
      total,
      selectedIds,
      collectorStatus,
      dialog,
      getStatusType,
      getStatusText,
      fetchData,
      handleQuery,
      resetQuery,
      handleSelectionChange,
      handleSizeChange,
      handleCurrentChange,
      handleAdd,
      handleEdit,
      handleDelete,
      handleBatchDelete,
      submitForm,
      handleTest,
      handleBatchTest,
      handleSetPrimary,
      handleRefresh,
      collectorForm_ref
    }
  }
}
</script>

<style scoped>
.collector-management {
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

.success-card .stat-value {
  color: #67C23A;
}

.warning-card .stat-value {
  color: #E6A23C;
}

.danger-card .stat-value {
  color: #F56C6C;
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
</style> 