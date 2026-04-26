package com.lasu.hyperduty.system.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.dto.PageRequestDTO;
import com.lasu.hyperduty.common.dto.PageResponseDTO;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysEmployee;
import com.lasu.hyperduty.system.service.SysEmployeeService;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;








@RestController
@RequestMapping("/employee")
public class SysEmployeeController {

    @Autowired
    private SysEmployeeService sysEmployeeService;

    /**
     * 获取所有人员列表（分页）
     */
    @GetMapping("/list")
    public ResponseResult<PageResponseDTO<SysEmployee>> getAllEmployees(@ModelAttribute PageRequestDTO pageRequestDTO, 
                                                                      @RequestParam(required = false) Long deptId) {
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (deptId != null) {
            params.put("deptId", deptId);
        }
        
        // 调用服务方法进行分页查询
        PageResponseDTO<SysEmployee> employeePage = sysEmployeeService.page(pageRequestDTO, params);
        return ResponseResult.success(employeePage);
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