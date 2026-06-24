import { defineStore } from 'pinia'
import { getUserMenus } from '../api/menu'

export const useMenuStore = defineStore('menu', {
  state: () => ({
    topMenus: [],
    routeNameMap: { '/dashboard': '首页' },
    loading: false,
    lastFetchTime: 0
  }),

  getters: {
    getTopMenuById: (state) => (id) => {
      return state.topMenus.find(menu => menu.id === id)
    },
    getRouteName: (state) => (path) => {
      return state.routeNameMap[path] || path
    }
  },

  actions: {
    // 刷新用户菜单
    async refreshMenus() {
      this.loading = true
      try {
        // 调用后端API获取用户的实际菜单权限
        const backendMenus = await getUserMenus()
        
        if (backendMenus && Array.isArray(backendMenus) && backendMenus.length > 0) {
          // 转换后端菜单数据为前端需要的格式
          this.topMenus = backendMenus.map(menu => {
            const children = menu.children && Array.isArray(menu.children) ? menu.children.map(child => {
              return {
                name: child.menuName || '未命名菜单',
                path: child.path || '',
                icon: child.icon || 'Menu'
              };
            }) : []
            
            return {
              id: menu.id ? menu.id.toString() : Date.now().toString(),
              name: menu.menuName || '未命名菜单',
              path: menu.path || '',
              icon: menu.icon || 'Menu',
              children
            }
          })
          
          // 更新路由名称映射
          const newRouteNameMap = { '/dashboard': '首页' }
          this.topMenus.forEach(menu => {
            if (menu.path) {
              newRouteNameMap[menu.path] = menu.name
            }
            if (menu.children) {
              menu.children.forEach(child => {
                if (child.path) {
                  newRouteNameMap[child.path] = child.name
                }
              })
            }
          })
          this.routeNameMap = newRouteNameMap
        } else {
          // 后端返回的菜单数据为空或格式不正确，使用默认菜单数据
          this.useDefaultMenus()
        }
      } catch (error) {
        console.error('获取菜单失败：', error)
        // 获取菜单失败时，使用默认菜单数据
        this.useDefaultMenus()
      } finally {
        this.loading = false
        this.lastFetchTime = Date.now()
      }
    },

    // 使用默认菜单数据作为 fallback
    useDefaultMenus() {
      this.topMenus = [
        {
          id: 'dashboard',
          name: '首页',
          path: '/dashboard',
          icon: 'HomeFilled',
          children: [{
            name: '首页',
            path: '/dashboard',
            icon: 'HomeFilled'
          }]
        },
        {
          id: 'system',
          name: '系统管理',
          path: '/system',
          icon: 'Setting',
          children: [
            {
              name: '部门管理',
              path: '/system/dept',
              icon: 'OfficeBuilding'
            },
            {
              name: '人员管理',
              path: '/system/employee',
              icon: 'UserFilled'
            },
            {
              name: '用户管理',
              path: '/system/user',
              icon: 'User'
            },
            {
              name: '菜单管理',
              path: '/system/menu',
              icon: 'Menu'
            },
            {
              name: '角色管理',
              path: '/system/role',
              icon: 'Operation'
            },
            {
              name: '字典管理',
              path: '/system/dict',
              icon: 'List'
            },
            {
              name: '操作日志',
              path: '/system/operation-log',
              icon: 'Document'
            },
            {
              name: '定时任务',
              path: '/system/schedule-job',
              icon: 'Clock'
            }
          ]
        },
        {
          id: 'duty',
          name: '值班管理',
          path: '/duty',
          icon: 'Calendar',
          children: [
            {
              name: '值班表管理',
              path: '/duty/schedule',
              icon: 'DocumentCopy'
            },
            {
              name: '排班模式管理',
              path: '/duty/schedule-mode',
              icon: 'Operation'
            },
            {
              name: '值班安排',
              path: '/duty/assignment',
              icon: 'Calendar'
            },
            {
              name: '加班记录',
              path: '/duty/record',
              icon: 'Document'
            },
            {
              name: '班次配置',
              path: '/duty/shift-config',
              icon: 'List'
            },
            {
              name: '请假申请',
              path: '/duty/leave-request',
              icon: 'User'
            },
            {
              name: '请假审批',
              path: '/duty/leave-approval',
              icon: 'Check'
            },
            {
              name: '调班管理',
              path: '/duty/swap-request',
              icon: 'SwitchButton'
            },
            {
              name: '排班统计',
              path: '/duty/statistics',
              icon: 'DataAnalysis'
            }
          ]
        },
        {
          id: 'project',
          name: '项目管理',
          path: '/project',
          icon: 'Briefcase',
          children: [
            {
              name: '项目列表',
              path: '/project/list',
              icon: 'List'
            },
            {
              name: '项目详情',
              path: '/project/detail',
              icon: 'View'
            },
            {
              name: '任务管理',
              path: '/project/task',
              icon: 'Document'
            },
            {
              name: '我的任务',
              path: '/project/my-task',
              icon: 'User'
            },
            {
              name: '甘特图',
              path: '/project/gantt',
              icon: 'DataAnalysis'
            },
            {
              name: '日历视图',
              path: '/project/calendar',
              icon: 'Calendar'
            },
            {
              name: '团队视图',
              path: '/project/team',
              icon: 'UserFilled'
            },
            {
              name: '项目统计',
              path: '/project/statistics',
              icon: 'DataAnalysis'
            },
            {
              name: 'AI报告生成',
              path: '/project/ai-report',
              icon: 'ChatDotRound'
            }
          ]
        },
        {
          id: 'workflow',
          name: '工作流管理',
          path: '/workflow',
          icon: 'Operation',
          children: [
            {
              name: '发起流程',
              path: '/workflow/start',
              icon: 'Promotion'
            },
            {
              name: '流程定义',
              path: '/workflow/process-list',
              icon: 'Document'
            },
            {
              name: '流程实例',
              path: '/workflow/instance-list',
              icon: 'List'
            },
            {
              name: '待办任务',
              path: '/workflow/todo-task',
              icon: 'Clock'
            },
            {
              name: '已办任务',
              path: '/workflow/done-task',
              icon: 'Check'
            },
            {
              name: '表单管理',
              path: '/workflow/form-list',
              icon: 'DocumentCopy'
            },
            {
              name: '流程分类',
              path: '/workflow/category-list',
              icon: 'Menu'
            },
            {
              name: '委托配置',
              path: '/workflow/delegate-list',
              icon: 'User'
            },
            {
              name: '我发起的',
              path: '/workflow/my-started',
              icon: 'Promotion'
            }
          ]
        },
        {
          id: 'score',
          name: '积分管理',
          path: '/score',
          icon: 'Trophy',
          children: [
            {
              name: '积分事件',
              path: '/score/event',
              icon: 'Setting'
            },
            {
              name: '积分记录',
              path: '/score/record',
              icon: 'Edit'
            },
            {
              name: '月度汇总',
              path: '/score/summary',
              icon: 'DataAnalysis'
            },
            {
              name: '评选排名',
              path: '/score/evaluation',
              icon: 'Trophy'
            }
          ]
        }
      ]
      
      // 更新路由名称映射
      this.routeNameMap = {
        '/dashboard': '首页',
        '/system': '系统管理',
        '/system/dept': '部门管理',
        '/system/employee': '人员管理',
        '/system/user': '用户管理',
        '/system/menu': '菜单管理',
        '/system/role': '角色管理',
        '/system/dict': '字典管理',
        '/system/operation-log': '操作日志',
        '/system/schedule-job': '定时任务',
        '/duty': '值班管理',
        '/duty/schedule': '值班表管理',
        '/duty/schedule-mode': '排班模式管理',
        '/duty/assignment': '值班安排',
        '/duty/record': '加班记录',
        '/duty/shift-config': '班次配置',
        '/duty/leave-request': '请假申请',
        '/duty/leave-approval': '请假审批',
        '/duty/swap-request': '调班管理',
        '/duty/statistics': '排班统计',
        '/project': '项目管理',
        '/project/list': '项目列表',
        '/project/detail': '项目详情',
        '/project/task': '任务管理',
        '/project/my-task': '我的任务',
        '/project/gantt': '甘特图',
        '/project/calendar': '日历视图',
        '/project/team': '团队视图',
        '/project/statistics': '项目统计',
        '/project/ai-report': 'AI报告生成',
        '/workflow': '工作流管理',
        '/workflow/start': '发起流程',
        '/workflow/process-list': '流程定义',
        '/workflow/instance-list': '流程实例',
        '/workflow/form-list': '表单管理',
        '/workflow/category-list': '流程分类',
        '/workflow/delegate-list': '委托配置',
        '/workflow/designer': '流程设计器',
        '/workflow/todo-task': '待办任务',
        '/workflow/done-task': '已办任务',
        '/workflow/my-started': '我发起的',
        '/score': '积分管理',
        '/score/event': '积分事件',
        '/score/record': '积分记录',
        '/score/summary': '月度汇总',
        '/score/evaluation': '评选排名'
      }
    },

    // 重置菜单状态
    resetMenus() {
      this.topMenus = []
      this.routeNameMap = { '/dashboard': '首页' }
      this.lastFetchTime = 0
    }
  }
})
