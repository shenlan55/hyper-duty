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
        path: 'test-person-selector',
        name: 'TestPersonSelector',
        component: () => import('../views/TestPersonSelector.vue'),
        meta: { title: '人员选择框测试' }
      },
      {
        path: 'system',
        name: 'System',
        component: () => import('../views/system/SystemLayout.vue'),
        meta: { title: '系统管理' },
        children: [
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
            path: 'menu',
            name: 'Menu',
            component: () => import('../views/Menu.vue'),
            meta: { title: '菜单管理' }
          },
          {
            path: 'role',
            name: 'Role',
            component: () => import('../views/Role.vue'),
            meta: { title: '角色管理' }
          },
          {
            path: 'dict',
            name: 'Dict',
            component: () => import('../views/Dict.vue'),
            meta: { title: '字典管理' }
          },
          {
            path: 'operation-log',
            name: 'OperationLog',
            component: () => import('../views/duty/OperationLog.vue'),
            meta: { title: '操作日志' }
          },
          {
            path: 'schedule-job',
            name: 'ScheduleJob',
            component: () => import('../views/system/ScheduleJob.vue'),
            meta: { title: '定时任务' }
          },

        ]
      },
      {
        path: 'duty',
        name: 'Duty',
        component: () => import('../views/duty/DutyLayout.vue'),
        meta: { title: '值班管理' },
        children: [
          {
            path: 'schedule',
            name: 'DutySchedule',
            component: () => import('../views/duty/DutySchedule.vue'),
            meta: { title: '值班表管理' }
          },
          {
            path: 'schedule-mode',
            name: 'ScheduleMode',
            component: () => import('../views/duty/ScheduleMode.vue'),
            meta: { title: '排班模式管理' }
          },
          {
            path: 'assignment',
            name: 'DutyAssignment',
            component: () => import('../views/duty/DutyAssignment.vue'),
            meta: { title: '值班安排' }
          },
          {
            path: 'record',
            name: 'DutyRecord',
            component: () => import('../views/duty/DutyRecord.vue'),
            meta: { title: '加班记录' }
          },
          {
            path: 'shift-config',
            name: 'ShiftConfig',
            component: () => import('../views/duty/ShiftConfig.vue'),
            meta: { title: '班次配置' }
          },
          {
            path: 'leave-request',
            name: 'LeaveRequest',
            component: () => import('../views/duty/LeaveRequest.vue'),
            meta: { title: '请假申请' }
          },
          {
            path: 'leave-approval',
            name: 'LeaveApproval',
            component: () => import('../views/duty/LeaveApproval.vue'),
            meta: { title: '请假审批' }
          },
          {
            path: 'swap-request',
            name: 'SwapRequest',
            component: () => import('../views/duty/SwapRequest.vue'),
            meta: { title: '调班管理' }
          },
          {
            path: 'statistics',
            name: 'Statistics',
            component: () => import('../views/duty/Statistics.vue'),
            meta: { title: '排班统计' }
          }
        ]
      },
      {
        path: 'project',
        name: 'Project',
        component: () => import('../views/project/ProjectLayout.vue'),
        meta: { title: '项目管理' },
        children: [
          {
            path: 'list',
            name: 'ProjectList',
            component: () => import('../views/project/ProjectList.vue'),
            meta: { title: '项目列表' }
          },
          {
            path: 'detail',
            name: 'ProjectDetail',
            component: () => import('../views/project/ProjectDetail.vue'),
            meta: { title: '项目详情' }
          },
          {
            path: 'task',
            name: 'TaskList',
            component: () => import('../views/project/TaskList.vue'),
            meta: { title: '任务列表' }
          },
          {
            path: 'my-task',
            name: 'MyTask',
            component: () => import('../views/project/MyTask.vue'),
            meta: { title: '我的任务' }
          },
          {
            path: 'gantt',
            name: 'ProjectGantt',
            component: () => import('../views/project/ProjectGantt.vue'),
            meta: { title: '甘特图' }
          },
          {
            path: 'statistics',
            name: 'ProjectStatistics',
            component: () => import('../views/project/ProjectStatistics.vue'),
            meta: { title: '项目统计' }
          },
          {
            path: 'calendar',
            name: 'TaskCalendar',
            component: () => import('../views/project/TaskCalendar.vue'),
            meta: { title: '日历视图' }
          },
          {
            path: 'team',
            name: 'TeamView',
            component: () => import('../views/project/TeamView.vue'),
            meta: { title: '团队视图' }
          }
        ]
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