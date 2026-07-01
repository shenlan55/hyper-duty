/**
 * 流程设计器自定义 Palette（v2：带分组标题 + 中文 tooltip）
 *
 * 【背景】bpmn-js 默认 PaletteProvider 提供的节点不满足 Hyper Duty 业务需求：
 *   - 默认只有 14 个 entry，没有 bpmn:UserTask（用户任务/审批节点）
 *   - 默认没有 service-task、send-task、receive-task、各种 timer/message/signal 事件
 *   - 默认没有 parallel-gateway、inclusive-gateway、event-based-gateway
 *   - 默认没有 text-annotation（文本注释）
 *
 * 【v1 → v2 升级】（2026-06-27）
 *   - 每个分组（activity/gateway/event/artifact）头部增加"分组标题"entry
 *   - 标题 entry 通过 separator 行为独占一行，className 设为 bpmn-palette-group-title
 *   - CSS 在 bpmn-palette.css 中定义：图标放大到 48×48、hover 高亮、分组标题样式
 *
 * 【踩坑1】不要在 factory 函数内部用 `this.xxx = ...` 来挂方法。
 *          didi 用 `factoryFn.apply(null, deps)` 调用，this 是 undefined。
 *
 * 【踩坑2】bpmn-js/didi 期望 additionalModules 每一项是对象
 *         { serviceName: ['type', FactoryFn] } 或 { serviceName: ['value', obj] }
 *         直接传 factory function 本身，didi 会跳过、模块不被实例化。
 *         修复：包一层声明式 module { __init__: ['xxx'], xxx: ['type', FactoryFn] }
 *
 * 【优先级】priority=500，bpmn-js 默认 Provider 的 priority=1000；
 *          数值越小优先级越高，相同 key 时本 Provider 优先生效。
 */

// 工具函数：创建一个指定 bpmn: 类型的 action
function makeAction(type, bpmnFactory, elementFactory, create, attrs) {
  return function (event) {
    let businessObject
    if (attrs && attrs.eventDefinitionType) {
      const eventDef = bpmnFactory.create(attrs.eventDefinitionType)
      businessObject = bpmnFactory.create(type, {
        eventDefinitions: [eventDef]
      })
    } else {
      businessObject = bpmnFactory.create(type)
    }
    const shape = elementFactory.createShape({ type, businessObject })
    create.start(event, shape)
  }
}

/**
 * 创建一个"分组标题"占位 entry
 * - 通过 separator: true 让它独占一行
 * - className 设为 bpmn-palette-group-title（在 bpmn-palette.css 中定义）
 * - action 返回 false 阻止 bpmn-js 默认 click 行为
 */
function groupTitle(label) {
  return {
    group: '__GROUP__',  // 占位 group，bpmn-js 会按字母序排在最前
    className: 'bpmn-palette-group-title',
    separator: true,
    title: label,
    action: { click: () => false, dragstart: () => false }
  }
}

/**
 * 业务分组配置：每组头部一个标题 entry，后跟该组的节点
 * - 用数组 + spread 注入到 getPaletteEntries 的返回值里
 * - bpmn-js palette 渲染时按 group 字段排序；为了标题在每组**最前**，
 *   把标题 entry 的 group 设为该组名+'_title'（在字母序上排在原 group 之前）
 */
const GROUP_DEFS = [
  {
    group: 'activity_title',
    title: '活动节点（审批 / 服务 / 手工）',
    entries: [
      {
        key: 'create.user-task',
        type: 'bpmn:UserTask',
        icon: 'bpmn-icon-user-task',
        label: '用户任务（审批节点 ⭐）'
      },
      {
        key: 'create.service-task',
        type: 'bpmn:ServiceTask',
        icon: 'bpmn-icon-service-task',
        label: '服务任务（后端自动执行）'
      },
      {
        key: 'create.send-task',
        type: 'bpmn:SendTask',
        icon: 'bpmn-icon-send-task',
        label: '发送任务'
      },
      {
        key: 'create.receive-task',
        type: 'bpmn:ReceiveTask',
        icon: 'bpmn-icon-receive-task',
        label: '接收任务'
      },
      {
        key: 'create.manual-task',
        type: 'bpmn:ManualTask',
        icon: 'bpmn-icon-manual-task',
        label: '手工任务'
      },
      {
        key: 'create.business-rule-task',
        type: 'bpmn:BusinessRuleTask',
        icon: 'bpmn-icon-business-rule-task',
        label: '业务规则任务'
      },
      {
        key: 'create.script-task',
        type: 'bpmn:ScriptTask',
        icon: 'bpmn-icon-script-task',
        label: '脚本任务'
      },
      {
        key: 'create.call-activity',
        type: 'bpmn:CallActivity',
        icon: 'bpmn-icon-call-activity',
        label: '调用活动（引用其他流程）'
      }
    ]
  },
  {
    group: 'gateway_title',
    title: '网关（流程分支 / 判断）',
    entries: [
      {
        key: 'create.exclusive-gateway',
        type: 'bpmn:ExclusiveGateway',
        icon: 'bpmn-icon-gateway-xor',
        label: '排他网关（条件判断，只能走一条）'
      },
      {
        key: 'create.parallel-gateway',
        type: 'bpmn:ParallelGateway',
        icon: 'bpmn-icon-gateway-parallel',
        label: '并行网关（多路同时执行）'
      },
      {
        key: 'create.inclusive-gateway',
        type: 'bpmn:InclusiveGateway',
        icon: 'bpmn-icon-gateway-or',
        label: '包容网关（满足条件的分支都执行）'
      },
      {
        key: 'create.event-based-gateway',
        type: 'bpmn:EventBasedGateway',
        icon: 'bpmn-icon-gateway-eventbased',
        label: '事件网关（等待事件触发）'
      },
      {
        key: 'create.complex-gateway',
        type: 'bpmn:ComplexGateway',
        icon: 'bpmn-icon-gateway-complex',
        label: '复杂网关（高级用）'
      }
    ]
  },
  {
    group: 'event_title',
    title: '事件（开始 / 中间 / 结束 / 边界）',
    entries: [
      {
        key: 'create.start-event',
        type: 'bpmn:StartEvent',
        icon: 'bpmn-icon-start-event-none',
        label: '开始事件（空白）'
      },
      {
        key: 'create.start-message-event',
        type: 'bpmn:StartEvent',
        icon: 'bpmn-icon-start-event-message',
        label: '开始事件（消息触发）',
        eventDefinitionType: 'bpmn:MessageEventDefinition'
      },
      {
        key: 'create.start-timer-event',
        type: 'bpmn:StartEvent',
        icon: 'bpmn-icon-start-event-timer',
        label: '开始事件（定时触发）',
        eventDefinitionType: 'bpmn:TimerEventDefinition'
      },
      {
        key: 'create.start-signal-event',
        type: 'bpmn:StartEvent',
        icon: 'bpmn-icon-start-event-signal',
        label: '开始事件（信号触发）',
        eventDefinitionType: 'bpmn:SignalEventDefinition'
      },
      {
        key: 'create.start-condition-event',
        type: 'bpmn:StartEvent',
        icon: 'bpmn-icon-start-event-condition',
        label: '开始事件（条件触发）',
        eventDefinitionType: 'bpmn:ConditionalEventDefinition'
      },
      {
        key: 'create.intermediate-event',
        type: 'bpmn:IntermediateThrowEvent',
        icon: 'bpmn-icon-intermediate-event-none',
        label: '中间事件（空白）'
      },
      {
        key: 'create.intermediate-message-event',
        type: 'bpmn:IntermediateCatchEvent',
        icon: 'bpmn-icon-intermediate-event-catch-message',
        label: '中间事件（消息捕获）',
        eventDefinitionType: 'bpmn:MessageEventDefinition'
      },
      {
        key: 'create.intermediate-timer-event',
        type: 'bpmn:IntermediateCatchEvent',
        icon: 'bpmn-icon-intermediate-event-catch-timer',
        label: '中间事件（定时器）',
        eventDefinitionType: 'bpmn:TimerEventDefinition'
      },
      {
        key: 'create.intermediate-signal-event',
        type: 'bpmn:IntermediateCatchEvent',
        icon: 'bpmn-icon-intermediate-event-catch-signal',
        label: '中间事件（信号捕获）',
        eventDefinitionType: 'bpmn:SignalEventDefinition'
      },
      {
        key: 'create.end-event',
        type: 'bpmn:EndEvent',
        icon: 'bpmn-icon-end-event-none',
        label: '结束事件（空白）'
      },
      {
        key: 'create.end-message-event',
        type: 'bpmn:EndEvent',
        icon: 'bpmn-icon-end-event-message',
        label: '结束事件（消息抛出）',
        eventDefinitionType: 'bpmn:MessageEventDefinition'
      },
      {
        key: 'create.end-signal-event',
        type: 'bpmn:EndEvent',
        icon: 'bpmn-icon-end-event-signal',
        label: '结束事件（信号抛出）',
        eventDefinitionType: 'bpmn:SignalEventDefinition'
      },
      {
        key: 'create.end-error-event',
        type: 'bpmn:EndEvent',
        icon: 'bpmn-icon-end-event-error',
        label: '结束事件（错误抛出）',
        eventDefinitionType: 'bpmn:ErrorEventDefinition'
      },
      {
        key: 'create.end-cancel-event',
        type: 'bpmn:EndEvent',
        icon: 'bpmn-icon-end-event-cancel',
        label: '结束事件（取消抛出）',
        eventDefinitionType: 'bpmn:CancelEventDefinition'
      },
      {
        key: 'create.end-compensation-event',
        type: 'bpmn:EndEvent',
        icon: 'bpmn-icon-end-event-compensation',
        label: '结束事件（补偿抛出）',
        eventDefinitionType: 'bpmn:CompensateEventDefinition'
      },
      {
        key: 'create.boundary-message-event',
        type: 'bpmn:BoundaryEvent',
        icon: 'bpmn-icon-intermediate-event-catch-message',
        label: '边界事件（消息）',
        eventDefinitionType: 'bpmn:MessageEventDefinition'
      },
      {
        key: 'create.boundary-timer-event',
        type: 'bpmn:BoundaryEvent',
        icon: 'bpmn-icon-intermediate-event-catch-timer',
        label: '边界事件（定时器，超时触发）',
        eventDefinitionType: 'bpmn:TimerEventDefinition'
      },
      {
        key: 'create.boundary-error-event',
        type: 'bpmn:BoundaryEvent',
        icon: 'bpmn-icon-intermediate-event-catch-error',
        label: '边界事件（错误捕获）',
        eventDefinitionType: 'bpmn:ErrorEventDefinition'
      },
      {
        key: 'create.boundary-signal-event',
        type: 'bpmn:BoundaryEvent',
        icon: 'bpmn-icon-intermediate-event-catch-signal',
        label: '边界事件（信号）',
        eventDefinitionType: 'bpmn:SignalEventDefinition'
      },
      {
        key: 'create.boundary-cancel-event',
        type: 'bpmn:BoundaryEvent',
        icon: 'bpmn-icon-intermediate-event-catch-cancel',
        label: '边界事件（取消）',
        eventDefinitionType: 'bpmn:CancelEventDefinition'
      },
      {
        key: 'create.boundary-compensation-event',
        type: 'bpmn:BoundaryEvent',
        icon: 'bpmn-icon-intermediate-event-catch-compensation',
        label: '边界事件（补偿）',
        eventDefinitionType: 'bpmn:CompensateEventDefinition'
      }
    ]
  },
  {
    group: 'artifact_title',
    title: '工件（注释 / 分组）',
    entries: [
      {
        key: 'create.text-annotation',
        type: 'bpmn:TextAnnotation',
        icon: 'bpmn-icon-text-annotation',
        label: '文本注释'
      }
    ]
  },
  // ====================== P2-2 新增：泳道 / 子流程 / 数据对象 ======================
  {
    group: 'container_title',
    title: '容器（泳道 / 子流程 / 调）',
    entries: [
      {
        key: 'create.participant',
        type: 'bpmn:Participant',
        icon: 'bpmn-icon-participant',
        label: '池 / 参与者（Pool，整池一个流程）'
      },
      {
        key: 'create.lane',
        type: 'bpmn:Lane',
        icon: 'bpmn-icon-lane',
        label: '泳道（Lane，按角色/部门分线）'
      },
      {
        key: 'create.sub-process',
        type: 'bpmn:SubProcess',
        icon: 'bpmn-icon-sub-process-expanded',
        label: '嵌入式子流程（折叠/展开）'
      },
      {
        key: 'create.transaction',
        type: 'bpmn:Transaction',
        icon: 'bpmn-icon-transaction',
        label: '事务子流程（带取消/补偿）'
      },
      {
        key: 'create.data-store',
        type: 'bpmn:DataStoreReference',
        icon: 'bpmn-icon-data-store',
        label: '数据存储引用'
      }
    ]
  }
]

/**
 * 工厂函数：返回 PaletteProvider
 * - 形参顺序与 $inject 严格对应
 * - 内部禁止使用 this（didi 用 apply(null, deps) 调用，this 是 undefined）
 */
export default function customPalette(bpmnFactory, create, elementFactory, palette, translate) {
  const provider = {
    getPaletteEntries: function () {
      const entries = {}

      // 1. 注入所有"分组标题"占位 entry
      //    【v3 升级 2026-06-28】去掉 separator: true —— bpmn-js 12+ 在 separator
      //    模式下会忽略 className/title，把 entry 强制替换为 <div class="separator">，
      //    导致分组标题完全不可见。改为普通 entry + action 返回 false + CSS
      //    pointer-events:none 屏蔽拖动。
      GROUP_DEFS.forEach((def) => {
        entries[`__group_title_${def.group}__`] = {
          group: def.group,  // 用 _title 后缀，bpmn-js 会按字母排序让标题排在原 group 之前
          className: 'bpmn-palette-group-title',  // CSS 在 bpmn-palette.css 中定义
          title: translate(def.title),
          html: `<div class="bpmn-palette-group-title-inner">${translate(def.title)}</div>`,
          action: {
            // 返回 false 阻止默认 click + dragstart，bpmn-js 会忽略该 entry
            click: () => false,
            dragstart: () => false
          }
        }
      })

      // 2. 注入所有业务 entry
      GROUP_DEFS.forEach((def) => {
        const realGroup = def.group.replace('_title', '')  // activity / gateway / event / artifact
        def.entries.forEach((e) => {
          entries[e.key] = {
            group: realGroup,
            className: e.icon,
            title: translate(e.label),
            action: {
              dragstart: makeAction(e.type, bpmnFactory, elementFactory, create, e.eventDefinitionType ? { eventDefinitionType: e.eventDefinitionType } : undefined),
              click: makeAction(e.type, bpmnFactory, elementFactory, create, e.eventDefinitionType ? { eventDefinitionType: e.eventDefinitionType } : undefined)
            }
          }
        })
      })

      return entries
    }
  }
  // 立即注册到 palette 服务（didi 注入的全局单例）
  palette.registerProvider(500, provider)
  return provider
}

// bpmn-js DI 元数据：参数顺序与上面工厂函数形参严格对应
customPalette.$inject = [
  'bpmnFactory',
  'create',
  'elementFactory',
  'palette',
  'translate'
]
