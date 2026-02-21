package com.lasu.hyperduty.controller;

import com.lasu.hyperduty.annotation.RateLimit;
import com.lasu.hyperduty.common.ResponseResult;
import com.lasu.hyperduty.dto.UserVO;
import com.lasu.hyperduty.dto.PageRequestDTO;
import com.lasu.hyperduty.dto.PageResponseDTO;
import com.lasu.hyperduty.entity.SysUser;
import com.lasu.hyperduty.service.SysUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/user")
public class SysUserController {

    @Autowired
    private SysUserService sysUserService;

    /**
     * 获取所有用户列表（分页）
     */
    @GetMapping("/list")
    public ResponseResult<PageResponseDTO<UserVO>> getAllUsers(@ModelAttribute PageRequestDTO pageRequestDTO, 
                                                             @RequestParam(required = false) Long deptId) {
        // 构建查询参数
        Map<String, Object> params = new HashMap<>();
        if (deptId != null) {
            params.put("deptId", deptId);
        }
        
        // 调用服务方法进行分页查询
        PageResponseDTO<UserVO> userPage = sysUserService.pageUserVO(pageRequestDTO, params);
        return ResponseResult.success(userPage);
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
