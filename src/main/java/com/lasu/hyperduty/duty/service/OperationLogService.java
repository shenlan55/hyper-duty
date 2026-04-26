package com.lasu.hyperduty.duty.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.duty.entity.OperationLog;
import com.lasu.hyperduty.duty.service.OperationLogService;
import java.time.LocalDate;
import java.util.List;








public interface OperationLogService extends IService<OperationLog> {

    void logOperation(Long operatorId, String operatorName, String operationType, 
                   String operationModule, String operationDesc, 
                   String requestMethod, String requestUrl, 
                   String requestParams, String responseResult,
                   String ipAddress, String userAgent, 
                   Integer executionTime, Integer status, String errorMsg);

    List<OperationLog> getLogsByOperatorId(Long operatorId);

    List<OperationLog> getLogsByOperationType(String operationType);

    List<OperationLog> getLogsByOperationModule(String operationModule);

    List<OperationLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate);

    List<OperationLog> searchLogs(Long operatorId, String operationType, 
                                 String operationModule, LocalDate startDate, LocalDate endDate);
    
    /**
     * 分页搜索日志
     * @param operatorName 操作人姓名
     * @param operationType 操作类型
     * @param operationModule 操作模块
     * @param startDate 开始日期
     * @param endDate 结束日期
     * @param page 页码
     * @param pageSize 每页大小
     * @return 分页结果
     */
    Page<OperationLog> searchLogsPage(
            String operatorName, 
            String operationType, 
            String operationModule, 
            LocalDate startDate, 
            LocalDate endDate, 
            Integer page, 
            Integer pageSize);

    /**
     * 统计今日登录次数
     * @param today 今天日期
     * @return 今日登录次数
     */
    long countTodayLogin(LocalDate today);
}
