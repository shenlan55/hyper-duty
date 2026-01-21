package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.PageResult;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.OperationLog;
import com.lasu.hyperduty.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/system/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public ResponseResult<PageResult<OperationLog>> getAllLogs(
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) Integer page,
            @RequestParam(required = false) Integer pageSize,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        // 先获取所有符合条件的数据（除了操作人姓名）
        List<OperationLog> list = operationLogService.searchLogs(
            null, operationType, operationModule, startDate, endDate
        );
        
        // 如果提供了操作人姓名，进行过滤
        if (operatorName != null && !operatorName.isEmpty()) {
            list = list.stream()
                .filter(log -> log.getOperatorName() != null && log.getOperatorName().contains(operatorName))
                .collect(java.util.stream.Collectors.toList());
        }
        
        // 保存总记录数
        long total = list.size();
        
        // 处理分页
        if (page != null && pageSize != null && page > 0 && pageSize > 0) {
            int startIndex = (page - 1) * pageSize;
            int endIndex = Math.min(startIndex + pageSize, list.size());
            if (startIndex < list.size()) {
                list = list.subList(startIndex, endIndex);
            } else {
                list = java.util.Collections.emptyList();
            }
        }
        
        // 创建分页结果
        PageResult<OperationLog> pageResult = new PageResult<>(list, total);
        
        return ResponseResult.success(pageResult);
    }

    @GetMapping("/search")
    public ResponseResult<List<OperationLog>> searchLogs(
            @RequestParam(required = false) Long operatorId,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        List<OperationLog> list = operationLogService.searchLogs(
            operatorId, operationType, operationModule, startDate, endDate
        );
        return ResponseResult.success(list);
    }

    @GetMapping("/{id}")
    public ResponseResult<OperationLog> getLogById(@PathVariable Long id) {
        OperationLog log = operationLogService.getById(id);
        return ResponseResult.success(log);
    }

    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteLog(@PathVariable Long id) {
        operationLogService.removeById(id);
        return ResponseResult.success();
    }
}
