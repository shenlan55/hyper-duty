package com.lasu.hyperduty.duty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.common.service.impl.CacheableServiceImpl;
import com.lasu.hyperduty.duty.entity.DutyScheduleMode;
import com.lasu.hyperduty.duty.mapper.DutyScheduleModeMapper;
import com.lasu.hyperduty.duty.service.DutyScheduleModeService;
import com.lasu.hyperduty.common.utils.CacheUtil;
import java.util.List;
import java.util.Map;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;








/**
 * 排班模式服务实现类
 */
@Service
public class DutyScheduleModeServiceImpl extends CacheableServiceImpl<DutyScheduleModeMapper, DutyScheduleMode>
        implements DutyScheduleModeService {

    private ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Cacheable(value = "scheduleMode", key = "'enabledModes'")
    public List<DutyScheduleMode> getEnabledModes() {
        QueryWrapper<DutyScheduleMode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("status", 1);
        queryWrapper.orderByAsc("sort");
        return this.list(queryWrapper);
    }

    @Override
    @Cacheable(value = "scheduleMode", key = "'allWithSort'")
    public List<DutyScheduleMode> getAllModesWithSort() {
        QueryWrapper<DutyScheduleMode> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByAsc("sort");
        return this.list(queryWrapper);
    }

    @Override
    @Cacheable(value = "scheduleMode", key = "'byCode_' + #modeCode")
    public DutyScheduleMode getByCode(String modeCode) {
        QueryWrapper<DutyScheduleMode> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("mode_code", modeCode);
        return this.getOne(queryWrapper);
    }

    @Override
    @Cacheable(value = "scheduleMode", key = "'config_' + #modeId")
    public Map<String, Object> getModeConfig(Long modeId) {
        DutyScheduleMode mode = this.getById(modeId);
        if (mode == null || mode.getConfigJson() == null) {
            return null;
        }

        try {
            return objectMapper.readValue(mode.getConfigJson(), Map.class);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    @Override
    public boolean saveModeConfig(Long modeId, Map<String, Object> configParams) {
        DutyScheduleMode mode = this.getById(modeId);
        if (mode == null) {
            return false;
        }

        try {
            String configJson = objectMapper.writeValueAsString(configParams);
            mode.setConfigJson(configJson);
            boolean result = this.updateById(mode);
            if (result) {
                // 清除所有排班模式缓存
                clearAllScheduleModeCache();
            }
            return result;
        } catch (JsonProcessingException e) {
            return false;
        }
    }

    /**
     * 清除所有排班模式缓存
     */
    private void clearAllScheduleModeCache() {
        // 清除所有排班模式缓存
        CacheUtil.deleteByPattern("scheduleMode::*");
    }

    @Override
    protected void clearCache(DutyScheduleMode entity) {
        // 清除所有排班模式缓存
        clearAllScheduleModeCache();
    }

    @Override
    public boolean save(DutyScheduleMode entity) {
        boolean result = super.save(entity);
        if (result) {
            // 清除所有排班模式缓存
            clearAllScheduleModeCache();
        }
        return result;
    }

    @Override
    public boolean updateById(DutyScheduleMode entity) {
        boolean result = super.updateById(entity);
        if (result) {
            // 清除所有排班模式缓存
            clearAllScheduleModeCache();
        }
        return result;
    }

    @Override
    public boolean removeById(java.io.Serializable id) {
        boolean result = super.removeById(id);
        if (result) {
            // 清除所有排班模式缓存
            clearAllScheduleModeCache();
        }
        return result;
    }

}