package com.lasu.hyperduty.workflow.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.dto.WfNodeHandlerDTO;
import com.lasu.hyperduty.workflow.entity.WfNodeHandler;
import com.lasu.hyperduty.workflow.service.WfNodeHandlerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 节点处理人配置控制器
 *
 * <p>由 BPMN 设计师属性面板调用，支持按节点保存处理人类型 / 多实例 / 抄送 等配置。
 * 部署流程时按 processDefinitionId 批量保存或覆盖。
 */
@Tag(name = "节点处理人配置", description = "BPMN 节点处理人/会签/抄送配置")
@RestController
@RequestMapping("/workflow/node-handler")
@RequiredArgsConstructor
public class WfNodeHandlerController {

    private final WfNodeHandlerService wfNodeHandlerService;

    @Operation(summary = "保存或更新单个节点处理人配置")
    @PostMapping("/save")
    public ResponseResult<WfNodeHandler> save(@RequestBody WfNodeHandlerDTO dto) {
        WfNodeHandler saved = wfNodeHandlerService.saveNodeHandler(dto);
        return ResponseResult.success(saved);
    }

    @Operation(summary = "按流程批量保存节点处理人配置（部署时调用，覆盖式）")
    @PostMapping("/save-batch")
    public ResponseResult<Void> saveBatch(@RequestParam String processDefinitionId,
                                          @RequestParam String processDefinitionKey,
                                          @RequestBody List<WfNodeHandlerDTO> handlers) {
        wfNodeHandlerService.saveBatch(processDefinitionId, processDefinitionKey, handlers);
        return ResponseResult.success();
    }

    @Operation(summary = "查询流程的节点处理人配置列表")
    @GetMapping("/list-by-definition")
    public ResponseResult<List<WfNodeHandler>> listByDefinition(@RequestParam String processDefinitionId) {
        List<WfNodeHandler> list = wfNodeHandlerService.listByProcessDefinitionId(processDefinitionId);
        return ResponseResult.success(list);
    }

    @Operation(summary = "查询流程 KEY 的最新版本节点处理人配置")
    @GetMapping("/list-by-key")
    public ResponseResult<List<WfNodeHandler>> listByKey(@RequestParam String processDefinitionKey) {
        List<WfNodeHandler> list = wfNodeHandlerService.listByProcessDefinitionKey(processDefinitionKey);
        return ResponseResult.success(list);
    }

    @Operation(summary = "删除节点处理人配置")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        wfNodeHandlerService.deleteById(id);
        return ResponseResult.success();
    }

    @Operation(summary = "删除流程的所有节点处理人配置")
    @DeleteMapping("/delete-by-definition")
    public ResponseResult<Void> deleteByDefinition(@RequestParam String processDefinitionId) {
        wfNodeHandlerService.deleteByProcessDefinitionId(processDefinitionId);
        return ResponseResult.success();
    }
}
