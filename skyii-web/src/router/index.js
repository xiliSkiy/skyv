import { createRouter, createWebHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'

// 路由配置
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/test-api',
    name: 'TestApi',
    component: () => import('@/views/auth/TestApi.vue'),
    meta: { title: 'API测试', requiresAuth: false }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/auth/ForgotPassword.vue'),
    meta: { title: '忘记密码', requiresAuth: false }
  },
  {
    path: '/reset-password',
    name: 'ResetPassword',
    component: () => import('@/views/auth/ResetPassword.vue'),
    meta: { title: '重置密码', requiresAuth: false }
  },
  {
    path: '/',
    component: () => import('@/views/layout/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/Dashboard.vue'),
        meta: { title: '控制台', icon: 'Odometer', requiresAuth: true }
      },
      {
        path: 'device',
        name: 'Device',
        component: () => import('@/views/device/DeviceList.vue'),
        meta: { title: '设备管理', icon: 'VideoCamera', requiresAuth: true }
      },
      {
        path: 'device/add',
        name: 'DeviceAdd',
        component: () => import('@/views/device/DeviceAdd.vue'),
        meta: { title: '添加设备', requiresAuth: true, activeMenu: '/device' }
      },
      {
        path: 'device/detail/:id',
        name: 'DeviceDetail',
        component: () => import('@/views/device/DeviceDetail.vue'),
        meta: { title: '设备详情', requiresAuth: true, activeMenu: '/device' }
      },
      {
        path: 'device/edit/:id',
        name: 'DeviceEdit',
        component: () => import('@/views/device/DeviceAdd.vue'),
        meta: { title: '编辑设备', requiresAuth: true, activeMenu: '/device', isEdit: true }
      },
      {
        path: 'monitoring',
        name: 'Monitoring',
        component: () => import('@/views/monitoring/Monitoring.vue'),
        meta: { title: '实时监控', icon: 'Monitor', requiresAuth: true }
      },
      {
        path: 'alerts',
        name: 'Alerts',
        component: () => import('@/views/alert/AlertList.vue'),
        meta: { title: '报警中心', icon: 'Bell', requiresAuth: true }
      },
      {
        path: 'analytics',
        name: 'Analytics',
        component: () => import('@/views/analytics/Analytics.vue'),
        meta: { title: '数据分析', icon: 'DataAnalysis', requiresAuth: true }
      },
      {
        path: 'task',
        name: 'Task',
        component: () => import('@/views/task/TaskList.vue'),
        meta: { title: '任务调度', icon: 'Calendar', requiresAuth: true }
      },
      {
        path: 'task/create',
        name: 'TaskCreate',
        component: () => import('@/views/task/TaskCreate.vue'),
        meta: { title: '创建任务', requiresAuth: true, activeMenu: '/task' }
      },
      {
        path: 'task/create/device',
        name: 'TaskCreateDevice',
        component: () => import('@/views/task/TaskCreateDevice.vue'),
        meta: { title: '创建任务-设备选择', requiresAuth: true, activeMenu: '/task' }
      },
      {
        path: 'task/create/metrics',
        name: 'TaskCreateMetrics',
        component: () => import('@/views/task/TaskCreateMetrics.vue'),
        meta: { title: '创建任务-指标配置', requiresAuth: true, activeMenu: '/task' }
      },
      {
        path: 'task/create/schedule',
        name: 'TaskCreateSchedule',
        component: () => import('@/views/task/TaskCreateSchedule.vue'),
        meta: { title: '创建任务-调度设置', requiresAuth: true, activeMenu: '/task' }
      },
      {
        path: 'task/detail/:id',
        name: 'TaskDetail',
        component: () => import('@/views/task/TaskDetail.vue'),
        meta: { title: '任务详情', requiresAuth: true, activeMenu: '/task' }
      },
      {
        path: 'task/edit/:id',
        name: 'TaskEdit',
        component: () => import('@/views/task/TaskCreate.vue'),
        meta: { title: '编辑任务', requiresAuth: true, activeMenu: '/task', isEdit: true }
      },
      {
        path: 'history',
        name: 'History',
        component: () => import('@/views/history/History.vue'),
        meta: { title: '历史记录', icon: 'Clock', requiresAuth: true }
      },
      {
        path: 'settings',
        name: 'Settings',
        component: () => import('@/views/settings/Settings.vue'),
        meta: { title: '系统设置', icon: 'Setting', requiresAuth: true }
      },
      {
        path: 'settings/system',
        name: 'SystemSettings',
        component: () => import('@/views/settings/SystemSettings.vue'),
        meta: { title: '系统配置', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/network',
        name: 'NetworkSettings',
        component: () => import('@/views/settings/NetworkSettings.vue'),
        meta: { title: '网络配置', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/storage',
        name: 'StorageSettings',
        component: () => import('@/views/settings/StorageSettings.vue'),
        meta: { title: '存储管理', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/alert',
        name: 'AlertSettings',
        component: () => import('@/views/settings/AlertSettings.vue'),
        meta: { title: '报警规则', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/log',
        name: 'LogSettings',
        component: () => import('@/views/settings/LogSettings.vue'),
        meta: { title: '日志管理', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/backup',
        name: 'BackupSettings',
        component: () => import('@/views/settings/BackupSettings.vue'),
        meta: { title: '备份与恢复', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/security',
        name: 'SecuritySettings',
        component: () => import('@/views/settings/SecuritySettings.vue'),
        meta: { title: '安全设置', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/notification',
        name: 'NotificationSettings',
        component: () => import('@/views/settings/NotificationSettings.vue'),
        meta: { title: '通知设置', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'settings/ai',
        name: 'AISettings',
        component: () => import('@/views/settings/AISettings.vue'),
        meta: { title: '智能分析', requiresAuth: true, activeMenu: '/settings' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('@/views/user/UserList.vue'),
        meta: { title: '用户管理', icon: 'User', requiresAuth: true }
      }
    ]
  },
  {
    path: '/404',
    name: '404',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404', requiresAuth: false }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/404'
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  // 开始进度条
  NProgress.start()
  
  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - ${import.meta.env.VITE_APP_TITLE}` : import.meta.env.VITE_APP_TITLE
  
  // 判断是否需要登录权限
  if (to.meta.requiresAuth !== false) {
    const token = getToken()
    if (token) {
      next()
    } else {
      next({ path: '/login', query: { redirect: to.fullPath } })
    }
  } else {
    next()
  }
})

router.afterEach(() => {
  // 结束进度条
  NProgress.done()
})

export default router 