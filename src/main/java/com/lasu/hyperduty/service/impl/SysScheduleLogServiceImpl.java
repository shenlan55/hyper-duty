package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.SysScheduleLog;
import com.lasu.hyperduty.mapper.SysScheduleLogMapper;
import com.lasu.hyperduty.service.SysScheduleLogService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 定时任务日志Service实现类
 */
@Service
public class SysScheduleLogServiceImpl extends ServiceImpl<SysScheduleLogMapper, SysScheduleLog> implements SysScheduleLogService {

    @Override
    @Transactional
    public boolean saveLog(SysScheduleLog log) {
        log.setCreateTime(LocalDateTime.now());
        return this.save(log);
    }

    @Override
    @Transactional
    public boolean cleanLogs(int days) {
        QueryWrapper<SysScheduleLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.lt("start_time", LocalDateTime.now().minusDays(days));
        return this.remove(queryWrapper);
    }

}
