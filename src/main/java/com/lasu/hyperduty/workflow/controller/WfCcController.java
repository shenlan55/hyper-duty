package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import com.lasu.hyperduty.workflow.dto.WfCcDTO;
import com.lasu.hyperduty.workflow.entity.WfCc;
import com.lasu.hyperduty.workflow.service.WfCcService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 流程抄送控制器
 */
@Tag(name = "流程抄送", description = "抄送我的 / 标记已读 / 主动抄送")
@RestController
@RequestMapping("/workflow/cc")
@RequiredArgsConstructor
public class WfCcController {

    private final WfCcService wfCcService;

    @Operation(summary = "分页查询抄送我的列表")
    @GetMapping("/mine/page")
    public ResponseResult<Page<WfCc>> pageMine(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Integer readStatus) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<WfCc> page = wfCcService.pageMine(userId, pageNum, pageSize, readStatus);
        return ResponseResult.success(page);
    }

    @Operation(summary = "新增抄送（任务办理时手动选人）")
    @PostMapping
    public ResponseResult<WfCc> create(@RequestBody WfCcDTO dto) {
        WfCc result = wfCcService.createCc(dto);
        return ResponseResult.success(result);
    }

    @Operation(summary = "标记单条已读")
    @PostMapping("/read/{id}")
    public ResponseResult<Void> markRead(@PathVariable Long id) {
        Long userId = SecurityUtil.getCurrentUserId();
        wfCcService.markRead(id, userId);
        return ResponseResult.success();
    }

    @Operation(summary = "全部标记已读")
    @PostMapping("/read-all")
    public ResponseResult<Integer> markAllRead() {
        Long userId = SecurityUtil.getCurrentUserId();
        int count = wfCcService.markAllRead(userId);
        return ResponseResult.success(count);
    }

    @Operation(summary = "节点触发批量抄送（流程引擎内部调用）")
    @PostMapping("/batch-for-node")
    public ResponseResult<Void> batchForNode(@RequestBody WfCcDTO dto) {
        wfCcService.batchCreateForNode(
                dto.getProcessInstanceId(),
                dto.getProcessDefinitionId(),
                dto.getProcessName(),
                dto.getNodeId(),
                dto.getNodeName(),
                dto.getCcUserIds(),
                dto.getFromUserId(),
                dto.getFromUserName(),
                dto.getTitle(),
                dto.getContent()
        );
        return ResponseResult.success();
    }
}
