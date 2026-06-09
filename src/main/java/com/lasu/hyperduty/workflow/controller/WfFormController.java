package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.entity.WfForm;
import com.lasu.hyperduty.workflow.service.WfFormService;
import com.lasu.hyperduty.common.utils.SecurityUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 表单管理控制器
 */
@Tag(name = "表单管理", description = "表单管理接口")
@RestController
@RequestMapping("/workflow/form")
@RequiredArgsConstructor
public class WfFormController {

    private final WfFormService wfFormService;

    @Operation(summary = "分页查询表单")
    @GetMapping("/page")
    public ResponseResult<Page<WfForm>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String name) {
        Page<WfForm> page = wfFormService.pageList(pageNum, pageSize, name);
        return ResponseResult.success(page);
    }

    @Operation(summary = "创建表单")
    @PostMapping
    public ResponseResult<WfForm> createForm(@RequestBody WfForm form) {
        Long userId = SecurityUtil.getCurrentUserId();
        form.setCreateUserId(userId);
        WfForm result = wfFormService.createForm(form);
        return ResponseResult.success(result);
    }

    @Operation(summary = "更新表单")
    @PutMapping
    public ResponseResult<WfForm> updateForm(@RequestBody WfForm form) {
        WfForm result = wfFormService.updateForm(form);
        return ResponseResult.success(result);
    }

    @Operation(summary = "删除表单")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteForm(@PathVariable Long id) {
        wfFormService.deleteForm(id);
        return ResponseResult.success();
    }

    @Operation(summary = "获取表单详情")
    @GetMapping("/{id}")
    public ResponseResult<WfForm> getFormDetail(@PathVariable Long id) {
        WfForm form = wfFormService.getFormDetail(id);
        return ResponseResult.success(form);
    }
}
