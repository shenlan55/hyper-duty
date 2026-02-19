package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lasu.hyperduty.entity.DutyScheduleMode;
import com.lasu.hyperduty.mapper.DutyScheduleModeMapper;
import com.lasu.hyperduty.service.DutyScheduleModeService;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 排班模式服务实现类
 */
@Service
public class DutyScheduleModeServiceImpl extends ServiceImpl<DutyScheduleModeMapper, DutyScheduleMode> 
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
    @CacheEvict(value = "scheduleMode", allEntries = true)
    public boolean saveModeConfig(Long modeId, Map<String, Object> configParams) {
        DutyScheduleMode mode = this.getById(modeId);
        if (mode == null) {
            return false;
        }

        try {
            String configJson = objectMapper.writeValueAsString(configParams);
            mode.setConfigJson(configJson);
            return this.updateById(mode);
        } catch (JsonProcessingException e) {
            return false;
        }
    }
}