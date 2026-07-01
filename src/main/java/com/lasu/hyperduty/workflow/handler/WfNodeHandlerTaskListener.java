package com.lasu.hyperduty.workflow.handler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * 节点处理人全局 TaskListener（P1-10）
 * ----------------------------------------------------------------------------
 * 在 FlowableConfig 中以 setTaskCreateListeners 形式注册到全局，
 * 所有 UserTask 在 create 事件触发时，自动调用 WfNodeHandlerResolver 解析处理人。
 *
 * 之所以用全局 TaskListener 而非 BPMN XML 内嵌：
 *   1. 节点处理人配置存于 wf_node_handler 表（设计器保存时落库）
 *   2. 同一流程 KEY 不同版本可能共享节点 ID
 *   3. 部署时改 XML 不便于后续维护
 *
 * 事件常量：TaskListener.EVENTNAME_CREATE = "create"
 * ----------------------------------------------------------------------------
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WfNodeHandlerTaskListener implements TaskListener {

    private final WfNodeHandlerResolver resolver;

    @Override
    public void notify(DelegateTask delegateTask) {
        if (!"create".equals(delegateTask.getEventName())) {
            return;
        }
        try {
            resolver.resolveAndApply(delegateTask);
        } catch (Exception e) {
            log.error("[节点处理人] 解析失败: taskId={}, nodeId={}",
                    delegateTask.getId(), delegateTask.getTaskDefinitionKey(), e);
            // 不抛异常：避免任务无法创建导致流程卡死；可后续补监控
        }
    }
}
