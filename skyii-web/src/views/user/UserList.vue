<template>
  <div class="user-management-container">
    <!-- 页面头部 -->
    <div class="page-header">
      <div class="header-content">
        <div class="header-left">
          <h2 class="page-title">
            <el-icon class="title-icon"><UserFilled /></el-icon>
            用户管理中心
          </h2>
          <p class="page-subtitle">用户账号管理，权限分配与安全控制</p>
        </div>
        <div class="header-actions">
          <el-button-group>
            <el-button @click="showOnlineUsers">
              <el-icon><Connection /></el-icon>
              在线用户
            </el-button>
            <el-button @click="exportUsers">
              <el-icon><Download /></el-icon>
              导出数据
            </el-button>
            <el-button @click="refreshUserList">
              <el-icon><Refresh /></el-icon>
              刷新列表
            </el-button>
          </el-button-group>
          <el-button type="primary" @click="showAddUserDialog">
            <el-icon><Plus /></el-icon>
            添加用户
          </el-button>
        </div>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <div class="statistics-panel">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card total-users" @click="filterByStatus(null)">
            <div class="stat-icon">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
              <div class="stat-label">总用户数</div>
              <div class="stat-trend">
                <el-icon class="trend-icon"><TrendCharts /></el-icon>
                <span class="trend-text">+12% 较上月</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card active-users" @click="filterByStatus(1)">
            <div class="stat-icon">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.activeUsers || 0 }}</div>
              <div class="stat-label">活跃用户</div>
              <div class="stat-trend">
                <el-icon class="trend-icon"><TrendCharts /></el-icon>
                <span class="trend-text">+8% 较上月</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card locked-users" @click="filterByStatus(2)">
            <div class="stat-icon">
              <el-icon><Lock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.lockedUsers || 0 }}</div>
              <div class="stat-label">锁定账号</div>
              <div class="stat-trend">
                <el-icon class="trend-icon"><TrendCharts /></el-icon>
                <span class="trend-text">-3% 较上月</span>
              </div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="24" :sm="12" :md="6">
          <div class="stat-card online-users" @click="showOnlineUsers">
            <div class="stat-icon">
              <el-icon><Connection /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ statistics.onlineUsers || 0 }}</div>
              <div class="stat-label">在线用户</div>
              <div class="stat-trend">
                <el-icon class="trend-icon pulse"><CircleCheckFilled /></el-icon>
                <span class="trend-text">实时数据</span>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 用户列表卡片 -->
    <el-card class="user-list-card" shadow="never">
      <!-- 卡片头部 -->
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <h3 class="card-title">
              <el-icon><List /></el-icon>
              {{ isOnlineView ? '在线用户列表' : '用户列表' }}
            </h3>
            <div class="header-info">
              <span class="user-count">共 {{ total }} 名用户</span>
              <el-divider direction="vertical" />
              <span class="selected-count" v-if="selectedIds.length > 0">
                已选中 {{ selectedIds.length }} 个用户
              </span>
            </div>
          </div>
          
          <div class="header-right">
            <el-button 
              v-if="isOnlineView" 
              type="primary" 
              link 
              @click="getList"
            >
              <el-icon><Back /></el-icon>
              返回所有用户
            </el-button>
            
            <el-button 
              v-if="selectedIds.length > 0" 
              type="danger" 
              size="small" 
              @click="handleBatchDelete"
            >
              <el-icon><Delete /></el-icon>
              批量删除
            </el-button>
          </div>
        </div>
      </template>

      <!-- 搜索和筛选区域 -->
      <div class="search-filter-section" v-if="!isOnlineView">
        <el-card shadow="never" class="filter-card">
          <el-form 
            :model="queryParams" 
            :inline="true" 
            class="search-form"
            @submit.prevent="handleQuery"
          >
            <div class="search-row">
              <el-form-item label="关键词搜索">
                <el-input
                  v-model="searchKeyword"
                  placeholder="搜索用户名、姓名或邮箱..."
                  clearable
                  class="search-input"
                  @keyup.enter="handleQuery"
                >
                  <template #prefix>
                    <el-icon><Search /></el-icon>
                  </template>
                  <template #append>
                    <el-button @click="handleQuery" type="primary">
                      搜索
                    </el-button>
                  </template>
                </el-input>
              </el-form-item>
            </div>
            
            <div class="filter-row">
              <el-form-item label="角色筛选">
                <el-select 
                  v-model="queryParams.roleId" 
                  placeholder="选择角色" 
                  clearable
                  style="width: 140px;"
                >
                  <el-option 
                    v-for="role in roles" 
                    :key="role.id" 
                    :label="role.name" 
                    :value="role.id" 
                  />
                </el-select>
              </el-form-item>
              
              <el-form-item label="账号状态">
                <el-select 
                  v-model="queryParams.status" 
                  placeholder="选择状态" 
                  clearable
                  style="width: 120px;"
                >
                  <el-option label="活跃" :value="1" />
                  <el-option label="非活跃" :value="0" />
                  <el-option label="锁定" :value="2" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="创建时间">
                <el-date-picker
                  v-model="dateRange"
                  type="daterange"
                  range-separator="至"
                  start-placeholder="开始日期"
                  end-placeholder="结束日期"
                  format="YYYY-MM-DD"
                  value-format="YYYY-MM-DD"
                  style="width: 240px;"
                />
              </el-form-item>
              
              <el-form-item>
                <el-button-group>
                  <el-button type="primary" @click="handleQuery">
                    <el-icon><Search /></el-icon>
                    查询
                  </el-button>
                  <el-button @click="resetQuery">
                    <el-icon><Refresh /></el-icon>
                    重置
                  </el-button>
                </el-button-group>
              </el-form-item>
            </div>
          </el-form>
        </el-card>
      </div>

      <!-- 用户数据表格 -->
      <div class="table-section">
        <el-table
          v-loading="loading"
          :data="userList"
          style="width: 100%"
          class="user-table"
          :row-class-name="getRowClassName"
          @selection-change="handleSelectionChange"
          @row-click="handleRowClick"
          element-loading-text="加载用户数据中..."
          element-loading-spinner="el-icon-loading"
        >
          <el-table-column type="selection" width="50" align="center" />
          
          <el-table-column label="用户信息" min-width="220" fixed="left">
            <template #default="scope">
              <div class="user-info-cell">
                <div class="user-avatar-wrapper">
                  <el-avatar :size="45" :src="scope.row.avatar">
                    {{ scope.row.name ? scope.row.name.substring(0, 1) : 'U' }}
                  </el-avatar>
                  <div class="status-indicator" :class="getStatusClass(scope.row.status)"></div>
                </div>
                <div class="user-details">
                  <div class="user-name">{{ scope.row.name || '--' }}</div>
                  <div class="username">@{{ scope.row.username }}</div>
                  <div class="user-email">{{ scope.row.email }}</div>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="角色权限" min-width="160" align="center">
            <template #default="scope">
              <div class="roles-cell">
                <el-tag
                  v-for="role in scope.row.roles"
                  :key="role.id"
                  :type="getRoleTagType(role.code)"
                  size="small"
                  class="role-tag"
                  effect="light"
                >
                  <el-icon class="role-icon">
                    <component :is="getRoleIcon(role.code)" />
                  </el-icon>
                  {{ role.name }}
                </el-tag>
                <span v-if="!scope.row.roles || scope.row.roles.length === 0" class="no-role">
                  未分配角色
                </span>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="账号状态" width="120" align="center">
            <template #default="scope">
              <div class="status-cell">
                <el-tag 
                  :type="getStatusTagType(scope.row.status)" 
                  size="small"
                  :effect="scope.row.status === 1 ? 'light' : 'plain'"
                >
                  <el-icon class="status-icon">
                    <component :is="getStatusIcon(scope.row.status)" />
                  </el-icon>
                  {{ getStatusText(scope.row.status) }}
                </el-tag>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="登录信息" min-width="160" align="center">
            <template #default="scope">
              <div class="login-info-cell">
                <div class="last-login">
                  <el-icon><Clock /></el-icon>
                  <span>{{ scope.row.lastLoginTime || '从未登录' }}</span>
                </div>
                <div class="create-time">
                  <span class="time-label">注册：</span>
                  <span>{{ scope.row.createdAt }}</span>
                </div>
              </div>
            </template>
          </el-table-column>
          
          <el-table-column label="操作" width="200" align="center" fixed="right">
            <template #default="scope">
              <div class="action-buttons">
                <el-tooltip content="重置密码" placement="top">
                  <el-button
                    type="warning"
                    size="small"
                    circle
                    @click.stop="showResetPasswordDialog(scope.row)"
                  >
                    <el-icon><Key /></el-icon>
                  </el-button>
                </el-tooltip>
                
                <el-tooltip content="编辑用户" placement="top">
                  <el-button
                    type="primary"
                    size="small"
                    circle
                    @click.stop="showEditUserDialog(scope.row)"
                  >
                    <el-icon><Edit /></el-icon>
                  </el-button>
                </el-tooltip>
                
                <el-tooltip content="用户详情" placement="top">
                  <el-button
                    type="info"
                    size="small"
                    circle
                    @click.stop="showUserDetail(scope.row)"
                  >
                    <el-icon><View /></el-icon>
                  </el-button>
                </el-tooltip>
                
                <el-popconfirm
                  title="确定要删除这个用户吗？"
                  @confirm="handleDelete(scope.row)"
                >
                  <template #reference>
                    <el-button
                      type="danger"
                      size="small"
                      circle
                      @click.stop
                    >
                      <el-icon><Delete /></el-icon>
                    </el-button>
                  </template>
                </el-popconfirm>
              </div>
            </template>
          </el-table-column>
        </el-table>

        <!-- 分页组件 -->
        <div class="pagination-section">
          <div class="pagination-info">
            <span>显示 {{ (queryParams.page - 1) * queryParams.size + 1 }} 到 
              {{ Math.min(queryParams.page * queryParams.size, total) }} 条，共 {{ total }} 条记录</span>
          </div>
          <el-pagination
            v-model:currentPage="queryParams.page"
            v-model:pageSize="queryParams.size"
            :total="total"
            :page-sizes="[10, 20, 50, 100]"
            layout="sizes, prev, pager, next, jumper"
            @size-change="handleSizeChange"
            @current-change="handleCurrentChange"
            background
          />
        </div>
      </div>
    </el-card>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      :title="dialogType === 'add' ? '添加用户' : '编辑用户'"
      v-model="dialogVisible"
      width="600px"
      append-to-body
      :close-on-click-modal="false"
      class="user-dialog"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userFormRules"
        label-width="80px"
        class="user-form"
      >
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="用户名" prop="username">
              <el-input 
                v-model="userForm.username" 
                placeholder="请输入用户名"
                :disabled="dialogType === 'edit'"
              >
                <template #prefix>
                  <el-icon><User /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20" v-if="dialogType === 'add'">
          <el-col :span="24">
            <el-form-item label="密码" prop="password">
              <el-input 
                v-model="userForm.password" 
                type="password" 
                placeholder="请输入密码"
                show-password
              >
                <template #prefix>
                  <el-icon><Lock /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="姓名" prop="name">
              <el-input v-model="userForm.name" placeholder="请输入姓名">
                <template #prefix>
                  <el-icon><UserFilled /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="userForm.email" placeholder="请输入邮箱">
                <template #prefix>
                  <el-icon><Message /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="手机号" prop="phone">
              <el-input v-model="userForm.phone" placeholder="请输入手机号">
                <template #prefix>
                  <el-icon><Phone /></el-icon>
                </template>
              </el-input>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态" prop="status">
              <el-radio-group v-model="userForm.status" class="status-radio-group">
                <el-radio :label="1">
                  <el-icon><CircleCheckFilled /></el-icon>
                  活跃
                </el-radio>
                <el-radio :label="0">
                  <el-icon><CircleCloseFilled /></el-icon>
                  非活跃
                </el-radio>
                <el-radio :label="2">
                  <el-icon><Lock /></el-icon>
                  锁定
                </el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>
        
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="角色" prop="roleIds">
              <el-select 
                v-model="userForm.roleIds" 
                multiple 
                placeholder="请选择角色"
                style="width: 100%;"
                collapse-tags
                collapse-tags-tooltip
                :max-collapse-tags="3"
              >
                <el-option
                  v-for="role in roles"
                  :key="role.id"
                  :label="role.name"
                  :value="role.id"
                >
                  <div class="role-option">
                    <el-icon><component :is="getRoleIcon(role.code)" /></el-icon>
                    <span>{{ role.name }}</span>
                    <span class="role-desc">{{ role.description }}</span>
                  </div>
                </el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="submitForm" size="large" :loading="submitLoading">
            <el-icon><Check /></el-icon>
            {{ dialogType === 'add' ? '创建用户' : '保存修改' }}
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 重置密码对话框 -->
    <el-dialog
      title="重置密码"
      v-model="resetPasswordDialogVisible"
      width="400px"
      append-to-body
      :close-on-click-modal="false"
      class="reset-password-dialog"
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
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="resetPasswordForm.confirmPassword"
            type="password"
            placeholder="请确认新密码"
            show-password
          >
            <template #prefix>
              <el-icon><Lock /></el-icon>
            </template>
          </el-input>
        </el-form-item>
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="resetPasswordDialogVisible = false">
            <el-icon><Close /></el-icon>
            取消
          </el-button>
          <el-button type="primary" @click="submitResetPassword" :loading="resetPasswordLoading">
            <el-icon><Check /></el-icon>
            确认重置
          </el-button>
        </div>
      </template>
    </el-dialog>

    <!-- 用户详情抽屉 -->
    <el-drawer
      v-model="userDetailVisible"
      title="用户详情"
      size="500px"
      class="user-detail-drawer"
    >
      <div class="user-detail-content" v-if="selectedUser">
        <div class="user-profile">
          <div class="profile-avatar">
            <el-avatar :size="80" :src="selectedUser.avatar">
              {{ selectedUser.name ? selectedUser.name.substring(0, 1) : 'U' }}
            </el-avatar>
            <div class="profile-status" :class="getStatusClass(selectedUser.status)"></div>
          </div>
          <div class="profile-info">
            <h3>{{ selectedUser.name }}</h3>
            <p>@{{ selectedUser.username }}</p>
            <el-tag :type="getStatusTagType(selectedUser.status)" size="small">
              {{ getStatusText(selectedUser.status) }}
            </el-tag>
          </div>
        </div>
        
        <el-divider />
        
        <div class="detail-sections">
          <div class="detail-section">
            <h4>基本信息</h4>
            <div class="info-list">
              <div class="info-item">
                <span class="label">邮箱地址</span>
                <span class="value">{{ selectedUser.email }}</span>
              </div>
              <div class="info-item">
                <span class="label">手机号码</span>
                <span class="value">{{ selectedUser.phone || '未设置' }}</span>
              </div>
              <div class="info-item">
                <span class="label">注册时间</span>
                <span class="value">{{ selectedUser.createdAt }}</span>
              </div>
              <div class="info-item">
                <span class="label">最后登录</span>
                <span class="value">{{ selectedUser.lastLoginTime || '从未登录' }}</span>
              </div>
            </div>
          </div>
          
          <div class="detail-section">
            <h4>角色权限</h4>
            <div class="roles-list">
              <el-tag
                v-for="role in selectedUser.roles"
                :key="role.id"
                :type="getRoleTagType(role.code)"
                class="role-tag-large"
                effect="light"
              >
                <el-icon><component :is="getRoleIcon(role.code)" /></el-icon>
                {{ role.name }}
              </el-tag>
              <span v-if="!selectedUser.roles || selectedUser.roles.length === 0">
                暂无角色权限
              </span>
            </div>
          </div>
        </div>
      </div>
    </el-drawer>
  </div>
</template>

<script>
import { ref, reactive, onMounted, computed, watch } from 'vue'
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
import {
  UserFilled, User, Connection, Download, Plus, Refresh, Lock,
  TrendCharts, CircleCheckFilled, List, Back, Delete, Search,
  Clock, Key, Edit, View, Close, Check, Message, Phone,
  CircleCloseFilled, StarFilled, Setting
} from '@element-plus/icons-vue'

export default {
  name: 'UserList',
  components: {
    UserFilled, User, Connection, Download, Plus, Refresh, Lock,
    TrendCharts, CircleCheckFilled, List, Back, Delete, Search,
    Clock, Key, Edit, View, Close, Check, Message, Phone,
    CircleCloseFilled, StarFilled, Setting
  },
  setup() {
    // 用户列表数据
    const loading = ref(false)
    const submitLoading = ref(false)
    const resetPasswordLoading = ref(false)
    const userList = ref([])
    const total = ref(0)
    const selectedIds = ref([])
    const selectedUser = ref(null)
    const dateRange = ref([])
    const isOnlineView = ref(false)
    const searchKeyword = ref('')
    const userDetailVisible = ref(false)

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

    // 重置密码对话框
    const resetPasswordDialogVisible = ref(false)
    const resetPasswordFormRef = ref(null)
    const resetPasswordForm = reactive({
      id: null,
      newPassword: '',
      confirmPassword: ''
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
        { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' }
      ],
      status: [
        { required: true, message: '请选择状态', trigger: 'change' }
      ],
      roleIds: [
        { required: true, message: '请选择至少一个角色', trigger: 'change' }
      ]
    }

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

    // 监听搜索关键词变化
    watch(searchKeyword, (newVal) => {
      // 将关键词分配到相应的查询字段
      queryParams.username = newVal
      queryParams.name = newVal
      queryParams.email = newVal
    })

    // 获取用户列表
    const getList = async () => {
      loading.value = true
      isOnlineView.value = false
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
          page: queryParams.page - 1
        })
        userList.value = res.data
        total.value = res.meta?.total || 0
      } catch (error) {
        console.error('获取用户列表失败', error)
        ElMessage.error('获取用户列表失败')
      } finally {
        loading.value = false
      }
    }

    // 刷新用户列表
    const refreshUserList = () => {
      ElMessage.success('正在刷新用户列表...')
      getList()
      getStatistics()
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
      searchKeyword.value = ''
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

    // 按状态筛选
    const filterByStatus = (status) => {
      queryParams.status = status
      queryParams.page = 1
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

    // 处理行点击
    const handleRowClick = (row) => {
      selectedUser.value = row
    }

    // 显示用户详情
    const showUserDetail = (row) => {
      selectedUser.value = row
      userDetailVisible.value = true
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
          submitLoading.value = true
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
            ElMessage.error('操作失败，请重试')
          } finally {
            submitLoading.value = false
          }
        }
      })
    }

    // 提交重置密码
    const submitResetPassword = async () => {
      if (!resetPasswordFormRef.value) return
      
      await resetPasswordFormRef.value.validate(async (valid) => {
        if (valid) {
          resetPasswordLoading.value = true
          try {
            await resetPassword(resetPasswordForm.id, resetPasswordForm.newPassword)
            ElMessage.success('重置密码成功')
            resetPasswordDialogVisible.value = false
          } catch (error) {
            console.error('重置密码失败', error)
            ElMessage.error('重置密码失败，请重试')
          } finally {
            resetPasswordLoading.value = false
          }
        }
      })
    }

    // 处理删除
    const handleDelete = async (row) => {
      try {
        await deleteUser(row.id)
        ElMessage.success('删除用户成功')
        getList()
        getStatistics()
      } catch (error) {
        console.error('删除用户失败', error)
        ElMessage.error('删除用户失败')
      }
    }

    // 批量删除
    const handleBatchDelete = () => {
      if (selectedIds.value.length === 0) {
        ElMessage.warning('请先选择要删除的用户')
        return
      }
      
      ElMessageBox.confirm(
        `确定要删除选中的 ${selectedIds.value.length} 个用户吗？此操作不可恢复。`,
        '批量删除确认',
        {
          confirmButtonText: '确定删除',
          cancelButtonText: '取消',
          type: 'warning',
          dangerouslyUseHTMLString: true
        }
      ).then(async () => {
        try {
          await batchDeleteUsers(selectedIds.value)
          ElMessage.success('批量删除成功')
          selectedIds.value = []
          getList()
          getStatistics()
        } catch (error) {
          console.error('批量删除失败', error)
          ElMessage.error('批量删除失败')
        }
      }).catch(() => {})
    }

    // 导出用户数据
    const exportUsers = () => {
      ElMessageBox.confirm('确定要导出当前筛选条件下的用户数据吗？', '导出确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'info'
      }).then(async () => {
        try {
          let params = { ...queryParams }
          if (dateRange.value && dateRange.value.length === 2) {
            params.startDate = dateRange.value[0]
            params.endDate = dateRange.value[1]
          }
          
          const queryString = new URLSearchParams(params).toString()
          const baseUrl = process.env.VUE_APP_BASE_API || ''
          const exportUrl = `${baseUrl}/api/user/export?${queryString}`
          window.open(exportUrl, '_blank')
          
          ElMessage.success('导出请求已发送，请等待下载')
        } catch (error) {
          console.error('导出用户数据失败', error)
          ElMessage.error('导出失败')
        }
      }).catch(() => {})
    }

    // 查看在线用户
    const showOnlineUsers = async () => {
      loading.value = true
      try {
        const res = await getOnlineUsers()
        userList.value = res.data
        total.value = res.data.length
        isOnlineView.value = true
        ElMessage.info(`当前共有 ${res.data.length} 名在线用户`)
      } catch (error) {
        console.error('获取在线用户失败', error)
        ElMessage.error('获取在线用户失败')
      } finally {
        loading.value = false
      }
    }

    // 获取角色图标
    const getRoleIcon = (roleCode) => {
      const iconMap = {
        'ADMIN': StarFilled,
        'SECURITY': Lock,
        'OPERATOR': Setting,
        'USER': User
      }
      return iconMap[roleCode] || User
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

    // 获取状态图标
    const getStatusIcon = (status) => {
      const iconMap = {
        1: CircleCheckFilled,
        0: CircleCloseFilled,
        2: Lock
      }
      return iconMap[status] || CircleCloseFilled
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

    // 获取状态样式类
    const getStatusClass = (status) => {
      const classMap = {
        1: 'status-active',
        0: 'status-inactive',
        2: 'status-locked'
      }
      return classMap[status] || ''
    }

    // 获取表格行样式类名
    const getRowClassName = ({ row }) => {
      if (row.status === 2) return 'row-locked'
      if (row.status === 0) return 'row-inactive'
      return ''
    }

    // 初始化
    onMounted(() => {
      getList()
      getRoleList()
      getStatistics()
    })

    return {
      loading,
      submitLoading,
      resetPasswordLoading,
      userList,
      total,
      selectedIds,
      selectedUser,
      queryParams,
      dateRange,
      searchKeyword,
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
      userDetailVisible,
      getList,
      refreshUserList,
      handleQuery,
      resetQuery,
      filterByStatus,
      handleCurrentChange,
      handleSizeChange,
      handleSelectionChange,
      handleRowClick,
      showUserDetail,
      showAddUserDialog,
      showEditUserDialog,
      showResetPasswordDialog,
      submitForm,
      submitResetPassword,
      handleDelete,
      handleBatchDelete,
      exportUsers,
      showOnlineUsers,
      getRoleIcon,
      getRoleTagType,
      getStatusIcon,
      getStatusTagType,
      getStatusText,
      getStatusClass,
      getRowClassName
    }
  }
}
</script>

<style lang="scss" scoped>
.user-management-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

// 页面头部样式
.page-header {
  margin-bottom: 24px;
  
  .header-content {
    display: flex;
    justify-content: space-between;
    align-items: flex-end;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 16px;
    padding: 24px 32px;
    color: white;
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.15);
    
    .header-left {
      .page-title {
        display: flex;
        align-items: center;
        gap: 12px;
        font-size: 28px;
        font-weight: 700;
        margin: 0 0 8px 0;
        
        .title-icon {
          color: #ffd700;
          filter: drop-shadow(0 2px 4px rgba(0, 0, 0, 0.3));
        }
      }
      
      .page-subtitle {
        margin: 0;
        font-size: 16px;
        opacity: 0.9;
      }
    }
    
    .header-actions {
      display: flex;
      gap: 12px;
      
      .el-button {
        border-color: rgba(255, 255, 255, 0.3);
        
        &:hover {
          background-color: rgba(255, 255, 255, 0.1);
          border-color: rgba(255, 255, 255, 0.5);
        }
      }
    }
  }
}

// 统计面板样式
.statistics-panel {
  margin-bottom: 24px;
  
  .stat-card {
    background: white;
    border-radius: 12px;
    padding: 20px;
    box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
    transition: all 0.3s;
    cursor: pointer;
    border: 2px solid transparent;
    position: relative;
    overflow: hidden;
    
    &:hover {
      transform: translateY(-4px);
      box-shadow: 0 8px 25px rgba(0, 0, 0, 0.1);
    }
    
    &.total-users {
      border-left: 4px solid #409eff;
      
      .stat-icon {
        background: linear-gradient(135deg, #409eff, #66b1ff);
        color: white;
      }
    }
    
    &.active-users {
      border-left: 4px solid #67c23a;
      
      .stat-icon {
        background: linear-gradient(135deg, #67c23a, #85ce61);
        color: white;
      }
    }
    
    &.locked-users {
      border-left: 4px solid #f56c6c;
      
      .stat-icon {
        background: linear-gradient(135deg, #f56c6c, #f78989);
        color: white;
      }
    }
    
    &.online-users {
      border-left: 4px solid #e6a23c;
      
      .stat-icon {
        background: linear-gradient(135deg, #e6a23c, #ebb563);
        color: white;
      }
    }
    
    .stat-icon {
      width: 60px;
      height: 60px;
      border-radius: 12px;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: 28px;
      margin-bottom: 16px;
    }
    
    .stat-content {
      .stat-value {
        font-size: 32px;
        font-weight: 700;
        color: #2c3e50;
        margin-bottom: 4px;
      }
      
      .stat-label {
        font-size: 14px;
        color: #7f8c8d;
        margin-bottom: 8px;
      }
      
      .stat-trend {
        display: flex;
        align-items: center;
        gap: 4px;
        font-size: 12px;
        color: #95a5a6;
        
        .trend-icon {
          &.pulse {
            animation: pulse 2s infinite;
          }
        }
      }
    }
  }
}

// 用户列表卡片样式
.user-list-card {
  border-radius: 12px;
  border: none;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
  
  .card-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    
    .header-left {
      display: flex;
      align-items: center;
      gap: 16px;
      
      .card-title {
        display: flex;
        align-items: center;
        gap: 8px;
        font-size: 18px;
        font-weight: 600;
        color: #2c3e50;
        margin: 0;
      }
      
      .header-info {
        display: flex;
        align-items: center;
        font-size: 14px;
        color: #7f8c8d;
        
        .selected-count {
          color: #409eff;
          font-weight: 500;
        }
      }
    }
  }
}

// 搜索筛选区域样式
.search-filter-section {
  margin-bottom: 20px;
  
  .filter-card {
    border-radius: 8px;
    border: 1px solid #e4e7ed;
    
    .search-form {
      .search-row {
        margin-bottom: 16px;
        
        .search-input {
          width: 400px;
        }
      }
      
      .filter-row {
        display: flex;
        align-items: center;
        gap: 16px;
        flex-wrap: wrap;
      }
    }
  }
}

// 表格样式
.table-section {
  .user-table {
    border-radius: 8px;
    overflow: hidden;
    
    :deep(.el-table__header) {
      th {
        background-color: #fafbfc;
        color: #606266;
        font-weight: 600;
      }
    }
    
    :deep(.el-table__row) {
      &.row-locked {
        background-color: #fef0f0;
      }
      
      &.row-inactive {
        background-color: #f5f7fa;
      }
      
      &:hover {
        background-color: #e6f7ff !important;
      }
    }
  }
  
  .user-info-cell {
    display: flex;
    align-items: center;
    gap: 12px;
    
    .user-avatar-wrapper {
      position: relative;
      
      .status-indicator {
        position: absolute;
        bottom: 2px;
        right: 2px;
        width: 12px;
        height: 12px;
        border-radius: 50%;
        border: 2px solid white;
        
        &.status-active {
          background-color: #67c23a;
        }
        
        &.status-inactive {
          background-color: #909399;
        }
        
        &.status-locked {
          background-color: #f56c6c;
        }
      }
    }
    
    .user-details {
      .user-name {
        font-weight: 600;
        color: #2c3e50;
        margin-bottom: 2px;
      }
      
      .username {
        font-size: 12px;
        color: #7f8c8d;
        margin-bottom: 2px;
      }
      
      .user-email {
        font-size: 12px;
        color: #95a5a6;
      }
    }
  }
  
  .roles-cell {
    .role-tag {
      margin: 2px;
      
      .role-icon {
        margin-right: 4px;
      }
    }
    
    .no-role {
      color: #c0c4cc;
      font-style: italic;
    }
  }
  
  .status-cell {
    .status-icon {
      margin-right: 4px;
    }
  }
  
  .login-info-cell {
    .last-login {
      display: flex;
      align-items: center;
      gap: 4px;
      font-size: 13px;
      color: #606266;
      margin-bottom: 4px;
    }
    
    .create-time {
      font-size: 12px;
      color: #909399;
      
      .time-label {
        color: #c0c4cc;
      }
    }
  }
  
  .action-buttons {
    display: flex;
    gap: 8px;
    justify-content: center;
  }
}

// 分页样式
.pagination-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 20px;
  
  .pagination-info {
    font-size: 14px;
    color: #7f8c8d;
  }
}

// 对话框样式
.user-dialog {
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    padding: 16px 20px;
    border-radius: 6px 6px 0 0;
  }
  
  .user-form {
    .status-radio-group {
      display: flex;
      gap: 16px;
      
      .el-radio {
        margin-right: 0;
        
        :deep(.el-radio__label) {
          display: flex;
          align-items: center;
          gap: 4px;
        }
      }
    }
    
    .role-option {
      display: flex;
      align-items: center;
      gap: 8px;
      
      .role-desc {
        margin-left: auto;
        font-size: 12px;
        color: #909399;
      }
    }
  }
}

.reset-password-dialog {
  :deep(.el-dialog__header) {
    background: linear-gradient(135deg, #e6a23c 0%, #f39c12 100%);
    color: white;
    padding: 16px 20px;
    border-radius: 6px 6px 0 0;
  }
}

// 用户详情抽屉样式
.user-detail-drawer {
  .user-detail-content {
    .user-profile {
      text-align: center;
      padding: 20px 0;
      
      .profile-avatar {
        position: relative;
        display: inline-block;
        margin-bottom: 16px;
        
        .profile-status {
          position: absolute;
          bottom: 5px;
          right: 5px;
          width: 16px;
          height: 16px;
          border-radius: 50%;
          border: 3px solid white;
          
          &.status-active {
            background-color: #67c23a;
          }
          
          &.status-inactive {
            background-color: #909399;
          }
          
          &.status-locked {
            background-color: #f56c6c;
          }
        }
      }
      
      .profile-info {
        h3 {
          margin: 0 0 8px 0;
          color: #2c3e50;
        }
        
        p {
          margin: 0 0 12px 0;
          color: #7f8c8d;
        }
      }
    }
    
    .detail-sections {
      .detail-section {
        margin-bottom: 24px;
        
        h4 {
          color: #606266;
          font-size: 16px;
          margin-bottom: 12px;
          padding-bottom: 8px;
          border-bottom: 1px solid #e4e7ed;
        }
        
        .info-list {
          .info-item {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 8px 0;
            border-bottom: 1px solid #f5f7fa;
            
            .label {
              color: #909399;
              font-size: 14px;
            }
            
            .value {
              color: #2c3e50;
              font-weight: 500;
            }
          }
        }
        
        .roles-list {
          .role-tag-large {
            margin: 4px 8px 4px 0;
            padding: 8px 12px;
            
            .el-icon {
              margin-right: 6px;
            }
          }
        }
      }
    }
  }
}

// 动画效果
@keyframes pulse {
  0% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
  100% {
    opacity: 1;
  }
}

// 响应式设计
@media (max-width: 1200px) {
  .search-filter-section {
    .search-form {
      .search-row .search-input {
        width: 100%;
      }
      
      .filter-row {
        flex-direction: column;
        align-items: stretch;
        gap: 12px;
        
        .el-form-item {
          margin-bottom: 0;
        }
      }
    }
  }
}

@media (max-width: 768px) {
  .user-management-container {
    padding: 12px;
  }
  
  .page-header .header-content {
    flex-direction: column;
    gap: 16px;
    align-items: center;
    text-align: center;
  }
  
  .statistics-panel {
    .el-col {
      margin-bottom: 16px;
    }
  }
  
  .card-header {
    flex-direction: column;
    gap: 12px;
    align-items: stretch;
    
    .header-left {
      flex-direction: column;
      gap: 8px;
    }
  }
  
  .table-section {
    .user-table {
      :deep(.el-table__fixed-right) {
        display: none;
      }
    }
  }
  
  .pagination-section {
    flex-direction: column;
    gap: 12px;
    
    .pagination-info {
      order: 2;
    }
  }
}
</style> 