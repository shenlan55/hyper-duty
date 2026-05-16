<template>
  <div class="bpmn-designer">
    <div class="toolbar">
      <el-button size="small" @click="save">保存</el-button>
      <el-button size="small" @click="exportXml">导出XML</el-button>
      <el-button size="small" @click="importXml">导入XML</el-button>
      <input
        type="file"
        ref="fileInput"
        style="display: none"
        accept=".bpmn,.xml"
        @change="handleFileImport"
      />
      <el-button size="small" @click="zoomIn">放大</el-button>
      <el-button size="small" @click="zoomOut">缩小</el-button>
      <el-button size="small" @click="zoomReset">重置</el-button>
    </div>
    <div class="canvas" ref="canvas"></div>
  </div>
</template>

<script>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import BpmnModeler from 'bpmn-js/lib/Modeler'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'
import 'bpmn-js/dist/assets/bpmn-font/css/bpmn.css'
import chineseTranslation from 'bpmn-js-i18n/translations/zn'

export default {
  name: 'BpmnDesigner',
  props: {
    xml: {
      type: String,
      default: ''
    }
  },
  emits: ['save', 'change'],
  setup(props, { emit }) {
    const canvas = ref(null)
    const fileInput = ref(null)
    const modeler = ref(null)

    // 自定义中文模块
    const customTranslate = {
      translate: ['value', function (template, replacements) {
        replacements = replacements || {}
        
        // 使用 bpmn-js-i18n 的翻译
        let translation = chineseTranslation[template]
        
        // 如果没有找到翻译，就返回英文原文
        if (!translation) {
          translation = template
        }
        
        // 替换占位符 {type}, {element} 等
        return translation.replace(/{([^}]+)}/g, function(_, key) {
          return replacements[key] || '{' + key + '}'
        })
      }]
    }

    // 初始化BpmnModeler
    const initModeler = () => {
      modeler.value = new BpmnModeler({
        container: canvas.value,
        keyboard: {
          bindTo: window
        },
        additionalModules: [
          customTranslate
        ]
      })

      modeler.value.on('commandStack.changed', () => {
        modeler.value.saveXML({ format: true }).then((result) => {
          emit('change', result.xml)
        })
      })

      importBpmn(props.xml)
    }

    // 导入BPMN
    const importBpmn = async (xml) => {
      if (!modeler.value) return
      
      let bpmnXml = xml
      if (!bpmnXml) {
        bpmnXml = `<?xml version="1.0" encoding="UTF-8"?>
<definitions xmlns="http://www.omg.org/spec/BPMN/20100524/MODEL"
             xmlns:bpmndi="http://www.omg.org/spec/BPMN/20100524/DI"
             xmlns:dc="http://www.omg.org/spec/DD/20100524/DC"
             xmlns:di="http://www.omg.org/spec/DD/20100524/DI"
             id="Definitions_1"
             targetNamespace="http://bpmn.io/schema/bpmn">
  <process id="Process_1" isExecutable="true">
    <startEvent id="StartEvent_1" />
  </process>
  <bpmndi:BPMNDiagram id="BPMNDiagram_1">
    <bpmndi:BPMNPlane id="BPMNPlane_1" bpmnElement="Process_1">
      <bpmndi:BPMNShape id="StartEvent_1_di" bpmnElement="StartEvent_1">
        <dc:Bounds x="179" y="159" width="36" height="36" />
      </bpmndi:BPMNShape>
    </bpmndi:BPMNPlane>
  </bpmndi:BPMNDiagram>
</definitions>`
      }

      try {
        await modeler.value.importXML(bpmnXml)
        const canvas = modeler.value.get('canvas')
        canvas.zoom('fit-viewport')
      } catch (error) {
        console.error('导入BPMN失败', error)
      }
    }

    // 保存
    const save = async () => {
      try {
        const result = await modeler.value.saveXML({ format: true })
        emit('save', result.xml)
      } catch (error) {
        console.error('保存失败', error)
      }
    }

    // 导出XML
    const exportXml = async () => {
      try {
        const result = await modeler.value.saveXML({ format: true })
        const blob = new Blob([result.xml], { type: 'text/xml' })
        const url = window.URL.createObjectURL(blob)
        const a = document.createElement('a')
        a.href = url
        a.download = 'process.bpmn'
        document.body.appendChild(a)
        a.click()
        document.body.removeChild(a)
        window.URL.revokeObjectURL(url)
      } catch (error) {
        console.error('导出失败', error)
      }
    }

    // 导入XML
    const importXml = () => {
      fileInput.value.click()
    }

    // 处理文件导入
    const handleFileImport = (event) => {
      const file = event.target.files[0]
      if (!file) return

      const reader = new FileReader()
      reader.onload = async (e) => {
        const xml = e.target.result
        await importBpmn(xml)
      }
      reader.readAsText(file)
    }

    // 放大
    const zoomIn = () => {
      const canvas = modeler.value.get('canvas')
      const currentZoom = canvas.zoom()
      canvas.zoom(currentZoom * 1.1)
    }

    // 缩小
    const zoomOut = () => {
      const canvas = modeler.value.get('canvas')
      const currentZoom = canvas.zoom()
      canvas.zoom(currentZoom * 0.9)
    }

    // 重置
    const zoomReset = () => {
      const canvas = modeler.value.get('canvas')
      canvas.zoom('fit-viewport')
    }

    // 监听 xml prop 变化
    watch(() => props.xml, (newXml) => {
      if (newXml) {
        importBpmn(newXml)
      }
    })

    onMounted(() => {
      initModeler()
    })

    onBeforeUnmount(() => {
      if (modeler.value) {
        modeler.value.destroy()
      }
    })

    return {
      canvas,
      fileInput,
      save,
      exportXml,
      importXml,
      handleFileImport,
      zoomIn,
      zoomOut,
      zoomReset
    }
  }
}
</script>

<style scoped>
.bpmn-designer {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.toolbar {
  padding: 10px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.canvas {
  flex: 1;
  min-height: 500px;
  background: #f5f5f5;
}
</style>
