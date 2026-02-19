<template>
  <div class="virtual-list" :style="{ height: height }">
    <el-virtual-scroll
      :data-key="dataKey"
      :data-sources="data"
      :data-component="itemComponent"
      :estimate-size="estimateSize"
      @scroll="handleScroll"
    >
      <template #default="{ item }">
        <slot :item="item">{{ item }}</slot>
      </template>
    </el-virtual-scroll>
  </div>
</template>

<script setup>
// Props
const props = defineProps({
  // 数据列表
  data: {
    type: Array,
    default: () => []
  },
  // 数据唯一键
  dataKey: {
    type: String,
    default: 'id'
  },
  // 估计高度
  estimateSize: {
    type: Number,
    default: 50
  },
  // 容器高度
  height: {
    type: String,
    default: '500px'
  },
  // 项目组件
  itemComponent: {
    type: Object,
    default: null
  }
})

// Emits
const emit = defineEmits(['scroll'])

// 处理滚动事件
const handleScroll = (event) => {
  emit('scroll', event)
}
</script>

<style scoped>
.virtual-list {
  overflow: auto;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}
</style>