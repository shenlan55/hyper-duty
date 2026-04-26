package com.lasu.hyperduty.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysDictType;
import com.lasu.hyperduty.system.service.SysDictTypeService;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;







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