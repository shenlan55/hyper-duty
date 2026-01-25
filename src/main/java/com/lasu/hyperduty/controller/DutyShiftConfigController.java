package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/duty/shift-config")
public class DutyShiftConfigController {

    @Autowired
    private DutyShiftConfigService dutyShiftConfigService;

    @GetMapping("/list")
    public ResponseResult<List<DutyShiftConfig>> getAllShiftConfigs() {
        List<DutyShiftConfig> list = dutyShiftConfigService.list();
        return ResponseResult.success(list);
    }

    @GetMapping("/{id}")
    public ResponseResult<DutyShiftConfig> getShiftConfigById(@PathVariable Long id) {
        DutyShiftConfig config = dutyShiftConfigService.getById(id);
        return ResponseResult.success(config);
    }

    @PostMapping
    public ResponseResult<Void> addShiftConfig(@Validated @RequestBody DutyShiftConfig shiftConfig) {
        dutyShiftConfigService.save(shiftConfig);
        return ResponseResult.success();
    }

    @PutMapping
    public ResponseResult<Void> updateShiftConfig(@Validated @RequestBody DutyShiftConfig shiftConfig) {
        dutyShiftConfigService.updateById(shiftConfig);
        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteShiftConfig(@PathVariable Long id) {
        dutyShiftConfigService.removeById(id);
        return ResponseResult.success();
    }

    @PutMapping("/status/{id}")
    public ResponseResult<Void> updateStatus(@PathVariable Long id, @RequestParam Integer status) {
        DutyShiftConfig config = dutyShiftConfigService.getById(id);
        if (config != null) {
            config.setStatus(status);
            dutyShiftConfigService.updateById(config);
        }
        return ResponseResult.success();
    }

    @GetMapping("/enabled")
    public ResponseResult<List<DutyShiftConfig>> getEnabledShiftConfigs() {
        List<DutyShiftConfig> list = dutyShiftConfigService.getEnabledShifts();
        return ResponseResult.success(list);
    }
}
