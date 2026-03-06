<template>
  <el-dialog
    v-model="dialogVisible"
    :title="title"
    :width="width"
    :fullscreen="fullscreen"
    :append-to-body="appendToBody"
    :close-on-click-modal="closeOnClickModal"
    :close-on-press-escape="closeOnPressEscape"
    @close="handleClose"
  >
    <!-- 弹窗内容插槽 -->
    <slot></slot>
    
    <!-- 弹窗底部插槽 -->
    <template #footer v-if="showFooter">
      <slot name="footer">
        <el-button @click="handleClose">取消</el-button>
        <el-button type="primary" @click="handleConfirm" :loading="confirmLoading">确定</el-button>
      </slot>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, computed, watch } from 'vue'

const props = defineProps({
  // 弹窗是否可见
  visible: {
    type: Boolean,
    default: false
  },
  // 弹窗标题
  title: {
    type: String,
    default: ''
  },
  // 弹窗宽度
  width: {
    type: String,
    default: '800px'
  },
  // 是否全屏
  fullscreen: {
    type: Boolean,
    default: false
  },
  // 是否将弹窗挂载到body上
  appendToBody: {
    type: Boolean,
    default: false
  },
  // 点击模态框是否关闭
  closeOnClickModal: {
    type: Boolean,
    default: true
  },
  // 按ESC是否关闭
  closeOnPressEscape: {
    type: Boolean,
    default: true
  },
  // 是否显示底部按钮
  showFooter: {
    type: Boolean,
    default: true
  },
  // 确定按钮加载状态
  confirmLoading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['update:visible', 'close', 'confirm'])

// 弹窗可见性
const dialogVisible = computed({
  get: () => props.visible,
  set: (value) => emit('update:visible', value)
})

// 处理关闭
const handleClose = () => {
  emit('close')
  dialogVisible.value = false
}

// 处理确定
const handleConfirm = () => {
  emit('confirm')
}
</script>

<style scoped>
/* 通用弹窗样式 */
:deep(.el-dialog__body) {
  overflow-x: hidden !important;
  padding: 20px;
}
</style>