package com.lasu.hyperduty.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysDictData;
import com.lasu.hyperduty.system.service.SysDictDataService;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
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

    /**
     * 按 dict_code 批量查询字典数据
     * 用于前端动态加载业务枚举（任务状态/班次/审批状态/是否等）
     * 例：GET /dict/data/byCodes?codes=task_status,task_priority,approval_status
     * @param codes 逗号分隔的字典编码
     * @return Map<dictCode, List<SysDictData>>
     */
    @GetMapping("/byCodes")
    public ResponseResult<Map<String, List<SysDictData>>> getByDictTypeCodes(
            @RequestParam("codes") String codes) {
        if (!StringUtils.hasText(codes)) {
            return ResponseResult.success(java.util.Collections.emptyMap());
        }
        List<String> codeList = Arrays.stream(codes.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .toList();
        Map<String, List<SysDictData>> result = sysDictDataService.getByDictTypeCodes(codeList);
        return ResponseResult.success(result);
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