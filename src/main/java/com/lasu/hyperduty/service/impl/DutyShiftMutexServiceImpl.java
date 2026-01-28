package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyShiftMutex;
import com.lasu.hyperduty.mapper.DutyShiftMutexMapper;
import com.lasu.hyperduty.service.DutyShiftMutexService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 班次互斥关系服务实现类
 */
@Service
public class DutyShiftMutexServiceImpl extends ServiceImpl<DutyShiftMutexMapper, DutyShiftMutex> implements DutyShiftMutexService {

    @Override
    public List<Long> getMutexShiftIdsByShiftConfigId(Long shiftConfigId) {
        return baseMapper.selectMutexShiftIdsByShiftConfigId(shiftConfigId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void saveMutexRelations(Long shiftConfigId, List<?> mutexShiftIds) {
        // 先删除该班次的所有互斥关系
        deleteByShiftConfigId(shiftConfigId);

        // 如果没有互斥班次，直接返回
        if (mutexShiftIds == null || mutexShiftIds.isEmpty()) {
            return;
        }

        // 保存新的互斥关系
        for (Object mutexShiftIdObj : mutexShiftIds) {
            DutyShiftMutex mutex = new DutyShiftMutex();
            mutex.setShiftConfigId(shiftConfigId);
            
            // 处理类型转换
            if (mutexShiftIdObj instanceof Long) {
                mutex.setMutexShiftConfigId((Long) mutexShiftIdObj);
            } else if (mutexShiftIdObj instanceof Integer) {
                mutex.setMutexShiftConfigId(((Integer) mutexShiftIdObj).longValue());
            } else if (mutexShiftIdObj instanceof String) {
                try {
                    mutex.setMutexShiftConfigId(Long.parseLong((String) mutexShiftIdObj));
                } catch (NumberFormatException e) {
                    continue; // 跳过无效的ID
                }
            } else {
                continue; // 跳过无效的类型
            }
            
            save(mutex);
        }
    }

    @Override
    public void deleteByShiftConfigId(Long shiftConfigId) {
        baseMapper.deleteByShiftConfigId(shiftConfigId);
    }

    @Override
    public boolean checkIfMutex(Long shiftConfigId1, Long shiftConfigId2) {
        // 检查shiftConfigId1是否将shiftConfigId2设置为互斥班次
        List<Long> mutexShiftIds1 = getMutexShiftIdsByShiftConfigId(shiftConfigId1);
        if (mutexShiftIds1.contains(shiftConfigId2)) {
            return true;
        }

        // 检查shiftConfigId2是否将shiftConfigId1设置为互斥班次
        List<Long> mutexShiftIds2 = getMutexShiftIdsByShiftConfigId(shiftConfigId2);
        return mutexShiftIds2.contains(shiftConfigId1);
    }

}