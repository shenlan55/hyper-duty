<template>
  <div class="layout-container">
    <!-- 顶部导航栏 -->
    <el-header class="header">
      <div class="header-left">
        <div class="logo">
          <h1>Hyper Duty</h1>
        </div>
      </div>
      <div class="header-right">
        <el-dropdown trigger="click">
          <div class="user-info">
            <el-avatar :size="32">{{ username[0] }}</el-avatar>
            <span class="username">{{ username }}</span>
            <el-icon class="arrow-down"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </div>
    </el-header>
    <!-- 主体内容 -->
    <div class="main-wrapper">
      <!-- 侧边栏 -->
      <el-aside width="200px" class="sidebar">
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          unique-opened
          :background-color="'#fff'"
          :text-color="'#303133'"
          :active-text-color="'#1177BB'"
          :default-openeds="['system']"
        >
          <!-- 动态二级菜单 -->
          <template v-if="leftMenus.length > 0">
            <el-menu-item
              v-for="menu in leftMenus"
              :key="menu.path"
              :index="menu.path"
            >
              <el-icon style="vertical-align: middle; margin-right: 8px;">
                <component :is="menu.icon && iconMap[menu.icon] ? iconMap[menu.icon] : Menu" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </el-menu-item>
          </template>
          <!-- 首页菜单项 -->
          <el-menu-item v-else-if="activeTopMenu === 'dashboard'" index="/dashboard">
            <template #icon>
              <el-icon><House /></el-icon>
            </template>
            <span>首页</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <!-- 右侧内容区 -->
      <div class="right-content">
        <!-- 顶部一级菜单栏 -->
        <div class="top-menu-bar">
          <el-menu
            :default-active="activeTopMenu"
            class="top-menu"
            mode="horizontal"
            @select="handleTopMenuChange"
          >
            <el-menu-item
              v-for="menu in topMenus"
              :key="menu.id"
              :index="menu.id"
            >
              <template #icon>
                <el-icon><component :is="menu.icon && iconMap[menu.icon] ? iconMap[menu.icon] : House" /></el-icon>
              </template>
              <span>{{ menu.name }}</span>
            </el-menu-item>
          </el-menu>
        </div>
        <!-- 标签页 -->
        <div class="tab-container">
          <el-tabs 
            v-model:active-tab="activeTab" 
            type="card" 
            @tab-remove="handleTabRemove"
            @tab-change="handleTabChange"
          >
            <el-tab-pane 
              v-for="tab in tabs" 
              :key="tab.path" 
              :label="tab.name" 
              :name="tab.path"
              closable
            >
            </el-tab-pane>
          </el-tabs>
        </div>
        <!-- 内容区域 -->
        <el-main class="content">
          <router-view v-if="$route.path === activeTab" />
        </el-main>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { logout } from '../api/auth'
import { getUserMenus } from '../api/menu'
import { ElMessage } from 'element-plus'
import { 
  House, Setting, OfficeBuilding, UserFilled, User, Menu, ArrowDown, 
  SwitchButton, HomeFilled, Operation, Edit, Delete, Plus, Check, Search,
  ArrowUp, ArrowLeft, ArrowRight, DocumentCopy, List, View, Calendar, Document,
  Avatar, WarningFilled, InfoFilled, SuccessFilled, QuestionFilled, StarFilled,
  Clock, CircleCheck, Refresh, DataAnalysis
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const username = computed(() => userStore.username)

// 动态菜单数据
const topMenus = ref([])
const loading = ref(false)

// 当前激活的一级菜单
const activeTopMenu = ref('dashboard')

// 动态二级菜单
const leftMenus = computed(() => {
  const menu = topMenus.value.find(m => m.id === activeTopMenu.value)
  
  // 首页菜单没有子菜单，显示首页本身
  if (activeTopMenu.value === 'dashboard') {
    return [{
      name: '首页',
      path: '/dashboard',
      icon: 'HomeFilled'
    }]
  }
  
  // 返回菜单的子菜单，如果没有则返回空数组
  return menu?.children || []
})

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 标签页相关状态
const tabs = ref([])
const activeTab = ref('/dashboard')

// 图标映射
const iconMap = {
  'House': House,
  'Setting': Setting,
  'OfficeBuilding': OfficeBuilding,
  'UserFilled': UserFilled,
  'User': User,
  'Menu': Menu,
  'HomeFilled': HomeFilled,
  'Operation': Operation,
  'Edit': Edit,
  'Delete': Delete,
  'Plus': Plus,
  'Check': Check,
  'Search': Search,
  'ArrowUp': ArrowUp,
  'ArrowDown': ArrowDown,
  'ArrowLeft': ArrowLeft,
  'ArrowRight': ArrowRight,
  'SwitchButton': SwitchButton,
  'DocumentCopy': DocumentCopy,
  'List': List,
  'View': View,
  'Calendar': Calendar,
  'Document': Document,
  'Avatar': Avatar,
  'WarningFilled': WarningFilled,
  'InfoFilled': InfoFilled,
  'SuccessFilled': SuccessFilled,
  'QuestionFilled': QuestionFilled,
  'StarFilled': StarFilled,
  'Clock': Clock,
  'CircleCheck': CircleCheck,
  'Refresh': Refresh,
  'DataAnalysis': DataAnalysis
}

// 路由名称映射
const routeNameMap = ref({
  '/dashboard': '首页'
})

// 获取用户菜单
const fetchUserMenus = async () => {
  loading.value = true
  try {
    const response = await getUserMenus()
    let menus = []
    
    if (response.code === 200) {
      // 处理菜单数据，后端返回的已经是树形结构
      menus = response.data
    }
    
    // 转换菜单数据格式，适配前端需要的结构
    const processedMenus = menus.filter(menu => menu).map(menu => {
      // 构建一级菜单
      const firstLevelMenu = {
        id: menu.id.toString(),
        name: menu.menuName,
        path: menu.path,
        icon: menu.icon,
        children: []
      }
      
      // 更新路由名称映射
      routeNameMap.value[menu.path] = menu.menuName
      
      // 处理二级菜单
      if (menu.children && menu.children.length > 0) {
        firstLevelMenu.children = menu.children.filter(childMenu => childMenu).map(childMenu => {
          let childPath = childMenu.path
          
          // 修复系统管理子菜单路径
          if (childPath === '/dept') childPath = '/system/dept'
          else if (childPath === '/employee') childPath = '/system/employee'
          else if (childPath === '/user') childPath = '/system/user'
          else if (childPath === '/menu') childPath = '/system/menu'
          else if (childPath === '/role') childPath = '/system/role'
          else if (childPath === '/dict') childPath = '/system/dict'
          else if (childPath === '/operation-log') childPath = '/system/operation-log'
          
          // 更新路由名称映射
          routeNameMap.value[childPath] = childMenu.menuName
          
          return {
            name: childMenu.menuName,
            path: childPath,
            icon: childMenu.icon
          }
        })
      }
      
      // 修复系统管理父菜单路径
      if (menu.menuName === '系统管理') {
        firstLevelMenu.path = '/system'
        routeNameMap.value['/system'] = menu.menuName
      }
      
      return firstLevelMenu
    })
    
    // 添加值班管理菜单（如果不存在）
    let dutyMenuExists = processedMenus.some(menu => menu.name === '值班管理')
    
    // 添加首页菜单（如果不存在）
    let dashboardMenuExists = processedMenus.some(menu => menu.name === '首页')
    
    if (!dashboardMenuExists) {
      processedMenus.unshift({
        id: 'dashboard',
        name: '首页',
        path: '/dashboard',
        icon: 'HomeFilled',
        children: [{
          name: '首页',
          path: '/dashboard',
          icon: 'HomeFilled'
        }]
      })
      routeNameMap.value['/dashboard'] = '首页'
    }
    
    if (!dutyMenuExists) {
      // 添加值班管理目录菜单
      const dutyMenu = {
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
            name: '值班安排',
            path: '/duty/assignment',
            icon: 'Calendar'
          },
          {
            name: '值班记录',
            path: '/duty/record',
            icon: 'Document'
          }
        ]
      }
      
      // 更新路由名称映射
      routeNameMap.value['/duty'] = '值班管理'
      routeNameMap.value['/duty/schedule'] = '值班表管理'
      routeNameMap.value['/duty/assignment'] = '值班安排'
      routeNameMap.value['/duty/record'] = '值班记录'
      
      // 添加到菜单列表
      processedMenus.push(dutyMenu)
    }
    
    // 更新菜单列表
    topMenus.value = processedMenus
    
    // 如果有菜单，设置第一个为默认激活
    if (topMenus.value.length > 0) {
      activeTopMenu.value = topMenus.value[0].id
    }
  } catch (error) {
    console.error('获取菜单失败：', error)
    ElMessage.error('获取菜单失败：' + error.message)
    
    // 获取菜单失败时，添加默认的值班管理菜单
    topMenus.value = [
      {
        id: 'dashboard',
        name: '首页',
        path: '/dashboard',
        icon: 'House',
        children: []
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
            name: '值班安排',
            path: '/duty/assignment',
            icon: 'Calendar'
          },
          {
            name: '值班记录',
            path: '/duty/record',
            icon: 'Document'
          }
        ]
      }
    ]
    
    // 更新路由名称映射
    routeNameMap.value = {
      '/dashboard': '首页',
      '/system': '系统管理',
      '/system/dept': '部门管理',
      '/system/employee': '人员管理',
      '/system/user': '用户管理',
      '/system/menu': '菜单管理',
      '/system/role': '角色管理',
      '/system/dict': '字典管理',
      '/system/operation-log': '操作日志',
      '/duty': '值班管理',
      '/duty/schedule': '值班表管理',
      '/duty/assignment': '值班安排',
      '/duty/record': '值班记录'
    }
  } finally {
    loading.value = false
  }
}

// 添加标签页
const addTab = (path) => {
  if (!tabs.value.some(tab => tab.path === path)) {
    tabs.value.push({
      path,
      name: routeNameMap.value[path] || path
    })
  }
  activeTab.value = path
}

// 处理标签页切换
const handleTabChange = (path) => {
  activeTab.value = path
  router.push(path)
}

// 处理标签页关闭
const handleTabRemove = (path) => {
  const index = tabs.value.findIndex(tab => tab.path === path)
  if (index > -1) {
    tabs.value.splice(index, 1)
    // 如果关闭的是当前激活的标签页，切换到前一个或第一个标签页
    if (activeTab.value === path) {
      const newActiveIndex = index > 0 ? index - 1 : 0
      if (tabs.value.length > 0) {
        activeTab.value = tabs.value[newActiveIndex].path
        router.push(activeTab.value)
      } else {
        // 如果没有标签页了，跳转到首页
        activeTab.value = '/dashboard'
        router.push('/dashboard')
      }
    }
  }
}

// 监听路由变化，更新激活的一级菜单和添加标签页
watch(() => route.path, (newPath) => {
  // 根据当前路由更新激活的一级菜单
  let found = false
  for (const menu of topMenus.value) {
    if (menu.path === newPath) {
      activeTopMenu.value = menu.id
      found = true
      break
    }
    for (const child of menu.children || []) {
      if (child.path === newPath) {
        activeTopMenu.value = menu.id
        found = true
        break
      }
    }
    if (found) break
  }
  // 添加标签页
  addTab(newPath)
})

// 切换一级菜单
const handleTopMenuChange = (index) => {
  activeTopMenu.value = index
  const menu = topMenus.value.find(m => m.id === index)
  if (menu) {
    if (menu.children && menu.children.length > 0) {
      // 跳转到第一个二级菜单
      router.push(menu.children[0].path)
    } else {
      // 直接跳转到一级菜单路径
      router.push(menu.path)
    }
  }
}

// 退出登录
const handleLogout = async () => {
  try {
    await logout()
    userStore.logout()
    router.push('/login')
    ElMessage.success('退出登录成功')
  } catch (error) {
    console.error('退出登录失败:', error)
    // 即使API调用失败，也要清除本地数据并跳转到登录页
    userStore.logout()
    router.push('/login')
  }
}

// 初始化
onMounted(async () => {
  // 获取用户菜单
  await fetchUserMenus()
  
  // 初始化标签页，只保留首页
  tabs.value = [
    {
      path: '/dashboard',
      name: '首页'
    }
  ]
  
  // 刷新页面时，默认回到首页
  activeTab.value = '/dashboard'
  
  // 如果当前路由不是首页，重定向到首页
  if (route.path !== '/dashboard') {
    router.push('/dashboard')
  }
})
</script>

<style scoped>
.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.logo h1 {
  margin: 0;
  font-size: 20px;
  color: #1890ff;
  font-weight: bold;
}

.top-menu {
  border-bottom: none;
}

.top-menu :deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  font-size: 14px;
}

.top-menu-bar {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  height: 60px;
}

.top-menu {
  border-bottom: none;
  height: 60px;
  line-height: 60px;
}

.top-menu :deep(.el-menu-item) {
  height: 60px;
  line-height: 60px;
  font-size: 14px;
}

.top-menu :deep(.el-menu-item.is-active) {
  color: #1890ff;
  background-color: #ecf5ff;
}

.top-menu :deep(.el-menu) {
  height: 60px;
  line-height: 60px;
  border-bottom: none;
}

.tab-container {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.09);
  height: 40px;
}

.tab-container :deep(.el-tabs__header) {
  margin: 0;
  padding: 0 10px;
  border-bottom: none;
  height: 40px;
  line-height: 40px;
}

.tab-container :deep(.el-tabs__nav-wrap) {
  margin: 0;
  height: 40px;
}

.tab-container :deep(.el-tabs__nav-scroll) {
  height: 40px;
}

.tab-container :deep(.el-tabs__item) {
  height: 40px;
  line-height: 40px;
  font-size: 14px;
}

.tab-container :deep(.el-tabs__nav) {
  height: 40px;
}

.tab-container :deep(.el-tabs__active-bar) {
  bottom: 0;
}

.header-right {
  display: flex;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 0 10px;
  height: 40px;
  border-radius: 4px;
  transition: background-color 0.3s;
}

.user-info:hover {
  background-color: #f5f7fa;
}

.username {
  margin: 0 8px;
  font-size: 14px;
}

.arrow-down {
  font-size: 12px;
}

.main-wrapper {
  display: flex;
  flex: 1;
  overflow: hidden;
}

.sidebar {
  background-color: #fff;
  color: #303133;
  height: 100%;
  overflow-y: auto;
  transition: width 0.3s;
  border-right: 1px solid #e6e6e6;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.09);
}

.right-content {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
}

.sidebar-menu {
  border-right: none;
  background-color: transparent;
  height: 100%;
}

.sidebar-menu :deep(.el-menu-item) {
  color: #303133;
  height: 50px;
  line-height: 50px;
}

.sidebar-menu :deep(.el-menu-item .el-icon) {
  font-size: 18px;
  width: 20px;
  text-align: center;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(17, 119, 187, 0.1);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: rgba(17, 119, 187, 0.2);
  color: #1177BB;
}

/* 自定义图标样式，确保与Element Plus图标大小对齐 */
.custom-icon {
  font-size: 18px;
  display: inline-block;
  width: 20px;
  text-align: center;
}

.content {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background-color: #f5f7fa;
}
</style>