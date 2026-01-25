package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyShiftConfig;

import java.util.List;

public interface DutyShiftConfigService extends IService<DutyShiftConfig> {

    /**
     * 获取启用的班次配置
     * @return 启用的班次配置列表
     */
    List<DutyShiftConfig> getEnabledShifts();

}
