<template>
  <div class="rich-text-editor">
    <div ref="editorRef" class="editor-container"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch, nextTick } from 'vue'
import Quill from 'quill'
import 'quill/dist/quill.snow.css'

const props = defineProps({
  modelValue: {
    type: String,
    default: ''
  },
  placeholder: {
    type: String,
    default: '请输入内容'
  }
})

const emit = defineEmits(['update:modelValue'])

const editorRef = ref(null)
let quill = null

onMounted(() => {
  nextTick(() => {
    // 初始化 Quill 编辑器
    quill = new Quill(editorRef.value, {
      theme: 'snow',
      placeholder: props.placeholder,
      modules: {
        toolbar: [
          ['bold', 'italic', 'underline', 'strike'],        // 加粗，斜体，下划线，删除线
          ['blockquote', 'code-block'],                    // 引用，代码块
          [{ 'header': 1 }, { 'header': 2 }],               // 标题，键值对形式
          [{ 'list': 'ordered' }, { 'list': 'bullet' }],    // 列表
          [{ 'script': 'sub' }, { 'script': 'super' }],     // 上标，下标
          [{ 'indent': '-1' }, { 'indent': '+1' }],         // 缩进
          [{ 'direction': 'rtl' }],                         // 文本方向
          [{ 'size': ['small', false, 'large', 'huge'] }],  // 字体大小
          [{ 'header': [1, 2, 3, 4, 5, 6, false] }],        // 标题
          [{ 'color': [] }, { 'background': [] }],          // 颜色选择
          [{ 'font': [] }],                                 // 字体
          [{ 'align': [] }],                                // 对齐方式
          ['clean']                                         // 清除格式
        ]
      }
    })

    // 设置初始内容
    if (props.modelValue) {
      quill.root.innerHTML = props.modelValue
    }

    // 监听内容变化
    quill.on('text-change', () => {
      const content = quill.root.innerHTML
      emit('update:modelValue', content)
    })
  })
})

// 监听外部数据变化
watch(
  () => props.modelValue,
  (newValue) => {
    if (quill && newValue !== quill.root.innerHTML) {
      quill.root.innerHTML = newValue || ''
    }
  }
)

// 暴露方法
defineExpose({
  getEditor: () => quill,
  focus: () => quill?.focus()
})
</script>

<style scoped>
.rich-text-editor {
  width: 100%;
}

.editor-container {
  min-height: 200px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

/* 确保编辑器中的图片自适应容器宽度 */
:deep(.ql-editor) {
  min-height: 200px;
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}

/* 确保编辑器中的图片自适应容器宽度 */
:deep(.ql-editor img) {
  max-width: 100% !important;
  height: auto !important;
  display: block !important;
  margin: 0 auto !important;
  box-sizing: border-box !important;
}

/* 确保编辑器容器宽度自适应 */
.rich-text-editor {
  width: 100%;
  box-sizing: border-box;
}

.editor-container {
  width: 100%;
  box-sizing: border-box;
  overflow-x: hidden;
}
</style>
