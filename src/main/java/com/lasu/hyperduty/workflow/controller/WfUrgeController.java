package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.dto.WfUrgeDTO;
import com.lasu.hyperduty.workflow.entity.WfUrgeRecord;
import com.lasu.hyperduty.workflow.service.WfUrgeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 流程催办接口
 * ----------------------------------------------------------------------------
 * 提供：发起催办 / 我发起的催办 / 我接收的催办
 * ----------------------------------------------------------------------------
 */
@Tag(name = "流程催办", description = "流程催办（URGE）接口")
@RestController
@RequestMapping("/workflow/urge")
@RequiredArgsConstructor
public class WfUrgeController {

    private final WfUrgeService wfUrgeService;

    @Operation(summary = "发起催办")
    @PostMapping
    public ResponseResult<Void> urge(@RequestBody WfUrgeDTO dto) {
        wfUrgeService.urge(dto);
        return ResponseResult.success();
    }

    @Operation(summary = "我发起的催办（分页）")
    @GetMapping("/page/sent")
    public ResponseResult<IPage<WfUrgeRecord>> pageSent(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseResult.success(wfUrgeService.pageSent(new Page<>(current, size)));
    }

    @Operation(summary = "我接收的催办（分页）")
    @GetMapping("/page/received")
    public ResponseResult<IPage<WfUrgeRecord>> pageReceived(
            @RequestParam(defaultValue = "1") long current,
            @RequestParam(defaultValue = "10") long size) {
        return ResponseResult.success(wfUrgeService.pageReceived(new Page<>(current, size)));
    }
}
