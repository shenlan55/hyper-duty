<template>
  <!-- ========================================= -->
  <!-- PC端布局（≥768px）- 原样保留 -->
  <!-- ========================================= -->
  <div v-if="!isMobile" class="layout-container">
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
              <el-dropdown-item @click="handlePasswordChange">
                <el-icon><Edit /></el-icon>
                <span>修改密码</span>
              </el-dropdown-item>
              <el-dropdown-item @click="handleLogout">
                <el-icon><SwitchButton /></el-icon>
                <span>退出登录</span>
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>

        <!-- 修改密码模态框 -->
        <el-dialog
          v-model="passwordDialogVisible"
          title="修改密码"
          width="400px"
        >
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
            </el-form-item>
            <el-form-item label="确认新密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
            </el-form-item>
          </el-form>
          <template #footer>
            <span class="dialog-footer">
              <el-button @click="passwordDialogVisible = false">取消</el-button>
              <el-button type="primary" @click="submitPasswordChange">确定</el-button>
            </span>
          </template>
        </el-dialog>
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
          <template v-if="leftMenus.length > 0">
            <el-menu-item
              v-for="menu in leftMenus"
              :key="menu.path"
              :index="menu.path"
            >
              <el-icon>
                <component :is="getMenuIcon(menu.icon)" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </el-menu-item>
          </template>
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
              <el-icon><component :is="getMenuIcon(menu.icon)" /></el-icon>
              <span>{{ menu.name }}</span>
            </el-menu-item>
          </el-menu>
        </div>

        <!-- 标签页 -->
        <div class="tab-container">
          <div class="tabs-wrapper">
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
            <div class="refresh-btn" @click="handleRefresh">
              <el-icon><Refresh /></el-icon>
            </div>
          </div>
        </div>

        <!-- 内容区域 -->
        <el-main class="content">
          <router-view v-if="$route.path === activeTab" />
        </el-main>
      </div>
    </div>
  </div>

  <!-- ========================================= -->
  <!-- 手机端布局（<768px） -->
  <!-- ========================================= -->
  <div v-else class="mobile-layout">
    <!-- 顶部导航栏 -->
    <div class="mobile-header">
      <div class="mobile-header-btn" @click="drawerVisible = true">
        <el-icon :size="22"><Menu /></el-icon>
      </div>
      <div class="mobile-header-title">Hyper Duty</div>
      <div class="mobile-header-btn" @click="handlePasswordChange">
        <el-avatar :size="28">{{ username[0] }}</el-avatar>
      </div>
    </div>

    <!-- 内容区域 -->
    <div class="mobile-content">
      <router-view />
    </div>

    <!-- 底部导航栏 -->
    <MobileBottomNav />

    <!-- 汉堡菜单抽屉 -->
    <MobileDrawer
      :visible="drawerVisible"
      :menus="topMenus"
      @close="drawerVisible = false"
      @navigate="handleMobileNavigate"
    />

    <!-- 修改密码弹窗 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="90%"
    >
      <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef">
        <el-form-item label="原密码" prop="oldPassword">
          <el-input v-model="passwordForm.oldPassword" type="password" placeholder="请输入原密码" />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="passwordForm.newPassword" type="password" placeholder="请输入新密码" />
        </el-form-item>
        <el-form-item label="确认新密码" prop="confirmPassword">
          <el-input v-model="passwordForm.confirmPassword" type="password" placeholder="请确认新密码" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="passwordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPasswordChange">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 退出登录确认 -->
    <el-dialog
      v-model="logoutDialogVisible"
      title="退出登录"
      width="90%"
    >
      <p style="text-align:center;padding:12px 0">确定要退出登录吗？</p>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="logoutDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="handleLogout">确定退出</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../stores/user'
import { useMenuStore } from '../stores/menu'
import { useResponsive } from '../hooks/useResponsive'
import { logout, changePassword } from '../api/auth'
import { ElMessage } from 'element-plus'
import { startAutoRefresh } from '../utils/request'
import MobileBottomNav from './MobileBottomNav.vue'
import MobileDrawer from './MobileDrawer.vue'
import {
  House, Setting, OfficeBuilding, UserFilled, User, Menu, ArrowDown,
  SwitchButton, HomeFilled, Operation, Edit, Delete, Plus, Check, Search,
  ArrowUp, ArrowLeft, ArrowRight, DocumentCopy, List, View, Calendar, Document,
  Avatar, WarningFilled, InfoFilled, SuccessFilled, QuestionFilled, StarFilled,
  Clock, CircleCheck, Refresh, DataAnalysis, Bell, Message, ChatDotRound, Phone,
  Location, Link, Star, Lock, Unlock, Warning, Close, ZoomIn, ZoomOut, FullScreen,
  Download, Upload, Sort, Filter, Share, Printer, Files, Folder, FolderOpened,
  Notebook, Briefcase
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const menuStore = useMenuStore()

// 设备检测
const { isMobile } = useResponsive()

// 用户信息
const username = computed(() => userStore.employeeName || userStore.username)

// 修改密码相关
const passwordDialogVisible = ref(false)
const passwordFormRef = ref(null)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})
const passwordRules = reactive({
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '新密码长度至少为6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
})

// 手机端状态
const drawerVisible = ref(false)
const logoutDialogVisible = ref(false)

// PC端：一级菜单
const topMenus = computed(() => menuStore.topMenus)
const routeNameMap = computed(() => menuStore.routeNameMap)

// PC端：当前激活的一级菜单
const activeTopMenu = ref('dashboard')

// PC端：动态二级菜单
const leftMenus = computed(() => {
  if (activeTopMenu.value === 'dashboard') {
    return [{ name: '首页', path: '/dashboard', icon: 'HomeFilled' }]
  }
  const menu = topMenus.value.find(m => m.id === activeTopMenu.value)
  return menu?.children || []
})

// PC端：当前激活菜单
const activeMenu = computed(() => route.path)

// PC端：标签页
const tabs = ref([])
const activeTab = ref('/dashboard')

// 图标映射
const iconMap = {
  'House': House, 'Setting': Setting, 'OfficeBuilding': OfficeBuilding,
  'UserFilled': UserFilled, 'User': User, 'Menu': Menu, 'HomeFilled': HomeFilled,
  'Operation': Operation, 'Edit': Edit, 'Delete': Delete, 'Plus': Plus,
  'Check': Check, 'Search': Search, 'ArrowUp': ArrowUp, 'ArrowDown': ArrowDown,
  'ArrowLeft': ArrowLeft, 'ArrowRight': ArrowRight, 'SwitchButton': SwitchButton,
  'DocumentCopy': DocumentCopy, 'List': List, 'View': View, 'Calendar': Calendar,
  'Document': Document, 'Avatar': Avatar, 'WarningFilled': WarningFilled,
  'InfoFilled': InfoFilled, 'SuccessFilled': SuccessFilled,
  'QuestionFilled': QuestionFilled, 'StarFilled': StarFilled, 'Clock': Clock,
  'CircleCheck': CircleCheck, 'Refresh': Refresh, 'DataAnalysis': DataAnalysis,
  'Bell': Bell, 'Message': Message, 'ChatDotRound': ChatDotRound,
  'Phone': Phone, 'Location': Location, 'Link': Link, 'Star': Star,
  'Lock': Lock, 'Unlock': Unlock, 'Warning': Warning, 'Close': Close,
  'ZoomIn': ZoomIn, 'ZoomOut': ZoomOut, 'FullScreen': FullScreen,
  'Download': Download, 'Upload': Upload, 'Sort': Sort, 'Filter': Filter,
  'Share': Share, 'Printer': Printer, 'Files': Files, 'Folder': Folder,
  'FolderOpened': FolderOpened, 'Notebook': Notebook, 'Briefcase': Briefcase
}

function getMenuIcon(iconName) {
  if (!iconName) return Menu
  return iconMap[iconName] || Menu
}

// PC端：标签页操作
function addTab(path) {
  if (!tabs.value.some(tab => tab.path === path)) {
    tabs.value.push({ path, name: routeNameMap.value[path] || path })
  }
  activeTab.value = path
}

function handleTabChange(path) {
  activeTab.value = path
  router.push(path)
}

function handleTabRemove(path) {
  const index = tabs.value.findIndex(tab => tab.path === path)
  if (index > -1) {
    tabs.value.splice(index, 1)
    if (activeTab.value === path) {
      const newActiveIndex = index > 0 ? index - 1 : 0
      if (tabs.value.length > 0) {
        activeTab.value = tabs.value[newActiveIndex].path
        router.push(activeTab.value)
      } else {
        activeTab.value = '/dashboard'
        router.push('/dashboard')
      }
    }
  }
}

// PC端：路由监听
watch(() => route.path, (newPath) => {
  if (isMobile.value) return // 手机端不需要标签页
  if (newPath === '/dashboard') {
    activeTopMenu.value = 'dashboard'
  } else {
    let found = false
    for (const menu of topMenus.value) {
      if (menu.path === newPath) { activeTopMenu.value = menu.id; found = true; break }
      for (const child of menu.children || []) {
        if (child.path === newPath) { activeTopMenu.value = menu.id; found = true; break }
      }
      if (found) break
    }
  }
  addTab(newPath)
})

// PC端：一级菜单切换
function handleTopMenuChange(index) {
  activeTopMenu.value = index
  const menu = topMenus.value.find(m => m.id === index)
  if (menu) {
    if (menu.children && menu.children.length > 0) {
      router.push(menu.children[0].path)
    } else {
      router.push(menu.path)
    }
  }
}

// PC端：刷新
function handleRefresh() {
  const currentPath = activeTab.value
  activeTab.value = ''
  setTimeout(() => { activeTab.value = currentPath }, 0)
}

// 修改密码
function handlePasswordChange() { passwordDialogVisible.value = true }

async function submitPasswordChange() {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    const response = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (response.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    } else {
      ElMessage.error(response.message || '密码修改失败')
    }
  } catch (error) {
    if (error.response && error.response.data && error.response.data.message) {
      ElMessage.error(error.response.data.message || '密码修改失败')
    } else {
      ElMessage.error('网络错误，请稍后重试')
    }
  }
}

// 退出登录
async function handleLogout() {
  try {
    await logout()
  } catch (e) { /* ignore */ }
  userStore.logout()
  router.push('/login')
  ElMessage.success('退出登录成功')
}

// 手机端：菜单抽屉导航
function handleMobileNavigate(path) {
  router.push(path)
}

// 初始化
onMounted(async () => {
  await menuStore.refreshMenus()

  if (isMobile.value) {
    // 手机端：不需要标签页初始化
    return
  }

  activeTopMenu.value = 'dashboard'
  if (route.path === '/dashboard') {
    tabs.value = [{ path: '/dashboard', name: '首页' }]
    activeTab.value = '/dashboard'
  } else {
    addTab(route.path)
  }

  const token = localStorage.getItem('token')
  const refreshTokenStr = localStorage.getItem('refreshToken')
  if (token && refreshTokenStr) {
    startAutoRefresh()
  }
})
</script>

<style scoped>
/* ======================================== */
/* PC端样式 - 原样保留 */
/* ======================================== */

.layout-container {
  display: flex;
  flex-direction: column;
  height: 100vh;
  overflow: hidden;
}

.header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.09);
  padding: 0 20px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.header-left { display: flex; align-items: center; gap: 20px; }
.logo h1 { margin: 0; font-size: 20px; color: #1890ff; font-weight: bold; }

.top-menu-bar {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.09);
  height: 60px;
}
.top-menu { border-bottom: none; height: 60px; line-height: 60px; }
.top-menu :deep(.el-menu-item) { height: 60px; line-height: 60px; font-size: 14px; }

.tab-container {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  box-shadow: 0 2px 8px rgba(0,0,0,0.09);
  height: 40px;
}

.tabs-wrapper {
  display: flex; align-items: center; height: 100%; padding: 0 10px;
}
.tabs-wrapper :deep(.el-tabs) { flex: 1; }

.refresh-btn {
  margin-left: 10px; padding: 0 10px; height: 32px; line-height: 32px;
  cursor: pointer; border-radius: 4px; transition: all 0.3s;
  display: flex; align-items: center; justify-content: center;
}
.refresh-btn:hover { background-color: #f5f7fa; color: #1890ff; }

.tab-container :deep(.el-tabs__header) { margin: 0; padding: 0; border-bottom: none; height: 40px; line-height: 40px; }
.tab-container :deep(.el-tabs__nav-wrap) { margin: 0; height: 40px; }
.tab-container :deep(.el-tabs__nav-scroll) { height: 40px; }
.tab-container :deep(.el-tabs__item) { height: 40px; line-height: 40px; font-size: 14px; }
.tab-container :deep(.el-tabs__nav) { height: 40px; }
.tab-container :deep(.el-tabs__active-bar) { bottom: 0; }

.header-right { display: flex; align-items: center; }
.user-info {
  display: flex; align-items: center; cursor: pointer;
  padding: 0 10px; height: 40px; border-radius: 4px;
  transition: background-color 0.3s;
}
.user-info:hover { background-color: #f5f7fa; }
.username { margin: 0 8px; font-size: 14px; }
.arrow-down { font-size: 12px; }

.main-wrapper { display: flex; flex: 1; overflow: hidden; }

.sidebar {
  background-color: #fff; color: #303133; height: 100%; overflow-y: auto;
  transition: width 0.3s; border-right: 1px solid #e6e6e6;
  box-shadow: 2px 0 8px rgba(0,0,0,0.09);
}

.right-content { display: flex; flex-direction: column; flex: 1; overflow: hidden; }

.sidebar-menu { border-right: none; background-color: transparent; height: 100%; }
.sidebar-menu :deep(.el-menu-item) { height: 50px; line-height: 50px; }
.sidebar-menu :deep(.el-menu-item .el-icon) { font-size: 18px; width: 20px; text-align: center; }

.content {
  flex: 1; padding: 10px; overflow-y: auto; background-color: #f5f7fa;
}

/* ======================================== */
/* 手机端样式 */
/* ======================================== */
.mobile-layout {
  display: flex;
  flex-direction: column;
  height: 100dvh;
  overflow: hidden;
  background: #f5f7fa;
}

.mobile-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 48px;
  padding: 0 12px;
  background: #fff;
  border-bottom: 1px solid #eee;
  flex-shrink: 0;
  z-index: 100;
}

.mobile-header-btn {
  width: 36px;
  height: 36px;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  border-radius: 50%;
  transition: background 0.2s;
}
.mobile-header-btn:active { background: #f0f0f0; }

.mobile-header-title {
  font-size: 16px;
  font-weight: 600;
  color: #1890ff;
}

.mobile-content {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
  padding-bottom: 56px; /* 底部导航高度 */
}
</style>