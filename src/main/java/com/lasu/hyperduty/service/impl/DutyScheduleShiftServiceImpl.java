package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyScheduleShift;
import com.lasu.hyperduty.mapper.DutyScheduleShiftMapper;
import com.lasu.hyperduty.service.DutyScheduleShiftService;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 值班表班次关联服务实现类
 */
@Service
public class DutyScheduleShiftServiceImpl extends ServiceImpl<DutyScheduleShiftMapper, DutyScheduleShift> implements DutyScheduleShiftService {

    /**
     * 根据值班表ID获取班次列表
     * @param scheduleId 值班表ID
     * @return 班次配置ID列表
     */
    @Override
    public List<Long> getShiftConfigIdsByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<DutyScheduleShift> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DutyScheduleShift::getScheduleId, scheduleId)
                .orderByAsc(DutyScheduleShift::getSortOrder);
        return baseMapper.selectList(queryWrapper).stream()
                .map(DutyScheduleShift::getShiftConfigId)
                .toList();
    }

    /**
     * 保存值班表班次关联
     * @param scheduleId 值班表ID
     * @param shiftConfigIds 班次配置ID列表
     */
    @Override
    public void saveScheduleShifts(Long scheduleId, List<Long> shiftConfigIds) {
        // 先删除旧的关联关系
        deleteByScheduleId(scheduleId);
        
        // 保存新的关联关系
        if (shiftConfigIds != null && !shiftConfigIds.isEmpty()) {
            for (int i = 0; i < shiftConfigIds.size(); i++) {
                DutyScheduleShift scheduleShift = new DutyScheduleShift();
                scheduleShift.setScheduleId(scheduleId);
                scheduleShift.setShiftConfigId(shiftConfigIds.get(i));
                scheduleShift.setSortOrder(i);
                baseMapper.insert(scheduleShift);
            }
        }
    }

    /**
     * 删除值班表班次关联
     * @param scheduleId 值班表ID
     */
    @Override
    public void deleteByScheduleId(Long scheduleId) {
        LambdaQueryWrapper<DutyScheduleShift> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DutyScheduleShift::getScheduleId, scheduleId);
        baseMapper.delete(queryWrapper);
    }

}
