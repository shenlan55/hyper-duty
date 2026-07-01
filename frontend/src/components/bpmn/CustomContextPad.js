/**
 * 流程设计器自定义 ContextPad（P2-1）
 * ----------------------------------------------------------------------------
 * bpmn-js 默认 ContextPad 只有 append/append-text/connect/delete 等少量按钮，
 * 满足不了 Hyper Duty 业务需要：
 *   - UserTask 需要"👤 处理人"配置按钮（打开 wf_node_handler 配置弹窗）
 *   - Gateway 需要"⚙ 网关条件"快捷入口
 *   - StartEvent 需要"🟢 配置发起人变量"入口
 *   - SubProcess/CallActivity 需要"🔁 多实例"配置入口
 *
 * 本插件的 entry 通过 ContextPadProvider 协议注册，bpmn-js 会在节点被选中时
 * 自动渲染到元素旁边的浮动按钮面板。
 *
 * 与 ProcessDesigner 的对接方式：
 *   1. 本文件 emit 自定义事件 `bpmn.contextPad.action`（通过 modeler.fire 透传）
 *   2. ProcessDesigner 监听该事件后用 el-dialog 弹对应配置面板
 * ----------------------------------------------------------------------------
 */
export default class CustomContextPadProvider {
  constructor(config, contextPad, create, elementFactory, injector, translate, modeling) {
    this._contextPad = contextPad
    this._create = create
    this._elementFactory = elementFactory
    this._injector = injector
    this._translate = translate
    this._modeling = modeling

    // 用 eventBus 拿 bpmn-js 全局事件总线（用于透传到 ProcessDesigner）
    this._eventBus = injector.get('eventBus', false)
  }

  getContextPadEntries(element) {
    const contextPad = this._contextPad
    const modeling = this._modeling
    const eventBus = this._eventBus
    const translate = this._translate
    const businessObject = element.businessObject

    /**
     * 工具方法：注册一个 contextPad entry
     * @param {string} actionName  业务名（处理人/网关条件/多实例/发起人变量）
     * @param {string} className   CSS class（图标背景用）
     * @param {string} title       鼠标悬浮提示
     */
    const addEntry = (actionName, className, title) => {
      contextPad.registerProvider(actionName, {
        title: translate(title),
        className: `bpmn-icon-${className} custom-context-pad-${actionName}`,
        action: {
          click: (event, el) => {
            // 阻止 bpmn-js 画布 click 清空选区
            event.preventDefault()
            event.stopPropagation()
            if (eventBus) {
              eventBus.fire('bpmn.contextPad.action', {
                action: actionName,
                element: el,
                businessObject
              })
            }
          }
        }
      })
    }

    // ====================== UserTask 专用 ======================
    if (businessObject.$type === 'bpmn:UserTask') {
      addEntry('handler', 'user', '配置处理人（打开节点处理人配置）')
      addEntry('multiInstance', 'parallel-mi-marker', '配置多实例（会签/或签/比例通过）')
      addEntry('cc', 'sub-process-marker', '配置抄送')
    }

    // ====================== SequenceFlow 专用 ======================
    if (businessObject.$type === 'bpmn:SequenceFlow') {
      addEntry('condition', 'bpmn-icon-screw-wrench', '配置流转条件')
    }

    // ====================== Gateway 专用 ======================
    if (
      businessObject.$type === 'bpmn:ExclusiveGateway' ||
      businessObject.$type === 'bpmn:InclusiveGateway' ||
      businessObject.$type === 'bpmn:ComplexGateway'
    ) {
      addEntry('condition', 'bpmn-icon-screw-wrench', '配置分支条件')
    }

    // ====================== SubProcess / Transaction ======================
    if (businessObject.$type === 'bpmn:SubProcess' || businessObject.$type === 'bpmn:Transaction') {
      addEntry('multiInstance', 'parallel-mi-marker', '配置多实例')
      addEntry('compensation', 'bpmn-icon-compensation', '关联补偿处理')
    }

    // ====================== CallActivity 专用（P2-2）======================
    if (businessObject.$type === 'bpmn:CallActivity') {
      addEntry('callActivity', 'bpmn-icon-call-activity', '配置被调用流程 / 变量映射')
    }

    // ====================== P2-2 各类 Event 高级配置 ======================
    // 通用：所有事件节点都可配置 initiator / eventType
    if (businessObject.$type === 'bpmn:StartEvent') {
      addEntry('initiator', 'bpmn-icon-start-event-none-svg', '配置发起人变量')
      addEntry('eventDef', 'bpmn-icon-start-event-message-svg', '配置事件定义（消息/定时/信号/条件）')
    }

    if (businessObject.$type === 'bpmn:IntermediateCatchEvent' ||
        businessObject.$type === 'bpmn:IntermediateThrowEvent') {
      addEntry('eventDef', 'bpmn-icon-intermediate-event-catch-message-svg', '配置事件定义')
    }

    if (businessObject.$type === 'bpmn:EndEvent') {
      addEntry('eventDef', 'bpmn-icon-end-event-message-svg', '配置事件定义（消息/错误/取消/补偿/终止）')
    }

    if (businessObject.$type === 'bpmn:BoundaryEvent') {
      addEntry('eventDef', 'bpmn-icon-intermediate-event-catch-error-svg', '配置 boundary 行为（cancelActivity/timeDate/errorRef）')
    }

    // ====================== P2-2 泳道 / 池 ======================
    if (businessObject.$type === 'bpmn:Participant') {
      addEntry('callActivity', 'bpmn-icon-participant', '配置 Participant 对应流程')
    }

    if (businessObject.$type === 'bpmn:Lane') {
      addEntry('callActivity', 'bpmn-icon-lane', '配置泳道（角色/部门）')
    }

    return () => {} // 不覆盖默认 entries
  }
}

CustomContextPadProvider.$inject = [
  'config',
  'contextPad',
  'create',
  'elementFactory',
  'injector',
  'translate',
  'modeling'
]
