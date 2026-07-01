<!--
  属性面板分发器
  - 根据当前选中元素类型，分发到对应的子面板
  - 当前支持：UserTask（已实现）
  - 待 P0-3 扩展：SequenceFlow（条件表达式）
  - 待 P2 扩展：Gateway、Event、Process
-->
<template>
  <div class="property-panel">
    <div class="panel-header">
      <span class="panel-title">{{ panelTitle }}</span>
      <el-button text type="primary" size="small" @click="$emit('clear-selection')">清空选择</el-button>
    </div>
    <div class="panel-body">
      <!-- 节点基础信息：所有可命名节点都展示 -->
      <el-form v-if="baseForm" label-position="top" size="small">
        <el-form-item label="节点ID">
          <el-input v-model="baseForm.id" disabled />
        </el-form-item>
        <el-form-item label="节点名称">
          <el-input
            v-model="baseForm.name"
            placeholder="请输入节点名称"
            @change="updateBaseProp('name', baseForm.name)"
          />
        </el-form-item>
      </el-form>

      <!-- UserTask 专属 -->
      <UserTaskPanel
        v-if="isUserTask"
        :user-task-form="userTaskForm"
        :handler-type-options="handlerTypeOptions"
        :employee-options="employeeOptions"
        :role-options="roleOptions"
        :employee-columns="employeeColumns"
        :role-columns="roleColumns"
        :update-element-prop="updateElementProp"
        @handler-type-change="$emit('handler-type-change', $event)"
        @multi-instance-change="$emit('multi-instance-change', $event)"
      />

      <!-- SequenceFlow 专属：待 P0-3 实现条件表达式 -->
      <SequenceFlowPanel
        v-else-if="isSequenceFlow"
        :element="selectedElement"
        :update-element-prop="updateElementProp"
      />

      <!-- 其它节点类型：占位 -->
      <el-empty
        v-else-if="selectedElement && !isUserTask && !isSequenceFlow"
        description="此节点类型暂未提供属性配置"
        :image-size="80"
      />

      <!-- 画布未选中任何节点 -->
      <el-empty
        v-else
        description="请在画布中选择一个节点"
        :image-size="80"
      />
    </div>
  </div>
</template>

<script>
import UserTaskPanel from './panels/UserTaskPanel.vue'
import SequenceFlowPanel from './panels/SequenceFlowPanel.vue'

export default {
  name: 'PropertyPanel',
  components: { UserTaskPanel, SequenceFlowPanel },
  props: {
    /** 当前选中的 bpmn 元素（bpmn-js element，可能为 null） */
    selectedElement: { type: Object, default: null },
    /** 基础表单 { id, name }，由父级 fillFormFromElement 维护 */
    baseForm: { type: Object, default: null },
    /** UserTask 专属表单 reactive（与父级共享） */
    userTaskForm: { type: Object, required: true },
    /** 处理人类型选项 */
    handlerTypeOptions: { type: Array, required: true },
    /** 员工/角色选项 */
    employeeOptions: { type: Array, default: () => [] },
    roleOptions: { type: Array, default: () => [] },
    /** SelectorDialog 列定义 */
    employeeColumns: { type: Array, required: true },
    roleColumns: { type: Array, required: true }
  },
  emits: [
    'clear-selection',
    'update-base-prop',
    'update-element-prop',
    'handler-type-change',
    'multi-instance-change'
  ],
  setup(props, { emit }) {
    const updateBaseProp = (key, value) => emit('update-base-prop', key, value)
    const updateElementProp = (key, value) => emit('update-element-prop', key, value)

    return {
      updateBaseProp,
      updateElementProp
    }
  },
  computed: {
    /** 当前元素类型映射后的中文标题 */
    panelTitle() {
      if (!this.selectedElement) return '属性'
      const bo = this.selectedElement.businessObject
      const map = {
        'bpmn:UserTask': '用户任务（审批节点）',
        'bpmn:ServiceTask': '服务任务',
        'bpmn:SequenceFlow': '流转线',
        'bpmn:ExclusiveGateway': '排他网关',
        'bpmn:ParallelGateway': '并行网关',
        'bpmn:InclusiveGateway': '包容网关',
        'bpmn:StartEvent': '开始事件',
        'bpmn:EndEvent': '结束事件',
        'bpmn:IntermediateThrowEvent': '中间事件',
        'bpmn:IntermediateCatchEvent': '捕获事件'
      }
      return map[bo?.$type] || `${bo?.$type || 'Element'} 属性`
    },
    /** 是否选中用户任务 */
    isUserTask() {
      return this.selectedElement?.type === 'bpmn:UserTask'
    },
    /** 是否选中流转线 */
    isSequenceFlow() {
      return this.selectedElement?.type === 'bpmn:SequenceFlow'
    }
  }
}
</script>

<style scoped>
.property-panel {
  width: 360px;
  border-left: 1px solid #e5e7eb;
  background: #fff;
  display: flex;
  flex-direction: column;
  height: 100%;
}
.panel-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}
.panel-title {
  font-weight: 600;
  font-size: 14px;
  color: #1f2937;
}
.panel-body {
  flex: 1;
  overflow-y: auto;
  padding: 16px;
}
</style>
