package com.lasu.hyperduty.service;

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
     * @return 清理结果
     */
    boolean cleanLogs(int days);

}
