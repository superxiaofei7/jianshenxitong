<template>
  <el-container class="layout-container">
    <el-aside :width="isCollapse ? '72px' : '260px'" class="layout-aside">
      <div class="logo-area">
        <div class="logo-icon">
          <el-icon :size="54"><Fitness /></el-icon>
        </div>
        <transition name="fade">
          <div v-if="!isCollapse" class="logo-text">
            <span class="logo-main">FitPro</span>
            <span class="logo-sub">私教管理系统</span>
          </div>
        </transition>
      </div>

      <el-scrollbar class="menu-scrollbar">
        <el-menu
          :default-active="activeMenu"
          router
          :collapse="isCollapse"
          :collapse-transition="false"
          background-color="transparent"
          text-color="rgba(0,0,0,0.55)"
          active-text-color="#4f46e5"
          class="sidebar-menu"
        >
          <div class="menu-group-label" v-if="!isCollapse">主菜单</div>
          <el-menu-item index="/dashboard">
            <el-icon><HomeFilled /></el-icon>
            <template #title>系统首页</template>
          </el-menu-item>
          <!-- 教练展示（所有角色除了财务） -->
          <el-menu-item v-if="!userStore.isFinance()" index="/trainers">
            <el-icon><UserFilled /></el-icon>
            <template #title>教练展示</template>
          </el-menu-item>

          <!-- 教练专属功能 -->
          <el-menu-item v-if="userStore.isTrainer()" index="/trainer-schedule">
            <el-icon><Calendar /></el-icon>
            <template #title>课程安排</template>
          </el-menu-item>

          <!-- 个人信息（会员和教练） -->
          <el-menu-item v-if="userStore.isMember() || userStore.isTrainer() || userStore.isAdmin()" index="/profile">
            <el-icon><User /></el-icon>
            <template #title>个人信息</template>
          </el-menu-item>

          <!-- 会员专属功能 -->
          <el-menu-item v-if="userStore.isMember()" index="/booking">
            <el-icon><Calendar /></el-icon>
            <template #title>在线预约</template>
          </el-menu-item>
          <el-menu-item v-if="userStore.isMember()" index="/my-bookings">
            <el-icon><Tickets /></el-icon>
            <template #title>我的预约</template>
          </el-menu-item>

          <!-- 管理中心（仅前台管理员） -->
          <template v-if="userStore.isAdmin()">
            <div class="menu-group-label" v-if="!isCollapse">管理中心</div>
            <el-menu-item index="/members">
              <el-icon><Avatar /></el-icon>
              <template #title>会员管理</template>
            </el-menu-item>
            <el-menu-item index="/trainer-manage">
              <el-icon><UserFilled /></el-icon>
              <template #title>教练管理</template>
            </el-menu-item>
            <el-menu-item index="/courses">
              <el-icon><Notebook /></el-icon>
              <template #title>课程管理</template>
            </el-menu-item>
            <el-menu-item index="/schedule">
              <el-icon><Clock /></el-icon>
              <template #title>排班管理</template>
            </el-menu-item>
            <el-menu-item index="/checkin">
              <el-icon><Checked /></el-icon>
              <template #title>签到管理</template>
            </el-menu-item>
          </template>

          <!-- 财务管理员专属功能 -->
          <template v-if="userStore.isFinance()">
            <div class="menu-group-label" v-if="!isCollapse">数据分析</div>
            <el-menu-item index="/statistics">
              <el-icon><DataAnalysis /></el-icon>
              <template #title>数据统计</template>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>

      <div class="sidebar-footer" v-if="!isCollapse">
        <div class="sidebar-footer-text">v2.0 · FitPro Studio</div>
      </div>
    </el-aside>

    <el-container class="main-container">
      <el-header class="layout-header" height="64px">
        <div class="header-left">
          <div class="collapse-btn" @click="isCollapse = !isCollapse">
            <el-icon :size="20">
              <Fold v-if="!isCollapse" />
              <Expand v-else />
            </el-icon>
          </div>
          <el-breadcrumb separator="/" class="header-breadcrumb">
            <el-breadcrumb-item :to="{ path: '/dashboard' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item v-if="currentPageTitle">{{ currentPageTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tooltip content="全屏" placement="bottom">
            <div class="header-action" @click="toggleFullscreen">
              <el-icon :size="18"><FullScreen /></el-icon>
            </div>
          </el-tooltip>

          <el-dropdown trigger="click" @command="handleCommand">
            <div class="user-info">
              <el-avatar :size="36" :src="userStore.user?.avatar" class="user-avatar">
                {{ userStore.user?.realName?.charAt(0) || 'U' }}
              </el-avatar>
              <div class="user-detail">
                <span class="user-name">{{ userStore.user?.realName }}</span>
                <el-tag :type="roleTagType" size="small" effect="dark" round>{{ roleName }}</el-tag>
              </div>
              <el-icon class="user-arrow"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <el-icon><User /></el-icon>个人信息
                </el-dropdown-item>
                <el-dropdown-item divided command="logout">
                  <el-icon><SwitchButton /></el-icon>退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <el-main class="layout-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-slide" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/store/user'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => route.path)

const currentPageTitle = computed(() => {
  const map = {
    '/dashboard': '系统首页',
    '/trainers': '教练展示',
    '/profile': '个人信息',
    '/booking': '在线预约',
    '/my-bookings': '我的预约',
    '/members': '会员管理',
    '/trainer-manage': '教练管理',
    '/courses': '课程管理',
    '/schedule': '排班管理',
    '/checkin': '签到管理',
    '/statistics': '数据统计',
    '/finance-dashboard': '财务首页',
    '/trainer-schedule': '课程安排',
    '/booking': '在线预约',
    '/my-bookings': '我的预约'
  }
  return map[route.path] || ''
})

const roleName = computed(() => {
  const map = { MEMBER: '会员', TRAINER: '教练', FRONT_DESK: '前台', FINANCE: '财务' }
  return map[userStore.user?.role] || '用户'
})

const roleTagType = computed(() => {
  const map = { MEMBER: 'success', TRAINER: 'warning', FRONT_DESK: '', FINANCE: 'danger' }
  return map[userStore.user?.role] || 'info'
})

const toggleFullscreen = () => {
  if (!document.fullscreenElement) {
    document.documentElement.requestFullscreen()
  } else {
    document.exitFullscreen()
  }
}

const handleCommand = (cmd) => {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    userStore.logout()
    router.push('/login')
  }
}
</script>

<style scoped>
.layout-container {
  height: 100vh;
  overflow: hidden;
}

/* Sidebar */
.layout-aside {
  background: linear-gradient(180deg, #ffffff 0%, #f8f9fb 50%, #f0f2f5 100%);
  transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  overflow: hidden;
  display: flex;
  flex-direction: column;
  box-shadow: 1px 0 8px rgba(0, 0, 0, 0.06);
  position: relative;
  z-index: 10;
  border-right: 1px solid #e8ecf0;
}

.logo-area {
  height: 100px;
  display: flex;
  align-items: center;
  padding: 0 20px;
  gap: 12px;
  border-bottom: 1px solid #e8ecf0;
  flex-shrink: 0;
}

.logo-icon {
  width: 90px;
  height: 90px;
  background: url('../images/logo.png') center/cover no-repeat;
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.25);
}

.logo-text {
  display: flex;
  flex-direction: column;
  white-space: nowrap;
}

.logo-main {
  color: #1e293b;
  font-size: 18px;
  font-weight: 700;
  letter-spacing: -0.02em;
}

.logo-sub {
  color: #94a3b8;
  font-size: 11px;
  font-weight: 400;
  margin-top: 1px;
}

/* Menu */
.menu-scrollbar {
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none !important;
  padding: 8px;
}

.sidebar-menu .el-menu-item {
  height: 44px;
  line-height: 44px;
  margin: 2px 0;
  border-radius: 10px;
  transition: all 0.25s ease;
}

.sidebar-menu .el-menu-item:hover {
  background: rgba(99, 102, 241, 0.08) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background: linear-gradient(135deg, rgba(99, 102, 241, 0.12), rgba(129, 140, 248, 0.08)) !important;
  box-shadow: 0 2px 8px rgba(99, 102, 241, 0.12);
}

.menu-group-label {
  color: rgba(0, 0, 0, 0.35);
  font-size: 11px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  padding: 20px 16px 8px;
}

.sidebar-footer {
  padding: 16px 20px;
  border-top: 1px solid #e8ecf0;
  flex-shrink: 0;
}

.sidebar-footer-text {
  color: rgba(0, 0, 0, 0.3);
  font-size: 11px;
}

/* Header */
.layout-header {
  background: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  border-bottom: 1px solid var(--border-color);
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.collapse-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: var(--transition);
}
.collapse-btn:hover {
  background: #f1f5f9;
  color: var(--primary);
}

.header-breadcrumb {
  font-size: 14px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.header-action {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  cursor: pointer;
  color: var(--text-secondary);
  transition: var(--transition);
}
.header-action:hover {
  background: #f1f5f9;
  color: var(--primary);
}

.user-info {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 4px 12px 4px 4px;
  border-radius: 24px;
  transition: var(--transition);
}
.user-info:hover {
  background: #f8fafc;
}

.user-avatar {
  background: linear-gradient(135deg, var(--primary), var(--primary-dark));
  color: white;
  font-weight: 600;
  font-size: 14px;
}

.user-detail {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  line-height: 1.2;
}

.user-arrow {
  color: var(--text-muted);
  font-size: 12px;
}

/* Main content */
.main-container {
  overflow: hidden;
}

.layout-main {
  background: var(--bg-main);
  padding: 24px;
  overflow-y: auto;
  height: calc(100vh - 64px);
}

/* Transitions */
.fade-enter-active, .fade-leave-active {
  transition: opacity 0.2s ease;
}
.fade-enter-from, .fade-leave-to {
  opacity: 0;
}
</style>
