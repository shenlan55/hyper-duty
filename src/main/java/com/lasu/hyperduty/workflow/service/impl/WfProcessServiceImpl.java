package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.dto.WfProcessStartDTO;
import com.lasu.hyperduty.workflow.service.WfProcessService;
import com.lasu.hyperduty.workflow.service.WfDelegateService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.workflow.entity.WfDefinition;
import com.lasu.hyperduty.workflow.mapper.WfDefinitionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.HistoryService;
import org.flowable.engine.IdentityService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.engine.runtime.ProcessInstanceQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 流程管理服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class WfProcessServiceImpl implements WfProcessService {

    private final RepositoryService repositoryService;
    private final RuntimeService runtimeService;
    private final HistoryService historyService;
    private final IdentityService identityService;
    private final TaskService taskService;
    private final WfDelegateService wfDelegateService;
    private final WfDefinitionMapper wfDefinitionMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Deployment deployProcess(String name, String bpmnXml) {
        try {
            Deployment deployment = repositoryService.createDeployment()
                    .name(name)
                    .addInputStream(name + ".bpmn20.xml", new ByteArrayInputStream(bpmnXml.getBytes(StandardCharsets.UTF_8)))
                    .deploy();

            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery()
                    .deploymentId(deployment.getId())
                    .list();

            for (ProcessDefinition pd : processDefinitions) {
                // 检查是否已存在同key的wf_definition，避免重复
                List<WfDefinition> existing = wfDefinitionMapper.selectList(
                        new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfDefinition>()
                                .eq(WfDefinition::getProcessDefinitionKey, pd.getKey())
                                .orderByDesc(WfDefinition::getVersion)
                                .last("LIMIT 1"));
                
                WfDefinition wfDefinition;
                if (!existing.isEmpty()) {
                    // 更新已有记录
                    wfDefinition = existing.get(0);
                    wfDefinition.setProcessDefinitionId(pd.getId());
                    wfDefinition.setProcessName(pd.getName() != null ? pd.getName() : name);
                    wfDefinition.setVersion(pd.getVersion());
                    wfDefinition.setUpdateTime(java.time.LocalDateTime.now());
                    wfDefinitionMapper.updateById(wfDefinition);
                } else {
                    // 新增记录
                    wfDefinition = new WfDefinition();
                    wfDefinition.setProcessDefinitionId(pd.getId());
                    wfDefinition.setProcessDefinitionKey(pd.getKey());
                    wfDefinition.setProcessName(pd.getName() != null ? pd.getName() : name);
                    wfDefinition.setVersion(pd.getVersion());
                    wfDefinition.setStatus(1);
                    wfDefinition.setCreateTime(java.time.LocalDateTime.now());
                    wfDefinition.setUpdateTime(java.time.LocalDateTime.now());
                    wfDefinitionMapper.insert(wfDefinition);
                }
            }

            return deployment;
        } catch (Exception e) {
            log.error("部署流程失败", e);
            throw new BusinessException("部署流程失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteDeployment(String deploymentId) {
        try {
            repositoryService.deleteDeployment(deploymentId, true);
        } catch (Exception e) {
            log.error("删除部署失败", e);
            throw new BusinessException("删除部署失败：" + e.getMessage());
        }
    }

    @Override
    public Page<ProcessDefinition> pageProcessDefinition(Integer pageNum, Integer pageSize, String processKey) {
        ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey().asc();

        if (StringUtils.hasText(processKey)) {
            query.processDefinitionKeyLike("%" + processKey + "%");
        }

        long total = query.count();
        List<ProcessDefinition> list = query.listPage((pageNum - 1) * pageSize, pageSize);

        Page<ProcessDefinition> page = new Page<>(pageNum, pageSize);
        page.setRecords(list);
        page.setTotal(total);
        return page;
    }

    @Override
    public List<ProcessDefinition> listProcessDefinition() {
        return repositoryService.createProcessDefinitionQuery()
                .latestVersion()
                .orderByProcessDefinitionKey().asc()
                .list();
    }

    @Override
    public ProcessDefinition getLatestProcessDefinition(String processKey) {
        return repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .latestVersion()
                .singleResult();
    }

    @Override
    public String getProcessBpmnXml(String processDefinitionId) {
        try {
            log.info("获取流程BPMN XML, processDefinitionId: {}", processDefinitionId);
            
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
                    .processDefinitionId(processDefinitionId)
                    .singleResult();
            
            if (processDefinition == null) {
                log.error("流程定义不存在, processDefinitionId: {}", processDefinitionId);
                throw new BusinessException("流程定义不存在");
            }
            
            log.info("找到流程定义: deploymentId={}, resourceName={}", 
                    processDefinition.getDeploymentId(), 
                    processDefinition.getResourceName());

            InputStream inputStream = repositoryService.getResourceAsStream(
                    processDefinition.getDeploymentId(),
                    processDefinition.getResourceName()
            );
            
            if (inputStream == null) {
                log.error("获取资源流失败, deploymentId={}, resourceName={}", 
                        processDefinition.getDeploymentId(), 
                        processDefinition.getResourceName());
                throw new BusinessException("流程资源不存在");
            }
            
            String xml = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            log.info("成功读取流程XML, 长度: {}", xml.length());
            return xml;
        } catch (Exception e) {
            log.error("获取流程BPMN XML失败", e);
            throw new BusinessException("获取流程BPMN XML失败：" + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProcessInstance startProcess(WfProcessStartDTO dto) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            String userName = SecurityUtil.getCurrentUsername();

            // 设置流程发起人身份
            identityService.setAuthenticatedUserId(userId.toString());

            Map<String, Object> variables = dto.getVariables();
            if (variables == null) {
                variables = new HashMap<>();
            }
            // 设置流程变量，供BPMN中assignee表达式使用（如 ${startUserId}）
            variables.put("startUserId", userId.toString());
            variables.put("startUserName", userName);
            variables.put("initiator", userId.toString());

            ProcessInstance processInstance;
            if (StringUtils.hasText(dto.getBusinessKey())) {
                processInstance = runtimeService.startProcessInstanceByKey(
                        dto.getProcessDefinitionKey(),
                        dto.getBusinessKey(),
                        variables
                );
            } else {
                processInstance = runtimeService.startProcessInstanceByKey(
                        dto.getProcessDefinitionKey(),
                        variables
                );
            }

            log.info("流程启动成功: processInstanceId={}, processDefinitionKey={}, startUserId={}", 
                    processInstance.getId(), dto.getProcessDefinitionKey(), userId);
            
            // 检查是否有待办任务生成
            long taskCount = taskService.createTaskQuery()
                    .processInstanceId(processInstance.getId())
                    .count();
            log.info("流程启动后待办任务数: {}", taskCount);
            if (taskCount == 0 && !processInstance.isEnded()) {
                log.warn("流程已启动但无待办任务，请检查BPMN流程中是否配置了用户任务(UserTask)及其assignee");
            }

            return processInstance;
        } catch (Exception e) {
            log.error("启动流程失败", e);
            throw new BusinessException("启动流程失败：" + e.getMessage());
        } finally {
            identityService.setAuthenticatedUserId(null);
        }
    }

    @Override
    public ProcessInstance getProcessInstance(String processInstanceId) {
        return runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }

    @Override
    public ProcessInstance getProcessInstanceByBusinessKey(String processDefinitionKey, String businessKey) {
        return runtimeService.createProcessInstanceQuery()
                .processDefinitionKey(processDefinitionKey)
                .processInstanceBusinessKey(businessKey)
                .singleResult();
    }

    @Override
    public Page<ProcessInstance> pageMyStartedProcess(Integer pageNum, Integer pageSize, Long userId) {
        ProcessInstanceQuery query = runtimeService.createProcessInstanceQuery()
                .startedBy(userId.toString());

        long total = query.count();
        List<ProcessInstance> list = query.listPage((pageNum - 1) * pageSize, pageSize);

        Page<ProcessInstance> page = new Page<>(pageNum, pageSize);
        page.setRecords(list);
        page.setTotal(total);
        return page;
    }

    @Override
    public Page<HistoricProcessInstance> pageMyCompletedProcess(Integer pageNum, Integer pageSize, Long userId) {
        org.flowable.engine.history.HistoricProcessInstanceQuery query = historyService.createHistoricProcessInstanceQuery()
                .startedBy(userId.toString())
                .finished()
                .orderByProcessInstanceEndTime().desc();

        long total = query.count();
        List<HistoricProcessInstance> list = query.listPage((pageNum - 1) * pageSize, pageSize);

        Page<HistoricProcessInstance> page = new Page<>(pageNum, pageSize);
        page.setRecords(list);
        page.setTotal(total);
        return page;
    }

    @Override
    public Map<String, Object> getProcessVariables(String processInstanceId) {
        return runtimeService.getVariables(processInstanceId);
    }

    @Override
    public void setProcessVariables(String processInstanceId, Map<String, Object> variables) {
        runtimeService.setVariables(processInstanceId, variables);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelProcess(String processInstanceId, String reason) {
        try {
            runtimeService.deleteProcessInstance(processInstanceId, reason);
        } catch (Exception e) {
            log.error("作废流程失败", e);
            throw new BusinessException("作废流程失败：" + e.getMessage());
        }
    }

    @Override
    public void suspendProcess(String processInstanceId) {
        try {
            runtimeService.suspendProcessInstanceById(processInstanceId);
        } catch (Exception e) {
            log.error("挂起流程失败", e);
            throw new BusinessException("挂起流程失败：" + e.getMessage());
        }
    }

    @Override
    public void activateProcess(String processInstanceId) {
        try {
            runtimeService.activateProcessInstanceById(processInstanceId);
        } catch (Exception e) {
            log.error("激活流程失败", e);
            throw new BusinessException("激活流程失败：" + e.getMessage());
        }
    }

    @Override
    public List<HistoricActivityInstance> getHistoricActivityInstances(String processInstanceId) {
        return historyService.createHistoricActivityInstanceQuery()
                .processInstanceId(processInstanceId)
                .orderByHistoricActivityInstanceStartTime().asc()
                .list();
    }

    @Override
    public HistoricProcessInstance getHistoricProcessInstance(String processInstanceId) {
        return historyService.createHistoricProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
    }
}
