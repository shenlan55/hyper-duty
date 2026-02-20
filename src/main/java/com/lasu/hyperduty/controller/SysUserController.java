package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.UserVO;
import com.lasu.hyperduty.entity.SysUser;
import com.lasu.hyperduty.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取所有用户列表
     */
    @GetMapping("/list")
    public ResponseResult<List<UserVO>> getAllUsers() {
        List<UserVO> userList = sysUserService.getAllUsers();
        return ResponseResult.success(userList);
    }

    /**
     * 添加用户
     */
    @PostMapping
    @RateLimit(window = 60, max = 20, message = "添加用户操作过于频繁，请60秒后再试")
    public ResponseResult<Void> addUser(@Validated @RequestBody SysUser sysUser) {
        sysUserService.save(sysUser);
        return ResponseResult.success();
    }

    /**
     * 修改用户
     */
    @PutMapping
    @RateLimit(window = 60, max = 20, message = "修改用户操作过于频繁，请60秒后再试")
    public ResponseResult<Void> updateUser(@Validated @RequestBody SysUser sysUser) {
        sysUserService.updateById(sysUser);
        return ResponseResult.success();
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    @RateLimit(window = 60, max = 20, message = "删除用户操作过于频繁，请60秒后再试")
    public ResponseResult<Void> deleteUser(@PathVariable Long id) {
        sysUserService.removeById(id);
        return ResponseResult.success();
    }
}
