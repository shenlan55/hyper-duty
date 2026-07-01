package com.lasu.hyperduty.workflow.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.exception.BusinessException;
import com.lasu.hyperduty.workflow.dto.WfDefinitionDiffDTO;
import com.lasu.hyperduty.workflow.dto.WfProcessStartDTO;
import com.lasu.hyperduty.workflow.service.WfProcessService;
import com.lasu.hyperduty.workflow.service.WfDelegateService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.workflow.entity.WfDefinition;
import com.lasu.hyperduty.workflow.entity.WfInstance;
import com.lasu.hyperduty.workflow.mapper.WfDefinitionMapper;
import com.lasu.hyperduty.workflow.mapper.WfInstanceMapper;
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
import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final WfInstanceMapper wfInstanceMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void withdrawProcess(String processInstanceId, String reason) {
        try {
            Long userId = SecurityUtil.getCurrentUserId();
            String userName = SecurityUtil.getCurrentUsername();

            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (processInstance == null) {
                throw new BusinessException("流程实例不存在或已结束");
            }
            if (processInstance.isEnded()) {
                throw new BusinessException("流程已结束，无法撤回");
            }
            if (!String.valueOf(userId).equals(processInstance.getStartUserId())) {
                throw new BusinessException("仅发起人可撤回流程");
            }

            // 检查是否已有节点完成（不允许撤回已审批的流程）
            long finishedActivityCount = historyService.createHistoricActivityInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .activityType("userTask")
                    .finished()
                    .count();
            if (finishedActivityCount > 0) {
                throw new BusinessException("流程已审批，无法撤回");
            }

            // 作废流程实例
            runtimeService.deleteProcessInstance(processInstanceId, reason != null ? reason : "发起人撤回");

            // 同步扩展表
            com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<WfInstance> wrapper =
                    new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();
            wrapper.eq(WfInstance::getProcessInstanceId, processInstanceId);
            WfInstance instance = wfInstanceMapper.selectOne(wrapper);
            if (instance != null) {
                instance.setStatus(5); // 5=已撤回（与字典 wf_instance_status 对应）
                instance.setWithdrawUserId(userId);
                instance.setWithdrawUserName(userName);
                instance.setWithdrawTime(LocalDateTime.now());
                instance.setWithdrawReason(reason);
                instance.setUpdateTime(LocalDateTime.now());
                instance.setEndTime(LocalDateTime.now());
                wfInstanceMapper.updateById(instance);
            }
            log.info("流程撤回成功: processInstanceId={}, userId={}, reason={}", processInstanceId, userId, reason);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("撤回流程失败", e);
            throw new BusinessException("撤回流程失败：" + e.getMessage());
        }
    }

    @Override
    public Map<String, Object> getProcessTraceData(String processInstanceId) {
        Map<String, Object> result = new HashMap<>();

        // 查询流程实例
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                .processInstanceId(processInstanceId)
                .singleResult();
        HistoricProcessInstance historicProcessInstance = null;
        if (processInstance == null) {
            // 已结束，查历史
            historicProcessInstance = historyService.createHistoricProcessInstanceQuery()
                    .processInstanceId(processInstanceId)
                    .singleResult();
            if (historicProcessInstance == null) {
                throw new BusinessException("流程实例不存在");
            }
        }
        String processDefinitionId = processInstance != null
                ? processInstance.getProcessDefinitionId()
                : historicProcessInstance.getProcessDefinitionId();
        result.put("processInstanceId", processInstanceId);
        result.put("processDefinitionId", processDefinitionId);
        result.put("bpmnXml", getProcessBpmnXml(processDefinitionId));

        // 历史活动实例
        List<HistoricActivityInstance> activities = getHistoricActivityInstances(processInstanceId);
        List<String> finishedNodeIds = new ArrayList<>();
        List<String> currentNodeIds = new ArrayList<>();
        for (HistoricActivityInstance a : activities) {
            if (a.getActivityType() == null) {
                continue;
            }
            // 只关心 userTask / startEvent / endEvent
            String type = a.getActivityType();
            if (!("userTask".equals(type) || "startEvent".equals(type) || "endEvent".equals(type))) {
                continue;
            }
            if (a.getEndTime() != null) {
                finishedNodeIds.add(a.getActivityId());
            } else if (processInstance != null && !processInstance.isEnded()) {
                currentNodeIds.add(a.getActivityId());
            }
        }
        result.put("finishedNodeIds", finishedNodeIds);
        result.put("currentNodeIds", currentNodeIds);
        result.put("activities", activities);
        result.put("isEnded", processInstance == null || processInstance.isEnded());
        return result;
    }

    // ====================== P1-9 流程版本管理 ======================

    @Override
    public Page<ProcessDefinition> pageProcessDefinitionVersions(String processKey, Integer pageNum, Integer pageSize) {
        Page<ProcessDefinition> page = new Page<>(pageNum, pageSize);
        org.flowable.engine.repository.ProcessDefinitionQuery query = repositoryService.createProcessDefinitionQuery()
                .processDefinitionKey(processKey)
                .orderByProcessDefinitionVersion().desc();
        long total = query.count();
        java.util.List<ProcessDefinition> records = query.listPage((pageNum - 1) * pageSize, pageSize);
        page.setTotal(total);
        page.setRecords(records);
        return page;
    }

    @Override
    public Deployment rollbackToVersion(String deploymentId) {
        if (org.springframework.util.StringUtils.isEmpty(deploymentId)) {
            throw new BusinessException("deploymentId 不能为空");
        }
        // 1. 读取历史 deployment 的资源
        java.util.List<org.flowable.engine.repository.Deployment> deployments = repositoryService.createDeploymentQuery()
                .deploymentId(deploymentId).list();
        if (deployments.isEmpty()) {
            throw new BusinessException("历史部署不存在：" + deploymentId);
        }
        org.flowable.engine.repository.Deployment history = deployments.get(0);
        java.util.List<String> resourceNames = repositoryService.getDeploymentResourceNames(history.getId());
        // 2. 拿到对应 processDefinitionKey + version
        java.util.List<ProcessDefinition> defs = repositoryService.createProcessDefinitionQuery()
                .deploymentId(history.getId()).list();
        if (defs.isEmpty()) {
            throw new BusinessException("历史部署无流程定义");
        }
        ProcessDefinition def = defs.get(0);
        // 3. 重新部署（同 KEY 会 +1 版本）
        org.flowable.engine.repository.DeploymentBuilder builder = repositoryService.createDeployment()
                .name("回滚-" + history.getName() + "-v" + def.getVersion())
                .key(history.getKey());
        for (String rn : resourceNames) {
            java.io.InputStream is = repositoryService.getResourceAsStream(history.getId(), rn);
            builder.addInputStream(rn, is);
        }
        Deployment newDeployment = builder.deploy();
        log.info("回滚成功: historyDeploymentId={} → newDeploymentId={}", history.getId(), newDeployment.getId());
        return newDeployment;
    }

    @Override
    public WfDefinitionDiffDTO compareVersions(String deploymentIdA, String deploymentIdB) {
        WfDefinitionDiffDTO result = new WfDefinitionDiffDTO();
        result.setDeploymentIdA(deploymentIdA);
        result.setDeploymentIdB(deploymentIdB);
        String xmlA = loadDeploymentXml(deploymentIdA);
        String xmlB = loadDeploymentXml(deploymentIdB);
        if (xmlA == null || xmlB == null) {
            throw new BusinessException("部署资源缺失 BPMN XML");
        }
        // 取版本号
        result.setVersionA(extractVersion(deploymentIdA));
        result.setVersionB(extractVersion(deploymentIdB));
        // 解析
        java.util.Map<String, org.w3c.dom.Element> elementsA = parseBpmnElements(xmlA);
        java.util.Map<String, org.w3c.dom.Element> elementsB = parseBpmnElements(xmlB);
        java.util.List<WfDefinitionDiffDTO.DiffNode> nodeDiffs = new java.util.ArrayList<>();
        java.util.List<WfDefinitionDiffDTO.DiffNode> flowDiffs = new java.util.ArrayList<>();
        // 处理 A 中存在元素
        for (java.util.Map.Entry<String, org.w3c.dom.Element> e : elementsA.entrySet()) {
            String id = e.getKey();
            org.w3c.dom.Element ea = e.getValue();
            org.w3c.dom.Element eb = elementsB.get(id);
            WfDefinitionDiffDTO.DiffNode node = compareElement(id, ea, eb);
            if ("sequenceFlow".equals(node.getType())) {
                flowDiffs.add(node);
            } else {
                nodeDiffs.add(node);
            }
        }
        // 处理 B 独有（A 没有的）
        for (java.util.Map.Entry<String, org.w3c.dom.Element> e : elementsB.entrySet()) {
            if (elementsA.containsKey(e.getKey())) continue;
            org.w3c.dom.Element eb = e.getValue();
            WfDefinitionDiffDTO.DiffNode node = compareElement(e.getKey(), null, eb);
            if ("sequenceFlow".equals(node.getType())) {
                flowDiffs.add(node);
            } else {
                nodeDiffs.add(node);
            }
        }
        result.setNodeDiffs(nodeDiffs);
        result.setFlowDiffs(flowDiffs);
        return result;
    }

    /**
     * 加载 deployment 的 BPMN XML（取第一个 .bpmn20.xml / .bpmn 资源）
     */
    private String loadDeploymentXml(String deploymentId) {
        java.util.List<String> names = repositoryService.getDeploymentResourceNames(deploymentId);
        for (String name : names) {
            if (name.endsWith(".bpmn20.xml") || name.endsWith(".bpmn")) {
                try (java.io.InputStream is = repositoryService.getResourceAsStream(deploymentId, name)) {
                    if (is == null) return null;
                    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
                    byte[] buf = new byte[4096];
                    int len;
                    while ((len = is.read(buf)) > 0) {
                        baos.write(buf, 0, len);
                    }
                    return baos.toString(java.nio.charset.StandardCharsets.UTF_8.name());
                } catch (Exception e) {
                    log.error("读取 BPMN XML 失败: deploymentId={}, name={}", deploymentId, name, e);
                    return null;
                }
            }
        }
        return null;
    }

    /**
     * 解析 BPMN XML 中所有"流程节点 + 连线"为 Map<id, Element>
     * 限定命名空间 http://www.omg.org/spec/BPMN/20100524/MODEL
     */
    private java.util.Map<String, org.w3c.dom.Element> parseBpmnElements(String xml) {
        java.util.Map<String, org.w3c.dom.Element> map = new java.util.HashMap<>();
        try {
            javax.xml.parsers.DocumentBuilderFactory dbf = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            dbf.setNamespaceAware(true);
            javax.xml.parsers.DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document doc = db.parse(new java.io.ByteArrayInputStream(
                    xml.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
            String ns = "http://www.omg.org/spec/BPMN/20100524/MODEL";
            // 抓 userTask / serviceTask / gateway / intermediateThrowEvent / boundaryEvent / endEvent / startEvent / sequenceFlow
            String[] tags = {"userTask", "serviceTask", "exclusiveGateway", "inclusiveGateway", "parallelGateway", "eventBasedGateway", "intermediateCatchEvent", "intermediateThrowEvent", "startEvent", "endEvent", "boundaryEvent", "sequenceFlow"};
            for (String tag : tags) {
                org.w3c.dom.NodeList list = doc.getElementsByTagNameNS(ns, tag);
                for (int i = 0; i < list.getLength(); i++) {
                    org.w3c.dom.Element el = (org.w3c.dom.Element) list.item(i);
                    String id = el.getAttribute("id");
                    if (id == null || id.isEmpty()) continue;
                    el.setAttribute("__bpmn_type__", tag);
                    map.put(id, el);
                }
            }
        } catch (Exception e) {
            log.error("解析 BPMN XML 失败", e);
        }
        return map;
    }

    /**
     * 对比两个 Element（可能为 null）
     */
    private WfDefinitionDiffDTO.DiffNode compareElement(String id, org.w3c.dom.Element ea, org.w3c.dom.Element eb) {
        WfDefinitionDiffDTO.DiffNode node = new WfDefinitionDiffDTO.DiffNode();
        node.setId(id);
        org.w3c.dom.Element src = ea != null ? ea : eb;
        node.setType(src.getAttribute("__bpmn_type__"));
        node.setName(src.getAttribute("name"));
        if (ea == null) {
            node.setStatus("ADDED");
        } else if (eb == null) {
            node.setStatus("REMOVED");
        } else {
            java.util.Map<String, String[]> changes = new java.util.LinkedHashMap<>();
            // 比较 name
            String na = ea.getAttribute("name");
            String nb = eb.getAttribute("name");
            if (!na.equals(nb)) {
                changes.put("name", new String[]{na, nb});
            }
            // 比较 assignee（userTask）
            if ("userTask".equals(node.getType())) {
                String aa = ea.getAttributeNS("http://flowable.org/bpmn", "assignee");
                String ab = eb.getAttributeNS("http://flowable.org/bpmn", "assignee");
                if (!java.util.Objects.equals(aa, ab)) {
                    changes.put("assignee", new String[]{aa == null ? "" : aa, ab == null ? "" : ab});
                }
            }
            if (changes.isEmpty()) {
                node.setStatus("UNCHANGED");
            } else {
                node.setStatus("MODIFIED");
                node.setChanges(changes);
            }
        }
        return node;
    }

    private String extractVersion(String deploymentId) {
        java.util.List<ProcessDefinition> list = repositoryService.createProcessDefinitionQuery()
                .deploymentId(deploymentId).list();
        if (!list.isEmpty()) {
            return String.valueOf(list.get(0).getVersion());
        }
        return deploymentId;
    }
}
