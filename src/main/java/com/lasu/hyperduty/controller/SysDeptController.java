package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.entity.SysDept;
import com.lasu.hyperduty.service.SysDeptService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/dept")
public class SysDeptController {

    @Autowired
    private SysDeptService sysDeptService;

    /**
     * 获取所有部门列表
     */
    @GetMapping("/list")
    public ResponseResult<List<SysDept>> getAllDepts() {
        List<SysDept> deptList = sysDeptService.getAllDepts();
        return ResponseResult.success(deptList);
    }

    /**
     * 获取部门树结构
     */
    @GetMapping("/tree")
    public ResponseResult<List<SysDept>> getDeptTree() {
        List<SysDept> deptTree = sysDeptService.getDeptTree();
        return ResponseResult.success(deptTree);
    }

    /**
     * 添加部门
     */
    @PostMapping
    public ResponseResult<Void> addDept(@Validated @RequestBody SysDept sysDept) {
        sysDeptService.save(sysDept);
        return ResponseResult.success();
    }

    /**
     * 修改部门
     */
    @PutMapping
    public ResponseResult<Void> updateDept(@Validated @RequestBody SysDept sysDept) {
        sysDeptService.updateById(sysDept);
        return ResponseResult.success();
    }

    /**
     * 删除部门
     */
    @DeleteMapping("/{id}")
    public ResponseResult<Void> deleteDept(@PathVariable Long id) {
        sysDeptService.removeById(id);
        return ResponseResult.success();
    }

}