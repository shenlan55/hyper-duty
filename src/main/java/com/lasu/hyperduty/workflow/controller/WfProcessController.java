package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.dto.DeploymentDTO;
import com.lasu.hyperduty.workflow.dto.ProcessDefinitionDTO;
import com.lasu.hyperduty.workflow.dto.WfProcessStartDTO;
import com.lasu.hyperduty.workflow.entity.WfDefinition;
import com.lasu.hyperduty.workflow.mapper.WfDefinitionMapper;
import com.lasu.hyperduty.workflow.service.WfProcessService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
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
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
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
@RequestMapping("/api/workflow/process")
@RequiredArgsConstructor
public class WfProcessController {

    private final WfProcessService wfProcessService;
    private final WfDefinitionMapper wfDefinitionMapper;
    private final RepositoryService repositoryService;

    @Operation(summary = "部署流程")
    @PostMapping("/deploy")
    public ResponseResult<DeploymentDTO> deployProcess(@RequestParam String name, @RequestBody String bpmnXml) {
        Deployment deployment = wfProcessService.deployProcess(name, bpmnXml);
        return ResponseResult.success(DeploymentDTO.fromDeployment(deployment));
    }

    @Operation(summary = "删除部署")
    @DeleteMapping("/deploy/{deploymentId}")
    public ResponseResult<Void> deleteDeployment(@PathVariable String deploymentId) {
        wfProcessService.deleteDeployment(deploymentId);
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
                        return ProcessDefinitionDTO.fromProcessDefinition(pd, wd.getFormId(), wd.getCategoryId(), wd.getRemark());
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
        
        List<WfDefinition> wfDefinitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<>());
        Map<String, WfDefinition> definitionMap = new HashMap<>();
        for (WfDefinition wd : wfDefinitions) {
            definitionMap.put(wd.getProcessDefinitionKey(), wd);
        }
        
        List<ProcessDefinitionDTO> dtoList = list.stream()
                .map(pd -> {
                    WfDefinition wd = definitionMap.get(pd.getKey());
                    if (wd != null) {
                        return ProcessDefinitionDTO.fromProcessDefinition(pd, wd.getFormId(), wd.getCategoryId(), wd.getRemark());
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
            return ResponseResult.success(ProcessDefinitionDTO.fromProcessDefinition(processDefinition, wd.getFormId(), wd.getCategoryId(), wd.getRemark()));
        }
        return ResponseResult.success(ProcessDefinitionDTO.fromProcessDefinition(processDefinition));
    }

    @Operation(summary = "同步流程定义到扩展表")
    @PostMapping("/definition/sync")
    public ResponseResult<Void> syncProcessDefinition() {
        List<ProcessDefinition> processDefinitions = wfProcessService.listProcessDefinition();
        List<WfDefinition> existingDefinitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<>());
        
        Map<String, WfDefinition> existingMap = new HashMap<>();
        for (WfDefinition wd : existingDefinitions) {
            existingMap.put(wd.getProcessDefinitionKey(), wd);
        }
        
        for (ProcessDefinition pd : processDefinitions) {
            if (!existingMap.containsKey(pd.getKey())) {
                WfDefinition wd = new WfDefinition();
                wd.setProcessDefinitionId(pd.getId());
                wd.setProcessDefinitionKey(pd.getKey());
                wd.setProcessName(pd.getName() != null ? pd.getName() : pd.getKey());
                wd.setVersion(pd.getVersion());
                wd.setStatus(1);
                wd.setCreateTime(java.time.LocalDateTime.now());
                wd.setUpdateTime(java.time.LocalDateTime.now());
                wfDefinitionMapper.insert(wd);
            }
        }
        
        return ResponseResult.success();
    }

    @Operation(summary = "绑定表单到流程")
    @PostMapping("/definition/bind-form")
    public ResponseResult<Void> bindFormToProcess(@RequestParam String processKey, @RequestParam(required = false) Long formId) {
        // 查询该流程Key的所有记录，只保留版本最高的一条
        List<WfDefinition> definitions = wfDefinitionMapper.selectList(new LambdaQueryWrapper<WfDefinition>()
                .eq(WfDefinition::getProcessDefinitionKey, processKey)
                .orderByDesc(WfDefinition::getVersion)
                .last("LIMIT 1"));
        
        WfDefinition wd = definitions.isEmpty() ? null : definitions.get(0);
        
        if (wd == null) {
            throw new com.lasu.hyperduty.common.exception.BusinessException("流程定义不存在，请先同步流程定义");
        }
        
        wd.setFormId(formId);
        wd.setUpdateTime(java.time.LocalDateTime.now());
        wfDefinitionMapper.updateById(wd);
        
        return ResponseResult.success();
    }

    @Operation(summary = "获取流程BPMN XML")
    @GetMapping("/definition/bpmn/{processDefinitionId}")
    public ResponseResult<String> getProcessBpmnXml(@PathVariable String processDefinitionId) {
        String bpmnXml = wfProcessService.getProcessBpmnXml(processDefinitionId);
        log.info("返回流程XML, 长度: {}, 内容预览: {}", bpmnXml.length(), 
                bpmnXml.length() > 100 ? bpmnXml.substring(0, 100) + "..." : bpmnXml);
        return ResponseResult.success(bpmnXml);
    }

    @Operation(summary = "启动流程")
    @PostMapping("/start")
    public ResponseResult<ProcessInstance> startProcess(@RequestBody WfProcessStartDTO dto) {
        ProcessInstance processInstance = wfProcessService.startProcess(dto);
        return ResponseResult.success(processInstance);
    }

    @Operation(summary = "获取流程实例")
    @GetMapping("/instance/{processInstanceId}")
    public ResponseResult<ProcessInstance> getProcessInstance(@PathVariable String processInstanceId) {
        ProcessInstance processInstance = wfProcessService.getProcessInstance(processInstanceId);
        return ResponseResult.success(processInstance);
    }

    @Operation(summary = "分页查询我发起的流程")
    @GetMapping("/instance/my/page")
    public ResponseResult<Page<ProcessInstance>> pageMyStartedProcess(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<ProcessInstance> page = wfProcessService.pageMyStartedProcess(pageNum, pageSize, userId);
        return ResponseResult.success(page);
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

    @Operation(summary = "调试：列出所有流程定义")
    @GetMapping("/debug/list-all")
    public ResponseResult<List<Map<String, Object>>> listAllProcessDefinitions() {
        List<ProcessDefinition> definitions = repositoryService.createProcessDefinitionQuery().list();
        List<Map<String, Object>> result = new ArrayList<>();
        
        for (ProcessDefinition pd : definitions) {
            Map<String, Object> info = new HashMap<>();
            info.put("id", pd.getId());
            info.put("key", pd.getKey());
            info.put("name", pd.getName());
            info.put("version", pd.getVersion());
            info.put("deploymentId", pd.getDeploymentId());
            info.put("resourceName", pd.getResourceName());
            result.add(info);
        }
        
        log.info("调试：找到 {} 个流程定义", result.size());
        return ResponseResult.success(result);
    }
}
