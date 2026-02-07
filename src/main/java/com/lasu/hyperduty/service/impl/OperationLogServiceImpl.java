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
        // 显式设置创建时间为当前本地时间，避免MySQL默认的UTC时间导致的时区问题
        log.setCreateTime(LocalDateTime.now());
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
            queryWrapper.ge("create_time", startDate.atStartOfDay());
        }
        
        if (endDate != null) {
            queryWrapper.le("create_time", endDate.plusDays(1).atStartOfDay().minusSeconds(1));
        }
        
        queryWrapper.orderByDesc("create_time");
        return list(queryWrapper);
    }

    @Override
    public long countTodayLogin(LocalDate today) {
        // 统计今日登录的唯一用户数
        QueryWrapper<OperationLog> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("operation_type", "登录");
        queryWrapper.ge("create_time", today.atStartOfDay());
        queryWrapper.le("create_time", today.plusDays(1).atStartOfDay().minusSeconds(1));
        
        // 获取今日所有登录记录
        List<OperationLog> loginLogs = list(queryWrapper);
        
        // 使用Set去重，统计唯一用户数
        // 注意：对于operator_id为null的记录，我们使用operator_name来区分
        java.util.Set<String> uniqueUsers = new java.util.HashSet<>();
        for (OperationLog log : loginLogs) {
            if (log.getOperatorId() != null) {
                // 使用operator_id作为唯一标识
                uniqueUsers.add(log.getOperatorId().toString());
            } else if (log.getOperatorName() != null && !"未知用户".equals(log.getOperatorName())) {
                // 使用operator_name作为唯一标识（当operator_id为null时）
                uniqueUsers.add(log.getOperatorName());
            }
        }
        
        return uniqueUsers.size();
    }
}
