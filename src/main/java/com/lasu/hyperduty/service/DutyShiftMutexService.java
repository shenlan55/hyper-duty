package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.DutyShiftMutex;

import java.util.List;

/**
 * 班次互斥关系服务接口
 */
public interface DutyShiftMutexService extends IService<DutyShiftMutex> {

    /**
     * 根据班次配置ID获取互斥班次配置ID列表
     * @param shiftConfigId 班次配置ID
     * @return 互斥班次配置ID列表
     */
    List<Long> getMutexShiftIdsByShiftConfigId(Long shiftConfigId);

    /**
     * 保存班次互斥关系
     * @param shiftConfigId 班次配置ID
     * @param mutexShiftIds 互斥班次ID列表
     */
    void saveMutexRelations(Long shiftConfigId, List<?> mutexShiftIds);

    /**
     * 删除班次的所有互斥关系
     * @param shiftConfigId 班次配置ID
     */
    void deleteByShiftConfigId(Long shiftConfigId);

    /**
     * 检查两个班次是否互斥
     * @param shiftConfigId1 班次配置ID1
     * @param shiftConfigId2 班次配置ID2
     * @return 是否互斥
     */
    boolean checkIfMutex(Long shiftConfigId1, Long shiftConfigId2);

}