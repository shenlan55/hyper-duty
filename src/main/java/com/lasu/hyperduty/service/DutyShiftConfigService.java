package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyShiftConfig;

import java.util.List;
import java.util.Map;

public interface DutyShiftConfigService extends IService<DutyShiftConfig> {

    /**
     * 获取启用的班次配置
     * @return 启用的班次配置列表
     */
    List<DutyShiftConfig> getEnabledShifts();

    /**
     * 获取带互斥班次信息的班次配置列表
     * @return 带互斥班次信息的班次配置列表
     */
    List<Map<String, Object>> getShiftConfigsWithMutex();

    /**
     * 根据ID获取带互斥班次信息的班次配置
     * @param id 班次配置ID
     * @return 带互斥班次信息的班次配置
     */
    Map<String, Object> getShiftConfigWithMutexById(Long id);

}
