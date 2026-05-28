// ==========================================
// 响应式设备检测 Hook
// 提供设备类型判断、断点检测等能力
// ==========================================

import { ref, onMounted, onUnmounted } from 'vue'

const BREAKPOINT_SM = 768
const BREAKPOINT_MD = 1024

// 全局共享状态，避免每个组件都创建监听
const windowWidth = ref(window.innerWidth)
const isMobile = ref(window.innerWidth < BREAKPOINT_SM)
const isTablet = ref(window.innerWidth >= BREAKPOINT_SM && window.innerWidth < BREAKPOINT_MD)
const isDesktop = ref(window.innerWidth >= BREAKPOINT_MD)

let listenerCount = 0
let resizeHandler = null

/**
 * 响应式设备检测
 * @returns {{ isMobile, isTablet, isDesktop, windowWidth }}
 */
export function useResponsive() {
  onMounted(() => {
    if (!resizeHandler) {
      resizeHandler = () => {
        const w = window.innerWidth
        windowWidth.value = w
        isMobile.value = w < BREAKPOINT_SM
        isTablet.value = w >= BREAKPOINT_SM && w < BREAKPOINT_MD
        isDesktop.value = w >= BREAKPOINT_MD
      }
      window.addEventListener('resize', resizeHandler)
    }
    listenerCount++
  })

  onUnmounted(() => {
    listenerCount--
    if (listenerCount <= 0 && resizeHandler) {
      window.removeEventListener('resize', resizeHandler)
      resizeHandler = null
    }
  })

  return { isMobile, isTablet, isDesktop, windowWidth }
}

/**
 * 判断当前是否为移动端（非响应式，仅读取当前状态）
 */
export function checkIsMobile() {
  return window.innerWidth < BREAKPOINT_SM
}