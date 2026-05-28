<template>
  <teleport to="body">
    <transition name="drawer">
      <div v-if="visible" class="mobile-drawer-overlay" @click.self="$emit('close')">
        <div class="mobile-drawer">
          <div class="drawer-header">
            <span class="drawer-title">菜单导航</span>
            <el-icon class="drawer-close" @click="$emit('close')"><Close /></el-icon>
          </div>
          <div class="drawer-body">
            <el-menu
              :default-active="activeMenu"
              class="drawer-menu"
              @select="handleSelect"
            >
              <template v-for="menu in menus" :key="menu.id">
                <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.id">
                  <template #title>
                    <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
                    <span>{{ menu.name }}</span>
                  </template>
                  <el-menu-item
                    v-for="child in menu.children"
                    :key="child.path"
                    :index="child.path"
                  >
                    {{ child.name }}
                  </el-menu-item>
                </el-sub-menu>

                <el-menu-item v-else :index="menu.path">
                  <el-icon><component :is="getIcon(menu.icon)" /></el-icon>
                  <span>{{ menu.name }}</span>
                </el-menu-item>
              </template>
            </el-menu>
          </div>
        </div>
      </div>
    </transition>
  </teleport>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { HomeFilled, Close, Menu } from '@element-plus/icons-vue'

const props = defineProps({
  visible: { type: Boolean, default: false },
  menus: { type: Array, default: () => [] }
})

const emit = defineEmits(['close', 'navigate'])

const route = useRoute()

const activeMenu = computed(() => route.path)

function handleSelect(index) {
  emit('navigate', index)
  emit('close')
}

// 图标映射（精简版，匹配菜单icon名称）
const iconMap = {
  'House': HomeFilled,
  'Setting': Menu,
  'HomeFilled': HomeFilled,
}
function getIcon(iconName) {
  return iconMap[iconName] || Menu
}
</script>

<style scoped>
.mobile-drawer-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 2000;
}

.mobile-drawer {
  position: absolute;
  top: 0;
  left: 0;
  bottom: 0;
  width: 280px;
  max-width: 80vw;
  background: #fff;
  display: flex;
  flex-direction: column;
}

.drawer-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  border-bottom: 1px solid #f0f0f0;
}

.drawer-title {
  font-size: 16px;
  font-weight: 600;
}

.drawer-close {
  font-size: 20px;
  cursor: pointer;
  padding: 4px;
}

.drawer-body {
  flex: 1;
  overflow-y: auto;
  -webkit-overflow-scrolling: touch;
}

.drawer-menu {
  border-right: none;
}

/* 过渡动画 */
.drawer-enter-active,
.drawer-leave-active {
  transition: all 0.25s ease;
}
.drawer-enter-active .mobile-drawer,
.drawer-leave-active .mobile-drawer {
  transition: transform 0.25s ease;
}

.drawer-enter-from { opacity: 0; }
.drawer-leave-to { opacity: 0; }
.drawer-enter-from .mobile-drawer { transform: translateX(-100%); }
.drawer-leave-to .mobile-drawer { transform: translateX(-100%); }
</style>