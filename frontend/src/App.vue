<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script setup>
import { onMounted } from 'vue'

// 设置HTML根节点字体大小（用于移动端适配）
function setRootFontSize() {
  const html = document.documentElement
  const width = html.clientWidth
  // 设计稿基准375px，根字体16px
  // 小于375时保持最小字体，大于768时用PC默认16px
  if (width < 375) {
    html.style.fontSize = '14px'
  } else if (width < 768) {
    html.style.fontSize = (width / 375 * 16) + 'px'
  } else {
    html.style.fontSize = '16px'
  }
}

onMounted(() => {
  setRootFontSize()
  window.addEventListener('resize', setRootFontSize)
})
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  background-color: #f5f7fa;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
}

#app {
  width: 100%;
  height: 100vh;
}
</style>