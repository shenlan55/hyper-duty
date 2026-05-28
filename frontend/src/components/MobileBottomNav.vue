<template>
  <div class="mobile-bottom-nav">
    <div
      v-for="tab in tabs"
      :key="tab.path"
      class="nav-item"
      :class="{ active: isActive(tab.path) }"
      @click="navigate(tab.path)"
    >
      <el-icon class="nav-icon"><component :is="tab.icon" /></el-icon>
      <span class="nav-label">{{ tab.label }}</span>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { HomeFilled, Clock, List, CircleCheck } from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()

// 底部Tab配置
const tabs = [
  { path: '/dashboard', label: '首页', icon: HomeFilled },
  { path: '/duty/assignment', label: '值班', icon: Clock },
  { path: '/project/my-task', label: '任务', icon: List },
  { path: '/workflow/todo-task', label: '待办', icon: CircleCheck }
]

function isActive(path) {
  if (path === '/dashboard') {
    return route.path === '/dashboard'
  }
  // 匹配模块前缀
  const prefix = path.split('/').slice(0, 2).join('/')
  return route.path.startsWith(prefix)
}

function navigate(path) {
  router.push(path)
}
</script>

<style scoped>
.mobile-bottom-nav {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 56px;
  background: #fff;
  border-top: 1px solid #eee;
  display: flex;
  z-index: 1000;
  padding-bottom: env(safe-area-inset-bottom, 0);
}

.nav-item {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
  transition: color 0.2s;
  cursor: pointer;
  -webkit-tap-highlight-color: transparent;
  user-select: none;
}

.nav-item.active {
  color: #409EFF;
}

.nav-icon {
  font-size: 22px;
  margin-bottom: 2px;
}

.nav-label {
  font-size: 11px;
  line-height: 1;
}
</style>