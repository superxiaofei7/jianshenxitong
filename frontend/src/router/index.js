import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/Register.vue'),
    meta: { title: '注册' }
  },
  {
    path: '/forgot-password',
    name: 'ForgotPassword',
    component: () => import('@/views/ForgotPassword.vue'),
    meta: { title: '忘记密码' }
  },
  {
    path: '/',
    component: () => import('@/views/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/Dashboard.vue'),
        meta: { title: '首页', icon: 'HomeFilled' }
      },
      {
        path: 'trainers',
        name: 'TrainerList',
        component: () => import('@/views/TrainerList.vue'),
        meta: { title: '教练展示', icon: 'UserFilled', roles: ['MEMBER', 'TRAINER', 'FRONT_DESK'] }
      },
      {
        path: 'booking',
        name: 'Booking',
        component: () => import('@/views/Booking.vue'),
        meta: { title: '在线预约', icon: 'Calendar', roles: ['MEMBER'] }
      },
      {
        path: 'my-bookings',
        name: 'MyBookings',
        component: () => import('@/views/MyBookings.vue'),
        meta: { title: '我的预约', icon: 'Tickets', roles: ['MEMBER'] }
      },
      {
        path: 'trainer-schedule',
        name: 'TrainerSchedule',
        component: () => import('@/views/TrainerSchedule.vue'),
        meta: { title: '课程安排', icon: 'Calendar', roles: ['TRAINER'] }
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/views/Profile.vue'),
        meta: { title: '个人信息', icon: 'User', roles: ['MEMBER', 'TRAINER', 'FRONT_DESK'] }
      },
      {
        path: 'members',
        name: 'MemberList',
        component: () => import('@/views/MemberList.vue'),
        meta: { title: '会员管理', icon: 'Avatar', roles: ['FRONT_DESK'] }
      },
      {
        path: 'trainer-manage',
        name: 'TrainerManage',
        component: () => import('@/views/TrainerManage.vue'),
        meta: { title: '教练管理', icon: 'UserFilled', roles: ['FRONT_DESK'] }
      },
      {
        path: 'courses',
        name: 'CourseList',
        component: () => import('@/views/CourseList.vue'),
        meta: { title: '课程管理', icon: 'Notebook', roles: ['FRONT_DESK'] }
      },
      {
        path: 'schedule',
        name: 'ScheduleManage',
        component: () => import('@/views/ScheduleManage.vue'),
        meta: { title: '排班管理', icon: 'Calendar', roles: ['FRONT_DESK'] }
      },
      {
        path: 'checkin',
        name: 'CheckIn',
        component: () => import('@/views/CheckIn.vue'),
        meta: { title: '签到管理', icon: 'Checked', roles: ['FRONT_DESK'] }
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/views/Statistics.vue'),
        meta: { title: '数据统计', icon: 'DataAnalysis', roles: ['FRONT_DESK', 'FINANCE'] }
      },
      {
        path: 'finance-dashboard',
        name: 'FinanceDashboard',
        component: () => import('@/views/FinanceDashboard.vue'),
        meta: { title: '财务首页', icon: 'Money', roles: ['FINANCE'] }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const user = JSON.parse(localStorage.getItem('user') || 'null')

  if (to.path === '/login' || to.path === '/register' || to.path === '/forgot-password') {
    if (token && to.path === '/login') {
      next('/dashboard')
    } else {
      next()
    }
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 角色权限检查
  if (to.meta.roles && user) {
    if (!to.meta.roles.includes(user.role)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
