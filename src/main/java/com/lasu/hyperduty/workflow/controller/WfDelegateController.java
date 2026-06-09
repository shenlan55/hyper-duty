package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.dto.WfDelegateConfigDTO;
import com.lasu.hyperduty.workflow.entity.WfDelegate;
import com.lasu.hyperduty.workflow.service.WfDelegateService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 委托管理控制器
 */
@Tag(name = "委托管理", description = "委托管理接口")
@RestController
@RequestMapping("/workflow/delegate")
@RequiredArgsConstructor
public class WfDelegateController {

    private final WfDelegateService wfDelegateService;

    @Operation(summary = "分页查询委托配置")
    @GetMapping("/page")
    public ResponseResult<Page<WfDelegate>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Long userId = SecurityUtil.getCurrentUserId();
        Page<WfDelegate> page = wfDelegateService.pageList(pageNum, pageSize, userId);
        return ResponseResult.success(page);
    }

    @Operation(summary = "创建委托配置")
    @PostMapping
    public ResponseResult<WfDelegate> createDelegate(@RequestBody WfDelegateConfigDTO dto) {
        WfDelegate delegate = wfDelegateService.createDelegate(dto);
        return ResponseResult.success(delegate);
    }

    @Operation(summary = "更新委托配置")
    @PutMapping("/{id}")
    public ResponseResult<WfDelegate> updateDelegate(@PathVariable Long id, @RequestBody WfDelegateConfigDTO dto) {
        WfDelegate delegate = wfDelegateService.updateDelegate(id, dto);
        return ResponseResult.success(delegate);
    }

    @Operation(summary = "删除委托配置")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteDelegate(@PathVariable Long id) {
        wfDelegateService.deleteDelegate(id);
        return ResponseResult.success();
    }

    @Operation(summary = "启用委托配置")
    @PostMapping("/enable/{id}")
    public ResponseResult<Void> enableDelegate(@PathVariable Long id) {
        wfDelegateService.toggleDelegate(id, 1);
        return ResponseResult.success();
    }

    @Operation(summary = "禁用委托配置")
    @PostMapping("/disable/{id}")
    public ResponseResult<Void> disableDelegate(@PathVariable Long id) {
        wfDelegateService.toggleDelegate(id, 0);
        return ResponseResult.success();
    }
}
