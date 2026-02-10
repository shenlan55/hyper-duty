package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyScheduleShift;

import java.util.List;

/**
 * 值班表班次关联服务接口
 */
public interface DutyScheduleShiftService extends IService<DutyScheduleShift> {

    /**
     * 根据值班表ID获取班次列表
     * @param scheduleId 值班表ID
     * @return 班次配置ID列表
     */
    List<Long> getShiftConfigIdsByScheduleId(Long scheduleId);

    /**
     * 保存值班表班次关联
     * @param scheduleId 值班表ID
     * @param shiftConfigIds 班次配置ID列表
     */
    void saveScheduleShifts(Long scheduleId, List<Long> shiftConfigIds);

    /**
     * 删除值班表班次关联
     * @param scheduleId 值班表ID
     */
    void deleteByScheduleId(Long scheduleId);

}
