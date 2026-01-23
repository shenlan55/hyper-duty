package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.mapper.DutyShiftConfigMapper;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DutyShiftConfigServiceImpl extends ServiceImpl<DutyShiftConfigMapper, DutyShiftConfig> implements DutyShiftConfigService {

    /**
     * 获取启用的班次配置
     * @return 启用的班次配置列表
     */
    @Override
    public List<DutyShiftConfig> getEnabledShifts() {
        LambdaQueryWrapper<DutyShiftConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DutyShiftConfig::getStatus, 1); // 1 表示启用状态
        queryWrapper.orderByAsc(DutyShiftConfig::getSort); // 按排序字段升序排列
        return this.list(queryWrapper);
    }

}
