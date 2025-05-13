<template>
  <div class="user-list-container">
    <div class="d-flex justify-content-between align-items-center mb-4">
      <h4>用户管理</h4>
      <div>
        <el-button type="info" size="small" plain @click="showOnlineUsers">
          <el-icon><Connection /></el-icon>在线用户
        </el-button>
        <el-button type="info" size="small" plain @click="exportUsers">
          <el-icon><Download /></el-icon>导出
        </el-button>
        <el-button type="primary" size="small" @click="showAddUserDialog">
          <el-icon><Plus /></el-icon>添加用户
        </el-button>
      </div>
    </div>
    
    <!-- 用户账号概览卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="statistics-card">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted mb-1">总用户数</div>
              <div class="h3 mb-0">{{ statistics.totalUsers || 0 }}</div>
            </div>
            <el-icon class="text-primary" :size="40"><User /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="statistics-card">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted mb-1">活跃用户</div>
              <div class="h3 mb-0">{{ statistics.activeUsers || 0 }}</div>
            </div>
            <el-icon class="text-success" :size="40"><UserFilled /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="statistics-card">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted mb-1">锁定账号</div>
              <div class="h3 mb-0">{{ statistics.lockedUsers || 0 }}</div>
            </div>
            <el-icon class="text-danger" :size="40"><Lock /></el-icon>
          </div>
        </el-card>
      </el-col>
      <el-col :xs="24" :sm="12" :md="6">
        <el-card shadow="hover" class="statistics-card">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <div class="text-muted mb-1">在线用户</div>
              <div class="h3 mb-0">{{ statistics.onlineUsers || 0 }}</div>
            </div>
            <el-icon class="text-info" :size="40"><Connection /></el-icon>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 用户列表 -->
    <el-card>
      <template #header>
        <div class="d-flex justify-content-between align-items-center">
          <span>用户列表</span>
          <el-button v-if="isOnlineView" type="primary" link size="small" @click="getList">
            <el-icon><Back /></el-icon>返回所有用户
          </el-button>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :model="queryParams" label-width="80px" size="small" @submit.prevent="handleQuery" v-if="!isOnlineView">
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="用户名">
              <el-input v-model="queryParams.username" placeholder="请输入用户名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="姓名">
              <el-input v-model="queryParams.name" placeholder="请输入姓名" clearable />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="角色">
              <el-select v-model="queryParams.roleId" placeholder="所有角色" clearable>
                <el-option v-for="role in roles" :key="role.id" :label="role.name" :value="role.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="6">
            <el-form-item label="状态">
              <el-select v-model="queryParams.status" placeholder="所有状态" clearable>
                <el-option label="活跃" :value="1" />
                <el-option label="非活跃" :value="0" />
                <el-option label="锁定" :value="2" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :xs="24" :sm="12" :md="8">
            <el-form-item label="创建日期">
              <el-date-picker
                v-model="dateRange"
                type="daterange"
                range-separator="至"
                start-placeholder="开始日期"
                end-placeholder="结束日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :xs="24" :sm="12" :md="16">
            <div class="d-flex justify-content-end">
              <el-button type="primary" @click="handleQuery">
                <el-icon><Search /></el-icon>查询
              </el-button>
              <el-button @click="resetQuery">
                <el-icon><Refresh /></el-icon>重置
              </el-button>
            </div>
          </el-col>
        </el-row>
      </el-form>

      <el-table
        v-loading="loading"
        :data="userList"
        style="width: 100%"
        border
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" align="center" />
        <el-table-column label="头像" width="60" align="center">
          <template #default="scope">
            <el-avatar :size="40" :src="scope.row.avatar">
              {{ scope.row.name ? scope.row.name.substring(0, 1) : 'U' }}
            </el-avatar>
          </template>
        </el-table-column>
        <el-table-column prop="username" label="用户名" min-width="100" />
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="email" label="邮箱" min-width="180" show-overflow-tooltip />
        <el-table-column label="角色" min-width="120">
          <template #default="scope">
            <el-tag
              v-for="role in scope.row.roles"
              :key="role.id"
              :type="getRoleTagType(role.code)"
              size="small"
              class="me-1"
            >
              {{ role.name }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="scope">
            <el-tag :type="getStatusTagType(scope.row.status)" size="small">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="lastLoginTime" label="最后登录" width="160" show-overflow-tooltip />
        <el-table-column prop="createdAt" label="创建日期" width="160" show-overflow-tooltip />
        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              link
              size="small"
              @click="showResetPasswordDialog(scope.row)"
              title="重置密码"
            >
              <el-icon><Key /></el-icon>
            </el-button>
            <el-button
              type="primary"
              link
              size="small"
              @click="showEditUserDialog(scope.row)"
              title="编辑"
            >
              <el-icon><Edit /></el-icon>
            </el-button>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(scope.row)"
              title="删除"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="d-flex justify-content-between mt-3">
        <div>
          <el-button
            type="danger"
            size="small"
            :disabled="selectedIds.length === 0"
            @click="handleBatchDelete"
          >
            <el-icon><Delete /></el-icon>批量删除
          </el-button>
        </div>
        <el-pagination
          v-model:currentPage="queryParams.page"
          v-model:pageSize="queryParams.size"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
          small
        />
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      v-model="dialogVisible"
      width="600px"
      append-to-body
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input v-model="userForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input v-model="userForm.password" type="password" placeholder="请输入密码" />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="userForm.phone" placeholder="请输入手机号" />
        </el-form-item>
        <el-form-item label="角色" prop="roleIds">
          <el-select v-model="userForm.roleIds" multiple placeholder="请选择角色">
            <el-option
              v-for="role in roles"
              :key="role.id"
              :label="role.name"
              :value="role.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">活跃</el-radio>
            <el-radio :label="0">非活跃</el-radio>
            <el-radio :label="2">锁定</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitForm">确定</el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPasswordDialogVisible"
      width="400px"
      append-to-body
    >
      <el-form
        ref="resetPasswordFormRef"
        :model="resetPasswordForm"
        :rules="resetPasswordFormRules"
        label-width="80px"
      >
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="resetPasswordForm.newPassword"
            type="password"
            placeholder="请输入新密码"
            show-password
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitResetPassword">确定</el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  getUserList,
  getUserDetail,
  createUser,
  updateUser,
  deleteUser,
  batchDeleteUsers,
  resetPassword,
  getAllRoles,
  getUserStatistics,
  getOnlineUsers,
  exportUserData
} from '@/api/user'

export default {
  name: 'UserList',
  setup() {
    // 用户列表数据
    const loading = ref(false)
    const userList = ref([])
    const total = ref(0)
    const selectedIds = ref([])
    const dateRange = ref([])
    const isOnlineView = ref(false) // 是否在查看在线用户

    // 查询参数
    const queryParams = reactive({
      page: 1,
      size: 10,
      username: '',
      name: '',
      email: '',
      status: null,
      roleId: null,
      startDate: null,
      endDate: null,
      sortBy: 'createdAt',
      sortOrder: 'desc'
    })

    // 角色列表
    const roles = ref([])

    // 统计信息
    const statistics = ref({
      totalUsers: 0,
      activeUsers: 0,
      lockedUsers: 0,
      onlineUsers: 0
    })

    // 对话框相关
    const dialogVisible = ref(false)
    const dialogType = ref('add')
    const userFormRef = ref(null)
    const userForm = reactive({
      id: null,
      username: '',
      password: '',
      name: '',
      email: '',
      phone: '',
      avatar: '',
      status: 1,
      roleIds: []
    })

    // 表单校验规则
    const userFormRules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' },
        { pattern: /^[a-zA-Z0-9_]+$/, message: '只能包含字母、数字和下划线', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      name: [
        { required: true, message: '请输入姓名', trigger: 'blur' },
        { max: 50, message: '长度不能超过 50 个字符', trigger: 'blur' }
      ],
      email: [
        { required: true, message: '请输入邮箱', trigger: 'blur' },
        { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
      ],
      phone: [
        { pattern: /^1[3456789]\d{9}$/, message: '请输入正确的手机号码', trigger: 'blur' }
      ],
      roleIds: [
        { type: 'array', required: true, message: '请选择角色', trigger: 'change' }
      ]
    }

    // 重置密码相关
    const resetPasswordDialogVisible = ref(false)
    const resetPasswordFormRef = ref(null)
    const resetPasswordForm = reactive({
      id: null,
      newPassword: '',
      confirmPassword: ''
    })

    // 重置密码表单校验规则
    const resetPasswordFormRules = {
      newPassword: [
        { required: true, message: '请输入新密码', trigger: 'blur' },
        { min: 6, max: 20, message: '长度在 6 到 20 个字符', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, message: '请确认新密码', trigger: 'blur' },
        {
          validator: (rule, value, callback) => {
            if (value !== resetPasswordForm.newPassword) {
              callback(new Error('两次输入的密码不一致'))
            } else {
              callback()
            }
          },
          trigger: 'blur'
        }
      ]
    }

    // 获取用户列表
    const getList = async () => {
      loading.value = true
      isOnlineView.value = false // 重置在线用户视图状态
      try {
        // 处理日期范围
        if (dateRange.value && dateRange.value.length === 2) {
          queryParams.startDate = dateRange.value[0]
          queryParams.endDate = dateRange.value[1]
        } else {
          queryParams.startDate = null
          queryParams.endDate = null
        }

        const res = await getUserList({
          ...queryParams,
          page: queryParams.page - 1 // 后端从0开始，前端从1开始
        })
        userList.value = res.data
        total.value = res.meta?.total || 0
      } catch (error) {
        console.error('获取用户列表失败', error)
      } finally {
        loading.value = false
      }
    }

    // 获取角色列表
    const getRoleList = async () => {
      try {
        const res = await getAllRoles()
        roles.value = res.data
      } catch (error) {
        console.error('获取角色列表失败', error)
      }
    }

    // 获取用户统计信息
    const getStatistics = async () => {
      try {
        const res = await getUserStatistics()
        statistics.value = res.data
      } catch (error) {
        console.error('获取用户统计信息失败', error)
      }
    }

    // 处理查询
    const handleQuery = () => {
      queryParams.page = 1
      getList()
    }

    // 重置查询
    const resetQuery = () => {
      dateRange.value = []
      Object.assign(queryParams, {
        page: 1,
        username: '',
        name: '',
        email: '',
        status: null,
        roleId: null,
        startDate: null,
        endDate: null
      })
      getList()
    }

    // 处理页码变化
    const handleCurrentChange = (page) => {
      queryParams.page = page
      getList()
    }

    // 处理每页条数变化
    const handleSizeChange = (size) => {
      queryParams.size = size
      queryParams.page = 1
      getList()
    }

    // 多选框选中数据
    const handleSelectionChange = (selection) => {
      selectedIds.value = selection.map(item => item.id)
    }

    // 显示添加用户对话框
    const showAddUserDialog = () => {
      dialogType.value = 'add'
      resetForm()
      dialogVisible.value = true
    }

    // 显示编辑用户对话框
    const showEditUserDialog = async (row) => {
      dialogType.value = 'edit'
      resetForm()
      
      try {
        const res = await getUserDetail(row.id)
        const userData = res.data
        
        userForm.id = userData.id
        userForm.username = userData.username
        userForm.name = userData.name
        userForm.email = userData.email
        userForm.phone = userData.phone
        userForm.avatar = userData.avatar
        userForm.status = userData.status
        userForm.roleIds = userData.roles.map(role => role.id)
        
        dialogVisible.value = true
      } catch (error) {
        console.error('获取用户详情失败', error)
        ElMessage.error('获取用户详情失败')
      }
    }

    // 显示重置密码对话框
    const showResetPasswordDialog = (row) => {
      resetPasswordForm.id = row.id
      resetPasswordForm.newPassword = ''
      resetPasswordForm.confirmPassword = ''
      resetPasswordDialogVisible.value = true
    }

    // 重置表单
    const resetForm = () => {
      userForm.id = null
      userForm.username = ''
      userForm.password = ''
      userForm.name = ''
      userForm.email = ''
      userForm.phone = ''
      userForm.avatar = ''
      userForm.status = 1
      userForm.roleIds = []
      
      if (userFormRef.value) {
        userFormRef.value.resetFields()
      }
    }

    // 提交表单
    const submitForm = async () => {
      if (!userFormRef.value) return
      
      await userFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            if (dialogType.value === 'add') {
              await createUser(userForm)
              ElMessage.success('添加用户成功')
            } else {
              await updateUser(userForm.id, userForm)
              ElMessage.success('更新用户成功')
            }
            
            dialogVisible.value = false
            getList()
            getStatistics()
          } catch (error) {
            console.error('提交用户表单失败', error)
          }
        }
      })
    }

    // 提交重置密码
    const submitResetPassword = async () => {
      if (!resetPasswordFormRef.value) return
      
      await resetPasswordFormRef.value.validate(async (valid) => {
        if (valid) {
          try {
            await resetPassword(resetPasswordForm.id, resetPasswordForm.newPassword)
            ElMessage.success('重置密码成功')
            resetPasswordDialogVisible.value = false
          } catch (error) {
            console.error('重置密码失败', error)
          }
        }
      })
    }

    // 处理删除
    const handleDelete = (row) => {
      ElMessageBox.confirm(`确定要删除用户 ${row.username} 吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await deleteUser(row.id)
          ElMessage.success('删除成功')
          getList()
          getStatistics()
        } catch (error) {
          console.error('删除用户失败', error)
        }
      }).catch(() => {})
    }

    // 处理批量删除
    const handleBatchDelete = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请选择要删除的用户')
        return
      }
      
      ElMessageBox.confirm(`确定要删除选中的 ${selectedIds.value.length} 个用户吗？`, '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }).then(async () => {
        try {
          await batchDeleteUsers(selectedIds.value)
          ElMessage.success('批量删除成功')
          getList()
          getStatistics()
        } catch (error) {
          console.error('批量删除用户失败', error)
        }
      }).catch(() => {})
    }

    // 导出用户
    const exportUsers = () => {
      ElMessageBox.confirm('确定要导出当前筛选条件下的用户数据吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          // 处理日期范围
          let params = { ...queryParams };
          if (dateRange.value && dateRange.value.length === 2) {
            params.startDate = dateRange.value[0];
            params.endDate = dateRange.value[1];
          }
          
          // 使用window.open直接打开导出链接
          const queryString = new URLSearchParams(params).toString();
          const baseUrl = process.env.VUE_APP_BASE_API || '';
          const exportUrl = `${baseUrl}/api/user/export?${queryString}`;
          window.open(exportUrl, '_blank');
          
          ElMessage.success('导出请求已发送，请等待下载');
        } catch (error) {
          console.error('导出用户数据失败', error);
          ElMessage.error('导出失败');
        }
      }).catch(() => {});
    }

    // 查看在线用户
    const showOnlineUsers = async () => {
      loading.value = true;
      try {
        const res = await getOnlineUsers();
        userList.value = res.data;
        total.value = res.data.length;
        isOnlineView.value = true; // 设置为在线用户视图
        ElMessage.info(`当前共有 ${res.data.length} 名在线用户`);
      } catch (error) {
        console.error('获取在线用户失败', error);
        ElMessage.error('获取在线用户失败');
      } finally {
        loading.value = false;
      }
    }

    // 获取角色标签类型
    const getRoleTagType = (roleCode) => {
      const typeMap = {
        'ADMIN': 'danger',
        'SECURITY': 'warning',
        'OPERATOR': 'success',
        'USER': 'info'
      }
      return typeMap[roleCode] || ''
    }

    // 获取状态标签类型
    const getStatusTagType = (status) => {
      const typeMap = {
        1: 'success',
        0: 'info',
        2: 'danger'
      }
      return typeMap[status] || ''
    }

    // 获取状态文本
    const getStatusText = (status) => {
      const textMap = {
        1: '活跃',
        0: '非活跃',
        2: '锁定'
      }
      return textMap[status] || '未知'
    }

    // 初始化
    onMounted(() => {
      getList()
      getRoleList()
      getStatistics()
    })

    return {
      loading,
      userList,
      total,
      selectedIds,
      queryParams,
      dateRange,
      roles,
      statistics,
      dialogVisible,
      dialogType,
      userFormRef,
      userForm,
      userFormRules,
      resetPasswordDialogVisible,
      resetPasswordFormRef,
      resetPasswordForm,
      resetPasswordFormRules,
      isOnlineView,
      getList,
      handleQuery,
      resetQuery,
      handleCurrentChange,
      handleSizeChange,
      handleSelectionChange,
      showAddUserDialog,
      showEditUserDialog,
      showResetPasswordDialog,
      submitForm,
      submitResetPassword,
      handleDelete,
      handleBatchDelete,
      exportUsers,
      showOnlineUsers,
      getRoleTagType,
      getStatusTagType,
      getStatusText
    }
  }
}
</script>

<style scoped>
.user-list-container {
  padding: 20px;
}

.statistics-card {
  height: 100%;
}

.el-avatar {
  background-color: #409eff;
  color: #fff;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
}
</style> 