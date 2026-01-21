package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutyScheduleMode;
import com.lasu.hyperduty.service.DutyScheduleModeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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
     * 根据ID获取排班模式
     * @param id 排班模式ID
     * @return 排班模式信息
     */
    @GetMapping("/getById")
    public ResponseResult<DutyScheduleMode> getScheduleModeById(Long id) {
        DutyScheduleMode mode = dutyScheduleModeService.getById(id);
        return ResponseResult.success(mode);
    }
}
