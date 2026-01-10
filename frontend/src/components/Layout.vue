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
          :background-color="'#001529'"
          :text-color="'rgba(255, 255, 255, 0.8)'"
          :active-text-color="'#fff'"
        >
          <!-- 动态二级菜单 -->
          <template v-if="leftMenus.length > 0">
            <el-menu-item
              v-for="menu in leftMenus"
              :key="menu.path"
              :index="menu.path"
            >
              <template #icon>
                <el-icon><component :is="menu.icon" /></el-icon>
              </template>
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
            router
            @select="handleTopMenuChange"
          >
            <el-menu-item index="dashboard">
              <template #icon>
                <el-icon><House /></el-icon>
              </template>
              <span>首页</span>
            </el-menu-item>
            <el-menu-item index="system">
              <template #icon>
                <el-icon><Setting /></el-icon>
              </template>
              <span>系统管理</span>
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
          <keep-alive>
            <router-view v-if="$route.path === activeTab" />
          </keep-alive>
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
import { ElMessage } from 'element-plus'
import { 
  House, Setting, OfficeBuilding, UserFilled, Menu, ArrowDown, 
  SwitchButton 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const collapsed = ref(false)

const username = computed(() => userStore.username)

// 顶部一级菜单数据
const topMenus = [
  {
    id: 'dashboard',
    name: '首页',
    path: '/dashboard',
    children: []
  },
  {
    id: 'system',
    name: '系统管理',
    children: [
      {
        name: '部门管理',
        path: '/dept',
        icon: OfficeBuilding
      },
      {
        name: '人员管理',
        path: '/employee',
        icon: UserFilled
      }
    ]
  }
]

// 当前激活的一级菜单
const activeTopMenu = ref('dashboard')

// 动态二级菜单
const leftMenus = computed(() => {
  const menu = topMenus.find(m => m.id === activeTopMenu.value)
  return menu?.children || []
})

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 标签页相关状态
const tabs = ref([])
const activeTab = ref('/dashboard')

// 路由名称映射
const routeNameMap = {
  '/dashboard': '首页',
  '/dept': '部门管理',
  '/employee': '人员管理'
}

// 添加标签页
const addTab = (path) => {
  if (!tabs.value.some(tab => tab.path === path)) {
    tabs.value.push({
      path,
      name: routeNameMap[path] || path
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
  if (newPath === '/dashboard') {
    activeTopMenu.value = 'dashboard'
  } else if (newPath.startsWith('/dept') || newPath.startsWith('/employee')) {
    activeTopMenu.value = 'system'
  }
  // 添加标签页
  addTab(newPath)
})

// 切换一级菜单
const handleTopMenuChange = (index) => {
  activeTopMenu.value = index
  // 如果是首页，直接跳转
  if (index === 'dashboard') {
    router.push('/dashboard')
  } else if (index === 'system' && leftMenus.value.length > 0) {
    // 否则跳转到第一个二级菜单
    router.push(leftMenus.value[0].path)
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

// 初始化标签页
onMounted(() => {
  addTab(route.path)
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
  background-color: #001529;
  color: #fff;
  height: 100%;
  overflow-y: auto;
  transition: width 0.3s;
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
  color: rgba(255, 255, 255, 0.8);
  height: 50px;
  line-height: 50px;
}

.sidebar-menu :deep(.el-menu-item:hover) {
  background-color: rgba(255, 255, 255, 0.1);
}

.sidebar-menu :deep(.el-menu-item.is-active) {
  background-color: #1890ff;
  color: #fff;
}

.content {
  flex: 1;
  padding: 10px;
  overflow-y: auto;
  background-color: #f5f7fa;
}
</style>