package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.service.SysDeptService;
import com.lasu.hyperduty.service.SysEmployeeService;
import com.lasu.hyperduty.service.SysUserService;
import com.lasu.hyperduty.service.OperationLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 系统统计控制器
 * 用于获取首页统计数据
 */
@RestController
@RequestMapping("/system/statistics")
public class SystemStatisticsController {

    @Autowired
    private SysDeptService sysDeptService;

    @Autowired
    private SysEmployeeService sysEmployeeService;

    @Autowired
    private OperationLogService operationLogService;

    /**
     * 获取首页统计数据
     * @return 统计数据
     */
    @GetMapping("/dashboard")
    public ResponseResult<Map<String, Object>> getDashboardStatistics() {
        Map<String, Object> statistics = new HashMap<>();

        // 获取部门总数
        long deptCount = sysDeptService.count();
        statistics.put("deptCount", deptCount);

        // 获取人员总数
        long employeeCount = sysEmployeeService.count();
        statistics.put("employeeCount", employeeCount);

        // 获取今日登录次数
        LocalDate today = LocalDate.now();
        long todayLoginCount = operationLogService.countTodayLogin(today);
        statistics.put("todayLoginCount", todayLoginCount);

        return ResponseResult.success(statistics);
    }
}
