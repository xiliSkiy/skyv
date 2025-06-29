<template>
  <div class="app-container">
    <!-- 侧边栏 -->
    <div class="sidebar" :class="{ 'collapsed': isCollapse }">
      <div class="logo">
        <el-icon><Monitor /></el-icon>
        <span v-show="!isCollapse">SkyEye 智能监控</span>
      </div>
      <el-menu
        :default-active="activeMenu"
        :collapse="isCollapse"
        background-color="#1e3c72"
        text-color="#fff"
        active-text-color="#ffd04b"
        router
        unique-opened
      >
        <el-menu-item index="/dashboard">
          <el-icon><Odometer /></el-icon>
          <template #title>控制台</template>
        </el-menu-item>
        
        <!-- 设备管理菜单 -->
        <el-sub-menu index="/device">
          <template #title>
            <el-icon><VideoCamera /></el-icon>
            <span>设备管理</span>
          </template>
          <el-menu-item index="/device">设备列表</el-menu-item>
          <el-menu-item index="/device/types">设备类型</el-menu-item>
          <el-menu-item index="/device/areas">设备区域</el-menu-item>
          <el-menu-item index="/device/groups">设备分组</el-menu-item>
          <el-menu-item index="/device/tags">设备标签</el-menu-item>
          <el-menu-item index="/device/protocols">设备协议</el-menu-item>
          <el-menu-item index="/device/templates">设备模板</el-menu-item>
        </el-sub-menu>
        
        <el-menu-item index="/monitoring">
          <el-icon><Monitor /></el-icon>
          <template #title>实时监控</template>
        </el-menu-item>
        <el-menu-item index="/alerts">
          <el-icon><Bell /></el-icon>
          <template #title>报警中心</template>
        </el-menu-item>
        <el-menu-item index="/analytics">
          <el-icon><DataAnalysis /></el-icon>
          <template #title>数据分析</template>
        </el-menu-item>
        <el-menu-item index="/task">
          <el-icon><Calendar /></el-icon>
          <template #title>任务调度</template>
        </el-menu-item>
        <el-menu-item index="/history">
          <el-icon><Clock /></el-icon>
          <template #title>历史记录</template>
        </el-menu-item>
        <el-menu-item index="/settings">
          <el-icon><Setting /></el-icon>
          <template #title>系统设置</template>
        </el-menu-item>
        <el-menu-item index="/user">
          <el-icon><User /></el-icon>
          <template #title>用户管理</template>
        </el-menu-item>
      </el-menu>
    </div>

    <!-- 主内容区 -->
    <div class="main-container" :class="{ 'collapsed': isCollapse }">
      <!-- 顶部导航栏 -->
      <div class="header">
        <div class="left">
          <el-button type="text" @click="toggleSidebar">
            <el-icon :size="20">
              <component :is="isCollapse ? 'Expand' : 'Fold'" />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentRouteName }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="right">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-container">
              <el-avatar :size="32" :src="userInfo.avatar">
                {{ userInfo.name ? userInfo.name.substring(0, 1) : 'U' }}
              </el-avatar>
              <span class="username">{{ userInfo.name || userInfo.username }}</span>
              <el-icon><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item command="settings">
                  <el-icon><Setting /></el-icon>账号设置
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <keep-alive>
              <component :is="Component" />
            </keep-alive>
          </transition>
        </router-view>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/modules/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 侧边栏折叠状态
const isCollapse = ref(false)

// 获取用户信息
const userInfo = computed(() => userStore.userInfo || {})

// 当前路由名称
const currentRouteName = computed(() => {
  return route.meta.title || route.name
})

// 当前激活菜单
const activeMenu = computed(() => {
  return route.meta.activeMenu || route.path
})

// 切换侧边栏
const toggleSidebar = () => {
  isCollapse.value = !isCollapse.value
}

// 处理下拉菜单命令
const handleCommand = (command) => {
  if (command === 'logout') {
    ElMessageBox.confirm('确定要退出登录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    }).then(async () => {
      await userStore.logout()
      router.push('/login')
    }).catch(() => {})
  } else if (command === 'profile') {
    router.push('/profile')
  } else if (command === 'settings') {
    router.push('/settings')
  }
}
</script>

<style lang="scss" scoped>
.app-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
  position: relative;
}

.sidebar {
  width: 180px;
  height: 100%;
  background: linear-gradient(180deg, #1e3c72, #2a5298);
  transition: all 0.3s ease;
  overflow-y: auto;
  overflow-x: hidden;
  box-shadow: 2px 0 10px rgba(0, 0, 0, 0.2);
  z-index: 10;
  position: relative;
  flex-shrink: 0;

  &.collapsed {
    width: 56px;
  }

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    color: white;
    font-size: 16px;
    font-weight: bold;
    padding: 0 12px;
    overflow: hidden;
    white-space: nowrap;
    border-bottom: 1px solid rgba(255, 255, 255, 0.1);

    .el-icon {
      margin-right: 8px;
      font-size: 22px;
    }
  }
}

.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background-color: #f5f7fa;
  transition: all 0.3s ease;

  .header {
    height: 60px;
    background-color: white;
    box-shadow: 0 2px 6px rgba(0, 0, 0, 0.05);
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 15px;
    position: relative;
    z-index: 5;

    .left {
      display: flex;
      align-items: center;

      .el-button {
        margin-right: 12px;
      }
    }

    .right {
      display: flex;
      align-items: center;
    }

    .avatar-container {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 4px 8px;
      border-radius: 4px;
      transition: background-color 0.2s;
      
      &:hover {
        background-color: rgba(0, 0, 0, 0.05);
      }

      .username {
        margin: 0 8px;
        font-size: 14px;
      }
    }
  }

  .content {
    flex: 1;
    padding: 8px;
    overflow-y: auto;
    position: relative;
  }
}

/* 路由过渡动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(20px);
}
</style> 