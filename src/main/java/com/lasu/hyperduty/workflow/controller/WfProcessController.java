package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.workflow.dto.*;
import com.lasu.hyperduty.workflow.entity.WfDefinition;
import com.lasu.hyperduty.workflow.mapper.WfDefinitionMapper;
import com.lasu.hyperduty.workflow.service.WfProcessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 流程管理控制器
 */
@Slf4j
@Tag(name = "流程管理", description = "流程管理接口")
@RestController
@RequestMapping("/workflow/process")
@RequiredArgsConstructor
public class WfProcessController {

    private final WfProcessService wfProcessService;
    private final WfDefinitionMapper wfDefinitionMapper;
    private final RepositoryService repositoryService;

    @Operation(summary = "部署流程")
    @PostMapping("/deploy")
    public ResponseResult<DeploymentDTO> deployProcess(@RequestBody DeployProcessDTO dto) {
        log.info("部署流程: name={}, bpmnXml长度={}", dto.getName(), dto.getBpmnXml() != null ? dto.getBpmnXml().length() : 0);
        Deployment deployment = wfProcessService.deployProcess(dto.getName(), dto.getBpmnXml());
        return ResponseResult.success(DeploymentDTO.fromDeployment(deployment));
    }

    @Operation(summary = "删除部署")
    @DeleteMapping("/deploy/{deploymentId}")
    public ResponseResult<Void> deleteDeployment(@PathVariable String deploymentId) {
        wfProcessService.deleteDeployment(deploymentId);
        // 同步清理扩展表：删除后重新同步，清除孤儿记录
        syncProcessDefinition();
        return ResponseResult.success();
    }

    @Operation(summary = "分页查询流程定义")
    @GetMapping("/definition/page")
    public ResponseResult<Page<ProcessDefinitionDTO>> pageProcessDefinition(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String processKey) {
        Page<ProcessDefinition> page = wfProcessService.pageProcessDefinition(pageNum, pageSize, processKey);
        
        List<WfDefinition> wfDefinitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, WfDefinition> definitionMap = new HashMap<>();
        for (WfDefinition wd : wfDefinitions) {
            definitionMap.put(wd.getProcessDefinitionKey(), wd);
        }
        
        Page<ProcessDefinitionDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(pd -> {
                    WfDefinition wd = definitionMap.get(pd.getKey());
                    if (wd != null) {
                        return ProcessDefinitionDTO.fromProcessDefinition(pd, wd.getFormId(), wd.getCategoryId(), wd.getRemark(), wd.getProcessName());
                    }
                    return ProcessDefinitionDTO.fromProcessDefinition(pd);
                })
                .collect(Collectors.toList()));
        return ResponseResult.success(dtoPage);
    }

    @Operation(summary = "获取流程定义列表")
    @GetMapping("/definition/list")
    public ResponseResult<List<ProcessDefinitionDTO>> listProcessDefinition() {
        List<ProcessDefinition> list = wfProcessService.listProcessDefinition();
        
        // 按key去重，取最新版本的wf_definition（避免重复key导致多条记录）
        List<WfDefinition> wfDefinitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, WfDefinition> definitionMap = new HashMap<>();
        for (WfDefinition wd : wfDefinitions) {
            WfDefinition existing = definitionMap.get(wd.getProcessDefinitionKey());
            if (existing == null || wd.getVersion() > existing.getVersion()) {
                definitionMap.put(wd.getProcessDefinitionKey(), wd);
            }
        }
        
        List<ProcessDefinitionDTO> dtoList = list.stream()
                .map(pd -> {
                    WfDefinition wd = definitionMap.get(pd.getKey());
                    if (wd != null) {
                        return ProcessDefinitionDTO.fromProcessDefinition(pd, wd.getFormId(), wd.getCategoryId(), wd.getRemark(), wd.getProcessName());
                    }
                    return ProcessDefinitionDTO.fromProcessDefinition(pd);
                })
                .collect(Collectors.toList());
        return ResponseResult.success(dtoList);
    }

    @Operation(summary = "获取最新版本流程定义")
    @GetMapping("/definition/latest/{processKey}")
    public ResponseResult<ProcessDefinitionDTO> getLatestProcessDefinition(@PathVariable String processKey) {
        ProcessDefinition processDefinition = wfProcessService.getLatestProcessDefinition(processKey);
        
        List<WfDefinition> definitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getProcessDefinitionKey, processKey)
                .orderByDesc(WfDefinition::getVersion)
                .last("LIMIT 1"));
        
        WfDefinition wd = definitions.isEmpty() ? null : definitions.get(0);
        
        if (wd != null) {
            return ResponseResult.success(ProcessDefinitionDTO.fromProcessDefinition(processDefinition, wd.getFormId(), wd.getCategoryId(), wd.getRemark(), wd.getProcessName()));
        }
        return ResponseResult.success(ProcessDefinitionDTO.fromProcessDefinition(processDefinition));
    }

    @Operation(summary = "同步流程定义到扩展表")
    @PostMapping("/definition/sync")
    @Transactional(rollbackFor = Exception.class)
    public ResponseResult<Void> syncProcessDefinition() {
        log.info("开始同步流程定义到扩展表...");
        List<ProcessDefinition> processDefinitions = wfProcessService.listProcessDefinition();
        List<WfDefinition> existingDefinitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<>());
        log.info("Flowable流程定义数: {}, 扩展表记录数: {}", processDefinitions.size(), existingDefinitions.size());
        
        // 构建Flowable中存在的key集合
        Map<String, ProcessDefinition> flowablePdMap = new HashMap<>();
        for (ProcessDefinition pd : processDefinitions) {
            flowablePdMap.put(pd.getKey(), pd);
        }
        
        // 构建已存在记录的key→最新版本映射
        Map<String, WfDefinition> existingMap = new HashMap<>();
        for (WfDefinition wd : existingDefinitions) {
            WfDefinition existing = existingMap.get(wd.getProcessDefinitionKey());
            if (existing == null || wd.getVersion() > existing.getVersion()) {
                existingMap.put(wd.getProcessDefinitionKey(), wd);
            }
        }
        
        int updateCount = 0;
        int insertCount = 0;
        
        // 新增Flowable中存在但扩展表不存在的，更新已存在的
        for (ProcessDefinition pd : processDefinitions) {
            if (existingMap.containsKey(pd.getKey())) {
                // 更新已有记录
                WfDefinition wd = existingMap.get(pd.getKey());
                wd.setProcessDefinitionId(pd.getId());
                wd.setProcessName(pd.getName() != null ? pd.getName() : pd.getKey());
                wd.setVersion(pd.getVersion());
                wd.setUpdateTime(java.time.LocalDateTime.now());
                wfDefinitionMapper.updateById(wd);
                updateCount++;
            } else {
                // 插入新记录
                WfDefinition wd = new WfDefinition();
                wd.setProcessDefinitionId(pd.getId());
                wd.setProcessDefinitionKey(pd.getKey());
                wd.setProcessName(pd.getName() != null ? pd.getName() : pd.getKey());
                wd.setVersion(pd.getVersion());
                wd.setStatus(1);
                wd.setCreateTime(java.time.LocalDateTime.now());
                wd.setUpdateTime(java.time.LocalDateTime.now());
                wfDefinitionMapper.insert(wd);
                insertCount++;
            }
        }
        
        // 删除Flowable中已不存在的记录
        int deleteCount = 0;
        for (WfDefinition wd : existingDefinitions) {
            if (!flowablePdMap.containsKey(wd.getProcessDefinitionKey())) {
                wfDefinitionMapper.deleteById(wd.getId());
                deleteCount++;
            }
        }
        
        log.info("同步完成: 更新{}条, 新增{}条, 删除{}条", updateCount, insertCount, deleteCount);
        return ResponseResult.success();
    }

    @Operation(summary = "绑定表单到流程")
    @PostMapping("/definition/bind-form")
    public ResponseResult<Void> bindFormToProcess(@RequestBody BindFormDTO dto) {
        // 查询该流程Key的最新版本记录
        List<WfDefinition> definitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getProcessDefinitionKey, dto.getProcessKey())
                .orderByDesc(WfDefinition::getVersion));
        
        if (definitions.isEmpty()) {
            throw new com.lasu.hyperduty.common.exception.BusinessException("流程定义不存在，请先同步流程定义");
        }
        
        // 更新所有同key的记录为相同的formId
        for (WfDefinition wd : definitions) {
            wd.setFormId(dto.getFormId());
            wd.setUpdateTime(java.time.LocalDateTime.now());
            wfDefinitionMapper.updateById(wd);
        }
        
        return ResponseResult.success();
    }

    @Operation(summary = "获取流程BPMN XML")
    @GetMapping("/definition/bpmn-xml")
    public ResponseResult<String> getProcessBpmnXml(@RequestParam String processDefinitionId) {
        String bpmnXml = wfProcessService.getProcessBpmnXml(processDefinitionId);
        log.info("返回流程XML, processDefinitionId={}, length={}", processDefinitionId, bpmnXml.length());
        // 使用 success(message, data) 重载，避免 String 类型被误识别为 message
        return ResponseResult.success("success", bpmnXml);
    }

    @Operation(summary = "启动流程")
    @PostMapping("/start")
    public ResponseResult<ProcessInstanceDTO> startProcess(@RequestBody WfProcessStartDTO dto) {
        ProcessInstance processInstance = wfProcessService.startProcess(dto);
        return ResponseResult.success(ProcessInstanceDTO.fromProcessInstance(processInstance));
    }

    @Operation(summary = "获取流程实例")
    @GetMapping("/instance/{processInstanceId}")
    public ResponseResult<ProcessInstanceDTO> getProcessInstance(@PathVariable String processInstanceId) {
        ProcessInstance processInstance = wfProcessService.getProcessInstance(processInstanceId);
        return ResponseResult.success(ProcessInstanceDTO.fromProcessInstance(processInstance));
    }

    @Operation(summary = "分页查询我发起的流程（运行中）")
    @GetMapping("/instance/my/page")
    public ResponseResult<Page<ProcessInstanceDTO>> pageMyStartedProcess(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<ProcessInstance> page = wfProcessService.pageMyStartedProcess(pageNum, pageSize, userId);
        
        Page<ProcessInstanceDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(ProcessInstanceDTO::fromProcessInstance)
                .collect(Collectors.toList()));
        return ResponseResult.success(dtoPage);
    }

    @Operation(summary = "分页查询我已完成的流程（历史）")
    @GetMapping("/instance/my/completed/page")
    public ResponseResult<Page<HistoricProcessInstanceDTO>> pageMyCompletedProcess(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<org.flowable.engine.history.HistoricProcessInstance> page = wfProcessService.pageMyCompletedProcess(pageNum, pageSize, userId);
        
        Page<HistoricProcessInstanceDTO> dtoPage = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        dtoPage.setRecords(page.getRecords().stream()
                .map(HistoricProcessInstanceDTO::fromHistoricProcessInstance)
                .collect(Collectors.toList()));
        return ResponseResult.success(dtoPage);
    }

    @Operation(summary = "获取流程变量")
    @GetMapping("/instance/variables/{processInstanceId}")
    public ResponseResult<Map<String, Object>> getProcessVariables(@PathVariable String processInstanceId) {
        Map<String, Object> variables = wfProcessService.getProcessVariables(processInstanceId);
        return ResponseResult.success(variables);
    }

    @Operation(summary = "设置流程变量")
    @PostMapping("/instance/variables/{processInstanceId}")
    public ResponseResult<Void> setProcessVariables(@PathVariable String processInstanceId, @RequestBody Map<String, Object> variables) {
        wfProcessService.setProcessVariables(processInstanceId, variables);
        return ResponseResult.success();
    }

    @Operation(summary = "作废流程")
    @PostMapping("/instance/cancel/{processInstanceId}")
    public ResponseResult<Void> cancelProcess(@PathVariable String processInstanceId, @RequestParam String reason) {
        wfProcessService.cancelProcess(processInstanceId, reason);
        return ResponseResult.success();
    }

    @Operation(summary = "挂起流程")
    @PostMapping("/instance/suspend/{processInstanceId}")
    public ResponseResult<Void> suspendProcess(@PathVariable String processInstanceId) {
        wfProcessService.suspendProcess(processInstanceId);
        return ResponseResult.success();
    }

    @Operation(summary = "激活流程")
    @PostMapping("/instance/activate/{processInstanceId}")
    public ResponseResult<Void> activateProcess(@PathVariable String processInstanceId) {
        wfProcessService.activateProcess(processInstanceId);
        return ResponseResult.success();
    }

    @Operation(summary = "获取流程历史活动实例（用于跟踪）")
    @GetMapping("/instance/history/activities/{processInstanceId}")
    public ResponseResult<List<HistoricActivityInstance>> getHistoricActivityInstances(@PathVariable String processInstanceId) {
        List<HistoricActivityInstance> list = wfProcessService.getHistoricActivityInstances(processInstanceId);
        return ResponseResult.success(list);
    }

    @Operation(summary = "获取历史流程实例")
    @GetMapping("/instance/history/{processInstanceId}")
    public ResponseResult<HistoricProcessInstance> getHistoricProcessInstance(@PathVariable String processInstanceId) {
        HistoricProcessInstance instance = wfProcessService.getHistoricProcessInstance(processInstanceId);
        return ResponseResult.success(instance);
    }

    @Operation(summary = "发起人撤回流程（需未结束、未审批）")
    @PostMapping("/instance/withdraw/{processInstanceId}")
    public ResponseResult<Void> withdrawProcess(@PathVariable String processInstanceId, @RequestParam(required = false) String reason) {
        wfProcessService.withdrawProcess(processInstanceId, reason);
        return ResponseResult.success();
    }

    @Operation(summary = "获取流程跟踪图数据：含 BPMN XML、已完成节点、当前节点")
    @GetMapping("/instance/trace/{processInstanceId}")
    public ResponseResult<Map<String, Object>> getProcessTraceData(@PathVariable String processInstanceId) {
        Map<String, Object> data = wfProcessService.getProcessTraceData(processInstanceId);
        return ResponseResult.success(data);
    }

}
