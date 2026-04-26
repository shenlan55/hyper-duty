package com.lasu.hyperduty.duty.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.PageResult;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.duty.entity.OperationLog;
import com.lasu.hyperduty.duty.service.OperationLogService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;








@RestController
@RequestMapping("/system/operation-log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @GetMapping("/list")
    public ResponseResult<Page<OperationLog>> getAllLogs(
            @RequestParam(required = false) String operatorName,
            @RequestParam(required = false) String operationType,
            @RequestParam(required = false) String operationModule,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate) {
        // 使用分页查询，直接在数据库层面处理
        Page<OperationLog> pageResult = operationLogService.searchLogsPage(
            operatorName, operationType, operationModule, startDate, endDate, pageNum, pageSize
        );
        
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
