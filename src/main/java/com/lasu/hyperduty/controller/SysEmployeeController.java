package com.lasu.hyperduty.controller;

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
    public ResponseResult<Void> addEmployee(@Validated @RequestBody SysEmployee sysEmployee) {
        sysEmployeeService.save(sysEmployee);
        return ResponseResult.success();
    }

    /**
     * 修改人员
     */
    @PutMapping
    public ResponseResult<Void> updateEmployee(@Validated @RequestBody SysEmployee sysEmployee) {
        sysEmployeeService.updateById(sysEmployee);
        return ResponseResult.success();
    }

    /**
     * 删除人员
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteEmployee(@PathVariable Long id) {
        sysEmployeeService.removeById(id);
        return ResponseResult.success();
    }

}