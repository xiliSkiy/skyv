<template>
  <div class="alert-list-container">
    <el-card class="box-card">
      <template #header>
        <div class="card-header">
          <span>报警事件列表</span>
          <el-button type="primary" size="small">刷新</el-button>
        </div>
      </template>
      
      <el-table :data="alertList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="deviceName" label="设备名称" width="150" />
        <el-table-column prop="alertType" label="报警类型" width="120">
          <template #default="scope">
            <el-tag :type="getAlertTypeTag(scope.row.alertType)">
              {{ getAlertTypeText(scope.row.alertType) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertLevel" label="报警级别" width="100">
          <template #default="scope">
            <el-tag :type="getAlertLevelTag(scope.row.alertLevel)">
              {{ getAlertLevelText(scope.row.alertLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="alertTime" label="报警时间" width="180" />
        <el-table-column prop="content" label="报警内容" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="scope">
            <el-tag :type="scope.row.status === 0 ? 'danger' : 'success'">
              {{ scope.row.status === 0 ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 0" 
              type="primary" 
              size="small"
              @click="handleAlert(scope.row)"
            >处理</el-button>
            <el-button 
              type="info" 
              size="small"
              @click="viewAlertDetail(scope.row)"
            >详情</el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <div class="pagination-container">
        <el-pagination
          background
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          :page-size="pageSize"
          :current-page="currentPage"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script>
export default {
  name: 'AlertList',
  data() {
    return {
      loading: false,
      alertList: [],
      total: 0,
      pageSize: 10,
      currentPage: 1
    }
  },
  created() {
    this.fetchAlertList()
  },
  methods: {
    fetchAlertList() {
      this.loading = true
      
      // 模拟数据，实际项目中应该调用API
      setTimeout(() => {
        this.alertList = [
          {
            id: 1,
            deviceName: '前门摄像头',
            alertType: 'motion',
            alertLevel: 'high',
            alertTime: '2025-05-12 10:23:45',
            content: '检测到可疑人员活动',
            status: 0
          },
          {
            id: 2,
            deviceName: '后门摄像头',
            alertType: 'offline',
            alertLevel: 'medium',
            alertTime: '2025-05-12 09:15:30',
            content: '设备离线',
            status: 1
          },
          {
            id: 3,
            deviceName: '仓库摄像头',
            alertType: 'intrusion',
            alertLevel: 'critical',
            alertTime: '2025-05-12 02:45:12',
            content: '检测到入侵行为',
            status: 0
          }
        ]
        this.total = 3
        this.loading = false
      }, 500)
    },
    handleAlert(row) {
      this.$message.success(`已处理ID为${row.id}的报警`)
      row.status = 1
    },
    viewAlertDetail(row) {
      this.$message.info(`查看ID为${row.id}的报警详情`)
    },
    handleSizeChange(size) {
      this.pageSize = size
      this.fetchAlertList()
    },
    handleCurrentChange(page) {
      this.currentPage = page
      this.fetchAlertList()
    },
    getAlertTypeTag(type) {
      const map = {
        motion: 'warning',
        offline: 'info',
        intrusion: 'danger'
      }
      return map[type] || 'info'
    },
    getAlertTypeText(type) {
      const map = {
        motion: '移动侦测',
        offline: '设备离线',
        intrusion: '入侵检测'
      }
      return map[type] || '未知类型'
    },
    getAlertLevelTag(level) {
      const map = {
        low: 'info',
        medium: 'warning',
        high: 'danger',
        critical: 'danger'
      }
      return map[level] || 'info'
    },
    getAlertLevelText(level) {
      const map = {
        low: '低',
        medium: '中',
        high: '高',
        critical: '紧急'
      }
      return map[level] || '未知'
    }
  }
}
</script>

<style scoped>
.alert-list-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  text-align: right;
}
</style> 