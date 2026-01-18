package com.lasu.hyperduty.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lasu.hyperduty.entity.OperationLog;
import com.lasu.hyperduty.mapper.OperationLogMapper;
import com.lasu.hyperduty.service.OperationLogService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Service
public class OperationLogServiceImpl extends ServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    @Override
    public void logOperation(Long operatorId, String operatorName, String operationType, 
                         String operationModule, String operationDesc, 
                         String requestMethod, String requestUrl, 
                         String requestParams, String responseResult,
                         String ipAddress, String userAgent, 
                         Integer executionTime, Integer status, String errorMsg) {
        OperationLog log = new OperationLog();
        log.setOperatorId(operatorId);
        log.setOperatorName(operatorName);
        log.setOperationType(operationType);
        log.setOperationModule(operationModule);
        log.setOperationDesc(operationDesc);
        log.setRequestMethod(requestMethod);
        log.setRequestUrl(requestUrl);
        log.setRequestParams(requestParams);
        log.setResponseResult(responseResult);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setExecutionTime(executionTime);
        log.setStatus(status);
        log.setErrorMsg(errorMsg);
        save(log);
    }

    @Override
    public List<OperationLog> getLogsByOperatorId(Long operatorId) {
        return lambdaQuery()
                .eq(OperationLog::getOperatorId, operatorId)
                .orderByDesc(OperationLog::getCreateTime)
                .list();
    }

    @Override
    public List<OperationLog> getLogsByOperationType(String operationType) {
        return lambdaQuery()
                .eq(OperationLog::getOperationType, operationType)
                .orderByDesc(OperationLog::getCreateTime)
                .list();
    }

    @Override
    public List<OperationLog> getLogsByOperationModule(String operationModule) {
        return lambdaQuery()
                .eq(OperationLog::getOperationModule, operationModule)
                .orderByDesc(OperationLog::getCreateTime)
                .list();
    }

    @Override
    public List<OperationLog> getLogsByDateRange(LocalDate startDate, LocalDate endDate) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.ge("create_time", LocalDateTime.of(startDate, LocalTime.MIN));
        queryWrapper.le("create_time", LocalDateTime.of(endDate, LocalTime.MAX));
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    @Override
    public List<OperationLog> searchLogs(Long operatorId, String operationType, 
                                       String operationModule, LocalDate startDate, LocalDate endDate) {
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        
        if (operatorId != null) {
            queryWrapper.eq("operator_id", operatorId);
        }
        if (operationType != null && !operationType.isEmpty()) {
            queryWrapper.eq("operation_type", operationType);
        }
        if (operationModule != null && !operationModule.isEmpty()) {
            queryWrapper.eq("operation_module", operationModule);
        }
        if (startDate != null) {
            queryWrapper.ge("create_time", LocalDateTime.of(startDate, LocalTime.MIN));
        }
        if (endDate != null) {
            queryWrapper.le("create_time", LocalDateTime.of(endDate, LocalTime.MAX));
        }
        
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }
}
