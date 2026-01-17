<template>
  <div class="duty-layout">
    <router-view />
  </div>
</template>

<script setup>
// 移除了内部标签页系统，直接使用路由视图
// 标签页管理由Layout.vue全局处理
</script>

<style scoped>
.duty-layout {
  padding: 20px;
}
</style>