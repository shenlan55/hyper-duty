/**
 * BPMN 工具方法集合（P3-1）
 * ----------------------------------------------------------------------------
 * - renderBpmnToSvg(xml): 把 BPMN XML 渲染为 SVG 字符串（用于模板预览）
 * - 用 bpmn-js 的 Viewer 模式（不引入 Modeler，体积更小）
 * ----------------------------------------------------------------------------
 */
import BpmnViewer from 'bpmn-js/lib/NavigatedViewer'
import 'bpmn-js/dist/assets/diagram-js.css'
import 'bpmn-js/dist/assets/bpmn-js.css'

let _viewer = null
const _ensureViewer = () => {
  if (_viewer) return _viewer
  // 用隐藏 div 挂载 viewer（必须有容器）
  const container = document.createElement('div')
  container.style.position = 'absolute'
  container.style.left = '-99999px'
  container.style.top = '-99999px'
  container.style.width = '800px'
  container.style.height = '600px'
  document.body.appendChild(container)
  _viewer = new BpmnViewer({ container })
  return _viewer
}

/**
 * 把 BPMN XML 渲染成 SVG 字符串
 * @param {string} xml
 * @returns {Promise<string>} svg 字符串
 */
export const renderBpmnToSvg = async (xml) => {
  if (!xml) return ''
  const viewer = _ensureViewer()
  try {
    const result = await viewer.importXML(xml)
    const { svg } = await viewer.saveSVG({ format: true })
    return svg
  } catch (e) {
    console.error('[bpmn-helper] renderBpmnToSvg failed', e)
    return `<div style="color:#f56c6c;padding:20px;">BPMN 解析失败：${e.message || e}</div>`
  }
}

export default { renderBpmnToSvg }
