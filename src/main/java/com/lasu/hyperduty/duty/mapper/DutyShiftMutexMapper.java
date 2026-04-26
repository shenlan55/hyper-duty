package com.lasu.hyperduty.duty.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lasu.hyperduty.duty.entity.DutyShiftMutex;
import com.lasu.hyperduty.duty.mapper.DutyShiftMutexMapper;
import java.util.List;








/**
 * 班次互斥关系Mapper
 */
public interface DutyShiftMutexMapper extends BaseMapper<DutyShiftMutex> {

    /**
     * 根据班次配置ID获取互斥班次配置ID列表
     * @param shiftConfigId 班次配置ID
     * @return 互斥班次配置ID列表
     */
    List<Long> selectMutexShiftIdsByShiftConfigId(Long shiftConfigId);

    /**
     * 根据班次配置ID删除所有互斥关系
     * @param shiftConfigId 班次配置ID
     * @return 删除的记录数
     */
    int deleteByShiftConfigId(Long shiftConfigId);

}