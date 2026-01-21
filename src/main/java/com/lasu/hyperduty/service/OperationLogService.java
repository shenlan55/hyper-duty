package com.lasu.hyperduty.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lasu.hyperduty.entity.OperationLog;

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
     * 统计今日登录次数
     * @param today 今天日期
     * @return 今日登录次数
     */
    long countTodayLogin(LocalDate today);
}
