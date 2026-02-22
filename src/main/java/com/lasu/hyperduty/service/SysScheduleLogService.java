package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.SysScheduleLog;

/**
 * 定时任务日志Service接口
 */
public interface SysScheduleLogService extends IService<SysScheduleLog> {

    /**
     * 保存定时任务执行日志
     * @param log 执行日志
     * @return 保存结果
     */
    boolean saveLog(SysScheduleLog log);

    /**
     * 清理指定时间之前的日志
     * @param days 天数
     * @return 删除的记录数
     */
    int cleanLogs(int days);

    /**
     * 获取定时任务日志列表（分页）
     * @param pageNum 页码
     * @param pageSize 每页大小
     * @param jobId 任务ID（可选）
     * @param keyword 搜索关键词（可选）
     * @return 日志列表
     */
    Page<SysScheduleLog> getLogList(int pageNum, int pageSize, Long jobId, String keyword);

}
