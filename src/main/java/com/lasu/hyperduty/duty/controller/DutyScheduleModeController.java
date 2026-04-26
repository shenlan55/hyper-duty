package com.lasu.hyperduty.duty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.duty.entity.DutyScheduleMode;
import com.lasu.hyperduty.duty.service.DutyScheduleModeService;
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
import org.springframework.web.bind.annotation.RestController;








/**
 * 排班模式控制器
 */
@RestController
@RequestMapping("/duty/schedule-mode")
public class DutyScheduleModeController {

    @Autowired
    private DutyScheduleModeService dutyScheduleModeService;

    /**
     * 获取所有启用的排班模式列表
     * @return 启用的排班模式列表
     */
    @GetMapping("/list")
    public ResponseResult<List<DutyScheduleMode>> getAllScheduleModes() {
        List<DutyScheduleMode> modeList = dutyScheduleModeService.getEnabledModes();
        return ResponseResult.success(modeList);
    }

    /**
     * 获取所有排班模式（包括禁用的）
     * @return 所有排班模式列表
     */
    @GetMapping("/all")
    public ResponseResult<List<DutyScheduleMode>> getAllModes() {
        List<DutyScheduleMode> modeList = dutyScheduleModeService.getAllModesWithSort();
        return ResponseResult.success(modeList);
    }

    /**
     * 根据ID获取排班模式
     * @param id 排班模式ID
     * @return 排班模式信息
     */
    @GetMapping("/getById")
    public ResponseResult<DutyScheduleMode> getScheduleModeById(Long id) {
        DutyScheduleMode mode = dutyScheduleModeService.getById(id);
        return ResponseResult.success(mode);
    }

    /**
     * 新增排班模式
     * @param mode 排班模式信息
     * @return 操作结果
     */
    @PostMapping
    public ResponseResult<?> addScheduleMode(@RequestBody DutyScheduleMode mode) {
        boolean success = dutyScheduleModeService.save(mode);
        return success ? ResponseResult.success() : ResponseResult.error("新增排班模式失败");
    }

    /**
     * 编辑排班模式
     * @param mode 排班模式信息
     * @return 操作结果
     */
    @PutMapping
    public ResponseResult<?> updateScheduleMode(@RequestBody DutyScheduleMode mode) {
        boolean success = dutyScheduleModeService.updateById(mode);
        return success ? ResponseResult.success() : ResponseResult.error("编辑排班模式失败");
    }

    /**
     * 删除排班模式
     * @param id 排班模式ID
     * @return 操作结果
     */
    @DeleteMapping("/{id}")
    public ResponseResult<?> deleteScheduleMode(@PathVariable Long id) {
        boolean success = dutyScheduleModeService.removeById(id);
        return success ? ResponseResult.success() : ResponseResult.error("删除排班模式失败");
    }
}
