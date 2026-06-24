<template>
  <div class="bpmn-viewer-container">
    <div id="bpmn-viewer" ref="bpmnViewer"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import BpmnViewer from 'bpmn-js/lib/Viewer'

const props = defineProps({
  bpmnXml: {
    type: String,
    default: ''
  },
  // 已完成的活动节点ID列表
  completedActivityIds: {
    type: Array,
    default: () => []
  },
  // 当前活动的节点ID列表
  currentActivityIds: {
    type: Array,
    default: () => []
  },
  // 已结束/跳过节点（灰色）
  terminatedActivityIds: {
    type: Array,
    default: () => []
  }
})

const bpmnViewer = ref(null)
let viewer = null

// 初始化 Viewer
const initViewer = () => {
  viewer = new BpmnViewer({
    container: bpmnViewer.value
  })
}

// 渲染 BPMN 并高亮节点
const renderBpmn = async () => {
  if (!props.bpmnXml || !viewer) return

  try {
    await viewer.importXML(props.bpmnXml)
    const canvas = viewer.get('canvas')
    canvas.zoom('fit-viewport')
    
    // 高亮已完成的节点
    props.completedActivityIds.forEach(id => {
      try {
        canvas.addMarker(id, 'highlight-completed')
      } catch (e) {
        console.debug('Node not found:', id)
      }
    })

    // 高亮当前节点
    props.currentActivityIds.forEach(id => {
      try {
        canvas.addMarker(id, 'highlight-current')
      } catch (e) {
        console.debug('Node not found:', id)
      }
    })

    // 高亮已结束/跳过节点
    props.terminatedActivityIds.forEach(id => {
      try {
        canvas.addMarker(id, 'highlight-terminated')
      } catch (e) {
        console.debug('Node not found:', id)
      }
    })
  } catch (err) {
    console.error('Render BPMN failed:', err)
  }
}

// 监听 bpmnXml 变化
watch(() => props.bpmnXml, () => {
  renderBpmn()
})

// 监听高亮节点变化
watch(
  [() => props.completedActivityIds, () => props.currentActivityIds, () => props.terminatedActivityIds],
  () => {
    renderBpmn()
  }
)

onMounted(() => {
  initViewer()
  renderBpmn()
})
</script>

<style scoped>
.bpmn-viewer-container {
  width: 100%;
  height: 500px;
  border: 1px solid #e4e7ed;
  border-radius: 4px;
}

#bpmn-viewer {
  width: 100%;
  height: 100%;
}
</style>

<style>
/* 已完成节点高亮 - 绿色 */
.highlight-completed .djs-visual > :nth-child(1) {
  fill: #f0f9eb !important;
  stroke: #67c23a !important;
  stroke-width: 2px !important;
}

/* 当前节点高亮 - 红色 */
.highlight-current .djs-visual > :nth-child(1) {
  fill: #fef0f0 !important;
  stroke: #f56c6c !important;
  stroke-width: 2px !important;
}

/* 已结束/跳过节点 - 灰色 */
.highlight-terminated .djs-visual > :nth-child(1) {
  fill: #f4f4f5 !important;
  stroke: #909399 !important;
  stroke-width: 1.5px !important;
  opacity: 0.6;
}
</style>
