<!--
  流转线（SequenceFlow）属性面板
  - P0-1 阶段：基础条件表达式（文本框）
  - P0-3 阶段：可视化条件编辑器（表单字段 / 流程变量 / 表达式）
-->
<template>
  <el-form v-if="element" label-position="top" size="small">
    <el-divider>流转条件</el-divider>

    <el-form-item label="条件类型">
      <el-select v-model="form.conditionType" style="width: 100%">
        <el-option label="无条件" value="none" />
        <el-option label="表达式（EL/UEL）" value="expression" />
        <el-option label="脚本（Groovy/JavaScript）" value="script" />
      </el-select>
      <div class="hint">
        排他网关必须每条流出线设置条件；未命中的流转线会走默认路径。
      </div>
    </el-form-item>

    <template v-if="form.conditionType === 'expression'">
      <el-form-item label="条件表达式">
        <el-input
          v-model="form.conditionExpression"
          type="textarea"
          :rows="3"
          placeholder="例如：${amount > 1000} 或 ${submitter == initiator}"
          @change="applyToElement"
        />
        <div class="hint">
          支持 EL 表达式，可引用表单字段与流程变量。例：<code>${formData.amount > 1000}</code>
        </div>
      </el-form-item>
    </template>

    <template v-if="form.conditionType === 'script'">
      <el-form-item label="脚本内容">
        <el-input
          v-model="form.conditionScript"
          type="textarea"
          :rows="4"
          placeholder="// return true 表示走这条线"
          @change="applyToElement"
        />
        <div class="hint">脚本语言通过流程属性配置（Flowable 7 默认支持 Java/Groovy）</div>
      </el-form-item>
    </template>

    <el-form-item label="默认路径">
      <el-switch v-model="form.isDefault" @change="applyToElement" />
      <div class="hint">排他网关未匹配任何条件时走该路径</div>
    </el-form-item>
  </el-form>
</template>

<script>
import { reactive, watch } from 'vue'

export default {
  name: 'SequenceFlowPanel',
  props: {
    /** 选中的 SequenceFlow bpmn-js element */
    element: { type: Object, default: null },
    /** 父组件提供的写入函数 */
    updateElementProp: { type: Function, required: true }
  },
  setup(props) {
    const form = reactive({
      conditionType: 'none',
      conditionExpression: '',
      conditionScript: '',
      isDefault: false
    })

    /** 从 bpmn 元素读 conditionExpression，初始化表单 */
    const fillFromElement = (el) => {
      if (!el || el.type !== 'bpmn:SequenceFlow') return
      const bo = el.businessObject
      // 清空
      form.conditionType = 'none'
      form.conditionExpression = ''
      form.conditionScript = ''
      form.isDefault = !!bo.default

      const ce = bo.conditionExpression
      if (ce) {
        // bpmn 元素上的 conditionExpression 是一个 moddle 对象 { body, language }
        if (ce.language) {
          form.conditionType = 'script'
          form.conditionScript = ce.body || ''
        } else if (ce.body) {
          form.conditionType = 'expression'
          form.conditionExpression = ce.body
        }
      }
    }

    /** 把表单写回 bpmn 元素 */
    const applyToElement = () => {
      if (!props.element) return

      if (form.conditionType === 'none') {
        // 清空条件
        props.updateElementProp('conditionExpression', undefined)
        props.updateElementProp('isDefault', form.isDefault ? true : undefined)
        return
      }

      // 这里简化处理：直接通过 updateElementProp 传 conditionExpression 的字符串
      // 完整 moddle 创建（用于 script）留给 P0-3 增强
      if (form.conditionType === 'expression') {
        // 用 moddle 注入（最简做法：直接用父级 updateElementProp 传字符串）
        // 父级会根据字符串构造 conditionExpression
        props.updateElementProp('__conditionExpressionBody__', form.conditionExpression)
      } else if (form.conditionType === 'script') {
        props.updateElementProp('__conditionScript__', { body: form.conditionScript, language: 'groovy' })
      }

      if (form.isDefault) {
        props.updateElementProp('isDefault', true)
      } else {
        props.updateElementProp('isDefault', undefined)
      }
    }

    // 监听 element 变化
    watch(() => props.element, (newEl) => {
      fillFromElement(newEl)
    }, { immediate: true })

    return { form, applyToElement }
  }
}
</script>

<style scoped>
.hint {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}
.hint code {
  background: #f5f7fa;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 12px;
}
:deep(.el-divider) {
  margin: 18px 0 12px;
}
:deep(.el-divider .el-divider__text) {
  font-weight: 600;
  color: #1f2937;
}
</style>
