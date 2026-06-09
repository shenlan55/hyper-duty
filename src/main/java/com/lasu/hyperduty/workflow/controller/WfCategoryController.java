package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.entity.WfCategory;
import com.lasu.hyperduty.workflow.service.WfCategoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 流程分类控制器
 */
@Tag(name = "流程分类管理", description = "流程分类管理接口")
@RestController
@RequestMapping("/workflow/category")
@RequiredArgsConstructor
public class WfCategoryController {

    private final WfCategoryService wfCategoryService;

    @Operation(summary = "分页查询分类")
    @GetMapping("/page")
    public ResponseResult<Page<WfCategory>> pageList(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<WfCategory> page = wfCategoryService.pageList(pageNum, pageSize);
        return ResponseResult.success(page);
    }

    @Operation(summary = "查询分类列表")
    @GetMapping("/list")
    public ResponseResult<Page<WfCategory>> list() {
        Page<WfCategory> page = wfCategoryService.pageList(1, 1000);
        return ResponseResult.success(page);
    }

    @Operation(summary = "创建分类")
    @PostMapping
    public ResponseResult<WfCategory> createCategory(@RequestBody WfCategory category) {
        WfCategory result = wfCategoryService.createCategory(category);
        return ResponseResult.success(result);
    }

    @Operation(summary = "更新分类")
    @PutMapping
    public ResponseResult<WfCategory> updateCategory(@RequestBody WfCategory category) {
        WfCategory result = wfCategoryService.updateCategory(category);
        return ResponseResult.success(result);
    }

    @Operation(summary = "删除分类")
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteCategory(@PathVariable Long id) {
        wfCategoryService.deleteCategory(id);
        return ResponseResult.success();
    }

    @Operation(summary = "获取分类详情")
    @GetMapping("/{id}")
    public ResponseResult<WfCategory> getCategoryDetail(@PathVariable Long id) {
        WfCategory category = wfCategoryService.getCategoryDetail(id);
        return ResponseResult.success(category);
    }
}
