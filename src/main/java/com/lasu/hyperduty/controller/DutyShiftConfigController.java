package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import com.lasu.hyperduty.service.DutyShiftMutexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/duty/shift-config")
public class DutyShiftConfigController {

    @Autowired
    private DutyShiftConfigService dutyShiftConfigService;

    @Autowired
    private DutyShiftMutexService dutyShiftMutexService;

    @GetMapping("/list")
    public ResponseResult<List<Map<String, Object>>> getAllShiftConfigs() {
        try {
            List<Map<String, Object>> list = dutyShiftConfigService.getShiftConfigsWithMutex();
            return ResponseResult.success(list);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseResult.error("获取班次配置列表失败: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseResult<Map<String, Object>> getShiftConfigById(@PathVariable Long id) {
        Map<String, Object> config = dutyShiftConfigService.getShiftConfigWithMutexById(id);
        return ResponseResult.success(config);
    }

    @PostMapping
    public ResponseResult<Void> addShiftConfig(@Validated @RequestBody Map<String, Object> shiftConfigMap) {
        // 提取基本信息
        DutyShiftConfig shiftConfig = new DutyShiftConfig();
        shiftConfig.setShiftName((String) shiftConfigMap.get("shiftName"));
        shiftConfig.setShiftCode((String) shiftConfigMap.get("shiftCode"));
        shiftConfig.setShiftType((Integer) shiftConfigMap.get("shiftType"));
        shiftConfig.setStartTime((String) shiftConfigMap.get("startTime"));
        shiftConfig.setEndTime((String) shiftConfigMap.get("endTime"));
        shiftConfig.setIsCrossDay((Integer) shiftConfigMap.get("isCrossDay"));
        // 处理 durationHours 类型转换
        Object durationHoursObj = shiftConfigMap.get("durationHours");
        if (durationHoursObj != null) {
            if (durationHoursObj instanceof java.math.BigDecimal) {
                shiftConfig.setDurationHours((java.math.BigDecimal) durationHoursObj);
            } else if (durationHoursObj instanceof Integer) {
                shiftConfig.setDurationHours(java.math.BigDecimal.valueOf((Integer) durationHoursObj));
            } else if (durationHoursObj instanceof Double) {
                shiftConfig.setDurationHours(java.math.BigDecimal.valueOf((Double) durationHoursObj));
            } else if (durationHoursObj instanceof Float) {
                shiftConfig.setDurationHours(java.math.BigDecimal.valueOf((Float) durationHoursObj));
            } else if (durationHoursObj instanceof String) {
                try {
                    shiftConfig.setDurationHours(new java.math.BigDecimal((String) durationHoursObj));
                } catch (NumberFormatException e) {
                    shiftConfig.setDurationHours(java.math.BigDecimal.ZERO);
                }
            } else {
                shiftConfig.setDurationHours(java.math.BigDecimal.ZERO);
            }
        }
        // 处理 breakHours 类型转换
        Object breakHoursObj = shiftConfigMap.get("breakHours");
        if (breakHoursObj != null) {
            if (breakHoursObj instanceof java.math.BigDecimal) {
                shiftConfig.setBreakHours((java.math.BigDecimal) breakHoursObj);
            } else if (breakHoursObj instanceof Integer) {
                shiftConfig.setBreakHours(java.math.BigDecimal.valueOf((Integer) breakHoursObj));
            } else if (breakHoursObj instanceof Double) {
                shiftConfig.setBreakHours(java.math.BigDecimal.valueOf((Double) breakHoursObj));
            } else if (breakHoursObj instanceof Float) {
                shiftConfig.setBreakHours(java.math.BigDecimal.valueOf((Float) breakHoursObj));
            } else if (breakHoursObj instanceof String) {
                try {
                    shiftConfig.setBreakHours(new java.math.BigDecimal((String) breakHoursObj));
                } catch (NumberFormatException e) {
                    shiftConfig.setBreakHours(java.math.BigDecimal.ZERO);
                }
            } else {
                shiftConfig.setBreakHours(java.math.BigDecimal.ZERO);
            }
        }
        shiftConfig.setRestDayRule((String) shiftConfigMap.get("restDayRule"));
        shiftConfig.setIsOvertimeShift((Integer) shiftConfigMap.get("isOvertimeShift"));
        shiftConfig.setStatus((Integer) shiftConfigMap.get("status"));
        shiftConfig.setSort((Integer) shiftConfigMap.get("sort"));
        shiftConfig.setRemark((String) shiftConfigMap.get("remark"));

        // 保存班次配置
        dutyShiftConfigService.save(shiftConfig);

        // 保存互斥班次关系
        List<Long> mutexShiftIds = (List<Long>) shiftConfigMap.get("mutexShiftIds");
        if (mutexShiftIds != null && !mutexShiftIds.isEmpty()) {
            dutyShiftMutexService.saveMutexRelations(shiftConfig.getId(), mutexShiftIds);
        }

        return ResponseResult.success();
    }

    @PutMapping
    public ResponseResult<Void> updateShiftConfig(@Validated @RequestBody Map<String, Object> shiftConfigMap) {
        // 提取基本信息
        Long id = ((Number) shiftConfigMap.get("id")).longValue();
        DutyShiftConfig shiftConfig = dutyShiftConfigService.getById(id);
        if (shiftConfig == null) {
            return ResponseResult.error("班次配置不存在");
        }

        shiftConfig.setShiftName((String) shiftConfigMap.get("shiftName"));
        shiftConfig.setShiftCode((String) shiftConfigMap.get("shiftCode"));
        shiftConfig.setShiftType((Integer) shiftConfigMap.get("shiftType"));
        shiftConfig.setStartTime((String) shiftConfigMap.get("startTime"));
        shiftConfig.setEndTime((String) shiftConfigMap.get("endTime"));
        shiftConfig.setIsCrossDay((Integer) shiftConfigMap.get("isCrossDay"));
        // 处理 durationHours 类型转换
        Object durationHoursObj = shiftConfigMap.get("durationHours");
        if (durationHoursObj != null) {
            if (durationHoursObj instanceof java.math.BigDecimal) {
                shiftConfig.setDurationHours((java.math.BigDecimal) durationHoursObj);
            } else if (durationHoursObj instanceof Integer) {
                shiftConfig.setDurationHours(java.math.BigDecimal.valueOf((Integer) durationHoursObj));
            } else if (durationHoursObj instanceof Double) {
                shiftConfig.setDurationHours(java.math.BigDecimal.valueOf((Double) durationHoursObj));
            } else if (durationHoursObj instanceof Float) {
                shiftConfig.setDurationHours(java.math.BigDecimal.valueOf((Float) durationHoursObj));
            } else if (durationHoursObj instanceof String) {
                try {
                    shiftConfig.setDurationHours(new java.math.BigDecimal((String) durationHoursObj));
                } catch (NumberFormatException e) {
                    shiftConfig.setDurationHours(java.math.BigDecimal.ZERO);
                }
            } else {
                shiftConfig.setDurationHours(java.math.BigDecimal.ZERO);
            }
        }
        // 处理 breakHours 类型转换
        Object breakHoursObj = shiftConfigMap.get("breakHours");
        if (breakHoursObj != null) {
            if (breakHoursObj instanceof java.math.BigDecimal) {
                shiftConfig.setBreakHours((java.math.BigDecimal) breakHoursObj);
            } else if (breakHoursObj instanceof Integer) {
                shiftConfig.setBreakHours(java.math.BigDecimal.valueOf((Integer) breakHoursObj));
            } else if (breakHoursObj instanceof Double) {
                shiftConfig.setBreakHours(java.math.BigDecimal.valueOf((Double) breakHoursObj));
            } else if (breakHoursObj instanceof Float) {
                shiftConfig.setBreakHours(java.math.BigDecimal.valueOf((Float) breakHoursObj));
            } else if (breakHoursObj instanceof String) {
                try {
                    shiftConfig.setBreakHours(new java.math.BigDecimal((String) breakHoursObj));
                } catch (NumberFormatException e) {
                    shiftConfig.setBreakHours(java.math.BigDecimal.ZERO);
                }
            } else {
                shiftConfig.setBreakHours(java.math.BigDecimal.ZERO);
            }
        }
        shiftConfig.setRestDayRule((String) shiftConfigMap.get("restDayRule"));
        shiftConfig.setIsOvertimeShift((Integer) shiftConfigMap.get("isOvertimeShift"));
        shiftConfig.setStatus((Integer) shiftConfigMap.get("status"));
        shiftConfig.setSort((Integer) shiftConfigMap.get("sort"));
        shiftConfig.setRemark((String) shiftConfigMap.get("remark"));

        // 更新班次配置
        dutyShiftConfigService.updateById(shiftConfig);

        // 保存互斥班次关系
        List<Long> mutexShiftIds = (List<Long>) shiftConfigMap.get("mutexShiftIds");
        dutyShiftMutexService.saveMutexRelations(id, mutexShiftIds);

        return ResponseResult.success();
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteShiftConfig(@PathVariable Long id) {
        // 删除班次的所有互斥关系
        dutyShiftMutexService.deleteByShiftConfigId(id);
        // 删除班次配置
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

    /**
     * 检查两个班次是否互斥
     * @param shiftConfigId1 班次配置ID1
     * @param shiftConfigId2 班次配置ID2
     * @return 是否互斥
     */
    @GetMapping("/check-mutex")
    public ResponseResult<Boolean> checkIfMutex(@RequestParam Long shiftConfigId1, @RequestParam Long shiftConfigId2) {
        boolean isMutex = dutyShiftMutexService.checkIfMutex(shiftConfigId1, shiftConfigId2);
        return ResponseResult.success(isMutex);
    }

}
