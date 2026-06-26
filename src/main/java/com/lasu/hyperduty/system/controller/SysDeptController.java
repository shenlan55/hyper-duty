package com.lasu.hyperduty.system.controller;

import com.lasu.hyperduty.common.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.system.entity.SysDept;
import com.lasu.hyperduty.system.service.SysDeptService;
import java.util.List;
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
import org.springframework.web.bind.annotation.RestController;








@RestController
@RequestMapping("/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 获取所有部门列表（全量，包括禁用）—— 系统管理用
     */
    @GetMapping("/list")
    public ResponseResult<List<SysDept>> getAllDepts() {
        List<SysDept> deptList = sysDeptService.getAllDepts();
        return ResponseResult.success(deptList);
    }

    /**
     * 获取部门树结构（全量，包括禁用）—— 系统管理用
     */
    @GetMapping("/tree")
    public ResponseResult<List<SysDept>> getDeptTree() {
        List<SysDept> deptTree = sysDeptService.getDeptTree();
        return ResponseResult.success(deptTree);
    }

    /**
     * 获取启用部门列表（status=1）—— 业务模块选人/选部门用
     * 2026-06-27 新增：双接口方案
     */
    @GetMapping("/active-list")
    public ResponseResult<List<SysDept>> getActiveDepts() {
        List<SysDept> activeDeptList = sysDeptService.getActiveDepts();
        return ResponseResult.success(activeDeptList);
    }

    /**
     * 获取启用部门树（status=1）—— 业务模块选人用
     * 2026-06-27 新增：双接口方案
     */
    @GetMapping("/active-tree")
    public ResponseResult<List<SysDept>> getActiveDeptTree() {
        List<SysDept> activeDeptTree = sysDeptService.getActiveDeptTree();
        return ResponseResult.success(activeDeptTree);
    }

    /**
     * 添加部门
     */
    @PostMapping
    @RateLimit(window = 60, max = 20, message = "添加部门操作过于频繁，请60秒后再试")
    public ResponseResult<Void> addDept(@Validated @RequestBody SysDept sysDept) {
        sysDeptService.save(sysDept);
        return ResponseResult.success();
    }

    /**
     * 修改部门
     */
    @PutMapping
    @RateLimit(window = 60, max = 20, message = "修改部门操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateDept(@Validated @RequestBody SysDept sysDept) {
        sysDeptService.updateById(sysDept);
        return ResponseResult.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    @RateLimit(window = 60, max = 20, message = "删除部门操作过于频繁，请60秒后再试")
    public ResponseResult<Void> deleteDept(@PathVariable Long id) {
        sysDeptService.removeById(id);
        return ResponseResult.success();
    }

}