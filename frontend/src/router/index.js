import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { title: '登录' }
  },
  {
    path: '/',
    name: 'Layout',
    component: () => import('../components/Layout.vue'),
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('../views/Dashboard.vue'),
        meta: { title: '首页' }
      },
      {
        path: 'dept',
        name: 'Dept',
        component: () => import('../views/Dept.vue'),
        meta: { title: '部门管理' }
      },
      {
        path: 'employee',
        name: 'Employee',
        component: () => import('../views/Employee.vue'),
        meta: { title: '人员管理' }
      },
      {
        path: 'user',
        name: 'User',
        component: () => import('../views/User.vue'),
        meta: { title: '用户管理' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// JWT解码工具函数
const decodeJWT = (token) => {
  try {
    const base64Url = token.split('.')[1]
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/')
    const jsonPayload = decodeURIComponent(atob(base64).split('').map(function(c) {
      return '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2)
    }).join(''))
    return JSON.parse(jsonPayload)
  } catch (error) {
    return null
  }
}

// 检查JWT是否过期
const isJWTExpired = (token) => {
  if (!token) return true
  
  const decoded = decodeJWT(token)
  if (!decoded || !decoded.exp) return true
  
  // 检查token是否过期（当前时间大于过期时间）
  const currentTime = Math.floor(Date.now() / 1000)
  return currentTime > decoded.exp
}

// 路由守卫
router.beforeEach((to, from, next) => {
  // 设置页面标题
  document.title = to.meta.title || 'Hyper Duty'
  
  // 检查是否登录，除了登录页
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else if (to.path !== '/login' && token) {
    // 检查token是否过期
    if (isJWTExpired(token)) {
      // token过期，清除token并重定向到登录页
      localStorage.removeItem('token')
      localStorage.removeItem('username')
      next('/login')
    } else {
      next()
    }
  } else {
    next()
  }
})

export default router