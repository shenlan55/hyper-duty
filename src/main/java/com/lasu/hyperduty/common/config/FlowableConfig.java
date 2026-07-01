package com.lasu.hyperduty.common.config;

import org.flowable.bpmn.model.UserTask;
import org.flowable.bpmn.model.FlowableListener;
import org.flowable.engine.*;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.engine.impl.bpmn.parser.handler.AbstractBpmnParseHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * Flowable工作流配置类
 */
@Configuration
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration> {

    @Override
    public void configure(SpringProcessEngineConfiguration engineConfiguration) {
        // 临时启用：执行 clean_flowable_all.sql 后，自动创建干净的表
        engineConfiguration.setDatabaseSchemaUpdate("true");
        // 设置字体，解决流程图中文乱码
        engineConfiguration.setActivityFontName("宋体");
        engineConfiguration.setLabelFontName("宋体");
        engineConfiguration.setAnnotationFontName("宋体");
        // 启用异步执行器
        engineConfiguration.setAsyncExecutorActivate(true);
        // ====================== P1-10 节点处理人运行时解析 ======================
        // 通过自定义 BpmnParseHandler，在每个 UserTask 解析后注入 TaskListener，
        // Listener 用 delegateExpression 引用 Spring 容器中的 WfNodeHandlerTaskListener bean
        List<org.flowable.engine.parse.BpmnParseHandler> postHandlers = new ArrayList<>(
                engineConfiguration.getPostBpmnParseHandlers() == null ? List.of() : engineConfiguration.getPostBpmnParseHandlers());
        postHandlers.add(new UserTaskHandlerListenerInjector());
        engineConfiguration.setPostBpmnParseHandlers(postHandlers);
    }

    /**
     * UserTask 解析后注入 TaskListener（delegateExpression 引用 Spring bean）
     */
    public static class UserTaskHandlerListenerInjector extends AbstractBpmnParseHandler<UserTask> {
        @Override
        protected void executeParse(org.flowable.engine.impl.bpmn.parser.BpmnParse bpmnParse, UserTask userTask) {
            // 避免重复注入（重新部署时已有 listener，跳过）
            boolean exists = userTask.getTaskListeners() != null && userTask.getTaskListeners().stream()
                    .anyMatch(l -> TaskListener.EVENTNAME_CREATE.equals(l.getEvent())
                            && "wfNodeHandlerTaskListener".equals(l.getImplementation()));
            if (exists) return;
            FlowableListener listener = new FlowableListener();
            listener.setEvent(TaskListener.EVENTNAME_CREATE);
            // delegateExpression：Flowable 会从 Spring 容器找 bean
            listener.setImplementation("wfNodeHandlerTaskListener");
            listener.setImplementationType("delegateExpression");
            userTask.getTaskListeners().add(listener);
        }

        @Override
        protected Class<? extends UserTask> getHandledType() {
            return UserTask.class;
        }
    }

    @Bean
    public RepositoryService repositoryService(ProcessEngine processEngine) {
        return processEngine.getRepositoryService();
    }

    @Bean
    public RuntimeService runtimeService(ProcessEngine processEngine) {
        return processEngine.getRuntimeService();
    }

    @Bean
    public TaskService taskService(ProcessEngine processEngine) {
        return processEngine.getTaskService();
    }

    @Bean
    public HistoryService historyService(ProcessEngine processEngine) {
        return processEngine.getHistoryService();
    }

    @Bean
    public IdentityService identityService(ProcessEngine processEngine) {
        return processEngine.getIdentityService();
    }

    @Bean
    public ManagementService managementService(ProcessEngine processEngine) {
        return processEngine.getManagementService();
    }

}
