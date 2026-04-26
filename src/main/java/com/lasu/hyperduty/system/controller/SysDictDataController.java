package com.lasu.hyperduty.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.service.SysDictDataService;
import java.util.List;
import lombok.Data;
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
@RequestMapping("/dict/data")
public class SysDictDataController {

    @Autowired
    private SysDictDataService sysDictDataService;

    @GetMapping("/list")
    public ResponseResult<Page<SysDictData>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long dictTypeId,
            @RequestParam(required = false) String keyword) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        Page<SysDictData> dictDataPage = sysDictDataService.page(page, dictTypeId, keyword);
        return ResponseResult.success(dictDataPage);
    }

    @GetMapping("/byType/{dictTypeId}")
    public ResponseResult<List<SysDictData>> getByDictTypeId(@PathVariable Long dictTypeId) {
        List<SysDictData> dataList = sysDictDataService.getByDictTypeId(dictTypeId);
        return ResponseResult.success(dataList);
    }

    @GetMapping("/detail/{id}")
    public ResponseResult<SysDictData> getById(@PathVariable Long id) {
        SysDictData dictData = sysDictDataService.getById(id);
        return ResponseResult.success(dictData);
    }

    @PostMapping
    @RateLimit(window = 60, max = 20, message = "保存字典数据操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> save(@RequestBody SysDictData sysDictData) {
        boolean result = sysDictDataService.save(sysDictData);
        return ResponseResult.success(result);
    }

    @PutMapping
    @RateLimit(window = 60, max = 20, message = "更新字典数据操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> update(@RequestBody SysDictData sysDictData) {
        boolean result = sysDictDataService.updateById(sysDictData);
        return ResponseResult.success(result);
    }

    @DeleteMapping("/{id}")
    @RateLimit(window = 60, max = 20, message = "删除字典数据操作过于频繁，请60秒后再试")
    public ResponseResult<Boolean> delete(@PathVariable Long id) {
        boolean result = sysDictDataService.removeById(id);
        return ResponseResult.success(result);
    }
}