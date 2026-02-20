package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SysEmployee;
import com.lasu.hyperduty.service.SysEmployeeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employee")
public class SysEmployeeController {

    @Autowired
    private SysEmployeeService sysEmployeeService;

    /**
     * 获取所有人员列表
     */
    @GetMapping("/list")
    public ResponseResult<List<SysEmployee>> getAllEmployees() {
        List<SysEmployee> employeeList = sysEmployeeService.getAllEmployees();
        return ResponseResult.success(employeeList);
    }

    /**
     * 根据部门ID获取人员列表
     */
    @GetMapping("/list/{deptId}")
    public ResponseResult<List<SysEmployee>> getEmployeesByDeptId(@PathVariable Long deptId) {
        List<SysEmployee> employeeList = sysEmployeeService.getEmployeesByDeptId(deptId);
        return ResponseResult.success(employeeList);
    }

    /**
     * 添加人员
     */
    @PostMapping
    @RateLimit(window = 60, max = 20, message = "添加人员操作过于频繁，请60秒后再试")
    public ResponseResult<Void> addEmployee(@Validated @RequestBody SysEmployee sysEmployee) {
        sysEmployeeService.save(sysEmployee);
        return ResponseResult.success();
    }

    /**
     * 修改人员
     */
    @PutMapping
    @RateLimit(window = 60, max = 20, message = "修改人员操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateEmployee(@Validated @RequestBody SysEmployee sysEmployee) {
        sysEmployeeService.updateById(sysEmployee);
        return ResponseResult.success();
    }

    /**
     * 删除人员
     */
    @DeleteMapping("/{id}")
    @RateLimit(window = 60, max = 20, message = "删除人员操作过于频繁，请60秒后再试")
    public ResponseResult<Void> deleteEmployee(@PathVariable Long id) {
        sysEmployeeService.removeById(id);
        return ResponseResult.success();
    }

}