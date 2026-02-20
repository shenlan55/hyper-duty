package com.lasu.hyperduty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SysDictData;
import com.lasu.hyperduty.service.SysDictDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dict/data")
public class SysDictDataController {

    @Autowired
    private SysDictDataService sysDictDataService;

    @GetMapping("/list")
    public ResponseResult<Page<SysDictData>> list(@RequestParam(defaultValue = "1") Integer pageNum,
                                                   @RequestParam(defaultValue = "10") Integer pageSize) {
        Page<SysDictData> page = new Page<>(pageNum, pageSize);
        Page<SysDictData> dictDataPage = sysDictDataService.page(page);
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