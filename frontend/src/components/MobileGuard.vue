<template>
  <div v-if="isMobile" class="mobile-guard">
    <el-result icon="warning" title="页面仅支持PC端访问" sub-title="该功能较为复杂，建议在PC端浏览器打开以获得完整体验">
      <template #extra>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </template>
    </el-result>
  </div>
  <slot v-else />
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const isMobile = ref(false)

function checkMobile() {
  isMobile.value = window.innerWidth < 768
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

function goHome() {
  router.push('/dashboard')
}
</script>

<style scoped>
.mobile-guard {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 60vh;
  padding: 20px;
}

.mobile-guard :deep(.el-result__title) {
  font-size: 16px;
}

.mobile-guard :deep(.el-result__subtitle) {
  font-size: 13px;
  margin-top: 8px;
}
</style>