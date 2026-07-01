package com.lasu.hyperduty.workflow.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.workflow.entity.WfTemplate;
import com.lasu.hyperduty.workflow.service.WfTemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 流程模板市场 Controller（P3-1）
 * ----------------------------------------------------------------------------
 * - GET  /api/wf/template/page   分页
 * - GET  /api/wf/template/list   全部启用（用于下拉/市场）
 * - GET  /api/wf/template/{id}   详情
 * - POST /api/wf/template        新建
 * - PUT  /api/wf/template        更新
 * - DELETE /api/wf/template/{id} 删除
 * - POST /api/wf/template/{id}/use  使用模板（+1 useCount）
 * ----------------------------------------------------------------------------
 */
@Slf4j
@RestController
@RequestMapping("/api/wf/template")
@RequiredArgsConstructor
public class WfTemplateController {

    private final WfTemplateService service;

    @GetMapping("/page")
    public ResponseResult<Page<WfTemplate>> page(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status) {
        return ResponseResult.success(service.pageTemplates(pageNum, pageSize, category, status));
    }

    @GetMapping("/list")
    public ResponseResult<Object> list(@RequestParam(required = false) String category) {
        return ResponseResult.success(service.listActive(category));
    }

    @GetMapping("/{id}")
    public ResponseResult<WfTemplate> getById(@PathVariable Long id) {
        return ResponseResult.success(service.getById(id));
    }

    @PostMapping
    public ResponseResult<Void> create(@RequestBody WfTemplate template) {
        service.saveTemplate(template);
        return ResponseResult.success();
    }

    @PutMapping
    public ResponseResult<Void> update(@RequestBody WfTemplate template) {
        service.saveTemplate(template);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> delete(@PathVariable Long id) {
        service.deleteTemplate(id);
        return ResponseResult.success();
    }

    @PostMapping("/{id}/use")
    public ResponseResult<Void> use(@PathVariable Long id) {
        service.incrementUseCount(id);
        return ResponseResult.success();
    }
}
