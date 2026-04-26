package com.lasu.hyperduty.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.system.entity.SysScheduleLog;
import com.lasu.hyperduty.system.mapper.SysScheduleLogMapper;
import com.lasu.hyperduty.system.service.SysScheduleLogService;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;








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
    public int cleanLogs(int days) {
        QueryWrapper<SysScheduleLog> queryWrapper = new QueryWrapper<>();
        
        if (days > 0) {
            // 清理指定天数之前的日志
            LocalDateTime threshold = LocalDateTime.now().minusDays(days);
            // 同时考虑 start_time 和 create_time，确保能清理到所有指定天数之前的日志
            queryWrapper.and(wrapper -> wrapper
                .lt("start_time", threshold)
                .or()
                .lt("create_time", threshold)
            );
        } else {
            // 清理所有日志
            // 空条件，表示查询所有记录
        }
        
        // 先统计要删除的记录数
        long count = this.count(queryWrapper);
        // 然后执行删除操作
        this.remove(queryWrapper);
        return (int) count;
    }

    @Override
    public Page<SysScheduleLog> getLogList(int pageNum, int pageSize, Long jobId, String keyword) {
        QueryWrapper<SysScheduleLog> queryWrapper = new QueryWrapper<>();
        
        // 根据jobId过滤
        if (jobId != null) {
            queryWrapper.eq("job_id", jobId);
        }
        
        // 根据关键词搜索
        if (keyword != null && !keyword.isEmpty()) {
            queryWrapper.and(wrapper -> wrapper
                .like("job_name", keyword)
                .or()
                .like("job_group", keyword)
                .or()
                .like("job_code", keyword)
                .or()
                .like("error_msg", keyword)
            );
        }
        
        // 按开始时间倒序排序
        queryWrapper.orderByDesc("start_time");
        
        // 执行分页查询
        Page<SysScheduleLog> page = new Page<>(pageNum, pageSize);
        return this.page(page, queryWrapper);
    }

}
