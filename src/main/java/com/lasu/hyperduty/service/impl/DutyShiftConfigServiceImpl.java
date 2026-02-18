package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.mapper.DutyShiftConfigMapper;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import com.lasu.hyperduty.service.DutyShiftMutexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DutyShiftConfigServiceImpl extends ServiceImpl<DutyShiftConfigMapper, DutyShiftConfig> implements DutyShiftConfigService {

    @Autowired
    private DutyShiftMutexService dutyShiftMutexService;

    /**
     * 获取启用的班次配置
     * @return 启用的班次配置列表
     */
    @Override
    @Cacheable(value = "shiftConfig", key = "'enabledShifts'")
    public List<DutyShiftConfig> getEnabledShifts() {
        LambdaQueryWrapper<DutyShiftConfig> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DutyShiftConfig::getStatus, 1); // 1 表示启用状态
        queryWrapper.orderByAsc(DutyShiftConfig::getSort); // 按排序字段升序排列
        return this.list(queryWrapper);
    }

    /**
     * 获取带互斥班次信息的班次配置列表
     * @return 带互斥班次信息的班次配置列表
     */
    @Override
    @Cacheable(value = "shiftConfig", key = "'allWithMutex'")
    public List<Map<String, Object>> getShiftConfigsWithMutex() {
        List<DutyShiftConfig> shiftConfigs = this.list();
        List<Map<String, Object>> result = new ArrayList<>();

        for (DutyShiftConfig config : shiftConfigs) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", config.getId());
            map.put("shiftName", config.getShiftName());
            map.put("shiftCode", config.getShiftCode());
            map.put("shiftType", config.getShiftType());
            map.put("startTime", config.getStartTime());
            map.put("endTime", config.getEndTime());
            map.put("isCrossDay", config.getIsCrossDay());
            map.put("durationHours", config.getDurationHours());
            map.put("breakHours", config.getBreakHours());
            map.put("restDayRule", config.getRestDayRule());
            map.put("isOvertimeShift", config.getIsOvertimeShift());
            map.put("status", config.getStatus());
            map.put("sort", config.getSort());
            map.put("remark", config.getRemark());
            map.put("createTime", config.getCreateTime());
            map.put("updateTime", config.getUpdateTime());

            // 添加互斥班次ID列表
            List<Long> mutexShiftIds = dutyShiftMutexService.getMutexShiftIdsByShiftConfigId(config.getId());
            map.put("mutexShiftIds", mutexShiftIds);

            result.add(map);
        }

        return result;
    }

    /**
     * 根据ID获取带互斥班次信息的班次配置
     * @param id 班次配置ID
     * @return 带互斥班次信息的班次配置
     */
    @Override
    @Cacheable(value = "shiftConfig", key = "'withMutex_' + #id")
    public Map<String, Object> getShiftConfigWithMutexById(Long id) {
        DutyShiftConfig config = this.getById(id);
        if (config == null) {
            return null;
        }

        Map<String, Object> map = new HashMap<>();
        map.put("id", config.getId());
        map.put("shiftName", config.getShiftName());
        map.put("shiftCode", config.getShiftCode());
        map.put("shiftType", config.getShiftType());
        map.put("startTime", config.getStartTime());
        map.put("endTime", config.getEndTime());
        map.put("isCrossDay", config.getIsCrossDay());
        map.put("durationHours", config.getDurationHours());
        map.put("breakHours", config.getBreakHours());
        map.put("restDayRule", config.getRestDayRule());
        map.put("isOvertimeShift", config.getIsOvertimeShift());
        map.put("status", config.getStatus());
        map.put("sort", config.getSort());
        map.put("remark", config.getRemark());
        map.put("createTime", config.getCreateTime());
        map.put("updateTime", config.getUpdateTime());

        // 添加互斥班次ID列表
        List<Long> mutexShiftIds = dutyShiftMutexService.getMutexShiftIdsByShiftConfigId(config.getId());
        map.put("mutexShiftIds", mutexShiftIds);

        return map;
    }

    /**
     * 保存班次配置
     * @param entity 班次配置实体
     * @return 是否保存成功
     */
    @Override
    @CacheEvict(value = "shiftConfig", allEntries = true)
    public boolean save(DutyShiftConfig entity) {
        return super.save(entity);
    }

    /**
     * 更新班次配置
     * @param entity 班次配置实体
     * @return 是否更新成功
     */
    @Override
    @CacheEvict(value = "shiftConfig", allEntries = true)
    public boolean updateById(DutyShiftConfig entity) {
        return super.updateById(entity);
    }

    /**
     * 删除班次配置
     * @param id 班次配置ID
     * @return 是否删除成功
     */
    @CacheEvict(value = "shiftConfig", allEntries = true)
    public boolean removeById(Long id) {
        return super.removeById(id);
    }

}
