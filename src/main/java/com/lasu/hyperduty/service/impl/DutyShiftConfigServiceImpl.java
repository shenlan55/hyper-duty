package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.DutyShiftConfig;
import com.lasu.hyperduty.mapper.DutyShiftConfigMapper;
import com.lasu.hyperduty.service.DutyShiftConfigService;
import com.lasu.hyperduty.service.DutyShiftMutexService;
import com.lasu.hyperduty.utils.CacheUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class DutyShiftConfigServiceImpl extends CacheableServiceImpl<DutyShiftConfigMapper, DutyShiftConfig> implements DutyShiftConfigService {

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
     * 获取带互斥班次信息的班次配置列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param keyword 搜索关键字
     * @return 带互斥班次信息的班次配置分页列表
     */
    @Override
    public Page<Map<String, Object>> getShiftConfigsWithMutex(Integer pageNum, Integer pageSize, String keyword) {
        // 创建分页对象
        Page<DutyShiftConfig> pagination = new Page<>(pageNum, pageSize);
        
        // 构建查询条件
        LambdaQueryWrapper<DutyShiftConfig> queryWrapper = new LambdaQueryWrapper<>();
        
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> 
                wrapper.like(DutyShiftConfig::getShiftName, keyword)
                       .or().like(DutyShiftConfig::getShiftCode, keyword)
                       .or().like(DutyShiftConfig::getRemark, keyword)
            );
        }
        
        // 按排序字段和创建时间倒序排序
        queryWrapper.orderByAsc(DutyShiftConfig::getSort)
                   .orderByDesc(DutyShiftConfig::getCreateTime);
        
        // 执行分页查询
        Page<DutyShiftConfig> pageResult = this.page(pagination, queryWrapper);
        
        // 转换为带互斥班次信息的结果
        Page<Map<String, Object>> resultPage = new Page<>(pageNum, pageSize, pageResult.getTotal());
        List<Map<String, Object>> records = new ArrayList<>();
        
        for (DutyShiftConfig config : pageResult.getRecords()) {
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

            records.add(map);
        }
        
        resultPage.setRecords(records);
        return resultPage;
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
     * 清除所有班次配置缓存
     */
    private void clearAllShiftConfigCache() {
        // 清除所有班次配置缓存
        CacheUtil.deleteByPattern("shiftConfig::*");
    }

    @Override
    protected void clearCache(DutyShiftConfig entity) {
        // 清除所有班次配置缓存
        clearAllShiftConfigCache();
    }

    /**
     * 保存班次配置
     * @param entity 班次配置实体
     * @return 是否保存成功
     */
    @Override
    public boolean save(DutyShiftConfig entity) {
        boolean result = super.save(entity);
        if (result) {
            // 清除所有班次配置缓存
            clearAllShiftConfigCache();
        }
        return result;
    }

    /**
     * 更新班次配置
     * @param entity 班次配置实体
     * @return 是否更新成功
     */
    @Override
    public boolean updateById(DutyShiftConfig entity) {
        boolean result = super.updateById(entity);
        if (result) {
            // 清除所有班次配置缓存
            clearAllShiftConfigCache();
        }
        return result;
    }

    /**
     * 删除班次配置
     * @param id 班次配置ID
     * @return 是否删除成功
     */
    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 清除所有班次配置缓存
            clearAllShiftConfigCache();
        }
        return result;
    }

    /**
     * 重载removeById方法，接收Long类型参数
     * @param id 班次配置ID
     * @return 是否删除成功
     */
    public boolean removeById(Long id) {
        return removeById((java.io.Serializable) id);
    }

}
