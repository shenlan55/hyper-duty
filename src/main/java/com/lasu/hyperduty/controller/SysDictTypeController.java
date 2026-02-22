package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SysDictType;
import com.lasu.hyperduty.service.SysDictTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dict/type")
public class SysDictTypeController {

    @Autowired
    private SysDictTypeService sysDictTypeService;

    @GetMapping("/list")
    public ResponseResult<Page<SysDictType>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysDictType> page = new Page<>(pageNum, pageSize);
        Page<SysDictType> dictTypePage = sysDictTypeService.page(page);
        return ResponseResult.success(dictTypePage);
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<SysDictType> getById(@PathVariable Long id) {
        SysDictType dictType = sysDictTypeService.getById(id);
        return ResponseResult.success(dictType);
    }

    @PostMapping
    @RateLimit(window = 60, max = 20, message = "保存字典类型操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> save(@RequestBody SysDictType sysDictType) {
        boolean result = sysDictTypeService.save(sysDictType);
        return ResponseResult.success(result);
    }

    @PutMapping
    @RateLimit(window = 60, max = 20, message = "更新字典类型操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> update(@RequestBody SysDictType sysDictType) {
        boolean result = sysDictTypeService.updateById(sysDictType);
        return ResponseResult.success(result);
    }

    @DeleteMapping("/{id}")
    @RateLimit(window = 60, max = 20, message = "删除字典类型操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        boolean result = sysDictTypeService.removeById(id);
        return ResponseResult.success(result);
    }
}